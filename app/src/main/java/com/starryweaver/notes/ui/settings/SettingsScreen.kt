package com.starryweaver.notes.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.starryweaver.notes.BuildConfig
import com.starryweaver.notes.data.NoteRepository
import com.starryweaver.notes.data.NoteSort
import com.starryweaver.notes.data.SettingsRepository
import com.starryweaver.notes.ui.privacy.PrivacyPolicyBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val sortOptions = listOf(
    NoteSort.UPDATED to "最近更新",
    NoteSort.CREATED to "创建时间",
    NoteSort.TITLE to "标题",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: NoteRepository,
    settings: SettingsRepository,
    onOpenTrash: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenAppearance: () -> Unit,
) {
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(repository, settings))
    val sort by viewModel.sort.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sortDialog by remember { mutableStateOf(false) }
    var privacyDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = viewModel.exportJson()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
                }
                Toast.makeText(context, "已导出", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }
                if (text != null) {
                    val count = viewModel.importJson(text)
                    Toast.makeText(context, "导入完成，新增 $count 条", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("设置") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    NavRow(
                        title = "外观",
                        subtitle = "主题模式、动态取色、主题色",
                        onClick = onOpenAppearance,
                    )
                }
            }

            SettingsSection("默认排序") {
                SettingCard(
                    title = "排序方式",
                    value = sortOptions.first { it.first == sort }.second,
                ) { sortDialog = true }
            }

            SettingsSection("数据") {
                ActionRow("导出全部笔记（JSON）") { exportLauncher.launch("notes-backup.json") }
                HorizontalDivider()
                ActionRow("导入笔记（JSON）") { importLauncher.launch(arrayOf("application/json")) }
                HorizontalDivider()
                ActionRow("回收站") { onOpenTrash() }
            }

            SettingsSection("关于") {
                ActionRow("关于 Starry Notes") { onOpenAbout() }
                HorizontalDivider()
                ActionRow("隐私政策") { privacyDialog = true }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("版本", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (sortDialog) {
        OptionDialog(
            title = "排序方式",
            options = sortOptions.map { it.second },
            selectedIndex = sortOptions.indexOfFirst { it.first == sort },
            onSelect = { index ->
                viewModel.setSort(sortOptions[index].first)
                sortDialog = false
            },
            onDismiss = { sortDialog = false },
        )
    }

    if (privacyDialog) {
        AlertDialog(
            onDismissRequest = { privacyDialog = false },
            title = { Text("隐私政策") },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    PrivacyPolicyBody()
                }
            },
            confirmButton = {
                TextButton(onClick = { privacyDialog = false }) { Text("关闭") }
            },
        )
    }
}
