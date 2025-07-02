package com.iptv.player.di

import android.content.Context
import androidx.room.Room
import com.iptv.player.data.database.IPTVDatabase
import com.iptv.player.data.database.SourceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): IPTVDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            IPTVDatabase::class.java,
            "iptv_database"
        ).build()
    }
    
    @Provides
    fun provideSourceDao(database: IPTVDatabase): SourceDao {
        return database.sourceDao()
    }
}