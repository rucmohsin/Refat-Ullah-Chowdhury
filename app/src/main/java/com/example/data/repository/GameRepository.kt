package com.example.data.repository

import com.example.data.local.GameDao
import com.example.data.local.LevelRecordEntity
import com.example.data.local.UserProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

class GameRepository(private val dao: GameDao) {

    val userProgress: Flow<UserProgressEntity> = dao.getUserProgress().map { progress ->
        if (progress == null) {
            val initial = UserProgressEntity()
            dao.insertUserProgress(initial)
            initial
        } else {
            // Check life regeneration
            checkAndRegenLives(progress)
        }
    }

    val allLevelRecords: Flow<List<LevelRecordEntity>> = dao.getAllLevelRecords()
    val completedCount: Flow<Int> = dao.getCompletedLevelCount()
    val totalScore: Flow<Int?> = dao.getTotalScore()

    private suspend fun checkAndRegenLives(progress: UserProgressEntity): UserProgressEntity {
        if (progress.lives >= 5) {
            return progress
        }
        val now = System.currentTimeMillis()
        val regenIntervalMs = 10 * 60 * 1000L // 10 minutes
        val elapsed = now - progress.lastLifeTimestampMs
        val livesToAdd = (elapsed / regenIntervalMs).toInt()

        if (livesToAdd > 0) {
            val newLives = min(5, progress.lives + livesToAdd)
            val updatedTimestamp = if (newLives >= 5) {
                now
            } else {
                progress.lastLifeTimestampMs + (livesToAdd * regenIntervalMs)
            }
            val updated = progress.copy(
                lives = newLives,
                lastLifeTimestampMs = updatedTimestamp
            )
            dao.updateUserProgress(updated)
            return updated
        }
        return progress
    }

    suspend fun getProgressOnce(): UserProgressEntity {
        val current = dao.getUserProgressOnce() ?: run {
            val initial = UserProgressEntity()
            dao.insertUserProgress(initial)
            initial
        }
        return checkAndRegenLives(current)
    }

    suspend fun deductLife(): Int {
        val current = getProgressOnce()
        if (current.lives <= 0) return 0

        val newLives = current.lives - 1
        val updatedTimestamp = if (current.lives == 5) {
            System.currentTimeMillis()
        } else {
            current.lastLifeTimestampMs
        }
        val updated = current.copy(
            lives = newLives,
            lastLifeTimestampMs = updatedTimestamp
        )
        dao.updateUserProgress(updated)
        return newLives
    }

    suspend fun refillLives(): UserProgressEntity {
        val current = getProgressOnce()
        val updated = current.copy(
            lives = 5,
            lastLifeTimestampMs = System.currentTimeMillis()
        )
        dao.updateUserProgress(updated)
        return updated
    }

    suspend fun completeLevel(
        levelNumber: Int,
        stars: Int,
        timeTakenSeconds: Int
    ): LevelRecordEntity {
        val score = (stars * 100) + maxOf(0, 300 - timeTakenSeconds * 5)
        val coinsEarned = stars * 15

        val record = LevelRecordEntity(
            levelNumber = levelNumber,
            stars = stars,
            score = score,
            timeTakenSeconds = timeTakenSeconds,
            completedAt = System.currentTimeMillis()
        )
        dao.saveLevelRecord(record)

        val current = getProgressOnce()
        val newMax = if (levelNumber >= current.maxUnlockedLevel && levelNumber < 20000) {
            levelNumber + 1
        } else {
            current.maxUnlockedLevel
        }
        val nextLevel = min(20000, levelNumber + 1)

        // As specified: Level won refills to 5 lives for the next level!
        val updated = current.copy(
            currentLevel = nextLevel,
            maxUnlockedLevel = newMax,
            coins = current.coins + coinsEarned,
            lives = 5,
            lastLifeTimestampMs = System.currentTimeMillis()
        )
        dao.updateUserProgress(updated)
        return record
    }

    suspend fun selectLevel(levelNumber: Int) {
        val current = getProgressOnce()
        if (levelNumber <= current.maxUnlockedLevel) {
            dao.updateUserProgress(current.copy(currentLevel = levelNumber))
        }
    }

    suspend fun purchaseSkin(skinId: String, cost: Int): Boolean {
        val current = getProgressOnce()
        val unlocked = current.unlockedSkins.split(",").toMutableSet()
        if (unlocked.contains(skinId)) {
            // Already owned, just select
            dao.updateUserProgress(current.copy(selectedSkinId = skinId))
            return true
        }
        if (current.coins >= cost) {
            unlocked.add(skinId)
            dao.updateUserProgress(
                current.copy(
                    coins = current.coins - cost,
                    unlockedSkins = unlocked.joinToString(","),
                    selectedSkinId = skinId
                )
            )
            return true
        }
        return false
    }

    suspend fun selectSkin(skinId: String) {
        val current = getProgressOnce()
        dao.updateUserProgress(current.copy(selectedSkinId = skinId))
    }

    suspend fun purchaseTheme(themeId: String, cost: Int): Boolean {
        val current = getProgressOnce()
        val unlocked = current.unlockedThemes.split(",").toMutableSet()
        if (unlocked.contains(themeId)) {
            dao.updateUserProgress(current.copy(selectedThemeId = themeId))
            return true
        }
        if (current.coins >= cost) {
            unlocked.add(themeId)
            dao.updateUserProgress(
                current.copy(
                    coins = current.coins - cost,
                    unlockedThemes = unlocked.joinToString(","),
                    selectedThemeId = themeId
                )
            )
            return true
        }
        return false
    }

    suspend fun selectTheme(themeId: String) {
        val current = getProgressOnce()
        dao.updateUserProgress(current.copy(selectedThemeId = themeId))
    }

    suspend fun addCoins(amount: Int) {
        val current = getProgressOnce()
        dao.updateUserProgress(current.copy(coins = current.coins + amount))
    }

    suspend fun toggleSound(enabled: Boolean) {
        val current = getProgressOnce()
        dao.updateUserProgress(current.copy(soundEnabled = enabled))
    }

    suspend fun toggleMusic(enabled: Boolean) {
        val current = getProgressOnce()
        dao.updateUserProgress(current.copy(musicEnabled = enabled))
    }

    suspend fun setFolkSong(songId: String) {
        val current = getProgressOnce()
        dao.updateUserProgress(current.copy(selectedFolkSong = songId))
    }

    suspend fun setCustomAudioUri(uriString: String?) {
        val current = getProgressOnce()
        dao.updateUserProgress(
            current.copy(
                customSongUri = uriString,
                isCustomSongActive = !uriString.isNullOrEmpty()
            )
        )
    }

    suspend fun updateUserProfile(name: String, district: String, avatarId: Int) {
        val current = getProgressOnce()
        dao.updateUserProgress(
            current.copy(
                playerName = name.trim().ifEmpty { "বাঙালি তীরন্দাজ" },
                playerDistrict = district.trim().ifEmpty { "কিশোরগঞ্জ" },
                playerAvatarId = avatarId
            )
        )
    }

    suspend fun setSongIndex(index: Int) {
        val current = getProgressOnce()
        dao.updateUserProgress(current.copy(currentSongIndex = index, isCustomSongActive = false))
    }

    suspend fun claimDailyBonus(): Int {
        val current = getProgressOnce()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (current.lastDailyBonusClaimDate == today) {
            return 0 // Already claimed today
        }

        val newStreak = (current.dailyBonusStreak % 7) + 1
        val bonusCoins = newStreak * 50 // Day 1: 50, Day 2: 100, ..., Day 7: 350

        dao.updateUserProgress(
            current.copy(
                coins = current.coins + bonusCoins,
                lastDailyBonusClaimDate = today,
                dailyBonusStreak = newStreak
            )
        )
        return bonusCoins
    }

    suspend fun addPlayTime(seconds: Long) {
        val current = getProgressOnce()
        dao.updateUserProgress(current.copy(totalPlaySeconds = current.totalPlaySeconds + seconds))
    }
}
