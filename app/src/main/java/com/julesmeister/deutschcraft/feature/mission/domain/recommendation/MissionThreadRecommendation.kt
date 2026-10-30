// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/recommendation/MissionThreadRecommendation.kt

package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity

data class MissionThreadRecommendation(
    val id: String,

    val scenarioType:
    MissionScenarioType,

    val title: String,

    val previewText: String,

    val opportunity:
    MissionOpportunity,

    val learningContext:
    MissionLearningContext =
        MissionLearningContext.Empty
) {

    val targetCapabilityIds: Set<String>
        get() =
            learningContext
                .targetCapabilityIds

    val adaptiveFocusKeys: Set<String>
        get() =
            learningContext
                .adaptiveFocusKeys

    val masteredCapabilityIds: Set<String>
        get() =
            learningContext
                .masteredCapabilityIds
}