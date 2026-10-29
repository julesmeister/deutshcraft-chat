package com.julesmeister.deutschcraft.feature.besser.data.selector

import android.util.Log
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionRequest
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionResult
import com.julesmeister.deutschcraft.feature.besser.domain.selector.BesserChallengeSelector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QwenBesserChallengeSelector @Inject constructor(
    private val textGenerator:
    BesserSelectionTextGenerator
) : BesserChallengeSelector {

    override suspend fun select(
        request: BesserChallengeSelectionRequest
    ): BesserChallengeSelectionResult {
        Log.d(
            SELECTOR_TAG,
            buildString {
                append("Selection started. ")
                append("current=")
                append(request.currentCefrLevel)
                append(", target=")
                append(request.targetCefrLevel)
                append(", candidates=")
                append(request.candidates.size)
                append(", messageLength=")
                append(request.userMessage.length)
            }
        )

        if (request.userMessage.isBlank()) {
            Log.w(
                SELECTOR_TAG,
                "Selection stopped: learner message is empty."
            )

            return BesserChallengeSelectionResult.NoChallenge(
                reason =
                    "Learner message was empty"
            )
        }

        if (request.candidates.isEmpty()) {
            Log.w(
                SELECTOR_TAG,
                "Selection stopped: local shortlist is empty."
            )

            return BesserChallengeSelectionResult.NoChallenge(
                reason =
                    "Local capability shortlist was empty"
            )
        }

        request.candidates.forEachIndexed {
                index,
                candidate ->

            Log.d(
                SELECTOR_TAG,
                buildString {
                    append("Candidate #")
                    append(index + 1)
                    append(": ")
                    append(candidate.capabilityId)
                    append(" | ")
                    append(candidate.name)
                    append(" | level=")
                    append(candidate.cefrLevel)
                    append(" | score=")
                    append(candidate.localPriorityScore)
                }
            )
        }

        val prompt =
            BesserSelectionPromptBuilder.build(
                request =
                    request
            )

        Log.d(
            SELECTOR_TAG,
            buildString {
                append("Prompt built. chars=")
                append(prompt.length)
                append(". Calling Qwen.")
            }
        )

        val rawResponse =
            runCatching {
                textGenerator.generate(
                    prompt =
                        prompt
                )
            }
                .onFailure { throwable ->
                    Log.e(
                        SELECTOR_TAG,
                        "Qwen generation failed.",
                        throwable
                    )
                }
                .getOrElse {
                    return BesserChallengeSelectionResult.NoChallenge(
                        reason =
                            "Besser selector generation failed"
                    )
                }

        Log.d(
            SELECTOR_TAG,
            buildString {
                append("Raw Qwen response: ")
                append(
                    rawResponse
                        .take(
                            MAXIMUM_LOG_RESPONSE_LENGTH
                        )
                )

                if (
                    rawResponse.length >
                    MAXIMUM_LOG_RESPONSE_LENGTH
                ) {
                    append("...")
                }
            }
        )

        val result =
            BesserSelectionResponseParser.parse(
                rawResponse =
                    rawResponse,
                candidates =
                    request.candidates
            )

        when (result) {
            is BesserChallengeSelectionResult.NoChallenge -> {
                Log.i(
                    SELECTOR_TAG,
                    buildString {
                        append("FINAL decision=NO_CHALLENGE")
                        append(", reason=")
                        append(
                            result.reason
                                ?: "none"
                        )
                    }
                )
            }

            is BesserChallengeSelectionResult.Selected -> {
                Log.i(
                    SELECTOR_TAG,
                    buildString {
                        append("FINAL decision=SELECT")
                        append(", capabilityId=")
                        append(
                            result
                                .capability
                                .capabilityId
                        )
                        append(", capability=")
                        append(
                            result
                                .capability
                                .name
                        )
                        append(", confidence=")
                        append(
                            result.confidence
                        )
                        append(", hints=")
                        append(
                            result.hints.size
                        )
                    }
                )
            }
        }

        return result
    }

    private companion object {

        const val SELECTOR_TAG =
            "BesserSelector"

        const val MAXIMUM_LOG_RESPONSE_LENGTH =
            2_000
    }
}