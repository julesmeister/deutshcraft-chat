// app/src/main/java/com/julesmeister/deutschcraft/feature/sync/worker/DeutschCraftSyncWorker.kt

package com.julesmeister.deutschcraft.feature.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.julesmeister.deutschcraft.feature.sync.domain.SyncError
import com.julesmeister.deutschcraft.feature.sync.domain.SyncRepository
import com.julesmeister.deutschcraft.feature.sync.domain.SyncResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

@HiltWorker
class DeutschCraftSyncWorker
@AssistedInject constructor(
    @Assisted
    context: Context,

    @Assisted
    workerParameters:
    WorkerParameters,

    private val syncRepository:
    SyncRepository
) : CoroutineWorker(
    context,
    workerParameters
) {

    override suspend fun doWork():
            Result {
        return try {
            when (
                val syncResult =
                    syncRepository.syncNow()
            ) {
                is SyncResult.Success -> {
                    Result.success()
                }

                is SyncResult.Failure -> {
                    syncResult.error
                        .toWorkerResult()
                }
            }
        } catch (
            cancellation:
            CancellationException
        ) {
            throw cancellation
        } catch (
            throwable:
            Throwable
        ) {
            Result.retry()
        }
    }

    private fun SyncError.toWorkerResult():
            Result {
        return when (this) {
            SyncError.NotAuthenticated -> {
                Result.success()
            }

            SyncError.NetworkUnavailable -> {
                Result.retry()
            }

            is SyncError.ServerError -> {
                if (
                    code in
                    RETRYABLE_HTTP_STATUS_CODES
                ) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }

            is SyncError.Unknown -> {
                Result.retry()
            }
        }
    }

    private companion object {

        val RETRYABLE_HTTP_STATUS_CODES =
            setOf(
                408,
                425,
                429,
                500,
                502,
                503,
                504
            )
    }
}