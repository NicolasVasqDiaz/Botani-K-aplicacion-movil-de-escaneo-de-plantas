package com.botanik.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.botanik.app.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingPageData(val icon: ImageVector, val title: String, val description: String)

private val pages = listOf(
    OnboardingPageData(Icons.Filled.Eco, "Descubre la flora de Cajamarca", "Conoce las especies endémicas de nuestra región andina, desde la Jalca hasta los valles secos."),
    OnboardingPageData(Icons.Filled.CameraAlt, "Escanea y aprende", "Toma una foto de cualquier planta y elige su especie en el catálogo de flora endémica."),
    OnboardingPageData(Icons.AutoMirrored.Filled.MenuBook, "Cuida y conserva", "Guarda tus descubrimientos en tu bitácora y aprende a cuidarlos correctamente.")
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) { viewModel.onPageChanged(pagerState.currentPage) }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundCream).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (!uiState.isLastPage) {
                TextButton(onClick = onFinished) { Text("Saltar", color = TextPrimary.copy(alpha = 0.7f)) }
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page -> OnboardingPage(pages[page]) }

        PagerIndicator(pageCount = pages.size, currentPage = uiState.currentPage)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (uiState.isLastPage) {
                    viewModel.completeOnboarding(onFinished)
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text(text = if (uiState.isLastPage) "Comenzar" else "Siguiente", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OnboardingPage(data: OnboardingPageData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp).background(SecondaryGreen.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = data.icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(56.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = data.title, style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = data.description, style = MaterialTheme.typography.bodyLarge, color = TextPrimary.copy(alpha = 0.75f))
    }
}

@Composable
private fun PagerIndicator(pageCount: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier.size(8.dp).background(
                    color = if (index == currentPage) PrimaryGreen else SecondaryGreen.copy(alpha = 0.4f),
                    shape = CircleShape
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingPreview() {
    BotaniKTheme { OnboardingPage(pages[0]) }
}
