package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)
}

@Dao
interface DailyTrackDao {
    @Query("SELECT * FROM daily_track ORDER BY dateString DESC")
    fun getAllHistoryFlow(): Flow<List<DailyTrackEntity>>

    @Query("SELECT * FROM daily_track WHERE dateString = :dateLimit ORDER BY dateString DESC LIMIT 1")
    suspend fun getTrackForDay(dateLimit: String): DailyTrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: DailyTrackEntity)

    @Query("DELETE FROM daily_track")
    suspend fun clearHistory()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals")
    fun getAllGoalsFlow(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Query("UPDATE goals SET currentValue = :valSelected WHERE id = :id")
    suspend fun updateProgress(id: Int, valSelected: Double)

    @Query("UPDATE goals SET isCompleted = :completed WHERE id = :id")
    suspend fun markCompleted(id: Int, completed: Boolean)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Int)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges")
    fun getUnlockedBadgesFlow(): Flow<List<BadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockBadge(badge: BadgeEntity)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges")
    fun getChallengesFlow(): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Query("UPDATE challenges SET progress = :prog, isCompleted = :comp WHERE id = :id")
    suspend fun updateProgress(id: String, prog: Int, comp: Boolean)
}

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getChatHistoryFlow(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_history")
    suspend fun clearChat()
}
