package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ProgressStreakButton
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadListTopBar(
    streakDays: Int,
    onProgressClick: () -> Unit,
    onNewConversationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by
    remember {
        mutableStateOf(false)
    }

    TopAppBar(
        modifier =
            modifier.fillMaxWidth(),
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .background,
                scrolledContainerColor =
                    MaterialTheme
                        .colorScheme
                        .background,
                titleContentColor =
                    DeutschTextPrimary,
                actionIconContentColor =
                    DeutschTextPrimary
            ),
        title = {
            Text(
                text =
                    "Unterhaltungen",
                color =
                    DeutschTextPrimary,
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )
        },
        actions = {
            ProgressStreakButton(
                streakDays =
                    streakDays,
                onClick =
                    onProgressClick
            )

            Spacer(
                modifier =
                    Modifier.width(
                        TOP_BAR_ACTION_SPACING
                    )
            )

            Box(
                modifier =
                    Modifier.padding(
                        end =
                            TOP_BAR_EDGE_PADDING
                    )
            ) {
                ThreadTopBarIconButton(
                    onClick = {
                        isMenuExpanded =
                            true
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.Outlined.MoreVert,
                        contentDescription =
                            "More options"
                    )
                }

                ThreadListTopBarMenu(
                    expanded =
                        isMenuExpanded,
                    onDismissRequest = {
                        isMenuExpanded =
                            false
                    },
                    onNewConversationClick = {
                        isMenuExpanded =
                            false

                        onNewConversationClick()
                    },
                    onSettingsClick = {
                        isMenuExpanded =
                            false

                        onSettingsClick()
                    }
                )
            }
        }
    )
}

private val TOP_BAR_EDGE_PADDING =
    8.dp

private val TOP_BAR_ACTION_SPACING =
    6.dp