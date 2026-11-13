package com.example.walk_a_living.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ExtraColors(
    val ticket: Color,
    val line: Color,
    val bad: Color,
    val accInk: Color,
    val mute: Color
)

val LocalExtraColors = staticCompositionLocalOf {
    ExtraColors(
        ticket = LightTicket,
        line = LightLine,
        bad = LightBad,
        accInk = LightAccInk,
        mute = LightMute
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkAcc,
    onPrimary = DarkAccInk,
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkCard,
    onSurface = DarkInk,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkMute,
    outline = DarkLine,
    error = DarkBad
)

private val LightColorScheme = lightColorScheme(
    primary = LightAcc,
    onPrimary = LightAccInk,
    background = LightBg,
    onBackground = LightInk,
    surface = LightCard,
    onSurface = LightInk,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightMute,
    outline = LightLine,
    error = LightBad
)

@Composable
fun WalkALivingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extraColors = if (darkTheme) {
        ExtraColors(
            ticket = DarkTicket,
            line = DarkLine,
            bad = DarkBad,
            accInk = DarkAccInk,
            mute = DarkMute
        )
    } else {
        ExtraColors(
            ticket = LightTicket,
            line = LightLine,
            bad = LightBad,
            accInk = LightAccInk,
            mute = LightMute
        )
    }

    CompositionLocalProvider(LocalExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
