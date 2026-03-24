package com.smartkrishi.presentation.navigation

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.smartkrishi.R
import com.smartkrishi.presentation.SplashVideoScreen
import com.smartkrishi.presentation.addNewFarm.AddNewFarm
import com.smartkrishi.presentation.addNewFarm.EditFarmScreen
import com.smartkrishi.presentation.auth.FarmerDetailsScreen
import com.smartkrishi.presentation.auth.LoginScreen
import com.smartkrishi.presentation.chat.KrishiMitriChatScreen
import com.smartkrishi.presentation.crops.CropsScreen
import com.smartkrishi.presentation.dashboard.DashboardScreen
import com.smartkrishi.presentation.disease.*
import com.smartkrishi.presentation.equipment.EquipmentScreen
import com.smartkrishi.presentation.home.FarmViewModel
import com.smartkrishi.presentation.home.HomeLandingScreen
import com.smartkrishi.presentation.logs.LogsScreen
import com.smartkrishi.presentation.market.MarketPriceScreen
import com.smartkrishi.presentation.model.Farm
import com.smartkrishi.presentation.onboarding.LanguageSelectionScreen
import com.smartkrishi.presentation.onboarding.OnboardingScreen
import com.smartkrishi.presentation.profile.ProfileScreen
import com.smartkrishi.presentation.rover.RoverScreen
import com.smartkrishi.presentation.schemes.GovtSchemesScreen
import com.smartkrishi.presentation.faq.FAQScreen
import com.smartkrishi.utils.SessionManager
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private const val TAG = "NavGraph"

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NavGraph(
    navController: androidx.navigation.NavHostController,
    drawerState: DrawerState,
    selectedFarmState: MutableState<Farm?>
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        // ═══════════════════════════════════════════════════════════
        // SPLASH SCREEN
        // ═══════════════════════════════════════════════════════════
        composable(Screen.Splash.route) {
            SplashVideoScreen(
                onFinish = {
                    val auth = FirebaseAuth.getInstance()
                    val user = auth.currentUser
                    val isLogged = SessionManager.isLoggedIn(context)

                    Log.d(TAG, "🚀 Splash: User = ${user?.email}, IsLogged = $isLogged")

                    if (user != null && isLogged) {
                        Log.d(TAG, "✅ Navigating to HomeLanding")
                        navController.navigate(Screen.HomeLanding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        Log.d(TAG, "🔹 Navigating to Onboarding")
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // ═══════════════════════════════════════════════════════════
        // ONBOARDING FLOW
        // ═══════════════════════════════════════════════════════════
        composable(Screen.Onboarding.route) {
            OnboardingScreen {
                navController.navigate(Screen.Language.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable(Screen.Language.route) {
            LanguageSelectionScreen {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Language.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        // ═══════════════════════════════════════════════════════════
        // AUTHENTICATION
        // ═══════════════════════════════════════════════════════════
        composable(Screen.Login.route) {
            LaunchedEffect(Unit) {
                Log.d(TAG, "📱 Login Screen loaded")
                Log.d(TAG, "Current user: ${FirebaseAuth.getInstance().currentUser?.email}")
                Log.d(TAG, "Session logged in: ${SessionManager.isLoggedIn(context)}")
            }

            LoginScreen(
                onLoginSuccess = { email ->
                    Log.d(TAG, "✅ Login success for: $email")
                    SessionManager.setLogin(context, true)
                    SessionManager.setUserEmail(context, email)
                    checkUserProfileAndNavigate(navController, email, context)
                }
            )
        }

        composable(
            route = Screen.FarmerDetails.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { entry ->
            val email = URLDecoder.decode(
                entry.arguments?.getString("email") ?: "",
                StandardCharsets.UTF_8.toString()
            )

            Log.d(TAG, "👨‍🌾 FarmerDetails screen for: $email")

            FarmerDetailsScreen(
                email = email,
                onSubmitSuccess = {
                    Log.d(TAG, "✅ Farmer details submitted, navigating to HomeLanding")
                    navController.navigate(Screen.HomeLanding.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ═══════════════════════════════════════════════════════════
        // HOME LANDING - MAIN FARM LIST
        // ═══════════════════════════════════════════════════════════
        composable(Screen.HomeLanding.route) {
            Log.d(TAG, "🏠 HomeLanding screen loaded")

            val farmVM: FarmViewModel = androidx.hilt.navigation.compose.hiltViewModel()

            HomeLandingScreen(
                navController = navController,
                onOpenFarm = { farm ->
                    selectedFarmState.value = farm
                    farmVM.selectFarm(farm)
                    Log.d(TAG, "📊 Opening farm dashboard: ${farm.name}")
                    navController.navigate(Screen.Dashboard.route) {
                        launchSingleTop = true
                    }
                },

                onAddFarm = {
                    Log.d(TAG, "➕ Adding new farm")
                    navController.navigate(Screen.AddFarm.route) {
                        launchSingleTop = true
                    }
                },

                onEditFarm = { farm ->
                    Log.d(TAG, "✏️ Edit Farm Clicked")
                    Log.d(TAG, "Farm ID: ${farm.id}, Name: ${farm.name}")

                    selectedFarmState.value = farm
                    farmVM.selectFarm(farm)

                    navController.navigate("editFarm/${farm.id}") {
                        launchSingleTop = true
                    }
                },

                onDeleteFarm = { farm ->
                    Log.d(TAG, "🗑️ Deleting farm: ${farm.id}")
                    farmVM.deleteFarm(
                        farmId = farm.id,
                        onSuccess = {
                            Log.d(TAG, "✅ Farm deleted successfully")
                            android.widget.Toast.makeText(
                                context,
                                "✅ ${farm.name} deleted successfully",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { error ->
                            Log.e(TAG, "❌ Failed to delete farm: $error")
                            android.widget.Toast.makeText(
                                context,
                                "❌ Failed to delete: $error",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                },

                onSkip = {
                    val farms = farmVM.farms.value

                    if (farms.isNotEmpty()) {
                        val firstFarm = farms.first()
                        selectedFarmState.value = firstFarm
                        farmVM.selectFarm(firstFarm)
                        Log.d(TAG, "⏭️ Skipping to dashboard with farm: ${firstFarm.name}")
                        navController.navigate(Screen.Dashboard.route) {
                            launchSingleTop = true
                        }
                    } else {
                        Log.w(TAG, "⚠️ No farms available to skip")
                        android.widget.Toast.makeText(
                            context,
                            "⚠️ Please add a farm first",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },

                viewModel = farmVM
            )
        }

        // ═══════════════════════════════════════════════════════════
        // FARM MANAGEMENT
        // ═══════════════════════════════════════════════════════════

        composable(Screen.AddFarm.route) {
            Log.d(TAG, "➕ Add Farm screen")
            AddNewFarm(
                onFarmSaved = {
                    Log.d(TAG, "✅ Farm saved, returning to home")
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "editFarm/{farmId}",
            arguments = listOf(navArgument("farmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: ""
            val farmVM: FarmViewModel = androidx.hilt.navigation.compose.hiltViewModel()

            val farms by farmVM.farms.collectAsState()
            val selectedFarm by farmVM.selectedFarm.collectAsState()
            val isLoading by farmVM.isLoading.collectAsState()

            val farm = selectedFarm?.takeIf { it.id == farmId } ?: farms.find { it.id == farmId }

            Log.d(TAG, "✏️ Edit Farm Screen")
            Log.d(TAG, "farmId: $farmId")
            Log.d(TAG, "selectedFarm: ${selectedFarm?.name} (${selectedFarm?.id})")
            Log.d(TAG, "farms count: ${farms.size}")
            Log.d(TAG, "found farm: ${farm?.name}")

            when {
                farm != null -> {
                    Log.d(TAG, "✅ Showing EditFarmScreen for: ${farm.name}")
                    EditFarmScreen(
                        farm = farm,
                        viewModel = farmVM,
                        onFarmUpdated = {
                            Log.d(TAG, "✅ Farm updated, returning to home")
                            navController.popBackStack()
                        },
                        onCancel = {
                            Log.d(TAG, "❌ Edit cancelled, returning to home")
                            navController.popBackStack()
                        }
                    )
                }

                isLoading -> {
                    Log.d(TAG, "⏳ Loading farm data...")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF2E7D32),
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                "Loading farm details...",
                                color = Color.Gray
                            )
                        }
                    }
                }

                else -> {
                    LaunchedEffect(Unit) {
                        Log.e(TAG, "❌ Farm not found: $farmId")
                        Log.e(TAG, "Available farms: ${farms.map { "${it.id}: ${it.name}" }}")
                        android.widget.Toast.makeText(
                            context,
                            "❌ Farm not found",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack()
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════════
        // DASHBOARD - MAIN FARM VIEW
        // ═══════════════════════════════════════════════════════════
        composable(Screen.Dashboard.route) {
            val farm = selectedFarmState.value
            Log.d(TAG, "📊 Dashboard screen for farm: ${farm?.name ?: "No farm selected"}")

            DashboardScreen(
                drawerState = drawerState,
                navController = navController,
                farm = farm ?: Farm(
                    id = "",
                    name = "No Farm Selected",
                    location = "",
                    lat = 0.0,
                    lon = 0.0,
                    acres = 0,
                    cropType = "NA",
                    soilType = "",
                    roverId = "",
                    backgroundRes = R.drawable.farm_placeholder,
                    ownerId = "",
                    userEmail = ""
                )
            )
        }

        // ═══════════════════════════════════════════════════════════
        // DISEASE DETECTION FLOW - UPDATED WITH CROP SELECTION
        // ═══════════════════════════════════════════════════════════
        composable(Screen.DiseaseDetection.route) {
            Log.d(TAG, "🦠 Disease Detection screen")
            DiseaseDetectionScreen(navController)
        }

        // Updated Disease Processing with imageUri and cropType parameters
        composable(
            route = Screen.DiseaseProcessing.route,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("cropType") { type = NavType.StringType }
            )
        ) { entry ->
            Log.d(TAG, "⚙️ Disease Processing screen")
            Log.d(TAG, "Image URI: ${entry.arguments?.getString("imageUri")}")
            Log.d(TAG, "Crop Type: ${entry.arguments?.getString("cropType")}")

            val imageUriString = entry.arguments?.getString("imageUri")
            val cropType = entry.arguments?.getString("cropType") ?: "Tomato"

            val imageUri = imageUriString?.let {
                try {
                    Uri.parse(Uri.decode(it))
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Invalid URI: $it", e)
                    null
                }
            }

            DiseaseProcessingScreen(
                navController = navController,
                imageUri = imageUri,
                selectedCrop = cropType
            )
        }

        // ═══════════════════════════════════════════════════════════
        // DISEASE RESULT SCREEN (FIXED)
        // ═══════════════════════════════════════════════════════════
        // ═══════════════════════════════════════════════════════════
// DISEASE RESULT SCREEN (HYBRID AI UPDATED)
// ═══════════════════════════════════════════════════════════
        // ═══════════════════════════════════════════════════════════
// DISEASE RESULT SCREEN (HYBRID AI UPDATED)
// ═══════════════════════════════════════════════════════════
        composable(
            route = Screen.DiseaseResult.route,
            arguments = listOf(
                navArgument("crop") { type = NavType.StringType },
                navArgument("disease") { type = NavType.StringType },
                navArgument("confidence") { type = NavType.FloatType },
                navArgument("imageUri") { type = NavType.StringType }
            )
        ) { entry ->

            val crop = URLDecoder.decode(
                entry.arguments?.getString("crop") ?: "Unknown",
                StandardCharsets.UTF_8.toString()
            )

            val disease = URLDecoder.decode(
                entry.arguments?.getString("disease") ?: "Unknown",
                StandardCharsets.UTF_8.toString()
            )

            val confidence = entry.arguments?.getFloat("confidence") ?: 0f

            val imageUri = entry.arguments?.getString("imageUri")?.let {
                Uri.decode(it)
            } ?: ""

            DiseaseResultScreen(
                navController = navController,
                crop = crop,
                disease = disease,
                confidence = confidence,
                imageUriString = imageUri
            )
        }     // ═══════════════════════════════════════════════════════════
        // KRISHI MITRI CHATBOT WITH CHATGPT
        // ═══════════════════════════════════════════════════════════
        composable(Screen.KrishiMitri.route) {
            Log.d(TAG, "🤖 Krishi Mitri Chat screen")

            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            var userName by remember { mutableStateOf<String?>(null) }
            var isLoadingUser by remember { mutableStateOf(true) }

            // Fetch user name from Firestore
            LaunchedEffect(Unit) {
                if (currentUser != null) {
                    FirebaseFirestore.getInstance()
                        .collection("farmers")
                        .document(currentUser.uid)
                        .get()
                        .addOnSuccessListener { doc ->
                            userName = doc.getString("name") ?: "Farmer"
                            isLoadingUser = false
                            Log.d(TAG, "✅ User name fetched: $userName")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "❌ Failed to fetch user name: ${e.message}")
                            userName = "Farmer" // Fallback
                            isLoadingUser = false
                        }
                } else {
                    userName = "Farmer" // Fallback if no user
                    isLoadingUser = false
                }
            }

            // Show loading or chat screen
            if (isLoadingUser) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF4CAF50),
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            "Loading KrishiMitri...",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                KrishiMitriChatScreen(
                    userName = userName,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // ═══════════════════════════════════════════════════════════
        // BOTTOM NAV SCREENS
        // ═══════════════════════════════════════════════════════════

        composable(Screen.FAQ.route) {
            Log.d(TAG, "❓ FAQ screen")
            FAQScreen(navController)
        }

        composable(Screen.Profile.route) {
            Log.d(TAG, "👤 Profile screen")
            ProfileScreen(navController)
        }

        // ═══════════════════════════════════════════════════════════
        // DRAWER NAV SCREENS
        // ═══════════════════════════════════════════════════════════

        composable(Screen.MarketPrice.route) {
            Log.d(TAG, "💰 Market Price screen")
            MarketPriceScreen(navController)
        }

        composable(Screen.GovtSchemes.route) {
            Log.d(TAG, "🏛️ Govt Schemes screen")
            GovtSchemesScreen(navController)
        }

        composable(Screen.Crops.route) {
            Log.d(TAG, "🌾 Crops screen")
            CropsScreen()
        }

        composable(Screen.Equipment.route) {
            Log.d(TAG, "🔧 Equipment screen")
            EquipmentScreen()
        }
// ═══════════════════════════════════════════════════════════
// GEMINI AI RECOMMENDATION SCREENS
// ═══════════════════════════════════════════════════════════

        composable(Screen.CropRecommendation.route) {
            Log.d(TAG, "🌾 AI Crop Recommendation screen")

            com.smartkrishi.presentation.ai.CropRecommendationScreen(
                navController = navController
            )
        }

        composable(Screen.FertilizerRecommendation.route) {
            Log.d(TAG, "🧪 AI Fertilizer Recommendation screen")

            com.smartkrishi.presentation.ai.FertilizerRecommendationScreen(
                navController = navController
            )
        }
        // ═══════════════════════════════════════════════════════════
        // ROVER/SENSORS SCREEN
        // ═══════════════════════════════════════════════════════════
        composable(Screen.Rover.route) {
            Log.d(TAG, "📹 Rover screen")
            val farm = selectedFarmState.value
            if (farm != null) {
                RoverScreen(navController = navController)
            } else {
                LaunchedEffect(Unit) {
                    Log.w(TAG, "⚠️ No farm selected for Rover")
                    android.widget.Toast.makeText(
                        context,
                        "⚠️ Please select a farm first",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    navController.popBackStack()
                }
            }
        }

        // ═══════════════════════════════════════════════════════════
        // LOGS & HISTORY
        // ═══════════════════════════════════════════════════════════
        composable(Screen.Logs.route) {
            Log.d(TAG, "📜 Logs screen")
            LogsScreen(navController)
        }

        // ═══════════════════════════════════════════════════════════
        // LOGOUT
        // ═══════════════════════════════════════════════════════════
        composable(Screen.Logout.route) {
            val farmVM: FarmViewModel = androidx.hilt.navigation.compose.hiltViewModel()

            LaunchedEffect(Unit) {
                Log.d(TAG, "🚪 Logging out user")

                FirebaseAuth.getInstance().signOut()
                SessionManager.clearSession(context)
                selectedFarmState.value = null
                farmVM.logout()

                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }

                Log.d(TAG, "✅ Logout complete")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// HELPER FUNCTIONS
// ═══════════════════════════════════════════════════════════════════

private fun checkUserProfileAndNavigate(
    navController: androidx.navigation.NavHostController,
    email: String,
    context: android.content.Context
) {
    Log.d(TAG, "🔍 Checking profile for: $email")

    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        Log.e(TAG, "❌ User is not authenticated!")
        android.widget.Toast.makeText(
            context,
            "❌ Authentication error. Please login again.",
            android.widget.Toast.LENGTH_LONG
        ).show()
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
        return
    }

    Log.d(TAG, "✅ User authenticated: ${currentUser.email}")

    val db = FirebaseFirestore.getInstance()

    db.collection("farmers")
        .document(currentUser.uid)
        .get()
        .addOnSuccessListener { doc ->
            if (doc.exists()) {
                Log.d(TAG, "✅ Profile exists, navigating to HomeLanding")
                navController.navigate(Screen.HomeLanding.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                Log.d(TAG, "❌ Profile doesn't exist, navigating to FarmerDetails")
                navController.navigate(Screen.FarmerDetails.pass(email)) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        .addOnFailureListener { exception ->
            Log.e(TAG, "❌ Firestore error: ${exception.message}", exception)

            if (exception is FirebaseFirestoreException) {
                when (exception.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        Log.e(TAG, "🚫 PERMISSION DENIED - Check Firestore security rules!")
                        android.widget.Toast.makeText(
                            context,
                            "🚫 Permission denied. Please check Firestore rules.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }

                    FirebaseFirestoreException.Code.UNAVAILABLE -> {
                        Log.e(TAG, "📡 Network unavailable")
                        android.widget.Toast.makeText(
                            context,
                            "📡 Network error. Please check your connection.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }

                    FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                        Log.e(TAG, "🔐 User not authenticated")
                        android.widget.Toast.makeText(
                            context,
                            "🔐 Authentication expired. Please login again.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                        return@addOnFailureListener
                    }

                    else -> {
                        android.widget.Toast.makeText(
                            context,
                            "❌ Error: ${exception.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            Log.d(TAG, "⚠️ Navigating to FarmerDetails as fallback")
            navController.navigate(Screen.FarmerDetails.pass(email)) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
}