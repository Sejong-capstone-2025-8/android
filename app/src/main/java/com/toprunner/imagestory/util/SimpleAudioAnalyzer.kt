package com.toprunner.imagestory.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import com.toprunner.imagestory.model.VoiceFeatures
import java.io.File
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class SimpleAudioAnalyzer(private val context: Context) {
    private val TAG = "SimpleAudioAnalyzer"
    private val SAMPLE_RATE = 44100 // 표준 샘플링 레이트 (Hz)

    // 고품질 분석 매개변수
    private val DEFAULT_FRAME_SIZES = intArrayOf(1024, 2048, 4096, 8192) // 다양한 프레임 크기 (더 정확한 분석용)
    private val MAX_SAMPLE_LENGTH = 3000000 // 최대 샘플 길이 (메모리 효율성)
    private val MIN_VOICE_FRAMES = 5 // 최소 유효 음성 프레임 수

    /**
     * 주어진 오디오 파일 경로로부터 음성을 분석하여 특성을 추출
     * @param audioFilePath 오디오 파일 경로
     * @return 음성 특성을 담은 VoiceFeatures 객체
     */
    fun analyzeAudio(audioFilePath: String): VoiceFeatures {
        Log.d(TAG, "오디오 분석 시작: $audioFilePath")

        try {
            val file = File(audioFilePath)
            if (!file.exists()) {
                Log.e(TAG, "오디오 파일이 존재하지 않음: $audioFilePath")
                return getDefaultVoiceFeatures()
            }

            // PCM 오디오 추출
            val samples = extractPCMAudio(audioFilePath)
            if (samples.isEmpty()) {
                Log.e(TAG, "오디오 샘플 추출 실패")
                return getDefaultVoiceFeatures()
            }

            // 다중 해상도 피치 분석 수행
            val pitchResults = mutableListOf<PitchAnalysisResult>()

            // 여러 프레임 크기를 사용하여 정확도 향상
            for (frameSize in DEFAULT_FRAME_SIZES) {
                val result = analyzeVoicePitch(samples, SAMPLE_RATE, frameSize)
                if (result.validFrames >= MIN_VOICE_FRAMES) {
                    pitchResults.add(result)
                }
            }

            val finalPitchResult = if (pitchResults.isNotEmpty()) {
                // 유효 프레임 기준 정렬 후 최적 결과 선택
                pitchResults.sortByDescending { it.validFrames }
                val bestResult = pitchResults.first()

                // 사람 목소리 범위로 제한 (75Hz-350Hz)
                PitchAnalysisResult(
                    averagePitch = bestResult.averagePitch.coerceIn(75.0, 350.0),
                    pitchStdDev = bestResult.pitchStdDev.coerceIn(5.0, 150.0),
                    validFrames = bestResult.validFrames
                )
            } else {
                // 유효한 분석이 없을 경우 기본값 사용
                PitchAnalysisResult(150.0, 15.0, 0)
            }

            // 개선된 신호 처리로 MFCC 계산
            val mfccResult = calculateImprovedMFCC(samples)

            Log.d(
                TAG,
                "분석 완료 - 피치: ${finalPitchResult.averagePitch}, 표준편차: ${finalPitchResult.pitchStdDev}"
            )

            return VoiceFeatures(
                averagePitch = finalPitchResult.averagePitch,
                pitchStdDev = finalPitchResult.pitchStdDev,
                mfccValues = mfccResult
            )

        } catch (e: Exception) {
            Log.e(TAG, "오디오 분석 오류: ${e.message}", e)
            return getDefaultVoiceFeatures()
        }
    }

    // 피치 분석 결과 데이터 클래스
    data class PitchAnalysisResult(
        val averagePitch: Double,  // 평균 피치 (Hz)
        val pitchStdDev: Double,   // 피치 표준편차 (Hz)
        val validFrames: Int       // 유효 프레임 수
    )

    /**
     * 효율성과 오류 처리가 개선된 PCM 오디오 추출
     * @param filePath 오디오 파일 경로
     * @return PCM 샘플 배열
     */
    private fun extractPCMAudio(filePath: String): ShortArray {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null

        try {
            extractor.setDataSource(filePath)

            // 오디오 트랙 찾기
            var audioTrackIndex = -1
            var format: MediaFormat? = null

            // 사용 가능한 트랙 중 오디오 트랙 검색
            for (i in 0 until extractor.trackCount) {
                val trackFormat = extractor.getTrackFormat(i)
                val mime = trackFormat.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    format = trackFormat
                    break
                }
            }

            if (audioTrackIndex == -1 || format == null) {
                Log.e(TAG, "오디오 트랙을 찾을 수 없음")
                return ShortArray(0)
            }

            extractor.selectTrack(audioTrackIndex)

            // 디코더 설정
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return ShortArray(0)
            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            // 버퍼 준비
            val bufferInfo = MediaCodec.BufferInfo()
            val pcmData = mutableListOf<Short>()

            var sawInputEOS = false
            var sawOutputEOS = false
            val TIMEOUT_US = 10000L // 타임아웃 (마이크로초)

            // 효율적인 처리를 위한 오디오 길이 추적
            var totalLength = 0
            var samplingInterval = 1

            // 디코딩 루프
            while (!sawOutputEOS) {
                // 입력 처리
                if (!sawInputEOS) {
                    val inputBufferIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputBufferIndex) ?: continue
                        inputBuffer.clear()

                        val sampleSize = extractor.readSampleData(inputBuffer, 0)

                        if (sampleSize < 0) {
                            // 더 이상 샘플이 없음 - 스트림 종료 표시
                            codec.queueInputBuffer(
                                inputBufferIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            sawInputEOS = true
                        } else {
                            // 샘플 데이터 큐에 추가
                            codec.queueInputBuffer(
                                inputBufferIndex, 0, sampleSize,
                                extractor.sampleTime, 0
                            )
                            extractor.advance()
                        }
                    }
                }

                // 출력 처리
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                if (outputBufferIndex >= 0) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        sawOutputEOS = true
                    }

                    if (bufferInfo.size > 0) {
                        val outputBuffer = codec.getOutputBuffer(outputBufferIndex) ?: continue
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)

                        // 총 오디오 길이 추정
                        totalLength += bufferInfo.size / 2

                        // 매우 긴 오디오에 대한 적응형 샘플링
                        if (totalLength > MAX_SAMPLE_LENGTH && samplingInterval == 1) {
                            samplingInterval = (totalLength / MAX_SAMPLE_LENGTH) + 1
                            Log.d(TAG, "오디오가 너무 길어 샘플링 간격 조정: $samplingInterval")
                        }

                        // 적응형 샘플링으로 샘플 추출
                        val samples = ShortArray(bufferInfo.size / 2)
                        outputBuffer.order(ByteOrder.LITTLE_ENDIAN)
                        outputBuffer.asShortBuffer().get(samples)

                        // 샘플링 간격 적용하여 효율성 향상
                        if (samplingInterval > 1) {
                            for (i in samples.indices step samplingInterval) {
                                pcmData.add(samples[i])
                            }
                        } else {
                            pcmData.addAll(samples.toList())
                        }
                    }

                    codec.releaseOutputBuffer(outputBufferIndex, false)
                }
            }

            // 분석에 적합한 대표 부분 선택
            return if (pcmData.size > MAX_SAMPLE_LENGTH) {
                // 중간 부분 선택 (일반적으로 가장 대표적)
                val startIdx = max(0, (pcmData.size - MAX_SAMPLE_LENGTH) / 2)
                pcmData.subList(startIdx, min(startIdx + MAX_SAMPLE_LENGTH, pcmData.size))
                    .toShortArray()
            } else {
                pcmData.toShortArray()
            }

        } catch (e: Exception) {
            Log.e(TAG, "PCM 오디오 추출 오류: ${e.message}", e)
            return ShortArray(0)
        } finally {
            try {
                codec?.stop()
                codec?.release()
                extractor.release()
            } catch (e: Exception) {
                Log.e(TAG, "리소스 해제 오류: ${e.message}")
            }
        }
    }

    /**
     * 강력한 잡음 처리 기능을 갖춘 음성 피치 분석 개선 버전
     * @param samples PCM 샘플 배열
     * @param sampleRate 샘플링 레이트 (Hz)
     * @param frameSize 분석 프레임 크기
     * @return 피치 분석 결과
     */
    private fun analyzeVoicePitch(
        samples: ShortArray,
        sampleRate: Int,
        frameSize: Int = 2048
    ): PitchAnalysisResult {
        if (samples.isEmpty()) return PitchAnalysisResult(150.0, 15.0, 0)

        val hopSize = frameSize / 2 // 50% 오버랩
        val pitchValues = mutableListOf<Double>()
        var validFrames = 0

        // 고주파 향상을 위한 pre-emphasis 필터 적용
        val preEmphasisSamples = ShortArray(samples.size)
        val preEmphasisFactor = 0.97f
        preEmphasisSamples[0] = samples[0]
        for (i in 1 until samples.size) {
            val value = samples[i].toDouble() - (preEmphasisFactor * samples[i - 1].toDouble())
            preEmphasisSamples[i] =
                value.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        // 더 나은 VAD 임계값 지정을 위한 전역 에너지 계산
        val globalEnergy = calculateGlobalEnergy(preEmphasisSamples)
        val energyThreshold = globalEnergy * 0.1 // 적응형 임계값

        // ZCR 임계값에 대한 이동 평균 스무딩 적용
        val zcrValues = calculateFrameZCRs(preEmphasisSamples, frameSize, hopSize)
        val zcrMedian = if (zcrValues.isNotEmpty()) findMedian(zcrValues) else 0.2
        val zcrThreshold = zcrMedian * 1.5 // 적응형 임계값

        // 프레임별 분석
        for (i in 0 until samples.size - frameSize step hopSize) {
            // 프레임 추출
            val frame = ShortArray(frameSize)
            for (j in 0 until frameSize) {
                if (i + j < samples.size) {
                    frame[j] = preEmphasisSamples[i + j]
                }
            }

            // 해밍 윈도우 적용
            applyHammingWindow(frame)

            // 개선된 음성 활동 감지
            val energy = calculateEnergy(frame)
            val zcr = calculateZeroCrossingRate(frame)

            if (energy > energyThreshold && zcr < zcrThreshold) {
                // 음성이 포함되어 있을 가능성이 높음 - 피치 추정
                val pitch = estimatePitchYIN(frame, sampleRate)

                // 사람 목소리 범위 필터링 (75-400 Hz)
                if (pitch in 75.0..400.0) {
                    pitchValues.add(pitch)
                    validFrames++
                }
            }
        }

        // 결과 계산
        if (pitchValues.isEmpty()) {
            return PitchAnalysisResult(150.0, 15.0, 0)
        }

        // 수정된 절사 평균을 사용한 강력한 통계
        val sortedPitches = pitchValues.sorted()
        val trimAmount = (pitchValues.size * 0.15).toInt() // 강건성을 위한 15% 절사
        val trimmedPitches = if (pitchValues.size > trimAmount * 2) {
            sortedPitches.subList(trimAmount, sortedPitches.size - trimAmount)
        } else {
            sortedPitches
        }

        // 음성 특성에 따른 동적 가중치 적용
        val energy = calculateGlobalEnergy(preEmphasisSamples)
        // 낮은 에너지의 소리(남성 목소리 경향)에 대한 보정
        val energyWeight = if (energy < 5000) 0.8 else 1.0

        val averagePitch = trimmedPitches.average()
        val adjustedAveragePitch = averagePitch * energyWeight // 기초적인 보정으로 나중에 실험을 통해서 개선가능

        // 강건한 표준편차 계산
        val variance = trimmedPitches.map { (it - averagePitch).pow(2) }.average()
        val pitchStdDev = sqrt(variance).coerceIn(0.0, 150.0)

        return PitchAnalysisResult(adjustedAveragePitch, pitchStdDev, validFrames)
    }

    /**
     * 적응형 임계값 지정을 위한 전역 에너지 계산
     * @param samples PCM 샘플 배열
     * @return 평균 에너지
     */
    private fun calculateGlobalEnergy(samples: ShortArray): Double {
        var sum = 0.0
        val samplesCount = min(samples.size, 30000) // 효율성을 위한 제한
        val samplingInterval = max(1, samples.size / samplesCount)

        for (i in 0 until samples.size step samplingInterval) {
            sum += samples[i].toDouble().pow(2)
        }

        return sum / (samples.size / samplingInterval)
    }

    /**
     * 적응형 임계값 지정을 위한 모든 프레임의 ZCR 계산
     * @param samples PCM 샘플 배열
     * @param frameSize 프레임 크기
     * @param hopSize 프레임 간 이동 크기
     * @return 모든 프레임의 ZCR 값 리스트
     */
    private fun calculateFrameZCRs(
        samples: ShortArray,
        frameSize: Int,
        hopSize: Int
    ): List<Double> {
        val zcrValues = mutableListOf<Double>()

        for (i in 0 until samples.size - frameSize step hopSize) {
            val frame = ShortArray(frameSize)
            for (j in 0 until frameSize) {
                if (i + j < samples.size) {
                    frame[j] = samples[i + j]
                }
            }

            zcrValues.add(calculateZeroCrossingRate(frame))
        }

        return zcrValues
    }

    /**
     * 중앙값 찾기
     * @param values 값 리스트
     * @return 중앙값
     */
    private fun findMedian(values: List<Double>): Double {
        val sorted = values.sorted()
        return if (sorted.size % 2 == 0) {
            (sorted[sorted.size / 2] + sorted[sorted.size / 2 - 1]) / 2
        } else {
            sorted[sorted.size / 2]
        }
    }

    /**
     * 피치 감지를 위한 향상된 YIN 알고리즘
     * @param frame 오디오 프레임
     * @param sampleRate 샘플링 레이트 (Hz)
     * @return 피치 주파수 (Hz)
     */
    private fun estimatePitchYIN(frame: ShortArray, sampleRate: Int): Double {
        val minFreq = 75.0  // 최소 감지 가능 주파수 (Hz)
        val maxFreq = 400.0 // 최대 감지 가능 주파수 (Hz)

        val minPeriod = (sampleRate / maxFreq).toInt()
        val maxPeriod = (sampleRate / minFreq).toInt().coerceAtMost(frame.size / 2)

        val frameSize = frame.size
        val yinBuffer = DoubleArray(frameSize / 2)

        // 단계 1: 정규화를 통한 차이 함수
        for (tau in 0 until yinBuffer.size) {
            yinBuffer[tau] = 0.0
            for (j in 0 until frameSize - tau) {
                val delta = frame[j].toDouble() - frame[j + tau].toDouble()
                yinBuffer[tau] += delta * delta
            }
        }

        // 단계 2: 개선된 알고리즘을 통한 누적 정규화
        var sum = 0.0
        yinBuffer[0] = 1.0
        for (tau in 1 until yinBuffer.size) {
            sum += yinBuffer[tau]
            if (sum != 0.0) {
                yinBuffer[tau] *= tau / sum
            }
        }

        // 단계 3: 최소값 감지를 위한 적응형 임계값
        var bestTau = -1
        var bestVal = Double.MAX_VALUE
        val threshold = 0.1 // YIN 임계값 매개변수

        // 임계값 이하의 첫 번째 지점 찾기
        for (t in minPeriod until maxPeriod) {
            if (yinBuffer[t] < threshold) {
                // 지역 최소값을 찾기 위한 새로운 변수 사용
                var localMinIdx = t
                while (localMinIdx + 1 < maxPeriod && yinBuffer[localMinIdx + 1] < yinBuffer[localMinIdx]) {
                    localMinIdx++
                }
                bestTau = localMinIdx
                break
            }
        }

        // 임계값 이하 지점을 찾지 못한 경우, 최소값 사용
        if (bestTau == -1) {
            for (tau in minPeriod until maxPeriod) {
                if (yinBuffer[tau] < bestVal) {
                    bestVal = yinBuffer[tau]
                    bestTau = tau
                }
            }
        }

        if (bestTau == -1) {
            return 0.0 // 피치를 찾지 못함
        }

        // 포물선 보간법으로 정밀한 tau 값 계산
        val preciseTau = parabolicInterpolation(yinBuffer, bestTau)
        return sampleRate / preciseTau
    }

    /**
     * 포물선 보간법
     * @param array 배열
     * @param peakIndex 피크 인덱스
     * @return 보간된 값
     */
    private fun parabolicInterpolation(array: DoubleArray, peakIndex: Int): Double {
        val x0 = (peakIndex - 1).coerceAtLeast(0)
        val x2 = (peakIndex + 1).coerceAtMost(array.size - 1)

        val s0 = array[x0]
        val s1 = array[peakIndex]
        val s2 = array[x2]

        if (s0 == s2 || 2 * s1 - s0 - s2 == 0.0) {
            return peakIndex.toDouble()
        }

        val adjustment = 0.5 * (s0 - s2) / (s0 - 2 * s1 + s2)

        return peakIndex + adjustment.coerceIn(-1.0, 1.0)
    }

    /**
     * 해밍 윈도우 함수 적용
     * @param buffer 신호 버퍼
     */
    private fun applyHammingWindow(buffer: ShortArray) {
        for (i in buffer.indices) {
            val multiplier = 0.54 - 0.46 * cos(2.0 * PI * i / (buffer.size - 1))
            buffer[i] = (buffer[i] * multiplier).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    /**
     * 제로 크로싱 레이트 계산
     * @param buffer 신호 버퍼
     * @return 제로 크로싱 레이트
     */
    private fun calculateZeroCrossingRate(buffer: ShortArray): Double {
        var crossings = 0
        for (i in 1 until buffer.size) {
            if ((buffer[i] > 0 && buffer[i - 1] <= 0) ||
                (buffer[i] <= 0 && buffer[i - 1] > 0)
            ) {
                crossings++
            }
        }
        return crossings.toDouble() / (buffer.size - 1)
    }

    /**
     * 신호 에너지 계산
     * @param buffer 신호 버퍼
     * @return 평균 에너지
     */
    private fun calculateEnergy(buffer: ShortArray): Double {
        var sum = 0.0
        for (sample in buffer) {
            sum += sample.toDouble().pow(2)
        }

        return sum / buffer.size
    }

    /**
     * 개선된 MFCC 계산 (MFCC: Mel-Frequency Cepstral Coefficients)
     * @param samples PCM 샘플 배열
     * @return MFCC 값 배열 리스트
     */
    private fun calculateImprovedMFCC(samples: ShortArray): List<DoubleArray> {
        val numFrames = 13 // 프레임 수
        val frameSize = 2048 // 프레임 크기
        val numMfccCoefficients = 13 // 표준 MFCC 계수 개수
        val numMelFilters = 40 // Mel 필터 개수

        val mfccValues = mutableListOf<DoubleArray>()

        val totalSamples = samples.size
        val hopSize = frameSize / 2 // 50% 오버랩
        val totalFrames = (totalSamples - frameSize) / hopSize + 1
        val frameStep = max(1, totalFrames / numFrames)

        // Mel 필터뱅크 생성
        val melFilters = createMelFilterbank(frameSize / 2 + 1, numMelFilters, SAMPLE_RATE)

        // DCT 행렬 생성 (이산 코사인 변환)
        val dctMatrix = createDCTMatrix(numMelFilters, numMfccCoefficients)

        // 모든 프레임에 대해 MFCC 계산
        for (frameIndex in 0 until min(numFrames * frameStep, totalFrames) step frameStep) {
            val startIndex = frameIndex * hopSize

            // 프레임 추출
            val frame = DoubleArray(frameSize)
            for (i in 0 until frameSize) {
                if (startIndex + i < samples.size) {
                    frame[i] = samples[startIndex + i].toDouble()
                }
            }

            // 전처리: pre-emphasis 적용
            applyPreEmphasis(frame, 0.97)

            // 해밍 윈도우 적용
            applyHammingWindowDouble(frame)

            // 파워 스펙트럼 계산 (FFT 적용 후)
            val powerSpectrum = calculatePowerSpectrum(frame)

            // Mel 필터뱅크 적용
            val melFilterbankEnergies = applyMelFilterbank(powerSpectrum, melFilters)

            // 로그 Mel 에너지 계산
            val logMelEnergies = DoubleArray(melFilterbankEnergies.size) {
                ln(max(melFilterbankEnergies[it], 1e-10))
            }

            // DCT 적용 (이산 코사인 변환)
            val mfcc = applyDCT(logMelEnergies, dctMatrix)

            // 첫 번째 계수는 로그 에너지로 대체
            mfcc[0] = ln(max(1e-10, frame.map { it * it }.sum() / frame.size))

            // MFCC에 liftering 적용
            lifterMFCC(mfcc, 22)

            // 결과 리스트에 추가
            mfccValues.add(mfcc)
        }

        // 충분한 프레임이 없는 경우 채우기
        while (mfccValues.size < numFrames) {
            if (mfccValues.isEmpty()) {
                mfccValues.add(DoubleArray(numMfccCoefficients) { 0.0 })
            } else {
                mfccValues.add(mfccValues.last().clone())
            }
        }

        return mfccValues
    }

    /**
     * pre-emphasis 필터 적용
     * @param samples 샘플 배열
     * @param factor pre-emphasis 계수
     */
    private fun applyPreEmphasis(samples: DoubleArray, factor: Double) {
        for (i in samples.size - 1 downTo 1) {
            samples[i] = samples[i] - factor * samples[i - 1]
        }
    }

    /**
     * 해밍 윈도우 함수 적용 (Double 버전)
     * @param buffer 신호 버퍼
     */
    private fun applyHammingWindowDouble(buffer: DoubleArray) {
        for (i in buffer.indices) {
            val multiplier = 0.54 - 0.46 * cos(2.0 * PI * i / (buffer.size - 1))
            buffer[i] *= multiplier
        }
    }

    /**
     * MFCC에 Liftering 적용 (주파수 응답 평활화)
     * @param mfcc MFCC 계수 배열
     * @param lifterParam liftering 파라미터
     */
    private fun lifterMFCC(mfcc: DoubleArray, lifterParam: Int) {
        for (i in mfcc.indices) {
            mfcc[i] *= 1 + (lifterParam / 2) * sin(PI * i / lifterParam)
        }
    }

    /**
     * Mel 필터뱅크 생성
     * @param fftSize FFT 크기
     * @param numFilters 필터 개수
     * @param sampleRate 샘플링 레이트
     * @return Mel 필터뱅크 배열
     */
    private fun createMelFilterbank(
        fftSize: Int,
        numFilters: Int,
        sampleRate: Int
    ): Array<DoubleArray> {
        fun hzToMel(hz: Double): Double = 2595.0 * log10(1.0 + hz / 700.0) // Hz를 Mel 스케일로 변환
        fun melToHz(mel: Double): Double = 700.0 * (10.0.pow(mel / 2595.0) - 1.0) // Mel을 Hz로 변환

        val filters = Array(numFilters) { DoubleArray(fftSize) }

        // Mel 스케일 범위 설정
        val lowerFreqMel = hzToMel(0.0)
        val upperFreqMel = hzToMel(sampleRate / 2.0)

        // Mel 포인트 균등 분포
        val melPoints = DoubleArray(numFilters + 2)
        for (i in melPoints.indices) {
            melPoints[i] = lowerFreqMel + (upperFreqMel - lowerFreqMel) * i / (numFilters + 1)
        }

        // Mel을 주파수 빈으로 변환
        val binIndices = melPoints.map { melToHz(it) * 2 * fftSize / sampleRate }.toDoubleArray()

        // 삼각형 필터 생성
        for (m in 0 until numFilters) {
            val leftBin = binIndices[m].toInt()
            val centerBin = binIndices[m + 1].toInt()
            val rightBin = binIndices[m + 2].toInt()

            // 상승 부분
            for (k in leftBin until centerBin) {
                if (k < fftSize) {
                    filters[m][k] = (k - leftBin).toDouble() / (centerBin - leftBin)
                }
            }

            // 하강 부분
            for (k in centerBin until rightBin) {
                if (k < fftSize) {
                    filters[m][k] = (rightBin - k).toDouble() / (rightBin - centerBin)
                }
            }
        }

        return filters
    }

    /**
     * DCT(이산 코사인 변환) 행렬 생성
     * @param numFilters 필터 개수
     * @param numCoefficients 계수 개수
     * @return DCT 행렬
     */
    private fun createDCTMatrix(numFilters: Int, numCoefficients: Int): Array<DoubleArray> {
        val dctMatrix = Array(numCoefficients) { DoubleArray(numFilters) }

        // DCT 계수 계산
        for (i in 0 until numCoefficients) {
            for (j in 0 until numFilters) {
                dctMatrix[i][j] = cos(PI * i * (j + 0.5) / numFilters)
            }
        }

        // 정규화 (표준 DCT-II)
        val normFactor = sqrt(2.0 / numFilters)
        for (i in 1 until numCoefficients) {
            for (j in 0 until numFilters) {
                dctMatrix[i][j] *= normFactor
            }
        }

        // 첫 번째 행 정규화
        val firstRowNorm = sqrt(1.0 / numFilters)
        for (j in 0 until numFilters) {
            dctMatrix[0][j] *= firstRowNorm
        }

        return dctMatrix
    }

    /**
     * 파워 스펙트럼 계산
     * @param frame 신호 프레임
     * @return 파워 스펙트럼
     */
    private fun calculatePowerSpectrum(frame: DoubleArray): DoubleArray {
        val n = frame.size
        val fftReal = frame.copyOf()
        val fftImag = DoubleArray(n)

        // FFT 계산
        fastFourierTransform(fftReal, fftImag)

        // 파워 스펙트럼 계산 (시간 정규화 포함)
        val powerSpectrum = DoubleArray(n / 2 + 1)
        for (k in powerSpectrum.indices) {
            powerSpectrum[k] = (fftReal[k] * fftReal[k] + fftImag[k] * fftImag[k]) / n
        }

        return powerSpectrum
    }

    /**
     * 고속 푸리에 변환 (FFT) 구현
     * @param real 실수부 배열
     * @param imag 허수부 배열
     */
    private fun fastFourierTransform(real: DoubleArray, imag: DoubleArray) {
        val n = real.size

        // 크기가 2의 거듭제곱인지 확인
        if (n and (n - 1) != 0) {
            throw IllegalArgumentException("FFT 크기는 2의 거듭제곱이어야 합니다")
        }

        // 비트 반전 순서로 데이터 재배열
        var j = 0
        for (i in 0 until n - 1) {
            if (i < j) {
                // 실수부 교환
                val tempReal = real[i]
                real[i] = real[j]
                real[j] = tempReal

                // 허수부 교환
                val tempImag = imag[i]
                imag[i] = imag[j]
                imag[j] = tempImag
            }

            var m = n / 2
            while (m > 0 && j >= m) {
                j -= m
                m /= 2
            }
            j += m
        }

        // Cooley-Tukey FFT 알고리즘
        var mmax = 1
        while (mmax < n) {
            val istep = mmax * 2
            val theta = -2.0 * PI / istep

            // 효율적인 sin/cos 계산
            val wpr = -2.0 * sin(0.5 * theta) * sin(0.5 * theta)
            val wpi = sin(theta)
            var wr = 1.0
            var wi = 0.0

            for (m in 0 until mmax) {
                for (i in m until n step istep) {
                    j = i + mmax

                    // 버터플라이 연산
                    val tr = wr * real[j] - wi * imag[j]
                    val ti = wr * imag[j] + wi * real[j]

                    real[j] = real[i] - tr
                    imag[j] = imag[i] - ti
                    real[i] += tr
                    imag[i] += ti
                }

                // 다음 단계 계산을 위한 삼각함수 값 업데이트
                val wtemp = wr
                wr += wr * wpr - wi * wpi
                wi += wi * wpr + wtemp * wpi
            }

            mmax = istep
        }
    }

    /**
     * Mel 필터뱅크 적용
     * @param powerSpectrum 파워 스펙트럼
     * @param melFilters Mel 필터뱅크
     * @return Mel 필터뱅크 에너지
     */
    private fun applyMelFilterbank(
        powerSpectrum: DoubleArray,
        melFilters: Array<DoubleArray>
    ): DoubleArray {
        val numFilters = melFilters.size
        val result = DoubleArray(numFilters)

        // 각 필터에 대해 에너지 계산
        for (m in 0 until numFilters) {
            for (k in powerSpectrum.indices) {
                result[m] += powerSpectrum[k] * melFilters[m][k]
            }
        }

        return result
    }

    /**
     * DCT(이산 코사인 변환) 적용
     * @param logMelEnergies 로그 Mel 에너지
     * @param dctMatrix DCT 행렬
     * @return MFCC 계수
     */
    private fun applyDCT(logMelEnergies: DoubleArray, dctMatrix: Array<DoubleArray>): DoubleArray {
        val numCoefficients = dctMatrix.size
        val result = DoubleArray(numCoefficients)

        // DCT 행렬 곱셈
        for (i in 0 until numCoefficients) {
            for (j in logMelEnergies.indices) {
                result[i] += logMelEnergies[j] * dctMatrix[i][j]
            }
        }

        return result
    }


     // 오류 발생 시 사용할 기본 음성 특성 반환
     // @return 기본 VoiceFeatures 객체

    private fun getDefaultVoiceFeatures(): VoiceFeatures {
        return VoiceFeatures(
            averagePitch = 150.0,  // 평균적인 피치 (Hz)
            pitchStdDev = 15.0,    // 평균적인 피치 변동성
            mfccValues = List(5) { DoubleArray(13) { 0.0 } }  // 5개 프레임, 각 13개 0으로 초기화된 계수
        )
    }
}