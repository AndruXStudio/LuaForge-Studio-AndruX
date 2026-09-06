package com.luaforge.studio.auth

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
 * Supabase users 表登录 / 注册（用户名 + 密码，注册含邮箱）
 */
object AuthService {
    const val SUPABASE_URL = "https://fnlaryjanmfqvnonqfyj.supabase.co"
    const val SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZubGFyeWphbm1mcXZub25xZnlqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzIwNzQzNzgsImV4cCI6MjA4NzY1MDM3OH0.4uGXyVd_6XQnK3o1vu_sDhpdRyrM67JFaBm2SnuJf1M"

    private const val PREFS = "applua_x_auth"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"

    data class AuthResult(val ok: Boolean, val message: String = "", val username: String = "")

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_LOGGED_IN, false)

    fun currentUsername(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_USERNAME, "") ?: ""

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun saveSession(context: Context, username: String, email: String = "") {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_USERNAME, username)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    private fun open(url: String, method: String): HttpURLConnection {
        val conn = (URL(url).openConnection() as HttpURLConnection)
        conn.requestMethod = method
        conn.connectTimeout = 20000
        conn.readTimeout = 25000
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY)
        conn.setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        return conn
    }

    private fun readBody(conn: HttpURLConnection): String {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return stream?.let { BufferedReader(InputStreamReader(it, StandardCharsets.UTF_8)).readText() } ?: ""
    }

    suspend fun login(context: Context, username: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val u = URLEncoder.encode(username.trim(), "UTF-8")
                val p = URLEncoder.encode(password, "UTF-8")
                val url =
                    "$SUPABASE_URL/rest/v1/users?users=eq.$u&password=eq.$p&select=*"
                val conn = open(url, "GET")
                val code = conn.responseCode
                val body = readBody(conn)
                if (code !in 200..299) {
                    return@withContext AuthResult(false, "登录失败 HTTP $code: ${body.take(200)}")
                }
                val arr = JSONArray(body)
                if (arr.length() == 0) {
                    return@withContext AuthResult(false, "用户名或密码错误")
                }
                val row = arr.getJSONObject(0)
                val name = row.optString("users", username.trim())
                val email = row.optString("email", "")
                saveSession(context, name, email)
                AuthResult(true, "登录成功", name)
            } catch (e: Exception) {
                AuthResult(false, e.message ?: e.toString())
            }
        }

    suspend fun register(
        context: Context,
        username: String,
        email: String,
        password: String,
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            // 查重
            val u = URLEncoder.encode(username.trim(), "UTF-8")
            val check = open("$SUPABASE_URL/rest/v1/users?users=eq.$u&select=users", "GET")
            val checkBody = readBody(check)
            if (check.responseCode in 200..299) {
                val arr = JSONArray(checkBody)
                if (arr.length() > 0) {
                    return@withContext AuthResult(false, "用户名已存在")
                }
            }
            val conn = open("$SUPABASE_URL/rest/v1/users", "POST")
            conn.setRequestProperty("Prefer", "return=representation")
            conn.doOutput = true
            val payload = JSONObject()
                .put("users", username.trim())
                .put("password", password)
                .put("email", email.trim())
            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { it.write(payload.toString()) }
            val code = conn.responseCode
            val body = readBody(conn)
            if (code !in 200..299) {
                return@withContext AuthResult(false, "注册失败 HTTP $code: ${body.take(240)}")
            }
            saveSession(context, username.trim(), email.trim())
            AuthResult(true, "注册成功", username.trim())
        } catch (e: Exception) {
            AuthResult(false, e.message ?: e.toString())
        }
    }
}
