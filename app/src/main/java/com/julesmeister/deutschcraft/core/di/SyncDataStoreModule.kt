// app/src/main/java/com/julesmeister/deutschcraft/core/di/SyncDataStoreModule.kt

package com.julesmeister.deutschcraft.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SyncDataStore

@Module
@InstallIn(SingletonComponent::class)
object SyncDataStoreModule {

    @Provides
    @Singleton
    @SyncDataStore
    fun provideSyncDataStore(
        @ApplicationContext
        context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            scope =
                CoroutineScope(
                    SupervisorJob() +
                            Dispatchers.IO
                ),
            produceFile = {
                context.preferencesDataStoreFile(
                    SYNC_DATA_STORE_NAME
                )
            }
        )
    }

    private const val SYNC_DATA_STORE_NAME =
        "deutschcraft_sync"
}