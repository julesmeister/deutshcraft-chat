package com.julesmeister.deutschcraft.feature.sync.domain

sealed interface SyncResult<out T> {
    data class Success<T>(val value: T) : SyncResult<T>
    data class Failure(val error: SyncError) : SyncResult<Nothing>
}

sealed interface SyncError {
    data object NotAuthenticated : SyncError
    data object NetworkUnavailable : SyncError
    data class ServerError(val code: Int, val message: String) : SyncError
    data class Unknown(val message: String) : SyncError
}
