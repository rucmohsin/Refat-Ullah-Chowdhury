package com.example.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.LevelRecordEntity
import com.example.data.local.UserProgressEntity
import com.example.data.model.ArrowMazeItem
import com.example.data.model.ArrowStatus
import com.example.data.model.LevelConfig
import com.example.data.model.MoralQuote
import com.example.data.model.MoralQuotesData
import com.example.data.model.SkinsAndThemes
import com.example.data.repository.GameRepository
import com.example.game.LevelGenerator
import com.example.game.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen {
    SPLASH,
    HOME,
    GAME,
    LEVEL_SELECT,
    SHOP,
    LEADERBOARD,
    MORAL_QUOTES
}

data class GameUiState(
    val currentScreen: Screen = Screen.SPLASH,
    val progress: UserProgressEntity = UserProgressEntity(),
    val currentLevel: Int = 1,
    val levelConfig: LevelConfig? = null,
    val arrows: List<ArrowMazeItem> = emptyList(),
    val lives: Int = 5,
    val nextLifeCountdownSec: Long = 0L,
    val isLevelComplete: Boolean = false,
    val lastRecord: LevelRecordEntity? = null,
    val lastMoralQuote: MoralQuote? = null,
    val isGameOver: Boolean = false,
    val isBreakReminderVisible: Boolean = false,
    val isDailyBonusDialogVisible: Boolean = false,
    val isProfileDialogVisible: Boolean = false,
    val dailyBonusStreak: Int = 0,
    val claimedBonusAmount: Int? = null,
    val sessionPlaySeconds: Long = 0L,
    val totalPlaySeconds: Long = 0L,
    val mistakesThisLevel: Int = 0,
    val levelStartTimeMs: Long = 0L,
    val completedLevelCount: Int = 0,
    val totalScore: Int = 0,
    val currentSongTitle: String = "১. ত্রিভুবনের প্রিয় মুহাম্মদ • কাজী নজরুল ইসলাম (গজল)"
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = GameRepository(database.gameDao())
    val soundManager = SoundManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var reminderShownThisSession = false
    private var lastBackPressTimeMs: Long = 0L

    init {
        // Song completion / change listener
        soundManager.onSongChangedListener = { index, title ->
            _uiState.update { it.copy(currentSongTitle = title) }
            if (index >= 0) {
                viewModelScope.launch {
                    repository.setSongIndex(index)
                }
            }
        }

        // Start music immediately upon app start!
        viewModelScope.launch {
            val prog = repository.getProgressOnce()
            soundManager.isSoundEnabled = prog.soundEnabled
            soundManager.isMusicEnabled = prog.musicEnabled
            if (prog.musicEnabled) {
                soundManager.ensureMusicPlaying(
                    startIndex = prog.currentSongIndex,
                    customUriString = prog.customSongUri,
                    preferCustom = prog.isCustomSongActive
                )
            }
        }

        // Observe progress from DB for UI state updates ONLY (DO NOT RESTART MUSIC)
        viewModelScope.launch {
            repository.userProgress.collectLatest { prog ->
                _uiState.update { current ->
                    current.copy(
                        progress = prog,
                        lives = prog.lives,
                        currentLevel = prog.currentLevel
                    )
                }
                soundManager.isSoundEnabled = prog.soundEnabled
                soundManager.isMusicEnabled = prog.musicEnabled
            }
        }

        // Observe total level records count & score
        viewModelScope.launch {
            repository.completedCount.collectLatest { count ->
                _uiState.update { it.copy(completedLevelCount = count) }
            }
        }
        viewModelScope.launch {
            repository.totalScore.collectLatest { score ->
                _uiState.update { it.copy(totalScore = score ?: 0) }
            }
        }

        // 5-second splash transition as requested:
        // "বাংলাদেশের পতাকা ও ম্যাপ শুরুতে ৫ সেকেন্ড স্থায়ী হবে আগে যা ১ সেকেন্ড ছিল।"
        viewModelScope.launch {
            delay(5000)
            _uiState.update { it.copy(currentScreen = Screen.HOME) }
        }

        startContinuousTimer()
    }

    private fun startContinuousTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    val newSessionSec = state.sessionPlaySeconds + 1
                    val newTotalSec = state.totalPlaySeconds + 1

                    // 1-hour continuous play alert (3600 seconds)
                    val showReminder = !reminderShownThisSession && newSessionSec >= 3600L
                    if (showReminder) {
                        reminderShownThisSession = true
                    }

                    // Next life countdown (10 minutes per life)
                    val nextSec = if (state.lives < 5) {
                        val elapsed = System.currentTimeMillis() - state.progress.lastLifeTimestampMs
                        val regenIntervalMs = 10 * 60 * 1000L
                        val remainingMs = maxOf(0L, regenIntervalMs - (elapsed % regenIntervalMs))
                        remainingMs / 1000L
                    } else {
                        0L
                    }

                    state.copy(
                        sessionPlaySeconds = newSessionSec,
                        totalPlaySeconds = newTotalSec,
                        isBreakReminderVisible = if (showReminder) true else state.isBreakReminderVisible,
                        nextLifeCountdownSec = nextSec
                    )
                }
            }
        }
    }

    fun dismissBreakReminder() {
        _uiState.update { it.copy(isBreakReminderVisible = false) }
    }

    fun navigateTo(screen: Screen) {
        soundManager.playClick()
        _uiState.update { it.copy(currentScreen = screen) }
    }

    /**
     * Handles double click within 1 second to return home:
     * "ব্যাক বাটনে ১ সেকেন্ডের ভিতরে ২ বার ক্লীক করলে গেমের হোমে চলে আসবে।"
     */
    fun handleBackNavigation(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTimeMs <= 1000L) {
            navigateTo(Screen.HOME)
            lastBackPressTimeMs = 0L
            return true
        } else {
            lastBackPressTimeMs = now
            return false
        }
    }

    fun loadLevel(levelNum: Int) {
        val config = LevelGenerator.generateLevel(levelNum)
        _uiState.update {
            it.copy(
                currentScreen = Screen.GAME,
                currentLevel = levelNum,
                levelConfig = config,
                arrows = config.arrows,
                isLevelComplete = false,
                isGameOver = false,
                mistakesThisLevel = 0,
                levelStartTimeMs = SystemClock.elapsedRealtime(),
                lastRecord = null,
                lastMoralQuote = null
            )
        }
        // Music remains uninterrupted!
    }

    fun onArrowClicked(arrow: ArrowMazeItem) {
        val currentState = _uiState.value
        val config = currentState.levelConfig ?: return
        if (currentState.isLevelComplete || currentState.isGameOver) return
        if (arrow.isCleared || arrow.isFlying || arrow.status != ArrowStatus.NORMAL) return

        val isClear = LevelGenerator.isArrowPathClear(
            arrow = arrow,
            allArrows = currentState.arrows,
            rows = config.gridRows,
            cols = config.gridCols
        )

        if (isClear) {
            // Correct click: Turn GREEN and slither out like a snake
            soundManager.onCorrectArrowLaunched()

            viewModelScope.launch {
                // Step 1: Turn Green immediately
                _uiState.update { state ->
                    val updated = state.arrows.map {
                        if (it.id == arrow.id) it.copy(status = ArrowStatus.CORRECT_GREEN, isFlying = true) else it
                    }
                    state.copy(arrows = updated)
                }

                // Step 2: Smooth snake slithering animation forward over 360ms
                val steps = 6
                for (step in 1..steps) {
                    delay(55)
                    val progress = step.toFloat() / steps.toFloat()
                    _uiState.update { state ->
                        val updated = state.arrows.map {
                            if (it.id == arrow.id) it.copy(flyOffset = progress) else it
                        }
                        state.copy(arrows = updated)
                    }
                }

                // Step 3: Arrow has exited the board, clear it
                _uiState.update { state ->
                    val updated = state.arrows.map {
                        if (it.id == arrow.id) it.copy(isCleared = true, isFlying = false, status = ArrowStatus.NORMAL) else it
                    }
                    state.copy(arrows = updated)
                }

                // Check victory condition
                val currentArrows = _uiState.value.arrows
                if (currentArrows.all { it.isCleared }) {
                    handleLevelWon()
                }
            }
        } else {
            // Wrong click: Turn RED, shake, deduct life (Music remains continuously playing!)
            soundManager.onWrongArrowBlocked()

            _uiState.update { state ->
                val updatedArrows = state.arrows.map {
                    if (it.id == arrow.id) it.copy(status = ArrowStatus.WRONG_RED, isShaking = true) else it
                }
                state.copy(
                    arrows = updatedArrows,
                    mistakesThisLevel = state.mistakesThisLevel + 1
                )
            }

            viewModelScope.launch {
                delay(350)
                _uiState.update { state ->
                    val updatedArrows = state.arrows.map {
                        if (it.id == arrow.id) it.copy(status = ArrowStatus.NORMAL, isShaking = false) else it
                    }
                    state.copy(arrows = updatedArrows)
                }
            }

            viewModelScope.launch {
                val remainingLives = repository.deductLife()
                if (remainingLives <= 0) {
                    _uiState.update { it.copy(isGameOver = true) }
                }
            }
        }
    }

    private fun handleLevelWon() {
        soundManager.onLevelWin()
        val state = _uiState.value
        val timeTakenSec = ((SystemClock.elapsedRealtime() - state.levelStartTimeMs) / 1000).toInt()
        val stars = when (state.mistakesThisLevel) {
            0 -> 3
            1, 2 -> 2
            else -> 1
        }
        val quote = MoralQuotesData.getRandomQuote()

        viewModelScope.launch {
            val record = repository.completeLevel(
                levelNumber = state.currentLevel,
                stars = stars,
                timeTakenSeconds = timeTakenSec
            )
            _uiState.update {
                it.copy(
                    isLevelComplete = true,
                    lastRecord = record,
                    lastMoralQuote = quote,
                    lives = 5
                )
            }
        }
        // Music remains uninterrupted!
    }

    fun restartCurrentLevel() {
        soundManager.playClick()
        val currentLvl = _uiState.value.currentLevel
        viewModelScope.launch {
            if (_uiState.value.lives <= 0) {
                repository.refillLives()
            }
            loadLevel(currentLvl)
        }
    }

    fun refillLivesWithCoins() {
        soundManager.playClick()
        viewModelScope.launch {
            val prog = _uiState.value.progress
            if (prog.coins >= 50) {
                repository.addCoins(-50)
                repository.refillLives()
                _uiState.update { it.copy(isGameOver = false) }
            }
        }
    }

    fun playNextLevel() {
        soundManager.playClick()
        val nextLevel = _uiState.value.currentLevel + 1
        if (nextLevel <= 20000) {
            loadLevel(nextLevel)
        }
    }

    fun openDailyBonus() {
        soundManager.playClick()
        _uiState.update { it.copy(isDailyBonusDialogVisible = true, claimedBonusAmount = null) }
    }

    fun claimDailyBonus() {
        soundManager.playClick()
        viewModelScope.launch {
            val amount = repository.claimDailyBonus()
            _uiState.update {
                it.copy(
                    claimedBonusAmount = amount,
                    dailyBonusStreak = it.progress.dailyBonusStreak
                )
            }
        }
    }

    fun closeDailyBonusDialog() {
        soundManager.playClick()
        _uiState.update { it.copy(isDailyBonusDialogVisible = false) }
    }

    // Profile Dialog Controls
    fun openProfileDialog() {
        soundManager.playClick()
        _uiState.update { it.copy(isProfileDialogVisible = true) }
    }

    fun closeProfileDialog() {
        soundManager.playClick()
        _uiState.update { it.copy(isProfileDialogVisible = false) }
    }

    fun saveUserProfile(name: String, district: String, avatarId: Int) {
        soundManager.playClick()
        viewModelScope.launch {
            repository.updateUserProfile(name, district, avatarId)
        }
    }

    fun buyOrSelectSkin(skinId: String, cost: Int) {
        soundManager.playClick()
        viewModelScope.launch {
            val unlocked = _uiState.value.progress.unlockedSkins.split(",").toSet()
            val skin = SkinsAndThemes.getSkinById(skinId)
            if (unlocked.contains(skinId) || skin.priceCoins == 0) {
                repository.selectSkin(skinId)
                _uiState.update { it.copy(progress = it.progress.copy(selectedSkinId = skinId)) }
            } else {
                val success = repository.purchaseSkin(skinId, cost)
                if (success) {
                    val updatedProg = repository.getProgressOnce()
                    _uiState.update { it.copy(progress = updatedProg) }
                }
            }
        }
    }

    fun buyOrSelectTheme(themeId: String, cost: Int) {
        soundManager.playClick()
        viewModelScope.launch {
            val unlocked = _uiState.value.progress.unlockedThemes.split(",").toSet()
            val theme = SkinsAndThemes.getThemeById(themeId)
            if (unlocked.contains(themeId) || theme.priceCoins == 0) {
                repository.selectTheme(themeId)
                _uiState.update { it.copy(progress = it.progress.copy(selectedThemeId = themeId)) }
            } else {
                val success = repository.purchaseTheme(themeId, cost)
                if (success) {
                    val updatedProg = repository.getProgressOnce()
                    _uiState.update { it.copy(progress = updatedProg) }
                }
            }
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            val current = _uiState.value.progress.soundEnabled
            repository.toggleSound(!current)
        }
    }

    fun toggleMusic() {
        viewModelScope.launch {
            val current = _uiState.value.progress.musicEnabled
            val newSetting = !current
            repository.toggleMusic(newSetting)
            if (!newSetting) {
                soundManager.stopMusic()
            } else {
                soundManager.resumeMusic()
            }
        }
    }

    fun nextMusicTrack() {
        soundManager.playNextSerialSong()
    }

    fun previousMusicTrack() {
        soundManager.playPreviousSerialSong()
    }

    fun selectFolkSongIndex(index: Int) {
        soundManager.playBuiltInSong(index)
        viewModelScope.launch {
            repository.setSongIndex(index)
        }
    }

    fun setCustomAudioSong(uriString: String?) {
        if (!uriString.isNullOrEmpty()) {
            soundManager.playCustomUri(uriString)
            viewModelScope.launch {
                repository.setCustomAudioUri(uriString)
            }
        }
    }

    fun purchaseCoinsPack(amount: Int) {
        soundManager.playClick()
        viewModelScope.launch {
            repository.addCoins(amount)
        }
    }

    fun onAppPaused() {
        soundManager.pauseMusic()
    }

    fun onAppResumed() {
        val prog = _uiState.value.progress
        if (prog.musicEnabled) {
            soundManager.resumeMusic()
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.stopMusic()
        timerJob?.cancel()
    }
}
