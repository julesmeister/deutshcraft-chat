package com.julesmeister.deutschcraft.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MissionSyncDatabaseMigration {

    val MIGRATION_20_21 =
        object : Migration(
            startVersion = 20,
            endVersion = 21
        ) {

            override fun migrate(
                database:
                SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    ALTER TABLE conversation_missions
                    ADD COLUMN syncStatus
                    TEXT NOT NULL DEFAULT 'LOCAL_ONLY'
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE conversation_missions
                    ADD COLUMN remoteVersion
                    INTEGER
                    DEFAULT NULL
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE conversation_missions
                    ADD COLUMN lastSyncedAt
                    INTEGER
                    DEFAULT NULL
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE conversation_missions
                    ADD COLUMN isDeleted
                    INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE conversation_missions
                    ADD COLUMN deletedAt
                    INTEGER
                    DEFAULT NULL
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_conversation_missions_updatedAt
                    ON conversation_missions(updatedAt)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_conversation_missions_syncStatus
                    ON conversation_missions(syncStatus)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_conversation_missions_isDeleted
                    ON conversation_missions(isDeleted)
                    """.trimIndent()
                )
            }
        }
}