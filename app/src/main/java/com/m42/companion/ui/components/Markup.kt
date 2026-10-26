package com.m42.companion.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.m42.companion.engine.TextStyle
import com.m42.companion.ui.theme.CardColors

private val Bold = SpanStyle(fontWeight = FontWeight.Bold)
private val Italic = SpanStyle(fontStyle = FontStyle.Italic)
private val SkillTest = Regex("""\[[^\]\n]+]""")
private val Resource = Regex("""(\d+ )?\b(Stamina|Sanity|Clue Tokens?)\b""")

/**
 * Renders the card text markup: `**bold**`, `*italic*` and line breaks. Skill tests in brackets are emboldened,
 * and Arkham cards colour Stamina, Sanity and Clue Tokens as the legacy app did.
 */
fun cardMarkup(text: String, style: TextStyle, colors: CardColors): AnnotatedString {
    val plain = StringBuilder()
    val spans = mutableListOf<Triple<SpanStyle, Int, Int>>()
    var boldStart = -1
    var italicStart = -1
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                if (boldStart < 0) boldStart = plain.length
                else spans += Triple(Bold, boldStart, plain.length).also { boldStart = -1 }
                i += 2
            }
            text[i] == '*' && (italicStart >= 0 || hasClosingStar(text, i + 1)) -> {
                if (italicStart < 0) italicStart = plain.length
                else spans += Triple(Italic, italicStart, plain.length).also { italicStart = -1 }
                i += 1
            }
            else -> plain.append(text[i++])
        }
    }
    val result = plain.toString()
    return AnnotatedString.Builder(result).apply {
        spans.forEach { (span, start, end) -> addStyle(span, start, end) }
        SkillTest.findAll(result).forEach { addStyle(Bold, it.range.first, it.range.last + 1) }
        if (style == TextStyle.ARKHAM) {
            Resource.findAll(result).forEach { match ->
                val colour = when {
                    match.groupValues[2] == "Stamina" -> colors.stamina
                    match.groupValues[2] == "Sanity" -> colors.sanity
                    else -> colors.clue
                }
                addStyle(SpanStyle(color = colour, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            }
        }
    }.toAnnotatedString()
}

/** A single star only opens italics when a closing star follows on the same line (`*x*`, or `***x***`). */
private fun hasClosingStar(text: String, from: Int): Boolean {
    var j = from
    while (j < text.length && text[j] != '\n') {
        if (text[j] == '*') {
            var run = 0
            while (j + run < text.length && text[j + run] == '*') run++
            if (run % 2 == 1) return true
            j += run
        } else {
            j++
        }
    }
    return false
}
