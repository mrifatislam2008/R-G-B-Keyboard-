package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RgbMode(val displayName: String) {
    RAINBOW_FLOW("Rainbow Flow"),
    COLOR_CYCLE("Color Cycle"),
    BREATHING_GLOW("Breathing Glow"),
    NEON_WAVE("Neon Wave"),
    STATIC_COLOR("Static Color"),
    REACTIVE_KEY_PRESS("Reactive Key Press"),
    OFF("Off")
}

enum class SoundProfile(val displayName: String) {
    MECHANICAL("Mechanical Clicky"),
    SOFT_CLICK("Soft Click"),
    BUBBLE("Bubble Pop"),
    DIGITAL("Digital Arcade"),
    CLASSIC("Classic Tap")
}

enum class KeyShape(val displayName: String, val radiusDp: Int) {
    ROUNDED("Rounded (8dp)", 8),
    PILL("Pill (16dp)", 16),
    SHARP("Sharp (3dp)", 3)
}

enum class KeyboardTheme(val displayName: String, val bgColorHex: Long, val keyColorHex: Long) {
    CYBER_DARK("Cyber Dark", 0xFF090C10, 0xFF212A3B),
    MIDNIGHT_OLED("Midnight OLED", 0xFF000000, 0xFF161616),
    DEEP_NEBULA("Deep Nebula", 0xFF0E0B1F, 0xFF231B42),
    MATRIX_CODE("Matrix Code", 0xFF051108, 0xFF0E2E16),
    CLEAN_LIGHT("Clean Light", 0xFFE2E8F0, 0xFFFFFFFF)
}

data class KeyboardSettings(
    val rgbMode: RgbMode = RgbMode.RAINBOW_FLOW,
    val rgbSpeed: Float = 1.0f,
    val rgbGlowIntensity: Float = 0.8f,
    val rgbBrightness: Float = 1.0f,
    val rgbAccentColorHex: Long = 0xFF00E5FF, // Neon Cyan default
    val rainbowBorder: Boolean = true,
    val theme: KeyboardTheme = KeyboardTheme.CYBER_DARK,
    val keyShape: KeyShape = KeyShape.ROUNDED,
    val heightScale: Float = 1.0f,
    val keySpacingDp: Int = 4,
    val keyFontSizeSp: Int = 16,
    val soundEnabled: Boolean = true,
    val soundProfile: SoundProfile = SoundProfile.MECHANICAL,
    val soundVolume: Float = 0.75f,
    val vibrationEnabled: Boolean = true,
    val vibrationIntensityMs: Int = 22,
    val numberRowEnabled: Boolean = true,
    val suggestionsEnabled: Boolean = true,
    val emojiKeyEnabled: Boolean = true,
    val autoCapitalization: Boolean = true,
    val activeLanguage: String = "EN" // "EN" or "BN"
)

class KeyboardPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("rimu_keyboard_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<KeyboardSettings> = _settings.asStateFlow()

    private fun loadSettings(): KeyboardSettings {
        val rgbModeName = prefs.getString("rgb_mode", RgbMode.RAINBOW_FLOW.name) ?: RgbMode.RAINBOW_FLOW.name
        val soundProfileName = prefs.getString("sound_profile", SoundProfile.MECHANICAL.name) ?: SoundProfile.MECHANICAL.name
        val keyShapeName = prefs.getString("key_shape", KeyShape.ROUNDED.name) ?: KeyShape.ROUNDED.name
        val themeName = prefs.getString("theme", KeyboardTheme.CYBER_DARK.name) ?: KeyboardTheme.CYBER_DARK.name

        return KeyboardSettings(
            rgbMode = runCatching { RgbMode.valueOf(rgbModeName) }.getOrDefault(RgbMode.RAINBOW_FLOW),
            rgbSpeed = prefs.getFloat("rgb_speed", 1.0f),
            rgbGlowIntensity = prefs.getFloat("rgb_glow_intensity", 0.8f),
            rgbBrightness = prefs.getFloat("rgb_brightness", 1.0f),
            rgbAccentColorHex = prefs.getLong("rgb_accent_color", 0xFF00E5FF),
            rainbowBorder = prefs.getBoolean("rainbow_border", true),
            theme = runCatching { KeyboardTheme.valueOf(themeName) }.getOrDefault(KeyboardTheme.CYBER_DARK),
            keyShape = runCatching { KeyShape.valueOf(keyShapeName) }.getOrDefault(KeyShape.ROUNDED),
            heightScale = prefs.getFloat("height_scale", 1.0f),
            keySpacingDp = prefs.getInt("key_spacing", 4),
            keyFontSizeSp = prefs.getInt("key_font_size", 16),
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            soundProfile = runCatching { SoundProfile.valueOf(soundProfileName) }.getOrDefault(SoundProfile.MECHANICAL),
            soundVolume = prefs.getFloat("sound_volume", 0.75f),
            vibrationEnabled = prefs.getBoolean("vibration_enabled", true),
            vibrationIntensityMs = prefs.getInt("vibration_intensity", 22),
            numberRowEnabled = prefs.getBoolean("number_row_enabled", true),
            suggestionsEnabled = prefs.getBoolean("suggestions_enabled", true),
            emojiKeyEnabled = prefs.getBoolean("emoji_key_enabled", true),
            autoCapitalization = prefs.getBoolean("auto_cap", true),
            activeLanguage = prefs.getString("active_lang", "EN") ?: "EN"
        )
    }

    fun updateSettings(transform: (KeyboardSettings) -> KeyboardSettings) {
        val newSettings = transform(_settings.value)
        _settings.value = newSettings

        prefs.edit().apply {
            putString("rgb_mode", newSettings.rgbMode.name)
            putFloat("rgb_speed", newSettings.rgbSpeed)
            putFloat("rgb_glow_intensity", newSettings.rgbGlowIntensity)
            putFloat("rgb_brightness", newSettings.rgbBrightness)
            putLong("rgb_accent_color", newSettings.rgbAccentColorHex)
            putBoolean("rainbow_border", newSettings.rainbowBorder)
            putString("theme", newSettings.theme.name)
            putString("key_shape", newSettings.keyShape.name)
            putFloat("height_scale", newSettings.heightScale)
            putInt("key_spacing", newSettings.keySpacingDp)
            putInt("key_font_size", newSettings.keyFontSizeSp)
            putBoolean("sound_enabled", newSettings.soundEnabled)
            putString("sound_profile", newSettings.soundProfile.name)
            putFloat("sound_volume", newSettings.soundVolume)
            putBoolean("vibration_enabled", newSettings.vibrationEnabled)
            putInt("vibration_intensity", newSettings.vibrationIntensityMs)
            putBoolean("number_row_enabled", newSettings.numberRowEnabled)
            putBoolean("suggestions_enabled", newSettings.suggestionsEnabled)
            putBoolean("emoji_key_enabled", newSettings.emojiKeyEnabled)
            putBoolean("auto_cap", newSettings.autoCapitalization)
            putString("active_lang", newSettings.activeLanguage)
            apply()
        }
    }

    companion object {
        @Volatile
        private var instance: KeyboardPreferences? = null

        fun getInstance(context: Context): KeyboardPreferences {
            return instance ?: synchronized(this) {
                instance ?: KeyboardPreferences(context.applicationContext).also { instance = it }
            }
        }

        val AVAILABLE_ACCENT_COLORS = listOf(
            0xFF00E5FF to "Neon Cyan",
            0xFFBD00FF to "Neon Purple",
            0xFFFF007F to "Neon Pink",
            0xFF2979FF to "Electric Blue",
            0xFF00E676 to "Matrix Green",
            0xFFFF1744 to "Crimson Red",
            0xFFFFEA00 to "Solar Yellow",
            0xFFFF9100 to "Neon Orange"
        )
    }
}
