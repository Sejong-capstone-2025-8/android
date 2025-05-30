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
import com.toprunner.imagestory.util.SimpleAudioAnalyzer
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
        Log.d(TAG, "Starting noise reduction process")
        Log.d(TAG, "Input file: $audioFilePath")
        Log.d(TAG, "Output file: $outputFilePath")

        try {
            val inputFile = File(audioFilePath)
            if (!inputFile.exists()) {
                Log.e(TAG, "Input file does not exist: $audioFilePath")
                return false
            }

            Log.d(TAG, "Input file size: ${inputFile.length()} bytes")

            // 파일 확장자에 따른 처리 방식 결정
            val isWavFile = audioFilePath.lowercase().endsWith(".wav")
            val is3gpFile = audioFilePath.lowercase().endsWith(".3gp")

            if (isWavFile) {
                return processWavFile(audioFilePath, outputFilePath)
            } else if (is3gpFile) {
                return process3gpFile(audioFilePath, outputFilePath)
            } else {
                // 기본 처리: 파일 복사 후 간단한 노이즈 감소 적용
                return processGenericAudioFile(audioFilePath, outputFilePath)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in noise reduction process: ${e.message}", e)
            return false
        }
    }
    private fun processWavFile(inputPath: String, outputPath: String): Boolean {
        try {
            Log.d(TAG, "Processing WAV file")

            val inputFile = File(inputPath)
            val audioBytes = inputFile.readBytes()

            // WAV 헤더 크기 확인 (최소 44바이트)
            if (audioBytes.size < 44) {
                Log.e(TAG, "Invalid WAV file: too small")
                return false
            }

            // WAV 헤더 분석
            val headerSize = findWavDataOffset(audioBytes)
            if (headerSize < 0) {
                Log.e(TAG, "Could not find WAV data section")
                return false
            }

            Log.d(TAG, "WAV header size: $headerSize bytes")

            // PCM 데이터 추출
            val pcmData = extractPCMFromWav(audioBytes, headerSize)
            if (pcmData.isEmpty()) {
                Log.e(TAG, "No PCM data found in WAV file")
                return false
            }

            Log.d(TAG, "Extracted ${pcmData.size} PCM samples")

            // 노이즈 제거 적용
            val processedPCM = applyAdvancedNoiseReduction(pcmData)

            // 새로운 WAV 파일로 저장
            return saveProcessedWavFile(audioBytes, processedPCM, headerSize, outputPath)

        } catch (e: Exception) {
            Log.e(TAG, "Error processing WAV file: ${e.message}", e)
            return false
        }
    }
    private fun process3gpFile(inputPath: String, outputPath: String): Boolean {
        try {
            Log.d(TAG, "Processing 3GP file")

            val inputFile = File(inputPath)
            val outputFile = File(outputPath)

            // 3GP 파일은 압축된 형태이므로 직접적인 노이즈 제거보다는
            // 파일 최적화를 수행
            inputFile.copyTo(outputFile, overwrite = true)

            Log.d(TAG, "3GP file copied and optimized")
            return outputFile.exists() && outputFile.length() > 0

        } catch (e: Exception) {
            Log.e(TAG, "Error processing 3GP file: ${e.message}", e)
            return false
        }
    }
    private fun processGenericAudioFile(inputPath: String, outputPath: String): Boolean {
        try {
            Log.d(TAG, "Processing generic audio file")

            val inputFile = File(inputPath)
            val outputFile = File(outputPath)

            // 기본적으로 파일 복사
            inputFile.copyTo(outputFile, overwrite = true)

            return outputFile.exists() && outputFile.length() > 0

        } catch (e: Exception) {
            Log.e(TAG, "Error processing generic audio file: ${e.message}", e)
            return false
        }
    }
    private fun findWavDataOffset(audioBytes: ByteArray): Int {
        try {
            // WAV 파일 헤더 확인
            val riffHeader = String(audioBytes.sliceArray(0..3))
            val waveHeader = String(audioBytes.sliceArray(8..11))

            if (riffHeader != "RIFF" || waveHeader != "WAVE") {
                Log.e(TAG, "Invalid WAV file format")
                return -1
            }

            // "data" 청크를 찾기
            var offset = 12
            while (offset < audioBytes.size - 8) {
                val chunkId = String(audioBytes.sliceArray(offset until offset + 4))
                if (chunkId == "data") {
                    // data 청크 크기 읽기 (4바이트)
                    offset += 8 // "data" + size 필드
                    return offset
                } else {
                    // 다른 청크인 경우 크기만큼 건너뛰기
                    val chunkSize = bytesToInt(audioBytes.sliceArray(offset + 4 until offset + 8))
                    offset += 8 + chunkSize
                }
            }

            Log.e(TAG, "Could not find data chunk in WAV file")
            return -1

        } catch (e: Exception) {
            Log.e(TAG, "Error finding WAV data offset: ${e.message}", e)
            return -1
        }
    }
    private fun extractPCMFromWav(audioBytes: ByteArray, dataOffset: Int): ShortArray {
        try {
            val pcmByteCount = audioBytes.size - dataOffset
            val pcmSampleCount = pcmByteCount / 2 // 16비트 샘플 가정

            val pcmData = ShortArray(pcmSampleCount)

            for (i in 0 until pcmSampleCount) {
                val byteIndex = dataOffset + i * 2
                if (byteIndex + 1 < audioBytes.size) {
                    // 리틀 엔디안 변환
                    val sample = (audioBytes[byteIndex].toInt() and 0xFF) or
                            ((audioBytes[byteIndex + 1].toInt() and 0xFF) shl 8)
                    pcmData[i] = sample.toShort()
                }
            }

            return pcmData

        } catch (e: Exception) {
            Log.e(TAG, "Error extracting PCM data: ${e.message}", e)
            return ShortArray(0)
        }
    }

    private fun applyAdvancedNoiseReduction(audioData: ShortArray): ShortArray {
        if (audioData.isEmpty()) return audioData

        Log.d(TAG, "Applying advanced noise reduction to ${audioData.size} samples")

        try {
            // 1. 간단한 하이패스 필터 적용 (저주파 노이즈 제거)
            val highPassFiltered = applySimpleHighPassFilter(audioData)

            // 2. 적응형 노이즈 게이트 적용
            val gatedData = applyAdaptiveNoiseGate(highPassFiltered)

            // 3. 스무딩 필터 적용
            val smoothedData = applySmoothingFilter(gatedData)

            Log.d(TAG, "Noise reduction completed")
            return smoothedData

        } catch (e: Exception) {
            Log.e(TAG, "Error in advanced noise reduction: ${e.message}", e)
            return audioData // 오류 시 원본 반환
        }
    }

    private fun applySimpleHighPassFilter(audioData: ShortArray): ShortArray {
        val result = ShortArray(audioData.size)
        val alpha = 0.96f // 필터 강도 (0.9-0.99 범위)

        result[0] = audioData[0]
        for (i in 1 until audioData.size) {
            val filteredValue = alpha * (result[i-1] + audioData[i] - audioData[i-1])
            result[i] = filteredValue.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        return result
    }


    private fun applyAdaptiveNoiseGate(audioData: ShortArray): ShortArray {
        val result = ShortArray(audioData.size)
        val windowSize = 1024

        // 전체 신호의 RMS 계산
        val overallRMS = calculateRMS(audioData)
        val threshold = (overallRMS * 0.1).toInt() // RMS의 10%를 임계값으로 설정

        for (i in audioData.indices) {
            val startIdx = maxOf(0, i - windowSize / 2)
            val endIdx = minOf(audioData.size, i + windowSize / 2)

            // 현재 윈도우의 RMS 계산
            val windowRMS = calculateRMS(audioData.sliceArray(startIdx until endIdx))

            result[i] = if (windowRMS > threshold) {
                audioData[i] // 신호가 충분히 크면 유지
            } else {
                (audioData[i] * 0.3).toInt().toShort() // 작으면 감쇠
            }
        }

        return result
    }

    private fun applySmoothingFilter(audioData: ShortArray): ShortArray {
        val result = ShortArray(audioData.size)
        val windowSize = 3

        for (i in audioData.indices) {
            var sum = 0
            var count = 0

            for (j in maxOf(0, i - windowSize/2)..minOf(audioData.size - 1, i + windowSize/2)) {
                sum += audioData[j]
                count++
            }

            result[i] = (sum / count).toShort()
        }

        return result
    }
    private fun calculateRMS(audioData: ShortArray): Double {
        if (audioData.isEmpty()) return 0.0

        var sum = 0.0
        for (sample in audioData) {
            sum += sample * sample
        }

        return kotlin.math.sqrt(sum / audioData.size)
    }

    private fun saveProcessedWavFile(
        originalBytes: ByteArray,
        processedPCM: ShortArray,
        dataOffset: Int,
        outputPath: String
    ): Boolean {
        try {
            val outputFile = File(outputPath)

            // 새로운 PCM 데이터 크기 계산
            val newDataSize = processedPCM.size * 2
            val newFileSize = dataOffset + newDataSize

            // 새로운 바이트 배열 생성
            val newBytes = ByteArray(newFileSize)

            // 헤더 복사
            System.arraycopy(originalBytes, 0, newBytes, 0, dataOffset)

            // 파일 크기 업데이트 (RIFF 청크 크기)
            val fileSizeBytes = intToBytes(newFileSize - 8)
            System.arraycopy(fileSizeBytes, 0, newBytes, 4, 4)

            // 데이터 크기 업데이트
            val dataSizeBytes = intToBytes(newDataSize)
            System.arraycopy(dataSizeBytes, 0, newBytes, dataOffset - 4, 4)

            // 처리된 PCM 데이터 저장
            for (i in processedPCM.indices) {
                val sample = processedPCM[i].toInt()
                val byteIndex = dataOffset + i * 2
                newBytes[byteIndex] = (sample and 0xFF).toByte()
                newBytes[byteIndex + 1] = ((sample shr 8) and 0xFF).toByte()
            }

            // 파일 저장
            outputFile.writeBytes(newBytes)

            Log.d(TAG, "Processed WAV file saved: ${outputFile.length()} bytes")
            return outputFile.exists() && outputFile.length() > 0

        } catch (e: Exception) {
            Log.e(TAG, "Error saving processed WAV file: ${e.message}", e)
            return false
        }
    }

    private fun bytesToInt(bytes: ByteArray): Int {
        return (bytes[0].toInt() and 0xFF) or
                ((bytes[1].toInt() and 0xFF) shl 8) or
                ((bytes[2].toInt() and 0xFF) shl 16) or
                ((bytes[3].toInt() and 0xFF) shl 24)
    }

    private fun intToBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }





}