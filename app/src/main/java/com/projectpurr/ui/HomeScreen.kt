package com.projectpurr.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectpurr.R
import com.projectpurr.ui.components.AtmosphericScene
import com.projectpurr.ui.components.CinematicTextureOverlay
import com.projectpurr.ui.theme.ColorMoonlitCream
import com.projectpurr.ui.theme.ColorOnPrimary
import com.projectpurr.ui.theme.ColorPrimary
import com.projectpurr.ui.theme.ColorPrimaryDim
import com.projectpurr.ui.theme.ColorTextSecondary
import com.projectpurr.ui.theme.ColorTextTertiary
import java.util.Calendar

// ── Shared tokens ─────────────────────────────────────────────────────────────

private val CardBg      = Color(0xFF191612)   // dark warm surface matching inspo
private val CardShape   = RoundedCornerShape(18.dp)
private val DAY_LABELS  = listOf("M", "T", "W", "T", "F", "S", "S")
private val BAR_HEIGHTS = listOf(0.32f, 0.52f, 0.40f, 0.66f, 0.48f, 0.86f, 1.00f)

// ── Root ──────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    sessionCount: Int,
    onSelectHouseCat: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onPreviewHaptic: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val greeting = remember {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            h < 12 -> "Good morning,"
            h < 17 -> "Good afternoon,"
            else   -> "Good evening,"
        }
    }
    val subtext = when {
        sessionCount == 0 -> "Ready to rest?"
        sessionCount == 1 -> "You returned."
        sessionCount < 7  -> "Your companion is waiting."
        else              -> "House Cat knows your evenings."
    }

    val entryAlpha by animateFloatAsState(1f, tween(900), label = "homeFade")

    Box(Modifier.fillMaxSize().alpha(entryAlpha)) {
        AtmosphericScene(heroImageRes = R.drawable.herobg, heroAlpha = 0.62f) {}
        CinematicTextureOverlay()

        Scaffold(
            containerColor       = Color.Transparent,
            contentWindowInsets  = WindowInsets(0),
            bottomBar = {
                FloatingNavBar(
                    onHome      = {},
                    onSessions  = onSelectHouseCat,
                    onFavorites = {},
                    onProfile   = onNavigateToSettings,
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier        = Modifier.fillMaxSize().padding(innerPadding).statusBarsPadding(),
                contentPadding  = PaddingValues(bottom = 16.dp),
            ) {
                item { GreetingHeader(greeting, subtext, onNavigateToSettings) }
                item { Spacer(Modifier.height(14.dp)) }
                item { HeroCard(onSelectHouseCat) }
                item { Spacer(Modifier.height(16.dp)) }
                item { RhythmSection(sessionCount) }
                item { Spacer(Modifier.height(12.dp)) }
                item { StatCardsRow(sessionCount) }
                item { Spacer(Modifier.height(12.dp)) }
                item { CompanionCard(sessionCount) }
            }
        }
    }
}

// ── Greeting ──────────────────────────────────────────────────────────────────

@Composable
private fun GreetingHeader(
    greeting: String,
    subtext: String,
    onNavigateToSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = greeting,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize   = 30.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 36.sp,
                ),
                color = ColorMoonlitCream,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = subtext,
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTextSecondary.copy(alpha = 0.78f),
            )
        }
        // Circled settings gear (matches inspo)
        Box(
            modifier = Modifier
                .size(42.dp)
                .border(1.dp, ColorMoonlitCream.copy(alpha = 0.28f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onNavigateToSettings,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector     = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint            = ColorMoonlitCream.copy(alpha = 0.68f),
                modifier        = Modifier.size(18.dp),
            )
        }
    }
}

// ── Hero Card ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(onSelectHouseCat: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(270.dp)
            .clip(RoundedCornerShape(22.dp)),
    ) {
        Image(
            painter            = painterResource(R.drawable.herocard),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize(),
        )

        // Left-heavy darkness so text is legible
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xF2100E0C),
                        0.46f to Color(0xB8100E0C),
                        0.70f to Color(0x70100E0C),
                        1.00f to Color(0x1C100E0C),
                    ),
                ),
            ),
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0x44100E0C),
                        0.40f to Color.Transparent,
                        0.70f to Color.Transparent,
                        1.00f to Color(0xCC100E0C),
                    ),
                ),
            ),
        )

        Column(
            modifier            = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Column {
                HouseCatBadge()
                Spacer(Modifier.height(10.dp))
                // Headline — 34 sp matches inspo proportions
                Text(
                    text       = "Soft purrs.\nDeeper rest.",
                    style      = MaterialTheme.typography.displayMedium.copy(
                        fontSize   = 34.sp,
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color      = ColorMoonlitCream,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "Calm your mind and body with\nreal feline purring.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                    color = ColorTextSecondary.copy(alpha = 0.82f),
                )
                Spacer(Modifier.height(16.dp))

                // Wide CTA pill + filter circle right-aligned
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFFBB7C34))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = onSelectHouseCat,
                            )
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector        = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint               = ColorOnPrimary,
                                modifier           = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text  = "Begin Session",
                                style = MaterialTheme.typography.labelLarge,
                                color = ColorOnPrimary,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .border(1.dp, ColorMoonlitCream.copy(alpha = 0.30f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Tune,
                            contentDescription = "Options",
                            tint               = ColorMoonlitCream.copy(alpha = 0.62f),
                            modifier           = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HouseCatBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xBB0F0D0B))
            .border(0.5.dp, ColorMoonlitCream.copy(alpha = 0.20f), RoundedCornerShape(50.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text  = "House Cat",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
            color = ColorMoonlitCream.copy(alpha = 0.90f),
        )
    }
}

// ── Rhythm Section ────────────────────────────────────────────────────────────

@Composable
private fun RhythmSection(sessionCount: Int) {
    val todayIndex = remember {
        // Calendar: Sun=1 Mon=2 … Sat=7 → map Mon=0 … Sun=6
        (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    // Horizontal layout: text left, day grid right — exactly as inspo
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(CardShape)
            .background(CardBg)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: rhythm text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = "Your rhythm",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = ColorTextSecondary.copy(alpha = 0.60f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "${sessionCount.coerceAtLeast(0)} nights",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 28.sp,
                ),
                color = ColorPrimary,
            )
        }

        Spacer(Modifier.width(16.dp))

        // Right: M T W T F S S labels + dots beneath
        Column(horizontalAlignment = Alignment.End) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DAY_LABELS.forEach { d ->
                    Text(
                        text      = d,
                        style     = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color     = ColorTextTertiary.copy(alpha = 0.50f),
                        modifier  = Modifier.width(22.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DAY_LABELS.forEachIndexed { idx, _ ->
                    val isToday = idx == todayIndex
                    val filled  = idx <= todayIndex && (todayIndex - idx) < sessionCount.coerceAtMost(7)
                    DayDot(isToday = isToday, filled = filled, size = 22.dp)
                }
            }
        }
    }
}

@Composable
private fun DayDot(isToday: Boolean, filled: Boolean, size: Dp = 22.dp) {
    when {
        filled -> Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(if (isToday) ColorPrimary else ColorPrimaryDim.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", color = ColorOnPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        else   -> Box(
            modifier = Modifier
                .size(size)
                .border(1.dp, ColorTextTertiary.copy(alpha = 0.30f), CircleShape),
        )
    }
}

// ── Stat Cards — equal height, darker, with charts ────────────────────────────

@Composable
private fun StatCardsRow(sessionCount: Int) {
    val totalMin = sessionCount * 47
    val timeStr  = when {
        totalMin == 0  -> "0m"
        totalMin < 60  -> "${totalMin}m"
        totalMin < 600 -> "${totalMin / 60}h ${totalMin % 60}m"
        else           -> "${totalMin / 60}h"
    }

    // Fixed height so both cards are always equal (matching inspo)
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(175.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Sessions card
        Column(
            modifier            = Modifier.weight(1f).fillMaxSize().clip(CardShape).background(CardBg).padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                // Value (left) + Icon (right)
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        "$sessionCount",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp, fontWeight = FontWeight.Bold),
                        color = ColorMoonlitCream,
                    )
                    IconCircle(icon = Icons.Filled.NightsStay)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Sessions this week",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
                    color = ColorTextSecondary.copy(alpha = 0.68f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "You're building a\nbeautiful rest habit.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 15.sp),
                    color = ColorTextSecondary.copy(alpha = 0.62f),
                )
            }
            BarChart(sessionCount = sessionCount)
        }

        // Time card
        Column(
            modifier            = Modifier.weight(1f).fillMaxSize().clip(CardShape).background(CardBg).padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                // Value (left) + Icon (right)
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        timeStr,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize   = if (timeStr.length > 6) 26.sp else 34.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = ColorMoonlitCream,
                    )
                    IconCircle(icon = Icons.Filled.AccessTime)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Total rest time",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
                    color = ColorTextSecondary.copy(alpha = 0.68f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Across $sessionCount ${if (sessionCount == 1) "session" else "sessions"}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                    color = ColorTextSecondary.copy(alpha = 0.62f),
                )
            }
            LineGraph()
        }
    }
}

@Composable
private fun IconCircle(icon: ImageVector) {
    Box(
        modifier         = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF252118)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = ColorPrimary.copy(alpha = 0.90f),
            modifier           = Modifier.size(17.dp),
        )
    }
}

@Composable
private fun BarChart(sessionCount: Int) {
    Canvas(modifier = Modifier.fillMaxWidth().height(38.dp)) {
        val count    = 7
        val gap      = size.width * 0.055f
        val barW     = (size.width - gap * (count - 1)) / count
        BAR_HEIGHTS.forEachIndexed { i, frac ->
            val active = i < sessionCount.coerceAtMost(count)
            val bh     = size.height * frac
            val left   = i * (barW + gap)
            val top    = size.height - bh
            drawRoundRect(
                color        = if (active) Color(0xFF604830) else Color(0xFF2C2318),
                topLeft      = Offset(left, top),
                size         = Size(barW, bh),
                cornerRadius = CornerRadius(3.dp.toPx()),
            )
            if (active) {
                drawRoundRect(
                    color        = Color(0xFF907050),
                    topLeft      = Offset(left, top),
                    size         = Size(barW, (bh * 0.22f).coerceAtLeast(3.dp.toPx())),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                )
            }
        }
    }
}

@Composable
private fun LineGraph() {
    Canvas(modifier = Modifier.fillMaxWidth().height(48.dp)) {
        val w = size.width
        val h = size.height
        val pts = listOf(
            Offset(0f,         h * 0.72f),
            Offset(w * 0.15f,  h * 0.82f),
            Offset(w * 0.30f,  h * 0.58f),
            Offset(w * 0.45f,  h * 0.68f),
            Offset(w * 0.60f,  h * 0.42f),
            Offset(w * 0.75f,  h * 0.52f),
            Offset(w * 0.88f,  h * 0.28f),
            Offset(w,          h * 0.12f),
        )
        val path = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            for (i in 0 until pts.size - 1) {
                val cx = (pts[i].x + pts[i + 1].x) / 2f
                cubicTo(cx, pts[i].y, cx, pts[i + 1].y, pts[i + 1].x, pts[i + 1].y)
            }
        }
        drawPath(path, ColorPrimary.copy(alpha = 0.85f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(ColorPrimary, 4.dp.toPx(), pts.last())
    }
}

// ── Companion Card ────────────────────────────────────────────────────────────

@Composable
private fun CompanionCard(sessionCount: Int) {
    val bond    = (sessionCount * 14).coerceAtMost(100)
    val heading = when {
        sessionCount < 3 -> "House Cat is getting to know you."
        sessionCount < 8 -> "House Cat is getting familiar with you."
        else             -> "House Cat knows your evenings well."
    }
    val sub = when {
        sessionCount == 0 -> "Begin your first session to start bonding."
        sessionCount < 3  -> "Keep returning to build your bond."
        else              -> "You've built a comforting routine together."
    }

    Row(
        modifier         = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            .clip(CardShape).background(CardBg).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Thumbnail with heart overlay
        Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))) {
            Image(
                painter            = painterResource(R.drawable.herocard),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC100E0C)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint               = ColorPrimary.copy(alpha = 0.88f),
                    modifier           = Modifier.size(10.dp),
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = heading,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
                color = ColorMoonlitCream,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text  = sub,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = ColorTextSecondary.copy(alpha = 0.66f),
            )
        }

        Spacer(Modifier.width(12.dp))

        // Bond circle + label below
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BondCircle(bond = bond)
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Bond level",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = ColorTextTertiary.copy(alpha = 0.65f),
            )
        }
    }
}

@Composable
private fun BondCircle(bond: Int) {
    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sw      = 4.dp.toPx()
            val inset   = sw / 2f
            val arcSz   = Size(size.width - sw, size.height - sw)
            val topLeft = Offset(inset, inset)
            drawArc(
                color      = Color(0xFF2A2318),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter  = false,
                style      = Stroke(width = sw, cap = StrokeCap.Round),
                size       = arcSz,
                topLeft    = topLeft,
            )
            if (bond > 0) {
                drawArc(
                    color      = ColorPrimary.copy(alpha = 0.92f),
                    startAngle = -90f,
                    sweepAngle = 360f * bond / 100f,
                    useCenter  = false,
                    style      = Stroke(width = sw, cap = StrokeCap.Round),
                    size       = arcSz,
                    topLeft    = topLeft,
                )
            }
        }
        Text(
            text       = "$bond%",
            style      = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
            color      = ColorMoonlitCream,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ── Floating Bottom Navigation ─────────────────────────────────────────────────

@Composable
private fun FloatingNavBar(
    onHome: () -> Unit,
    onSessions: () -> Unit,
    onFavorites: () -> Unit,
    onProfile: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xF0161310))
                .border(0.5.dp, ColorMoonlitCream.copy(alpha = 0.09f), RoundedCornerShape(28.dp))
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NavItem(icon = Icons.Filled.Home,             label = "Home",      active = true,  onClick = onHome)
            NavItem(icon = Icons.Filled.NightsStay,        label = "Sessions",  active = false, onClick = onSessions)
            NavItem(icon = Icons.Outlined.Favorite,        label = "Favorites", active = false, onClick = onFavorites)
            NavItem(icon = Icons.Filled.Person,            label = "Profile",   active = false, onClick = onProfile)
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (active) ColorPrimary else ColorMoonlitCream.copy(alpha = 0.28f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 4.dp),
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = tint,
        )
    }
}
