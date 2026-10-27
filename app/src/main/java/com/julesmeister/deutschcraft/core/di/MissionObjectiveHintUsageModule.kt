package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.learninganalytics.data.LocalMissionObjectiveHintUsageRepository
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionObjectiveHintUsageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
 * Tells Hilt:
 *
 * Whenever a class requests
 * MissionObjectiveHintUsageRepository,
 * provide LocalMissionObjectiveHintUsageRepository.
 */
@Module
@InstallIn(
    SingletonComponent::class
)
abstract class MissionObjectiveHintUsageModule {

    @Binds
    @Singleton
    abstract fun bindMissionObjectiveHintUsageRepository(
        implementation:
        LocalMissionObjectiveHintUsageRepository
    ): MissionObjectiveHintUsageRepository
}