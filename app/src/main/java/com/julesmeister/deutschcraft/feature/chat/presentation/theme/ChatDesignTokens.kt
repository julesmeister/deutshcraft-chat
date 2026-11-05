package com.julesmeister.deutschcraft.feature.chat.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Chat appearance and motion values. Component-specific tokens are grouped below;
 * non-visual presentation constants live in ChatPresentationTokens.
 */
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

    // Trim excess font padding while preserving a centered line box.
    val ComposerCenteredLineHeightStyle =
        LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both
        )

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

    object AnimatedTopBarTitle {
        const val ObjectiveCompletionDisplayMillis = 1_650L
        const val AdaptiveFeedbackDisplayMillis = 3_000L
        val TopBarContentMinimumHeight = 42.dp
        val OnlineDotSpacing = 5.dp
        val NormalSubtitleTopPadding = 1.dp
        val StatusIconSize = 32.dp
        const val TypingDotsSizeScale = 0.6f
        val StatusIconCornerRadius = 12.dp
        val StatusContentSpacing = 8.dp
        const val AdaptiveIconBackgroundAlpha = 0.15f
        const val LightTertiaryContainerAlpha = 0.12f
        const val StatusSizeAnimationMillis = 380
        const val StatusEnterDurationMillis = 360
        const val StatusExitDurationMillis = 220
        const val StatusEnterScale = 0.97f
        const val StatusExitScale = 0.98f
    }

    object AssistantStatus {
        val StatusSpacing = 5.dp
        val StatusDotSize = 6.dp
        val StatusFontSize = 11.sp
        val StatusLineHeight = 13.sp
    }

    object RefinementFeedback {
        const val FeedbackMaxLines = 3
        val FeedbackTopPadding = 2.dp
        val EvaluatingStrokeWidth = 2.dp
    }

    object MessageListAnimation {
        const val TypingIndicatorKey = "assistant-typing-indicator"
        const val BottomAnchorKey = "chat-bottom-anchor"
        val BottomAnchorHeight = 8.dp
        const val KeyboardScrollDelayMillis = 240L
        const val ContentScrollDelayMillis = 180L
        const val MessageEntranceStartDelayMillis = 35L
        const val MessageEnterDurationMillis = 340
        const val MessageExitDurationMillis = 180
        const val MessageEnterScale = 0.97f
        const val MessageExitScale = 0.98f
        const val MessageVerticalOffsetFraction = 0.28f
    }

    object MessageListScroll {
        const val ChatInitialBottomSettleDelayMillis = 120L
        const val ChatKeyboardScrollDelayMillis = 80L
        const val ChatContentScrollDelayMillis = 80L
        const val ChatMessageFinalCorrectionDelayMillis = 120L
        const val ChatSuggestionScrollDelayMillis = 160L
        const val ChatOverlayScrollSettleDelayMillis = 80L
        const val ChatOverlayFinalCorrectionDelayMillis = 120L
        const val ChatBottomVisibilityTolerancePx = 8
    }

    object PresenceIndicator {
        val OnlineIndicatorContainerSize = 18.dp
        val OnlineDotSize = 7.dp
        val OnlineHaloMaxSize = 17.dp
        val OnlineCoreOutlineWidth = 0.65.dp
        val LightModeOnlineGreen = Color(0xFF2A9D55)
        val LightModeOnlineHalo = Color(0xFF35AA61)
        const val OnlinePulseDurationMillis = 1_800
        const val OnlineHaloStartScale = 0f
        const val OnlineHaloEndScale = 1f
        const val OnlineHaloStartAlpha = 0.32f
        const val OnlineCoreGlowAlpha = 0.16f
        const val OnlineCoreGlowScale = 1.45f
        const val OnlineCoreOutlineAlpha = 0.34f
        const val OnlineHighlightAlpha = 0.42f
        const val OnlineHighlightRadiusFactor = 0.23f
        const val OnlineHighlightOffsetFactor = 0.27f
    }

    object MissionDrawer {
        const val MaxHeightFraction = 0.62f
        val FallbackPanelHeight = 360.dp
        const val ScrimMaxAlpha = 0.36f
        const val ExpandThreshold = 0.4f
        val FlingVelocity = 400.dp
        const val SpringDampingRatio = 0.86f
        const val SpringStiffness = 420f

        val HandleTouchWidth = 112.dp
        val HandleTouchHeight = 40.dp
        val HandleWidth = 72.dp
        val HandleHeight = 22.dp
        val HandleCornerRadius = 14.dp
        val HandleIconSize = 18.dp
        val HandleFilletRadius = 10.dp
        val ShadowRadius = 7.dp
        val ShadowOffsetY = 1.dp
        const val ShadowLightAlpha = 0.09f
        const val ShadowDarkAlpha = 0.32f
        const val HandleIconAlpha = 0.72f
        const val HandleRotationDegrees = 180f

        val ContentPadding = 18.dp
        val SectionSpacing = 18.dp
        val ItemSpacing = 8.dp
        val SectionIconSize = 16.dp
        val CardCornerRadius = 18.dp
        val CardPadding = 14.dp
        val GrammarBadgeSize = 28.dp
        val GrammarBadgeCornerRadius = 9.dp

        val ProgressRingSize = 58.dp
        val ProgressRingStroke = 5.dp
        val BadgeHorizontalPadding = 10.dp
        val BadgeVerticalPadding = 4.dp

        val StepDotSize = 24.dp
        val StepIconSize = 14.dp
        val StepLineWidth = 2.dp
        val StepLineMinHeight = 12.dp
        val StepVerticalPadding = 4.dp
        const val UpcomingStepAlpha = 0.6f

        val VocabularyCardWidth = 176.dp
        const val AccentContainerDarkAlpha = 0.14f
        const val AccentContainerLightAlpha = 0.32f

        val SkeletonLineHeight = 12.dp
        val SkeletonCardHeight = 72.dp
        const val SkeletonCardCount = 2
        const val ShimmerMillis = 1100
        const val ShimmerMinAlpha = 0.35f
        const val ShimmerMaxAlpha = 0.85f
        const val SkeletonShortLineFraction = 0.55f
    }

    object TopBar {
        val TopBarIconButtonSize = 40.dp
        val TopBarEdgePadding = 8.dp
        val TitleStartPadding = 8.dp
        val TitleEndPadding = 2.dp
        val CopyConfirmationSpacing = 6.dp
        val CopyConfirmationIconContainerSize = 22.dp
        val CopyConfirmationCheckSize = 13.dp
        val CopyConfirmationStartPadding = 5.dp
        val CopyConfirmationEndPadding = 9.dp
        val CopyConfirmationVerticalPadding = 4.dp
        val CopyConfirmationContentSpacing = 5.dp
        val LightModeBackButtonBackground = Color(0xFFE3E4E8)
        val LightModeBackButtonPressedBackground = Color(0xFFD3D5DB)
        const val FullCornerPercent = 50
        const val NormalIconScale = 1f
        const val PressedIconScale = 0.9f
        const val NormalButtonAlpha = 0.82f
        const val IconPressAnimationDurationMillis = 130
        const val CopyConfirmationInitialScale = 0.55f
        const val CopyConfirmationInitialRotation = -10f
        const val CopyConfirmationRotationDamping = 0.58f
        const val CopyConfirmationRotationStiffness = 440f
        const val CopyConfirmationEnterMillis = 160
        const val CopyConfirmationExitMillis = 170
    }

    object ReplySuggestions {
        val MinimumChipHeight = 38.dp
        val MaximumChipWidth = 280.dp
        const val NormalChipScale = 1f
        const val PressedChipScale = 0.96f
        const val ChipEnterScale = 0.92f
        const val ChipExitScale = 0.96f
        const val ChipPressDampingRatio = 0.72f
        const val ChipPressStiffness = 620f
        const val SuggestionStaggerMillis = 90L
        const val ContainerEnterMillis = 280
        const val ContainerExitMillis = 180
        const val ChipEnterMillis = 320
        const val ChipExitMillis = 160
        const val RevealDelayMillis = 140L
    }

    object MessageBubble {
        const val MaximumBubbleWidthFraction = 0.78f
        val MaximumBubbleWidthCap = 420.dp
        val CopiedIndicatorSpacing = 8.dp
        val CopiedIndicatorContainerSize = 42.dp
        val CopiedBadgeSize = 30.dp
        val CopiedCheckSize = 17.dp
        val CopiedBadgeElevation = 5.dp
        const val CopiedBadgeInitialScale = 0.35f
        const val CopiedBadgeInitialRotation = 22f
        const val CopiedBadgeRotationDamping = 0.55f
        const val CopiedBadgeRotationStiffness = 430f
        const val CopiedGlowInitialScale = 0.80f
        const val CopiedGlowFinalScale = 1.65f
        const val CopiedGlowInitialAlpha = 0.28f
        const val CopiedGlowDurationMillis = 330
        const val CopiedIndicatorDurationMillis = 900L
        const val CopiedIndicatorEnterMillis = 140
        const val CopiedIndicatorExitMillis = 180
    }

    object BubbleContainer {
        val MinimumBubbleWidth = 64.dp
        val DefaultMaximumBubbleWidth = 320.dp
        val BorderWidth = 0.75.dp
        const val PressedBubbleScale = 0.965f
    }

    object BubbleContent {
        const val TimestampAlpha = 0.55f
        const val CorrectionHighlightAlpha = 0.18f
        val MessageLineHeight = 22.sp
    }

    object BubbleStyle {
        val SourceBubbleCorner = 20.dp
        val SourceBubbleTailCorner = 6.dp
        val AttachmentCorner = 0.dp
        const val ColorTransitionMillis = 450
    }

    object CorrectionContent {
        const val CollapsedMaxLines = 2
        const val OriginalTextAlpha = 0.52f
        const val OriginalLabelAlpha = 0.48f
        val OriginalSectionTopPadding = 8.dp
        val OriginalLabelSpacing = 4.dp
        val OriginalTextTopPadding = 2.dp
        val OriginalLabelIconSize = 9.sp
        val OriginalLabelSize = 9.sp
        val OriginalLabelLetterSpacing = 0.7.sp
        val OriginalTextSize = 12.sp
        val OriginalLineHeight = 16.sp
    }

    object ObjectiveCompletion {
        val CompletionIconSize = 32.dp
        val CompletionIconCornerRadius = 12.dp
        val CompletionContentSpacing = 8.dp
        const val CompletionIconBackgroundAlpha = 0.16f
        const val CompletionInitialScale = 0.72f
        const val CompletionFinalScale = 1f
        const val CompletionScaleDurationMillis = 320
    }

    object MissionOffer {
        val CardShape = RoundedCornerShape(22.dp)
        val CardElevation = 12.dp
        val CardHorizontalPadding = 16.dp
        val CardVerticalPadding = 14.dp
        val IconContainerSize = 38.dp
        val IconPadding = 7.dp
        val HeaderHorizontalSpacing = 10.dp
        const val IconBackgroundAlpha = 0.16f
        const val PillShapePercent = 50
        const val DifficultyBackgroundAlpha = 0.14f
        val DifficultyHorizontalPadding = 9.dp
        val DifficultyVerticalPadding = 5.dp
        val DescriptionTopSpacing = 10.dp
        const val DescriptionMaxLines = 3
        val ActionsTopSpacing = 12.dp
        val ActionSpacing = 8.dp
        val ButtonHorizontalPadding = 14.dp
        val ButtonVerticalPadding = 9.dp
        const val NormalButtonScale = 1f
        const val PressedButtonScale = 0.95f
        const val ButtonDampingRatio = 0.74f
        const val ButtonStiffness = 620f
        const val DisabledPrimaryAlpha = 0.38f
        val LoadingIndicatorSize = 16.dp
        val LoadingIndicatorStrokeWidth = 2.dp
        val LoadingTextSpacing = 7.dp
        const val CardSizeAnimationMillis = 260
    }

    object MissionVariation {
        val DialogHorizontalMargin = 20.dp
        val DialogContentPadding = 20.dp
        val DialogShape = RoundedCornerShape(28.dp)
        val DialogPromptTopPadding = 20.dp
        val DialogPromptBottomPadding = 10.dp
        val HeaderIconSize = 48.dp
        val HeaderIconContentSize = 23.dp
        val HeaderContentPadding = 12.dp
        val HeaderSubtitleTopPadding = 3.dp
        val CloseButtonSize = 38.dp
        val CloseIconSize = 19.dp
        val ActionItemShape = RoundedCornerShape(20.dp)
        val ActionItemSpacing = 8.dp
        val ActionHorizontalPadding = 4.dp
        val ActionVerticalPadding = 10.dp
        val ActionContentSpacing = 14.dp
        val ActionIconSize = 46.dp
        val ActionIconContentSize = 22.dp
        val ActionDescriptionTopPadding = 2.dp
    }

    object ProgressStreak {
        val MinimumButtonSize = 40.dp
        val ProgressIconSize = 22.dp
        val HorizontalPadding = 10.dp
        val VerticalPadding = 8.dp
        val StreakHorizontalPadding = 1.dp
        val StreakTextSpacing = 4.dp
        const val ProgressDisplayDurationMillis = 4_000L
        const val StreakDisplayDurationMillis = 3_000L
        const val ContainerAnimationDurationMillis = 380
        const val ContentEnterDurationMillis = 320
        const val ContentExitDurationMillis = 220
        const val ContentEnterScale = 0.88f
        const val ContentExitScale = 0.92f
        const val FireInitialScale = 0f
        const val FireInitialRotationDegrees = -24f
        // Fire grows from its base, like a flame igniting.
        const val FirePivotX = 0.5f
        const val FirePivotY = 1f
        const val CountStartDelayMillis = 120L
        const val CountDurationMillis = 700
        const val CountPulseScale = 1.18f
        const val CountPulseUpDurationMillis = 110
    }

    object TypingIndicator {
        val DotSize = 7.dp
        val DotSpacing = 5.dp
        val IndicatorHorizontalPadding = 16.dp
        val IndicatorVerticalPadding = 14.dp
        const val DotCount = 3
        // The last dot finishes before the cycle repeats, leaving a short pause.
        const val DotAnimationCycleMillis = 1_200
        const val DotAnimationDelayMillis = 180
        const val DotRiseDurationMillis = 170
        const val DotBounceDurationMillis = 390
        const val RestingOffset = 0f
        const val BounceOffset = -7f
        const val RestingAlpha = 0.38f
        const val ActiveAlpha = 1f
        const val RestingScale = 0.88f
        const val ActiveScale = 1.08f
    }

    object LevelProgressDropdown {
        val BentoGap = 8.dp
        val HeaderShape = RoundedCornerShape(18.dp)
        val HeaderPadding = 12.dp
        val HeaderContentSpacing = 10.dp
        val ReadinessTrackHeight = 6.dp
        val PrimaryNeedShape = RoundedCornerShape(20.dp)
        val PrimaryNeedBorderWidth = 0.75.dp
        val PrimaryNeedPadding = 14.dp
        val WhyNowShape = RoundedCornerShape(13.dp)
        val WhyNowPadding = 10.dp
        val SmallBentoShape = RoundedCornerShape(17.dp)
        val SmallBentoPadding = 11.dp
        val TeachingBentoHeight = 126.dp
        val TargetDotSize = 5.dp
        val TargetDotTopPadding = 5.dp
        val TargetDotSpacing = 7.dp
        val ExampleShape = RoundedCornerShape(18.dp)
        val ExamplePadding = 13.dp
        const val HeaderBackgroundAlpha = 0.13f
        const val ReadinessTrackAlpha = 0.72f
        const val PrimaryNeedBorderAlpha = 0.30f
        const val WhyNowBackgroundAlpha = 0.10f
        const val ExampleBackgroundAlpha = 0.13f
        const val MaximumTargetCount = 4
    }

    object LevelProgress {
        val Orange = Color(0xFFE87516)
        val Amber = Color(0xFFE6B522)
        val Green = Color(0xFF2E9B57)
        const val ColorMidpoint = 0.5f
    }

    object LevelProgressIndicator {
        val IndicatorTouchSize = 48.dp
        val IndicatorSize = 40.dp
        val IndicatorCenterSize = 31.dp
        val IndicatorStrokeWidth = 3.dp
        const val FullRotationDegrees = 360f
        const val FullCoinRotationY = 360f
        const val InitialCoinRotationY = -90f
        const val CoinBackFaceStartDegrees = 90f
        const val CoinBackFaceEndDegrees = 270f
        const val CoinCameraDistance = 14f
        const val IndicatorTrackAlpha = 0.55f
        const val IndicatorInitialScale = 0.76f
        const val ReplayInitialScale = 0.92f
        const val ReplayProgressDistance = 0.12f
        const val StaticCenterColorBlend = 0.34f
        const val RestingCenterColorBlend = 0.23f
        const val ActiveCenterColorBlend = 0.78f
        const val CenterTintSurfaceMix = 0.18f
        const val LightCenterLuminanceThreshold = 0.48f
        const val ShimmerTravelDistance = 1.8f
        const val ShimmerStartOffset = 0.4f
        const val ShimmerRadiusFactor = 0.32f
        const val ShimmerVerticalPosition = 0.34f
        const val ShimmerMaxAlpha = 0.20f
        const val ShimmerMidpoint = 0.5f
        const val IndicatorEntranceAnimationMillis = 500
        const val InitialProgressAnimationMillis = 1_300
        const val InitialCoinAnimationMillis = 760
        const val InitialCenterColorAnimationMillis = 1_100
        const val ReplayAnimationMillis = 1_200
        const val ReplayScaleAnimationMillis = 520
        const val CoinFlipAnimationMillis = 1_050
        const val CenterColorBloomMillis = 650
        const val CenterColorSettleMillis = 850
        const val CoinShimmerAnimationMillis = 1_050
        const val ReplayIntervalMillis = 10_000L
    }
}
