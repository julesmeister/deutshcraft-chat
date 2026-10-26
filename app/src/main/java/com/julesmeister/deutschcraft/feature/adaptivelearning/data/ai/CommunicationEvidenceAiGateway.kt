package com.julesmeister.deutschcraft.feature.adaptivelearning.data.ai

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveConversationTurn
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.CommunicationEvidenceBatch
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.CommunicationSkill

/*
 * Evaluates one learner message for CEFR-appropriate
 * communication behaviors.
 *
 * This gateway does not persist anything.
 *
 * Persistence remains the responsibility of:
 *
 * GenerateAdaptiveFocusesUseCase
 * RecordAdaptiveLearningObservationsUseCase
 */
interface CommunicationEvidenceAiGateway {

    suspend fun evaluate(
        learnerMessage: String,
        cefrLevel: CefrLevel,
        recentConversation:
        List<AdaptiveConversationTurn>,
        expectedSkills:
        Set<CommunicationSkill>
    ): CommunicationEvidenceBatch
}