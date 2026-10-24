package com.julesmeister.deutschcraft.core.cefr.model

data class AssessmentEvidence(
    val id: String,
    val messageId: String,
    val conversationId: String,
    val assessedLevel: CefrLevel,
    val grammarScore: Int,
    val vocabularyScore: Int,
    val coherenceScore: Int,
    val accuracyScore: Int,
    val rangeScore: Int,
    val detectedGrammarFeatures:
    Set<GrammarFeature>,
    val mistakes: List<DetectedMistake>,
    val analyzedAt: Long,
    val source: AssessmentSource,
    val ruleVersion: Int
)

data class DetectedMistake(
    val category: MistakeCategory,
    val grammarFeature: GrammarFeature?,
    val originalText: String?,
    val correctedText: String?,
    val severity: MistakeSeverity
)

enum class MistakeCategory {
    ARTICLE,
    CASE,
    GENDER,
    WORD_ORDER,
    VERB_CONJUGATION,
    TENSE,
    PREPOSITION,
    VOCABULARY,
    SPELLING,
    REGISTER,
    COHERENCE
}

enum class MistakeSeverity {
    MINOR,
    MODERATE,
    CRITICAL
}

enum class AssessmentSource {
    LOCAL_RULE_ENGINE,
    REMOTE_AI,
    HYBRID
}