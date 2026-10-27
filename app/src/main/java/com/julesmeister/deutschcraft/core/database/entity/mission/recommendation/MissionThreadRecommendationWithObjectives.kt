package com.julesmeister.deutschcraft.core.database.entity.mission.recommendation

import androidx.room.Embedded
import androidx.room.Relation

/*
 * Room relation representing one complete persisted
 * recommendation and its generated objectives.
 */
data class MissionThreadRecommendationWithObjectives(
    @Embedded
    val recommendation:
    MissionThreadRecommendationEntity,

    @Relation(
        parentColumn =
            "id",
        entityColumn =
            "recommendationId"
    )
    val objectives:
    List<MissionThreadRecommendationObjectiveEntity>
)