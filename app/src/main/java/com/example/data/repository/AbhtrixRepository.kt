package com.example.data.repository

import android.content.Context
import com.example.data.local.AbhtrixDatabase
import com.example.data.local.MediaDao
import com.example.data.model.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AbhtrixRepository(private val mediaDao: MediaDao) {

    val allMedia: Flow<List<MediaItem>> = mediaDao.getAllMedia()
    val trendingMedia: Flow<List<MediaItem>> = mediaDao.getTrendingMedia()
    val popularMedia: Flow<List<MediaItem>> = mediaDao.getPopularMedia()
    val latestMedia: Flow<List<MediaItem>> = mediaDao.getLatestMedia()
    val webSeries: Flow<List<MediaItem>> = mediaDao.getWebSeries()
    val continueWatching: Flow<List<MediaItem>> = mediaDao.getContinueWatching()
    val recommendedMedia: Flow<List<MediaItem>> = mediaDao.getRecommendedMedia()
    val watchlist: Flow<List<MediaItem>> = mediaDao.getWatchlist()

    suspend fun getMediaById(id: String): Flow<MediaItem?> = mediaDao.getMediaById(id)

    fun search(query: String): Flow<List<MediaItem>> = mediaDao.searchMedia(query)

    suspend fun toggleWatchlist(id: String) {
        val item = mediaDao.getMediaById(id).first()
        item?.let {
            mediaDao.setWatchlistStatus(id, !it.isInWatchlist)
        }
    }

    suspend fun updateWatchProgress(id: String, progress: Float, positionMs: Long) {
        mediaDao.updateWatchProgress(id, progress, positionMs)
    }

    // Admin Panel CRUD Operations
    suspend fun addMedia(item: MediaItem) {
        mediaDao.insert(item)
    }

    suspend fun updateMedia(item: MediaItem) {
        mediaDao.update(item)
    }

    suspend fun deleteMedia(id: String) {
        mediaDao.deleteById(id)
    }

    suspend fun resetToDefaultCatalog() {
        val catalog = DefaultSampleData.getInitialCatalog()
        mediaDao.insertAll(catalog)
    }

    companion object {
        @Volatile
        private var INSTANCE: AbhtrixRepository? = null

        fun getInstance(context: Context): AbhtrixRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AbhtrixDatabase.getDatabase(context)
                val repo = AbhtrixRepository(db.mediaDao())
                INSTANCE = repo
                // Seed initial catalog if needed
                CoroutineScope(Dispatchers.IO).launch {
                    val count = db.mediaDao().getCount()
                    if (count == 0) {
                        repo.resetToDefaultCatalog()
                    }
                }
                repo
            }
        }
    }
}
