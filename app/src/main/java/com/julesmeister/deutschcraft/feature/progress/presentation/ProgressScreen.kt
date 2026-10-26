package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    uiState: ProgressUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier =
            modifier.fillMaxSize(),
        containerColor =
            MaterialTheme
                .colorScheme
                .background,
        topBar = {
            ProgressTopBar(
                onBackClick =
                    onBackClick
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                ProgressLoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            innerPadding
                        )
                )
            }

            uiState.progress != null -> {
                ProgressContent(
                    progress =
                        uiState.progress,
                    completedMissionCount =
                        uiState.completedMissionCount,
                    recentCompletedMissions =
                        uiState.recentCompletedMissions,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            innerPadding
                        )
                )
            }

            else -> {
                ProgressEmptyContent(
                    message =
                        uiState.errorMessage
                            ?: DEFAULT_EMPTY_MESSAGE,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            innerPadding
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text =
                    "Dein Fortschritt",
                modifier =
                    Modifier.padding(
                        start =
                            TOP_BAR_TITLE_START_PADDING
                    ),
                color =
                    DeutschTextPrimary,
                fontWeight =
                    FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(
                onClick =
                    onBackClick,
                modifier = Modifier
                    .padding(
                        start =
                            TOP_BAR_EDGE_PADDING
                    )
                    .size(
                        TOP_BAR_BUTTON_SIZE
                    ),
                shape =
                    CircleShape,
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .surfaceContainer,
                        contentColor =
                            DeutschTextPrimary
                    )
            ) {
                Icon(
                    imageVector =
                        Icons.AutoMirrored
                            .Outlined
                            .ArrowBack,
                    contentDescription =
                        "Zurück"
                )
            }
        },
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
                navigationIconContentColor =
                    DeutschTextPrimary
            )
    )
}

@Composable
private fun ProgressLoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier,
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        CircularProgressIndicator(
            color =
                DeutschLime
        )

        Text(
            text =
                "Fortschritt wird geladen …",
            modifier =
                Modifier.padding(
                    top =
                        LOADING_TEXT_TOP_PADDING
                ),
            color =
                DeutschTextSecondary,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )
    }
}

@Composable
private fun ProgressEmptyContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.padding(
                EMPTY_CONTENT_PADDING
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(
                    EMPTY_ICON_CONTAINER_SIZE
                )
                .clip(
                    CircleShape
                )
                .background(
                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceContainer
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.Insights,
                contentDescription =
                    null,
                tint =
                    DeutschTextSecondary
            )
        }

        Text(
            text =
                "Noch keine CEFR-Daten",
            modifier =
                Modifier.padding(
                    top =
                        EMPTY_TITLE_TOP_PADDING
                ),
            style =
                MaterialTheme
                    .typography
                    .titleMedium,
            fontWeight =
                FontWeight.SemiBold,
            color =
                DeutschTextPrimary,
            textAlign =
                TextAlign.Center
        )

        Text(
            text =
                message,
            modifier =
                Modifier.padding(
                    top =
                        EMPTY_MESSAGE_TOP_PADDING
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                DeutschTextSecondary,
            textAlign =
                TextAlign.Center
        )
    }
}

private const val DEFAULT_EMPTY_MESSAGE =
    "Noch keine Fortschrittsdaten verfügbar."

private val TOP_BAR_EDGE_PADDING =
    8.dp

private val TOP_BAR_BUTTON_SIZE =
    40.dp

private val TOP_BAR_TITLE_START_PADDING =
    12.dp

private val LOADING_TEXT_TOP_PADDING =
    16.dp

private val EMPTY_CONTENT_PADDING =
    32.dp

private val EMPTY_ICON_CONTAINER_SIZE =
    68.dp

private val EMPTY_TITLE_TOP_PADDING =
    18.dp

private val EMPTY_MESSAGE_TOP_PADDING =
    8.dp