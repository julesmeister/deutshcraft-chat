package com.julesmeister.deutschcraft.feature.besser.application

import com.julesmeister.deutschcraft.feature.besser.domain.session.BesserChallengeSessionAction
import com.julesmeister.deutschcraft.feature.besser.domain.session.BesserChallengeSessionReducer
import com.julesmeister.deutschcraft.feature.besser.domain.session.BesserChallengeSessionState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/*
 * In-memory owner of the currently active contextual Besser
 * Challenge session.
 *
 * The current vertical slice supports one active refinement
 * session at a time because the main composer can edit only one
 * learner message at a time.
 *
 * null means that no challenge session is currently active.
 */
@Singleton
class BesserChallengeSessionStore @Inject constructor() {

    private val mutableState =
        MutableStateFlow<BesserChallengeSessionState?>(
            null
        )

    val state: StateFlow<BesserChallengeSessionState?> =
        mutableState.asStateFlow()

    /*
     * Installs a newly generated challenge session.
     *
     * If another session was already active, the new session
     * becomes the current one.
     */
    fun setSession(
        session: BesserChallengeSessionState
    ) {
        mutableState.value =
            session
    }

    /*
     * Applies an action only when a session exists.
     */
    fun dispatch(
        action: BesserChallengeSessionAction
    ) {
        mutableState.update { currentState ->
            currentState?.let { session ->
                BesserChallengeSessionReducer.reduce(
                    state =
                        session,
                    action =
                        action
                )
            }
        }
    }

    /*
     * Clears the active session entirely.
     *
     * Clear is a store operation, not a reducer action, because
     * the reducer always transforms one valid session into
     * another valid session.
     */
    fun clear() {
        mutableState.value =
            null
    }

    fun currentSession():
            BesserChallengeSessionState? {
        return mutableState.value
    }

    fun sessionForMessage(
        messageId: String
    ): BesserChallengeSessionState? {
        return mutableState.value
            ?.takeIf { session ->
                session.messageId ==
                        messageId
            }
    }

    fun hasSessionForMessage(
        messageId: String
    ): Boolean {
        return mutableState.value
            ?.messageId ==
                messageId
    }
}