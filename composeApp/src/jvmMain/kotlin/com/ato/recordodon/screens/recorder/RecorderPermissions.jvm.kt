package com.ato.recordodon.screens.recorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
actual fun rememberRecorderPermissionState(): RecorderPermissionState {
    return remember {
        object : RecorderPermissionState {
            override val isGranted: Boolean = true

            override fun requestPermission() {
            }

            override fun openSettings() {
            }
        }
    }
}

@Composable
actual fun rememberRecorderFileProvider(): RecorderFileProvider {
    return remember {
        object : RecorderFileProvider {
            override fun createOutputFilePath(): String {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val directory = File(System.getProperty("user.home"), "Recordodon")
                directory.mkdirs()
                return File(directory, "recording_$timestamp.wav").absolutePath
            }
        }
    }
}
