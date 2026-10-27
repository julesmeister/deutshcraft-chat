package com.julesmeister.deutschcraft.feature.phrasemarking.domain

enum class MarkedPhraseType(
    val label: String,
    val icon: String
) {
    DIFFICULT_WORD(
        label = "Schwieriges Wort",
        icon = "📖"
    ),
    GRAMMAR(
        label = "Grammatik verstehen",
        icon = "🧩"
    )
}

enum class ExplanationStatus {
    PENDING,
    READY,
    FAILED
}

data class MarkedPhrase(
    val id: String,
    val conversationId: String,
    val messageId: String,
    val phrase: String,
    val sourceSentence: String,
    val type: MarkedPhraseType,
    val meaning: String?,
    val explanation: String?,
    val example: String?,
    val status: ExplanationStatus,
    val createdAt: Long
)

data class PhraseExplanation(
    val meaning: String,
    val explanation: String,
    val example: String
)