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

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetUrl = VideoLinkResolver.normalizePageUrl(
            intent.getStringExtra(EXTRA_TARGET_URL).orEmpty(),
        )
        val parserId = intent.getStringExtra(EXTRA_PARSER_ID)
        val parser = ParseSources.defaults.find { it.id == parserId && !it.isCloudDirect }
            ?: ParseSources.defaults.first { !it.isCloudDirect }
        val loadUrl = buildParseUrl(parser, targetUrl)

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
                          if(/解析失败|播放失败|未找到|无效链接/.test(t)){
                            return 'fail';
                          }
                          return 'ok';
                        })();
                        """.trimIndent(),
                    ) { value ->
                        if (value == "\"fail\"") {
                            Toast.makeText(
                                this@ParseWebActivity,
                                "当前解析源失败，请返回后在「设置」更换解析源重试",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                }
            }
            loadUrl(loadUrl)
        }
        fullScreenView.addView(webView)
        setContentView(fullScreenView)

        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) webView.goBack() else finish()
        }
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
