package com.projectpurr.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.projectpurr.ui.theme.ColorOnBackground
import com.projectpurr.ui.theme.ColorPrimary
import com.projectpurr.ui.theme.ColorTextSecondary
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val heroLine1: String,
    val heroLine1Amber: Boolean = false,
    val heroLine2: String = "",
    val heroLine2Amber: Boolean = false,
    val body: String,
)

private val SLIDES = listOf(
    OnboardingSlide(
        heroLine1 = "Feel it.",
        heroLine1Amber = true,
        body = "Not just sound. Real vibration — the way your cat says: I'm here.",
    ),
    OnboardingSlide(
        heroLine1 = "Place your phone",
        heroLine2 = "on your chest.",
        heroLine2Amber = true,
        body = "Close your eyes. The purr will do the rest.",
    ),
    OnboardingSlide(
        heroLine1 = "Your House Cat",
        heroLine2 = "is waiting.",
        heroLine2Amber = true,
        body = "Tap play whenever you need it. No routine required.",
    ),
)

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { SLIDES.size })
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp),
        ) {
            // Skip link — only visible before last slide
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (pagerState.currentPage < SLIDES.lastIndex) {
                    TextButton(onClick = onGetStarted) {
                        Text(
                            "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            color = ColorTextSecondary,
                        )
                    }
                }
            }

            // Slides
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) { page ->
                val slide = SLIDES[page]
                Column(modifier = Modifier.fillMaxWidth()) {
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
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = slide.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ColorTextSecondary,
                    )
                }
            }

            // Dots + CTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp),
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
                                    else ColorTextSecondary.copy(alpha = 0.35f),
                                    shape = CircleShape,
                                ),
                        )
                    }
                }

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
                        if (isLast) "Get Started" else "Next",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
