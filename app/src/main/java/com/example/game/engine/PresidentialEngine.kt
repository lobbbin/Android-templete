package com.example.game.engine

import com.example.game.model.*
import kotlin.random.Random

/**
 * Presidential simulation engine — handles time progression, policies, crises, and diplomacy.
 */
class PresidentialEngine {
    
    private var state: PresidentState = PresidentState()
    private val policies = mutableListOf<Policy>()
    private val nations = mutableListOf<Nation>()
    private val cabinet = mutableListOf<CabinetMember>()
    private val crises = mutableListOf<Crisis>()
    private val logMessages = mutableListOf<GameLogMessage>()
    
    init {
        initializeDefaultPolicies()
        initializeDefaultNations()
        initializeDefaultCabinet()
    }
    
    // ── Game initialization ─────────────────────────────────────
    
    fun newGame(presidentName: String, countryName: String): GameState {
        state = PresidentState(
            presidentName = presidentName,
            countryName = countryName,
        )
        policies.clear()
        nations.clear()
        cabinet.clear()
        crises.clear()
        logMessages.clear()
        
        initializeDefaultPolicies()
        initializeDefaultNations()
        initializeDefaultCabinet()
        
        log("Inaugurated as President of $countryName", "🎉")
        log("First term begins", "📅")
        
        return buildGameState()
    }
    
    fun loadGame(state: PresidentState): GameState {
        this.state = state
        return buildGameState()
    }
    
    // ── Time progression ────────────────────────────────────────
    
    /** Advance one day. Returns true if quarter ended. */
    fun advanceDay(): Pair<Boolean, GameState> {
        val quarterEnded = state.dayInQuarters >= 90
        
        state = state.copy(
            dayInQuarters = if (quarterEnded) 1 else state.dayInQuarters + 1,
            quarter = if (quarterEnded) state.quarter.next() else state.quarter,
            year = if (quarterEnded && state.quarter == Quarter.Q4) state.year + 1 else state.year,
            term = if (quarterEnded && state.quarter == Quarter.Q4) state.term + 1 else state.term,
            daysInOffice = state.daysInOffice + 1,
        )
        
        // Daily economic fluctuation
        if (Random.nextInt(100) < 10) {
            val gdpChange = Random.nextInt(-1, 2)
            state = state.copy(gdpGrowth = (state.gdpGrowth + gdpChange).coerceIn(-5, 10))
        }
        
        // Random crisis spawn
        if (Random.nextInt(100) < 5 && crises.size < 3) {
            spawnRandomCrisis()
        }
        
        // Log quarter change
        if (quarterEnded) {
            log("${state.quarter.displayName} of Year ${state.year} begins", state.quarter.emoji)
            checkRe-election()
        }
        
        return Pair(quarterEnded, buildGameState())
    }
    
    // ── Policy system ───────────────────────────────────────────
    
    fun enactPolicy(policyId: String): GameState {
        val policy = policies.find { it.id == policyId && !it.enacted }
            ?: return buildGameState()
        
        val successChance = state.congressSupport + policy.economicImpact
        val success = Random.nextInt(100) < successChance
        
        if (success) {
            state = state.copy(
                approvalRating = (state.approvalRating + policy.approvalImpact).coerceIn(0, 100),
                treasury = state.treasury + policy.budgetImpact,
                politicalCapital = (state.politicalCapital - 10).coerceIn(0, 100),
                billsEnacted = state.billsEnacted + 1,
            )
            policies.replaceAll { if (it.id == policyId) it.copy(enacted = true) else it }
            log("Signed ${policy.name} into law", "📜")
        } else {
            log("Congress blocked ${policy.name}", "❌")
        }
        
        return buildGameState()
    }
    
    fun getPoliciesByCategory(category: PolicyCategory): List<Policy> =
        policies.filter { it.category == category }
    
    // ── Crisis management ───────────────────────────────────────
    
    fun resolveCrisis(crisisId: String, optionId: String): GameState {
        val crisis = crises.find { it.id == crisisId && !it.resolved }
            ?: return buildGameState()
        
        val option = crisis.options.find { it.id == optionId }
            ?: return buildGameState()
        
        state = state.copy(
            approvalRating = (state.approvalRating + option.approvalImpact).coerceIn(0, 100),
            treasury = state.treasury + option.budgetImpact,
            gdpGrowth = (state.gdpGrowth + option.economicImpact).coerceIn(-5, 10),
            crisesResolved = state.crisesResolved + 1,
        )
        
        crises.replaceAll { if (it.id == crisisId) it.copy(resolved = true) else it }
        log("Resolved: ${crisis.title}", crisis.emoji)
        
        return buildGameState()
    }
    
    fun getActiveCrises(): List<Crisis> = crises.filter { !it.resolved }
    
    // ── Diplomacy system ────────────────────────────────────────
    
    fun improveRelations(nationId: String, aidAmount: Int): GameState {
        val nation = nations.find { it.id == nationId }
            ?: return buildGameState()
        
        val improvement = when {
            aidAmount >= 500 -> 20
            aidAmount >= 100 -> 10
            else -> 5
        }
        
        val newStatus = when (nation.relationship) {
            DiplomaticStatus.HOSTILE -> DiplomaticStatus.TENSE
            DiplomaticStatus.TENSE -> DiplomaticStatus.NEUTRAL
            DiplomaticStatus.NEUTRAL -> DiplomaticStatus.FRIENDLY
            DiplomaticStatus.FRIENDLY -> DiplomaticStatus.ALLY
            else -> nation.relationship
        }
        
        nations.replaceAll { 
            if (it.id == nationId) 
                it.copy(relationship = newStatus, aidGiven = it.aidGiven + aidAmount, tradeValue = it.tradeValue + aidAmount / 2)
            else it 
        }
        
        state = state.copy(
            treasury = state.treasury - aidAmount,
            internationalReputation = (state.internationalReputation + 5).coerceIn(0, 100),
        )
        
        log("Sent $aidAmountM aid to ${nation.name}", "🤝")
        
        return buildGameState()
    }
    
    fun declareWar(nationId: String): GameState {
        val nation = nations.find { it.id == nationId }
            ?: return buildGameState()
        
        nations.replaceAll { 
            if (it.id == nationId) it.copy(relationship = DiplomaticStatus.AT_WAR) else it 
        }
        
        state = state.copy(
            isAtWar = true,
            warsStarted = state.warsStarted + 1,
            approvalRating = (state.approvalRating + 10).coerceIn(0, 100), // Rally 'round the flag
            treasury = state.treasury - 1000, // War is expensive
        )
        
        log("Declared war on ${nation.name}!", "⚔️")
        
        return buildGameState()
    }
    
    fun signPeaceTreaty(nationId: String): GameState {
        val nation = nations.find { it.id == nationId }
            ?: return buildGameState()
        
        nations.replaceAll { 
            if (it.id == nationId) it.copy(relationship = DiplomaticStatus.NEUTRAL) else it 
        }
        
        val stillAtWar = nations.any { it.relationship == DiplomaticStatus.AT_WAR }
        
        state = state.copy(
            isAtWar = stillAtWar,
            internationalReputation = (state.internationalReputation + 10).coerceIn(0, 100),
        )
        
        log("Peace treaty signed with ${nation.name}", "🕊️")
        
        return buildGameState()
    }
    
    // ── Cabinet management ──────────────────────────────────────
    
    fun fireCabinetMember(memberId: String): GameState {
        val member = cabinet.find { it.id == memberId }
            ?: return buildGameState()
        
        state = state.copy(
            politicalCapital = (state.politicalCapital - 5).coerceIn(0, 100),
            approvalRating = if (member.scandal) (state.approvalRating + 5).coerceIn(0, 100) else (state.approvalRating - 5).coerceIn(0, 100),
        )
        
        cabinet.removeAll { it.id == memberId }
        log("Dismissed ${member.name} from cabinet", "📤")
        
        return buildGameState()
    }
    
    // ── Win/Lose conditions ─────────────────────────────────────
    
    private fun checkRe-election() {
        if (state.quarter == Quarter.Q4 && state.year % 4 == 0 && state.term <= 2) {
            val re-elected = state.approvalRating >= 50 && state.gdpGrowth >= 0
            if (re-elected && state.term == 1) {
                state = state.copy(re-elected = true)
                log("Re-elected for a second term!", "🎉")
            } else if (!re-elected && state.term == 1) {
                state = state.copy(impeached = true) // Lost election
                log("Lost re-election. Presidency ended.", "❌")
            }
        }
    }
    
    fun isGameOver(): Boolean = state.impeached || state.term > 2
    
    // ── Internal helpers ────────────────────────────────────────
    
    private fun buildGameState(): GameState = GameState(
        president = state,
        policies = policies.toList(),
        nations = nations.toList(),
        activeCrises = crises.filter { !it.resolved },
        cabinet = cabinet.toList(),
        logMessages = logMessages.takeLast(50),
    )
    
    private fun log(message: String, emoji: String = "") {
        logMessages.add(
            GameLogMessage(
                day = state.dayInQuarters,
                quarter = state.quarter,
                year = state.year,
                message = message,
                emoji = emoji,
            )
        )
    }
    
    private fun spawnRandomCrisis() {
        val crisisTemplates = listOf(
            Crisis(
                id = "recession",
                title = "Economic Recession",
                description = "GDP is shrinking. Unemployment rising.",
                emoji = "📉",
                type = CrisisType.ECONOMIC,
                severity = 7,
                options = listOf(
                    CrisisOption("stimulus", "Stimulus Package ($500M)", -5, 10, -500, 1, "Boost economy with spending"),
                    CrisisOption("austerity", "Austerity Measures", 2, -15, 200, -2, "Cut spending, balance budget"),
                    CrisisOption("tax_cuts", "Tax Cuts", -3, 5, -300, 2, "Cut taxes to stimulate growth"),
                ),
            ),
            Crisis(
                id = "wildfire",
                title = "Major Wildfire",
                description = "Devastating wildfires across the nation.",
                emoji = "🔥",
                type = CrisisType.DISASTER,
                severity = 8,
                options = listOf(
                    CrisisOption("fema", "Deploy FEMA ($200M)", 0, 10, -200, 0, "Federal disaster response"),
                    CrisisOption("national_guard", "National Guard", 0, 5, -50, 0, "Deploy military support"),
                    CrisisOption("state_only", "Let States Handle It", 0, -10, 0, 0, "State responsibility"),
                ),
            ),
            Crisis(
                id = "scandal",
                title = "Cabinet Scandal",
                description = "One of your advisors is embroiled in controversy.",
                emoji = "💼",
                type = CrisisType.SCANDAL,
                severity = 5,
                options = listOf(
                    CrisisOption("fire", "Fire Them Immediately", 0, 5, 0, 0, "Swift action"),
                    CrisisOption("investigate", "Order Investigation", 0, -5, 0, 0, "Wait for facts"),
                    CrisisOption("defend", "Defend Them Publicly", 0, -10, 0, 0, "Stand by your team"),
                ),
            ),
            Crisis(
                id = "invasion",
                title = "Foreign Aggression",
                description = "A hostile nation is threatening invasion.",
                emoji = "⚔️",
                type = CrisisType.MILITARY,
                severity = 9,
                options = listOf(
                    CrisisOption("mobilize", "Mobilize Military ($1B)", 0, 10, -1000, 0, "Show of force"),
                    CrisisOption("sanctions", "Economic Sanctions", -2, 0, 100, 0, "Economic pressure"),
                    CrisisOption("diplomacy", "Diplomatic Talks", 0, -5, -50, 0, "Peaceful resolution"),
                ),
            ),
        )
        
        val template = crisisTemplates.random()
        crises.add(template.copy(id = "${template.id}_${state.daysInOffice}"))
    }
    
    private fun initializeDefaultPolicies() {
        policies.addAll(listOf(
            Policy("universal_healthcare", "Universal Healthcare", "Government-funded healthcare for all", PolicyCategory.HEALTHCARE, -2, 15, -800, 5),
            Policy("tax_cut_rich", "Tax Cuts for Wealthy", "Reduce taxes on top earners", PolicyCategory.ECONOMY, 3, -10, -400, -3),
            Policy("tax_cut_middle", "Middle Class Tax Cut", "Reduce taxes for middle class", PolicyCategory.ECONOMY, 2, 8, -600, 2),
            Policy("green_new_deal", "Green New Deal", "Aggressive climate action plan", PolicyCategory.ENVIRONMENT, -1, 12, -1200, 3),
            Policy("military_buildup", "Military Buildup", "Increase defense spending", PolicyCategory.DEFENSE, 1, 5, -500, -5),
            Policy("free_college", "Free College Tuition", "Public universities tuition-free", PolicyCategory.EDUCATION, 1, 10, -700, 5),
            Policy("border_wall", "Border Wall", "Build wall on southern border", PolicyCategory.IMMIGRATION, 0, -8, -300, -8),
            Policy("infrastructure", "Infrastructure Bill", "Repair roads, bridges, broadband", PolicyCategory.INFRASTRUCTURE, 2, 7, -900, 2),
        ))
    }
    
    private fun initializeDefaultNations() {
        nations.addAll(listOf(
            Nation("canada", "Canada", "🇨🇦", DiplomaticStatus.ALLY, 300, 10),
            Nation("uk", "United Kingdom", "🇬🇧", DiplomaticStatus.ALLY, 250, 15),
            Nation("china", "China", "🇨🇳", DiplomaticStatus.TENSE, 500, 60),
            Nation("russia", "Russia", "🇷🇺", DiplomaticStatus.HOSTILE, 100, 70),
            Nation("mexico", "Mexico", "🇲🇽", DiplomaticStatus.FRIENDLY, 200, 20),
            Nation("eu", "European Union", "🇪🇺", DiplomaticStatus.FRIENDLY, 400, 25),
            Nation("iran", "Iran", "🇮🇷", DiplomaticStatus.HOSTILE, 50, 55),
            Nation("north_korea", "North Korea", "🇰🇵", DiplomaticStatus.HOSTILE, 10, 80),
        ))
    }
    
    private fun initializeDefaultCabinet() {
        cabinet.addAll(listOf(
            CabinetMember("vp", "Alex Morgan", "Vice President", "👔", 7, 8),
            CabinetMember("sec_state", "Sarah Chen", "Secretary of State", "🌐", 8, 6),
            CabinetMember("sec_defense", "Gen. Robert Hayes", "Secretary of Defense", "🛡️", 9, 7),
            CabinetMember("sec_treasury", "Michael Torres", "Secretary of Treasury", "💰", 7, 5),
            CabinetMember("atty_general", "Jennifer Walsh", "Attorney General", "⚖️", 8, 6),
        ))
    }
}