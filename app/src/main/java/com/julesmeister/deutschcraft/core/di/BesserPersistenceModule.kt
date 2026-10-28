package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.BesserChallengeDao
import com.julesmeister.deutschcraft.core.database.dao.CapabilityDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
object BesserPersistenceModule {

    @Provides
    @Singleton
    fun provideCapabilityDao(
        database:
        DeutschCraftDatabase
    ): CapabilityDao {
        return database.capabilityDao()
    }

    @Provides
    @Singleton
    fun provideBesserChallengeDao(
        database:
        DeutschCraftDatabase
    ): BesserChallengeDao {
        return database.besserChallengeDao()
    }
}