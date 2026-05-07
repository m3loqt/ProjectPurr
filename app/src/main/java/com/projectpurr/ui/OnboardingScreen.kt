package com.projectpurr.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.projectpurr.R
import com.projectpurr.ui.theme.ColorOnBackground
import com.projectpurr.ui.theme.ColorPrimary
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val heroLine1: String,
    val heroLine1Amber: Boolean = false,
    val heroLine2: String = "",
    val heroLine2Amber: Boolean = false,
    val body: String,
    val imageContentDescription: String,
)

private val OnboardingHeroDrawables = listOf(
    R.drawable.onboarding_hero_1,
    R.drawable.onboarding_hero_2,
    R.drawable.onboarding_hero_3,
)

private val SLIDES = listOf(
    OnboardingSlide(
        heroLine1 = "Feel it.",
        heroLine1Amber = true,
        body = "Not just sound. A soft presence you can rest with.",
        imageContentDescription = "Cat in moonlight, calm night scene.",
    ),
    OnboardingSlide(
        heroLine1 = "Settle in.",
        heroLine2 = "Let it purr.",
        heroLine2Amber = true,
        body = "Place your phone on your chest and breathe slowly.",
        imageContentDescription = "Cat resting in warm dim light.",
    ),
    OnboardingSlide(
        heroLine1 = "Quiet comfort",
        heroLine2 = "anytime.",
        heroLine2Amber = true,
        body = "A warm ritual for moments that feel heavy or lonely.",
        imageContentDescription = "Sleeping cat curled up in cozy darkness.",
    ),
)

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { SLIDES.size })
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF070605),
    ) {
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val slide = SLIDES[page]
                Box(Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(OnboardingHeroDrawables[page]),
                        contentDescription = slide.imageContentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0f to Color.Black.copy(alpha = 0.52f),
                                        0.22f to Color.Black.copy(alpha = 0.06f),
                                        0.58f to Color.Black.copy(alpha = 0.10f),
                                        1f to Color(0xFF070605).copy(alpha = 0.90f),
                                    ),
                                ),
                            ),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp)
                            .padding(bottom = 196.dp),
                    ) {
                        Spacer(Modifier.weight(1f))
                        val hero = buildAnnotatedString {
                            val c1 = if (slide.heroLine1Amber) ColorPrimary else ColorOnBackground
                            withStyle(SpanStyle(color = c1)) { append(slide.heroLine1) }
                            if (slide.heroLine2.isNotEmpty()) {
                                append("\n")
                                val c2 = if (slide.heroLine2Amber) ColorPrimary else ColorOnBackground
                                withStyle(SpanStyle(color = c2)) { append(slide.heroLine2) }
                            }
                        }
                        Text(text = hero, style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(22.dp))
                        Text(
                            text = slide.body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ColorOnBackground.copy(alpha = 0.82f),
                        )
                    }
                }
            }

            // Skip — above imagery; top vignette keeps legibility
            if (pagerState.currentPage < SLIDES.lastIndex) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 8.dp, end = 20.dp),
                ) {
                    TextButton(onClick = onGetStarted) {
                        Text(
                            "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            color = ColorOnBackground.copy(alpha = 0.82f),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(SLIDES.size) { i ->
                        val active = i == pagerState.currentPage
                        val w by animateDpAsState(
                            targetValue = if (active) 24.dp else 8.dp,
                            label = "dot",
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(w)
                                .background(
                                    color = if (active) ColorPrimary
                                    else ColorOnBackground.copy(alpha = 0.28f),
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
                Spacer(Modifier.height(26.dp))
                val isLast = pagerState.currentPage == SLIDES.lastIndex
                Button(
                    onClick = {
                        if (isLast) {
                            onGetStarted()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        if (isLast) "Enter Calm Mode" else "Next",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
