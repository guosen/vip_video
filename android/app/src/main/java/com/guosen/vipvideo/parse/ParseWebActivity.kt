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
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import com.guosen.vipvideo.core.model.ParseSource
import com.guosen.vipvideo.core.model.ParseSources

class ParseWebActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var fullScreenView: FrameLayout

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetUrl = intent.getStringExtra(EXTRA_TARGET_URL).orEmpty()
        val parserId = intent.getStringExtra(EXTRA_PARSER_ID)
        val parser = ParseSources.defaults.find { it.id == parserId } ?: ParseSources.defaults.first { !it.isCloudDirect }
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
            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
                    super.onShowCustomView(view, callback)
                }
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false
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
        return parser.baseUrl + encoded
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
