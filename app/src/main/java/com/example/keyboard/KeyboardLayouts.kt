package com.example.keyboard

object KeyboardLayouts {

    val NUMBER_ROW = listOf(
        KeyModel("1", "!"),
        KeyModel("2", "@"),
        KeyModel("3", "#"),
        KeyModel("4", "$"),
        KeyModel("5", "%"),
        KeyModel("6", "^"),
        KeyModel("7", "&"),
        KeyModel("8", "*"),
        KeyModel("9", "("),
        KeyModel("0", ")", altChars = listOf("º", "°", "⓪"))
    )

    val BANGLA_NUMBER_ROW = listOf(
        KeyModel("১", "1"),
        KeyModel("২", "2"),
        KeyModel("৩", "3"),
        KeyModel("৪", "4"),
        KeyModel("৫", "5"),
        KeyModel("৬", "6"),
        KeyModel("৭", "7"),
        KeyModel("৮", "8"),
        KeyModel("৯", "9"),
        KeyModel("০", "0")
    )

    fun getEnglishRows(shiftState: ShiftState, showNumberRow: Boolean, enterActionLabel: String = "↵"): List<List<KeyModel>> {
        val rows = mutableListOf<List<KeyModel>>()
        if (showNumberRow) {
            rows.add(NUMBER_ROW)
        }

        val isUpper = shiftState != ShiftState.OFF

        fun charKey(lower: String, upper: String, alts: List<String> = emptyList()) =
            KeyModel(
                primaryText = if (isUpper) upper else lower,
                secondaryText = null,
                type = KeyType.CHARACTER,
                altChars = alts
            )

        // Row 1
        rows.add(
            listOf(
                charKey("q", "Q"),
                charKey("w", "W"),
                charKey("e", "E", listOf("é", "è", "ê", "ë", "ē")),
                charKey("r", "R"),
                charKey("t", "T"),
                charKey("y", "Y"),
                charKey("u", "U", listOf("ú", "ù", "û", "ü", "ū")),
                charKey("i", "I", listOf("í", "ì", "î", "ï", "ī")),
                charKey("o", "O", listOf("ó", "ò", "ô", "ö", "õ", "œ")),
                charKey("p", "P")
            )
        )

        // Row 2
        rows.add(
            listOf(
                charKey("a", "A", listOf("á", "à", "â", "ä", "ã", "å", "æ")),
                charKey("s", "S", listOf("ß", "$")),
                charKey("d", "D"),
                charKey("f", "F"),
                charKey("g", "G"),
                charKey("h", "H"),
                charKey("j", "J"),
                charKey("k", "K"),
                charKey("l", "L")
            )
        )

        // Row 3
        rows.add(
            listOf(
                KeyModel(
                    primaryText = if (shiftState == ShiftState.CAPS_LOCK) "⇪" else "⇧",
                    type = KeyType.SHIFT,
                    weight = 1.4f
                ),
                charKey("z", "Z"),
                charKey("x", "X"),
                charKey("c", "C", listOf("ç")),
                charKey("v", "V"),
                charKey("b", "B"),
                charKey("n", "N", listOf("ñ")),
                charKey("m", "M"),
                KeyModel(primaryText = "⌫", type = KeyType.BACKSPACE, weight = 1.4f)
            )
        )

        // Row 4 (Bottom Bar)
        rows.add(
            listOf(
                KeyModel("?123", type = KeyType.MODE_SWITCH, weight = 1.25f),
                KeyModel("BN", type = KeyType.LANG_SWITCH, weight = 0.95f),
                KeyModel("😀", type = KeyType.EMOJI, weight = 0.95f),
                KeyModel("Rimu RGB", type = KeyType.SPACE, weight = 4.2f),
                KeyModel(".", type = KeyType.CHARACTER, weight = 0.95f, altChars = listOf(",", "?", "!", ";", ":")),
                KeyModel(enterActionLabel, type = KeyType.ENTER, weight = 1.4f)
            )
        )

        return rows
    }

    fun getBanglaRows(shiftState: ShiftState, showNumberRow: Boolean, enterActionLabel: String = "↵"): List<List<KeyModel>> {
        val rows = mutableListOf<List<KeyModel>>()
        if (showNumberRow) {
            rows.add(BANGLA_NUMBER_ROW)
        }

        val isShifted = shiftState != ShiftState.OFF

        if (!isShifted) {
            // Base Bengali Layout (Consonants & common characters)
            rows.add(
                listOf(
                    KeyModel("ক"), KeyModel("খ"), KeyModel("গ"), KeyModel("ঘ"), KeyModel("ঙ"),
                    KeyModel("চ"), KeyModel("ছ"), KeyModel("জ"), KeyModel("ঝ"), KeyModel("ঞ")
                )
            )
            rows.add(
                listOf(
                    KeyModel("ট"), KeyModel("ঠ"), KeyModel("ড"), KeyModel("ঢ"), KeyModel("ণ"),
                    KeyModel("ত"), KeyModel("থ"), KeyModel("দ"), KeyModel("ধ"), KeyModel("ন")
                )
            )
            rows.add(
                listOf(
                    KeyModel("⇧", type = KeyType.SHIFT, weight = 1.3f),
                    KeyModel("প"), KeyModel("ফ"), KeyModel("ব"), KeyModel("ভ"), KeyModel("ম"),
                    KeyModel("য"), KeyModel("র"), KeyModel("ল"), KeyModel("শ"),
                    KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.3f)
                )
            )
        } else {
            // Shifted Bengali Layout (Vowels অ-ঔ, Kars া-ৌ, and additional characters)
            rows.add(
                listOf(
                    KeyModel("অ"), KeyModel("আ"), KeyModel("ই"), KeyModel("ঈ"), KeyModel("উ"),
                    KeyModel("ঊ"), KeyModel("ঋ"), KeyModel("এ"), KeyModel("ঐ"), KeyModel("ও")
                )
            )
            rows.add(
                listOf(
                    KeyModel("া"), KeyModel("ি"), KeyModel("ী"), KeyModel("ু"), KeyModel("ূ"),
                    KeyModel("ৃ"), KeyModel("ে"), KeyModel("ৈ"), KeyModel("ো"), KeyModel("ৌ")
                )
            )
            rows.add(
                listOf(
                    KeyModel("⇧", type = KeyType.SHIFT, weight = 1.3f),
                    KeyModel("ষ"), KeyModel("স"), KeyModel("হ"), KeyModel("ড়"), KeyModel("ঢ়"),
                    KeyModel("য়"), KeyModel("ৎ"), KeyModel("ং"), KeyModel("ঃ"),
                    KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.3f)
                )
            )
        }

        // Bottom Row
        rows.add(
            listOf(
                KeyModel("?123", type = KeyType.MODE_SWITCH, weight = 1.25f),
                KeyModel("EN", type = KeyType.LANG_SWITCH, weight = 0.95f),
                KeyModel("😀", type = KeyType.EMOJI, weight = 0.95f),
                KeyModel("বাংলা", type = KeyType.SPACE, weight = 4.2f),
                KeyModel("।", type = KeyType.CHARACTER, weight = 0.95f, altChars = listOf("?", "!", ",", ".")),
                KeyModel(enterActionLabel, type = KeyType.ENTER, weight = 1.4f)
            )
        )

        return rows
    }

    fun getSymbols1Rows(enterActionLabel: String = "↵"): List<List<KeyModel>> {
        return listOf(
            listOf(
                KeyModel("1"), KeyModel("2"), KeyModel("3"), KeyModel("4"), KeyModel("5"),
                KeyModel("6"), KeyModel("7"), KeyModel("8"), KeyModel("9"), KeyModel("0")
            ),
            listOf(
                KeyModel("@"), KeyModel("#"), KeyModel("$", altChars = listOf("€", "£", "¥", "৳", "₹", "¢")),
                KeyModel("_"), KeyModel("&"), KeyModel("-"), KeyModel("+"),
                KeyModel("("), KeyModel(")"), KeyModel("/")
            ),
            listOf(
                KeyModel("=\\<", type = KeyType.MODE_SWITCH, weight = 1.35f),
                KeyModel("*"), KeyModel("\""), KeyModel("'"), KeyModel(":"),
                KeyModel(";"), KeyModel("!"), KeyModel("?"),
                KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.35f)
            ),
            listOf(
                KeyModel("ABC", type = KeyType.MODE_SWITCH, weight = 1.3f),
                KeyModel("📋", type = KeyType.CLIPBOARD, weight = 0.95f),
                KeyModel("😀", type = KeyType.EMOJI, weight = 0.95f),
                KeyModel("Rimu RGB", type = KeyType.SPACE, weight = 4.2f),
                KeyModel(",", type = KeyType.CHARACTER, weight = 0.95f),
                KeyModel(enterActionLabel, type = KeyType.ENTER, weight = 1.4f)
            )
        )
    }

    fun getSymbols2Rows(enterActionLabel: String = "↵"): List<List<KeyModel>> {
        return listOf(
            listOf(
                KeyModel("~"), KeyModel("`"), KeyModel("|"), KeyModel("•"), KeyModel("√"),
                KeyModel("π"), KeyModel("÷"), KeyModel("×"), KeyModel("¶"), KeyModel("∆")
            ),
            listOf(
                KeyModel("£"), KeyModel("€"), KeyModel("¥"), KeyModel("$"), KeyModel("¢"),
                KeyModel("^"), KeyModel("°"), KeyModel("="), KeyModel("{"), KeyModel("}")
            ),
            listOf(
                KeyModel("?123", type = KeyType.MODE_SWITCH, weight = 1.35f),
                KeyModel("\\"), KeyModel("%"), KeyModel("©"), KeyModel("®"),
                KeyModel("™"), KeyModel("["), KeyModel("]"),
                KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.35f)
            ),
            listOf(
                KeyModel("ABC", type = KeyType.MODE_SWITCH, weight = 1.3f),
                KeyModel("<"), KeyModel(">"),
                KeyModel("Rimu RGB", type = KeyType.SPACE, weight = 4.2f),
                KeyModel("§"),
                KeyModel(enterActionLabel, type = KeyType.ENTER, weight = 1.4f)
            )
        )
    }

    val EMOJI_COLLECTIONS = mapOf(
        EmojiCategory.SMILEYS to listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
            "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
            "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔",
            "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥",
            "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮",
            "🤧", "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "😎", "🤓",
            "🧐", "😕", "😟", "🙁", "😮", "😯", "😲", "😳", "🥺", "😦",
            "😧", "😨", "😰", "😥", "😢", "😭", "😱", "😖", "😣", "😞",
            "😓", "😩", "😫", "🥱", "😤", "😡", "😠", "🤬", "😈", "👿",
            "💀", "☠️", "💩", "🤡", "👹", "👺", "👻", "👽", "👾", "🤖"
        ),
        EmojiCategory.GESTURES to listOf(
            "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞",
            "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "👍",
            "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝",
            "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂",
            "🦻", "👃", "🧠", "👀", "👁️", "👅", "👄", "💋", "❤️", "🔥"
        ),
        EmojiCategory.ANIMALS to listOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
            "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦆",
            "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋",
            "🐌", "🐞", "🐜", "🕷️", "🦂", "🐢", "🐍", "🦎", "🐙", "🦑",
            "🦐", "🦀", "🐡", "🐠", "🐟", "🐬", "🐳", "🦈", "🐊", "🐅"
        ),
        EmojiCategory.FOOD to listOf(
            "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
            "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🥑", "🥦",
            "🌶️", "🌽", "🥕", "🥔", "🥐", "🥯", "🍞", "🧀", "🥚", "🍳",
            "🥞", "🥓", "🍗", "🥩", "🍔", "🍟", "🍕", "🥪", "🌮", "🌯",
            "🥗", "🥘", "🍝", "🍜", "🍲", "🍛", "🍣", "🍱", "🥟", "🍦",
            "🎂", "🍰", "🧁", "🍩", "🍪", "🍫", "🍿", "☕", "🧃", "🍺"
        ),
        EmojiCategory.ACTIVITIES to listOf(
            "🎮", "🕹️", "🎲", "♠️", "♥️", "♦️", "♣️", "♟️", "🎯", "🎳",
            "🎧", "🎤", "🎬", "🎨", "🎪", "🎭", "🏆", "🥇", "🥈", "🥉",
            "⚽", "🏀", "🏈", "⚾", "🎾", "🏐", "🥏", "🎱", "🏓", "🏸",
            "🛹", "🛼", "🏹", "🎣", "🥊", "🥋", "🚗", "🚀", "🛸", "🏍️"
        ),
        EmojiCategory.OBJECTS to listOf(
            "📱", "📲", "💻", "⌨️", "🖥️", "🖨️", "🖱️", "🕹️", "💽", "💾",
            "📷", "📸", "📹", "🎥", "📞", "☎️", "📺", "📻", "🎙️", "🧭",
            "⏱️", "⏰", "⏳", "📡", "🔋", "🔌", "💡", "🔦", "🕯️", "💸",
            "💵", "💰", "💳", "💎", "⚖️", "🧰", "🔧", "🔨", "⚙️", "🔑"
        ),
        EmojiCategory.SYMBOLS to listOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
            "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "✨", "⭐",
            "🌟", "⚡", "💥", "🔥", "🌈", "☀️", "🌙", "☁️", "❄️", "🎵",
            "🎶", "💯", "✅", "❌", "⚠️", "⛔", "🚫", "💤", "💬", "🇧🇩"
        )
    )
}
