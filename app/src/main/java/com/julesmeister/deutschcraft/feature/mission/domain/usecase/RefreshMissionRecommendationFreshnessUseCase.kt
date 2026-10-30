// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/usecase/RefreshMissionRecommendationFreshnessUseCase.kt

package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.assessment.domain.GetCurrentLearnerCefrLevelUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionLearningContext
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendationStore
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RefreshMissionRecommendationFreshnessUseCase
@Inject constructor(
    private val getCurrentLearnerCefrLevel:
    GetCurrentLearnerCefrLevelUseCase,

    private val buildMissionLearningContext:
    BuildMissionLearningContextUseCase,

    private val recommendationStore:
    MissionThreadRecommendationStore
) {

    private val refreshMutex =
        Mutex()

    suspend operator fun invoke():
            Int {

        return refreshMutex.withLock {
            try {
                val learnerLevel =
                    getCurrentLearnerCefrLevel()

                recommendationStore.performMaintenance(
                    cefrLevel =
                        learnerLevel
                )

                val currentLearningContext =
                    try {
                        buildMissionLearningContext(
                            cefrLevel =
                                learnerLevel
                        )
                    } catch (
                        cancellationException:
                        CancellationException
                    ) {
                        throw cancellationException
                    } catch (throwable: Throwable) {
                        Log.w(
                            USE_CASE_TAG,
                            "Learning context could not be rebuilt.",
                            throwable
                        )

                        MissionLearningContext.Empty
                    }

                val invalidatedCount =
                    recommendationStore
                        .invalidateStaleRecommendations(
                            cefrLevel =
                                learnerLevel,

                            currentLearningContext =
                                currentLearningContext
                        )

                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Recommendation freshness refreshed. "
                        )

                        append(
                            "level="
                        )

                        append(
                            learnerLevel.name
                        )

                        append(
                            ", invalidated="
                        )

                        append(
                            invalidatedCount
                        )
                    }
                )

                invalidatedCount
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    USE_CASE_TAG,
                    "Recommendation freshness refresh failed.",
                    throwable
                )

                0
            }
        }
    }

    private companion object {

        const val USE_CASE_TAG =
            "RecommendationFreshness"
    }
}