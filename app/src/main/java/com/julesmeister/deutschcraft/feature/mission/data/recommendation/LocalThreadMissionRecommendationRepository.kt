package com.julesmeister.deutschcraft.feature.mission.data.recommendation

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.CefrMissionScenarioCatalog
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionRecommendationContext
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioDefinition
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioType
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.SelectMissionScenarioUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.ThreadMissionRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.ThreadMissionRecommendationRepository
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.toThreadRecommendation
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
 * Dedicated DataStore for thread-list mission history.
 *
 * It uses a different file name from the settings DataStore,
 * so both can safely exist in the same application.
 */
private val Context.threadMissionRecommendationDataStore:
        DataStore<Preferences> by
preferencesDataStore(
    name =
        "deutschcraft_thread_mission_recommendations"
)

@Singleton
class LocalThreadMissionRecommendationRepository
@Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val catalog:
    CefrMissionScenarioCatalog,
    private val selectMissionScenario:
    SelectMissionScenarioUseCase
) : ThreadMissionRecommendationRepository {

    private val dataStore:
            DataStore<Preferences>
        get() =
            context.threadMissionRecommendationDataStore

    private val currentRecommendation =
        MutableStateFlow<ThreadMissionRecommendation?>(
            null
        )

    /*
     * Prevents two simultaneous refreshes from selecting and
     * recording different scenarios at the same time.
     */
    private val refreshMutex =
        Mutex()

    override fun observeRecommendation():
            Flow<ThreadMissionRecommendation?> {
        return currentRecommendation.asStateFlow()
    }

    override suspend fun refreshRecommendation(
        cefrLevel: CefrLevel,
        adaptiveLearningFocuses: List<String>,
        completedScenarioTypes:
        List<MissionScenarioType>,
        forceDifferentScenario: Boolean
    ): ThreadMissionRecommendation? =
        refreshMutex.withLock {
            val previousRecommendation =
                currentRecommendation.value

            /*
             * Reuse the visible recommendation when the
             * screen simply reloads and the level is unchanged.
             *
             * This prevents the mission cell from changing
             * every time the thread list is opened.
             */
            if (
                !forceDifferentScenario &&
                previousRecommendation != null &&
                previousRecommendation.cefrLevel ==
                cefrLevel
            ) {
                return@withLock previousRecommendation
            }

            val recentScenarioTypes =
                loadRecentScenarioTypes()
                    .toMutableList()

            /*
             * When the learner asks for another mission,
             * guarantee the currently visible scenario is
             * excluded.
             */
            if (
                forceDifferentScenario &&
                previousRecommendation != null
            ) {
                recentScenarioTypes +=
                    previousRecommendation.scenarioType
            }

            val scenario =
                selectMissionScenario(
                    context =
                        MissionRecommendationContext(
                            cefrLevel =
                                cefrLevel,

                            /*
                             * The thread-list mission cell is
                             * curriculum driven.
                             *
                             * Recent chat text is deliberately
                             * not used here, otherwise a coffee
                             * conversation keeps producing café
                             * recommendations.
                             */
                            recentConversationText =
                                "",
                            recentlyOfferedScenarioTypes =
                                recentScenarioTypes,
                            recentlyCompletedScenarioTypes =
                                completedScenarioTypes,
                            adaptiveLearningFocuses =
                                adaptiveLearningFocuses,
                            activeScenarioType =
                                null
                        )
                )
                    ?: selectFallbackScenario(
                        cefrLevel =
                            cefrLevel,
                        excludedScenarioTypes =
                            recentScenarioTypes.toSet()
                    )
                    ?: run {
                        Log.w(
                            REPOSITORY_TAG,
                            "No mission scenario exists for " +
                                    "CEFR ${cefrLevel.name}."
                        )

                        return@withLock null
                    }

            val recommendation =
                scenario.toThreadRecommendation(
                    cefrLevel =
                        cefrLevel
                )

            if (!recommendation.isUsable) {
                Log.w(
                    REPOSITORY_TAG,
                    "Selected scenario was not usable: " +
                            scenario.type
                )

                return@withLock null
            }

            currentRecommendation.value =
                recommendation

            addScenarioToRecentHistory(
                scenarioType =
                    recommendation.scenarioType
            )

            Log.d(
                REPOSITORY_TAG,
                buildString {
                    append("Thread mission recommendation: ")
                    append(recommendation.scenarioType)
                    append(" / ")
                    append(recommendation.title)
                    append(" / ")
                    append(cefrLevel.name)
                    append(", recentHistory=")
                    append(recentScenarioTypes)
                }
            )

            recommendation
        }

    override suspend fun markRecommendationShown(
        scenarioType: MissionScenarioType
    ) {
        addScenarioToRecentHistory(
            scenarioType =
                scenarioType
        )
    }

    override suspend fun dismissRecommendation(
        scenarioType: MissionScenarioType
    ) {
        addScenarioToRecentHistory(
            scenarioType =
                scenarioType
        )

        if (
            currentRecommendation
                .value
                ?.scenarioType ==
            scenarioType
        ) {
            currentRecommendation.value =
                null
        }
    }

    override suspend fun clearRecommendation() {
        currentRecommendation.value =
            null
    }

    /*
     * Keeps the most recent scenario types in order.
     *
     * An existing type is moved to the end so it becomes the
     * most recently shown scenario.
     */
    private suspend fun addScenarioToRecentHistory(
        scenarioType: MissionScenarioType
    ) {
        try {
            dataStore.edit { preferences ->
                val currentHistory =
                    decodeScenarioTypes(
                        storedValue =
                            preferences[
                                RECENT_SCENARIO_TYPES_KEY
                            ]
                    )

                val updatedHistory =
                    (
                            currentHistory.filterNot { type ->
                                type == scenarioType
                            } +
                                    scenarioType
                            )
                        .takeLast(
                            MAXIMUM_RECENT_SCENARIO_COUNT
                        )

                preferences[
                    RECENT_SCENARIO_TYPES_KEY
                ] =
                    updatedHistory.joinToString(
                        separator =
                            SCENARIO_SEPARATOR
                    ) { type ->
                        type.name
                    }
            }
        } catch (ioException: IOException) {
            /*
             * History is supplementary. Failing to save it
             * must not break the thread list.
             */
            Log.e(
                REPOSITORY_TAG,
                "Mission recommendation history could not " +
                        "be saved.",
                ioException
            )
        }
    }

    private suspend fun loadRecentScenarioTypes():
            List<MissionScenarioType> {
        val preferences =
            dataStore
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
                .first()

        return decodeScenarioTypes(
            storedValue =
                preferences[
                    RECENT_SCENARIO_TYPES_KEY
                ]
        )
    }

    private fun decodeScenarioTypes(
        storedValue: String?
    ): List<MissionScenarioType> {
        if (storedValue.isNullOrBlank()) {
            return emptyList()
        }

        return storedValue
            .split(
                SCENARIO_SEPARATOR
            )
            .mapNotNull { storedName ->
                enumValueOrNull<MissionScenarioType>(
                    storedValue =
                        storedName
                )
            }
            .distinct()
            .takeLast(
                MAXIMUM_RECENT_SCENARIO_COUNT
            )
    }

    /*
     * Used only if the selector returns null.
     */
    private fun selectFallbackScenario(
        cefrLevel: CefrLevel,
        excludedScenarioTypes:
        Set<MissionScenarioType>
    ): MissionScenarioDefinition? {
        val levelScenarios =
            catalog.scenariosFor(
                level =
                    cefrLevel
            )

        return levelScenarios
            .firstOrNull { scenario ->
                scenario.type !in
                        excludedScenarioTypes
            }
            ?: levelScenarios.firstOrNull()
    }

    private inline fun <reified T : Enum<T>>
            enumValueOrNull(
        storedValue: String
    ): T? {
        val normalizedStoredValue =
            storedValue
                .trim()
                .uppercase()

        return enumValues<T>()
            .firstOrNull { enumValue ->
                enumValue.name ==
                        normalizedStoredValue
            }
    }

    private companion object {
        const val REPOSITORY_TAG =
            "ThreadMissionRecommendation"

        /*
         * The last six shown or dismissed scenario types are
         * excluded from the next recommendation.
         */
        const val MAXIMUM_RECENT_SCENARIO_COUNT =
            6

        const val SCENARIO_SEPARATOR =
            "|"

        val RECENT_SCENARIO_TYPES_KEY =
            stringPreferencesKey(
                "thread_mission_recent_scenario_types"
            )
    }
}