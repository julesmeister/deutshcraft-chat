package com.julesmeister.deutschcraft.feature.sync.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncPreferences
@Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val NEXT_CURSOR_KEY =
            longPreferencesKey("sync_next_cursor")

        private val LAST_SYNC_TIMESTAMP_KEY =
            longPreferencesKey("sync_last_sync_timestamp")
    }

    val nextCursor =
        dataStore.data.map { prefs ->
            prefs[NEXT_CURSOR_KEY] ?: 0L
        }

    val lastSyncTimestamp =
        dataStore.data.map { prefs ->
            prefs[LAST_SYNC_TIMESTAMP_KEY] ?: 0L
        }

    suspend fun updateNextCursor(
        cursor: Long
    ) {
        dataStore.edit { prefs ->
            prefs[NEXT_CURSOR_KEY] = cursor
        }
    }

    suspend fun updateLastSyncTimestamp(
        timestamp: Long
    ) {
        dataStore.edit { prefs ->
            prefs[LAST_SYNC_TIMESTAMP_KEY] = timestamp
        }
    }

    suspend fun clearSyncState() {
        dataStore.edit { prefs ->
            prefs.remove(NEXT_CURSOR_KEY)
            prefs.remove(LAST_SYNC_TIMESTAMP_KEY)
        }
    }
}
