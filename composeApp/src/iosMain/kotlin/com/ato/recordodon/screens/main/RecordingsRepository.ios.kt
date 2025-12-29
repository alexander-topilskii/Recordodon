package com.ato.recordodon.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory

@Composable
actual fun rememberRecordingsRepository(): RecordingsRepository {
    return remember {
        IosRecordingsRepository()
    }
}

private class IosRecordingsRepository : RecordingsRepository {
    override fun loadRecordings(): List<String> {
        val directory = NSTemporaryDirectory()
        val fileManager = NSFileManager.defaultManager
        val contents = fileManager.contentsOfDirectoryAtPath(
            path = directory,
            error = null,
        ) as? List<String> ?: return emptyList()
        return contents
            .filter { it.endsWith(".wav") }
            .sortedDescending()
    }
}
