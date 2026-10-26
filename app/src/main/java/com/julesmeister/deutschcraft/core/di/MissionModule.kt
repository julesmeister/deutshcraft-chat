package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.mission.data.LocalFirstMissionRepository
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MissionModule {

    @Binds
    @Singleton
    abstract fun bindMissionRepository(
        implementation:
        LocalFirstMissionRepository
    ): MissionRepository
}