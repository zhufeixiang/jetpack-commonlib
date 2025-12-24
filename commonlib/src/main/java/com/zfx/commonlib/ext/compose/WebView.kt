package com.zfx.commonlib.ext.compose

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Compose 中的 WebView 组件
 * 
 * 使用示例：
 * ```kotlin
 * WebView(
 *     url = "https://www.example.com",
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 * 
 * 或者带自定义配置：
 * ```kotlin
 * WebView(
 *     url = "https://www.example.com",
 *     modifier = Modifier.fillMaxSize(),
 *     onPageStarted = { url -> println("页面开始加载: $url") },
 *     onPageFinished = { url -> println("页面加载完成: $url") },
 *     onReceivedError = { error -> println("加载错误: $error") }
 * )
 * ```
 * 
 * @param url 要加载的 URL
 * @param modifier 修饰符
 * @param onPageStarted 页面开始加载时的回调
 * @param onPageFinished 页面加载完成时的回调
 * @param onReceivedError 加载错误时的回调
 * @param webViewClient 自定义 WebViewClient，如果提供则忽略其他回调
 * 
 * @author zhufeixiang
 * @date 2025/01/XX
 */
@Composable
fun WebView(
    url: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    onPageStarted: ((String?) -> Unit)? = null,
    onPageFinished: ((String?) -> Unit)? = null,
    onReceivedError: ((android.webkit.WebResourceError?) -> Unit)? = null,
    webViewClient: WebViewClient? = null
) {
    val context = LocalContext.current
    
    val webViewClientInstance = remember {
        webViewClient ?: object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onPageStarted?.invoke(url)
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onPageFinished?.invoke(url)
            }
            
            override fun onReceivedError(
                view: WebView?,
                request: android.webkit.WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                onReceivedError?.invoke(error)
            }
        }
    }
    
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    allowFileAccess = true
                    allowContentAccess = true
                }
                this.webViewClient = webViewClientInstance
            }
        },
        modifier = modifier,
        update = { webView ->
            webView.loadUrl(url)
        }
    )
}

/**
 * 可组合的 WebView，支持动态更新 URL
 * 
 * 使用示例：
 * ```kotlin
 * var url by remember { mutableStateOf("https://www.example.com") }
 * 
 * WebViewComposable(
 *     url = url,
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 * 
 * @param url 要加载的 URL，当 URL 改变时会自动重新加载
 * @param modifier 修饰符
 * @param onPageStarted 页面开始加载时的回调
 * @param onPageFinished 页面加载完成时的回调
 * @param onReceivedError 加载错误时的回调
 * @param webViewClient 自定义 WebViewClient，如果提供则忽略其他回调
 */
@Composable
fun WebViewComposable(
    url: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    onPageStarted: ((String?) -> Unit)? = null,
    onPageFinished: ((String?) -> Unit)? = null,
    onReceivedError: ((android.webkit.WebResourceError?) -> Unit)? = null,
    webViewClient: WebViewClient? = null
) {
    val context = LocalContext.current
    
    val webViewClientInstance = remember {
        webViewClient ?: object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onPageStarted?.invoke(url)
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onPageFinished?.invoke(url)
            }
            
            override fun onReceivedError(
                view: WebView?,
                request: android.webkit.WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                onReceivedError?.invoke(error)
            }
        }
    }
    
    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                allowFileAccess = true
                allowContentAccess = true
            }
            this.webViewClient = webViewClientInstance
        }
    }
    
    LaunchedEffect(url) {
        webView.loadUrl(url)
    }
    
    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}

