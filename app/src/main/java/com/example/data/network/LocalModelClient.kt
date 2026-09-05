package com.example.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class LocalHostStatus(
    val isReachable: Boolean,
    val serverType: String,
    val latencyMs: Long,
    val availableModels: List<String>,
    val errorMessage: String? = null
)

object LocalModelClient {
    private const val TAG = "LocalModelClient"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun pingServer(baseUrl: String): LocalHostStatus = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trimEnd('/')
        val startTime = System.currentTimeMillis()

        // 1. Try Ollama /api/tags
        try {
            val ollamaReq = Request.Builder()
                .url("$cleanUrl/api/tags")
                .get()
                .build()

            val response = client.newCall(ollamaReq).execute()
            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: "{}"
                val json = JSONObject(bodyStr)
                val modelsArray = json.optJSONArray("models")
                val modelNames = mutableListOf<String>()
                if (modelsArray != null) {
                    for (i in 0 until modelsArray.length()) {
                        val mObj = modelsArray.optJSONObject(i)
                        val name = mObj?.optString("name")
                        if (!name.isNullOrBlank()) modelNames.add(name)
                    }
                }
                return@withContext LocalHostStatus(
                    isReachable = true,
                    serverType = "Ollama Local Engine",
                    latencyMs = latency,
                    availableModels = if (modelNames.isNotEmpty()) modelNames else listOf("llama3.2", "mistral", "deepseek-r1")
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "Ollama check failed, testing v1/models: ${e.message}")
        }

        // 2. Try OpenAI-compatible /v1/models (LM Studio, vLLM, LocalAI)
        try {
            val lmReq = Request.Builder()
                .url("$cleanUrl/v1/models")
                .get()
                .build()

            val response = client.newCall(lmReq).execute()
            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: "{}"
                val json = JSONObject(bodyStr)
                val dataArray = json.optJSONArray("data")
                val modelNames = mutableListOf<String>()
                if (dataArray != null) {
                    for (i in 0 until dataArray.length()) {
                        val mObj = dataArray.optJSONObject(i)
                        val id = mObj?.optString("id")
                        if (!id.isNullOrBlank()) modelNames.add(id)
                    }
                }
                return@withContext LocalHostStatus(
                    isReachable = true,
                    serverType = "OpenAI-Compatible Local Host (LM Studio/vLLM)",
                    latencyMs = latency,
                    availableModels = if (modelNames.isNotEmpty()) modelNames else listOf("local-model-default")
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "LM Studio check failed: ${e.message}")
        }

        LocalHostStatus(
            isReachable = false,
            serverType = "Unreachable",
            latencyMs = 0,
            availableModels = emptyList(),
            errorMessage = "Could not connect to $cleanUrl. Ensure server is running (e.g., 'ollama serve' or LM Studio) and CORS/origins are permitted."
        )
    }

    suspend fun generateLocalCompletion(
        baseUrl: String,
        modelName: String,
        prompt: String,
        systemPrompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trimEnd('/')

        // Try Ollama native /api/generate
        try {
            val reqBody = JSONObject().apply {
                put("model", modelName)
                put("prompt", prompt)
                if (systemPrompt.isNotBlank()) put("system", systemPrompt)
                put("stream", false)
            }.toString().toRequestBody(JSON_MEDIA_TYPE)

            val request = Request.Builder()
                .url("$cleanUrl/api/generate")
                .post(reqBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "{}")
                val responseText = json.optString("response")
                if (responseText.isNotBlank()) {
                    return@withContext Result.success(responseText)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Local Ollama generation failed, trying /v1/chat/completions: ${e.message}")
        }

        // Try /v1/chat/completions (LM Studio, vLLM, Ollama OpenAI compatibility)
        try {
            val reqBody = JSONObject().apply {
                put("model", modelName)
                put("messages", JSONArray().apply {
                    if (systemPrompt.isNotBlank()) {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                    }
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", 0.7)
            }.toString().toRequestBody(JSON_MEDIA_TYPE)

            val request = Request.Builder()
                .url("$cleanUrl/v1/chat/completions")
                .post(reqBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "{}")
                val choices = json.optJSONArray("choices")
                val firstChoice = choices?.optJSONObject(0)
                val msg = firstChoice?.optJSONObject("message")
                val content = msg?.optString("content")
                if (!content.isNullOrBlank()) {
                    return@withContext Result.success(content)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Local model completion failed", e)
            return@withContext Result.failure(e)
        }

        Result.failure(Exception("Failed to generate completion from local host at $cleanUrl"))
    }
}
