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

    private val _isCreatingNewStory = MutableStateFlow(false)
    val isCreatingNewStory: StateFlow<Boolean> = _isCreatingNewStory


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

    fun deleteFairyTale(fairyTaleId: Long) { // 타입 수정됨
        viewModelScope.launch {
            fairyTaleRepository.deleteFairyTale(fairyTaleId)
            loadFairyTales()
        }
    }
    // 추천 음성으로 동화 생성 시작 시 호출
    fun startCreatingRecommendedVoiceStory() {
        _isCreatingNewStory.value = true
    }
    fun finishCreatingRecommendedVoiceStory() {
        _isCreatingNewStory.value = false
        // 동화 목록 갱신
        loadFairyTales()
    }
}
