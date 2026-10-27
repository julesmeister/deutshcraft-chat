package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun MissionTopBarPager(
    missionTitle: String,
    currentObjective: String?,
    currentGuidance: String?,
    missionProgressPercentage: Int,

    /*
     * Opens the Mission Guidance bottom sheet.
     *
     * Both mission pages act as the entry point:
     *
     * - Progress page
     * - Objective/guidance page
     */
    onMissionGuidanceClick:
        () -> Unit,

    modifier: Modifier =
        Modifier
) {
    val pages =
        buildMissionTopBarPages(
            missionTitle =
                missionTitle,
            currentObjective =
                currentObjective,
            currentGuidance =
                currentGuidance,
            missionProgressPercentage =
                missionProgressPercentage
        )

    val pagerState =
        rememberPagerState(
            initialPage =
                0,
            pageCount = {
                pages.size
            }
        )

    /*
     * Restart automatic rotation whenever:
     *
     * - The learner manually moves to another page
     * - The active mission changes
     * - The current objective changes
     * - The current guidance changes
     * - The page count changes
     *
     * collectLatest cancels the old timer whenever settledPage
     * changes, giving a manually selected page a complete
     * reading period.
     */
    LaunchedEffect(
        pagerState,
        pages.size,
        missionTitle,
        currentObjective,
        currentGuidance
    ) {
        snapshotFlow {
            pagerState.settledPage
        }.collectLatest {
            if (
                pages.size <=
                1
            ) {
                return@collectLatest
            }

            delay(
                AUTO_ADVANCE_DELAY_MILLIS
                    .milliseconds
            )

            /*
             * If the learner starts swiping near the end of
             * the automatic delay, wait for that gesture to
             * finish instead of fighting the pager.
             */
            while (
                pagerState.isScrollInProgress
            ) {
                delay(
                    MANUAL_SCROLL_CHECK_DELAY_MILLIS
                        .milliseconds
                )
            }

            /*
             * Read settledPage again because a manual swipe
             * may have happened during the delay.
             */
            val currentSettledPage =
                pagerState.settledPage

            val nextPage =
                if (
                    currentSettledPage >=
                    pages.lastIndex
                ) {
                    0
                } else {
                    currentSettledPage +
                            1
                }

            pagerState.animateScrollToPage(
                page =
                    nextPage
            )
        }
    }

    /*
     * The complete active-mission area is clickable.
     *
     * Tapping either the progress page or objective page opens
     * the same learner-support bottom sheet.
     */
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onMissionGuidanceClick
                ),
        horizontalAlignment =
            Alignment.Start
    ) {
        HorizontalPager(
            state =
                pagerState,
            modifier =
                Modifier.fillMaxWidth(),
            userScrollEnabled =
                pages.size >
                        1,
            beyondViewportPageCount =
                1,
            key = { pageIndex ->
                pages[pageIndex].key
            }
        ) { pageIndex ->
            MissionTopBarPageContent(
                page =
                    pages[pageIndex],
                modifier =
                    Modifier.fillMaxWidth()
            )
        }

        if (
            pages.size >
            1
        ) {
            MissionTopBarPageIndicator(
                pageCount =
                    pages.size,
                selectedPage =
                    pagerState.currentPage,
                modifier =
                    Modifier.padding(
                        top =
                            PAGE_INDICATOR_TOP_PADDING
                    )
            )
        }
    }
}

private fun buildMissionTopBarPages(
    missionTitle: String,
    currentObjective: String?,
    currentGuidance: String?,
    missionProgressPercentage: Int
): List<MissionTopBarPage> {
    return buildList {
        add(
            MissionTopBarPage.Progress(
                title =
                    missionTitle,
                percentage =
                    missionProgressPercentage
                        .coerceIn(
                            minimumValue =
                                0,
                            maximumValue =
                                100
                        )
            )
        )

        val safeObjective =
            currentObjective
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        val safeGuidance =
            currentGuidance
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        if (
            safeObjective !=
            null
        ) {
            add(
                MissionTopBarPage.Objective(
                    objective =
                        safeObjective,
                    guidance =
                        safeGuidance
                            ?: DEFAULT_MISSION_GUIDANCE
                )
            )
        }
    }
}

@Composable
private fun MissionTopBarPageContent(
    page: MissionTopBarPage,
    modifier: Modifier =
        Modifier
) {
    when (page) {
        is MissionTopBarPage.Progress -> {
            MissionProgressPage(
                title =
                    page.title,
                percentage =
                    page.percentage,
                modifier =
                    modifier
            )
        }

        is MissionTopBarPage.Objective -> {
            MissionObjectivePage(
                objective =
                    page.objective,
                guidance =
                    page.guidance,
                modifier =
                    modifier
            )
        }
    }
}

@Composable
private fun MissionProgressPage(
    title: String,
    percentage: Int,
    modifier: Modifier =
        Modifier
) {
    val safePercentage =
        percentage.coerceIn(
            minimumValue =
                0,
            maximumValue =
                100
        )

    Column(
        modifier =
            modifier,
        horizontalAlignment =
            Alignment.Start
    ) {
        Text(
            text =
                "🎯 $title",
            modifier =
                Modifier.fillMaxWidth(),
            style =
                MaterialTheme
                    .typography
                    .titleSmall,
            fontWeight =
                FontWeight.SemiBold,
            textAlign =
                TextAlign.Start,
            maxLines =
                1,
            overflow =
                TextOverflow.Ellipsis
        )

        Spacer(
            modifier =
                Modifier.height(
                    MISSION_PROGRESS_VERTICAL_SPACING
                )
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.Start,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = {
                    safePercentage /
                            100f
                },
                modifier =
                    Modifier
                        .width(
                            MISSION_PROGRESS_WIDTH
                        )
                        .height(
                            MISSION_PROGRESS_HEIGHT
                        )
                        .clip(
                            CircleShape
                        ),
                color =
                    DeutschLime,
                trackColor =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHighest
            )

            Spacer(
                modifier =
                    Modifier.width(
                        MISSION_PERCENTAGE_SPACING
                    )
            )

            Text(
                text =
                    "$safePercentage%",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.Bold,
                color =
                    DeutschLime,
                textAlign =
                    TextAlign.Start
            )
        }
    }
}

@Composable
private fun MissionObjectivePage(
    objective: String,
    guidance: String,
    modifier: Modifier =
        Modifier
) {
    Row(
        modifier =
            modifier,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        MISSION_GUIDANCE_ICON_SIZE
                    )
                    .clip(
                        RoundedCornerShape(
                            MISSION_GUIDANCE_ICON_RADIUS
                        )
                    )
                    .background(
                        color =
                            DeutschLime.copy(
                                alpha =
                                    MISSION_GUIDANCE_ICON_ALPHA
                            )
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    "💡",
                textAlign =
                    TextAlign.Center
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    MISSION_GUIDANCE_SPACING
                )
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                ),
            horizontalAlignment =
                Alignment.Start
        ) {
            /*
             * Keep the current objective stable.
             *
             * Guidance is the more descriptive learner-facing
             * text, so only that line gets marquee behavior.
             */
            Text(
                text =
                    objective,
                modifier =
                    Modifier.fillMaxWidth(),
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground,
                textAlign =
                    TextAlign.Start,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )

            /*
             * Long guidance automatically scrolls.
             *
             * Short guidance remains stationary because
             * basicMarquee activates only when the content is
             * wider than the available layout width.
             */
            Text(
                text =
                    guidance,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top =
                                MISSION_GUIDANCE_TEXT_SPACING
                        )
                        .basicMarquee(
                            iterations =
                                Int.MAX_VALUE
                        ),
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                textAlign =
                    TextAlign.Start,
                maxLines =
                    1,
                softWrap =
                    false,
                overflow =
                    TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun MissionTopBarPageIndicator(
    pageCount: Int,
    selectedPage: Int,
    modifier: Modifier =
        Modifier
) {
    Row(
        modifier =
            modifier,
        horizontalArrangement =
            Arrangement.spacedBy(
                PAGE_INDICATOR_SPACING
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        repeat(
            pageCount
        ) { pageIndex ->
            val isSelected =
                pageIndex ==
                        selectedPage

            Box(
                modifier =
                    Modifier
                        .width(
                            if (isSelected) {
                                SELECTED_PAGE_INDICATOR_WIDTH
                            } else {
                                PAGE_INDICATOR_SIZE
                            }
                        )
                        .height(
                            PAGE_INDICATOR_SIZE
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            color =
                                if (isSelected) {
                                    DeutschLime
                                } else {
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                                        .copy(
                                            alpha =
                                                UNSELECTED_INDICATOR_ALPHA
                                        )
                                }
                        )
            )
        }
    }
}

private sealed interface MissionTopBarPage {

    val key: String

    data class Progress(
        val title: String,
        val percentage: Int
    ) : MissionTopBarPage {

        override val key: String =
            "progress:$title:$percentage"
    }

    data class Objective(
        val objective: String,
        val guidance: String
    ) : MissionTopBarPage {

        override val key: String =
            "objective:$objective:$guidance"
    }
}

private const val DEFAULT_MISSION_GUIDANCE =
    "Continue the conversation to complete this objective."

private const val AUTO_ADVANCE_DELAY_MILLIS =
    6_000L

private const val MANUAL_SCROLL_CHECK_DELAY_MILLIS =
    100L

private val MISSION_PROGRESS_VERTICAL_SPACING =
    4.dp

private val MISSION_PROGRESS_WIDTH =
    76.dp

private val MISSION_PROGRESS_HEIGHT =
    4.dp

private val MISSION_PERCENTAGE_SPACING =
    6.dp

private val MISSION_GUIDANCE_ICON_SIZE =
    32.dp

private val MISSION_GUIDANCE_ICON_RADIUS =
    12.dp

private val MISSION_GUIDANCE_SPACING =
    8.dp

private val MISSION_GUIDANCE_TEXT_SPACING =
    1.dp

private val PAGE_INDICATOR_TOP_PADDING =
    4.dp

private val PAGE_INDICATOR_SIZE =
    3.dp

private val SELECTED_PAGE_INDICATOR_WIDTH =
    12.dp

private val PAGE_INDICATOR_SPACING =
    4.dp

private const val MISSION_GUIDANCE_ICON_ALPHA =
    0.15f

private const val UNSELECTED_INDICATOR_ALPHA =
    0.28f