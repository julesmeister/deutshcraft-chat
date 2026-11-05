package com.julesmeister.deutschcraft.feature.sync.domain

data class SyncState(
    val isEnabled: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: Long = 0L,
    val nextCursor: Long = 0L,
    val pendingChanges: Int = 0
)
