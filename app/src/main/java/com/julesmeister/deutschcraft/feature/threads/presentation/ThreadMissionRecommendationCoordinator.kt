package com.julesmeister.deutschcraft.feature.threads.presentation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionThreadRecommendationCache
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.LoadMissionThreadRecommendationUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.StartRecommendedMissionThreadUseCase
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Coordinates the mission recommendation displayed at the
 * top of the thread-list screen.
 *
 * This is separate from the in-chat mission-offer system.
 *
 * Thread-list mission flow:
 *
 * Threads screen
 *      ↓
 * ThreadMissionRecommendationCoordinator
 *      ↓
 * LoadMissionThreadRecommendationUseCase
 *      ↓
 * MissionThreadRecommendationCache
 *
 * In-chat mission flow:
 *
 * Active chat
 *      ↓
 * ChatMissionOfferCoordinator
 *      ↓
 * DetectMissionOpportunityUseCase
 */
class ThreadMissionRecommendationCoordinator
@Inject constructor(
    private val loadMissionRecommendation:
    LoadMissionThreadRecommendationUseCase,
    private val startRecommendedMission:
    StartRecommendedMissionThreadUseCase,
    private val recommendationCache:
    MissionThreadRecommendationCache
) {

    /*
     * Restores the currently cached recommendation when the
     * thread-list screen is recreated.
     *
     * This prevents unnecessary recommendation regeneration
     * during configuration changes or navigation.
     */
    fun restoreCachedRecommendation():
            MissionThreadRecommendation? {
        return recommendationCache.get()
    }

    /*
     * Loads a CEFR-appropriate mission recommendation for
     * the thread-list mission cell.
     *
     * The learner level is derived from the most recently
     * updated active conversation until a dedicated
     * profile-level CEFR source is connected.
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

        return try {
            val recommendation =
                loadMissionRecommendation(
                    cefrLevel =
                        learnerLevel,
                    forceRefresh =
                        forceRefresh
                )

            Log.d(
                COORDINATOR_TAG,
                buildString {
                    append(
                        "Thread mission recommendation loaded. "
                    )
                    append("cefrLevel=")
                    append(learnerLevel.name)
                    append(", forceRefresh=")
                    append(forceRefresh)
                    append(", recommendation=")
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
             * The thread-list recommendation is
             * supplementary.
             *
             * Failure must not prevent the learner from
             * viewing or opening existing conversations.
             */
            Log.e(
                COORDINATOR_TAG,
                "Thread mission recommendation could not " +
                        "be loaded.",
                throwable
            )

            restoreCachedRecommendation()
        }
    }

    /*
     * Forces the recommendation generator to provide another
     * topic.
     *
     * The use case behind this function must exclude the
     * previously displayed semantic scenario, not only its
     * title.
     *
     * For example, these must all count as CAFE_ORDERING:
     *
     * - Bestellung im Café
     * - Morgenkaffee
     * - Im Café
     * - Einen Kaffee bestellen
     */
    suspend fun loadAnotherRecommendation(
        activeThreads:
        List<ConversationThread>
    ): MissionThreadRecommendation? {
        return loadRecommendation(
            activeThreads =
                activeThreads,
            forceRefresh =
                true
        )
    }

    /*
     * Starts the mission represented by the thread-list
     * recommendation.
     *
     * The start use case is responsible for:
     *
     * - Creating the conversation
     * - Persisting the generated mission template
     * - Persisting its objectives
     * - Starting the conversation mission
     * - Returning the created conversation for navigation
     */
    suspend fun startRecommendation(
        recommendation:
        MissionThreadRecommendation
    ): Conversation {
        require(
            recommendation.title.isNotBlank()
        ) {
            "The recommended mission title must not be blank."
        }

        return try {
            val conversation =
                startRecommendedMission(
                    recommendation =
                        recommendation
                )

            /*
             * Clear the cached cell after the learner starts
             * it. The next visit to the thread list should
             * produce a different recommendation.
             */
            recommendationCache.clear()

            Log.d(
                COORDINATOR_TAG,
                buildString {
                    append(
                        "Recommended mission thread started. "
                    )
                    append("title=")
                    append(recommendation.title)
                    append(", conversationId=")
                    append(conversation.id)
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
                "The recommended mission thread could not " +
                        "be started.",
                throwable
            )

            throw throwable
        }
    }

    /*
     * Clears the visible cached recommendation.
     *
     * This does not necessarily erase recommendation
     * history. Semantic scenario history should be owned by
     * LoadMissionThreadRecommendationUseCase or its
     * repository.
     */
    fun clearCachedRecommendation() {
        recommendationCache.clear()
    }

    private fun determineLearnerLevel(
        activeThreads:
        List<ConversationThread>
    ): CefrLevel {
        /*
         * Prefer the CEFR level of the most recently updated
         * conversation.
         *
         * This is a temporary local-first strategy until the
         * thread-list feature observes the persistent learner
         * profile directly.
         */
        return activeThreads
            .asSequence()
            .maxByOrNull { thread ->
                thread.updatedAt
            }
            ?.cefrLevel
            ?: CefrLevel.A1
    }

    private companion object {
        const val COORDINATOR_TAG =
            "ThreadMissionRecommendation"
    }
}