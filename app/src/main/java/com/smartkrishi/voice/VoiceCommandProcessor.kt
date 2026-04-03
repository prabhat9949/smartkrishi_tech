package com.smartkrishi.voice

import androidx.navigation.NavController
import com.smartkrishi.presentation.navigation.Screen
import java.util.Locale

class VoiceCommandProcessor(
    private val navController: NavController,
    private val onPumpToggle: (Boolean) -> Unit,
    private val speak: (String) -> Unit,
    private val getSensorData: () -> SensorSnapshot
) {

    data class SensorSnapshot(
        val moisture: String = "--",
        val temperature: String = "--",
        val humidity: String = "--",
        val ph: String = "--",
        val ec: String = "--",
        val n: String = "--",
        val p: String = "--",
        val k: String = "--",
        val tankLevel: String = "--",
        val rainStatus: String = "--",
        val pumpStatus: String = "--",
        val weatherSummary: String = "मौसम डेटा उपलब्ध नहीं है"
    )

    private enum class CommandLanguage {
        HINDI, ENGLISH, MIXED
    }

    // ================= NORMALIZATION =================
    private fun normalize(input: String): String {
        return input
            .lowercase(Locale.getDefault())
            .replace("  ", " ")
            .replace("kro", "करो")
            .replace("karo", "करो")
            .replace("kar do", "कर दो")
            .replace("kar de", "कर दो")
            .replace("khol do", "खोलो")
            .replace("khol do", "खोलो")
            .replace("open", "खोलो")
            .replace("start", "चालू")
            .replace("switch on", "चालू")
            .replace("turn on", "चालू")
            .replace("turn off", "बंद")
            .replace("stop", "बंद")
            .replace("off", "बंद")
            .replace("band", "बंद")
            .replace("close", "बंद")
            .replace("pump", "पंप")
            .replace("motor", "पंप")
            .replace("disease", "रोग")
            .replace("weather", "मौसम")
            .replace("crop", "फसल")
            .replace("chat", "चैट")
            .replace("market", "मंडी")
            .replace("price", "भाव")
            .replace("log", "लॉग")
            .replace("history", "लॉग")
            .replace("record", "रिकॉर्ड")
            .replace("scheme", "योजना")
            .replace("government", "सरकारी")
            .replace("rover", "रोवर")
            .replace("camera", "कैमरा")
            .replace("video", "वीडियो")
            .replace("value", "वैल्यू")
            .replace("values", "वैल्यू")
            .replace("status", "स्थिति")
            .replace("moisture", "मॉइस्चर")
            .replace("temperature", "तापमान")
            .replace("humidity", "नमी")
            .replace("ph level", "पीएच")
            .replace("ph", "पीएच")
            .replace("nitrogen", "नाइट्रोजन")
            .replace("phosphorus", "फॉस्फोरस")
            .replace("potassium", "पोटैशियम")
            .replace("tank", "टैंक")
            .replace("rain", "बारिश")
            .replace("all values", "सभी वैल्यू")
            .replace("tell me", "बताओ")
            .replace("show me", "दिखाओ")
            .replace("speak", "बताओ")
            .replace("say", "बताओ")
            .trim()
    }

    private fun detectLanguage(command: String): CommandLanguage {
        val hasHindi = command.any { it.code in 0x0900..0x097F }
        val englishHints = listOf(
            "weather", "crop", "pump", "chat", "market", "status",
            "value", "tell", "show", "ph", "moisture", "temperature",
            "humidity", "tank", "rain"
        )
        val hasEnglish = englishHints.any { command.lowercase(Locale.getDefault()).contains(it) }

        return when {
            hasHindi && hasEnglish -> CommandLanguage.MIXED
            hasHindi -> CommandLanguage.HINDI
            else -> CommandLanguage.ENGLISH
        }
    }

    fun process(command: String) {
        val raw = command.trim()
        val cmd = normalize(raw)
        val lang = detectLanguage(raw)

        when {

            // ================= ALL VALUES =================
            cmd.containsAny(
                "सभी वैल्यू", "वैल्यू बताओ", "सब वैल्यू", "सारी वैल्यू",
                "सेंसर वैल्यू", "स्थिति बताओ", "सभी स्थिति", "पूरा स्टेटस",
                "tell all values", "tell values", "all sensor values", "all values",
                "sensor status", "full status"
            ) -> {
                speak(buildAllValuesResponse(lang))
            }

            // ================= PH VALUE =================
            cmd.containsAny(
                "पीएच", "ph", "पीएच वैल्यू", "ph value", "पीएच बताओ", "ph batao"
            ) -> {
                speak(buildSingleValueResponse("ph", lang))
            }

            // ================= MOISTURE VALUE =================
            cmd.containsAny(
                "मॉइस्चर", "नमी", "moisture", "moisture value", "नमी बताओ"
            ) -> {
                speak(buildSingleValueResponse("moisture", lang))
            }

            // ================= TEMPERATURE VALUE =================
            cmd.containsAny(
                "तापमान", "temperature", "temperature value", "तापमान बताओ"
            ) -> {
                speak(buildSingleValueResponse("temperature", lang))
            }

            // ================= HUMIDITY VALUE =================
            cmd.containsAny(
                "humidity", "नमी प्रतिशत", "ह्यूमिडिटी", "आर्द्रता"
            ) -> {
                speak(buildSingleValueResponse("humidity", lang))
            }

            // ================= EC VALUE =================
            cmd.containsAny("ec", "ईसी", "ec value") -> {
                speak(buildSingleValueResponse("ec", lang))
            }

            // ================= N VALUE =================
            cmd.containsAny("नाइट्रोजन", "nitrogen", "n value") -> {
                speak(buildSingleValueResponse("n", lang))
            }

            // ================= P VALUE =================
            cmd.containsAny("फॉस्फोरस", "phosphorus", "p value") -> {
                speak(buildSingleValueResponse("p", lang))
            }

            // ================= K VALUE =================
            cmd.containsAny("पोटैशियम", "potassium", "k value") -> {
                speak(buildSingleValueResponse("k", lang))
            }

            // ================= TANK VALUE =================
            cmd.containsAny("टैंक", "tank level", "tank status") -> {
                speak(buildSingleValueResponse("tank", lang))
            }

            // ================= RAIN VALUE =================
            cmd.containsAny("बारिश", "rain", "rain status") -> {
                speak(buildSingleValueResponse("rain", lang))
            }

            // ================= PUMP STATUS =================
            cmd.containsAny("पंप स्थिति", "pump status", "पंप स्टेटस") -> {
                speak(buildSingleValueResponse("pump", lang))
            }

            // ================= WEATHER =================
            cmd.containsAny("मौसम", "बारिश का मौसम", "weather forecast", "weather") -> {
                val weather = getSensorData().weatherSummary
                if (lang == CommandLanguage.ENGLISH) {
                    speak("Opening weather. Current weather summary is $weather")
                } else {
                    speak("मौसम की जानकारी खोल रहा हूँ। वर्तमान मौसम सारांश है: $weather")
                }
            }

            // ================= CROP =================
            cmd.containsAny("फसल", "सलाह", "recommendation", "crop recommendation", "crop") -> {
                navController.navigate(Screen.CropRecommendation.route)
                if (lang == CommandLanguage.ENGLISH) {
                    speak("Opening crop recommendation")
                } else {
                    speak("फसल सुझाव खोल रहा हूँ")
                }
            }

            // ================= DISEASE =================
            cmd.containsAny("रोग", "बीमारी", "infection", "disease") -> {
                navController.navigate(Screen.DiseaseDetection.route)
                if (lang == CommandLanguage.ENGLISH) {
                    speak("Opening disease detection")
                } else {
                    speak("रोग पहचान खोल रहा हूँ")
                }
            }

            // ================= CHAT AI =================
            cmd.containsAny("चैट", "मित्र", "help", "assistant", "chatbot", "ai help") -> {
                navController.navigate(Screen.KrishiMitri.route)
                if (lang == CommandLanguage.ENGLISH) {
                    speak("Opening Krishi AI assistant")
                } else {
                    speak("कृषि एआई सहायक खोल रहा हूँ")
                }
            }

            // ================= MARKET =================
            cmd.containsAny("मंडी", "भाव", "price", "market price", "market") -> {
                navController.navigate(Screen.MarketPrice.route)
                if (lang == CommandLanguage.ENGLISH) {
                    speak("Opening market prices")
                } else {
                    speak("मंडी भाव दिखा रहा हूँ")
                }
            }

            // ================= LOGS =================
            cmd.containsAny("लॉग", "history", "record", "logs") -> {
                navController.navigate(Screen.Logs.route)
                if (lang == CommandLanguage.ENGLISH) {
                    speak("Opening your data history")
                } else {
                    speak("आपका डेटा इतिहास खोल रहा हूँ")
                }
            }

            // ================= ROVER =================
            cmd.containsAny("रोवर", "camera", "video", "rover") -> {
                navController.navigate(Screen.Rover.route)
                if (lang == CommandLanguage.ENGLISH) {
                    speak("Opening rover control")
                } else {
                    speak("रोवर कंट्रोल खोल रहा हूँ")
                }
            }

            // ================= GOVT SCHEMES =================
            cmd.containsAny("योजना", "government", "scheme", "government scheme") -> {
                navController.navigate(Screen.GovtSchemes.route)
                if (lang == CommandLanguage.ENGLISH) {
                    speak("Opening government schemes")
                } else {
                    speak("सरकारी योजनाएँ दिखा रहा हूँ")
                }
            }

            // ================= PUMP ON =================
            cmd.containsAny(
                "पंप चालू", "पंप चालू करो", "पंप चालू कर दो",
                "pump on", "pump start", "start pump", "turn pump on",
                "motor on", "motor start"
            ) -> {
                onPumpToggle(true)
                if (lang == CommandLanguage.ENGLISH) {
                    speak("Water pump has been turned on")
                } else {
                    speak("पानी का पंप चालू कर दिया गया है")
                }
            }

            // ================= PUMP OFF =================
            cmd.containsAny(
                "पंप बंद", "पंप बंद करो", "पंप बंद कर दो",
                "pump off", "pump stop", "stop pump", "turn pump off",
                "motor off", "motor stop"
            ) -> {
                onPumpToggle(false)
                if (lang == CommandLanguage.ENGLISH) {
                    speak("Water pump has been turned off")
                } else {
                    speak("पानी का पंप बंद कर दिया गया है")
                }
            }

            // ================= STATUS =================
            cmd.containsAny(
                "status", "स्थिति", "क्या हाल", "system status", "app status"
            ) -> {
                if (lang == CommandLanguage.ENGLISH) {
                    speak("System is working normally. All sensors are active")
                } else {
                    speak("सिस्टम सामान्य रूप से काम कर रहा है। सभी सेंसर सक्रिय हैं")
                }
            }

            // ================= HOME =================
            cmd.containsAny("होम", "घर", "dashboard", "home") -> {
                navController.popBackStack()
                if (lang == CommandLanguage.ENGLISH) {
                    speak("Returned to home screen")
                } else {
                    speak("होम स्क्रीन पर वापस आ गया हूँ")
                }
            }

            // ================= AI SMART RESPONSE =================
            cmd.containsAny("सलाह दो", "help me", "क्या करूँ", "what should i do") -> {
                if (lang == CommandLanguage.ENGLISH) {
                    speak("You can ask me about crop, weather, disease, pump, or sensor values")
                } else {
                    speak("आप मुझसे फसल, मौसम, रोग, पंप, या सेंसर वैल्यू के बारे में पूछ सकते हैं")
                }
            }

            // ================= UNKNOWN =================
            else -> {
                if (lang == CommandLanguage.ENGLISH) {
                    speak("Sorry, I could not understand. Please speak again")
                } else {
                    speak("माफ कीजिए, मैं समझ नहीं पाया। कृपया दोबारा बोलें")
                }
            }
        }
    }

    private fun buildAllValuesResponse(lang: CommandLanguage): String {
        val data = getSensorData()
        return if (lang == CommandLanguage.ENGLISH) {
            "Current values are: " +
                    "moisture ${data.moisture}, " +
                    "temperature ${data.temperature}, " +
                    "humidity ${data.humidity}, " +
                    "p h ${data.ph}, " +
                    "e c ${data.ec}, " +
                    "nitrogen ${data.n}, " +
                    "phosphorus ${data.p}, " +
                    "potassium ${data.k}, " +
                    "tank level ${data.tankLevel}, " +
                    "rain status ${data.rainStatus}, " +
                    "pump status ${data.pumpStatus}."
        } else {
            "वर्तमान मान इस प्रकार हैं। " +
                    "मॉइस्चर ${data.moisture}, " +
                    "तापमान ${data.temperature}, " +
                    "नमी ${data.humidity}, " +
                    "पीएच ${data.ph}, " +
                    "ईसी ${data.ec}, " +
                    "नाइट्रोजन ${data.n}, " +
                    "फॉस्फोरस ${data.p}, " +
                    "पोटैशियम ${data.k}, " +
                    "टैंक स्तर ${data.tankLevel}, " +
                    "बारिश की स्थिति ${data.rainStatus}, " +
                    "पंप की स्थिति ${data.pumpStatus}।"
        }
    }

    private fun buildSingleValueResponse(type: String, lang: CommandLanguage): String {
        val data = getSensorData()
        return when (type) {
            "ph" -> if (lang == CommandLanguage.ENGLISH) {
                "Current p h value is ${data.ph}"
            } else {
                "वर्तमान पीएच वैल्यू ${data.ph} है"
            }

            "moisture" -> if (lang == CommandLanguage.ENGLISH) {
                "Current moisture value is ${data.moisture}"
            } else {
                "वर्तमान मॉइस्चर वैल्यू ${data.moisture} है"
            }

            "temperature" -> if (lang == CommandLanguage.ENGLISH) {
                "Current temperature is ${data.temperature}"
            } else {
                "वर्तमान तापमान ${data.temperature} है"
            }

            "humidity" -> if (lang == CommandLanguage.ENGLISH) {
                "Current humidity is ${data.humidity}"
            } else {
                "वर्तमान नमी ${data.humidity} है"
            }

            "ec" -> if (lang == CommandLanguage.ENGLISH) {
                "Current e c value is ${data.ec}"
            } else {
                "वर्तमान ईसी वैल्यू ${data.ec} है"
            }

            "n" -> if (lang == CommandLanguage.ENGLISH) {
                "Current nitrogen value is ${data.n}"
            } else {
                "वर्तमान नाइट्रोजन वैल्यू ${data.n} है"
            }

            "p" -> if (lang == CommandLanguage.ENGLISH) {
                "Current phosphorus value is ${data.p}"
            } else {
                "वर्तमान फॉस्फोरस वैल्यू ${data.p} है"
            }

            "k" -> if (lang == CommandLanguage.ENGLISH) {
                "Current potassium value is ${data.k}"
            } else {
                "वर्तमान पोटैशियम वैल्यू ${data.k} है"
            }

            "tank" -> if (lang == CommandLanguage.ENGLISH) {
                "Current tank level is ${data.tankLevel}"
            } else {
                "वर्तमान टैंक स्तर ${data.tankLevel} है"
            }

            "rain" -> if (lang == CommandLanguage.ENGLISH) {
                "Current rain status is ${data.rainStatus}"
            } else {
                "वर्तमान बारिश की स्थिति ${data.rainStatus} है"
            }

            "pump" -> if (lang == CommandLanguage.ENGLISH) {
                "Current pump status is ${data.pumpStatus}"
            } else {
                "वर्तमान पंप की स्थिति ${data.pumpStatus} है"
            }

            else -> if (lang == CommandLanguage.ENGLISH) {
                "Value is not available"
            } else {
                "वैल्यू उपलब्ध नहीं है"
            }
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}