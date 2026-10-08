package com.guosen.vipvideo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guosen.vipvideo.core.model.HomeCategory
import com.guosen.vipvideo.core.model.VodItem
import com.guosen.vipvideo.core.repository.HomeFeedCache
import com.guosen.vipvideo.core.repository.VodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val latest: List<VodItem> = HomeFeedCache.getFresh()?.latest.orEmpty(),
    val categories: List<HomeCategory> = HomeFeedCache.getFresh()?.categories.orEmpty(),
    val isRefreshing: Boolean = HomeFeedCache.getFresh() == null,
    val error: String? = null,
)

class HomeViewModel(
    private val repository: VodRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refresh(force = HomeFeedCache.getFresh() == null)
    }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            val hasCache = HomeFeedCache.getFresh() != null
            _state.update {
                it.copy(
                    isRefreshing = !hasCache || force,
                    error = null,
                )
            }
            runCatching { repository.loadHome(forceRefresh = force || !hasCache) }
                .onSuccess { feed ->
                    _state.update {
                        it.copy(
                            latest = feed.latest,
                            categories = feed.categories,
                            isRefreshing = false,
                            error = null,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            error = if (it.latest.isEmpty() && it.categories.isEmpty()) {
                                err.message ?: "加载失败"
                            } else {
                                null
                            },
                        )
                    }
                }
        }
    }
}
