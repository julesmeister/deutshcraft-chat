package com.julesmeister.deutschcraft.feature.sync.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.julesmeister.deutschcraft.feature.sync.domain.SyncRepository
import com.julesmeister.deutschcraft.feature.sync.domain.SyncResult
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

class DeutschCraftSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val result =
                syncRepository.syncNow()

            when (result) {
                is SyncResult.Success -> Result.success()
                is SyncResult.Failure -> {
                    Result.retry()
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Result.retry()
        }
    }

    companion object {

        private const val PERIODIC_SYNC_TAG =
            "deutschcraft-periodic-sync"

        private const val MIN_BACKOFF_MILLIS = 15000L

        fun schedulePeriodic(
            context: Context
        ) {
            val syncRequest =
                PeriodicWorkRequestBuilder<DeutschCraftSyncWorker>(
                    15,
                    TimeUnit.MINUTES,
                    MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                    .setConstraints(
                        Constraints
                            .Builder()
                            .setRequiredNetworkType(
                                NetworkType.CONNECTED
                            )
                            .build()
                    )
                    .addTag(PERIODIC_SYNC_TAG)
                    .build()

            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_SYNC_TAG,
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )
        }

        fun cancelPeriodic(
            context: Context
        ) {
            WorkManager
                .getInstance(context)
                .cancelAllWorkByTag(PERIODIC_SYNC_TAG)
        }

        fun syncNow(
            context: Context
        ) {
            val syncRequest =
                OneTimeWorkRequestBuilder<DeutschCraftSyncWorker>()
                    .setConstraints(
                        Constraints
                            .Builder()
                            .setRequiredNetworkType(
                                NetworkType.CONNECTED
                            )
                            .build()
                    )
                    .build()

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    "deutschcraft-sync-now",
                    androidx.work.ExistingWorkPolicy.KEEP,
                    syncRequest
                )
        }
    }
}
