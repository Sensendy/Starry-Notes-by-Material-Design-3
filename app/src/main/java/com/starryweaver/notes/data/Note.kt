package com.starryweaver.notes.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
) {
    val isDeleted: Boolean get() = deletedAt != null

    val displayTitle: String get() = title.ifBlank { "无标题" }

    /** 列表预览用的单行摘要，去掉 Markdown 常见标记。 */
    val preview: String
        get() = content
            .lineSequence()
            .map { it.replace(Regex("^\\s*(#{1,6}|>|[-*+]|\\d+[.)])\\s*"), "") }
            .map { it.replace(Regex("[*_`~]"), "") }
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?: ""
}
