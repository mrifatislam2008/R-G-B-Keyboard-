package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ClipboardItem(
    val id: Long,
    val text: String,
    val timestamp: Long,
    val isPinned: Boolean = false
)

class ClipboardManagerHelper(private val context: Context) {
    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    private val prefs = context.getSharedPreferences("rimu_clipboard_store", Context.MODE_PRIVATE)

    private val _items = MutableStateFlow<List<ClipboardItem>>(loadClips())
    val items: StateFlow<List<ClipboardItem>> = _items.asStateFlow()

    init {
        // Sync system clipboard on init
        checkSystemClipboard()
    }

    fun checkSystemClipboard() {
        clipboard?.primaryClip?.let { clip ->
            if (clip.itemCount > 0) {
                val text = clip.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
                if (!text.isNullOrEmpty()) {
                    addClip(text)
                }
            }
        }
    }

    fun addClip(text: String) {
        val current = _items.value.toMutableList()
        // Deduplicate
        current.removeAll { it.text == text && !it.isPinned }
        current.add(0, ClipboardItem(System.currentTimeMillis(), text, System.currentTimeMillis()))
        // Keep up to 25 items
        val pruned = current.take(25)
        _items.value = pruned
        saveClips(pruned)
    }

    fun copyToSystemClipboard(text: String) {
        clipboard?.setPrimaryClip(ClipData.newPlainText("Rimu Keyboard", text))
        addClip(text)
    }

    fun deleteClip(id: Long) {
        val updated = _items.value.filterNot { it.id == id }
        _items.value = updated
        saveClips(updated)
    }

    fun togglePin(id: Long) {
        val updated = _items.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
        _items.value = updated
        saveClips(updated)
    }

    fun clearAll() {
        val unpinnedCleared = _items.value.filter { it.isPinned }
        _items.value = unpinnedCleared
        saveClips(unpinnedCleared)
    }

    private fun loadClips(): List<ClipboardItem> {
        val serialized = prefs.getString("clips_data", "") ?: ""
        if (serialized.isEmpty()) {
            return listOf(
                ClipboardItem(1, "Welcome to Rimu Keyboard! ✨", System.currentTimeMillis(), true),
                ClipboardItem(2, "RGB gaming aesthetic with Bengali & English typing.", System.currentTimeMillis(), false)
            )
        }
        return try {
            serialized.split("\u001F").mapNotNull { entry ->
                val parts = entry.split("\u001E")
                if (parts.size >= 4) {
                    ClipboardItem(
                        id = parts[0].toLongOrNull() ?: 0L,
                        text = parts[1],
                        timestamp = parts[2].toLongOrNull() ?: 0L,
                        isPinned = parts[3].toBoolean()
                    )
                } else null
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveClips(list: List<ClipboardItem>) {
        val serialized = list.joinToString("\u001F") {
            "${it.id}\u001E${it.text.replace("\u001E", " ").replace("\u001F", " ")}\u001E${it.timestamp}\u001E${it.isPinned}"
        }
        prefs.edit().putString("clips_data", serialized).apply()
    }

    companion object {
        @Volatile
        private var instance: ClipboardManagerHelper? = null

        fun getInstance(context: Context): ClipboardManagerHelper {
            return instance ?: synchronized(this) {
                instance ?: ClipboardManagerHelper(context.applicationContext).also { instance = it }
            }
        }
    }
}
