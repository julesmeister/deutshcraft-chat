package com.julesmeister.deutschcraft.feature.besser.presentation

import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallenge

fun BesserChallenge.toUiModel():
        BesserChallengeUiModel {
    return BesserChallengeUiModel(
        id = id,
        messageId = messageId,
        conversationId = conversationId,
        capabilityId = capabilityId,
        instruction = instruction,
        confidence =
            confidence.coerceIn(
                minimumValue = 0f,
                maximumValue = 1f
            ),
        recommendedAssistanceLevel =
            recommendedAssistanceLevel,
        status =
            status,
        hints =
            hints,
        revealedHintCount =
            revealedHintCount.coerceIn(
                minimumValue = 0,
                maximumValue = hints.size
            ),
        originalMessageContent =
            originalMessageContent
    )
}