package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val isOnboarded: Boolean = false,
    val transportType: String = "",
    val transportDistance: String = "",
    val energyHabit: String = "",
    val energyUsage: String = "",
    val dietType: String = "",
    val dietDetails: String = "",
    val flightFreq: String = "",
    val flightDetails: String = "",
    val shoppingFreq: String = "",
    val shoppingDetails: String = "",
    val wasteHabit: String = "",
    val wasteDetails: String = "",
    val householdSize: Int = 1,
    val baselineCarbonDaily: Double = 15.0, // Baseline in kg CO2e per day
    val streakCount: Int = 0,
    val greenScore: Int = 70, // Improvement-based green score
    val points: Int = 0,
    val level: Int = 1
)

@Entity(tableName = "daily_track")
data class DailyTrackEntity(
    @PrimaryKey val dateString: String, // format: "yyyy-MM-dd"
    val carbonValue: Double, // calculated/updated value for this day in kg CO2e
    val hadChanges: Boolean,
    val changesListJson: String, // comma-separated or JSON list of selected changes
    val aiImpactComment: String // AI interpretation of the changes
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "Overall", "Transport", "Energy", "Diet", "Shopping", "Waste"
    val targetValue: Double, // Target emissions in kg CO2e per day/month
    val currentValue: Double, // Current actual emissions
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String, // e.g. "walking_warrior"
    val title: String,
    val description: String,
    val iconName: String, // material icon name or code
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String, // Transport, Diet, Energy, Shopping, Waste
    val target: Int,
    val progress: Int,
    val isCompleted: Boolean = false,
    val rewardPoints: Int = 50
)

@Entity(tableName = "chat_history")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "ai"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)
