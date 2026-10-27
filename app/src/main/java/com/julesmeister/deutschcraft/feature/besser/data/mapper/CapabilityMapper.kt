package com.julesmeister.deutschcraft.feature.besser.data.mapper

import com.julesmeister.deutschcraft.core.database.entity.besser.CapabilityEntity
import com.julesmeister.deutschcraft.feature.besser.domain.Capability
import com.julesmeister.deutschcraft.feature.besser.domain.CapabilityCategory

fun CapabilityEntity.toDomain():
        Capability {
    return Capability(
        id = id,
        name = name,
        description = description,
        category =
            CapabilityCategory.fromStorage(
                value = category
            ),
        cefrLevel = cefrLevel,
        sortOrder = sortOrder,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Capability.toEntity():
        CapabilityEntity {
    return CapabilityEntity(
        id = id,
        name = name,
        description = description,
        category = category.name,
        cefrLevel = cefrLevel,
        sortOrder = sortOrder,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}