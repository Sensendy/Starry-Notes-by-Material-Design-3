package com.starryweaver.notes.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.starryweaver.notes.data.ImportExport
import com.starryweaver.notes.data.Note
import com.starryweaver.notes.data.NoteRepository
import com.starryweaver.notes.data.NoteSort
import com.starryweaver.notes.data.NotesLayout
import com.starryweaver.notes.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NoteListUiState(
    val pinned: List<Note> = emptyList(),
    val others: List<Note> = emptyList(),
    val allTags: List<String> = emptyList(),
    val query: String = "",
    val selectedTag: String? = null,
    val sort: NoteSort = NoteSort.UPDATED,
    val layout: NotesLayout = NotesLayout.SINGLE,
) {
    val isEmpty: Boolean get() = pinned.isEmpty() && others.isEmpty()
}

class NoteListViewModel(
    private val repository: NoteRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedTag = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NoteListUiState> = combine(
        repository.notes,
        query,
        selectedTag,
        settings.sort,
        settings.notesLayout,
    ) { notes, q, tag, sort, layout ->
        val active = notes.filterNot { it.isDeleted }
        val filtered = active.filter { note ->
            val matchesQuery = q.isBlank() ||
                note.title.contains(q, ignoreCase = true) ||
                note.content.contains(q, ignoreCase = true) ||
                note.tags.any { it.contains(q, ignoreCase = true) }
            val matchesTag = tag == null || note.tags.contains(tag)
            matchesQuery && matchesTag
        }
        val sorted = when (sort) {
            NoteSort.UPDATED -> filtered.sortedByDescending { it.updatedAt }
            NoteSort.CREATED -> filtered.sortedByDescending { it.createdAt }
            NoteSort.TITLE -> filtered.sortedWith(compareBy { it.title.lowercase() })
        }
        NoteListUiState(
            pinned = sorted.filter { it.pinned },
            others = sorted.filterNot { it.pinned },
            allTags = active.flatMap { it.tags }.distinct().sorted(),
            query = q,
            selectedTag = tag,
            sort = sort,
            layout = layout,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteListUiState())

    init {
        viewModelScope.launch { repository.ensureLoaded() }
    }

    fun onQueryChange(value: String) { query.value = value }

    fun clearQuery() { query.value = "" }

    fun setSelectedTag(tag: String?) { selectedTag.value = tag }

    fun setSort(sort: NoteSort) = settings.setSort(sort)

    fun toggleLayout() {
        val next = if (settings.notesLayout.value == NotesLayout.SINGLE) {
            NotesLayout.DOUBLE
        } else {
            NotesLayout.SINGLE
        }
        settings.setNotesLayout(next)
    }

    fun togglePin(id: String) = viewModelScope.launch { repository.togglePin(id) }

    fun trash(id: String) = viewModelScope.launch { repository.moveToTrash(id) }

    fun shareText(note: Note): String = ImportExport.shareText(note)

    fun createNote(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val note = Note()
            repository.upsert(note)
            onCreated(note.id)
        }
    }

    companion object {
        fun factory(repository: NoteRepository, settings: SettingsRepository) = viewModelFactory {
            initializer { NoteListViewModel(repository, settings) }
        }
    }
}
