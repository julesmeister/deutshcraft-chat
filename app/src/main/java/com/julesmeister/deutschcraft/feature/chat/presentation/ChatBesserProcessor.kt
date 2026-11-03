// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/ChatBesserProcessor.kt

package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.GenerateAndPersistBesserChallengeUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class ChatBesserProcessor
@Inject constructor(
    private val generateAndPersistBesserChallenge:
    GenerateAndPersistBesserChallengeUseCase,

    private val besserChallengeRepository:
    BesserChallengeRepository
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

            val missionObjective =
                activeMission
                    ?.currentObjective
                    ?.objective
                    ?.title
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )

            val recentlyChallengedCapabilityIds =
                loadRecentlyChallengedCapabilityIds(
                    conversationId =
                        conversationId,

                    currentMessageId =
                        learnerMessage.id
                )

            Log.d(
                ChatPresentationTokens.BesserProcessor.BesserTag,
                buildString {
                    append(
                        "Preparing contextual Besser selection. "
                    )

                    append(
                        "messageId="
                    )

                    append(
                        learnerMessage.id
                    )

                    append(
                        ", currentLevel="
                    )

                    append(
                        currentLevelName
                    )

                    append(
                        ", targetLevel="
                    )

                    append(
                        targetLevelName
                    )

                    append(
                        ", recentCapabilityIds="
                    )

                    append(
                        recentlyChallengedCapabilityIds
                    )
                }
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

                    missionObjective =
                        missionObjective,

                    detectedCategories =
                        emptySet(),

                    recentlyChallengedCapabilityIds =
                        recentlyChallengedCapabilityIds
                )

            if (challenge == null) {
                Log.d(
                    ChatPresentationTokens.BesserProcessor.BesserTag,
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
                ChatPresentationTokens.BesserProcessor.BesserTag,
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

                    append(
                        ", recentPenaltyApplied="
                    )

                    append(
                        challenge.capabilityId in
                                recentlyChallengedCapabilityIds
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
                ChatPresentationTokens.BesserProcessor.BesserTag,
                "Contextual Besser generation failed.",
                throwable
            )
        }
    }

    private suspend fun loadRecentlyChallengedCapabilityIds(
        conversationId: String,
        currentMessageId: String
    ): Set<String> {
        val challenges =
            besserChallengeRepository
                .observeForConversation(
                    conversationId =
                        conversationId
                )
                .first()

        return challenges
            .asSequence()
            /*
             * Exclude a challenge already attached to the
             * message currently being processed.
             */
            .filter { challenge ->
                challenge.messageId !=
                        currentMessageId
            }
            /*
             * Newest challenges first.
             *
             * AVAILABLE and ACCEPTED challenges also count as
             * recent exposure. This prevents several nearby
             * asynchronous turns from being offered the same
             * capability while an earlier challenge is still
             * unresolved.
             */
            .sortedByDescending { challenge ->
                challenge.updatedAt
            }
            /*
             * We care about the latest distinct capabilities,
             * not merely the latest challenge rows.
             */
            .distinctBy { challenge ->
                challenge.capabilityId
            }
            .take(
                ChatPresentationTokens.BesserProcessor.RecentCapabilityHistoryLimit
            )
            .map { challenge ->
                challenge.capabilityId
            }
            .filter(
                String::isNotBlank
            )
            .toSet()
    }

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

}
