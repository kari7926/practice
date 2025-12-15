package com.example.smartspeakertester

import com.example.smartspeakertester.audio.ReplyEndDetector
import com.example.smartspeakertester.audio.ReplyEndResult
import com.example.smartspeakertester.data.TestCommand
import com.example.smartspeakertester.tts.TtsController
import com.example.smartspeakertester.tts.VoiceGender
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StateMachineTest {
    @Test
    fun progressesThroughCommands() = runBlocking {
        val fakeTts = FakeTts()
        val fakeDetector = FakeDetector()
        val viewModel = SmartSpeakerViewModel(fakeTts, fakeDetector)
        viewModel.updateOptions(com.example.smartspeakertester.domain.TestRunOptions(useAll = true))
        viewModel.seedCommands(listOf("One", "Two"))
        viewModel.startTest()
        // allow coroutine to finish
        kotlinx.coroutines.delay(500)
        assertEquals(TestState.Completed, viewModel.state.value.testState)
        assertEquals(2, viewModel.state.value.summary?.totalExecuted)
        assertTrue(fakeTts.spoken.containsAll(listOf("One", "Two")))
    }
}

private class FakeTts : TtsController {
    val spoken = mutableListOf<String>()
    override suspend fun init(): Boolean = true
    override suspend fun speak(text: String, onDone: () -> Unit) {
        spoken.add(text)
        onDone()
    }
    override fun setVoiceGender(gender: VoiceGender) {}
    override fun shutdown() {}
}

private class FakeDetector : ReplyEndDetector() {
    override suspend fun detectReplyEnd(): ReplyEndResult = ReplyEndResult.Completed
}
