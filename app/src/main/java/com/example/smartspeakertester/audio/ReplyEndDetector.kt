package com.example.smartspeakertester.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.sqrt

sealed class ReplyEndResult {
    object Completed : ReplyEndResult()
    object Timeout : ReplyEndResult()
    data class Error(val reason: String) : ReplyEndResult()
}

open class ReplyEndDetector(
    private val sampleRate: Int = 16000,
    private val channel: Int = AudioFormat.CHANNEL_IN_MONO,
    private val encoding: Int = AudioFormat.ENCODING_PCM_16BIT,
    private val minReplyListenMs: Long = 800,
    private val maxReplyListenMs: Long = 12000,
    private val silenceEndMs: Long = 1200,
    private val ambientNoiseCalibration: Double = 6.0
) {
    open suspend fun detectReplyEnd(): ReplyEndResult = withContext(Dispatchers.IO) {
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
        if (bufferSize <= 0) return@withContext ReplyEndResult.Error("Buffer size error")
        val record = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, channel, encoding, bufferSize)
        try {
            record.startRecording()
            val ambient = calibrateAmbientRms(record, bufferSize)
            val silenceThreshold = ambient * ambientNoiseCalibration
            val data = ShortArray(bufferSize)
            var silenceDuration = 0L
            val start = System.currentTimeMillis()
            while (currentCoroutineContext().isActive) {
                val read = record.read(data, 0, data.size)
                if (read <= 0) continue
                val rms = rms(data, read)
                val now = System.currentTimeMillis()
                val elapsed = now - start
                if (elapsed < minReplyListenMs) {
                    silenceDuration = 0
                } else {
                    silenceDuration = if (rms < silenceThreshold) silenceDuration + 50L else 0
                }
                if (silenceDuration >= silenceEndMs) {
                    return@withContext ReplyEndResult.Completed
                }
                if (elapsed >= maxReplyListenMs) {
                    return@withContext ReplyEndResult.Timeout
                }
                delay(50L)
            }
            ReplyEndResult.Timeout
        } catch (t: Throwable) {
            ReplyEndResult.Error(t.message ?: "Audio error")
        } finally {
            try {
                record.stop()
                record.release()
            } catch (_: Throwable) {
            }
        }
    }

    private suspend fun calibrateAmbientRms(record: AudioRecord, bufferSize: Int): Double {
        val data = ShortArray(bufferSize)
        var total = 0.0
        var samples = 0
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < 400) {
            val read = record.read(data, 0, data.size)
            if (read > 0) {
                total += rms(data, read)
                samples++
            }
            delay(20L)
        }
        return if (samples == 0) 1.0 else total / samples
    }

    private fun rms(buffer: ShortArray, read: Int): Double {
        var sum = 0.0
        for (i in 0 until read) {
            sum += buffer[i].toDouble().pow(2.0)
        }
        val mean = sum / read.toDouble()
        return sqrt(mean)
    }
}
