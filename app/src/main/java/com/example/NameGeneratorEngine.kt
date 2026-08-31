package com.example

import kotlin.random.Random

enum class NameStyle(val displayName: String, val key: String) {
    NORMAL("Normal", "normal"),
    TWO_PART("Two-Part", "two-part"),
    SHORT("Short", "short"),
    LONG("Long", "long"),
    GERMAN("German", "german"),
    FRENCH("French", "french"), // Added French as requested
    SPANISH("Spanish", "spanish"),
    BRITISH("British", "british"),
    AMERICAN("American", "american"),
    ARABIC("Arabic", "arabic"),
    TURKISH("Turkish", "turkish"),
    JAPANESE("Japanese", "japanese"),
    CHINESE("Chinese", "chinese")
}

object NameGeneratorEngine {

    // === REAL-LIFE NAME BASES ===
    // Used as a foundation, then mutated to create new, natural-sounding names
    private val normalBases = listOf("Arthur", "Elara", "Roland", "Lyra", "Kael", "Rowan", "Nyx", "Sylas", "Elowen", "Gael", "Isolde", "Tristan", "Mira", "Dorian", "Vera", "Finn", "Luna", "Leo", "Maeve", "Silas")
    private val germanBases = listOf("Albert", "Friedrich", "Ludwig", "Heinrich", "Wilhelm", "Stefan", "Lukas", "Markus", "Jurgen", "Klaus", "Werner", "Felix")
    private val frenchBases = listOf("Louis", "Antoine", "Pierre", "Jacques", "Michel", "Laurent", "Pascal", "Dominique", "Claude", "Marc", "Paul", "Henri")
    private val spanishBases = listOf("Mateo", "Diego", "Carlos", "Javier", "Luis", "Pablo", "Miguel", "Rafael", "Fernando", "Alejandro", "Sergio")
    private val britishBases = listOf("Oliver", "George", "Harry", "Jack", "Jacob", "Charlie", "Thomas", "James", "William", "Henry")
    private val americanBases = listOf("James", "John", "Robert", "Michael", "David", "Richard", "Joseph", "Thomas", "Charles", "Daniel")
    private val arabicBases = listOf("Ahmed", "Omar", "Tariq", "Khalid", "Rami", "Sami", "Nasser", "Hadi", "Malik", "Zayd", "Faris", "Jamil", "Rashid", "Karim")
    private val turkishBases = listOf("Emir", "Yigit", "Baris", "Cihan", "Volkan", "Serdar", "Okan", "Deniz", "Berk", "Kerem", "Onur", "Mert")
    private val japaneseBases = listOf("Haruto", "Yuto", "Sota", "Yuki", "Hiroshi", "Kenji", "Riku", "Kaito", "Sora", "Rin", "Aoi", "Ren")
    private val chineseBases = listOf("Wei", "Feng", "Tao", "Jun", "Lei", "Hao", "Ming", "Chen", "Yu", "Lin", "Jian", "Hui")

    // Mutations to apply to real names to make them "weird/new" but valid
    private val mutationSuffixes = listOf("us", "ia", "iel", "is", "an", "or", "yx", "ax", "eth", "in", "os", "ar")

    // Vocalization maps for cultures that use them
    private val vocalizationMap = mapOf(
        'a' to listOf('á', 'à', 'â', 'ä'),
        'e' to listOf('é', 'è', 'ê', 'ë'),
        'i' to listOf('í', 'î', 'ï'),
        'o' to listOf('ó', 'ô', 'ö'),
        'u' to listOf('ú', 'û', 'ü'),
        'c' to listOf('ç'),
        's' to listOf('ş'),
        'g' to listOf('ğ')
    )
    
    // Cultures that are allowed to have vocalizations
    private val vocalizedCultures = listOf(NameStyle.FRENCH, NameStyle.GERMAN, NameStyle.SPANISH, NameStyle.TURKISH)

    private val longPrefixes = listOf(
        "New", "Old", "Great", "High", "Deep", "Far", "North", "South", "East", "West",
        "Upper", "Lower", "Ancient", "Hidden", "Silent", "Golden", "Silver", "Iron",
        "Shadow", "Crystal", "Eternal", "Forgotten", "Sacred", "Wild", "Pale", "Dark"
    )

    // User-provided custom letter combinations
    private val customCombinations = mutableListOf<String>()

    // Track used names to strongly avoid repeats
    private val usedNames = mutableSetOf<String>()

    private fun <T> List<T>.randomItem(): T = this[Random.nextInt(size)]

    // === GENERATION CORE ===

    private fun mutateBase(base: String): String {
        val sb = StringBuilder(base.lowercase())
        val mutationType = Random.nextInt(4)

        when (mutationType) {
            0 -> { // Swap a vowel
                val vowels = "aeiou"
                val indices = sb.indices.filter { sb[it] in vowels }
                if (indices.isNotEmpty()) {
                    val idx = indices.randomItem()
                    sb.setCharAt(idx, vowels.randomItem())
                }
            }
            1 -> { // Change ending to a fantasy suffix
                if (sb.length > 3) {
                    val cut = Random.nextInt(2, 4)
                    sb.delete(sb.length - cut, sb.length)
                    sb.append(mutationSuffixes.randomItem())
                }
            }
            2 -> { // Duplicate a consonant
                val consonants = "bcdfghjklmnpqrstvwxyz"
                val indices = sb.indices.filter { sb[it] in consonants }
                if (indices.isNotEmpty()) {
                    val idx = indices.randomItem()
                    sb.insert(idx + 1, sb[idx])
                }
            }
            3 -> { // Remove a vowel
                val vowels = "aeiou"
                val indices = sb.indices.filter { sb[it] in vowels && it != 0 }
                if (indices.isNotEmpty()) {
                    val idx = indices.randomItem()
                    sb.deleteCharAt(idx)
                }
            }
        }
        
        // Ensure length is at least 4
        if (sb.length < 4) {
            sb.append(mutationSuffixes.randomItem())
        }
        
        return sb.toString().replaceFirstChar { it.uppercase() }
    }

    private fun injectVocalization(input: String): String {
        val sb = StringBuilder(input)
        val targetIndices = sb.indices.filter { sb[it].lowercaseChar() in vocalizationMap.keys }
        if (targetIndices.isEmpty()) return input
        
        // Only vocalize ONE letter to keep it natural
        val idx = targetIndices.randomItem()
        val originalChar = sb[idx].lowercaseChar()
        val isUpper = sb[idx].isUpperCase()
        
        val replacementOptions = vocalizationMap[originalChar] ?: return input
        val replacement = replacementOptions.randomItem()
        
        sb.setCharAt(idx, if (isUpper) replacement.titlecase()[0] else replacement)
        return sb.toString()
    }

    private fun generateBaseName(style: NameStyle): String {
        return when (style) {
            NameStyle.JAPANESE -> (1..3).map { japaneseBases.randomItem().take(Random.nextInt(2, 4)) }.joinToString("")
            NameStyle.CHINESE -> (1..2).map { chineseBases.randomItem().take(Random.nextInt(2, 3)) }.joinToString("")
            else -> {
                val realBase = when (style) {
                    NameStyle.NORMAL -> normalBases.randomItem()
                    NameStyle.GERMAN -> germanBases.randomItem()
                    NameStyle.FRENCH -> frenchBases.randomItem()
                    NameStyle.SPANISH -> spanishBases.randomItem()
                    NameStyle.BRITISH -> britishBases.randomItem()
                    NameStyle.AMERICAN -> americanBases.randomItem()
                    NameStyle.ARABIC -> arabicBases.randomItem()
                    NameStyle.TURKISH -> turkishBases.randomItem()
                    else -> normalBases.randomItem()
                }
                mutateBase(realBase)
            }
        }
    }

    fun generateUniqueName(style: NameStyle = NameStyle.NORMAL): String {
        var attempts = 0
        var candidate: String

        do {
            // 1. Generate the base mutated name
            candidate = generateBaseName(style)

            // 2. Custom combinations (46% chance)
            if (customCombinations.isNotEmpty() && Random.nextFloat() < 0.46f) {
                val custom = customCombinations.randomItem()
                candidate = when (Random.nextInt(3)) {
                    0 -> if (candidate.length > 2) candidate.dropLast(2) + custom else custom + candidate
                    1 -> if (candidate.length > 2) custom.replaceFirstChar { it.uppercase() } + candidate.drop(2) else custom.replaceFirstChar { it.uppercase() } + candidate
                    else -> if (candidate.length > 2) candidate.dropLast(candidate.length / 2) + custom else custom
                }
            }

            // 3. Vocalization (33% chance, only for specific cultures)
            if (Random.nextFloat() < 0.33f && style in vocalizedCultures) {
                candidate = injectVocalization(candidate)
            }

            // 4. Normalize
            candidate = candidate
                .replace(Regex("\\s+"), "")
                .filter { it.isLetter() } // strip any weird artifacts
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            // Ensure it's not too short
            if (candidate.length < 3) {
                candidate += mutationSuffixes.randomItem()
            }

            attempts++
        } while ((candidate in usedNames) && attempts < 60)

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
                if (full.length > 7) full.take(Random.nextInt(4, 8)) else full
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
