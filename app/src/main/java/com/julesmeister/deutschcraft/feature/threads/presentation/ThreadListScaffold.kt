package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.julesmeister.deutschcraft.feature.threads.presentation.component.ThreadListTopBar
import com.julesmeister.deutschcraft.feature.threads.presentation.theme.ThreadDesignTokens as Tokens
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime

@Composable
fun ThreadListScaffold(
    snackbarHostState: SnackbarHostState,
    isCreatingThread: Boolean,
    streakDays: Int,
    onProgressClick: () -> Unit,
    onNewThreadClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (
        PaddingValues
    ) -> Unit
) {
    Scaffold(
        modifier =
            modifier.fillMaxSize(),

        topBar = {
            ThreadListTopBar(
                streakDays =
                    streakDays,
                onProgressClick =
                    onProgressClick,
                onNewConversationClick =
                    onNewThreadClick,
                onSettingsClick =
                    onSettingsClick
            )
        },

        snackbarHost = {
            SnackbarHost(
                hostState =
                    snackbarHostState
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!isCreatingThread) {
                        onNewThreadClick()
                    }
                },

                containerColor =
                    DeutschLime,

                contentColor =
                    DeutschTextOnLime,

                /*
                 * Telegram-like asymmetric shape.
                 *
                 * The first three corners remain soft while the
                 * bottom-right corner is completely flat.
                 */
                shape =
                    RoundedCornerShape(
                        topStart =
                            Tokens.CreateThreadFabCornerRadius,
                        topEnd =
                            Tokens.CreateThreadFabCornerRadius,
                        bottomStart =
                            Tokens.CreateThreadFabCornerRadius,
                        bottomEnd =
                            Tokens.CreateThreadFabFlatCorner
                    ),

                /*
                 * Absolutely no FAB elevation.
                 *
                 * Material normally changes FAB elevation for
                 * pressed/focused/hovered states, so all four
                 * must explicitly be zero.
                 */
                elevation =
                    FloatingActionButtonDefaults.elevation(
                        defaultElevation =
                            Tokens.CreateThreadFabElevation,
                        pressedElevation =
                            Tokens.CreateThreadFabElevation,
                        focusedElevation =
                            Tokens.CreateThreadFabElevation,
                        hoveredElevation =
                            Tokens.CreateThreadFabElevation
                    )
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.Add,
                    contentDescription =
                        "New conversation"
                )
            }
        },

        content =
            content
    )
}