package com.ato.recordodon

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ato.recordodon.navigation.AppRoutes
import com.ato.recordodon.screens.about.AboutScreen
import com.ato.recordodon.screens.main.MainScreen
import com.ato.recordodon.screens.main.MainViewModel
import com.ato.recordodon.screens.main.rememberRecordingsRepository
import com.ato.recordodon.screens.recorder.RecorderScreen
import com.ato.recordodon.screens.settings.SettingsScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = AppRoutes.MAIN,
        ) {
            composable(AppRoutes.MAIN) {
                val recordingsRepository = rememberRecordingsRepository()
                val viewModel = viewModel { MainViewModel(recordingsRepository) }
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.refresh()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }
                MainScreen(
                    recordings = viewModel.uiState.recordings,
                    onCreateRecording = { navController.navigate(AppRoutes.RECORDER) },
                    onOpenSettings = { navController.navigate(AppRoutes.SETTINGS) },
                    onOpenAbout = { navController.navigate(AppRoutes.ABOUT) },
                )
            }
            composable(AppRoutes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(AppRoutes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(AppRoutes.RECORDER) {
                RecorderScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
