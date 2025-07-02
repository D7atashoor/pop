package com.example.iptvhost.featuresources.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iptvhost.coredata.model.IptvSource
import com.example.iptvhost.coredata.repository.SourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val repository: SourceRepository
) : ViewModel() {

    val sources: StateFlow<List<IptvSource>> = repository.observeSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addSource(source: IptvSource) {
        viewModelScope.launch {
            repository.addSource(source)
        }
    }

    fun deleteSource(id: String) {
        viewModelScope.launch {
            repository.removeSource(id)
        }
    }
}