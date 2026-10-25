package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
fun ArchivePullIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio =
                Spring.DampingRatioLowBouncy,
            stiffness =
                Spring.StiffnessMediumLow
        ),
        label = "archive-pull-progress"
    )

    val thresholdReached =
        animatedProgress >= 0.98f

    val blobScaleX =
        0.7f + animatedProgress * 0.45f

    val blobScaleY =
        0.7f + animatedProgress * 0.25f

    val iconScale =
        0.72f + animatedProgress * 0.38f

    val iconRotation =
        -20f + animatedProgress * 20f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            /*
             * Elastic lime blob behind the icon.
             */
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer {
                        scaleX = blobScaleX
                        scaleY = blobScaleY
                        alpha =
                            0.2f +
                                    animatedProgress * 0.8f
                    }
                    .clip(CircleShape)
                    .background(DeutschLime)
            )

            AnimatedContent(
                targetState = thresholdReached,
                transitionSpec = {
                    fadeIn(
                        animationSpec = spring(
                            dampingRatio =
                                Spring
                                    .DampingRatioLowBouncy
                        )
                    ) togetherWith fadeOut()
                },
                label = "archive-pull-icon"
            ) { reached ->
                Icon(
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                            rotationZ = if (reached) {
                                0f
                            } else {
                                iconRotation
                            }
                        },
                    imageVector = if (reached) {
                        Icons.Outlined.Archive
                    } else {
                        Icons.Outlined
                            .KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = DeutschTextOnLime
                )
            }
        }

        AnimatedContent(
            modifier = Modifier.padding(
                start = 10.dp
            ),
            targetState = thresholdReached,
            transitionSpec = {
                (
                        fadeIn() togetherWith fadeOut()
                        ).using(
                        SizeTransform(
                            clip = false
                        )
                    )
            },
            label = "archive-pull-text"
        ) { reached ->
            Text(
                text = if (reached) {
                    "Loslassen für Archiv"
                } else {
                    "Nach unten ziehen für Archiv"
                },
                color = if (reached) {
                    DeutschLime
                } else {
                    DeutschTextSecondary
                },
                style =
                    MaterialTheme.typography
                        .labelLarge,
                fontWeight = if (reached) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
            )
        }
    }
}