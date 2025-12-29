package com.ato.recordodon

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ato.recordodon.navigation.AppRoutes
import com.ato.recordodon.screens.about.AboutScreen
import com.ato.recordodon.screens.main.MainScreen
import com.ato.recordodon.screens.recorder.RecorderScreen
import com.ato.recordodon.screens.settings.SettingsScreen
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
                MainScreen(
                    recordings = emptyList(),
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
