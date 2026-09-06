package com.luaforge.studio.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luaforge.studio.BuildConfig
import com.luaforge.studio.auth.AuthService

@Composable
fun ProfileTabScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val username = AuthService.currentUsername(context).ifBlank { "用户" }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Column {
                    Text(username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Applua X", style = MaterialTheme.typography.bodyMedium)
                    Text("v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                ListItem(
                    headlineContent = { Text("软件设置") },
                    leadingContent = { Icon(Icons.Default.Settings, null) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingContent = {
                        Button(onClick = onOpenSettings) { Text("打开") }
                    },
                )
                ListItem(
                    headlineContent = { Text("关于软件") },
                    leadingContent = { Icon(Icons.Default.Info, null) },
                    trailingContent = {
                        OutlinedButton(onClick = onOpenAbout) { Text("查看") }
                    },
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                AuthService.logout(context)
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(Modifier.size(8.dp))
            Text("退出登录")
        }
    }
}
