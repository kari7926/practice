package com.example.smartspeakertester.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartspeakertester.SmartSpeakerViewModel
import com.example.smartspeakertester.ui.home.HomeScreen
import com.example.smartspeakertester.ui.preview.CommandsPreviewScreen
import com.example.smartspeakertester.ui.results.ResultsScreen
import com.example.smartspeakertester.ui.running.RunningTestScreen

sealed class Destinations(val route: String) {
    data object Home : Destinations("home")
    data object Preview : Destinations("preview")
    data object Running : Destinations("running")
    data object Results : Destinations("results")
}

@Composable
fun SmartSpeakerNavGraph(
    navController: NavHostController,
    viewModel: SmartSpeakerViewModel,
    modifier: Modifier = Modifier,
    onImportFile: () -> Unit,
    onRequestMicPermission: () -> Unit
) {
    NavHost(navController = navController, startDestination = Destinations.Home.route, modifier = modifier) {
        composable(Destinations.Home.route) {
            val uiState by viewModel.state.collectAsState()
            LaunchedEffect(Unit) { viewModel.initializeTts() }
            HomeScreen(
                uiState = uiState,
                onImportFile = onImportFile,
                onStartTest = {
                    if (uiState.ttsReady) {
                        onRequestMicPermission()
                    }
                },
                onVoiceSelected = { viewModel.setVoiceGender(it) },
                onOptionsChanged = { viewModel.updateOptions(it) },
                onOpenPreview = { navController.navigate(Destinations.Preview.route) },
                onOpenResults = { navController.navigate(Destinations.Results.route) }
            )
        }
        composable(Destinations.Preview.route) {
            val uiState by viewModel.state.collectAsState()
            CommandsPreviewScreen(
                commands = uiState.commands,
                options = uiState.testOptions,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Destinations.Running.route) {
            val uiState by viewModel.state.collectAsState()
            RunningTestScreen(
                uiState = uiState,
                onPause = { viewModel.pauseTest() },
                onResume = { viewModel.resumeTest() },
                onStop = {
                    viewModel.stopTest()
                    navController.navigate(Destinations.Results.route)
                },
                onSkip = { viewModel.skipCommand() },
                onBack = {
                    viewModel.stopTest()
                    navController.popBackStack()
                }
            )
        }
        composable(Destinations.Results.route) {
            val uiState by viewModel.state.collectAsState()
            ResultsScreen(
                summary = uiState.summary,
                logs = uiState.logs,
                onBack = {
                    navController.popBackStack(Destinations.Home.route, inclusive = false)
                }
            )
        }
    }
}
