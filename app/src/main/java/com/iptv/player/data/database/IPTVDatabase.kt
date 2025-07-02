package com.iptv.player.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.iptv.player.data.model.Source

@Database(
    entities = [Source::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class IPTVDatabase : RoomDatabase() {
    
    abstract fun sourceDao(): SourceDao
    
    companion object {
        @Volatile
        private var INSTANCE: IPTVDatabase? = null
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE sources ADD COLUMN macAddress TEXT")
                database.execSQL("ALTER TABLE sources ADD COLUMN portalPath TEXT")
                database.execSQL("ALTER TABLE sources ADD COLUMN serialNumber TEXT")
                database.execSQL("ALTER TABLE sources ADD COLUMN deviceId TEXT")
                database.execSQL("ALTER TABLE sources ADD COLUMN userAgent TEXT")
                database.execSQL("ALTER TABLE sources ADD COLUMN referer TEXT")
                database.execSQL("ALTER TABLE sources ADD COLUMN lastChecked INTEGER")
                database.execSQL("ALTER TABLE sources ADD COLUMN accountStatus TEXT")
                database.execSQL("ALTER TABLE sources ADD COLUMN expiryDate TEXT")
                database.execSQL("ALTER TABLE sources ADD COLUMN maxConnections INTEGER")
                database.execSQL("ALTER TABLE sources ADD COLUMN activeConnections INTEGER")
                database.execSQL("ALTER TABLE sources ADD COLUMN isTrial INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE sources ADD COLUMN countryCode TEXT")
                database.execSQL("ALTER TABLE sources ADD COLUMN serverInfo TEXT")
            }
        }
        
        fun getDatabase(context: Context): IPTVDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IPTVDatabase::class.java,
                    "iptv_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}