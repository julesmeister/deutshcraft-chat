// app/src/main/java/com/julesmeister/deutschcraft/feature/sync/domain/SyncChange.kt

package com.julesmeister.deutschcraft.feature.sync.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SyncChangeRequest(
    val entityKey: String,
    val entityType: String,
    val entityId: String,
    val clientUpdatedAt: Long,
    val deleted: Boolean = false,
    val payload: JsonObject =
        JsonObject(
            emptyMap()
        )
)

@Serializable
data class SyncUploadRequest(
    val changes:
    List<SyncChangeRequest>
)

@Serializable
data class SyncUploadResponse(
    val written: Int,
    val cursor: Long
)

@Serializable
data class SyncChangeResponse(
    val entityKey: String,
    val entityType: String,
    val entityId: String,
    val payload: JsonObject =
        JsonObject(
            emptyMap()
        ),
    val clientUpdatedAt: Long,
    val serverUpdatedAt: Long,
    val deleted: Boolean = false,
    val schemaVersion: Int = 1
)

@Serializable
data class SyncDownloadResponse(
    val changes:
    List<SyncChangeResponse>,
    val cursor: Long
)

object SyncEntityType {

    const val CONVERSATION =
        "CONVERSATION"

    const val MESSAGE =
        "MESSAGE"

    const val CONVERSATION_MISSION =
        "CONVERSATION_MISSION"
}