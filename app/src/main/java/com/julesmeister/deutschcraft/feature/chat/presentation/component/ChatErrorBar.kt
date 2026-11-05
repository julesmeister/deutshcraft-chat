package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatErrorType

@Composable
fun ChatErrorBar(
    error: ChatErrorType?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = error != null,
        enter = slideInVertically(
            initialOffsetY = { it }
        ),
        exit = slideOutVertically(
            targetOffsetY = { it }
        ),
        modifier = modifier
    ) {
        if (error != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        color = Color(0xFFD32F2F)
                    )
                    .padding(
                        horizontal = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = error.displayMessage,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .weight(1f)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.Close,
                        contentDescription =
                            "Dismiss error",
                        tint = Color.White,
                        modifier = Modifier
                            .height(16.dp)
                    )
                }
            }
        }
    }
}
