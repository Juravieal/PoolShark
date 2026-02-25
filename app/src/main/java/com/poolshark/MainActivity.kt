package com.poolshark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.poolcoach.ui.navigation.PoolCoachNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isFirstLaunch = !getPreferences(MODE_PRIVATE).getBoolean("calibrated", false)
        setContent {
            PoolCoachNavGraph(isFirstLaunch = isFirstLaunch)
        }
    }
}
