package com.julesmeister.deutschcraft.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/*
 * Complete known DeutschCraft database migration chain.
 *
 * Current database version:
 *
 * 12
 *
 * Known migration history:
 *
 * 5 -> 6
 * 6 -> 7
 * 7 -> 8
 * 8 -> 9
 * 9 -> 10
 * 10 -> 11
 * 11 -> 12
 *
 * Future migrations should continue to be added here and
 * appended to ALL.
 */
object DatabaseMigrations {

    /*
     * IMPORTANT:
     *
     * The project code supplied for 5 -> 6 did not contain
     * migration SQL.
     *
     * If the original project contained real 5 -> 6 SQL,
     * restore those statements here.
     */
    val MIGRATION_5_6 =
        object : Migration(
            startVersion =
                5,
            endVersion =
                6
        ) {

            override fun migrate(
                database:
                SupportSQLiteDatabase
            ) {
                /*
                 * Restore the original 5 -> 6 SQL here if
                 * version 6 changed the database schema.
                 */
            }
        }

    /*
     * Version 7 added learner-support content to mission
     * objectives.
     */
    val MIGRATION_6_7 =
        object : Migration(
            startVersion =
                6,
            endVersion =
                7
        ) {

            override fun migrate(
                database:
                SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    ALTER TABLE mission_objectives
                    ADD COLUMN coachingHintsJson TEXT
                    NOT NULL DEFAULT '[]'
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE mission_objectives
                    ADD COLUMN targetVocabularyJson TEXT
                    NOT NULL DEFAULT '[]'
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE mission_objectives
                    ADD COLUMN examplePhrasesJson TEXT
                    NOT NULL DEFAULT '[]'
                    """.trimIndent()
                )
            }
        }

    /*
     * Version 8 introduced persistent adaptive-learning
     * focuses and message-level evidence.
     */
    val MIGRATION_7_8 =
        object : Migration(
            startVersion =
                7,
            endVersion =
                8
        ) {

            override fun migrate(
                database:
                SupportSQLiteDatabase
            ) {
                createAdaptiveLearningFocusTable(
                    database =
                        database
                )

                createAdaptiveLearningFocusIndices(
                    database =
                        database
                )

                createAdaptiveLearningEvidenceTable(
                    database =
                        database
                )

                createAdaptiveLearningEvidenceIndices(
                    database =
                        database
                )
            }
        }

    /*
     * Version 9 introduced persistent thread-list mission
     * recommendations.
     */
    val MIGRATION_8_9 =
        object : Migration(
            startVersion =
                8,
            endVersion =
                9
        ) {

            override fun migrate(
                database:
                SupportSQLiteDatabase
            ) {
                createMissionRecommendationTable(
                    database =
                        database
                )

                createMissionRecommendationIndices(
                    database =
                        database
                )

                createMissionRecommendationObjectiveTable(
                    database =
                        database
                )

                createMissionRecommendationObjectiveIndices(
                    database =
                        database
                )
            }
        }

    /*
     * Version 10 introduced persistent mission analytics.
     */
    val MIGRATION_9_10 =
        object : Migration(
            startVersion =
                9,
            endVersion =
                10
        ) {

            override fun migrate(
                database:
                SupportSQLiteDatabase
            ) {
                createMissionAnalyticsTable(
                    database =
                        database
                )

                createMissionAnalyticsIndices(
                    database =
                        database
                )

                createMissionObjectiveAnalyticsTable(
                    database =
                        database
                )

                createMissionObjectiveAnalyticsIndices(
                    database =
                        database
                )
            }
        }

    /*
     * Version 11 introduced persistent mission-objective hint
     * usage.
     */
    val MIGRATION_10_11 =
        object : Migration(
            startVersion =
                10,
            endVersion =
                11
        ) {

            override fun migrate(
                database:
                SupportSQLiteDatabase
            ) {
                createMissionObjectiveHintUsageTable(
                    database =
                        database
                )

                createMissionObjectiveHintUsageIndices(
                    database =
                        database
                )
            }
        }

    /*
     * Version 12 introduces persisted per-message mission
     * interaction evidence.
     *
     * This stores the semantic interaction classification
     * returned by the existing Qwen mission-evaluation call.
     */
    val MIGRATION_11_12 =
        object : Migration(
            startVersion =
                11,
            endVersion =
                12
        ) {

            override fun migrate(
                database:
                SupportSQLiteDatabase
            ) {
                createMissionInteractionEvidenceTable(
                    database =
                        database
                )

                createMissionInteractionEvidenceIndices(
                    database =
                        database
                )
            }
        }

    /*
     * Complete migration chain consumed by DatabaseModule:
     *
     * .addMigrations(
     *     *DatabaseMigrations.ALL
     * )
     */
    val ALL =
        arrayOf(
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12
        )

    /*
     * =======================================================
     * VERSION 7 -> 8
     * ADAPTIVE LEARNING
     * =======================================================
     */

    private fun createAdaptiveLearningFocusTable(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS
            adaptive_learning_focuses
            (
                id TEXT NOT NULL,
                focusKey TEXT NOT NULL,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                targetCefrLevel TEXT NOT NULL,
                communicationSkill TEXT,
                masteryState TEXT NOT NULL,
                evidenceCount INTEGER NOT NULL,
                struggleCount INTEGER NOT NULL,
                successCount INTEGER NOT NULL,
                consecutiveSuccessCount INTEGER NOT NULL,
                confidence REAL NOT NULL,
                masteryScore REAL NOT NULL,
                reinforcementInstruction TEXT NOT NULL,
                learnerGuidance TEXT NOT NULL,
                firstObservedAt INTEGER NOT NULL,
                lastObservedAt INTEGER NOT NULL,
                lastPracticedAt INTEGER,
                masteredAt INTEGER,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
    }

    private fun createAdaptiveLearningFocusIndices(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
            index_adaptive_learning_focuses_focusKey
            ON adaptive_learning_focuses(focusKey)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            index_adaptive_learning_focuses_type
            ON adaptive_learning_focuses(type)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            index_adaptive_learning_focuses_masteryState
            ON adaptive_learning_focuses(masteryState)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            index_adaptive_learning_focuses_targetCefrLevel
            ON adaptive_learning_focuses(targetCefrLevel)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            index_adaptive_learning_focuses_lastObservedAt
            ON adaptive_learning_focuses(lastObservedAt)
            """.trimIndent()
        )
    }

    private fun createAdaptiveLearningEvidenceTable(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS
            adaptive_learning_evidence
            (
                id TEXT NOT NULL,
                focusId TEXT NOT NULL,
                messageId TEXT NOT NULL,
                conversationId TEXT,
                result TEXT NOT NULL,
                confidence REAL NOT NULL,
                reason TEXT NOT NULL,
                learnerText TEXT NOT NULL,
                correctedText TEXT,
                levelAppropriateText TEXT,
                observedAt INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                PRIMARY KEY(id),
                FOREIGN KEY(focusId)
                    REFERENCES adaptive_learning_focuses(id)
                    ON UPDATE NO ACTION
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    private fun createAdaptiveLearningEvidenceIndices(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            index_adaptive_learning_evidence_focusId
            ON adaptive_learning_evidence(focusId)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            index_adaptive_learning_evidence_messageId
            ON adaptive_learning_evidence(messageId)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
            index_adaptive_learning_evidence_focusId_messageId
            ON adaptive_learning_evidence(
                focusId,
                messageId
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            index_adaptive_learning_evidence_result
            ON adaptive_learning_evidence(result)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            index_adaptive_learning_evidence_observedAt
            ON adaptive_learning_evidence(observedAt)
            """.trimIndent()
        )
    }

    /*
     * =======================================================
     * VERSION 8 -> 9
     * MISSION RECOMMENDATIONS
     * =======================================================
     */

    private fun createMissionRecommendationTable(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS
            `mission_thread_recommendations`
            (
                `id` TEXT NOT NULL,
                `scenarioType` TEXT NOT NULL,
                `cefrLevel` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `previewText` TEXT NOT NULL,
                `confidence` REAL NOT NULL,
                `reason` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `scenario` TEXT NOT NULL,
                `learnerRole` TEXT NOT NULL,
                `assistantRole` TEXT NOT NULL,
                `icon` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `displayedAt` INTEGER,
                `skippedAt` INTEGER,
                `startedAt` INTEGER,
                `completedAt` INTEGER,
                `expiresAt` INTEGER,
                `conversationId` TEXT,
                `conversationMissionId` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }

    private fun createMissionRecommendationIndices(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_mission_thread_recommendations_cefrLevel_status_createdAt`
            ON `mission_thread_recommendations`
            (
                `cefrLevel`,
                `status`,
                `createdAt`
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_mission_thread_recommendations_scenarioType`
            ON `mission_thread_recommendations`
            (
                `scenarioType`
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_mission_thread_recommendations_status_expiresAt`
            ON `mission_thread_recommendations`
            (
                `status`,
                `expiresAt`
            )
            """.trimIndent()
        )
    }

    private fun createMissionRecommendationObjectiveTable(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS
            `mission_thread_recommendation_objectives`
            (
                `id` TEXT NOT NULL,
                `recommendationId` TEXT NOT NULL,
                `sequenceNumber` INTEGER NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `completionHint` TEXT NOT NULL,
                `coachingHintsJson` TEXT NOT NULL,
                `targetVocabularyJson` TEXT NOT NULL,
                `examplePhrasesJson` TEXT NOT NULL,
                `isRequired` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`recommendationId`)
                    REFERENCES
                    `mission_thread_recommendations`(`id`)
                    ON UPDATE NO ACTION
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    private fun createMissionRecommendationObjectiveIndices(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_mission_thread_recommendation_objectives_recommendationId`
            ON `mission_thread_recommendation_objectives`
            (
                `recommendationId`
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
            `index_mission_thread_recommendation_objectives_recommendationId_sequenceNumber`
            ON `mission_thread_recommendation_objectives`
            (
                `recommendationId`,
                `sequenceNumber`
            )
            """.trimIndent()
        )
    }

    /*
     * =======================================================
     * VERSION 9 -> 10
     * MISSION ANALYTICS
     * =======================================================
     */

    private fun createMissionAnalyticsTable(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS
            `mission_analytics`
            (
                `missionId` TEXT NOT NULL,
                `conversationId` TEXT NOT NULL,
                `cefrLevel` TEXT NOT NULL,
                `completedObjectiveCount` INTEGER NOT NULL,
                `totalObjectiveCount` INTEGER NOT NULL,
                `completedRequiredObjectiveCount` INTEGER NOT NULL,
                `totalRequiredObjectiveCount` INTEGER NOT NULL,
                `overallScore` INTEGER NOT NULL,
                `independenceScore` INTEGER NOT NULL,
                `grammarScore` INTEGER NOT NULL,
                `vocabularyScore` INTEGER NOT NULL,
                `comprehensionScore` INTEGER NOT NULL,
                `interactionScore` INTEGER NOT NULL,
                `hintsUsed` INTEGER NOT NULL,
                `besserCorrectionCount` INTEGER NOT NULL,
                `resolvedBesserCorrectionCount` INTEGER NOT NULL,
                `independentlyCompletedObjectiveCount` INTEGER NOT NULL,
                `supportedObjectiveCount` INTEGER NOT NULL,
                `directlyAssistedObjectiveCount` INTEGER NOT NULL,
                `conversationScore` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `completedAt` INTEGER NOT NULL,
                PRIMARY KEY(`missionId`)
            )
            """.trimIndent()
        )
    }

    private fun createMissionAnalyticsIndices(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_mission_analytics_conversationId`
            ON `mission_analytics`
            (
                `conversationId`
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_mission_analytics_cefrLevel_completedAt`
            ON `mission_analytics`
            (
                `cefrLevel`,
                `completedAt`
            )
            """.trimIndent()
        )
    }

    private fun createMissionObjectiveAnalyticsTable(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS
            `mission_objective_analytics`
            (
                `missionId` TEXT NOT NULL,
                `objectiveId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `isRequired` INTEGER NOT NULL,
                `wasCompleted` INTEGER NOT NULL,
                `hintsUsed` INTEGER NOT NULL,
                `besserCorrectionCount` INTEGER NOT NULL,
                `evaluationConfidence` REAL NOT NULL,
                `targetVocabularyJson` TEXT NOT NULL,
                `correctlyUsedVocabularyJson` TEXT NOT NULL,
                PRIMARY KEY(
                    `missionId`,
                    `objectiveId`
                ),
                FOREIGN KEY(`missionId`)
                    REFERENCES
                    `mission_analytics`(`missionId`)
                    ON UPDATE NO ACTION
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    private fun createMissionObjectiveAnalyticsIndices(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_mission_objective_analytics_missionId`
            ON `mission_objective_analytics`
            (
                `missionId`
            )
            """.trimIndent()
        )
    }

    /*
     * =======================================================
     * VERSION 10 -> 11
     * MISSION OBJECTIVE HINT USAGE
     * =======================================================
     */

    private fun createMissionObjectiveHintUsageTable(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS
            `mission_objective_hint_usage`
            (
                `conversationMissionId` TEXT NOT NULL,
                `objectiveId` TEXT NOT NULL,
                `hintLevel` INTEGER NOT NULL,
                `hintText` TEXT NOT NULL,
                `revealedAt` INTEGER NOT NULL,
                PRIMARY KEY(
                    `conversationMissionId`,
                    `objectiveId`,
                    `hintLevel`
                )
            )
            """.trimIndent()
        )
    }

    private fun createMissionObjectiveHintUsageIndices(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_mission_objective_hint_usage_conversationMissionId`
            ON `mission_objective_hint_usage`
            (
                `conversationMissionId`
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_mission_objective_hint_usage_conversationMissionId_objectiveId`
            ON `mission_objective_hint_usage`
            (
                `conversationMissionId`,
                `objectiveId`
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_mission_objective_hint_usage_revealedAt`
            ON `mission_objective_hint_usage`
            (
                `revealedAt`
            )
            """.trimIndent()
        )
    }

    /*
     * =======================================================
     * VERSION 11 -> 12
     * MISSION INTERACTION EVIDENCE
     * =======================================================
     */

    private fun createMissionInteractionEvidenceTable(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS
            `mission_interaction_evidence`
            (
                `conversationMissionId` TEXT NOT NULL,
                `messageId` TEXT NOT NULL,
                `relevantReply` INTEGER NOT NULL,
                `followUpQuestion` INTEGER NOT NULL,
                `clarification` INTEGER NOT NULL,
                `replyExpected` INTEGER NOT NULL,
                `confidence` REAL NOT NULL,
                `evaluatedAt` INTEGER NOT NULL,
                PRIMARY KEY(
                    `conversationMissionId`,
                    `messageId`
                )
            )
            """.trimIndent()
        )
    }

    private fun createMissionInteractionEvidenceIndices(
        database:
        SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_mission_interaction_evidence_conversationMissionId`
            ON `mission_interaction_evidence`
            (
                `conversationMissionId`
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
            `index_mission_interaction_evidence_messageId`
            ON `mission_interaction_evidence`
            (
                `messageId`
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_mission_interaction_evidence_conversationMissionId_evaluatedAt`
            ON `mission_interaction_evidence`
            (
                `conversationMissionId`,
                `evaluatedAt`
            )
            """.trimIndent()
        )
    }
}