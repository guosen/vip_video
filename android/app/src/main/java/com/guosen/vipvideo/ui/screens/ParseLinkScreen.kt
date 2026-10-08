package com.guosen.vipvideo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guosen.vipvideo.appContainer
import com.guosen.vipvideo.core.model.ParseSources
import com.guosen.vipvideo.parse.ParseWebActivity

@Composable
fun ParseLinkScreen() {
    val context = LocalContext.current
    val prefs = LocalContext.current.appContainer().preferences
    val selectedId by prefs.selectedParseSourceId.collectAsState(initial = ParseSources.defaults.first().id)
    var url by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("粘贴爱奇艺 / 腾讯 / 优酷 / 芒果 / B站 等视频页链接，使用解析源播放。")
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            placeholder = { Text("https://...") },
            minLines = 3,
        )
        Text(
            text = "当前解析源：${ParseSources.defaults.find { it.id == selectedId }?.name ?: selectedId}",
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Button(
            onClick = {
                val trimmed = url.trim()
                if (trimmed.startsWith("http")) {
                    val parser = ParseSources.defaults.find { it.id == selectedId }
                    if (parser?.isCloudDirect == true) {
                        // Cloud mode expects title search; for raw links use first iframe parser.
                        val fallback = ParseSources.defaults.first { !it.isCloudDirect }
                        context.startActivity(ParseWebActivity.intent(context, trimmed, fallback.id))
                    } else {
                        context.startActivity(ParseWebActivity.intent(context, trimmed, selectedId))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = url.trim().startsWith("http"),
        ) {
            Text("开始解析播放")
        }
    }
}
