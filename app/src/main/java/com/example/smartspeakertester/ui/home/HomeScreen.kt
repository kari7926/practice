package com.example.smartspeakertester.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartspeakertester.SmartSpeakerUiState
import com.example.smartspeakertester.domain.TestRunOptions
import com.example.smartspeakertester.tts.VoiceGender
import com.example.smartspeakertester.ui.theme.Divider as DividerColor
import com.example.smartspeakertester.ui.theme.SurfaceLight

@Composable
fun HomeScreen(
    uiState: SmartSpeakerUiState,
    onImportFile: () -> Unit,
    onStartTest: () -> Unit,
    onVoiceSelected: (VoiceGender) -> Unit,
    onOptionsChanged: (TestRunOptions) -> Unit,
    onOpenPreview: () -> Unit,
    onOpenResults: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Smart Speaker Tester", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = "Created By Byungsoo Kwak", color = Color.Gray)

        SectionCard(title = "Import") {
            Text(text = uiState.fileName ?: "No file imported", fontWeight = FontWeight.SemiBold)
            Text(text = "Commands: ${uiState.commandCount}", color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onImportFile) { Text("Import File") }
            if (uiState.importError != null) {
                Text(text = uiState.importError, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
        }

        SectionCard(title = "Voice") {
            SegmentedControl(
                options = listOf("Female" to VoiceGender.FEMALE, "Male" to VoiceGender.MALE),
                selected = uiState.selectedVoice,
                onSelected = onVoiceSelected
            )
            if (uiState.ttsError != null) {
                Text(text = uiState.ttsError, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
        }

        SectionCard(title = "Test Options") {
            SegmentedControl(
                options = listOf("All" to true, "Custom" to false),
                selected = uiState.testOptions.useAll,
                onSelected = { all ->
                    onOptionsChanged(
                        uiState.testOptions.copy(useAll = all)
                    )
                }
            )
            if (!uiState.testOptions.useAll) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = uiState.testOptions.startIndex?.toString().orEmpty(),
                        onValueChange = { value ->
                            val num = value.toIntOrNull()
                            onOptionsChanged(uiState.testOptions.copy(startIndex = num))
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Start") }
                    )
                    OutlinedTextField(
                        value = uiState.testOptions.endIndex?.toString().orEmpty(),
                        onValueChange = { value ->
                            val num = value.toIntOrNull()
                            onOptionsChanged(uiState.testOptions.copy(endIndex = num))
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("End") }
                    )
                }
            }
            if (!uiState.optionsValidation.isValid) {
                Text(text = uiState.optionsValidation.errorMessage ?: "Invalid range", color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
        }

        SectionCard(title = "Navigation") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onOpenPreview) { Text("Commands Preview") }
                TextButton(onClick = onOpenResults) { Text("Results") }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onStartTest,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.optionsValidation.isValid && uiState.commandCount > 0
        ) {
            Text(text = "Start Test", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun <T> SegmentedControl(options: List<Pair<String, T>>, selected: T, onSelected: (T) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            options.forEach { option ->
                val isSelected = option.second == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                        .padding(vertical = 12.dp)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.first,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                    )
                }
                if (option != options.last()) {
                    Divider(color = DividerColor, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Divider(color = DividerColor, thickness = 1.dp)
            content()
        }
    }
}
