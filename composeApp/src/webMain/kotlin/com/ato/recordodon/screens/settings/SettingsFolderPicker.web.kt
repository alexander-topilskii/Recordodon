package com.ato.recordodon.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement

@Composable
actual fun rememberSettingsFolderPicker(
    onFolderPicked: (String) -> Unit,
): SettingsFolderPicker {
    val currentOnFolderPicked by rememberUpdatedState(onFolderPicked)
    return remember {
        object : SettingsFolderPicker {
            override fun launchPickFolder() {
                val showDirectoryPicker = js("window.showDirectoryPicker")
                if (showDirectoryPicker != null) {
                    val promise = js("window.showDirectoryPicker()")
                    promise.then({ handle: dynamic ->
                        val name = handle?.name as? String
                        if (name != null) {
                            currentOnFolderPicked(name)
                        }
                    })
                    return
                }

                val input = document.createElement("input") as HTMLInputElement
                input.type = "file"
                input.setAttribute("webkitdirectory", "")
                input.setAttribute("directory", "")
                input.onchange = {
                    val file = input.files?.item(0)
                    val relativePath = file?.asDynamic()?.webkitRelativePath as? String
                    val folderName = relativePath?.substringBefore("/") ?: file?.name
                    if (folderName != null) {
                        currentOnFolderPicked(folderName)
                    }
                    null
                }
                input.click()
            }
        }
    }
}
