package com.dorm.health.ui.screens.home

import android.widget.Toast
import com.dorm.health.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dorm.health.ui.components.AppBackground
import com.dorm.health.ui.screens.home.components.DormInfoCard
import com.dorm.health.ui.screens.home.components.EnvironmentGrid
import com.dorm.health.ui.screens.home.components.HealthScoreCard
import com.dorm.health.ui.screens.home.components.HomeActionButtons
import com.dorm.health.ui.screens.home.components.SuggestionCard
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun HomeScreen(
    onNavigateAnalytics: () -> Unit,
    onNavigateReport: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isAppInDarkTheme()
    val context = LocalContext.current
    val swipeState = rememberSwipeRefreshState(uiState.isRefreshing)

    LaunchedEffect(uiState.isNightOwl) {
        if (uiState.isNightOwl) {
            Toast.makeText(context, "检测到熬夜状态，健康评分已降低", Toast.LENGTH_SHORT).show()
        }
    }

    AppBackground(isDark = isDark) {
        SwipeRefresh(
            state = swipeState,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                uiState.welcomeMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = MaterialTheme.typography.titleLarge.lineHeight
                    )
                }
                DormInfoCard(
                    dormName = uiState.dormName,
                    memberCount = uiState.memberCount,
                    currentTime = uiState.currentTime,
                    healthLevel = uiState.healthLevel,
                    isDark = isDark
                )
                Spacer(Modifier.height(12.dp))
                EnvironmentGrid(uiState.snapshot, isDark)
                Spacer(Modifier.height(12.dp))
                HealthScoreCard(uiState.healthScore, uiState.rankingPercent, isDark)
                Spacer(Modifier.height(12.dp))
                SuggestionCard(uiState.suggestions, isDark)
                Spacer(Modifier.height(12.dp))
                HomeActionButtons(
                    onRefresh = { viewModel.refresh() },
                    onAnalytics = onNavigateAnalytics,
                    onReport = onNavigateReport
                )
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}
