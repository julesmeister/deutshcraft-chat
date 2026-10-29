package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.GenerateAndPersistBesserChallengeUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Handles contextual Besser challenge generation after an exact
 * persisted learner message has been identified.
 *
 * A Besser failure never fails the normal chat send.
 */
class ChatBesserProcessor
@Inject constructor(
    private val generateAndPersistBesserChallenge:
    GenerateAndPersistBesserChallengeUseCase
) {

    suspend fun process(
        conversationId: String,
        learnerMessage: ChatMessage,
        currentCefrLevel: CefrLevel,
        activeMission: ConversationMission?
    ) {
        try {
            val currentLevelName =
                currentCefrLevel.name

            val targetLevelName =
                nextCefrLevelName(
                    currentLevel =
                        currentLevelName
                )

            val missionTitle =
                activeMission
                    ?.template
                    ?.title
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )

            val challenge =
                generateAndPersistBesserChallenge(
                    conversationId =
                        conversationId,

                    messageId =
                        learnerMessage.id,

                    learnerMessage =
                        learnerMessage.content,

                    currentCefrLevel =
                        currentLevelName,

                    targetCefrLevel =
                        targetLevelName,

                    missionTitle =
                        missionTitle,

                    /*
                     * Add the exact active objective here later
                     * when ConversationMission exposes the
                     * current-objective property.
                     */
                    missionObjective =
                        null,

                    detectedCategories =
                        emptySet(),

                    recentlyChallengedCapabilityIds =
                        emptySet()
                )

            if (challenge == null) {
                Log.d(
                    BESSER_TAG,
                    buildString {
                        append(
                            "No contextual Besser challenge. "
                        )
                        append(
                            "messageId="
                        )
                        append(
                            learnerMessage.id
                        )
                    }
                )

                return
            }

            Log.i(
                BESSER_TAG,
                buildString {
                    append(
                        "Contextual Besser challenge saved. "
                    )
                    append(
                        "challengeId="
                    )
                    append(
                        challenge.id
                    )
                    append(
                        ", messageId="
                    )
                    append(
                        challenge.messageId
                    )
                    append(
                        ", capabilityId="
                    )
                    append(
                        challenge.capabilityId
                    )
                    append(
                        ", confidence="
                    )
                    append(
                        challenge.confidence
                    )
                }
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                BESSER_TAG,
                "Contextual Besser generation failed.",
                throwable
            )
        }
    }

    /*
     * Returns the next CEFR destination.
     *
     * C2 learners remain at C2 so Besser can focus on
     * consolidation rather than a nonexistent next band.
     */
    private fun nextCefrLevelName(
        currentLevel: String
    ): String {
        return when (
            currentLevel
                .trim()
                .uppercase()
        ) {
            "A1" ->
                "A2"

            "A2" ->
                "B1"

            "B1" ->
                "B2"

            "B2" ->
                "C1"

            "C1" ->
                "C2"

            "C2" ->
                "C2"

            else ->
                currentLevel
                    .trim()
                    .uppercase()
        }
    }

    private companion object {

        const val BESSER_TAG =
            "ContextualBesser"
    }
}