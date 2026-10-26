package com.julesmeister.deutschcraft.core.database.entity.mission

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "mission_objectives",
    primaryKeys = [
        "id"
    ],
    foreignKeys = [
        ForeignKey(
            entity =
                MissionTemplateEntity::class,
            parentColumns = [
                "id"
            ],
            childColumns = [
                "missionTemplateId"
            ],
            onDelete =
                ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [
                "missionTemplateId"
            ]
        ),
        Index(
            value = [
                "missionTemplateId",
                "sequenceNumber"
            ],
            unique = true
        )
    ]
)
data class MissionObjectiveEntity(
    val id: String,
    val missionTemplateId: String,
    val title: String,
    val description: String,
    val sequenceNumber: Int,
    val isRequired: Boolean,
    val completionHint: String
)