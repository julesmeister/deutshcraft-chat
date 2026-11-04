package com.julesmeister.deutschcraft.feature.settings.presentation.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object SettingsDesignTokens {

    /*
     * =======================================================
     * Screen
     * =======================================================
     */

    val ScreenHorizontalPadding = 16.dp
    val ScreenTopPadding = 4.dp
    val ScreenBottomPadding = 40.dp
    val SectionSpacing = 22.dp

    /*
     * =======================================================
     * Top bar / header
     * =======================================================
     */

    val TopBarStartPadding = 8.dp
    val TopBarButtonSize = 40.dp
    val HeaderBottomPadding = 6.dp
    val HeaderSubtitleTopPadding = 4.dp

    /*
     * =======================================================
     * Section
     * =======================================================
     */

    val SectionShape = RoundedCornerShape(26.dp)
    val SectionTitleStartPadding = 8.dp
    val SectionTitleBottomPadding = 8.dp
    val SectionVerticalPadding = 6.dp

    val DividerStartPadding = 68.dp
    val DividerEndPadding = 16.dp
    val DividerHeight = 1.dp
    const val DividerAlpha = 0.45f

    /*
     * =======================================================
     * Rows
     * =======================================================
     */

    val RowPadding = 16.dp
    val DescriptionTopPadding = 2.dp
    val SwitchSpacing = 8.dp

    val IconSize = 40.dp
    val IconShape = RoundedCornerShape(14.dp)
    val IconSpacing = 12.dp
    const val IconBackgroundAlpha = 0.14f
    const val IconActiveBackgroundAlpha = 0.26f

    const val StateAnimationMillis = 220

    /*
     * =======================================================
     * Theme picker
     * =======================================================
     */

    val ThemeCardSpacing = 12.dp
    val ThemeCardShape = RoundedCornerShape(20.dp)
    val ThemeCardPadding = 10.dp
    val ThemeCardBorderSelected = 2.dp
    val ThemeCardBorderResting = 1.dp
    val ThemeCardLabelTopPadding = 10.dp
    val ThemeCardLabelIconSize = 16.dp
    val ThemeCardLabelIconSpacing = 6.dp

    val ThemePreviewHeight = 92.dp
    val ThemePreviewShape = RoundedCornerShape(14.dp)
    val ThemePreviewPadding = 10.dp
    val ThemePreviewBubbleSpacing = 6.dp
    val ThemePreviewBubbleHeight = 14.dp
    val ThemePreviewBubbleShape = RoundedCornerShape(7.dp)
    const val ThemePreviewIncomingWidth = 0.62f
    const val ThemePreviewOutgoingWidth = 0.48f
    const val ThemePreviewShortWidth = 0.36f

    val ThemeCheckSize = 22.dp
    val ThemeCheckIconSize = 14.dp
    val ThemeCheckOffset = 6.dp

    const val ThemeCardRestingBorderAlpha = 0.5f

    /*
     * Fixed mock colors: each preview always shows its own
     * theme, regardless of the currently active theme.
     */
    val DarkPreviewBackground = Color(0xFF121417)
    val DarkPreviewIncoming = Color(0xFF2A2E33)
    val DarkPreviewOutgoing = Color(0xFFC6F24E)

    val LightPreviewBackground = Color(0xFFF4F5F7)
    val LightPreviewIncoming = Color(0xFFFFFFFF)
    val LightPreviewOutgoing = Color(0xFFB7E33C)

    /*
     * =======================================================
     * Font scale picker
     * =======================================================
     */

    val FontPillSpacing = 8.dp
    val FontPillHeight = 52.dp
    val FontPillShape = RoundedCornerShape(16.dp)
    val FontPillBorder = 1.dp
    const val FontPillRestingBorderAlpha = 0.5f

    val FontPreviewTopPadding = 14.dp
    val FontPreviewShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = 4.dp,
        bottomEnd = 18.dp
    )
    val FontPreviewHorizontalPadding = 14.dp
    val FontPreviewVerticalPadding = 10.dp
    const val FontPreviewWidthFraction = 0.85f

    const val FontPreviewSentence = "Wie geht es dir heute?"
    const val FontPillLetter = "A"

    /*
     * =======================================================
     * About
     * =======================================================
     */

    val AboutBadgeSize = 48.dp
    val AboutBadgeShape = CircleShape
    val AboutBadgeSpacing = 14.dp
    const val AboutMonogram = "DC"

    /*
     * =======================================================
     * Lazy keys
     * =======================================================
     */

    const val HeaderKey = "header"
    const val AccountSectionKey = "account"
    const val AppearanceSectionKey = "appearance"
    const val LearningSupportSectionKey = "learning_support"
    const val AboutSectionKey = "about"
}