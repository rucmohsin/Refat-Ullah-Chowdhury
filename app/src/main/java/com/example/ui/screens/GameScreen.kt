package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArrowMazeItem
import com.example.data.model.SkinsAndThemes
import com.example.game.LevelGenerator
import com.example.ui.BanglaUtils
import com.example.ui.GameUiState
import com.example.ui.components.ArrowMazeBoard
import com.example.ui.components.TopBar
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    state: GameUiState,
    onArrowClick: (ArrowMazeItem) -> Unit,
    onRestart: () -> Unit,
    onBackToHome: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onNextMusicTrack: (() -> Unit)? = null,
    onPreviousMusicTrack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    var showBackToast by remember { mutableStateOf(false) }

    LaunchedEffect(showBackToast) {
        if (showBackToast) {
            delay(1200)
            showBackToast = false
        }
    }

    // Double tap within 1 second to return home:
    // "ব্যাক বাটনে ১ সেকেন্ডের ভিতরে ২ বার ক্লীক করলে গেমের হোমে চলে আসবে।"
    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTime <= 1000L) {
            onBackToHome()
        } else {
            lastBackPressTime = now
            showBackToast = true
        }
    }

    val config = state.levelConfig ?: return
    val theme = SkinsAndThemes.getThemeById(state.progress.selectedThemeId)
    val skin = SkinsAndThemes.getSkinById(state.progress.selectedSkinId)

    val remainingArrows = state.arrows.count { !it.isCleared }
    val totalArrows = state.arrows.size

    var hintedArrowId by remember(state.currentLevel) { mutableStateOf<Int?>(null) }

    val findFreeArrow = {
        val freeArrow = state.arrows.firstOrNull { arrow ->
            !arrow.isCleared && LevelGenerator.isArrowPathClear(
                arrow = arrow,
                allArrows = state.arrows,
                rows = config.gridRows,
                cols = config.gridCols
            )
        }
        hintedArrowId = freeArrow?.id
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(theme.backgroundStartHex),
                        Color(theme.backgroundEndHex)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            TopBar(
                lives = state.lives,
                countdownSec = state.nextLifeCountdownSec,
                coins = state.progress.coins,
                soundEnabled = state.progress.soundEnabled,
                musicEnabled = state.progress.musicEnabled,
                onToggleSound = onToggleSound,
                onToggleMusic = onToggleMusic,
                onBackClick = {
                    val now = System.currentTimeMillis()
                    if (now - lastBackPressTime <= 1000L) {
                        onBackToHome()
                    } else {
                        lastBackPressTime = now
                        showBackToast = true
                    }
                },
                title = "লেভেল ${BanglaUtils.toBanglaDigits(state.currentLevel)}"
            )

            // Audio Player Bar on Game Screen (10 Real Songs Serial Player)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64FFDA).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFF64FFDA),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.progress.musicEnabled) "গান চলছে: ${state.currentSongTitle}" else "গান বন্ধ রয়েছে",
                            color = if (state.progress.musicEnabled) Color(0xFF64FFDA) else Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onPreviousMusicTrack != null) {
                            IconButton(onClick = onPreviousMusicTrack, modifier = Modifier.size(26.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.SkipPrevious,
                                    contentDescription = "আগের গান",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        if (onNextMusicTrack != null) {
                            IconButton(onClick = onNextMusicTrack, modifier = Modifier.size(26.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = "পরের গান",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Level Info Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(theme.cardBackgroundHex).copy(alpha = 0.85f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "লেভেল ${BanglaUtils.toBanglaDigits(state.currentLevel)}",
                            color = Color(0xFFFFD54F),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = config.difficultyBn,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }

                    // Remaining Arrows Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(theme.gridBorderHex))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "অবশিষ্ট তীর: ",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${BanglaUtils.toBanglaDigits(remainingArrows)} / ${BanglaUtils.toBanglaDigits(totalArrows)}",
                                color = Color(0xFFFFD54F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Maze Game Board Area (White Background Canvas)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val maxW = maxWidth
                    val maxH = maxHeight
                    val aspectRatio = config.gridCols.toFloat() / config.gridRows.toFloat()

                    val (boardW, boardH) = if (maxW / maxH > aspectRatio) {
                        Pair(maxH * aspectRatio, maxH)
                    } else {
                        Pair(maxW, maxW / aspectRatio)
                    }

                    Surface(
                        modifier = Modifier
                            .size(boardW, boardH)
                            .testTag("arrow_maze_board_container"),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 6.dp,
                        border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFF1B1B1B))
                    ) {
                        ArrowMazeBoard(
                            arrows = state.arrows,
                            rows = config.gridRows,
                            cols = config.gridCols,
                            skin = skin,
                            hintedArrowId = hintedArrowId,
                            onArrowTapped = { arrow ->
                                hintedArrowId = null
                                onArrowClick(arrow)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                        )
                    }
                }
            }

            // Bottom Action Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hint Button
                    OutlinedButton(
                        onClick = findFreeArrow,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("hint_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFFD54F)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = "সহায়তা",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "সহায়তা",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Restart Level Button
                    Button(
                        onClick = onRestart,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("restart_level_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC62828)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "পুনরায় খেলুন",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "পুনরায় শুরু",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Double back press hint toast
        AnimatedVisibility(
            visible = showBackToast,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
            ) {
                Text(
                    text = "হোমে ফিরতে ১ সেকেন্ডের মধ্যে আবার ব্যাক চাপুন",
                    color = Color(0xFFFFD54F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
