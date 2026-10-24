package com.julesmeister.deutschcraft.core.cefr.engine

import com.julesmeister.deutschcraft.core.cefr.model.AssessmentEvidence
import com.julesmeister.deutschcraft.core.cefr.model.AssessmentSource
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.cefr.model.GrammarFeature
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface LocalMessageEvaluator {

    fun evaluate(
        messageId: String,
        conversationId: String,
        content: String,
        currentLevel: CefrLevel
    ): AssessmentEvidence
}

@Singleton
class DefaultLocalMessageEvaluator @Inject constructor() :
    LocalMessageEvaluator {

    override fun evaluate(
        messageId: String,
        conversationId: String,
        content: String,
        currentLevel: CefrLevel
    ): AssessmentEvidence {
        val normalizedText = content.trim()

        val words = normalizedText
            .split(Regex("\\s+"))
            .filter(String::isNotBlank)

        val uniqueWordCount = words
            .map { word ->
                word.lowercase()
                    .trim('.', ',', '!', '?', ';', ':')
            }
            .filter(String::isNotBlank)
            .toSet()
            .size

        val sentenceCount = normalizedText
            .split(
                Regex("[.!?]+")
            )
            .count { sentence ->
                sentence.isNotBlank()
            }
            .coerceAtLeast(1)

        val averageWordsPerSentence =
            words.size.toDouble() /
                    sentenceCount.toDouble()

        val vocabularyDiversity =
            if (words.isEmpty()) {
                0.0
            } else {
                uniqueWordCount.toDouble() /
                        words.size.toDouble()
            }

        val connectors = listOf(
            "und",
            "aber",
            "oder",
            "denn",
            "weil",
            "dass",
            "obwohl",
            "deshalb",
            "trotzdem",
            "während"
        )

        val connectorCount = words.count { word ->
            word.lowercase()
                .trim(',', '.', '!', '?') in connectors
        }

        val vocabularyScore =
            (
                    35 +
                            words.size.coerceAtMost(30) +
                            vocabularyDiversity * 25
                    )
                .toInt()
                .clampScore()

        val coherenceScore =
            (
                    40 +
                            connectorCount * 8 +
                            sentenceCount.coerceAtMost(5) * 4
                    )
                .clampScore()

        val rangeScore =
            (
                    35 +
                            uniqueWordCount.coerceAtMost(35) +
                            connectorCount * 5
                    )
                .clampScore()

        val punctuationBonus =
            if (
                normalizedText.endsWith(".") ||
                normalizedText.endsWith("!") ||
                normalizedText.endsWith("?")
            ) {
                8
            } else {
                0
            }

        val capitalizationBonus =
            if (
                normalizedText.firstOrNull()
                    ?.isUpperCase() == true
            ) {
                7
            } else {
                0
            }

        val accuracyScore =
            (
                    45 +
                            punctuationBonus +
                            capitalizationBonus
                    )
                .clampScore()

        /*
         * Offline heuristics cannot reliably judge German
         * grammar. Keep this conservative until AI or a
         * dedicated grammar analyzer supplies evidence.
         */
        val grammarScore =
            (
                    40 +
                            capitalizationBonus +
                            connectorCount * 3
                    )
                .clampScore()

        val detectedFeatures =
            buildSet {
                add(GrammarFeature.PRESENT_TENSE)

                if (connectorCount > 0) {
                    add(
                        GrammarFeature
                            .COORDINATING_CONJUNCTIONS
                    )
                }

                if (
                    words.any { word ->
                        word.lowercase() in
                                setOf(
                                    "weil",
                                    "dass",
                                    "obwohl",
                                    "während"
                                )
                    }
                ) {
                    add(
                        GrammarFeature
                            .SUBORDINATE_CLAUSES
                    )
                }
            }

        return AssessmentEvidence(
            id = UUID.randomUUID().toString(),
            messageId = messageId,
            conversationId = conversationId,
            assessedLevel = currentLevel,
            grammarScore = grammarScore,
            vocabularyScore = vocabularyScore,
            coherenceScore = coherenceScore,
            accuracyScore = accuracyScore,
            rangeScore = rangeScore,
            detectedGrammarFeatures =
                detectedFeatures,
            mistakes = emptyList(),
            analyzedAt =
                System.currentTimeMillis(),
            source =
                AssessmentSource.LOCAL_RULE_ENGINE,
            ruleVersion = 1
        )
    }
}