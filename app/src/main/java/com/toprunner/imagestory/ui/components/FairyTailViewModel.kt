package com.toprunner.imagestory.ui.components

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.toprunner.imagestory.data.database.AppDatabase
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import kotlinx.coroutines.launch
import androidx.compose.runtime.State


class FairyTaleViewModel(application: Application) : AndroidViewModel(application) {
    private val fairyTaleDao = AppDatabase.getInstance(application).fairyTaleDao()

    private val _fairyTales = mutableStateOf(emptyList<FairyTaleEntity>())
    val fairyTales: State<List<FairyTaleEntity>> = _fairyTales

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadFairyTales()
    }

    fun loadFairyTales() {
        viewModelScope.launch {
            _isLoading.value = true
            _fairyTales.value = fairyTaleDao.getAllFairyTales()
            _isLoading.value = false
        }
    }
}
