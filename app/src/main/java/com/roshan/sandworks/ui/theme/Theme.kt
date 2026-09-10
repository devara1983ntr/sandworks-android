package com.roshan.sandworks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandOrange,
    onPrimary = Color.White,
    primaryContainer = BrandDeepOrange,
    onPrimaryContainer = Color.White,
    secondary = BrandSandGold,
    onSecondary = BrandDeepCharcoal,
    secondaryContainer = BrandSlate,
    onSecondaryContainer = BrandOffWhite,
    tertiary = BrandSandGold,
    onTertiary = BrandDeepCharcoal,
    background = BrandDeepCharcoal,
    onBackground = BrandOffWhite,
    surface = BrandGraphite,
    onSurface = BrandOffWhite,
    surfaceVariant = BrandSlate,
    onSurfaceVariant = BrandMuted,
    outline = BrandSteel,
    outlineVariant = BrandSlate,
    error = SemanticError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BrandOrange,
    onPrimary = Color.White,
    primaryContainer = BrandSandGold,
    onPrimaryContainer = BrandDeepCharcoal,
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightBorder,
    onSecondaryContainer = LightText,
    tertiary = BrandDeepOrange,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightBorder,
    onSurfaceVariant = LightSecondary,
    outline = LightBorder,
    outlineVariant = Color(0xFFC7CDD1),
    error = SemanticError,
    onError = Color.White
)

@Composable
fun SandWorksTheme(
    darkTheme: Boolean = true, // Dark-first industrial theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
