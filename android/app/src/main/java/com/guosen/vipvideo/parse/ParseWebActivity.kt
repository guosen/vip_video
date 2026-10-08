package com.guosen.vipvideo.parse

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import com.guosen.vipvideo.core.link.VideoLinkResolver
import com.guosen.vipvideo.core.model.ParseSource
import com.guosen.vipvideo.core.model.ParseSources
import com.guosen.vipvideo.core.network.NetworkModule

class ParseWebActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var fullScreenView: FrameLayout
    private var parserQueue: List<ParseSource> = emptyList()
    private var parserIndex = 0
    private lateinit var targetVideoUrl: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rawTarget = intent.getStringExtra(EXTRA_TARGET_URL).orEmpty()
        targetVideoUrl = when {
            rawTarget.contains("m.v.qq.com", ignoreCase = true) -> rawTarget
            else -> VideoLinkResolver.normalizePageUrl(rawTarget)
        }
        val parserId = intent.getStringExtra(EXTRA_PARSER_ID)
        val preferred = ParseSources.defaults.filter { !it.isCloudDirect }
        val first = preferred.find { it.id == parserId } ?: preferred.first()
        parserQueue = listOf(first) + preferred.filter { it.id != first.id }
        parserIndex = 0

        fullScreenView = FrameLayout(this)
        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.userAgentString = NetworkModule.webViewUserAgent
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.evaluateJavascript(
                        """
                        (function(){
                          var t=(document.body&&document.body.innerText)||'';
                          if(/解析失败|播放失败|无法解析|未找到|无效链接|系统错误/.test(t)){
                            return 'fail';
                          }
                          return 'ok';
                        })();
                        """.trimIndent(),
                    ) { value ->
                        if (value == "\"fail\"") tryNextParser()
                    }
                }
            }
        }
        fullScreenView.addView(webView)
        setContentView(fullScreenView)
        loadCurrentParser()

        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) webView.goBack() else finish()
        }
    }

    private fun loadCurrentParser() {
        val parser = parserQueue.getOrNull(parserIndex) ?: return
        webView.loadUrl(buildParseUrl(parser, targetVideoUrl))
    }

    private fun tryNextParser() {
        if (parserIndex + 1 >= parserQueue.size) {
            Toast.makeText(
                this,
                "全部解析源均失败，请改用「搜索」按片名播放",
                Toast.LENGTH_LONG,
            ).show()
            return
        }
        parserIndex += 1
        val next = parserQueue[parserIndex]
        Toast.makeText(this, "正在切换解析源：${next.name}", Toast.LENGTH_SHORT).show()
        loadCurrentParser()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    private fun buildParseUrl(parser: ParseSource, videoUrl: String): String {
        val encoded = java.net.URLEncoder.encode(videoUrl, Charsets.UTF_8.name())
        return parser.baseUrl.orEmpty() + encoded
    }

    companion object {
        private const val EXTRA_TARGET_URL = "target_url"
        private const val EXTRA_PARSER_ID = "parser_id"

        fun intent(context: Context, videoUrl: String, parserId: String): Intent {
            return Intent(context, ParseWebActivity::class.java).apply {
                putExtra(EXTRA_TARGET_URL, videoUrl)
                putExtra(EXTRA_PARSER_ID, parserId)
            }
        }
    }
}
