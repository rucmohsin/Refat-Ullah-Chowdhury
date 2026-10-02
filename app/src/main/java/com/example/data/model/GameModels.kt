package com.example.data.model

enum class Direction(val angleDegrees: Float, val deltaRow: Int, val deltaCol: Int, val nameBn: String) {
    UP(270f, -1, 0, "উপর"),
    RIGHT(0f, 0, 1, "ডান"),
    DOWN(90f, 1, 0, "নিচ"),
    LEFT(180f, 0, -1, "বাম")
}

data class GridPoint(
    val row: Int,
    val col: Int
)

enum class ArrowStatus {
    NORMAL,
    WRONG_RED,
    CORRECT_GREEN,
    FLYING
}

data class ArrowMazeItem(
    val id: Int,
    val points: List<GridPoint>, // Vertices along the path from tail to head
    val headDirection: Direction,
    val isCleared: Boolean = false,
    val isFlying: Boolean = false,
    val isShaking: Boolean = false,
    val status: ArrowStatus = ArrowStatus.NORMAL,
    val flyOffset: Float = 0f
)

data class LevelConfig(
    val levelNumber: Int,
    val gridRows: Int,
    val gridCols: Int,
    val arrows: List<ArrowMazeItem>,
    val difficultyBn: String
)

data class ArrowSkin(
    val id: String,
    val nameBn: String,
    val descriptionBn: String,
    val priceCoins: Int,
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val isUnlocked: Boolean = false
)

data class BoardTheme(
    val id: String,
    val nameBn: String,
    val descriptionBn: String,
    val backgroundStartHex: Long,
    val backgroundEndHex: Long,
    val cardBackgroundHex: Long,
    val gridBorderHex: Long,
    val isUnlocked: Boolean = false,
    val priceCoins: Int = 0
)

data class MoralQuote(
    val id: Int,
    val quoteBn: String,
    val categoryBn: String,
    val lessonBn: String
)

data class LeaderboardEntry(
    val rank: Int,
    val nameBn: String,
    val level: Int,
    val score: Int,
    val stars: Int,
    val isPlayer: Boolean = false,
    val districtBn: String = "বাংলাদেশ",
    val scoreDisplay: String? = null,
    val starsDisplay: String? = null
)
