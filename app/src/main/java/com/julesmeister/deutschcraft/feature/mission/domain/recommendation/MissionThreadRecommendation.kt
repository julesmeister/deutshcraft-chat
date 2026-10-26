package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity

data class MissionThreadRecommendation(
    val id: String,
    val title: String,
    val previewText: String,
    val opportunity: MissionOpportunity
)