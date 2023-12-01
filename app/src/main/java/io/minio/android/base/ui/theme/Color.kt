package io.minio.android.base.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF386a20),
    secondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFF3F3F3),
    background = Color(0xFFF3F3F3),
)
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF386a20),
    secondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF000000),
    background = Color(0xFFF3F3F3),
)

@Composable
fun colorPrimary() = MaterialTheme.colorScheme.primary

@Composable
fun colorSecondary() = MaterialTheme.colorScheme.secondary

@Composable
fun colorTertiary() = MaterialTheme.colorScheme.tertiary

@Composable
fun colorBackground() = MaterialTheme.colorScheme.background

@Composable
fun colorGray() = Color.Gray

