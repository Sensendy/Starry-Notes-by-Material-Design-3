package com.starryweaver.notes.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class BackupFile(
    val app: String = "starry-notes",
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val notes: List<Note> = emptyList(),
)

/**
 * 纯逻辑的导入导出，不依赖 Android API，便于单元测试。
 */
object ImportExport {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun exportJson(notes: List<Note>): String = json.encodeToString(BackupFile(notes = notes))

    /** 同时兼容完整备份文件与裸笔记数组两种 JSON 形态；无法解析时返回空列表。 */
    fun importJson(text: String): List<Note> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return emptyList()
        return runCatching { json.decodeFromString<BackupFile>(trimmed).notes }
            .recoverCatching { json.decodeFromString<List<Note>>(trimmed) }
            .getOrDefault(emptyList())
    }

    fun exportMarkdown(note: Note): String = buildString {
        append("---\n")
        append("title: ").append(note.title).append('\n')
        if (note.tags.isNotEmpty()) {
            append("tags: ").append(note.tags.joinToString(", ")).append('\n')
        }
        append("created: ").append(formatDate(note.createdAt)).append('\n')
        append("updated: ").append(formatDate(note.updatedAt)).append('\n')
        append("---\n\n")
        append(note.content)
    }

    fun shareText(note: Note): String =
        if (note.title.isBlank()) note.content else "${note.title}\n\n${note.content}"

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))

    fun suggestedFileName(note: Note): String {
        val base = note.title.ifBlank { "note" }
            .replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")
            .trim('_')
            .take(40)
            .ifBlank { "note" }
        return "$base.md"
    }
}
