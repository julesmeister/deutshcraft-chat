package com.julesmeister.deutschcraft.core.database.entity.learning

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * A phrase the learner highlighted in a message and marked
 * as difficult or worth understanding.
 *
 * The same phrase can only be marked once per message.
 */
@Entity(
    tableName = "marked_phrases",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["messageId"]),
        Index(value = ["messageId", "phrase"], unique = true)
    ]
)
data class MarkedPhraseEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val messageId: String,
    val phrase: String,
    val sourceSentence: String,
    val markType: String,
    val meaning: String?,
    val explanation: String?,
    val example: String?,
    val explanationStatus: String,
    val createdAt: Long,
    val updatedAt: Long
)