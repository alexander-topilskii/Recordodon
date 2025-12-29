package com.ato.recordodon.screens.recorder

import androidx.compose.runtime.Composable

interface RecorderPermissionState {
    val isGranted: Boolean
    fun requestPermission()
    fun openSettings()
}

@Composable
expect fun rememberRecorderPermissionState(): RecorderPermissionState

interface RecorderFileProvider {
    fun createOutputFilePath(): String
}

@Composable
expect fun rememberRecorderFileProvider(): RecorderFileProvider
