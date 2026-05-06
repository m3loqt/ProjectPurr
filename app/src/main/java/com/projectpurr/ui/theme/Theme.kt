package com.projectpurr.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Base ──────────────────────────────────────────────────────────────────────
val ColorBackground     = Color(0xFF0F0B08)   // deep warm black
val ColorSurface        = Color(0xFF1A1410)   // card base
val ColorSurfaceVariant = Color(0xFF241C15)   // elevated surface

// ── Amber accent ──────────────────────────────────────────────────────────────
val ColorPrimary        = Color(0xFFE09040)   // warm amber (cat-eye / candlelight)
val ColorPrimaryDim     = Color(0xFFA06428)   // pressed / secondary action
val ColorPrimaryGlow    = Color(0x26E09040)   // 15 % opacity — glow behind play button
val ColorOnPrimary      = Color(0xFF0F0B08)   // text on amber button

// ── Text ──────────────────────────────────────────────────────────────────────
val ColorOnBackground   = Color(0xFFF5EAD8)   // warm cream (primary text)
val ColorOnSurface      = Color(0xFFF0E0C8)   // text on cards
val ColorTextSecondary  = Color(0xFF9C8068)   // labels, metadata
val ColorTextTertiary   = Color(0xFF6A5040)   // placeholders, very dim

// ── Glass surface helpers (used in GlassCard) ─────────────────────────────────
val ColorGlassFill      = Color(0x12FFEEDD)   // warm white @ 7 %
val ColorGlassBorder    = Color(0x1FFFEEDD)   // warm white @ 12 %

// ── Scheme ────────────────────────────────────────────────────────────────────
private val PurrDark = darkColorScheme(
    primary             = ColorPrimary,
    onPrimary           = ColorOnPrimary,
    primaryContainer    = Color(0xFF3D2612),
    onPrimaryContainer  = ColorOnBackground,
    secondary           = ColorPrimaryDim,
    onSecondary         = ColorOnBackground,
    background          = ColorBackground,
    onBackground        = ColorOnBackground,
    surface             = ColorSurface,
    onSurface           = ColorOnSurface,
    surfaceVariant      = ColorSurfaceVariant,
    onSurfaceVariant    = ColorTextSecondary,
    outline             = ColorGlassBorder,
)

@Composable
fun ProjectPurrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PurrDark,
        typography  = PurrTypography,
        content     = content,
    )
}
