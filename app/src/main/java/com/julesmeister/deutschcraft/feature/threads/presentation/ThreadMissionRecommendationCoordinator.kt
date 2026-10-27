package com.julesmeister.deutschcraft.feature.threads.presentation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioType
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.LoadMissionThreadRecommendationUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.StartRecommendedMissionThreadUseCase
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/*
 * Coordinates the featured mission recommendation displayed
 * at the top of the thread-list screen.
 *
 * Room is now the recommendation source of truth.
 *
 * Responsibilities:
 *
 * - Load an existing persisted recommendation
 * - Mark the current recommendation skipped on left swipe
 * - Return the next persisted recommendation
 * - Generate a fallback when Room is empty
 * - Replenish the persisted recommendation pool to five
 * - Exclude recognizable scenarios already represented by
 *   visible conversation threads
 * - Start a selected recommendation
 */
class ThreadMissionRecommendationCoordinator
@Inject constructor(
    private val loadRecommendation:
    LoadMissionThreadRecommendationUseCase,
    private val startRecommendedMission:
    StartRecommendedMissionThreadUseCase
) {

    /*
     * Background job that maintains five persisted
     * recommendations.
     */
    private var prefetchJob:
            Job? =
        null

    /*
     * The CEFR level currently associated with prefetchJob.
     */
    private var prefetchLevel:
            CefrLevel? =
        null

    /*
     * A refill may be requested while another refill is still
     * active.
     *
     * In that case, another cycle is run after the active job
     * completes if Room still contains fewer than five
     * eligible recommendations.
     */
    private var refillRequestedWhileActive:
            Boolean =
        false

    /*
     * Loads the recommendation that should currently be shown.
     *
     * forceRefresh = false:
     *
     * - Restore the persisted DISPLAYED recommendation
     * - Otherwise use the oldest persisted READY item
     * - Otherwise generate and persist one item
     *
     * forceRefresh = true:
     *
     * - Mark the current recommendation SKIPPED
     * - Return the next persisted READY recommendation
     * - Generate one only if the persisted pool is exhausted
     */
    suspend fun loadRecommendation(
        activeThreads:
        List<ConversationThread>,
        forceRefresh: Boolean =
            false
    ): MissionThreadRecommendation? {
        val learnerLevel =
            determineLearnerLevel(
                activeThreads =
                    activeThreads
            )

        val existingThreadScenarioTypes =
            classifyExistingThreads(
                activeThreads =
                    activeThreads
            )

        return try {
            val recommendation =
                if (forceRefresh) {
                    loadRecommendation.advance(
                        cefrLevel =
                            learnerLevel,
                        externalExcludedScenarioTypes =
                            existingThreadScenarioTypes
                    )
                } else {
                    loadRecommendation(
                        cefrLevel =
                            learnerLevel,
                        forceRefresh =
                            false,
                        externalExcludedScenarioTypes =
                            existingThreadScenarioTypes
                    )
                }

            Log.d(
                COORDINATOR_TAG,
                buildString {
                    append(
                        "Loaded persisted mission recommendation. "
                    )
                    append("level=")
                    append(
                        learnerLevel.name
                    )
                    append(", forceRefresh=")
                    append(
                        forceRefresh
                    )
                    append(", scenario=")
                    append(
                        recommendation?.scenarioType
                    )
                    append(", title=")
                    append(
                        recommendation?.title
                    )
                }
            )

            recommendation
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            /*
             * Recommendation loading is supplementary.
             *
             * A Room, network, generation, or parsing failure
             * must not prevent saved conversation threads from
             * being displayed.
             */
            Log.e(
                COORDINATOR_TAG,
                "Mission recommendation could not be loaded.",
                throwable
            )

            null
        }
    }

    /*
     * Ensures Room contains five eligible recommendations.
     *
     * The function starts background work and returns
     * immediately.
     *
     * During the first app launch:
     *
     * - Recommendation #1 is delivered as soon as it is saved
     * - Recommendations #2 through #5 remain persisted and
     *   hidden until the learner swipes
     *
     * During later app launches:
     *
     * - Persisted recommendations load immediately
     * - Missing pool items are generated in the background
     */
    fun prefetchRecommendations(
        activeThreads:
        List<ConversationThread>,
        scope: CoroutineScope,
        onFirstRecommendationAvailable:
            (MissionThreadRecommendation) -> Unit,
        onFailure:
            (String) -> Unit =
            {}
    ) {
        val learnerLevel =
            determineLearnerLevel(
                activeThreads =
                    activeThreads
            )

        val existingThreadScenarioTypes =
            classifyExistingThreads(
                activeThreads =
                    activeThreads
            )

        /*
         * Do not start a duplicate prefetch cycle for the same
         * CEFR level.
         *
         * Remember that another refill was requested, because
         * the current cycle may finish below the target after
         * generation or validation failures.
         */
        if (
            prefetchJob?.isActive ==
            true &&
            prefetchLevel ==
            learnerLevel
        ) {
            refillRequestedWhileActive =
                true

            Log.d(
                COORDINATOR_TAG,
                "Recommendation prefetch is already active. " +
                        "A follow-up refill was requested."
            )

            return
        }

        /*
         * A CEFR-level change makes the previous prefetch job
         * obsolete.
         */
        if (
            prefetchLevel != null &&
            prefetchLevel !=
            learnerLevel
        ) {
            prefetchJob?.cancel()

            prefetchJob =
                null

            refillRequestedWhileActive =
                false
        }

        prefetchLevel =
            learnerLevel

        startPrefetchJob(
            learnerLevel =
                learnerLevel,
            existingThreadScenarioTypes =
                existingThreadScenarioTypes,
            scope =
                scope,
            onFirstRecommendationAvailable =
                onFirstRecommendationAvailable,
            onFailure =
                onFailure
        )
    }

    /*
     * Starts the supplied recommendation as a real
     * conversation mission.
     *
     * StartRecommendedMissionThreadUseCase currently returns
     * the created Conversation.
     *
     * The recommendation-to-mission link will be completed in
     * the next integration step, where that use case will also
     * return the generated conversationMissionId.
     */
    suspend fun startRecommendation(
        recommendation:
        MissionThreadRecommendation
    ): Conversation {
        require(
            recommendation.id.isNotBlank()
        ) {
            "Recommendation ID must not be blank."
        }

        return try {
            val conversation =
                startRecommendedMission(
                    recommendation =
                        recommendation
                )

            Log.d(
                COORDINATOR_TAG,
                buildString {
                    append(
                        "Started recommended mission thread. "
                    )
                    append("recommendationId=")
                    append(
                        recommendation.id
                    )
                    append(", scenario=")
                    append(
                        recommendation.scenarioType
                    )
                    append(", conversationId=")
                    append(
                        conversation.id
                    )
                }
            )

            conversation
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                COORDINATOR_TAG,
                "Recommended mission thread could not be " +
                        "started.",
                throwable
            )

            throw throwable
        }
    }

    fun cancelPrefetch() {
        prefetchJob?.cancel()

        prefetchJob =
            null

        prefetchLevel =
            null

        refillRequestedWhileActive =
            false
    }

    private fun startPrefetchJob(
        learnerLevel: CefrLevel,
        existingThreadScenarioTypes:
        Set<MissionScenarioType>,
        scope: CoroutineScope,
        onFirstRecommendationAvailable:
            (MissionThreadRecommendation) -> Unit,
        onFailure:
            (String) -> Unit
    ) {
        prefetchJob =
            scope.launch {
                try {
                    loadRecommendation.prefetchUntilFull(
                        cefrLevel =
                            learnerLevel,
                        externalExcludedScenarioTypes =
                            existingThreadScenarioTypes,
                        onRecommendationAvailable = {
                                recommendation,
                                availableCount ->

                            /*
                             * The ViewModel only applies this
                             * value when it does not already
                             * display a recommendation.
                             *
                             * Later generated recommendations
                             * remain hidden in Room.
                             */
                            onFirstRecommendationAvailable(
                                recommendation
                            )

                            Log.d(
                                COORDINATOR_TAG,
                                buildString {
                                    append(
                                        "Persisted recommendation "
                                    )
                                    append(
                                        "became available. "
                                    )
                                    append("scenario=")
                                    append(
                                        recommendation
                                            .scenarioType
                                    )
                                    append(", available=")
                                    append(
                                        availableCount
                                    )
                                }
                            )
                        }
                    )
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        COORDINATOR_TAG,
                        "Mission recommendation prefetch " +
                                "failed.",
                        throwable
                    )

                    onFailure(
                        PREFETCH_ERROR
                    )
                } finally {
                    prefetchJob =
                        null

                    val followUpRequested =
                        refillRequestedWhileActive

                    refillRequestedWhileActive =
                        false

                    /*
                     * Count the persisted recommendations only
                     * after the current prefetch cycle ends.
                     *
                     * Start another cycle when:
                     *
                     * - A swipe requested refill while the job
                     *   was active, and
                     * - Room still contains fewer than five
                     *   eligible recommendations
                     */
                    val shouldRunAnotherCycle =
                        if (followUpRequested) {
                            try {
                                loadRecommendation
                                    .availableCount(
                                        cefrLevel =
                                            learnerLevel
                                    ) <
                                        TARGET_RECOMMENDATION_COUNT
                            } catch (
                                cancellationException:
                                CancellationException
                            ) {
                                throw cancellationException
                            } catch (throwable: Throwable) {
                                Log.e(
                                    COORDINATOR_TAG,
                                    "Could not inspect the " +
                                            "persisted recommendation " +
                                            "pool.",
                                    throwable
                                )

                                false
                            }
                        } else {
                            false
                        }

                    if (shouldRunAnotherCycle) {
                        Log.d(
                            COORDINATOR_TAG,
                            "Starting requested follow-up " +
                                    "recommendation refill."
                        )

                        startPrefetchJob(
                            learnerLevel =
                                learnerLevel,
                            existingThreadScenarioTypes =
                                existingThreadScenarioTypes,
                            scope =
                                scope,
                            onFirstRecommendationAvailable =
                                onFirstRecommendationAvailable,
                            onFailure =
                                onFailure
                        )
                    }
                }
            }
    }

    /*
     * Uses the most recently updated active thread as a
     * temporary CEFR source.
     *
     * This should eventually be replaced by the persisted
     * learner profile level.
     */
    private fun determineLearnerLevel(
        activeThreads:
        List<ConversationThread>
    ): CefrLevel {
        return activeThreads
            .maxByOrNull { thread ->
                thread.updatedAt
            }
            ?.cefrLevel
            ?: CefrLevel.A1
    }

    /*
     * Classifies existing visible thread titles into semantic
     * mission scenarios.
     *
     * This prevents the recommendation pool from generating
     * obvious scenarios already represented on the thread
     * screen.
     *
     * Long term, scenarioType should be persisted directly on
     * the conversation or mission instead of inferred from a
     * title.
     */
    private fun classifyExistingThreads(
        activeThreads:
        List<ConversationThread>
    ): Set<MissionScenarioType> {
        return activeThreads
            .mapNotNull { thread ->
                classifyThreadTitle(
                    title =
                        thread.title
                )
            }
            .toSet()
    }

    private fun classifyThreadTitle(
        title: String
    ): MissionScenarioType? {
        val normalizedTitle =
            title
                .trim()
                .lowercase()

        return when {
            normalizedTitle.containsAny(
                "kaffee",
                "coffee",
                "café",
                "cafe",
                "bäckerei",
                "baeckerei",
                "bakery",
                "brötchen",
                "brot bestellen",
                "frühstück bestellen"
            ) -> {
                MissionScenarioType.CAFE_ORDERING
            }

            normalizedTitle.containsAny(
                "restaurant",
                "speisekarte",
                "abendessen",
                "mittagessen",
                "essen bestellen"
            ) -> {
                MissionScenarioType.RESTAURANT_ORDERING
            }

            normalizedTitle.containsAny(
                "supermarkt",
                "lebensmittel",
                "grocery"
            ) -> {
                MissionScenarioType.GROCERY_SHOPPING
            }

            normalizedTitle.containsAny(
                "kleidung",
                "schuhe",
                "clothing"
            ) -> {
                MissionScenarioType.CLOTHING_SHOPPING
            }

            normalizedTitle.containsAny(
                "vorstellungsgespräch",
                "bewerbungsgespräch",
                "job interview",
                "bewerbung"
            ) -> {
                MissionScenarioType.JOB_INTERVIEW
            }

            normalizedTitle.containsAny(
                "hotel",
                "einchecken",
                "check-in",
                "check in"
            ) -> {
                MissionScenarioType.HOTEL_CHECK_IN
            }

            normalizedTitle.containsAny(
                "arzt",
                "ärztin",
                "doctor",
                "praxis"
            ) -> {
                MissionScenarioType.DOCTOR_APPOINTMENT
            }

            normalizedTitle.containsAny(
                "apotheke",
                "pharmacy",
                "medikament"
            ) -> {
                MissionScenarioType.VISITING_A_PHARMACY
            }

            normalizedTitle.containsAny(
                "weg fragen",
                "wegbeschreibung",
                "directions",
                "bahnhof finden"
            ) -> {
                MissionScenarioType.ASKING_FOR_DIRECTIONS
            }

            normalizedTitle.containsAny(
                "bus",
                "bahn",
                "zug",
                "public transport",
                "öffentliche verkehrsmittel"
            ) -> {
                MissionScenarioType.PUBLIC_TRANSPORT
            }

            normalizedTitle.containsAny(
                "sich vorstellen",
                "kennenlernen",
                "introducing yourself"
            ) -> {
                MissionScenarioType.INTRODUCING_YOURSELF
            }

            normalizedTitle.containsAny(
                "familie",
                "family"
            ) -> {
                MissionScenarioType.TALKING_ABOUT_FAMILY
            }

            normalizedTitle.containsAny(
                "tagesablauf",
                "daily routine",
                "mein tag"
            ) -> {
                MissionScenarioType.DAILY_ROUTINE
            }

            normalizedTitle.containsAny(
                "hobby",
                "hobbys",
                "freizeit"
            ) -> {
                MissionScenarioType.TALKING_ABOUT_HOBBIES
            }

            normalizedTitle.containsAny(
                "wetter",
                "weather"
            ) -> {
                MissionScenarioType.TALKING_ABOUT_WEATHER
            }

            normalizedTitle.containsAny(
                "reise planen",
                "urlaub planen",
                "trip planning"
            ) -> {
                MissionScenarioType.PLANNING_A_TRIP
            }

            normalizedTitle.containsAny(
                "beschwerde",
                "complaint",
                "kundenservice"
            ) -> {
                MissionScenarioType.MAKING_A_COMPLAINT
            }

            normalizedTitle.containsAny(
                "teammeeting",
                "team meeting"
            ) -> {
                MissionScenarioType.TEAM_MEETING
            }

            normalizedTitle.containsAny(
                "verhandlung",
                "negotiation"
            ) -> {
                MissionScenarioType.NEGOTIATION
            }

            normalizedTitle.containsAny(
                "geschichte erzählen",
                "story",
                "erlebnis erzählen"
            ) -> {
                MissionScenarioType.TELLING_A_STORY
            }

            normalizedTitle.containsAny(
                "meinung",
                "opinion"
            ) -> {
                MissionScenarioType.EXPRESSING_AN_OPINION
            }

            else -> {
                null
            }
        }
    }

    private fun String.containsAny(
        vararg values: String
    ): Boolean {
        return values.any { value ->
            contains(
                other =
                    value,
                ignoreCase =
                    true
            )
        }
    }

    private companion object {
        const val COORDINATOR_TAG =
            "ThreadMissionRecommendation"

        const val TARGET_RECOMMENDATION_COUNT =
            5

        const val PREFETCH_ERROR =
            "Weitere Missionsempfehlungen konnten nicht " +
                    "vorbereitet werden."
    }
}