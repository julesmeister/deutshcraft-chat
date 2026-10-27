package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.learninganalytics.data.LocalMissionInteractionEvidenceRepository
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionInteractionEvidenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
abstract class MissionInteractionEvidenceModule {

    @Binds
    @Singleton
    abstract fun bindMissionInteractionEvidenceRepository(
        implementation:
        LocalMissionInteractionEvidenceRepository
    ): MissionInteractionEvidenceRepository
}