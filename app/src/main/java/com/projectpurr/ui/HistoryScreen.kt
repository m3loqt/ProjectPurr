package com.projectpurr.ui

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectpurr.R
import com.projectpurr.data.db.RestSessionEntity
import com.projectpurr.ui.theme.ColorMoonlitCream
import com.projectpurr.ui.theme.ColorPrimary
import com.projectpurr.ui.theme.ColorTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

private val PageBg    = Color(0xFF0D0B09)
private val CardBg    = Color(0xFF191612)
private val CardShape = RoundedCornerShape(18.dp)

@Composable
fun HistoryScreen(
    sessions: List<RestSessionEntity>,
    onNavigateHome: () -> Unit,
    onNavigateToSession: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    val entryAlpha by animateFloatAsState(1f, tween(900), label = "historyFade")

    Box(Modifier.fillMaxSize().alpha(entryAlpha).background(PageBg)) {

        Column(Modifier.fillMaxSize()) {
            if (sessions.isEmpty()) {
                Box(Modifier.statusBarsPadding()) { HistoryHeader() }
                Box(
                    modifier         = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState()
                }
            } else {
                LazyColumn(
                    modifier       = Modifier.weight(1f).statusBarsPadding(),
                    contentPadding = PaddingValues(bottom = 12.dp),
                ) {
                    item { HistoryHeader() }
                    val grouped = groupSessions(sessions)
                    grouped.forEach { (label, group) ->
                        item {
                            Text(
                                text     = label,
                                style    = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                color    = ColorPrimary.copy(alpha = 0.80f),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            )
                        }
                        items(group) { session ->
                            SessionHistoryCard(session)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            QuoteCard()

            HistoryNavBar(
                onHome      = onNavigateHome,
                onSessions  = onNavigateToSession,
                onHistory   = {},
                onProfile   = onNavigateToProfile,
            )
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun HistoryHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
    ) {
        // last.png — full width background
        Image(
            painter            = painterResource(R.drawable.last),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize(),
        )

        // Left-heavy dark overlay for text legibility
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.00f to PageBg,
                        0.36f to PageBg,
                        0.60f to PageBg.copy(alpha = 0.82f),
                        0.82f to PageBg.copy(alpha = 0.40f),
                        1.00f to PageBg.copy(alpha = 0.12f),
                    ),
                ),
            ),
        )

        // Bottom fade — seamless dissolve into the charcoal page background
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1.00f to PageBg,
                    ),
                ),
            ),
        )

        // Title + description — bottom-left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 22.dp),
        ) {
            Text(
                text  = "Nights",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize   = 46.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 54.sp,
                ),
                color = ColorMoonlitCream,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = "Your quiet moments,\nwhenever you needed them.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
                color = ColorTextSecondary.copy(alpha = 0.65f),
            )
        }
    }
}

// ── Session card ──────────────────────────────────────────────────────────────

@Composable
private fun SessionHistoryCard(session: RestSessionEntity) {
    val dayLabel  = remember(session.startedAtMillis) { formatDayLabel(session.startedAtMillis) }
    val timeLabel = remember(session.startedAtMillis) { formatTime(session.startedAtMillis) }
    val durLabel  = remember(session.durationMillis)  { formatDuration(session.durationMillis) }
    val modeTag   = remember(session.usedSilentMode, session.usedChestMode) {
        buildModeTag(session.usedSilentMode, session.usedChestMode)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(CardShape)
            .background(CardBg)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 72.dp)
                .clip(RoundedCornerShape(12.dp)),
        ) {
            Image(
                painter            = painterResource(R.drawable.herocard),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0x30080604),
                            1.0f to Color(0x60080604),
                        ),
                    ),
                ),
            )
        }

        Spacer(Modifier.width(14.dp))

        // Middle info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = dayLabel,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = ColorMoonlitCream,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = session.companionName,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = ColorTextSecondary.copy(alpha = 0.60f),
            )
            if (modeTag.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = modeTag,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = ColorPrimary.copy(alpha = 0.80f),
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Right: duration + time
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text  = durLabel,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = ColorPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = timeLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = ColorTextSecondary.copy(alpha = 0.50f),
            )
            Spacer(Modifier.height(4.dp))
            Icon(
                imageVector        = Icons.Outlined.Bedtime,
                contentDescription = null,
                tint               = ColorPrimary.copy(alpha = 0.45f),
                modifier           = Modifier.size(16.dp),
            )
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector        = Icons.Outlined.Bedtime,
            contentDescription = null,
            tint               = ColorPrimary.copy(alpha = 0.30f),
            modifier           = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "Your quiet nights will appear here.",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            color = ColorMoonlitCream.copy(alpha = 0.70f),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text  = "Complete a rest session to begin your history.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = ColorTextSecondary.copy(alpha = 0.50f),
        )
    }
}

// ── Quote card ────────────────────────────────────────────────────────────────

@Composable
private fun QuoteCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(76.dp)
            .clip(CardShape),
    ) {
        Box(Modifier.fillMaxSize().background(CardBg))

        // Image on the right, smaller — fades to the left
        Image(
            painter            = painterResource(R.drawable.every),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(160.dp),
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.00f to CardBg,
                        0.48f to CardBg,
                        0.72f to CardBg.copy(alpha = 0.80f),
                        1.00f to Color.Transparent,
                    ),
                ),
            ),
        )

        Row(
            modifier          = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Moon icon with circular background
            Box(
                modifier         = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF252118)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Filled.NightsStay,
                    contentDescription = null,
                    tint               = ColorPrimary.copy(alpha = 0.85f),
                    modifier           = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text  = "Every night is a step\ntoward a gentler tomorrow.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize   = 12.sp,
                    lineHeight = 17.sp,
                ),
                color = ColorMoonlitCream.copy(alpha = 0.68f),
            )
        }
    }
    Spacer(Modifier.height(8.dp))
}

// ── Nav bar ───────────────────────────────────────────────────────────────────

@Composable
private fun HistoryNavBar(
    onHome: () -> Unit,
    onSessions: () -> Unit,
    onHistory: () -> Unit,
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
            HistoryNavItem(icon = Icons.Filled.Home,              label = "Home",    active = false, onClick = onHome)
            HistoryNavItem(icon = Icons.Filled.Pets,        label = "Purr",   active = false, onClick = onSessions)
            HistoryNavItem(icon = Icons.Filled.NightsStay,  label = "Nights", active = true,  onClick = onHistory)
            HistoryNavItem(icon = Icons.Filled.Person,            label = "Profile", active = false, onClick = onProfile)
        }
    }
}

@Composable
private fun HistoryNavItem(
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

// ── Grouping + formatting helpers ─────────────────────────────────────────────

private fun groupSessions(sessions: List<RestSessionEntity>): List<Pair<String, List<RestSessionEntity>>> {
    val now      = System.currentTimeMillis()
    val weekMs   = 7L * 24 * 60 * 60 * 1000
    val thisWeek = sessions.filter { now - it.startedAtMillis < weekMs }
    val lastWeek = sessions.filter { it.startedAtMillis !in thisWeek.map { s -> s.startedAtMillis } && now - it.startedAtMillis < 2 * weekMs }
    val earlier  = sessions.filter { now - it.startedAtMillis >= 2 * weekMs }

    return buildList {
        if (thisWeek.isNotEmpty()) add("This week" to thisWeek)
        if (lastWeek.isNotEmpty()) add("Last week" to lastWeek)
        if (earlier.isNotEmpty())  add("Earlier"   to earlier)
    }
}

private fun formatDayLabel(millis: Long): String {
    val now     = Calendar.getInstance()
    val session = Calendar.getInstance().apply { timeInMillis = millis }
    val hour    = session.get(Calendar.HOUR_OF_DAY)
    val isNight = hour >= 18 || hour < 5
    val suffix  = if (isNight) " night" else ""

    val daysDiff = daysBetween(session, now)
    return when (daysDiff) {
        0    -> if (isNight) "Tonight" else "Today"
        1    -> "Yesterday$suffix"
        else -> {
            val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(millis))
            "$dayName$suffix"
        }
    }
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))

private fun formatDuration(durationMs: Long): String {
    val mins = durationMs / 60_000
    return if (mins < 60) "${mins} min" else "${mins / 60}h ${mins % 60}m"
}

private fun buildModeTag(silent: Boolean, chest: Boolean): String = when {
    silent && chest -> "Silent · Chest mode"
    silent          -> "Silent"
    chest           -> "Chest mode"
    else            -> ""
}

private fun daysBetween(from: Calendar, to: Calendar): Int {
    val f = from.clone() as Calendar
    val t = to.clone() as Calendar
    f.set(Calendar.HOUR_OF_DAY, 0); f.set(Calendar.MINUTE, 0); f.set(Calendar.SECOND, 0); f.set(Calendar.MILLISECOND, 0)
    t.set(Calendar.HOUR_OF_DAY, 0); t.set(Calendar.MINUTE, 0); t.set(Calendar.SECOND, 0); t.set(Calendar.MILLISECOND, 0)
    return ((t.timeInMillis - f.timeInMillis) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
}
