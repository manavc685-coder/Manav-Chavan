package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.MediaItem
import com.example.ui.AbhtrixViewModel
import com.example.ui.screens.admin.AdminPanelScreen
import com.example.ui.screens.details.MediaDetailScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.player.VideoPlayerScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.watchlist.WatchlistScreen
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

sealed class BottomTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    object Home : BottomTab("Home", Icons.Filled.Home, Icons.Outlined.Home, "tab_home")
    object Search : BottomTab("Search", Icons.Filled.Search, Icons.Outlined.Search, "tab_search")
    object Watchlist : BottomTab("Watchlist", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "tab_watchlist")
    object Profile : BottomTab("Profile", Icons.Filled.Person, Icons.Outlined.PersonOutline, "tab_profile")
}

@Composable
fun AbhtrixNavGraph(
    viewModel: AbhtrixViewModel,
    modifier: Modifier = Modifier
) {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = "splash",
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Splash Screen
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    rootNavController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 2. Main Shell with Bottom Navigation Bar
        composable("main") {
            MainShellScreen(
                viewModel = viewModel,
                onNavigateToDetail = { media ->
                    rootNavController.navigate("detail/${media.id}")
                },
                onNavigateToPlayer = { media, isTrailer ->
                    rootNavController.navigate("player/${media.id}/$isTrailer")
                },
                onNavigateToAdmin = {
                    rootNavController.navigate("admin")
                }
            )
        }

        // 3. Media Details Screen
        composable(
            route = "detail/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId") ?: ""
            val allMedia by viewModel.allMedia.collectAsStateWithLifecycle()
            val media = allMedia.find { it.id == mediaId }

            if (media != null) {
                val recommendations = allMedia.filter { it.id != media.id && it.genre.split("•").firstOrNull() == media.genre.split("•").firstOrNull() }
                    .ifEmpty { allMedia.filter { it.id != media.id }.take(6) }

                MediaDetailScreen(
                    media = media,
                    recommendations = recommendations,
                    onBackClick = { rootNavController.popBackStack() },
                    onWatchNowClick = {
                        rootNavController.navigate("player/${media.id}/false")
                    },
                    onTrailerClick = {
                        rootNavController.navigate("player/${media.id}/true")
                    },
                    onToggleWatchlist = { viewModel.toggleWatchlist(media.id) },
                    onMediaClick = { selected ->
                        rootNavController.navigate("detail/${selected.id}")
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize())
            }
        }

        // 4. Video Player Screen
        composable(
            route = "player/{mediaId}/{isTrailer}",
            arguments = listOf(
                navArgument("mediaId") { type = NavType.StringType },
                navArgument("isTrailer") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId") ?: ""
            val isTrailer = backStackEntry.arguments?.getBoolean("isTrailer") ?: false
            val allMedia by viewModel.allMedia.collectAsStateWithLifecycle()
            val media = allMedia.find { it.id == mediaId }

            if (media != null) {
                VideoPlayerScreen(
                    media = media,
                    isTrailer = isTrailer,
                    onBackClick = { rootNavController.popBackStack() },
                    onProgressUpdate = { progress, posMs ->
                        if (!isTrailer) {
                            viewModel.updateWatchProgress(media.id, progress, posMs)
                        }
                    }
                )
            }
        }

        // 5. Admin Panel Screen
        composable("admin") {
            val allMedia by viewModel.allMedia.collectAsStateWithLifecycle()
            AdminPanelScreen(
                catalog = allMedia,
                onAddOrUpdateMedia = { viewModel.addOrUpdateMedia(it) },
                onDeleteMedia = { viewModel.deleteMedia(it) },
                onResetCatalog = { viewModel.resetCatalog() },
                onBackClick = { rootNavController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainShellScreen(
    viewModel: AbhtrixViewModel,
    onNavigateToDetail: (MediaItem) -> Unit,
    onNavigateToPlayer: (MediaItem, Boolean) -> Unit,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = remember {
        listOf(
            BottomTab.Home,
            BottomTab.Search,
            BottomTab.Watchlist,
            BottomTab.Profile
        )
    }

    val trending by viewModel.trending.collectAsStateWithLifecycle()
    val popular by viewModel.popular.collectAsStateWithLifecycle()
    val latest by viewModel.latest.collectAsStateWithLifecycle()
    val webSeries by viewModel.webSeries.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val recommended by viewModel.recommended.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandCrimson,
                            selectedTextColor = BrandCrimson,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0x33FF2442)
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTabIndex) {
                0 -> HomeScreen(
                    trending = trending,
                    popular = popular,
                    latest = latest,
                    webSeries = webSeries,
                    continueWatching = continueWatching,
                    recommended = recommended,
                    onMediaClick = onNavigateToDetail,
                    onPlayClick = { onNavigateToPlayer(it, false) },
                    onToggleWatchlist = { viewModel.toggleWatchlist(it.id) },
                    onSearchClick = { selectedTabIndex = 1 }
                )
                1 -> SearchScreen(
                    searchQuery = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    selectedGenre = selectedGenre,
                    onGenreSelect = { viewModel.onGenreSelected(it) },
                    results = searchResults,
                    onMediaClick = onNavigateToDetail
                )
                2 -> WatchlistScreen(
                    watchlist = watchlist,
                    onMediaClick = onNavigateToDetail,
                    onRemoveFromWatchlist = { viewModel.toggleWatchlist(it) },
                    onExploreClick = { selectedTabIndex = 0 }
                )
                3 -> ProfileScreen(
                    watchHistory = continueWatching,
                    watchlistCount = watchlist.size,
                    onMediaClick = onNavigateToDetail,
                    onNavigateToWatchlist = { selectedTabIndex = 2 },
                    onNavigateToAdmin = onNavigateToAdmin
                )
            }
        }
    }
}
