package com.ato.recordodon.screens.recorder

actual class RecorderAudioEngine actual constructor() {
    actual val isRecording: Boolean = false
    actual val isPaused: Boolean = false

    actual fun start(
        outputFilePath: String,
        onAmplitude: (Int) -> Unit,
    ): Boolean {
        return false
    }

    actual fun pause() {
    }

    actual fun resume() {
    }

    actual fun stop() {
    }

    actual fun addMarker() {
    }

    actual fun release() {
    }
}
