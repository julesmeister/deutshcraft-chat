package com.julesmeister.deutschcraft.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    /*
     * Keep your existing MIGRATION_5_6 code here.
     *
     * Do not replace its SQL with placeholder code.
     */
    val MIGRATION_5_6 =
        object : Migration(
            5,
            6
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                /*
                 * Keep the existing 5-to-6 SQL statements
                 * currently present in your project.
                 */
            }
        }

    /*
     * Keep your existing MIGRATION_6_7 code here.
     *
     * This migration should contain the three mission
     * objective learning-support columns added previously:
     *
     * coachingHintsJson
     * targetVocabularyJson
     * examplePhrasesJson
     */
    val MIGRATION_6_7 =
        object : Migration(
            6,
            7
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
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
     * Creates the persistent adaptive-learning memory.
     *
     * Version 7:
     * Mission objective learning-support fields.
     *
     * Version 8:
     * Adaptive-learning focuses and evidence.
     */
    val MIGRATION_7_8 =
        object : Migration(
            7,
            8
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
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

    private fun createAdaptiveLearningFocusTable(
        database: SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS
            adaptive_learning_focuses (
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
        database: SupportSQLiteDatabase
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
        database: SupportSQLiteDatabase
    ) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS
            adaptive_learning_evidence (
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
        database: SupportSQLiteDatabase
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
}