package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.keyboard.KeyboardLayouts
import com.example.keyboard.ShiftState
import com.example.rgb.RgbAnimationEngine
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Rimu Keyboard", appName)
    }

    @Test
    fun `keyboard preferences updates correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = KeyboardPreferences.getInstance(context)

        assertEquals(RgbMode.RAINBOW_FLOW, prefs.settings.value.rgbMode)
        assertTrue(prefs.settings.value.soundEnabled)

        prefs.updateSettings { it.copy(rgbMode = RgbMode.BREATHING_GLOW, soundProfile = SoundProfile.MECHANICAL) }
        assertEquals(RgbMode.BREATHING_GLOW, prefs.settings.value.rgbMode)
        assertEquals(SoundProfile.MECHANICAL, prefs.settings.value.soundProfile)
    }

    @Test
    fun `suggestion engine provides suggestions and emojis`() {
        val englishMatches = SuggestionEngine.getSuggestions("key", "EN")
        assertTrue(englishMatches.contains("keyboard"))

        val banglaMatches = SuggestionEngine.getSuggestions("বাংলা", "BN")
        assertTrue(banglaMatches.contains("বাংলাদেশ") || banglaMatches.contains("বাংলা"))

        val emojiMatches = SuggestionEngine.getSuggestions("happy", "EN")
        assertTrue(emojiMatches.contains("😄"))
    }

    @Test
    fun `keyboard layouts generate valid rows`() {
        val enRows = KeyboardLayouts.getEnglishRows(ShiftState.OFF, showNumberRow = true)
        assertEquals(5, enRows.size)

        val bnRows = KeyboardLayouts.getBanglaRows(ShiftState.OFF, showNumberRow = true)
        assertEquals(5, bnRows.size)
    }

    @Test
    fun `rgb engine returns valid colors`() {
        val color = RgbAnimationEngine.getColorFromSpectrum(0.5f, 1.0f)
        assertNotNull(color)
    }
}
