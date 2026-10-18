package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardSettings

@Composable
fun InputAboutScreen(
    settings: KeyboardSettings,
    preferences: KeyboardPreferences,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Languages & Input Behavior Card ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("languages_input_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color(settings.rgbAccentColorHex),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Languages & Typing Behavior",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Default Language Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Language", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("Switch between English QWERTY and Bangla", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("EN" to "English", "BN" to "বাংলা").forEach { (code, label) ->
                            val isSelected = settings.activeLanguage == code
                            Surface(
                                onClick = { preferences.updateSettings { it.copy(activeLanguage = code) } },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(settings.rgbAccentColorHex) else Color(settings.theme.keyColorHex),
                                modifier = Modifier.testTag("lang_toggle_$code")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Number Row Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dedicated Number Row", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("Show numeric keys (1-0 / ১-০) above letter keys", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = settings.numberRowEnabled,
                        onCheckedChange = { checked -> preferences.updateSettings { it.copy(numberRowEnabled = checked) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(settings.rgbAccentColorHex)
                        ),
                        modifier = Modifier.testTag("switch_number_row")
                    )
                }

                // Autocorrect / Suggestions Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Predictive Suggestions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("Show candidate words and emoji shortcuts while typing", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = settings.suggestionsEnabled,
                        onCheckedChange = { checked -> preferences.updateSettings { it.copy(suggestionsEnabled = checked) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(settings.rgbAccentColorHex)
                        ),
                        modifier = Modifier.testTag("switch_suggestions")
                    )
                }

                // Emoji Key Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dedicated Emoji Key", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("Show emoji button on the bottom bar", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = settings.emojiKeyEnabled,
                        onCheckedChange = { checked -> preferences.updateSettings { it.copy(emojiKeyEnabled = checked) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(settings.rgbAccentColorHex)
                        ),
                        modifier = Modifier.testTag("switch_emoji_key")
                    )
                }
            }
        }

        // --- 2. Privacy & Offline Guarantee Card ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("privacy_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy & Offline Security",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "Rimu Keyboard is built strictly privacy-first:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                val points = listOf(
                    "All typing input and autocorrect processing are done 100% locally on your device.",
                    "No keystrokes, personal messages, or passwords are ever collected, logged, or transmitted.",
                    "Clipboard history is stored entirely in private on-device app storage and is never uploaded anywhere.",
                    "No background analytics, no ads tracking, and no external telemetry servers."
                )

                points.forEach { point ->
                    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                        Text("🛡️", fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = point,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // --- 3. About App Card ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("about_app_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D1117))
                        .border(2.dp, Color(settings.rgbAccentColorHex), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.rimu_logo),
                        contentDescription = "Rimu Keyboard Logo",
                        modifier = Modifier.size(54.dp).clip(CircleShape)
                    )
                }

                Text(
                    text = "Rimu Keyboard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(settings.rgbAccentColorHex).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(settings.rgbAccentColorHex).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Version 1.0 • RGB Gaming Edition",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(settings.rgbAccentColorHex),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Crafted with passion for speed, neon aesthetics, and seamless Bengali & English typing.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Developer", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    Text("Rimu", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Engine", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    Text("Android IME + Jetpack Compose", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}
