// app/src/main/java/com/julesmeister/deutschcraft/core/database/migration/SyncDatabaseMigration.kt

package com.julesmeister.deutschcraft.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object SyncDatabaseMigration {

    val MIGRATION_19_20 =
        object : Migration(
            startVersion = 19,
            endVersion = 20
        ) {
            override fun migrate(
                database:
                SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    ALTER TABLE conversations
                    ADD COLUMN isDeleted
                    INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE conversations
                    ADD COLUMN deletedAt
                    INTEGER
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_conversations_isDeleted
                    ON conversations(isDeleted)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE messages
                    ADD COLUMN updatedAt
                    INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    UPDATE messages
                    SET updatedAt = createdAt
                    WHERE updatedAt = 0
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE messages
                    ADD COLUMN isDeleted
                    INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE messages
                    ADD COLUMN deletedAt
                    INTEGER
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_messages_updatedAt
                    ON messages(updatedAt)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_messages_isDeleted
                    ON messages(isDeleted)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_messages_syncStatus
                    ON messages(syncStatus)
                    """.trimIndent()
                )
            }
        }
}