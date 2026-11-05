package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.R
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ProgressStreakButton
import com.julesmeister.deutschcraft.feature.threads.presentation.theme.ThreadDesignTokens as Tokens

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
                    MaterialTheme
                        .colorScheme
                        .onBackground,
                actionIconContentColor =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            ),
        title = {
            val threadColors =
                rememberThreadColors()

            Image(
                painter =
                    painterResource(
                        id =
                            if (threadColors.isDark) {
                                R.drawable.threads_title_dark
                            } else {
                                R.drawable.threads_title_light
                            }
                    ),
                contentDescription =
                    "Unterhaltungen",
                modifier =
                    Modifier
                        .height(
                            Tokens.TopBarTitleHeight
                        )
                        .widthIn(
                            max =
                                Tokens.TopBarTitleMaxWidth
                        ),
                contentScale =
                    ContentScale.Fit
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
                        8.dp
                    )
            )

            Box {
                val menuVisual: @Composable () -> Unit = {
                    Box(
                        modifier = Modifier
                            .size(Tokens.TopBarIconButtonSize),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                ThreadTopBarIconButton(
                    onClick = {
                        isMenuExpanded = true
                    }
                ) {
                    menuVisual()
                }

                ThreadListTopBarMenu(
                    anchorContent = menuVisual,
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

            Spacer(Modifier.width(8.dp))
        }
    )
}
