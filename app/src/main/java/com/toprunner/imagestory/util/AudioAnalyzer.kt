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
import com.toprunner.imagestory.model.VoiceFeatures
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.CountDownLatch
import kotlin.math.pow
import kotlin.math.sqrt

class AudioAnalyzer(private val context: Context) {
    private val TAG = "AudioAnalyzer"


    // FloatArray를 DoubleArray로 변환하는 확장 함수
    private fun FloatArray.toDoubleArray(): DoubleArray {
        return DoubleArray(this.size) { i -> this[i].toDouble() }
    }

    fun analyzeAudioFile(audioFilePath: String): VoiceFeatures {
        val pitchValues = mutableListOf<Float>()
        val mfccFrames = mutableListOf<DoubleArray>()
        val latch = CountDownLatch(1)

        try {
            Log.d(TAG, "Analyzing audio file: $audioFilePath")
            val file = File(audioFilePath)
            if (!file.exists()) {
                Log.e(TAG, "Audio file does not exist: $audioFilePath")
                return getDefaultVoiceFeatures()
            }

            // 오디오 형식 정의
            val audioFormat = TarsosDSPAudioFormat(
                44100f, // 샘플레이트
                16,     // 비트뎁스
                1,      // 채널 수 (모노)
                true,   // signed
                false   // bigEndian
            )

            // fromPipe() 대신 fromFile() 사용
            val bufferSize = 2048
            val bufferOverlap = 1024

            // 파일에서 직접 디스패처 생성
            val dispatcher = AudioDispatcherFactory.fromFile(
                File(audioFilePath),
                bufferSize,
                bufferOverlap
            )
            // 피치 감지기 설정
            val pitchProcessor = PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.YIN,
                audioFormat.sampleRate,
                bufferSize,
                PitchDetectionHandler { result, _ ->
                    val pitchInHz = result.pitch
                    if (pitchInHz != -1f) {
                        pitchValues.add(pitchInHz)
                    }
                }
            )

            // MFCC 처리기 설정
            val mfccProcessor = MFCC(
                bufferSize,
                audioFormat.sampleRate,
                13,
                40,
                50F,
                audioFormat.sampleRate / 2
            )

            // 오디오 처리기 추가
            dispatcher.addAudioProcessor(pitchProcessor)
            dispatcher.addAudioProcessor(mfccProcessor)

            // MFCC 값 캡처를 위한 처리기 추가
            dispatcher.addAudioProcessor(object : AudioProcessor {
                override fun process(audioEvent: AudioEvent?): Boolean {
                    // FloatArray를 DoubleArray로 변환
                    val mfccValues = mfccProcessor.mfcc.clone().toDoubleArray()
                    mfccFrames.add(mfccValues)
                    return true
                }

                override fun processingFinished() {
                    dispatcher.stop()
                    latch.countDown()
                }
            })

            // 별도 스레드에서 오디오 처리 시작
            val thread = Thread(dispatcher)
            thread.start()

            // 처리 완료 대기
            latch.await()

            // 수집된 값으로 VoiceFeatures 객체 생성
            if (pitchValues.isNotEmpty()) {
                val averagePitch = pitchValues.average()
                val pitchStdDev = calculatePitchStdDev(pitchValues, averagePitch)

                // MFCC 프레임 샘플링
                val sampledMfccs = sampleMfccFrames(mfccFrames)

                Log.d(TAG, "Analysis completed. Average pitch: $averagePitch Hz, StdDev: $pitchStdDev")

                return VoiceFeatures(
                    averagePitch = averagePitch,
                    pitchStdDev = pitchStdDev,
                    mfccValues = sampledMfccs
                )
            } else {
                Log.w(TAG, "No valid pitch values detected in the audio file")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing audio: ${e.message}", e)
        }

        // 오류 발생 또는 유효한 피치가 없는 경우 기본값 반환
        return getDefaultVoiceFeatures()
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

    private fun getDefaultVoiceFeatures(): VoiceFeatures {
        return VoiceFeatures(
            averagePitch = 120.0,
            pitchStdDev = 15.0,
            mfccValues = List(5) { DoubleArray(13) { 0.0 } }
        )
    }
}