// app/src/main/java/com/julesmeister/deutschcraft/feature/sync/di/SyncModule.kt

package com.julesmeister.deutschcraft.feature.sync.di

import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.feature.sync.data.SyncApi
import com.julesmeister.deutschcraft.feature.sync.data.SyncRepositoryImpl
import com.julesmeister.deutschcraft.feature.sync.domain.SyncRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(
    SingletonComponent::class
)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        implementation:
        SyncRepositoryImpl
    ): SyncRepository

    companion object {

        @Provides
        @Singleton
        fun provideSyncApi(
            retrofit: Retrofit
        ): SyncApi {
            check(
                BuildConfig.QWEN_BASE_URL
                    .isNotBlank()
            ) {
                "AI_PROXY_BASE_URL must be configured " +
                        "before synchronization can start."
            }

            return retrofit.create(
                SyncApi::class.java
            )
        }
    }
}