package com.guosen.vipvideo.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val vodId: Int,
    val title: String,
    val poster: String?,
    val episodeIndex: Int,
    val episodeName: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val vodId: Int,
    val title: String,
    val poster: String?,
    val typeName: String?,
    val addedAt: Long,
)
