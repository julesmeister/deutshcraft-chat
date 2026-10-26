package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.entity.ConversationTitleSource
import com.julesmeister.deutschcraft.feature.chat.data.factory.ConversationFactory
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionOfferCache
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionThreadRecommendationCache
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import java.util.UUID
import javax.inject.Inject

class StartRecommendedMissionThreadUseCase @Inject constructor(
    private val conversationFactory:
    ConversationFactory,
    private val missionOfferCache:
    MissionOfferCache,
    private val recommendationCache:
    MissionThreadRecommendationCache,
    private val acceptMissionOffer:
    AcceptMissionOfferUseCase
) {

    suspend operator fun invoke(
        recommendation:
        MissionThreadRecommendation
    ): Conversation {
        val conversationId =
            UUID.randomUUID().toString()

        /*
         * Create the conversation using the generated
         * mission title and Emil's opening message.
         *
         * AI_GENERATED is the correct value from the
         * ConversationTitleSource enum.
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
         * AcceptMissionOfferUseCase expects the generated
         * opportunity to be temporarily available under the
         * new conversation ID.
         */
        missionOfferCache.replaceOffer(
            conversationId =
                conversation.id,
            opportunity =
                recommendation.opportunity
        )

        try {
            /*
             * This persists the generated template, creates
             * its objective progress records, and starts the
             * mission for the new conversation.
             */
            acceptMissionOffer(
                conversationId =
                    conversation.id
            )
        } catch (throwable: Throwable) {
            /*
             * Remove the temporary offer if mission creation
             * fails after the conversation is created.
             */
            missionOfferCache.clearOffer(
                conversationId =
                    conversation.id
            )

            throw throwable
        }

        /*
         * The recommendation has now become a real
         * conversation and active mission.
         */
        recommendationCache.clear()

        return conversation
    }
}

private fun MissionDifficulty.toCefrLevel():
        CefrLevel {
    return CefrLevel.valueOf(
        name
    )
}