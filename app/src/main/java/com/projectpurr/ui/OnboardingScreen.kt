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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.projectpurr.R
import com.projectpurr.ui.components.OnboardingPillButton
import com.projectpurr.ui.components.OnboardingSwipeButton
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val title: String,
    val imageContentDescription: String,
)

private val OnboardingHeroDrawables = listOf(
    R.drawable.onboarding_hero_1,
    R.drawable.onboarding_hero_2,
    R.drawable.onboarding_hero_3,
)

private val SLIDES = listOf(
    OnboardingSlide(
        title = "Feel the purr",
        imageContentDescription = "Cat in moonlight, calm night scene.",
    ),
    OnboardingSlide(
        title = "Settle on your chest",
        imageContentDescription = "Cat resting in warm dim light.",
    ),
    OnboardingSlide(
        title = "Rest when you need",
        imageContentDescription = "Sleeping cat curled up in cozy darkness.",
    ),
)

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { SLIDES.size })
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black,
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
                                        0f to Color.Black.copy(alpha = 0.50f),
                                        0.30f to Color.Black.copy(alpha = 0.08f),
                                        0.62f to Color.Black.copy(alpha = 0.18f),
                                        1f to Color.Black.copy(alpha = 0.88f),
                                    ),
                                ),
                            ),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp)
                            .padding(bottom = 200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = slide.title,
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

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
                            color = Color.White.copy(alpha = 0.72f),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 32.dp),
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
                                    color = if (active) Color.White
                                    else Color.White.copy(alpha = 0.28f),
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))

                val isLast = pagerState.currentPage == SLIDES.lastIndex
                if (isLast) {
                    OnboardingSwipeButton(
                        label = "Begin your journey",
                        onComplete = onGetStarted,
                    )
                } else {
                    OnboardingPillButton(
                        label = "Next",
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
}
