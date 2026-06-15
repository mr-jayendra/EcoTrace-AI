package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EcoRepository(private val db: AppDatabase) {

    val userProfileFlow: Flow<UserProfileEntity?> = db.userProfileDao().getProfileFlow()
    val allHistoryFlow: Flow<List<DailyTrackEntity>> = db.dailyTrackDao().getAllHistoryFlow()
    val allGoalsFlow: Flow<List<GoalEntity>> = db.goalDao().getAllGoalsFlow()
    val unlockedBadgesFlow: Flow<List<BadgeEntity>> = db.badgeDao().getUnlockedBadgesFlow()
    val challengesFlow: Flow<List<ChallengeEntity>> = db.challengeDao().getChallengesFlow()
    val chatHistoryFlow: Flow<List<ChatMessageEntity>> = db.chatHistoryDao().getChatHistoryFlow()

    suspend fun getProfile(): UserProfileEntity? {
        return db.userProfileDao().getProfile()
    }

    suspend fun saveProfile(profile: UserProfileEntity) {
        db.userProfileDao().saveProfile(profile)
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        db.userProfileDao().updateProfile(profile)
    }

    /**
     * Complete Onboarding & Save custom baseline
     */
    suspend fun completeOnboarding(profile: UserProfileEntity) {
        val breakdown = CarbonCalculator.calculateBaseline(profile)
        val updatedProfile = profile.copy(
            isOnboarded = true,
            baselineCarbonDaily = breakdown.totalDaily,
            greenScore = 70,
            points = 20, // baseline onboarding points
            level = 1
        )
        db.userProfileDao().saveProfile(updatedProfile)

        // Seed initial history item for today
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db.dailyTrackDao().insertTrack(
            DailyTrackEntity(
                dateString = todayStr,
                carbonValue = breakdown.totalDaily,
                hadChanges = false,
                changesListJson = "",
                aiImpactComment = "Baseline carbon daily footprint calculated at ${String.format("%.1f", breakdown.totalDaily)} kg CO₂e. Welcome to EcoTrace!"
            )
        )

        // Preseed initial challenges and badges
        seedInitialData()
    }

    /**
     * Retrieve daily track info, or return baseline values default if not yet stored
     */
    suspend fun getTrackForDate(dateStr: String): DailyTrackEntity? {
        return db.dailyTrackDao().getTrackForDay(dateStr)
    }

    /**
     * Inserts/Updates a track entry for a specific day
     */
    suspend fun saveDailyTrack(dateStr: String, changes: List<String>, comment: String = "") {
        val profile = getProfile() ?: return
        val baselineBreakdown = CarbonCalculator.calculateBaseline(profile)
        val calculatedEmissions = CarbonCalculator.calculateDailyEmissions(baselineBreakdown, changes)

        val trackEntity = DailyTrackEntity(
            dateString = dateStr,
            carbonValue = calculatedEmissions,
            hadChanges = changes.isNotEmpty(),
            changesListJson = changes.joinToString(","),
            aiImpactComment = comment.ifEmpty {
                if (changes.isEmpty()) {
                    "You operated on your standard lifestyle carbon baseline of ${String.format("%.1f", baselineBreakdown.totalDaily)} kg CO₂e."
                } else {
                    val saved = baselineBreakdown.totalDaily - calculatedEmissions
                    if (saved > 0) {
                        "Excellent changes today! You saved ${String.format("%.1f", saved)} kg CO₂e compared to your standard baseline."
                    } else if (saved < 0) {
                        "Your activities today added ${String.format("%.1f", -saved)} kg CO₂e above your standard carbon baseline. Keep tracking and balancing!"
                    } else {
                        "Modified baseline calculated. Keep logging actions!"
                    }
                }
            }
        )
        db.dailyTrackDao().insertTrack(trackEntity)

        // Update gamification points, score, levels, challenges & badge checks
        processDailyGamificationUpdates(changes)
    }

    /**
     * Seed first-launch database entries (Profile, Coach Chat Greeting, Challenges)
     */
    suspend fun seedInitialData() {
        // 1. Seed default user profile if none exists so the app never gets stuck on the loading spinner
        if (db.userProfileDao().getProfile() == null) {
            db.userProfileDao().saveProfile(UserProfileEntity())
        }

        // 2. Seed initial supportive coach introduction bubble
        val currentChat = db.chatHistoryDao().getChatHistoryFlow().firstOrNull()
        if (currentChat.isNullOrEmpty()) {
            db.chatHistoryDao().insertMessage(
                ChatMessageEntity(
                    sender = "ai",
                    messageText = "Hello! I am your EcoTrace AI Coach. Ask me anything about tracking your emissions, reducing energy consumption, completing weekly challenges, or improving your standard Green Score!"
                )
            )
        }

        // 3. Seed default challenges
        val current = db.challengeDao().getChallengesFlow().firstOrNull()
        if (current.isNullOrEmpty()) {
            val list = listOf(
                ChallengeEntity("ch_walk", "Walking Warrior", "Walk or cycle instead of driving", "Transport", 3, 0),
                ChallengeEntity("ch_bus", "Green Commuter", "Take a bus or train to work/school", "Transport", 2, 0),
                ChallengeEntity("ch_energy", "Energy Miser", "Reduce home device electricity use", "Energy", 4, 0),
                ChallengeEntity("ch_veg", "Plant-Based Power", "Eat plant-focused vegetarian meals", "Diet", 3, 0),
                ChallengeEntity("ch_shop", "Mindful Shopper", "Postpone an unnecessary clothes/tech buy", "Shopping", 1, 0)
            )
            db.challengeDao().insertChallenges(list)
        }
    }

    /**
     * Process updates to Green Score, Challenges, Points, and Badge triggers
     */
    private suspend fun processDailyGamificationUpdates(changes: List<String>) {
        val profile = getProfile() ?: return
        var additionalPoints = 10 // tracking logging bonus points
        var trackedSomethingPositive = false

        // Load challenges
        val activeChallenges = db.challengeDao().getChallengesFlow().firstOrNull() ?: emptyList()

        for (change in changes) {
            when (change) {
                "Walked or cycled" -> {
                    trackedSomethingPositive = true
                    updateChallengeProgress("ch_walk", activeChallenges)
                }
                "Used public transport" -> {
                    trackedSomethingPositive = true
                    updateChallengeProgress("ch_bus", activeChallenges)
                }
                "Ate vegetarian meals" -> {
                    trackedSomethingPositive = true
                    updateChallengeProgress("ch_veg", activeChallenges)
                }
                "Higher electricity usage" -> {
                    // negative habit
                }
                "Large shopping purchase" -> {
                    // shopping
                }
                "Other custom activity" -> {
                    additionalPoints += 5
                }
            }
        }

        // Standard positive action point bonuses
        if (trackedSomethingPositive) {
            additionalPoints += 15
        }

        val currentStreak = profile.streakCount + 1
        val pointsCombined = profile.points + additionalPoints
        val targetLevel = (pointsCombined / 250) + 1
        var badgeToUnlock: BadgeEntity? = null

        // Check badge triggers
        if (currentStreak >= 3) {
            badgeToUnlock = BadgeEntity(
                id = "streak_master",
                title = "Habit Crusader",
                description = "Tracked 3 days consecutive baseline profiles!",
                iconName = "LocalFireDepartment"
            )
        } else if (profile.points == 0) {
            badgeToUnlock = BadgeEntity(
                id = "new_eco_badge",
                title = "Eco Beginner",
                description = "Completed profile baseline onboarding setup!",
                iconName = "Eco"
            )
        }

        // Adjust Green Score based on tracking density and positive habits
        // Green score considers tracking habits and reduction history
        var calculatedScore = profile.greenScore
        if (trackedSomethingPositive) {
            calculatedScore = (calculatedScore + 3).coerceAtMost(100)
        } else if (changes.isNotEmpty()) {
            // standard tracking
            calculatedScore = (calculatedScore + 1).coerceAtMost(100)
        }

        // Save updated profile
        saveProfile(profile.copy(
            streakCount = currentStreak,
            points = pointsCombined,
            level = targetLevel,
            greenScore = calculatedScore
        ))

        // Save badges
        badgeToUnlock?.let {
            db.badgeDao().unlockBadge(it)
        }
    }

    private suspend fun updateChallengeProgress(id: String, activeChallenges: List<ChallengeEntity>) {
        val ch = activeChallenges.find { it.id == id } ?: return
        if (ch.isCompleted) return

        val nextProg = ch.progress + 1
        val isNowComp = nextProg >= ch.target
        db.challengeDao().updateProgress(id, nextProg, isNowComp)

        if (isNowComp) {
            // award reward points
            val profile = getProfile() ?: return
            val updated = profile.copy(
                points = profile.points + ch.rewardPoints,
                greenScore = (profile.greenScore + 5).coerceAtMost(100)
            )
            db.userProfileDao().saveProfile(updated)

            // Unlock a challenge completion badge
            val challengeBadge = BadgeEntity(
                id = "badge_${ch.id}",
                title = "${ch.title} Hero",
                description = "Successfully finished challenge: '${ch.title}'",
                iconName = "Star"
            )
            db.badgeDao().unlockBadge(challengeBadge)
        }
    }

    // Goal handlers
    suspend fun addGoal(goal: GoalEntity) {
        db.goalDao().insertGoal(goal)
    }

    suspend fun deleteGoal(id: Int) {
        db.goalDao().deleteGoal(id)
    }

    // Chat handlers
    suspend fun clearHistory() {
        db.dailyTrackDao().clearHistory()
        val profile = getProfile()
        if (profile != null) {
            saveProfile(profile.copy(isOnboarded = false, points = 0, level = 1, streakCount = 0))
        }
    }

    suspend fun addChatMessage(sender: String, messageText: String) {
        db.chatHistoryDao().insertMessage(ChatMessageEntity(sender = sender, messageText = messageText))
    }

    suspend fun clearChatHistory() {
        db.chatHistoryDao().clearChat()
    }
}
