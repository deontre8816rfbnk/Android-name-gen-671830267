package com.example

import kotlin.random.Random

enum class NameStyle(val displayName: String, val key: String) {
    NORMAL("Normal", "normal"),
    TWO_PART("Two-Part", "two-part"),
    SHORT("Short", "short"),
    LONG("Long", "long"),
    GERMAN("German", "german"),
    SPANISH("Spanish", "spanish"),
    BRITISH("British", "british"),
    AMERICAN("American", "american"),
    ARABIC("Arabic", "arabic"),
    TURKISH("Turkish", "turkish"),
    JAPANESE("Japanese", "japanese"),
    CHINESE("Chinese", "chinese")
}

object NameGeneratorEngine {

    // === CORE BUILDING BLOCKS ===
    private val prefixes = mutableListOf(
        "Ael", "Aer", "Al", "An", "Ar", "Ash", "Bel", "Bri", "Cael", "Cal", "Cel", "Cor", "Dael", "Dar", "Del",
        "Eld", "El", "Ely", "Fael", "Fen", "Gal", "Gwen", "Hal", "Ith", "Kael", "Kel", "Lor", "Lyn", "Mal",
        "Mir", "Mor", "Nal", "Ner", "Or", "Quel", "Rav", "Rey", "Sar", "Sel", "Syl", "Thal", "Thor", "Val",
        "Vel", "Vor", "Wyn", "Xan", "Yel", "Zan", "Zha", "Dre", "Nyx", "Orr", "Quin", "Riven", "Sor", "Tor",
        "Ull", "Vex", "Wren", "Yara", "Zor", "Briar", "Cyr", "Dusk", "Ember", "Frost", "Grim", "Hollow",
        "Ivor", "Jor", "Keth", "Lir", "Morr", "Nox", "Onyx", "Pyre", "Rook", "Shade", "Thorn", "Umber"
    )

    private val middles = mutableListOf(
        "an", "ar", "en", "er", "ia", "iel", "in", "ion", "ir", "is", "ith", "or", "oth", "ul", "un", "ur",
        "yn", "ys", "ae", "ei", "ai", "oa", "ua", "dra", "drae", "lith", "mir", "nor", "riel", "ther", "vyn",
        "lle", "au", "ghe", "llighe", "vlle", "ndor", "thir", "quen", "vynne", "rith", "sira", "lora",
        "keth", "miri", "thal", "vorn", "zel", "rian", "dor", "fen", "gar", "hel", "jor", "kal",
        "llae", "ghul", "vryn", "skarn", "drak", "morn", "thel", "vash", "kor", "ryn", "syl", "vor",
        "ae", "oa", "ua", "ie", "ue", "yri", "ora", "ira", "ula", "ena", "ara", "yna"
    )

    private val suffixes = mutableListOf(
        "a", "ae", "an", "ar", "ath", "en", "er", "ia", "iel", "in", "ion", "is", "ith", "or", "os", "oth",
        "ul", "um", "us", "yn", "ys", "ara", "aria", "elle", "ora", "oria", "wyn", "eth", "iel",
        "wood", "haven", "mere", "fell", "ridge", "vale", "moor", "crest", "spire", "reach", "hold",
        "heim", "stadt", "burg", "dorf", "land", "stein", "berg", "wald", "ford", "ham", "ton", "bury"
    )

    // Cultural specific
    private val germanParts = listOf("berg", "stein", "wald", "heim", "stadt", "burg", "dorf", "land", "hof", "bach", "tal", "feld", "brück", "hausen", "ringen", "fels", "see")
    private val spanishParts = listOf("ía", "éz", "án", "ón", "illo", "ito", "eño", "oso", "ado", "iente", "ario", "uelo", "anza", "esco", "uelo", "ín")
    private val britishParts = listOf("shire", "ham", "ton", "ford", "bury", "chester", "field", "wood", "worth", "ley", "by", "wick", "stead", "mont", "brook", "well")
    private val americanParts = listOf("ville", "ton", "burg", "field", "wood", "creek", "ridge", "view", "land", "port", "side", "hill", "grove", "falls", "springs")
    private val arabicParts = listOf("al-", "ibn-", "bin-", "abd-", "nur", "din", "ullah", "karim", "rashid", "saif", "zayn", "farid", "hakim", "jalal", "noor", "amir")
    private val turkishParts = listOf("ğlu", "lar", "ler", "lik", "cı", "çi", "taş", "kaya", "yıldız", "demir", "çınar", "güneş", "aydın", "öz", "yılmaz", "kaya")
    private val japaneseParts = listOf("shi", "to", "ka", "mi", "yo", "ra", "tsu", "no", "ri", "sa", "ki", "na", "yu", "ha", "ma", "ko", "aya", "sora", "haru", "yuki")
    private val chineseParts = listOf("li", "wei", "ming", "hua", "xin", "yu", "chen", "yang", "feng", "ling", "hao", "jun", "tao", "yan", "qi", "zhe", "xuan", "lei")

    private val specialChars = listOf(
        "à", "á", "â", "ä", "å", "ă", "è", "é", "ê", "ë", "ì", "í", "î", "ï",
        "ò", "ó", "ô", "ö", "ù", "ú", "û", "ü", "ý", "ÿ", "ñ", "ç", "ş", "ğ", "ı", "ț", "ș"
    )

    private val longPrefixes = listOf(
        "New", "Old", "Great", "High", "Deep", "Far", "North", "South", "East", "West",
        "Upper", "Lower", "Ancient", "Hidden", "Silent", "Golden", "Silver", "Iron",
        "Shadow", "Crystal", "Eternal", "Forgotten", "Sacred", "Wild", "Pale", "Dark"
    )

    // User-provided custom letter combinations (can be added at runtime)
    private val customCombinations = mutableListOf<String>()

    // Track used names to strongly avoid repeats
    private val usedNames = mutableSetOf<String>()

    private fun <T> List<T>.randomItem(): T = this[Random.nextInt(size)]

    // === ADVANCED GENERATION CORE ===

    private fun injectSpecialChars(input: String): String {
        if (input.length < 3 || Random.nextFloat() > 0.42f) return input
        val result = StringBuilder(input)
        val positions = (1 until input.length - 1).shuffled().take(Random.nextInt(1, 3))
        for (pos in positions) {
            if (result[pos].isLetter()) {
                result.setCharAt(pos, specialChars.randomItem()[0])
            }
        }
        return result.toString()
    }

    private fun buildFromParts(): String {
        val useCustom = customCombinations.isNotEmpty() && Random.nextFloat() < 0.45f

        return when {
            useCustom -> {
                val custom = customCombinations.randomItem()
                val prefix = prefixes.randomItem()
                val suffix = suffixes.randomItem()
                when (Random.nextInt(4)) {
                    0 -> custom.replaceFirstChar { it.uppercase() } + suffix
                    1 -> prefix + custom
                    2 -> prefix + custom + suffix
                    else -> custom.replaceFirstChar { it.uppercase() } + middles.randomItem() + suffix
                }
            }
            Random.nextFloat() < 0.25f -> {
                // Longer experimental names
                val p = prefixes.randomItem()
                val m1 = middles.randomItem()
                val m2 = middles.randomItem()
                val m3 = if (Random.nextBoolean()) middles.randomItem().take(Random.nextInt(2, 4)) else ""
                val s = suffixes.randomItem()
                p + m1 + m2 + m3 + s
            }
            Random.nextFloat() < 0.55f -> {
                prefixes.randomItem() + middles.randomItem() + suffixes.randomItem()
            }
            else -> {
                val m = middles.randomItem()
                m.replaceFirstChar { it.uppercase() } + middles.randomItem() + suffixes.randomItem()
            }
        }
    }

    private fun applyCulturalStyle(base: String, style: NameStyle): String {
        return when (style) {
            NameStyle.GERMAN -> {
                val part = germanParts.randomItem()
                if (Random.nextBoolean()) base + part else part.replaceFirstChar { it.uppercase() } + base.lowercase()
            }
            NameStyle.SPANISH -> base.dropLastWhile { !it.isLetter() } + spanishParts.randomItem()
            NameStyle.BRITISH -> base + britishParts.randomItem()
            NameStyle.AMERICAN -> base + americanParts.randomItem()
            NameStyle.ARABIC -> {
                val part = arabicParts.randomItem()
                if (part.endsWith("-")) part + base.lowercase() else base + part
            }
            NameStyle.TURKISH -> base + turkishParts.randomItem()
            NameStyle.JAPANESE -> {
                (1..Random.nextInt(2, 4))
                    .map { japaneseParts.randomItem() }
                    .joinToString("")
                    .replaceFirstChar { it.uppercase() }
            }
            NameStyle.CHINESE -> {
                (1..Random.nextInt(2, 3))
                    .map { chineseParts.randomItem() }
                    .joinToString("")
                    .replaceFirstChar { it.uppercase() }
            }
            else -> base
        }
    }

    fun generateUniqueName(style: NameStyle = NameStyle.NORMAL): String {
        var attempts = 0
        var candidate: String

        do {
            var base = buildFromParts()
            base = injectSpecialChars(base)

            candidate = when (style) {
                NameStyle.NORMAL, NameStyle.TWO_PART, NameStyle.SHORT, NameStyle.LONG -> base
                else -> applyCulturalStyle(base, style)
            }

            // Normalize
            candidate = candidate
                .replace(Regex("\\s+"), "")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            attempts++
        } while ((candidate in usedNames || candidate.length < 3) && attempts < 60)

        usedNames.add(candidate)

        // Prevent memory growth
        if (usedNames.size > 800) {
            val toRemove = usedNames.take(300)
            usedNames.removeAll(toRemove.toSet())
        }

        return candidate
    }

    fun generateName(type: NameStyle): String {
        return when (type) {
            NameStyle.TWO_PART -> "${generateUniqueName(NameStyle.NORMAL)} ${generateUniqueName(NameStyle.NORMAL)}"
            NameStyle.SHORT -> {
                val full = generateUniqueName(NameStyle.NORMAL)
                if (full.length > 8) full.take(Random.nextInt(5, 9)) else full
            }
            NameStyle.LONG -> {
                val extra = if (Random.nextDouble() > 0.5) " ${generateUniqueName(NameStyle.NORMAL)}" else ""
                "${longPrefixes.randomItem()} ${generateUniqueName(NameStyle.NORMAL)}$extra"
            }
            else -> generateUniqueName(type)
        }
    }

    fun generateBatch(type: NameStyle, count: Int = 4): List<String> {
        return List(count) { generateName(type) }
    }

    // === CUSTOM COMBINATIONS API ===
    fun addCustomCombination(combo: String) {
        val cleaned = combo.trim().lowercase()
        if (cleaned.length in 2..12 && cleaned !in customCombinations) {
            customCombinations.add(cleaned)
            // Also inject into middles for broader use
            if (cleaned !in middles) {
                middles.add(cleaned)
            }
        }
    }

    fun addCustomCombinations(combos: List<String>) {
        combos.forEach { addCustomCombination(it) }
    }

    fun getCustomCombinations(): List<String> = customCombinations.toList()

    fun clearCustomCombinations() {
        customCombinations.clear()
    }

    fun clearUsedNames() {
        usedNames.clear()
    }
}
