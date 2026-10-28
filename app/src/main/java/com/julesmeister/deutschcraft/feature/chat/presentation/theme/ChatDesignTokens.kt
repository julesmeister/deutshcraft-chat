package com.julesmeister.deutschcraft.feature.chat.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object ChatDesignTokens {

    /*
     * =======================================================
     * Screen
     * =======================================================
     */

    const val CopyConfirmationDurationMillis =
        1_100L

    /*
     * =======================================================
     * Bottom bar
     * =======================================================
     */

    val BottomBarHorizontalPadding =
        12.dp

    val BottomBarTopPadding =
        14.dp

    val BottomBarBottomPadding =
        10.dp

    val OfferContentSpacing =
        10.dp

    val SuggestionComposerSpacing =
        8.dp

    const val BottomBarFadeMidStop =
        0.35f

    const val LayoutAnimationMillis =
        240

    const val OfferEnterDurationMillis =
        340

    const val OfferExitDurationMillis =
        220

    const val OfferEnterScale =
        0.96f

    const val OfferExitScale =
        0.98f

    const val OfferEnterSlideDivisor =
        3

    const val OfferExitSlideDivisor =
        4

    /*
     * =======================================================
     * Message list
     * =======================================================
     */

    val MessageListTopPadding =
        4.dp

    val MessageListBottomBreathingRoom =
        8.dp

    val MessageHorizontalPadding =
        12.dp

    val MessageGroupStartSpacing =
        12.dp

    val MessageGroupedSpacing =
        2.dp

    val BottomAnchorHeight =
        1.dp

    const val MessageGroupTimeWindowMillis =
        5 * 60 * 1_000L

    const val TypingIndicatorKey =
        "typing-indicator"

    const val BottomAnchorKey =
        "bottom-anchor"

    /*
     * =======================================================
     * Date separator
     * =======================================================
     */

    val DateSeparatorTopPadding =
        16.dp

    val DateSeparatorBottomPadding =
        4.dp

    val DateSeparatorHorizontalPadding =
        12.dp

    val DateSeparatorVerticalPadding =
        4.dp

    val DateSeparatorShape =
        RoundedCornerShape(
            percent = 50
        )

    const val DateSeparatorBackgroundAlpha =
        0.85f

    /*
     * =======================================================
     * Mission top bar pager
     * =======================================================
     */

    const val DefaultMissionGuidance =
        "Continue the conversation to complete this objective."

    const val MissionPagerAutoAdvanceDelayMillis =
        6_000L

    const val MissionPagerScrollCheckDelayMillis =
        100L

    const val MissionPagerBeyondViewportPageCount =
        1

    const val MissionProgressMinimum =
        0

    const val MissionProgressMaximum =
        100

    const val MissionTitleMaxLines =
        1

    const val MissionObjectiveMaxLines =
        1

    const val MissionGuidanceMaxLines =
        1

    const val MissionTitleIcon =
        "🎯"

    const val MissionGuidanceIcon =
        "💡"

    val MissionProgressVerticalSpacing =
        4.dp

    val MissionProgressWidth =
        76.dp

    val MissionProgressHeight =
        4.dp

    val MissionPercentageSpacing =
        6.dp

    val MissionGuidanceIconSize =
        32.dp

    val MissionGuidanceIconRadius =
        12.dp

    val MissionGuidanceSpacing =
        8.dp

    val MissionGuidanceTextSpacing =
        1.dp

    val MissionPageIndicatorTopPadding =
        4.dp

    val MissionPageIndicatorSize =
        3.dp

    val MissionSelectedPageIndicatorWidth =
        12.dp

    val MissionPageIndicatorSpacing =
        4.dp

    const val MissionGuidanceIconDarkAlpha =
        0.15f

    const val MissionGuidanceIconLightAlpha =
        0.22f

    const val MissionUnselectedIndicatorAlpha =
        0.38f

    /*
     * Lime is difficult to read as text on white.
     *
     * Use a darker lime-derived accent for foreground content
     * in light mode while retaining DeutschLime for filled
     * progress bars and decorative elements.
     */
    val MissionLightAccent =
        Color(0xFF5A7A00)

    /*
     * =======================================================
     * Composer
     * =======================================================
     */

    val ComposerShape =
        RoundedCornerShape(
            percent = 50
        )

    val ComposerMinHeight =
        58.dp

    val ComposerStartPadding =
        8.dp

    val ComposerEndPadding =
        7.dp

    val ComposerVerticalPadding =
        6.dp

    val ComposerMultilineVerticalPadding =
        10.dp

    val ComposerTextHorizontalPadding =
        10.dp

    val ComposerTextVerticalPadding =
        1.dp

    val ComposerTextSendSpacing =
        5.dp

    val ComposerTextHighlightRadius =
        22.dp

    val ComposerTextHighlightShape =
        RoundedCornerShape(
            ComposerTextHighlightRadius
        )

    const val ComposerMaxLines =
        5

    const val ComposerAnimationMillis =
        180

    val ComposerInnerMinHeight =
        ComposerMinHeight -
                ComposerVerticalPadding * 2

    /*
     * =======================================================
     * Composer adaptive shape
     * =======================================================
     */

    val ComposerSingleLineCornerRadius =
        29.dp

    val ComposerMultilineCornerRadius =
        18.dp

    val ComposerMultilineBottomEndCornerRadius =
        29.dp

    const val ComposerShapeAnimationMillis =
        180

    /*
     * =======================================================
     * Animated glass reflection
     * =======================================================
     */

    const val GlassReflectionDurationMillis =
        1_400

    const val GlassReflectionStartFraction =
        -0.30f

    const val GlassReflectionEndFraction =
        1.30f

    const val GlassReflectionBandWidthFraction =
        0.18f

    val GlassReflectionStrokeWidth =
        1.6.dp

    const val GlassReflectionDarkAlpha =
        0.38f

    const val GlassReflectionLightAlpha =
        0.62f

    const val GlassReflectionRestingStrength =
        0.72f

    const val GlassReflectionFocusedStrength =
        1f

    const val GlassReflectionSoftAlphaMultiplier =
        0.36f

    const val GlassReflectionSoftStop =
        0.28f

    const val GlassReflectionCenterStop =
        0.50f

    const val GlassReflectionEndSoftStop =
        0.72f

    /*
     * =======================================================
     * Theme detection
     * =======================================================
     */

    const val DarkLuminanceThreshold =
        0.5f

    /*
     * =======================================================
     * Liquid glass: shared geometry
     * =======================================================
     */

    val GlassBorderWidth =
        1.dp

    const val GlassBorderStateStop =
        0.40f

    /*
     * =======================================================
     * Liquid glass: dark mode
     * =======================================================
     */

    const val GlassTopAlpha =
        0.78f

    const val GlassMiddleAlpha =
        0.64f

    const val GlassBottomAlpha =
        0.72f

    const val GlassHighlightEdgeAlpha =
        0.08f

    const val GlassHighlightCenterAlpha =
        0.025f

    const val GlassBorderHighlightAlpha =
        0.28f

    const val GlassBorderBottomAlpha =
        0.05f

    const val GlassRestingBorderAlpha =
        0.30f

    const val GlassFocusedBorderAlpha =
        0.40f

    const val PlaceholderAlpha =
        0.70f

    val GlassShadowElevation =
        0.dp

    /*
     * =======================================================
     * Real liquid glass
     * =======================================================
     */

    val LiquidBlurRadius =
        2.dp

    val LiquidLensHeight =
        12.dp

    val LiquidLensAmount =
        24.dp

    const val LiquidDarkSurfaceAlpha =
        0.28f

    const val LiquidLightSurfaceAlpha =
        0.55f

    const val BottomBarFadeMidAlpha =
        0.25f

    const val BottomBarFadeEndAlpha =
        0.55f

    /*
     * =======================================================
     * Liquid glass: light mode
     * =======================================================
     */

    const val GlassLightTopAlpha =
        0.97f

    const val GlassLightMiddleAlpha =
        0.92f

    const val GlassLightBottomAlpha =
        0.95f

    const val GlassLightHighlightEdgeAlpha =
        0f

    const val GlassLightHighlightCenterAlpha =
        0f

    const val GlassLightBorderHighlightAlpha =
        0.90f

    const val GlassLightBorderBottomAlpha =
        0.04f

    const val GlassLightRestingBorderAlpha =
        0.10f

    const val GlassLightFocusedBorderAlpha =
        0.75f

    const val GlassLightPlaceholderAlpha =
        0.80f

    const val GlassLightShadowAlpha =
        0.10f

    val GlassLightShadowElevation =
        6.dp

    /*
     * =======================================================
     * Send button
     * =======================================================
     */

    val SendButtonSize =
        42.dp

    val SendIconSize =
        20.dp

    val SendProgressSize =
        18.dp

    val SendProgressStroke =
        2.dp

    val SendBorderWidth =
        1.dp

    const val SendGlassTopAlpha =
        0.92f

    const val SendGlassBottomAlpha =
        0.88f

    const val SendBorderTopAlpha =
        0.36f

    const val SendBorderBottomAlpha =
        0.18f

    const val SendDisabledBackgroundAlpha =
        0.78f

    const val SendDisabledContentAlpha =
        0.55f

    const val SendNormalScale =
        1f

    const val SendPressedScale =
        0.88f

    const val SendScaleAnimationMillis =
        100

    const val SendDisabledIconAlpha =
        0.38f

    const val SendActiveIconRotation =
        0f

    const val SendInactiveIconRotation =
        -35f

    const val SendActiveIconScale =
        1f

    const val SendInactiveIconScale =
        0.85f

    const val SendButtonSpringDampingRatio =
        0.7f

    const val SendButtonSpringStiffness =
        650f

    const val SendIconSpringDampingRatio =
        0.6f

    const val SendIconSpringStiffness =
        400f

    const val SendColorAnimationMillis =
        220

    const val SendLightDisabledIconAlpha =
        0.42f

    /*
     * =======================================================
     * Besser refinement: geometry
     * =======================================================
     */

    val RefinementRadius =
        18.dp

    val RefinementShape =
        RoundedCornerShape(
            RefinementRadius
        )

    val RefinementHorizontalMargin =
        5.dp

    val RefinementComposerSpacing =
        7.dp

    val RefinementBorderWidth =
        1.dp

    val RefinementContentHorizontalPadding =
        12.dp

    val RefinementContentVerticalPadding =
        9.dp

    val RefinementIconContainerSize =
        32.dp

    val RefinementIconSize =
        17.dp

    val RefinementIconTextSpacing =
        10.dp

    val RefinementTextSpacing =
        2.dp

    val RefinementCancelSpacing =
        8.dp

    val RefinementCancelSize =
        30.dp

    val RefinementCancelIconSize =
        17.dp

    const val RefinementInstructionMaxLines =
        2

    const val RefinementAnimationMillis =
        220

    const val RefinementSlideOffsetDivisor =
        3

    /*
     * =======================================================
     * Besser refinement: dark mode
     * =======================================================
     */

    const val RefinementBorderAlpha =
        0.52f

    const val RefinementBackgroundAlpha =
        0.92f

    const val RefinementHeaderBorderAlpha =
        0.18f

    const val RefinementIconBackgroundAlpha =
        0.14f

    const val RefinementCancelBackgroundAlpha =
        0.72f

    /*
     * =======================================================
     * Besser refinement: light mode
     * =======================================================
     */

    const val RefinementLightBorderAlpha =
        0.85f

    const val RefinementLightBackgroundAlpha =
        0.97f

    const val RefinementLightHeaderBorderAlpha =
        0.06f

    const val RefinementLightIconBackgroundAlpha =
        0.20f

    const val RefinementLightCancelBackgroundAlpha =
        0.90f

    /*
     * =======================================================
     * Besser challenge bubble
     * =======================================================
     */

    const val BesserChallengeWidthFraction =
        0.88f

    val BesserChallengeRadius =
        18.dp

    val BesserChallengePadding =
        14.dp

    val BesserChallengeTopSpacing =
        2.dp

    val BesserChallengeBottomSpacing =
        4.dp

    val BesserChallengeIconContainerSize =
        30.dp

    val BesserChallengeIconSize =
        17.dp

    val BesserChallengeHeaderSpacing =
        8.dp

    val BesserChallengeInstructionTopPadding =
        10.dp

    val BesserChallengeHintTopPadding =
        8.dp

    val BesserChallengeActionsTopPadding =
        12.dp

    val BesserChallengeActionSpacing =
        6.dp

    val BesserChallengeActionRadius =
        50.dp

    val BesserChallengeActionHorizontalPadding =
        10.dp

    val BesserChallengeActionVerticalPadding =
        7.dp

    val BesserChallengeActionIconSpacing =
        5.dp

    val BesserChallengeActionIconSize =
        16.dp

    const val BesserChallengeIconBackgroundAlpha =
        0.14f

    const val BesserChallengeAnimationMillis =
        220

    const val BesserChallengeSlideOffsetDivisor =
        3
}