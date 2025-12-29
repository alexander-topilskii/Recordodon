package com.ato.recordodon.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberRecordingsRepository(): RecordingsRepository {
    return remember {
        WebRecordingsRepository()
    }
}

private class WebRecordingsRepository : RecordingsRepository {
    override fun loadRecordings(): List<String> {
        return emptyList()
    }
}
