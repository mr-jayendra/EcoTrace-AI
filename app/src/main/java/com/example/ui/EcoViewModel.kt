package com.example.ui

import android.app.Application
import com.example.BuildConfig
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiClient
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EcoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = EcoRepository(db)

    // Exposed DB Flows
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val trackHistory: StateFlow<List<DailyTrackEntity>> = repository.allHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeGoals: StateFlow<List<GoalEntity>> = repository.allGoalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unlockedBadges: StateFlow<List<BadgeEntity>> = repository.unlockedBadgesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChallenges: StateFlow<List<ChallengeEntity>> = repository.challengesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatHistory: StateFlow<List<ChatMessageEntity>> = repository.chatHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Onboarding Temporary State Holders ---
    var onboardingIndex by mutableIntStateOf(0)
    var obTransportType by mutableStateOf("Car")
    var obTransportDistance by mutableStateOf("")
    var obEnergyHabit by mutableStateOf("Average")
    var obEnergyUsage by mutableStateOf("")
    var obDietType by mutableStateOf("Mixed")
    var obFlightFreq by mutableStateOf("Rare")
    var obShoppingFreq by mutableStateOf("Average")
    var obWasteHabit by mutableStateOf("Average")
    var obHouseholdSize by mutableIntStateOf(1)

    // --- Tracking Changes Multi-selection state ---
    val selectedDailyChanges = mutableStateListOf<String>()
    var trackingCompletedToday by mutableStateOf(false)

    // --- AI Coach Chat States ---
    var isCoachTyping by mutableStateOf(false)
    var coachError by mutableStateOf("")

    // --- Scenario Simulator States ---
    var simulatorInput by mutableStateOf("")
    var simulatorOutput by mutableStateOf("")
    var isSimulatorLoading by mutableStateOf(false)

    // --- AI Custom Recommendation (On Home) ---
    var homeRecommendation by mutableStateOf("Evaluating your tracking trends to prepare personalized options...")
    var isRecommendationLoading by mutableStateOf(false)

    // Ensure database contains core parameters
    init {
        viewModelScope.launch {
            repository.seedInitialData()
            checkIfTrackedToday()
            refreshHomeRecommendation()
        }
    }

    private suspend fun checkIfTrackedToday() {
        val todayStr = getTodayDateStr()
        val track = repository.getTrackForDate(todayStr)
        if (track != null && track.hadChanges) {
            // Already modified baseline status
            selectedDailyChanges.clear()
            selectedDailyChanges.addAll(track.changesListJson.split(",").filter { it.isNotEmpty() })
            trackingCompletedToday = true
        } else {
            trackingCompletedToday = false
        }
    }

    fun toggleDailyChange(activity: String) {
        if (selectedDailyChanges.contains(activity)) {
            selectedDailyChanges.remove(activity)
        } else {
            selectedDailyChanges.add(activity)
        }
    }

    /**
     * Submit today's activity modifications
     */
    fun submitDailyTrackChanges() {
        viewModelScope.launch {
            val todayStr = getTodayDateStr()
            val promptContext = buildCalculatorContext()
            
            var interpretation = ""
            // Call Gemini to explain the user's specific daily additions/subtractions in simple friendly terms if a key exists
            if (BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" && BuildConfig.GEMINI_API_KEY != "YOUR_API_KEY_HERE") {
                val actList = if (selectedDailyChanges.isEmpty()) "Standard Lifestyle Baseline" else selectedDailyChanges.joinToString(", ")
                val prompt = "Based on their environmental profile baseline ($promptContext), the user reported these daily activity changes: [$actList]. Provide a 1-sentence supportive, ultra-friendly carbon evaluation of how this affects their footprint today. Keep it motivating."
                interpretation = GeminiClient.generateResponse(prompt, systemInstruction = "You are an encouraging carbon footprint analyzer.")
            }

            repository.saveDailyTrack(todayStr, selectedDailyChanges.toList(), interpretation)
            trackingCompletedToday = true
            refreshHomeRecommendation()
        }
    }

    /**
     * Complete onboarding with currently held answers
     */
    fun completeOnboardingWorkflow() {
        viewModelScope.launch {
            val userProfile = UserProfileEntity(
                isOnboarded = true,
                transportType = obTransportType,
                transportDistance = obTransportDistance,
                energyHabit = obEnergyHabit,
                energyUsage = obEnergyUsage,
                dietType = obDietType,
                flightFreq = obFlightFreq,
                shoppingFreq = obShoppingFreq,
                wasteHabit = obWasteHabit,
                householdSize = obHouseholdSize
            )
            repository.completeOnboarding(userProfile)
            refreshHomeRecommendation()
        }
    }

    /**
     * Reset variables and restart onboarding baseline flow
     */
    fun resetAppWorkflow() {
        viewModelScope.launch {
            repository.clearHistory()
            onboardingIndex = 0
            obTransportType = "Car"
            obTransportDistance = ""
            obEnergyHabit = "Average"
            obEnergyUsage = ""
            obDietType = "Mixed"
            obFlightFreq = "Rare"
            obShoppingFreq = "Average"
            obWasteHabit = "Average"
            obHouseholdSize = 1
            selectedDailyChanges.clear()
            trackingCompletedToday = false
            homeRecommendation = "Complete onboarding setup to create personal advice!"
        }
    }

    /**
     * Trigger fresh AI coach recommended options
     */
    fun refreshHomeRecommendation() {
        viewModelScope.launch {
            val cachedProfile = repository.getProfile()
            if (cachedProfile == null || !cachedProfile.isOnboarded) return@launch
            isRecommendationLoading = true
            
            val promptContext = buildCalculatorContext()
            val prompt = "Standard Carbon Profile: $promptContext. Based on this profile, calculate their largest greenhouse category and write a friendly 1-sentence personalized tip under 20 words suggesting their easiest next action. Start the sentence directly with action, no fluff."
            
            homeRecommendation = GeminiClient.generateResponse(
                prompt = prompt,
                systemInstruction = "You are a concise lifestyle sustainability coach. Limit responses to 15-20 words."
            )
            isRecommendationLoading = false
        }
    }

    /**
     * Submit an interactive chat message to the coach
     */
    fun sendCoachMessage(userMsg: String) {
        if (userMsg.trim().isEmpty()) return
        
        viewModelScope.launch {
            // 1. Save user msg to local database
            repository.addChatMessage("user", userMsg)
            isCoachTyping = true
            coachError = ""

            // 2. Build history payload for context
            val profile = repository.getProfile()
            val trackerHistory = trackHistory.value.take(5).joinToString("\n") { 
                "Date: ${it.dateString}, CO2: ${it.carbonValue}kg, Changes: ${it.changesListJson}" 
            }
            
            val systemContextInstruction = """
                You are EcoTrace AI, a friendly, ultra-knowledgeable sustainability expert who behaves like an encouraging health app coach.
                The user has no sustainability knowledge and wants simple, motivating steps without jargon.
                User profile context:
                - Transport: ${profile?.transportType} (${profile?.transportDistance})
                - Diet: ${profile?.dietType}
                - Household size: ${profile?.householdSize}
                - Estimated Carbon Daily: ${profile?.baselineCarbonDaily} kg CO2e
                
                Recent 5 tracked days:
                $trackerHistory
                
                Keep answers conversational, positive, and structured with clean bullet points. Answer in under 3-4 sentences.
            """.trimIndent()

            // Run Gemini REST API call
            val responseText = GeminiClient.generateResponse(userMsg, systemInstruction = systemContextInstruction)
            
            // 3. Save response
            repository.addChatMessage("ai", responseText)
            isCoachTyping = false
        }
    }

    fun clearCoachChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            repository.addChatMessage("ai", "Hello! I am your AI Sustainability Coach. Ask me anything about your carbon footprint, how to complete challenges, or how to reduce emissions in your daily routines!")
        }
    }

    /**
     * Scenario Simulator action
     */
    fun runScenarioSimulation(query: String) {
        if (query.trim().isEmpty()) return
        viewModelScope.launch {
            isSimulatorLoading = true
            simulatorInput = query
            
            val profile = repository.getProfile()
            val baselineBreakdown = profile?.let { CarbonCalculator.calculateBaseline(it) }
            val breakdownDescription = baselineBreakdown?.let {
                "Daily emissions: Total: ${it.totalDaily}kg. Transport: ${it.transport}kg, Energy: ${it.energy}kg, Diet: ${it.diet}kg, Shopping: ${it.shopping}kg, Waste: ${it.waste}kg."
            } ?: "Baseline is unavailable."

            val prompt = """
                The user asks: "$query"
                Their carbon baseline is: $breakdownDescription
                
                Calculate exactly what reductions/increases in kg CO2e this change represents. Explain it mathematically yet simply, and show the approximate percent reduction from their daily/weekly total. Keep the message highly encouraging, educational, and suitable for non-sustainability experts. Limit description to a clean, readable layout with bold numbers under 80 words.
            """.trimIndent()

            simulatorOutput = GeminiClient.generateResponse(prompt, systemInstruction = "You are a friendly environmental math simulator.")
            isSimulatorLoading = false
        }
    }

    // --- Goal-adding interface ---
    fun createGoal(category: String, target: Double, title: String) {
        viewModelScope.launch {
            repository.addGoal(
                GoalEntity(
                    category = category,
                    targetValue = target,
                    currentValue = 0.0,
                    title = title
                )
            )
        }
    }

    fun removeGoal(id: Int) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    // Helper functions
    fun getTodayDateStr(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun buildCalculatorContext(): String {
        return "Transport: $obTransportType ($obTransportDistance), Energy: $obEnergyHabit ($obEnergyUsage), Diet: $obDietType, Flights: $obFlightFreq, Shopping: $obShoppingFreq, Household: $obHouseholdSize people."
    }
}
