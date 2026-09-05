package com.luaforge.studio.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.luaforge.studio.R
import kotlinx.coroutines.launch

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
    val messages = remember { mutableStateListOf<UiChatMessage>() }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf(DeepSeekService.getApiKey(context)) }
    var model by remember { mutableStateOf(DeepSeekService.getModel(context)) }
    var useSearch by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

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
                    Text("DeepSeek 助手", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (model == DeepSeekService.MODEL_REASONER) "推理模式 · deepseek-reasoner" else "对话模式 · deepseek-chat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { showSettings = !showSettings }) {
                    Icon(Icons.Default.Settings, contentDescription = "设置")
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }

            if (showSettings) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("DeepSeek API Key") },
                    singleLine = true,
                    supportingText = { Text("在 platform.deepseek.com 创建，支持免费试用额度") },
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
                        label = { Text("思考/Reasoner") },
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
                }) { Text("保存设置") }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {
                        input = "请检查并修复以下代码中的错误，给出修正后的完整代码：\n```lua\n$currentCode\n```"
                    },
                    label = { Text("修错") },
                    leadingIcon = { Icon(Icons.Default.Build, null, Modifier.size(16.dp)) },
                )
                AssistChip(
                    onClick = {
                        input = "请解释文件 $currentFileName 的代码逻辑：\n```lua\n$currentCode\n```"
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
                            "可以问我：改 bug、写布局、解释 API、搜索资料。需要先填写 API Key。",
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
                                    "思考过程：\n${msg.reasoning}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            }
                            Text(msg.content, style = MaterialTheme.typography.bodyMedium)
                            if (!isUser) {
                                IconButton(onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
                                DeepSeekService.ChatMessage("system", DeepSeekService.systemPrompt())
                            )
                            messages.dropLast(1).takeLast(8).forEach {
                                history.add(DeepSeekService.ChatMessage(it.role, it.content))
                            }
                            history.add(DeepSeekService.ChatMessage("user", userContent))
                            val result = DeepSeekService.chat(context, history, model)
                            loading = false
                            if (result.error != null) {
                                messages.add(UiChatMessage("assistant", "错误：${result.error}"))
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
