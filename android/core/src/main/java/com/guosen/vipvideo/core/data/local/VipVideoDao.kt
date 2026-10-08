package com.guosen.vipvideo.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VipVideoDao {
    @Query("SELECT * FROM watch_history ORDER BY updatedAt DESC LIMIT 50")
    fun observeHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE vodId = :vodId")
    suspend fun deleteHistory(vodId: Int)

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(item: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE vodId = :vodId")
    suspend fun removeFavorite(vodId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE vodId = :vodId)")
    fun observeIsFavorite(vodId: Int): Flow<Boolean>
}
