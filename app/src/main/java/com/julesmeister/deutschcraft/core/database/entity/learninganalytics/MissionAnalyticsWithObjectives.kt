package com.julesmeister.deutschcraft.core.database.entity.learninganalytics

import androidx.room.Embedded
import androidx.room.Relation

data class MissionAnalyticsWithObjectives(
    @Embedded
    val analytics:
    MissionAnalyticsEntity,

    @Relation(
        parentColumn =
            "missionId",
        entityColumn =
            "missionId"
    )
    val objectives:
    List<MissionObjectiveAnalyticsEntity>
)