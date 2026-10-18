package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.audio.SoundHapticManager
import com.example.data.*
import com.example.keyboard.*
import com.example.rgb.RgbAnimationEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RimuKeyboardView(
    settings: KeyboardSettings,
    soundHapticManager: SoundHapticManager?,
    onCharInput: (String) -> Unit,
    onDelete: () -> Unit,
    onEnter: () -> Unit,
    onSpace: () -> Unit,
    onVoiceInput: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSwitchIme: () -> Unit = {},
    currentWordPrefix: String = "",
    enterActionLabel: String = "↵",
    onSelectSuggestion: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var activePage by remember { mutableStateOf(KeyboardPage.LETTERS) }
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var activeLang by remember { mutableStateOf(settings.activeLanguage) }
    var selectedEmojiCategory by remember { mutableStateOf(EmojiCategory.SMILEYS) }
    val context = LocalContext.current
    val clipboardHelper = remember(context) { ClipboardManagerHelper.getInstance(context) }
    val clipboardItems by clipboardHelper.items.collectAsState()

    // RGB phase
    val phaseState = RgbAnimationEngine.rememberAnimatedRgbPhase(settings)
    val phase = phaseState.value

    val suggestions = remember(currentWordPrefix, activeLang, settings.suggestionsEnabled) {
        if (settings.suggestionsEnabled) {
            SuggestionEngine.getSuggestions(currentWordPrefix, activeLang)
        } else emptyList()
    }

    val themeBgColor = Color(settings.theme.bgColorHex)
    val cornerRadius = settings.keyShape.radiusDp.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(themeBgColor)
            .then(
                if (settings.rainbowBorder && settings.rgbMode != RgbMode.OFF) {
                    Modifier.border(
                        width = 2.dp,
                        brush = RgbAnimationEngine.getBorderBrush(settings, phase),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                } else Modifier
            )
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        // --- 1. Top Utility & Suggestion Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    Color(settings.theme.keyColorHex).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick icons: Mic, Clipboard, Settings, Language Badge
            IconButton(
                onClick = {
                    soundHapticManager?.performHapticFeedback(settings.vibrationEnabled, settings.vibrationIntensityMs)
                    onVoiceInput()
                },
                modifier = Modifier.size(36.dp).testTag("kb_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = Color(settings.rgbAccentColorHex),
                    modifier = Modifier.size(19.dp)
                )
            }

            IconButton(
                onClick = {
                    soundHapticManager?.performHapticFeedback(settings.vibrationEnabled, settings.vibrationIntensityMs)
                    clipboardHelper.checkSystemClipboard()
                    activePage = if (activePage == KeyboardPage.CLIPBOARD) KeyboardPage.LETTERS else KeyboardPage.CLIPBOARD
                },
                modifier = Modifier.size(36.dp).testTag("kb_clipboard_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Clipboard History",
                    tint = if (activePage == KeyboardPage.CLIPBOARD) Color(settings.rgbAccentColorHex) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(36.dp).testTag("kb_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Keyboard Settings",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Suggestions chips
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (suggestions.isEmpty()) {
                    Text(
                        text = "Rimu RGB Keyboard",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    suggestions.forEach { suggestion ->
                        Surface(
                            onClick = {
                                soundHapticManager?.playSound(settings.soundProfile, settings.soundVolume, settings.soundEnabled)
                                soundHapticManager?.performHapticFeedback(settings.vibrationEnabled, settings.vibrationIntensityMs)
                                onSelectSuggestion(suggestion)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(settings.rgbAccentColorHex).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(settings.rgbAccentColorHex).copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Language Indicator Chip
            Surface(
                onClick = {
                    soundHapticManager?.performHapticFeedback(settings.vibrationEnabled, settings.vibrationIntensityMs)
                    activeLang = if (activeLang == "EN") "BN" else "EN"
                },
                shape = RoundedCornerShape(10.dp),
                color = Color(settings.theme.keyColorHex),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(settings.rgbAccentColorHex).copy(alpha = 0.5f)),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = activeLang,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(settings.rgbAccentColorHex),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // --- 2. Keyboard Views by Page ---
        when (activePage) {
            KeyboardPage.CLIPBOARD -> {
                ClipboardView(
                    items = clipboardItems,
                    settings = settings,
                    onItemClick = { text ->
                        soundHapticManager?.playSound(settings.soundProfile, settings.soundVolume, settings.soundEnabled)
                        soundHapticManager?.performHapticFeedback(settings.vibrationEnabled, settings.vibrationIntensityMs)
                        onCharInput(text)
                        activePage = KeyboardPage.LETTERS
                    },
                    onTogglePin = { id -> clipboardHelper.togglePin(id) },
                    onDelete = { id -> clipboardHelper.deleteClip(id) },
                    onClearAll = { clipboardHelper.clearAll() },
                    onClose = { activePage = KeyboardPage.LETTERS }
                )
            }
            KeyboardPage.EMOJIS -> {
                EmojiView(
                    selectedCategory = selectedEmojiCategory,
                    onSelectCategory = { selectedEmojiCategory = it },
                    onEmojiClick = { emoji ->
                        soundHapticManager?.playSound(settings.soundProfile, settings.soundVolume, settings.soundEnabled)
                        soundHapticManager?.performHapticFeedback(settings.vibrationEnabled, settings.vibrationIntensityMs)
                        onCharInput(emoji)
                    },
                    onBackspace = {
                        soundHapticManager?.playSound(settings.soundProfile, settings.soundVolume, settings.soundEnabled)
                        soundHapticManager?.performHapticFeedback(settings.vibrationEnabled, settings.vibrationIntensityMs)
                        onDelete()
                    },
                    onBackToLetters = { activePage = KeyboardPage.LETTERS },
                    settings = settings
                )
            }
            else -> {
                // Regular Keyboard Rows (Letters, Symbols 1, Symbols 2)
                val rows = remember(activePage, activeLang, shiftState, settings.numberRowEnabled, enterActionLabel) {
                    when (activePage) {
                        KeyboardPage.SYMBOLS_1 -> KeyboardLayouts.getSymbols1Rows(enterActionLabel)
                        KeyboardPage.SYMBOLS_2 -> KeyboardLayouts.getSymbols2Rows(enterActionLabel)
                        else -> {
                            if (activeLang == "BN") {
                                KeyboardLayouts.getBanglaRows(shiftState, settings.numberRowEnabled, enterActionLabel)
                            } else {
                                KeyboardLayouts.getEnglishRows(shiftState, settings.numberRowEnabled, enterActionLabel)
                            }
                        }
                    }
                }

                val rowHeight = (46 * settings.heightScale).dp

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(settings.keySpacingDp.dp)
                ) {
                    rows.forEachIndexed { rowIndex, rowKeys ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight),
                            horizontalArrangement = Arrangement.spacedBy(settings.keySpacingDp.dp)
                        ) {
                            rowKeys.forEachIndexed { colIndex, keyModel ->
                                val relativeX = colIndex.toFloat() / rowKeys.size.toFloat()
                                val relativeY = rowIndex.toFloat() / rows.size.toFloat()

                                RimuKey(
                                    key = keyModel,
                                    settings = settings,
                                    phase = phase,
                                    relativeX = relativeX,
                                    relativeY = relativeY,
                                    modifier = Modifier.weight(keyModel.weight),
                                    onTap = {
                                        soundHapticManager?.playSound(settings.soundProfile, settings.soundVolume, settings.soundEnabled)
                                        soundHapticManager?.performHapticFeedback(settings.vibrationEnabled, settings.vibrationIntensityMs)

                                        when (keyModel.type) {
                                            KeyType.CHARACTER -> {
                                                onCharInput(keyModel.primaryText)
                                                if (shiftState == ShiftState.ONCE) {
                                                    shiftState = ShiftState.OFF
                                                }
                                            }
                                            KeyType.SHIFT -> {
                                                shiftState = when (shiftState) {
                                                    ShiftState.OFF -> ShiftState.ONCE
                                                    ShiftState.ONCE -> ShiftState.CAPS_LOCK
                                                    ShiftState.CAPS_LOCK -> ShiftState.OFF
                                                }
                                            }
                                            KeyType.BACKSPACE -> onDelete()
                                            KeyType.SPACE -> {
                                                onSpace()
                                                if (shiftState == ShiftState.ONCE) {
                                                    shiftState = ShiftState.OFF
                                                }
                                            }
                                            KeyType.ENTER -> onEnter()
                                            KeyType.MODE_SWITCH -> {
                                                activePage = when (keyModel.primaryText) {
                                                    "?123" -> KeyboardPage.SYMBOLS_1
                                                    "=\\<" -> KeyboardPage.SYMBOLS_2
                                                    "ABC" -> KeyboardPage.LETTERS
                                                    else -> KeyboardPage.LETTERS
                                                }
                                            }
                                            KeyType.LANG_SWITCH -> {
                                                activeLang = if (activeLang == "EN") "BN" else "EN"
                                            }
                                            KeyType.SWITCH_IME -> onSwitchIme()
                                            KeyType.EMOJI -> activePage = KeyboardPage.EMOJIS
                                            KeyType.CLIPBOARD -> {
                                                clipboardHelper.checkSystemClipboard()
                                                activePage = KeyboardPage.CLIPBOARD
                                            }
                                            KeyType.MIC -> onVoiceInput()
                                            KeyType.SETTINGS -> onOpenSettings()
                                        }
                                    },
                                    onLongPress = {
                                        if (keyModel.type == KeyType.LANG_SWITCH) {
                                            soundHapticManager?.performHapticFeedback(settings.vibrationEnabled, settings.vibrationIntensityMs)
                                            onSwitchIme()
                                        }
                                    },
                                    onSelectAlt = { alt ->
                                        soundHapticManager?.playSound(settings.soundProfile, settings.soundVolume, settings.soundEnabled)
                                        soundHapticManager?.performHapticFeedback(settings.vibrationEnabled, settings.vibrationIntensityMs)
                                        onCharInput(alt)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RimuKey(
    key: KeyModel,
    settings: KeyboardSettings,
    phase: Float,
    relativeX: Float,
    relativeY: Float,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onLongPress: () -> Unit = {},
    onSelectAlt: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }
    var showAltPopup by remember { mutableStateOf(false) }
    val reactiveAlpha = remember { Animatable(0f) }

    val glowColor = RgbAnimationEngine.getKeyGlowColor(
        settings = settings,
        phase = phase,
        keyRelativeX = relativeX,
        keyRelativeY = relativeY,
        isPressedOrReactive = isPressed || reactiveAlpha.value > 0.05f,
        reactiveAlpha = reactiveAlpha.value
    )

    val shape = RoundedCornerShape(settings.keyShape.radiusDp.dp)
    val isSpecial = key.type != KeyType.CHARACTER && key.type != KeyType.SPACE

    val baseKeyColor = if (isSpecial) {
        Color(settings.theme.keyColorHex).copy(alpha = 0.65f)
    } else {
        Color(settings.theme.keyColorHex)
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(shape)
            .background(baseKeyColor)
            .then(
                if (settings.rgbMode != RgbMode.OFF) {
                    Modifier.border(
                        width = if (isPressed || reactiveAlpha.value > 0.2f) 1.6.dp else 1.dp,
                        color = glowColor,
                        shape = shape
                    )
                } else Modifier
            )
            .pointerInput(key) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        if (settings.rgbMode == RgbMode.REACTIVE_KEY_PRESS) {
                            coroutineScope.launch {
                                reactiveAlpha.snapTo(1f)
                                reactiveAlpha.animateTo(0f, animationSpec = tween(400))
                            }
                        }
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onTap() },
                    onLongPress = {
                        if (key.altChars.isNotEmpty()) {
                            showAltPopup = true
                        } else {
                            onLongPress()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Label Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = key.primaryText,
                fontSize = if (key.type == KeyType.SPACE) (settings.keyFontSizeSp - 3).sp else settings.keyFontSizeSp.sp,
                fontWeight = if (key.type == KeyType.CHARACTER) FontWeight.Medium else FontWeight.SemiBold,
                color = when {
                    key.type == KeyType.SHIFT && (key.primaryText == "⇪" || key.primaryText == "⇧") -> Color(settings.rgbAccentColorHex)
                    key.type == KeyType.SPACE -> Color.White.copy(alpha = 0.6f)
                    isSpecial -> Color.White.copy(alpha = 0.9f)
                    else -> Color.White
                },
                textAlign = TextAlign.Center
            )

            // Secondary hint (like numbers or alt chars)
            if (key.secondaryText != null) {
                Text(
                    text = key.secondaryText,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Long-press alternate characters popup
        if (showAltPopup && key.altChars.isNotEmpty()) {
            Popup(
                alignment = Alignment.TopCenter,
                onDismissRequest = { showAltPopup = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(settings.theme.bgColorHex),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(settings.rgbAccentColorHex)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(bottom = 50.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        key.altChars.forEach { alt ->
                            Surface(
                                onClick = {
                                    showAltPopup = false
                                    onSelectAlt(alt)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(settings.theme.keyColorHex),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = alt,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmojiView(
    selectedCategory: EmojiCategory,
    onSelectCategory: (EmojiCategory) -> Unit,
    onEmojiClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onBackToLetters: () -> Unit,
    settings: KeyboardSettings
) {
    val emojis = KeyboardLayouts.EMOJI_COLLECTIONS[selectedCategory] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(horizontal = 4.dp)
    ) {
        // Category row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmojiCategory.values().forEach { cat ->
                val isSelected = cat == selectedCategory
                Surface(
                    onClick = { onSelectCategory(cat) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(settings.rgbAccentColorHex).copy(alpha = 0.25f) else Color.Transparent,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(settings.rgbAccentColorHex)) else null
                ) {
                    Text(
                        text = "${cat.icon} ${cat.title}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color(settings.rgbAccentColorHex) else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Emoji Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 40.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEmojiClick(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }
        }

        // Bottom control row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackToLetters,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(settings.theme.keyColorHex))
            ) {
                Text("ABC", fontWeight = FontWeight.Bold, color = Color.White)
            }

            IconButton(
                onClick = onBackspace,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(settings.theme.keyColorHex), RoundedCornerShape(10.dp))
            ) {
                Text("⌫", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun ClipboardView(
    items: List<ClipboardItem>,
    settings: KeyboardSettings,
    onItemClick: (String) -> Unit,
    onTogglePin: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = null,
                    tint = Color(settings.rgbAccentColorHex),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Clipboard History",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (items.isNotEmpty()) {
                    TextButton(
                        onClick = onClearAll,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Clear All", fontSize = 11.sp, color = Color.Red.copy(alpha = 0.8f))
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No clipboard items yet.\nCopied text will appear here locally.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(items) { item ->
                    Surface(
                        onClick = { onItemClick(item.text) },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(settings.theme.keyColorHex),
                        border = if (item.isPinned) androidx.compose.foundation.BorderStroke(1.dp, Color(settings.rgbAccentColorHex)) else null
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (item.isPinned) "📌 Pinned" else "Clip",
                                    fontSize = 10.sp,
                                    color = if (item.isPinned) Color(settings.rgbAccentColorHex) else Color.White.copy(alpha = 0.4f)
                                )
                                Row {
                                    IconButton(
                                        onClick = { onTogglePin(item.id) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Text(if (item.isPinned) "★" else "☆", fontSize = 12.sp, color = Color.White)
                                    }
                                    IconButton(
                                        onClick = { onDelete(item.id) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Text("×", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                                    }
                                }
                            }
                            Text(
                                text = item.text,
                                fontSize = 12.sp,
                                color = Color.White,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}
