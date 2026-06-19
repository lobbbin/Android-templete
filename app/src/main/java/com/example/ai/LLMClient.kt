package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable

/**
 * HTTP client for calling external LLM APIs.
 */
class LLMClient(private val config: AIConfig) {
    
    @Serializable
    data class OpenAIRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.7,
        val max_tokens: Int = 500,
    )
    
    @Serializable
    data class Message(
        val role: String,
        val content: String,
    )
    
    @Serializable
    data class OpenAIResponse(
        val choices: List<Choice>,
    )
    
    @Serializable
    data class Choice(
        val message: Message,
    )
    
    /**
     * Call OpenAI-compatible API endpoint.
     */
    suspend fun chat(prompt: String): String = withContext(Dispatchers.IO) {
        val url = URL(config.apiEndpoint)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 60000
            
            val requestJson = Json.encodeToString(
                OpenAIRequest.serializer(),
                OpenAIRequest(
                    model = config.model,
                    messages = listOf(Message("user", prompt)),
                )
            )
            
            connection.outputStream.bufferedWriter().use {
                it.write(requestJson)
            }
            
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                return@withContext """{
                    "reasoning": "API error: $errorBody",
                    "action": "do_nothing",
                    "parameters": {}
                }"""
            }
            
            val responseBody = connection.inputStream.bufferedReader().readText()
            val response = Json.decodeFromString(OpenAIResponse.serializer(), responseBody)
            
            response.choices.firstOrNull()?.message?.content
                ?: """{
                    "reasoning": "No response from API",
                    "action": "do_nothing",
                    "parameters": {}
                }"""
        } catch (e: Exception) {
            e.printStackTrace()
            """{
                "reasoning": "Error: ${e.message}",
                "action": "do_nothing",
                "parameters": {}
            }"""
        } finally {
            connection.disconnect()
        }
    }
}