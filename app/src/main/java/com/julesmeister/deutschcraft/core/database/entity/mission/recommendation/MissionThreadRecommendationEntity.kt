package com.julesmeister.deutschcraft.core.database.entity.mission.recommendation

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * Persisted draft recommendation displayed in the featured
 * mission cell on the thread-list screen.
 *
 * This is not yet a real ConversationMission.
 *
 * A real conversation and mission are created only after the
 * learner starts the recommendation.
 */
@Entity(
    tableName =
        "mission_thread_recommendations",
    indices = [
        Index(
            value = [
                "cefrLevel",
                "status",
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
                "status",
                "expiresAt"
            ]
        )
    ]
)
data class MissionThreadRecommendationEntity(
    @PrimaryKey
    val id: String,

    /*
     * Stable semantic identity.
     *
     * Examples:
     *
     * CAFE_ORDERING
     * JOB_INTERVIEW
     * DAILY_ROUTINE
     */
    val scenarioType: String,

    val cefrLevel: String,

    val title: String,

    val previewText: String,

    val confidence: Float,

    val reason: String,

    val description: String,

    val scenario: String,

    val learnerRole: String,

    val assistantRole: String,

    val icon: String,

    /*
     * READY
     * DISPLAYED
     * SKIPPED
     * STARTED
     * COMPLETED
     * EXPIRED
     * INVALID
     */
    val status: String,

    val createdAt: Long,

    val updatedAt: Long,

    val displayedAt: Long?,

    val skippedAt: Long?,

    val startedAt: Long?,

    val completedAt: Long?,

    val expiresAt: Long?,

    /*
     * Populated after the recommendation creates a real
     * conversation.
     */
    val conversationId: String?,

    /*
     * Populated after the recommendation creates a real
     * ConversationMission.
     */
    val conversationMissionId: String?
)