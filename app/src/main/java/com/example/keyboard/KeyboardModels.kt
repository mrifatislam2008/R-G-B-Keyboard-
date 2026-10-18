package com.example.keyboard

enum class KeyType {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    ENTER,
    SPACE,
    MODE_SWITCH,
    LANG_SWITCH,
    EMOJI,
    CLIPBOARD,
    MIC,
    SETTINGS,
    SWITCH_IME
}

data class KeyModel(
    val primaryText: String,
    val secondaryText: String? = null,
    val type: KeyType = KeyType.CHARACTER,
    val weight: Float = 1.0f,
    val altChars: List<String> = emptyList()
)

enum class ShiftState {
    OFF,
    ONCE,
    CAPS_LOCK
}

enum class KeyboardPage {
    LETTERS,
    SYMBOLS_1,
    SYMBOLS_2,
    EMOJIS,
    CLIPBOARD
}

enum class EmojiCategory(val icon: String, val title: String) {
    SMILEYS("😀", "Smileys"),
    GESTURES("👍", "Gestures"),
    ANIMALS("🐱", "Animals"),
    FOOD("🍕", "Food"),
    ACTIVITIES("🎮", "Gaming"),
    OBJECTS("💡", "Objects"),
    SYMBOLS("❤️", "Symbols")
}
