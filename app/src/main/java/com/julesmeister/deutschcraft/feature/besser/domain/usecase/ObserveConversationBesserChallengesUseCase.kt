package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeUiModel
import com.julesmeister.deutschcraft.feature.besser.presentation.toUiModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveConversationBesserChallengesUseCase @Inject constructor(
    private val besserChallengeRepository:
    BesserChallengeRepository
) {

    operator fun invoke(
        conversationId: String
    ): Flow<Map<String, BesserChallengeUiModel>> {
        require(
            conversationId.isNotBlank()
        ) {
            "Conversation ID cannot be blank."
        }

        return besserChallengeRepository
            .observeActiveForConversation(
                conversationId =
                    conversationId
            )
            .map { challenges ->
                challenges
                    .sortedBy(
                        selector =
                            com.julesmeister.deutschcraft
                                .feature
                                .besser
                                .domain
                                .BesserChallenge::createdAt
                    )
                    .associateBy(
                        keySelector = { challenge ->
                            challenge.messageId
                        },
                        valueTransform = { challenge ->
                            challenge.toUiModel()
                        }
                    )
            }
    }
}