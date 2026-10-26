package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionThreadRecommendationAiGateway
import com.julesmeister.deutschcraft.feature.mission.data.ai.QwenMissionThreadRecommendationAiGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MissionRecommendationAiModule {

    @Binds
    @Singleton
    abstract fun bindMissionThreadRecommendationAiGateway(
        implementation:
        QwenMissionThreadRecommendationAiGateway
    ): MissionThreadRecommendationAiGateway
}