package com.ato.recordodon.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import javax.swing.JFileChooser

@Composable
actual fun rememberSettingsFolderPicker(
    onFolderPicked: (String) -> Unit,
): SettingsFolderPicker {
    val currentOnFolderPicked by rememberUpdatedState(onFolderPicked)
    return remember {
        object : SettingsFolderPicker {
            override fun launchPickFolder() {
                val chooser = JFileChooser().apply {
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    isAcceptAllFileFilterUsed = false
                }
                val result = chooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    currentOnFolderPicked(chooser.selectedFile.absolutePath)
                }
            }
        }
    }
}
