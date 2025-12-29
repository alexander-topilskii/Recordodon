package com.ato.recordodon.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberSettingsFolderPicker(
    onFolderPicked: (String) -> Unit,
): SettingsFolderPicker {
    val context = LocalContext.current
    val currentOnFolderPicked by rememberUpdatedState(onFolderPicked)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            val flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            currentOnFolderPicked(uri.toString())
        }
    }

    return remember {
        object : SettingsFolderPicker {
            override fun launchPickFolder() {
                launcher.launch(null)
            }
        }
    }
}
