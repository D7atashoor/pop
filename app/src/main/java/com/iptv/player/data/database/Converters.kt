package com.iptv.player.data.database

import androidx.room.TypeConverter
import com.iptv.player.data.model.SourceType

class Converters {
    
    @TypeConverter
    fun fromSourceType(sourceType: SourceType): String {
        return sourceType.name
    }
    
    @TypeConverter
    fun toSourceType(sourceType: String): SourceType {
        return SourceType.valueOf(sourceType)
    }
}