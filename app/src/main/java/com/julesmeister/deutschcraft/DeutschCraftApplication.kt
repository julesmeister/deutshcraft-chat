// app/src/main/java/com/julesmeister/deutschcraft/DeutschCraftApplication.kt

package com.julesmeister.deutschcraft

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.julesmeister.deutschcraft.feature.auth.di.SyncWorkerStartupEffect
import com.julesmeister.deutschcraft.feature.besser.data.catalogue.CapabilityCatalogueInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class DeutschCraftApplication :
    Application(),
    Configuration.Provider {

    @Inject
    lateinit var capabilityCatalogueInitializer:
            CapabilityCatalogueInitializer

    @Inject
    lateinit var workerFactory:
            HiltWorkerFactory

    @Inject
    lateinit var syncWorkerStartupEffect:
            SyncWorkerStartupEffect

    private val applicationScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )

    override val workManagerConfiguration:
            Configuration
        get() =
            Configuration.Builder()
                .setWorkerFactory(
                    workerFactory
                )
                .setMinimumLoggingLevel(
                    if (BuildConfig.DEBUG) {
                        Log.DEBUG
                    } else {
                        Log.INFO
                    }
                )
                .build()

    override fun onCreate() {
        super.onCreate()

        initializeBesserCatalogue()

        syncWorkerStartupEffect.start()
    }

    private fun initializeBesserCatalogue() {
        applicationScope.launch {
            runCatching {
                capabilityCatalogueInitializer
                    .initialize()
            }
                .onSuccess {
                    Log.i(
                        APPLICATION_TAG,
                        "Besser capability catalogue ready."
                    )
                }
                .onFailure { throwable ->
                    Log.e(
                        APPLICATION_TAG,
                        "Besser capability catalogue " +
                                "initialization failed.",
                        throwable
                    )
                }
        }
    }

    private companion object {

        const val APPLICATION_TAG =
            "DeutschCraftApplication"
    }
}