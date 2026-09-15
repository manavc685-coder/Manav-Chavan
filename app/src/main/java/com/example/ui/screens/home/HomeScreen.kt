package com.example.ui.screens.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.ui.components.HeroBanner
import com.example.ui.components.MediaSectionRow
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextPrimary

@Composable
fun HomeScreen(
    trending: List<MediaItem>,
    popular: List<MediaItem>,
    latest: List<MediaItem>,
    webSeries: List<MediaItem>,
    continueWatching: List<MediaItem>,
    recommended: List<MediaItem>,
    onMediaClick: (MediaItem) -> Unit,
    onPlayClick: (MediaItem) -> Unit,
    onToggleWatchlist: (MediaItem) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val featuredItem = trending.firstOrNull() ?: popular.firstOrNull()

    Scaffold(
        containerColor = DarkBackground,
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = innerPadding.calculateBottomPadding() + 20.dp)
            ) {
                // Hero Banner
                if (featuredItem != null) {
                    HeroBanner(
                        media = featuredItem,
                        onPlayClick = { onPlayClick(featuredItem) },
                        onDetailsClick = { onMediaClick(featuredItem) },
                        onToggleWatchlist = { onToggleWatchlist(featuredItem) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Continue Watching (with progress indicators)
                if (continueWatching.isNotEmpty()) {
                    MediaSectionRow(
                        title = "Continue Watching",
                        items = continueWatching,
                        onItemClick = onMediaClick,
                        cardWidth = 145.dp,
                        cardHeight = 195.dp,
                        showProgress = true
                    )
                }

                // Trending Now
                MediaSectionRow(
                    title = "Trending Now",
                    items = trending,
                    onItemClick = onMediaClick
                )

                // Web Series
                MediaSectionRow(
                    title = "Popular Web Series",
                    items = webSeries,
                    onItemClick = onMediaClick
                )

                // Popular Movies
                MediaSectionRow(
                    title = "Popular Movies",
                    items = popular,
                    onItemClick = onMediaClick
                )

                // Latest Movies
                MediaSectionRow(
                    title = "Latest Releases",
                    items = latest,
                    onItemClick = onMediaClick
                )

                // Recommended For You
                MediaSectionRow(
                    title = "Recommended For You",
                    items = recommended,
                    onItemClick = onMediaClick
                )

                Spacer(modifier = Modifier.height(30.dp))
            }

            // Floating Top Header Bar (Over Hero Banner)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xCC0A0B10), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wordmark Logo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ABH",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "TRIX",
                        color = BrandCrimson,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // Quick Action Icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0x33000000), CircleShape)
                            .testTag("home_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { /* Cast feature affordance */ },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0x33000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = "Chromecast",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}
