package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
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
import com.example.data.*
import com.example.ui.keyboard.RimuKeyboardView

@Composable
fun RgbAppearanceScreen(
    settings: KeyboardSettings,
    preferences: KeyboardPreferences,
    soundHapticManager: SoundHapticManager,
    modifier: Modifier = Modifier
) {
    var previewText by remember { mutableStateOf("RGB") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Live Interactive Preview Card ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("appearance_preview_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(settings.rgbAccentColorHex).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Appearance Preview",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${settings.rgbMode.displayName} • ${settings.theme.displayName}",
                        fontSize = 11.sp,
                        color = Color(settings.rgbAccentColorHex)
                    )
                }

                RimuKeyboardView(
                    settings = settings,
                    soundHapticManager = soundHapticManager,
                    onCharInput = { previewText += it },
                    onDelete = { if (previewText.isNotEmpty()) previewText = previewText.dropLast(1) },
                    onEnter = {},
                    onSpace = { previewText += " " },
                    currentWordPrefix = previewText,
                    onSelectSuggestion = { previewText = it },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                )
            }
        }

        // --- 2. RGB Lighting Modes ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("rgb_modes_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "RGB Animation Mode",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                RgbMode.values().forEach { mode ->
                    val isSelected = settings.rgbMode == mode
                    Surface(
                        onClick = { preferences.updateSettings { it.copy(rgbMode = mode) } },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(settings.rgbAccentColorHex).copy(alpha = 0.2f) else Color(settings.theme.keyColorHex).copy(alpha = 0.5f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(settings.rgbAccentColorHex)) else null,
                        modifier = Modifier.fillMaxWidth().testTag("mode_${mode.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = mode.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(settings.rgbAccentColorHex) else Color.White
                                )
                                Text(
                                    text = when (mode) {
                                        RgbMode.RAINBOW_FLOW -> "Smooth flowing rainbow wave across keyboard"
                                        RgbMode.COLOR_CYCLE -> "All keys cycle gradually through RGB spectrum"
                                        RgbMode.BREATHING_GLOW -> "Pulsing neon glow with sine-wave breathing"
                                        RgbMode.NEON_WAVE -> "Dynamic neon circular ripple across the keys"
                                        RgbMode.STATIC_COLOR -> "Steady intense neon illumination"
                                        RgbMode.REACTIVE_KEY_PRESS -> "Keys flash neon bright on tap and fade smoothly"
                                        RgbMode.OFF -> "Disable all RGB lighting effects for maximum battery"
                                    },
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(settings.rgbAccentColorHex),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. RGB Controls (Speed, Glow, Brightness, Border) ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("rgb_sliders_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "RGB Fine Tuning",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Animation Speed
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Animation Speed", fontSize = 13.sp, color = Color.White)
                        Text(
                            "${String.format("%.1f", settings.rgbSpeed)}x",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(settings.rgbAccentColorHex)
                        )
                    }
                    Slider(
                        value = settings.rgbSpeed,
                        onValueChange = { speed -> preferences.updateSettings { it.copy(rgbSpeed = speed) } },
                        valueRange = 0.5f..2.5f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(settings.rgbAccentColorHex),
                            activeTrackColor = Color(settings.rgbAccentColorHex)
                        ),
                        modifier = Modifier.testTag("slider_rgb_speed")
                    )
                }

                // Glow Intensity
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Glow Intensity", fontSize = 13.sp, color = Color.White)
                        Text(
                            "${(settings.rgbGlowIntensity * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(settings.rgbAccentColorHex)
                        )
                    }
                    Slider(
                        value = settings.rgbGlowIntensity,
                        onValueChange = { glow -> preferences.updateSettings { it.copy(rgbGlowIntensity = glow) } },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(settings.rgbAccentColorHex),
                            activeTrackColor = Color(settings.rgbAccentColorHex)
                        ),
                        modifier = Modifier.testTag("slider_glow_intensity")
                    )
                }

                // Brightness
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("RGB Brightness", fontSize = 13.sp, color = Color.White)
                        Text(
                            "${(settings.rgbBrightness * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(settings.rgbAccentColorHex)
                        )
                    }
                    Slider(
                        value = settings.rgbBrightness,
                        onValueChange = { bright -> preferences.updateSettings { it.copy(rgbBrightness = bright) } },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(settings.rgbAccentColorHex),
                            activeTrackColor = Color(settings.rgbAccentColorHex)
                        ),
                        modifier = Modifier.testTag("slider_rgb_brightness")
                    )
                }

                // Rainbow Border Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Rainbow Border", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("Outer animated glowing rim around keyboard", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = settings.rainbowBorder,
                        onCheckedChange = { checked -> preferences.updateSettings { it.copy(rainbowBorder = checked) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(settings.rgbAccentColorHex)
                        ),
                        modifier = Modifier.testTag("switch_rainbow_border")
                    )
                }
            }
        }

        // --- 4. Neon Color Palette ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("neon_colors_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Neon Color Palette",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Used for static lighting, breathing glow, reactive key press, and accents",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KeyboardPreferences.AVAILABLE_ACCENT_COLORS.forEach { (colorHex, colorName) ->
                        val isSelected = settings.rgbAccentColorHex == colorHex
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { preferences.updateSettings { it.copy(rgbAccentColorHex = colorHex) } }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(3.dp, Color.White, CircleShape)
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = colorName,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(colorHex) else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // --- 5. Keyboard Themes ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("themes_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Keyboard Theme",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                KeyboardTheme.values().forEach { theme ->
                    val isSelected = settings.theme == theme
                    Surface(
                        onClick = { preferences.updateSettings { it.copy(theme = theme) } },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(theme.bgColorHex),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(settings.rgbAccentColorHex)) else androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(theme.keyColorHex))
                                        .border(1.dp, Color(settings.rgbAccentColorHex).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = theme.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (theme == KeyboardTheme.CLEAN_LIGHT) Color.Black else Color.White
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(settings.rgbAccentColorHex)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 6. Key Shape & Sizing ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("key_shape_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Keycap Shape & Ergonomics",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Key Shape
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KeyShape.values().forEach { shape ->
                        val isSelected = settings.keyShape == shape
                        Surface(
                            onClick = { preferences.updateSettings { it.copy(keyShape = shape) } },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(settings.rgbAccentColorHex) else Color(settings.theme.keyColorHex),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = shape.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                // Height Scale
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Keyboard Height", fontSize = 13.sp, color = Color.White)
                        Text(
                            when {
                                settings.heightScale < 0.95f -> "Compact (85%)"
                                settings.heightScale > 1.05f -> "Tall (115%)"
                                else -> "Standard (100%)"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(settings.rgbAccentColorHex)
                        )
                    }
                    Slider(
                        value = settings.heightScale,
                        onValueChange = { h -> preferences.updateSettings { it.copy(heightScale = h) } },
                        valueRange = 0.85f..1.15f,
                        steps = 2,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(settings.rgbAccentColorHex),
                            activeTrackColor = Color(settings.rgbAccentColorHex)
                        )
                    )
                }
            }
        }
    }
}
