package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.adaptivelearning.data.LocalFirstAdaptiveLearningRepository
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.repository.AdaptiveLearningRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdaptiveLearningModule {

    @Binds
    @Singleton
    abstract fun bindAdaptiveLearningRepository(
        implementation:
        LocalFirstAdaptiveLearningRepository
    ): AdaptiveLearningRepository
}