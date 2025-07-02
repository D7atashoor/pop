package com.iptv.player.data.database

import androidx.room.*
import com.iptv.player.data.model.Source
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    
    @Query("SELECT * FROM sources ORDER BY name ASC")
    fun getAllSources(): Flow<List<Source>>
    
    @Query("SELECT * FROM sources WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveSources(): Flow<List<Source>>
    
    @Query("SELECT * FROM sources WHERE id = :id")
    suspend fun getSourceById(id: Long): Source?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: Source): Long
    
    @Update
    suspend fun updateSource(source: Source)
    
    @Delete
    suspend fun deleteSource(source: Source)
    
    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun deleteSourceById(id: Long)
    
    @Query("UPDATE sources SET isActive = :isActive WHERE id = :id")
    suspend fun updateSourceStatus(id: Long, isActive: Boolean)
}