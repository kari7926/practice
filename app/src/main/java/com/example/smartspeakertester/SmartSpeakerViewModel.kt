package com.example.smartspeakertester

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.annotation.VisibleForTesting
import com.example.smartspeakertester.audio.ReplyEndDetector
import com.example.smartspeakertester.audio.ReplyEndResult
import com.example.smartspeakertester.data.TestCommand
import com.example.smartspeakertester.domain.RangeValidationResult
import com.example.smartspeakertester.domain.RangeValidator
import com.example.smartspeakertester.domain.TestRunOptions
import com.example.smartspeakertester.parsing.CommandParser
import com.example.smartspeakertester.parsing.CsvCommandParser
import com.example.smartspeakertester.parsing.XlsxCommandParser
import com.example.smartspeakertester.tts.TtsController
import com.example.smartspeakertester.tts.VoiceGender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.util.Locale

private const val DELAY_AFTER_TTS_MS = 350L

data class UiLog(val timestamp: Long, val message: String)

data class TestSummary(
    val totalExecuted: Int,
    val durationMillis: Long,
    val uncertainEnds: Int,
    val stopped: Boolean
)

data class SmartSpeakerUiState(
    val fileName: String? = null,
    val commandCount: Int = 0,
    val commands: List<TestCommand> = emptyList(),
    val importError: String? = null,
    val ttsReady: Boolean = false,
    val ttsError: String? = null,
    val selectedVoice: VoiceGender = VoiceGender.FEMALE,
    val testOptions: TestRunOptions = TestRunOptions(),
    val optionsValidation: RangeValidationResult = RangeValidationResult(false, "Import a command file first."),
    val testState: TestState = TestState.Idle,
    val currentCommand: TestCommand? = null,
    val logs: List<UiLog> = emptyList(),
    val listeningStatus: String? = null,
    val summary: TestSummary? = null
)

class SmartSpeakerViewModel(
    private val ttsController: TtsController,
    private val replyEndDetector: ReplyEndDetector,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
    private val _state = MutableStateFlow(SmartSpeakerUiState())
    val state: StateFlow<SmartSpeakerUiState> = _state.asStateFlow()

    private var testJob: Job? = null
    private val csvParser: CommandParser = CsvCommandParser()
    private val xlsxParser: CommandParser = XlsxCommandParser()
    private var pauseRequested = false
    private var stopRequested = false
    private var skipRequested = false

    fun initializeTts() {
        viewModelScope.launch {
            try {
                val ok = ttsController.init()
                if (ok) {
                    _state.value = _state.value.copy(ttsReady = true, ttsError = null)
                } else {
                    _state.value = _state.value.copy(ttsReady = false, ttsError = "TTS unavailable")
                }
            } catch (t: Throwable) {
                _state.value = _state.value.copy(ttsReady = false, ttsError = t.message ?: "TTS init failed")
            }
        }
    }

    fun importFile(uri: Uri, resolver: ContentResolver) {
        viewModelScope.launch(dispatcher) {
            runCatching {
                resolver.openInputStream(uri)?.use { inputStream ->
                    val name = resolver.getFileName(uri)
                    val parser = selectParser(name)
                    val buffered = BufferedInputStream(inputStream)
                    val commands = parser.parse(buffered)
                        .filter { it.isNotBlank() }
                        .mapIndexed { index, text -> TestCommand(index + 1, text.trim()) }
                    if (commands.isEmpty()) throw IllegalArgumentException("No commands found")
                    val validation = RangeValidator.validate(commands.size, _state.value.testOptions)
                    _state.value = _state.value.copy(
                        fileName = name,
                        commandCount = commands.size,
                        commands = commands,
                        importError = null,
                        optionsValidation = validation
                    )
                } ?: throw IllegalStateException("Unable to open file")
            }.onFailure { error ->
                _state.value = _state.value.copy(importError = error.message ?: "Import failed", commandCount = 0, commands = emptyList())
            }
        }
    }

    private fun selectParser(name: String?): CommandParser {
        val lower = name?.lowercase(Locale.ROOT) ?: ""
        return if (lower.endsWith(".csv")) csvParser else xlsxParser
    }

    fun setVoiceGender(gender: VoiceGender) {
        _state.value = _state.value.copy(selectedVoice = gender)
        ttsController.setVoiceGender(gender)
    }

    fun updateOptions(options: TestRunOptions) {
        val validation = RangeValidator.validate(_state.value.commandCount, options)
        _state.value = _state.value.copy(testOptions = options, optionsValidation = validation)
    }

    fun startTest() {
        val currentState = _state.value
        val validation = RangeValidator.validate(currentState.commandCount, currentState.testOptions)
        if (!validation.isValid) {
            _state.value = currentState.copy(optionsValidation = validation)
            return
        }
        if (testJob?.isActive == true) return
        pauseRequested = false
        stopRequested = false
        skipRequested = false
        testJob = viewModelScope.launch(dispatcher) {
            runTest(validation)
        }
    }

    private suspend fun runTest(validation: RangeValidationResult) {
        val state = _state.value
        val commands = state.commands
        if (commands.isEmpty()) return
        val ready = runCatching { ttsController.init() }.getOrDefault(false)
        if (!ready) {
            _state.value = _state.value.copy(ttsReady = false, ttsError = "Text-to-speech unavailable")
            return
        }
        var uncertain = 0
        val startTime = System.currentTimeMillis()
        var executed = 0
        val slice = commands.filter { it.index in validation.resolvedStart..validation.resolvedEnd }
        for (command in slice) {
            if (stopRequested) break
            handlePause()
            skipRequested = false
            updateStateForCommand(TestState.Speaking, command, "Speaking")
            addLog("Speaking command ${command.index}")
            try {
                ttsController.speak(command.text) {}
            } catch (t: Throwable) {
                addLog("TTS error: ${t.message}")
                _state.value = _state.value.copy(testState = TestState.Error, listeningStatus = t.message)
                return
            }
            delay(DELAY_AFTER_TTS_MS)
            if (stopRequested) break
            if (skipRequested) continue
            handlePause()
            updateStateForCommand(TestState.Listening, command, "Listening for reply")
            addLog("Listening for reply for ${command.index}")
            when (val result = replyEndDetector.detectReplyEnd()) {
                ReplyEndResult.Completed -> addLog("Reply end detected for ${command.index}")
                ReplyEndResult.Timeout -> {
                    addLog("Uncertain end (timeout) for ${command.index}")
                    uncertain++
                }
                is ReplyEndResult.Error -> {
                    addLog("Listening error: ${result.reason}")
                    uncertain++
                }
            }
            executed++
        }
        val duration = System.currentTimeMillis() - startTime
        val finalState = if (stopRequested) TestState.Stopped else TestState.Completed
        _state.value = _state.value.copy(
            testState = finalState,
            currentCommand = null,
            listeningStatus = null,
            summary = TestSummary(
                totalExecuted = executed,
                durationMillis = duration,
                uncertainEnds = uncertain,
                stopped = stopRequested
            )
        )
    }

    private suspend fun handlePause() {
        while (pauseRequested && !stopRequested) {
            _state.value = _state.value.copy(testState = TestState.Paused)
            delay(200)
        }
    }

    private fun updateStateForCommand(testState: TestState, command: TestCommand, status: String?) {
        _state.value = _state.value.copy(
            testState = testState,
            currentCommand = command,
            listeningStatus = status
        )
    }

    private fun addLog(message: String) {
        val now = System.currentTimeMillis()
        val updated = _state.value.logs + UiLog(now, message)
        _state.value = _state.value.copy(logs = updated.takeLast(200))
    }

    fun pauseTest() {
        pauseRequested = true
    }

    fun resumeTest() {
        pauseRequested = false
    }

    fun stopTest() {
        stopRequested = true
        testJob?.cancel()
        _state.value = _state.value.copy(testState = TestState.Stopped, currentCommand = null)
    }

    fun skipCommand() {
        skipRequested = true
    }

    override fun onCleared() {
        super.onCleared()
        ttsController.shutdown()
    }

    @VisibleForTesting
    fun seedCommands(commands: List<String>) {
        val mapped = commands.mapIndexed { index, text -> TestCommand(index + 1, text) }
        val validation = RangeValidator.validate(mapped.size, _state.value.testOptions)
        _state.value = _state.value.copy(
            fileName = "test.csv",
            commandCount = mapped.size,
            commands = mapped,
            importError = null,
            optionsValidation = validation
        )
    }
}

private fun ContentResolver.getFileName(uri: Uri): String {
    return uri.lastPathSegment?.substringAfterLast('/') ?: "commands"
}
