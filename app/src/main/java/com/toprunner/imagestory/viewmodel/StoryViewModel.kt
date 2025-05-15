package com.toprunner.imagestory.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toprunner.imagestory.service.GPTService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoryViewModel(
    private val gptService: GPTService = GPTService()
) : ViewModel() {

    // 1) 화면 상태
    private val _capturedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedImageBitmap: StateFlow<Bitmap?> = _capturedImageBitmap.asStateFlow()

    private val _selectedTheme = MutableStateFlow<String?>(null)
    val selectedTheme: StateFlow<String?> = _selectedTheme.asStateFlow()

    // 2) 로딩 & 에러
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    // 3) 동화 생성 호출
    fun generateStory() {
        val image = _capturedImageBitmap.value ?: return
        val theme = _selectedTheme.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                gptService.generateStory(image, theme)
                // TODO: 성공 시 필요한 후속 처리
            } catch (e: Exception) {
                _errorMsg.value = e.message ?: "알 수 없는 오류"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMsg.value = null
    }

    fun setImage(bitmap: Bitmap) {
        _capturedImageBitmap.value = bitmap
    }

    fun setTheme(theme: String) {
        _selectedTheme.value = theme
    }
}