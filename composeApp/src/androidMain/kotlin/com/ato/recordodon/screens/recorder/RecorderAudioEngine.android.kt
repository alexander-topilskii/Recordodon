package com.ato.recordodon.screens.recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.max

actual class RecorderAudioEngine actual constructor() {
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val mainHandler = Handler(Looper.getMainLooper())

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var pcmFile: File? = null
    private var outputFilePath: String? = null
    private var amplitudeCallback: ((Int) -> Unit)? = null
    private var recordedSamples: Long = 0
    private val markerSamples = mutableListOf<Long>()

    private val isRecordingFlag = AtomicBoolean(false)
    private val isPausedFlag = AtomicBoolean(false)

    actual val isRecording: Boolean
        get() = isRecordingFlag.get()

    actual val isPaused: Boolean
        get() = isPausedFlag.get()

    actual fun start(
        outputFilePath: String,
        onAmplitude: (Int) -> Unit,
    ): Boolean {
        if (isRecording) return false
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = max(minBufferSize, sampleRate / 10)
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize,
        )
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            return false
        }

        audioRecord = recorder
        this.outputFilePath = outputFilePath
        pcmFile = File("$outputFilePath.pcm")
        pcmFile?.parentFile?.mkdirs()
        amplitudeCallback = onAmplitude
        recordedSamples = 0
        markerSamples.clear()

        isRecordingFlag.set(true)
        isPausedFlag.set(false)
        recorder.startRecording()

        val pcmFileLocal = pcmFile ?: return false
        recordingThread = Thread {
            val buffer = ShortArray(bufferSize / 2)
            FileOutputStream(pcmFileLocal).use { output ->
                while (isRecordingFlag.get()) {
                    if (isPausedFlag.get()) {
                        try {
                            Thread.sleep(60)
                        } catch (_: InterruptedException) {
                        }
                        continue
                    }
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val byteBuffer = ByteBuffer
                            .allocate(read * 2)
                            .order(ByteOrder.LITTLE_ENDIAN)
                        var maxAmplitude = 0
                        for (index in 0 until read) {
                            val sample = buffer[index].toInt()
                            byteBuffer.putShort(sample.toShort())
                            maxAmplitude = max(maxAmplitude, abs(sample))
                        }
                        output.write(byteBuffer.array())
                        recordedSamples += read
                        mainHandler.post { amplitudeCallback?.invoke(maxAmplitude) }
                    }
                }
                output.flush()
            }
        }.apply { start() }
        return true
    }

    actual fun pause() {
        if (!isRecording || isPaused) return
        isPausedFlag.set(true)
        audioRecord?.stop()
    }

    actual fun resume() {
        if (!isRecording || !isPaused) return
        audioRecord?.startRecording()
        isPausedFlag.set(false)
    }

    actual fun stop() {
        if (!isRecording) return
        isRecordingFlag.set(false)
        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
        } catch (_: IllegalStateException) {
        }
        audioRecord?.release()
        audioRecord = null
        recordingThread?.join()
        recordingThread = null

        val pcm = pcmFile
        val output = outputFilePath
        if (pcm != null && output != null) {
            val wavFile = File(output)
            wavFile.parentFile?.mkdirs()
            writeWavFile(pcm, wavFile)
            pcm.delete()
        }
    }

    actual fun addMarker() {
        if (!isRecording) return
        markerSamples.add(recordedSamples)
    }

    actual fun release() {
        stop()
        amplitudeCallback = null
    }

    private fun writeWavFile(pcmFile: File, wavFile: File) {
        val dataSize = pcmFile.length()
        val hasMarkers = markerSamples.isNotEmpty()
        val cueChunkSize = if (hasMarkers) 4 + markerSamples.size * 24 else 0
        val riffChunkSize = 4 + (8 + 16) + (if (hasMarkers) 8 + cueChunkSize else 0) + (8 + dataSize)

        FileOutputStream(wavFile).use { output ->
            output.write("RIFF".toByteArray())
            output.writeIntLE(riffChunkSize.toInt())
            output.write("WAVE".toByteArray())

            output.write("fmt ".toByteArray())
            output.writeIntLE(16)
            output.writeShortLE(1)
            output.writeShortLE(1)
            output.writeIntLE(sampleRate)
            output.writeIntLE(sampleRate * 2)
            output.writeShortLE(2)
            output.writeShortLE(16)

            if (hasMarkers) {
                output.write("cue ".toByteArray())
                output.writeIntLE(cueChunkSize)
                output.writeIntLE(markerSamples.size)
                markerSamples.forEachIndexed { index, sampleOffset ->
                    output.writeIntLE(index + 1)
                    output.writeIntLE(sampleOffset.toInt())
                    output.write("data".toByteArray())
                    output.writeIntLE(0)
                    output.writeIntLE(0)
                    output.writeIntLE(sampleOffset.toInt())
                }
            }

            output.write("data".toByteArray())
            output.writeIntLE(dataSize.toInt())
            FileInputStream(pcmFile).use { input ->
                input.copyTo(output)
            }
        }
    }
}

private fun OutputStream.writeIntLE(value: Int) {
    write(value and 0xFF)
    write(value shr 8 and 0xFF)
    write(value shr 16 and 0xFF)
    write(value shr 24 and 0xFF)
}

private fun OutputStream.writeShortLE(value: Int) {
    write(value and 0xFF)
    write(value shr 8 and 0xFF)
}
