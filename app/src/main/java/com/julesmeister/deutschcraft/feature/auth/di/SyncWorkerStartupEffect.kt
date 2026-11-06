// app/src/main/java/com/julesmeister/deutschcraft/feature/auth/di/SyncWorkerStartupEffect.kt

package com.julesmeister.deutschcraft.feature.auth.di

import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import com.julesmeister.deutschcraft.feature.auth.domain.AuthSession
import com.julesmeister.deutschcraft.feature.sync.scheduling.SyncScheduler
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Singleton
class SyncWorkerStartupEffect
@Inject constructor(
    private val authRepository:
    AuthRepository,

    private val syncScheduler:
    SyncScheduler
) {

    private val scope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.Main.immediate
        )

    @Volatile
    private var started =
        false

    fun start() {
        if (started) {
            return
        }

        synchronized(this) {
            if (started) {
                return
            }

            started =
                true

            observeAuthentication()
        }
    }

    private fun observeAuthentication() {
        scope.launch {
            authRepository
                .session
                .map { session ->
                    when (session) {
                        is AuthSession.SignedIn -> {
                            session.userId
                        }

                        AuthSession.Guest,
                        AuthSession.Loading -> {
                            null
                        }
                    }
                }
                .distinctUntilChanged()
                .collect { userId ->
                    if (userId == null) {
                        syncScheduler.cancelAllSync()
                    } else {
                        /*
                         * Run one synchronization immediately
                         * after this account becomes active.
                         */
                        syncScheduler
                            .requestImmediateSync()

                        /*
                         * Also register the background worker.
                         *
                         * SyncScheduler applies a 15-minute
                         * initial delay, so this worker does not
                         * duplicate the immediate sign-in sync.
                         */
                        syncScheduler
                            .schedulePeriodicSync()
                    }
                }
        }
    }
}