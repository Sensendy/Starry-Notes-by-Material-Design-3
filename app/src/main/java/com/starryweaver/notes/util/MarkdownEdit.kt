package com.starryweaver.notes.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/** 编辑器工具栏支持的 Markdown 操作。 */
enum class MarkdownAction {
    H1, H2, H3,
    BOLD, ITALIC, STRIKE, INLINE_CODE,
    CODE_BLOCK, QUOTE, BULLET, ORDERED, TODO, LINK, RULE,
}

/**
 * 基于 [TextFieldValue] 的 Markdown 编辑操作，纯逻辑、无 Android 依赖，便于单元测试。
 * 光标处于选区时对选中文本生效，否则对光标所在行生效。
 */
object MarkdownEdit {

    private val headingRegex = Regex("^#{1,6} ")

    fun apply(value: TextFieldValue, action: MarkdownAction): TextFieldValue = when (action) {
        MarkdownAction.H1 -> setHeading(value, 1)
        MarkdownAction.H2 -> setHeading(value, 2)
        MarkdownAction.H3 -> setHeading(value, 3)
        MarkdownAction.QUOTE -> toggleLinePrefix(value, "> ")
        MarkdownAction.BULLET -> toggleLinePrefix(value, "- ")
        MarkdownAction.ORDERED -> toggleLinePrefix(value, "1. ")
        MarkdownAction.TODO -> toggleLinePrefix(value, "- [ ] ")
        MarkdownAction.BOLD -> wrap(value, "**")
        MarkdownAction.ITALIC -> wrap(value, "*")
        MarkdownAction.STRIKE -> wrap(value, "~~")
        MarkdownAction.INLINE_CODE -> wrap(value, "`")
        MarkdownAction.LINK -> link(value)
        MarkdownAction.CODE_BLOCK -> insertBlock(value, "```\n", "\n```")
        MarkdownAction.RULE -> insertBlock(value, "---\n", "")
    }

    private fun setHeading(value: TextFieldValue, level: Int): TextFieldValue {
        val prefix = "#".repeat(level) + " "
        val lines = blockLines(value)
        val allSame = lines.all { it.startsWith(prefix) }
        return editLines(value) { line ->
            val stripped = line.replace(headingRegex, "")
            if (allSame) stripped else prefix + stripped
        }
    }

    private fun toggleLinePrefix(value: TextFieldValue, prefix: String): TextFieldValue {
        val lines = blockLines(value)
        val allPrefixed = lines.all { it.startsWith(prefix) }
        return editLines(value) { line ->
            when {
                allPrefixed -> line.removePrefix(prefix)
                line.startsWith(prefix) -> line
                else -> prefix + line
            }
        }
    }

    private fun wrap(value: TextFieldValue, marker: String): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        val end = value.selection.max
        val selected = text.substring(start, end)

        if (selected.length >= marker.length * 2 &&
            selected.startsWith(marker) && selected.endsWith(marker)
        ) {
            val inner = selected.substring(marker.length, selected.length - marker.length)
            val newText = text.replaceRange(start, end, inner)
            return value.copy(text = newText, selection = TextRange(start, start + inner.length))
        }

        val hasOuter = start >= marker.length &&
            end + marker.length <= text.length &&
            text.substring(start - marker.length, start) == marker &&
            text.substring(end, end + marker.length) == marker
        if (hasOuter) {
            val newText = text.removeRange(end, end + marker.length).removeRange(start - marker.length, start)
            return value.copy(
                text = newText,
                selection = TextRange(start - marker.length, end - marker.length),
            )
        }

        val newText = text.replaceRange(start, end, marker + selected + marker)
        return value.copy(
            text = newText,
            selection = TextRange(start + marker.length, end + marker.length),
        )
    }

    private fun link(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        val end = value.selection.max
        val label = text.substring(start, end).ifBlank { "文本" }
        val inserted = "[$label](url)"
        val newText = text.replaceRange(start, end, inserted)
        val urlStart = start + label.length + 3
        return value.copy(text = newText, selection = TextRange(urlStart, urlStart + 3))
    }

    private fun insertBlock(value: TextFieldValue, before: String, after: String): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        val end = value.selection.max
        val leading = if (start > 0 && text[start - 1] != '\n') "\n" else ""
        val inserted = leading + before + text.substring(start, end) + after
        val newText = text.replaceRange(start, end, inserted)
        val cursor = start + leading.length + before.length
        return value.copy(
            text = newText,
            selection = TextRange(cursor, cursor + (end - start)),
        )
    }

    private fun blockLines(value: TextFieldValue): List<String> {
        val (start, end) = blockRange(value.text, value.selection.min, value.selection.max)
        return value.text.substring(start, end).split("\n")
    }

    private fun blockRange(text: String, selStart: Int, selEnd: Int): Pair<Int, Int> {
        val start = if (selStart <= 0) {
            0
        } else {
            val newline = text.lastIndexOf('\n', selStart - 1)
            if (newline < 0) 0 else newline + 1
        }
        val newlineAfter = text.indexOf('\n', selEnd.coerceIn(0, text.length))
        val end = if (newlineAfter < 0) text.length else newlineAfter
        return start to end
    }

    private fun editLines(value: TextFieldValue, transform: (String) -> String): TextFieldValue {
        val text = value.text
        val (blockStart, blockEnd) = blockRange(text, value.selection.min, value.selection.max)
        val oldLines = text.substring(blockStart, blockEnd).split("\n")
        val newLines = oldLines.map(transform)
        val newBlock = newLines.joinToString("\n")
        val newText = text.replaceRange(blockStart, blockEnd, newBlock)
        val newStart = mapPosition(oldLines, newLines, blockStart, value.selection.min)
        val newEnd = mapPosition(oldLines, newLines, blockStart, value.selection.max)
        return value.copy(text = newText, selection = TextRange(newStart, newEnd))
    }

    private fun mapPosition(
        oldLines: List<String>,
        newLines: List<String>,
        blockStart: Int,
        position: Int,
    ): Int {
        var oldOffset = 0
        var newOffset = 0
        for (i in oldLines.indices) {
            val lineLen = oldLines[i].length
            if (position <= blockStart + oldOffset + lineLen) {
                val within = (position - blockStart - oldOffset).coerceIn(0, lineLen)
                val delta = newLines[i].length - lineLen
                val newWithin = when {
                    delta == 0 -> within
                    within == 0 && delta > 0 -> delta
                    else -> (within + delta).coerceIn(0, newLines[i].length)
                }
                return blockStart + newOffset + newWithin
            }
            oldOffset += lineLen + 1
            newOffset += newLines[i].length + 1
        }
        return blockStart + newOffset
    }
}
