package com.acms.cinemeteor.OnBoardingScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
) {
    val pageState = rememberPagerState(pageCount = { onBoardingPages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()

    ) {
        val currentItem = onBoardingPages[pageState.currentPage]
        Image(
            painter = painterResource(id = currentItem.image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pageState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val item = onBoardingPages[page]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    repeat(onBoardingPages.size) { index ->
                        val color =
                            if (pageState.currentPage == index) Color.White
                            else Color.Gray
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (pageState.currentPage == index) 12.dp else 8.dp)
                                .background(color, shape = MaterialTheme.shapes.small)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pageState.currentPage + 1 < onBoardingPages.size) {
                            scope.launch {
                                pageState.animateScrollToPage(pageState.currentPage + 1)
                            }
                        } else {
                            onFinish()
                        }
                    },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = if (pageState.currentPage == onBoardingPages.size - 1)
                            "Go!"
                        else
                            "Next"
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = false)
@Composable
fun OnBoardingPage(){
    OnboardingScreen {  }
}



