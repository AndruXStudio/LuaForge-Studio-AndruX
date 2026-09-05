package com.luaforge.studio.ai

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.luaforge.studio.R
import kotlinx.coroutines.launch

private const val DEEPSEEK_FREE_CHAT = "https://chat.deepseek.com/"

data class UiChatMessage(
    val role: String,
    val content: String,
    val reasoning: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    currentCode: String,
    currentFileName: String,
) {
    if (!visible) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 0 = 免费网页对话（默认）, 1 = API 模式
    var mode by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf(DeepSeekService.getApiKey(context)) }
    var model by remember { mutableStateOf(DeepSeekService.getModel(context)) }
    var useSearch by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<UiChatMessage>() }
    val listState = rememberLazyListState()
    var webProgress by remember { mutableFloatStateOf(0f) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_deepseek),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "DeepSeek 助手",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        if (mode == 0) "免费网页对话 · chat.deepseek.com"
                        else if (model == DeepSeekService.MODEL_REASONER) "API · reasoner"
                        else "API · chat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (mode == 0) {
                    IconButton(onClick = { webViewRef?.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
                IconButton(onClick = { showSettings = !showSettings }) {
                    Icon(Icons.Default.Settings, contentDescription = "设置")
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mode == 0,
                    onClick = { mode = 0 },
                    label = { Text("免费对话") },
                )
                FilterChip(
                    selected = mode == 1,
                    onClick = { mode = 1 },
                    label = { Text("API 模式") },
                )
            }

            if (showSettings) {
                Spacer(Modifier.height(8.dp))
                if (mode == 0) {
                    Text(
                        "免费模式直接打开 DeepSeek 官方网页版，用账号登录即可对话，不消耗 API 余额。\n可在网页里粘贴当前代码提问。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val tip = "文件: $currentFileName\n```lua\n${currentCode.take(8000)}\n```"
                        cm.setPrimaryClip(ClipData.newPlainText("code", tip))
                    }) {
                        Text("复制当前代码到剪贴板（方便粘贴到网页）")
                    }
                } else {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("DeepSeek API Key") },
                        singleLine = true,
                        supportingText = {
                            Text("API 需充值；余额不足会 402。免费请用「免费对话」页。")
                        },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = model == DeepSeekService.MODEL_CHAT,
                            onClick = { model = DeepSeekService.MODEL_CHAT },
                            label = { Text("Chat") },
                        )
                        FilterChip(
                            selected = model == DeepSeekService.MODEL_REASONER,
                            onClick = { model = DeepSeekService.MODEL_REASONER },
                            label = { Text("Reasoner") },
                        )
                        FilterChip(
                            selected = useSearch,
                            onClick = { useSearch = !useSearch },
                            label = { Text("联网搜索") },
                            leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(16.dp)) },
                        )
                    }
                    TextButton(onClick = {
                        DeepSeekService.setApiKey(context, apiKey)
                        DeepSeekService.setModel(context, model)
                        showSettings = false
                    }) { Text("保存 API 设置") }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            if (mode == 0) {
                // 免费网页版
                if (webProgress in 0f..0.99f) {
                    LinearProgressIndicator(
                        progress = { webProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(
                                ClipData.newPlainText(
                                    "code",
                                    "请检查并修复错误：\n```lua\n${currentCode.take(6000)}\n```",
                                )
                            )
                        },
                        label = { Text("复制「修错」提示") },
                        leadingIcon = { Icon(Icons.Default.Build, null, Modifier.size(16.dp)) },
                    )
                    AssistChip(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(
                                ClipData.newPlainText(
                                    "code",
                                    "请解释 $currentFileName：\n```lua\n${currentCode.take(6000)}\n```",
                                )
                            )
                        },
                        label = { Text("复制「解释」提示") },
                        leadingIcon = { Icon(Icons.Default.Lightbulb, null, Modifier.size(16.dp)) },
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    DeepSeekWebPane(
                        url = DEEPSEEK_FREE_CHAT,
                        onProgress = { webProgress = it },
                        onWebViewCreated = { webViewRef = it },
                    )
                }
                Text(
                    "使用官方免费网页对话，登录 DeepSeek 账号即可。API 模式需余额。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp, top = 4.dp),
                )
            } else {
                // API 模式
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {
                            input =
                                "请检查并修复以下代码中的错误，给出修正后的完整代码：\n```lua\n$currentCode\n```"
                        },
                        label = { Text("修错") },
                        leadingIcon = { Icon(Icons.Default.Build, null, Modifier.size(16.dp)) },
                    )
                    AssistChip(
                        onClick = {
                            input =
                                "请解释文件 $currentFileName 的代码逻辑：\n```lua\n$currentCode\n```"
                        },
                        label = { Text("解释") },
                        leadingIcon = { Icon(Icons.Default.Lightbulb, null, Modifier.size(16.dp)) },
                    )
                    AssistChip(
                        onClick = {
                            input = "根据当前工程风格，帮我写一段 AndroLua/Material 示例代码："
                        },
                        label = { Text("写代码") },
                        leadingIcon = { Icon(Icons.Default.Psychology, null, Modifier.size(16.dp)) },
                    )
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Text(
                                "API 需要账户余额。若出现 402 Insufficient Balance，请改用「免费对话」，或在 platform.deepseek.com 充值。",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    items(messages) { msg ->
                        val isUser = msg.role == "user"
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = if (isUser) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    if (isUser) "你" else "DeepSeek",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (!msg.reasoning.isNullOrBlank()) {
                                    Text(
                                        "思考：\n${msg.reasoning}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(msg.content, style = MaterialTheme.typography.bodyMedium)
                                if (!isUser) {
                                    IconButton(onClick = {
                                        val cm =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cm.setPrimaryClip(ClipData.newPlainText("ai", msg.content))
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "复制")
                                    }
                                }
                            }
                        }
                    }
                }
                if (loading) {
                    Row(
                        Modifier.fillMaxWidth().padding(4.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(Modifier.size(28.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入问题…") },
                        maxLines = 4,
                    )
                    IconButton(
                        enabled = !loading && input.isNotBlank(),
                        onClick = {
                            val q = input.trim()
                            if (q.isEmpty()) return@IconButton
                            input = ""
                            messages.add(UiChatMessage("user", q))
                            loading = true
                            scope.launch {
                                DeepSeekService.setApiKey(context, apiKey)
                                DeepSeekService.setModel(context, model)
                                var userContent = q
                                if (useSearch) {
                                    val search = DeepSeekService.webSearch(q)
                                    userContent = "【网络搜索结果】\n$search\n\n【用户问题】\n$q"
                                }
                                val history = mutableListOf(
                                    DeepSeekService.ChatMessage(
                                        "system",
                                        DeepSeekService.systemPrompt(),
                                    )
                                )
                                messages.dropLast(1).takeLast(8).forEach {
                                    history.add(DeepSeekService.ChatMessage(it.role, it.content))
                                }
                                history.add(DeepSeekService.ChatMessage("user", userContent))
                                val result = DeepSeekService.chat(context, history, model)
                                loading = false
                                if (result.error != null) {
                                    val err = result.error
                                    val hint =
                                        if (err.contains("402") || err.contains("Insufficient", true)) {
                                            "\n\n→ 余额不足。请切换到顶部「免费对话」使用网页版，无需 API 余额。"
                                        } else ""
                                    messages.add(UiChatMessage("assistant", "错误：$err$hint"))
                                } else {
                                    messages.add(
                                        UiChatMessage(
                                            "assistant",
                                            result.content.ifBlank { "(空回复)" },
                                            result.reasoning,
                                        )
                                    )
                                }
                            }
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun DeepSeekWebPane(
    url: String,
    onProgress: (Float) -> Unit,
    onWebViewCreated: (WebView) -> Unit,
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        onDispose {
            // WebView destroyed with AndroidView
        }
    }
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                settings.userAgentString = settings.userAgentString
                webViewClient = object : WebViewClient() {}
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgress(newProgress / 100f)
                    }
                }
                loadUrl(url)
                onWebViewCreated(this)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { /* keep */ },
    )
}
