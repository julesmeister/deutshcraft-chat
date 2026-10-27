package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.entity.ConversationTitleSource
import com.julesmeister.deutschcraft.feature.chat.data.factory.ConversationFactory
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionOfferCache
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendationStore
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Converts one persisted thread-list recommendation into:
 *
 * - A real conversation
 * - A persisted mission template
 * - A ConversationMission
 * - Objective progress records
 * - An active mission
 *
 * After successful creation, the original persisted
 * recommendation becomes STARTED and is linked to both:
 *
 * - conversationId
 * - conversationMissionId
 */
class StartRecommendedMissionThreadUseCase
@Inject constructor(
    private val conversationFactory:
    ConversationFactory,
    private val missionOfferCache:
    MissionOfferCache,
    private val recommendationStore:
    MissionThreadRecommendationStore,
    private val acceptMissionOffer:
    AcceptMissionOfferUseCase
) {

    suspend operator fun invoke(
        recommendation:
        MissionThreadRecommendation
    ): Conversation {
        validateRecommendation(
            recommendation =
                recommendation
        )

        val conversationId =
            UUID.randomUUID().toString()

        /*
         * Create the new conversation from the already
         * generated recommendation.
         *
         * No additional Qwen request is required here.
         */
        val conversation =
            conversationFactory.create(
                conversationId =
                    conversationId,
                title =
                    recommendation.title,
                titleSource =
                    ConversationTitleSource
                        .AI_GENERATED,
                cefrLevel =
                    recommendation
                        .opportunity
                        .mission
                        .difficulty
                        .toCefrLevel(),
                openingMessage =
                    recommendation.previewText
            )

        /*
         * AcceptMissionOfferUseCase expects a generated
         * opportunity to be available temporarily through
         * MissionOfferCache.
         *
         * The opportunity is placed under the newly created
         * conversation ID.
         */
        missionOfferCache.replaceOffer(
            conversationId =
                conversation.id,
            opportunity =
                recommendation.opportunity
        )

        try {
            /*
             * AcceptMissionOfferUseCase:
             *
             * 1. Saves the generated MissionTemplate
             * 2. Saves generated mission objectives
             * 3. Creates a ConversationMission
             * 4. Creates objective-progress rows
             * 5. Starts the ConversationMission
             * 6. Returns the created conversationMissionId
             */
            val conversationMissionId =
                acceptMissionOffer(
                    conversationId =
                        conversation.id
                )

            /*
             * The recommendation is no longer an available
             * draft.
             *
             * Persist the link to the real conversation and
             * real ConversationMission. This also prevents the
             * scenario from being recommended again as READY
             * after the application restarts.
             */
            recommendationStore.markStarted(
                recommendationId =
                    recommendation.id,
                conversationId =
                    conversation.id,
                conversationMissionId =
                    conversationMissionId
            )

            return conversation
        } catch (
            cancellationException:
            CancellationException
        ) {
            /*
             * Always remove the temporary in-memory offer when
             * the operation is cancelled.
             */
            missionOfferCache.clearOffer(
                conversationId =
                    conversation.id
            )

            throw cancellationException
        } catch (throwable: Throwable) {
            /*
             * Remove the temporary generated offer after any
             * failure.
             *
             * The recommendation remains persisted unless
             * markStarted succeeded. This allows the learner
             * to retry after a transient failure.
             */
            missionOfferCache.clearOffer(
                conversationId =
                    conversation.id
            )

            throw throwable
        } finally {
            /*
             * AcceptMissionOfferUseCase may already clear the
             * offer after success, but clearing it again is
             * harmless and ensures no stale offer remains.
             */
            missionOfferCache.clearOffer(
                conversationId =
                    conversation.id
            )
        }
    }

    private fun validateRecommendation(
        recommendation:
        MissionThreadRecommendation
    ) {
        require(
            recommendation.id.isNotBlank()
        ) {
            "Recommendation ID must not be blank."
        }

        require(
            recommendation.title.isNotBlank()
        ) {
            "Recommendation title must not be blank."
        }

        require(
            recommendation.previewText.isNotBlank()
        ) {
            "Recommendation opening message must not be blank."
        }

        val mission =
            recommendation
                .opportunity
                .mission

        require(
            mission.title.isNotBlank()
        ) {
            "Generated mission title must not be blank."
        }

        require(
            mission.description.isNotBlank()
        ) {
            "Generated mission description must not be blank."
        }

        require(
            mission.scenario.isNotBlank()
        ) {
            "Generated mission scenario must not be blank."
        }

        require(
            mission.learnerRole.isNotBlank()
        ) {
            "Generated learner role must not be blank."
        }

        require(
            mission.assistantRole.isNotBlank()
        ) {
            "Generated assistant role must not be blank."
        }

        require(
            mission.objectives.isNotEmpty()
        ) {
            "Generated mission must contain objectives."
        }
    }
}

private fun MissionDifficulty.toCefrLevel():
        CefrLevel {
    return CefrLevel.valueOf(
        value =
            name
    )
}