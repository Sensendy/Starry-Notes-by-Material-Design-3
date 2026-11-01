package com.starryweaver.notes.ui.notes

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.starryweaver.notes.R
import com.starryweaver.notes.data.ImportExport
import com.starryweaver.notes.data.Note
import com.starryweaver.notes.data.NoteRepository
import com.starryweaver.notes.data.NoteSort
import com.starryweaver.notes.data.NotesLayout
import com.starryweaver.notes.data.SettingsRepository
import com.starryweaver.notes.ui.components.NoteCard
import com.starryweaver.notes.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    repository: NoteRepository,
    settings: SettingsRepository,
    onOpenNote: (String) -> Unit,
    onCreateNote: (String) -> Unit,
    onOpenTrash: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: NoteListViewModel = viewModel(
        factory = NoteListViewModel.factory(repository, settings),
    )
    val state by viewModel.uiState.collectAsState()

    var searchActive by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Starry Notes") },
                actions = {
                    IconButton(onClick = {
                        searchActive = !searchActive
                        if (!searchActive) viewModel.clearQuery()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_filter_list),
                            contentDescription = "筛选",
                            tint = if (searchActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            val switchToDouble = state.layout == NotesLayout.SINGLE
                            DropdownMenuItem(
                                text = { Text(if (switchToDouble) "切换为双栏" else "切换为单栏") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(
                                            if (switchToDouble) R.drawable.ic_view_grid else R.drawable.ic_view_list,
                                        ),
                                        contentDescription = null,
                                    )
                                },
                                onClick = { menuOpen = false; viewModel.toggleLayout() },
                            )
                            HorizontalDivider()
                            SortMenuItem("最近更新", NoteSort.UPDATED, state.sort) { viewModel.setSort(it); menuOpen = false }
                            SortMenuItem("创建时间", NoteSort.CREATED, state.sort) { viewModel.setSort(it); menuOpen = false }
                            SortMenuItem("标题", NoteSort.TITLE, state.sort) { viewModel.setSort(it); menuOpen = false }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("回收站") },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                onClick = { menuOpen = false; onOpenTrash() },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.createNote { id -> onCreateNote(id) } },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("新建") },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (searchActive) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("搜索标题、内容或标签") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = viewModel::clearQuery) {
                                Icon(Icons.Filled.Close, contentDescription = "清除")
                            }
                        }
                    },
                    singleLine = true,
                )
            }

            if (state.allTags.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedTag == null,
                            onClick = { viewModel.setSelectedTag(null) },
                            label = { Text("全部") },
                        )
                    }
                    items(state.allTags) { tag ->
                        FilterChip(
                            selected = state.selectedTag == tag,
                            onClick = {
                                viewModel.setSelectedTag(if (state.selectedTag == tag) null else tag)
                            },
                            label = { Text(tag) },
                        )
                    }
                }
            }

            if (state.isEmpty) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (state.query.isBlank()) "还没有笔记，点击右下角新建" else "没有匹配的笔记",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val compact = state.layout == NotesLayout.DOUBLE
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (compact) 2 else 1),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.pinned.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader("已置顶") }
                        gridItems(state.pinned, key = { it.id }) { note ->
                            NoteCardItem(
                                note = note,
                                viewModel = viewModel,
                                compact = compact,
                                onOpen = { onOpenNote(note.id) },
                                onExport = { exportingNote = note; exportLauncher.launch(ImportExport.suggestedFileName(note)) },
                                onShare = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, viewModel.shareText(note))
                                    }
                                    context.startActivity(Intent.createChooser(intent, "分享笔记"))
                                },
                            )
                        }
                    }
                    if (state.others.isNotEmpty()) {
                        if (state.pinned.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader("其它") }
                        }
                        gridItems(state.others, key = { it.id }) { note ->
                            NoteCardItem(
                                note = note,
                                viewModel = viewModel,
                                compact = compact,
                                onOpen = { onOpenNote(note.id) },
                                onExport = { exportingNote = note; exportLauncher.launch(ImportExport.suggestedFileName(note)) },
                                onShare = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, viewModel.shareText(note))
                                    }
                                    context.startActivity(Intent.createChooser(intent, "分享笔记"))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCardItem(
    note: Note,
    viewModel: NoteListViewModel,
    compact: Boolean,
    onOpen: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
) {
    NoteCard(
        note = note,
        onClick = onOpen,
        onTogglePin = { viewModel.togglePin(note.id) },
        onShare = onShare,
        onExport = onExport,
        onDelete = { viewModel.trash(note.id) },
        compact = compact,
    )
}

@Composable
private fun SortMenuItem(
    label: String,
    value: NoteSort,
    current: NoteSort,
    onSelect: (NoteSort) -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label) },
        leadingIcon = {
            if (value == current) {
                Icon(Icons.Filled.Check, contentDescription = null)
            }
        },
        onClick = { onSelect(value) },
    )
}
