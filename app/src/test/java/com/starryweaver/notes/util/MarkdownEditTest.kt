package com.starryweaver.notes.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownEditTest {

    private fun value(text: String, start: Int = 0, end: Int = start) =
        TextFieldValue(text = text, selection = TextRange(start, end))

    @Test
    fun boldWrapsSelection() {
        val result = MarkdownEdit.apply(value("hello world", 0, 5), MarkdownAction.BOLD)
        assertEquals("**hello** world", result.text)
        assertEquals(TextRange(2, 7), result.selection)
    }

    @Test
    fun boldWithEmptySelectionInsertsMarkers() {
        val result = MarkdownEdit.apply(value(""), MarkdownAction.BOLD)
        assertEquals("****", result.text)
        assertEquals(TextRange(2, 2), result.selection)
    }

    @Test
    fun boldTogglesOffWhenSelectionAlreadyWrapped() {
        val result = MarkdownEdit.apply(value("**hello**", 0, 9), MarkdownAction.BOLD)
        assertEquals("hello", result.text)
        assertEquals(TextRange(0, 5), result.selection)
    }

    @Test
    fun headingAddsThenRemovesPrefix() {
        val added = MarkdownEdit.apply(value("title", 5), MarkdownAction.H1)
        assertEquals("# title", added.text)
        assertEquals(TextRange(7, 7), added.selection)

        val removed = MarkdownEdit.apply(added, MarkdownAction.H1)
        assertEquals("title", removed.text)
        assertEquals(TextRange(5, 5), removed.selection)
    }

    @Test
    fun headingReplacesExistingLevel() {
        val result = MarkdownEdit.apply(value("# title"), MarkdownAction.H2)
        assertEquals("## title", result.text)
    }

    @Test
    fun quotePrefixesEverySelectedLine() {
        val result = MarkdownEdit.apply(value("a\nb", 0, 3), MarkdownAction.QUOTE)
        assertEquals("> a\n> b", result.text)
    }

    @Test
    fun todoPrefixToggles() {
        val added = MarkdownEdit.apply(value("task", 4), MarkdownAction.TODO)
        assertEquals("- [ ] task", added.text)

        val removed = MarkdownEdit.apply(added, MarkdownAction.TODO)
        assertEquals("task", removed.text)
    }

    @Test
    fun codeBlockPlacesCursorInside() {
        val result = MarkdownEdit.apply(value(""), MarkdownAction.CODE_BLOCK)
        assertEquals("```\n\n```", result.text)
        assertEquals(TextRange(4, 4), result.selection)
    }

    @Test
    fun ruleStartsOnNewLine() {
        val result = MarkdownEdit.apply(value("text", 4), MarkdownAction.RULE)
        assertEquals("text\n---\n", result.text)
    }

    @Test
    fun linkWrapsSelectionAndSelectsUrl() {
        val result = MarkdownEdit.apply(value("site", 0, 4), MarkdownAction.LINK)
        assertEquals("[site](url)", result.text)
        assertEquals("url", result.text.substring(result.selection.min, result.selection.max))
    }
}
