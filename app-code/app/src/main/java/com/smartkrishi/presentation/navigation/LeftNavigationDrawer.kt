package com.smartkrishi.presentation.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.smartkrishi.presentation.model.Farm
import com.smartkrishi.presentation.theme.ThemeState
import kotlinx.coroutines.launch

@Composable
fun SmartKrishiAppRoot() {

    // READ GLOBAL THEME STATE
    val isDark = ThemeState.isDarkTheme.value

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // GLOBAL FARM STATE
    val selectedFarmState = remember { mutableStateOf<Farm?>(null) }

    // APP THEMING
    MaterialTheme(
        colorScheme = if (isDark) darkColorScheme else lightColorScheme
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawer(
                    selectedFarm = selectedFarmState.value,
                    onDestinationClick = { screen ->
                        scope.launch { drawerState.close() }
                        navController.navigate(screen.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Screen.Dashboard.route)
                        }
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    }
                )
            }
        ) {
            NavGraph(
                navController = navController,
                drawerState = drawerState,
                selectedFarmState = selectedFarmState
            )
        }
    }
}

// =====================================================================================
// PROVIDED COLOR SCHEMES (GLOBAL)
// =====================================================================================
private val lightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    background = Color(0xFFF4F6F5),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF0A140D),
)

private val darkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    background = Color(0xFF0A1C12),
    surface = Color(0xFF153525),
    onPrimary = Color.White,
    onBackground = Color(0xFFE8F5E9),
)


// =====================================================================================
// DRAWER — NOW FULLY THEME AWARE
// =====================================================================================
@Composable
private fun AppDrawer(
    selectedFarm: Farm?,
    onDestinationClick: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    val isDark = ThemeState.isDarkTheme.value

    val drawerBg = if (isDark) Color(0xFF0D1F14) else Color(0xFFE8F5E9)
    val textColor = if (isDark) Color(0xFFE8F5E9) else Color(0xFF0A140D)

    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(drawerBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {

            Text(
                text = "SMART KRISHI",
                style = MaterialTheme.typography.headlineSmall,
                color = textColor
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = selectedFarm?.name ?: "No farm selected",
                color = textColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(24.dp))

            DrawerItem("Dashboard", Icons.Default.Dashboard, textColor) {
                onDestinationClick(Screen.Dashboard)
            }
            DrawerItem("Disease Detection", Icons.Default.MedicalServices, textColor) {
                onDestinationClick(Screen.DiseaseDetection)
            }
            DrawerItem("Market Prices", Icons.Default.AttachMoney, textColor) {
                onDestinationClick(Screen.MarketPrice)
            }
            DrawerItem("Rover", Icons.Default.PedalBike, textColor) {
                onDestinationClick(Screen.Rover)
            }
            DrawerItem("Logs & History", Icons.Default.History, textColor) {
                onDestinationClick(Screen.Logs)
            }
            DrawerItem("Krishi Mitri", Icons.Default.Chat, textColor) {
                onDestinationClick(Screen.KrishiMitri)
            }
            DrawerItem("Profile", Icons.Default.Person, textColor) {
                onDestinationClick(Screen.Profile)
            }

            Spacer(Modifier.height(14.dp))
            Divider(color = textColor.copy(alpha = 0.25f))
            Spacer(Modifier.height(14.dp))

            DrawerItem("Govt. Schemes", Icons.Default.AccountBalance, textColor) {
                onDestinationClick(Screen.GovtSchemes)
            }
            DrawerItem("Crop Listing", Icons.Default.Grass, textColor) {
                onDestinationClick(Screen.Crops)
            }
            DrawerItem("Equipment Listing", Icons.Default.Agriculture, textColor) {
                onDestinationClick(Screen.Equipment)
            }
        }

        // BOTTOM — LOGOUT
        Column {
            Divider(color = textColor.copy(alpha = 0.25f))
            Spacer(Modifier.height(12.dp))

            DrawerItem("Logout", Icons.Default.ExitToApp, textColor) {
                onLogout()
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = color)
        Spacer(Modifier.width(14.dp))
        Text(label, color = color, style = MaterialTheme.typography.titleMedium)
    }
}
