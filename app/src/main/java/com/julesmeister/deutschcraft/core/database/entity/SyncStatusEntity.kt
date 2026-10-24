package com.julesmeister.deutschcraft.core.database.entity

enum class SyncStatusEntity {
    LOCAL_ONLY,
    PENDING_UPLOAD,
    SYNCED,
    PENDING_DELETE
}