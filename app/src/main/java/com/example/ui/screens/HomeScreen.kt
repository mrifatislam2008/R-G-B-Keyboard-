package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundHapticManager
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardSettings
import com.example.data.RgbMode
import com.example.rgb.RgbAnimationEngine
import com.example.ui.keyboard.RimuKeyboardView

@Composable
fun HomeScreen(
    settings: KeyboardSettings,
    preferences: KeyboardPreferences,
    soundHapticManager: SoundHapticManager,
    onNavigateToRgb: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(checkIsImeEnabled(context)) }
    var isSelected by remember { mutableStateOf(checkIsImeSelected(context)) }
    var testText by remember { mutableStateOf("") }

    // RGB phase for border
    val phaseState = RgbAnimationEngine.rememberAnimatedRgbPhase(settings)

    LaunchedEffect(Unit) {
        isEnabled = checkIsImeEnabled(context)
        isSelected = checkIsImeSelected(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Setup Guide Card ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("setup_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(settings.rgbAccentColorHex).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Setup Rimu Keyboard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isEnabled && isSelected) "Ready to type with RGB effects!" else "Follow steps below to activate",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(
                        onClick = {
                            isEnabled = checkIsImeEnabled(context)
                            isSelected = checkIsImeSelected(context)
                        },
                        modifier = Modifier.size(36.dp).testTag("refresh_status_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Status",
                            tint = Color(settings.rgbAccentColorHex)
                        )
                    }
                }

                // Step 1: Enable in Settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(settings.theme.keyColorHex).copy(alpha = 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "1. Enable in Settings",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            if (isEnabled) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Enabled",
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "Turn on Rimu Keyboard in virtual keyboards list",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        enabled = !isEnabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(settings.rgbAccentColorHex),
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF00E676).copy(alpha = 0.2f),
                            disabledContentColor = Color(0xFF00E676)
                        ),
                        modifier = Modifier.testTag("enable_keyboard_btn")
                    ) {
                        Text(if (isEnabled) "Enabled" else "Enable", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Step 2: Select Keyboard
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(settings.theme.keyColorHex).copy(alpha = 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "2. Select Input Method",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "Set Rimu Keyboard as default keyboard",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    Button(
                        onClick = {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.showInputMethodPicker()
                        },
                        enabled = isEnabled && !isSelected,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(settings.rgbAccentColorHex),
                            contentColor = Color.Black,
                            disabledContainerColor = if (isSelected) Color(0xFF00E676).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.3f),
                            disabledContentColor = if (isSelected) Color(0xFF00E676) else Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("select_keyboard_btn")
                    ) {
                        Text(if (isSelected) "Active" else "Select", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // --- Quick RGB Mode Selector ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("quick_rgb_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RGB Lighting Mode",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = onNavigateToRgb) {
                        Text("All Styles →", color = Color(settings.rgbAccentColorHex), fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RgbMode.values().forEach { mode ->
                        val isCurrent = settings.rgbMode == mode
                        Surface(
                            onClick = {
                                preferences.updateSettings { it.copy(rgbMode = mode) }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) Color(settings.rgbAccentColorHex) else Color(settings.theme.keyColorHex),
                            border = if (!isCurrent) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
                        ) {
                            Text(
                                text = mode.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Live Interactive Keyboard Preview & Test Field ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("interactive_preview_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = Color(settings.rgbAccentColorHex),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Interactive Test & Preview",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (testText.isNotEmpty()) {
                        TextButton(onClick = { testText = "" }) {
                            Text("Clear", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    }
                }

                // Test input display box
                OutlinedTextField(
                    value = testText,
                    onValueChange = { testText = it },
                    placeholder = {
                        Text(
                            text = "Tap keyboard keys below to test typing...",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("home_test_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(settings.rgbAccentColorHex),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color(settings.theme.bgColorHex),
                        unfocusedContainerColor = Color(settings.theme.bgColorHex)
                    ),
                    singleLine = false,
                    maxLines = 3
                )

                // The embedded interactive keyboard preview!
                RimuKeyboardView(
                    settings = settings,
                    soundHapticManager = soundHapticManager,
                    onCharInput = { char ->
                        testText += char
                    },
                    onDelete = {
                        if (testText.isNotEmpty()) {
                            testText = testText.dropLast(1)
                        }
                    },
                    onEnter = {
                        testText += "\n"
                    },
                    onSpace = {
                        testText += " "
                    },
                    onVoiceInput = {
                        testText += " [Voice] "
                    },
                    onOpenSettings = onNavigateToRgb,
                    currentWordPrefix = testText.split(Regex("[\\s,;:.!?]+")).lastOrNull() ?: "",
                    onSelectSuggestion = { word ->
                        val parts = testText.split(Regex("(?<=[\\s,;:.!?])|(?=[\\s,;:.!?])")).toMutableList()
                        if (parts.isNotEmpty() && !parts.last().matches(Regex("[\\s,;:.!?]+"))) {
                            parts.removeAt(parts.lastIndex)
                        }
                        testText = parts.joinToString("") + word + " "
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

fun checkIsImeEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
    val enabledMethods = imm.enabledInputMethodList
    val packageName = context.packageName
    return enabledMethods.any { it.packageName == packageName }
}

fun checkIsImeSelected(context: Context): Boolean {
    val currentIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: return false
    return currentIme.contains(context.packageName)
}
