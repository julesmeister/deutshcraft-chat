package com.julesmeister.deutschcraft.feature.besser.data.evaluation

import android.util.Log
import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallenge
import com.julesmeister.deutschcraft.feature.besser.domain.BesserRefinementEvaluation
import com.julesmeister.deutschcraft.feature.besser.domain.evaluation.BesserRefinementEvaluator
import com.julesmeister.deutschcraft.feature.chat.data.ai.DeutschCraftAiGateway
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Singleton
class QwenBesserRefinementEvaluator
@Inject constructor(
    private val aiGateway:
    DeutschCraftAiGateway
) : BesserRefinementEvaluator {

    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }

    override suspend fun evaluate(
        challenge: BesserChallenge,
        refinedText: String
    ): BesserRefinementEvaluation {
        val rawResponse =
            aiGateway.generateStructuredText(
                systemPrompt =
                    SYSTEM_PROMPT,
                userPrompt =
                    buildUserPrompt(
                        challenge =
                            challenge,
                        refinedText =
                            refinedText
                    )
            )

        Log.d(
            EVALUATOR_TAG,
            "Raw evaluation: ${rawResponse.take(1_000)}"
        )

        val payload =
            json.decodeFromString<EvaluationPayload>(
                cleanResponse(
                    rawResponse
                )
            )

        val feedback =
            payload.feedback
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?: DEFAULT_FEEDBACK

        return BesserRefinementEvaluation(
            demonstrated =
                payload.demonstrated,
            score =
                payload.score.coerceIn(
                    minimumValue = 0,
                    maximumValue = 100
                ),
            feedback =
                feedback,
            evidence =
                payload.evidence
                    ?.trim()
                    .orEmpty()
        )
    }

    private fun buildUserPrompt(
        challenge: BesserChallenge,
        refinedText: String
    ): String {
        val hints =
            challenge.visibleHints
                .joinToString(
                    separator = "\n"
                ) { hint ->
                    "- $hint"
                }
                .ifBlank {
                    "- (keine)"
                }

        return """
            Capability ID:
            ${challenge.capabilityId}

            Challenge instruction given to the learner:
            ${challenge.instruction}

            Hints the learner revealed:
            $hints

            Original learner sentence:
            "${challenge.originalMessageContent}"

            Rewritten learner sentence:
            "$refinedText"

            Evaluate the rewritten sentence now.
        """.trimIndent()
    }

    private fun cleanResponse(
        raw: String
    ): String {
        return raw
            .trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    @Serializable
    private data class EvaluationPayload(
        @SerialName("demonstrated")
        val demonstrated: Boolean = false,

        @SerialName("score")
        val score: Int = 0,

        @SerialName("feedback")
        val feedback: String? = null,

        @SerialName("evidence")
        val evidence: String? = null
    )

    private companion object {

        const val EVALUATOR_TAG =
            "BesserEvaluator"

        const val DEFAULT_FEEDBACK =
            "Versuche es noch einmal."

        val SYSTEM_PROMPT =
            """
            You are a strict but encouraging German language
            evaluator inside DeutschCraft.

            You judge ONE thing: whether the rewritten sentence
            demonstrates the capability the challenge asked for.

            Rules:
            - Compare the original and rewritten sentences.
            - Merely changing words, adding adjectives, or
              adding intensifiers does NOT demonstrate the
              capability.
            - The capability must be clearly and correctly
              present in the rewrite.
            - Minor spelling mistakes do not fail the attempt
              if the capability is clearly demonstrated.
            - Serious grammar errors in the targeted structure
              reduce the score.

            Scoring:
            - 80-100: capability clearly demonstrated.
            - 50-79: partially demonstrated or flawed.
            - 0-49: not demonstrated.

            feedback:
            - Exactly one short sentence in simple German.
            - Address the learner with "du".
            - If passed, praise specifically.
            - If not passed, say what is still missing.
            - Never give the full corrected sentence.

            evidence:
            - Quote the part of the rewrite that shows the
              capability, or empty string if none.

            Return exactly one JSON object:

            {
              "demonstrated": true,
              "score": 0,
              "feedback": "...",
              "evidence": "..."
            }

            Return JSON only. No Markdown. No commentary.
            """.trimIndent()
    }
}