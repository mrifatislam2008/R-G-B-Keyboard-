package com.example.ime

import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.speech.RecognizerIntent
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.MainActivity
import com.example.audio.SoundHapticManager
import com.example.data.KeyboardPreferences
import com.example.ui.keyboard.RimuKeyboardView

/**
 * Core Android InputMethodService for Rimu Keyboard.
 * Handles system communication, lifecycle, input events, editor actions,
 * autofill, subtypes, and secure text entry across all Android apps.
 */
class RimuInputMethodService : InputMethodService() {

    private lateinit var preferences: KeyboardPreferences
    private lateinit var soundHapticManager: SoundHapticManager
    private var imeLifecycleOwner: ImeLifecycleOwner? = null

    private val currentPrefixState = mutableStateOf("")
    private val enterActionLabelState = mutableStateOf("↵")
    private val isPasswordFieldState = mutableStateOf(false)
    private val activeSubtypeLangState = mutableStateOf("EN")

    override fun onCreate() {
        super.onCreate()
        preferences = KeyboardPreferences.getInstance(this)
        soundHapticManager = SoundHapticManager(this)
    }

    override fun onCreateInputView(): View {
        val lifecycleOwner = ImeLifecycleOwner()
        imeLifecycleOwner = lifecycleOwner

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            lifecycleOwner.attachToDecor(this)

            setContent {
                val settings by preferences.settings.collectAsState()
                val currentPrefix by currentPrefixState
                val enterActionLabel by enterActionLabelState
                val isPassword by isPasswordFieldState

                // Merge active subtype language if changed externally
                val effectiveSettings = if (settings.activeLanguage != activeSubtypeLangState.value) {
                    settings.copy(activeLanguage = activeSubtypeLangState.value)
                } else settings

                RimuKeyboardView(
                    settings = effectiveSettings,
                    soundHapticManager = soundHapticManager,
                    onCharInput = { char ->
                        handleCharInput(char)
                    },
                    onDelete = {
                        handleDelete()
                    },
                    onEnter = {
                        handleEnter()
                    },
                    onSpace = {
                        handleSpace()
                    },
                    onVoiceInput = {
                        launchVoiceInput()
                    },
                    onOpenSettings = {
                        openSettingsActivity()
                    },
                    onSwitchIme = {
                        switchToNextIme()
                    },
                    currentWordPrefix = if (isPassword) "" else currentPrefix,
                    enterActionLabel = enterActionLabel,
                    onSelectSuggestion = { word ->
                        replaceCurrentPrefixWithWord(word)
                    }
                )
            }
        }

        return composeView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        inspectEditorInfo(attribute)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        imeLifecycleOwner?.onStart()
        inspectEditorInfo(info)
        updateWordPrefix()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        imeLifecycleOwner?.onStop()
        currentPrefixState.value = ""
    }

    override fun onFinishInput() {
        super.onFinishInput()
        currentPrefixState.value = ""
    }

    override fun onWindowShown() {
        super.onWindowShown()
        imeLifecycleOwner?.onStart()
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        imeLifecycleOwner?.onStop()
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        updateWordPrefix()
    }

    override fun onCurrentInputMethodSubtypeChanged(newSubtype: InputMethodSubtype?) {
        super.onCurrentInputMethodSubtypeChanged(newSubtype)
        val locale = newSubtype?.locale ?: ""
        if (locale.startsWith("bn", ignoreCase = true)) {
            activeSubtypeLangState.value = "BN"
            preferences.updateSettings { it.copy(activeLanguage = "BN") }
        } else if (locale.startsWith("en", ignoreCase = true)) {
            activeSubtypeLangState.value = "EN"
            preferences.updateSettings { it.copy(activeLanguage = "EN") }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isInputViewShown) {
                requestHideSelf(0)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun inspectEditorInfo(info: EditorInfo?) {
        if (info == null) {
            enterActionLabelState.value = "↵"
            isPasswordFieldState.value = false
            return
        }

        // 1. Password detection for privacy protection
        val inputType = info.inputType
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val isPassword = when (variation) {
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            InputType.TYPE_NUMBER_VARIATION_PASSWORD -> true
            else -> false
        }
        isPasswordFieldState.value = isPassword

        // 2. Action label resolution (Search, Send, Go, Next, Done, Enter)
        val action = info.imeOptions and EditorInfo.IME_MASK_ACTION
        enterActionLabelState.value = when (action) {
            EditorInfo.IME_ACTION_SEARCH -> "🔍"
            EditorInfo.IME_ACTION_SEND -> "➤"
            EditorInfo.IME_ACTION_GO -> "➔"
            EditorInfo.IME_ACTION_NEXT -> "⇥"
            EditorInfo.IME_ACTION_DONE -> "✓"
            else -> {
                if (info.actionLabel != null && info.actionLabel.isNotEmpty()) {
                    info.actionLabel.toString()
                } else {
                    "↵"
                }
            }
        }
    }

    private fun handleCharInput(char: String) {
        val conn = currentInputConnection ?: return
        conn.commitText(char, 1)
        updateWordPrefix()
    }

    private fun handleDelete() {
        val conn = currentInputConnection ?: return
        val selectedText = conn.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            conn.commitText("", 1)
        } else {
            conn.deleteSurroundingText(1, 0)
        }
        updateWordPrefix()
    }

    private fun handleEnter() {
        val conn = currentInputConnection ?: return
        val info = currentInputEditorInfo
        val action = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        if (action != null && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            conn.performEditorAction(action)
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
        currentPrefixState.value = ""
    }

    private fun handleSpace() {
        val conn = currentInputConnection ?: return
        conn.commitText(" ", 1)
        currentPrefixState.value = ""
    }

    private fun updateWordPrefix() {
        if (isPasswordFieldState.value) {
            currentPrefixState.value = ""
            return
        }

        val text = currentInputConnection?.getTextBeforeCursor(40, 0)?.toString() ?: ""
        if (text.isEmpty() || text.endsWith(" ") || text.endsWith("\n") || text.endsWith("\t")) {
            currentPrefixState.value = ""
        } else {
            val lastWord = text.split(Regex("[\\s,;:.!?]+")).lastOrNull() ?: ""
            currentPrefixState.value = lastWord
        }
    }

    private fun replaceCurrentPrefixWithWord(word: String) {
        val conn = currentInputConnection ?: return
        val currentPrefix = currentPrefixState.value
        conn.beginBatchEdit()
        try {
            if (currentPrefix.isNotEmpty()) {
                conn.deleteSurroundingText(currentPrefix.length, 0)
            }
            conn.commitText("$word ", 1)
        } finally {
            conn.endBatchEdit()
        }
        currentPrefixState.value = ""
    }

    private fun switchToNextIme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                if (shouldOfferSwitchingToNextInputMethod()) {
                    switchToNextInputMethod(false)
                    return
                }
            } catch (_: Exception) {}
        }
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }

    private fun launchVoiceInput() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to type with Rimu Keyboard...")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {
            // Speech recognizer intent fallback
        }
    }

    private fun openSettingsActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        imeLifecycleOwner?.onDestroy()
        soundHapticManager.release()
    }
}
