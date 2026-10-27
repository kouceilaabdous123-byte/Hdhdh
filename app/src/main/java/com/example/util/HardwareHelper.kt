package com.example.util

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.sin

class HardwareHelper(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var strobeJob: Job? = null
    private var torchCameraId: String? = null

    init {
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK && torchCameraId == null) {
                    torchCameraId = id
                }
            }
            if (torchCameraId == null && (cameraManager?.cameraIdList?.isNotEmpty() == true)) {
                torchCameraId = cameraManager.cameraIdList[0]
            }
        } catch (_: Exception) {
            // Flash not available
        }
    }

    fun hasFlashlight(): Boolean = torchCameraId != null

    fun setTorch(enabled: Boolean): Boolean {
        strobeJob?.cancel()
        strobeJob = null
        val id = torchCameraId ?: return false
        return try {
            cameraManager?.setTorchMode(id, enabled)
            true
        } catch (_: CameraAccessException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    fun startStrobe(scope: CoroutineScope, delayMs: Long = 100) {
        val id = torchCameraId ?: return
        strobeJob?.cancel()
        strobeJob = scope.launch(Dispatchers.Default) {
            var state = false
            try {
                while (isActive) {
                    state = !state
                    try {
                        cameraManager?.setTorchMode(id, state)
                    } catch (_: Exception) {
                        break
                    }
                    delay(delayMs)
                }
            } finally {
                try {
                    cameraManager?.setTorchMode(id, false)
                } catch (_: Exception) {}
            }
        }
    }

    fun startSos(scope: CoroutineScope) {
        val id = torchCameraId ?: return
        strobeJob?.cancel()
        strobeJob = scope.launch(Dispatchers.Default) {
            // S-O-S: 3 short, 3 long, 3 short
            val pattern = listOf(
                150L, 100L, 150L, 100L, 150L, // S
                300L,
                400L, 150L, 400L, 150L, 400L, // O
                300L,
                150L, 100L, 150L, 100L, 150L, // S
                800L
            )
            try {
                while (isActive) {
                    for (i in pattern.indices step 2) {
                        if (!isActive) break
                        cameraManager?.setTorchMode(id, true)
                        delay(pattern[i])
                        cameraManager?.setTorchMode(id, false)
                        if (i + 1 < pattern.size) {
                            delay(pattern[i + 1])
                        }
                    }
                }
            } finally {
                try {
                    cameraManager?.setTorchMode(id, false)
                } catch (_: Exception) {}
            }
        }
    }

    fun stopTorch() {
        strobeJob?.cancel()
        strobeJob = null
        torchCameraId?.let {
            try {
                cameraManager?.setTorchMode(it, false)
            } catch (_: Exception) {}
        }
    }

    // --- Vibration ---
    fun hasVibrator(): Boolean = vibrator?.hasVibrator() == true

    fun vibrateShort() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(100)
        }
    }

    fun vibrateMedium() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(350)
        }
    }

    fun vibrateLong() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(800)
        }
    }

    fun vibrateSos() {
        val timings = longArrayOf(0, 150, 100, 150, 100, 150, 300, 400, 150, 400, 150, 400, 300, 150, 100, 150, 100, 150)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(timings, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(timings, -1)
        }
    }

    // --- Audio Tone Generator ---
    private var activeTrack: AudioTrack? = null

    fun playTone(freqHz: Int, durationMs: Int = 1200, channelPan: Float = 0f /* -1 left, 1 right */) {
        stopTone()
        val sampleRate = 44100
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val generatedSnd = ByteArray(2 * numSamples * 2) // 16-bit stereo

        var sampleIndex = 0
        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i / (sampleRate / freqHz.toDouble())
            // Apply slight envelope at start and end to avoid audio clicks
            val envelope = when {
                i < 441 -> i / 441.0
                i > numSamples - 441 -> (numSamples - i) / 441.0
                else -> 1.0
            }
            val sampleVal = (sin(angle) * 32767.0 * envelope).toInt().toShort()

            val leftGain = if (channelPan <= 0f) 1.0f else (1.0f - channelPan)
            val rightGain = if (channelPan >= 0f) 1.0f else (1.0f + channelPan)

            val leftVal = (sampleVal * leftGain).toInt().toShort()
            val rightVal = (sampleVal * rightGain).toInt().toShort()

            // Left channel
            generatedSnd[sampleIndex++] = (leftVal.toInt() and 0x00ff).toByte()
            generatedSnd[sampleIndex++] = ((leftVal.toInt() and 0xff00) ushr 8).toByte()
            // Right channel
            generatedSnd[sampleIndex++] = (rightVal.toInt() and 0x00ff).toByte()
            generatedSnd[sampleIndex++] = ((rightVal.toInt() and 0xff00) ushr 8).toByte()
        }

        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(generatedSnd.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(generatedSnd, 0, generatedSnd.size)
            track.play()
            activeTrack = track
        } catch (_: Exception) {}
    }

    fun stopTone() {
        try {
            activeTrack?.stop()
            activeTrack?.release()
        } catch (_: Exception) {}
        activeTrack = null
    }

    // --- Audio Record Test ---
    data class AudioRecordResult(val samples: ShortArray, val sampleRate: Int)

    suspend fun recordAudioSnippet(
        durationMs: Int = 3000,
        onDecibelUpdate: (Float) -> Unit
    ): AudioRecordResult? = withContext(Dispatchers.IO) {
        val sampleRate = 44100
        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBufferSize <= 0) return@withContext null

        var recorder: AudioRecord? = null
        try {
            recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 2
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext null
            }

            recorder.startRecording()
            val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val recordedData = ShortArray(totalSamples)
            var offset = 0
            val readBuffer = ShortArray(minBufferSize)

            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < durationMs && offset < totalSamples) {
                val read = recorder.read(readBuffer, 0, minBufferSize.coerceAtMost(totalSamples - offset))
                if (read > 0) {
                    System.arraycopy(readBuffer, 0, recordedData, offset, read)
                    offset += read

                    // Calculate RMS decibels
                    var sum = 0.0
                    for (i in 0 until read) {
                        sum += readBuffer[i] * readBuffer[i]
                    }
                    val rms = Math.sqrt(sum / read)
                    val db = (20 * Math.log10(rms.coerceAtLeast(1.0))).toFloat()
                    onDecibelUpdate(db.coerceIn(0f, 100f))
                }
            }

            AudioRecordResult(recordedData, sampleRate)
        } catch (_: Exception) {
            null
        } finally {
            try {
                recorder?.stop()
                recorder?.release()
            } catch (_: Exception) {}
        }
    }

    fun playRecordedSnippet(snippet: AudioRecordResult) {
        stopTone()
        val sampleRate = snippet.sampleRate
        val bufferSize = snippet.samples.size * 2
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(snippet.samples, 0, snippet.samples.size)
            track.play()
            activeTrack = track
        } catch (_: Exception) {}
    }

    // --- System Hardware Specs ---
    fun getSystemInfo(): SystemHardwareInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        activityManager?.getMemoryInfo(memoryInfo)

        val totalRamMb = (memoryInfo.totalMem / (1024 * 1024)).toInt()
        val availRamMb = (memoryInfo.availMem / (1024 * 1024)).toInt()
        val usedRamMb = totalRamMb - availRamMb

        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availBlocks = stat.availableBlocksLong

        val totalStorageGb = (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
        val freeStorageGb = (availBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
        val usedStorageGb = totalStorageGb - freeStorageGb

        val cpuCores = Runtime.getRuntime().availableProcessors()

        return SystemHardwareInfo(
            brand = Build.BRAND.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            device = Build.DEVICE,
            board = Build.BOARD,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A",
            cpuCores = cpuCores,
            cpuAbis = Build.SUPPORTED_ABIS.joinToString(", "),
            totalRamMb = totalRamMb,
            usedRamMb = usedRamMb,
            freeRamMb = availRamMb,
            totalStorageGb = String.format("%.1f", totalStorageGb),
            usedStorageGb = String.format("%.1f", usedStorageGb),
            freeStorageGb = String.format("%.1f", freeStorageGb)
        )
    }
}

data class SystemHardwareInfo(
    val brand: String,
    val model: String,
    val manufacturer: String,
    val device: String,
    val board: String,
    val androidVersion: String,
    val sdkInt: Int,
    val securityPatch: String,
    val cpuCores: Int,
    val cpuAbis: String,
    val totalRamMb: Int,
    val usedRamMb: Int,
    val freeRamMb: Int,
    val totalStorageGb: String,
    val usedStorageGb: String,
    val freeStorageGb: String
)

data class BatteryDiagnosticData(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val chargeSource: String = "Débranché",
    val status: String = "Inconnu",
    val health: String = "Bonne",
    val temperatureC: Float = 0f,
    val voltageMv: Int = 0,
    val technology: String = "Li-ion",
    val isPresent: Boolean = true
)
