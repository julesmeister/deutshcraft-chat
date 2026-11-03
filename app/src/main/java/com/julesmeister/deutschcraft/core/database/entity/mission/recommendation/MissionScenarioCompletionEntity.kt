package com.julesmeister.deutschcraft.core.database.entity.mission.recommendation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mission_scenario_completions")
data class MissionScenarioCompletionEntity(
    @PrimaryKey val scenarioType: String
)
