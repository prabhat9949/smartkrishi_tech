package com.smartkrishi.presentation.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import com.smartkrishi.R
import kotlinx.coroutines.launch

// 🎨 PROFESSIONAL COLOR SCHEME
private val PrimaryGreen = Color(0xFF2E7D32)
private val LightGreen = Color(0xFF66BB6A)
private val BackgroundWhite = Color(0xFFFAFAFA)

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Manage your farm finances",
            description = "Smart Krishi lets you plan, monitor and analyze all activities on your farm in a very simple and intuitive way. It provides real-time insight into daily progress of your crops , manages sales and, expenses  to ensure the health of your finances.",
            imageRes = R.drawable.onboarding_finance // Replace with your drawable
        ),
        OnboardingPage(
            title = "Disease and Pest Detection",
            description = "Smart Krishi disease prediction system informs farmers about the severity index of crop disease before hand so that farmers can precisely time their preventive measures.",
            imageRes = R.drawable.onboarding_disease // Replace with your drawable
        ),
        OnboardingPage(
            title = "Smart Farm Monitoring",
            description = "Monitor your crops with AI-powered insights. Get weather updates, soil health reports, and irrigation recommendations to maximize your yield.",
            imageRes = R.drawable.onboarding_monitoring // Replace with your drawable
        )
    )

    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ✅ TOP BAR WITH BACK & SKIP
            TopBar(
                showBack = pagerState.currentPage > 0,
                onBackClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onSkipClick = onFinish
            )

            // ✅ PAGER CONTENT
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // ✅ PAGE INDICATORS
            PageIndicators(
                pageCount = pages.size,
                currentPage = pagerState.currentPage
            )

            Spacer(Modifier.height(24.dp))

            // ✅ CONTINUE BUTTON
            ContinueButton(
                isLastPage = pagerState.currentPage == pages.size - 1,
                onClick = {
                    if (pagerState.currentPage == pages.size - 1) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TopBar(
    showBack: Boolean,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // BACK BUTTON
        AnimatedVisibility(
            visible = showBack,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally()
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert),
                    contentDescription = "Back",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (!showBack) {
            Spacer(Modifier.width(40.dp))
        }

        // SKIP BUTTON
        TextButton(onClick = onSkipClick) {
            Text(
                text = "Skip",
                color = PrimaryGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        // TITLE
        Text(
            text = page.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF212121),
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(Modifier.height(32.dp))

        // ILLUSTRATION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = page.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(32.dp))

        // DESCRIPTION
        Text(
            text = page.description,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF616161),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun PageIndicators(
    pageCount: Int,
    currentPage: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(if (isSelected) 32.dp else 10.dp)
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) PrimaryGreen else Color(0xFFE0E0E0)
                    )
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    )
            )
        }
    }
}

@Composable
private fun ContinueButton(
    isLastPage: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreen,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = if (isLastPage) "GET STARTED" else "CONTINUE",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
