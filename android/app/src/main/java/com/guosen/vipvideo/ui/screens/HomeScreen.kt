package com.guosen.vipvideo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guosen.vipvideo.appContainer
import com.guosen.vipvideo.core.model.HomeCategory
import com.guosen.vipvideo.core.model.VodItem
import com.guosen.vipvideo.ui.components.SectionHeader
import com.guosen.vipvideo.ui.components.VodPosterCard

@Composable
fun HomeScreen(
    onOpenCategory: (typeId: Int, name: String) -> Unit,
    onOpenDetail: (Int) -> Unit,
) {
    val repository = LocalContext.current.appContainer().repository
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var categories by remember { mutableStateOf<List<HomeCategory>>(emptyList()) }
    var latest by remember { mutableStateOf<List<VodItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        runCatching {
            categories = repository.loadHome()
            latest = repository.latestUpdates().first
        }.onFailure { error = it.message ?: "加载失败" }
        loading = false
    }

    when {
        loading -> CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        error != null -> Text(
            text = error.orEmpty(),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxSize(),
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Text(
                    text = "VIP影视",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxSize(),
                )
                SectionHeader("今日更新")
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
                    items(latest, key = { it.idResolved }) { item ->
                        VodPosterCard(
                            item = item,
                            onClick = { onOpenDetail(item.idResolved) },
                            modifier = Modifier.fillParentMaxWidth(0.38f),
                        )
                    }
                }
            }
            categories.forEach { category ->
                item {
                    SectionHeader(category.typeName) {
                        onOpenCategory(category.typeId, category.typeName)
                    }
                }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
                        items(category.items, key = { it.idResolved }) { item ->
                            VodPosterCard(
                                item = item,
                                onClick = { onOpenDetail(item.idResolved) },
                                modifier = Modifier.fillParentMaxWidth(0.38f),
                            )
                        }
                    }
                }
            }
        }
    }
}
