package com.julesmeister.deutschcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.julesmeister.deutschcraft.navigation.DeutschCraftNavHost
import com.julesmeister.deutschcraft.ui.theme.DeutschCraftTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {
            DeutschCraftTheme {
                DeutschCraftNavHost()
            }
        }
    }
}