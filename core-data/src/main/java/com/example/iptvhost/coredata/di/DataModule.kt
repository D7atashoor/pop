package com.example.iptvhost.coredata.di

import com.example.iptvhost.coredata.repository.InMemorySourceRepository
import com.example.iptvhost.coredata.repository.SourceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindSourceRepository(impl: InMemorySourceRepository): SourceRepository
}