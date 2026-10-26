package com.julesmeister.deutschcraft.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.julesmeister.deutschcraft.core.database.entity.mission.ConversationMissionEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveProgressEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionTemplateEntity

data class ConversationMissionWithDetails(
    @Embedded
    val conversationMission:
    ConversationMissionEntity,

    @Relation(
        parentColumn =
            "missionTemplateId",
        entityColumn =
            "id"
    )
    val template:
    MissionTemplateEntity,

    @Relation(
        parentColumn =
            "missionTemplateId",
        entityColumn =
            "missionTemplateId"
    )
    val objectives:
    List<MissionObjectiveEntity>,

    @Relation(
        parentColumn =
            "id",
        entityColumn =
            "conversationMissionId"
    )
    val objectiveProgress:
    List<MissionObjectiveProgressEntity>
)