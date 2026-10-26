package com.julesmeister.deutschcraft.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.julesmeister.deutschcraft.core.database.dao.ActivityDayDao
import com.julesmeister.deutschcraft.core.database.dao.AdaptiveLearningDao
import com.julesmeister.deutschcraft.core.database.dao.AssessmentDao
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.LearnerProfileDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.dao.MissionDao
import com.julesmeister.deutschcraft.core.database.entity.ActivityDayEntity
import com.julesmeister.deutschcraft.core.database.entity.AssessmentEvidenceEntity
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.DetectedMistakeEntity
import com.julesmeister.deutschcraft.core.database.entity.EvidenceGrammarFeatureEntity
import com.julesmeister.deutschcraft.core.database.entity.LearnerProfileEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
import com.julesmeister.deutschcraft.core.database.entity.SkillScoreEntity
import com.julesmeister.deutschcraft.core.database.entity.adaptivelearning.AdaptiveLearningEvidenceEntity
import com.julesmeister.deutschcraft.core.database.entity.adaptivelearning.AdaptiveLearningFocusEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.ConversationMissionEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveProgressEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionTemplateEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        LearnerProfileEntity::class,
        SkillScoreEntity::class,
        AssessmentEvidenceEntity::class,
        EvidenceGrammarFeatureEntity::class,
        DetectedMistakeEntity::class,
        ActivityDayEntity::class,

        /*
         * Mission system entities.
         */
        MissionTemplateEntity::class,
        MissionObjectiveEntity::class,
        ConversationMissionEntity::class,
        MissionObjectiveProgressEntity::class,

        /*
         * Persistent adaptive-learning memory.
         */
        AdaptiveLearningFocusEntity::class,
        AdaptiveLearningEvidenceEntity::class
    ],
    version =
        8,
    exportSchema =
        true
)
abstract class DeutschCraftDatabase :
    RoomDatabase() {

    abstract fun conversationDao():
            ConversationDao

    abstract fun messageDao():
            MessageDao

    abstract fun learnerProfileDao():
            LearnerProfileDao

    abstract fun assessmentDao():
            AssessmentDao

    abstract fun activityDayDao():
            ActivityDayDao

    abstract fun missionDao():
            MissionDao

    abstract fun adaptiveLearningDao():
            AdaptiveLearningDao
}