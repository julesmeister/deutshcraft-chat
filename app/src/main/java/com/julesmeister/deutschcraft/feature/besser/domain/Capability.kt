package com.julesmeister.deutschcraft.feature.besser.domain

data class Capability(
    val id: String,
    val name: String,
    val description: String,
    val category: CapabilityCategory,
    val cefrLevel: String,
    val sortOrder: Int,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)