package com.smartkrishi.presentation.navigation

import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {

    // ------------------------------------------------
    // APP ENTRY
    // ------------------------------------------------
    object Splash : Screen("splash")

    // ------------------------------------------------
    // FIRST TIME FLOW
    // ------------------------------------------------
    object Onboarding : Screen("onboarding")
    object Language : Screen("language")

    // ------------------------------------------------
    // AUTHENTICATION
    // ------------------------------------------------
    object Login : Screen("login")
    object Otp : Screen("otp")

    // ------------------------------------------------
    // USER DETAILS
    // ------------------------------------------------
    object FarmerDetails : Screen("farmer_details/{email}") {
        fun pass(email: String): String {
            return "farmer_details/${Uri.encode(email)}"
        }
    }

    // ------------------------------------------------
    // MAIN APP (AFTER LOGIN)
    // ------------------------------------------------
    object HomeLanding : Screen("home_landing")
    object Dashboard : Screen("dashboard")
    object Logout : Screen("logout")

    // ------------------------------------------------
    // FARM MODULE
    // ------------------------------------------------
    object AddFarm : Screen("add_farm")

    object FarmNodes : Screen("farm_nodes/{farmId}") {
        fun pass(farmId: String): String {
            return "farm_nodes/${Uri.encode(farmId)}"
        }
    }

    object FarmMap : Screen("farm_map/{farmId}") {
        fun pass(farmId: String): String {
            return "farm_map/${Uri.encode(farmId)}"
        }
    }

    object EditFarm : Screen("edit_farm/{farmId}") {
        fun pass(farmId: String): String {
            return "edit_farm/${Uri.encode(farmId)}"
        }
    }

    // ------------------------------------------------
    // PROFILE
    // ------------------------------------------------
    object Profile : Screen("profile")

    // ------------------------------------------------
    // AI CHAT
    // ------------------------------------------------
    object KrishiMitri : Screen("krishimitri_chat")

    // ------------------------------------------------
    // AI RECOMMENDATION SCREENS (NEW GEMINI SCREENS)
    // ------------------------------------------------
    object CropRecommendation : Screen("crop_recommendation")

    object FertilizerRecommendation : Screen("fertilizer_recommendation")

    // ------------------------------------------------
    // DISEASE MODULE
    // ------------------------------------------------
    object DiseaseDetection : Screen("disease_detection")

    object DiseaseProcessing :
        Screen("disease_processing/{imageUri}/{cropType}") {

        fun pass(imageUri: String, cropType: String): String {
            val encodedUri = Uri.encode(imageUri)
            val encodedCrop = Uri.encode(cropType)
            return "disease_processing/$encodedUri/$encodedCrop"
        }
    }

    object DiseaseResult :
        Screen("disease_result/{crop}/{disease}/{confidence}/{imageUri}") {

        fun pass(
            crop: String,
            disease: String,
            confidence: Float,
            imageUri: String
        ): String {

            val encodedCrop =
                URLEncoder.encode(crop, StandardCharsets.UTF_8.toString())

            val encodedDisease =
                URLEncoder.encode(disease, StandardCharsets.UTF_8.toString())

            val encodedUri = Uri.encode(imageUri)

            return "disease_result/$encodedCrop/$encodedDisease/$confidence/$encodedUri"
        }
    }

    // ------------------------------------------------
    // MARKET MODULE
    // ------------------------------------------------
    object MarketPrice : Screen("market_price")

    // ------------------------------------------------
    // ROVER MODULE
    // ------------------------------------------------
    object Rover : Screen("rover")

    // ------------------------------------------------
    // LOGS
    // ------------------------------------------------
    object Logs : Screen("logs")

    // ------------------------------------------------
    // ASSISTANT
    // ------------------------------------------------
    object Assistant : Screen("assistant")

    // ------------------------------------------------
    // OTHER MODULES
    // ------------------------------------------------
    object GovtSchemes : Screen("govt_schemes")
    object Crops : Screen("crop_listing")
    object Equipment : Screen("equipment_listing")

    // ------------------------------------------------
    // FAQ
    // ------------------------------------------------
    object FAQ : Screen("faq")
}