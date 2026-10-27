package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.mission.data.recommendation.RoomMissionThreadRecommendationStore
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendationStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
abstract class MissionThreadRecommendationStoreModule {

    @Binds
    @Singleton
    abstract fun bindMissionThreadRecommendationStore(
        implementation:
        RoomMissionThreadRecommendationStore
    ): MissionThreadRecommendationStore
}