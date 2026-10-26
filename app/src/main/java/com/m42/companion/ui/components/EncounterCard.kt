package com.m42.companion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m42.companion.engine.CardFace
import com.m42.companion.engine.Encountered
import com.m42.companion.engine.SectionPositions
import com.m42.companion.ui.theme.CardBodyStyle
import com.m42.companion.ui.theme.Caslon
import com.m42.companion.ui.theme.LocalCardColors

/**
 * One encounter card. Tapping a selectable section resolves the card with that section. In the discard pile,
 * sections other than the one that was resolved are shaded, as in the original app.
 */
@Composable
fun EncounterCard(
    face: CardFace,
    onSection: ((Encountered) -> Unit)?,
    modifier: Modifier = Modifier,
    textScale: Float = 1f,
) {
    val colors = LocalCardColors.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.paper, contentColor = colors.ink),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            face.sections.forEachIndexed { index, section ->
                val position = SectionPositions[index]
                if (section.header == null && section.text.isBlank()) return@forEachIndexed
                val selectable = onSection != null && position in face.selectable
                val shaded = when (face.encountered) {
                    Encountered.REMOVED -> true
                    Encountered.TOP, Encountered.MIDDLE, Encountered.BOTTOM -> position != face.encountered
                    else -> false
                }
                val marked = remember(section.text, face.textStyle, colors) { cardMarkup(section.text, face.textStyle, colors) }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .then(if (shaded) Modifier.background(colors.shaded) else Modifier)
                        .then(
                            if (selectable) {
                                Modifier
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    .clickable(role = Role.Button, onClickLabel = "Resolve this encounter") {
                                        onSection(position)
                                    }
                            } else Modifier,
                        )
                        .alpha(if (shaded) 0.55f else 1f)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    section.header?.let {
                        Text(
                            it,
                            fontFamily = Caslon,
                            fontSize = 24.sp * textScale,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        )
                    }
                    if (section.text.isNotBlank()) {
                        Text(
                            marked.text,
                            inlineContent = remember(marked, colors) { termIcons(marked.kinds, colors) },
                            style = CardBodyStyle.copy(
                                fontSize = CardBodyStyle.fontSize * textScale,
                                lineHeight = CardBodyStyle.lineHeight * textScale,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            Text(
                face.cardId,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = colors.ink.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp, end = 4.dp),
            )
        }
    }
}
