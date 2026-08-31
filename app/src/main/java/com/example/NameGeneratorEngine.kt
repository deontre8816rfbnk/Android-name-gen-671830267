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

    // Base building blocks
    private val prefixes = listOf(
        "Ael", "Aer", "Al", "An", "Ar", "Ash", "Bel", "Bri", "Cael", "Cal", "Cel", "Cor", "Dael", "Dar", "Del",
        "Eld", "El", "Ely", "Fael", "Fen", "Gal", "Gwen", "Hal", "Ith", "Kael", "Kel", "Lor", "Lyn", "Mal",
        "Mir", "Mor", "Nal", "Ner", "Or", "Quel", "Rav", "Rey", "Sar", "Sel", "Syl", "Thal", "Thor", "Val",
        "Vel", "Vor", "Wyn", "Xan", "Yel", "Zan", "Zha", "Bri", "Dre", "Fen", "Lor", "Nyx", "Orr", "Quin",
        "Riven", "Sor", "Tor", "Ull", "Vex", "Wren", "Yara", "Zor"
    )

    private val middles = listOf(
        "an", "ar", "en", "er", "ia", "iel", "in", "ion", "ir", "is", "ith", "or", "oth", "ul", "un", "ur",
        "yn", "ys", "ae", "ei", "ai", "oa", "ua", "dra", "drae", "lith", "mir", "nor", "riel", "ther", "vyn",
        "lle", "au", "ghe", "llighe", "vlle", "ndor", "thir", "quen", "vynne", "rith", "sira", "lora",
        "keth", "miri", "thal", "vorn", "zel", "rian", "dor", "fen", "gar", "hel", "jor", "kal"
    )

    private val suffixes = listOf(
        "a", "ae", "an", "ar", "ath", "en", "er", "ia", "iel", "in", "ion", "is", "ith", "or", "os", "oth",
        "ul", "um", "us", "yn", "ys", "ara", "aria", "elle", "ora", "oria", "wyn", "eth", "iel", "ith",
        "wood", "haven", "mere", "fell", "ridge", "vale", "moor", "crest", "spire", "reach", "hold",
        "heim", "stadt", "burg", "dorf", "land", "stein", "berg", "wald"
    )

    // Cultural / style specific pieces
    private val germanParts = listOf("berg", "stein", "wald", "heim", "stadt", "burg", "dorf", "land", "hof", "bach", "tal", "feld", "brück", "hausen", "ringen")
    private val spanishParts = listOf("ía", "éz", "án", "ón", "illo", "ito", "eño", "oso", "ado", "iente", "ario", "uelo", "anza", "esco")
    private val britishParts = listOf("shire", "ham", "ton", "ford", "bury", "chester", "field", "wood", "worth", "ley", "by", "wick", "stead", "mont")
    private val americanParts = listOf("ville", "ton", "burg", "field", "wood", "creek", "ridge", "view", "land", "port", "side", "hill", "grove")
    private val arabicParts = listOf("al-", "ibn-", "bin-", "abd-", "nur", "din", "ullah", "karim", "rashid", "saif", "zayn", "farid", "hakim", "jalal")
    private val turkishParts = listOf("ğlu", "lar", "ler", "lik", "cı", "çi", "taş", "kaya", "yıldız", "demir", "çınar", "güneş", "aydin", "öz")
    private val japaneseParts = listOf("shi", "to", "ka", "mi", "yo", "ra", "tsu", "no", "ri", "sa", "ki", "na", "yu", "ha", "ma", "ko", "aya", "sora")
    private val chineseParts = listOf("li", "wei", "ming", "hua", "xin", "yu", "chen", "yang", "feng", "ling", "hao", "jun", "tao", "yan", "qi", "zhe")

    private val specialChars = listOf("à", "á", "â", "ä", "å", "ă", "è", "é", "ê", "ë", "ì", "í", "î", "ï", "ò", "ó", "ô", "ö", "ù", "ú", "û", "ü", "ý", "ÿ", "ñ", "ç", "ş", "ğ", "ı")

    private val longPrefixes = listOf(
        "New", "Old", "Great", "High", "Deep", "Far", "North", "South", "East", "West", "Upper", "Lower",
        "Ancient", "Hidden", "Silent", "Golden", "Silver", "Iron", "Shadow", "Crystal"
    )

    // Track used names to avoid repeats in the same session
    private val usedNames = mutableSetOf<String>()

    private fun <T> List<T>.randomItem(): T = this[Random.nextInt(size)]

    private fun maybeAddSpecial(base: String): String {
        if (Random.nextFloat() > 0.35f) return base
        val char = specialChars.randomItem()
        val pos = Random.nextInt(1, maxOf(2, base.length))
        return base.take(pos) + char + base.drop(pos)
    }

    private fun generateBaseName(): String {
        val styleRoll = Random.nextDouble()
        return when {
            styleRoll < 0.30 -> prefixes.randomItem() + middles.randomItem() + suffixes.randomItem()
            styleRoll < 0.50 -> prefixes.randomItem() + middles.randomItem() + middles.randomItem().take(3) + suffixes.randomItem()
            styleRoll < 0.70 -> middles.randomItem().replaceFirstChar { it.uppercase() } + middles.randomItem() + suffixes.randomItem()
            styleRoll < 0.85 -> prefixes.randomItem() + suffixes.randomItem()
            else -> {
                val p1 = prefixes.randomItem()
                val m1 = middles.randomItem()
                val m2 = middles.randomItem().take(Random.nextInt(2, 5))
                val s1 = suffixes.randomItem()
                p1 + m1 + m2 + s1
            }
        }.let { maybeAddSpecial(it) }
    }

    private fun applyCulturalStyle(base: String, style: NameStyle): String {
        return when (style) {
            NameStyle.GERMAN -> {
                val part = germanParts.randomItem()
                if (Random.nextBoolean()) base + part else part.replaceFirstChar { it.uppercase() } + base.lowercase()
            }
            NameStyle.SPANISH -> {
                val part = spanishParts.randomItem()
                base.dropLastWhile { !it.isLetter() } + part
            }
            NameStyle.BRITISH -> {
                val part = britishParts.randomItem()
                base + part
            }
            NameStyle.AMERICAN -> {
                val part = americanParts.randomItem()
                base + part
            }
            NameStyle.ARABIC -> {
                val part = arabicParts.randomItem()
                if (part.endsWith("-")) part + base.lowercase() else base + part
            }
            NameStyle.TURKISH -> {
                val part = turkishParts.randomItem()
                base + part
            }
            NameStyle.JAPANESE -> {
                val parts = (1..Random.nextInt(2, 4)).map { japaneseParts.randomItem() }
                parts.joinToString("") { it }.replaceFirstChar { it.uppercase() }
            }
            NameStyle.CHINESE -> {
                val parts = (1..Random.nextInt(2, 3)).map { chineseParts.randomItem() }
                parts.joinToString("") { it }.replaceFirstChar { it.uppercase() }
            }
            else -> base
        }
    }

    fun generateUniqueName(style: NameStyle = NameStyle.NORMAL): String {
        var attempts = 0
        var name: String
        do {
            val base = generateBaseName()
            name = when (style) {
                NameStyle.NORMAL, NameStyle.TWO_PART, NameStyle.SHORT, NameStyle.LONG -> base
                else -> applyCulturalStyle(base, style)
            }
            // Clean up and capitalize properly
            name = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            attempts++
        } while (name in usedNames && attempts < 40)

        usedNames.add(name)
        // Keep memory reasonable
        if (usedNames.size > 500) {
            usedNames.clear()
        }
        return name
    }

    fun generateName(type: NameStyle): String {
        return when (type) {
            NameStyle.TWO_PART -> {
                "${generateUniqueName(NameStyle.NORMAL)} ${generateUniqueName(NameStyle.NORMAL)}"
            }
            NameStyle.SHORT -> {
                val full = generateUniqueName(NameStyle.NORMAL)
                if (full.length > 7) full.take(7) else full
            }
            NameStyle.LONG -> {
                val extra = if (Random.nextDouble() > 0.55) " ${generateUniqueName(NameStyle.NORMAL)}" else ""
                "${longPrefixes.randomItem()} ${generateUniqueName(NameStyle.NORMAL)}$extra"
            }
            else -> generateUniqueName(type)
        }
    }

    fun generateBatch(type: NameStyle, count: Int = 4): List<String> {
        return List(count) { generateName(type) }
    }

    fun clearUsedNames() {
        usedNames.clear()
    }
}
