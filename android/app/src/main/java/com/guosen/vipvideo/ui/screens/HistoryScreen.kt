package com.guosen.vipvideo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guosen.vipvideo.appContainer
@Composable
fun HistoryScreen(onOpenDetail: (Int) -> Unit) {
    val dao = LocalContext.current.appContainer().dao
    val history by dao.observeHistory().collectAsState(initial = emptyList())

    if (history.isEmpty()) {
        Text("暂无观看历史", modifier = Modifier.padding(16.dp))
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(history, key = { it.vodId }) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDetail(item.vodId) }
                    .padding(16.dp),
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${item.episodeName.ifBlank { "第${item.episodeIndex + 1}集" }} · 继续观看",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
