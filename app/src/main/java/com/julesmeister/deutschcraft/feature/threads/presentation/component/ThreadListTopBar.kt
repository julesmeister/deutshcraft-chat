package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadListTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "Unterhaltungen",
                color = DeutschTextPrimary,
                style =
                    MaterialTheme.typography
                        .titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .background,
                titleContentColor =
                    DeutschTextPrimary
            )
    )
}