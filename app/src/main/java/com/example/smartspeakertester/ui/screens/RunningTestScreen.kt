package com.example.smartspeakertester.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartspeakertester.SmartSpeakerUiState
import com.example.smartspeakertester.TestState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RunningTestScreen(
    uiState: SmartSpeakerUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    val formatter = rememberTimeFormatter()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Running Test", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Monitoring replies and timing each command.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        StatusRow(label = "State", value = uiState.testState.name)
        StatusRow(
            label = "Command",
            value = if (uiState.activeCommandIndex >= 0) "${uiState.activeCommandIndex + 1} / ${uiState.commands.size}" else "Idle"
        )
        StatusRow(label = "Voice", value = uiState.selectedVoice.name)
        StatusRow(label = "Imported", value = uiState.importedFileName ?: "None")
        uiState.testError?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        ControlRow(
            testState = uiState.testState,
            onPause = onPause,
            onResume = onResume,
            onSkip = onSkip,
            onStop = onStop,
            onBack = onBack
        )

        Divider()
        Text(text = "Recent events", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = true)) {
            items(uiState.testLogs.takeLast(20).reversed()) { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = entry.message, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = formatter.format(Date(entry.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ControlRow(
    testState: TestState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (testState) {
            TestState.Paused -> Button(onClick = onResume) { Text("Resume") }
            TestState.Completed -> Button(onClick = onBack) { Text("Done") }
            else -> Button(onClick = onPause) { Text("Pause") }
        }
        Button(onClick = onSkip) { Text("Skip") }
        Button(onClick = onStop) { Text("Stop") }
    }
}

@Composable
private fun rememberTimeFormatter(): SimpleDateFormat {
    return remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
}

