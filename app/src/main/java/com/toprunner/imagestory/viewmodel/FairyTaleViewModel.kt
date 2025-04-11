package com.toprunner.imagestory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import com.toprunner.imagestory.repository.FairyTaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FairyTaleViewModel(
    private val fairyTaleRepository: FairyTaleRepository
) : ViewModel() {

    private val _fairyTales = MutableStateFlow<List<FairyTaleEntity>>(emptyList())
    val fairyTales: StateFlow<List<FairyTaleEntity>> get() = _fairyTales

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadFairyTales()
    }

    fun loadFairyTales() {
        viewModelScope.launch {
            _isLoading.value = true
            _fairyTales.value = fairyTaleRepository.getAllFairyTales()
            _isLoading.value = false
        }
    }

    fun saveFairyTale(fairyTaleEntity: FairyTaleEntity) {
        viewModelScope.launch {
            fairyTaleRepository.insertFairyTale(fairyTaleEntity)
            loadFairyTales()
        }
    }

    fun deleteFairyTale(fairyTaleId: Long) { // ← 타입 수정됨
        viewModelScope.launch {
            fairyTaleRepository.deleteFairyTale(fairyTaleId)
            loadFairyTales()
        }
    }
}
