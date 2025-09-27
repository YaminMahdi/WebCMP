package com.mlab.web

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.UIView
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKNavigationTypeLinkActivated
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWebsiteDataStore
import platform.WebKit.javaScriptEnabled
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeWebView(
    url: String,
    onRefresh: (refreshCallback: () -> Unit) -> Unit,
    isLoading: (isLoading: Boolean) -> Unit,
    onUrlClicked: (url: String) -> Unit,
    modifier: Modifier
) {
    // Configure WebView with full access
    val config = remember {
        WKWebViewConfiguration().apply {
            allowsInlineMediaPlayback = true
            allowsAirPlayForMediaPlayback = true
            allowsPictureInPictureMediaPlayback = true
            mediaTypesRequiringUserActionForPlayback = 0u // Allow all media to autoplay

            preferences.apply {
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
            }

            // Use default data store for cookies and storage
            websiteDataStore = WKWebsiteDataStore.defaultDataStore()
        }
    }

    // Create WebView instance
    val webView = remember {
        WKWebView(frame = CGRectZero.readValue(), configuration = config).apply {
            // Set user agent to avoid mobile redirects
            customUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"

            // Enable scrolling
            scrollView.scrollEnabled = true
            scrollView.bounces = true

            // Auto layout
            translatesAutoresizingMaskIntoConstraints = false
        }
    }

    // Define the WKNavigationDelegate
    val navigationDelegate = remember {
        createWebViewDelegate(onUrlClicked, isLoading)
    }

    // Set delegate
    webView.navigationDelegate = navigationDelegate

    // Provide the refresh callback to the parent
    LaunchedEffect(webView) {
        onRefresh {
            webView.reload()
        }
    }

    UIKitView(factory = {
        val container = UIView().apply {
            translatesAutoresizingMaskIntoConstraints = false
        }

        // Add webview to container
        container.addSubview(webView)

        // Set up constraints for webview to fill container
        webView.topAnchor.constraintEqualToAnchor(container.topAnchor).active = true
        webView.bottomAnchor.constraintEqualToAnchor(container.bottomAnchor).active = true
        webView.leadingAnchor.constraintEqualToAnchor(container.leadingAnchor).active = true
        webView.trailingAnchor.constraintEqualToAnchor(container.trailingAnchor).active = true

        container
    },
        modifier = modifier.fillMaxSize(),
        update = { _ ->
            // Load URL when it changes
            val nsUrl = NSURL(string = url)
            val request = NSURLRequest(nsUrl)
            webView.loadRequest(request)
        },
        properties = UIKitInteropProperties(isInteractive = true, isNativeAccessibilityEnabled = true))
}

@OptIn(ExperimentalForeignApi::class)
private fun createWebViewDelegate(
    onUrlClicked: (String) -> Unit,
    isLoading: (isLoading: Boolean) -> Unit
): WKNavigationDelegateProtocol {
    return object : NSObject(), WKNavigationDelegateProtocol {

        override fun webView(
            webView: WKWebView,
            decidePolicyForNavigationAction: WKNavigationAction,
            decisionHandler: (WKNavigationActionPolicy) -> Unit
        ) {
            val request = decidePolicyForNavigationAction.request
            val urlString = request.URL?.absoluteString ?: ""
            val navigationType = decidePolicyForNavigationAction.navigationType

            // Log navigation for debugging
            println("Navigation: $urlString, Type: $navigationType")

            when (navigationType) {
                WKNavigationTypeLinkActivated -> {
                    // Handle link clicks
                    onUrlClicked(urlString)

                    // Check if it's an external link or special URL
                    if (urlString.contains("jpg") ||
                        urlString.contains("png") ||
                        urlString.contains("attachment_id") ||
                        urlString.startsWith("mailto:") ||
                        urlString.startsWith("tel:")) {

                        // Don't load these in WebView
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    } else {
                        // Allow normal navigation
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
                    }
                }
                else -> {
                    // Allow all other types of navigation
                    decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
                }
            }
        }

        override fun webView(
            webView: WKWebView,
            didStartProvisionalNavigation: WKNavigation?
        ) {
            println("WebView: Started loading")
            isLoading(true)
        }

        override fun webView(
            webView: WKWebView,
            didFailProvisionalNavigation: WKNavigation?,
            withError: platform.Foundation.NSError
        ) {
            println("WebView: Failed provisional navigation - ${withError.localizedDescription}")
            isLoading(false)
        }
    }
}