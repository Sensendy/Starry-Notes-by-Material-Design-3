package com.starryweaver.notes.data

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

enum class NoteSort { UPDATED, CREATED, TITLE }

/**
 * 以应用私有目录下的单个 JSON 文件作为唯一持久化载体，内存中维护一份全量列表并通过 StateFlow 暴露。
 */
class NoteRepository(
    context: Context,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {
    private val file = File(context.filesDir, "notes.json")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    private val mutex = Mutex()
    private var loadedOnce = false

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    suspend fun ensureLoaded() {
        if (loadedOnce) return
        withContext(io) {
            mutex.withLock {
                if (loadedOnce) return@withLock
                _notes.value = if (file.exists()) readFromDisk() else seedWelcomeNote()
                loadedOnce = true
            }
        }
    }

    suspend fun getById(id: String): Note? {
        ensureLoaded()
        return _notes.value.firstOrNull { it.id == id }
    }

    fun activeNotes(): List<Note> = _notes.value.filterNot { it.isDeleted }

    suspend fun upsert(note: Note) = mutate { list ->
        if (list.any { it.id == note.id }) {
            list.map { if (it.id == note.id) note else it }
        } else {
            list + note
        }
    }

    suspend fun moveToTrash(id: String) = mutate { list ->
        list.map { if (it.id == id) it.copy(deletedAt = System.currentTimeMillis(), pinned = false) else it }
    }

    suspend fun restore(id: String) = mutate { list ->
        list.map { if (it.id == id) it.copy(deletedAt = null) else it }
    }

    suspend fun deleteForever(id: String) = mutate { list -> list.filterNot { it.id == id } }

    suspend fun emptyTrash() = mutate { list -> list.filterNot { it.isDeleted } }

    suspend fun togglePin(id: String) = mutate { list ->
        list.map { if (it.id == id) it.copy(pinned = !it.pinned) else it }
    }

    /** 按 id 合并导入，同 id 取 updatedAt 较新者；返回新增条数。 */
    suspend fun importMerge(incoming: List<Note>): Int {
        var added = 0
        mutate { current ->
            val byId = LinkedHashMap<String, Note>()
            current.forEach { byId[it.id] = it }
            for (note in incoming) {
                val existing = byId[note.id]
                when {
                    existing == null -> {
                        byId[note.id] = note
                        added++
                    }
                    note.updatedAt > existing.updatedAt -> byId[note.id] = note
                }
            }
            byId.values.toList()
        }
        return added
    }

    private suspend fun mutate(block: (List<Note>) -> List<Note>): List<Note> {
        ensureLoaded()
        return withContext(io) {
            mutex.withLock {
                val updated = block(_notes.value)
                writeToDisk(updated)
                _notes.value = updated
                updated
            }
        }
    }

    private fun readFromDisk(): List<Note> =
        runCatching { json.decodeFromString<List<Note>>(file.readText()) }
            .getOrDefault(emptyList())

    /** 首次安装（数据文件不存在）时写入一张使用说明笔记。 */
    private fun seedWelcomeNote(): List<Note> {
        val notes = listOf(Note(title = WelcomeNote.TITLE, content = WelcomeNote.CONTENT))
        writeToDisk(notes)
        return notes
    }

    private fun writeToDisk(list: List<Note>) {
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.writeText(json.encodeToString(list))
        if (file.exists()) file.delete()
        tmp.renameTo(file)
    }
}
