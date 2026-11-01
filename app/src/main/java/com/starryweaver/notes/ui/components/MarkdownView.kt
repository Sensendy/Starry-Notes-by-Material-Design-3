package com.starryweaver.notes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.starryweaver.notes.util.MarkdownParser
import com.starryweaver.notes.util.MdBlock

private val inlineRegex = Regex(
    "`([^`]+)`" +
        "|\\*\\*([^*]+)\\*\\*" +
        "|__([^_]+)__" +
        "|\\*([^*\\n]+)\\*" +
        "|_([^_\\n]+)_" +
        "|~~([^~]+)~~" +
        "|\\[([^\\]]+)\\]\\(([^)\\s]+)\\)",
)

private fun renderInline(text: String, linkColor: Color, codeBackground: Color): AnnotatedString =
    buildAnnotatedString {
        var last = 0
        for (match in inlineRegex.findAll(text)) {
            if (match.range.first > last) append(text.substring(last, match.range.first))
            val groups = match.groupValues
            when {
                groups[1].isNotEmpty() -> withStyle(
                    SpanStyle(fontFamily = FontFamily.Monospace, background = codeBackground),
                ) { append(groups[1]) }

                groups[2].isNotEmpty() || groups[3].isNotEmpty() -> {
                    val value = if (groups[2].isNotEmpty()) groups[2] else groups[3]
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(value) }
                }

                groups[4].isNotEmpty() || groups[5].isNotEmpty() -> {
                    val value = if (groups[4].isNotEmpty()) groups[4] else groups[5]
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(value) }
                }

                groups[6].isNotEmpty() -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    append(groups[6])
                }

                else -> withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                    append(groups[7])
                }
            }
            last = match.range.last + 1
        }
        if (last < text.length) append(text.substring(last))
    }

@Composable
fun MarkdownView(markdown: String, modifier: Modifier = Modifier) {
    val blocks = remember(markdown) { MarkdownParser.parse(markdown) }
    val linkColor = MaterialTheme.colorScheme.primary
    val codeBackground = MaterialTheme.colorScheme.surfaceVariant
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        blocks.forEach { block -> RenderBlock(block, linkColor, codeBackground, muted) }
    }
}

@Composable
private fun RenderBlock(
    block: MdBlock,
    linkColor: Color,
    codeBackground: Color,
    muted: Color,
) {
    when (block) {
        is MdBlock.Heading -> Text(
            text = renderInline(block.text, linkColor, codeBackground),
            style = when (block.level) {
                1 -> MaterialTheme.typography.headlineMedium
                2 -> MaterialTheme.typography.headlineSmall
                3 -> MaterialTheme.typography.titleLarge
                else -> MaterialTheme.typography.titleMedium
            },
            fontWeight = FontWeight.Bold,
        )

        is MdBlock.Paragraph -> Text(
            text = renderInline(block.text, linkColor, codeBackground),
            style = MaterialTheme.typography.bodyLarge,
        )

        is MdBlock.Code -> Surface(color = codeBackground, shape = RoundedCornerShape(8.dp)) {
            Text(
                text = block.code,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
            )
        }

        is MdBlock.Quote -> Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(muted, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = renderInline(block.text, linkColor, codeBackground),
                style = MaterialTheme.typography.bodyLarge,
                color = muted,
            )
        }

        is MdBlock.BulletList -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            block.items.forEach { item ->
                Row {
                    Text("•  ", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = renderInline(item, linkColor, codeBackground),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        is MdBlock.OrderedList -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            block.items.forEachIndexed { index, item ->
                Row {
                    Text("${index + 1}.  ", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = renderInline(item, linkColor, codeBackground),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        MdBlock.Rule -> HorizontalDivider()
    }
}
