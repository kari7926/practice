package com.example.smartspeakertester.ui.running

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowBack
import androidx.compose.material3.icons.filled.Pause
import androidx.compose.material3.icons.filled.PlayArrow
import androidx.compose.material3.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartspeakertester.SmartSpeakerUiState
import com.example.smartspeakertester.TestState
import com.example.smartspeakertester.UiLog
import com.example.smartspeakertester.ui.theme.SurfaceLight

@Composable
fun RunningTestScreen(
    uiState: SmartSpeakerUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Running Test") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceLight)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(uiState)
            ControlRow(
                testState = uiState.testState,
                onPause = onPause,
                onResume = onResume,
                onStop = onStop,
                onSkip = onSkip
            )
            LogsCard(logs = uiState.logs)
        }
    }
}

@Composable
private fun StatusCard(uiState: SmartSpeakerUiState) {
    val current = uiState.currentCommand
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Status: ${uiState.testState}", fontWeight = FontWeight.Bold)
            Text(text = "Command: ${current?.text ?: "Waiting"}")
            Text(text = "Index: ${current?.index ?: 0} / ${uiState.commandCount}")
            Text(text = uiState.listeningStatus ?: "")
        }
    }
}

@Composable
private fun ControlRow(
    testState: TestState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { if (testState == TestState.Paused) onResume() else onPause() }, modifier = Modifier.weight(1f)) {
            if (testState == TestState.Paused) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                Text("Resume")
            } else {
                Icon(Icons.Default.Pause, contentDescription = "Pause")
                Text("Pause")
            }
        }
        Button(onClick = onSkip, modifier = Modifier.weight(1f)) {
            Text("Skip")
        }
        Button(
            onClick = onStop,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
        ) {
            Icon(Icons.Default.Stop, contentDescription = "Stop")
            Text("Stop")
        }
    }
}

@Composable
private fun LogsCard(logs: List<UiLog>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Log", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(logs) { log ->
                    Text(text = "${log.timestamp}: ${log.message}", color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}
