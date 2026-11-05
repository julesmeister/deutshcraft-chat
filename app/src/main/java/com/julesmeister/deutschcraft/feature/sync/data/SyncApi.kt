package com.julesmeister.deutschcraft.feature.sync.data

import com.julesmeister.deutschcraft.feature.sync.domain.SyncDownloadResponse
import com.julesmeister.deutschcraft.feature.sync.domain.SyncUploadRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SyncApi {

    /**
     * Download all changes since the provided cursor.
     *
     * Requires JWT authentication.
     */
    @GET("v1/sync")
    suspend fun downloadChanges(
        @Query("since") sinceCursor: Long
    ): SyncDownloadResponse

    /**
     * Upload local changes.
     *
     * Requires JWT authentication.
     * Lambda extracts userId from JWT sub.
     */
    @POST("v1/sync")
    suspend fun uploadChanges(
        @Body request: SyncUploadRequest
    )
}
