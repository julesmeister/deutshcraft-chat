package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.data.local.ChatLocalDataSource
import com.julesmeister.deutschcraft.feature.chat.data.local.toDomain
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionOfferCache
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.AcceptMissionOfferUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.DetectMissionOpportunityUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.DismissMissionOfferUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class ChatMissionOfferCoordinator @Inject constructor(
    private val localDataSource:
    ChatLocalDataSource,
    private val detectMissionOpportunity:
    DetectMissionOpportunityUseCase,
    private val acceptMissionOffer:
    AcceptMissionOfferUseCase,
    private val dismissMissionOffer:
    DismissMissionOfferUseCase,
    private val missionOfferCache:
    MissionOfferCache
) {

    fun restoreOffer(
        conversationId: String
    ): MissionOpportunity? {
        return missionOfferCache.getOffer(
            conversationId =
                conversationId
        )
    }

    suspend fun detectOffer(
        conversationId: String,
        hasActiveMission: Boolean
    ): MissionOpportunity? {
        if (hasActiveMission) {
            missionOfferCache.clearOffer(
                conversationId =
                    conversationId
            )

            return null
        }

        return try {
            val conversation =
                localDataSource.getConversation(
                    conversationId =
                        conversationId
                )
                    ?: return null

            val conversationHistory =
                localDataSource
                    .getMessages(
                        conversationId =
                            conversationId
                    )
                    .map { entity ->
                        entity.toDomain()
                    }

            detectMissionOpportunity(
                conversationId =
                    conversationId,
                conversationHistory =
                    conversationHistory,
                cefrLevel =
                    conversation.cefrLevel
                        ?: CefrLevel.A1,
                hasActiveMission =
                    false
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            /*
             * Mission detection is supplementary.
             *
             * It must never cause a successful chat response
             * to appear as failed.
             */
            Log.e(
                ChatPresentationTokens.MissionOfferCoordinator.CoordinatorTag,
                "Mission opportunity detection failed.",
                throwable
            )

            null
        }
    }

    suspend fun acceptOffer(
        conversationId: String
    ): String {
        return acceptMissionOffer(
            conversationId =
                conversationId
        )
    }

    fun dismissOffer(
        conversationId: String
    ) {
        dismissMissionOffer(
            conversationId =
                conversationId
        )
    }

    fun clearOffer(
        conversationId: String
    ) {
        missionOfferCache.clearOffer(
            conversationId =
                conversationId
        )
    }

}
