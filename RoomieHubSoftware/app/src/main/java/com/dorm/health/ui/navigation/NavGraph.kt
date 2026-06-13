package com.dorm.health.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dorm.health.DormHealthApp
import com.dorm.health.ui.screens.analytics.AnalyticsScreen
import com.dorm.health.ui.screens.auth.LoginScreen
import com.dorm.health.ui.screens.home.HomeScreen
import com.dorm.health.ui.screens.profile.ProfileScreen
import com.dorm.health.ui.screens.report.ReportScreen
import com.dorm.health.ui.screens.rules.DormRulesScreen
import com.dorm.health.ui.screens.serverconnection.ServerConnectionScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector?) {
    data object Login : Screen("login", "登录", null)
    data object Home : Screen("home", "首页", Icons.Outlined.Home)
    data object Analytics : Screen("analytics", "分析", Icons.Outlined.Analytics)
    data object Rules : Screen("rules", "公约", Icons.Outlined.Article)
    data object Profile : Screen("profile", "我的", Icons.Outlined.Person)
    data object Report : Screen("report", "报告", null)
    data object ServerConnection : Screen("server_connection", "服务器连接", null)
}

@Composable
fun DormHealthNavHost() {
    val authRepo = DormHealthApp.instance.authRepository
    val isLoggedIn by authRepo.isLoggedIn.collectAsState()

    if (!isLoggedIn) {
        LoginScreen(onLoginSuccess = { /* isLoggedIn 状态更新后自动进入主界面 */ })
    } else {
        MainNavHost()
    }
}

@Composable
private fun MainNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val bottomRoutes = listOf(
        Screen.Home.route,
        Screen.Analytics.route,
        Screen.Rules.route,
        Screen.Profile.route
    )
    val showBottomBar = currentRoute in bottomRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    listOf(Screen.Home, Screen.Analytics, Screen.Rules, Screen.Profile).forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon!!, contentDescription = screen.label) },
                            label = {
                                Text(
                                    text = screen.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn() + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 4 } }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateAnalytics = { navController.navigate(Screen.Analytics.route) },
                    onNavigateReport = { navController.navigate(Screen.Report.route) }
                )
            }
            composable(Screen.Analytics.route) { AnalyticsScreen() }
            composable(Screen.Rules.route) { DormRulesScreen() }
            composable(Screen.Profile.route) {
                ProfileScreen(onNavigateServerConnection = {
                    navController.navigate(Screen.ServerConnection.route)
                })
            }
            composable(Screen.Report.route) {
                ReportScreen()
            }
            composable(Screen.ServerConnection.route) {
                ServerConnectionScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
