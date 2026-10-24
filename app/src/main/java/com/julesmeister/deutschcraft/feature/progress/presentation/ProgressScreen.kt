package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressScreen(
    uiState: ProgressUiState,
    onBackClick: () -> Unit
) {

    val progress = uiState.progress

    Scaffold {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = onBackClick
            ) {
                Text("Back")
            }

            if (progress == null) {

                Text(
                    text = "No profile found."
                )

                return@Column
            }

            Text(
                text = "German Progress",
                style =
                    MaterialTheme.typography
                        .headlineMedium
            )

            Card {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {

                    Text(
                        text =
                            "Level: ${progress.currentLevel}"
                    )

                    Text(
                        text =
                            "Readiness: ${progress.readinessPercentage}%"
                    )

                    Text(
                        text =
                            "Overall: ${progress.overallScore}"
                    )
                }
            }

            Card {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {

                    Text(
                        text =
                            "Grammar: ${progress.grammarScore}"
                    )

                    Text(
                        text =
                            "Vocabulary: ${progress.vocabularyScore}"
                    )

                    Text(
                        text =
                            "Coherence: ${progress.coherenceScore}"
                    )

                    Text(
                        text =
                            "Accuracy: ${progress.accuracyScore}"
                    )

                    Text(
                        text =
                            "Range: ${progress.rangeScore}"
                    )
                }
            }

            Card {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {

                    Text(
                        text =
                            "Messages Assessed: ${progress.assessedMessageCount}"
                    )

                    Text(
                        text =
                            "Active Days: ${progress.activeDayCount}"
                    )
                }
            }
        }
    }
}