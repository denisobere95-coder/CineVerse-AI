package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WatchlistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_items ORDER BY addedTimestamp DESC")
    fun getAllWatchlistItems(): Flow<List<WatchlistItemEntity>>

    @Query("SELECT * FROM watchlist_items WHERE isFavorite = 1 ORDER BY addedTimestamp DESC")
    fun getFavoriteMovies(): Flow<List<WatchlistItemEntity>>

    @Query("SELECT * FROM watchlist_items WHERE watchStatus = :status ORDER BY addedTimestamp DESC")
    fun getWatchlistByStatus(status: String): Flow<List<WatchlistItemEntity>>

    @Query("SELECT * FROM watchlist_items WHERE platform = :platform ORDER BY addedTimestamp DESC")
    fun getWatchlistByPlatform(platform: String): Flow<List<WatchlistItemEntity>>

    @Query("SELECT * FROM watchlist_items WHERE customListName = :listName ORDER BY addedTimestamp DESC")
    fun getWatchlistByCustomList(listName: String): Flow<List<WatchlistItemEntity>>

    @Query("SELECT DISTINCT customListName FROM watchlist_items WHERE customListName IS NOT NULL AND customListName != ''")
    fun getCustomListNames(): Flow<List<String>>

    @Query("SELECT * FROM watchlist_items WHERE mediaId = :mediaId LIMIT 1")
    suspend fun getItemById(mediaId: String): WatchlistItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateItem(item: WatchlistItemEntity)

    @Update
    suspend fun updateItem(item: WatchlistItemEntity)

    @Delete
    suspend fun deleteItem(item: WatchlistItemEntity)

    @Query("DELETE FROM watchlist_items WHERE mediaId = :mediaId")
    suspend fun deleteItemById(mediaId: String)

    @Query("UPDATE watchlist_items SET watchStatus = :status WHERE mediaId = :mediaId")
    suspend fun updateWatchStatus(mediaId: String, status: String)

    @Query("UPDATE watchlist_items SET isFavorite = :isFavorite WHERE mediaId = :mediaId")
    suspend fun updateFavoriteStatus(mediaId: String, isFavorite: Boolean)

    @Query("UPDATE watchlist_items SET userRating = :rating WHERE mediaId = :mediaId")
    suspend fun updateUserRating(mediaId: String, rating: Int)

    @Query("SELECT COUNT(*) FROM watchlist_items WHERE watchStatus = 'WATCHED'")
    fun getWatchedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM watchlist_items")
    suspend fun getWatchlistCount(): Int

    @Query("""
        SELECT * FROM watchlist_items 
        WHERE (:query = '' OR title LIKE '%' || :query || '%')
        AND (:platform = '' OR platform = :platform)
        AND (:genre = '' OR genre LIKE '%' || :genre || '%')
        ORDER BY addedTimestamp DESC
    """)
    fun searchAndFilterWatchlist(query: String, platform: String, genre: String): Flow<List<WatchlistItemEntity>>
}
