package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

/*
 * Lightweight conversation context supplied to the
 * adaptive-learning communication evaluator.
 *
 * Assistant messages provide conversational context, but
 * only learner messages may produce learning evidence.
 */
data class AdaptiveConversationTurn(
    val speaker: AdaptiveConversationSpeaker,
    val content: String
) {

    val sanitizedContent: String
        get() =
            content.trim()

    val isUsable: Boolean
        get() =
            sanitizedContent.isNotBlank()
}

enum class AdaptiveConversationSpeaker {
    LEARNER,
    ASSISTANT
}