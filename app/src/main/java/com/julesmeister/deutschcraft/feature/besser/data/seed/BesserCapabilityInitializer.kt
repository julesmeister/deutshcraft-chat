package com.julesmeister.deutschcraft.feature.besser.data.seed

import com.julesmeister.deutschcraft.feature.besser.domain.usecase.SeedBesserCapabilitiesUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BesserCapabilityInitializer @Inject constructor(
    private val seedBesserCapabilities:
    SeedBesserCapabilitiesUseCase
) {

    suspend fun initialize() {
        seedBesserCapabilities()
    }
}