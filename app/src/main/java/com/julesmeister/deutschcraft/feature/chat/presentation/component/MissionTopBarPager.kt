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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens as Tokens
import com.julesmeister.deutschcraft.feature.settings.presentation.LocalUserSettings
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
    onMissionGuidanceClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false
) {
    val reduceAnimations = LocalUserSettings.current.reduceAnimations
    val pages =
        buildMissionTopBarPages(
            missionTitle = missionTitle,
            currentObjective = currentObjective,
            currentGuidance = currentGuidance,
            missionProgressPercentage = missionProgressPercentage
        )

    val pagerState =
        rememberPagerState(
            initialPage = 0,
            pageCount = {
                pages.size
            }
        )

    LaunchedEffect(
        pagerState,
        pages.size,
        missionTitle,
        currentObjective,
        currentGuidance,
        reduceAnimations,
        isExpanded
    ) {
        if (reduceAnimations || isExpanded) {
            return@LaunchedEffect
        }
        snapshotFlow {
            pagerState.settledPage
        }.collectLatest {
            if (pages.size <= 1) {
                return@collectLatest
            }

            delay(
                Tokens
                    .MissionPagerAutoAdvanceDelayMillis
                    .milliseconds
            )

            while (pagerState.isScrollInProgress) {
                delay(
                    Tokens
                        .MissionPagerScrollCheckDelayMillis
                        .milliseconds
                )
            }

            val currentSettledPage =
                pagerState.settledPage

            val nextPage =
                if (
                    currentSettledPage >=
                    pages.lastIndex
                ) {
                    0
                } else {
                    currentSettledPage + 1
                }

            pagerState.animateScrollToPage(
                page = nextPage
            )
        }
    }

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
                pages.size > 1 && !isExpanded,
            beyondViewportPageCount =
                Tokens.MissionPagerBeyondViewportPageCount,
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

        if (pages.size > 1) {
            MissionTopBarPageIndicator(
                pageCount =
                    pages.size,
                selectedPage =
                    pagerState.currentPage,
                modifier =
                    Modifier.padding(
                        top =
                            Tokens
                                .MissionPageIndicatorTopPadding
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
                                Tokens.MissionProgressMinimum,
                            maximumValue =
                                Tokens.MissionProgressMaximum
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

        if (safeObjective != null) {
            add(
                MissionTopBarPage.Objective(
                    objective =
                        safeObjective,
                    guidance =
                        safeGuidance
                            ?: Tokens.DefaultMissionGuidance
                )
            )
        }
    }
}

@Composable
private fun MissionTopBarPageContent(
    page: MissionTopBarPage,
    modifier: Modifier = Modifier
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
    modifier: Modifier = Modifier
) {
    val colors =
        rememberMissionTopBarColors()

    val safePercentage =
        percentage.coerceIn(
            minimumValue =
                Tokens.MissionProgressMinimum,
            maximumValue =
                Tokens.MissionProgressMaximum
        )

    Column(
        modifier =
            modifier,
        horizontalAlignment =
            Alignment.Start
    ) {
        Text(
            text =
                "${Tokens.MissionTitleIcon} $title",
            modifier =
                Modifier.fillMaxWidth(),
            style =
                MaterialTheme
                    .typography
                    .titleSmall,
            fontWeight =
                FontWeight.SemiBold,
            color =
                colors.primaryText,
            textAlign =
                TextAlign.Start,
            maxLines =
                Tokens.MissionTitleMaxLines,
            overflow =
                TextOverflow.Ellipsis
        )

        Spacer(
            modifier =
                Modifier.height(
                    Tokens
                        .MissionProgressVerticalSpacing
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
                    safePercentage.toFloat() /
                            Tokens.MissionProgressMaximum.toFloat()
                },
                modifier =
                    Modifier
                        .width(
                            Tokens.MissionProgressWidth
                        )
                        .height(
                            Tokens.MissionProgressHeight
                        )
                        .clip(
                            CircleShape
                        ),
                color =
                    DeutschLime,
                trackColor =
                    colors.progressTrack
            )

            Spacer(
                modifier =
                    Modifier.width(
                        Tokens
                            .MissionPercentageSpacing
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
                    colors.accentText,
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
    modifier: Modifier = Modifier
) {
    val colors =
        rememberMissionTopBarColors()

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
                        Tokens
                            .MissionGuidanceIconSize
                    )
                    .clip(
                        RoundedCornerShape(
                            Tokens
                                .MissionGuidanceIconRadius
                        )
                    )
                    .background(
                        colors.guidanceContainer
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    Tokens.MissionGuidanceIcon,
                textAlign =
                    TextAlign.Center
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    Tokens
                        .MissionGuidanceSpacing
                )
        )

        Column(
            modifier =
                Modifier.weight(1f),
            horizontalAlignment =
                Alignment.Start
        ) {
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
                    colors.primaryText,
                textAlign =
                    TextAlign.Start,
                maxLines =
                    Tokens.MissionObjectiveMaxLines,
                overflow =
                    TextOverflow.Ellipsis
            )

            if (LocalUserSettings.current.showMissionHints) {
                Text(
                    text =
                        guidance,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                top =
                                    Tokens
                                        .MissionGuidanceTextSpacing
                            )
                            .basicMarquee(
                                iterations =
                                    if (LocalUserSettings.current.reduceAnimations) 0 else Int.MAX_VALUE
                            ),
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,
                    color =
                        colors.secondaryText,
                    textAlign =
                        TextAlign.Start,
                    maxLines =
                        Tokens.MissionGuidanceMaxLines,
                    softWrap =
                        false,
                    overflow =
                        TextOverflow.Clip
                )
            }
        }
    }
}

@Composable
private fun MissionTopBarPageIndicator(
    pageCount: Int,
    selectedPage: Int,
    modifier: Modifier = Modifier
) {
    val colors =
        rememberMissionTopBarColors()

    Row(
        modifier =
            modifier,
        horizontalArrangement =
            Arrangement.spacedBy(
                Tokens
                    .MissionPageIndicatorSpacing
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
                                Tokens
                                    .MissionSelectedPageIndicatorWidth
                            } else {
                                Tokens
                                    .MissionPageIndicatorSize
                            }
                        )
                        .height(
                            Tokens
                                .MissionPageIndicatorSize
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            color =
                                if (isSelected) {
                                    colors.accentText
                                } else {
                                    colors
                                        .secondaryText
                                        .copy(
                                            alpha =
                                                Tokens
                                                    .MissionUnselectedIndicatorAlpha
                                        )
                                }
                        )
            )
        }
    }
}

private data class MissionTopBarColors(
    val primaryText: Color,
    val secondaryText: Color,
    val accentText: Color,
    val progressTrack: Color,
    val guidanceContainer: Color
)

@Composable
private fun rememberMissionTopBarColors(): MissionTopBarColors {
    val colorScheme =
        MaterialTheme.colorScheme

    val isDark =
        colorScheme
            .background
            .luminance() <
                Tokens.DarkLuminanceThreshold

    return if (isDark) {
        MissionTopBarColors(
            primaryText =
                colorScheme.onBackground,
            secondaryText =
                colorScheme.onSurfaceVariant,
            accentText =
                DeutschLime,
            progressTrack =
                colorScheme.surfaceContainerHighest,
            guidanceContainer =
                DeutschLime.copy(
                    alpha =
                        Tokens
                            .MissionGuidanceIconDarkAlpha
                )
        )
    } else {
        MissionTopBarColors(
            primaryText =
                colorScheme.onSurface,
            secondaryText =
                colorScheme.onSurfaceVariant,
            accentText =
                Tokens.MissionLightAccent,
            progressTrack =
                colorScheme.surfaceContainerHighest,
            guidanceContainer =
                DeutschLime.copy(
                    alpha =
                        Tokens
                            .MissionGuidanceIconLightAlpha
                )
        )
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