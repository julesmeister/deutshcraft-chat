package com.julesmeister.deutschcraft.feature.besser.application

import com.julesmeister.deutschcraft.feature.besser.domain.session.BesserChallengeSessionState
import com.julesmeister.deutschcraft.feature.besser.domain.session.BesserChallengeSessionStatus
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeUiModel
import javax.inject.Inject

/*
 * Creates the pure domain session used by the refinement state
 * machine.
 *
 * OFFERED no longer exists.
 *
 * A newly displayed challenge begins in AVAILABLE state.
 */
class BesserChallengeSessionFactory @Inject constructor() {

    fun create(
        challenge: BesserChallengeUiModel,
        originalMessageText: String
    ): BesserChallengeSessionState {
        require(
            challenge.id.isNotBlank()
        ) {
            "Besser challenge ID must not be blank."
        }

        require(
            challenge.messageId.isNotBlank()
        ) {
            "Besser challenge message ID must not be blank."
        }

        require(
            challenge.capabilityId.isNotBlank()
        ) {
            "Besser challenge capability ID must not be blank."
        }

        require(
            originalMessageText.isNotBlank()
        ) {
            "Original learner message must not be blank."
        }

        return BesserChallengeSessionState(
            challengeId =
                challenge.id,
            messageId =
                challenge.messageId,
            capabilityId =
                challenge.capabilityId,
            originalText =
                originalMessageText.trim(),
            hints =
                challenge.hints
                    .asSequence()
                    .map(
                        String::trim
                    )
                    .filter(
                        String::isNotBlank
                    )
                    .distinct()
                    .toList(),
            revealedHintCount =
                challenge.revealedHintCount.coerceIn(
                    minimumValue =
                        0,
                    maximumValue =
                        challenge.hints.size
                ),
            refinementText =
                "",
            submittedText =
                null,
            feedback =
                null,
            evaluationScore =
                null,
            status =
                BesserChallengeSessionStatus.AVAILABLE
        )
    }

    fun createAndStore(
        challenge: BesserChallengeUiModel,
        originalMessageText: String,
        store: BesserChallengeSessionStore
    ): BesserChallengeSessionState {
        val session =
            create(
                challenge =
                    challenge,
                originalMessageText =
                    originalMessageText
            )

        store.setSession(
            session =
                session
        )

        return session
    }
}