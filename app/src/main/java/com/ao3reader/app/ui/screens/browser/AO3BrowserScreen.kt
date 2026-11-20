package com.ao3reader.app.ui.screens.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.ao3reader.app.AO3Application
import com.ao3reader.app.data.parser.AO3HtmlParser
import com.ao3reader.app.ui.navigation.Screen
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AO3BrowserScreen(
    navController: NavController,
    initialUrl: String = "https://archiveofourown.org"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val workRepo = remember { AO3Application.instance.workRepository }

    var webView: WebView? by remember { mutableStateOf(null) }
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var pageTitle by remember { mutableStateOf("AO3 探索") }
    var detectedWorkId by remember { mutableStateOf<String?>(null) }
    var isParsingWork by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }

    // Beautify CSS injected into AO3 web pages
    val beautifyCss = """
        /* AO3 Beautifier Mobile CSS */
        body { font-size: 16px !important; line-height: 1.6 !important; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif !important; }
        #outer { max-width: 100% !important; padding: 6px !important; }
        .tags li { display: inline-block !important; margin: 2px 4px !important; padding: 3px 6px !important; background: #eef2ff !important; border-radius: 4px !important; font-size: 13px !important; }
        .blurb { border-radius: 10px !important; border: 1px solid #e2e8f0 !important; box-shadow: 0 1px 3px rgba(0,0,0,0.05) !important; padding: 12px !important; margin-bottom: 12px !important; }
        #header .heading { font-size: 18px !important; }
        .userstuff { font-size: 17px !important; line-height: 1.75 !important; }
    """.trimIndent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = pageTitle,
                            maxLines = 1,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = currentUrl,
                            maxLines = 1,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { webView?.goBack() },
                        enabled = canGoBack
                    ) {
                        Icon(Icons.Default.NavigateBefore, contentDescription = "上一页")
                    }
                    IconButton(
                        onClick = { webView?.goForward() },
                        enabled = canGoForward
                    ) {
                        Icon(Icons.Default.NavigateNext, contentDescription = "下一页")
                    }
                    IconButton(onClick = { webView?.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = detectedWorkId != null,
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 })
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val workId = detectedWorkId ?: return@ExtendedFloatingActionButton
                        isParsingWork = true
                        scope.launch {
                            val result = workRepo.fetchWork(workId)
                            isParsingWork = false
                            result.onSuccess { work ->
                                navController.navigate(Screen.WorkDetail.createRoute(work.id))
                            }.onFailure { e ->
                                Toast.makeText(context, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    icon = {
                        if (isParsingWork) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.AutoStories, contentDescription = null)
                        }
                    },
                    text = {
                        Text(
                            if (isParsingWork) "正在解析全文..." else "✨ 一键进入纯净阅读",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        settings.userAgentString =
                            "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

                        // Enable cookies
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)
                        cookieManager.setCookie("https://archiveofourown.org", "view_adult=true; Domain=archiveofourown.org; Path=/")

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                url?.let {
                                    currentUrl = it
                                    detectedWorkId = AO3HtmlParser.extractWorkId(it)
                                }
                                canGoBack = view?.canGoBack() ?: false
                                canGoForward = view?.canGoForward() ?: false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                url?.let {
                                    currentUrl = it
                                    detectedWorkId = AO3HtmlParser.extractWorkId(it)
                                }
                                canGoBack = view?.canGoBack() ?: false
                                canGoForward = view?.canGoForward() ?: false
                                pageTitle = view?.title ?: "AO3 探索"

                                // Inject Beautifier CSS
                                val js = """
                                    (function() {
                                        var style = document.getElementById('ao3-beautifier-style');
                                        if (!style) {
                                            style = document.createElement('style');
                                            style.id = 'ao3-beautifier-style';
                                            style.innerHTML = `$beautifyCss`;
                                            document.head.appendChild(style);
                                        }
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(js, null)
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val targetUrl = request?.url?.toString() ?: return false
                                // Auto append view_adult=true to avoid warning prompts
                                if (targetUrl.contains("archiveofourown.org/works/") && !targetUrl.contains("view_adult=true")) {
                                    val newUrl = if (targetUrl.contains("?")) "$targetUrl&view_adult=true" else "$targetUrl?view_adult=true"
                                    view?.loadUrl(newUrl)
                                    return true
                                }
                                return false
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                pageTitle = title ?: "AO3 探索"
                            }
                        }

                        loadUrl(initialUrl)
                        webView = this
                    }
                }
            )

            if (progress in 1..99) {
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}
