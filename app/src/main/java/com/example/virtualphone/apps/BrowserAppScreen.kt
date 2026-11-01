package com.example.virtualphone.apps

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*
import com.example.virtualphone.viewmodel.VirtualPhoneViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserAppScreen(
    viewModel: VirtualPhoneViewModel,
    modifier: Modifier = Modifier
) {
    val initialUrl by viewModel.browserUrl.collectAsState()
    var inputUrl by remember(initialUrl) { mutableStateOf(initialUrl) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    // Clean up WebView on disposal to avoid orphaned render threads
    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewInstance?.stopLoading()
                webViewInstance?.onPause()
                webViewInstance?.destroy()
                webViewInstance = null
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Address Bar & Controls
        Surface(
            color = Color(0xFF1E293B),
            shadowElevation = 4.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { webViewInstance?.goBack() },
                        enabled = webViewInstance?.canGoBack() == true,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (webViewInstance?.canGoBack() == true) CyanNeon else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { webViewInstance?.goForward() },
                        enabled = webViewInstance?.canGoForward() == true,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Forward",
                            tint = if (webViewInstance?.canGoForward() == true) CyanNeon else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { webViewInstance?.reload() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reload",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Input Box
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("browser_url_input"),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = SeveritySecure, modifier = Modifier.size(14.dp))
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Go",
                                tint = CyanNeon,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        viewModel.openBrowserWithUrl(inputUrl)
                                    }
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            viewModel.openBrowserWithUrl(inputUrl)
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = BorderStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                        shape = RoundedCornerShape(20.dp)
                    )

                    IconButton(
                        onClick = {
                            inputUrl = "https://www.google.com"
                            viewModel.openBrowserWithUrl(inputUrl)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = CyanNeon, modifier = Modifier.size(18.dp))
                    }
                }

                // Quick Bookmarks Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickBookmarkChip("🔍 Google", "https://www.google.com") { viewModel.openBrowserWithUrl(it) }
                    QuickBookmarkChip("▶️ YouTube", "https://m.youtube.com") { viewModel.openBrowserWithUrl(it) }
                    QuickBookmarkChip("🦆 DuckDuckGo", "https://duckduckgo.com") { viewModel.openBrowserWithUrl(it) }
                    QuickBookmarkChip("📚 Wikipedia", "https://en.m.wikipedia.org") { viewModel.openBrowserWithUrl(it) }
                    QuickBookmarkChip("🐙 GitHub", "https://github.com") { viewModel.openBrowserWithUrl(it) }
                }

                // Loading progress bar
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = CyanNeon,
                        trackColor = Color.Transparent
                    )
                }
            }
        }

        // Real Interactive Android WebView
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize().testTag("real_webview"),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        try {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.databaseEnabled = true
                            settings.setSupportZoom(true)
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            }
                        } catch (_: Exception) {}

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return false // Load internally in this webview
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                url?.let { inputUrl = it }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                url?.let { inputUrl = it }
                            }

                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                super.onReceivedError(view, request, error)
                                isLoading = false
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress / 100f
                            }
                        }

                        loadUrl(initialUrl)
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    if (webView.url != initialUrl && initialUrl.isNotBlank()) {
                        webView.loadUrl(initialUrl)
                    }
                }
            )
        }
    }
}

@Composable
fun QuickBookmarkChip(label: String, url: String, onSelect: (String) -> Unit) {
    Box(
        modifier = Modifier
            .background(Color(0xFF334155), RoundedCornerShape(12.dp))
            .clickable { onSelect(url) }
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = label, fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
