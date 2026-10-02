package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldenAmber,
    secondary = MintAccent,
    tertiary = PalashRed,
    background = DarkEmerald,
    surface = SurfaceDark,
    onPrimary = Color(0xFF1B1B1B),
    onSecondary = Color(0xFF1B1B1B),
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DeepGold,
    secondary = EmeraldGreen,
    tertiary = PalashRed,
    background = DarkEmerald,
    surface = SurfaceDark,
    onPrimary = Color(0xFF1B1B1B),
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MohsinArrowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
