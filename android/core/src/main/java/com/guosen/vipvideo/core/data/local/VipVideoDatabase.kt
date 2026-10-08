package com.guosen.vipvideo.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WatchHistoryEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class VipVideoDatabase : RoomDatabase() {
    abstract fun dao(): VipVideoDao

    companion object {
        @Volatile
        private var instance: VipVideoDatabase? = null

        fun get(context: Context): VipVideoDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    VipVideoDatabase::class.java,
                    "vip_video.db",
                ).build().also { instance = it }
            }
        }
    }
}
