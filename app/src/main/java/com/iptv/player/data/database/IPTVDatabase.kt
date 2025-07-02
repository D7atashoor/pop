package com.iptv.player.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.iptv.player.data.model.Source

@Database(
    entities = [Source::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class IPTVDatabase : RoomDatabase() {
    
    abstract fun sourceDao(): SourceDao
    
    companion object {
        @Volatile
        private var INSTANCE: IPTVDatabase? = null
        
        fun getDatabase(context: Context): IPTVDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IPTVDatabase::class.java,
                    "iptv_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}