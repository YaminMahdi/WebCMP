package com.mlab.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
expect fun NativeWebView(
    url: String,
    onRefresh: (refreshCallback: () -> Unit) -> Unit,
    isLoading: (isLoading: Boolean) -> Unit,
    onUrlClicked: (url: String) -> Unit,
    modifier: Modifier
)