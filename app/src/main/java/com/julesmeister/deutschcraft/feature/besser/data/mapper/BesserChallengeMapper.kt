package com.julesmeister.deutschcraft.feature.besser.data.mapper

import com.julesmeister.deutschcraft.core.database.entity.besser.BesserChallengeEntity
import com.julesmeister.deutschcraft.core.database.entity.besser.BesserChallengeHintEntity
import com.julesmeister.deutschcraft.core.database.model.BesserChallengeWithHints
import com.julesmeister.deutschcraft.feature.besser.domain.AssistanceLevel
import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallenge
import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallengeOutcome
import com.julesmeister.deutschcraft.feature.besser.domain.ChallengeStatus

fun BesserChallengeWithHints.toDomain():
        BesserChallenge {
    return challenge.toDomain(
        hints =
            orderedHints.map { hint ->
                hint.content
            }
    )
}

fun BesserChallengeEntity.toDomain(
    hints: List<String>
): BesserChallenge {
    return BesserChallenge(
        id =
            id,
        messageId =
            messageId,
        conversationId =
            conversationId,
        capabilityId =
            capabilityId,
        outcome =
            BesserChallengeOutcome.fromStorage(
                value =
                    outcome
            ),
        instruction =
            instruction,
        reasonCode =
            reasonCode,
        confidence =
            confidence.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            ),
        recommendedAssistanceLevel =
            AssistanceLevel.fromStorage(
                value =
                    recommendedAssistanceLevel
            ) ?: AssistanceLevel.PROMPTED,
        actualAssistanceLevel =
            AssistanceLevel.fromStorage(
                value =
                    actualAssistanceLevel
            ),
        status =
            ChallengeStatus.fromStorage(
                value =
                    status
            ),
        hints =
            hints,
        revealedHintCount =
            revealedHintCount.coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    hints.size
            ),
        originalMessageContent =
            originalMessageContent,
        refinedMessageContent =
            refinedMessageContent,
        createdAt =
            createdAt,
        acceptedAt =
            acceptedAt,
        skippedAt =
            skippedAt,
        completedAt =
            completedAt,
        updatedAt =
            updatedAt
    )
}

fun BesserChallenge.toEntity():
        BesserChallengeEntity {
    return BesserChallengeEntity(
        id =
            id,
        messageId =
            messageId,
        conversationId =
            conversationId,
        capabilityId =
            capabilityId,
        outcome =
            outcome.name,
        instruction =
            instruction,
        reasonCode =
            reasonCode,
        confidence =
            confidence.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            ),
        recommendedAssistanceLevel =
            recommendedAssistanceLevel.name,
        actualAssistanceLevel =
            actualAssistanceLevel?.name,
        status =
            status.name,
        revealedHintCount =
            revealedHintCount.coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    hints.size
            ),
        originalMessageContent =
            originalMessageContent,
        refinedMessageContent =
            refinedMessageContent,
        createdAt =
            createdAt,
        acceptedAt =
            acceptedAt,
        skippedAt =
            skippedAt,
        completedAt =
            completedAt,
        updatedAt =
            updatedAt
    )
}

fun BesserChallenge.toHintEntities():
        List<BesserChallengeHintEntity> {
    return hints.mapIndexed { index, hint ->
        BesserChallengeHintEntity(
            id =
                "$id-hint-${index + 1}",
            challengeId =
                id,
            position =
                index,
            content =
                hint
        )
    }
}