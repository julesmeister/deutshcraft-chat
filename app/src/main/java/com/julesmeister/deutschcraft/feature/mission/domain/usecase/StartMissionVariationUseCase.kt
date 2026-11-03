// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/usecase/StartMissionVariationUseCase.kt

package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import com.julesmeister.deutschcraft.feature.chat.data.local.ChatLocalDataSource
import com.julesmeister.deutschcraft.feature.chat.data.local.toDomain
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionVariationAiGenerator
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionOfferCache
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjective
import com.julesmeister.deutschcraft.feature.mission.domain.MissionTemplate
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionDraft
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationContext
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationMode
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationType
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class StartMissionVariationUseCase
@Inject constructor(
    private val localDataSource:
    ChatLocalDataSource,

    private val missionRepository:
    MissionRepository,

    private val missionVariationAiGenerator:
    MissionVariationAiGenerator,

    private val missionOfferCache:
    MissionOfferCache,

    private val acceptMissionOffer:
    AcceptMissionOfferUseCase,

    private val chatRepository:
    ChatRepository,

    private val clock:
    Clock
) {

    suspend operator fun invoke(
        conversationId: String,
        context: MissionVariationContext,
        variationType: MissionVariationType
    ): String {
        require(
            conversationId.isNotBlank()
        ) {
            "Conversation ID must not be blank."
        }

        val sourceMissionId =
            context.resolvedSourceMissionId

        require(
            sourceMissionId.isNotBlank()
        ) {
            "Source mission ID must not be blank."
        }

        val currentMission =
            missionRepository.getConversationMission(
                conversationMissionId =
                    sourceMissionId
            )
                ?: throw IllegalStateException(
                    "The source mission could not be found."
                )

        require(
            currentMission.conversationId ==
                    conversationId
        ) {
            "The source mission does not belong to this conversation."
        }

        val rootMission =
            loadRootMission(
                currentMission =
                    currentMission
            )

        val conversationHistory =
            localDataSource
                .getMessages(
                    conversationId =
                        conversationId
                )
                .map { entity ->
                    entity.toDomain()
                }
                .sortedBy { message ->
                    message.sequenceNumber
                }

        require(
            conversationHistory.isNotEmpty()
        ) {
            "The conversation does not contain any messages."
        }

        val generatedVariation =
            missionVariationAiGenerator.generate(
                variationType =
                    variationType,

                rootMission =
                    rootMission,

                currentMission =
                    currentMission,

                conversationHistory =
                    conversationHistory,

                context =
                    context
            )

        val generatedTemplate =
            generatedVariation
                .mission
                .toTemplate()

        val resultingMissionId =
            when (context.mode) {
                MissionVariationMode.BRANCH_ACTIVE_MISSION -> {
                    missionRepository.replaceActiveMissionBranch(
                        conversationMissionId =
                            currentMission.id,

                        replacementTemplate =
                            generatedTemplate
                    )

                    currentMission.id
                }

                MissionVariationMode.EXTEND_COMPLETED_MISSION -> {
                    startCompletedMissionExtension(
                        conversationId =
                            conversationId,

                        sourceMission =
                            currentMission,

                        generatedMission =
                            generatedVariation.mission
                    )
                }
            }

        appendOpeningMessage(
            conversationId =
                conversationId,

            content =
                generatedVariation.openingMessage
        )

        return resultingMissionId
    }

    private suspend fun loadRootMission(
        currentMission: ConversationMission
    ): MissionTemplate {

        return missionRepository.getMissionTemplate(
            missionTemplateId =
                currentMission
                    .resolvedRootMissionTemplateId
        )
            ?: currentMission.template
    }

    private suspend fun startCompletedMissionExtension(
        conversationId: String,
        sourceMission: ConversationMission,
        generatedMission: GeneratedMissionDraft
    ): String {
        val opportunity =
            MissionOpportunity(
                confidence =
                    GENERATED_VARIATION_CONFIDENCE,

                reason =
                    COMPLETED_EXTENSION_REASON,

                mission =
                    generatedMission
            )

        missionOfferCache.replaceOffer(
            conversationId =
                conversationId,

            opportunity =
                opportunity
        )

        return try {
            acceptMissionOffer(
                conversationId =
                    conversationId,

                sourceRecommendationId =
                    null,

                targetCapabilityIds =
                    sourceMission.targetCapabilityIds,

                adaptiveFocusKeys =
                    sourceMission.adaptiveFocusKeys
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } finally {
            missionOfferCache.clearOffer(
                conversationId =
                    conversationId
            )
        }
    }

    private fun GeneratedMissionDraft.toTemplate():
            MissionTemplate {

        val templateId =
            UUID.randomUUID().toString()

        return MissionTemplate(
            id =
                templateId,

            title =
                title.trim(),

            description =
                description.trim(),

            scenario =
                scenario.trim(),

            learnerRole =
                learnerRole.trim(),

            assistantRole =
                assistantRole.trim(),

            difficulty =
                difficulty,

            icon =
                icon
                    .trim()
                    .ifBlank {
                        DEFAULT_MISSION_ICON
                    },

            objectives =
                objectives.mapIndexed {
                        index,
                        objective ->

                    MissionObjective(
                        id =
                            UUID.randomUUID().toString(),

                        missionTemplateId =
                            templateId,

                        title =
                            objective.title.trim(),

                        description =
                            objective.description.trim(),

                        sequenceNumber =
                            index + 1,

                        isRequired =
                            objective.isRequired,

                        completionHint =
                            objective.completionHint.trim(),

                        coachingHints =
                            objective.sanitizedCoachingHints,

                        targetVocabulary =
                            objective.sanitizedTargetVocabulary,

                        examplePhrases =
                            objective.sanitizedExamplePhrases
                    )
                },

            isEnabled =
                true
        )
    }

    private suspend fun appendOpeningMessage(
        conversationId: String,
        content: String
    ) {
        val normalizedContent =
            content
                .trim()
                .replace(
                    MULTIPLE_WHITESPACE_REGEX,
                    " "
                )

        require(
            normalizedContent.isNotBlank()
        ) {
            "The generated opening message is blank."
        }

        chatRepository.saveMessage(
            message =
                ChatMessage(
                    id =
                        UUID.randomUUID().toString(),

                    conversationId =
                        conversationId,

                    sequenceNumber =
                        chatRepository.getNextSequenceNumber(
                            conversationId =
                                conversationId
                        ),

                    sender =
                        MessageSender.ASSISTANT,

                    content =
                        normalizedContent,

                    timestamp =
                        clock.millis()
                )
        )
    }

    private companion object {

        const val GENERATED_VARIATION_CONFIDENCE =
            1f

        const val COMPLETED_EXTENSION_REASON =
            "AI-generated continuation of a completed mission."

        const val DEFAULT_MISSION_ICON =
            "↻"

        val MULTIPLE_WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}