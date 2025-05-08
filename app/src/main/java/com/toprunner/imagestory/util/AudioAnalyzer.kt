package com.toprunner.imagestory.util

import android.content.Context
import android.util.Log
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.mfcc.MFCC
import com.toprunner.imagestory.SimpleAudioAnalyzer
import com.toprunner.imagestory.model.VoiceFeatures
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.CountDownLatch
import kotlin.math.pow
import kotlin.math.sqrt

class AudioAnalyzer(private val context: Context) {
    private val TAG = "AudioAnalyzer"
    private val simpleAnalyzer = SimpleAudioAnalyzer(context)

    fun analyzeAudioFile(audioFilePath: String): VoiceFeatures {
        Log.d(TAG, "Analyzing audio file: $audioFilePath")

        // SimpleAudioAnalyzer로 분석 작업 위임
        return simpleAnalyzer.analyzeAudio(audioFilePath)
    }



    // FloatArray를 DoubleArray로 변환하는 확장 함수
    private fun FloatArray.toDoubleArray(): DoubleArray {
        return DoubleArray(this.size) { i -> this[i].toDouble() }
    }



    private fun calculatePitchStdDev(pitches: List<Float>, average: Double): Double {
        if (pitches.size <= 1) return 15.0 // 기본값

        val variance = pitches.sumOf { (it - average).pow(2) } / (pitches.size - 1)
        return sqrt(variance)
    }

    private fun sampleMfccFrames(frames: List<DoubleArray>, targetSize: Int = 5): List<DoubleArray> {
        if (frames.isEmpty()) {
            return List(targetSize) { DoubleArray(13) { 0.0 } }
        }

        val result = mutableListOf<DoubleArray>()

        // 고르게 샘플링
        val step = frames.size.toFloat() / targetSize
        for (i in 0 until targetSize) {
            val index = (i * step).toInt().coerceAtMost(frames.size - 1)
            result.add(frames[index])
        }

        return result
    }

    // 기본 VoiceFeatures 객체 반환 메소드
    private fun getDefaultVoiceFeatures(): VoiceFeatures {
        return VoiceFeatures(
            averagePitch = 150.0,
            pitchStdDev = 15.0,
            mfccValues = listOf(DoubleArray(13) { 0.0 })
        )
    }
}