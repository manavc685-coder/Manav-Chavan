package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY addedTimestamp DESC")
    fun getAllMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isTrending = 1 ORDER BY rating DESC")
    fun getTrendingMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isPopular = 1 ORDER BY rating DESC")
    fun getPopularMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isLatest = 1 ORDER BY releaseYear DESC")
    fun getLatestMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isWebSeries = 1 OR type = 'SERIES'")
    fun getWebSeries(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE watchProgress > 0 ORDER BY lastWatchedPositionMs DESC")
    fun getContinueWatching(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isRecommended = 1 ORDER BY rating DESC")
    fun getRecommendedMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isInWatchlist = 1 ORDER BY addedTimestamp DESC")
    fun getWatchlist(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    fun getMediaById(id: String): Flow<MediaItem?>

    @Query("""
        SELECT * FROM media_items 
        WHERE title LIKE '%' || :query || '%' 
           OR genre LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%'
        ORDER BY rating DESC
    """)
    fun searchMedia(query: String): Flow<List<MediaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MediaItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItem>)

    @Update
    suspend fun update(item: MediaItem)

    @Delete
    suspend fun delete(item: MediaItem)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE media_items SET isInWatchlist = :inWatchlist WHERE id = :id")
    suspend fun setWatchlistStatus(id: String, inWatchlist: Boolean)

    @Query("UPDATE media_items SET watchProgress = :progress, lastWatchedPositionMs = :positionMs WHERE id = :id")
    suspend fun updateWatchProgress(id: String, progress: Float, positionMs: Long)

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun getCount(): Int
}
