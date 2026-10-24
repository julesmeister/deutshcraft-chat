package com.julesmeister.deutschcraft.core.cefr.model

data class CefrLevelDefinition(
    val level: CefrLevel,
    val summary: String,
    val conversationGuidelines: ConversationGuidelines,
    val permittedTopics: Set<ConversationTopic>,
    val targetGrammar: Set<GrammarFeature>,
    val expectedCapabilities: List<String>,
    val progressionRequirements: ProgressionRequirements
)

data class ConversationGuidelines(
    val preferredSentenceLength: IntRange,
    val preferredResponseSentenceCount: IntRange,
    val assistantVocabularyLevel: CefrLevel,
    val correctionFrequency: CorrectionFrequency,
    val allowIdioms: Boolean,
    val allowAbstractTopics: Boolean,
    val explanationStyle: ExplanationStyle
)

data class ProgressionRequirements(
    val minimumOverallScore: Int,
    val minimumRequiredSkillScore: Int,
    val minimumAssessedMessages: Int,
    val minimumActiveDays: Int,
    val maximumRecentCriticalErrorRate: Double,
    val requiredConsecutiveQualifiedAssessments: Int
)

enum class CorrectionFrequency {
    FREQUENT,
    MODERATE,
    LIGHT,
    ON_REQUEST
}

enum class ExplanationStyle {
    VERY_SIMPLE,
    SIMPLE,
    STANDARD,
    DETAILED,
    ADVANCED
}