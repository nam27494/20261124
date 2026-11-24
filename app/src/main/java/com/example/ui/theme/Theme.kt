package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SeniorSemanticColors(
    val ivoryBackground: Color = IvoryBackground,
    val ivoryCard: Color = IvorySurface,
    val ivoryHeader: Color = IvorySurfaceVariant,
    val warmBorder: Color = WarmOutline,
    val inkPrimary: Color = InkPrimary,
    val inkSecondary: Color = InkSecondary,
    val medDoneBg: Color = MedDoneGreen,
    val medDoneBorder: Color = MedDoneGreenDark,
    val medDoneBadgeBg: Color = MedDoneBadgeBg,
    val medDoneBadgeText: Color = MedDoneBadgeText,
    val medPendingBadgeBg: Color = MedPendingBadgeBg,
    val morningAccent: Color = MorningAmber,
    val lunchAccent: Color = LunchOrange,
    val dinnerAccent: Color = DinnerIndigo,
    val waterPrimary: Color = WaterBlue,
    val waterDark: Color = WaterBlueDark,
    val waterLight: Color = WaterBlueLight,
    val waterCupFill: Color = WaterCupFill,
    val waterCupFillTop: Color = WaterCupFillTop,
    val waterCupEmptyBg: Color = WaterCupEmptyBg,
    val waterCupEmptyOutline: Color = WaterCupEmptyOutline,
)

val LocalSeniorColors = staticCompositionLocalOf { SeniorSemanticColors() }

private val IvoryLightColorScheme = lightColorScheme(
    primary = MedDoneGreen,
    onPrimary = Color.White,
    primaryContainer = MedDoneBadgeBg,
    onPrimaryContainer = MedDoneGreenDark,
    secondary = WaterBlue,
    onSecondary = Color.White,
    secondaryContainer = WaterBlueLight,
    onSecondaryContainer = WaterBlueDark,
    background = IvoryBackground,
    onBackground = InkPrimary,
    surface = IvorySurface,
    onSurface = InkPrimary,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = WarmOutline,
)

@Composable
fun SeniorCareTheme(
    content: @Composable () -> Unit,
) {
    val semanticColors = SeniorSemanticColors()
    CompositionLocalProvider(LocalSeniorColors provides semanticColors) {
        MaterialTheme(
            colorScheme = IvoryLightColorScheme,
            typography = SeniorTypography,
            content = content,
        )
    }
}
