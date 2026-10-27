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
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.dao.MissionAnalyticsDao
import com.julesmeister.deutschcraft.core.database.dao.MissionDao
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
             * DatabaseMigrations.ALL currently contains:
             *
             * 5 -> 6
             * 6 -> 7
             * 7 -> 8
             * 8 -> 9
             * 9 -> 10
             *
             * Future migrations only need to be added to the
             * ALL array in DatabaseMigrations.
             */
            .addMigrations(
                *DatabaseMigrations.ALL
            )
            .build()
    }

    @Provides
    fun provideConversationDao(
        database:
        DeutschCraftDatabase
    ): ConversationDao {
        return database
            .conversationDao()
    }

    @Provides
    fun provideMessageDao(
        database:
        DeutschCraftDatabase
    ): MessageDao {
        return database
            .messageDao()
    }

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

    @Provides
    fun provideActivityDayDao(
        database:
        DeutschCraftDatabase
    ): ActivityDayDao {
        return database
            .activityDayDao()
    }

    @Provides
    fun provideMissionDao(
        database:
        DeutschCraftDatabase
    ): MissionDao {
        return database
            .missionDao()
    }

    /*
     * Generated and persisted featured mission
     * recommendations.
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
     * Mission-performance analytics used by mission results
     * and future CEFR progress calculations.
     */
    @Provides
    fun provideMissionAnalyticsDao(
        database:
        DeutschCraftDatabase
    ): MissionAnalyticsDao {
        return database
            .missionAnalyticsDao()
    }

    @Provides
    fun provideAdaptiveLearningDao(
        database:
        DeutschCraftDatabase
    ): AdaptiveLearningDao {
        return database
            .adaptiveLearningDao()
    }

    @Provides
    fun provideAdaptiveLearningMistakeDao(
        database:
        DeutschCraftDatabase
    ): AdaptiveLearningMistakeDao {
        return database
            .adaptiveLearningMistakeDao()
    }

    private const val DATABASE_NAME =
        "deutschcraft.db"
}