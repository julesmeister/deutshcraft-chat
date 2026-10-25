package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.threads.data.LocalThreadRepository
import com.julesmeister.deutschcraft.feature.threads.domain.ThreadRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ThreadRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindThreadRepository(
        implementation:
        LocalThreadRepository
    ): ThreadRepository
}