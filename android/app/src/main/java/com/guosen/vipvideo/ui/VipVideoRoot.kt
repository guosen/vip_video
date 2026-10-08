package com.guosen.vipvideo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.guosen.vipvideo.ui.screens.CategoryScreen
import com.guosen.vipvideo.ui.screens.DetailScreen
import com.guosen.vipvideo.ui.screens.FavoritesScreen
import com.guosen.vipvideo.ui.screens.HistoryScreen
import com.guosen.vipvideo.ui.screens.HomeScreen
import com.guosen.vipvideo.ui.screens.ParseLinkScreen
import com.guosen.vipvideo.ui.screens.SearchScreen
import com.guosen.vipvideo.ui.screens.SettingsScreen
import com.guosen.vipvideo.ui.tv.TvShell

sealed class MainTab(val route: String, val label: String) {
    data object Home : MainTab("home", "首页")
    data object Search : MainTab("search", "搜索")
    data object Parse : MainTab("parse", "链接")
    data object History : MainTab("history", "历史")
    data object Favorites : MainTab("favorites", "收藏")
    data object Settings : MainTab("settings", "设置")
}

private val mobileTabs = listOf(
    MainTab.Home,
    MainTab.Search,
    MainTab.Parse,
    MainTab.History,
    MainTab.Favorites,
    MainTab.Settings,
)

@Composable
fun VipVideoRoot(isTv: Boolean) {
    if (isTv) {
        TvShell()
        return
    }
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in mobileTabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    mobileTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        MainTab.Home -> Icons.Outlined.Home
                                        MainTab.Search -> Icons.Outlined.Search
                                        MainTab.Parse -> Icons.Outlined.Link
                                        MainTab.History -> Icons.Outlined.History
                                        MainTab.Favorites -> Icons.Outlined.Favorite
                                        MainTab.Settings -> Icons.Outlined.Settings
                                    },
                                    contentDescription = tab.label,
                                )
                            },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(MainTab.Home.route) {
                HomeScreen(
                    onOpenCategory = { typeId, name ->
                        navController.navigate("category/$typeId?name=$name")
                    },
                    onOpenDetail = { id -> navController.navigate("detail/$id") },
                )
            }
            composable(MainTab.Search.route) {
                SearchScreen(onOpenDetail = { id -> navController.navigate("detail/$id") })
            }
            composable(MainTab.Parse.route) { ParseLinkScreen() }
            composable(MainTab.History.route) {
                HistoryScreen(onOpenDetail = { id -> navController.navigate("detail/$id") })
            }
            composable(MainTab.Favorites.route) {
                FavoritesScreen(onOpenDetail = { id -> navController.navigate("detail/$id") })
            }
            composable(MainTab.Settings.route) { SettingsScreen() }
            composable(
                route = "category/{typeId}?name={name}",
                arguments = listOf(
                    navArgument("typeId") { type = NavType.IntType },
                    navArgument("name") { defaultValue = "" },
                ),
            ) { entry ->
                CategoryScreen(
                    typeId = entry.arguments?.getInt("typeId") ?: 1,
                    typeName = entry.arguments?.getString("name").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onOpenDetail = { id -> navController.navigate("detail/$id") },
                )
            }
            composable(
                route = "detail/{vodId}",
                arguments = listOf(navArgument("vodId") { type = NavType.IntType }),
            ) { entry ->
                DetailScreen(
                    vodId = entry.arguments?.getInt("vodId") ?: 0,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
