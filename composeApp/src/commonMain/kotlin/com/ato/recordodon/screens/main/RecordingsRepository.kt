package com.ato.recordodon.screens.main

import androidx.compose.runtime.Composable

interface RecordingsRepository {
    fun loadRecordings(): List<String>
}

@Composable
expect fun rememberRecordingsRepository(): RecordingsRepository
