package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    catalog: List<MediaItem>,
    onAddOrUpdateMedia: (MediaItem) -> Unit,
    onDeleteMedia: (String) -> Unit,
    onResetCatalog: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingItem by remember { mutableStateOf<MediaItem?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<MediaItem?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ABHTRIX Admin Studio",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Catalog & Content Management",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.testTag("admin_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Catalog",
                            tint = BrandCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreatingNew = true },
                containerColor = BrandCrimson,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("admin_add_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add New Movie")
            }
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_panel_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Media Titles (${catalog.size})",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(catalog, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(72.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            AsyncImage(
                                model = item.posterUrl,
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.type} • ${item.releaseYear} • ${item.category}",
                                color = BrandCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = item.genre,
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Edit Button
                        IconButton(
                            onClick = { editingItem = item },
                            modifier = Modifier.testTag("admin_edit_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White
                            )
                        }

                        // Delete Button
                        IconButton(
                            onClick = { itemToDelete = item },
                            modifier = Modifier.testTag("admin_delete_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFFF5252)
                            )
                        }
                    }
                }
            }
        }

        // Add/Edit Dialog
        if (isCreatingNew || editingItem != null) {
            val itemToEdit = editingItem
            MediaEditDialog(
                initialItem = itemToEdit,
                onDismiss = {
                    isCreatingNew = false
                    editingItem = null
                },
                onSave = { updatedItem ->
                    onAddOrUpdateMedia(updatedItem)
                    isCreatingNew = false
                    editingItem = null
                }
            )
        }

        // Delete Confirmation Dialog
        itemToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("Delete '${item.title}'?", color = TextPrimary) },
                text = { Text("This will permanently remove this title from the local database.", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteMedia(item.id)
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = DarkSurfaceElevated
            )
        }

        // Reset Catalog Dialog
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("Reset to Default Sample Catalog?", color = TextPrimary) },
                text = { Text("This will re-populate all default trending movies and web-series.", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            onResetCatalog()
                            showResetConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCrimson)
                    ) {
                        Text("Reset Catalog", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = DarkSurfaceElevated
            )
        }
    }
}

@Composable
private fun MediaEditDialog(
    initialItem: MediaItem?,
    onDismiss: () -> Unit,
    onSave: (MediaItem) -> Unit
) {
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var type by remember { mutableStateOf(initialItem?.type ?: "MOVIE") }
    var releaseYear by remember { mutableStateOf((initialItem?.releaseYear ?: 2026).toString()) }
    var genre by remember { mutableStateOf(initialItem?.genre ?: "Sci-Fi • Thriller") }
    var rating by remember { mutableStateOf((initialItem?.rating ?: 9.0).toString()) }
    var duration by remember { mutableStateOf(initialItem?.duration ?: "2h 10m") }
    var category by remember { mutableStateOf(initialItem?.category ?: "Trending") }
    var description by remember { mutableStateOf(initialItem?.description ?: "") }
    var posterUrl by remember { mutableStateOf(initialItem?.posterUrl ?: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80") }
    var backdropUrl by remember { mutableStateOf(initialItem?.backdropUrl ?: "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1200&auto=format&fit=crop&q=80") }
    var videoUrl by remember { mutableStateOf(initialItem?.videoUrl ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") }
    var trailerUrl by remember { mutableStateOf(initialItem?.trailerUrl ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem != null) "Edit Media Title" else "Add New Media",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminTextField("Title", title) { title = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AdminTextField("Type (MOVIE/SERIES)", type) { type = it }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AdminTextField("Year", releaseYear) { releaseYear = it }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AdminTextField("Rating (0.0-10.0)", rating) { rating = it }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AdminTextField("Duration", duration) { duration = it }
                    }
                }
                AdminTextField("Category (Trending/Popular/Latest/Web Series)", category) { category = it }
                AdminTextField("Genre(s)", genre) { genre = it }
                AdminTextField("Description", description, singleLine = false) { description = it }
                AdminTextField("Poster Image URL", posterUrl) { posterUrl = it }
                AdminTextField("Backdrop Image URL", backdropUrl) { backdropUrl = it }
                AdminTextField("Video Stream URL (MP4)", videoUrl) { videoUrl = it }
                AdminTextField("Trailer Stream URL (MP4)", trailerUrl) { trailerUrl = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalItem = MediaItem(
                        id = initialItem?.id ?: UUID.randomUUID().toString(),
                        title = title.ifBlank { "Untitled" },
                        type = if (type.contains("SERIES", ignoreCase = true)) "SERIES" else "MOVIE",
                        releaseYear = releaseYear.toIntOrNull() ?: 2026,
                        genre = genre.ifBlank { "Action • Cinema" },
                        rating = rating.toDoubleOrNull() ?: 8.5,
                        duration = duration.ifBlank { "2h 00m" },
                        category = category.ifBlank { "Trending" },
                        description = description.ifBlank { "Exciting cinematic release on ABHTRIX." },
                        posterUrl = posterUrl,
                        backdropUrl = backdropUrl,
                        videoUrl = videoUrl,
                        trailerUrl = trailerUrl,
                        isTrending = category.contains("Trending", ignoreCase = true),
                        isPopular = category.contains("Popular", ignoreCase = true),
                        isLatest = category.contains("Latest", ignoreCase = true),
                        isWebSeries = type.contains("SERIES", ignoreCase = true) || category.contains("Series", ignoreCase = true),
                        isRecommended = true,
                        isInWatchlist = initialItem?.isInWatchlist ?: false
                    )
                    onSave(finalItem)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandCrimson)
            ) {
                Text("Save to Catalog", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurfaceElevated
    )
}

@Composable
private fun AdminTextField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandCrimson,
            unfocusedBorderColor = DarkSurface,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
