package com.starryweaver.notes.util

sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class Code(val language: String, val code: String) : MdBlock
    data class Quote(val text: String) : MdBlock
    data class BulletList(val items: List<String>) : MdBlock
    data class OrderedList(val items: List<String>) : MdBlock
    data object Rule : MdBlock
}

/**
 * 轻量 Markdown 块级解析，只覆盖预览常用的语法：标题、段落、围栏代码块、引用、有序/无序列表、分隔线。
 * 行内语法（加粗、斜体、行内代码、删除线、链接）在渲染层处理。
 */
object MarkdownParser {

    private val headingRegex = Regex("^(#{1,6})\\s+(.*)$")
    private val bulletRegex = Regex("^\\s*[-*+]\\s+(.*)$")
    private val orderedRegex = Regex("^\\s*\\d+[.)]\\s+(.*)$")
    private val ruleRegex = Regex("^\\s*([-*_])\\s*(\\1\\s*){2,}$")

    fun parse(markdown: String): List<MdBlock> {
        val blocks = mutableListOf<MdBlock>()
        val lines = markdown.replace("\r\n", "\n").split("\n")
        val paragraph = mutableListOf<String>()
        var i = 0

        fun flushParagraph() {
            if (paragraph.isNotEmpty()) {
                blocks += MdBlock.Paragraph(paragraph.joinToString(" ").trim())
                paragraph.clear()
            }
        }

        while (i < lines.size) {
            val line = lines[i]
            when {
                line.trimStart().startsWith("```") -> {
                    flushParagraph()
                    val language = line.trim().removePrefix("```").trim()
                    val code = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                        code += lines[i]
                        i++
                    }
                    blocks += MdBlock.Code(language, code.joinToString("\n"))
                }

                ruleRegex.matches(line) -> {
                    flushParagraph()
                    blocks += MdBlock.Rule
                }

                headingRegex.matches(line) -> {
                    flushParagraph()
                    val match = headingRegex.find(line)!!
                    blocks += MdBlock.Heading(match.groupValues[1].length, match.groupValues[2].trim())
                }

                line.trimStart().startsWith(">") -> {
                    flushParagraph()
                    val quote = mutableListOf<String>()
                    while (i < lines.size && lines[i].trimStart().startsWith(">")) {
                        quote += lines[i].trimStart().removePrefix(">").trimStart()
                        i++
                    }
                    blocks += MdBlock.Quote(quote.joinToString("\n"))
                    continue
                }

                bulletRegex.matches(line) -> {
                    flushParagraph()
                    val items = mutableListOf<String>()
                    while (i < lines.size && bulletRegex.matches(lines[i])) {
                        items += bulletRegex.find(lines[i])!!.groupValues[1]
                        i++
                    }
                    blocks += MdBlock.BulletList(items)
                    continue
                }

                orderedRegex.matches(line) -> {
                    flushParagraph()
                    val items = mutableListOf<String>()
                    while (i < lines.size && orderedRegex.matches(lines[i])) {
                        items += orderedRegex.find(lines[i])!!.groupValues[1]
                        i++
                    }
                    blocks += MdBlock.OrderedList(items)
                    continue
                }

                line.isBlank() -> flushParagraph()

                else -> paragraph += line.trim()
            }
            i++
        }
        flushParagraph()
        return blocks
    }
}
