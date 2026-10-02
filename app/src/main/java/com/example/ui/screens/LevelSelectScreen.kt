package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SkinsAndThemes
import com.example.ui.BanglaUtils
import com.example.ui.GameUiState
import kotlin.math.max
import kotlin.math.min

@Composable
fun LevelSelectScreen(
    state: GameUiState,
    onSelectLevel: (Int) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackToHome()
    }

    val theme = SkinsAndThemes.getThemeById(state.progress.selectedThemeId)
    val maxUnlocked = state.progress.maxUnlockedLevel

    // Paged in chunks of 50 levels
    val initialPage = (state.currentLevel - 1) / 50
    var currentPage by remember { mutableIntStateOf(initialPage) }

    val startLevel = currentPage * 50 + 1
    val endLevel = min(20000, startLevel + 49)
    val totalPages = (20000 + 49) / 50

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(theme.backgroundStartHex), Color(theme.backgroundEndHex))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(theme.cardBackgroundHex).copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier.testTag("level_select_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "হোমে ফিরুন",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "লেভেল নির্বাচন (১ হতে ২০,০০০)",
                            color = Color(0xFFFFD54F),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "সর্বোচ্চ উন্মুক্ত: লেভেল ${BanglaUtils.toBanglaDigits(maxUnlocked)}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Page Selector / Quick Navigator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentPage > 0) currentPage-- },
                    enabled = currentPage > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "আগের পৃষ্ঠা",
                        tint = if (currentPage > 0) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(theme.gridBorderHex))
                ) {
                    Text(
                        text = "লেভেল ${BanglaUtils.toBanglaDigits(startLevel)} - ${BanglaUtils.toBanglaDigits(endLevel)}",
                        color = Color(0xFFFFD54F),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = { if (currentPage < totalPages - 1) currentPage++ },
                    enabled = currentPage < totalPages - 1
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "পরের পৃষ্ঠা",
                        tint = if (currentPage < totalPages - 1) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            // Quick Jump to Current Level button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        currentPage = (state.currentLevel - 1) / 50
                    },
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
                ) {
                    Text(
                        text = "বর্তমান লেভেলে যান",
                        color = Color(0xFFFFD54F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Grid of 50 Levels
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val levels = (startLevel..endLevel).toList()
                items(levels.size) { index ->
                    val lvl = levels[index]
                    val isUnlocked = lvl <= maxUnlocked
                    val isCurrent = lvl == state.currentLevel

                    LevelTile(
                        levelNumber = lvl,
                        isUnlocked = isUnlocked,
                        isCurrent = isCurrent,
                        themeColor = Color(theme.cardBackgroundHex),
                        onClick = {
                            if (isUnlocked) {
                                onSelectLevel(lvl)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelTile(
    levelNumber: Int,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    themeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(62.dp)
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("level_tile_$levelNumber"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrent -> Color(0xFFFFC107)
                isUnlocked -> themeColor.copy(alpha = 0.9f)
                else -> Color.Black.copy(alpha = 0.35f)
            }
        ),
        border = when {
            isCurrent -> androidx.compose.foundation.BorderStroke(2.dp, Color.White)
            isUnlocked -> androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            else -> null
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 3.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isUnlocked) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = BanglaUtils.toBanglaDigits(levelNumber),
                        color = if (isCurrent) Color(0xFF1B1B1B) else Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Stars indicator
                    Row {
                        repeat(3) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (isCurrent) Color(0xFF1B1B1B) else Color(0xFFFFD54F),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "লক করা",
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
