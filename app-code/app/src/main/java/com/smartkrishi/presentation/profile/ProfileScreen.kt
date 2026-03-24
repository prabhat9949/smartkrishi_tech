package com.smartkrishi.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.smartkrishi.presentation.navigation.Screen
import com.smartkrishi.presentation.theme.ThemeState
import com.smartkrishi.utils.SessionManager

private val PrimaryGreen = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {

    val isDark by ThemeState.isDarkTheme
    val background = if (isDark) Color(0xFF07150F) else Color(0xFFF4FFF7)
    val cardColor = if (isDark) Color(0xFF10271A) else Color.White
    val textPrimary = if (isDark) Color(0xFFE8F5E9) else Color(0xFF09150C)
    val textSecondary = if (isDark) Color(0xFFB5CBB8) else Color(0xFF5F6C63)

    // 👉 Firebase User Details
    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: "Farmer User"
    val userPhone = user?.phoneNumber ?: "Unknown"
    val userDistrict = "Your district"  // Optional (Future from DB)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF10271A) else Color(0xFFC8E6C9)
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // ==================== USER INFO ====================
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // Avatar + Name + Details
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(userName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textPrimary)
                            Text("Phone: $userPhone", fontSize = 13.sp, color = textSecondary)
                            Text("District: $userDistrict", fontSize = 13.sp, color = textSecondary)
                        }
                    }

                    // Account Section
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Account", fontWeight = FontWeight.SemiBold, color = textPrimary)
                            Text("Linked farms: 1 (default)", fontSize = 13.sp, color = textSecondary)
                            Text("Login method: Phone / Google", fontSize = 13.sp, color = textSecondary)
                        }
                    }

                    // Settings Section
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Settings, null, tint = PrimaryGreen)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("App Settings", fontWeight = FontWeight.SemiBold, color = textPrimary)
                                Text(
                                    "Language & theme are managed from Dashboard.",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }

                // ==================== LOGOUT BUTTON ====================
                Column {
                    Divider(color = textSecondary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            SessionManager.clearSession(navController.context)
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Logout", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
