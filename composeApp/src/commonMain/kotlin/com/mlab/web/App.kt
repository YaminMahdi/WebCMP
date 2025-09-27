package com.mlab.web

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        var url by remember { mutableStateOf("https://www.google.com") }
        var isLoading by remember { mutableStateOf(false) }
        var isRefreshing by remember { mutableStateOf(false) }
        var webViewRefresh by remember { mutableStateOf<(() -> Unit)?>(null) }

        val scope = rememberCoroutineScope()
        val pullToRefreshState = rememberPullToRefreshState()

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues)
            ) {
                // Progress indicator for page loading
                AnimatedVisibility(isLoading || isRefreshing) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Pull to refresh wrapper
                PullToRefreshBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        scope.launch {
                            isRefreshing = true
                            // Add a small delay for better UX
                            delay(500)
                            // Trigger WebView refresh using the lambda
                            webViewRefresh?.invoke()
                            isRefreshing = false
                        }
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NativeWebView(
                            url = url,
                            onRefresh = { refreshCallback ->
                                webViewRefresh = refreshCallback
                            },
                            isLoading = { loading ->
                                isLoading = loading
                            },
                            onUrlClicked = { clickedUrl ->
                                println("URL clicked: $clickedUrl")
                                // Optionally navigate to clicked URL
                                // url = clickedUrl
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}