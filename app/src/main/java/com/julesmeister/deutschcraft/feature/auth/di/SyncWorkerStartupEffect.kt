package com.julesmeister.deutschcraft.feature.auth.di

import android.content.Context
import androidx.startup.Initializer
import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import com.julesmeister.deutschcraft.feature.auth.domain.AuthSession
import com.julesmeister.deutschcraft.feature.sync.worker.DeutschCraftSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Starts/stops periodic background sync based on auth state.
 *
 * When the user signs in, periodic sync begins.
 * When the user signs out, periodic sync is cancelled.
 * Guests don't sync.
 */
@Singleton
class SyncWorkerStartupEffect
@Inject constructor(
    @ApplicationContext
    private val context: Context,

    private val authRepository: AuthRepository
) {

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        scope.launch {
            authRepository.session
                .collect { session ->
                    when (session) {
                        is AuthSession.SignedIn -> {
                            DeutschCraftSyncWorker.schedulePeriodic(
                                context
                            )
                        }

                        is AuthSession.Guest,
                        AuthSession.Loading -> {
                            DeutschCraftSyncWorker.cancelPeriodic(
                                context
                            )
                        }
                    }
                }
        }
    }
}
