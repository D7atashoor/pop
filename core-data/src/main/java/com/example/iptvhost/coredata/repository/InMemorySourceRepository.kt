package com.example.iptvhost.coredata.repository

import com.example.iptvhost.coredata.model.IptvSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemorySourceRepository @Inject constructor(): SourceRepository {
    private val flow = MutableStateFlow<List<IptvSource>>(emptyList())

    override fun observeSources(): Flow<List<IptvSource>> = flow

    override suspend fun getSources(): List<IptvSource> = flow.value

    override suspend fun addSource(source: IptvSource) {
        flow.value = flow.value + source
    }

    override suspend fun removeSource(sourceId: String) {
        flow.value = flow.value.filterNot { it.id == sourceId }
    }
}