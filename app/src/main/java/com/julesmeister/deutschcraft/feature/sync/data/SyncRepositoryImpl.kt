// app/src/main/java/com/julesmeister/deutschcraft/feature/sync/data/SyncRepositoryImpl.kt

package com.julesmeister.deutschcraft.feature.sync.data

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.ConversationTitleSource
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageSenderEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import com.julesmeister.deutschcraft.feature.auth.domain.AuthSession
import com.julesmeister.deutschcraft.feature.sync.domain.SyncChangeRequest
import com.julesmeister.deutschcraft.feature.sync.domain.SyncChangeResponse
import com.julesmeister.deutschcraft.feature.sync.domain.SyncDownloadResponse
import com.julesmeister.deutschcraft.feature.sync.domain.SyncEntityType
import com.julesmeister.deutschcraft.feature.sync.domain.SyncError
import com.julesmeister.deutschcraft.feature.sync.domain.SyncRepository
import com.julesmeister.deutschcraft.feature.sync.domain.SyncResult
import com.julesmeister.deutschcraft.feature.sync.domain.SyncState
import com.julesmeister.deutschcraft.feature.sync.domain.SyncUploadRequest
import java.io.IOException
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import retrofit2.HttpException

@Singleton
class SyncRepositoryImpl
@Inject constructor(
    private val database:
    DeutschCraftDatabase,

    private val conversationDao:
    ConversationDao,

    private val messageDao:
    MessageDao,

    private val syncApi:
    SyncApi,

    private val syncPreferences:
    SyncPreferences,

    private val authRepository:
    AuthRepository,

    private val clock:
    Clock
) : SyncRepository {

    private val repositoryScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )

    private val syncMutex =
        Mutex()

    private val mutableSyncState =
        MutableStateFlow(
            SyncState(
                isEnabled = false
            )
        )

    override val syncState:
            StateFlow<SyncState> =
        mutableSyncState.asStateFlow()

    init {
        repositoryScope.launch {
            authRepository.session.collectLatest { session ->
                when (session) {
                    is AuthSession.SignedIn -> {
                        refreshState(
                            session = session
                        )
                    }

                    AuthSession.Guest,
                    AuthSession.Loading -> {
                        mutableSyncState.value =
                            SyncState(
                                isEnabled = false
                            )
                    }
                }
            }
        }
    }

    override suspend fun downloadChanges():
            SyncResult<Int> {
        val session =
            signedInSession()
                ?: return SyncResult.Failure(
                    SyncError.NotAuthenticated
                )

        return try {
            val cursor =
                syncPreferences.getNextCursor(
                    userId = session.userId
                )

            val response =
                syncApi.downloadChanges(
                    sinceCursor = cursor
                )

            val appliedCount =
                applyDownloadedChanges(
                    response = response
                )

            syncPreferences.updateNextCursor(
                userId = session.userId,
                cursor = response.cursor
            )

            refreshState(
                session = session
            )

            SyncResult.Success(
                appliedCount
            )
        } catch (
            cancellation:
            CancellationException
        ) {
            throw cancellation
        } catch (
            throwable:
            Throwable
        ) {
            SyncResult.Failure(
                throwable.toSyncError(
                    fallbackMessage =
                        "Sync download failed."
                )
            )
        }
    }

    override suspend fun uploadChanges():
            SyncResult<Int> {
        val session =
            signedInSession()
                ?: return SyncResult.Failure(
                    SyncError.NotAuthenticated
                )

        return try {
            val pendingRecords =
                loadPendingUploadRecords()

            if (pendingRecords.isEmpty()) {
                refreshState(
                    session = session
                )

                return SyncResult.Success(
                    0
                )
            }

            var uploadedCount =
                0

            var latestCursor =
                syncPreferences.getNextCursor(
                    userId = session.userId
                )

            pendingRecords
                .chunked(
                    MAXIMUM_CHANGES_PER_UPLOAD
                )
                .forEach { batch ->
                    ensureSessionStillMatches(
                        expectedUserId =
                            session.userId
                    )

                    val response =
                        syncApi.uploadChanges(
                            request =
                                SyncUploadRequest(
                                    changes =
                                        batch.map(
                                            PendingUploadRecord::change
                                        )
                                )
                        )

                    if (
                        response.written !=
                        batch.size
                    ) {
                        throw SyncBatchAcknowledgementException(
                            expected =
                                batch.size,
                            acknowledged =
                                response.written
                        )
                    }

                    val syncedAt =
                        clock.millis()

                    database.withTransaction {
                        batch.forEach { record ->
                            when (record) {
                                is PendingUploadRecord.Conversation -> {
                                    conversationDao
                                        .markConversationSynced(
                                            conversationId =
                                                record.entity.id,
                                            expectedUpdatedAt =
                                                record.entity.updatedAt,
                                            remoteVersion =
                                                response.cursor,
                                            lastSyncedAt =
                                                syncedAt
                                        )
                                }

                                is PendingUploadRecord.Message -> {
                                    messageDao
                                        .markMessageSynced(
                                            messageId =
                                                record.entity.id,
                                            expectedUpdatedAt =
                                                record.entity.updatedAt,
                                            remoteVersion =
                                                response.cursor,
                                            lastSyncedAt =
                                                syncedAt
                                        )
                                }
                            }
                        }
                    }

                    latestCursor =
                        maxOf(
                            latestCursor,
                            response.cursor
                        )

                    syncPreferences.updateNextCursor(
                        userId =
                            session.userId,
                        cursor =
                            latestCursor
                    )

                    syncPreferences
                        .updateLastSyncTimestamp(
                            userId =
                                session.userId,
                            timestamp =
                                syncedAt
                        )

                    uploadedCount +=
                        batch.size

                    refreshState(
                        session = session
                    )
                }

            SyncResult.Success(
                uploadedCount
            )
        } catch (
            cancellation:
            CancellationException
        ) {
            throw cancellation
        } catch (
            throwable:
            Throwable
        ) {
            SyncResult.Failure(
                throwable.toSyncError(
                    fallbackMessage =
                        "Sync upload failed."
                )
            )
        }
    }

    override suspend fun syncNow():
            SyncResult<Unit> {
        return syncMutex.withLock {
            val session =
                signedInSession()

            if (session == null) {
                return@withLock SyncResult.Failure(
                    SyncError.NotAuthenticated
                )
            }

            mutableSyncState.value =
                mutableSyncState.value.copy(
                    isEnabled = true,
                    isSyncing = true
                )

            try {
                when (
                    val uploadResult =
                        uploadChanges()
                ) {
                    is SyncResult.Failure -> {
                        SyncResult.Failure(
                            uploadResult.error
                        )
                    }

                    is SyncResult.Success -> {
                        when (
                            val downloadResult =
                                downloadChanges()
                        ) {
                            is SyncResult.Failure -> {
                                SyncResult.Failure(
                                    downloadResult.error
                                )
                            }

                            is SyncResult.Success -> {
                                syncPreferences
                                    .updateLastSyncTimestamp(
                                        userId =
                                            session.userId,
                                        timestamp =
                                            clock.millis()
                                    )

                                refreshState(
                                    session = session
                                )

                                SyncResult.Success(
                                    Unit
                                )
                            }
                        }
                    }
                }
            } finally {
                mutableSyncState.value =
                    mutableSyncState.value.copy(
                        isSyncing = false
                    )
            }
        }
    }

    override suspend fun markMessageDirty(
        messageId: String
    ) {
        messageDao.markMessagePendingUpload(
            messageId = messageId,
            updatedAt = clock.millis()
        )

        signedInSession()
            ?.let { session ->
                refreshState(
                    session = session
                )
            }
    }

    override suspend fun clearSyncState() {
        val session =
            signedInSession()

        if (session != null) {
            syncPreferences.clearSyncState(
                userId = session.userId
            )
        }

        mutableSyncState.value =
            SyncState(
                isEnabled = false
            )
    }

    private suspend fun loadPendingUploadRecords():
            List<PendingUploadRecord> {
        val conversations =
            conversationDao
                .getPendingSyncConversations()
                .map { conversation ->
                    PendingUploadRecord.Conversation(
                        entity = conversation,
                        change =
                            conversation.toSyncChange()
                    )
                }

        val messages =
            messageDao
                .getPendingSyncMessages()
                .map { message ->
                    PendingUploadRecord.Message(
                        entity = message,
                        change =
                            message.toSyncChange()
                    )
                }

        return (
                conversations +
                        messages
                )
            .sortedWith(
                compareBy<PendingUploadRecord> { record ->
                    record.change.clientUpdatedAt
                }.thenBy { record ->
                    when (record) {
                        is PendingUploadRecord.Conversation ->
                            0

                        is PendingUploadRecord.Message ->
                            1
                    }
                }.thenBy { record ->
                    record.change.entityKey
                }
            )
    }

    private fun ensureSessionStillMatches(
        expectedUserId: String
    ) {
        val currentUserId =
            signedInSession()
                ?.userId

        if (
            currentUserId !=
            expectedUserId
        ) {
            throw SyncSessionChangedException()
        }
    }

    private suspend fun applyDownloadedChanges(
        response:
        SyncDownloadResponse
    ): Int {
        var appliedCount =
            0

        val orderedChanges =
            response.changes.sortedWith(
                compareBy<SyncChangeResponse> { change ->
                    when (
                        change.entityType
                    ) {
                        SyncEntityType.CONVERSATION ->
                            0

                        SyncEntityType.MESSAGE ->
                            1

                        else ->
                            2
                    }
                }.thenBy { change ->
                    change.serverUpdatedAt
                }
            )

        database.withTransaction {
            orderedChanges.forEach { change ->
                val applied =
                    when (
                        change.entityType
                    ) {
                        SyncEntityType.CONVERSATION -> {
                            applyConversationChange(
                                change = change
                            )
                        }

                        SyncEntityType.MESSAGE -> {
                            applyMessageChange(
                                change = change
                            )
                        }

                        else -> {
                            false
                        }
                    }

                if (applied) {
                    appliedCount +=
                        1
                }
            }
        }

        return appliedCount
    }

    private suspend fun applyConversationChange(
        change:
        SyncChangeResponse
    ): Boolean {
        val local =
            conversationDao
                .getConversationIncludingDeleted(
                    conversationId =
                        change.entityId
                )

        if (local.isLocallyDirty()) {
            return false
        }

        if (
            local?.remoteVersion != null &&
            local.remoteVersion >=
            change.serverUpdatedAt
        ) {
            return false
        }

        if (
            local?.updatedAt != null &&
            local.updatedAt >
            change.clientUpdatedAt
        ) {
            return false
        }

        val payload =
            change.payload

        val remote =
            ConversationEntity(
                id =
                    change.entityId,

                title =
                    payload.string(
                        key = "title"
                    ) ?: local?.title
                    ?: DEFAULT_CONVERSATION_TITLE,

                titleSource =
                    payload.enumValueOrNull<
                            ConversationTitleSource
                            >(
                        key = "titleSource"
                    ) ?: local?.titleSource
                    ?: ConversationTitleSource.DEFAULT,

                cefrLevel =
                    payload.enumValueOrNull<
                            CefrLevel
                            >(
                        key = "cefrLevel"
                    ) ?: local?.cefrLevel
                    ?: CefrLevel.A1,

                localDate =
                    payload.string(
                        key = "localDate"
                    ) ?: local?.localDate
                    ?: DEFAULT_LOCAL_DATE,

                epochDay =
                    payload.long(
                        key = "epochDay"
                    ) ?: local?.epochDay
                    ?: 0L,

                createdAt =
                    payload.long(
                        key = "createdAt"
                    ) ?: local?.createdAt
                    ?: change.clientUpdatedAt,

                updatedAt =
                    change.clientUpdatedAt,

                closedAt =
                    payload.nullableLong(
                        key = "closedAt"
                    ),

                messageCount =
                    payload.int(
                        key = "messageCount"
                    ) ?: local?.messageCount
                    ?: 0,

                previewText =
                    payload.nullableString(
                        key = "previewText"
                    ),

                isClosed =
                    payload.boolean(
                        key = "isClosed"
                    ) ?: local?.isClosed
                    ?: false,

                isArchived =
                    payload.boolean(
                        key = "isArchived"
                    ) ?: local?.isArchived
                    ?: false,

                syncStatus =
                    SyncStatusEntity.SYNCED,

                remoteVersion =
                    change.serverUpdatedAt,

                lastSyncedAt =
                    clock.millis(),

                isDeleted =
                    change.deleted,

                deletedAt =
                    if (change.deleted) {
                        payload.long(
                            key = "deletedAt"
                        ) ?: change.clientUpdatedAt
                    } else {
                        null
                    }
            )

        conversationDao.upsertConversation(
            conversation = remote
        )

        return true
    }

    private suspend fun applyMessageChange(
        change:
        SyncChangeResponse
    ): Boolean {
        val local =
            messageDao
                .getMessageIncludingDeleted(
                    messageId =
                        change.entityId
                )

        if (local.isLocallyDirty()) {
            return false
        }

        if (
            local?.remoteVersion != null &&
            local.remoteVersion >=
            change.serverUpdatedAt
        ) {
            return false
        }

        if (
            local?.updatedAt != null &&
            local.updatedAt >
            change.clientUpdatedAt
        ) {
            return false
        }

        val payload =
            change.payload

        val conversationId =
            payload.string(
                key = "conversationId"
            ) ?: local?.conversationId
            ?: return false

        val parentConversation =
            conversationDao
                .getConversationIncludingDeleted(
                    conversationId =
                        conversationId
                )
                ?: return false

        if (parentConversation.isDeleted) {
            return false
        }

        val remote =
            MessageEntity(
                id =
                    change.entityId,

                conversationId =
                    conversationId,

                content =
                    payload.string(
                        key = "content"
                    ) ?: local?.content
                    ?: "",

                sender =
                    payload.enumValueOrNull<
                            MessageSenderEntity
                            >(
                        key = "sender"
                    ) ?: local?.sender
                    ?: MessageSenderEntity.ASSISTANT,

                sequenceNumber =
                    payload.long(
                        key = "sequenceNumber"
                    ) ?: local?.sequenceNumber
                    ?: return false,

                createdAt =
                    payload.long(
                        key = "createdAt"
                    ) ?: local?.createdAt
                    ?: change.clientUpdatedAt,

                correction =
                    payload.nullableString(
                        key = "correction"
                    ),

                syncStatus =
                    SyncStatusEntity.SYNCED,

                remoteVersion =
                    change.serverUpdatedAt,

                lastSyncedAt =
                    clock.millis(),

                updatedAt =
                    change.clientUpdatedAt,

                isDeleted =
                    change.deleted,

                deletedAt =
                    if (change.deleted) {
                        payload.long(
                            key = "deletedAt"
                        ) ?: change.clientUpdatedAt
                    } else {
                        null
                    }
            )

        messageDao.upsertMessage(
            message = remote
        )

        return true
    }

    private suspend fun refreshState(
        session:
        AuthSession.SignedIn
    ) {
        val pendingChanges =
            conversationDao
                .countPendingSyncConversations() +
                    messageDao
                        .countPendingSyncMessages()

        mutableSyncState.value =
            mutableSyncState.value.copy(
                isEnabled = true,

                lastSyncTimestamp =
                    syncPreferences
                        .getLastSyncTimestamp(
                            userId =
                                session.userId
                        ),

                nextCursor =
                    syncPreferences
                        .getNextCursor(
                            userId =
                                session.userId
                        ),

                pendingChanges =
                    pendingChanges
            )
    }

    private fun signedInSession():
            AuthSession.SignedIn? {
        return authRepository
            .session
            .value as?
                AuthSession.SignedIn
    }

    private fun ConversationEntity.toSyncChange():
            SyncChangeRequest {
        return SyncChangeRequest(
            entityKey =
                "CONVERSATION#$id",

            entityType =
                SyncEntityType.CONVERSATION,

            entityId =
                id,

            clientUpdatedAt =
                updatedAt,

            deleted =
                isDeleted ||
                        syncStatus ==
                        SyncStatusEntity.PENDING_DELETE,

            payload =
                JsonObject(
                    mapOf(
                        "title" to
                                JsonPrimitive(
                                    title
                                ),

                        "titleSource" to
                                JsonPrimitive(
                                    titleSource.name
                                ),

                        "cefrLevel" to
                                JsonPrimitive(
                                    cefrLevel.name
                                ),

                        "localDate" to
                                JsonPrimitive(
                                    localDate
                                ),

                        "epochDay" to
                                JsonPrimitive(
                                    epochDay
                                ),

                        "createdAt" to
                                JsonPrimitive(
                                    createdAt
                                ),

                        "closedAt" to
                                closedAt.toJsonElement(),

                        "messageCount" to
                                JsonPrimitive(
                                    messageCount
                                ),

                        "previewText" to
                                previewText.toJsonElement(),

                        "isClosed" to
                                JsonPrimitive(
                                    isClosed
                                ),

                        "isArchived" to
                                JsonPrimitive(
                                    isArchived
                                ),

                        "deletedAt" to
                                deletedAt.toJsonElement()
                    )
                )
        )
    }

    private fun MessageEntity.toSyncChange():
            SyncChangeRequest {
        return SyncChangeRequest(
            entityKey =
                "MESSAGE#$conversationId#" +
                        sequenceNumber
                            .toString()
                            .padStart(
                                SEQUENCE_KEY_WIDTH,
                                '0'
                            ),

            entityType =
                SyncEntityType.MESSAGE,

            entityId =
                id,

            clientUpdatedAt =
                updatedAt,

            deleted =
                isDeleted ||
                        syncStatus ==
                        SyncStatusEntity.PENDING_DELETE,

            payload =
                JsonObject(
                    mapOf(
                        "conversationId" to
                                JsonPrimitive(
                                    conversationId
                                ),

                        "content" to
                                JsonPrimitive(
                                    content
                                ),

                        "sender" to
                                JsonPrimitive(
                                    sender.name
                                ),

                        "sequenceNumber" to
                                JsonPrimitive(
                                    sequenceNumber
                                ),

                        "createdAt" to
                                JsonPrimitive(
                                    createdAt
                                ),

                        "correction" to
                                correction.toJsonElement(),

                        "deletedAt" to
                                deletedAt.toJsonElement()
                    )
                )
        )
    }

    private fun ConversationEntity?.isLocallyDirty():
            Boolean {
        return this != null &&
                syncStatus in
                LOCAL_DIRTY_STATUSES
    }

    private fun MessageEntity?.isLocallyDirty():
            Boolean {
        return this != null &&
                syncStatus in
                LOCAL_DIRTY_STATUSES
    }

    private fun Throwable.toSyncError(
        fallbackMessage: String
    ): SyncError {
        return when (this) {
            is SyncSessionChangedException -> {
                SyncError.NotAuthenticated
            }

            is IOException -> {
                SyncError.NetworkUnavailable
            }

            is HttpException -> {
                SyncError.ServerError(
                    code =
                        code(),

                    message =
                        message()
                            .ifBlank {
                                fallbackMessage
                            }
                )
            }

            is SyncBatchAcknowledgementException -> {
                SyncError.Unknown(
                    message
                        ?: fallbackMessage
                )
            }

            else -> {
                SyncError.Unknown(
                    message
                        ?: fallbackMessage
                )
            }
        }
    }

    private fun JsonObject.string(
        key: String
    ): String? {
        return this[key]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.trim()
            ?.takeIf(
                String::isNotBlank
            )
    }

    private fun JsonObject.nullableString(
        key: String
    ): String? {
        val element =
            this[key]
                ?: return null

        if (element is JsonNull) {
            return null
        }

        return element
            .jsonPrimitive
            .contentOrNull
    }

    private fun JsonObject.long(
        key: String
    ): Long? {
        return this[key]
            ?.jsonPrimitive
            ?.longOrNull
    }

    private fun JsonObject.nullableLong(
        key: String
    ): Long? {
        val element =
            this[key]
                ?: return null

        if (element is JsonNull) {
            return null
        }

        return element
            .jsonPrimitive
            .longOrNull
    }

    private fun JsonObject.int(
        key: String
    ): Int? {
        return this[key]
            ?.jsonPrimitive
            ?.intOrNull
    }

    private fun JsonObject.boolean(
        key: String
    ): Boolean? {
        return this[key]
            ?.jsonPrimitive
            ?.booleanOrNull
    }

    private inline fun <reified T : Enum<T>>
            JsonObject.enumValueOrNull(
        key: String
    ): T? {
        val storedValue =
            string(
                key = key
            ) ?: return null

        return enumValues<T>()
            .firstOrNull { value ->
                value.name ==
                        storedValue
                            .trim()
                            .uppercase()
            }
    }

    private fun String?.toJsonElement() =
        this?.let(
            ::JsonPrimitive
        ) ?: JsonNull

    private fun Long?.toJsonElement() =
        this?.let(
            ::JsonPrimitive
        ) ?: JsonNull

    private sealed interface PendingUploadRecord {

        val change:
                SyncChangeRequest

        data class Conversation(
            val entity:
            ConversationEntity,

            override val change:
            SyncChangeRequest
        ) : PendingUploadRecord

        data class Message(
            val entity:
            MessageEntity,

            override val change:
            SyncChangeRequest
        ) : PendingUploadRecord
    }

    private class SyncBatchAcknowledgementException(
        expected: Int,
        acknowledged: Int
    ) : IllegalStateException(
        "The server acknowledged " +
                "$acknowledged of $expected " +
                "sync changes."
    )

    private class SyncSessionChangedException :
        IllegalStateException(
            "The signed-in account changed during synchronization."
        )

    private companion object {

        const val MAXIMUM_CHANGES_PER_UPLOAD =
            100

        const val DEFAULT_CONVERSATION_TITLE =
            "Neue Unterhaltung"

        const val DEFAULT_LOCAL_DATE =
            "1970-01-01"

        const val SEQUENCE_KEY_WIDTH =
            12

        val LOCAL_DIRTY_STATUSES =
            setOf(
                SyncStatusEntity.LOCAL_ONLY,
                SyncStatusEntity.PENDING_UPLOAD,
                SyncStatusEntity.PENDING_DELETE
            )
    }
}