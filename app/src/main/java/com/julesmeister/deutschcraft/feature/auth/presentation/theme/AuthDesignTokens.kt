package com.julesmeister.deutschcraft.feature.auth.presentation.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AuthDesignTokens {

    /*
     * =======================================================
     * Screen
     * =======================================================
     */

    val ScreenHorizontalPadding = 16.dp
    val ScreenTopPadding = 4.dp
    val ScreenBottomPadding = 32.dp
    val ContentSpacing = 12.dp
    val SectionSpacing = 22.dp

    /*
     * =======================================================
     * Top bar / header
     * =======================================================
     */

    val TopBarStartPadding = 8.dp
    val TopBarButtonSize = 40.dp

    val HeaderStartPadding = 8.dp
    val HeaderBadgeSize = 52.dp
    val HeaderBadgeShape = CircleShape
    val HeaderBadgeBottomPadding = 18.dp
    val HeaderSubtitleTopPadding = 4.dp
    val HeaderBottomPadding = 6.dp
    const val HeaderMonogram = "DC"

    /*
     * =======================================================
     * Form card
     * =======================================================
     */

    val CardShape = RoundedCornerShape(26.dp)
    val CardPadding = 16.dp

    /*
     * =======================================================
     * Text fields
     * =======================================================
     */

    val FieldShape = RoundedCornerShape(18.dp)
    val FieldIconSize = 20.dp
    val FieldSupportingTopPadding = 4.dp
    val FieldSupportingStartPadding = 14.dp
    const val FieldUnfocusedBorderAlpha = 0.0f

    /*
     * =======================================================
     * Buttons
     * =======================================================
     */

    val ButtonHeight = 54.dp
    val ButtonShape = RoundedCornerShape(18.dp)
    val ButtonProgressSize = 20.dp
    val ButtonProgressStroke = 2.dp
    val ButtonIconSpacing = 10.dp
    val GoogleLogoSize = 18.dp
    val SecondaryButtonBorder = 1.dp
    const val SecondaryButtonBorderAlpha = 0.6f
    const val DisabledButtonAlpha = 0.45f

    /*
     * =======================================================
     * Divider
     * =======================================================
     */

    val DividerHeight = 1.dp
    val DividerLabelSpacing = 12.dp
    const val DividerAlpha = 0.45f

    /*
     * =======================================================
     * Banners
     * =======================================================
     */

    val BannerShape = RoundedCornerShape(18.dp)
    val BannerPadding = 14.dp
    val BannerIconSize = 20.dp
    val BannerIconSpacing = 10.dp
    const val BannerBackgroundAlpha = 0.12f
    const val InfoBannerBackgroundAlpha = 0.16f

    /*
     * =======================================================
     * Verification code
     * =======================================================
     */

    const val CodeLength = 6
    val CodeCellSize = 48.dp
    val CodeCellSpacing = 8.dp
    val CodeCellShape = RoundedCornerShape(14.dp)
    val CodeCellBorderFocused = 2.dp
    val CodeCellBorderResting = 1.dp
    const val CodeCellRestingBorderAlpha = 0.5f
    const val ResendCooldownSeconds = 30

    /*
     * =======================================================
     * Validation
     * =======================================================
     */

    const val MinPasswordLength = 8

    /*
     * =======================================================
     * Google brand colors (logo only)
     * =======================================================
     */

    val GoogleBlue = Color(0xFF4285F4)
    val GoogleRed = Color(0xFFEA4335)
    val GoogleYellow = Color(0xFFFBBC05)
    val GoogleGreen = Color(0xFF34A853)

    const val StateAnimationMillis = 220

    /*
     * Light-mode primary (DeutschLimeDark) is too pale for text
     * on a light background; threads use the same darker olive.
     */
    val LightAccentText = Color(0xFF5A7A00)
    const val DarkLuminanceThreshold = 0.5f
}
