package com.toprunner.imagestory.util

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import com.toprunner.imagestory.model.VoiceFeatures
import java.io.File
import java.io.FileOutputStream
import java.util.*

class FileStorageManager {

    companion object {
        private const val STORY_CONTENT_DIR = "story_contents"
        private const val STORY_IMAGE_DIR = "story_images"
        private const val AUDIO_DIR = "audio_files"
        private const val VOICE_FEATURES_DIR = "voice_features"
        private const val MUSIC_DIR = "music_files"
    }

    fun saveTextFile(context: Context, content: String, fileName: String? = null): String {
        val actualFileName = fileName ?: generateUniqueFileName("story_content", "txt")
        val contentDir = File(context.filesDir, STORY_CONTENT_DIR).apply {
            if (!exists()) mkdirs()
        }
        val file = File(contentDir, actualFileName)

        file.writeText(content)
        return file.absolutePath
    }

    fun saveAudioFile(context: Context, audioData: ByteArray, fileName: String? = null): String {
        val actualFileName = fileName ?: generateUniqueFileName("audio", "wav")
        val audioDir = File(context.filesDir, AUDIO_DIR).apply {
            if (!exists()) mkdirs()
        }
        val file = File(audioDir, actualFileName)

        FileOutputStream(file).use { it.write(audioData) }
        return file.absolutePath
    }

    fun saveImageFile(context: Context, bitmap: Bitmap, fileName: String? = null): String {
        val actualFileName = fileName ?: generateUniqueFileName("image", "jpg")
        val imageDir = File(context.filesDir, STORY_IMAGE_DIR).apply {
            if (!exists()) mkdirs()
        }
        val file = File(imageDir, actualFileName)

        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        return file.absolutePath
    }

    fun saveVoiceFeatures(context: Context, voiceFeatures: VoiceFeatures, fileName: String? = null): String {
        val actualFileName = fileName ?: generateUniqueFileName("voice_features", "json")
        val featuresDir = File(context.filesDir, VOICE_FEATURES_DIR).apply {
            if (!exists()) mkdirs()
        }
        val file = File(featuresDir, actualFileName)

        // VoiceFeatures 객체를 JSON으로 변환하여 저장
        val gson = Gson()
        val json = gson.toJson(voiceFeatures)
        file.writeText(json)

        return file.absolutePath
    }

    fun readTextFile(filePath: String): String {
        return File(filePath).readText()
    }

    fun readAudioFile(filePath: String): ByteArray {
        return File(filePath).readBytes()
    }

    fun readVoiceFeatures(filePath: String): VoiceFeatures {
        val json = File(filePath).readText()
        val gson = Gson()
        return gson.fromJson(json, VoiceFeatures::class.java)
    }

    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) file.delete() else false
    }

    fun generateUniqueFileName(prefix: String, extension: String): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        return "${prefix}_${timestamp}_${uuid}.${extension}"
    }
}