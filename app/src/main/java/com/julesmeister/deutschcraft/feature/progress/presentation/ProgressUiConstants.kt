package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*
 * ===========================================================
 * PROGRESS SCREEN
 * ===========================================================
 */

internal val ProgressTopBarEdgePadding =
    8.dp

internal val ProgressTopBarButtonSize =
    40.dp

internal val ProgressTopBarTitleStartPadding =
    12.dp

internal val ProgressLoadingTextTopPadding =
    16.dp

internal val ProgressEmptyContentPadding =
    32.dp

internal val ProgressEmptyIconContainerSize =
    68.dp

internal val ProgressEmptyTitleTopPadding =
    18.dp

internal val ProgressEmptyMessageTopPadding =
    8.dp

internal const val ProgressLanguageButtonAlpha =
    0.12f

internal const val ProgressLanguageAnimationDurationMillis =
    180

internal const val ProgressDefaultEmptyMessageGerman =
    "Noch keine Fortschrittsdaten verfügbar."

internal const val ProgressDefaultEmptyMessageEnglish =
    "No progress data available yet."

/*
 * ===========================================================
 * PROGRESS CONTENT
 * ===========================================================
 */

internal val ProgressContentHorizontalPadding =
    20.dp

internal val ProgressContentTopPadding =
    16.dp

internal val ProgressContentBottomPadding =
    40.dp

internal val ProgressContentItemSpacing =
    16.dp

internal val ProgressIntroductionDescriptionTopPadding =
    6.dp

/*
 * ===========================================================
 * CEFR BENTO
 * ===========================================================
 */

internal const val CefrCurrentLevelWidthFraction =
    0.42f

internal val CefrBentoTotalHeight =
    272.dp

internal val CefrBentoTopHeight =
    152.dp

internal val CefrBentoGap =
    8.dp

internal val CefrBentoOuterRadius =
    22.dp

internal val CefrBentoInnerRadius =
    18.dp

internal val CefrBentoTilePadding =
    18.dp

internal val CefrProgressRowVerticalPadding =
    14.dp

internal val CefrProgressBarHeight =
    10.dp

internal val CefrProgressBarTopPadding =
    8.dp

internal val CefrReadinessMessageTopPadding =
    8.dp

/*
 * Current-level tile deliberately uses an inverted light
 * treatment even when DeutschCraft is in its dark theme.
 */
internal val CefrCurrentLevelBackground =
    Color(0xFFF4F4F2)

internal val CefrCurrentLevelTextPrimary =
    Color(0xFF111111)

internal val CefrCurrentLevelTextSecondary =
    Color(0xFF111111).copy(
        alpha = 0.62f
    )

/*
 * Dark content displayed over DeutschLime.
 */
internal val CefrOnLimePrimary =
    Color(0xFF111111)

internal val CefrOnLimeSecondary =
    Color(0xFF111111).copy(
        alpha = 0.68f
    )

internal val CefrOnLimeTrack =
    Color(0xFF111111).copy(
        alpha = 0.14f
    )


/*
 * ===========================================================
 * COMPLETED MISSIONS
 * ===========================================================
 */

internal val CompletedMissionsPanelCornerRadius =
    28.dp

internal val CompletedMissionsPanelPadding =
    20.dp

internal val CompletedMissionsSummaryIconSize =
    48.dp

internal val CompletedMissionsSummaryIconRadius =
    16.dp

internal val CompletedMissionsSummaryContentSpacing =
    12.dp

internal val CompletedMissionsSummaryDescriptionTopPadding =
    2.dp

internal val CompletedMissionsHeaderContentSpacing =
    20.dp

internal val CompletedMissionsListTopSpacing =
    12.dp

internal val CompletedMissionRowSpacing =
    10.dp

internal val CompletedMissionRowRadius =
    20.dp

internal val CompletedMissionRowPadding =
    14.dp

internal val CompletedMissionIconSize =
    42.dp

internal val CompletedMissionIconSpacing =
    12.dp

internal val CompletedMissionCheckSize =
    20.dp

internal val CompletedMissionMetadataTopPadding =
    3.dp

internal val CompletedMissionProgressTopPadding =
    8.dp

internal val CompletedMissionProgressHeight =
    5.dp

internal val CompletedMissionEmptyIconSpacing =
    12.dp

internal const val CompletedMissionsSummaryIconBackgroundAlpha =
    0.14f

internal const val CompletedMissionIconBackgroundAlpha =
    0.14f

internal const val CompletedMissionsPanelEnterDurationMillis =
    500

internal const val CompletedMissionCountAnimationDurationMillis =
    300

internal const val CompletedMissionProgressAnimationMillis =
    850