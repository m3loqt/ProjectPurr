package com.projectpurr.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Base ──────────────────────────────────────────────────────────────────────
val ColorBackground     = Color(0xFF0D0B0A)   // deeper charcoal-warm black
val ColorSurface        = Color(0xFF171210)   // softened card base
val ColorSurfaceVariant = Color(0xFF211A15)   // elevated surface

// ── Amber accent ──────────────────────────────────────────────────────────────
val ColorPrimary        = Color(0xFFC88848)   // dim candlelight amber (not bright orange)
val ColorPrimaryDim     = Color(0xFF8A5E30)   // pressed / secondary action
val ColorPrimaryGlow    = Color(0x1AC88848)   // restrained ambient glow
val ColorOnPrimary      = Color(0xFF0F0B08)   // text on amber button

// ── Sensory orb & moonlight (session centerpiece) ─────────────────────────────
val ColorOrbAmber       = Color(0xFFB87840)
val ColorOrbAmberDim    = Color(0xFF6E4828)
val ColorOrbGlow        = Color(0x38C88848)
val ColorMoonlitCream   = Color(0xFFE8DCC8)   // soft highlight — never pure white

// ── Text ──────────────────────────────────────────────────────────────────────
val ColorOnBackground   = Color(0xFFF5EAD8)   // warm cream (primary text)
val ColorOnSurface      = Color(0xFFF0E0C8)   // text on cards
val ColorTextSecondary  = Color(0xFFA28870)   // labels, metadata
val ColorTextTertiary   = Color(0xFF725748)   // placeholders, very dim

// ── Glass surface helpers (used in GlassCard) ─────────────────────────────────
val ColorGlassFill      = Color(0x14FFEED6)   // warm white @ ~8 %
val ColorGlassBorder    = Color(0x24FFEED6)   // warm white @ ~14 %

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
