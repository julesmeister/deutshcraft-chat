package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import com.julesmeister.deutschcraft.feature.threads.presentation.component.ArchivePullIndicator
import com.julesmeister.deutschcraft.feature.threads.presentation.component.ArchivedThreadsRow
import com.julesmeister.deutschcraft.feature.threads.presentation.component.RecommendedMissionThreadRow
import com.julesmeister.deutschcraft.feature.threads.presentation.component.ThreadArchivePullState
import com.julesmeister.deutschcraft.feature.threads.presentation.component.ThreadListEmptyState
import com.julesmeister.deutschcraft.feature.threads.presentation.component.ThreadListLoadingState
import com.julesmeister.deutschcraft.feature.threads.presentation.component.ThreadRow
import com.julesmeister.deutschcraft.feature.threads.presentation.component.ThreadSectionHeader

@Composable
fun ThreadListContent(
    uiState: ThreadListUiState,
    archivePullState: ThreadArchivePullState,
    listState: LazyListState,
    innerPadding: PaddingValues,
    onThreadClick: (String) -> Unit,
    onRecommendedMissionClick: () -> Unit,
    onNewThreadClick: () -> Unit,
    onArchivedThreadsClick: () -> Unit,
    onThreadActionsRequested:
        (ConversationThread) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        /*
         * The regular thread data is loading.
         *
         * Mission recommendation loading does not replace the
         * complete screen with a loading state because the
         * saved threads should remain usable.
         */
        uiState.isLoading -> {
            ThreadListLoadingState(
                modifier = modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )
            )
        }

        /*
         * Only show the empty screen if there are no saved
         * conversations, archived threads, or recommended
         * mission row.
         */
        uiState.isEmpty &&
                !uiState.hasArchivedThreads -> {

            ThreadListEmptyState(
                modifier = modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    ),
                onNewThreadClick =
                    onNewThreadClick
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )
                    .nestedScroll(
                        archivePullState
                            .nestedScrollConnection
                    ),
                state =
                    listState,
                contentPadding =
                    PaddingValues(
                        bottom =
                            THREAD_LIST_BOTTOM_PADDING
                    )
            ) {
                item(
                    key =
                        ARCHIVE_PULL_INDICATOR_KEY
                ) {
                    AnimatedVisibility(
                        visible =
                            archivePullState
                                .isPullIndicatorVisible,
                        enter =
                            expandVertically(
                                animationSpec =
                                    spring(
                                        dampingRatio =
                                            Spring
                                                .DampingRatioLowBouncy,
                                        stiffness =
                                            Spring
                                                .StiffnessMediumLow
                                    )
                            ) +
                                    fadeIn(),
                        exit =
                            shrinkVertically() +
                                    fadeOut()
                    ) {
                        ArchivePullIndicator(
                            progress =
                                archivePullState
                                    .pullProgress
                        )
                    }
                }

                item(
                    key =
                        ARCHIVED_ROW_KEY
                ) {
                    AnimatedVisibility(
                        visible =
                            uiState.hasArchivedThreads &&
                                    archivePullState
                                        .isArchiveRowRevealed,
                        enter =
                            expandVertically(
                                animationSpec =
                                    spring(
                                        dampingRatio =
                                            Spring
                                                .DampingRatioMediumBouncy,
                                        stiffness =
                                            Spring
                                                .StiffnessMediumLow
                                    )
                            ) +
                                    slideInVertically(
                                        initialOffsetY = {
                                                height ->

                                            -height / 2
                                        },
                                        animationSpec =
                                            spring(
                                                dampingRatio =
                                                    Spring
                                                        .DampingRatioMediumBouncy
                                            )
                                    ) +
                                    fadeIn(),
                        exit =
                            shrinkVertically() +
                                    fadeOut()
                    ) {
                        ArchivedThreadsRow(
                            count =
                                uiState
                                    .archivedThreadCount,
                            onClick =
                                onArchivedThreadsClick
                        )
                    }
                }

                /*
                 * The generated mission recommendation looks
                 * like a conversation already waiting for the
                 * learner.
                 *
                 * It has no section heading and no visible
                 * "mission" or "recommended" label.
                 */
                item(
                    key =
                        RECOMMENDED_MISSION_THREAD_KEY
                ) {
                    AnimatedVisibility(
                        visible =
                            uiState
                                .missionRecommendation !=
                                    null,
                        enter =
                            expandVertically(
                                expandFrom =
                                    Alignment.Top,
                                animationSpec =
                                    spring(
                                        dampingRatio =
                                            Spring
                                                .DampingRatioMediumBouncy,
                                        stiffness =
                                            Spring
                                                .StiffnessMediumLow
                                    )
                            ) +
                                    slideInVertically(
                                        initialOffsetY = {
                                                height ->

                                            -height / 3
                                        }
                                    ) +
                                    fadeIn(),
                        exit =
                            shrinkVertically(
                                shrinkTowards =
                                    Alignment.Top
                            ) +
                                    slideOutVertically(
                                        targetOffsetY = {
                                                height ->

                                            -height / 4
                                        }
                                    ) +
                                    fadeOut()
                    ) {
                        uiState
                            .missionRecommendation
                            ?.let { recommendation ->
                                RecommendedMissionThreadRow(
                                    recommendation =
                                        recommendation,
                                    isStarting =
                                        uiState
                                            .isStartingRecommendedMission,
                                    onClick =
                                        onRecommendedMissionClick
                                )
                            }
                    }
                }

                uiState.sections.forEach { section ->
                    item(
                        key =
                            "header-${section.id}"
                    ) {
                        ThreadSectionHeader(
                            title =
                                section.title
                        )
                    }

                    items(
                        items =
                            section.threads,
                        key = { thread ->
                            thread.id
                        }
                    ) { thread ->
                        ThreadRow(
                            thread =
                                thread,
                            onClick = {
                                onThreadClick(
                                    thread.id
                                )
                            },
                            onActionsRequested = {
                                onThreadActionsRequested(
                                    thread
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

private const val ARCHIVE_PULL_INDICATOR_KEY =
    "archive-pull-indicator"

private const val ARCHIVED_ROW_KEY =
    "archived-threads-row"

private const val RECOMMENDED_MISSION_THREAD_KEY =
    "recommended-mission-thread"

private val THREAD_LIST_BOTTOM_PADDING =
    100.dp