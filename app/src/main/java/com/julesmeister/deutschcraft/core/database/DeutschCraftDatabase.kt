package com.julesmeister.deutschcraft.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.julesmeister.deutschcraft.core.database.dao.ActivityDayDao
import com.julesmeister.deutschcraft.core.database.dao.AdaptiveLearningDao
import com.julesmeister.deutschcraft.core.database.dao.AdaptiveLearningMistakeDao
import com.julesmeister.deutschcraft.core.database.dao.AssessmentDao
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.LearnerProfileDao
import com.julesmeister.deutschcraft.core.database.dao.MarkedPhraseDao
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
         */

        MissionTemplateEntity::class,
        MissionObjectiveEntity::class,
        ConversationMissionEntity::class,
        MissionObjectiveProgressEntity::class,

        /*
         * ===================================================
         * Mission recommendation persistence
         * ===================================================
         */

        MissionThreadRecommendationEntity::class,
        MissionThreadRecommendationObjectiveEntity::class,

        /*
         * ===================================================
         * Mission learning analytics
         * ===================================================
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
        AdaptiveLearningEvidenceEntity::class,

        /*
         * ===================================================
         * Learner-marked phrases
         * ===================================================
         *
         * Words and grammar the learner highlighted as
         * difficult, with their AI explanation.
         */

        MarkedPhraseEntity::class
    ],
    version =
        13,
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

    abstract fun missionDao():
            MissionDao

    /*
     * =======================================================
     * Mission recommendation persistence
     * =======================================================
     */

    abstract fun missionThreadRecommendationDao():
            MissionThreadRecommendationDao

    /*
     * =======================================================
     * Mission learning analytics
     * =======================================================
     */

    abstract fun missionAnalyticsDao():
            MissionAnalyticsDao

    abstract fun missionObjectiveHintUsageDao():
            MissionObjectiveHintUsageDao

    abstract fun missionInteractionEvidenceDao():
            MissionInteractionEvidenceDao

    /*
     * =======================================================
     * Adaptive learning
     * =======================================================
     */

    abstract fun adaptiveLearningDao():
            AdaptiveLearningDao

    abstract fun adaptiveLearningMistakeDao():
            AdaptiveLearningMistakeDao

    /*
     * =======================================================
     * Marked phrases
     * =======================================================
     *
     * Learner-highlighted difficult words and grammar,
     * shown as explanation bubbles under their message and
     * later reused for practice.
     */
    abstract fun markedPhraseDao():
            MarkedPhraseDao
}