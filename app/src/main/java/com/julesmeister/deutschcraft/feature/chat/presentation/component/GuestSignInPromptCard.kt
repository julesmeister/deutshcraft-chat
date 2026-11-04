package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.auth.domain.GuestMessagePolicy
import com.julesmeister.deutschcraft.feature.auth.presentation.theme.authAccent
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens

/*
 * Shown above the input once a guest has used up the free
 * messages in this conversation. Mirrors MissionOfferCard.
 */
@Composable
internal fun GuestSignInPromptCard(
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme =
        MaterialTheme.colorScheme

    val accent =
        authAccent

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(
                    ChatDesignTokens.MissionOffer.CardShape
                )
                .background(
                    color =
                        colorScheme.surfaceContainerHigh
                )
                .padding(
                    horizontal =
                        ChatDesignTokens.MissionOffer.CardHorizontalPadding,
                    vertical =
                        ChatDesignTokens.MissionOffer.CardVerticalPadding
                )
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.Lock,

                contentDescription =
                    null,

                tint =
                    accent,

                modifier =
                    Modifier
                        .size(
                            ChatDesignTokens.MissionOffer.IconContainerSize
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            color =
                                colorScheme
                                    .primary
                                    .copy(
                                        alpha =
                                            ChatDesignTokens.MissionOffer.IconBackgroundAlpha
                                    )
                        )
                        .padding(
                            ChatDesignTokens.MissionOffer.IconPadding
                        )
            )

            Spacer(
                modifier =
                    Modifier.width(
                        ChatDesignTokens.MissionOffer.HeaderHorizontalSpacing
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                Text(
                    text =
                        "GUEST LIMIT REACHED",

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        accent
                )

                Text(
                    text =
                        "Sign in to keep chatting",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        colorScheme.onSurface
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    ChatDesignTokens.MissionOffer.DescriptionTopSpacing
                )
        )

        Text(
            text =
                "Guests can send ${GuestMessagePolicy.MessageLimitPerConversation} messages " +
                        "per conversation. Sign in or create a free account to continue this one.",

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            color =
                colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(
                    ChatDesignTokens.MissionOffer.ActionsTopSpacing
                )
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.End
        ) {
            Text(
                text =
                    "Sign in",

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    colorScheme.onPrimary,

                modifier =
                    Modifier
                        .clip(
                            RoundedCornerShape(
                                percent =
                                    ChatDesignTokens.MissionOffer.PillShapePercent
                            )
                        )
                        .background(
                            color =
                                colorScheme.primary
                        )
                        .clickable(
                            onClick =
                                onSignInClick
                        )
                        .padding(
                            horizontal =
                                ChatDesignTokens.MissionOffer.ButtonHorizontalPadding,

                            vertical =
                                ChatDesignTokens.MissionOffer.ButtonVerticalPadding
                        )
            )
        }
    }
}
