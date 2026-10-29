package com.julesmeister.deutschcraft.feature.besser.data.selector

import android.util.Log
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserCapabilityCandidate
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal object BesserSelectionResponseParser {

    private val json =
        Json {
            ignoreUnknownKeys =
                true

            isLenient =
                true

            explicitNulls =
                false
        }

    fun parse(
        rawResponse: String,
        candidates: List<BesserCapabilityCandidate>
    ): BesserChallengeSelectionResult {
        Log.d(
            PARSER_TAG,
            buildString {
                append("Parsing response. ")
                append("rawLength=")
                append(rawResponse.length)
                append(", candidates=")
                append(candidates.size)
            }
        )

        if (rawResponse.isBlank()) {
            Log.w(
                PARSER_TAG,
                "Rejected: Qwen response was empty."
            )

            return noChallenge(
                reason =
                    "Empty selector response"
            )
        }

        val cleanedResponse =
            cleanResponse(
                rawResponse =
                    rawResponse
            )

        Log.d(
            PARSER_TAG,
            buildString {
                append("Cleaned response: ")
                append(
                    cleanedResponse.take(
                        MAXIMUM_LOG_RESPONSE_LENGTH
                    )
                )

                if (
                    cleanedResponse.length >
                    MAXIMUM_LOG_RESPONSE_LENGTH
                ) {
                    append("...")
                }
            }
        )

        val response =
            runCatching {
                json.decodeFromString<BesserSelectionResponse>(
                    cleanedResponse
                )
            }
                .onFailure { throwable ->
                    Log.e(
                        PARSER_TAG,
                        "JSON parsing failed.",
                        throwable
                    )
                }
                .getOrElse {
                    return noChallenge(
                        reason =
                            "Invalid selector JSON"
                    )
                }

        val decision =
            response.decision
                .trim()
                .uppercase()

        Log.d(
            PARSER_TAG,
            buildString {
                append("Parsed decision=")
                append(decision)
                append(", capabilityId=")
                append(response.capabilityId)
                append(", confidence=")
                append(response.confidence)
                append(", hintCount=")
                append(
                    response.hints
                        ?.size
                        ?: 0
                )
            }
        )

        return when (decision) {
            DECISION_NO_CHALLENGE -> {
                val reason =
                    response.reason
                        ?.trim()
                        ?.takeIf(
                            String::isNotBlank
                        )

                Log.i(
                    PARSER_TAG,
                    buildString {
                        append(
                            "Qwen explicitly returned NO_CHALLENGE"
                        )
                        append(", reason=")
                        append(
                            reason
                                ?: "none"
                        )
                    }
                )

                noChallenge(
                    reason =
                        reason
                )
            }

            DECISION_SELECT -> {
                parseSelection(
                    response =
                        response,
                    candidates =
                        candidates
                )
            }

            else -> {
                Log.w(
                    PARSER_TAG,
                    buildString {
                        append(
                            "Rejected: unsupported decision="
                        )
                        append(decision)
                    }
                )

                noChallenge(
                    reason =
                        "Unsupported selector decision"
                )
            }
        }
    }

    private fun parseSelection(
        response: BesserSelectionResponse,
        candidates: List<BesserCapabilityCandidate>
    ): BesserChallengeSelectionResult {
        val capabilityId =
            response.capabilityId
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        if (capabilityId == null) {
            Log.w(
                PARSER_TAG,
                "Rejected SELECT: capabilityId is missing."
            )

            return noChallenge(
                reason =
                    "Selection did not include a capability ID"
            )
        }

        Log.d(
            PARSER_TAG,
            buildString {
                append(
                    "Validating selected capabilityId="
                )
                append(capabilityId)
            }
        )

        val capability =
            candidates.firstOrNull { candidate ->
                candidate.capabilityId ==
                        capabilityId
            }

        if (capability == null) {
            Log.w(
                PARSER_TAG,
                buildString {
                    append(
                        "Rejected SELECT: capability is not "
                    )
                    append(
                        "in the local shortlist. selected="
                    )
                    append(capabilityId)
                    append(", allowed=")
                    append(
                        candidates.map { candidate ->
                            candidate.capabilityId
                        }
                    )
                }
            )

            return noChallenge(
                reason =
                    "Selected capability was not in the local shortlist"
            )
        }

        val rationale =
            response.rationale
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        if (rationale == null) {
            Log.w(
                PARSER_TAG,
                "Rejected SELECT: rationale is missing."
            )

            return noChallenge(
                reason =
                    "Selection did not include a rationale"
            )
        }

        val instruction =
            response.instruction
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        if (instruction == null) {
            Log.w(
                PARSER_TAG,
                "Rejected SELECT: instruction is missing."
            )

            return noChallenge(
                reason =
                    "Selection did not include an instruction"
            )
        }

        val rawConfidence =
            response.confidence

        val confidence =
            rawConfidence
                ?.coerceIn(
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
                )
                ?: 0f

        Log.d(
            PARSER_TAG,
            buildString {
                append("Selection validation: capability=")
                append(capability.capabilityId)
                append(", rawConfidence=")
                append(rawConfidence)
                append(", normalizedConfidence=")
                append(confidence)
                append(", threshold=")
                append(MINIMUM_SELECTION_CONFIDENCE)
            }
        )

        if (
            confidence <
            MINIMUM_SELECTION_CONFIDENCE
        ) {
            Log.w(
                PARSER_TAG,
                buildString {
                    append(
                        "Rejected SELECT: confidence below threshold. "
                    )
                    append("confidence=")
                    append(confidence)
                    append(", threshold=")
                    append(MINIMUM_SELECTION_CONFIDENCE)
                }
            )

            return noChallenge(
                reason =
                    "Selector confidence was below the threshold"
            )
        }

        val hints =
            response.hints
                .orEmpty()
                .asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()
                .take(
                    MAXIMUM_HINT_COUNT
                )
                .toList()

        Log.i(
            PARSER_TAG,
            buildString {
                append("SELECT accepted. ")
                append("capabilityId=")
                append(capability.capabilityId)
                append(", capability=")
                append(capability.name)
                append(", confidence=")
                append(confidence)
                append(", hintCount=")
                append(hints.size)
            }
        )

        return BesserChallengeSelectionResult.Selected(
            capability =
                capability,
            rationale =
                rationale,
            instruction =
                instruction,
            hints =
                hints,
            confidence =
                confidence
        )
    }

    private fun noChallenge(
        reason: String?
    ): BesserChallengeSelectionResult.NoChallenge {
        return BesserChallengeSelectionResult.NoChallenge(
            reason =
                reason
        )
    }

    private fun cleanResponse(
        rawResponse: String
    ): String {
        var cleaned =
            rawResponse.trim()

        if (
            cleaned.startsWith(
                prefix =
                    "```json",
                ignoreCase =
                    true
            )
        ) {
            cleaned =
                cleaned.substring(
                    startIndex =
                        "```json".length
                )
        } else if (
            cleaned.startsWith(
                prefix =
                    "```"
            )
        ) {
            cleaned =
                cleaned.substring(
                    startIndex =
                        "```".length
                )
        }

        if (
            cleaned.endsWith(
                suffix =
                    "```"
            )
        ) {
            cleaned =
                cleaned.dropLast(
                    3
                )
        }

        return cleaned.trim()
    }

    private const val PARSER_TAG =
        "BesserParser"

    private const val DECISION_SELECT =
        "SELECT"

    private const val DECISION_NO_CHALLENGE =
        "NO_CHALLENGE"

    private const val MAXIMUM_HINT_COUNT =
        3

    private const val MINIMUM_SELECTION_CONFIDENCE =
        0.62f

    private const val MAXIMUM_LOG_RESPONSE_LENGTH =
        2_000
}

@Serializable
private data class BesserSelectionResponse(
    @SerialName(
        "decision"
    )
    val decision: String,

    @SerialName(
        "capabilityId"
    )
    val capabilityId: String? =
        null,

    @SerialName(
        "rationale"
    )
    val rationale: String? =
        null,

    @SerialName(
        "instruction"
    )
    val instruction: String? =
        null,

    @SerialName(
        "hints"
    )
    val hints: List<String>? =
        null,

    @SerialName(
        "confidence"
    )
    val confidence: Float? =
        null,

    @SerialName(
        "reason"
    )
    val reason: String? =
        null
)