package com.guosen.vipvideo.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guosen.vipvideo.appContainer
import com.guosen.vipvideo.core.model.HomeCategory
import com.guosen.vipvideo.core.model.VodItem
import com.guosen.vipvideo.ui.components.VodPosterCard
import com.guosen.vipvideo.ui.screens.DetailScreen

private enum class TvSection(val label: String) {
    Home("首页"),
    Search("搜索"),
    Settings("设置"),
}

@Composable
fun TvShell() {
    var section by remember { mutableStateOf(TvSection.Home) }
    var selectedVodId by remember { mutableIntStateOf(0) }
    var showDetail by remember { mutableStateOf(false) }

    if (showDetail && selectedVodId > 0) {
        DetailScreen(vodId = selectedVodId, onBack = { showDetail = false })
        return
    }

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF0B1220))) {
        Column(
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("VIP影视 TV", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            TvSection.entries.forEach { item ->
                Box(
                    modifier = Modifier
                        .focusable()
                        .onFocusChanged { if (it.isFocused) section = item }
                        .padding(8.dp),
                ) {
                    Text(
                        text = item.label,
                        color = if (section == item) MaterialTheme.colorScheme.primary else Color.White,
                    )
                }
            }
        }
        when (section) {
            TvSection.Home -> TvHome(onOpenDetail = { id ->
                selectedVodId = id
                showDetail = true
            })
            TvSection.Search -> TvSearch(onOpenDetail = { id ->
                selectedVodId = id
                showDetail = true
            })
            TvSection.Settings -> com.guosen.vipvideo.ui.screens.SettingsScreen()
        }
    }
}

@Composable
private fun TvHome(onOpenDetail: (Int) -> Unit) {
    val repository = LocalContext.current.appContainer().repository
    var categories by remember { mutableStateOf<List<HomeCategory>>(emptyList()) }

    LaunchedEffect(Unit) {
        categories = repository.loadHome()
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        categories.forEach { category ->
            item {
                Text(category.typeName, style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            items(category.items, key = { it.idResolved }) { item ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    VodPosterCard(
                        item = item,
                        onClick = { onOpenDetail(item.idResolved) },
                        focusable = true,
                        modifier = Modifier.width(160.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TvSearch(onOpenDetail: (Int) -> Unit) {
    // TV 端简化为展示「今日更新」列表；完整键盘搜索可在后续版本接入 Leanback SearchSupportFragment。
    val repository = LocalContext.current.appContainer().repository
    var items by remember { mutableStateOf<List<VodItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        items = repository.latestUpdates().first
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        items(items, key = { it.idResolved }) { item ->
            VodPosterCard(item = item, onClick = { onOpenDetail(item.idResolved) }, focusable = true)
        }
    }
}
