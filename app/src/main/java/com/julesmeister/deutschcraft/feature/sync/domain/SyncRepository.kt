package com.julesmeister.deutschcraft.feature.sync.domain

import kotlinx.coroutines.flow.StateFlow

interface SyncRepository {
    val syncState: StateFlow<SyncState>

    /**
     * Download changes from the cloud since the last cursor.
     * Applies them to Room in a transaction.
     * Only works for authenticated users.
     */
    suspend fun downloadChanges(): SyncResult<Int>

    /**
     * Upload pending local changes to the cloud.
     * Marks successfully synced messages as SYNCED in Room.
     * Only works for authenticated users.
     */
    suspend fun uploadChanges(): SyncResult<Int>

    /**
     * Trigger a complete sync cycle: download first, then upload.
     */
    suspend fun syncNow(): SyncResult<Unit>

    /**
     * Mark a message as needing sync.
     */
    suspend fun markMessageDirty(messageId: String)

    /**
     * Clear sync state (when signing out).
     */
    suspend fun clearSyncState()
}
