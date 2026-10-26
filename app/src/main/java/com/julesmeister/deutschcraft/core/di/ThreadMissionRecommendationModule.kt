package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.mission.data.recommendation.LocalThreadMissionRecommendationRepository
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.ThreadMissionRecommendationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
abstract class ThreadMissionRecommendationModule {

    @Binds
    @Singleton
    abstract fun bindThreadMissionRecommendationRepository(
        implementation:
        LocalThreadMissionRecommendationRepository
    ): ThreadMissionRecommendationRepository
}