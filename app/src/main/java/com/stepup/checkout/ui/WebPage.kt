package com.stepup.checkout.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import com.stepup.checkout.R
import com.stepup.checkout.common.OnLifecycleEvent
import com.stepup.checkout.databinding.WebViewLayoutBinding

@Composable
fun WebPage(
    webUrl: String,
    modifier: Modifier = Modifier,
    onLoadingUpdated: (isLoading: Boolean) -> Unit = {},
    overrideUrl: (String) -> Boolean = { false },
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    webView?.let { view ->
        BackHandler(enabled = canGoBack) { view.goBack() }
        OnLifecycleEvent(Lifecycle.Event.ON_RESUME, view) { view.onResume() }
        OnLifecycleEvent(Lifecycle.Event.ON_PAUSE, view) { view.onPause() }
        DisposableEffect(view) {
            onDispose {
                view.onPause()
                view.destroy()
            }
        }
    }
    AndroidView(
        factory = { context ->
            val binding = WebViewLayoutBinding.inflate(LayoutInflater.from(context))
            // The Checkout URL doesn't display correctly in the WebView
            // if it is a direct child of Compose, so using it wrapped in a FrameLayout
            with(binding.webView) {
                webView = this
                @SuppressLint("SetJavaScriptEnabled")
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        onLoadingUpdated(true)
                        canGoBack = canGoBack()
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        onLoadingUpdated(false)
                        canGoBack = canGoBack()
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, url: String) =
                        overrideUrl(url)
                }
            }
            binding.root
        },
        update = {
            it.findViewById<WebView>(R.id.web_view).loadUrl(webUrl)
        },
        modifier = modifier.fillMaxSize()
    )
}
