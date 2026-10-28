package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
        modifier =
            modifier,
        onClick = {
            if (!isCreatingThread) {
                onClick()
            }
        },
        containerColor =
            DeutschLime,
        contentColor =
            DeutschTextOnLime,

        /*
         * Soft rounded shape everywhere except the
         * bottom-right, which is completely flat.
         */
        shape =
            RoundedCornerShape(
                topStart =
                    FAB_CORNER_RADIUS,
                topEnd =
                    FAB_CORNER_RADIUS,
                bottomStart =
                    FAB_CORNER_RADIUS,
                bottomEnd =
                    FAB_FLAT_CORNER
            ),

        /*
         * No Material FAB shadow whatsoever.
         *
         * Both resting and pressed elevation remain at 0dp,
         * so pressing the button does not briefly introduce
         * another shadow.
         */
        elevation =
            FloatingActionButtonDefaults.elevation(
                defaultElevation =
                    FAB_ELEVATION,
                pressedElevation =
                    FAB_ELEVATION,
                focusedElevation =
                    FAB_ELEVATION,
                hoveredElevation =
                    FAB_ELEVATION
            )
    ) {
        if (isCreatingThread) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(
                        FAB_PROGRESS_SIZE
                    ),
                color =
                    DeutschTextOnLime,
                strokeWidth =
                    FAB_PROGRESS_STROKE
            )
        } else {
            Icon(
                imageVector =
                    Icons.Outlined.Add,
                contentDescription =
                    "Neue Unterhaltung"
            )
        }
    }
}

private val FAB_CORNER_RADIUS =
    18.dp

private val FAB_FLAT_CORNER =
    0.dp

private val FAB_ELEVATION =
    0.dp

private val FAB_PROGRESS_SIZE =
    22.dp

private val FAB_PROGRESS_STROKE =
    2.dp