package com.example

import kotlin.random.Random

enum class NameStyle(val displayName: String, val key: String) {
    NORMAL("Normal", "normal"),
    TWO_PART("Two-Part", "two-part"),
    SHORT("Short", "short"),
    LONG("Long", "long"),
    GERMAN("German", "german"),
    FRENCH("French", "french"),
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

    private val mutationSuffixes = listOf("us", "ia", "iel", "is", "an", "or", "yx", "ax", "eth", "in", "os", "ar")
    
    // Unorthodox clusters to add slight unconventionality
    private val unorthodoxClusters = listOf("aei", "vz", "kh", "thl", "ss", "rr", "yy", "ou", "io", "ae", "zh", "wr")

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
    
    private val vocalizedCultures = listOf(NameStyle.FRENCH, NameStyle.GERMAN, NameStyle.SPANISH, NameStyle.TURKISH)

    private val longPrefixes = listOf(
        "New", "Old", "Great", "High", "Deep", "Far", "North", "South", "East", "West",
        "Upper", "Lower", "Ancient", "Hidden", "Silent", "Golden", "Silver", "Iron",
        "Shadow", "Crystal", "Eternal", "Forgotten", "Sacred", "Wild", "Pale", "Dark"
    )

    private val customCombinations = mutableListOf<String>()
    private val usedNames = mutableSetOf<String>()

    // === CONTEXTUAL LEARNING STATE ===
    private val learnedNames = mutableListOf<String>()
    private val learnedSyllables = mutableListOf<String>()

    // === DYNAMIC INTERVAL STATE ===
    private var generationCycleCount = 0
    private var customComboInterval = Random.nextInt(2, 5) // Every 2 to 4 requests

    private fun <T> List<T>.randomItem(): T = this[Random.nextInt(size)]

    // === CONTEXTUAL LEARNING API ===
    fun ingestLearnedNames(names: List<String>) {
        learnedNames.clear()
        learnedSyllables.clear()
        
        learnedNames.addAll(names.filter { it.isNotBlank() })
        
        // Extract 2-4 letter chunks (syllables) from learned names
        names.forEach { name ->
            val cleanName = name.lowercase().filter { it.isLetter() }
            if (cleanName.length >= 4) {
                val chunkSize = Random.nextInt(2, 5)
                if (cleanName.length >= chunkSize) {
                    val startIndex = Random.nextInt(0, cleanName.length - chunkSize + 1)
                    learnedSyllables.add(cleanName.substring(startIndex, startIndex + chunkSize))
                }
            }
        }
    }

    private fun mutateBase(base: String): String {
        val sb = StringBuilder(base.lowercase())
        val mutationType = Random.nextInt(5) // Added 5th mutation type

        when (mutationType) {
            0 -> { // Swap a vowel
                val vowels = listOf('a', 'e', 'i', 'o', 'u')
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
            4 -> { // Inject unorthodox cluster
                if (sb.length > 3) {
                    val insertPos = Random.nextInt(1, sb.length - 2)
                    sb.insert(insertPos, unorthodoxClusters.randomItem())
                }
            }
        }
        
        if (sb.length < 4) {
            sb.append(mutationSuffixes.randomItem())
        }
        
        return sb.toString().replaceFirstChar { it.uppercase() }
    }

    private fun injectVocalization(input: String): String {
        val sb = StringBuilder(input)
        val targetIndices = sb.indices.filter { sb[it].lowercaseChar() in vocalizationMap.keys }
        if (targetIndices.isEmpty()) return input
        
        val idx = targetIndices.randomItem()
        val originalChar = sb[idx].lowercaseChar()
        val isUpper = sb[idx].isUpperCase()
        
        val replacementOptions = vocalizationMap[originalChar] ?: return input
        val replacement = replacementOptions.randomItem()
        
        sb.setCharAt(idx, if (isUpper) replacement.titlecase()[0] else replacement)
        return sb.toString()
    }

    private fun generateBaseName(style: NameStyle): String {
        // Contextual Learning: 40% chance to use a learned name directly if available
        if (learnedNames.isNotEmpty() && Random.nextFloat() < 0.40f) {
            return mutateBase(learnedNames.randomItem())
        }
        
        // Fallback to cultural/real-world bases
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
            candidate = generateBaseName(style)

            // Dynamic Custom Combination Interval Check
            if (customCombinations.isNotEmpty() && generationCycleCount >= customComboInterval) {
                val custom = customCombinations.randomItem()
                candidate = when (Random.nextInt(3)) {
                    0 -> if (candidate.length > 2) candidate.dropLast(2) + custom else custom + candidate
                    1 -> if (candidate.length > 2) custom.replaceFirstChar { it.uppercase() } + candidate.drop(2) else custom.replaceFirstChar { it.uppercase() } + candidate
                    else -> if (candidate.length > 2) candidate.dropLast(candidate.length / 2) + custom else custom
                }
            }

            // Vocalization (33% chance, only for specific cultures)
            if (Random.nextFloat() < 0.33f && style in vocalizedCultures) {
                candidate = injectVocalization(candidate)
            }

            // Normalize and clean up
            candidate = candidate
                .replace(Regex("\\s+"), "")
                .filter { it.isLetter() }
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            if (candidate.length < 3) {
                candidate += mutationSuffixes.randomItem()
            }

            attempts++
        } while ((candidate in usedNames) && attempts < 60)

        usedNames.add(candidate)

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

    // Enforce strict uniqueness per batch request
    fun generateBatch(type: NameStyle, count: Int = 4): List<String> {
        // Update cycle counter
        generationCycleCount++
        // Reset interval dynamically (2 to 4)
        if (generationCycleCount >= customComboInterval) {
            generationCycleCount = 0
            customComboInterval = Random.nextInt(2, 5)
        }

        val batchNames = mutableSetOf<String>()
        var safetyCounter = 0
        
        while (batchNames.size < count && safetyCounter < count * 20) {
            val newName = generateName(type)
            if (newName !in batchNames) {
                batchNames.add(newName)
            }
            safetyCounter++
        }
        
        return batchNames.toList()
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
