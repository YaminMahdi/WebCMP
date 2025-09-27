@file:Suppress("DEPRECATION")

package com.mlab.web

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun NativeWebView(
    url: String,
    onRefresh: (refreshCallback: () -> Unit) -> Unit,
    isLoading: (isLoading: Boolean) -> Unit,
    onUrlClicked: (url: String) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val processName = Application.getProcessName()
                    WebView.setDataDirectorySuffix(processName)
                }
            }
            scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            settings.apply {
                // Full JavaScript support
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true

                // DOM storage, cache, DB
                domStorageEnabled = true
                databaseEnabled = true

                // File access
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true

                // Viewport and zoom
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false

                // Mixed content (HTTP/HTTPS)
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                // Caching
                cacheMode = WebSettings.LOAD_DEFAULT
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(
                    view: WebView?,
                    url: String?,
                    favicon: Bitmap?
                ) {
                    isLoading(true)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    view?.scrollTo(view.contentHeight, 0)
                    isLoading(false)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val clickedUrl = request?.url.toString()
                    return if (
                        clickedUrl.contains("jpg") ||
                        clickedUrl.contains("png") ||
                        clickedUrl.contains("attachment_id")
                    ) {
                        true
                    } else {
                        onUrlClicked(clickedUrl)
                        true
                    }
                }
            }
        }
    }

    LaunchedEffect(webView) {
        onRefresh {
            webView.reload()
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { webView },
            update = { webView ->
                webView.loadUrl(url)
            }
        )
        BackHandler(webView.canGoBack()) {
            webView.goBack()
        }
    }
}
