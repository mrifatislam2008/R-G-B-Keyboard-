package com.example.rgb

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.KeyboardSettings
import com.example.data.RgbMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object RgbAnimationEngine {

    val RAINBOW_SPECTRUM = listOf(
        Color(0xFFFF0055), // Neon Red/Pink
        Color(0xFFFF9900), // Neon Orange
        Color(0xFFFFEA00), // Neon Yellow
        Color(0xFF00E676), // Neon Green
        Color(0xFF00E5FF), // Neon Cyan
        Color(0xFF2979FF), // Neon Blue
        Color(0xFFBD00FF), // Neon Purple
        Color(0xFFFF0055)  // Loop back
    )

    @Composable
    fun rememberAnimatedRgbPhase(settings: KeyboardSettings): State<Float> {
        val transition = rememberInfiniteTransition(label = "rgb_transition")
        val baseDuration = 4000
        val adjustedDuration = (baseDuration / settings.rgbSpeed.coerceIn(0.5f, 2.5f)).toInt()

        return transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = adjustedDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rgb_phase"
        )
    }

    /**
     * Calculates the dynamic RGB border brush based on current mode and phase.
     */
    fun getBorderBrush(settings: KeyboardSettings, phase: Float): Brush {
        if (settings.rgbMode == RgbMode.OFF) {
            return Brush.linearGradient(listOf(Color(0xFF2A364F), Color(0xFF1E2838)))
        }

        val brightness = settings.rgbBrightness.coerceIn(0.1f, 1.0f)
        val glow = settings.rgbGlowIntensity.coerceIn(0.0f, 1.0f)
        val accent = Color(settings.rgbAccentColorHex)

        return when (settings.rgbMode) {
            RgbMode.RAINBOW_FLOW -> {
                val shiftedColors = getShiftedRainbow(phase, brightness)
                Brush.horizontalGradient(shiftedColors)
            }
            RgbMode.COLOR_CYCLE -> {
                val currentColor = getColorFromSpectrum(phase, brightness)
                val adjacentColor = getColorFromSpectrum((phase + 0.15f) % 1f, brightness)
                Brush.linearGradient(listOf(currentColor, adjacentColor, currentColor))
            }
            RgbMode.BREATHING_GLOW -> {
                val breath = (sin(phase * 2.0 * PI) * 0.5 + 0.5).toFloat()
                val alpha = (0.2f + 0.8f * breath) * glow * brightness
                val base = accent.copy(alpha = alpha.coerceIn(0.1f, 1.0f))
                Brush.horizontalGradient(listOf(base.copy(alpha = 0.3f), base, base.copy(alpha = 0.3f)))
            }
            RgbMode.NEON_WAVE -> {
                val c1 = getColorFromSpectrum(phase, brightness)
                val c2 = getColorFromSpectrum((phase + 0.5f) % 1f, brightness)
                Brush.sweepGradient(listOf(c1, c2, c1))
            }
            RgbMode.STATIC_COLOR -> {
                val litAccent = accent.copy(alpha = (glow * brightness).coerceIn(0.3f, 1.0f))
                Brush.linearGradient(listOf(litAccent, litAccent.copy(alpha = 0.5f), litAccent))
            }
            RgbMode.REACTIVE_KEY_PRESS -> {
                val subtle = accent.copy(alpha = 0.25f * brightness)
                Brush.linearGradient(listOf(subtle, subtle.copy(alpha = 0.4f), subtle))
            }
            RgbMode.OFF -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
        }
    }

    /**
     * Determines key border or glow color given normalized coordinate x/y in keyboard grid.
     */
    fun getKeyGlowColor(
        settings: KeyboardSettings,
        phase: Float,
        keyRelativeX: Float,
        keyRelativeY: Float,
        isPressedOrReactive: Boolean,
        reactiveAlpha: Float
    ): Color {
        if (isPressedOrReactive && reactiveAlpha > 0.05f) {
            val accent = Color(settings.rgbAccentColorHex)
            return accent.copy(alpha = reactiveAlpha.coerceIn(0.1f, 1.0f))
        }

        if (settings.rgbMode == RgbMode.OFF) {
            return Color.Transparent
        }

        val brightness = settings.rgbBrightness.coerceIn(0.1f, 1.0f)
        val glow = settings.rgbGlowIntensity.coerceIn(0.0f, 1.0f)
        val accent = Color(settings.rgbAccentColorHex)

        return when (settings.rgbMode) {
            RgbMode.RAINBOW_FLOW -> {
                val keyPhase = (phase + keyRelativeX * 0.7f + keyRelativeY * 0.3f) % 1.0f
                getColorFromSpectrum(keyPhase, brightness).copy(alpha = (0.55f * glow).coerceIn(0.05f, 0.95f))
            }
            RgbMode.COLOR_CYCLE -> {
                getColorFromSpectrum(phase, brightness).copy(alpha = (0.5f * glow).coerceIn(0.05f, 0.9f))
            }
            RgbMode.BREATHING_GLOW -> {
                val breath = (sin(phase * 2.0 * PI) * 0.5 + 0.5).toFloat()
                val alpha = (0.15f + 0.75f * breath) * glow * brightness
                accent.copy(alpha = alpha.coerceIn(0.05f, 0.95f))
            }
            RgbMode.NEON_WAVE -> {
                val wavePhase = (phase + (keyRelativeX - 0.5f) * (keyRelativeX - 0.5f) + (keyRelativeY - 0.5f) * (keyRelativeY - 0.5f)) % 1.0f
                getColorFromSpectrum(wavePhase, brightness).copy(alpha = (0.55f * glow).coerceIn(0.05f, 0.95f))
            }
            RgbMode.STATIC_COLOR -> {
                accent.copy(alpha = (0.6f * glow * brightness).coerceIn(0.05f, 0.95f))
            }
            RgbMode.REACTIVE_KEY_PRESS -> {
                // Dim resting state; glows bright on tap
                accent.copy(alpha = 0.08f * brightness)
            }
            RgbMode.OFF -> Color.Transparent
        }
    }

    private fun getShiftedRainbow(phase: Float, brightness: Float): List<Color> {
        val n = RAINBOW_SPECTRUM.size
        return (0 until n).map { i ->
            val colorPhase = (phase + (i.toFloat() / n)) % 1.0f
            getColorFromSpectrum(colorPhase, brightness)
        }
    }

    fun getColorFromSpectrum(phase: Float, brightness: Float): Color {
        val p = ((phase % 1.0f) + 1.0f) % 1.0f
        val count = RAINBOW_SPECTRUM.size - 1
        val scaled = p * count
        val index = scaled.toInt().coerceIn(0, count - 1)
        val fraction = scaled - index

        val c1 = RAINBOW_SPECTRUM[index]
        val c2 = RAINBOW_SPECTRUM[index + 1]

        val r = (c1.red + (c2.red - c1.red) * fraction) * brightness
        val g = (c1.green + (c2.green - c1.green) * fraction) * brightness
        val b = (c1.blue + (c2.blue - c1.blue) * fraction) * brightness

        return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f), 1f)
    }
}
