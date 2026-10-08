package com.guosen.vipvideo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guosen.vipvideo.appContainer
import com.guosen.vipvideo.core.model.VodItem
import com.guosen.vipvideo.ui.components.VodPosterCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(onOpenDetail: (Int) -> Unit) {
    val repository = LocalContext.current.appContainer().repository
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<VodItem>>(emptyList()) }

    LaunchedEffect(query) {
        delay(350)
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            results = emptyList()
            error = null
            loading = false
            return@LaunchedEffect
        }
        loading = true
        error = null
        try {
            results = repository.search(trimmed).first
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "搜索失败"
            results = emptyList()
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索电影、电视剧、动漫、综艺…") },
            singleLine = true,
        )
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
        error?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 8.dp),
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
            )
        }
        if (!loading && query.trim().length >= 2 && results.isEmpty() && error == null) {
            Text("未找到相关结果", modifier = Modifier.padding(top = 12.dp))
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            items(results, key = { it.idResolved }) { item ->
                VodPosterCard(item = item, onClick = { onOpenDetail(item.idResolved) })
            }
        }
    }
}
