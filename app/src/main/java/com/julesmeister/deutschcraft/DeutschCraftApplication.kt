package com.julesmeister.deutschcraft

import android.app.Application
import android.util.Log
import com.julesmeister.deutschcraft.feature.besser.data.catalogue.CapabilityCatalogueInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class DeutschCraftApplication :
    Application() {

    @Inject
    lateinit var capabilityCatalogueInitializer:
            CapabilityCatalogueInitializer

    private val applicationScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )

    override fun onCreate() {
        super.onCreate()

        initializeBesserCatalogue()
    }

    private fun initializeBesserCatalogue() {
        applicationScope.launch {
            runCatching {
                capabilityCatalogueInitializer.initialize()
            }
                .onSuccess {
                    Log.i(
                        APPLICATION_TAG,
                        "Besser capability catalogue ready."
                    )
                }
                .onFailure { throwable ->
                    /*
                     * Catalogue initialization must never prevent
                     * DeutschCraft from starting.
                     */
                    Log.e(
                        APPLICATION_TAG,
                        "Besser capability catalogue initialization failed.",
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