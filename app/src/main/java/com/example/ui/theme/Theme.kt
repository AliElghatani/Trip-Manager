package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Define Theme Colors for the 5 Color Palettes
// Palette 0: Cosmic Slate (Professional Polish Indigo-600)
private val CosmicPrimary = Color(0xFF4F46E5) // Indigo 600
private val CosmicSecondary = Color(0xFF6366F1) // Indigo 500
private val CosmicTertiary = Color(0xFF818CF8) // Indigo 400

// Palette 1: Desert Gold (Warm Gold & Sand)
private val DesertPrimary = Color(0xFFD4AF37)
private val DesertSecondary = Color(0xFFE5C158)
private val DesertTertiary = Color(0xFFF1DCA7)

// Palette 2: Emerald Pine (Rich Teal/Green)
private val EmeraldPrimary = Color(0xFF00A86B)
private val EmeraldSecondary = Color(0xFF40C594)
private val EmeraldTertiary = Color(0xFF8CE1C2)

// Palette 3: Royal Indigo (Navy Blue & Indigo)
private val RoyalPrimary = Color(0xFF6A5ACD)
private val RoyalSecondary = Color(0xFF8A7BE0)
private val RoyalTertiary = Color(0xFFB1A7F0)

// Palette 4: Crimson Sunset (Warm Terracotta & Coral)
private val CrimsonPrimary = Color(0xFFE04F5F)
private val CrimsonSecondary = Color(0xFFF07D89)
private val CrimsonTertiary = Color(0xFFF7ADB4)

@Composable
fun MyApplicationTheme(
    paletteIndex: Int = 0, // 0 to 4
    themeMode: String = "DARK", // LIGHT, DARK, DIM
    fontName: String = "Cairo", // Cairo, Dubai, Hacen Tunisia
    content: @Composable () -> Unit
) {
    // Determine base primary/secondary/tertiary colors based on selected palette
    val (primary, secondary, tertiary) = when (paletteIndex) {
        1 -> Triple(DesertPrimary, DesertSecondary, DesertTertiary)
        2 -> Triple(EmeraldPrimary, EmeraldSecondary, EmeraldTertiary)
        3 -> Triple(RoyalPrimary, RoyalSecondary, RoyalTertiary)
        4 -> Triple(CrimsonPrimary, CrimsonSecondary, CrimsonTertiary)
        else -> Triple(CosmicPrimary, CosmicSecondary, CosmicTertiary) // Palette 0: Cosmic Slate
    }

    // Determine colors based on Mode: LIGHT, DARK, or DIM
    val colorScheme: ColorScheme = when (themeMode) {
        "LIGHT" -> {
            lightColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primary.copy(alpha = 0.12f),
                onPrimaryContainer = primary,
                secondary = secondary,
                onSecondary = Color.White,
                tertiary = tertiary,
                onTertiary = Color.White,
                background = Color(0xFFF8F9FB), // Professional Polish light slate background
                onBackground = Color(0xFF0F172A), // Slate 900
                surface = Color.White,
                onSurface = Color(0xFF1E293B), // Slate 800
                surfaceVariant = Color(0xFFF1F5F9), // Slate 100
                onSurfaceVariant = Color(0xFF64748B), // Slate 500
                outline = Color(0xFFE2E8F0) // Slate 200
            )
        }
        "DIM" -> {
            darkColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primary.copy(alpha = 0.2f),
                onPrimaryContainer = secondary,
                secondary = secondary,
                onSecondary = Color.Black,
                tertiary = tertiary,
                onTertiary = Color.Black,
                background = Color(0xFF19222D), // Cozy deep slate background
                onBackground = Color(0xFFE9ECEF),
                surface = Color(0xFF222E3C), // Muted dark blue card surface
                onSurface = Color(0xFFF8F9FA),
                surfaceVariant = Color(0xFF2D3C4F),
                onSurfaceVariant = Color(0xFFDEE2E6),
                outline = Color(0xFF495057)
            )
        }
        else -> { // "DARK" - default
            darkColorScheme(
                primary = primary,
                onPrimary = Color.Black,
                primaryContainer = primary.copy(alpha = 0.25f),
                onPrimaryContainer = secondary,
                secondary = secondary,
                onSecondary = Color.Black,
                tertiary = tertiary,
                onTertiary = Color.Black,
                background = Color(0xFF0F1113), // Deep pitch black surface
                onBackground = Color(0xFFF1F3F5),
                surface = Color(0xFF181B1F), // Solid obsidian surface
                onSurface = Color(0xFFF1F3F5),
                surfaceVariant = Color(0xFF252930),
                onSurfaceVariant = Color(0xFFE9ECEF),
                outline = Color(0xFF3B424F)
            )
        }
    }

    // Select suitable base system fonts as beautiful fallback sans-serif
    // Since custom web fonts require a font provider, we can configure clean, professional text sizes
    val customFontFamily = FontFamily.SansSerif

    val customTypography = Typography(
        displayLarge = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.15.sp
        ),
        titleMedium = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        labelLarge = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelSmall = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        content = content
    )
}
