package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.SoundProfile
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundHapticManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<SoundProfile, Int>()
    private var isLoaded = false

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attrs)
            .build()

        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                isLoaded = true
            }
        }

        try {
            SoundProfile.values().forEach { profile ->
                val waveFile = generateSoundFile(context, profile)
                if (waveFile.exists()) {
                    val id = soundPool?.load(waveFile.absolutePath, 1) ?: -1
                    if (id != -1) {
                        soundIds[profile] = id
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback gracefully if sound synthesis encounters storage constraints
        }
    }

    fun playSound(profile: SoundProfile, volume: Float, isSoundEnabled: Boolean) {
        if (!isSoundEnabled || volume <= 0.01f) return

        // Respect device ringer mode: silent or vibrate mode should suppress keypress audio
        if (audioManager != null && audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            return
        }

        val soundId = soundIds[profile]
        if (soundId != null && soundId > 0) {
            val clampedVol = volume.coerceIn(0.05f, 1.0f)
            soundPool?.play(soundId, clampedVol, clampedVol, 1, 0, 1.0f)
        }
    }

    fun performHapticFeedback(isEnabled: Boolean, durationMs: Int) {
        if (!isEnabled || vibrator == null || !vibrator.hasVibrator()) return

        val clampedDuration = durationMs.coerceIn(5, 50).toLong()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitude = (clampedDuration * 4).coerceIn(40, 255).toInt()
            val effect = VibrationEffect.createOneShot(clampedDuration, amplitude)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(clampedDuration)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
    }

    companion object {
        private fun generateSoundFile(context: Context, profile: SoundProfile): File {
            val file = File(context.cacheDir, "rimu_${profile.name.lowercase()}.wav")
            if (file.exists() && file.length() > 100) {
                return file
            }

            val sampleRate = 44100
            val samples: ShortArray = when (profile) {
                SoundProfile.MECHANICAL -> generateMechanicalClick(sampleRate)
                SoundProfile.SOFT_CLICK -> generateSoftClick(sampleRate)
                SoundProfile.BUBBLE -> generateBubblePop(sampleRate)
                SoundProfile.DIGITAL -> generateDigitalBeep(sampleRate)
                SoundProfile.CLASSIC -> generateClassicTap(sampleRate)
            }

            writeWavFile(file, samples, sampleRate)
            return file
        }

        private fun generateMechanicalClick(sampleRate: Int): ShortArray {
            // High tactile click followed by tiny thud (mechanical blue/brown switch)
            val durationSec = 0.045
            val numSamples = (sampleRate * durationSec).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val envClick = exp(-t * 260.0)
                val click = sin(2.0 * PI * 2800.0 * t) * envClick * 0.75
                val envThud = exp(-t * 90.0)
                val thud = sin(2.0 * PI * 260.0 * t) * envThud * 0.45
                val sample = ((click + thud) * 32767.0).coerceIn(-32768.0, 32767.0)
                buffer[i] = sample.toInt().toShort()
            }
            return buffer
        }

        private fun generateSoftClick(sampleRate: Int): ShortArray {
            // Gentle tactile low-frequency bump
            val durationSec = 0.035
            val numSamples = (sampleRate * durationSec).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = exp(-t * 180.0)
                val tone = sin(2.0 * PI * 680.0 * t) * env * 0.6
                buffer[i] = (tone * 32767.0).coerceIn(-32768.0, 32767.0).toInt().toShort()
            }
            return buffer
        }

        private fun generateBubblePop(sampleRate: Int): ShortArray {
            // Frequency sweep upward (chirp/pop)
            val durationSec = 0.06
            val numSamples = (sampleRate * durationSec).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = exp(-t * 80.0) * (1.0 - exp(-t * 1200.0))
                val freq = 450.0 + (t / durationSec) * 750.0
                val tone = sin(2.0 * PI * freq * t) * env * 0.7
                buffer[i] = (tone * 32767.0).coerceIn(-32768.0, 32767.0).toInt().toShort()
            }
            return buffer
        }

        private fun generateDigitalBeep(sampleRate: Int): ShortArray {
            // Retro 8-bit crisp arcade blip
            val durationSec = 0.04
            val numSamples = (sampleRate * durationSec).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = exp(-t * 130.0)
                // Square wave approximation
                val sinVal = sin(2.0 * PI * 1350.0 * t)
                val squareVal = if (sinVal >= 0) 0.5 else -0.5
                val sample = squareVal * env * 0.6 * 32767.0
                buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
            }
            return buffer
        }

        private fun generateClassicTap(sampleRate: Int): ShortArray {
            // Typewriter strike
            val durationSec = 0.05
            val numSamples = (sampleRate * durationSec).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = exp(-t * 150.0)
                val noise = ((Math.random() * 2.0) - 1.0) * exp(-t * 400.0) * 0.4
                val tone = sin(2.0 * PI * 1100.0 * t) * env * 0.5
                val sample = (tone + noise) * 32767.0
                buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
            }
            return buffer
        }

        private fun writeWavFile(file: File, samples: ShortArray, sampleRate: Int) {
            val byteRate = sampleRate * 2 // 16-bit mono
            val dataSize = samples.size * 2
            val totalSize = 36 + dataSize

            val header = ByteBuffer.allocate(44).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put("RIFF".toByteArray())
                putInt(totalSize)
                put("WAVE".toByteArray())
                put("fmt ".toByteArray())
                putInt(16) // Subchunk1Size for PCM
                putShort(1) // AudioFormat 1 = PCM
                putShort(1) // NumChannels 1 = mono
                putInt(sampleRate)
                putInt(byteRate)
                putShort(2) // BlockAlign
                putShort(16) // BitsPerSample
                put("data".toByteArray())
                putInt(dataSize)
            }

            FileOutputStream(file).use { fos ->
                fos.write(header.array())
                val dataBuffer = ByteBuffer.allocate(samples.size * 2).apply {
                    order(ByteOrder.LITTLE_ENDIAN)
                    samples.forEach { putShort(it) }
                }
                fos.write(dataBuffer.array())
            }
        }
    }
}
