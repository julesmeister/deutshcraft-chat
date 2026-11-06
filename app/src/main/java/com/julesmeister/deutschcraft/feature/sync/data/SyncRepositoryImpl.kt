package com.julesmeister.deutschcraft.feature.sync.data

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.dao.MissionDao
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.ConversationTitleSource
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageSenderEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.ConversationMissionEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveProgressEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionTemplateEntity
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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import retrofit2.HttpException

@Singleton
class SyncRepositoryImpl
@Inject constructor(
    private val database: DeutschCraftDatabase,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val missionDao: MissionDao,
    private val syncApi: SyncApi,
    private val syncPreferences: SyncPreferences,
    private val authRepository: AuthRepository,
    private val clock: Clock
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
            cancellation: CancellationException
        ) {
            throw cancellation
        } catch (
            throwable: Throwable
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
                                        batch.map { record ->
                                            record.change
                                        }
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
                                    messageDao.markMessageSynced(
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

                                is PendingUploadRecord.ConversationMission -> {
                                    missionDao
                                        .markConversationMissionSynced(
                                            conversationMissionId =
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

                    syncPreferences.updateLastSyncTimestamp(
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
            cancellation: CancellationException
        ) {
            throw cancellation
        } catch (
            throwable: Throwable
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
        signedInSession()
            ?.let { session ->
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
                        entity =
                            conversation,
                        change =
                            conversation.toSyncChange()
                    )
                }

        val messages =
            messageDao
                .getPendingSyncMessages()
                .map { message ->
                    PendingUploadRecord.Message(
                        entity =
                            message,
                        change =
                            message.toSyncChange()
                    )
                }

        val missions =
            missionDao
                .getPendingSyncConversationMissions()
                .map { mission ->
                    PendingUploadRecord.ConversationMission(
                        entity =
                            mission,
                        change =
                            mission.toSyncChange()
                    )
                }

        return (
                conversations +
                        messages +
                        missions
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

                        is PendingUploadRecord.ConversationMission ->
                            2
                    }
                }.thenBy { record ->
                    record.change.entityKey
                }
            )
    }

    private suspend fun ConversationMissionEntity.toSyncChange():
            SyncChangeRequest {
        val canonicalMissionId =
            id

        val isTombstone =
            isDeleted ||
                    syncStatus ==
                    SyncStatusEntity.PENDING_DELETE

        val template =
            missionDao.getMissionTemplate(
                missionTemplateId =
                    missionTemplateId
            )

        if (
            !isTombstone &&
            template == null
        ) {
            throw IllegalStateException(
                "Mission template does not exist: " +
                        missionTemplateId
            )
        }

        val objectives =
            if (template == null) {
                emptyList()
            } else {
                missionDao.getMissionObjectives(
                    missionTemplateId =
                        missionTemplateId
                )
            }

        val objectiveProgress =
            missionDao.getObjectiveProgressEntities(
                conversationMissionId =
                    canonicalMissionId
            )

        return SyncChangeRequest(
            entityKey =
                "CONVERSATION_MISSION#" +
                        "$conversationId#" +
                        canonicalMissionId,

            entityType =
                SyncEntityType.CONVERSATION_MISSION,

            entityId =
                canonicalMissionId,

            clientUpdatedAt =
                updatedAt,

            deleted =
                isTombstone,

            payload =
                JsonObject(
                    mapOf(
                        "conversationId" to
                                JsonPrimitive(
                                    conversationId
                                ),

                        "missionTemplateId" to
                                JsonPrimitive(
                                    missionTemplateId
                                ),

                        "rootMissionTemplateId" to
                                rootMissionTemplateId
                                    .toJsonElement(),

                        "sourceRecommendationId" to
                                sourceRecommendationId
                                    .toJsonElement(),

                        "targetCapabilityIdsJson" to
                                JsonPrimitive(
                                    targetCapabilityIdsJson
                                ),

                        "adaptiveFocusKeysJson" to
                                JsonPrimitive(
                                    adaptiveFocusKeysJson
                                ),

                        "status" to
                                JsonPrimitive(
                                    status
                                ),

                        "startedAt" to
                                startedAt.toJsonElement(),

                        "completedAt" to
                                completedAt.toJsonElement(),

                        "dismissedAt" to
                                dismissedAt.toJsonElement(),

                        "createdAt" to
                                JsonPrimitive(
                                    createdAt
                                ),

                        "deletedAt" to
                                deletedAt.toJsonElement(),

                        "template" to
                                template.toSyncJsonElement(),

                        "objectives" to
                                JsonArray(
                                    objectives.map { objective ->
                                        objective.toSyncJsonObject()
                                    }
                                ),

                        "objectiveProgress" to
                                JsonArray(
                                    objectiveProgress.map { progress ->
                                        progress.toSyncJsonObject(
                                            canonicalMissionId =
                                                canonicalMissionId
                                        )
                                    }
                                )
                    )
                )
        )
    }

    private fun MissionTemplateEntity?.toSyncJsonElement():
            JsonElement {
        if (this == null) {
            return JsonNull
        }

        return JsonObject(
            mapOf(
                "id" to
                        JsonPrimitive(
                            id
                        ),

                "title" to
                        JsonPrimitive(
                            title
                        ),

                "description" to
                        JsonPrimitive(
                            description
                        ),

                "scenario" to
                        JsonPrimitive(
                            scenario
                        ),

                "learnerRole" to
                        JsonPrimitive(
                            learnerRole
                        ),

                "assistantRole" to
                        JsonPrimitive(
                            assistantRole
                        ),

                "difficulty" to
                        JsonPrimitive(
                            difficulty
                        ),

                "icon" to
                        JsonPrimitive(
                            icon
                        ),

                "isEnabled" to
                        JsonPrimitive(
                            isEnabled
                        ),

                "createdAt" to
                        JsonPrimitive(
                            createdAt
                        ),

                "updatedAt" to
                        JsonPrimitive(
                            updatedAt
                        )
            )
        )
    }

    private fun MissionObjectiveEntity.toSyncJsonObject():
            JsonObject {
        return JsonObject(
            mapOf(
                "id" to
                        JsonPrimitive(
                            id
                        ),

                "missionTemplateId" to
                        JsonPrimitive(
                            missionTemplateId
                        ),

                "title" to
                        JsonPrimitive(
                            title
                        ),

                "description" to
                        JsonPrimitive(
                            description
                        ),

                "sequenceNumber" to
                        JsonPrimitive(
                            sequenceNumber
                        ),

                "isRequired" to
                        JsonPrimitive(
                            isRequired
                        ),

                "completionHint" to
                        JsonPrimitive(
                            completionHint
                        ),

                "coachingHintsJson" to
                        JsonPrimitive(
                            coachingHintsJson
                        ),

                "targetVocabularyJson" to
                        JsonPrimitive(
                            targetVocabularyJson
                        ),

                "examplePhrasesJson" to
                        JsonPrimitive(
                            examplePhrasesJson
                        )
            )
        )
    }

    private fun MissionObjectiveProgressEntity.toSyncJsonObject(
        canonicalMissionId: String
    ): JsonObject {
        return JsonObject(
            mapOf(
                "conversationMissionId" to
                        JsonPrimitive(
                            canonicalMissionId
                        ),

                "objectiveId" to
                        JsonPrimitive(
                            objectiveId
                        ),

                "isCompleted" to
                        JsonPrimitive(
                            isCompleted
                        ),

                "completedAt" to
                        completedAt.toJsonElement(),

                "completedByMessageId" to
                        completedByMessageId
                            .toJsonElement(),

                "confidence" to
                        JsonPrimitive(
                            confidence.coerceIn(
                                minimumValue =
                                    0f,
                                maximumValue =
                                    1f
                            )
                        ),

                "updatedAt" to
                        JsonPrimitive(
                            updatedAt
                        )
            )
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
        response: SyncDownloadResponse
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

                        SyncEntityType.CONVERSATION_MISSION ->
                            2

                        else ->
                            3
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

                        SyncEntityType.CONVERSATION_MISSION -> {
                            applyConversationMissionChange(
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
        change: SyncChangeResponse
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
        change: SyncChangeResponse
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

    private suspend fun applyConversationMissionChange(
        change: SyncChangeResponse
    ): Boolean {
        val canonicalMissionId =
            change.entityId

        val local =
            missionDao
                .getConversationMissionEntityIncludingDeleted(
                    conversationMissionId =
                        canonicalMissionId
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

        if (
            parentConversation.isDeleted &&
            !change.deleted
        ) {
            return false
        }

        if (change.deleted) {
            if (local == null) {
                return false
            }

            missionDao.upsertConversationMission(
                conversationMission =
                    local.copy(
                        updatedAt =
                            change.clientUpdatedAt,

                        syncStatus =
                            SyncStatusEntity.SYNCED,

                        remoteVersion =
                            change.serverUpdatedAt,

                        lastSyncedAt =
                            clock.millis(),

                        isDeleted =
                            true,

                        deletedAt =
                            payload.long(
                                key = "deletedAt"
                            ) ?: change.clientUpdatedAt
                    )
            )

            return true
        }

        val templateJson =
            payload.objectValue(
                key = "template"
            ) ?: return false

        val template =
            templateJson.toMissionTemplateEntity()
                ?: return false

        val objectives =
            payload.objectArray(
                key = "objectives"
            ).mapNotNull { objectiveJson ->
                objectiveJson.toMissionObjectiveEntity()
            }

        val downloadedProgress =
            payload.objectArray(
                key = "objectiveProgress"
            ).mapNotNull { progressJson ->
                progressJson
                    .toMissionObjectiveProgressEntity()
            }

        val missionTemplateId =
            payload.string(
                key = "missionTemplateId"
            ) ?: template.id

        if (
            missionTemplateId !=
            template.id
        ) {
            return false
        }

        val validObjectives =
            objectives.filter { objective ->
                objective.missionTemplateId ==
                        missionTemplateId
            }

        if (validObjectives.isEmpty()) {
            return false
        }

        missionDao.upsertMissionTemplate(
            template =
                template
        )

        missionDao.upsertMissionObjectives(
            objectives =
                validObjectives
        )

        val remote =
            ConversationMissionEntity(
                id =
                    canonicalMissionId,

                conversationId =
                    conversationId,

                missionTemplateId =
                    missionTemplateId,

                rootMissionTemplateId =
                    payload.nullableString(
                        key =
                            "rootMissionTemplateId"
                    ),

                sourceRecommendationId =
                    payload.nullableString(
                        key =
                            "sourceRecommendationId"
                    ),

                targetCapabilityIdsJson =
                    payload.rawString(
                        key =
                            "targetCapabilityIdsJson"
                    ) ?: local?.targetCapabilityIdsJson
                    ?: EMPTY_JSON_ARRAY,

                adaptiveFocusKeysJson =
                    payload.rawString(
                        key =
                            "adaptiveFocusKeysJson"
                    ) ?: local?.adaptiveFocusKeysJson
                    ?: EMPTY_JSON_ARRAY,

                status =
                    payload.string(
                        key = "status"
                    ) ?: local?.status
                    ?: DEFAULT_MISSION_STATUS,

                startedAt =
                    payload.nullableLong(
                        key = "startedAt"
                    ),

                completedAt =
                    payload.nullableLong(
                        key = "completedAt"
                    ),

                dismissedAt =
                    payload.nullableLong(
                        key = "dismissedAt"
                    ),

                createdAt =
                    payload.long(
                        key = "createdAt"
                    ) ?: local?.createdAt
                    ?: change.clientUpdatedAt,

                updatedAt =
                    change.clientUpdatedAt,

                syncStatus =
                    SyncStatusEntity.SYNCED,

                remoteVersion =
                    change.serverUpdatedAt,

                lastSyncedAt =
                    clock.millis(),

                isDeleted =
                    false,

                deletedAt =
                    null
            )

        missionDao.upsertConversationMission(
            conversationMission =
                remote
        )

        missionDao.deleteObjectiveProgressForMission(
            conversationMissionId =
                canonicalMissionId
        )

        val validObjectiveIds =
            validObjectives
                .mapTo(
                    mutableSetOf()
                ) { objective ->
                    objective.id
                }

        val normalizedProgress =
            downloadedProgress
                .filter { progress ->
                    progress.objectiveId in
                            validObjectiveIds
                }
                .map { progress ->
                    progress.copy(
                        conversationMissionId =
                            canonicalMissionId
                    )
                }

        missionDao.upsertObjectiveProgressList(
            progress =
                normalizedProgress
        )

        return true
    }

    private fun JsonObject.toMissionTemplateEntity():
            MissionTemplateEntity? {
        return MissionTemplateEntity(
            id =
                string(
                    key = "id"
                ) ?: return null,

            title =
                rawString(
                    key = "title"
                ) ?: return null,

            description =
                rawString(
                    key = "description"
                ) ?: return null,

            scenario =
                rawString(
                    key = "scenario"
                ) ?: return null,

            learnerRole =
                rawString(
                    key = "learnerRole"
                ) ?: return null,

            assistantRole =
                rawString(
                    key = "assistantRole"
                ) ?: return null,

            difficulty =
                string(
                    key = "difficulty"
                ) ?: return null,

            icon =
                rawString(
                    key = "icon"
                ).orEmpty(),

            isEnabled =
                boolean(
                    key = "isEnabled"
                ) ?: true,

            createdAt =
                long(
                    key = "createdAt"
                ) ?: 0L,

            updatedAt =
                long(
                    key = "updatedAt"
                ) ?: 0L
        )
    }

    private fun JsonObject.toMissionObjectiveEntity():
            MissionObjectiveEntity? {
        return MissionObjectiveEntity(
            id =
                string(
                    key = "id"
                ) ?: return null,

            missionTemplateId =
                string(
                    key = "missionTemplateId"
                ) ?: return null,

            title =
                rawString(
                    key = "title"
                ) ?: return null,

            description =
                rawString(
                    key = "description"
                ) ?: return null,

            sequenceNumber =
                int(
                    key = "sequenceNumber"
                ) ?: return null,

            isRequired =
                boolean(
                    key = "isRequired"
                ) ?: true,

            completionHint =
                rawString(
                    key = "completionHint"
                ).orEmpty(),

            coachingHintsJson =
                rawString(
                    key = "coachingHintsJson"
                ) ?: EMPTY_JSON_ARRAY,

            targetVocabularyJson =
                rawString(
                    key = "targetVocabularyJson"
                ) ?: EMPTY_JSON_ARRAY,

            examplePhrasesJson =
                rawString(
                    key = "examplePhrasesJson"
                ) ?: EMPTY_JSON_ARRAY
        )
    }

    private fun JsonObject.toMissionObjectiveProgressEntity():
            MissionObjectiveProgressEntity? {
        return MissionObjectiveProgressEntity(
            conversationMissionId =
                string(
                    key = "conversationMissionId"
                ) ?: return null,

            objectiveId =
                string(
                    key = "objectiveId"
                ) ?: return null,

            isCompleted =
                boolean(
                    key = "isCompleted"
                ) ?: false,

            completedAt =
                nullableLong(
                    key = "completedAt"
                ),

            completedByMessageId =
                nullableString(
                    key = "completedByMessageId"
                ),

            confidence =
                float(
                    key = "confidence"
                )
                    ?.coerceIn(
                        minimumValue =
                            0f,
                        maximumValue =
                            1f
                    )
                    ?: 0f,

            updatedAt =
                long(
                    key = "updatedAt"
                ) ?: 0L
        )
    }

    private suspend fun refreshState(
        session: AuthSession.SignedIn
    ) {
        val pendingChanges =
            conversationDao
                .countPendingSyncConversations() +
                    messageDao
                        .countPendingSyncMessages() +
                    missionDao
                        .countPendingSyncConversationMissions()

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

    private fun ConversationMissionEntity?.isLocallyDirty():
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
        return rawString(
            key = key
        )
            ?.trim()
            ?.takeIf(
                String::isNotBlank
            )
    }

    private fun JsonObject.rawString(
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

    private fun JsonObject.nullableString(
        key: String
    ): String? {
        return rawString(
            key = key
        )
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

    private fun JsonObject.float(
        key: String
    ): Float? {
        return this[key]
            ?.jsonPrimitive
            ?.floatOrNull
    }

    private fun JsonObject.boolean(
        key: String
    ): Boolean? {
        return this[key]
            ?.jsonPrimitive
            ?.booleanOrNull
    }

    private fun JsonObject.objectValue(
        key: String
    ): JsonObject? {
        return this[key] as?
                JsonObject
    }

    private fun JsonObject.objectArray(
        key: String
    ): List<JsonObject> {
        return (
                this[key] as?
                        JsonArray
                )
            ?.mapNotNull { element ->
                element as?
                        JsonObject
            }
            .orEmpty()
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

    private fun String?.toJsonElement():
            JsonElement {
        return this?.let(
            ::JsonPrimitive
        ) ?: JsonNull
    }

    private fun Long?.toJsonElement():
            JsonElement {
        return this?.let(
            ::JsonPrimitive
        ) ?: JsonNull
    }

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

        data class ConversationMission(
            val entity:
            ConversationMissionEntity,

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

        const val DEFAULT_MISSION_STATUS =
            "OFFERED"

        const val EMPTY_JSON_ARRAY =
            "[]"

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