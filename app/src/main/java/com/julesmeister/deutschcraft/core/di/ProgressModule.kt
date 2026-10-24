package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.progress.data.LocalProgressRepository
import com.julesmeister.deutschcraft.feature.progress.domain.ProgressRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProgressModule {

    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        implementation: LocalProgressRepository
    ): ProgressRepository
}