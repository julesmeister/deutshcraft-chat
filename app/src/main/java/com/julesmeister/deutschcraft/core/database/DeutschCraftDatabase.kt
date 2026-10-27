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
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionAnalyticsEntity
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionInteractionEvidenceEntity
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionObjectiveAnalyticsEntity
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionObjectiveHintUsageEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.ConversationMissionEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveProgressEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionTemplateEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionThreadRecommendationEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionThreadRecommendationObjectiveEntity

@Database(
    entities = [
        /*
         * ===================================================
         * Conversation and chat
         * ===================================================
         */

        ConversationEntity::class,
        MessageEntity::class,

        /*
         * ===================================================
         * Learner assessment and CEFR evidence
         * ===================================================
         */

        LearnerProfileEntity::class,
        SkillScoreEntity::class,
        AssessmentEvidenceEntity::class,
        EvidenceGrammarFeatureEntity::class,
        DetectedMistakeEntity::class,
        ActivityDayEntity::class,

        /*
         * ===================================================
         * Mission system
         * ===================================================
         *
         * These represent:
         *
         * - reusable mission templates
         * - objective definitions
         * - conversation-specific mission attempts
         * - objective completion progress
         */

        MissionTemplateEntity::class,
        MissionObjectiveEntity::class,
        ConversationMissionEntity::class,
        MissionObjectiveProgressEntity::class,

        /*
         * ===================================================
         * Mission recommendation persistence
         * ===================================================
         *
         * Generated recommendation drafts remain separate
         * from real ConversationMission records until the
         * learner starts them.
         */

        MissionThreadRecommendationEntity::class,
        MissionThreadRecommendationObjectiveEntity::class,

        /*
         * ===================================================
         * Mission learning analytics
         * ===================================================
         *
         * MissionAnalyticsEntity
         *
         * Stores the finalized mission-level scores.
         *
         *
         * MissionObjectiveAnalyticsEntity
         *
         * Stores the final evidence for each mission
         * objective.
         *
         *
         * MissionObjectiveHintUsageEntity
         *
         * Stores coaching hints actually revealed to the
         * learner.
         *
         *
         * MissionInteractionEvidenceEntity
         *
         * Stores semantic interaction/comprehension evidence
         * for every learner message evaluated during a
         * mission.
         */

        MissionAnalyticsEntity::class,
        MissionObjectiveAnalyticsEntity::class,
        MissionObjectiveHintUsageEntity::class,
        MissionInteractionEvidenceEntity::class,

        /*
         * ===================================================
         * Adaptive-learning memory
         * ===================================================
         */

        AdaptiveLearningFocusEntity::class,
        AdaptiveLearningEvidenceEntity::class
    ],
    version =
        12,
    exportSchema =
        true
)
abstract class DeutschCraftDatabase :
    RoomDatabase() {

    /*
     * =======================================================
     * Conversation and chat
     * =======================================================
     */

    abstract fun conversationDao():
            ConversationDao

    abstract fun messageDao():
            MessageDao

    /*
     * =======================================================
     * Learner profile and CEFR assessment
     * =======================================================
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
     * =======================================================
     * Mission system
     * =======================================================
     */

    /*
     * Handles:
     *
     * - mission templates
     * - mission objectives
     * - conversation missions
     * - objective progress
     */
    abstract fun missionDao():
            MissionDao

    /*
     * =======================================================
     * Mission recommendation persistence
     * =======================================================
     */

    /*
     * Stores the persisted thread-list mission
     * recommendation pool.
     */
    abstract fun missionThreadRecommendationDao():
            MissionThreadRecommendationDao

    /*
     * =======================================================
     * Mission learning analytics
     * =======================================================
     */

    /*
     * Stores finalized mission-level and objective-level
     * analytics.
     *
     * Includes:
     *
     * - overall score
     * - independence
     * - grammar
     * - vocabulary
     * - comprehension
     * - interaction
     *
     * These records later feed CEFR mastery and next-level
     * readiness.
     */
    abstract fun missionAnalyticsDao():
            MissionAnalyticsDao

    /*
     * Stores every coaching hint that was actually revealed
     * to the learner.
     *
     * Assistance interpretation:
     *
     * 0 hints
     * Independent
     *
     * 1 hint
     * Gentle support
     *
     * 2 hints
     * Contextual/substantial support
     *
     * 3 hints
     * Direct assistance
     */
    abstract fun missionObjectiveHintUsageDao():
            MissionObjectiveHintUsageDao

    /*
     * Stores interaction/comprehension evidence returned by
     * the existing Qwen mission evaluation request.
     *
     * One record belongs to one learner message and contains:
     *
     * - relevantReply
     * - followUpQuestion
     * - clarification
     * - replyExpected
     * - confidence
     *
     * These records are accumulated across the mission when
     * calculating final interaction and comprehension scores.
     */
    abstract fun missionInteractionEvidenceDao():
            MissionInteractionEvidenceDao

    /*
     * =======================================================
     * Adaptive learning
     * =======================================================
     */

    /*
     * Persistent adaptive-learning focuses and evidence.
     */
    abstract fun adaptiveLearningDao():
            AdaptiveLearningDao

    /*
     * Reads DetectedMistakeEntity and related assessment
     * records.
     *
     * This DAO does not own another Room table.
     *
     * Adaptive learning and mission Besser analytics both
     * consume the existing assessment evidence.
     */
    abstract fun adaptiveLearningMistakeDao():
            AdaptiveLearningMistakeDao
}