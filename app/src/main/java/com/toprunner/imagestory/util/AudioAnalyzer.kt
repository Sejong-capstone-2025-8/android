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
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import kotlin.math.pow
import kotlin.math.sqrt

class AudioAnalyzer(private val context: Context) {
    private val TAG = "AudioAnalyzer"
    private val simpleAnalyzer = SimpleAudioAnalyzer(context)

    companion object {
        const val SAMPLE_RATE = 44100  // 44.1kHz는 일반적인 오디오 샘플링 레이트
    }

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
    // AudioAnalyzer.kt에 추가할 노이즈 제거 관련 함수들

    /**
     * 오디오 파일에서 노이즈를 제거하고 결과를 새 파일에 저장
     * @param audioFilePath 원본 오디오 파일 경로
     * @param outputFilePath 노이즈 제거된 결과 파일 경로
     * @return 성공 여부
     */
    fun reduceNoise(audioFilePath: String, outputFilePath: String): Boolean {
        Log.d(TAG, "Reducing noise in audio file: $audioFilePath")

        try {
            val simpleAnalyzer = SimpleAudioAnalyzer(context)

            // 1. 오디오 데이터 분석을 통해 간접적으로 오디오 데이터 얻기
            // (이 과정에서 내부적으로 extractPCMAudio 호출됨)
            val voiceFeatures = simpleAnalyzer.analyzeAudio(audioFilePath)

            // 2. 원본 파일을 읽어서 PCM 데이터로 변환
            val inputFile = File(audioFilePath)
            val audioBytes = inputFile.readBytes()

            // 3. PCM 데이터로 변환 (헤더가 있는 WAV 파일인 경우 헤더 건너뛰기)
            // WAV 헤더는 일반적으로 44바이트
            val pcmData = byteArrayToPCM(audioBytes, 44)

            // 4. 노이즈 제거 알고리즘 적용
            val processedPCM = applyNoiseReduction(pcmData)

            // 5. PCM 데이터를 WAV 파일로 저장
            return savePCMAudioToWav(processedPCM, outputFilePath, SAMPLE_RATE)

        } catch (e: Exception) {
            Log.e(TAG, "Error in noise reduction process: ${e.message}", e)
            return false
        }
    }
    private fun byteArrayToPCM(audioBytes: ByteArray, headerSize: Int = 0): ShortArray {
        val pcmData = ShortArray((audioBytes.size - headerSize) / 2)

        // 리틀 엔디안 형식으로 바이트 데이터를 쇼트(16비트) 데이터로 변환
        for (i in pcmData.indices) {
            val byteIndex = headerSize + i * 2
            if (byteIndex + 1 < audioBytes.size) {
                // 리틀 엔디안 변환 (첫 번째 바이트가 하위 바이트, 두 번째 바이트가 상위 바이트)
                pcmData[i] = (audioBytes[byteIndex].toInt() and 0xFF or
                        ((audioBytes[byteIndex + 1].toInt() and 0xFF) shl 8)).toShort()
            }
        }

        return pcmData
    }
    /**
     * 다양한 노이즈 제거 기법을 조합하여 적용
     */
    private fun applyNoiseReduction(audioData: ShortArray): ShortArray {
        // 음성 데이터 복사본 생성
        val processedData = audioData.copyOf()

        // 1. 초기 임계값 기반 노이즈 게이트 적용
        val gatedData = applyNoiseGate(processedData)

        // 2. 미디언 필터링으로 임펄스 노이즈 제거
        val medianFiltered = applyMedianFilter(gatedData)

        // 3. 모바일 평균 필터로 고주파 노이즈 완화
        return applyMovingAverageFilter(medianFiltered)
    }

    /**
     * 노이즈 게이트 적용 - 특정 임계값 이하의 신호를 감쇠시켜 배경 노이즈 제거
     */
    private fun applyNoiseGate(audioData: ShortArray): ShortArray {
        val result = ShortArray(audioData.size)

        // 에너지 레벨 계산을 위한 버퍼 크기
        val bufferSize = 2048
        val energyThresholdRatio = 0.05 // 전체 에너지의 5%를 임계값으로 사용

        // 전체 오디오 데이터의 평균 에너지 계산
        var totalEnergy = 0.0
        for (i in audioData.indices) {
            totalEnergy += audioData[i].toDouble().pow(2)
        }
        val averageEnergy = totalEnergy / audioData.size

        // 임계값 설정 (평균 에너지의 일정 비율)
        val threshold = (averageEnergy * energyThresholdRatio).toInt()

        // 구간별 에너지 계산 및 게이트 적용
        var i = 0
        while (i < audioData.size) {
            // 현재 버퍼의 에너지 계산
            var bufferEnergy = 0.0
            val endIndex = minOf(i + bufferSize, audioData.size)

            for (j in i until endIndex) {
                bufferEnergy += audioData[j].toDouble().pow(2)
            }
            bufferEnergy /= (endIndex - i)

            // 에너지가 임계값보다 낮으면 감쇠 적용
            val attenuationFactor = if (bufferEnergy < threshold) {
                0.1 // 90% 감쇠
            } else {
                1.0 // 원본 유지
            }

            // 감쇠 적용
            for (j in i until endIndex) {
                result[j] = (audioData[j] * attenuationFactor).toInt().toShort()
            }

            i += bufferSize
        }

        return result
    }

    /**
     * 미디언 필터 적용 - 임펄스 노이즈(뾰족한 스파이크) 제거에 효과적
     */
    private fun applyMedianFilter(audioData: ShortArray): ShortArray {
        val result = ShortArray(audioData.size)
        val filterSize = 5 // 필터 윈도우 크기 (홀수여야 함)
        val halfFilterSize = filterSize / 2

        // 경계 처리를 위한 패딩 적용
        val paddedData = ShortArray(audioData.size + filterSize - 1)
        // 왼쪽 패딩
        for (i in 0 until halfFilterSize) {
            paddedData[i] = audioData[0]
        }
        // 데이터 복사
        audioData.copyInto(paddedData, halfFilterSize)
        // 오른쪽 패딩
        for (i in 0 until halfFilterSize) {
            paddedData[paddedData.size - 1 - i] = audioData[audioData.size - 1]
        }

        // 필터 적용
        for (i in audioData.indices) {
            val window = ShortArray(filterSize)
            for (j in 0 until filterSize) {
                window[j] = paddedData[i + j]
            }
            window.sort() // 정렬
            result[i] = window[halfFilterSize] // 중앙값 선택
        }

        return result
    }

    /**
     * 이동 평균 필터 적용 - 고주파 노이즈 제거 효과
     */
    private fun applyMovingAverageFilter(audioData: ShortArray): ShortArray {
        val result = ShortArray(audioData.size)
        val filterSize = 3 // 필터 윈도우 크기
        val halfFilterSize = filterSize / 2

        // 경계 처리를 위한 패딩 적용
        val paddedData = ShortArray(audioData.size + filterSize - 1)
        // 왼쪽 패딩
        for (i in 0 until halfFilterSize) {
            paddedData[i] = audioData[0]
        }
        // 데이터 복사
        audioData.copyInto(paddedData, halfFilterSize)
        // 오른쪽 패딩
        for (i in 0 until halfFilterSize) {
            paddedData[paddedData.size - 1 - i] = audioData[audioData.size - 1]
        }

        // 필터 적용
        for (i in audioData.indices) {
            var sum = 0.0
            for (j in 0 until filterSize) {
                sum += paddedData[i + j]
            }
            result[i] = (sum / filterSize).toInt().toShort()
        }

        return result
    }

    /**
     * 처리된 PCM 오디오 데이터를 WAV 파일로 저장
     */
    private fun savePCMAudioToWav(audioData: ShortArray, outputFilePath: String, sampleRate: Int): Boolean {
        try {
            // WAV 파일 헤더 정보
            val numChannels = 1 // 모노
            val bitsPerSample = 16 // 16비트 PCM
            val byteRate = sampleRate * numChannels * bitsPerSample / 8
            val blockAlign = numChannels * bitsPerSample / 8
            val dataSize = audioData.size * 2 // 각 샘플은 2바이트
            val fileSize = 36 + dataSize

            val outputFile = FileOutputStream(outputFilePath)

            // WAV 헤더 작성
            // RIFF 청크
            outputFile.write("RIFF".toByteArray())
            writeInt(outputFile, fileSize)
            outputFile.write("WAVE".toByteArray())

            // fmt 청크
            outputFile.write("fmt ".toByteArray())
            writeInt(outputFile, 16) // fmt 청크 크기
            writeShort(outputFile, 1) // 오디오 포맷 (1 = PCM)
            writeShort(outputFile, numChannels)
            writeInt(outputFile, sampleRate)
            writeInt(outputFile, byteRate)
            writeShort(outputFile, blockAlign)
            writeShort(outputFile, bitsPerSample)

            // data 청크
            outputFile.write("data".toByteArray())
            writeInt(outputFile, dataSize)

            // 오디오 데이터 쓰기
            val buffer = ByteArray(2)
            for (sample in audioData) {
                buffer[0] = (sample.toInt() and 0xFF).toByte()
                buffer[1] = (sample.toInt() shr 8 and 0xFF).toByte()
                outputFile.write(buffer)
            }

            outputFile.close()
            Log.d(TAG, "Successfully saved processed audio to: $outputFilePath")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error saving processed audio: ${e.message}", e)
            return false
        }
    }

    // 헬퍼 함수 - 정수를 리틀 엔디안으로 쓰기
    private fun writeInt(output: FileOutputStream, value: Int) {
        output.write(value and 0xFF)
        output.write(value shr 8 and 0xFF)
        output.write(value shr 16 and 0xFF)
        output.write(value shr 24 and 0xFF)
    }

    // 헬퍼 함수 - 짧은 정수를 리틀 엔디안으로 쓰기
    private fun writeShort(output: FileOutputStream, value: Int) {
        output.write(value and 0xFF)
        output.write(value shr 8 and 0xFF)
    }
}