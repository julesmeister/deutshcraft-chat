package com.julesmeister.deutschcraft.feature.adaptivelearning.data.mapper

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.cefr.model.GrammarFeature
import com.julesmeister.deutschcraft.core.cefr.model.MistakeCategory
import com.julesmeister.deutschcraft.core.cefr.model.MistakeSeverity
import com.julesmeister.deutschcraft.core.database.entity.DetectedMistakeEntity
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningObservation
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.LearningEvidenceResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.LearningFocusType
import javax.inject.Inject

class DetectedMistakeAdaptiveLearningMapper
@Inject constructor() {

    fun map(
        mistake: DetectedMistakeEntity,
        targetCefrLevel: CefrLevel
    ): AdaptiveLearningObservation? {
        val learnerText =
            mistake
                .originalText
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return null

        val focusType =
            determineFocusType(
                mistake =
                    mistake
            )

        val focusKey =
            createFocusKey(
                mistake =
                    mistake,
                focusType =
                    focusType
            )

        if (focusKey.isBlank()) {
            return null
        }

        val correctedText =
            mistake
                .correctedText
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        val explanation =
            mistake
                .explanation
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        return AdaptiveLearningObservation(
            focusKey =
                focusKey,
            type =
                focusType,
            title =
                createTitle(
                    mistake =
                        mistake
                ),
            description =
                createDescription(
                    mistake =
                        mistake,
                    explanation =
                        explanation
                ),
            targetCefrLevel =
                targetCefrLevel,
            communicationSkill =
                null,
            result =
                determineEvidenceResult(
                    mistake =
                        mistake
                ),
            confidence =
                calculateConfidence(
                    mistake =
                        mistake
                ),
            reason =
                explanation
                    ?: createFallbackReason(
                        mistake =
                            mistake
                    ),
            reinforcementInstruction =
                createReinforcementInstruction(
                    mistake =
                        mistake
                ),
            learnerGuidance =
                createLearnerGuidance(
                    mistake =
                        mistake
                ),
            learnerText =
                learnerText,
            correctedText =
                correctedText,

            /*
             * For now, use the corrected text as the safest
             * level-appropriate version.
             *
             * A later CEFR-aware Besser gateway can generate
             * a richer A2, B1, B2, C1, or C2 version without
             * inventing personal information.
             */
            levelAppropriateText =
                correctedText
        )
    }

    fun mapAll(
        mistakes: List<DetectedMistakeEntity>,
        targetCefrLevel: CefrLevel
    ): List<AdaptiveLearningObservation> {
        return mistakes
            .asSequence()
            .mapNotNull { mistake ->
                map(
                    mistake =
                        mistake,
                    targetCefrLevel =
                        targetCefrLevel
                )
            }
            /*
             * The same assessment may contain more than one
             * row for the same grammar or vocabulary focus.
             *
             * Keep the strongest observation for each focus.
             */
            .groupBy { observation ->
                observation.normalizedFocusKey
            }
            .mapNotNull { (_, observations) ->
                observations.maxByOrNull { observation ->
                    observation.normalizedConfidence
                }
            }
            .sortedByDescending { observation ->
                observation.normalizedConfidence
            }
    }

    private fun determineFocusType(
        mistake: DetectedMistakeEntity
    ): LearningFocusType {
        return when (mistake.category) {
            MistakeCategory.VOCABULARY -> {
                LearningFocusType.VOCABULARY
            }

            else -> {
                LearningFocusType.GRAMMAR
            }
        }
    }

    private fun createFocusKey(
        mistake: DetectedMistakeEntity,
        focusType: LearningFocusType
    ): String {
        val keySuffix =
            when {
                mistake.grammarFeature != null -> {
                    mistake
                        .grammarFeature
                        .name
                        .lowercase()
                }

                else -> {
                    mistake
                        .category
                        .name
                        .lowercase()
                }
            }

        return "${focusType.name.lowercase()}:$keySuffix"
    }

    private fun createTitle(
        mistake: DetectedMistakeEntity
    ): String {
        val rawTitle =
            mistake
                .grammarFeature
                ?.name
                ?: mistake.category.name

        return rawTitle.toDisplayLabel()
    }

    private fun createDescription(
        mistake: DetectedMistakeEntity,
        explanation: String?
    ): String {
        if (explanation != null) {
            return explanation
        }

        return when (mistake.grammarFeature) {
            GrammarFeature.PRESENT_TENSE -> {
                "Correct and consistent use of German " +
                        "present-tense verb forms."
            }

            GrammarFeature.BASIC_WORD_ORDER -> {
                "Correct positioning of verbs and sentence " +
                        "elements in basic German statements."
            }

            GrammarFeature.YES_NO_QUESTIONS -> {
                "Correct word order when asking German " +
                        "yes-or-no questions."
            }

            GrammarFeature.W_QUESTIONS -> {
                "Correct formation of questions using words " +
                        "such as wer, was, wann, wo, and warum."
            }

            GrammarFeature.PERSONAL_PRONOUNS -> {
                "Correct selection and use of German personal " +
                        "pronouns."
            }

            GrammarFeature.NOMINATIVE_CASE -> {
                "Correct use of articles and nouns in the " +
                        "nominative case."
            }

            GrammarFeature.ACCUSATIVE_CASE -> {
                "Correct use of articles and nouns in the " +
                        "accusative case."
            }

            GrammarFeature.DATIVE_CASE -> {
                "Correct use of articles and nouns in the " +
                        "dative case."
            }

            GrammarFeature.DEFINITE_ARTICLES -> {
                "Correct use of der, die, das, and their " +
                        "case-dependent forms."
            }

            GrammarFeature.INDEFINITE_ARTICLES -> {
                "Correct use of ein, eine, and their " +
                        "case-dependent forms."
            }

            GrammarFeature.POSSESSIVE_ARTICLES -> {
                "Correct use of possessive articles such as " +
                        "mein, dein, sein, and ihr."
            }

            GrammarFeature.MODAL_VERBS -> {
                "Correct use of modal verbs and the associated " +
                        "infinitive structure."
            }

            GrammarFeature.SEPARABLE_VERBS -> {
                "Correct placement of prefixes when using " +
                        "separable German verbs."
            }

            GrammarFeature.PERFECT_TENSE -> {
                "Correct formation of the German perfect tense " +
                        "using haben or sein and a participle."
            }

            GrammarFeature.SIMPLE_PAST_COMMON_VERBS -> {
                "Correct use of common simple-past verb forms."
            }

            GrammarFeature.IMPERATIVE -> {
                "Correct formation of commands and requests."
            }

            GrammarFeature.COMPARATIVE -> {
                "Correct formation and use of comparative " +
                        "adjectives."
            }

            GrammarFeature.SUPERLATIVE -> {
                "Correct formation and use of superlative " +
                        "adjectives."
            }

            GrammarFeature.COORDINATING_CONJUNCTIONS -> {
                "Natural connection of ideas using coordinating " +
                        "conjunctions."
            }

            GrammarFeature.SUBORDINATE_CLAUSES -> {
                "Correct word order and verb placement in " +
                        "subordinate clauses."
            }

            GrammarFeature.RELATIVE_CLAUSES -> {
                "Correct use of relative pronouns and relative " +
                        "clause word order."
            }

            GrammarFeature.ADJECTIVE_ENDINGS -> {
                "Correct adjective endings based on article, " +
                        "case, number, and gender."
            }

            GrammarFeature.PASSIVE_VOICE -> {
                "Correct formation and use of the German " +
                        "passive voice."
            }

            GrammarFeature.KONJUNKTIV_TWO -> {
                "Correct use of Konjunktiv II for wishes, " +
                        "possibilities, and hypothetical situations."
            }

            GrammarFeature.COMPLEX_CONNECTORS -> {
                "Appropriate use of advanced connectors to " +
                        "organize and relate ideas."
            }

            GrammarFeature.NOMINALIZATION -> {
                "Natural conversion of verbs or adjectives into " +
                        "nouns where appropriate."
            }

            GrammarFeature.ADVANCED_REGISTER_CONTROL -> {
                "Selection of language appropriate to formal, " +
                        "neutral, or informal situations."
            }

            GrammarFeature.IDIOMATIC_GRAMMAR -> {
                "Natural use of advanced and idiomatic German " +
                        "grammar patterns."
            }

            null -> {
                "A recurring ${mistake.category.name.toDisplayLabel()} " +
                        "pattern detected in the learner's German."
            }
        }
    }

    private fun determineEvidenceResult(
        mistake: DetectedMistakeEntity
    ): LearningEvidenceResult {
        return if (mistake.isResolved) {
            LearningEvidenceResult.SUCCESS
        } else {
            LearningEvidenceResult.STRUGGLED
        }
    }

    private fun calculateConfidence(
        mistake: DetectedMistakeEntity
    ): Float {
        val occurrenceConfidence =
            when {
                mistake.occurrenceCount >=
                        TEN_OR_MORE_OCCURRENCES -> {

                    0.96f
                }

                mistake.occurrenceCount >=
                        FIVE_OR_MORE_OCCURRENCES -> {

                    0.88f
                }

                mistake.occurrenceCount >=
                        THREE_OR_MORE_OCCURRENCES -> {

                    0.78f
                }

                mistake.occurrenceCount >=
                        TWO_OR_MORE_OCCURRENCES -> {

                    0.68f
                }

                else -> {
                    0.56f
                }
            }

        val severityAdjustment =
            when (mistake.severity) {
                MistakeSeverity.MINOR -> LOW_SEVERITY_ADJUSTMENT
                MistakeSeverity.MODERATE -> MEDIUM_SEVERITY_ADJUSTMENT
                MistakeSeverity.CRITICAL -> HIGH_SEVERITY_ADJUSTMENT
            }

        val resolvedAdjustment =
            if (mistake.isResolved) {
                RESOLVED_CONFIDENCE_ADJUSTMENT
            } else {
                0f
            }

        return (
                occurrenceConfidence +
                        severityAdjustment +
                        resolvedAdjustment
                )
            .coerceIn(
                minimumValue =
                    MINIMUM_CONFIDENCE,
                maximumValue =
                    MAXIMUM_CONFIDENCE
            )
    }

    private fun createFallbackReason(
        mistake: DetectedMistakeEntity
    ): String {
        return if (mistake.isResolved) {
            "The learner demonstrated a successful use of " +
                    createTitle(
                        mistake =
                            mistake
                    ) +
                    "."
        } else {
            "The assessment detected a recurring issue with " +
                    createTitle(
                        mistake =
                            mistake
                    ) +
                    "."
        }
    }

    private fun createReinforcementInstruction(
        mistake: DetectedMistakeEntity
    ): String {
        return when (mistake.grammarFeature) {
            GrammarFeature.PRESENT_TENSE -> {
                "Ask natural questions about routines, current " +
                        "activities, and familiar facts so the " +
                        "learner can reuse present-tense verb forms."
            }

            GrammarFeature.BASIC_WORD_ORDER -> {
                "Encourage complete German statements and " +
                        "follow-up explanations that require natural " +
                        "verb-second word order."
            }

            GrammarFeature.YES_NO_QUESTIONS -> {
                "Create natural opportunities for the learner " +
                        "to ask yes-or-no questions."
            }

            GrammarFeature.W_QUESTIONS -> {
                "Create natural opportunities for the learner " +
                        "to ask follow-up questions using German " +
                        "question words."
            }

            GrammarFeature.PERSONAL_PRONOUNS -> {
                "Ask questions requiring the learner to refer " +
                        "to themselves and other people with personal " +
                        "pronouns."
            }

            GrammarFeature.NOMINATIVE_CASE -> {
                "Prompt descriptions that require identifying " +
                        "people, objects, and subjects using the " +
                        "nominative case."
            }

            GrammarFeature.ACCUSATIVE_CASE -> {
                "Create conversation turns requiring direct " +
                        "objects and accusative articles."
            }

            GrammarFeature.DATIVE_CASE -> {
                "Create natural conversational situations using " +
                        "dative verbs, indirect objects, or dative " +
                        "prepositions."
            }

            GrammarFeature.DEFINITE_ARTICLES,
            GrammarFeature.INDEFINITE_ARTICLES,
            GrammarFeature.POSSESSIVE_ARTICLES -> {
                "Create situations requiring repeated article " +
                        "selection across familiar nouns and cases."
            }

            GrammarFeature.MODAL_VERBS -> {
                "Ask about abilities, obligations, permissions, " +
                        "plans, and preferences using modal verbs."
            }

            GrammarFeature.SEPARABLE_VERBS -> {
                "Ask about routines and actions that naturally " +
                        "use separable verbs."
            }

            GrammarFeature.PERFECT_TENSE,
            GrammarFeature.SIMPLE_PAST_COMMON_VERBS -> {
                "Ask the learner about previous experiences and " +
                        "past events."
            }

            GrammarFeature.IMPERATIVE -> {
                "Use scenarios requiring the learner to give " +
                        "clear instructions or polite directions."
            }

            GrammarFeature.COMPARATIVE,
            GrammarFeature.SUPERLATIVE -> {
                "Ask the learner to compare options, experiences, " +
                        "places, or preferences."
            }

            GrammarFeature.COORDINATING_CONJUNCTIONS -> {
                "Invite the learner to connect two related ideas " +
                        "within one response."
            }

            GrammarFeature.SUBORDINATE_CLAUSES -> {
                "Ask follow-up questions that encourage reasons, " +
                        "conditions, or explanations using subordinate " +
                        "clauses."
            }

            GrammarFeature.RELATIVE_CLAUSES -> {
                "Ask the learner to describe people, places, or " +
                        "objects with additional identifying details."
            }

            GrammarFeature.ADJECTIVE_ENDINGS -> {
                "Create descriptive situations requiring " +
                        "adjectives after different articles."
            }

            GrammarFeature.PASSIVE_VOICE -> {
                "Introduce topics involving processes, procedures, " +
                        "or events where passive voice is natural."
            }

            GrammarFeature.KONJUNKTIV_TWO -> {
                "Ask about wishes, imagined situations, polite " +
                        "requests, or hypothetical choices."
            }

            GrammarFeature.COMPLEX_CONNECTORS -> {
                "Encourage extended answers using contrast, cause, " +
                        "consequence, and qualification."
            }

            GrammarFeature.NOMINALIZATION -> {
                "Introduce formal or explanatory topics where " +
                        "nominalized expressions are useful."
            }

            GrammarFeature.ADVANCED_REGISTER_CONTROL -> {
                "Vary the social situation so the learner must " +
                        "adapt between informal, neutral, and formal " +
                        "German."
            }

            GrammarFeature.IDIOMATIC_GRAMMAR -> {
                "Create natural advanced conversation contexts " +
                        "where idiomatic grammar can be reused."
            }

            null -> {
                when (
                    determineFocusType(
                        mistake =
                            mistake
                    )
                ) {
                    LearningFocusType.VOCABULARY -> {
                        "Naturally create another opportunity to " +
                                "use the corrected vocabulary in a " +
                                "meaningful response."
                    }

                    LearningFocusType.GRAMMAR -> {
                        "Naturally reinforce this grammar pattern " +
                                "through a relevant follow-up question."
                    }

                    LearningFocusType.COMMUNICATION -> {
                        "Naturally reinforce this communication " +
                                "skill through conversation."
                    }
                }
            }
        }
    }

    private fun createLearnerGuidance(
        mistake: DetectedMistakeEntity
    ): String {
        return when (mistake.grammarFeature) {
            GrammarFeature.PRESENT_TENSE -> {
                "Check the present-tense verb form."
            }

            GrammarFeature.BASIC_WORD_ORDER -> {
                "Watch German word order."
            }

            GrammarFeature.YES_NO_QUESTIONS -> {
                "Put the verb first in a yes-or-no question."
            }

            GrammarFeature.W_QUESTIONS -> {
                "Check the structure after the question word."
            }

            GrammarFeature.PERSONAL_PRONOUNS -> {
                "Check the personal pronoun."
            }

            GrammarFeature.NOMINATIVE_CASE -> {
                "Check the article used for the subject."
            }

            GrammarFeature.ACCUSATIVE_CASE -> {
                "Watch the accusative article."
            }

            GrammarFeature.DATIVE_CASE -> {
                "Watch the dative article."
            }

            GrammarFeature.DEFINITE_ARTICLES -> {
                "Check the definite article."
            }

            GrammarFeature.INDEFINITE_ARTICLES -> {
                "Check the indefinite article."
            }

            GrammarFeature.POSSESSIVE_ARTICLES -> {
                "Check the possessive article ending."
            }

            GrammarFeature.MODAL_VERBS -> {
                "Keep the second verb in the infinitive."
            }

            GrammarFeature.SEPARABLE_VERBS -> {
                "Watch where the separable prefix goes."
            }

            GrammarFeature.PERFECT_TENSE -> {
                "Check the auxiliary verb and past participle."
            }

            GrammarFeature.SIMPLE_PAST_COMMON_VERBS -> {
                "Check the simple-past verb form."
            }

            GrammarFeature.IMPERATIVE -> {
                "Check the command form."
            }

            GrammarFeature.COMPARATIVE -> {
                "Check the comparative form."
            }

            GrammarFeature.SUPERLATIVE -> {
                "Check the superlative form."
            }

            GrammarFeature.COORDINATING_CONJUNCTIONS -> {
                "Connect the related ideas naturally."
            }

            GrammarFeature.SUBORDINATE_CLAUSES -> {
                "Remember the verb position in the subordinate " +
                        "clause."
            }

            GrammarFeature.RELATIVE_CLAUSES -> {
                "Check the relative pronoun and final verb."
            }

            GrammarFeature.ADJECTIVE_ENDINGS -> {
                "Check the adjective ending."
            }

            GrammarFeature.PASSIVE_VOICE -> {
                "Check how the passive form is built."
            }

            GrammarFeature.KONJUNKTIV_TWO -> {
                "Check the hypothetical or polite verb form."
            }

            GrammarFeature.COMPLEX_CONNECTORS -> {
                "Use a connector that clearly relates the ideas."
            }

            GrammarFeature.NOMINALIZATION -> {
                "Check the noun form and capitalization."
            }

            GrammarFeature.ADVANCED_REGISTER_CONTROL -> {
                "Match the language to the social situation."
            }

            GrammarFeature.IDIOMATIC_GRAMMAR -> {
                "This advanced pattern may need another try."
            }

            null -> {
                if (
                    determineFocusType(
                        mistake =
                            mistake
                    ) ==
                    LearningFocusType.VOCABULARY
                ) {
                    "Try the corrected word again later."
                } else {
                    "This language pattern may need more practice."
                }
            }
        }
    }

    private fun String.toDisplayLabel(): String {
        return lowercase()
            .split(
                "_"
            )
            .filter(
                String::isNotBlank
            )
            .joinToString(
                separator =
                    " "
            ) { word ->
                word.replaceFirstChar { character ->
                    character.uppercase()
                }
            }
    }

    private companion object {
        const val TWO_OR_MORE_OCCURRENCES =
            2

        const val THREE_OR_MORE_OCCURRENCES =
            3

        const val FIVE_OR_MORE_OCCURRENCES =
            5

        const val TEN_OR_MORE_OCCURRENCES =
            10

        const val LOW_SEVERITY_ADJUSTMENT =
            0f

        const val MEDIUM_SEVERITY_ADJUSTMENT =
            0.04f

        const val HIGH_SEVERITY_ADJUSTMENT =
            0.08f

        const val RESOLVED_CONFIDENCE_ADJUSTMENT =
            0.02f

        const val MINIMUM_CONFIDENCE =
            0f

        const val MAXIMUM_CONFIDENCE =
            1f
    }
}