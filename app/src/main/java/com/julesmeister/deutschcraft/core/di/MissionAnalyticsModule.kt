package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.learninganalytics.data.LocalMissionAnalyticsRepository
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionAnalyticsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
abstract class MissionAnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindMissionAnalyticsRepository(
        implementation:
        LocalMissionAnalyticsRepository
    ): MissionAnalyticsRepository
}