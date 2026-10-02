package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val currentLevel: Int = 1,
    val maxUnlockedLevel: Int = 1,
    val coins: Int = 200,
    val lives: Int = 5,
    val lastLifeTimestampMs: Long = System.currentTimeMillis(),
    val selectedSkinId: String = "skin_black_classic",
    val selectedThemeId: String = "theme_emerald",
    val unlockedSkins: String = "skin_black_classic",
    val unlockedThemes: String = "theme_emerald",
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val selectedFolkSong: String = "song_1",
    val currentSongIndex: Int = 0,
    val customSongUri: String? = null,
    val isCustomSongActive: Boolean = false,
    val playerName: String = "বাঙালি তীরন্দাজ",
    val playerDistrict: String = "কিশোরগঞ্জ",
    val playerAvatarId: Int = 1,
    val lastDailyBonusClaimDate: String = "",
    val dailyBonusStreak: Int = 0,
    val totalPlaySeconds: Long = 0L
)

@Entity(tableName = "level_records")
data class LevelRecordEntity(
    @PrimaryKey val levelNumber: Int,
    val stars: Int = 3,
    val score: Int = 100,
    val timeTakenSeconds: Int = 0,
    val completedAt: Long = System.currentTimeMillis()
)
