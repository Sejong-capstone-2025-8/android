package com.toprunner.imagestory.repository

import android.content.Context
import android.graphics.Bitmap
import com.toprunner.imagestory.data.dao.ImageDao
import com.toprunner.imagestory.data.database.AppDatabase
import com.toprunner.imagestory.data.entity.ImageEntity
import com.toprunner.imagestory.util.FileStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageRepository(private val context: Context) {
    private val imageDao: ImageDao = AppDatabase.getInstance(context).imageDao()
    private val fileStorageManager = FileStorageManager()

    suspend fun saveImage(
        context: Context,
        title: String,
        bitmap: Bitmap
    ): Long = withContext(Dispatchers.IO) {
        // 이미지 파일 저장
        val imagePath = fileStorageManager.saveImageFile(context, bitmap)

        // 데이터베이스에 저장
        val imageEntity = ImageEntity(
            title = title,
            image_path = imagePath,
            created_at = System.currentTimeMillis()
        )

        // 데이터베이스에 삽입하고 ID 반환
        imageDao.insertImage(imageEntity)
    }

    suspend fun getImageById(imageId: Long): ImageEntity? = withContext(Dispatchers.IO) {
        imageDao.getImageById(imageId)
    }

    suspend fun getAllImages(): List<ImageEntity> = withContext(Dispatchers.IO) {
        imageDao.getAllImages()
    }

    suspend fun deleteImage(imageId: Long): Boolean = withContext(Dispatchers.IO) {
        val image = imageDao.getImageById(imageId) ?: return@withContext false

        // 이미지 파일 삭제
        fileStorageManager.deleteFile(image.image_path)

        // 데이터베이스에서 삭제
        val result = imageDao.deleteImage(imageId)
        result > 0
    }
}