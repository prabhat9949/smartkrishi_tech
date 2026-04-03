package com.smartkrishi.utils

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import okhttp3.MediaType.Companion.toMediaType
private const val TAG = "OpenAIHelper"
private const val OPENAI_API_KEY = "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"

object OpenAIHelper {

    private val client = OkHttpClient()

    suspend fun analyzeLeaf(
        bitmap: Bitmap,
        crop: String,
        tfPrediction: String
    ): String = withContext(Dispatchers.IO) {
       // val json = JSONObject().apply {
       //     put("model", "gpt-image-1-mini")
     //   }
        try {

            val base64Image = bitmapToBase64(bitmap)

            val json = JSONObject().apply {
                put("model", "gpt-4o")

                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {

                            // TEXT PART
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", """
You are an agricultural plant pathologist.

Selected crop: $crop
TensorFlow predicted: $tfPrediction

Analyze the image carefully.

Rules:
1. If image crop does NOT match selected crop → return ONLY:
Wrong crop selected

2. If healthy → return ONLY:
Healthy

3. Otherwise return ONLY disease name.

Do not explain.
                                """.trimIndent())
                            })


                            // IMAGE PART
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/jpeg;base64,$base64Image")
                                })
                            })
                        })
                    })
                })
            }

            val requestBody = RequestBody.create(
                "application/json".toMediaType(),
                json.toString()
            )

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $OPENAI_API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e(TAG, "API Error: ${response.code}")
                return@withContext "OpenAI unavailable"
            }

            val responseBody = response.body?.string() ?: return@withContext "No response"

            Log.d(TAG, "OpenAI RAW: $responseBody")

            val parsed = JSONObject(responseBody)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            return@withContext parsed.trim()

        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            return@withContext "OpenAI unavailable"
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}