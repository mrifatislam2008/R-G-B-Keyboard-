package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundHapticManager
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardSettings
import com.example.data.SoundProfile

@Composable
fun SoundHapticsScreen(
    settings: KeyboardSettings,
    preferences: KeyboardPreferences,
    soundHapticManager: SoundHapticManager,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Keypress Sound Settings Card ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("sound_settings_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color(settings.rgbAccentColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Keypress Sound", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Play sound effects on keystrokes", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }

                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = { checked ->
                            preferences.updateSettings { it.copy(soundEnabled = checked) }
                            if (checked) {
                                soundHapticManager.playSound(settings.soundProfile, settings.soundVolume, true)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(settings.rgbAccentColorHex)
                        ),
                        modifier = Modifier.testTag("switch_sound_enabled")
                    )
                }

                if (settings.soundEnabled) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Sound Volume Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sound Volume", fontSize = 13.sp, color = Color.White)
                            Text(
                                "${(settings.soundVolume * 100).toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(settings.rgbAccentColorHex)
                            )
                        }
                        Slider(
                            value = settings.soundVolume,
                            onValueChange = { vol ->
                                preferences.updateSettings { it.copy(soundVolume = vol) }
                            },
                            valueRange = 0.05f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(settings.rgbAccentColorHex),
                                activeTrackColor = Color(settings.rgbAccentColorHex)
                            ),
                            modifier = Modifier.testTag("slider_sound_volume")
                        )
                    }

                    Text(
                        text = "Sound Profiles",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    SoundProfile.values().forEach { profile ->
                        val isSelected = settings.soundProfile == profile
                        Surface(
                            onClick = {
                                preferences.updateSettings { it.copy(soundProfile = profile) }
                                soundHapticManager.playSound(profile, settings.soundVolume, true)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(settings.rgbAccentColorHex).copy(alpha = 0.2f) else Color(settings.theme.keyColorHex).copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(settings.rgbAccentColorHex)) else null,
                            modifier = Modifier.fillMaxWidth().testTag("sound_profile_${profile.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(settings.rgbAccentColorHex) else Color.White
                                    )
                                    Text(
                                        text = when (profile) {
                                            SoundProfile.MECHANICAL -> "Tactile clicky switch sound with dampening"
                                            SoundProfile.SOFT_CLICK -> "Gentle low-mid keystroke tap"
                                            SoundProfile.BUBBLE -> "Satisfying bubble pop sound"
                                            SoundProfile.DIGITAL -> "Retro 8-bit gaming arcade blip"
                                            SoundProfile.CLASSIC -> "Traditional typewriter transient strike"
                                        },
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        soundHapticManager.playSound(profile, settings.soundVolume, true)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Preview Sound",
                                        tint = Color(settings.rgbAccentColorHex),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Vibration & Haptics Settings Card ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("haptics_settings_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = Color(settings.rgbAccentColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Keypress Vibration", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Haptic tactile feedback on keypresses", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }

                    Switch(
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { checked ->
                            preferences.updateSettings { it.copy(vibrationEnabled = checked) }
                            if (checked) {
                                soundHapticManager.performHapticFeedback(true, settings.vibrationIntensityMs)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(settings.rgbAccentColorHex)
                        ),
                        modifier = Modifier.testTag("switch_vibration_enabled")
                    )
                }

                if (settings.vibrationEnabled) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Vibration Intensity", fontSize = 13.sp, color = Color.White)
                            Text(
                                "${settings.vibrationIntensityMs} ms",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(settings.rgbAccentColorHex)
                            )
                        }
                        Slider(
                            value = settings.vibrationIntensityMs.toFloat(),
                            onValueChange = { ms ->
                                preferences.updateSettings { it.copy(vibrationIntensityMs = ms.toInt()) }
                            },
                            onValueChangeFinished = {
                                soundHapticManager.performHapticFeedback(true, settings.vibrationIntensityMs)
                            },
                            valueRange = 8f..45f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(settings.rgbAccentColorHex),
                                activeTrackColor = Color(settings.rgbAccentColorHex)
                            ),
                            modifier = Modifier.testTag("slider_vibration_intensity")
                        )
                    }

                    Button(
                        onClick = {
                            soundHapticManager.performHapticFeedback(true, settings.vibrationIntensityMs)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(settings.rgbAccentColorHex),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("test_haptic_button")
                    ) {
                        Text("Test Haptic Vibration", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
