package com.julesmeister.deutschcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.julesmeister.deutschcraft.navigation.DeutschCraftNavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        setContent {
            DeutschCraftNavHost()
        }
    }
}