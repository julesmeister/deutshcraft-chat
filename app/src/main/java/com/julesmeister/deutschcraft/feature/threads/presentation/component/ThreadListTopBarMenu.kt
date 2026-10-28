package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.threads.presentation.theme.ThreadDesignTokens as Tokens
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime

@Composable
internal fun ThreadListTopBarMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onNewConversationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberThreadColors()

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.width(Tokens.MenuWidth),
        shape = Tokens.MenuShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = Tokens.MenuTonalElevation,
        shadowElevation = if (colors.isDark) {
            Tokens.MenuDarkShadowElevation
        } else {
            Tokens.MenuLightShadowElevation
        }
    ) {
        Column(
            modifier = Modifier.padding(Tokens.MenuContentPadding)
        ) {
            Text(
                text = "QUICK ACTIONS",
                modifier = Modifier.padding(
                    start = Tokens.MenuHeaderHorizontalPadding,
                    end = Tokens.MenuHeaderHorizontalPadding,
                    top = Tokens.MenuHeaderTopPadding,
                    bottom = Tokens.MenuHeaderBottomPadding
                ),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary
            )

            FeaturedMenuAction(
                title = "New conversation",
                description = "Start a fresh German chat",
                icon = Icons.Outlined.AddComment,
                onClick = onNewConversationClick
            )

            MenuActionRow(
                title = "Settings",
                description = "Customize DeutschCraft",
                icon = Icons.Outlined.Settings,
                colors = colors,
                onClick = onSettingsClick
            )
        }
    }
}

/*
 * Primary action: lime card with dark content.
 * Readable in both themes.
 */
@Composable
private fun FeaturedMenuAction(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interactionSource)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(Tokens.MenuFeaturedShape)
            .background(DeutschLime)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(Tokens.MenuFeaturedPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Tokens.MenuFeaturedIconSize)
                .clip(Tokens.CircleShapeToken)
                .background(
                    DeutschTextOnLime.copy(
                        alpha = Tokens.MenuFeaturedIconBackgroundAlpha
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Tokens.MenuFeaturedIconGlyphSize),
                tint = DeutschTextOnLime
            )
        }

        Spacer(modifier = Modifier.width(Tokens.MenuRowIconTextSpacing))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = DeutschTextOnLime
            )

            Text(
                text = description,
                modifier = Modifier.padding(top = Tokens.MenuDescriptionTopPadding),
                style = MaterialTheme.typography.bodySmall,
                color = DeutschTextOnLime.copy(
                    alpha = Tokens.MenuFeaturedDescriptionAlpha
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(Tokens.MenuTrailingIconSize),
            tint = DeutschTextOnLime
        )
    }
}

/*
 * Secondary action: quiet row with tinted icon and chevron.
 */
@Composable
private fun MenuActionRow(
    title: String,
    description: String,
    icon: ImageVector,
    colors: ThreadColors,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interactionSource)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(Tokens.MenuRowShape)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                role = Role.Button,
                onClick = onClick
            )
            .padding(Tokens.MenuRowPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Tokens.MenuIconContainerSize)
                .clip(Tokens.CircleShapeToken)
                .background(colors.accentContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent
            )
        }

        Spacer(modifier = Modifier.width(Tokens.MenuRowIconTextSpacing))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )

            Text(
                text = description,
                modifier = Modifier.padding(top = Tokens.MenuDescriptionTopPadding),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(Tokens.MenuTrailingIconSize),
            tint = colors.textSecondary
        )
    }
}

@Composable
private fun MenuDivider(
    colors: ThreadColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Tokens.MenuDividerHorizontalPadding,
                vertical = Tokens.MenuDividerVerticalPadding
            )
            .height(Tokens.MenuDividerHeight)
            .background(
                colors.textSecondary.copy(alpha = Tokens.MenuDividerAlpha * 0.3f)
            )
    )
}

@Composable
private fun rememberPressScale(
    interactionSource: MutableInteractionSource
) = run {
    val isPressed by interactionSource.collectIsPressedAsState()

    animateFloatAsState(
        targetValue = if (isPressed) Tokens.MenuPressedScale else 1f,
        animationSpec = tween(
            durationMillis = Tokens.MenuPressAnimationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "MenuPressScale"
    )
}