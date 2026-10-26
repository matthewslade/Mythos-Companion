package com.m42.companion.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.m42.companion.ui.GameViewModel

@Composable
fun CheckRow(label: String, checked: Boolean, onToggle: () -> Unit, enabled: Boolean = true, indent: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(start = if (indent) 32.dp else 0.dp)
            .alpha(if (enabled) 1f else 0.45f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
    }
}

fun galleryActions(vm: GameViewModel<*, *>) = GalleryActions(
    onResolve = vm::resolve,
    onDiscard = vm::discard,
    onShuffle = vm::shuffleOpenDeck,
    onReturnToDeck = vm::returnToDeck,
    onClose = vm::closeGallery,
)

/** Full-screen gallery used on phones. Closing the gallery (or any card action) returns to the board. */
@Composable
fun GalleryScreen(vm: GameViewModel<*, *>, onClosed: () -> Unit) {
    val gallery by vm.gallery.collectAsStateWithLifecycle()
    val current = gallery
    LaunchedEffect(current == null) { if (current == null) onClosed() }
    if (current == null) return
    BackHandler(onBack = vm::closeGallery)
    val wide = LocalConfiguration.current.screenWidthDp >= 600
    Surface(Modifier.fillMaxSize()) {
        GalleryPane(
            current,
            galleryActions(vm),
            modifier = Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
            textScale = if (wide) 1.15f else 1f,
        )
    }
}

/** Start / Continue, pinned to the bottom of the setup screens so they are reachable without scrolling. */
@Composable
fun SetupActions(ready: Boolean, hasSave: Boolean, onStart: () -> Unit, onContinue: () -> Unit) {
    Surface(tonalElevation = 3.dp, shadowElevation = 6.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        ) {
            Button(onClick = onStart, enabled = ready, modifier = Modifier.widthIn(min = 140.dp)) {
                Text(if (hasSave) "New game" else "Start")
            }
            if (hasSave) {
                OutlinedButton(onClick = onContinue, modifier = Modifier.widthIn(min = 140.dp)) { Text("Continue") }
            }
        }
    }
}
