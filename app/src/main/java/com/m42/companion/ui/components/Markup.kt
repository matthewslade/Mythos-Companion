package com.m42.companion.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import com.m42.companion.engine.TextStyle
import com.m42.companion.ui.theme.CardColors

private val Bold = SpanStyle(fontWeight = FontWeight.Bold)
private val Italic = SpanStyle(fontStyle = FontStyle.Italic)
private val SkillTestBrackets = Regex("""\[[^\]\n]+]""")

/** Card text ready to draw: the styled string plus the term kinds whose icons it uses. */
class MarkedText(val text: AnnotatedString, val kinds: Set<TermKind>)

/** A highlighted term found in the plain text. */
data class TermMatch(val kind: TermKind, val range: IntRange, val iconAt: Int)

/**
 * Renders card text markup — `**bold**`, `*italic*`, line breaks — then colours game terms (resources, skill
 * tests, threats, conditions, statuses and possessions) and puts each term's icon in front of it.
 */
fun cardMarkup(text: String, style: TextStyle, colors: CardColors): MarkedText {
    val (parsed, spans) = parseEmphasis(text)
    val terms = findTerms(parsed, style)
    val plain = keepTermsTogether(parsed, terms)

    // Each icon is inserted with a word joiner after it, so every original offset after an icon shifts by two.
    val iconPositions = terms.filter { it.kind.icon != null }.map { it.iconAt }.sorted()
    fun shifted(index: Int) = index + 2 * iconPositions.count { it < index }

    val builder = AnnotatedString.Builder()
    var cursor = 0
    for (position in iconPositions) {
        builder.append(plain.substring(cursor, position))
        // A blank alternate text keeps screen readers from announcing the placeholder character.
        builder.appendInlineContent(terms.first { it.kind.icon != null && it.iconAt == position }.kind.name, " ")
        builder.append(WORD_JOINER)
        cursor = position
    }
    builder.append(plain.substring(cursor))

    spans.forEach { (span, start, end) -> builder.addStyle(span, shifted(start), shifted(end)) }
    SkillTestBrackets.findAll(plain).forEach { builder.addStyle(Bold, shifted(it.range.first), shifted(it.range.last + 1)) }
    for (term in terms) {
        val color = colors.tone(term.kind.tone)
        val termStyle = if (term.kind == TermKind.TRAIT) {
            SpanStyle(color = color, fontSize = 0.82.em, letterSpacing = 0.04.em, fontWeight = FontWeight.SemiBold)
        } else {
            SpanStyle(color = color, fontWeight = FontWeight.Bold)
        }
        builder.addStyle(termStyle, shifted(term.range.first), shifted(term.range.last + 1))
    }
    return MarkedText(builder.toAnnotatedString(), terms.map { it.kind }.toSet())
}

private const val WORD_JOINER = "\u2060"
private const val SHORT_TERM = 24

/**
 * Stops a line break from splitting a short term ("1 Clue", "Luck (-1) check") by using non-breaking spaces
 * inside it. Longer terms may still wrap, so they never overflow a narrow screen.
 */
private fun keepTermsTogether(plain: String, terms: List<TermMatch>): String {
    val chars = plain.toCharArray()
    for (term in terms) {
        if (term.range.last - term.range.first + 1 > SHORT_TERM) continue
        for (i in term.range) if (chars[i] == ' ') chars[i] = '\u00A0'
    }
    return String(chars)
}

/** Finds highlighted terms; when matches overlap, the rule listed first in [TermRules] wins. */
fun findTerms(plain: String, style: TextStyle): List<TermMatch> {
    val claimed = BooleanArray(plain.length)
    val found = mutableListOf<TermMatch>()
    for (rule in TermRules) {
        if (style !in rule.games) continue
        for (match in rule.regex.findAll(plain)) {
            val range = match.range
            if (range.isEmpty() || range.any { claimed[it] }) continue
            range.forEach { claimed[it] = true }
            val iconAt = (if (rule.hasIconGroup) match.groups["k"]?.range?.first else null) ?: range.first
            found += TermMatch(rule.kind(match), range, iconAt)
        }
    }
    return found.sortedBy { it.range.first }
}

/** Icons for the term kinds used in a piece of text, sized to the surrounding text and tinted to match. */
fun termIcons(kinds: Set<TermKind>, colors: CardColors): Map<String, InlineTextContent> =
    kinds.mapNotNull { kind ->
        val icon = kind.icon ?: return@mapNotNull null
        kind.name to InlineTextContent(Placeholder(1.15.em, 1.em, PlaceholderVerticalAlign.TextCenter)) {
            Icon(icon, contentDescription = null, tint = colors.tone(kind.tone), modifier = Modifier.fillMaxSize())
        }
    }.toMap()

private fun parseEmphasis(text: String): Pair<String, List<Triple<SpanStyle, Int, Int>>> {
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
    return plain.toString() to spans
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
