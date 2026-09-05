package com.luaforge.studio.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * DeepSeek OpenAI-compatible chat API client.
 * Models: deepseek-chat (fast), deepseek-reasoner (thinking).
 * Get a key at https://platform.deepseek.com (free trial quota available).
 */
object DeepSeekService {

    private const val CHAT_URL = "https://api.deepseek.com/chat/completions"
    private const val PREFS = "deepseek_ai_prefs"
    private const val KEY_API = "api_key"
    private const val KEY_MODEL = "model"

    const val MODEL_CHAT = "deepseek-chat"
    const val MODEL_REASONER = "deepseek-reasoner"

    fun getApiKey(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_API, "") ?: ""

    fun setApiKey(context: Context, key: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_API, key.trim()).apply()
    }

    fun getModel(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_MODEL, MODEL_CHAT) ?: MODEL_CHAT

    fun setModel(context: Context, model: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_MODEL, model).apply()
    }

    data class ChatMessage(val role: String, val content: String)

    data class ChatResult(
        val content: String,
        val reasoning: String? = null,
        val error: String? = null,
    )

    suspend fun chat(
        context: Context,
        messages: List<ChatMessage>,
        model: String? = null,
        temperature: Double = 0.3,
    ): ChatResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)
        if (apiKey.isBlank()) {
            return@withContext ChatResult(
                content = "",
                error = "请先在 AI 面板设置 DeepSeek API Key（platform.deepseek.com 可领取试用额度）。"
            )
        }
        val useModel = model ?: getModel(context)
        try {
            val body = JSONObject().apply {
                put("model", useModel)
                put("temperature", temperature)
                put("stream", false)
                put("messages", JSONArray().apply {
                    messages.forEach { m ->
                        put(JSONObject().put("role", m.role).put("content", m.content))
                    }
                })
            }
            val conn = (URL(CHAT_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 30000
                readTimeout = 120000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
            }
            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { it.write(body.toString()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.let { BufferedReader(InputStreamReader(it, StandardCharsets.UTF_8)).readText() } ?: ""
            if (code !in 200..299) {
                return@withContext ChatResult(content = "", error = "HTTP $code: ${text.take(400)}")
            }
            val json = JSONObject(text)
            val choice = json.getJSONArray("choices").getJSONObject(0)
            val msg = choice.getJSONObject("message")
            val content = msg.optString("content", "")
            val reasoning = msg.optString("reasoning_content", null)
            ChatResult(content = content, reasoning = reasoning?.ifBlank { null })
        } catch (e: Exception) {
            ChatResult(content = "", error = e.message ?: e.toString())
        }
    }

    /** Lightweight web search via DuckDuckGo Instant Answer API (no key). */
    suspend fun webSearch(query: String): String = withContext(Dispatchers.IO) {
        try {
            val q = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://api.duckduckgo.com/?q=$q&format=json&no_html=1&skip_disambig=1")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 20000
                requestMethod = "GET"
            }
            val text = BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).readText()
            val json = JSONObject(text)
            val sb = StringBuilder()
            val abstract = json.optString("AbstractText", "")
            val heading = json.optString("Heading", "")
            if (heading.isNotBlank()) sb.append("标题: ").append(heading).append('\n')
            if (abstract.isNotBlank()) sb.append(abstract).append('\n')
            val related = json.optJSONArray("RelatedTopics")
            if (related != null) {
                var n = 0
                for (i in 0 until related.length()) {
                    if (n >= 5) break
                    val item = related.optJSONObject(i) ?: continue
                    val t = item.optString("Text", "")
                    if (t.isNotBlank()) {
                        sb.append("- ").append(t).append('\n')
                        n++
                    }
                }
            }
            if (sb.isEmpty()) "未找到简明摘要，请换关键词或结合模型推理。" else sb.toString()
        } catch (e: Exception) {
            "网络搜索失败: ${e.message}"
        }
    }

    fun systemPrompt(): String = """
你是 LuaForge Studio（AndruX）内置的 AI 编程助手，擅长 Lua、AndroLua、Android View/Material、布局表语法。
规则：
1. 回答简洁、可直接用于工程；代码用 markdown 代码块。
2. 修 bug 时指出原因并给出完整可替换片段。
3. 若用户提供了搜索结果，优先依据搜索结果再作答。
4. 使用中文回答，除非用户要求英文。
""".trimIndent()
}
