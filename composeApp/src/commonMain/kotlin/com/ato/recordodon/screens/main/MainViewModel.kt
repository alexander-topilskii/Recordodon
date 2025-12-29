package com.ato.recordodon.screens.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class MainUiState(
    val recordings: List<String> = emptyList(),
)

class MainViewModel(
    private val recordingsRepository: RecordingsRepository,
) : ViewModel() {
    var uiState by mutableStateOf(MainUiState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        uiState = uiState.copy(recordings = recordingsRepository.loadRecordings())
    }
}
