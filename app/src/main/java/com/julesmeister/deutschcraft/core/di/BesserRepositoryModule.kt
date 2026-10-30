// app/src/main/java/com/julesmeister/deutschcraft/core/di/BesserRepositoryModule.kt

package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.besser.data.repository.LocalBesserChallengeRepository
import com.julesmeister.deutschcraft.feature.besser.data.repository.LocalCapabilityRepository
import com.julesmeister.deutschcraft.feature.besser.data.repository.LocalMissionCapabilityEvidenceRepository
import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import com.julesmeister.deutschcraft.feature.besser.domain.repository.CapabilityRepository
import com.julesmeister.deutschcraft.feature.besser.domain.repository.MissionCapabilityEvidenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
abstract class BesserRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCapabilityRepository(
        implementation:
        LocalCapabilityRepository
    ): CapabilityRepository

    @Binds
    @Singleton
    abstract fun bindBesserChallengeRepository(
        implementation:
        LocalBesserChallengeRepository
    ): BesserChallengeRepository

    @Binds
    @Singleton
    abstract fun bindMissionCapabilityEvidenceRepository(
        implementation:
        LocalMissionCapabilityEvidenceRepository
    ): MissionCapabilityEvidenceRepository
}