package com.projectpurr.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectpurr.R
import com.projectpurr.ui.components.AtmosphericScene
import com.projectpurr.ui.components.CinematicTextureOverlay
import com.projectpurr.ui.theme.ColorMoonlitCream
import com.projectpurr.ui.theme.ColorPrimary
import com.projectpurr.ui.theme.ColorTextSecondary

private val PageBg    = Color(0xFF090704)
private val CardBg    = Color(0xFF191612)
private val CardShape = RoundedCornerShape(18.dp)

@Composable
fun ProfileScreen(
    lastSessionEpoch: Long,
    lastSessionDurationMs: Long = 0L,
    onNavigateHome: () -> Unit,
    onNavigateToSession: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
) {
    val entryAlpha by animateFloatAsState(1f, tween(900), label = "profileFade")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(entryAlpha)
            .background(PageBg),
    ) {
        AtmosphericScene(heroImageRes = R.drawable.herobg, heroAlpha = 0.62f) {}
        CinematicTextureOverlay()

        Scaffold(
            containerColor      = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                ProfileNavBar(
                    onHome      = onNavigateHome,
                    onSessions  = onNavigateToSession,
                    onFavorites = onNavigateToHistory,
                    onProfile   = {},
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(innerPadding).statusBarsPadding(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item { Spacer(Modifier.height(16.dp)) }
                item { ProfileHeader(onNavigateToSettings) }
                item { Spacer(Modifier.height(20.dp)) }
                item { LastRestedCard(lastSessionEpoch, lastSessionDurationMs) }
                item { Spacer(Modifier.height(10.dp)) }
                item { CurrentCompanionCard(onNavigateToSession) }
                item { Spacer(Modifier.height(24.dp)) }
                item { OtherCompanionsSection() }
                item { Spacer(Modifier.height(12.dp)) }
                item { SettingsNavigationRow(onNavigateToSettings) }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(onNavigateToSettings: () -> Unit) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = "Profile",
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize   = 36.sp,
                fontWeight = FontWeight.Light,
            ),
            color = ColorMoonlitCream,
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, ColorMoonlitCream.copy(alpha = 0.18f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onNavigateToSettings,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint               = ColorMoonlitCream.copy(alpha = 0.50f),
                modifier           = Modifier.size(17.dp),
            )
        }
    }
}

// ── Last Rested Card ──────────────────────────────────────────────────────────

@Composable
private fun LastRestedCard(lastSessionEpoch: Long, lastSessionDurationMs: Long = 0L) {
    val relativeTime = remember(lastSessionEpoch) {
        if (lastSessionEpoch == 0L) return@remember null
        val diffDays = ((System.currentTimeMillis() - lastSessionEpoch) / 86_400_000L).toInt()
        when {
            diffDays < 1 -> "Today"
            diffDays < 2 -> "Yesterday"
            diffDays < 7 -> "$diffDays days ago"
            else         -> "A while ago"
        }
    }
    val durationLabel = remember(lastSessionDurationMs) {
        if (lastSessionDurationMs <= 0L) return@remember null
        val mins = (lastSessionDurationMs / 60_000L).toInt().coerceAtLeast(1)
        if (mins < 60) "$mins min" else "${mins / 60}h ${mins % 60}m"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(158.dp)
            .clip(CardShape),
    ) {
        Box(Modifier.fillMaxSize().background(CardBg))

        Image(
            painter            = painterResource(R.drawable.last),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize(),
        )

        // Left-heavy dark gradient so text is readable
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.00f to CardBg,
                        0.38f to CardBg.copy(alpha = 0.94f),
                        0.58f to CardBg.copy(alpha = 0.62f),
                        0.80f to CardBg.copy(alpha = 0.18f),
                        1.00f to Color.Transparent,
                    ),
                ),
            ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            SectionChip("LAST RESTED")
            Spacer(Modifier.height(8.dp))
            if (relativeTime != null) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text  = relativeTime,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize   = 30.sp,
                            fontWeight = FontWeight.Light,
                            lineHeight = 36.sp,
                        ),
                        color = ColorMoonlitCream,
                    )
                    if (durationLabel != null) {
                        Text(
                            text  = "  $durationLabel",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize   = 19.sp,
                                fontWeight = FontWeight.Light,
                            ),
                            color = ColorMoonlitCream.copy(alpha = 0.55f),
                        )
                    }
                }
            } else {
                Text(
                    text  = "No sessions yet",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Light),
                    color = ColorMoonlitCream.copy(alpha = 0.50f),
                )
            }
            Spacer(Modifier.height(18.dp))
            Box(
                Modifier
                    .fillMaxWidth(0.50f)
                    .height(0.5.dp)
                    .background(ColorMoonlitCream.copy(alpha = 0.18f)),
            )
            Spacer(Modifier.height(13.dp))
            Text(
                text  = "A quieter evening.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = ColorPrimary.copy(alpha = 0.70f),
            )
        }
    }
}

// ── Current Companion Card ────────────────────────────────────────────────────

@Composable
private fun CurrentCompanionCard(onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(182.dp)
            .clip(CardShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onTap,
            ),
    ) {
        Box(Modifier.fillMaxSize().background(CardBg))

        // Cat portrait — right-aligned, full card height
        Image(
            painter            = painterResource(R.drawable.herocard),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(210.dp),
        )

        // Left-heavy gradient
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.00f to CardBg,
                        0.36f to CardBg.copy(alpha = 0.96f),
                        0.56f to CardBg.copy(alpha = 0.68f),
                        0.78f to CardBg.copy(alpha = 0.22f),
                        1.00f to Color.Transparent,
                    ),
                ),
            ),
        )

        // Start session button — top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .size(34.dp)
                .border(1.dp, ColorPrimary.copy(alpha = 0.35f), CircleShape)
                .background(Color(0x44090704), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "Start session",
                tint               = ColorPrimary.copy(alpha = 0.75f),
                modifier           = Modifier.size(18.dp),
            )
        }

        // Text content — bottom left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            SectionChip("CURRENT COMPANION")
            Spacer(Modifier.height(8.dp))
            Text(
                text  = "House Cat",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 34.sp,
                ),
                color = ColorMoonlitCream,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text  = "Warm  ·  Familiar  ·  Gentle",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = ColorTextSecondary.copy(alpha = 0.55f),
            )
            Spacer(Modifier.height(16.dp))
            // Heart pill
            Row(
                modifier          = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF1C1510))
                    .border(0.5.dp, ColorPrimary.copy(alpha = 0.18f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint               = ColorPrimary.copy(alpha = 0.80f),
                    modifier           = Modifier.size(11.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text  = "Most nights begin here.",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = ColorPrimary.copy(alpha = 0.85f),
                )
            }
        }
    }
}

// ── Section chip ──────────────────────────────────────────────────────────────

@Composable
private fun SectionChip(label: String) {
    Text(
        text  = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize      = 10.sp,
            letterSpacing = 1.4.sp,
        ),
        color = ColorPrimary.copy(alpha = 0.75f),
    )
}

// ── Other Companions ──────────────────────────────────────────────────────────

private data class CompanionSlot(
    val name: String,
    val imageRes: Int,
    val isActive: Boolean,
)

private val COMPANION_SLOTS = listOf(
    CompanionSlot("House Cat",  R.drawable.herocard,    isActive = true),
    CompanionSlot("Maincoon",   R.drawable.maincoon,    isActive = false),
    CompanionSlot("Ragdoll",    R.drawable.ragdoll,     isActive = false),
    CompanionSlot("Bluepersian",R.drawable.bluepersian, isActive = false),
)

@Composable
private fun OtherCompanionsSection() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionChip("OTHER COMPANIONS")
        Spacer(Modifier.height(12.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            COMPANION_SLOTS.forEach { slot ->
                CompanionMiniCard(
                    slot     = slot,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CompanionMiniCard(slot: CompanionSlot, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(138.dp)
            .clip(RoundedCornerShape(14.dp)),
    ) {
        Image(
            painter            = painterResource(slot.imageRes),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize(),
        )

        // Dark overlay — heavier for locked
        Box(
            Modifier.fillMaxSize().background(
                if (slot.isActive) Color(0x66080604) else Color(0xBB080604),
            ),
        )

        // Bottom gradient for text readability
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.40f to Color.Transparent,
                        1.00f to Color(0xCC080604),
                    ),
                ),
            ),
        )

        // Badge — top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (slot.isActive) ColorPrimary.copy(alpha = 0.22f)
                    else Color(0xFF1A1612),
                )
                .border(
                    0.5.dp,
                    if (slot.isActive) ColorPrimary.copy(alpha = 0.50f)
                    else ColorMoonlitCream.copy(alpha = 0.12f),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = if (slot.isActive) Icons.Filled.Check else Icons.Filled.Lock,
                contentDescription = null,
                tint               = if (slot.isActive) ColorPrimary else ColorMoonlitCream.copy(alpha = 0.40f),
                modifier           = Modifier.size(12.dp),
            )
        }

        // Name + status — bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            Text(
                text  = slot.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = ColorMoonlitCream.copy(alpha = 0.88f),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text  = if (slot.isActive) "Active" else "Locked",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = if (slot.isActive) ColorPrimary.copy(alpha = 0.85f)
                        else ColorMoonlitCream.copy(alpha = 0.32f),
            )
        }
    }
}

// ── Settings navigation row ───────────────────────────────────────────────────

@Composable
private fun SettingsNavigationRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(CardShape)
            .background(Color(0xFF191612))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1B16)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Outlined.Settings,
                contentDescription = null,
                tint               = ColorMoonlitCream.copy(alpha = 0.55f),
                modifier           = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = "Settings",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = ColorMoonlitCream.copy(alpha = 0.88f),
            )
            Text(
                text  = "Customize your experience",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = ColorTextSecondary.copy(alpha = 0.52f),
            )
        }
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = ColorMoonlitCream.copy(alpha = 0.28f),
            modifier           = Modifier.size(18.dp),
        )
    }
}

// ── Bottom nav ────────────────────────────────────────────────────────────────

@Composable
private fun ProfileNavBar(
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
            ProfileNavItem(icon = Icons.Filled.Home,       label = "Home",    active = false, onClick = onHome)
            ProfileNavItem(icon = Icons.Filled.Pets,       label = "Purr",    active = false, onClick = onSessions)
            ProfileNavItem(icon = Icons.Filled.NightsStay, label = "Nights",  active = false, onClick = onFavorites)
            ProfileNavItem(icon = Icons.Filled.Person,     label = "Profile", active = true,  onClick = onProfile)
        }
    }
}

@Composable
private fun ProfileNavItem(
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
