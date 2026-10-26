package com.julesmeister.deutschcraft.core.database.entity.adaptivelearning

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName =
        "adaptive_learning_focuses",
    primaryKeys = [
        "id"
    ],
    indices = [
        /*
         * A focusKey represents one stable learning pattern.
         *
         * Examples:
         *
         * grammar:dative_articles
         * vocabulary:bestellen
         * communication:giving_reasons
         */
        Index(
            value = [
                "focusKey"
            ],
            unique =
                true
        ),
        Index(
            value = [
                "type"
            ]
        ),
        Index(
            value = [
                "masteryState"
            ]
        ),
        Index(
            value = [
                "targetCefrLevel"
            ]
        ),
        Index(
            value = [
                "lastObservedAt"
            ]
        )
    ]
)
data class AdaptiveLearningFocusEntity(
    val id: String,

    /*
     * Stable normalized identifier used to group equivalent
     * learning evidence.
     */
    val focusKey: String,

    /*
     * Stored LearningFocusType enum name:
     *
     * GRAMMAR
     * VOCABULARY
     * COMMUNICATION
     */
    val type: String,

    val title: String,

    val description: String,

    /*
     * Stored CefrLevel enum name.
     */
    val targetCefrLevel: String,

    /*
     * Stored CommunicationSkill enum name.
     *
     * Grammar and vocabulary focuses normally leave this
     * value null.
     */
    val communicationSkill: String?,

    /*
     * Stored MasteryState enum name.
     */
    val masteryState: String,

    val evidenceCount: Int,

    val struggleCount: Int,

    val successCount: Int,

    val consecutiveSuccessCount: Int,

    /*
     * Value from 0 to 1 representing confidence that this is
     * a real repeated learner pattern.
     */
    val confidence: Float,

    /*
     * Value from 0 to 1 representing current mastery.
     */
    val masteryScore: Float,

    /*
     * Internal instruction used by Emil and other AI
     * gateways to reinforce this focus naturally.
     */
    val reinforcementInstruction: String,

    /*
     * Short learner-facing guidance suitable for subtle
     * feedback in the chat top bar.
     */
    val learnerGuidance: String,

    val firstObservedAt: Long,

    val lastObservedAt: Long,

    val lastPracticedAt: Long?,

    val masteredAt: Long?,

    val createdAt: Long,

    val updatedAt: Long
)