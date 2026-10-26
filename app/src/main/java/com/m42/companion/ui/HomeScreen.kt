package com.m42.companion.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.m42.companion.R
import com.m42.companion.ui.components.Emblem

@Composable
fun HomeScreen(
    eldritchInProgress: Boolean,
    arkhamInProgress: Boolean,
    onEldritch: () -> Unit,
    onArkham: () -> Unit,
) {
    Surface(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val wide = maxWidth >= 600.dp
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            ) {
                Text("Mythos Companion", style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
                Text(
                    "Encounter decks for Eldritch Horror and Arkham Horror (2nd edition).",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                val eldritch = @Composable { m: Modifier ->
                    GameCard("Eldritch Horror", R.drawable.emblem_elder_sign, eldritchInProgress, onEldritch, m)
                }
                val arkham = @Composable { m: Modifier ->
                    GameCard("Arkham Horror", R.drawable.emblem_cthulhu, arkhamInProgress, onArkham, m)
                }
                if (wide) {
                    Row(Modifier.widthIn(max = 880.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        eldritch(Modifier.weight(1f))
                        arkham(Modifier.weight(1f))
                    }
                } else {
                    eldritch(Modifier.fillMaxWidth())
                    arkham(Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun GameCard(title: String, emblem: Int, inProgress: Boolean, onClick: () -> Unit, modifier: Modifier) {
    ElevatedCard(onClick = onClick, modifier = modifier) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Emblem(emblem, 120.dp)
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Text(
                if (inProgress) "Game in progress" else "No game in progress",
                style = MaterialTheme.typography.labelLarge,
                color = if (inProgress) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
