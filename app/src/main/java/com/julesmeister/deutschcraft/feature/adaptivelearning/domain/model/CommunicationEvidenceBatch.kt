package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

/*
 * Result of evaluating one learner message for
 * CEFR-appropriate communication behavior.
 */
data class CommunicationEvidenceBatch(
    val evidence:
    List<CommunicationEvidence> =
        emptyList()
) {

    val usableEvidence:
            List<CommunicationEvidence>
        get() =
            evidence
                .asSequence()
                .filter { item ->
                    item.hasUsableEvidence
                }
                .groupBy { item ->
                    item.focusKey
                }
                .mapNotNull { (_, groupedEvidence) ->
                    groupedEvidence.maxByOrNull { item ->
                        item.normalizedConfidence
                    }
                }
                .sortedByDescending { item ->
                    item.normalizedConfidence
                }

    val hasUsableEvidence: Boolean
        get() =
            usableEvidence.isNotEmpty()
}