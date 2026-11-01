package com.starryweaver.notes.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserTest {

    @Test
    fun parsesHeadingAndParagraph() {
        val blocks = MarkdownParser.parse("# 标题\n\n正文内容\n")
        assertEquals(MdBlock.Heading(1, "标题"), blocks[0])
        assertEquals(MdBlock.Paragraph("正文内容"), blocks[1])
    }

    @Test
    fun parsesLists() {
        val blocks = MarkdownParser.parse("- a\n- b\n\n1. one\n2. two\n")
        assertEquals(MdBlock.BulletList(listOf("a", "b")), blocks[0])
        assertEquals(MdBlock.OrderedList(listOf("one", "two")), blocks[1])
    }

    @Test
    fun parsesFencedCodeBlock() {
        val blocks = MarkdownParser.parse("```kotlin\nfun main() {}\n```\n")
        assertEquals(MdBlock.Code("kotlin", "fun main() {}"), blocks[0])
    }

    @Test
    fun parsesQuoteAndRule() {
        assertEquals(MdBlock.Quote("quoted"), MarkdownParser.parse("> quoted")[0])
        assertEquals(listOf(MdBlock.Rule), MarkdownParser.parse("---"))
    }

    @Test
    fun emptyInputProducesNoBlocks() {
        assertTrue(MarkdownParser.parse("").isEmpty())
    }
}
