package com.guosen.vipvideo

import android.app.Application
import com.guosen.vipvideo.core.data.UserPreferences
import com.guosen.vipvideo.core.data.local.VipVideoDatabase
import com.guosen.vipvideo.core.repository.VodRepository

class VipVideoApp : Application() {
    val repository by lazy { VodRepository() }
    val database by lazy { VipVideoDatabase.get(this) }
    val userPreferences by lazy { UserPreferences(this) }
}
