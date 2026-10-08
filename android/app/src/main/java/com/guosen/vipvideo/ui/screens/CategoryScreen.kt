package com.guosen.vipvideo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guosen.vipvideo.appContainer
import com.guosen.vipvideo.core.model.VodItem
import com.guosen.vipvideo.ui.components.VodPosterCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    typeId: Int,
    typeName: String,
    onBack: () -> Unit,
    onOpenDetail: (Int) -> Unit,
) {
    val repository = LocalContext.current.appContainer().repository
    var page by remember { mutableIntStateOf(1) }
    var items by remember { mutableStateOf<List<VodItem>>(emptyList()) }

    LaunchedEffect(typeId, page) {
        val (list, _) = repository.listByType(typeId, page)
        items = if (page == 1) list else items + list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(typeName.ifBlank { "分类" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(items, key = { it.idResolved }) { item ->
                VodPosterCard(item = item, onClick = { onOpenDetail(item.idResolved) })
            }
        }
    }
}
