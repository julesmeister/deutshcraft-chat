// app/src/main/java/com/julesmeister/deutschcraft/feature/sync/scheduling/SyncScheduler.kt

package com.julesmeister.deutschcraft.feature.sync.scheduling

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.julesmeister.deutschcraft.feature.sync.worker.DeutschCraftSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler
@Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val workManager:
            WorkManager
        get() =
            WorkManager.getInstance(
                context
            )

    /*
     * Requests synchronization as soon as network access is
     * available.
     *
     * KEEP is intentional. If several local mutations happen
     * close together, they collapse into one synchronization
     * worker instead of creating one worker per mutation.
     */
    fun requestImmediateSync() {
        val request =
            OneTimeWorkRequestBuilder<
                    DeutschCraftSyncWorker
                    >()
                .setConstraints(
                    syncConstraints()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    MINIMUM_BACKOFF_SECONDS,
                    TimeUnit.SECONDS
                )
                .addTag(
                    IMMEDIATE_SYNC_TAG
                )
                .build()

        workManager.enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    /*
     * Keeps synchronization alive in the background.
     *
     * The initial delay is important.
     *
     * Sign-in already requests an immediate synchronization.
     * Without this delay, a newly created periodic worker may
     * also run immediately and produce the duplicate GET
     * requests we observed in Logcat.
     */
    fun schedulePeriodicSync() {
        val request =
            PeriodicWorkRequestBuilder<
                    DeutschCraftSyncWorker
                    >(
                PERIODIC_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            )
                .setInitialDelay(
                    PERIODIC_INTERVAL_MINUTES,
                    TimeUnit.MINUTES
                )
                .setConstraints(
                    syncConstraints()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    MINIMUM_BACKOFF_SECONDS,
                    TimeUnit.SECONDS
                )
                .addTag(
                    PERIODIC_SYNC_TAG
                )
                .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /*
     * Called when the signed-in session disappears.
     *
     * This stops both pending immediate synchronization and
     * the user's periodic background synchronization.
     */
    fun cancelAllSync() {
        workManager.cancelUniqueWork(
            IMMEDIATE_WORK_NAME
        )

        workManager.cancelUniqueWork(
            PERIODIC_WORK_NAME
        )
    }

    private fun syncConstraints():
            Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(
                NetworkType.CONNECTED
            )
            .build()
    }

    private companion object {

        const val IMMEDIATE_WORK_NAME =
            "deutschcraft-immediate-sync"

        const val PERIODIC_WORK_NAME =
            "deutschcraft-periodic-sync"

        const val IMMEDIATE_SYNC_TAG =
            "deutschcraft-immediate-sync"

        const val PERIODIC_SYNC_TAG =
            "deutschcraft-periodic-sync"

        const val PERIODIC_INTERVAL_MINUTES =
            15L

        const val MINIMUM_BACKOFF_SECONDS =
            15L
    }
}