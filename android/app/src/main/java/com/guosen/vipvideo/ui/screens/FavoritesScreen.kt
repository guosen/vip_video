package com.guosen.vipvideo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guosen.vipvideo.appContainer
import com.guosen.vipvideo.core.model.VodItem
import com.guosen.vipvideo.ui.components.VodPosterCard

@Composable
fun FavoritesScreen(onOpenDetail: (Int) -> Unit) {
    val dao = LocalContext.current.appContainer().dao
    val favorites by dao.observeFavorites().collectAsState(initial = emptyList())

    if (favorites.isEmpty()) {
        Text("暂无收藏", modifier = Modifier.fillMaxSize().padding(PaddingValues(16.dp)))
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(favorites, key = { it.vodId }) { fav ->
            VodPosterCard(
                item = VodItem(vodId = fav.vodId, vodName = fav.title, vodPic = fav.poster, typeName = fav.typeName),
                onClick = { onOpenDetail(fav.vodId) },
            )
        }
    }
}
