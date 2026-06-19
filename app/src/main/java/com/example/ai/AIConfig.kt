package com.example.ai

/**
 * AI agent configuration — stored persistently.
 */
data class AIConfig(
    val apiKey: String = "",
    val apiEndpoint: String = "",
    val model: String = "gpt-4o",
    val enabled: Boolean = false,
    val autoPlay: Boolean = false,
)