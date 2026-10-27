package com.julesmeister.deutschcraft.feature.besser.data.repository


import com.julesmeister.deutschcraft.core.database.dao.CapabilityDao
import com.julesmeister.deutschcraft.feature.besser.data.mapper.toDomain
import com.julesmeister.deutschcraft.feature.besser.data.mapper.toEntity
import com.julesmeister.deutschcraft.feature.besser.domain.Capability
import com.julesmeister.deutschcraft.feature.besser.domain.CapabilityCategory
import com.julesmeister.deutschcraft.feature.besser.domain.repository.CapabilityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalCapabilityRepository @Inject constructor(
    private val capabilityDao:
    CapabilityDao
) : CapabilityRepository {

    override suspend fun save(
        capability: Capability
    ) {
        capabilityDao.upsert(
            capability =
                capability.toEntity()
        )
    }

    override suspend fun saveAll(
        capabilities: List<Capability>
    ) {
        if (capabilities.isEmpty()) {
            return
        }

        capabilityDao.upsertAll(
            capabilities =
                capabilities.map(
                    Capability::toEntity
                )
        )
    }

    override suspend fun getById(
        capabilityId: String
    ): Capability? {
        return capabilityDao
            .getById(
                capabilityId =
                    capabilityId
            )
            ?.toDomain()
    }

    override fun observeById(
        capabilityId: String
    ): Flow<Capability?> {
        return capabilityDao
            .observeById(
                capabilityId =
                    capabilityId
            )
            .map { entity ->
                entity?.toDomain()
            }
    }

    override fun observeActive():
            Flow<List<Capability>> {
        return capabilityDao
            .observeActive()
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override fun observeActiveByCefrLevel(
        cefrLevel: String
    ): Flow<List<Capability>> {
        return capabilityDao
            .observeActiveByCefrLevel(
                cefrLevel =
                    cefrLevel
                        .trim()
                        .uppercase()
            )
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override fun observeActiveByCategory(
        category: CapabilityCategory
    ): Flow<List<Capability>> {
        return capabilityDao
            .observeActiveByCategory(
                category =
                    category.name
            )
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun getActiveForShortlist(
        cefrLevels: List<String>
    ): List<Capability> {
        val normalizedLevels =
            cefrLevels
                .map(
                    String::trim
                )
                .map(
                    String::uppercase
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()

        if (normalizedLevels.isEmpty()) {
            return emptyList()
        }

        return capabilityDao
            .getActiveForShortlist(
                cefrLevels =
                    normalizedLevels
            )
            .map { entity ->
                entity.toDomain()
            }
    }

    override suspend fun setActive(
        capabilityId: String,
        isActive: Boolean,
        updatedAt: Long
    ): Boolean {
        val changedRows =
            capabilityDao.setActive(
                capabilityId =
                    capabilityId,
                isActive =
                    isActive,
                updatedAt =
                    updatedAt
            )

        return changedRows == 1
    }

    override suspend fun count():
            Int {
        return capabilityDao.count()
    }
}