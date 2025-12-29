package com.ato.recordodon.screens.main

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
actual fun rememberRecordingsRepository(): RecordingsRepository {
    val context = LocalContext.current
    return remember(context) {
        AndroidRecordingsRepository(context)
    }
}

private class AndroidRecordingsRepository(
    private val context: Context,
) : RecordingsRepository {
    override fun loadRecordings(): List<String> {
        val directory = File(context.getExternalFilesDir(null), "recordings")
        if (!directory.exists()) return emptyList()
        return directory.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() == "wav" }
            ?.sortedByDescending { it.lastModified() }
            ?.map { it.name }
            ?: emptyList()
    }
}
