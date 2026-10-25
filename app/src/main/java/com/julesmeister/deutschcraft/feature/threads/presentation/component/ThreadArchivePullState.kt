package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

@Stable
class ThreadArchivePullState internal constructor(
    private val listState: LazyListState,
    private val archiveEnabled: Boolean,
    private val revealThresholdPx: Float,
    private val minimumVisibilityPx: Float
) {
    var pullDistancePx by mutableFloatStateOf(0f)
        private set

    var isArchiveRowRevealed by mutableStateOf(false)
        private set

    val pullProgress: Float
        get() {
            return (
                    pullDistancePx /
                            revealThresholdPx
                    ).coerceIn(
                    minimumValue = 0f,
                    maximumValue = 1f
                )
        }

    val isThresholdReached: Boolean
        get() {
            return pullDistancePx >=
                    revealThresholdPx
        }

    val isPullIndicatorVisible: Boolean
        get() {
            return archiveEnabled &&
                    !isArchiveRowRevealed &&
                    pullDistancePx >=
                    minimumVisibilityPx
        }

    private val isListAtTop: Boolean
        get() {
            return listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0
        }

    val nestedScrollConnection =
        object : NestedScrollConnection {

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val isPullingDown =
                    available.y > 0f

                if (
                    archiveEnabled &&
                    isPullingDown &&
                    isListAtTop &&
                    !isArchiveRowRevealed
                ) {
                    val resistance =
                        calculateResistance()

                    val resistedDistance =
                        available.y * resistance

                    pullDistancePx =
                        (
                                pullDistancePx +
                                        resistedDistance
                                ).coerceAtMost(
                                revealThresholdPx *
                                        MAXIMUM_PULL_MULTIPLIER
                            )

                    return Offset(
                        x = 0f,
                        y = available.y
                    )
                }

                return Offset.Zero
            }

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val isPushingUp =
                    available.y < 0f

                if (
                    isPushingUp &&
                    pullDistancePx > 0f &&
                    !isArchiveRowRevealed
                ) {
                    val previousDistance =
                        pullDistancePx

                    pullDistancePx =
                        (
                                pullDistancePx +
                                        available.y
                                ).coerceAtLeast(0f)

                    return Offset(
                        x = 0f,
                        y = pullDistancePx -
                                previousDistance
                    )
                }

                return Offset.Zero
            }

            override suspend fun onPreFling(
                available: Velocity
            ): Velocity {
                finishPull()
                return Velocity.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                finishPull()
                return Velocity.Zero
            }
        }

    fun finishPull() {
        if (!archiveEnabled) {
            reset()
            return
        }

        if (
            !isArchiveRowRevealed &&
            isThresholdReached
        ) {
            isArchiveRowRevealed = true
        }

        pullDistancePx = 0f
    }

    fun hideArchiveRow() {
        isArchiveRowRevealed = false
        pullDistancePx = 0f
    }

    private fun reset() {
        pullDistancePx = 0f
        isArchiveRowRevealed = false
    }

    private fun calculateResistance(): Float {
        return when {
            pullProgress < 0.35f -> {
                0.65f
            }

            pullProgress < 0.7f -> {
                0.45f
            }

            else -> {
                0.28f
            }
        }
    }

    private companion object {
        const val MAXIMUM_PULL_MULTIPLIER =
            1.25f
    }
}

@Composable
fun rememberThreadArchivePullState(
    listState: LazyListState,
    archiveEnabled: Boolean
): ThreadArchivePullState {
    val density = LocalDensity.current

    val revealThresholdPx = with(density) {
        ARCHIVE_REVEAL_THRESHOLD.toPx()
    }

    val minimumVisibilityPx = with(density) {
        MINIMUM_PULL_VISIBILITY.toPx()
    }

    return remember(
        listState,
        archiveEnabled,
        revealThresholdPx,
        minimumVisibilityPx
    ) {
        ThreadArchivePullState(
            listState = listState,
            archiveEnabled = archiveEnabled,
            revealThresholdPx =
                revealThresholdPx,
            minimumVisibilityPx =
                minimumVisibilityPx
        )
    }
}

private val ARCHIVE_REVEAL_THRESHOLD =
    84.dp

private val MINIMUM_PULL_VISIBILITY =
    6.dp