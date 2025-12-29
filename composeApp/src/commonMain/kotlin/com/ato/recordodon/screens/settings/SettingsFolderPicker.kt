package com.ato.recordodon.screens.settings

import androidx.compose.runtime.Composable

interface SettingsFolderPicker {
    fun launchPickFolder()
}

@Composable
expect fun rememberSettingsFolderPicker(
    onFolderPicked: (String) -> Unit,
): SettingsFolderPicker
