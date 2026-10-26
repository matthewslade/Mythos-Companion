package com.m42.companion

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.m42.companion.data.CardDataParser
import com.m42.companion.data.CardSection
import com.m42.companion.engine.CardFace
import com.m42.companion.engine.Encountered
import com.m42.companion.engine.TextStyle
import com.m42.companion.ui.components.EncounterCard
import com.m42.companion.ui.components.TermKind
import com.m42.companion.ui.components.findTerms
import com.m42.companion.ui.theme.CompanionTheme
import com.m42.companion.ui.theme.LocalCardColors
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Renders one real card per highlighted term kind and game, in light and dark themes, and saves PNGs to the
 * app's files/markup folder so the colouring can be checked by eye. Not an assertion test.
 */
@RunWith(AndroidJUnit4::class)
class MarkupScreenshots {
    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val out = File(context.filesDir, "markup").apply { deleteRecursively(); mkdirs() }

    private data class Example(val game: String, val face: CardFace)

    /** Every encounter text with the header it would show, per game. */
    private val candidates: List<Pair<TextStyle, CardFace>> by lazy {
        val eldritch = CardDataParser.eldritch(context.assets.open("eldritch.json")).cards.flatMap { card ->
            card.sections.mapIndexedNotNull { i, section ->
                if (section.text.isBlank()) return@mapIndexedNotNull null
                val header = section.header ?: card.title
                TextStyle.ELDRITCH to face("${card.id}#${i + 1}", CardSection(header, section.text), TextStyle.ELDRITCH)
            }
        }
        val arkhamData = CardDataParser.arkham(context.assets.open("arkham.json"))
        val arkham = arkhamData.locationEncounters.map { TextStyle.ARKHAM to face(it.id, CardSection(it.location, it.text), TextStyle.ARKHAM) } +
            arkhamData.otherWorldEncounters.map {
                TextStyle.ARKHAM to face(it.id, CardSection(it.world ?: "Other World", it.text), TextStyle.ARKHAM)
            }
        eldritch + arkham
    }

    private fun face(id: String, section: CardSection, style: TextStyle) =
        CardFace(id, "", "", listOf(section), emptySet(), Encountered.NONE, style)

    /** The shortest reasonably sized card in each game that shows [kind]. */
    private fun examples(kind: TermKind): List<Example> = listOf(TextStyle.ELDRITCH, TextStyle.ARKHAM).mapNotNull { style ->
        candidates.asSequence()
            .filter { (s, face) -> s == style && face.sections[0].text.length in 70..300 }
            .filter { (_, face) -> findTerms(face.sections[0].text, style).any { it.kind == kind } }
            .minByOrNull { (_, face) -> face.sections[0].text.length }
            ?.let { (_, face) -> Example(if (style == TextStyle.ELDRITCH) "Eldritch Horror" else "Arkham Horror", face) }
    }

    @Test
    fun captureEveryTermKind() {
        var page by mutableStateOf<(@Composable () -> Unit)?>(null)
        rule.setContent { Box(Modifier.testTag("shot").wrapContentHeight()) { page?.invoke() } }

        fun save(name: String, content: @Composable () -> Unit) {
            page = content
            rule.waitForIdle()
            val bitmap = rule.onNodeWithTag("shot").captureToImage().asAndroidBitmap()
            File(out, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }

        save("00-legend") { BothThemes("Legend") { Legend() } }
        save("00-icons-large") { BothThemes("Icons") { LargeIcons() } }
        TermKind.entries.forEachIndexed { index, kind ->
            val examples = examples(kind)
            if (examples.isEmpty()) return@forEachIndexed
            val name = "%02d-%s".format(index + 1, kind.name.lowercase())
            save(name) {
                BothThemes(kind.label) {
                    examples.forEach { example ->
                        Text(example.game, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        EncounterCard(example.face, onSection = null, modifier = Modifier.fillMaxWidth(), textScale = 1.1f)
                    }
                }
            }
        }
    }

    @Composable
    private fun BothThemes(title: String, content: @Composable () -> Unit) {
        Row(Modifier.width(1240.dp)) {
            for (dark in listOf(false, true)) {
                CompanionTheme(darkTheme = dark) {
                    Surface(Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (dark) "$title — dark" else "$title — light", style = MaterialTheme.typography.titleLarge)
                            content()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LargeIcons() {
        val colors = LocalCardColors.current
        Column(Modifier.background(colors.paper).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TermKind.entries.filter { it.icon != null }.chunked(8).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { kind -> Icon(kind.icon!!, kind.label, tint = colors.tone(kind.tone), modifier = Modifier.size(48.dp)) }
                }
            }
        }
    }

    @Composable
    private fun Legend() {
        val colors = LocalCardColors.current
        Column(Modifier.background(colors.paper).padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            TermKind.entries.chunked(2).forEach { pair ->
                Row {
                    pair.forEach { kind ->
                        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            val tint = colors.tone(kind.tone)
                            if (kind.icon != null) Icon(kind.icon!!, null, tint = tint, modifier = Modifier.size(20.dp))
                            else Box(Modifier.size(14.dp).background(tint, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(kind.label, color = tint, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
