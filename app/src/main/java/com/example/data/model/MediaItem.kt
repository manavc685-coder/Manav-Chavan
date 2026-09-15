package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: String = "MOVIE", // "MOVIE" or "SERIES"
    val releaseYear: Int,
    val genre: String,
    val rating: Double,
    val duration: String,
    val description: String,
    val posterUrl: String,
    val backdropUrl: String,
    val videoUrl: String,
    val trailerUrl: String,
    val category: String,
    val isTrending: Boolean = false,
    val isPopular: Boolean = false,
    val isLatest: Boolean = false,
    val isWebSeries: Boolean = false,
    val isRecommended: Boolean = false,
    val isInWatchlist: Boolean = false,
    val watchProgress: Float = 0f, // 0.0 to 1.0 for continue watching
    val lastWatchedPositionMs: Long = 0L,
    val cast: String = "Marcus Vance, Maya Lin, David Chen",
    val director: String = "Elena Rostova",
    val ageRating: String = "16+",
    val totalSeasons: Int = 1,
    val addedTimestamp: Long = System.currentTimeMillis()
)

data class Episode(
    val episodeNumber: Int,
    val title: String,
    val duration: String,
    val description: String,
    val thumbnailUrl: String
)
