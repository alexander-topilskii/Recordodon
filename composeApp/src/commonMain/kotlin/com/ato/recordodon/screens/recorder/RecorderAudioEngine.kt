package com.ato.recordodon.screens.recorder

expect class RecorderAudioEngine() {
    val isRecording: Boolean
    val isPaused: Boolean

    fun start(
        outputFilePath: String,
        onAmplitude: (Int) -> Unit,
    ): Boolean

    fun pause()

    fun resume()

    fun stop()

    fun addMarker()

    fun release()
}
