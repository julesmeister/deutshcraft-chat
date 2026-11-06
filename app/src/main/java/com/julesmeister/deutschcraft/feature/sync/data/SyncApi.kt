// app/src/main/java/com/julesmeister/deutschcraft/feature/sync/data/SyncApi.kt

package com.julesmeister.deutschcraft.feature.sync.data

import com.julesmeister.deutschcraft.feature.sync.domain.SyncDownloadResponse
import com.julesmeister.deutschcraft.feature.sync.domain.SyncUploadRequest
import com.julesmeister.deutschcraft.feature.sync.domain.SyncUploadResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SyncApi {

    @GET("sync")
    suspend fun downloadChanges(
        @Query("since")
        sinceCursor: Long
    ): SyncDownloadResponse

    @POST("sync")
    suspend fun uploadChanges(
        @Body
        request: SyncUploadRequest
    ): SyncUploadResponse
}