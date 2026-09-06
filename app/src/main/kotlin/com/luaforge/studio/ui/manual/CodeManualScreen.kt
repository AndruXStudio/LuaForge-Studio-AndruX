package com.luaforge.studio.ui.manual

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val MANUAL_FILES = listOf(
    "基础代码.txt",
    "实用代码.txt",
    "用户界面.txt",
    "文件操作.txt",
    "网络操作.txt",
    "Intent类.txt",
    "笔记.txt",
    "Lua教程.txt",
    "整合代码.txt",
    "进阶代码.txt",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeManualScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf<String?>(null) }
    var body by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(selected) {
        val name = selected ?: return@LaunchedEffect
        loading = true
        body = withContext(Dispatchers.IO) {
            try {
                context.assets.open("code_manual/$name").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                "加载失败: ${e.message}"
            }
        }
        loading = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "manual",
        ) { sel ->
            if (sel == null) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Text(
                            "代码手册",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        Text(
                            "AndroLua / Lua 常用代码与教程",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    items(MANUAL_FILES) { name ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = name },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            shape = MaterialTheme.shapes.large,
                        ) {
                            Text(
                                name.removeSuffix(".txt"),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text(sel.removeSuffix(".txt")) },
                        navigationIcon = {
                            IconButton(onClick = { selected = null; body = "" }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("manual", body))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "复制")
                            }
                        },
                    )
                    Text(
                        text = if (loading) "加载中…" else body,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    )
                }
            }
        }
    }
}
