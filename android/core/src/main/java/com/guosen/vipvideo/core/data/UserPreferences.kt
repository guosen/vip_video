package com.guosen.vipvideo.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.guosen.vipvideo.core.model.ParseSources
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "vip_video_prefs")

class UserPreferences(private val context: Context) {
    private val parseSourceKey = stringPreferencesKey("parse_source_id")

    val selectedParseSourceId = context.dataStore.data.map { prefs ->
        prefs[parseSourceKey] ?: ParseSources.defaults.first().id
    }

    suspend fun setParseSourceId(id: String) {
        context.dataStore.edit { it[parseSourceKey] = id }
    }
}
