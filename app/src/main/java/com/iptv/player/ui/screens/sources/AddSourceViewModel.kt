package com.iptv.player.ui.screens.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.model.Source
import com.iptv.player.data.model.SourceType
import com.iptv.player.data.repository.SourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSourceViewModel @Inject constructor(
    private val sourceRepository: SourceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddSourceUiState())
    val uiState: StateFlow<AddSourceUiState> = _uiState.asStateFlow()
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }
    
    fun updateUrl(url: String) {
        _uiState.value = _uiState.value.copy(url = url)
    }
    
    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    fun updateMacAddress(macAddress: String) {
        _uiState.value = _uiState.value.copy(macAddress = macAddress)
    }
    
    fun updateSerialNumber(serialNumber: String) {
        _uiState.value = _uiState.value.copy(serialNumber = serialNumber)
    }
    
    fun updateSourceType(type: SourceType) {
        _uiState.value = _uiState.value.copy(
            selectedType = type,
            // Clear fields when changing type
            url = "",
            username = "",
            password = "",
            macAddress = "",
            serialNumber = ""
        )
    }
    
    fun addSource() {
        val currentState = _uiState.value
        
        if (currentState.name.isBlank() || currentState.url.isBlank()) {
            _uiState.value = currentState.copy(error = "يرجى ملء جميع الحقول المطلوبة")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            try {
                val source = Source(
                    name = currentState.name.trim(),
                    type = currentState.selectedType,
                    url = currentState.url.trim(),
                    username = currentState.username.takeIf { it.isNotBlank() },
                    password = currentState.password.takeIf { it.isNotBlank() }
                )
                
                sourceRepository.insertSource(source)
                
                _uiState.value = currentState.copy(
                    isLoading = false,
                    isSourceAdded = true
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = "فشل في إضافة المصدر: ${e.message}"
                )
            }
        }
    }
}

data class AddSourceUiState(
    val name: String = "",
    val selectedType: SourceType = SourceType.M3U,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val macAddress: String = "",
    val serialNumber: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSourceAdded: Boolean = false
)