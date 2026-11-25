package com.tareasverde.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgBlack = Color(0xFF000000)
val CardDark = Color(0xFF0F0F0F)
val CardHi = Color(0xFF1A1A1A)
val GreenAccent = Color(0xFF00E676)
val GreenDeep = Color(0xFF12301F)
val TextMain = Color(0xFFEDEDED)
val TextDim = Color(0xFF8A8A8A)
val LineDim = Color(0xFF2A2A2A)
val Danger = Color(0xFFFF5252)

private val scheme = darkColorScheme(
    primary = GreenAccent,
    onPrimary = BgBlack,
    secondary = GreenAccent,
    background = BgBlack,
    onBackground = TextMain,
    surface = CardDark,
    onSurface = TextMain,
    surfaceVariant = CardHi,
    onSurfaceVariant = TextDim,
    outline = LineDim,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
