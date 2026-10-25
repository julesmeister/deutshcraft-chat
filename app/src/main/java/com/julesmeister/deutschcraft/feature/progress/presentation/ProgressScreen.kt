package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSkillSummary
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary
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
        modifier = modifier.fillMaxSize(),
        containerColor =
            MaterialTheme.colorScheme.background,
        topBar = {
            ProgressTopBar(
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                ProgressLoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            uiState.progress != null -> {
                ProgressContent(
                    progress = uiState.progress,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                ProgressEmptyContent(
                    message = uiState.errorMessage
                        ?: "Noch keine Fortschrittsdaten verfügbar.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
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
                text = "Dein Fortschritt",
                modifier = Modifier.padding(start = 8.dp),
                color = DeutschTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp),
                shape = CircleShape,
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
                        Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Zurück"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor =
                MaterialTheme.colorScheme.background,
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
        modifier = modifier,
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = DeutschLime
        )

        Text(
            text = "Fortschritt wird geladen …",
            modifier = Modifier.padding(top = 16.dp),
            color = DeutschTextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ProgressContent(
    progress: ProgressSummary,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding =
            androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                top = 16.dp,
                end = 20.dp,
                bottom = 40.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "CEFR-Entwicklung",
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DeutschTextPrimary
            )

            Text(
                text = "Deine Einschätzung entwickelt sich mit jeder analysierten deutschen Nachricht.",
                modifier = Modifier.padding(top = 6.dp),
                style =
                    MaterialTheme.typography.bodyMedium,
                color = DeutschTextSecondary
            )
        }

        item {
            CurrentNiveauPanel(
                progress = progress
            )
        }

        item {
            ReadinessPanel(
                progress = progress
            )
        }

        item {
            ProgressStatistics(
                progress = progress
            )
        }

        item {
            SkillBreakdownPanel(
                progress = progress
            )
        }

        item {
            ConfidencePanel(
                progress = progress
            )
        }
    }
}

@Composable
private fun CurrentNiveauPanel(
    progress: ProgressSummary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color =
            MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(DeutschLime),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text =
                        progress.currentLevel.name,
                    style =
                        MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(
                modifier = Modifier.width(18.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Aktuelles Niveau",
                    style =
                        MaterialTheme.typography.labelLarge,
                    color = DeutschTextSecondary
                )

                Text(
                    text = progress.currentLevel.name,
                    modifier = Modifier.padding(top = 2.dp),
                    style =
                        MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeutschTextPrimary
                )

                Text(
                    text = "${progress.overallScore.coerceIn(0, 100)}% Gesamtbewertung",
                    modifier = Modifier.padding(top = 6.dp),
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color = DeutschTextSecondary
                )
            }
        }
    }
}

@Composable
private fun ReadinessPanel(
    progress: ProgressSummary,
    modifier: Modifier = Modifier
) {
    val destinationLabel =
        if (progress.isHighestLevel) {
            "C2 festigen"
        } else {
            "Bereitschaft für ${progress.nextLevelName}"
        }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color =
            MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoGraph,
                    contentDescription = null,
                    tint = DeutschLime
                )

                Text(
                    text = destinationLabel,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .weight(1f),
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DeutschTextPrimary
                )

                Text(
                    text =
                        "${progress.readinessPercentage.coerceIn(0, 100)}%",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeutschLime
                )
            }

            LinearProgressIndicator(
                progress = {
                    progress.normalizedReadiness
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .height(10.dp)
                    .clip(CircleShape),
                color = DeutschLime,
                trackColor =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHighest
            )

            Text(
                text = if (progress.isHighestLevel) {
                    "Du hast das höchste CEFR-Niveau erreicht. Deine Bewertung zeigt jetzt die Festigung deiner C2-Fähigkeiten."
                } else {
                    "Diese Bewertung zeigt, wie nah dein aktuelles Sprachprofil am nächsten Niveau liegt."
                },
                modifier = Modifier.padding(top = 12.dp),
                style =
                    MaterialTheme.typography.bodySmall,
                color = DeutschTextSecondary
            )
        }
    }
}

@Composable
private fun ProgressStatistics(
    progress: ProgressSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        ProgressStatisticItem(
            value = progress
                .assessedMessageCount
                .toString(),
            label = "Bewertete\nNachrichten",
            icon = {
                Icon(
                    imageVector =
                        Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = DeutschLime
                )
            },
            modifier = Modifier.weight(1f)
        )

        ProgressStatisticItem(
            value = progress
                .activeDayCount
                .toString(),
            label = "Aktive\nTage",
            icon = {
                Icon(
                    imageVector =
                        Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = DeutschLime
                )
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProgressStatisticItem(
    value: String,
    label: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color =
            MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            icon()

            Text(
                text = value,
                modifier = Modifier.padding(top = 14.dp),
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DeutschTextPrimary
            )

            Text(
                text = label,
                modifier = Modifier.padding(top = 4.dp),
                style =
                    MaterialTheme.typography.bodySmall,
                color = DeutschTextSecondary
            )
        }
    }
}

@Composable
private fun SkillBreakdownPanel(
    progress: ProgressSummary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color =
            MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Insights,
                    contentDescription = null,
                    tint = DeutschLime
                )

                Text(
                    text = "Fähigkeiten",
                    modifier = Modifier.padding(start = 10.dp),
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DeutschTextPrimary
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            progress.skillSummaries.forEachIndexed {
                    index,
                    skill ->

                SkillProgressRow(
                    skill = skill
                )

                if (
                    index <
                    progress.skillSummaries.lastIndex
                ) {
                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillProgressRow(
    skill: ProgressSkillSummary,
    modifier: Modifier = Modifier
) {
    val safeScore =
        skill.score.coerceIn(
            minimumValue = 0,
            maximumValue = 100
        )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = skill.label,
                modifier = Modifier.weight(1f),
                style =
                    MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = DeutschTextPrimary
            )

            Text(
                text = "$safeScore%",
                style =
                    MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DeutschTextPrimary
            )
        }

        LinearProgressIndicator(
            progress = {
                safeScore / 100f
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(8.dp)
                .clip(CircleShape),
            color = DeutschLime,
            trackColor =
                MaterialTheme
                    .colorScheme
                    .surfaceContainerHighest
        )
    }
}

@Composable
private fun ConfidencePanel(
    progress: ProgressSummary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color =
            MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Evidenzqualität",
                style =
                    MaterialTheme.typography.labelLarge,
                color = DeutschTextSecondary
            )

            Text(
                text =
                    progress.confidence.displayName,
                modifier = Modifier.padding(top = 4.dp),
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = DeutschTextPrimary
            )

            Text(
                text =
                    progress.confidence.description,
                modifier = Modifier.padding(top = 8.dp),
                style =
                    MaterialTheme.typography.bodyMedium,
                color = DeutschTextSecondary
            )
        }
    }
}

@Composable
private fun ProgressEmptyContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Insights,
                contentDescription = null,
                tint = DeutschTextSecondary
            )
        }

        Text(
            text = "Noch keine CEFR-Daten",
            modifier = Modifier.padding(top = 18.dp),
            style =
                MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = DeutschTextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style =
                MaterialTheme.typography.bodyMedium,
            color = DeutschTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}