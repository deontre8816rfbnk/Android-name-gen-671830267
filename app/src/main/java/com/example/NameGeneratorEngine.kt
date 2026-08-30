package com.example

import kotlin.random.Random

enum class NameStyle(val displayName: String, val key: String) {
    NORMAL("Normal", "normal"),
    TWO_PART("Two-Part", "two-part"),
    SHORT("Short", "short"),
    LONG("Long", "long")
}

object NameGeneratorEngine {
    val prefixes = listOf(
        "Ael", "Aer", "Al", "An", "Ar", "Ash", "Bel", "Bri", "Cael", "Cal", "Cel", "Cor", "Dael", "Dar", "Del",
        "Eld", "El", "Ely", "Fael", "Fen", "Gal", "Gwen", "Hal", "Ith", "Kael", "Kel", "Lor", "Lyn", "Mal",
        "Mir", "Mor", "Nal", "Ner", "Or", "Quel", "Rav", "Rey", "Sar", "Sel", "Syl", "Thal", "Thor", "Val",
        "Vel", "Vor", "Wyn", "Xan", "Yel", "Zan", "Zha"
    )

    val middles = listOf(
        "an", "ar", "en", "er", "ia", "iel", "in", "ion", "ir", "is", "ith", "or", "oth", "ul", "un", "ur",
        "yn", "ys", "ae", "ei", "ai", "oa", "ua", "dra", "drae", "lith", "mir", "nor", "riel", "ther", "vyn"
    )

    val suffixes = listOf(
        "a", "ae", "an", "ar", "ath", "en", "er", "ia", "iel", "in", "ion", "is", "ith", "or", "os", "oth",
        "ul", "um", "us", "yn", "ys", "ara", "aria", "elle", "ora", "oria", "wyn", "eth", "iel", "ith",
        "wood", "haven", "mere", "fell", "ridge", "vale", "moor", "crest", "spire", "reach", "hold"
    )

    val natureWords = listOf(
        "Ash", "Birch", "Cedar", "Elder", "Fern", "Frost", "Glen", "Hawk", "Iron", "Jade", "Lake", "Mist",
        "Oak", "Pine", "River", "Shadow", "Silver", "Stone", "Storm", "Thorn", "Willow", "Wolf", "Moon",
        "Star", "Sun", "Sky", "Cloud", "Rain", "Snow", "Wind", "Fire", "Ember", "Crystal", "Amber"
    )

    val darkWords = listOf(
        "Black", "Bleak", "Blood", "Bone", "Dark", "Dread", "Gloom", "Grim", "Hollow", "Night", "Raven",
        "Shade", "Sorrow", "Thorn", "Void", "Wraith", "Grave", "Crypt", "Doom", "Fell"
    )

    val elegantWords = listOf(
        "Aether", "Azure", "Celestia", "Elysia", "Lumina", "Seraph", "Solara", "Vespera", "Aurora",
        "Celene", "Elara", "Lyra", "Nova", "Orion", "Sable", "Silva", "Vera", "Zara"
    )

    val longPrefixes = listOf(
        "New", "Old", "Great", "High", "Deep", "Far", "North", "South", "East", "West", "Upper", "Lower"
    )

    private fun <T> List<T>.randomItem(): T = this[Random.nextInt(size)]

    fun generateUniqueName(): String {
        val style = Random.nextDouble()
        return when {
            style < 0.35 -> {
                prefixes.randomItem() + middles.randomItem() + suffixes.randomItem()
            }
            style < 0.55 -> {
                natureWords.randomItem() + suffixes.randomItem()
            }
            style < 0.70 -> {
                val darkEndings = listOf("a", "e", "ia", "is", "or", "yn")
                darkWords.randomItem() + middles.randomItem() + darkEndings.randomItem()
            }
            style < 0.85 -> {
                elegantWords.randomItem()
            }
            else -> {
                val middle1 = middles.randomItem()
                val middle2 = middles.randomItem()
                val middlePart = if (middle2.length >= 2) middle2.substring(0, 2) else middle2
                prefixes.randomItem() + middle1 + middlePart + suffixes.randomItem()
            }
        }
    }

    fun generateName(type: NameStyle): String {
        return when (type) {
            NameStyle.TWO_PART -> {
                "${generateUniqueName()} ${generateUniqueName()}"
            }
            NameStyle.SHORT -> {
                val short = generateUniqueName()
                if (short.length > 6) short.substring(0, 6) else short
            }
            NameStyle.LONG -> {
                val extraPart = if (Random.nextDouble() > 0.6) " ${generateUniqueName()}" else ""
                "${longPrefixes.randomItem()} ${generateUniqueName()}$extraPart"
            }
            NameStyle.NORMAL -> {
                generateUniqueName()
            }
        }
    }

    fun generateBatch(type: NameStyle, count: Int = 4): List<String> {
        return List(count) { generateName(type) }
    }
}
