package com.guosen.vipvideo

import android.content.Context
import com.guosen.vipvideo.core.data.UserPreferences
import com.guosen.vipvideo.core.data.local.VipVideoDao
import com.guosen.vipvideo.core.repository.VodRepository

data class AppContainer(
    val repository: VodRepository,
    val dao: VipVideoDao,
    val preferences: UserPreferences,
)

fun Context.appContainer(): AppContainer {
    val app = applicationContext as VipVideoApp
    return AppContainer(app.repository, app.database.dao(), app.userPreferences)
}
