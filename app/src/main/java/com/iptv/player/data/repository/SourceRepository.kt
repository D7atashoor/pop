package com.iptv.player.data.repository

import com.iptv.player.data.database.SourceDao
import com.iptv.player.data.model.Source
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceRepository @Inject constructor(
    private val sourceDao: SourceDao
) {
    
    fun getAllSources(): Flow<List<Source>> = sourceDao.getAllSources()
    
    fun getActiveSources(): Flow<List<Source>> = sourceDao.getActiveSources()
    
    suspend fun getSourceById(id: Long): Source? = sourceDao.getSourceById(id)
    
    suspend fun insertSource(source: Source): Long = sourceDao.insertSource(source)
    
    suspend fun updateSource(source: Source) = sourceDao.updateSource(source)
    
    suspend fun deleteSource(source: Source) = sourceDao.deleteSource(source)
    
    suspend fun deleteSourceById(id: Long) = sourceDao.deleteSourceById(id)
    
    suspend fun updateSourceStatus(id: Long, isActive: Boolean) = 
        sourceDao.updateSourceStatus(id, isActive)
}