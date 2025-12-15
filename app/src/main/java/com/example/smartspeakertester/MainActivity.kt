package com.example.smartspeakertester

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.smartspeakertester.audio.ReplyEndDetector
import com.example.smartspeakertester.navigation.Destinations
import com.example.smartspeakertester.navigation.SmartSpeakerNavGraph
import com.example.smartspeakertester.tts.DefaultTtsController
import com.example.smartspeakertester.ui.theme.SmartSpeakerTesterTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<SmartSpeakerViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val tts = DefaultTtsController(applicationContext)
                val detector = ReplyEndDetector()
                @Suppress("UNCHECKED_CAST")
                return SmartSpeakerViewModel(tts, detector) as T
            }
        }
    }

    private val importFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importFile(it, contentResolver) }
    }

    private val micPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.startTest()
            navController?.navigate(Destinations.Running.route)
        }
    }

    private var navController: androidx.navigation.NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartSpeakerTesterTheme {
                val controller = rememberNavController()
                navController = controller
                val uiState by viewModel.state.collectAsState()
                KeepScreenOnEffect(uiState.testState != TestState.Idle && uiState.testState != TestState.Completed)
                SmartSpeakerNavGraph(
                    navController = controller,
                    viewModel = viewModel,
                    onImportFile = {
                        importFileLauncher.launch(arrayOf("text/csv", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel"))
                    },
                    onRequestMicPermission = {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            }
        }
    }
}

@Composable
fun KeepScreenOnEffect(enabled: Boolean) {
    val context = LocalContext.current
    DisposableEffect(enabled) {
        val window = (context as ComponentActivity).window
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
