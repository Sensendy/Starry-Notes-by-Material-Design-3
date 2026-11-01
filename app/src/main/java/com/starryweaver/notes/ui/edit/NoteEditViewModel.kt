package com.starryweaver.notes.ui.edit

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.starryweaver.notes.data.Note
import com.starryweaver.notes.data.NoteRepository
import com.starryweaver.notes.util.MarkdownAction
import com.starryweaver.notes.util.MarkdownEdit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/** 从系统分享或「处理文本」进入 app 时携带的初始内容。 */
data class SharedNote(val title: String, val text: String)

enum class EditorPreviewMode { EDIT, SPLIT, PREVIEW }

data class EditorState(
    val title: String = "",
    val content: TextFieldValue = TextFieldValue(""),
    val tags: List<String> = emptyList(),
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val loaded: Boolean = false,
    val previewMode: EditorPreviewMode = EditorPreviewMode.EDIT,
    val dirty: Boolean = false,
)

class NoteEditViewModel(
    private val repository: NoteRepository,
    private val saveScope: CoroutineScope,
    noteId: String,
    seed: SharedNote?,
    fresh: Boolean,
) : ViewModel() {

    val isNew: Boolean = noteId == NEW_ID
    val noteId: String = if (isNew) UUID.randomUUID().toString() else noteId

    /** 本次是新建的空稿，退出时若标题与正文都为空则回收该笔记。 */
    private val isFresh: Boolean = fresh || isNew

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private var saveJob: Job? = null
    private var finished = false

    init {
        if (isNew) {
            _state.value = EditorState(
                title = seed?.title.orEmpty(),
                content = TextFieldValue(seed?.text.orEmpty()),
                loaded = true,
            )
            if (seed != null && (seed.title.isNotBlank() || seed.text.isNotBlank())) {
                saveNow()
            }
        } else {
            viewModelScope.launch {
                val note = repository.getById(noteId)
                _state.value = if (note != null) {
                    EditorState(
                        title = note.title,
                        content = TextFieldValue(note.content),
                        tags = note.tags,
                        pinned = note.pinned,
                        createdAt = note.createdAt,
                        loaded = true,
                    )
                } else {
                    EditorState(loaded = true)
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        _state.update { it.copy(title = value, dirty = true) }
        scheduleSave()
    }

    fun onContentChange(value: TextFieldValue) {
        _state.update { it.copy(content = value, dirty = true) }
        scheduleSave()
    }

    fun applyMarkdown(action: MarkdownAction) {
        _state.update { it.copy(content = MarkdownEdit.apply(it.content, action), dirty = true) }
        scheduleSave()
    }

    fun setPreviewMode(mode: EditorPreviewMode) {
        _state.update { it.copy(previewMode = mode) }
    }

    fun addTag(raw: String) {
        val tag = raw.trim().removePrefix("#")
        if (tag.isEmpty()) return
        _state.update { if (it.tags.contains(tag)) it else it.copy(tags = it.tags + tag, dirty = true) }
        saveNow()
    }

    fun removeTag(tag: String) {
        _state.update { it.copy(tags = it.tags - tag, dirty = true) }
        saveNow()
    }

    fun togglePin() {
        _state.update { it.copy(pinned = !it.pinned, dirty = true) }
        saveNow()
    }

    fun trash(onDone: () -> Unit) {
        if (finished) return
        finished = true
        saveJob?.cancel()
        saveScope.launch { repository.moveToTrash(noteId) }
        onDone()
    }

    /** 离开编辑器：新建的空稿直接回收，否则落盘未保存的改动。 */
    fun onExit() {
        if (finished) return
        finished = true
        saveJob?.cancel()
        val current = _state.value
        val blank = current.title.isBlank() &&
            current.content.text.isBlank() &&
            current.tags.isEmpty()
        saveScope.launch {
            when {
                isFresh && blank -> repository.deleteForever(noteId)
                current.dirty || isNew -> write(current)
            }
        }
    }

    fun saveNow() {
        if (finished) return
        saveJob?.cancel()
        saveScope.launch { persist() }
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = saveScope.launch {
            delay(500)
            persist()
        }
    }

    private suspend fun persist() {
        if (finished) return
        val current = _state.value
        val text = current.content.text
        if (current.title.isBlank() && text.isBlank() && current.tags.isEmpty()) return
        if (!current.dirty && !isNew) return
        write(current)
    }

    private suspend fun write(current: EditorState) {
        val now = System.currentTimeMillis()
        repository.upsert(
            Note(
                id = noteId,
                title = current.title,
                content = current.content.text,
                tags = current.tags,
                pinned = current.pinned,
                createdAt = if (isNew) now else current.createdAt,
                updatedAt = now,
            ),
        )
        _state.update { it.copy(dirty = false) }
    }

    companion object {
        const val NEW_ID = "new"

        fun factory(
            repository: NoteRepository,
            saveScope: CoroutineScope,
            noteId: String,
            seed: SharedNote?,
            fresh: Boolean,
        ) = viewModelFactory {
            initializer { NoteEditViewModel(repository, saveScope, noteId, seed, fresh) }
        }
    }
}
