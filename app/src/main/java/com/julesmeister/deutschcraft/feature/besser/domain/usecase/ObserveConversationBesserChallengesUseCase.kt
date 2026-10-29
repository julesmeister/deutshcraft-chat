package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallenge
import com.julesmeister.deutschcraft.feature.besser.domain.ChallengeStatus
import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeUiModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveConversationBesserChallengesUseCase @Inject constructor(
    private val besserChallengeRepository:
    BesserChallengeRepository
) {

    operator fun invoke(
        conversationId: String
    ): Flow<Map<String, BesserChallengeUiModel>> {
        require(
            conversationId.isNotBlank()
        ) {
            "Conversation ID cannot be blank."
        }

        return besserChallengeRepository
            .observeActiveForConversation(
                conversationId =
                    conversationId
            )
            .map { challenges ->
                challenges
                    .sortedBy(
                        BesserChallenge::createdAt
                    )
                    .associateBy(
                        keySelector = { challenge ->
                            challenge.messageId
                        },
                        valueTransform = { challenge ->
                            challenge.toCurrentUiModel()
                        }
                    )
            }
    }
}

/*
 * ===========================================================
 * Domain -> presentation
 * ===========================================================
 *
 * BesserChallenge already contains List<String> hints.
 *
 * There is therefore no hint entity ordering or hint content
 * mapping here. Persistence is responsible for reconstructing
 * the ordered domain list before this point.
 */
private fun BesserChallenge.toCurrentUiModel():
        BesserChallengeUiModel {
    val safeRevealedHintCount =
        revealedHintCount.coerceIn(
            minimumValue =
                0,
            maximumValue =
                hints.size
        )

    return BesserChallengeUiModel(
        id =
            id,

        messageId =
            messageId,

        capabilityId =
            capabilityId,

        /*
         * The current persisted challenge stores capabilityId,
         * but does not contain the catalogue display name.
         *
         * The catalogue name can be resolved later when we join
         * CapabilityRepository into the presentation pipeline.
         *
         * For now this preserves real persisted information
         * instead of fabricating a name.
         */
        capabilityName =
            capabilityId,

        instruction =
            instruction,

        /*
         * reasonCode is the closest persisted equivalent to the
         * newer contextual rationale.
         *
         * This is preferable to throwing away the existing
         * persisted explanation completely.
         */
        rationale =
            reasonCode,

        /*
         * BesserChallenge already exposes List<String>.
         */
        hints =
            hints,

        revealedHintCount =
            safeRevealedHintCount,

        /*
         * Unlike the older mapper assumption, confidence is
         * already persisted directly on BesserChallenge.
         */
        confidence =
            confidence.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            ),

        /*
         * Refinement belongs primarily to the in-memory session
         * state.
         *
         * However, an accepted persisted challenge represents
         * the challenge currently being worked on, so restoring
         * the conversation can reflect that state until the
         * session store is reconstructed.
         */
        isRefinementActive =
            status ==
                    ChallengeStatus.ACCEPTED
    )
}