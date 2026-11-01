package com.starryweaver.notes.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.starryweaver.notes.data.Note
import com.starryweaver.notes.data.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrashViewModel(private val repository: NoteRepository) : ViewModel() {

    val notes: StateFlow<List<Note>> = repository.notes
        .map { list -> list.filter { it.isDeleted }.sortedByDescending { it.deletedAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repository.ensureLoaded() }
    }

    fun restore(id: String) = viewModelScope.launch { repository.restore(id) }

    fun deleteForever(id: String) = viewModelScope.launch { repository.deleteForever(id) }

    fun emptyTrash() = viewModelScope.launch { repository.emptyTrash() }

    companion object {
        fun factory(repository: NoteRepository) = viewModelFactory {
            initializer { TrashViewModel(repository) }
        }
    }
}
