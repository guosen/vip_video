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
import com.guosen.vipvideo.core.model.SuggestItem
import com.guosen.vipvideo.core.model.VodItem
import com.guosen.vipvideo.ui.components.VodPosterCard
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(onOpenDetail: (Int) -> Unit) {
    val repository = LocalContext.current.appContainer().repository
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var suggests by remember { mutableStateOf<List<SuggestItem>>(emptyList()) }
    var results by remember { mutableStateOf<List<VodItem>>(emptyList()) }

    LaunchedEffect(query) {
        delay(350)
        if (query.trim().length < 2) {
            suggests = emptyList()
            results = emptyList()
            return@LaunchedEffect
        }
        loading = true
        suggests = repository.suggest(query)
        results = repository.search(query).first
        loading = false
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
        if (suggests.isNotEmpty() && results.isEmpty()) {
            Text("联想", modifier = Modifier.padding(top = 12.dp))
            suggests.take(8).forEach { s ->
                Text(
                    text = s.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onOpenDetail(s.id) },
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(results, key = { it.idResolved }) { item ->
                VodPosterCard(item = item, onClick = { onOpenDetail(item.idResolved) })
            }
        }
    }
}
