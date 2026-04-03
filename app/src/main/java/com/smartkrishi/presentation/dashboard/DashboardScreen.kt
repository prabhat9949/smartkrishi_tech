package com.smartkrishi.presentation.dashboard

import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.smartkrishi.R
import com.smartkrishi.ml.CropPrediction
import com.smartkrishi.ml.CropRecommendationHelper
import com.smartkrishi.presentation.chat.KrishiMitriChatScreen
import com.smartkrishi.presentation.chat.KrishiMitriChatViewModel
import com.smartkrishi.presentation.dashboard.Strings.pumpOn
import com.smartkrishi.presentation.home.FarmViewModel
import com.smartkrishi.presentation.model.Farm
import com.smartkrishi.presentation.model.SensorNode
import com.smartkrishi.presentation.navigation.Screen
import com.smartkrishi.presentation.theme.ThemeState
import com.smartkrishi.voice.VoiceCommandProcessor
import com.smartkrishi.voice.VoiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import com.smartkrishi.voice.VoiceHandler
// ═══════════════════════════════════════════════════════════════════
//  THEME COLORS
// ═══════════════════════════════════════════════════════════════════
private val PrimaryGreen    = Color(0xFF2E7D32)
private val AccentGreenSoft = Color(0xFFC8E6C9)
private val LightBackground = Color(0xFFF3FBF5)
private val DarkBackground  = Color(0xFF0A1C12)
private val CardLight       = Color(0xFFFFFFFF)
private val CardDark        = Color(0xFF153525)

// ═══════════════════════════════════════════════════════════════════
//  LANGUAGE STRINGS
// ═══════════════════════════════════════════════════════════════════
object Strings {
    val greetingMorning   = mapOf("EN" to "Good Morning",                 "HI" to "शुभ प्रभात")
    val greetingAfternoon = mapOf("EN" to "Good Afternoon",               "HI" to "शुभ दोपहर")
    val greetingEvening   = mapOf("EN" to "Good Evening",                 "HI" to "शुभ संध्या")
    val namaste           = mapOf("EN" to "Namaste",                      "HI" to "नमस्ते")
    val managing          = mapOf("EN" to "Managing",                     "HI" to "प्रबंध")
    val noFarm            = mapOf("EN" to "No farm selected",             "HI" to "कोई खेत नहीं चुना")
    val currentFarm       = mapOf("EN" to "Current Farm",                 "HI" to "वर्तमान खेत")
    val selectFarm        = mapOf("EN" to "Select Farm",                  "HI" to "खेत चुनें")
    val farmSystems       = mapOf("EN" to "Farm Systems",                 "HI" to "खेत प्रणाली")
    val soilNodes         = mapOf("EN" to "Soil Nodes",                   "HI" to "मृदा नोड्स")
    val weatherNotAvail   = mapOf("EN" to "Weather not available",        "HI" to "मौसम उपलब्ध नहीं")
    val loading           = mapOf("EN" to "Loading…",                     "HI" to "लोड हो रहा है…")
    val quickModules      = mapOf("EN" to "Quick Modules",                "HI" to "त्वरित मॉड्यूल")
    val logsHistory       = mapOf("EN" to "Logs & History",               "HI" to "लॉग और इतिहास")
    val last7days         = mapOf("EN" to "Last 7 days moisture events",  "HI" to "पिछले 7 दिन नमी घटनाएँ")
    val disease           = mapOf("EN" to "Disease",                      "HI" to "रोग")
    val rover             = mapOf("EN" to "Rover",                        "HI" to "रोवर")
    val market            = mapOf("EN" to "Market",                       "HI" to "बाज़ार")
    val logs              = mapOf("EN" to "Logs",                         "HI" to "लॉग")
    val chat              = mapOf("EN" to "Chat",                         "HI" to "चैट")
    val tds               = mapOf("EN" to "TDS",                          "HI" to "टीडीएस")
    val rainSensor        = mapOf("EN" to "Rain Sensor",                  "HI" to "वर्षा सेंसर")
    val rainDetected      = mapOf("EN" to "Rain Detected",                "HI" to "वर्षा हो रही है")
    val noRain            = mapOf("EN" to "No Rain",                      "HI" to "वर्षा नहीं")
    val pumpStatus        = mapOf("EN" to "Pump Status",                  "HI" to "पंप स्थिति")
    val tankLevel         = mapOf("EN" to "Tank Level",                   "HI" to "टैंक स्तर")
    val solar             = mapOf("EN" to "Solar Charging",               "HI" to "सौर चार्जिंग")
    val irrigation        = mapOf("EN" to "Irrigation",                   "HI" to "सिंचाई")
    val nextIn2hrs        = mapOf("EN" to "Next in 2 hrs",                "HI" to "2 घंटे में अगला")
    val autoMode          = mapOf("EN" to "Auto Mode",                    "HI" to "स्वचालित मोड")
    val liveFromField     = mapOf("EN" to "Live from field",              "HI" to "खेत से सीधे")
    val syncing           = mapOf("EN" to "Syncing…",                     "HI" to "सिंक हो रहा है…")
    val sync              = mapOf("EN" to "Sync",                         "HI" to "सिंक")
    val noSensorValues    = mapOf("EN" to "No sensor values available",   "HI" to "कोई सेंसर डेटा उपलब्ध नहीं")
    val waitingLive       = mapOf("EN" to "Waiting for live sensor data…","HI" to "लाइव डेटा की प्रतीक्षा…")
    val feelsLike         = mapOf("EN" to "Feels like",                   "HI" to "अनुभव")
    val sevenDay          = mapOf("EN" to "7-Day Forecast",               "HI" to "7 दिन का पूर्वानुमान")
    val addNewFarm        = mapOf("EN" to "Add New Farm",                 "HI" to "नया खेत जोड़ें")
    val krishi            = mapOf("EN" to "Krishi AI",                    "HI" to "कृषि AI")
    val currentConditions = mapOf("EN" to "Current Conditions",           "HI" to "वर्तमान स्थिति")
    val enoughWater       = mapOf("EN" to "Enough Water",                 "HI" to "पर्याप्त पानी")
    val charging          = mapOf("EN" to "Charging",                     "HI" to "चार्जिंग")
    val good              = mapOf("EN" to "Good",                         "HI" to "अच्छा")
    val dry               = mapOf("EN" to "Dry",                          "HI" to "शुष्क")
    val navigation        = mapOf("EN" to "Navigation",                   "HI" to "नेविगेशन")
    val dashboard         = mapOf("EN" to "Dashboard",                    "HI" to "डैशबोर्ड")
    val diseaseDetect     = mapOf("EN" to "Disease Detection",            "HI" to "रोग पहचान")
    val cropRecommend     = mapOf("EN" to "Crop Recommendation",          "HI" to "फसल सिफारिश")
    val fertRecommend     = mapOf("EN" to "Fertilizer Recommendation",    "HI" to "खाद सिफारिश")
    val roverControl      = mapOf("EN" to "Rover Control",                "HI" to "रोवर नियंत्रण")
    val logsMenu          = mapOf("EN" to "Logs & History",               "HI" to "लॉग और इतिहास")
    val krishiChat        = mapOf("EN" to "KrishiMitri Chat",             "HI" to "कृषि मित्र चैट")
    val profile           = mapOf("EN" to "Profile",                      "HI" to "प्रोफ़ाइल")
    val online            = mapOf("EN" to "Online",                       "HI" to "ऑनलाइन")
    val selectFarmTitle   = mapOf("EN" to "Select Farm",                  "HI" to "खेत चुनें")
    val farmsAvail        = mapOf("EN" to "farms available",              "HI" to "खेत उपलब्ध")
    val farmAvail1        = mapOf("EN" to "farm available",               "HI" to "खेत उपलब्ध")
    val updatingPump      = mapOf("EN" to "Updating",                     "HI" to "अपडेट हो रहा है")

    // Irrigation strings
    val irrigationPanel   = mapOf("EN" to "Smart Irrigation",             "HI" to "स्मार्ट सिंचाई")
    val lastIrrigated     = mapOf("EN" to "Last Irrigated",               "HI" to "अंतिम सिंचाई")
    val nextIrrigation    = mapOf("EN" to "Next Irrigation",              "HI" to "अगली सिंचाई")
    val moistureStatus    = mapOf("EN" to "Soil Moisture",                "HI" to "मृदा नमी")
    val pumpOn            = mapOf("EN" to "Running",                      "HI" to "चालू")
    val pumpOff           = mapOf("EN" to "Standby",                      "HI" to "बंद")
    val tapToOperate      = mapOf("EN" to "Tap anywhere to refresh",      "HI" to "रिफ्रेश के लिए कहीं भी टैप करें")
    val analysing         = mapOf("EN" to "Refreshing data…",             "HI" to "डेटा रिफ्रेश हो रहा है…")
    val lastUpdated       = mapOf("EN" to "Last updated",                 "HI" to "अंतिम अपडेट")
    val scheduleAdvisory  = mapOf("EN" to "Schedule Advisory",            "HI" to "शेड्यूल सलाह")
    val manualOpNote      = mapOf("EN" to "For manual pump operation, use the control below.",
        "HI" to "मैन्युअल पंप संचालन के लिए नीचे दिए नियंत्रण का उपयोग करें।")

    // Alert strings
    val alertsTitle       = mapOf("EN" to "Field Alerts",                 "HI" to "खेत अलर्ट")
    val alertSubtitle     = mapOf("EN" to "Active conditions requiring attention", "HI" to "ध्यान देने योग्य स्थितियाँ")
    val noAlerts          = mapOf("EN" to "All systems normal. No alerts at this time.", "HI" to "सभी प्रणालियाँ सामान्य हैं।")
    val highPriority      = mapOf("EN" to "HIGH",   "HI" to "उच्च")
    val medPriority       = mapOf("EN" to "MED",    "HI" to "मध्यम")
    val lowPriority       = mapOf("EN" to "LOW",    "HI" to "कम")
    val scrollForMore     = mapOf("EN" to "Scroll for more alerts", "HI" to "अधिक अलर्ट के लिए स्क्रॉल करें")

    fun get(map: Map<String, String>, lang: String) = map[lang] ?: map["EN"] ?: ""
}

// ═══════════════════════════════════════════════════════════════════
//  WEATHER DATA MODELS
// ═══════════════════════════════════════════════════════════════════
private const val OPEN_WEATHER_API_KEY = "64961347ba9d05d6b1a486037c5142c1"

lateinit var voiceProcessor: VoiceCommandProcessor
data class HourlyForecast(val time: String, val temp: String, val condition: String, val rainChance: Int)
data class MlCropRecommendation(val topCrops: List<CropPrediction>, val inputParams: String)
data class DailyForecast(
    val label: String, val minTempC: Int, val maxTempC: Int,
    val condition: String, val rainChance: Int, val iconCode: String = ""
)
data class WeatherInfo(
    val condition: String, val tempC: Int, val humidity: Int, val windKph: Int,
    val pressure: Int = 1013, val visibility: Double = 10.0, val uvIndex: Int = 0,
    val dewPoint: Int = 0, val feelsLike: Int = 0,
    val daily: List<DailyForecast>, val hourly: List<HourlyForecast> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)
data class AiRecommendations(val alertLine: String?, val cropSuggestions: List<String>, val fertiliserPlanLines: List<String>)

// ═══════════════════════════════════════════════════════════════════
//  IRRIGATION DATA MODELS
// ═══════════════════════════════════════════════════════════════════
data class IrrigationRecord(
    val timestamp: Long,
    val durationMinutes: Int,
    val waterLiters: Int,
    val triggerReason: String
)

data class IrrigationSchedule(
    val nextDateTime: Long,
    val durationMinutes: Int,
    val confidence: Int,
    val basisNote: String
)

data class IrrigationState(
    val lastRecord: IrrigationRecord?,
    val schedule: IrrigationSchedule,
    val advisoryLine: String,
    val moistureAvg: Float,
    val cropType: String,
    val lastAnalysedAt: Long
)

// ═══════════════════════════════════════════════════════════════════
//  STRUCTURED ALERT MODEL
// ═══════════════════════════════════════════════════════════════════
enum class AlertPriority { HIGH, MEDIUM, LOW }

data class FieldAlert(
    val priority: AlertPriority,
    val category: String,
    val title: String,
    val detail: String
)

// ═══════════════════════════════════════════════════════════════════
//  SENSOR UI MODELS
// ═══════════════════════════════════════════════════════════════════
data class SoilParam(val name: String, val displayValue: String, val numericValue: Float)

private data class SoilNodeUi(val id: String, val title: String, val params: List<SoilParam>)

// ═══════════════════════════════════════════════════════════════════
//  HELPER — format node id to "Zone N" instead of "Node zone_1"
// ═══════════════════════════════════════════════════════════════════
private fun formatZoneLabel(rawId: String, lang: String): String {
    // Extract numeric suffix from strings like "zone_1", "node_1", "1", "node1", etc.
    val num = rawId.filter { it.isDigit() }.takeIf { it.isNotEmpty() } ?: "1"
    return if (lang == "HI") "ज़ोन $num" else "Zone $num"
}

// ═══════════════════════════════════════════════════════════════════
//  STRUCTURED ALERT GENERATOR — no emojis, Zone N labels, max 3
// ═══════════════════════════════════════════════════════════════════
private fun generateFieldAlerts(
    nodes: List<SoilNodeUi>,
    weather: WeatherInfo?,
    lang: String
): List<FieldAlert> {
    val alerts = mutableListOf<FieldAlert>()

    nodes.forEach { node ->
        val zoneLabel = formatZoneLabel(node.id, lang)
        node.params.forEach { param ->
            val status = cropBasedStatus(null, param.name, param.numericValue)
            when {
                param.name == "Moisture" && status == "LOW" -> alerts += FieldAlert(
                    priority = AlertPriority.HIGH,
                    category = if (lang == "HI") "मृदा" else "Soil",
                    title    = if (lang == "HI") "$zoneLabel: नमी कम" else "$zoneLabel: Low Moisture",
                    detail   = if (lang == "HI") "मृदा नमी अनुशंसित स्तर से कम है। तुरंत सिंचाई आवश्यक है।"
                    else "Soil moisture is below the recommended threshold. Immediate irrigation required."
                )
                param.name == "Moisture" && status == "HIGH" -> alerts += FieldAlert(
                    priority = AlertPriority.MEDIUM,
                    category = if (lang == "HI") "मृदा" else "Soil",
                    title    = if (lang == "HI") "$zoneLabel: अत्यधिक नमी" else "$zoneLabel: Excess Moisture",
                    detail   = if (lang == "HI") "जल निकासी की जाँच करें। अत्यधिक नमी जड़ सड़न का कारण बन सकती है।"
                    else "Check drainage channels. Excess moisture may cause root rot and fungal infections."
                )
                param.name == "Nitrogen" && status == "LOW" -> alerts += FieldAlert(
                    priority = AlertPriority.MEDIUM,
                    category = if (lang == "HI") "पोषक तत्व" else "Nutrient",
                    title    = if (lang == "HI") "$zoneLabel: नाइट्रोजन की कमी" else "$zoneLabel: Nitrogen Deficiency",
                    detail   = if (lang == "HI") "यूरिया या DAP खाद का प्रयोग करें। पत्तियाँ पीली हो सकती हैं।"
                    else "Apply urea fertiliser at recommended dosage. Yellowing of leaves may be observed."
                )
                param.name == "Phosphorus" && status == "LOW" -> alerts += FieldAlert(
                    priority = AlertPriority.MEDIUM,
                    category = if (lang == "HI") "पोषक तत्व" else "Nutrient",
                    title    = if (lang == "HI") "$zoneLabel: फॉस्फोरस की कमी" else "$zoneLabel: Phosphorus Deficiency",
                    detail   = if (lang == "HI") "DAP खाद डालें। जड़ विकास और फूल आने पर प्रतिकूल प्रभाव पड़ सकता है।"
                    else "Apply DAP fertiliser. Root development and flowering may be adversely affected."
                )
                param.name == "Potassium" && status == "LOW" -> alerts += FieldAlert(
                    priority = AlertPriority.LOW,
                    category = if (lang == "HI") "पोषक तत्व" else "Nutrient",
                    title    = if (lang == "HI") "$zoneLabel: पोटेशियम कम" else "$zoneLabel: Low Potassium",
                    detail   = if (lang == "HI") "MOP खाद का प्रयोग करें। फसल रोग प्रतिरोधक क्षमता कम हो सकती है।"
                    else "Apply MOP fertiliser. Crop disease resistance and fruit quality may be reduced."
                )
                param.name == "pH" && status == "LOW" -> alerts += FieldAlert(
                    priority = AlertPriority.HIGH,
                    category = if (lang == "HI") "मृदा" else "Soil",
                    title    = if (lang == "HI") "$zoneLabel: pH बहुत अम्लीय" else "$zoneLabel: Soil pH Too Acidic",
                    detail   = if (lang == "HI") "चूना (Agricultural lime) मिलाएं। अम्लीय मिट्टी पोषक तत्व अवशोषण को रोकती है।"
                    else "Add agricultural lime to correct acidity. Acidic soil inhibits nutrient absorption."
                )
                param.name == "pH" && status == "CRITICAL" -> alerts += FieldAlert(
                    priority = AlertPriority.HIGH,
                    category = if (lang == "HI") "मृदा" else "Soil",
                    title    = if (lang == "HI") "$zoneLabel: pH गंभीर स्तर" else "$zoneLabel: Critical pH Level",
                    detail   = if (lang == "HI") "मिट्टी परीक्षण करवाएं। तत्काल मृदा सुधार उपाय आवश्यक हैं।"
                    else "Conduct immediate soil testing. Urgent soil amendment measures are required."
                )
                param.name == "EC" && status == "HIGH" -> alerts += FieldAlert(
                    priority = AlertPriority.HIGH,
                    category = if (lang == "HI") "मृदा" else "Soil",
                    title    = if (lang == "HI") "$zoneLabel: उच्च लवणता (EC)" else "$zoneLabel: High Salinity (EC)",
                    detail   = if (lang == "HI") "अत्यधिक लवणता फसल को नुकसान पहुँचाती है। ताजे पानी से लीचिंग करें।"
                    else "Excessive salinity damages crop roots. Leach with fresh water and improve drainage."
                )
            }
        }
    }

    weather?.let { w ->
        if (w.tempC > 38) alerts += FieldAlert(
            priority = AlertPriority.HIGH,
            category = if (lang == "HI") "मौसम" else "Weather",
            title    = if (lang == "HI") "अत्यधिक तापमान ${w.tempC}°C" else "Extreme Temperature ${w.tempC}°C",
            detail   = if (lang == "HI") "शाम 5 बजे के बाद सिंचाई करें। दोपहर में छिड़काव से बचें।"
            else "Schedule irrigation after 5 PM. Avoid foliar spray during peak heat hours."
        )
        if (w.humidity > 85) alerts += FieldAlert(
            priority = AlertPriority.MEDIUM,
            category = if (lang == "HI") "मौसम" else "Weather",
            title    = if (lang == "HI") "उच्च आर्द्रता ${w.humidity}%" else "High Humidity ${w.humidity}%",
            detail   = if (lang == "HI") "फफूंद और जीवाणु रोगों की संभावना अधिक है। निवारक कवकनाशी का प्रयोग करें।"
            else "High risk of fungal and bacterial diseases. Consider preventive fungicide application."
        )
        if (w.windKph > 35) alerts += FieldAlert(
            priority = AlertPriority.LOW,
            category = if (lang == "HI") "मौसम" else "Weather",
            title    = if (lang == "HI") "तेज़ हवाएं ${w.windKph} km/h" else "Strong Winds ${w.windKph} km/h",
            detail   = if (lang == "HI") "फसल सहारा संरचनाओं की जाँच करें। कीटनाशक छिड़काव स्थगित करें।"
            else "Check crop support structures. Postpone pesticide or foliar spray applications."
        )
        w.daily.firstOrNull()?.let { today ->
            if (today.rainChance > 70) alerts += FieldAlert(
                priority = AlertPriority.MEDIUM,
                category = if (lang == "HI") "मौसम" else "Weather",
                title    = if (lang == "HI") "वर्षा संभावना ${today.rainChance}%" else "Rain Probability ${today.rainChance}%",
                detail   = if (lang == "HI") "खाद और रसायन प्रयोग आज टालें। स्वचालित सिंचाई अस्थायी रूप से बंद रखें।"
                else "Delay fertiliser and chemical applications today. Pause automated irrigation schedule."
            )
        }
    }

    // Sort HIGH first, cap at 3
    return alerts.sortedBy { it.priority.ordinal }.take(3)
}

// ═══════════════════════════════════════════════════════════════════
//  IRRIGATION STATE CALCULATOR — dynamic advisory based on live data
// ═══════════════════════════════════════════════════════════════════
private fun computeIrrigationState(
    nodes: List<SoilNodeUi>,
    cropType: String,
    lastRecord: IrrigationRecord?,
    weather: WeatherInfo?,
    lang: String,
    isPumpOn: Boolean = false
): IrrigationState {
    // ✅ Get ONLY Zone 1 moisture
    val zone1Moisture = nodes.firstOrNull { it.id.contains("1") }
        ?.params
        ?.find { it.name == "Moisture" }
        ?.numericValue

    val avgMoisture = zone1Moisture ?: 65f
    val (optMin, optMax) = when (cropType.lowercase(Locale.getDefault())) {
        "wheat", "गेहूँ"         -> Pair(55f, 70f)
        "rice", "paddy", "चावल"  -> Pair(70f, 85f)
        "sugarcane", "गन्ना"     -> Pair(65f, 80f)
        "cotton", "कपास"         -> Pair(50f, 65f)
        "maize", "corn", "मक्का" -> Pair(55f, 72f)
        else                      -> Pair(55f, 75f)
    }

    val tempC      = weather?.tempC ?: 28
    val rainChance = weather?.daily?.firstOrNull()?.rainChance ?: 0
    val nowMs      = System.currentTimeMillis()

    // If pump is currently on, advisory reflects active irrigation
    if (isPumpOn) {
        return IrrigationState(
            lastRecord     = lastRecord,
            schedule       = IrrigationSchedule(
                nextDateTime    = nowMs + 24 * 3600_000L,
                durationMinutes = 25,
                confidence      = 92,
                basisNote       = if (lang == "HI") "पंप सक्रिय — सिंचाई जारी है"
                else "Pump active — irrigation in progress"
            ),
            advisoryLine   = if (lang == "HI") "सिंचाई अभी चल रही है। समाप्त होने पर अगला शेड्यूल अपडेट होगा।"
            else "Irrigation is currently active. Next schedule will update when complete.",
            moistureAvg    = avgMoisture,
            cropType       = cropType,
            lastAnalysedAt = nowMs
        )
    }

    val nextHours: Float
    val confidence: Int
    val basisNote: String
    val advisoryLine: String
    val schedDurationMin: Int

    when {
        rainChance > 70 -> {
            nextHours        = 36f
            confidence       = 80
            schedDurationMin = 20
            basisNote = if (lang == "HI") "वर्षा पूर्वानुमान ${rainChance}% — सिंचाई स्थगित"
            else "Rain forecast ${rainChance}% — irrigation deferred"
            advisoryLine = if (lang == "HI") "वर्षा की संभावना अधिक है, सिंचाई स्थगित की गई।"
            else "High rain probability. Irrigation deferred to avoid over-saturation."
        }
        avgMoisture < 30f -> {
            nextHours        = 0f
            confidence       = 97
            schedDurationMin = 40
            basisNote = if (lang == "HI") "नमी ${avgMoisture.roundToInt()}% — गंभीर रूप से कम"
            else "Moisture ${avgMoisture.roundToInt()}% — critically low"
            advisoryLine = if (lang == "HI") "नमी गंभीर रूप से कम है। तुरंत सिंचाई आवश्यक है।"
            else "Moisture critically low. Immediate irrigation required."
        }
        avgMoisture < optMin -> {
            val hrs = if (tempC > 35) 1.5f else 3f
            nextHours        = hrs
            confidence       = 90
            schedDurationMin = 30
            basisNote = if (lang == "HI") "नमी ${avgMoisture.roundToInt()}%, तापमान ${tempC}°C"
            else "Moisture ${avgMoisture.roundToInt()}%, temp ${tempC}°C"
            advisoryLine = if (lang == "HI") "नमी कम है। ${hrs.roundToInt()} घंटे में सिंचाई की सलाह दी जाती है।"
            else "Moisture below optimal. Irrigation advised within ${hrs.roundToInt()} hour(s)."
        }
        avgMoisture <= optMax -> {
            nextHours        = 24f
            confidence       = 85
            schedDurationMin = 25
            basisNote = if (lang == "HI") "नमी ${avgMoisture.roundToInt()}% — इष्टतम स्तर"
            else "Moisture ${avgMoisture.roundToInt()}% — within optimal range"
            advisoryLine = if (lang == "HI") "नमी उचित स्तर पर है। अगली सिंचाई कल निर्धारित है।"
            else "Moisture is optimal. Next irrigation scheduled for tomorrow."
        }
        else -> {
            nextHours        = 48f
            confidence       = 75
            schedDurationMin = 20
            basisNote = if (lang == "HI") "नमी ${avgMoisture.roundToInt()}% — अत्यधिक"
            else "Moisture ${avgMoisture.roundToInt()}% — above optimal"
            advisoryLine = if (lang == "HI") "नमी अधिक है। अगली 48 घंटों में सिंचाई न करें।"
            else "Moisture is high. Avoid irrigation for the next 48 hours."
        }
    }

    return IrrigationState(
        lastRecord     = lastRecord,
        schedule       = IrrigationSchedule(
            nextDateTime    = nowMs + (nextHours * 3600_000L).toLong(),
            durationMinutes = schedDurationMin,
            confidence      = confidence,
            basisNote       = basisNote
        ),
        advisoryLine   = advisoryLine,
        moistureAvg    = avgMoisture,
        cropType       = cropType,
        lastAnalysedAt = nowMs
    )
}

// ═══════════════════════════════════════════════════════════════════
//  SMART IRRIGATION PANEL
//  — click anywhere on card to refresh
//  — no re-analyse button, no manual control section header
//  — pump toggle only (no button bar)
//  — schedule advisory live from sensor data
// ═══════════════════════════════════════════════════════════════════
@Composable
fun SmartIrrigationPanel(
    irrigationState: IrrigationState,
    isPumpOn: Boolean,
    isPumpUpdating: Boolean,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    language: String,
    onPumpToggleRequest: (Boolean) -> Unit,
    onCardClick: () -> Unit,
    isRefreshing: Boolean
) {
    val isDark        = ThemeState.isDarkTheme.value
    val moisture      = irrigationState.moistureAvg
    val moistureColor = when {
        moisture < 30f -> Color(0xFFD32F2F)
        moisture < 55f -> Color(0xFFF57C00)
        moisture < 80f -> Color(0xFF2E7D32)
        else           -> Color(0xFF0277BD)
    }
    val context = LocalContext.current
    val activity = context as Activity

    val voiceManager = remember { VoiceManager(context) }

    val pumpColor = when {
        isPumpUpdating -> Color(0xFFF57C00)
        isPumpOn       -> Color(0xFF2E7D32)
        else           -> Color(0xFF546E7A)
    }
    val surfaceBg    = if (isDark) Color(0xFF0D2318) else Color(0xFFF6FBF7)
    val dividerColor = if (isDark) Color(0xFF1B3A24) else Color(0xFFE0ECE2)

    val sdfDate = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val nextMs  = irrigationState.schedule.nextDateTime
    val nowMs   = System.currentTimeMillis()
    val diffMs  = nextMs - nowMs

    val nextLabel: String
    val nextSub: String
    when {
        diffMs <= 0 -> {
            nextLabel = if (language == "HI") "अभी आवश्यक" else "Now — Urgent"
            nextSub   = if (language == "HI") "तुरंत सिंचाई करें" else "Start irrigation immediately"
        }
        diffMs < 3600_000L -> {
            val mins = (diffMs / 60000).toInt()
            nextLabel = if (language == "HI") "$mins मिनट में" else "In $mins min"
            nextSub   = sdfTime.format(Date(nextMs))
        }
        diffMs < 86400_000L -> {
            val hrs = (diffMs / 3600000).toInt()
            nextLabel = if (language == "HI") "$hrs घंटे में" else "In ${hrs}h"
            nextSub   = sdfTime.format(Date(nextMs))
        }
        else -> {
            nextLabel = sdfDate.format(Date(nextMs))
            nextSub   = if (language == "HI") "${irrigationState.schedule.durationMinutes} मिनट की सिंचाई"
            else "${irrigationState.schedule.durationMinutes} min session"
        }
    }

    val lastLabel: String
    val lastSub: String
    if (irrigationState.lastRecord != null) {
        val rec     = irrigationState.lastRecord
        val ago     = nowMs - rec.timestamp
        val agoHrs  = ago / 3600000
        val agoDays = agoHrs / 24
        lastLabel = when {
            agoHrs < 1    -> if (language == "HI") "अभी-अभी" else "Just now"
            agoHrs < 24   -> if (language == "HI") "${agoHrs}h पहले" else "${agoHrs}h ago"
            agoDays == 1L -> if (language == "HI") "कल" else "Yesterday"
            else          -> if (language == "HI") "${agoDays} दिन पहले" else "${agoDays}d ago"
        }
        lastSub = if (language == "HI") "${rec.durationMinutes} मिनट • ${rec.triggerReason}"
        else "${rec.durationMinutes} min • ${rec.triggerReason}"
    } else {
        lastLabel = if (language == "HI") "कोई रिकॉर्ड नहीं" else "No record"
        lastSub   = if (language == "HI") "डेटा उपलब्ध नहीं" else "Data not available"
    }

    val updatedStr = remember(irrigationState.lastAnalysedAt) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(irrigationState.lastAnalysedAt))
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isRefreshing) { onCardClick() },
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {

            // ── Header ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(PrimaryGreen.copy(alpha = 0.12f), Color.Transparent)),
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            Strings.get(Strings.irrigationPanel, language),
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = textPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${Strings.get(Strings.lastUpdated, language)}: $updatedStr",
                                fontSize = 10.sp,
                                color    = textSecondary
                            )
                            if (isRefreshing) {
                                Spacer(Modifier.width(6.dp))
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(10.dp),
                                    strokeWidth = 1.5.dp,
                                    color       = PrimaryGreen
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

                // ─ Tap hint ──────────────────────────────────────────
                Text(
                    Strings.get(Strings.tapToOperate, language),
                    fontSize = 10.sp,
                    color    = textSecondary.copy(alpha = 0.65f)
                )

                Spacer(Modifier.height(12.dp))

                // ─ Moisture bar ──────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        Strings.get(Strings.moistureStatus, language),
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = textSecondary
                    )
                    Text(
                        "${"%.0f".format(moisture)}%",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = moistureColor
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(moistureColor.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((moisture / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(moistureColor.copy(alpha = 0.5f), moistureColor)))
                    )
                }
                Spacer(Modifier.height(3.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0%",   fontSize = 9.sp, color = textSecondary.copy(alpha = 0.6f))
                    Text(
                        if (language == "HI") "इष्टतम 55–75%" else "Optimal 55–75%",
                        fontSize = 9.sp, color = PrimaryGreen.copy(alpha = 0.8f)
                    )
                    Text("100%", fontSize = 9.sp, color = textSecondary.copy(alpha = 0.6f))
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(Modifier.height(14.dp))

                // ─ Last / Next row ────────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        color    = surfaceBg,
                        border   = BorderStroke(1.dp, dividerColor)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.History, contentDescription = null, tint = textSecondary, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    Strings.get(Strings.lastIrrigated, language).uppercase(),
                                    fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary, letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(lastLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(lastSub, fontSize = 10.sp, color = textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        color    = moistureColor.copy(alpha = 0.06f),
                        border   = BorderStroke(1.dp, moistureColor.copy(alpha = 0.22f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = moistureColor, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    Strings.get(Strings.nextIrrigation, language).uppercase(),
                                    fontSize = 9.sp, fontWeight = FontWeight.Bold, color = moistureColor, letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(nextLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = moistureColor)
                            Text(nextSub, fontSize = 10.sp, color = textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // ─ Schedule advisory — live from sensor data ──────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    color    = surfaceBg,
                    border   = BorderStroke(1.dp, dividerColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                Strings.get(Strings.scheduleAdvisory, language).uppercase(),
                                fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary, letterSpacing = 0.5.sp
                            )
                            Text(
                                "${irrigationState.schedule.confidence}% ${if (language == "HI") "विश्वसनीय" else "confidence"}",
                                fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = PrimaryGreen
                            )
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(irrigationState.advisoryLine, fontSize = 12.sp, color = textPrimary, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(3.dp))
                        Text(irrigationState.schedule.basisNote, fontSize = 10.sp, color = textSecondary)
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(Modifier.height(12.dp))

                // Refreshing overlay strip (no button, inline only)
                if (isRefreshing) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(10.dp),
                        color    = PrimaryGreen.copy(alpha = 0.06f),
                        border   = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.22f))
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = PrimaryGreen)
                            Text(
                                Strings.get(Strings.analysing, language),
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryGreen
                            )
                        }
                    }
                }
            }
        }
    }
}
// ═══════════════════════════════════════════════════════════════════
//  MINIMAL FIELD ALERTS PANEL — Single-line, farmer friendly
// ═══════════════════════════════════════════════════════════════════

@Composable
fun FieldAlertsPanel(
    alerts: List<FieldAlert>,
    textPrimary: Color,
    textSecondary: Color,
    language: String
) {
    val visibleAlerts = alerts.take(3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        Strings.get(Strings.alertsTitle, language),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Text(
                        Strings.get(Strings.alertSubtitle, language),
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }

                if (visibleAlerts.isNotEmpty()) {
                    Text(
                        "${visibleAlerts.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32) // green count
                    )
                }
            }

            HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFEAEAEA))

            if (visibleAlerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        Strings.get(Strings.noAlerts, language),
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                }
            } else {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    visibleAlerts.forEach { alert ->
                        MinimalAlertRow(
                            alert = alert,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalAlertRow(
    alert: FieldAlert,
    textPrimary: Color,
    textSecondary: Color
) {
    val (startColor, endColor) = when (alert.priority) {
        AlertPriority.HIGH -> Pair(Color(0xFFD32F2F), Color(0xFFFFCDD2))
        AlertPriority.MEDIUM -> Pair(Color(0xFFF57C00), Color(0xFFFFE0B2))
        AlertPriority.LOW -> Pair(Color(0xFF2E7D32), Color(0xFFC8E6C9))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Gradient indicator bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(startColor, endColor)
                    )
                )
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Single line message (MOST IMPORTANT)
        Text(
            text = "${alert.title} - ${alert.detail}",
            fontSize = 12.sp,
            color = textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
// ═══════════════════════════════════════════════════════════════════
//  DASHBOARD MAIN SCREEN
// ═══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    drawerState: DrawerState,
    farm: Farm
) {
    val scope          = rememberCoroutineScope()
    val farmViewModel: FarmViewModel = hiltViewModel()
    val farms          by farmViewModel.farms.collectAsState()
    var selectedFarm   by remember { mutableStateOf(farm) }
    val isDarkTheme    = ThemeState.isDarkTheme.value
    var showChatPopup  by remember { mutableStateOf(false) }
    var language       by remember { mutableStateOf("EN") }
    var showFarmDialog by remember { mutableStateOf(false) }

    val firebaseAuth     = remember { FirebaseAuth.getInstance() }
    val firebaseDatabase = remember { FirebaseDatabase.getInstance() }

    LaunchedEffect(farms, farm) {
        selectedFarm = when {
            farm.id.isNotBlank() -> farm
            farms.isNotEmpty()   -> farms.first()
            else                 -> Farm()
        }
    }

    var isPumpUpdating   by remember { mutableStateOf(false) }
    var isIrrRefreshing  by remember { mutableStateOf(false) }

    val backgroundColor = if (isDarkTheme) DarkBackground else LightBackground
    val cardColor       = if (isDarkTheme) CardDark else CardLight
    val textPrimary     = if (isDarkTheme) Color(0xFFE8F5E9) else Color(0xFF0A0F0B)
    val textSecondary   = if (isDarkTheme) Color(0xFFB5CBB8) else Color(0xFF607D8B)

    val userName = remember { firebaseAuth.currentUser?.displayName ?: "Farmer" }

    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val chatViewModel: KrishiMitriChatViewModel = viewModel()

    LaunchedEffect(Unit) { dashboardViewModel.startListening() }

    val sensorNodes by dashboardViewModel.nodes.collectAsState()
    val tds         by dashboardViewModel.tds.collectAsState()
    val tank        by dashboardViewModel.tank.collectAsState()
    val rain        by dashboardViewModel.rain.collectAsState()
    val pumpValue   by dashboardViewModel.pump.collectAsState()
    var isPumpOn    by remember { mutableStateOf(false) }
    val context  = LocalContext.current
    val activity = context as Activity

    val voiceManager = remember { VoiceManager(context) }
    LaunchedEffect(pumpValue) {
        isPumpOn = pumpValue == 1
    }

// ✅ ADD VOICE PROCESSOR HERE (AFTER isPumpOn exists)

    LaunchedEffect(Unit) {
        VoiceHandler.processor = VoiceCommandProcessor(
            navController = navController,

            onPumpToggle = { isOn ->
                isPumpOn = isOn

                val pumpValue = if (isOn) 1 else 0

                firebaseDatabase.reference
                    .child("dashboard")
                    .child("farmer_SK001")
                    .child("farm_alpha01")
                    .child("commands")
                    .child("pump")
                    .setValue(pumpValue)

                firebaseDatabase.reference
                    .child("dashboard")
                    .child("farmer_SK001")
                    .child("farm_alpha01")
                    .child("live")
                    .child("irrigation")
                    .child("pump")
                    .setValue(pumpValue)
            },

            speak = { text ->
                voiceManager.speak(text)
            },

            getSensorData = {
                VoiceCommandProcessor.SensorSnapshot(
                    moisture = sensorNodes.firstOrNull()?.moisture?.toString() ?: "0",
                    temperature = sensorNodes.firstOrNull()?.temperature?.toString() ?: "0",
                    humidity = sensorNodes.firstOrNull()?.humidity?.toString() ?: "0",
                    ph = sensorNodes.firstOrNull()?.ph?.toString() ?: "0",
                    ec = sensorNodes.firstOrNull()?.ec?.toString() ?: "0",
                    n = sensorNodes.firstOrNull()?.n?.toString() ?: "0",
                    p = sensorNodes.firstOrNull()?.p?.toString() ?: "0",
                    k = sensorNodes.firstOrNull()?.k?.toString() ?: "0",
                    tankLevel = tank.toString(),
                    rainStatus = if (rain == 1) "Rain detected" else "No rain",
                    pumpStatus = if (isPumpOn) "चालू" else "बंद",

                    )
            }
        )
    }
    LaunchedEffect(pumpValue) { isPumpOn = pumpValue == 1 }

    // Weather: show last cached, update silently in background
    var weatherInfo       by remember { mutableStateOf<WeatherInfo?>(null) }
    var weatherLoading    by remember { mutableStateOf(false) }
    var weatherError      by remember { mutableStateOf<String?>(null) }
    var showWeatherDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFarm.id) {
        val locName = selectedFarm.location
        val lat     = selectedFarm.lat
        val lon     = selectedFarm.lon
        if ((lat == 0.0 && lon == 0.0) && locName.isBlank()) {
            weatherError = "No location set for this farm"
            return@LaunchedEffect
        }
        // Only show loader on first fetch; silent background refresh after
        if (weatherInfo == null) weatherLoading = true
        val fresh = fetchWeatherForLocation(locName, lat, lon)
        weatherLoading = false
        if (fresh != null) { weatherInfo = fresh; weatherError = null }
        else if (weatherInfo == null) weatherError = "Unable to fetch weather"
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(10L * 60L * 1000L)
            val f = selectedFarm
            if (f.id.isBlank()) continue
            val fresh = fetchWeatherForLocation(f.location, f.lat, f.lon)
            if (fresh != null) { weatherInfo = fresh; weatherError = null }
        }
    }


    val mlHelper = remember(context) { CropRecommendationHelper(context) }
    DisposableEffect(Unit) { onDispose { mlHelper.close() } }

    val uiNodes = remember(sensorNodes) { sensorNodes.map { it.toSoilNodeUi() } }

    // Last irrigation record — replace with Firebase listener in production
    var lastIrrigationRecord by remember {
        mutableStateOf<IrrigationRecord?>(
            IrrigationRecord(
                timestamp       = System.currentTimeMillis() - 18 * 3600_000L,
                durationMinutes = 25,
                waterLiters     = 320,
                triggerReason   = "Auto — Low Moisture"
            )
        )
    }

    // Irrigation state — recomputes any time sensors, pump, weather, or language changes
    var irrigationState by remember {
        mutableStateOf(computeIrrigationState(emptyList(), "", null, null, "EN", false))
    }
    LaunchedEffect(uiNodes, selectedFarm.cropType, lastIrrigationRecord, weatherInfo, language, isPumpOn) {
        irrigationState = computeIrrigationState(
            uiNodes, selectedFarm.cropType, lastIrrigationRecord, weatherInfo, language, isPumpOn
        )
    }

    val fieldAlerts = remember(uiNodes, weatherInfo, language) {
        generateFieldAlerts(uiNodes, weatherInfo, language)
    }

    if (showFarmDialog) {
        FarmSelectionDialog(
            farms          = farms,
            selectedFarm   = selectedFarm,
            onDismiss      = { showFarmDialog = false },
            onFarmSelected = { f -> selectedFarm = f; showFarmDialog = false },
            navController  = navController,
            cardColor      = cardColor,
            textPrimary    = textPrimary,
            textSecondary  = textSecondary,
            language       = language
        )
    }

    if (showWeatherDialog && weatherInfo != null) {
        WeatherDetailDialog(
            weather       = weatherInfo!!,
            location      = selectedFarm.location,
            onDismiss     = { showWeatherDialog = false },
            cardColor     = cardColor,
            textPrimary   = textPrimary,
            textSecondary = textSecondary,
            language      = language
        )
    }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = { DrawerContent(navController, language) }
    ) {
        Scaffold(
            containerColor = backgroundColor,
            floatingActionButton = {
                if (!showChatPopup) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // 🎤 MIC BUTTON
                        FloatingActionButton(
                            onClick = {
                                voiceManager.startListening(activity)
                            },
                            containerColor = PrimaryGreen
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice")
                        }

                        // 🤖 CHAT BUTTON (existing)
                        AnimatedChatbotFab {
                            showChatPopup = true
                        }
                    }
                }
            },
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor    = Color.Transparent,
                        titleContentColor = textPrimary
                    ),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SMART KRISHI", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(
                                if (language == "HI") "हर पत्ती में बुद्धि लाना" else "Bringing intelligence to every leaf",
                                fontSize = 11.sp, color = textSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = textPrimary)
                        }
                    },
                    actions = {
                        Surface(
                            shape    = RoundedCornerShape(50),
                            color    = Color.Transparent,
                            border   = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.7f)),
                            modifier = Modifier.height(30.dp).clickable { language = if (language == "EN") "HI" else "EN" }
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                                Text(language, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textPrimary)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { ThemeState.isDarkTheme.value = !ThemeState.isDarkTheme.value }) {
                            Icon(
                                imageVector        = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle theme",
                                tint               = PrimaryGreen
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(backgroundColor)
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GreetingSection(
                    userName = userName, farmName = selectedFarm.name,
                    textPrimary = textPrimary, textSecondary = textSecondary, language = language
                )
                ProfessionalFarmSelector(
                    selectedFarm = selectedFarm, onClick = { showFarmDialog = true },
                    cardColor = cardColor, textPrimary = textPrimary, textSecondary = textSecondary, language = language
                )
                CompactWeatherCard(
                    cardColor = cardColor, textPrimary = textPrimary, textSecondary = textSecondary,
                    farmLocation = selectedFarm.location, weather = weatherInfo,
                    isLoading = weatherLoading, error = weatherError,
                    onClick = { showWeatherDialog = true }, language = language
                )
                NodeSensorsSection(
                    nodes = sensorNodes, cardColor = cardColor, textPrimary = textPrimary,
                    textSecondary = textSecondary, onSyncAll = { dashboardViewModel.reloadAll() },
                    onSyncNode = { dashboardViewModel.reloadAll() },
                    selectedCrop = selectedFarm.cropType, language = language
                )

                // ── Smart Irrigation Panel — click anywhere to refresh
                SmartIrrigationPanel(
                    irrigationState     = irrigationState,
                    isPumpOn            = isPumpOn,
                    isPumpUpdating      = isPumpUpdating,
                    cardColor           = cardColor,
                    textPrimary         = textPrimary,
                    textSecondary       = textSecondary,
                    language            = language,
                    isRefreshing        = isIrrRefreshing,
                    onCardClick         = {
                        if (!isIrrRefreshing) {
                            isIrrRefreshing = true
                            scope.launch {
                                delay(2000L)
                                irrigationState = computeIrrigationState(
                                    uiNodes, selectedFarm.cropType, lastIrrigationRecord, weatherInfo, language, isPumpOn
                                )
                                isIrrRefreshing = false
                            }
                        }
                    },
                    onPumpToggleRequest = { desired ->
                        if (isPumpUpdating) return@SmartIrrigationPanel
                        isPumpUpdating = true
                        scope.launch {
                            try {
                                val uid    = firebaseAuth.currentUser?.uid
                                val farmId = selectedFarm.id
                                if (uid != null && farmId.isNotBlank()) {
                                    firebaseDatabase.reference
                                        .child("dashboard").child("farmer_SK001")
                                        .child("farm_alpha01").child("live")
                                        .child("irrigation").child("pump")
                                        .setValue(if (desired) 1 else 0)
                                }
                                if (!desired && isPumpOn) {
                                    lastIrrigationRecord = IrrigationRecord(
                                        timestamp       = System.currentTimeMillis(),
                                        durationMinutes = 0,
                                        waterLiters     = 0,
                                        triggerReason   = "Manual"
                                    )
                                }
                            } finally {
                                delay(800)
                                isPumpOn       = desired
                                isPumpUpdating = false
                                irrigationState = computeIrrigationState(
                                    uiNodes, selectedFarm.cropType, lastIrrigationRecord, weatherInfo, language, desired
                                )
                            }
                        }
                    }
                )

                // ── Field Alerts
                FieldAlertsPanel(
                    alerts = fieldAlerts,
                    textPrimary = textPrimary, textSecondary = textSecondary, language = language
                )

                // ── System status grid
                SystemStatusGrid(
                    cardColor = cardColor, textPrimary = textPrimary, textSecondary = textSecondary,
                    isPumpOn = isPumpOn, isPumpUpdating = isPumpUpdating,
                    tds = tds, tank = tank, rain = rain, language = language,
                    onPumpToggleRequest = { desired ->
                        if (isPumpUpdating) return@SystemStatusGrid
                        isPumpUpdating = true
                        scope.launch {
                            try {
                                val uid    = firebaseAuth.currentUser?.uid
                                val farmId = selectedFarm.id
                                if (uid != null && farmId.isNotBlank()) {
                                    firebaseDatabase.reference
                                        .child("dashboard").child("farmer_SK001")
                                        .child("farm_alpha01").child("live")
                                        .child("irrigation").child("pump")
                                        .setValue(if (desired) 1 else 0)
                                }
                            } finally {
                                delay(800)
                                isPumpOn       = desired
                                isPumpUpdating = false
                            }
                        }
                    }
                )

                LogHistoryBarGraph(
                    cardColor = cardColor, textPrimary = textPrimary,
                    textSecondary = textSecondary, navController = navController, language = language
                )
                QuickModuleButtons(
                    navController = navController, textPrimary = textPrimary,
                    textSecondary = textSecondary, language = language
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showChatPopup) {
        ChatPopup(
            navController = navController, chatViewModel = chatViewModel,
            onClosePopup = { showChatPopup = false }, language = language, isDark = isDarkTheme
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
//  DRAWER CONTENT
// ═══════════════════════════════════════════════════════════════════
@Composable
fun DrawerContent(navController: NavController, language: String = "EN") {
    val isDark    by ThemeState.isDarkTheme
    val textColor = if (isDark) Color(0xFFE8F5E9) else Color(0xFF0A140D)
    val bgColor   = if (isDark) Color(0xFF10261A) else Color(0xFFE8F5E9)

    Column(modifier = Modifier.fillMaxHeight().width(270.dp).background(bgColor).padding(16.dp)) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text(Strings.get(Strings.navigation, language), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
            Spacer(Modifier.height(18.dp))
            DrawerItem(Strings.get(Strings.dashboard, language),    Icons.Default.Home)          { navController.navigate(Screen.Dashboard.route) }
            DrawerItem(Strings.get(Strings.diseaseDetect, language), Icons.Default.BugReport)    { navController.navigate(Screen.DiseaseDetection.route) }
            DrawerItem(Strings.get(Strings.cropRecommend, language), Icons.Default.Grass)        { navController.navigate(Screen.CropRecommendation.route) }
            DrawerItem(Strings.get(Strings.fertRecommend, language), Icons.Default.Eco)          { navController.navigate(Screen.FertilizerRecommendation.route) }
            DrawerItem(Strings.get(Strings.roverControl, language),  Icons.Default.Videocam)     { navController.navigate(Screen.Rover.route) }
            DrawerItem(Strings.get(Strings.logsMenu, language),      Icons.Default.History)      { navController.navigate(Screen.Logs.route) }
            DrawerItem(Strings.get(Strings.krishiChat, language),    Icons.Default.SupportAgent) { navController.navigate(Screen.KrishiMitri.route) }
            Spacer(Modifier.height(12.dp))
        }
        Column {
            Divider(color = Color.Gray.copy(alpha = 0.3f))
            Spacer(Modifier.height(6.dp))
            DrawerItem(Strings.get(Strings.profile, language), Icons.Default.Person) { navController.navigate(Screen.Profile.route) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    val isDark    by ThemeState.isDarkTheme
    val textColor = if (isDark) Color(0xFFE8F5E9) else Color(0xFF0A140D)
    val itemBg    = if (isDark) Color(0xFF173822) else Color(0xFFFFFFFF)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp)).background(itemBg).clickable(onClick = onClick).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.padding(4.dp).size(32.dp).clip(CircleShape).background(PrimaryGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = PrimaryGreen) }
        Spacer(Modifier.width(12.dp))
        Text(label, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = textColor)
    }
}

// ═══════════════════════════════════════════════════════════════════
//  GREETING SECTION
// ═══════════════════════════════════════════════════════════════════
@Composable
fun GreetingSection(userName: String, farmName: String, textPrimary: Color, textSecondary: Color, language: String) {
    val hour     = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11  -> Strings.get(Strings.greetingMorning, language)
        in 12..16 -> Strings.get(Strings.greetingAfternoon, language)
        else      -> Strings.get(Strings.greetingEvening, language)
    }
    val dateString = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date())
    Column {
        Text("${Strings.get(Strings.namaste, language)}, $userName 🙏", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textPrimary)
        Spacer(Modifier.height(2.dp))
        Text("$greeting • $dateString", color = textSecondary, fontSize = 13.sp)
        Text(
            if (farmName.isBlank()) Strings.get(Strings.noFarm, language)
            else "${Strings.get(Strings.managing, language)} $farmName",
            color = textSecondary, fontSize = 13.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
//  COMPACT WEATHER CARD
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun CompactWeatherCard(
    cardColor: Color, textPrimary: Color, textSecondary: Color,
    farmLocation: String, weather: WeatherInfo?, isLoading: Boolean,
    error: String?, onClick: () -> Unit, language: String
) {
    val hour     = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val timeIcon = when (hour) { in 6..17 -> "☀️"; in 18..19 -> "🌆"; else -> "🌙" }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        when {
            isLoading && weather == null -> Box(
                modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = PrimaryGreen, strokeWidth = 3.dp, modifier = Modifier.size(32.dp)) }

            error != null && weather == null -> Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(error, fontSize = 13.sp, color = Color(0xFFD32F2F))
            }

            weather != null -> Box(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(
                    Brush.verticalGradient(listOf(PrimaryGreen.copy(alpha = 0.12f), Color.Transparent))
                ))
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(farmLocation.ifBlank { "Unknown" }, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text(timeIcon, fontSize = 28.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("${weather.tempC}°", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = textPrimary, lineHeight = 64.sp)
                            Text(weather.condition, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textPrimary)
                        }
                        Text(weatherEmojiFor(weather.condition), fontSize = 72.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Thermostat, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${Strings.get(Strings.feelsLike, language)} ${weather.feelsLike}°", fontSize = 13.sp, color = textSecondary)
                        }
                        weather.daily.firstOrNull()?.let { today ->
                            Text("↑ ${today.maxTempC}° / ↓ ${today.minTempC}°", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textSecondary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        CompactMetric(Icons.Outlined.WaterDrop, "${weather.humidity}%", textSecondary)
                        CompactMetric(Icons.Outlined.Air,       "${weather.windKph} km/h", textSecondary)
                        CompactMetric(Icons.Outlined.Visibility,"${weather.visibility.roundToInt()} km", textSecondary)
                    }
                }
            }

            else -> Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(Strings.get(Strings.weatherNotAvail, language), fontSize = 13.sp, color = textSecondary)
            }
        }
    }
}

@Composable
private fun CompactMetric(icon: ImageVector, value: String, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

private fun weatherEmojiFor(condition: String): String {
    val c = condition.lowercase(Locale.getDefault())
    return when {
        "thunder" in c                -> "⛈️"
        "rain" in c || "drizzle" in c -> "🌧️"
        "snow" in c                   -> "❄️"
        "cloud" in c                  -> "☁️"
        "clear" in c                  -> "☀️"
        else                          -> "🌤️"
    }
}

// ═══════════════════════════════════════════════════════════════════
//  PROFESSIONAL FARM SELECTOR
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun ProfessionalFarmSelector(
    selectedFarm: Farm, onClick: () -> Unit,
    cardColor: Color, textPrimary: Color, textSecondary: Color, language: String
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PrimaryGreen, PrimaryGreen.copy(alpha = 0.7f)))),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Agriculture, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(Strings.get(Strings.currentFarm, language), fontSize = 11.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                    Text(
                        selectedFarm.name.ifBlank { Strings.get(Strings.selectFarm, language) },
                        fontSize = 17.sp, fontWeight = FontWeight.Bold, color = textPrimary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    if (selectedFarm.location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(selectedFarm.location, fontSize = 12.sp, color = textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Change farm", tint = PrimaryGreen, modifier = Modifier.size(28.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  FARM SELECTION DIALOG
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun FarmSelectionDialog(
    farms: List<Farm>, selectedFarm: Farm, onDismiss: () -> Unit,
    onFarmSelected: (Farm) -> Unit, navController: NavController,
    cardColor: Color, textPrimary: Color, textSecondary: Color, language: String = "EN"
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(Strings.get(Strings.selectFarmTitle, language), fontWeight = FontWeight.Bold, fontSize = 22.sp, color = textPrimary)
                        Text("${farms.size} ${if (farms.size != 1) Strings.get(Strings.farmsAvail, language) else Strings.get(Strings.farmAvail1, language)}", fontSize = 13.sp, color = textSecondary)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close", tint = textSecondary) }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = textSecondary.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))
                Button(onClick = { onDismiss(); navController.navigate(Screen.AddFarm.route) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(Strings.get(Strings.addNewFarm, language), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    farms.forEach { f ->
                        val isSel = f.id == selectedFarm.id
                        Card(modifier = Modifier.fillMaxWidth().clickable { onFarmSelected(f) }, shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSel) PrimaryGreen.copy(alpha = 0.15f) else cardColor),
                            border = if (isSel) BorderStroke(2.dp, PrimaryGreen) else null,
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(PrimaryGreen.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Agriculture, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(f.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${f.acres} acres • ${f.cropType.take(20)}", fontSize = 13.sp, color = textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (f.location.isNotBlank()) Text(f.location, fontSize = 12.sp, color = textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Icon(if (isSel) Icons.Default.CheckCircle else Icons.Default.ChevronRight, contentDescription = null, tint = if (isSel) PrimaryGreen else textSecondary, modifier = Modifier.size(if (isSel) 26.dp else 24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  WEATHER DETAIL DIALOG
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun WeatherDetailDialog(
    weather: WeatherInfo, location: String, onDismiss: () -> Unit,
    cardColor: Color, textPrimary: Color, textSecondary: Color, language: String
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth(0.96f).fillMaxHeight(0.92f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Brush.verticalGradient(listOf(PrimaryGreen.copy(alpha = 0.3f), Color.Transparent)))) {
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) { Icon(Icons.Default.Close, contentDescription = "Close", tint = textPrimary) }
                    Column(modifier = Modifier.align(Alignment.Center).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(weatherEmojiFor(weather.condition), fontSize = 72.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("${weather.tempC}°C", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text(weather.condition, fontSize = 20.sp, color = textSecondary)
                        Text(location, fontSize = 14.sp, color = textSecondary.copy(alpha = 0.7f), maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(Strings.get(Strings.currentConditions, language), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary)
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WeatherDetailCard(Icons.Outlined.Thermostat, Strings.get(Strings.feelsLike, language), "${weather.feelsLike}°C", Modifier.weight(1f), cardColor, textPrimary, textSecondary)
                            WeatherDetailCard(Icons.Outlined.WaterDrop,  "Humidity",   "${weather.humidity}%",     Modifier.weight(1f), cardColor, textPrimary, textSecondary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WeatherDetailCard(Icons.Outlined.Air,        "Wind",       "${weather.windKph} km/h",  Modifier.weight(1f), cardColor, textPrimary, textSecondary)
                            WeatherDetailCard(Icons.Outlined.Visibility, "Visibility", "${weather.visibility} km", Modifier.weight(1f), cardColor, textPrimary, textSecondary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WeatherDetailCard(Icons.Outlined.Compress,   "Pressure",   "${weather.pressure} mb",   Modifier.weight(1f), cardColor, textPrimary, textSecondary)
                            WeatherDetailCard(Icons.Outlined.WaterDrop,  "Dew Point",  "${weather.dewPoint}°C",    Modifier.weight(1f), cardColor, textPrimary, textSecondary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WeatherDetailCard(Icons.Outlined.WbSunny,    "UV Index",   weather.uvIndex.toString(), Modifier.weight(1f), cardColor, textPrimary, textSecondary)
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    if (weather.daily.isNotEmpty()) {
                        Text(Strings.get(Strings.sevenDay, language), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary)
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            weather.daily.forEach { day -> DailyForecastRow(day, cardColor, textPrimary, textSecondary) }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailCard(icon: ImageVector, title: String, value: String, modifier: Modifier = Modifier, cardColor: Color, textPrimary: Color, textSecondary: Color) {
    Card(modifier = modifier.height(90.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
            Column {
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(title, fontSize = 11.sp, color = textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun DailyForecastRow(day: DailyForecast, cardColor: Color, textPrimary: Color, textSecondary: Color) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(day.label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Outlined.WaterDrop, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${day.rainChance}%", fontSize = 12.sp, color = textSecondary)
                }
            }
            Text(weatherEmojiFor(day.condition), fontSize = 32.sp)
            Column(horizontalAlignment = Alignment.End) {
                Text("${day.maxTempC}°", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                Text("${day.minTempC}°", fontSize = 14.sp, color = textSecondary)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  HELPER
// ═══════════════════════════════════════════════════════════════════
private fun computeAverageParams(nodes: List<SoilNodeUi>): List<SoilParam> {
    if (nodes.isEmpty()) return emptyList()
    val grouped = nodes.flatMap { it.params }.groupBy { it.name }
    return grouped.map { (name, paramList) ->
        val avgValue = paramList.map { it.numericValue }.average().toFloat()
        val display  = when (name) {
            "Moisture"   -> "${avgValue.roundToInt()}%"
            "pH"         -> String.format(Locale.getDefault(), "%.1f", avgValue)
            "EC"         -> String.format(Locale.getDefault(), "%.1f", avgValue)
            "Nitrogen"   -> "${avgValue.roundToInt()} mg/kg"
            "Phosphorus" -> "${avgValue.roundToInt()} mg/kg"
            "Potassium"  -> "${avgValue.roundToInt()} mg/kg"
            "Soil Temp"  -> "${avgValue.roundToInt()}°C"
            else         -> paramList.firstOrNull()?.displayValue ?: ""
        }
        SoilParam(name = name, displayValue = display, numericValue = avgValue)
    }
}

// ═══════════════════════════════════════════════════════════════════
//  NODE SENSORS SECTION
// ═══════════════════════════════════════════════════════════════════
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NodeSensorsSection(
    nodes: List<SensorNode>, cardColor: Color, textPrimary: Color, textSecondary: Color,
    onSyncAll: () -> Unit, onSyncNode: (String) -> Unit, selectedCrop: String?, language: String
) {
    val scope       = rememberCoroutineScope()
    val syncingById = remember { mutableStateMapOf<String, Boolean>() }
    val uiNodes     = remember(nodes) { nodes.map { it.toSoilNodeUi() } }
    val pagerState  = rememberPagerState(pageCount = { uiNodes.size })

    Column {
        Text(Strings.get(Strings.soilNodes, language), fontWeight = FontWeight.Bold, color = textPrimary, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        if (nodes.isEmpty()) {
            Text(Strings.get(Strings.waitingLive, language), color = textSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            uiNodes.getOrNull(page)?.let { node ->
                val nodeSyncing = syncingById[node.id] == true
                NodeSensorCardWithCircles(
                    nodeId = node.id, title = node.title, params = node.params,
                    cardColor = cardColor, textPrimary = textPrimary, textSecondary = textSecondary,
                    isSyncing = nodeSyncing, onSync = {
                        if (!nodeSyncing) {
                            syncingById[node.id] = true
                            scope.launch { onSyncNode(node.id); delay(700); syncingById[node.id] = false }
                        }
                    }, selectedCrop = selectedCrop, language = language
                )
            }
        }
        if (uiNodes.size > 1) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                repeat(uiNodes.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(modifier = Modifier.padding(horizontal = 3.dp).size(if (selected) 8.dp else 5.dp)
                        .background(if (selected) PrimaryGreen else PrimaryGreen.copy(alpha = 0.3f), CircleShape))
                }
            }
        }
    }
}

private fun SensorNode.toSoilNodeUi(): SoilNodeUi {
    val params = mutableListOf<SoilParam>()

    moisture?.let    { params.add(SoilParam("Moisture",   "${it.toInt()}%", it)) }
    temperature?.let { params.add(SoilParam("Soil Temp",  "${it.toInt()}°C", it)) }

    // ✅ ADD THIS (Humidity)
    humidity?.let { params.add(SoilParam("Humidity", "${it.toInt()}%", it)) }

    n?.let  { params.add(SoilParam("Nitrogen",   "${it.toInt()} mg/kg", it)) }
    p?.let  { params.add(SoilParam("Phosphorus", "${it.toInt()} mg/kg", it)) }
    k?.let  { params.add(SoilParam("Potassium",  "${it.toInt()} mg/kg", it)) }
    ph?.let { params.add(SoilParam("pH", String.format("%.1f", it), it)) }
    ec?.let { params.add(SoilParam("EC", String.format("%.1f", it), it)) }

    return SoilNodeUi(id = id, title = "Node $id", params = params)
}
@Composable
fun NodeSensorCardWithCircles(
    nodeId: String = "", title: String, params: List<SoilParam>,
    cardColor: Color, textPrimary: Color, textSecondary: Color,
    isSyncing: Boolean, onSync: () -> Unit, selectedCrop: String?, language: String
) {
    // Display zone label instead of raw node id
    val zoneTitle = if (nodeId.isNotBlank()) formatZoneLabel(nodeId, language) else title

    Card(modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(zoneTitle, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text(Strings.get(Strings.liveFromField, language), fontSize = 11.sp, color = textSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryGreen)
                        Spacer(Modifier.width(6.dp))
                        Text(Strings.get(Strings.syncing, language), fontSize = 11.sp, color = textSecondary)
                    } else {
                        Text(Strings.get(Strings.sync, language), fontSize = 12.sp, color = PrimaryGreen, modifier = Modifier.clickable { onSync() }.padding(4.dp))
                        IconButton(onClick = onSync, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Sync, contentDescription = "Sync node", tint = PrimaryGreen)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (params.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Text(Strings.get(Strings.noSensorValues, language), color = textSecondary, fontSize = 12.sp)
                }
            } else {
                val rows = params.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rows.forEach { rowParams ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            rowParams.forEach { param -> SingleSensorCircle(param = param, selectedCrop = selectedCrop) }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  SINGLE SENSOR CIRCLE — toggle CRITICAL/LOW/etc ↔ value every 2-3s
// ═══════════════════════════════════════════════════════════════════
@Composable
fun SingleSensorCircle(param: SoilParam, selectedCrop: String?) {
    val status   = cropBasedStatus(selectedCrop, param.name, param.numericValue)
    val color    = when (status) {
        "LOW"      -> Color(0xFFFFA000)
        "MODERATE" -> Color(0xFFFFC107)
        "OPTIMAL"  -> Color(0xFF2E7D32)
        "HIGH"     -> Color(0xFFE74A46)
        "CRITICAL" -> Color(0xFF6C0505)
        else       -> Color.Gray
    }
    val progress = when (param.name) {
        "Moisture" -> param.numericValue / 100f
        "pH"       -> param.numericValue / 14f
        "Humidity" -> param.numericValue / 100f
        else       -> param.numericValue / 600f
    }.coerceIn(0f, 1f)

    // Toggle between status label and actual value every 2–3 seconds
    var showValue by remember { mutableStateOf(false) }
    LaunchedEffect(param.name) {
        while (true) {
            delay(2500L)   // 2.5 s — fast as requested
            showValue = !showValue
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(85.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(color = color.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = 360f,           useCenter = false, style = Stroke(width = 12f))
                drawArc(color = color,                     startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = Stroke(width = 12f))
            }
            Text(
                text       = if (showValue) param.displayValue else status,
                fontWeight = FontWeight.Bold,
                fontSize   = 12.sp,
                color      = color,
                textAlign  = TextAlign.Center
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(param.name, fontSize = 11.sp, color = Color.Gray)
    }
}

private fun cropBasedStatus(crop: String?, paramName: String, value: Float): String =
    when (paramName) {
        "Moisture"   -> when { value < 55f -> "LOW"; value in 55f..80f -> "OPTIMAL"; value in 80f..90f -> "MODERATE"; else -> "HIGH" }
        "Nitrogen"   -> when { value < 280f -> "LOW"; value in 280f..560f -> "OPTIMAL"; else -> "HIGH" }
        "Phosphorus" -> when { value < 15f -> "LOW"; value in 15f..30f -> "OPTIMAL"; value in 30f..80f -> "HIGH"; else -> "CRITICAL" }
        "Potassium"  -> when { value < 120f -> "LOW"; value in 120f..280f -> "OPTIMAL"; else -> "HIGH" }
        "pH"         -> when { value < 5.5f -> "LOW"; value in 5.5f..8.5f -> "OPTIMAL"; else -> "CRITICAL" }
        "EC"         -> when { value < 0.8f -> "LOW"; value in 0.8f..2.5f -> "OPTIMAL"; value in 2.5f..4f -> "MODERATE"; else -> "HIGH" }
        "Humidity" -> when {
            value < 30f -> "LOW"
            value in 30f..70f -> "OPTIMAL"
            else -> "HIGH"
        }

        else         -> "OPTIMAL"
    }

private fun soilStatusColor(name: String, value: Float): Color = when (name) {
    "Moisture"  -> when { value < 30 -> Color(0xFFD32F2F); value < 50 -> Color(0xFFF57C00); else -> PrimaryGreen }
    "pH"        -> when { value < 5.5f || value > 8f -> Color(0xFFD32F2F); value < 6f || value > 7.5f -> Color(0xFFF57C00); else -> PrimaryGreen }
    "EC"        -> when { value > 4f -> Color(0xFFD32F2F); value > 2f -> Color(0xFFF57C00); else -> PrimaryGreen }
    "Soil Temp" -> when { value < 10 || value > 35 -> Color(0xFFD32F2F); value < 15 || value > 30 -> Color(0xFFF57C00); else -> PrimaryGreen }
    else        -> PrimaryGreen
}

// ═══════════════════════════════════════════════════════════════════
//  SYSTEM STATUS GRID
// ═══════════════════════════════════════════════════════════════════
@Composable
fun SystemStatusGrid(
    cardColor: Color, textPrimary: Color, textSecondary: Color,
    isPumpOn: Boolean, isPumpUpdating: Boolean, tds: Int, tank: Int, rain: Int,
    language: String, onPumpToggleRequest: (Boolean) -> Unit
) {
    Column {
        Text(Strings.get(Strings.farmSystems, language), fontWeight = FontWeight.Bold, color = textPrimary)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SystemTile(Strings.get(Strings.tds, language),       "$tds ppm",
                    Strings.get(Strings.good, language),  Icons.Default.InvertColors, PrimaryGreen,     cardColor, textPrimary, textSecondary, Modifier.weight(1f))
                SystemTile(Strings.get(Strings.rainSensor, language),
                    if (rain == 1) Strings.get(Strings.rainDetected, language) else Strings.get(Strings.noRain, language),
                    Strings.get(Strings.dry, language),   Icons.Default.Umbrella,     Color(0xFF0288D1), cardColor, textPrimary, textSecondary, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PumpStatusTile(Strings.get(Strings.pumpStatus, language), isPumpOn, isPumpUpdating, cardColor, textPrimary, textSecondary, { onPumpToggleRequest(it) }, Modifier.weight(1f))
                SystemTile(Strings.get(Strings.tankLevel, language),  "$tank%",
                    Strings.get(Strings.enoughWater, language), Icons.Default.Opacity, PrimaryGreen,    cardColor, textPrimary, textSecondary, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SystemTile(Strings.get(Strings.solar, language), Strings.get(Strings.charging, language),
                    "1.4 kW", Icons.Default.Bolt, Color(0xFFFFC107), cardColor, textPrimary, textSecondary, Modifier.weight(1f))
                SystemTile(
                    title    = Strings.get(Strings.irrigation, language),
                    subtitle = if (isPumpOn) (if (language == "HI") "पंप चालू है" else "Pump running") else Strings.get(Strings.nextIn2hrs, language),
                    status   = if (isPumpOn) (if (language == "HI") "सक्रिय" else "Active") else Strings.get(Strings.autoMode, language),
                    icon     = Icons.Default.Water,
                    tint     = if (isPumpOn) PrimaryGreen else Color(0xFF0288D1),
                    cardColor = cardColor, textPrimary = textPrimary, textSecondary = textSecondary, modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SystemTile(title: String, subtitle: String, status: String, icon: ImageVector, tint: Color, cardColor: Color, textPrimary: Color, textSecondary: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(110.dp).clickable { }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(tint.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = tint) }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, fontSize = 11.sp, color = textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Text(status, fontSize = 11.sp, color = tint, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun PumpStatusTile(title: String, isOn: Boolean, isUpdating: Boolean, cardColor: Color, textPrimary: Color, textSecondary: Color, onToggle: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val tint     = when { isUpdating -> Color(0xFFF57C00); isOn -> PrimaryGreen; else -> Color(0xFF546E7A) }
    val subtitle = when { isUpdating -> "Updating…"; isOn -> "ON"; else -> "OFF" }

    Card(modifier = modifier.height(110.dp).clickable(enabled = !isUpdating) { onToggle(!isOn) }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(tint.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.WaterDamage, contentDescription = null, tint = tint) }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    Text(subtitle, fontSize = 11.sp, color = textSecondary)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = tint)
                    Spacer(Modifier.width(6.dp))
                    Text("Updating", fontSize = 11.sp, color = tint)
                } else {
                    Switch(checked = isOn, onCheckedChange = { onToggle(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen, uncheckedThumbColor = Color.White, uncheckedTrackColor = Color(0xFF90A4AE)))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  LOG HISTORY BAR GRAPH
// ═══════════════════════════════════════════════════════════════════
@Composable
fun LogHistoryBarGraph(cardColor: Color, textPrimary: Color, textSecondary: Color, navController: NavController, language: String) {
    val values = listOf(45, 68, 55, 72, 60, 78, 65)
    val days   = if (language == "HI") listOf("सोम","मंगल","बुध","गुरु","शुक्र","शनि","रवि")
    else listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")

    Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.Logs.route) }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(Strings.get(Strings.logsHistory, language), fontWeight = FontWeight.Bold, color = textPrimary)
                    Text(Strings.get(Strings.last7days, language), fontSize = 11.sp, color = textSecondary)
                }
                Icon(Icons.Default.History, contentDescription = null, tint = PrimaryGreen)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                values.forEachIndexed { index, v ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.height((v * 1.2).dp).width(16.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryGreen))
                        Spacer(Modifier.height(4.dp))
                        Text(days[index], fontSize = 10.sp, color = textSecondary)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  QUICK MODULE BUTTONS
// ═══════════════════════════════════════════════════════════════════
@Composable
fun QuickModuleButtons(navController: NavController, textPrimary: Color, textSecondary: Color, language: String) {
    Column {
        Text(Strings.get(Strings.quickModules, language), fontWeight = FontWeight.Bold, color = textPrimary)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            QuickButton(Strings.get(Strings.disease, language), Icons.Default.BugReport,    textSecondary) { navController.navigate(Screen.DiseaseDetection.route) }
            QuickButton(Strings.get(Strings.rover, language),   Icons.Default.Videocam,     textSecondary) { navController.navigate(Screen.Rover.route) }
            QuickButton(Strings.get(Strings.market, language),  Icons.Default.AttachMoney,  textSecondary) { navController.navigate(Screen.MarketPrice.route) }
            QuickButton(Strings.get(Strings.logs, language),    Icons.Default.History,      textSecondary) { navController.navigate(Screen.Logs.route) }
            QuickButton(Strings.get(Strings.chat, language),    Icons.Default.SupportAgent, textSecondary) { navController.navigate(Screen.KrishiMitri.route) }
        }
    }
}

@Composable
fun QuickButton(label: String, icon: ImageVector, textColor: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(modifier = Modifier.size(55.dp).clip(CircleShape).background(AccentGreenSoft), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = PrimaryGreen)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = textColor)
    }
}

// ═══════════════════════════════════════════════════════════════════
//  CHAT POPUP
// ═══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPopup(navController: NavController, chatViewModel: KrishiMitriChatViewModel, onClosePopup: () -> Unit, language: String, isDark: Boolean) {
    val headerBg     = if (isDark) Color(0xFF0D2318) else Color(0xFFF1FAF3)
    val textPrimary  = if (isDark) Color(0xFFE8F5E9) else Color(0xFF0A0F0B)
    val dividerColor = if (isDark) Color(0xFF1E3A26) else Color(0xFFE0E0E0)

    Dialog(onDismissRequest = { onClosePopup() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.90f), shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0A1C12) else Color(0xFFF3FBF5))) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(modifier = Modifier.fillMaxWidth(), color = headerBg, shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryGreen.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Image(painter = painterResource(id = R.drawable.bot), contentDescription = "Bot", modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(Strings.get(Strings.krishi, language), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF4CAF50), CircleShape))
                                Spacer(Modifier.width(5.dp))
                                Text(Strings.get(Strings.online, language), fontSize = 11.sp, color = Color(0xFF4CAF50))
                            }
                        }
                        IconButton(onClick = { onClosePopup(); navController.navigate(Screen.KrishiMitri.route) }) { Icon(Icons.Default.Fullscreen, contentDescription = "Full screen", tint = textPrimary) }
                        IconButton(onClick = { onClosePopup() }) { Icon(Icons.Default.Close, contentDescription = "Close", tint = textPrimary) }
                    }
                }
                HorizontalDivider(color = dividerColor)
                KrishiMitriChatScreen(viewModel = chatViewModel, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  ANIMATED CHATBOT FAB
// ═══════════════════════════════════════════════════════════════════
@Composable
fun AnimatedChatbotFab(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "chatbotFab")
    val scale by infiniteTransition.animateFloat(
        initialValue  = 0.95f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "fabScale"
    )
    FloatingActionButton(onClick = onClick, containerColor = Color.White, modifier = Modifier.scale(scale)) {
        Image(painter = painterResource(id = R.drawable.bot), contentDescription = "Chatbot", modifier = Modifier.size(52.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════
//  WEATHER FETCH — shows cached data, silently updates
// ═══════════════════════════════════════════════════════════════════
private suspend fun fetchWeatherForLocation(locationName: String, lat: Double, lon: Double): WeatherInfo? = withContext(Dispatchers.IO) {
    try {
        val useLatLon    = lat != 0.0 && lon != 0.0
        val safeLocation = if (locationName.isBlank()) "Delhi,IN" else locationName
        val encoded      = URLEncoder.encode(safeLocation, "UTF-8")

        val currentUrl = if (useLatLon)
            URL("https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&appid=$OPEN_WEATHER_API_KEY")
        else URL("https://api.openweathermap.org/data/2.5/weather?q=$encoded&units=metric&appid=$OPEN_WEATHER_API_KEY")

        val conn = currentUrl.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"; conn.connectTimeout = 10000; conn.readTimeout = 10000
        if (conn.responseCode != HttpURLConnection.HTTP_OK) { conn.disconnect(); return@withContext null }
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()

        val json       = JSONObject(body)
        val main       = json.getJSONObject("main")
        val wObj       = json.getJSONArray("weather").getJSONObject(0)
        val windObj    = json.optJSONObject("wind") ?: JSONObject()
        val temp       = main.getDouble("temp").toFloat().roundToInt()
        val feelsLike  = main.getDouble("feels_like").toFloat().roundToInt()
        val humidity   = main.getInt("humidity")
        val pressure   = main.getInt("pressure")
        val windKph    = ((windObj.optDouble("speed", 0.0)) * 3.6).roundToInt()
        val condition  = wObj.getString("main")
        val visibility = json.optDouble("visibility", 10000.0) / 1000.0

        val fcUrl = if (useLatLon)
            URL("https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&units=metric&appid=$OPEN_WEATHER_API_KEY")
        else URL("https://api.openweathermap.org/data/2.5/forecast?q=$encoded&units=metric&appid=$OPEN_WEATHER_API_KEY")

        val fcConn = fcUrl.openConnection() as HttpURLConnection
        fcConn.requestMethod = "GET"; fcConn.connectTimeout = 10000; fcConn.readTimeout = 10000
        if (fcConn.responseCode != HttpURLConnection.HTTP_OK) {
            fcConn.disconnect()
            return@withContext WeatherInfo(condition, temp, humidity, windKph, pressure, visibility, feelsLike = feelsLike, dewPoint = temp - ((100 - humidity) / 5), daily = emptyList())
        }
        val fcBody = fcConn.inputStream.bufferedReader().use { it.readText() }
        fcConn.disconnect()

        val fcJson  = JSONObject(fcBody)
        val listArr = fcJson.getJSONArray("list")
        val byDate  = linkedMapOf<String, MutableList<JSONObject>>()
        for (i in 0 until listArr.length()) {
            val item    = listArr.getJSONObject(i)
            val dateKey = item.getString("dt_txt").substring(0, 10)
            byDate.getOrPut(dateKey) { mutableListOf() }.add(item)
        }
        val sdfIn  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfOut = SimpleDateFormat("EEE",        Locale.getDefault())
        val dailyForecasts = mutableListOf<DailyForecast>()
        byDate.entries.take(5).forEachIndexed { index, (dateKey, items) ->
            var minT = Float.MAX_VALUE; var maxT = -Float.MAX_VALUE
            var rainSum = 0.0; var rainCount = 0; var condName = "Clear"
            items.forEach { obj ->
                val t = obj.getJSONObject("main").getDouble("temp").toFloat()
                if (t < minT) minT = t; if (t > maxT) maxT = t
                condName = obj.getJSONArray("weather").getJSONObject(0).getString("main")
                rainSum += obj.optDouble("pop", 0.0); rainCount++
            }
            val rainChance = if (rainCount > 0) ((rainSum / rainCount) * 100).roundToInt() else 0
            val label = when (index) {
                0    -> "Today"
                1    -> "Tomorrow"
                else -> try { sdfOut.format(sdfIn.parse(dateKey)!!) } catch (e: Exception) { dateKey }
            }
            dailyForecasts += DailyForecast(label, minT.roundToInt(), maxT.roundToInt(), condName, rainChance)
        }
        WeatherInfo(condition, temp, humidity, windKph, pressure, visibility, 0, temp - ((100 - humidity) / 5), feelsLike, dailyForecasts)
    } catch (e: Exception) { e.printStackTrace(); null }
}