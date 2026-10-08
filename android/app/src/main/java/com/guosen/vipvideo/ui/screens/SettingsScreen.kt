package com.guosen.vipvideo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guosen.vipvideo.appContainer
import com.guosen.vipvideo.core.model.ParseSources
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = context.appContainer().preferences
    val selectedId by prefs.selectedParseSourceId.collectAsState(initial = ParseSources.defaults.first().id)
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("默认解析源", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "与开源脚本 video_vip 保持同一组解析接口，可在「链接」页粘贴 VIP 页面地址播放。",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        ParseSources.defaults.forEach { source ->
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { scope.launch { prefs.setParseSourceId(source.id) } }
                    .padding(vertical = 8.dp),
            ) {
                RowWithRadio(
                    selected = selectedId == source.id,
                    title = source.name,
                    subtitle = when {
                        source.isCloudDirect -> "应用内搜索/选集直连（推荐）"
                        else -> source.baseUrl.orEmpty()
                    },
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
        Text(
            text = "免责声明：解析接口来自第三方，仅供学习交流。请支持正版。",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun RowWithRadio(selected: Boolean, title: String, subtitle: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
