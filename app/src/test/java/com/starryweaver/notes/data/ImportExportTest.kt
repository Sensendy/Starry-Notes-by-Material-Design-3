package com.starryweaver.notes.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportExportTest {

    @Test
    fun jsonRoundTripPreservesNotes() {
        val notes = listOf(
            Note(
                id = "a",
                title = "标题 A",
                content = "# hello\n**bold**",
                tags = listOf("x", "y"),
                pinned = true,
                createdAt = 1000,
                updatedAt = 2000,
            ),
            Note(id = "b", title = "", content = "plain", createdAt = 3000, updatedAt = 3000),
        )
        val restored = ImportExport.importJson(ImportExport.exportJson(notes))
        assertEquals(notes, restored)
    }

    @Test
    fun importsBareArray() {
        val notes = listOf(Note(id = "z", title = "t", content = "c"))
        val raw = Json { encodeDefaults = true }.encodeToString(notes)
        assertEquals(notes, ImportExport.importJson(raw))
    }

    @Test
    fun invalidJsonReturnsEmpty() {
        assertTrue(ImportExport.importJson("not a json").isEmpty())
        assertTrue(ImportExport.importJson("").isEmpty())
    }

    @Test
    fun markdownExportHasFrontMatter() {
        val note = Note(id = "a", title = "标题", content = "body", tags = listOf("t1"), createdAt = 0, updatedAt = 0)
        val markdown = ImportExport.exportMarkdown(note)
        assertTrue(markdown.startsWith("---"))
        assertTrue(markdown.contains("title: 标题"))
        assertTrue(markdown.contains("tags: t1"))
        assertTrue(markdown.endsWith("body"))
    }
}
