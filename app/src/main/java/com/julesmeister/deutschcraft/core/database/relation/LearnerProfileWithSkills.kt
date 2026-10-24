package com.julesmeister.deutschcraft.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.julesmeister.deutschcraft.core.database.entity.LearnerProfileEntity
import com.julesmeister.deutschcraft.core.database.entity.SkillScoreEntity

data class LearnerProfileWithSkills(
    @Embedded
    val profile: LearnerProfileEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "profileId"
    )
    val skillScores: List<SkillScoreEntity>
)