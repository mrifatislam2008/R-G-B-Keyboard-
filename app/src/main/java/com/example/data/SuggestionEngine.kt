package com.example.data

object SuggestionEngine {

    private val ENGLISH_WORDS = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
        "keyboard", "gaming", "neon", "speed", "light", "color", "android", "smooth", "custom", "effect",
        "awesome", "beautiful", "super", "quick", "best", "great", "thank", "thanks", "hello", "welcome"
    )

    private val BANGLA_WORDS = listOf(
        "আমি", "তুমি", "আমরা", "তিনি", "তারা", "আপনি", "কি", "কেন", "কোথায়", "কখন",
        "ভালো", "কেমন", "আছো", "আছেন", "বাংলাদেশ", "বাংলা", "ধন্যবাদ", "স্বাগতম", "হ্যাঁ", "না",
        "অনেক", "সুন্দর", "খুব", "আজ", "কাল", "এখন", "সময়", "কাজ", "বন্ধু", "ভালোবাসা",
        "মানুষ", "দেশ", "কথা", "জীবন", "মন", "দিন", "রাত্রি", "গান", "বই", "কীবোর্ড",
        "রিমু", "খবর", "ঠিক", "সবাই", "নতুন", "সকাল", "সন্ধ্যা", "পথ", "জল", "আলো"
    )

    private val EMOJI_MAP = mapOf(
        "smile" to "😊",
        "happy" to "😄",
        "love" to "❤️",
        "heart" to "💖",
        "cool" to "😎",
        "fire" to "🔥",
        "game" to "🎮",
        "good" to "👍",
        "check" to "✅",
        "star" to "⭐",
        "sun" to "☀️",
        "moon" to "🌙",
        "cat" to "🐱",
        "dog" to "🐶",
        "coffee" to "☕",
        "party" to "🎉",
        "sad" to "😢",
        "wow" to "😮",
        "laugh" to "😂",
        "halo" to "😇",
        "bangla" to "🇧🇩",
        "rimu" to "⚡"
    )

    fun getSuggestions(prefix: String, language: String): List<String> {
        val clean = prefix.trim().lowercase()
        if (clean.isEmpty()) {
            return if (language == "BN") {
                listOf("ধন্যবাদ", "কেমন আছো?", "ভালো", "হ্যাঁ")
            } else {
                listOf("the", "I", "and", "thanks")
            }
        }

        val dictionary = if (language == "BN") BANGLA_WORDS else ENGLISH_WORDS
        val matches = dictionary.filter { it.lowercase().startsWith(clean) }
            .take(3)
            .toMutableList()

        // Check if there is an emoji match
        EMOJI_MAP[clean]?.let { emoji ->
            if (!matches.contains(emoji)) {
                matches.add(0, emoji)
            }
        }

        // If no exact matches, provide fallback or uppercase version
        if (matches.isEmpty()) {
            if (prefix.isNotEmpty()) {
                matches.add(prefix)
            }
        }

        return matches.take(3)
    }
}
