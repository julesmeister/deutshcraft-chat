package com.julesmeister.deutschcraft.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

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
}