package com.julesmeister.deutschcraft.feature.auth.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.auth.presentation.theme.authAccent
import com.julesmeister.deutschcraft.feature.auth.presentation.theme.AuthDesignTokens as Tokens

/*
 * Lime CTA. primary/onPrimary already map to DeutschLime +
 * DeutschTextOnLime (dark) and DeutschLimeDark + text-on-lime
 * (light).
 */
@Composable
internal fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Tokens.ButtonHeight),
        enabled = enabled && !isLoading,
        shape = Tokens.ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary,
            disabledContainerColor = colorScheme.primary.copy(
                alpha = Tokens.DisabledButtonAlpha
            ),
            disabledContentColor = colorScheme.onPrimary.copy(
                alpha = Tokens.DisabledButtonAlpha
            )
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Tokens.ButtonProgressSize),
                strokeWidth = Tokens.ButtonProgressStroke,
                color = colorScheme.onPrimary
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
internal fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Tokens.ButtonHeight),
        enabled = enabled && !isLoading,
        shape = Tokens.ButtonShape,
        border = BorderStroke(
            width = Tokens.SecondaryButtonBorder,
            color = colorScheme.outlineVariant.copy(
                alpha = Tokens.SecondaryButtonBorderAlpha
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colorScheme.surfaceContainerHigh,
            contentColor = colorScheme.onSurface
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Tokens.ButtonProgressSize),
                strokeWidth = Tokens.ButtonProgressStroke,
                color = authAccent
            )
        } else {
            GoogleLogo(
                modifier = Modifier.size(Tokens.GoogleLogoSize)
            )

            Spacer(modifier = Modifier.width(Tokens.ButtonIconSpacing))

            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/*
 * Four-colour "G" drawn with arcs so no image asset is needed.
 */
@Composable
private fun GoogleLogo(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.22f
        val inset = strokeWidth / 2f
        val arcSize = Size(
            width = size.width - strokeWidth,
            height = size.height - strokeWidth
        )
        val topLeft = Offset(inset, inset)
        val stroke = Stroke(width = strokeWidth)

        // Angles: 0° = 3 o'clock, clockwise.
        drawArc(Tokens.GoogleRed, 200f, 120f, false, topLeft, arcSize, style = stroke)
        drawArc(Tokens.GoogleYellow, 140f, 60f, false, topLeft, arcSize, style = stroke)
        drawArc(Tokens.GoogleGreen, 45f, 95f, false, topLeft, arcSize, style = stroke)
        drawArc(Tokens.GoogleBlue, 0f, 45f, false, topLeft, arcSize, style = stroke)

        drawRect(
            color = Tokens.GoogleBlue,
            topLeft = Offset(
                x = size.width / 2f,
                y = size.height / 2f - strokeWidth / 2f
            ),
            size = Size(
                width = size.width / 2f,
                height = strokeWidth
            )
        )
    }
}

@Composable
internal fun AuthOrDivider(
    modifier: Modifier = Modifier
) {
    val lineColor =
        MaterialTheme.colorScheme.outlineVariant.copy(
            alpha = Tokens.DividerAlpha
        )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(Tokens.DividerHeight)
                .background(lineColor)
        )

        Text(
            text = "or",
            modifier = Modifier.padding(horizontal = Tokens.DividerLabelSpacing),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(Tokens.DividerHeight)
                .background(lineColor)
        )
    }
}

enum class AuthBannerTone {
    Error,
    Info
}

@Composable
internal fun AuthBanner(
    message: String?,
    tone: AuthBannerTone,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val colorScheme = MaterialTheme.colorScheme

        val accent: Color =
            when (tone) {
                AuthBannerTone.Error -> colorScheme.error
                AuthBannerTone.Info -> authAccent
            }

        val icon: ImageVector =
            when (tone) {
                AuthBannerTone.Error -> Icons.Outlined.ErrorOutline
                AuthBannerTone.Info -> Icons.Outlined.CheckCircle
            }

        val backgroundAlpha =
            when (tone) {
                AuthBannerTone.Error -> Tokens.BannerBackgroundAlpha
                AuthBannerTone.Info -> Tokens.InfoBannerBackgroundAlpha
            }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Tokens.BannerShape)
                .background(accent.copy(alpha = backgroundAlpha))
                .padding(Tokens.BannerPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(Tokens.BannerIconSize)
            )

            Spacer(modifier = Modifier.width(Tokens.BannerIconSpacing))

            Text(
                // Keep the last message during the exit animation.
                text = message.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )
        }
    }
}

/*
 * "New to DeutschCraft?  Create account"
 */
@Composable
internal fun AuthFooterLink(
    prompt: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AuthTextAction(
            text = action,
            onClick = onClick,
            enabled = enabled
        )
    }
}

@Composable
internal fun AuthTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color =
                if (enabled) {
                    authAccent
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
        )
    }
}
