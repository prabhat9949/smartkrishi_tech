package com.smartkrishi.data.ai


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.smartkrishi.BuildConfig
class OpenAIService {

    companion object {
        private val apiKey = BuildConfig.OPENAI_API_KEY
    }

    suspend fun getRecommendation(prompt: String): String =
        withContext(Dispatchers.IO) {

            try {

                val url = URL("https://api.openai.com/v1/chat/completions")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $apiKey") // ✅ FIXED
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val body = JSONObject().apply {

                    put("model", "gpt-4o-mini")

                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "You are an agricultural AI expert.")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })

                    put("temperature", 0.7)
                }

                connection.outputStream.use {
                    it.write(body.toString().toByteArray())
                }

                val responseCode = connection.responseCode

                val responseText = if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream?.bufferedReader()?.readText()
                        ?: "Unknown error"
                }

                if (responseCode !in 200..299) {
                    return@withContext "API Error: $responseCode\n$responseText"
                }

                val json = JSONObject(responseText)

                json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

            } catch (e: Exception) {
                "Exception: ${e.message}"
            }
        }
}