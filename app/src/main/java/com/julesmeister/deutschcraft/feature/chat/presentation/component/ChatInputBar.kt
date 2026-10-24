package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschOutline
import com.julesmeister.deutschcraft.ui.theme.DeutschSurface
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
fun ChatInputBar(
    value: String,
    canSend: Boolean,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.background
            )
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
        verticalAlignment = Alignment.Bottom
    ) {
        IconButton(
            modifier = Modifier
                .padding(end = 8.dp)
                .background(
                    color = DeutschSurface,
                    shape = CircleShape
                ),
            onClick = onAddClick
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "More actions",
                tint = DeutschTextPrimary
            )
        }

        TextField(
            modifier = Modifier.weight(1f),
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = "Nachricht schreiben",
                    color = DeutschTextSecondary
                )
            },
            maxLines = 5,
            shape = RoundedCornerShape(26.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (canSend) {
                        onSendClick()
                    }
                }
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor =
                    DeutschSurface,
                unfocusedContainerColor =
                    DeutschSurface,
                disabledContainerColor =
                    DeutschSurface,

                focusedTextColor =
                    DeutschTextPrimary,
                unfocusedTextColor =
                    DeutschTextPrimary,
                disabledTextColor =
                    DeutschTextSecondary,

                cursorColor = DeutschLime,

                focusedIndicatorColor =
                    Color.Transparent,
                unfocusedIndicatorColor =
                    Color.Transparent,
                disabledIndicatorColor =
                    Color.Transparent
            )
        )

        FilledIconButton(
            modifier = Modifier.padding(
                start = 8.dp
            ),
            onClick = onSendClick,
            enabled = canSend,
            shape = CircleShape,
            colors =
                IconButtonDefaults
                    .filledIconButtonColors(
                        containerColor =
                            DeutschLime,
                        contentColor =
                            DeutschTextOnLime,
                        disabledContainerColor =
                            DeutschOutline,
                        disabledContentColor =
                            DeutschTextSecondary
                    )
        ) {
            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message"
            )
        }
    }
}