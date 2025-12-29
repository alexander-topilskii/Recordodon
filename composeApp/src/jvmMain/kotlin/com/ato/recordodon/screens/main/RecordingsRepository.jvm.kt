package com.ato.recordodon.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File

@Composable
actual fun rememberRecordingsRepository(): RecordingsRepository {
    return remember {
        JvmRecordingsRepository()
    }
}

private class JvmRecordingsRepository : RecordingsRepository {
    override fun loadRecordings(): List<String> {
        val directory = File(System.getProperty("user.home"), "Recordodon")
        if (!directory.exists()) return emptyList()
        return directory.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() == "wav" }
            ?.sortedByDescending { it.lastModified() }
            ?.map { it.name }
            ?: emptyList()
    }
}
