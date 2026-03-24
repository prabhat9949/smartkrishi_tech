package com.smartkrishi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.smartkrishi.presentation.model.Farm
import com.smartkrishi.presentation.navigation.NavGraph
import com.smartkrishi.presentation.theme.SmartKrishiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            SmartKrishiTheme {

                val navController = rememberNavController()

                val drawerState = remember { DrawerState(initialValue = DrawerValue.Closed) }

                val selectedFarmState = remember { mutableStateOf<Farm?>(null) }

                NavGraph(
                    navController = navController,
                    drawerState = drawerState,
                    selectedFarmState = selectedFarmState
                )
            }
        }
    }
}
