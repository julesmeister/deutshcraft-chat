package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.adaptivelearning.data.ai.CommunicationEvidenceAiGateway
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveConversationTurn
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.CefrCommunicationProfiles
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.CommunicationEvidenceBatch
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Evaluates one learner turn for communication behaviors
 * expected at the learner's current CEFR level.
 *
 * This use case does not persist evidence. It only produces
 * CommunicationEvidenceBatch.
 *
 * GenerateAdaptiveFocusesUseCase later combines this result
 * with detected grammar and vocabulary mistakes.
 */
class EvaluateCommunicationEvidenceUseCase
@Inject constructor(
    private val aiGateway:
    CommunicationEvidenceAiGateway
) {

    suspend operator fun invoke(
        learnerMessage: String,
        cefrLevel: CefrLevel,
        recentConversation:
        List<AdaptiveConversationTurn>
    ): CommunicationEvidenceBatch {
        val safeLearnerMessage =
            learnerMessage.trim()

        if (safeLearnerMessage.isBlank()) {
            return CommunicationEvidenceBatch()
        }

        val communicationProfile =
            CefrCommunicationProfiles.forLevel(
                level =
                    cefrLevel
            )

        if (
            communicationProfile
                .expectedSkills
                .isEmpty()
        ) {
            return CommunicationEvidenceBatch()
        }

        return try {
            val result =
                aiGateway.evaluate(
                    learnerMessage =
                        safeLearnerMessage,
                    cefrLevel =
                        cefrLevel,
                    recentConversation =
                        recentConversation
                            .filter { turn ->
                                turn.isUsable
                            }
                            .takeLast(
                                MAXIMUM_RECENT_TURN_COUNT
                            ),
                    expectedSkills =
                        communicationProfile.expectedSkills
                )

            Log.d(
                USE_CASE_TAG,
                buildString {
                    append("Communication evaluation completed. ")
                    append("level=")
                    append(cefrLevel.name)
                    append(", evidence=")
                    append(result.usableEvidence.size)
                    append(", skills=")
                    append(
                        result
                            .usableEvidence
                            .map { evidence ->
                                evidence.skill.name
                            }
                    )
                }
            )

            CommunicationEvidenceBatch(
                evidence =
                    result.usableEvidence
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (
            throwable:
            Throwable
        ) {
            /*
             * Communication evaluation must never make an
             * otherwise successful chat operation fail.
             */
            Log.e(
                USE_CASE_TAG,
                "Communication evidence evaluation failed.",
                throwable
            )

            CommunicationEvidenceBatch()
        }
    }

    private companion object {
        const val USE_CASE_TAG =
            "CommunicationEvaluation"

        const val MAXIMUM_RECENT_TURN_COUNT =
            10
    }
}