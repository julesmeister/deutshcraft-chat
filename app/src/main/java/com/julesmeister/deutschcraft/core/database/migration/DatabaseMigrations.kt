package com.julesmeister.deutschcraft.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    /*
     * Version 5 -> 6
     *
     * Adds persisted Besser corrections.
     */
    val MIGRATION_5_6 =
        object : Migration(
            startVersion = 5,
            endVersion = 6
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    ALTER TABLE messages
                    ADD COLUMN correction TEXT DEFAULT NULL
                    """.trimIndent()
                )
            }
        }

    /*
     * Version 6 -> 7
     *
     * Mission system persistence.
     */
    val MIGRATION_6_7 =
        object : Migration(
            startVersion = 6,
            endVersion = 7
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
            ) {

                /*
                 * Mission templates
                 */
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS mission_templates (
                        id TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        scenario TEXT NOT NULL,
                        learnerRole TEXT NOT NULL,
                        assistantRole TEXT NOT NULL,
                        difficulty TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        isEnabled INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )

                /*
                 * Mission objectives
                 */
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS mission_objectives (
                        id TEXT NOT NULL,
                        missionTemplateId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        sequenceNumber INTEGER NOT NULL,
                        isRequired INTEGER NOT NULL,
                        completionHint TEXT NOT NULL,
                        PRIMARY KEY(id),
                        FOREIGN KEY(missionTemplateId)
                            REFERENCES mission_templates(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_mission_objectives_missionTemplateId
                    ON mission_objectives(
                        missionTemplateId
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_mission_objectives_template_sequence
                    ON mission_objectives(
                        missionTemplateId,
                        sequenceNumber
                    )
                    """.trimIndent()
                )

                /*
                 * Conversation missions
                 *
                 * IMPORTANT:
                 * If your ConversationEntity table name is not
                 * "conversations", replace it below.
                 */
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS conversation_missions (
                        id TEXT NOT NULL,
                        conversationId TEXT NOT NULL,
                        missionTemplateId TEXT NOT NULL,
                        status TEXT NOT NULL,
                        startedAt INTEGER,
                        completedAt INTEGER,
                        dismissedAt INTEGER,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id),
                        FOREIGN KEY(conversationId)
                            REFERENCES conversations(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE,
                        FOREIGN KEY(missionTemplateId)
                            REFERENCES mission_templates(id)
                            ON UPDATE NO ACTION
                            ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_conversation_missions_conversationId
                    ON conversation_missions(
                        conversationId
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_conversation_missions_missionTemplateId
                    ON conversation_missions(
                        missionTemplateId
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_conversation_missions_status
                    ON conversation_missions(
                        status
                    )
                    """.trimIndent()
                )

                /*
                 * Mission objective progress
                 */
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS
                    mission_objective_progress (
                        conversationMissionId TEXT NOT NULL,
                        objectiveId TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        completedAt INTEGER,
                        completedByMessageId TEXT,
                        confidence REAL NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(
                            conversationMissionId,
                            objectiveId
                        ),
                        FOREIGN KEY(conversationMissionId)
                            REFERENCES conversation_missions(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE,
                        FOREIGN KEY(objectiveId)
                            REFERENCES mission_objectives(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_mission_progress_conversationMissionId
                    ON mission_objective_progress(
                        conversationMissionId
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_mission_progress_objectiveId
                    ON mission_objective_progress(
                        objectiveId
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_mission_progress_completedByMessageId
                    ON mission_objective_progress(
                        completedByMessageId
                    )
                    """.trimIndent()
                )
            }
        }
}