package com.smartkrishi.presentation.navigation

import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {

    // ---------------- APP ENTRY ----------------
    object Splash : Screen("splash")

    // ---------------- FIRST TIME FLOW ----------------
    object Onboarding : Screen("onboarding")
    object Language : Screen("language") // ✅ ADDED

    // ---------------- AUTH ----------------
    object Login : Screen("login")
    object Otp : Screen("otp")

    // ---------------- USER DETAILS ----------------
    object FarmerDetails : Screen("farmer_details/{email}") {
        fun pass(email: String): String =
            "farmer_details/${Uri.encode(email)}"
    }

    // ---------------- AFTER LOGIN ----------------
    object HomeLanding : Screen("home_landing")
    object Dashboard : Screen("dashboard")
    object Logout : Screen("logout")

    // ---------------- FARM MODULE ----------------
    object AddFarm : Screen("add_farm")
    object FarmNodes : Screen("farm_nodes/{farmId}") {
        fun pass(farmId: String) = "farm_nodes/$farmId"
    }

    object FarmMap : Screen("farm_map/{farmId}") {
        fun pass(farmId: String) = "farm_map/$farmId"
    }

    object EditFarm : Screen("edit_farm/{farmId}") {
        fun pass(farmId: String) = "edit_farm/$farmId"
    }

    // ---------------- USER PROFILE ----------------
    object Profile : Screen("profile")

    // ---------------- AI CHAT ----------------
    object KrishiMitri : Screen("krishimitri_chat")

    // ---------------- DISEASE MODULE ----------------
    object DiseaseDetection : Screen("disease_detection")

    object DiseaseProcessing : Screen("disease_processing/{uri}") {
        fun pass(uri: String) =
            "disease_processing/${Uri.encode(uri)}"
    }

    object DiseaseResult :
        Screen("disease_result/{crop}/{disease}/{confidence}/{imageUri}") {

        fun pass(
            crop: String,
            disease: String,
            confidence: Float,
            imageUri: String
        ): String {

            val c = URLEncoder.encode(crop, StandardCharsets.UTF_8.toString())
            val d = URLEncoder.encode(disease, StandardCharsets.UTF_8.toString())
            val uri = Uri.encode(imageUri)

            return "disease_result/$c/$d/$confidence/$uri"
        }
    }

    // ---------------- MARKET ----------------
    object MarketPrice : Screen("market_price")

    // ---------------- ROVER ----------------
    object Rover : Screen("rover")

    // ---------------- LOGS ----------------
    object Logs : Screen("logs")

    // ---------------- ASSISTANT ----------------
    object Assistant : Screen("assistant")

    // ---------------- OTHER MODULES ----------------
    object GovtSchemes : Screen("govt_schemes")
    object Crops : Screen("crop_listing")
    object Equipment : Screen("equipment_listing")
}
