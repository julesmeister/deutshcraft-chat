package com.julesmeister.deutschcraft.core.database.entity.mission.recommendation

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * One objective belonging to a persisted mission-thread
 * recommendation draft.
 */
@Entity(
    tableName =
        "mission_thread_recommendation_objectives",
    foreignKeys = [
        ForeignKey(
            entity =
                MissionThreadRecommendationEntity::class,
            parentColumns = [
                "id"
            ],
            childColumns = [
                "recommendationId"
            ],
            onDelete =
                ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [
                "recommendationId"
            ]
        ),
        Index(
            value = [
                "recommendationId",
                "sequenceNumber"
            ],
            unique =
                true
        )
    ]
)
data class MissionThreadRecommendationObjectiveEntity(
    @PrimaryKey
    val id: String,

    val recommendationId: String,

    val sequenceNumber: Int,

    val title: String,

    val description: String,

    val completionHint: String,

    /*
     * These use the same JSON-list storage approach already
     * used by MissionObjectiveEntity.
     */
    val coachingHintsJson: String,

    val targetVocabularyJson: String,

    val examplePhrasesJson: String,

    val isRequired: Boolean
)