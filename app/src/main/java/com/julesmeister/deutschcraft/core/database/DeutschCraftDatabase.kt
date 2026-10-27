package com.julesmeister.deutschcraft.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.julesmeister.deutschcraft.core.database.dao.ActivityDayDao
import com.julesmeister.deutschcraft.core.database.dao.AdaptiveLearningDao
import com.julesmeister.deutschcraft.core.database.dao.AdaptiveLearningMistakeDao
import com.julesmeister.deutschcraft.core.database.dao.AssessmentDao
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.LearnerProfileDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.dao.MissionAnalyticsDao
import com.julesmeister.deutschcraft.core.database.dao.MissionDao
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
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionAnalyticsEntity
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionObjectiveAnalyticsEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.ConversationMissionEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveProgressEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionTemplateEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionThreadRecommendationEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionThreadRecommendationObjectiveEntity

@Database(
    entities = [
        /*
         * Conversation and chat entities.
         */
        ConversationEntity::class,
        MessageEntity::class,

        /*
         * Learner assessment and CEFR progress entities.
         */
        LearnerProfileEntity::class,
        SkillScoreEntity::class,
        AssessmentEvidenceEntity::class,
        EvidenceGrammarFeatureEntity::class,
        DetectedMistakeEntity::class,
        ActivityDayEntity::class,

        /*
         * Mission-system entities.
         *
         * These represent real mission templates, active
         * conversation missions, and objective progress.
         */
        MissionTemplateEntity::class,
        MissionObjectiveEntity::class,
        ConversationMissionEntity::class,
        MissionObjectiveProgressEntity::class,

        /*
         * Persisted featured mission recommendations.
         *
         * These are generated recommendation drafts waiting
         * in the thread-list recommendation pool.
         *
         * They remain separate from ConversationMissionEntity
         * until the learner actually starts one.
         */
        MissionThreadRecommendationEntity::class,
        MissionThreadRecommendationObjectiveEntity::class,

        /*
         * Mission-learning analytics.
         *
         * MissionAnalyticsEntity stores the overall result of
         * a mission attempt.
         *
         * MissionObjectiveAnalyticsEntity stores the
         * objective-level evidence used to understand:
         *
         * - independent completion
         * - hint dependency
         * - Besser corrections
         * - vocabulary usage
         * - objective confidence
         *
         * These records will later feed CEFR mastery and
         * next-level readiness.
         */
        MissionAnalyticsEntity::class,
        MissionObjectiveAnalyticsEntity::class,

        /*
         * Persistent adaptive-learning memory.
         */
        AdaptiveLearningFocusEntity::class,
        AdaptiveLearningEvidenceEntity::class
    ],
    version =
        10,
    exportSchema =
        true
)
abstract class DeutschCraftDatabase :
    RoomDatabase() {

    /*
     * Conversations and chat.
     */
    abstract fun conversationDao():
            ConversationDao

    abstract fun messageDao():
            MessageDao

    /*
     * Learner profile and CEFR assessment.
     */
    abstract fun learnerProfileDao():
            LearnerProfileDao

    abstract fun assessmentDao():
            AssessmentDao

    /*
     * Learning activity and streak tracking.
     */
    abstract fun activityDayDao():
            ActivityDayDao

    /*
     * Real mission templates, conversation missions, and
     * objective progress.
     */
    abstract fun missionDao():
            MissionDao

    /*
     * Persisted mission-performance analytics.
     *
     * These analytics later feed:
     *
     * - Mission result summaries
     * - Besser learning feedback
     * - CEFR mastery
     * - Next-level readiness
     */
    abstract fun missionAnalyticsDao():
            MissionAnalyticsDao

    /*
     * Persisted thread-list recommendation drafts.
     *
     * Provides the five-item recommendation pool so mission
     * suggestions can survive process death and app restarts.
     */
    abstract fun missionThreadRecommendationDao():
            MissionThreadRecommendationDao

    /*
     * Persistent adaptive-learning focuses and evidence.
     */
    abstract fun adaptiveLearningDao():
            AdaptiveLearningDao

    /*
     * Reads detected mistakes already persisted by the
     * assessment system.
     *
     * This DAO does not own another table.
     */
    abstract fun adaptiveLearningMistakeDao():
            AdaptiveLearningMistakeDao
}