package com.example.smartspeakertester.tts

interface TtsController {
    suspend fun init(): Boolean
    suspend fun speak(text: String, onDone: () -> Unit)
    fun setVoiceGender(gender: VoiceGender)
    fun shutdown()
}
