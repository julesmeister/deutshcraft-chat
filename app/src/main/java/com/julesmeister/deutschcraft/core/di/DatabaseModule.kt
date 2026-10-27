package com.julesmeister.deutschcraft.core.di

import android.content.Context
import androidx.room.Room
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
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
import com.julesmeister.deutschcraft.core.database.migration.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
object DatabaseModule {

    /*
     * Main DeutschCraft Room database.
     *
     * DatabaseMigrations.ALL contains the complete migration
     * chain currently supported by the application.
     *
     * Current schema version:
     *
     * 13
     */
    @Provides
    @Singleton
    fun provideDeutschCraftDatabase(
        @ApplicationContext
        context: Context
    ): DeutschCraftDatabase {
        return Room.databaseBuilder(
            context,
            DeutschCraftDatabase::class.java,
            DATABASE_NAME
        )
            /*
             * Current migration chain:
             *
             * 5 -> 6
             * 6 -> 7
             * 7 -> 8
             * 8 -> 9
             * 9 -> 10
             * 10 -> 11
             * 11 -> 12
             * 12 -> 13
             *
             * Future migrations only need to be added to:
             *
             * DatabaseMigrations.ALL
             */
            .addMigrations(
                *DatabaseMigrations.ALL
            )
            .build()
    }

    /*
     * =======================================================
     * CONVERSATION AND CHAT
     * =======================================================
     */

    @Provides
    fun provideConversationDao(
        database:
        DeutschCraftDatabase
    ): ConversationDao {
        return database
            .conversationDao()
    }

    /*
     * Handles persisted conversation messages.
     *
     * Mission analytics also uses this DAO to retrieve learner
     * messages from a mission's active time window.
     */
    @Provides
    fun provideMessageDao(
        database:
        DeutschCraftDatabase
    ): MessageDao {
        return database
            .messageDao()
    }

    /*
     * =======================================================
     * LEARNER ASSESSMENT
     * =======================================================
     */

    @Provides
    fun provideLearnerProfileDao(
        database:
        DeutschCraftDatabase
    ): LearnerProfileDao {
        return database
            .learnerProfileDao()
    }

    @Provides
    fun provideAssessmentDao(
        database:
        DeutschCraftDatabase
    ): AssessmentDao {
        return database
            .assessmentDao()
    }

    /*
     * Learning activity and streak tracking.
     */
    @Provides
    fun provideActivityDayDao(
        database:
        DeutschCraftDatabase
    ): ActivityDayDao {
        return database
            .activityDayDao()
    }

    /*
     * =======================================================
     * MISSION SYSTEM
     * =======================================================
     */

    /*
     * Handles:
     *
     * - mission templates
     * - mission objectives
     * - active conversation missions
     * - objective progress
     */
    @Provides
    fun provideMissionDao(
        database:
        DeutschCraftDatabase
    ): MissionDao {
        return database
            .missionDao()
    }

    /*
     * Handles the persisted thread-list recommendation pool.
     */
    @Provides
    fun provideMissionThreadRecommendationDao(
        database:
        DeutschCraftDatabase
    ): MissionThreadRecommendationDao {
        return database
            .missionThreadRecommendationDao()
    }

    /*
     * =======================================================
     * MISSION LEARNING ANALYTICS
     * =======================================================
     */

    /*
     * Stores finalized mission-performance analytics.
     */
    @Provides
    fun provideMissionAnalyticsDao(
        database:
        DeutschCraftDatabase
    ): MissionAnalyticsDao {
        return database
            .missionAnalyticsDao()
    }

    /*
     * Stores coaching hints actually revealed by the learner.
     */
    @Provides
    fun provideMissionObjectiveHintUsageDao(
        database:
        DeutschCraftDatabase
    ): MissionObjectiveHintUsageDao {
        return database
            .missionObjectiveHintUsageDao()
    }

    /*
     * Stores semantic interaction evidence for each evaluated
     * learner message during a mission.
     */
    @Provides
    fun provideMissionInteractionEvidenceDao(
        database:
        DeutschCraftDatabase
    ): MissionInteractionEvidenceDao {
        return database
            .missionInteractionEvidenceDao()
    }

    /*
     * =======================================================
     * ADAPTIVE LEARNING
     * =======================================================
     */

    /*
     * Persistent adaptive-learning focuses and evidence.
     */
    @Provides
    fun provideAdaptiveLearningDao(
        database:
        DeutschCraftDatabase
    ): AdaptiveLearningDao {
        return database
            .adaptiveLearningDao()
    }

    /*
     * Reads DetectedMistakeEntity and its assessment evidence.
     */
    @Provides
    fun provideAdaptiveLearningMistakeDao(
        database:
        DeutschCraftDatabase
    ): AdaptiveLearningMistakeDao {
        return database
            .adaptiveLearningMistakeDao()
    }

    /*
     * =======================================================
     * MARKED PHRASES
     * =======================================================
     */

    /*
     * Learner-highlighted difficult words and grammar.
     *
     * Used by MarkedPhraseRepository to show explanation
     * bubbles under their original message and, later, to
     * bring weak points back into practice.
     */
    @Provides
    fun provideMarkedPhraseDao(
        database:
        DeutschCraftDatabase
    ): MarkedPhraseDao {
        return database
            .markedPhraseDao()
    }

    private const val DATABASE_NAME =
        "deutschcraft.db"
}