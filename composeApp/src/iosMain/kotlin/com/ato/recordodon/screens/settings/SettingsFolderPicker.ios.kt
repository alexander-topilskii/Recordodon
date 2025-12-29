package com.ato.recordodon.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerModeOpen
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject

@Composable
actual fun rememberSettingsFolderPicker(
    onFolderPicked: (String) -> Unit,
): SettingsFolderPicker {
    val currentOnFolderPicked by rememberUpdatedState(onFolderPicked)
    return remember {
        object : SettingsFolderPicker {
            private val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAtURLs: List<*>,
                ) {
                    val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                    val path = url?.path
                    if (path != null) {
                        currentOnFolderPicked(path)
                    }
                }
            }

            override fun launchPickFolder() {
                val picker = UIDocumentPickerViewController(
                    documentTypes = listOf("public.folder"),
                    inMode = UIDocumentPickerModeOpen,
                )
                picker.delegate = delegate
                picker.allowsMultipleSelection = false
                val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
                rootViewController?.presentViewController(
                    picker,
                    animated = true,
                    completion = null,
                )
            }
        }
    }
}
