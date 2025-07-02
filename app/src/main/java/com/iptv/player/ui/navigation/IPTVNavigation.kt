package com.iptv.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.iptv.player.ui.screens.home.HomeScreen
import com.iptv.player.ui.screens.sources.SourcesScreen
import com.iptv.player.ui.screens.sources.AddSourceScreen
import com.iptv.player.ui.screens.channels.ChannelsScreen

@Composable
fun IPTVNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Sources.route) {
            SourcesScreen(navController = navController)
        }
        
        composable(Screen.AddSource.route) {
            AddSourceScreen(navController = navController)
        }
        
        composable(Screen.Channels.route) {
            ChannelsScreen(navController = navController)
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Sources : Screen("sources")
    object AddSource : Screen("add_source")
    object Channels : Screen("channels")
    object Player : Screen("player")
}