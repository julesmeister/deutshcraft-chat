package com.julesmeister.deutschcraft.feature.besser.domain.repository

import com.julesmeister.deutschcraft.feature.besser.domain.Capability
import com.julesmeister.deutschcraft.feature.besser.domain.CapabilityCategory
import kotlinx.coroutines.flow.Flow

interface CapabilityRepository {

    suspend fun save(
        capability: Capability
    )

    suspend fun saveAll(
        capabilities: List<Capability>
    )

    suspend fun getById(
        capabilityId: String
    ): Capability?

    fun observeById(
        capabilityId: String
    ): Flow<Capability?>

    fun observeActive():
            Flow<List<Capability>>

    fun observeActiveByCefrLevel(
        cefrLevel: String
    ): Flow<List<Capability>>

    fun observeActiveByCategory(
        category: CapabilityCategory
    ): Flow<List<Capability>>

    suspend fun getActiveForShortlist(
        cefrLevels: List<String>
    ): List<Capability>

    suspend fun setActive(
        capabilityId: String,
        isActive: Boolean,
        updatedAt: Long
    ): Boolean

    suspend fun count():
            Int
}