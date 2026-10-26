package com.julesmeister.deutschcraft.core.database.entity.mission

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "mission_templates"
)
data class MissionTemplateEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val scenario: String,
    val learnerRole: String,
    val assistantRole: String,
    val difficulty: String,
    val icon: String,
    val isEnabled: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)