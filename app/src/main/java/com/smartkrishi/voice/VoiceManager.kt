package com.smartkrishi.voice

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale

class VoiceManager(private val context: Context) {

    companion object {
        const val VOICE_REQUEST_CODE = 101
        private const val LANG_HI = "hi"
        private const val LANG_EN = "en"
    }

    private var tts: TextToSpeech? = null
    private var currentLang = LANG_HI
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                setLanguage(LANG_HI)
            } else {
                isTtsReady = false
            }
        }
    }

    // 🔊 Set TTS Language Dynamically
    fun setLanguage(lang: String) {
        val targetLang = if (lang == LANG_EN) LANG_EN else LANG_HI
        currentLang = targetLang

        if (!isTtsReady) return

        val locale = when (targetLang) {
            LANG_EN -> Locale("en", "IN")
            else -> Locale("hi", "IN")
        }

        val result = tts?.setLanguage(locale)

        if (result == TextToSpeech.LANG_MISSING_DATA ||
            result == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            val fallbackLocale = Locale.US
            tts?.setLanguage(fallbackLocale)
            currentLang = LANG_EN
        }
    }

    // 🎤 Voice Input
    fun startListening(activity: Activity?) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            // Better support for Hindi + English mixed speech
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak or बोलिए...")
        }

        try {
            activity?.startActivityForResult(intent, VOICE_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "Speech recognition is not supported on this device",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to start voice input",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 🔊 Smart Speak with auto language detection
    fun speak(text: String) {
        if (!isTtsReady || text.isBlank()) return

        val detectedLang = detectSpeechLanguage(text)
        if (detectedLang != currentLang) {
            setLanguage(detectedLang)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "smartkrishi_tts")
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun getCurrentLanguage(): String = currentLang

    private fun detectSpeechLanguage(text: String): String {
        val trimmed = text.trim()
        val hasHindi = Regex("[\\u0900-\\u097F]").containsMatchIn(trimmed)

        if (hasHindi) return LANG_HI

        val englishHints = listOf(
            "weather", "pump", "market", "disease", "crop", "status",
            "temperature", "moisture", "humidity", "value", "open",
            "start", "stop", "help", "scheme", "rover", "log"
        )

        return if (englishHints.any { trimmed.lowercase(Locale.getDefault()).contains(it) }) {
            LANG_EN
        } else {
            LANG_HI
        }
    }

    // 🧹 Cleanup
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isTtsReady = false
    }
}