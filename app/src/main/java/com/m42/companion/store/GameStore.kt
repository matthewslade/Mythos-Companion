package com.m42.companion.store

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Holds one game's session in memory and mirrors every change to a JSON file, so a game survives the app
 * being closed or killed in the background. Writes happen off the main thread; only the latest state is kept.
 */
class GameStore<S : Any>(
    scope: CoroutineScope,
    directory: File,
    fileName: String,
    private val serializer: KSerializer<S>,
) {
    private val file = File(directory, fileName)
    private val json = Json { ignoreUnknownKeys = true }
    private val _session = MutableStateFlow<S?>(null)
    private val pendingWrites = Channel<Write<S>>(Channel.CONFLATED)

    val session: StateFlow<S?> = _session.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            for (write in pendingWrites) {
                runCatching { persist(write.session) }.onFailure { Log.e(TAG, "Could not save ${file.name}", it) }
            }
        }
    }

    fun hasSavedGame(): Boolean = _session.value != null || file.exists()

    /** The current session, loading it from disk if this process has not seen it yet. */
    suspend fun restore(): S? = _session.value ?: withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext null
        runCatching { json.decodeFromString(serializer, file.readText()) }
            .onFailure {
                Log.w(TAG, "Discarding unreadable save ${file.name}", it)
                file.delete()
            }
            .getOrNull()
    }?.also { loaded -> _session.compareAndSet(null, loaded) }

    fun start(session: S) {
        _session.value = session
        pendingWrites.trySend(Write(session))
    }

    fun update(transform: (S) -> S) {
        val updated = _session.updateAndGet { it?.let(transform) } ?: return
        pendingWrites.trySend(Write(updated))
    }

    fun end() {
        _session.value = null
        pendingWrites.trySend(Write(null))
    }

    private fun persist(session: S?) {
        if (session == null) {
            file.delete()
            return
        }
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.writeText(json.encodeToString(serializer, session))
        if (!tmp.renameTo(file)) {
            tmp.copyTo(file, overwrite = true)
            tmp.delete()
        }
    }

    private class Write<S>(val session: S?)

    private companion object {
        const val TAG = "GameStore"
    }
}
