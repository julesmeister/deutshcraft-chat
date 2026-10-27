package com.julesmeister.deutschcraft.feature.settings.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.julesmeister.deutschcraft.feature.settings.domain.AppTheme
import com.julesmeister.deutschcraft.feature.settings.domain.FontScaleOption
import com.julesmeister.deutschcraft.feature.settings.domain.ProgressLanguage
import com.julesmeister.deutschcraft.feature.settings.domain.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by
preferencesDataStore(
    name = "deutschcraft_settings"
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    val settings: Flow<UserSettings> =
        context
            .settingsDataStore
            .data
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(
                        emptyPreferences()
                    )
                } else {
                    throw throwable
                }
            }
            .map { preferences ->
                UserSettings(
                    appTheme =
                        AppTheme.fromStoredName(
                            value =
                                preferences[
                                    APP_THEME_KEY
                                ]
                        ),

                    progressLanguage =
                        ProgressLanguage.fromStoredName(
                            value =
                                preferences[
                                    PROGRESS_LANGUAGE_KEY
                                ]
                        ),

                    fontScaleOption =
                        FontScaleOption.fromStoredName(
                            value =
                                preferences[
                                    FONT_SCALE_KEY
                                ]
                        ),

                    reduceAnimations =
                        preferences[
                            REDUCE_ANIMATIONS_KEY
                        ] ?: false,

                    showMissionHints =
                        preferences[
                            SHOW_MISSION_HINTS_KEY
                        ] ?: true,

                    showExamplePhrases =
                        preferences[
                            SHOW_EXAMPLE_PHRASES_KEY
                        ] ?: true,

                    showTargetVocabulary =
                        preferences[
                            SHOW_TARGET_VOCABULARY_KEY
                        ] ?: true
                )
            }

    suspend fun setAppTheme(
        theme: AppTheme
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[
                APP_THEME_KEY
            ] =
                theme.name
        }
    }

    suspend fun setProgressLanguage(
        language: ProgressLanguage
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[
                PROGRESS_LANGUAGE_KEY
            ] =
                language.name
        }
    }

    suspend fun setFontScale(
        option: FontScaleOption
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[
                FONT_SCALE_KEY
            ] =
                option.name
        }
    }

    suspend fun setReduceAnimations(
        enabled: Boolean
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[
                REDUCE_ANIMATIONS_KEY
            ] =
                enabled
        }
    }

    suspend fun setShowMissionHints(
        enabled: Boolean
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[
                SHOW_MISSION_HINTS_KEY
            ] =
                enabled
        }
    }

    suspend fun setShowExamplePhrases(
        enabled: Boolean
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[
                SHOW_EXAMPLE_PHRASES_KEY
            ] =
                enabled
        }
    }

    suspend fun setShowTargetVocabulary(
        enabled: Boolean
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[
                SHOW_TARGET_VOCABULARY_KEY
            ] =
                enabled
        }
    }

    private companion object {

        val APP_THEME_KEY =
            stringPreferencesKey(
                name = "app_theme"
            )

        val PROGRESS_LANGUAGE_KEY =
            stringPreferencesKey(
                name = "progress_language"
            )

        val FONT_SCALE_KEY =
            stringPreferencesKey(
                name = "font_scale"
            )

        val REDUCE_ANIMATIONS_KEY =
            booleanPreferencesKey(
                name = "reduce_animations"
            )

        val SHOW_MISSION_HINTS_KEY =
            booleanPreferencesKey(
                name = "show_mission_hints"
            )

        val SHOW_EXAMPLE_PHRASES_KEY =
            booleanPreferencesKey(
                name = "show_example_phrases"
            )

        val SHOW_TARGET_VOCABULARY_KEY =
            booleanPreferencesKey(
                name = "show_target_vocabulary"
            )
    }
}