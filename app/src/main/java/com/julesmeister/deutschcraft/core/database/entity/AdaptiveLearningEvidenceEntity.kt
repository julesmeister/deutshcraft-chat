package com.julesmeister.deutschcraft.core.database.entity.adaptivelearning

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName =
        "adaptive_learning_evidence",
    primaryKeys = [
        "id"
    ],
    foreignKeys = [
        ForeignKey(
            entity =
                AdaptiveLearningFocusEntity::class,
            parentColumns = [
                "id"
            ],
            childColumns = [
                "focusId"
            ],
            onDelete =
                ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [
                "focusId"
            ]
        ),
        Index(
            value = [
                "messageId"
            ]
        ),
        Index(
            value = [
                "focusId",
                "messageId"
            ],
            unique =
                true
        ),
        Index(
            value = [
                "result"
            ]
        ),
        Index(
            value = [
                "observedAt"
            ]
        )
    ]
)
data class AdaptiveLearningEvidenceEntity(
    val id: String,

    val focusId: String,

    /*
     * The learner message that produced this evidence.
     */
    val messageId: String,

    /*
     * The conversation where the evidence was detected.
     *
     * This remains nullable so adaptive-learning history can
     * survive independently from a conversation.
     */
    val conversationId: String?,

    /*
     * Stored LearningEvidenceResult enum name:
     *
     * STRUGGLED
     * PARTIAL_SUCCESS
     * SUCCESS
     * NOT_OBSERVED
     */
    val result: String,

    /*
     * Evaluator confidence from 0 to 1.
     */
    val confidence: Float,

    /*
     * Internal explanation of why this evidence was
     * classified with the selected result.
     */
    val reason: String,

    /*
     * Snapshot of the learner's original message.
     */
    val learnerText: String,

    /*
     * Corrected version of the learner's message.
     *
     * Example:
     *
     * Ich gehe zur Schule.
     */
    val correctedText: String?,

    /*
     * A natural version demonstrating how the same thought
     * could be expressed at the learner's current CEFR level.
     *
     * This is different from correctedText.
     *
     * correctedText fixes an error.
     *
     * levelAppropriateText also reinforces communication
     * behaviors expected at the current level.
     *
     * For a B1 learner, it might add a reason or connect
     * related ideas without inventing personal facts.
     */
    val levelAppropriateText: String?,

    val observedAt: Long,

    val createdAt: Long
)