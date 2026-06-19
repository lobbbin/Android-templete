package com.example.ai

import com.example.game.model.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * AI Game Agent — controls the presidency via LLM.
 * 
 * The AI receives the current game state and returns a structured action.
 */
class AIGameAgent(
    private val config: AIConfig,
    private val onAction: (AIAction) -> Unit,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val llmClient = LLMClient(config)

    /**
     * AI Action types — what the AI can do.
     */
    @Serializable
    sealed class AIAction {
        @Serializable
        data class AdvanceDay(val days: Int = 1) : AIAction()

        @Serializable
        data class EnactPolicy(val policyId: String) : AIAction()

        @Serializable
        data class SendAid(val nationId: String, val amount: Int) : AIAction()

        @Serializable
        data class DeclareWar(val nationId: String) : AIAction()

        @Serializable
        data class SignPeaceTreaty(val nationId: String) : AIAction()

        @Serializable
        data class ResolveCrisis(val crisisId: String, val optionId: String) : AIAction()

        @Serializable
        data class HireAdvisor(val categoryId: String, val name: String) : AIAction()

        @Serializable
        data class FireAdvisor(val categoryId: String) : AIAction()

        @Serializable
        object DoNothing : AIAction()
    }

    /**
     * AI Response schema.
     */
    @Serializable
    data class AIResponse(
        val reasoning: String,
        val action: String,
        val parameters: Map<String, String> = emptyMap(),
    )

    /**
     * Generate a prompt for the LLM with current game state.
     */
    fun buildPrompt(gameState: GameState): String {
        val president = gameState.president

        val crisisInfo = if (gameState.activeCrises.isNotEmpty()) {
            gameState.activeCrises.joinToString("\n") { crisis ->
                """
                |⚠️ CRISIS: ${crisis.title}
                |   ${crisis.description}
                |   Options:
                |   ${crisis.options.joinToString("\n   ") { "• ${it.id}: ${it.text} (${formatImpacts(it)})" }}
                """.trimMargin()
            }
        } else {
            "No active crises."
        }

        val topPolicies = gameState.policies.filter { !it.enacted }.take(5)
        val policyList = if (topPolicies.isNotEmpty()) {
            topPolicies.joinToString("\n") { policy ->
                "• ${policy.id}: ${policy.name} (${policy.category.emoji}) - Cost: ${policy.politicalCapitalCost} PC"
            }
        } else {
            "No pending policies."
        }

        val diplomacyInfo = gameState.nations.joinToString("\n") { nation ->
            "• ${nation.name}: ${nation.relationship.emoji} ${nation.relationship.displayName}"
        }

        return """
|You are the President of ${president.countryName}. Your goal is to be re-elected and leave a positive legacy.
|
|📊 CURRENT STATE (Day ${gameState.dayInQuarters} of ${president.quarter.displayName}, Year ${president.year})
|
|**Metrics:**
|• Approval Rating: ${president.approvalRating}% (need >50% for re-election)
|• GDP Growth: ${president.gdpGrowth}%
|• Treasury: $${president.treasury}M
|• National Debt: $${president.nationalDebt}M
|• Unemployment: ${president.unemployment}%
|• Inflation: ${president.inflation}%
|• Political Capital: ${president.politicalCapital}/100
|• Congress Support: ${president.congressSupport}/100
|• Days in Office: ${president.daysInOffice}
|• Term: ${president.term}/2
|
|**Active Crises:**
|$crisisInfo
|
|**Available Policies:**
|$policyList
|
|**Diplomatic Relations:**
|$diplomacyInfo
|
|**Cabinet:**
|${gameState.cabinet.joinToString("\n") { "• ${it.category.emoji} ${it.category.displayName}: ${it.name}" }}
|
|⚡ **YOUR OPTIONS:**
|1. `advance_day` - Pass time (1-7 days)
|2. `enact_policy` - Sign a policy (requires Political Capital)
|3. `send_aid` - Foreign aid to a nation
|4. `declare_war` - Military action (risky!)
|5. `sign_peace` - End a war
|6. `resolve_crisis` - Choose a crisis response
|7. `hire_advisor` / `fire_advisor` - Cabinet management
|8. `do_nothing` - Wait and observe
|
|Respond in JSON format:
|{
|  "reasoning": "Your strategic thinking (2-3 sentences)",
|  "action": "action_name",
|  "parameters": { "key": "value" } // if needed
|}
|
|Think strategically: balance economy, approval, and crises. Prioritize re-election!
""".trimMargin()
    }

    /**
     * Parse AI response into an action.
     */
    fun parseResponse(responseText: String): AIAction {
        return try {
            val aiResponse = json.decodeFromString<AIResponse>(responseText)
            
            when (aiResponse.action) {
                "advance_day" -> AIAction.AdvanceDay(
                    aiResponse.parameters["days"]?.toIntOrNull() ?: 1
                )
                "enact_policy" -> AIAction.EnactPolicy(
                    aiResponse.parameters["policyId"] ?: return AIAction.DoNothing
                )
                "send_aid" -> AIAction.SendAid(
                    aiResponse.parameters["nationId"] ?: return AIAction.DoNothing,
                    aiResponse.parameters["amount"]?.toIntOrNull() ?: 100
                )
                "declare_war" -> AIAction.DeclareWar(
                    aiResponse.parameters["nationId"] ?: return AIAction.DoNothing
                )
                "sign_peace" -> AIAction.SignPeaceTreaty(
                    aiResponse.parameters["nationId"] ?: return AIAction.DoNothing
                )
                "resolve_crisis" -> AIAction.ResolveCrisis(
                    aiResponse.parameters["crisisId"] ?: return AIAction.DoNothing,
                    aiResponse.parameters["optionId"] ?: return AIAction.DoNothing
                )
                "hire_advisor" -> AIAction.HireAdvisor(
                    aiResponse.parameters["categoryId"] ?: return AIAction.DoNothing,
                    aiResponse.parameters["name"] ?: "Advisor"
                )
                "fire_advisor" -> AIAction.FireAdvisor(
                    aiResponse.parameters["categoryId"] ?: return AIAction.DoNothing
                )
                "do_nothing" -> AIAction.DoNothing
                else -> AIAction.DoNothing
            }
        } catch (e: Exception) {
            // Fallback: advance day if parsing fails
            AIAction.AdvanceDay(1)
        }
    }

    /**
     * Execute AI turn — call LLM, parse response, invoke action.
     */
    suspend fun makeDecision(gameState: GameState) {
        if (!config.enabled || config.apiKey.isEmpty() || config.apiEndpoint.isEmpty()) return

        val prompt = buildPrompt(gameState)

        // Call LLM API
        val responseText = llmClient.chat(prompt)

        // Parse and execute action
        val action = parseResponse(responseText)
        onAction(action)
    }

    private fun formatImpacts(option: CrisisOption): String {
        val parts = mutableListOf<String>()
        if (option.economicImpact != 0) parts += "${if (option.economicImpact > 0) "+" else ""}${option.economicImpact} GDP"
        if (option.approvalImpact != 0) parts += "${if (option.approvalImpact > 0) "+" else ""}${option.approvalImpact} Approval"
        if (option.budgetImpact != 0) parts += "$${option.budgetImpact}M"
        return parts.joinToString(", ")
    }
}