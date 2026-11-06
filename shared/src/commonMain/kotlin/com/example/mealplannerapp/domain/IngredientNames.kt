

package com.example.mealplannerapp.domain

/**
 * Turns an ingredient name (or a non-standard unit like "cans") into a comparison key, so that
 * "Eggs", " egg " and "EGGS" all become "egg". Only the LAST word is singularized
 * ("green onions" -> "green onion"). The key is only used for matching; the app still displays
 * the name the user typed.
 */
object IngredientNames {

    // Words ending in "s" that are already singular.
    private val keepAsIs = setOf("molasses", "hummus", "couscous", "asparagus", "series", "species")

    fun normalize(name: String): String {
        val words = name.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (words.isEmpty()) return ""
        return (words.dropLast(1) + singularize(words.last())).joinToString(" ")
    }

    internal fun singularize(word: String): String {
        if (word in keepAsIs || word.length <= 3) return word
        return when {
            word.endsWith("ies") -> word.dropLast(3) + "y"      // berries -> berry
            word.endsWith("oes") -> word.dropLast(2)            // tomatoes -> tomato
            word.endsWith("ches") || word.endsWith("shes") ||
                word.endsWith("xes") || word.endsWith("zes") -> word.dropLast(2) // peaches -> peach
            word.endsWith("ss") || word.endsWith("us") || word.endsWith("is") -> word // swiss, citrus
            word.endsWith("s") -> word.dropLast(1)              // eggs -> egg
            else -> word
        }
    }
}
