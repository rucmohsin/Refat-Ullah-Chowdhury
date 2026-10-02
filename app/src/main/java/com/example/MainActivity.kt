package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GameUiState
import com.example.ui.GameViewModel
import com.example.ui.Screen
import com.example.ui.dialogs.BreakReminderDialog
import com.example.ui.dialogs.DailyBonusDialog
import com.example.ui.dialogs.GameOverDialog
import com.example.ui.dialogs.LevelCompleteDialog
import com.example.ui.dialogs.MusicSelectDialog
import com.example.ui.dialogs.ProfileDialog
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.MoralQuotesScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MohsinArrowTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MohsinArrowTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    MainAppContent(
                        state = state,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppPaused()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResumed()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppPaused()
    }
}

@Composable
fun MainAppContent(
    state: GameUiState,
    viewModel: GameViewModel
) {
    var isMusicDialogOpen by remember { mutableStateOf(false) }

    // Screen Router
    when (state.currentScreen) {
        Screen.SPLASH -> {
            SplashScreen()
        }
        Screen.HOME -> {
            HomeScreen(
                state = state,
                onStartLevel = { lvl -> viewModel.loadLevel(lvl) },
                onNavigate = { screen -> viewModel.navigateTo(screen) },
                onOpenDailyBonus = { viewModel.openDailyBonus() },
                onOpenMusicDialog = { isMusicDialogOpen = true },
                onOpenProfileDialog = { viewModel.openProfileDialog() },
                onNextSong = { viewModel.nextMusicTrack() },
                onPreviousSong = { viewModel.previousMusicTrack() },
                onToggleSound = { viewModel.toggleSound() },
                onToggleMusic = { viewModel.toggleMusic() }
            )
        }
        Screen.GAME -> {
            GameScreen(
                state = state,
                onArrowClick = { arrow -> viewModel.onArrowClicked(arrow) },
                onRestart = { viewModel.restartCurrentLevel() },
                onBackToHome = { viewModel.navigateTo(Screen.HOME) },
                onToggleSound = { viewModel.toggleSound() },
                onToggleMusic = { viewModel.toggleMusic() },
                onNextMusicTrack = { viewModel.nextMusicTrack() },
                onPreviousMusicTrack = { viewModel.previousMusicTrack() }
            )
        }
        Screen.LEVEL_SELECT -> {
            LevelSelectScreen(
                state = state,
                onSelectLevel = { lvl -> viewModel.loadLevel(lvl) },
                onBackToHome = { viewModel.navigateTo(Screen.HOME) }
            )
        }
        Screen.SHOP -> {
            ShopScreen(
                state = state,
                onBuyOrSelectSkin = { id, cost -> viewModel.buyOrSelectSkin(id, cost) },
                onBuyOrSelectTheme = { id, cost -> viewModel.buyOrSelectTheme(id, cost) },
                onBuyCoins = { amount -> viewModel.purchaseCoinsPack(amount) },
                onBackToHome = { viewModel.navigateTo(Screen.HOME) }
            )
        }
        Screen.LEADERBOARD -> {
            LeaderboardScreen(
                state = state,
                onBackToHome = { viewModel.navigateTo(Screen.HOME) },
                onOpenProfile = { viewModel.openProfileDialog() }
            )
        }
        Screen.MORAL_QUOTES -> {
            MoralQuotesScreen(
                state = state,
                onBackToHome = { viewModel.navigateTo(Screen.HOME) }
            )
        }
    }

    // Level Complete Dialog
    if (state.isLevelComplete) {
        LevelCompleteDialog(
            levelNumber = state.currentLevel,
            record = state.lastRecord,
            moralQuote = state.lastMoralQuote,
            onNextLevel = { viewModel.playNextLevel() },
            onBackHome = { viewModel.navigateTo(Screen.HOME) }
        )
    }

    // Game Over Dialog (5 lives lost)
    if (state.isGameOver) {
        GameOverDialog(
            countdownSec = state.nextLifeCountdownSec,
            userCoins = state.progress.coins,
            onRestartLevel = { viewModel.restartCurrentLevel() },
            onRefillWithCoins = { viewModel.refillLivesWithCoins() },
            onBackHome = { viewModel.navigateTo(Screen.HOME) }
        )
    }

    // 1-Hour Continuous Play Reminder Dialog
    if (state.isBreakReminderVisible) {
        BreakReminderDialog(
            onDismiss = { viewModel.dismissBreakReminder() }
        )
    }

    // Daily Bonus Dialog
    if (state.isDailyBonusDialogVisible) {
        DailyBonusDialog(
            streak = state.progress.dailyBonusStreak,
            claimedAmount = state.claimedBonusAmount,
            onClaim = { viewModel.claimDailyBonus() },
            onDismiss = { viewModel.closeDailyBonusDialog() }
        )
    }

    // User Profile Dialog
    if (state.isProfileDialogVisible) {
        ProfileDialog(
            state = state,
            onSaveProfile = { name, district, avatarId ->
                viewModel.saveUserProfile(name, district, avatarId)
            },
            onDismiss = { viewModel.closeProfileDialog() }
        )
    }

    // 10 Real Songs Serial Music Dialog
    if (isMusicDialogOpen) {
        MusicSelectDialog(
            selectedSongIndex = state.progress.currentSongIndex,
            customUri = state.progress.customSongUri,
            isCustomActive = state.progress.isCustomSongActive,
            onSelectSongIndex = { index ->
                viewModel.selectFolkSongIndex(index)
                isMusicDialogOpen = false
            },
            onSetCustomAudio = { uri ->
                viewModel.setCustomAudioSong(uri)
                isMusicDialogOpen = false
            },
            onDismiss = { isMusicDialogOpen = false }
        )
    }
}
