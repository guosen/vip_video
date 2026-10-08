package com.guosen.vipvideo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.guosen.vipvideo.appContainer
import com.guosen.vipvideo.core.data.local.FavoriteEntity
import com.guosen.vipvideo.core.model.Episode
import com.guosen.vipvideo.core.model.VodItem
import com.guosen.vipvideo.player.PlayerActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(vodId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val container = context.appContainer()
    var loading by remember { mutableStateOf(true) }
    var detail by remember { mutableStateOf<VodItem?>(null) }
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    val isFavorite by container.dao.observeIsFavorite(vodId).collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    LaunchedEffect(vodId) {
        loading = true
        detail = container.repository.detail(vodId)
        episodes = container.repository.episodes(vodId)
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.title ?: "详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val d = detail ?: return@launch
                                if (isFavorite) {
                                    container.dao.removeFavorite(vodId)
                                } else {
                                    container.dao.addFavorite(
                                        FavoriteEntity(
                                            vodId = vodId,
                                            title = d.title,
                                            poster = d.poster,
                                            typeName = d.typeName,
                                            addedAt = System.currentTimeMillis(),
                                        ),
                                    )
                                }
                            }
                        },
                    ) {
                        Icon(
                            if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "收藏",
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            loading -> CircularProgressIndicator(Modifier.fillMaxSize())
            detail == null -> Text("未找到内容", modifier = Modifier.padding(padding))
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                AsyncImage(
                    model = detail?.poster,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = detail?.title.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp),
                )
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    detail?.typeName?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                    detail?.vodYear?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                    detail?.vodRemarks?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                }
                Text(
                    text = detail?.vodContent?.replace(Regex("<[^>]+>"), "").orEmpty(),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "选集",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(episodes) { index, ep ->
                        AssistChip(
                            onClick = {
                                val intent = PlayerActivity.intent(
                                    context = context,
                                    vodId = vodId,
                                    title = detail?.title.orEmpty(),
                                    poster = detail?.poster,
                                    episodeIndex = index,
                                    episodes = episodes,
                                )
                                context.startActivity(intent)
                            },
                            label = { Text(ep.name.ifBlank { "第${index + 1}集" }) },
                        )
                    }
                }
            }
        }
    }
}
