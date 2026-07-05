package com.apex.karting.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ApexRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBEE),
    onPrimaryContainer = ApexRedDark,
    secondary = Color(0xFF455A64),
    background = ApexBackground,
    surface = ApexSurface,
    onSurface = ApexOnSurface,
    error = ApexError,
    onError = Color.White
)

@Composable
fun ApexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}