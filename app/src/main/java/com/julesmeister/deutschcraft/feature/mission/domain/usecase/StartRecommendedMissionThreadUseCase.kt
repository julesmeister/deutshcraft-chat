// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/usecase/StartRecommendedMissionThreadUseCase.kt

package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.entity.ConversationTitleSource
import com.julesmeister.deutschcraft.feature.chat.data.factory.ConversationFactory
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionOfferCache
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendationStore
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class StartRecommendedMissionThreadUseCase
@Inject constructor(
    private val conversationFactory:
    ConversationFactory,

    private val missionOfferCache:
    MissionOfferCache,

    private val recommendationStore:
    MissionThreadRecommendationStore,

    private val acceptMissionOffer:
    AcceptMissionOfferUseCase,

    private val database: DeutschCraftDatabase
) {

    private val startingMutex = Mutex()

    suspend operator fun invoke(
        recommendation:
        MissionThreadRecommendation
    ): Conversation = startingMutex.withLock {
        database.withTransaction {
            val eligibleRecommendation = recommendationStore.getEligibleRecommendations(
                cefrLevel = recommendation.opportunity.mission.difficulty.toCefrLevel(),
                limit = Int.MAX_VALUE
            ).firstOrNull { it.id == recommendation.id }

            requireNotNull(eligibleRecommendation) {
                "This mission recommendation is no longer available."
            }

            startEligibleRecommendation(eligibleRecommendation)
        }
    }

    private suspend fun startEligibleRecommendation(
        recommendation: MissionThreadRecommendation
    ): Conversation {

        validateRecommendation(
            recommendation =
                recommendation
        )

        val conversationId =
            UUID.randomUUID().toString()

        val conversation =
            conversationFactory.create(
                conversationId =
                    conversationId,

                title =
                    recommendation.title,

                titleSource =
                    ConversationTitleSource.AI_GENERATED,

                cefrLevel =
                    recommendation
                        .opportunity
                        .mission
                        .difficulty
                        .toCefrLevel(),

                openingMessage =
                    recommendation.previewText
            )

        missionOfferCache.replaceOffer(
            conversationId =
                conversation.id,

            opportunity =
                recommendation.opportunity
        )

        try {
            val conversationMissionId =
                acceptMissionOffer(
                    conversationId =
                        conversation.id,

                    sourceRecommendationId =
                        recommendation.id,

                    targetCapabilityIds =
                        recommendation
                            .targetCapabilityIds,

                    adaptiveFocusKeys =
                        recommendation
                            .adaptiveFocusKeys
                )

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
            missionOfferCache.clearOffer(
                conversationId =
                    conversation.id
            )

            throw cancellationException
        } catch (throwable: Throwable) {
            missionOfferCache.clearOffer(
                conversationId =
                    conversation.id
            )

            throw throwable
        } finally {
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