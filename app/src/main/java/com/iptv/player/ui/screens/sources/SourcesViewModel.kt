package com.iptv.player.ui.screens.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.model.Source
import com.iptv.player.data.repository.SourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val sourceRepository: SourceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SourcesUiState())
    val uiState: StateFlow<SourcesUiState> = _uiState.asStateFlow()
    
    init {
        loadSources()
    }
    
    private fun loadSources() {
        viewModelScope.launch {
            sourceRepository.getAllSources()
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true) }
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
                .collect { sources ->
                    _uiState.value = _uiState.value.copy(
                        sources = sources,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
    
    fun toggleSourceStatus(sourceId: Long) {
        viewModelScope.launch {
            try {
                val source = sourceRepository.getSourceById(sourceId)
                source?.let {
                    sourceRepository.updateSourceStatus(sourceId, !it.isActive)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteSource(source: Source) {
        viewModelScope.launch {
            try {
                sourceRepository.deleteSource(source)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

data class SourcesUiState(
    val sources: List<Source> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)