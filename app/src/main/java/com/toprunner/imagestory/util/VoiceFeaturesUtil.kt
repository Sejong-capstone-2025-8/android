package com.toprunner.imagestory.util

import android.util.Log
import com.toprunner.imagestory.model.VoiceFeatures
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.pow
import kotlin.math.sqrt

class VoiceFeaturesUtil {
    private val TAG = "VoiceFeaturesUtil"

    fun parseVoiceFeatures(json: String): VoiceFeatures {
        try {
            val jsonObj = JSONObject(json)
            val averagePitch = jsonObj.optDouble("averagePitch", 120.0)
            val pitchStdDev = jsonObj.optDouble("pitchStdDev", 15.0)
            val mfccValues = mutableListOf<DoubleArray>()

            if (jsonObj.has("mfccValues")) {
                val mfccArray = jsonObj.getJSONArray("mfccValues")
                for (i in 0 until mfccArray.length()) {
                    val coeffArray = mfccArray.getJSONArray(i)
                    val coeffs = DoubleArray(coeffArray.length())
                    for (j in 0 until coeffArray.length()) {
                        coeffs[j] = coeffArray.getDouble(j)
                    }
                    mfccValues.add(coeffs)
                }
            } else {
                // 기본값 추가
                mfccValues.add(DoubleArray(13) { 0.0 })
            }

            return VoiceFeatures(averagePitch, pitchStdDev, mfccValues)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing voice features: ${e.message}", e)
            // 파싱 실패 시 기본값 반환
            return VoiceFeatures(
                averagePitch = 120.0,
                pitchStdDev = 15.0,
                mfccValues = listOf(DoubleArray(13) { 0.0 })
            )
        }
    }

    fun voiceFeaturesToJson(voiceFeatures: VoiceFeatures): String {
        try {
            val jsonObj = JSONObject()
            jsonObj.put("averagePitch", voiceFeatures.averagePitch)
            jsonObj.put("pitchStdDev", voiceFeatures.pitchStdDev)

            val mfccArray = JSONArray()
            for (coeffs in voiceFeatures.mfccValues) {
                val coeffArray = JSONArray()
                for (coeff in coeffs) {
                    coeffArray.put(coeff)
                }
                mfccArray.put(coeffArray)
            }
            jsonObj.put("mfccValues", mfccArray)

            return jsonObj.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error converting voice features to JSON: ${e.message}", e)
            // 변환 실패 시 기본 JSON 반환
            return """{"averagePitch":120.0,"pitchStdDev":15.0,"mfccValues":[[0,0,0,0,0,0,0,0,0,0,0,0,0]]}"""
        }
    }

    fun calculateSimilarity(features1: VoiceFeatures, features2: VoiceFeatures): Double {
        try {
            // 피치 유사도 계산 (표준화된 유클리드 거리)
            val pitchDiff = (features1.averagePitch - features2.averagePitch).pow(2) / 100.0 +
                    (features1.pitchStdDev - features2.pitchStdDev).pow(2) / 25.0
            val pitchDistance = sqrt(pitchDiff)

            // MFCC 유사도 계산 (코사인 유사도로 확장 가능)
            // 간단하게 첫 번째 MFCC 벡터만 사용
            var mfccDistance = 0.0
            if (features1.mfccValues.isNotEmpty() && features2.mfccValues.isNotEmpty()) {
                val mfcc1 = features1.mfccValues[0]
                val mfcc2 = features2.mfccValues[0]

                // 벡터 길이가 다를 경우 처리
                val minLength = minOf(mfcc1.size, mfcc2.size)
                var sumSquaredDiff = 0.0
                for (i in 0 until minLength) {
                    sumSquaredDiff += (mfcc1[i] - mfcc2[i]).pow(2)
                }
                mfccDistance = sqrt(sumSquaredDiff) / minLength
            }

            // 피치와 MFCC 유사도 조합 (가중치 조정 가능)
            val combinedDistance = pitchDistance * 0.7 + mfccDistance * 0.3

            // 거리를 유사도로 변환 (0.0~1.0, 1.0이 가장 유사)
            return 1.0 / (1.0 + combinedDistance)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating similarity: ${e.message}", e)
            return 0.5 // 기본 유사도
        }
    }

    fun findBestMatchingVoice(
        targetFeatures: VoiceFeatures,
        voicesList: List<Pair<Long, VoiceFeatures>>
    ): Long {
        if (voicesList.isEmpty()) {
            return 0L
        }

        var bestMatch = voicesList.first().first
        var highestSimilarity = 0.0

        for ((voiceId, features) in voicesList) {
            val similarity = calculateSimilarity(targetFeatures, features)
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity
                bestMatch = voiceId
            }
        }

        return bestMatch
    }
}