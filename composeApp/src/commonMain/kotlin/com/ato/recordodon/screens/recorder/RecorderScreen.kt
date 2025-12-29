package com.ato.recordodon.screens.recorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderScreen(
    onBack: () -> Unit,
    viewModel: RecorderViewModel = viewModel { RecorderViewModel() },
) {
    val permissionState = rememberRecorderPermissionState()
    val fileProvider = rememberRecorderFileProvider()
    var hasRequestedPermission by remember { mutableStateOf(false) }

    LaunchedEffect(permissionState.isGranted) {
        if (!permissionState.isGranted && !hasRequestedPermission) {
            hasRequestedPermission = true
            permissionState.requestPermission()
        }
    }

    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Запись") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Назад")
                    }
                },
            )
        },
    ) { padding ->
        if (!permissionState.isGranted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Для записи нужен доступ к микрофону.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(onClick = permissionState::openSettings) {
                    Text("В настройки")
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = { viewModel.startRecording(fileProvider.createOutputFilePath()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isRecording,
            ) {
                Text("Начать запись")
            }

            AudioHistogram(
                amplitudes = uiState.amplitudes,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val isPauseEnabled = uiState.isRecording
                val pauseLabel = if (uiState.isPaused) "Продолжить" else "Пауза"
                Button(
                    onClick = {
                        if (uiState.isPaused) {
                            viewModel.resumeRecording()
                        } else {
                            viewModel.pauseRecording()
                        }
                    },
                    enabled = isPauseEnabled,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(pauseLabel)
                }
                Button(
                    onClick = viewModel::stopRecording,
                    enabled = uiState.isRecording,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Стоп")
                }
            }

            Button(
                onClick = viewModel::addMarker,
                enabled = uiState.isRecording,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Маркер (${uiState.markerCount})")
            }

            if (uiState.outputPath != null) {
                Text(
                    text = "Файл: ${uiState.outputPath}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun AudioHistogram(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val barCount = max(1, amplitudes.size)
        val barWidth = size.width / barCount
        val centerY = size.height / 2f
        amplitudes.forEachIndexed { index, amplitude ->
            val barHeight = amplitude.coerceIn(0f, 1f) * size.height
            val x = index * barWidth + barWidth / 2f
            drawLine(
                color = Color(0xFF4CAF50),
                start = Offset(x, centerY - barHeight / 2f),
                end = Offset(x, centerY + barHeight / 2f),
                strokeWidth = barWidth * 0.6f,
                cap = StrokeCap.Round,
            )
        }
    }
}
