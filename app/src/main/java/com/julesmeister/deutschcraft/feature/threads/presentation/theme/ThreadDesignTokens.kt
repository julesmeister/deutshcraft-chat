package com.julesmeister.deutschcraft.feature.threads.presentation.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object ThreadDesignTokens {

    /*
     * =======================================================
     * Theme colors
     * =======================================================
     */

    const val DarkLuminanceThreshold = 0.5f

    /*
     * Olive-lime used for lime text and icons in light mode.
     * Matches the Progress screen.
     */
    val LightAccent = Color(0xFF5A7A00)

    const val DarkAccentContainerAlpha = 0.14f
    const val LightAccentContainerAlpha = 0.35f

    const val DarkErrorContainerAlpha = 0.16f
    const val LightErrorContainerAlpha = 0.12f

    /*
     * =======================================================
     * Archive pull indicator
     * =======================================================
     */

    val PullHeight = 58.dp
    val PullHorizontalPadding = 20.dp
    val PullIconBoxSize = 44.dp
    val PullBlobSize = 38.dp
    val PullIconSize = 20.dp
    val PullTextStartPadding = 10.dp

    const val PullThreshold = 0.98f

    const val PullBlobBaseScaleX = 0.7f
    const val PullBlobGrowthX = 0.45f
    const val PullBlobBaseScaleY = 0.7f
    const val PullBlobGrowthY = 0.25f
    const val PullBlobBaseAlpha = 0.2f
    const val PullBlobGrowthAlpha = 0.8f

    const val PullIconBaseScale = 0.72f
    const val PullIconGrowthScale = 0.38f
    const val PullIconBaseRotation = -20f
    const val PullIconGrowthRotation = 20f

    /*
     * =======================================================
     * Top bar menu
     * =======================================================
     */

    val MenuShape = RoundedCornerShape(24.dp)
    val MenuItemShape = RoundedCornerShape(18.dp)
    val MenuWidth = 260.dp
    val MenuVerticalPadding = 8.dp
    val MenuItemHorizontalMargin = 6.dp
    val MenuItemSpacing = 2.dp
    val MenuDescriptionTopPadding = 2.dp
    val MenuIconContainerSize = 38.dp
    val MenuTonalElevation = 6.dp
    val MenuDarkShadowElevation = 14.dp
    val MenuLightShadowElevation = 6.dp

    /*
     * =======================================================
     * Top bar icon button
     * =======================================================
     */

    val TopBarIconButtonSize = 40.dp
    const val TopBarIconNormalScale = 1f
    const val TopBarIconPressedScale = 0.9f
    const val TopBarIconDarkRestingAlpha = 0.68f
    const val TopBarIconPressAnimationMillis = 130

    /*
     * =======================================================
     * Archived threads row
     * =======================================================
     */

    val ArchivedRowHorizontalPadding = 16.dp
    val ArchivedRowVerticalPadding = 12.dp
    val ArchivedIconCircleSize = 48.dp
    val ArchivedIconSize = 23.dp
    val ArchivedTextStartPadding = 12.dp
    val ArchivedCountTopPadding = 2.dp

    /*
     * =======================================================
     * Thread action dialog
     * =======================================================
     */

    val DialogOuterHorizontalPadding = 20.dp
    val DialogShape = RoundedCornerShape(28.dp)
    val DialogPadding = 20.dp
    val DialogPromptTopPadding = 20.dp
    val DialogPromptBottomPadding = 10.dp
    val DialogSecondActionTopPadding = 8.dp

    val DialogHeaderBadgeSize = 48.dp
    val DialogHeaderTextHorizontalPadding = 12.dp
    val DialogHeaderPreviewTopPadding = 3.dp
    val DialogCloseButtonSize = 38.dp
    val DialogCloseIconSize = 19.dp

    val DialogActionShape = RoundedCornerShape(20.dp)
    val DialogActionHorizontalPadding = 4.dp
    val DialogActionVerticalPadding = 10.dp
    val DialogActionSpacing = 14.dp
    val DialogActionIconCircleSize = 46.dp
    val DialogActionIconSize = 22.dp
    val DialogActionDescriptionTopPadding = 2.dp

    /*
     * =======================================================
     * Section header
     * =======================================================
     */

    val SectionHorizontalPadding = 16.dp
    val SectionTopPadding = 18.dp
    val SectionBottomPadding = 6.dp

    /*
     * =======================================================
     * Shared shapes
     * =======================================================
     */

    val CircleShapeToken = CircleShape

    /*
     * =======================================================
     * Delete thread dialog
     * =======================================================
     */

    val DeleteDialogShape = RoundedCornerShape(26.dp)

    /*
     * =======================================================
     * Top bar menu (redesign)
     * =======================================================
     */

    val MenuContentPadding = 8.dp
    val MenuHeaderHorizontalPadding = 12.dp
    val MenuHeaderTopPadding = 4.dp
    val MenuHeaderBottomPadding = 8.dp

    val MenuFeaturedShape = RoundedCornerShape(20.dp)
    val MenuFeaturedPadding = 12.dp
    val MenuFeaturedIconSize = 40.dp
    val MenuFeaturedIconGlyphSize = 20.dp
    const val MenuFeaturedIconBackgroundAlpha = 0.12f
    const val MenuFeaturedDescriptionAlpha = 0.72f

    val MenuRowShape = RoundedCornerShape(18.dp)
    val MenuRowPadding = 12.dp
    val MenuRowIconTextSpacing = 12.dp
    val MenuTrailingIconSize = 18.dp

    val MenuDividerVerticalPadding = 8.dp
    val MenuDividerHorizontalPadding = 12.dp
    val MenuDividerHeight = 1.dp
    const val MenuDividerAlpha = 0.5f

    const val MenuPressedScale = 0.97f
    const val MenuPressAnimationMillis = 120

    /*
     * =======================================================
     * Create thread FAB
     * =======================================================
     */

    val CreateThreadFabCornerRadius =
        18.dp

    val CreateThreadFabFlatCorner =
        0.dp

    val CreateThreadFabElevation =
        0.dp
}
