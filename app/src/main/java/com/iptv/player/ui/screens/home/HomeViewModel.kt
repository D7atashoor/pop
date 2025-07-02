package com.iptv.player.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.repository.SourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sourceRepository: SourceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            combine(
                sourceRepository.getAllSources(),
                sourceRepository.getActiveSources()
            ) { allSources, activeSources ->
                _uiState.value = _uiState.value.copy(
                    sourcesCount = allSources.size,
                    activeSourcesCount = activeSources.size
                )
            }.collect()
        }
    }
}

data class HomeUiState(
    val sourcesCount: Int = 0,
    val activeSourcesCount: Int = 0,
    val isLoading: Boolean = false
)