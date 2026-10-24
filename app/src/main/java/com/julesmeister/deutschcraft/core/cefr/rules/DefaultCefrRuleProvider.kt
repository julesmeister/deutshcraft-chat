package com.julesmeister.deutschcraft.core.cefr.rules

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevelDefinition
import com.julesmeister.deutschcraft.core.cefr.model.ConversationGuidelines
import com.julesmeister.deutschcraft.core.cefr.model.ConversationTopic
import com.julesmeister.deutschcraft.core.cefr.model.CorrectionFrequency
import com.julesmeister.deutschcraft.core.cefr.model.ExplanationStyle
import com.julesmeister.deutschcraft.core.cefr.model.GrammarFeature
import com.julesmeister.deutschcraft.core.cefr.model.ProgressionRequirements
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultCefrRuleProvider @Inject constructor() :
    CefrRuleProvider {

    private val definitions:
            Map<CefrLevel, CefrLevelDefinition> =
        listOf(
            createA1Definition(),
            createA2Definition(),
            createB1Definition(),
            createB2Definition(),
            createC1Definition(),
            createC2Definition()
        ).associateBy { definition ->
            definition.level
        }

    override fun getDefinition(
        level: CefrLevel
    ): CefrLevelDefinition {
        return requireNotNull(definitions[level]) {
            "No CEFR definition exists for $level"
        }
    }

    override fun getAllDefinitions():
            List<CefrLevelDefinition> {
        return CefrLevel.entries.map(::getDefinition)
    }

    private fun createA1Definition():
            CefrLevelDefinition {
        return CefrLevelDefinition(
            level = CefrLevel.A1,
            summary =
                "Can participate in very simple exchanges " +
                        "using familiar words and basic phrases.",
            conversationGuidelines = ConversationGuidelines(
                preferredSentenceLength = 3..10,
                preferredResponseSentenceCount = 1..2,
                assistantVocabularyLevel = CefrLevel.A1,
                correctionFrequency =
                    CorrectionFrequency.FREQUENT,
                allowIdioms = false,
                allowAbstractTopics = false,
                explanationStyle =
                    ExplanationStyle.VERY_SIMPLE
            ),
            permittedTopics = setOf(
                ConversationTopic.GREETINGS,
                ConversationTopic.PERSONAL_INFORMATION,
                ConversationTopic.FAMILY,
                ConversationTopic.DAILY_ROUTINE,
                ConversationTopic.FOOD_AND_DRINK,
                ConversationTopic.WEATHER,
                ConversationTopic.HOBBIES
            ),
            targetGrammar = setOf(
                GrammarFeature.PRESENT_TENSE,
                GrammarFeature.BASIC_WORD_ORDER,
                GrammarFeature.YES_NO_QUESTIONS,
                GrammarFeature.W_QUESTIONS,
                GrammarFeature.PERSONAL_PRONOUNS,
                GrammarFeature.NOMINATIVE_CASE,
                GrammarFeature.DEFINITE_ARTICLES,
                GrammarFeature.INDEFINITE_ARTICLES
            ),
            expectedCapabilities = listOf(
                "Introduce oneself using simple phrases",
                "Answer basic personal questions",
                "Describe simple daily activities",
                "Express basic likes and dislikes",
                "Ask short and familiar questions"
            ),
            progressionRequirements =
                ProgressionRequirements(
                    minimumOverallScore = 75,
                    minimumRequiredSkillScore = 65,
                    minimumAssessedMessages = 80,
                    minimumActiveDays = 7,
                    maximumRecentCriticalErrorRate = 0.25,
                    requiredConsecutiveQualifiedAssessments = 3
                )
        )
    }

    private fun createA2Definition():
            CefrLevelDefinition {
        return CefrLevelDefinition(
            level = CefrLevel.A2,
            summary =
                "Can communicate in routine situations " +
                        "and describe familiar everyday matters.",
            conversationGuidelines = ConversationGuidelines(
                preferredSentenceLength = 5..15,
                preferredResponseSentenceCount = 1..3,
                assistantVocabularyLevel = CefrLevel.A2,
                correctionFrequency =
                    CorrectionFrequency.FREQUENT,
                allowIdioms = false,
                allowAbstractTopics = false,
                explanationStyle =
                    ExplanationStyle.SIMPLE
            ),
            permittedTopics = setOf(
                ConversationTopic.GREETINGS,
                ConversationTopic.PERSONAL_INFORMATION,
                ConversationTopic.FAMILY,
                ConversationTopic.DAILY_ROUTINE,
                ConversationTopic.FOOD_AND_DRINK,
                ConversationTopic.SHOPPING,
                ConversationTopic.TRAVEL,
                ConversationTopic.WEATHER,
                ConversationTopic.HOBBIES,
                ConversationTopic.HEALTH,
                ConversationTopic.WORK
            ),
            targetGrammar = setOf(
                GrammarFeature.ACCUSATIVE_CASE,
                GrammarFeature.DATIVE_CASE,
                GrammarFeature.POSSESSIVE_ARTICLES,
                GrammarFeature.MODAL_VERBS,
                GrammarFeature.SEPARABLE_VERBS,
                GrammarFeature.PERFECT_TENSE,
                GrammarFeature.IMPERATIVE,
                GrammarFeature.COMPARATIVE
            ),
            expectedCapabilities = listOf(
                "Handle routine social exchanges",
                "Describe past activities simply",
                "Discuss familiar work and leisure topics",
                "Make simple requests and suggestions",
                "Explain immediate needs"
            ),
            progressionRequirements =
                ProgressionRequirements(
                    minimumOverallScore = 77,
                    minimumRequiredSkillScore = 67,
                    minimumAssessedMessages = 120,
                    minimumActiveDays = 14,
                    maximumRecentCriticalErrorRate = 0.22,
                    requiredConsecutiveQualifiedAssessments = 3
                )
        )
    }

    private fun createB1Definition():
            CefrLevelDefinition {
        return CefrLevelDefinition(
            level = CefrLevel.B1,
            summary =
                "Can maintain conversations about familiar " +
                        "topics and express connected opinions.",
            conversationGuidelines = ConversationGuidelines(
                preferredSentenceLength = 8..22,
                preferredResponseSentenceCount = 2..4,
                assistantVocabularyLevel = CefrLevel.B1,
                correctionFrequency =
                    CorrectionFrequency.MODERATE,
                allowIdioms = false,
                allowAbstractTopics = true,
                explanationStyle =
                    ExplanationStyle.STANDARD
            ),
            permittedTopics = setOf(
                ConversationTopic.DAILY_ROUTINE,
                ConversationTopic.TRAVEL,
                ConversationTopic.HEALTH,
                ConversationTopic.EDUCATION,
                ConversationTopic.WORK,
                ConversationTopic.TECHNOLOGY,
                ConversationTopic.CULTURE,
                ConversationTopic.NEWS
            ),
            targetGrammar = setOf(
                GrammarFeature.SIMPLE_PAST_COMMON_VERBS,
                GrammarFeature.COORDINATING_CONJUNCTIONS,
                GrammarFeature.SUBORDINATE_CLAUSES,
                GrammarFeature.RELATIVE_CLAUSES,
                GrammarFeature.COMPARATIVE,
                GrammarFeature.SUPERLATIVE
            ),
            expectedCapabilities = listOf(
                "Maintain connected conversations",
                "Explain plans, experiences, and opinions",
                "Give reasons for preferences",
                "Handle common travel complications",
                "Understand the main point of clear messages"
            ),
            progressionRequirements =
                ProgressionRequirements(
                    minimumOverallScore = 79,
                    minimumRequiredSkillScore = 69,
                    minimumAssessedMessages = 180,
                    minimumActiveDays = 21,
                    maximumRecentCriticalErrorRate = 0.18,
                    requiredConsecutiveQualifiedAssessments = 4
                )
        )
    }

    private fun createB2Definition():
            CefrLevelDefinition {
        return CefrLevelDefinition(
            level = CefrLevel.B2,
            summary =
                "Can interact with substantial fluency and " +
                        "explain viewpoints on concrete and abstract topics.",
            conversationGuidelines = ConversationGuidelines(
                preferredSentenceLength = 10..28,
                preferredResponseSentenceCount = 2..5,
                assistantVocabularyLevel = CefrLevel.B2,
                correctionFrequency =
                    CorrectionFrequency.MODERATE,
                allowIdioms = true,
                allowAbstractTopics = true,
                explanationStyle =
                    ExplanationStyle.DETAILED
            ),
            permittedTopics = setOf(
                ConversationTopic.EDUCATION,
                ConversationTopic.WORK,
                ConversationTopic.TECHNOLOGY,
                ConversationTopic.CULTURE,
                ConversationTopic.NEWS,
                ConversationTopic.ABSTRACT_IDEAS,
                ConversationTopic.PROFESSIONAL_DISCUSSION
            ),
            targetGrammar = setOf(
                GrammarFeature.ADJECTIVE_ENDINGS,
                GrammarFeature.PASSIVE_VOICE,
                GrammarFeature.KONJUNKTIV_TWO,
                GrammarFeature.COMPLEX_CONNECTORS,
                GrammarFeature.RELATIVE_CLAUSES
            ),
            expectedCapabilities = listOf(
                "Defend a viewpoint with supporting reasons",
                "Participate comfortably in detailed discussions",
                "Explain advantages and disadvantages",
                "Adjust wording when misunderstood",
                "Produce clear and detailed connected text"
            ),
            progressionRequirements =
                ProgressionRequirements(
                    minimumOverallScore = 82,
                    minimumRequiredSkillScore = 72,
                    minimumAssessedMessages = 250,
                    minimumActiveDays = 30,
                    maximumRecentCriticalErrorRate = 0.14,
                    requiredConsecutiveQualifiedAssessments = 4
                )
        )
    }

    private fun createC1Definition():
            CefrLevelDefinition {
        return CefrLevelDefinition(
            level = CefrLevel.C1,
            summary =
                "Can communicate flexibly, precisely, and " +
                        "effectively about complex subjects.",
            conversationGuidelines = ConversationGuidelines(
                preferredSentenceLength = 12..35,
                preferredResponseSentenceCount = 3..6,
                assistantVocabularyLevel = CefrLevel.C1,
                correctionFrequency =
                    CorrectionFrequency.LIGHT,
                allowIdioms = true,
                allowAbstractTopics = true,
                explanationStyle =
                    ExplanationStyle.ADVANCED
            ),
            permittedTopics = setOf(
                ConversationTopic.TECHNOLOGY,
                ConversationTopic.CULTURE,
                ConversationTopic.NEWS,
                ConversationTopic.ABSTRACT_IDEAS,
                ConversationTopic.PROFESSIONAL_DISCUSSION,
                ConversationTopic.ACADEMIC_DISCUSSION
            ),
            targetGrammar = setOf(
                GrammarFeature.COMPLEX_CONNECTORS,
                GrammarFeature.NOMINALIZATION,
                GrammarFeature.ADVANCED_REGISTER_CONTROL,
                GrammarFeature.PASSIVE_VOICE,
                GrammarFeature.KONJUNKTIV_TWO
            ),
            expectedCapabilities = listOf(
                "Express complex ideas precisely",
                "Adapt register to social and professional contexts",
                "Organize extended and coherent arguments",
                "Use language flexibly for different purposes",
                "Recognize and communicate subtle distinctions"
            ),
            progressionRequirements =
                ProgressionRequirements(
                    minimumOverallScore = 85,
                    minimumRequiredSkillScore = 76,
                    minimumAssessedMessages = 350,
                    minimumActiveDays = 45,
                    maximumRecentCriticalErrorRate = 0.10,
                    requiredConsecutiveQualifiedAssessments = 5
                )
        )
    }

    private fun createC2Definition():
            CefrLevelDefinition {
        return CefrLevelDefinition(
            level = CefrLevel.C2,
            summary =
                "Can communicate with precision, flexibility, " +
                        "and nuanced control in highly complex contexts.",
            conversationGuidelines = ConversationGuidelines(
                preferredSentenceLength = 12..45,
                preferredResponseSentenceCount = 3..8,
                assistantVocabularyLevel = CefrLevel.C2,
                correctionFrequency =
                    CorrectionFrequency.ON_REQUEST,
                allowIdioms = true,
                allowAbstractTopics = true,
                explanationStyle =
                    ExplanationStyle.ADVANCED
            ),
            permittedTopics =
                ConversationTopic.entries.toSet(),
            targetGrammar = setOf(
                GrammarFeature.ADVANCED_REGISTER_CONTROL,
                GrammarFeature.IDIOMATIC_GRAMMAR,
                GrammarFeature.NOMINALIZATION,
                GrammarFeature.COMPLEX_CONNECTORS
            ),
            expectedCapabilities = listOf(
                "Express subtle shades of meaning precisely",
                "Reformulate ideas naturally and effectively",
                "Control idiomatic and colloquial expression",
                "Participate in highly complex discussions",
                "Adapt style and register with consistent precision"
            ),
            progressionRequirements =
                ProgressionRequirements(
                    minimumOverallScore = 90,
                    minimumRequiredSkillScore = 82,
                    minimumAssessedMessages = 500,
                    minimumActiveDays = 60,
                    maximumRecentCriticalErrorRate = 0.06,
                    requiredConsecutiveQualifiedAssessments = 6
                )
        )
    }
}