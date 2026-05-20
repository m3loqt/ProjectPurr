package com.projectpurr.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
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
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.filled.Person
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
import com.projectpurr.data.db.RestSessionEntity
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

private val CardBg    = Color(0xFF191612)
private val CardShape = RoundedCornerShape(18.dp)
private val DAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")

// ── Stats derived from real session data ──────────────────────────────────────

private data class HomeStats(
    val thisWeekCount: Int,
    val totalDurationMin: Int,
    val sessionCount: Int,
    /** Sessions per weekday Mon=0..Sun=6 for this week, relative heights 0..1f */
    val weekBarFractions: List<Float>,
    /** Cumulative session count per weekday for line graph, normalised 0..1f */
    val weekLineFractions: List<Float>,
)

private fun computeHomeStats(sessions: List<RestSessionEntity>): HomeStats {
    val now     = Calendar.getInstance()
    // Start of this week (Monday)
    val weekStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        // if today is Sunday, roll back a week so Monday is correct
        if (now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) add(Calendar.WEEK_OF_YEAR, -1)
    }
    val weekStartMs = weekStart.timeInMillis

    val thisWeek = sessions.filter { it.startedAtMillis >= weekStartMs }
    val counts   = IntArray(7) // Mon=0..Sun=6
    thisWeek.forEach { s ->
        val cal = Calendar.getInstance().apply { timeInMillis = s.startedAtMillis }
        val dow = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Sun=1→6, Mon=2→0
        counts[dow]++
    }

    val maxCount = counts.max().coerceAtLeast(1)
    val fracs    = counts.map { it.toFloat() / maxCount }

    // Cumulative across the week for the line graph
    var running = 0
    val lineFracs = counts.map { c -> running += c; running }.let { cum ->
        val peak = cum.last().coerceAtLeast(1)
        cum.map { it.toFloat() / peak }
    }

    val totalMin = sessions.sumOf { it.durationMillis / 60_000L }.toInt()

    return HomeStats(
        thisWeekCount     = thisWeek.size,
        totalDurationMin  = totalMin,
        sessionCount      = sessions.size,
        weekBarFractions  = fracs,
        weekLineFractions = lineFracs,
    )
}

// ── Root ──────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    sessionCount: Int,
    recentSessions: List<RestSessionEntity> = emptyList(),
    pendingBondDelta: Int = 0,
    onBondDeltaConsumed: () -> Unit = {},
    onSelectHouseCat: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onPreviewHaptic: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
) {
    val stats      = remember(recentSessions) { computeHomeStats(recentSessions) }
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
                    onFavorites = onNavigateToHistory,
                    onProfile   = onNavigateToProfile,
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier        = Modifier.fillMaxSize().padding(innerPadding).statusBarsPadding(),
                contentPadding  = PaddingValues(bottom = 16.dp),
            ) {
                item { Spacer(Modifier.height(24.dp)) }
                item { HeroCard(onSelectHouseCat) }
                item { Spacer(Modifier.height(16.dp)) }
                item { RhythmSection(stats) }
                item { Spacer(Modifier.height(12.dp)) }
                item { StatCardsRow(stats) }
                item { Spacer(Modifier.height(12.dp)) }
                item { CompanionCard(stats.sessionCount) }
            }
        }

        if (pendingBondDelta > 0) {
            BondDeltaToast(delta = pendingBondDelta, onDone = onBondDeltaConsumed)
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
                    text       = "Soft purrs,\nDeeper rest",
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
private fun RhythmSection(stats: HomeStats) {
    val todayIndex = remember {
        // Calendar: Sun=1 Mon=2 … Sat=7 → map Mon=0 … Sun=6
        (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(CardShape)
            .background(CardBg)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = "Your rhythm",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = ColorTextSecondary.copy(alpha = 0.60f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "${stats.thisWeekCount} nights",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 28.sp,
                ),
                color = ColorPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = "this week",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = ColorTextTertiary.copy(alpha = 0.45f),
            )
        }

        Spacer(Modifier.width(16.dp))

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
                    val filled  = idx <= todayIndex && stats.weekBarFractions[idx] > 0f
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
private fun StatCardsRow(stats: HomeStats) {
    val totalMin = stats.totalDurationMin
    val timeStr  = when {
        totalMin == 0  -> "0m"
        totalMin < 60  -> "${totalMin}m"
        totalMin < 600 -> "${totalMin / 60}h ${totalMin % 60}m"
        else           -> "${totalMin / 60}h"
    }
    val n = stats.sessionCount

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
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        "${stats.thisWeekCount}",
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
                val habit = when {
                    n == 0 -> "Start your first session\ntonight."
                    n < 3  -> "You're building a\nbeautiful rest habit."
                    else   -> "A beautiful rest habit\nis forming."
                }
                Text(
                    habit,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 15.sp),
                    color = ColorTextSecondary.copy(alpha = 0.62f),
                )
            }
            BarChart(fractions = stats.weekBarFractions)
        }

        // Time card
        Column(
            modifier            = Modifier.weight(1f).fillMaxSize().clip(CardShape).background(CardBg).padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
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
                    "Across $n ${if (n == 1) "session" else "sessions"}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                    color = ColorTextSecondary.copy(alpha = 0.62f),
                )
            }
            LineGraph(fractions = stats.weekLineFractions)
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
private fun BarChart(fractions: List<Float>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(38.dp)) {
        val count = fractions.size
        val gap   = size.width * 0.055f
        val barW  = (size.width - gap * (count - 1)) / count
        fractions.forEachIndexed { i, frac ->
            val active = frac > 0f
            val bh     = size.height * frac.coerceAtLeast(0.08f)
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
private fun LineGraph(fractions: List<Float>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(48.dp)) {
        val w = size.width
        val h = size.height
        if (fractions.isEmpty()) return@Canvas

        val stepX = w / (fractions.size - 1).coerceAtLeast(1).toFloat()
        val pts   = fractions.mapIndexed { i, frac ->
            // invert: frac=0 → bottom, frac=1 → top
            Offset(i * stepX, h * (1f - frac * 0.88f))
        }

        if (pts.size == 1) {
            drawCircle(ColorPrimary, 4.dp.toPx(), pts[0])
            return@Canvas
        }

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

// ── Bond Delta Toast ──────────────────────────────────────────────────────────

@Composable
private fun BondDeltaToast(delta: Int, onDone: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2200)
        visible = false
        delay(500)
        onDone()
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(if (visible) 350 else 500),
        label = "bondDeltaAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(bottom = 90.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .alpha(alpha)
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFBB7C34))
                .padding(horizontal = 18.dp, vertical = 9.dp),
        ) {
            Text(
                text = "+$delta bond",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                color = Color(0xFF1A0E05),
                fontWeight = FontWeight.SemiBold,
            )
        }
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
            NavItem(icon = Icons.Filled.Home,             label = "Home",    active = true,  onClick = onHome)
            NavItem(icon = Icons.Filled.Pets,        label = "Purr",   active = false, onClick = onSessions)
            NavItem(icon = Icons.Filled.NightsStay,  label = "Nights", active = false, onClick = onFavorites)
            NavItem(icon = Icons.Filled.Person,           label = "Profile", active = false, onClick = onProfile)
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
