package com.guosen.vipvideo.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guosen.vipvideo.appContainer
import com.guosen.vipvideo.ui.components.SectionHeader
import com.guosen.vipvideo.ui.components.VodPosterCard
import com.guosen.vipvideo.ui.home.HomeViewModel
import com.guosen.vipvideo.ui.home.HomeViewModelFactory

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onOpenCategory: (typeId: Int, name: String) -> Unit,
    onOpenDetail: (Int) -> Unit,
) {
    val repository = LocalContext.current.appContainer().repository
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
    val uiState by viewModel.state.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh(force = true) },
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState),
    ) {
        when {
            uiState.error != null && uiState.latest.isEmpty() && uiState.categories.isEmpty() -> {
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            uiState.latest.isEmpty() && uiState.categories.isEmpty() && uiState.isRefreshing -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    Text(
                        text = "VIP影视",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                    SectionHeader("今日更新")
                }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
                        items(uiState.latest, key = { it.idResolved }) { item ->
                            VodPosterCard(
                                item = item,
                                onClick = { onOpenDetail(item.idResolved) },
                                modifier = Modifier.fillParentMaxWidth(0.38f),
                            )
                        }
                    }
                }
                uiState.categories.forEach { category ->
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
        PullRefreshIndicator(
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}
