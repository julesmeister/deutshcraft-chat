package com.julesmeister.deutschcraft.feature.sync.data

import android.content.Context
import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import com.julesmeister.deutschcraft.feature.auth.domain.AuthSession
import com.julesmeister.deutschcraft.feature.sync.domain.SyncChangeOperation
import com.julesmeister.deutschcraft.feature.sync.domain.SyncChangeRequest
import com.julesmeister.deutschcraft.feature.sync.domain.SyncChangeType
import com.julesmeister.deutschcraft.feature.sync.domain.SyncError
import com.julesmeister.deutschcraft.feature.sync.domain.SyncRepository
import com.julesmeister.deutschcraft.feature.sync.domain.SyncResult
import com.julesmeister.deutschcraft.feature.sync.domain.SyncState
import com.julesmeister.deutschcraft.feature.sync.domain.SyncUploadRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl
@Inject constructor(
    private val context: Context,
    private val syncApi: SyncApi,
    private val syncPreferences: SyncPreferences,
    private val authRepository: AuthRepository
) : SyncRepository {

    private val mutableSyncState =
        MutableStateFlow<SyncState>(
            SyncState(isEnabled = false)
        )

    override val syncState: StateFlow<SyncState> =
        mutableSyncState.asStateFlow()

    init {
        /*
         * Enable sync only when signed in.
         */
        combine(
            authRepository.session,
            syncPreferences.nextCursor,
            syncPreferences.lastSyncTimestamp
        ) { session, cursor, lastSync ->
            val isSignedIn =
                session is AuthSession.SignedIn

            if (isSignedIn) {
                SyncState(
                    isEnabled = true,
                    nextCursor = cursor,
                    lastSyncTimestamp = lastSync
                )
            } else {
                SyncState(isEnabled = false)
            }
        }.also { flow ->
            // Collect in a coroutine scope if needed, or use launchIn
        }
    }

    override suspend fun downloadChanges(): SyncResult<Int> {
        val session =
            authRepository.session.first()

        if (session !is AuthSession.SignedIn) {
            return SyncResult.Failure(SyncError.NotAuthenticated)
        }

        return try {
            val cursor =
                syncPreferences.nextCursor.first()

            val response =
                syncApi.downloadChanges(cursor)

            /*
             * Apply changes to Room in a transaction.
             */
            response.changes.forEach { change ->
                when (change.type) {
                    "CONVERSATION" -> {
                        // TODO: apply to conversations via repository
                    }

                    "MESSAGE" -> {
                        // TODO: apply to messages via repository
                    }
                }
            }

            /*
             * Update cursor for next sync.
             */
            syncPreferences.updateNextCursor(
                response.nextCursor
            )

            SyncResult.Success(response.changes.size)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            SyncResult.Failure(
                SyncError.Unknown(
                    throwable.message ?: "Download failed"
                )
            )
        }
    }

    override suspend fun uploadChanges(): SyncResult<Int> {
        val session =
            authRepository.session.first()

        if (session !is AuthSession.SignedIn) {
            return SyncResult.Failure(SyncError.NotAuthenticated)
        }

        return try {
            /*
             * Collect pending changes from Room.
             * For now, this is a placeholder.
             */
            val changes =
                emptyList<SyncChangeRequest>()

            if (changes.isEmpty()) {
                return SyncResult.Success(0)
            }

            syncApi.uploadChanges(
                SyncUploadRequest(changes)
            )

            /*
             * Mark changes as synced in Room.
             */
            syncPreferences.updateLastSyncTimestamp(
                System.currentTimeMillis()
            )

            SyncResult.Success(changes.size)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            SyncResult.Failure(
                SyncError.Unknown(
                    throwable.message ?: "Upload failed"
                )
            )
        }
    }

    override suspend fun syncNow(): SyncResult<Unit> {
        mutableSyncState.value =
            mutableSyncState.value.copy(
                isSyncing = true
            )

        return try {
            val downloadResult =
                downloadChanges()

            val uploadResult =
                uploadChanges()

            when {
                downloadResult is SyncResult.Failure ->
                    SyncResult.Failure(downloadResult.error)

                uploadResult is SyncResult.Failure ->
                    SyncResult.Failure(uploadResult.error)

                else ->
                    SyncResult.Success(Unit)
            }
        } finally {
            mutableSyncState.value =
                mutableSyncState.value.copy(
                    isSyncing = false
                )
        }
    }

    override suspend fun markMessageDirty(
        messageId: String
    ) {
        // TODO: mark message as LOCAL_ONLY in Room
    }

    override suspend fun clearSyncState() {
        syncPreferences.clearSyncState()

        mutableSyncState.value =
            SyncState(isEnabled = false)
    }
}
