package com.julesmeister.deutschcraft.core.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.julesmeister.deutschcraft.core.database.entity.besser.BesserChallengeEntity
import com.julesmeister.deutschcraft.core.database.entity.besser.BesserChallengeHintEntity

data class BesserChallengeWithHints(
    @Embedded
    val challenge: BesserChallengeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "challenge_id"
    )
    val hints: List<BesserChallengeHintEntity>
) {

    val orderedHints: List<BesserChallengeHintEntity>
        get() =
            hints.sortedBy(
                BesserChallengeHintEntity::position
            )
}