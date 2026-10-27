package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionInteractionEvidence
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionInteractionEvidenceRepository
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class BuildMissionInteractionEvidenceUseCase
@Inject constructor(
    private val repository:
    MissionInteractionEvidenceRepository
) {

    suspend operator fun invoke(
        mission:
        ConversationMission
    ): MissionInteractionEvidence {
        require(
            mission.id.isNotBlank()
        ) {
            "Conversation mission ID must not be blank."
        }

        return try {
            val evidence =
                repository.getForMission(
                    conversationMissionId =
                        mission.id
                )

            if (evidence.isEmpty()) {
                return MissionInteractionEvidence.EMPTY
            }

            /*
             * Low-confidence classifications are excluded.
             *
             * Missing or uncertain AI evidence should not
             * count against the learner.
             */
            val acceptedEvidence =
                evidence.filter { item ->
                    item.normalizedConfidence >=
                            MINIMUM_ACCEPTED_CONFIDENCE
                }

            if (acceptedEvidence.isEmpty()) {
                return MissionInteractionEvidence.EMPTY
            }

            val expectedReplies =
                acceptedEvidence.count { item ->
                    item.replyExpected
                }

            /*
             * Relevant replies are counted only when a reply
             * was expected. This keeps the numerator aligned
             * with the comprehension denominator.
             */
            val relevantReplies =
                acceptedEvidence.count { item ->
                    item.replyExpected &&
                            item.relevantReply
                }

            val followUpQuestions =
                acceptedEvidence.count { item ->
                    item.followUpQuestion
                }

            val clarifications =
                acceptedEvidence.count { item ->
                    item.clarification
                }

            Log.d(
                USE_CASE_TAG,
                buildString {
                    append(
                        "Mission interaction evidence built. "
                    )
                    append(
                        "missionId="
                    )
                    append(
                        mission.id
                    )
                    append(
                        ", evaluated="
                    )
                    append(
                        acceptedEvidence.size
                    )
                    append(
                        ", expectedReplies="
                    )
                    append(
                        expectedReplies
                    )
                    append(
                        ", relevantReplies="
                    )
                    append(
                        relevantReplies
                    )
                    append(
                        ", followUps="
                    )
                    append(
                        followUpQuestions
                    )
                    append(
                        ", clarifications="
                    )
                    append(
                        clarifications
                    )
                }
            )

            MissionInteractionEvidence(
                expectedReplyCount =
                    expectedReplies,
                contextuallyRelevantReplyCount =
                    relevantReplies,
                independentFollowUpQuestionCount =
                    followUpQuestions,
                successfulClarificationCount =
                    clarifications,
                evaluatedMessageCount =
                    acceptedEvidence.size
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                USE_CASE_TAG,
                "Mission interaction evidence could not be " +
                        "built.",
                throwable
            )

            MissionInteractionEvidence.EMPTY
        }
    }

    private companion object {
        const val USE_CASE_TAG =
            "MissionInteractionEvidence"

        /*
         * Interaction results below this threshold are treated
         * as insufficient evidence.
         */
        const val MINIMUM_ACCEPTED_CONFIDENCE =
            0.60f
    }
}