package com.luaforge.studio.ui.settings

import android.content.Context
import android.graphics.Typeface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataArray
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.luaforge.studio.R
import com.luaforge.studio.ui.components.MorphSwitch
import com.luaforge.studio.ui.theme.ThemeType
import com.luaforge.studio.utils.NonBlockingToastState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    currentSettings: SettingsData,
    onSettingsChanged: (SettingsData) -> Unit,
    toast: NonBlockingToastState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val extras = remember { ExtraSettings(context) }

    var dynamicColor by remember { mutableStateOf(currentSettings.dynamicColor) }
    var darkFollow by remember { mutableStateOf(currentSettings.darkMode) }
    var tabHistory by remember { mutableStateOf(currentSettings.enableTabHistory) }
    var caseSensitive by remember { mutableStateOf(currentSettings.completionCaseSensitive) }
    var indentGuide by remember { mutableStateOf(currentSettings.indentGuideEnabled) }
    var wordWrap by remember { mutableStateOf(currentSettings.editorWordWrap) }
    var hexHighlight by remember { mutableStateOf(currentSettings.hexColorHighlightEnabled) }
    var swipeGesture by remember { mutableStateOf(currentSettings.enableSwipeGesture) }

    var collapsingToolbar by remember { mutableStateOf(extras.getBool("collapsing_toolbar", false)) }
    var projectListIcons by remember { mutableStateOf(extras.getBool("project_list_icons", true)) }
    var fragmentAnim by remember { mutableStateOf(extras.getBool("fragment_anim", true)) }
    var fileTabIcons by remember { mutableStateOf(extras.getBool("file_tab_icons", true)) }
    var autoBackup by remember { mutableStateOf(extras.getBool("auto_backup", false)) }
    var soraEditor by remember { mutableStateOf(extras.getBool("sora_editor", true)) }
    var fullParamTypes by remember { mutableStateOf(extras.getBool("full_param_types", false)) }
    var analyzeImports by remember { mutableStateOf(extras.getBool("analyze_imports", false)) }
    var symbolAutoFill by remember { mutableStateOf(extras.getBool("symbol_auto_fill", false)) }
    var layoutChinese by remember { mutableStateOf(extras.getBool("layout_chinese", true)) }
    var requestIntercept by remember { mutableStateOf(extras.getBool("request_intercept", true)) }
    var offlineMode by remember { mutableStateOf(extras.getBool("offline_mode", false)) }
    var checkUpdate by remember { mutableStateOf(extras.getBool("check_update", true)) }

    fun persist(new: SettingsData) {
        onSettingsChanged(new)
        scope.launch {
            SettingsManager.updateSettings(new)
            SettingsManager.saveSettings(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("软件设置", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item { SectionHeader("界面") }
            item {
                SettingsNavRow(
                    icon = Icons.Filled.Palette,
                    title = "主题颜色",
                    subtitle = when (currentSettings.themeType) {
                        ThemeType.CARMINE -> "卡莫纳色"
                        ThemeType.BLUE -> "蓝色"
                        ThemeType.GREEN -> "绿色"
                        ThemeType.PINK -> "粉色"
                    },
                    onClick = {
                        val values = ThemeType.entries.toList()
                        val next = values[(values.indexOf(currentSettings.themeType) + 1) % values.size]
                        persist(currentSettings.copy(themeType = next))
                        toast.showToast("主题已切换")
                    },
                )
            }
            item {
                SettingsSwitchRow(Icons.Filled.ColorLens, "动态取色", "适配 Android 12 新特性", dynamicColor) {
                    dynamicColor = it; persist(currentSettings.copy(dynamicColor = it))
                }
            }
            item {
                SettingsNavRow(
                    icon = Icons.Filled.DarkMode,
                    title = "深色模式",
                    subtitle = when (darkFollow) {
                        DarkMode.FOLLOW_SYSTEM -> "跟随系统"
                        DarkMode.LIGHT -> "浅色"
                        DarkMode.DARK -> "深色"
                    },
                    onClick = {
                        darkFollow = when (darkFollow) {
                            DarkMode.FOLLOW_SYSTEM -> DarkMode.LIGHT
                            DarkMode.LIGHT -> DarkMode.DARK
                            DarkMode.DARK -> DarkMode.FOLLOW_SYSTEM
                        }
                        persist(currentSettings.copy(darkMode = darkFollow))
                    },
                )
            }
            item {
                SettingsSwitchRow(Icons.Filled.ViewAgenda, "折叠工具栏", null, collapsingToolbar) {
                    collapsingToolbar = it; extras.setBool("collapsing_toolbar", it)
                }
            }
            item {
                SettingsSwitchRow(Icons.Filled.Folder, "项目列表图标", null, projectListIcons) {
                    projectListIcons = it; extras.setBool("project_list_icons", it)
                }
            }
            item {
                SettingsSwitchRow(Icons.Filled.Animation, "Fragment 动画", "Fade Animation", fragmentAnim) {
                    fragmentAnim = it; extras.setBool("fragment_anim", it)
                }
            }

            item { SectionHeader("插件") }
            item {
                SettingsNavRow(Icons.Filled.Extension, "插件管理", "管理已安装插件") {
                    toast.showToast("插件管理将在后续版本开放")
                }
            }

            item { SectionHeader("标签栏") }
            item {
                SettingsSwitchRow(Icons.Filled.Widgets, "文件图标", null, fileTabIcons) {
                    fileTabIcons = it; extras.setBool("file_tab_icons", it)
                }
            }
            item {
                SettingsSwitchRow(Icons.Filled.History, "历史记录", "编辑器标签历史", tabHistory) {
                    tabHistory = it; persist(currentSettings.copy(enableTabHistory = it))
                }
            }

            item { SectionHeader("编辑器") }
            item {
                SettingsSwitchRow(Icons.Filled.Backup, "自动备份", "每5分钟为当前打开的项目进行备份一次", autoBackup) {
                    autoBackup = it; extras.setBool("auto_backup", it)
                    toast.showToast(if (it) "已开启自动备份" else "已关闭自动备份")
                }
            }
            item {
                SettingsSwitchRow(Icons.Filled.Code, "Sora-Editor", "使用 Sora 代码编辑器引擎", soraEditor) {
                    soraEditor = it; extras.setBool("sora_editor", it)
                }
            }
            item {
                SettingsSwitchRow(Icons.Filled.DataArray, "完整的参数类型", "代码补全框显示完整的参数类型", fullParamTypes) {
                    fullParamTypes = it; extras.setBool("full_param_types", it)
                }
            }
            item {
                SettingsSwitchRow(Icons.Filled.Functions, "分析导入类", "打开项目时自动分析libs下的dex类", analyzeImports) {
                    analyzeImports = it; extras.setBool("analyze_imports", it)
                }
            }
            item {
                SettingsSwitchRow(Icons.Filled.TextFields, "区分大小写", "代码补全框区分大小写", caseSensitive) {
                    caseSensitive = it; persist(currentSettings.copy(completionCaseSensitive = it))
                }
            }
            item {
                SettingsNavRow(Icons.Filled.Edit, "编辑框配置", if (wordWrap) "自动换行：开" else "自动换行：关") {
                    wordWrap = !wordWrap
                    persist(currentSettings.copy(editorWordWrap = wordWrap))
                }
            }
            item {
                SettingsNavRow(
                    Icons.Filled.ZoomOutMap, "缩放范围",
                    "字体缩放 ${(currentSettings.fontSizeScale * 100).toInt()}%（点按切换 85/100/120/150）",
                ) {
                    val next = when {
                        currentSettings.fontSizeScale < 1f -> 1f
                        currentSettings.fontSizeScale < 1.2f -> 1.2f
                        currentSettings.fontSizeScale < 1.5f -> 1.5f
                        else -> 0.85f
                    }
                    persist(currentSettings.copy(fontSizeScale = next))
                }
            }
            item {
                SettingsNavRow(Icons.Filled.FontDownload, "字体", currentSettings.editorFontType.name) {
                    val values = EditorFontType.entries.toList()
                    val next = values[(values.indexOf(currentSettings.editorFontType) + 1) % values.size]
                    persist(currentSettings.copy(editorFontType = next))
                    toast.showToast("字体: ${next.name}")
                }
            }
            item {
                SettingsSwitchRow(Icons.AutoMirrored.Filled.FormatIndentIncrease, "缩进参考线", null, indentGuide) {
                    indentGuide = it; persist(currentSettings.copy(indentGuideEnabled = it))
                }
            }
            item {
                SettingsSwitchRow(Icons.Filled.ColorLens, "十六进制颜色高亮", null, hexHighlight) {
                    hexHighlight = it; persist(currentSettings.copy(hexColorHighlightEnabled = it))
                }
            }

            item { SectionHeader("符号栏") }
            item {
                SettingsSwitchRow(Icons.Filled.Keyboard, "自动填充", null, symbolAutoFill) {
                    symbolAutoFill = it; extras.setBool("symbol_auto_fill", it)
                }
            }
            item {
                SettingsNavRow(Icons.Filled.Keyboard, "自定义符号", "编辑符号栏内容") {
                    toast.showToast("请在编辑器符号栏中自定义")
                }
            }

            item { SectionHeader("布局助手") }
            item {
                SettingsSwitchRow(Icons.Filled.Language, "控件中文名", null, layoutChinese) {
                    layoutChinese = it; extras.setBool("layout_chinese", it)
                }
            }
            item {
                SettingsNavRow(Icons.Filled.Widgets, "自定义控件类", "布局助手控件列表") {
                    toast.showToast("可在布局助手中管理控件类")
                }
            }

            item { SectionHeader("网络") }
            item {
                SettingsSwitchRow(Icons.Filled.Security, "请求拦截", "电子网络请求拦截", requestIntercept) {
                    requestIntercept = it; extras.setBool("request_intercept", it)
                }
            }
            item {
                SettingsSwitchRow(Icons.Filled.NetworkCheck, "离线模式", "拒绝一切网络操作，节省流量消耗", offlineMode) {
                    offlineMode = it; extras.setBool("offline_mode", it)
                }
            }

            item { SectionHeader("软件") }
            item {
                SettingsSwitchRow(Icons.Filled.Update, "检查更新", null, checkUpdate) {
                    checkUpdate = it; extras.setBool("check_update", it)
                }
            }
            item {
                SettingsNavRow(Icons.Filled.Info, "关于软件", "Applua X") {
                    toast.showToast("Applua X · AndruX developer-Studio")
                }
            }
            item {
                SettingsSwitchRow(Icons.AutoMirrored.Filled.Sort, "边缘滑动手势", null, swipeGesture) {
                    swipeGesture = it; persist(currentSettings.copy(enableSwipeGesture = it))
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = if (subtitle != null) ({
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }) else null,
        leadingContent = {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            MorphSwitch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.fillMaxWidth(),
    )
    HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp)
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = if (subtitle != null) ({
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }) else null,
        leadingContent = {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    )
    HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp)
}

class ExtraSettings(context: Context) {
    private val sp = context.getSharedPreferences("applua_extra_settings", Context.MODE_PRIVATE)
    fun getBool(key: String, def: Boolean) = sp.getBoolean(key, def)
    fun setBool(key: String, value: Boolean) = sp.edit().putBoolean(key, value).apply()
}


enum class EditorFontType {
    JETBRAINS_MONO,
    FIRA_CODE,
    CUSTOM
}

enum class DarkMode {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK
}

enum class FontFamilyType {
    DEFAULT,
    SANS_SERIF,
    SERIF,
    MONOSPACE,
    JOSEFIN_SANS
}

object FontManager {
    fun getEditorTypeface(context: Context, settings: SettingsData): Typeface? {
        return try {
            when (settings.editorFontType) {
                EditorFontType.JETBRAINS_MONO -> {
                    ResourcesCompat.getFont(context, R.font.jetbrains_mono)
                }

                EditorFontType.FIRA_CODE -> {
                    ResourcesCompat.getFont(context, R.font.fira_code)
                }

                EditorFontType.CUSTOM -> {
                    if (settings.customFontPath.isNotBlank()) {
                        try {
                            Typeface.createFromFile(settings.customFontPath)
                        } catch (_: Exception) {
                            ResourcesCompat.getFont(context, R.font.jetbrains_mono)
                        }
                    } else {
                        ResourcesCompat.getFont(context, R.font.jetbrains_mono)
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
