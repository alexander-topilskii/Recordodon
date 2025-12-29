package com.ato.recordodon.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class SettingsUiState(
    val saveFolder: String? = null,
)

class SettingsViewModel : ViewModel() {
    var uiState by mutableStateOf(SettingsUiState())
        private set

    fun updateSaveFolder(path: String) {
        uiState = uiState.copy(saveFolder = path)
    }
}
