// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/usecase/GenerateAndPersistBesserChallengeUseCase.kt

package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.AssistanceLevel
import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallenge
import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallengeOutcome
import com.julesmeister.deutschcraft.feature.besser.domain.ChallengeStatus
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionResult
import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import java.util.UUID
import javax.inject.Inject

class GenerateAndPersistBesserChallengeUseCase
@Inject constructor(
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
        missionTitle: String? =
            null,
        missionObjective: String? =
            null,
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

        if (
            besserChallengeRepository
                .countActiveForMessage(
                    messageId =
                        messageId
                ) >
            0
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

        val normalizedRecentCapabilityIds =
            recentlyChallengedCapabilityIds
                .asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .toSet()

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
                    normalizedRecentCapabilityIds
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

        outcome =
            BesserChallengeOutcome.GROWTH_OPPORTUNITY,

        instruction =
            instruction.trim(),

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

        evaluationScore =
            null,

        evaluationFeedback =
            null,

        evaluationEvidence =
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