package com.julesmeister.deutschcraft.feature.besser.domain.usecase


import com.julesmeister.deutschcraft.feature.besser.data.seed.SeededBesserCapabilities
import com.julesmeister.deutschcraft.feature.besser.domain.repository.CapabilityRepository
import javax.inject.Inject

class SeedBesserCapabilitiesUseCase @Inject constructor(
    private val capabilityRepository:
    CapabilityRepository
) {

    suspend operator fun invoke() {
        val existingCapabilityCount =
            capabilityRepository.count()

        if (existingCapabilityCount > 0) {
            return
        }

        val timestamp =
            System.currentTimeMillis()

        val seededCapabilities =
            SeededBesserCapabilities.create(
                timestamp =
                    timestamp
            )

        capabilityRepository.saveAll(
            capabilities =
                seededCapabilities
        )
    }
}