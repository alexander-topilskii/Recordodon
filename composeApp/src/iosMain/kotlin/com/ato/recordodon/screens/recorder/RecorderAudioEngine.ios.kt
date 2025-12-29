package com.ato.recordodon.screens.recorder

import kotlin.math.pow
import platform.AVFoundation.AVAudioRecorder
import platform.AVFoundation.AVAudioSession
import platform.AVFoundation.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFoundation.AVAudioSessionCategoryPlayAndRecord
import platform.AVFoundation.AVAudioSessionModeDefault
import platform.AVFoundation.AVAudioSessionRecordPermissionGranted
import platform.AVFoundation.AVAudioSessionRecordPermissionUndetermined
import platform.AVFoundation.AVFormatIDKey
import platform.AVFoundation.AVLinearPCMBitDepthKey
import platform.AVFoundation.AVLinearPCMIsBigEndianKey
import platform.AVFoundation.AVLinearPCMIsFloatKey
import platform.AVFoundation.AVNumberOfChannelsKey
import platform.AVFoundation.AVSampleRateKey
import platform.AudioToolbox.kAudioFormatLinearPCM
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSRunLoop
import platform.Foundation.NSURL
import platform.Foundation.NSTimer
import platform.Foundation.NSDefaultRunLoopMode

actual class RecorderAudioEngine actual constructor() {
    private val sampleRate = 44100.0
    private val meterIntervalSeconds = 0.1

    private var recorder: AVAudioRecorder? = null
    private var meterTimer: NSTimer? = null
    private var amplitudeCallback: ((Int) -> Unit)? = null
    private val markerSamples = mutableListOf<Long>()

    private var isRecordingFlag: Boolean = false
    private var isPausedFlag: Boolean = false

    actual val isRecording: Boolean
        get() = isRecordingFlag

    actual val isPaused: Boolean
        get() = isPausedFlag

    actual fun start(
        outputFilePath: String,
        onAmplitude: (Int) -> Unit,
    ): Boolean {
        if (isRecording) return false
        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            AVAudioSessionCategoryPlayAndRecord,
            mode = AVAudioSessionModeDefault,
            options = AVAudioSessionCategoryOptionDefaultToSpeaker,
            error = null,
        )
        session.setActive(true, error = null)

        if (session.recordPermission != AVAudioSessionRecordPermissionGranted) {
            if (session.recordPermission == AVAudioSessionRecordPermissionUndetermined) {
                session.requestRecordPermission { _ -> }
            }
            return false
        }

        val outputUrl = NSURL.fileURLWithPath(outputFilePath)
        outputUrl.path?.let { path ->
            val parentPath = path.substringBeforeLast("/", "")
            if (parentPath.isNotEmpty()) {
                NSFileManager.defaultManager.createDirectoryAtPath(
                    parentPath,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null,
                )
            }
        }

        val settings = mapOf(
            AVFormatIDKey to NSNumber(kAudioFormatLinearPCM),
            AVSampleRateKey to NSNumber(sampleRate),
            AVNumberOfChannelsKey to NSNumber(1),
            AVLinearPCMBitDepthKey to NSNumber(16),
            AVLinearPCMIsBigEndianKey to NSNumber(false),
            AVLinearPCMIsFloatKey to NSNumber(false),
        )

        val recorder = AVAudioRecorder(outputUrl, settings, error = null) ?: return false
        recorder.meteringEnabled = true
        if (!recorder.prepareToRecord()) return false
        if (!recorder.record()) return false

        this.recorder = recorder
        amplitudeCallback = onAmplitude
        isRecordingFlag = true
        isPausedFlag = false
        markerSamples.clear()
        startMetering()
        return true
    }

    actual fun pause() {
        if (!isRecording || isPaused) return
        recorder?.pause()
        isPausedFlag = true
    }

    actual fun resume() {
        if (!isRecording || !isPaused) return
        recorder?.record()
        isPausedFlag = false
    }

    actual fun stop() {
        if (!isRecording) return
        recorder?.stop()
        recorder = null
        stopMetering()
        isRecordingFlag = false
        isPausedFlag = false
    }

    actual fun addMarker() {
        if (!isRecording) return
        val currentTime = recorder?.currentTime ?: 0.0
        markerSamples.add((currentTime * sampleRate).toLong())
    }

    actual fun release() {
        stop()
        amplitudeCallback = null
    }

    private fun startMetering() {
        stopMetering()
        meterTimer = NSTimer.scheduledTimerWithTimeInterval(
            meterIntervalSeconds,
            repeats = true,
        ) { _ ->
            val recorder = recorder ?: return@scheduledTimerWithTimeInterval
            if (!isRecordingFlag || isPausedFlag) return@scheduledTimerWithTimeInterval
            recorder.updateMeters()
            val power = recorder.averagePowerForChannel(0)
            amplitudeCallback?.invoke(powerToAmplitude(power))
        }
        meterTimer?.let { NSRunLoop.mainRunLoop.addTimer(it, forMode = NSDefaultRunLoopMode) }
    }

    private fun stopMetering() {
        meterTimer?.invalidate()
        meterTimer = null
    }

    private fun powerToAmplitude(powerDb: Float): Int {
        val normalized = 10.0.pow(powerDb / 20.0).coerceIn(0.0, 1.0)
        return (normalized * Short.MAX_VALUE).toInt()
    }
}
