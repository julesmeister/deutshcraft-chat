package com.julesmeister.deutschcraft.feature.sync.di

import com.julesmeister.deutschcraft.feature.sync.data.SyncApi
import com.julesmeister.deutschcraft.feature.sync.data.SyncRepositoryImpl
import com.julesmeister.deutschcraft.feature.sync.domain.SyncRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        impl: SyncRepositoryImpl
    ): SyncRepository

    companion object {

        @Provides
        @Singleton
        fun provideSyncApi(
            retrofit: Retrofit
        ): SyncApi =
            retrofit.create(SyncApi::class.java)
    }
}
