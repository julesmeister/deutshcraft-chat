package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.AssistanceLevel
import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallenge
import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallengeOutcome
import com.julesmeister.deutschcraft.feature.besser.domain.ChallengeStatus
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionResult
import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import java.util.UUID
import javax.inject.Inject

/*
 * Runs contextual Besser selection and persists the resulting
 * challenge.
 *
 * NO_CHALLENGE remains silent and does not create a database
 * row.
 */
class GenerateAndPersistBesserChallengeUseCase @Inject constructor(
    private val generateContextualChallenge:
    GenerateContextualBesserChallengeUseCase,

    private val besserChallengeRepository:
    BesserChallengeRepository
) {

    suspend operator fun invoke(
        conversationId: String,
        messageId: String,
        learnerMessage: String,
        currentCefrLevel: String,
        targetCefrLevel: String,
        missionTitle: String? = null,
        missionObjective: String? = null,
        detectedCategories: Set<String> =
            emptySet(),
        recentlyChallengedCapabilityIds: Set<String> =
            emptySet(),
        createdAt: Long =
            System.currentTimeMillis()
    ): BesserChallenge? {
        require(
            conversationId.isNotBlank()
        ) {
            "Conversation ID cannot be blank."
        }

        require(
            messageId.isNotBlank()
        ) {
            "Message ID cannot be blank."
        }

        val cleanMessage =
            learnerMessage.trim()

        if (cleanMessage.isBlank()) {
            return null
        }

        /*
         * Never attach more than one active Besser challenge to
         * the same learner message.
         */
        if (
            besserChallengeRepository
                .countActiveForMessage(
                    messageId =
                        messageId
                ) > 0
        ) {
            return besserChallengeRepository
                .getActiveForMessages(
                    messageIds =
                        listOf(
                            messageId
                        )
                )
                .firstOrNull()
        }

        val selectionResult =
            generateContextualChallenge(
                learnerMessage =
                    cleanMessage,
                currentCefrLevel =
                    currentCefrLevel,
                targetCefrLevel =
                    targetCefrLevel,
                missionTitle =
                    missionTitle,
                missionObjective =
                    missionObjective,
                detectedCategories =
                    detectedCategories,
                recentlyChallengedCapabilityIds =
                    recentlyChallengedCapabilityIds
            )

        return when (selectionResult) {
            is BesserChallengeSelectionResult.NoChallenge -> {
                null
            }

            is BesserChallengeSelectionResult.Selected -> {
                val challenge =
                    selectionResult.toPersistedChallenge(
                        conversationId =
                            conversationId,
                        messageId =
                            messageId,
                        originalMessageContent =
                            cleanMessage,
                        createdAt =
                            createdAt
                    )

                besserChallengeRepository.save(
                    challenge =
                        challenge
                )

                challenge
            }
        }
    }
}

private fun BesserChallengeSelectionResult.Selected
        .toPersistedChallenge(
    conversationId: String,
    messageId: String,
    originalMessageContent: String,
    createdAt: Long
): BesserChallenge {
    val cleanHints =
        hints
            .asSequence()
            .map(
                String::trim
            )
            .filter(
                String::isNotBlank
            )
            .distinct()
            .take(
                MAXIMUM_PERSISTED_HINT_COUNT
            )
            .toList()

    return BesserChallenge(
        id =
            UUID
                .randomUUID()
                .toString(),

        messageId =
            messageId,

        conversationId =
            conversationId,

        capabilityId =
            capability.capabilityId,

        /*
         * The current selector represents contextual growth.
         *
         * CORRECTION_NEEDED and MISSION_GAP can be introduced
         * when the selector result explicitly exposes them.
         */
        outcome =
            BesserChallengeOutcome.GROWTH_OPPORTUNITY,

        instruction =
            instruction.trim(),

        /*
         * BesserChallenge currently stores a reason code rather
         * than a separate rationale field.
         *
         * Preserve the contextual rationale here so the
         * observer can restore it into the UI model.
         */
        reasonCode =
            rationale
                .trim()
                .takeIf(
                    String::isNotBlank
                )
                ?: DEFAULT_REASON_CODE,

        confidence =
            confidence.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            ),

        /*
         * The challenge itself is a prompt. Additional revealed
         * hints increase the actual assistance later.
         */
        recommendedAssistanceLevel =
            AssistanceLevel.PROMPTED,

        actualAssistanceLevel =
            null,

        status =
            ChallengeStatus.AVAILABLE,

        hints =
            cleanHints,

        revealedHintCount =
            0,

        originalMessageContent =
            originalMessageContent,

        refinedMessageContent =
            null,

        createdAt =
            createdAt,

        acceptedAt =
            null,

        skippedAt =
            null,

        completedAt =
            null,

        updatedAt =
            createdAt
    )
}

private const val MAXIMUM_PERSISTED_HINT_COUNT =
    3

private const val DEFAULT_REASON_CODE =
    "CONTEXTUAL_GROWTH_OPPORTUNITY"