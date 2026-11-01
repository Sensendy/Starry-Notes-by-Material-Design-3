package com.starryweaver.notes.ui.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.starryweaver.notes.BuildConfig

private val techStackLines = listOf(
    "语言：Kotlin",
    "界面：Jetpack Compose + Material 3",
    "架构：MVVM + Repository",
    "导航：Navigation Compose",
    "存储：kotlinx.serialization（JSON 本地文件）",
    "最低版本：Android 8.0（API 26）",
)

private data class LicenseEntry(val name: String, val url: String)

private val licenseEntries = listOf(
    LicenseEntry("AndroidX（Core / Lifecycle / Activity / Navigation）", "https://github.com/androidx/androidx"),
    LicenseEntry("Jetpack Compose", "https://github.com/androidx/androidx/tree/androidx-main/compose"),
    LicenseEntry(
        "Material Components for Android",
        "https://github.com/material-components/material-components-android",
    ),
    LicenseEntry("Kotlin 标准库", "https://github.com/JetBrains/kotlin"),
    LicenseEntry("kotlinx.serialization", "https://github.com/Kotlin/kotlinx.serialization"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Starry Notes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "版本 v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            TextCard("开发者") {
                Text("Nx430", style = MaterialTheme.typography.bodyLarge)
            }

            TextCard("技术栈") {
                techStackLines.forEach { line ->
                    Text(line, style = MaterialTheme.typography.bodyMedium)
                }
            }

            TextCard("开源许可") {
                Text(
                    text = "本应用基于以下开源组件构建，均遵循 Apache License 2.0；点击仓库地址可查看源码：",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                licenseEntries.forEach { entry ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri(entry.url) }
                            .padding(vertical = 6.dp),
                    ) {
                        Text(entry.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = entry.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        )
                    }
                }
            }
        }
    }
}

/** 统一的文字卡片：标题 + 若干行正文。 */
@Composable
private fun TextCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}
