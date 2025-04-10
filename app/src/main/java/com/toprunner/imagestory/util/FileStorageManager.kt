package com.toprunner.imagestory.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.toprunner.imagestory.model.VoiceFeatures
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class FileStorageManager {
    private val TAG = "FileStorageManager"

    companion object {
        private const val STORY_CONTENT_DIR = "story_contents"
        private const val STORY_IMAGE_DIR = "story_images"
        private const val AUDIO_DIR = "audio_files"
        private const val VOICE_SAMPLE_DIR = "voice_samples"
        private const val MFCC_DATA_DIR = "mfcc_data"
        private const val MUSIC_DIR = "music_files"
    }

    fun saveTextFile(context: Context, content: String, fileName: String? = null): String {
        val actualFileName = fileName ?: generateUniqueFileName("story_content", "txt")
        val contentDir = File(context.filesDir, STORY_CONTENT_DIR).apply {
            if (!exists()) mkdirs()
        }
        val file = File(contentDir, actualFileName)

        try {
            file.writeText(content)
            Log.d(TAG, "Text file saved successfully at: ${file.absolutePath}")
            return file.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Error saving text file: ${e.message}", e)
            throw e
        }
    }

    fun saveAudioFile(context: Context, audioData: ByteArray, fileName: String? = null): String {
        val actualFileName = fileName ?: generateUniqueFileName("audio", "wav")
        val audioDir = File(context.filesDir, AUDIO_DIR).apply {
            if (!exists()) mkdirs()
        }
        val file = File(audioDir, actualFileName)

        try {
            FileOutputStream(file).use { it.write(audioData) }
            Log.d(TAG, "Audio file saved successfully at: ${file.absolutePath}")
            return file.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Error saving audio file: ${e.message}", e)
            throw e
        }
    }

    fun saveImageFile(context: Context, bitmap: Bitmap, fileName: String? = null): String {
        val actualFileName = fileName ?: generateUniqueFileName("image", "jpg")
        val imageDir = File(context.filesDir, STORY_IMAGE_DIR).apply {
            if (!exists()) mkdirs()
        }
        val file = File(imageDir, actualFileName)

        try {
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
            Log.d(TAG, "Image file saved successfully at: ${file.absolutePath}")
            return file.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Error saving image file: ${e.message}", e)
            throw e
        }
    }

    fun saveVoiceFeatures(context: Context, voiceFeatures: VoiceFeatures, fileName: String? = null): String {
        val actualFileName = fileName ?: generateUniqueFileName("voice_features", "json")
        val mfccDir = File(context.filesDir, MFCC_DATA_DIR).apply {
            if (!exists()) mkdirs()
        }
        val file = File(mfccDir, actualFileName)

        try {
            // VoiceFeatures 객체를 JSON으로 변환
            val jsonObj = JSONObject()
            jsonObj.put("averagePitch", voiceFeatures.averagePitch)
            jsonObj.put("pitchStdDev", voiceFeatures.pitchStdDev)

            // MFCC 값 배열 변환
            val mfccArray = JSONArray()
            for (coeffs in voiceFeatures.mfccValues) {
                val coeffArray = JSONArray()
                for (coeff in coeffs) {
                    coeffArray.put(coeff)
                }
                mfccArray.put(coeffArray)
            }
            jsonObj.put("mfccValues", mfccArray)

            // JSON 문자열 저장
            file.writeText(jsonObj.toString())
            Log.d(TAG, "Voice features file saved successfully at: ${file.absolutePath}")
            return file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving voice features file: ${e.message}", e)
            throw e
        }
    }

    fun readTextFile(filePath: String): String {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e(TAG, "Text file does not exist: $filePath")
            return ""
        }

        try {
            return file.readText()
        } catch (e: IOException) {
            Log.e(TAG, "Error reading text file: ${e.message}", e)
            return ""
        }
    }

    fun readAudioFile(filePath: String): ByteArray {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e(TAG, "Audio file does not exist: $filePath")
            return ByteArray(0)
        }

        try {
            return file.readBytes()
        } catch (e: IOException) {
            Log.e(TAG, "Error reading audio file: ${e.message}", e)
            return ByteArray(0)
        }
    }

    fun readVoiceFeatures(filePath: String): VoiceFeatures? {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e(TAG, "Voice features file does not exist: $filePath")
            return null
        }

        try {
            val jsonString = file.readText()
            val jsonObj = JSONObject(jsonString)

            val averagePitch = jsonObj.optDouble("averagePitch", 120.0)
            val pitchStdDev = jsonObj.optDouble("pitchStdDev", 15.0)

            // MFCC 값 파싱
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
                mfccValues.add(DoubleArray(13) { 0.0 })
            }

            return VoiceFeatures(averagePitch, pitchStdDev, mfccValues)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading voice features file: ${e.message}", e)
            return null
        }
    }

    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) {
            Log.d(TAG, "File does not exist: $filePath")
            return false
        }

        try {
            val result = file.delete()
            if (result) {
                Log.d(TAG, "File deleted successfully: $filePath")
            } else {
                Log.e(TAG, "Failed to delete file: $filePath")
            }
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: ${e.message}", e)
            return false
        }
    }

    fun generateUniqueFileName(prefix: String, extension: String): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        return "${prefix}_${timestamp}_${uuid}.${extension}"
    }

    // 이미지 최적화
    fun optimizeImage(bitmap: Bitmap, maxDimension: Int = 1024, quality: Int = 85): Bitmap {
        // 이미지 크기 조정 필요 여부 확인
        if (bitmap.width <= maxDimension && bitmap.height <= maxDimension) {
            return bitmap
        }

        // 비율 유지하면서 크기 조정
        val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    // 비트맵 -> ByteArray 변환
    fun bitmapToByteArray(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }
}