package com.ato.recordodon.screens.recorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

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
                val tempDir = NSTemporaryDirectory()
                val fileName = "recording_${NSUUID().UUIDString}.wav"
                return "$tempDir/$fileName"
            }
        }
    }
}
