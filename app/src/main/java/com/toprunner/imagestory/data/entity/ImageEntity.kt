package com.toprunner.imagestory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true)
    val image_id: Long = 0,
    val title: String,
    val image_path: String,
    val created_at: Long = System.currentTimeMillis()
)