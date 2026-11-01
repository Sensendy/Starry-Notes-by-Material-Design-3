package com.starryweaver.notes.ui.edit

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.starryweaver.notes.data.ImportExport
import com.starryweaver.notes.data.Note
import com.starryweaver.notes.data.NoteRepository
import com.starryweaver.notes.ui.components.MarkdownView
import com.starryweaver.notes.util.MarkdownAction
import kotlinx.coroutines.CoroutineScope

private val markdownTools = listOf(
    "H1" to MarkdownAction.H1,
    "H2" to MarkdownAction.H2,
    "H3" to MarkdownAction.H3,
    "粗体" to MarkdownAction.BOLD,
    "斜体" to MarkdownAction.ITALIC,
    "删除线" to MarkdownAction.STRIKE,
    "行内码" to MarkdownAction.INLINE_CODE,
    "代码块" to MarkdownAction.CODE_BLOCK,
    "引用" to MarkdownAction.QUOTE,
    "列表" to MarkdownAction.BULLET,
    "编号" to MarkdownAction.ORDERED,
    "待办" to MarkdownAction.TODO,
    "链接" to MarkdownAction.LINK,
    "分隔线" to MarkdownAction.RULE,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    repository: NoteRepository,
    saveScope: CoroutineScope,
    noteId: String,
    seed: SharedNote?,
    fresh: Boolean,
    onBack: () -> Unit,
    onSharedConsumed: () -> Unit,
) {
    val viewModel: NoteEditViewModel = viewModel(
        key = "edit-$noteId",
        factory = NoteEditViewModel.factory(repository, saveScope, noteId, seed, fresh),
    )
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var menuOpen by remember { mutableStateOf(false) }
    var tagInput by remember { mutableStateOf("") }
    var exportingNote by remember { mutableStateOf<Note?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown"),
    ) { uri ->
        val note = exportingNote
        exportingNote = null
        if (uri != null && note != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(ImportExport.exportMarkdown(note).toByteArray())
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.isNew && seed != null) onSharedConsumed()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.onExit() }
    }

    val currentNote = Note(
        id = viewModel.noteId,
        title = state.title,
        content = state.content.text,
        tags = state.tags,
        pinned = state.pinned,
        createdAt = state.createdAt,
        updatedAt = System.currentTimeMillis(),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.onExit(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                title = { Text(if (viewModel.isNew) "新建笔记" else "编辑笔记") },
                actions = {
                    IconButton(onClick = viewModel::togglePin) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = if (state.pinned) "取消置顶" else "置顶",
                            tint = if (state.pinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "更多操作")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("分享") },
                                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, ImportExport.shareText(currentNote))
                                    }
                                    context.startActivity(Intent.createChooser(intent, "分享笔记"))
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("导出 Markdown") },
                                onClick = {
                                    menuOpen = false
                                    exportingNote = currentNote
                                    exportLauncher.launch(ImportExport.suggestedFileName(currentNote))
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("移到回收站") },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    viewModel.trash { onBack() }
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (!state.loaded) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PreviewModeRow(current = state.previewMode, onSelect = viewModel::setPreviewMode)

            when (state.previewMode) {
                EditorPreviewMode.EDIT -> {
                    EditorMeta(
                        title = state.title,
                        tags = state.tags,
                        tagInput = tagInput,
                        onTitleChange = viewModel::onTitleChange,
                        onTagInputChange = { tagInput = it },
                        onAddTag = { viewModel.addTag(tagInput); tagInput = "" },
                        onRemoveTag = viewModel::removeTag,
                    )
                    ContentEditor(
                        value = state.content,
                        onValueChange = viewModel::onContentChange,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    )
                    MarkdownToolbar(onAction = viewModel::applyMarkdown)
                    Spacer(Modifier.height(8.dp))
                }

                EditorPreviewMode.SPLIT -> {
                    EditorMeta(
                        title = state.title,
                        tags = state.tags,
                        tagInput = tagInput,
                        onTitleChange = viewModel::onTitleChange,
                        onTagInputChange = { tagInput = it },
                        onAddTag = { viewModel.addTag(tagInput); tagInput = "" },
                        onRemoveTag = viewModel::removeTag,
                    )
                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        MarkdownToolbar(onAction = viewModel::applyMarkdown)
                        ContentEditor(
                            value = state.content,
                            onValueChange = viewModel::onContentChange,
                            modifier = Modifier.fillMaxWidth().weight(1f),
                        )
                    }
                    HorizontalDivider()
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        PreviewPane(
                            title = state.title,
                            content = state.content.text,
                            showTitle = false,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                EditorPreviewMode.PREVIEW -> {
                    PreviewPane(
                        title = state.title,
                        content = state.content.text,
                        showTitle = true,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewModeRow(current: EditorPreviewMode, onSelect: (EditorPreviewMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = current == EditorPreviewMode.EDIT,
            onClick = { onSelect(EditorPreviewMode.EDIT) },
            label = { Text("编辑") },
        )
        FilterChip(
            selected = current == EditorPreviewMode.SPLIT,
            onClick = { onSelect(EditorPreviewMode.SPLIT) },
            label = { Text("分栏") },
        )
        FilterChip(
            selected = current == EditorPreviewMode.PREVIEW,
            onClick = { onSelect(EditorPreviewMode.PREVIEW) },
            label = { Text("预览") },
        )
    }
}

@Composable
private fun MarkdownToolbar(onAction: (MarkdownAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        markdownTools.forEach { (label, action) ->
            TextButton(onClick = { onAction(action) }) {
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun EditorMeta(
    title: String,
    tags: List<String>,
    tagInput: String,
    onTitleChange: (String) -> Unit,
    onTagInputChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit,
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("标题") },
        singleLine = true,
    )

    if (tags.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tags.forEach { tag ->
                InputChip(
                    selected = false,
                    onClick = { onRemoveTag(tag) },
                    label = { Text("#$tag") },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "删除标签",
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }
    }

    OutlinedTextField(
        value = tagInput,
        onValueChange = onTagInputChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("添加标签，回车确认") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onAddTag() }),
    )
}

@Composable
private fun ContentEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box {
                if (value.text.isEmpty()) {
                    Text(
                        text = "开始用 Markdown 写作…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun PreviewPane(
    title: String,
    content: String,
    showTitle: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showTitle) {
            Text(title.ifBlank { "无标题" }, style = MaterialTheme.typography.headlineSmall)
        }
        if (content.isBlank()) {
            Text("（空白笔记）", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            MarkdownView(content)
        }
    }
}
