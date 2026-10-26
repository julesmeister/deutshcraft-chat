package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionOfferCache
import javax.inject.Inject

class DismissMissionOfferUseCase @Inject constructor(
    private val missionOfferCache:
    MissionOfferCache
) {

    operator fun invoke(
        conversationId: String
    ) {
        val offer =
            missionOfferCache.getOffer(
                conversationId =
                    conversationId
            ) ?: return

        missionOfferCache.dismissOffer(
            conversationId =
                conversationId,
            missionTitle =
                offer.mission.title
        )
    }
}