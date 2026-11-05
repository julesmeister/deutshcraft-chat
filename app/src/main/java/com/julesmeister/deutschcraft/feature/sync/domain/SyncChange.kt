package com.julesmeister.deutschcraft.feature.sync.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

enum class SyncChangeType {
    CONVERSATION,
    MESSAGE
}

enum class SyncChangeOperation {
    UPSERT,
    DELETE
}

@Serializable
data class SyncChangeRequest(
    val type: String,
    val operation: String,
    val id: String,
    val conversationId: String? = null,
    val updatedAt: Long,
    val payload: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class SyncUploadRequest(
    val changes: List<SyncChangeRequest>
)

@Serializable
data class SyncChangeResponse(
    val type: String,
    val entityType: String,
    val entityKey: String,
    val entityId: String,
    val conversationId: String? = null,
    val updatedAt: Long,
    val serverUpdatedAt: Long,
    val deleted: Boolean = false,
    val payload: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class SyncDownloadResponse(
    val changes: List<SyncChangeResponse>,
    val nextCursor: Long
)
