package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionOpportunityAiGateway
import com.julesmeister.deutschcraft.feature.mission.data.ai.QwenMissionOpportunityAiGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MissionAiModule {

    @Binds
    @Singleton
    abstract fun bindMissionOpportunityAiGateway(
        implementation:
        QwenMissionOpportunityAiGateway
    ): MissionOpportunityAiGateway
}