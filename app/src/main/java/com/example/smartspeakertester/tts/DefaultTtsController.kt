package com.example.smartspeakertester.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DefaultTtsController(private val context: Context) : TtsController {
    private var tts: TextToSpeech? = null
    private var desiredGender: VoiceGender = VoiceGender.FEMALE

    override suspend fun init(): Boolean = withContext(Dispatchers.Main) {
        if (tts != null) return@withContext true
        var attempts = 0
        while (attempts < 3) {
            attempts++
            try {
                val ready = createTts()
                if (ready) return@withContext true
            } catch (t: Throwable) {
                if (attempts == 3) throw t
            }
            delay(250L)
        }
        false
    }

    private suspend fun createTts(): Boolean = suspendCancellableCoroutine { cont ->
        val instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                instance.language = Locale.US
                selectVoice(instance)
                cont.resume(true)
            } else {
                cont.resume(false)
            }
        }
        cont.invokeOnCancellation { instance.shutdown() }
        tts = instance
    }

    override fun setVoiceGender(gender: VoiceGender) {
        desiredGender = gender
        tts?.let { selectVoice(it) }
    }

    private fun selectVoice(tts: TextToSpeech) {
        val targetVoices: List<Voice> = tts.voices?.filter { voice ->
            voice.locale == Locale.US && !voice.isNetworkConnectionRequired
        }?.sortedBy { it.quality } ?: emptyList()

        val match = when (desiredGender) {
            VoiceGender.FEMALE -> targetVoices.firstOrNull { it.name.contains("female", true) }
                ?: targetVoices.firstOrNull { it.name.contains("feminine", true) }
            VoiceGender.MALE -> targetVoices.firstOrNull { it.name.contains("male", true) }
                ?: targetVoices.firstOrNull { it.name.contains("masculine", true) }
        } ?: targetVoices.firstOrNull()

        match?.let { tts.voice = it }
    }

    override suspend fun speak(text: String, onDone: () -> Unit) {
        withContext(Dispatchers.Main) {
            val engine = tts ?: throw IllegalStateException("TTS not initialized")
            suspendCancellableCoroutine { cont ->
                val utteranceId = System.currentTimeMillis().toString()
                engine.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onError(utteranceId: String?) {
                        if (cont.isActive) cont.resumeWithException(RuntimeException("TTS error"))
                    }

                    override fun onDone(utteranceId: String?) {
                        if (cont.isActive) cont.resume(Unit)
                        onDone()
                    }
                })
                engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                cont.invokeOnCancellation { engine.stop() }
            }
        }
    }

    override fun shutdown() {
        tts?.shutdown()
        tts = null
    }
}
