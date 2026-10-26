package com.julesmeister.deutschcraft.feature.mission.data.cache

import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissionOfferCache @Inject constructor() {

    private val offersByConversationId =
        mutableMapOf<String, MissionOpportunity>()

    private val dismissedMissionTitles =
        mutableMapOf<String, MutableSet<String>>()

    private val lastCheckMessageCounts =
        mutableMapOf<String, Int>()

    @Synchronized
    fun getOffer(
        conversationId: String
    ): MissionOpportunity? {
        return offersByConversationId[
            conversationId
        ]
    }

    @Synchronized
    fun replaceOffer(
        conversationId: String,
        opportunity: MissionOpportunity
    ) {
        offersByConversationId[
            conversationId
        ] = opportunity
    }

    @Synchronized
    fun clearOffer(
        conversationId: String
    ) {
        offersByConversationId.remove(
            conversationId
        )
    }

    @Synchronized
    fun dismissOffer(
        conversationId: String,
        missionTitle: String
    ) {
        clearOffer(
            conversationId =
                conversationId
        )

        dismissedMissionTitles
            .getOrPut(
                conversationId
            ) {
                mutableSetOf()
            }
            .add(
                normalizeTitle(
                    missionTitle
                )
            )
    }

    @Synchronized
    fun wasDismissed(
        conversationId: String,
        missionTitle: String
    ): Boolean {
        return normalizeTitle(
            missionTitle
        ) in (
                dismissedMissionTitles[
                    conversationId
                ] ?: emptySet()
                )
    }

    @Synchronized
    fun getLastCheckMessageCount(
        conversationId: String
    ): Int {
        return lastCheckMessageCounts[
            conversationId
        ] ?: 0
    }

    @Synchronized
    fun recordCheck(
        conversationId: String,
        learnerMessageCount: Int
    ) {
        lastCheckMessageCounts[
            conversationId
        ] = learnerMessageCount
    }

    private fun normalizeTitle(
        value: String
    ): String {
        return value
            .trim()
            .lowercase()
    }
}