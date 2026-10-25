package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime

@Composable
fun CreateThreadFab(
    isCreatingThread: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = {
            if (!isCreatingThread) {
                onClick()
            }
        },
        containerColor = DeutschLime,
        contentColor = DeutschTextOnLime,
        shape = CircleShape
    ) {
        if (isCreatingThread) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = DeutschTextOnLime,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription =
                    "Neue Unterhaltung"
            )
        }
    }
}