package com.projectpurr.ui

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.projectpurr.R
import com.projectpurr.ui.components.OnboardingPillButton
import com.projectpurr.ui.components.OnboardingSwipeButton
import com.projectpurr.ui.theme.ColorMoonlitCream
import com.projectpurr.ui.theme.ColorPrimary
import com.projectpurr.ui.theme.ColorTextSecondary
import kotlinx.coroutines.launch

private val PageBg = Color(0xFF090704)

private data class OnboardingSlide(
    val tag: String,
    val title: String,
    val subtitle: String,
    val imageContentDescription: String,
)

private val OnboardingHeroDrawables = listOf(
    R.drawable.onboarding_hero_1,
    R.drawable.onboarding_hero_2,
    R.drawable.onboarding_hero_3,
)

private val SLIDES = listOf(
    OnboardingSlide(
        tag      = "HAPTIC · AUDIO",
        title    = "Feel the purr,\nbefore you sleep",
        subtitle = "Real feline vibration and sound,\nsynchronized through your phone.",
        imageContentDescription = "Cat in moonlight, calm night scene.",
    ),
    OnboardingSlide(
        tag      = "CHEST MODE",
        title    = "Rest it\non your chest",
        subtitle = "Lie down. Place your phone.\nClose your eyes.",
        imageContentDescription = "Cat resting in warm dim light.",
    ),
    OnboardingSlide(
        tag      = "ALWAYS HERE",
        title    = "Whenever\nyou need rest",
        subtitle = "A quiet companion,\nalways within reach.",
        imageContentDescription = "Sleeping cat curled up in cozy darkness.",
    ),
)

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { SLIDES.size })
    val scope      = rememberCoroutineScope()
    val view       = LocalView.current

    // Pages 0 and 1 are fully immersive — no system nav bar.
    // Page 2 shows it so the user feels oriented when entering the app.
    SideEffect {
        val window     = (view.context as ComponentActivity).window
        val controller = WindowCompat.getInsetsController(window, view)
        if (pagerState.currentPage < SLIDES.lastIndex) {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg),
    ) {
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            OnboardingPage(
                slide   = SLIDES[page],
                heroRes = OnboardingHeroDrawables[page],
            )
        }

        // Skip — top right, only on non-last pages
        if (pagerState.currentPage < SLIDES.lastIndex) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 10.dp, end = 20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onGetStarted,
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text  = "Skip",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                    color = ColorMoonlitCream.copy(alpha = 0.42f),
                )
            }
        }

        // Bottom overlay: progress dots + action button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement   = Arrangement.Bottom,
            horizontalAlignment   = Alignment.CenterHorizontally,
        ) {
            // Amber pill dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(SLIDES.size) { i ->
                    val active = i == pagerState.currentPage
                    val w by animateDpAsState(
                        targetValue = if (active) 20.dp else 6.dp,
                        label       = "dot",
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(w)
                            .clip(CircleShape)
                            .background(
                                if (active) ColorPrimary
                                else ColorMoonlitCream.copy(alpha = 0.20f),
                            ),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (pagerState.currentPage == SLIDES.lastIndex) {
                OnboardingSwipeButton(
                    label      = "Begin your journey",
                    onComplete = onGetStarted,
                )
            } else {
                OnboardingPillButton(
                    label   = "Continue",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(slide: OnboardingSlide, heroRes: Int) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter            = painterResource(heroRes),
            contentDescription = slide.imageContentDescription,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize(),
        )

        // Bottom-heavy gradient — same language as HeroCard and Nights header
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0x1A090704),
                        0.35f to Color(0x44090704),
                        0.58f to Color(0xA8090704),
                        0.78f to Color(0xEA090704),
                        1.00f to PageBg,
                    ),
                ),
            ),
        )

        // Text block — bottom-left, clears the dots + button area
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 28.dp)
                .padding(bottom = 158.dp),
        ) {
            // Amber label — matches SectionChip in Profile
            Text(
                text  = slide.tag,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize      = 10.sp,
                    letterSpacing = 1.4.sp,
                ),
                color = ColorPrimary.copy(alpha = 0.80f),
            )
            Spacer(Modifier.height(12.dp))
            // Main title
            Text(
                text  = slide.title,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 40.sp,
                ),
                color = ColorMoonlitCream,
            )
            Spacer(Modifier.height(10.dp))
            // Subtitle
            Text(
                text  = slide.subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize   = 14.sp,
                    lineHeight = 20.sp,
                ),
                color = ColorTextSecondary.copy(alpha = 0.68f),
            )
        }
    }
}
