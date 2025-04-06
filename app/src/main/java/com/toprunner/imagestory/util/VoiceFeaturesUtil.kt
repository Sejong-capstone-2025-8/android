package com.toprunner.imagestory.util

import com.toprunner.imagestory.model.VoiceFeatures
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class VoiceFeaturesUtil {

    fun parseVoiceFeatures(json: String): VoiceFeatures {
        val gson = com.google.gson.Gson()
        return gson.fromJson(json, VoiceFeatures::class.java)
    }

    fun voiceFeaturesToJson(voiceFeatures: VoiceFeatures): String {
        val gson = com.google.gson.Gson()
        return gson.toJson(voiceFeatures)
    }

    /**
     * 두 음성 특성 간의 유사도를 계산하는 함수
     * 반환값이 작을수록 더 유사함
     */
    fun calculateSimilarity(features1: VoiceFeatures, features2: VoiceFeatures): Double {
        var similarity = 0.0

        // 피치 차이 계산 (가중치: 0.3)
        val pitchDiff = abs(features1.averagePitch - features2.averagePitch)
        similarity += 0.3 * (pitchDiff / 100.0) // 피치는 일반적으로 수백 Hz 단위

        // 피치 표준편차 차이 계산 (가중치: 0.2)
        val stdDevDiff = abs(features1.pitchStdDev - features2.pitchStdDev)
        similarity += 0.2 * (stdDevDiff / 50.0) // 표준편차는 일반적으로 수십 단위

        // MFCC 벡터 유사도 계산 (가중치: 0.5)
        val mfccSimilarity = calculateMfccSimilarity(features1.mfccValues, features2.mfccValues)
        similarity += 0.5 * mfccSimilarity

        return similarity
    }

    /**
     * MFCC 벡터 간의 유사도를 계산하는 함수 (유클리드 거리 사용)
     */
    private fun calculateMfccSimilarity(mfcc1: List<DoubleArray>, mfcc2: List<DoubleArray>): Double {
        // 두 MFCC 벡터의 길이가 다를 경우 짧은 쪽에 맞춤
        val minSize = minOf(mfcc1.size, mfcc2.size)

        var totalDistance = 0.0

        for (i in 0 until minSize) {
            val frame1 = mfcc1[i]
            val frame2 = mfcc2[i]

            // 각 프레임의 유클리드 거리 계산
            val frameDistance = calculateEuclideanDistance(frame1, frame2)
            totalDistance += frameDistance
        }

        // 정규화된 평균 거리 반환
        return totalDistance / minSize
    }

    private fun calculateEuclideanDistance(array1: DoubleArray, array2: DoubleArray): Double {
        val minSize = minOf(array1.size, array2.size)
        var sumSquaredDiff = 0.0

        for (i in 0 until minSize) {
            val diff = array1[i] - array2[i]
            sumSquaredDiff += diff.pow(2)
        }

        return sqrt(sumSquaredDiff)
    }

    /**
     * 주어진 음성 특성에 가장 유사한 음성 ID를 찾는 함수
     */
    fun findBestMatchingVoice(targetFeatures: VoiceFeatures, voicesList: List<Pair<Long, VoiceFeatures>>): Long {
        if (voicesList.isEmpty()) {
            return 0L // 기본값
        }

        var bestVoiceId = voicesList[0].first
        var bestSimilarity = Double.MAX_VALUE

        for ((voiceId, features) in voicesList) {
            val similarity = calculateSimilarity(targetFeatures, features)
            if (similarity < bestSimilarity) {
                bestSimilarity = similarity
                bestVoiceId = voiceId
            }
        }

        return bestVoiceId
    }
}