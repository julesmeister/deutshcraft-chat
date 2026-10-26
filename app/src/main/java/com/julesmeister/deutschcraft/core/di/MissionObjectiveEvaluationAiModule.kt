package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionObjectiveEvaluationAiGateway
import com.julesmeister.deutschcraft.feature.mission.data.ai.QwenMissionObjectiveEvaluationAiGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MissionObjectiveEvaluationAiModule {

    @Binds
    @Singleton
    abstract fun bindMissionObjectiveEvaluationAiGateway(
        implementation:
        QwenMissionObjectiveEvaluationAiGateway
    ): MissionObjectiveEvaluationAiGateway
}