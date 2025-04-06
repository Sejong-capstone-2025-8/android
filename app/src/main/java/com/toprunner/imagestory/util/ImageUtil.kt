package com.toprunner.imagestory.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ImageUtil {
    companion object {
        private const val TAG = "ImageUtil"
    }

    fun compressImage(bitmap: Bitmap, quality: Int): Bitmap {
        // 비트맵을 압축
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val compressedBytes = outputStream.toByteArray()

        // 압축된 데이터로 새 비트맵 생성
        return BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
    }

    fun resizeImage(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        // 원본 비트맵의 너비와 높이
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        // 크기 조정이 필요한지 확인
        if (originalWidth == width && originalHeight == height) {
            return bitmap
        }

        // 비율 계산
        val scaleWidth = width.toFloat() / originalWidth
        val scaleHeight = height.toFloat() / originalHeight

        // 비율을 유지하면서 크기 조정
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        // 새 비트맵 생성
        return Bitmap.createBitmap(bitmap, 0, 0, originalWidth, originalHeight, matrix, false)
    }

    fun bitmapToUri(bitmap: Bitmap, context: Context): Uri {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        try {
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                output.flush()
            }

            return Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting bitmap to URI: ${e.message}", e)
            throw e
        }
    }

    fun uriToBitmap(uri: Uri, context: Context): Bitmap {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: throw IllegalArgumentException("URI에서 비트맵을 로드할 수 없습니다.")
    }
}