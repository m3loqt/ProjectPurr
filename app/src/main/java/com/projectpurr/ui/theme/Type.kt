package com.projectpurr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type scale capturing the reference image's editorial style:
 *   - DisplayLarge: big, light-weight hero text (onboarding "Feel it." / "Not just sound.")
 *   - HeadlineLarge: section / screen titles
 *   - TitleLarge: card titles
 *   - Body / Label: unchanged from Material defaults in spirit, tuned for warmth
 *
 * System SansSerif (Roboto) carries Light (300) on Android and looks great at 52 sp —
 * matches the thin editorial feel of the reference without bundling custom font files.
 */
val PurrTypography = Typography(
    // ── Display — onboarding hero text ────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Light,       // 300 — thin, editorial
        fontSize      = 50.sp,
        lineHeight    = 58.sp,
        letterSpacing = (-0.5).sp,             // tighter at large sizes
    ),
    displayMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Light,
        fontSize      = 42.sp,
        lineHeight    = 48.sp,
        letterSpacing = (-0.25).sp,
    ),

    // ── Headlines ─────────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = 0.1.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 24.sp,
        lineHeight    = 30.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 20.sp,
        lineHeight    = 26.sp,
        letterSpacing = 0.sp,
    ),

    // ── Titles — card titles ───────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 18.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    titleMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 15.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // ── Body ──────────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // ── Labels — chips, buttons, metadata ─────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.5.sp,
    ),
)
