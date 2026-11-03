// app/src/main/java/com/julesmeister/deutschcraft/core/database/DeutschCraftDatabase.kt

package com.julesmeister.deutschcraft.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.julesmeister.deutschcraft.core.database.dao.ActivityDayDao
import com.julesmeister.deutschcraft.core.database.dao.AdaptiveLearningDao
import com.julesmeister.deutschcraft.core.database.dao.AdaptiveLearningMistakeDao
import com.julesmeister.deutschcraft.core.database.dao.AssessmentDao
import com.julesmeister.deutschcraft.core.database.dao.BesserChallengeDao
import com.julesmeister.deutschcraft.core.database.dao.CapabilityDao
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.LearnerProfileDao
import com.julesmeister.deutschcraft.core.database.dao.MarkedPhraseDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.dao.MissionAnalyticsDao
import com.julesmeister.deutschcraft.core.database.dao.MissionCapabilityEvidenceDao
import com.julesmeister.deutschcraft.core.database.dao.MissionDao
import com.julesmeister.deutschcraft.core.database.dao.MissionInteractionEvidenceDao
import com.julesmeister.deutschcraft.core.database.dao.MissionObjectiveHintUsageDao
import com.julesmeister.deutschcraft.core.database.dao.MissionThreadRecommendationDao
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
import com.julesmeister.deutschcraft.core.database.entity.besser.BesserChallengeEntity
import com.julesmeister.deutschcraft.core.database.entity.besser.BesserChallengeHintEntity
import com.julesmeister.deutschcraft.core.database.entity.besser.CapabilityEntity
import com.julesmeister.deutschcraft.core.database.entity.besser.MissionCapabilityEvidenceEntity
import com.julesmeister.deutschcraft.core.database.entity.learning.MarkedPhraseEntity
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionAnalyticsEntity
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionInteractionEvidenceEntity
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionObjectiveAnalyticsEntity
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionObjectiveHintUsageEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.ConversationMissionEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveProgressEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionTemplateEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionThreadRecommendationEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionScenarioCompletionEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionThreadRecommendationObjectiveEntity

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

        MissionTemplateEntity::class,
        MissionObjectiveEntity::class,
        ConversationMissionEntity::class,
        MissionObjectiveProgressEntity::class,

        MissionThreadRecommendationEntity::class,
        MissionScenarioCompletionEntity::class,
        MissionThreadRecommendationObjectiveEntity::class,

        MissionAnalyticsEntity::class,
        MissionObjectiveAnalyticsEntity::class,
        MissionObjectiveHintUsageEntity::class,
        MissionInteractionEvidenceEntity::class,

        AdaptiveLearningFocusEntity::class,
        AdaptiveLearningEvidenceEntity::class,

        MarkedPhraseEntity::class,

        CapabilityEntity::class,
        BesserChallengeEntity::class,
        BesserChallengeHintEntity::class,
        MissionCapabilityEvidenceEntity::class
    ],
    version = 19,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 14,
            to = 15
        ),
        AutoMigration(
            from = 15,
            to = 16
        ),
        AutoMigration(
            from = 16,
            to = 17
        ),
        AutoMigration(
            from = 17,
            to = 18
        )
    ]
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

    abstract fun missionThreadRecommendationDao():
            MissionThreadRecommendationDao

    abstract fun missionAnalyticsDao():
            MissionAnalyticsDao

    abstract fun missionObjectiveHintUsageDao():
            MissionObjectiveHintUsageDao

    abstract fun missionInteractionEvidenceDao():
            MissionInteractionEvidenceDao

    abstract fun adaptiveLearningDao():
            AdaptiveLearningDao

    abstract fun adaptiveLearningMistakeDao():
            AdaptiveLearningMistakeDao

    abstract fun markedPhraseDao():
            MarkedPhraseDao

    abstract fun capabilityDao():
            CapabilityDao

    abstract fun besserChallengeDao():
            BesserChallengeDao

    abstract fun missionCapabilityEvidenceDao():
            MissionCapabilityEvidenceDao
}