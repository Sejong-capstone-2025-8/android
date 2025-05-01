package com.toprunner.imagestory

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaCodec
import android.media.AudioTrack
import android.media.AudioFormat
import android.media.AudioManager
import android.util.Log
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt
import com.toprunner.imagestory.model.VoiceFeatures

class SimpleAudioAnalyzer(private val context: Context) {
    private val TAG = "SimpleAudioAnalyzer"

    fun analyzeAudio(audioFilePath: String): VoiceFeatures {
        Log.d(TAG, "Analyzing audio: $audioFilePath")

        try {
            val file = File(audioFilePath)
            if (!file.exists()) {
                Log.e(TAG, "Audio file does not exist: $audioFilePath")
                return VoiceFeatures(
                    averagePitch = 150.0,
                    pitchStdDev = 15.0,
                    mfccValues = listOf(DoubleArray(13) { 0.0 })
                )
            }

            // 오디오 데이터 추출
            val samples = extractPCMAudio(audioFilePath)
            if (samples.isEmpty()) {
                Log.e(TAG, "Failed to extract audio samples")
                return VoiceFeatures(
                    averagePitch = 150.0,
                    pitchStdDev = 15.0,
                    mfccValues = listOf(DoubleArray(13) { 0.0 })
                )
            }

            // 주파수 분석
            val pitchAnalysisResult = analyzeVoicePitch(samples, 44100)

            // 간단한 MFCC 계산 (13개 계수)
            val mfccResult = calculateSimpleMFCC(samples)

            return VoiceFeatures(
                averagePitch = pitchAnalysisResult.first,
                pitchStdDev = pitchAnalysisResult.second,
                mfccValues = mfccResult
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing audio: ${e.message}", e)
            return VoiceFeatures(
                averagePitch = 150.0,
                pitchStdDev = 15.0,
                mfccValues = listOf(DoubleArray(13) { 0.0 })
            )
        }
    }

    // PCM 오디오 데이터 추출
    private fun extractPCMAudio(filePath: String): ShortArray {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null

        try {
            extractor.setDataSource(filePath)

            // 오디오 트랙 찾기
            val trackCount = extractor.trackCount
            var audioTrackIndex = -1
            var format: MediaFormat? = null

            for (i in 0 until trackCount) {
                val trackFormat = extractor.getTrackFormat(i)
                val mime = trackFormat.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    format = trackFormat
                    break
                }
            }

            if (audioTrackIndex == -1 || format == null) {
                Log.e(TAG, "No audio track found")
                return ShortArray(0)
            }

            extractor.selectTrack(audioTrackIndex)

            // 디코더 설정
            val mime = format.getString(MediaFormat.KEY_MIME)
            codec = MediaCodec.createDecoderByType(mime!!)
            codec.configure(format, null, null, 0)
            codec.start()

            // 버퍼 준비
            val inputBuffers = codec.inputBuffers
            val outputBuffers = codec.outputBuffers
            val bufferInfo = MediaCodec.BufferInfo()

            // PCM 데이터 저장할 목록
            val pcmData = mutableListOf<Short>()
            var sawInputEOS = false
            var sawOutputEOS = false

            // 디코딩 루프
            while (!sawOutputEOS) {
                // 입력 처리
                if (!sawInputEOS) {
                    val inputBufferIndex = codec.dequeueInputBuffer(10000)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = inputBuffers[inputBufferIndex]
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)

                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            sawInputEOS = true
                        } else {
                            codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                // 출력 처리
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufferIndex >= 0) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        sawOutputEOS = true
                    }

                    if (bufferInfo.size > 0) {
                        val outputBuffer = outputBuffers[outputBufferIndex]
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)

                        // PCM 데이터를 ShortArray로 변환
                        val samples = ShortArray(bufferInfo.size / 2)
                        outputBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples)
                        pcmData.addAll(samples.toList())
                    }

                    codec.releaseOutputBuffer(outputBufferIndex, false)
                }
            }

            return pcmData.toShortArray()

        } catch (e: Exception) {
            Log.e(TAG, "Error extracting PCM audio: ${e.message}", e)
            return ShortArray(0)
        } finally {
            codec?.stop()
            codec?.release()
            extractor.release()
        }
    }

    // 음성 피치 분석 (자기상관 방법)
    private fun analyzeVoicePitch(samples: ShortArray, sampleRate: Int): Pair<Double, Double> {
        if (samples.isEmpty()) return Pair(150.0, 15.0)

        val bufferSize = 1024
        val pitchValues = mutableListOf<Double>()

        // 프레임 단위로 분석
        for (i in 0 until samples.size - bufferSize step bufferSize / 2) {
            val buffer = ShortArray(bufferSize)
            for (j in 0 until bufferSize) {
                if (i + j < samples.size) {
                    buffer[j] = samples[i + j]
                }
            }

            // 해당 프레임이 음성인지 확인 (에너지 기반)
            val energy = calculateEnergy(buffer)
            if (energy > 500) { // 임계값 (조절 필요)
                // 자기상관(Autocorrelation) 기반 피치 추정
                val pitch = estimatePitchByAutocorrelation(buffer, sampleRate)
                if (pitch in 50.0..400.0) { // 유효한 인간 음성 범위
                    pitchValues.add(pitch)
                }
            }
        }

        // 결과 계산
        if (pitchValues.isEmpty()) return Pair(150.0, 15.0)

        val averagePitch = pitchValues.average()

        // 표준 편차 계산
        val pitchVariance = pitchValues.map { (it - averagePitch).pow(2) }.average()
        val pitchStdDev = sqrt(pitchVariance)

        return Pair(averagePitch, pitchStdDev)
    }

    // 자기상관 함수로 피치 추정
    private fun estimatePitchByAutocorrelation(buffer: ShortArray, sampleRate: Int): Double {
        val minLag = (sampleRate / 400.0).toInt() // 400Hz (높은 피치의 한계)
        val maxLag = (sampleRate / 50.0).toInt()  // 50Hz (낮은 피치의 한계)

        var maxCorrelation = 0.0
        var bestLag = 0

        // 정규화된 자기상관
        for (lag in minLag..maxLag) {
            var correlation = 0.0
            var energy1 = 0.0
            var energy2 = 0.0

            for (i in 0 until buffer.size - lag) {
                val sample1 = buffer[i].toDouble()
                val sample2 = buffer[i + lag].toDouble()

                correlation += (sample1 * sample2)
                energy1 += (sample1 * sample1)
                energy2 += (sample2 * sample2)
            }

            // 정규화
            val normalizedCorrelation = correlation / sqrt(energy1 * energy2 + 1e-10)

            if (normalizedCorrelation > maxCorrelation) {
                maxCorrelation = normalizedCorrelation
                bestLag = lag
            }
        }

        // 피치 계산 (Hz)
        return if (bestLag > 0) sampleRate.toDouble() / bestLag else 0.0
    }

    // 프레임 에너지 계산
    private fun calculateEnergy(buffer: ShortArray): Double {
        var sum = 0.0
        for (sample in buffer) {
            sum += sample.toDouble().pow(2)
        }
        return sum / buffer.size
    }

    // 간단한 MFCC 계산
    private fun calculateSimpleMFCC(samples: ShortArray): List<DoubleArray> {
        val numFrames = 5 // 분석할 프레임 수
        val frameSize = 2048 // 프레임 크기
        val hopSize = samples.size / (numFrames * 2) // 프레임 이동 크기

        val mfccValues = mutableListOf<DoubleArray>()

        for (frameIndex in 0 until numFrames) {
            val startIndex = frameIndex * hopSize
            if (startIndex + frameSize > samples.size) break

            val frame = ShortArray(frameSize)
            for (i in 0 until frameSize) {
                if (startIndex + i < samples.size) {
                    frame[i] = samples[startIndex + i]
                }
            }

            // 해당 프레임의 MFCC 계산
            val mfcc = calculateMFCCForFrame(frame)
            mfccValues.add(mfcc)
        }

        // 분석 결과가 없으면 기본값 제공
        if (mfccValues.isEmpty()) {
            return List(5) { DoubleArray(13) { 0.0 } }
        }

        return mfccValues
    }

    // 프레임별 MFCC 계산 (간단화된 버전)
    private fun calculateMFCCForFrame(frame: ShortArray): DoubleArray {
        val numCoefficients = 13 // MFCC 계수 수
        val mfcc = DoubleArray(numCoefficients)

        // 1. 창 함수 적용 (Hamming)
        val windowed = DoubleArray(frame.size)
        for (i in frame.indices) {
            windowed[i] = frame[i] * (0.54 - 0.46 * Math.cos(2 * Math.PI * i / (frame.size - 1)))
        }

        // 2. FFT (간단한 스펙트럼 추정)
        val spectrum = estimateSpectrum(windowed)

        // 3. Mel 필터뱅크 적용 (13개 밴드)
        val melBands = applyMelFilterbank(spectrum)

        // 4. 로그 적용
        for (i in melBands.indices) {
            melBands[i] = Math.log10(melBands[i] + 1e-10)
        }

        // 5. DCT (이산 코사인 변환) 간소화
        for (i in 0 until numCoefficients) {
            var sum = 0.0
            for (j in melBands.indices) {
                sum += melBands[j] * Math.cos(Math.PI * i * (j + 0.5) / melBands.size)
            }
            mfcc[i] = sum
        }

        return mfcc
    }

    // 간단한 스펙트럼 추정 (FFT 대신 간소화된 스펙트럼 계산)
    private fun estimateSpectrum(samples: DoubleArray): DoubleArray {
        val numBands = 26 // 주파수 대역 수
        val spectrum = DoubleArray(numBands)

        // 주파수 대역별 에너지 계산
        for (band in 0 until numBands) {
            val minFreq = 20.0 * (band + 1)
            val maxFreq = 20.0 * (band + 2)

            // 특정 주파수 대역에 해당하는 에너지 추정
            spectrum[band] = estimateBandEnergy(samples, minFreq, maxFreq, 44100.0)
        }

        return spectrum
    }

    // 특정 주파수 대역의 에너지 추정
    private fun estimateBandEnergy(samples: DoubleArray, minFreq: Double, maxFreq: Double, sampleRate: Double): Double {
        val minPeriod = (sampleRate / maxFreq).toInt().coerceAtLeast(1)
        val maxPeriod = (sampleRate / minFreq).toInt().coerceAtMost(samples.size / 2)

        var maxEnergy = 0.0

        for (period in minPeriod..maxPeriod) {
            var energy = 0.0

            // 특정 주기에 대한 에너지 계산
            for (i in 0 until samples.size - period) {
                energy += samples[i] * samples[i + period]
            }

            // 정규화
            energy /= (samples.size - period)

            if (Math.abs(energy) > Math.abs(maxEnergy)) {
                maxEnergy = energy
            }
        }

        return Math.abs(maxEnergy)
    }

    // Mel 필터뱅크 적용 (간소화)
    private fun applyMelFilterbank(spectrum: DoubleArray): DoubleArray {
        val numMelBands = 13
        val melBands = DoubleArray(numMelBands)

        // 간단한 Mel 스케일 변환 및 필터링
        for (i in 0 until numMelBands) {
            val startBand = i * spectrum.size / (numMelBands + 1)
            val midBand = (i + 1) * spectrum.size / (numMelBands + 1)
            val endBand = (i + 2) * spectrum.size / (numMelBands + 1)

            var sum = 0.0
            var count = 0

            // 삼각형 필터 적용
            for (j in startBand until endBand) {
                if (j >= spectrum.size) break

                var weight = 0.0
                if (j < midBand) {
                    weight = (j - startBand).toDouble() / (midBand - startBand)
                } else {
                    weight = 1.0 - (j - midBand).toDouble() / (endBand - midBand)
                }

                sum += spectrum[j] * weight
                count++
            }

            melBands[i] = if (count > 0) sum / count else 0.0
        }

        return melBands
    }
}