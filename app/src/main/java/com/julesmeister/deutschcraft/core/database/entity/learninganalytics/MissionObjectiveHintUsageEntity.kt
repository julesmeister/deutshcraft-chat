package com.julesmeister.deutschcraft.core.database.entity.learninganalytics

import androidx.room.Entity
import androidx.room.Index

/*
 * Records one hint revealed for one mission objective.
 *
 * A separate table is used instead of adding one counter to
 * MissionObjectiveProgressEntity because this preserves:
 *
 * - Which hint level was revealed
 * - When the hint was revealed
 * - Which conversation mission used it
 *
 * hintLevel:
 *
 * 1 = gentle hint
 * 2 = contextual or more specific hint
 * 3 = direct wording suggestion
 */
@Entity(
    tableName =
        "mission_objective_hint_usage",
    primaryKeys = [
        "conversationMissionId",
        "objectiveId",
        "hintLevel"
    ],
    indices = [
        Index(
            value = [
                "conversationMissionId"
            ]
        ),
        Index(
            value = [
                "conversationMissionId",
                "objectiveId"
            ]
        ),
        Index(
            value = [
                "revealedAt"
            ]
        )
    ]
)
data class MissionObjectiveHintUsageEntity(
    val conversationMissionId: String,

    val objectiveId: String,

    val hintLevel: Int,

    /*
     * The hint text is persisted for historical analytics.
     *
     * Future prompt changes will not alter the evidence from
     * a completed mission.
     */
    val hintText: String,

    val revealedAt: Long
)