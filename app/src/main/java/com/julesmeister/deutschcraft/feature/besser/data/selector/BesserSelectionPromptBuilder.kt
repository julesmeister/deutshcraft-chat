// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/data/selector/BesserSelectionPromptBuilder.kt

package com.julesmeister.deutschcraft.feature.besser.data.selector

import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMasteryStatus
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserCapabilityCandidate
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionRequest

internal object BesserSelectionPromptBuilder {

    fun build(
        request: BesserChallengeSelectionRequest
    ): String {

        val candidatesText =
            request
                .candidates
                .mapIndexed { index, candidate ->
                    candidate.toPromptText(
                        index =
                            index
                    )
                }
                .joinToString(
                    separator =
                        "\n\n"
                )

        val missionContext =
            buildMissionContext(
                request =
                    request
            )

        val learnerHistory =
            buildLearnerHistory(
                candidates =
                    request.candidates
            )

        return buildString {
            appendLine(
                "You are the contextual Besser growth selector " +
                        "for DeutschCraft."
            )

            appendLine()

            appendLine(
                "DeutschCraft develops German communication " +
                        "through real conversation."
            )

            appendLine()
            appendLine("YOUR JOB")
            appendLine()

            appendLine(
                "Determine whether the learner's latest message " +
                        "contains one natural opportunity to practise " +
                        "a capability from the supplied candidate list."
            )

            appendLine()
            appendLine("SELECTION PRIORITIES")
            appendLine()

            appendLine(
                "1. Conversation relevance is mandatory."
            )

            appendLine(
                "2. Never create a challenge merely because a " +
                        "candidate has a high progression score."
            )

            appendLine(
                "3. Prefer a DEVELOPMENTALLY NEXT capability when " +
                        "it naturally improves what the learner was " +
                        "already trying to communicate."
            )

            appendLine(
                "4. A candidate marked successorOfMastered=true is " +
                        "a particularly useful next step when relevant."
            )

            appendLine(
                "5. A candidate marked progressionReady=true has " +
                        "sufficient prerequisite evidence."
            )

            appendLine(
                "6. A candidate marked progressionReady=false is " +
                        "not forbidden, but select it only when the " +
                        "current conversational opportunity clearly " +
                        "justifies the stretch."
            )

            appendLine(
                "7. Prefer DEVELOPING capabilities over MASTERED " +
                        "capabilities when both fit equally well."
            )

            appendLine(
                "8. Avoid repeating MASTERED capabilities unless " +
                        "they are clearly the most useful option."
            )

            appendLine(
                "9. Do not force unnecessary grammatical or " +
                        "rhetorical complexity."
            )

            appendLine(
                "10. The selected capability must improve the " +
                        "learner's intended meaning."
            )

            appendLine(
                "11. Never select anything outside the supplied " +
                        "candidate list."
            )

            appendLine(
                "12. If none naturally fits, return NO_CHALLENGE."
            )

            appendLine(
                "13. The learner-facing instruction must describe " +
                        "what to improve without writing the answer."
            )

            appendLine(
                "14. Hints must become progressively more helpful."
            )

            appendLine()
            append("Current CEFR: ")
            appendLine(
                request.currentCefrLevel
            )

            append("Target CEFR: ")
            appendLine(
                request.targetCefrLevel
            )

            if (missionContext.isNotBlank()) {
                appendLine()
                appendLine("MISSION CONTEXT")
                appendLine()
                appendLine(
                    missionContext
                )
            }

            appendLine()
            appendLine("LEARNER MESSAGE")
            appendLine()

            appendLine(
                request.userMessage.trim()
            )

            appendLine()
            appendLine("LEARNER CAPABILITY HISTORY")
            appendLine()

            appendLine(
                learnerHistory
            )

            appendLine()
            appendLine("ALLOWED CAPABILITIES")
            appendLine()

            appendLine(
                candidatesText
            )

            appendLine()
            appendLine("OUTPUT")
            appendLine()

            appendLine(
                "Return exactly one JSON object."
            )

            appendLine()
            appendLine(
                "If no challenge is appropriate:"
            )
            appendLine()

            appendLine(
                """
                {
                  "decision": "NO_CHALLENGE",
                  "reason": "brief reason"
                }
                """.trimIndent()
            )

            appendLine()
            appendLine(
                "If a challenge is appropriate:"
            )
            appendLine()

            appendLine(
                """
                {
                  "decision": "SELECT",
                  "capabilityId": "exact capabilityId from the candidate list",
                  "rationale": "short explanation in German",
                  "instruction": "short learner-facing challenge in German",
                  "hints": [
                    "gentle first hint",
                    "more specific second hint",
                    "optional stronger third hint"
                  ],
                  "confidence": 0.85
                }
                """.trimIndent()
            )

            appendLine()
            appendLine(
                "confidence must be between 0.0 and 1.0."
            )

            appendLine(
                "Return JSON only."
            )

            appendLine(
                "Do not use Markdown."
            )

            appendLine(
                "Do not wrap the JSON in code fences."
            )
        }
    }

    private fun BesserCapabilityCandidate.toPromptText(
        index: Int
    ): String {

        return buildString {
            append("Candidate ")
            appendLine(
                index +
                        1
            )

            append("capabilityId: ")
            appendLine(
                capabilityId
            )

            append("name: ")
            appendLine(
                name
            )

            append("description: ")
            appendLine(
                description
            )

            append("category: ")
            appendLine(
                category
            )

            append("cefrLevel: ")
            appendLine(
                cefrLevel
            )

            append("masteryStatus: ")
            appendLine(
                masteryStatus.name
            )

            append("masteryPercentage: ")
            append(
                masteryPercentage
            )
            appendLine("%")

            append("attemptCount: ")
            appendLine(
                masteryAttemptCount
            )

            append("completedCount: ")
            appendLine(
                masteryCompletedCount
            )

            append("skippedCount: ")
            appendLine(
                masterySkippedCount
            )

            append("progressionReady: ")
            appendLine(
                progressionReady
            )

            append("successorOfMastered: ")
            appendLine(
                isDirectSuccessorOfMasteredCapability
            )

            append("prerequisitesSatisfied: ")
            append(
                satisfiedProgressionPrerequisiteCount
            )
            append("/")
            appendLine(
                progressionPrerequisiteCount
            )

            append("prerequisiteMastery: ")
            append(
                prerequisiteMasteryPercentage
            )
            appendLine("%")
        }
    }

    private fun buildMissionContext(
        request: BesserChallengeSelectionRequest
    ): String {

        return buildString {
            request
                .missionTitle
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
                ?.let { title ->
                    append("Mission title: ")
                    appendLine(
                        title
                    )
                }

            request
                .missionObjective
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
                ?.let { objective ->
                    append(
                        "Mission objective: "
                    )

                    appendLine(
                        objective
                    )
                }
        }
            .trim()
    }

    private fun buildLearnerHistory(
        candidates:
        List<BesserCapabilityCandidate>
    ): String {

        val mastered =
            candidates.filter { candidate ->
                candidate.masteryStatus ==
                        CapabilityMasteryStatus.MASTERED
            }

        val practising =
            candidates.filter { candidate ->
                candidate.masteryStatus ==
                        CapabilityMasteryStatus.PRACTISING
            }

        val developing =
            candidates.filter { candidate ->
                candidate.masteryStatus ==
                        CapabilityMasteryStatus.DEVELOPING
            }

        val nextSteps =
            candidates.filter { candidate ->
                candidate
                    .isDirectSuccessorOfMasteredCapability
            }

        return buildString {
            appendLine(
                "Recommended next-step capabilities:"
            )

            appendLine(
                nextSteps.toHistoryList()
            )

            appendLine()

            appendLine(
                "Developing capabilities:"
            )

            appendLine(
                developing.toHistoryList()
            )

            appendLine()

            appendLine(
                "Practising capabilities:"
            )

            appendLine(
                practising.toHistoryList()
            )

            appendLine()

            appendLine(
                "Mastered capabilities:"
            )

            appendLine(
                mastered.toHistoryList()
            )
        }
            .trim()
    }

    private fun List<BesserCapabilityCandidate>.toHistoryList():
            String {

        if (isEmpty()) {
            return "- none"
        }

        return joinToString(
            separator =
                "\n"
        ) { candidate ->
            buildString {
                append("- ")
                append(
                    candidate.capabilityId
                )

                append(": ")
                append(
                    candidate.masteryPercentage
                )

                append("% mastery, ")

                append(
                    candidate.masteryStatus.name
                )

                append(", progressionReady=")
                append(
                    candidate.progressionReady
                )
            }
        }
    }
}