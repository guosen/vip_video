package com.guosen.vipvideo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guosen.vipvideo.appContainer
import com.guosen.vipvideo.core.link.LinkResolveResult
import com.guosen.vipvideo.core.model.ParseSources
import com.guosen.vipvideo.parse.ParseWebActivity
import com.guosen.vipvideo.player.PlayerActivity
import kotlinx.coroutines.launch

@Composable
fun ParseLinkScreen() {
    val context = LocalContext.current
    val container = context.appContainer()
    val prefs = container.preferences
    val selectedId by prefs.selectedParseSourceId.collectAsState(initial = ParseSources.defaults.first().id)
    var url by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var hint by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "粘贴视频页分享链接。将优先识别片名并在无损云资源库播放；未收录时再使用网页解析。",
        )
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            placeholder = { Text("https://m.v.qq.com/... 或 v.qq.com/...") },
            minLines = 3,
            enabled = !loading,
        )
        Text(
            text = "网页解析源：${ParseSources.defaults.find { it.id == selectedId && !it.isCloudDirect }?.name ?: "麒麟解析"}（可在设置中更换）",
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodySmall,
        )
        hint?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
        Button(
            onClick = {
                val trimmed = url.trim()
                if (!trimmed.startsWith("http")) return@Button
                scope.launch {
                    loading = true
                    hint = null
                    when (val result = container.repository.resolveShareLink(trimmed)) {
                        is LinkResolveResult.NativePlay -> {
                            hint = "已匹配资源库：${result.item.title}"
                            context.startActivity(
                                PlayerActivity.intent(
                                    context = context,
                                    vodId = result.item.idResolved,
                                    title = result.item.title,
                                    poster = result.item.poster,
                                    episodeIndex = result.episodeIndex,
                                ),
                            )
                        }
                        is LinkResolveResult.WebParse -> {
                            hint = result.reason
                            val parserId = ParseSources.defaults.find { it.id == selectedId && !it.isCloudDirect }?.id
                                ?: ParseSources.defaults.first { !it.isCloudDirect }.id
                            context.startActivity(
                                ParseWebActivity.intent(context, result.normalizedUrl, parserId),
                            )
                        }
                        is LinkResolveResult.Failed -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = url.trim().startsWith("http") && !loading,
        ) {
            Text(if (loading) "解析中…" else "开始解析播放")
        }
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}
