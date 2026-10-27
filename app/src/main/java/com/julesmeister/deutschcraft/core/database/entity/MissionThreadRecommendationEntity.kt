package com.julesmeister.deutschcraft.feature.mission.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName =
        "mission_thread_recommendations",
    indices = [
        Index(
            value = [
                "status",
                "cefrLevel",
                "createdAt"
            ]
        ),
        Index(
            value = [
                "scenarioType"
            ]
        ),
        Index(
            value = [
                "sourceRecommendationId"
            ],
            unique =
                false
        )
    ]
)
data class MissionThreadRecommendationEntity(
    @PrimaryKey
    val id: String,

    /*
     * Stable semantic category such as JOB_INTERVIEW or
     * CAFE_ORDERING.
     */
    val scenarioType: String,

    val cefrLevel: String,

    val title: String,

    val previewText: String,

    /*
     * The generated mission draft is stored as JSON.
     *
     * This lets the recommendation be converted into a real
     * mission without calling Qwen again.
     */
    val opportunityJson: String,

    val status: String,

    val createdAt: Long,

    val displayedAt: Long?,

    val skippedAt: Long?,

    val startedAt: Long?,

    val expiresAt: Long?,

    /*
     * Set after this recommendation creates a real mission.
     */
    val conversationMissionId: String?,

    /*
     * Optional link to an older equivalent recommendation.
     */
    val sourceRecommendationId: String?
)