package com.ato.recordodon.screens.recorder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

private const val MaxHistogramBars = 40

data class RecorderUiState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val amplitudes: List<Float> = emptyList(),
    val markerCount: Int = 0,
    val outputPath: String? = null,
)

class RecorderViewModel(
    private val audioEngine: RecorderAudioEngine = RecorderAudioEngine(),
) : ViewModel() {
    private val amplitudeHistory = mutableStateListOf<Float>()
    private var markerCountInternal = 0

    var uiState by mutableStateOf(RecorderUiState())
        private set

    fun startRecording(outputFilePath: String) {
        if (audioEngine.isRecording) return
        amplitudeHistory.clear()
        markerCountInternal = 0
        val started = audioEngine.start(outputFilePath) { amplitude ->
            val normalized = (amplitude / Short.MAX_VALUE.toFloat()).coerceIn(0f, 1f)
            if (amplitudeHistory.size >= MaxHistogramBars) {
                amplitudeHistory.removeAt(0)
            }
            amplitudeHistory.add(normalized)
            uiState = uiState.copy(amplitudes = amplitudeHistory.toList())
        }
        if (started) {
            uiState = RecorderUiState(
                isRecording = true,
                isPaused = false,
                amplitudes = amplitudeHistory.toList(),
                markerCount = markerCountInternal,
                outputPath = outputFilePath,
            )
        }
    }

    fun pauseRecording() {
        if (!audioEngine.isRecording || audioEngine.isPaused) return
        audioEngine.pause()
        uiState = uiState.copy(isPaused = true)
    }

    fun resumeRecording() {
        if (!audioEngine.isRecording || !audioEngine.isPaused) return
        audioEngine.resume()
        uiState = uiState.copy(isPaused = false)
    }

    fun stopRecording() {
        if (!audioEngine.isRecording) return
        audioEngine.stop()
        uiState = uiState.copy(isRecording = false, isPaused = false)
    }

    fun addMarker() {
        if (!audioEngine.isRecording) return
        audioEngine.addMarker()
        markerCountInternal += 1
        uiState = uiState.copy(markerCount = markerCountInternal)
    }

    override fun onCleared() {
        audioEngine.release()
        super.onCleared()
    }
}
