package com.julesmeister.deutschcraft.feature.besser.data.catalogue

import android.util.Log
import com.julesmeister.deutschcraft.core.database.dao.CapabilityDao
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Makes sure DeutschCraft's built-in Besser curriculum exists
 * in Room.
 *
 * Stable IDs make this operation idempotent.
 *
 * We intentionally do not use:
 *
 *     if (dao.count() == 0)
 *
 * because future app versions may add capabilities to an
 * already-populated catalogue.
 */
@Singleton
class CapabilityCatalogueInitializer
@Inject constructor(
    private val capabilityDao:
    CapabilityDao
) {

    suspend fun initialize() {
        val now =
            System.currentTimeMillis()

        val catalogue =
            BuiltInCapabilityCatalogue.create(
                now =
                    now
            )

        capabilityDao.upsertAll(
            capabilities =
                catalogue
        )

        Log.i(
            CATALOGUE_TAG,
            buildString {
                append(
                    "Besser capability catalogue initialized. "
                )
                append(
                    "capabilities="
                )
                append(
                    catalogue.size
                )
            }
        )
    }

    private companion object {

        const val CATALOGUE_TAG =
            "BesserCatalogue"
    }
}