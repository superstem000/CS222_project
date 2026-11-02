package com.example.mealplannerapp.fridgefirst.domain

/**
 * Decides whether a recipe ingredient and an inventory item are "the same thing" and how their
 * quantities compare. This is the seam for Jayda's ingredient catalog (docs/ingredient-matching-plan.md):
 * once recipes and inventory share catalog ids, a catalog-backed matcher replaces
 * [NormalizingIngredientMatcher] and nothing else changes.
 */
interface IngredientMatcher {
    /** Two names refer to the same ingredient iff their keys are equal ("Eggs" and "egg" -> "egg"). */
    fun nameKey(name: String): String

    /** Normalized unit spelling, used to decide whether two rows can simply be added together. */
    fun unitKey(unit: String): String

    /**
     * Converts [quantity] from [fromUnit] to [toUnit], or returns null when the two can't be
     * compared (different unit categories, or an unknown unit that isn't spelled the same).
     */
    fun convert(quantity: Double, fromUnit: String, toUnit: String): Double?

    /**
     * Groups units that can be converted into each other. Ingredients with the same name key and
     * the same bucket merge into one grocery line.
     */
    fun unitBucket(unit: String): String
}

enum class UnitCategory { MASS, VOLUME, COUNT }

/**
 * INTERIM implementation of Jayda's plan, until the shared ingredient catalog exists:
 * - Names: trim, lowercase, collapse spaces, simple singularization of the last word (§3).
 * - Units: mass / volume / count categories with the plan's fixed factors (§2.2).
 * - Different categories (e.g. cups vs. grams) are not comparable, so they count as missing (§2.2).
 */
class NormalizingIngredientMatcher : IngredientMatcher {

    private data class KnownUnit(val category: UnitCategory, val toBase: Double)

    override fun nameKey(name: String): String {
        val words = name.trim().lowercase().split(WHITESPACE).filter { it.isNotEmpty() }
        if (words.isEmpty()) return ""
        return (words.dropLast(1) + singularize(words.last())).joinToString(" ")
    }

    override fun unitKey(unit: String): String {
        val cleaned = unit.trim().lowercase().trimEnd('.').split(WHITESPACE)
            .filter { it.isNotEmpty() }.joinToString(" ")
        return UNIT_ALIASES[cleaned] ?: cleaned
    }

    override fun convert(quantity: Double, fromUnit: String, toUnit: String): Double? {
        val from = unitKey(fromUnit)
        val to = unitKey(toUnit)
        if (from == to) return quantity
        val fromUnitInfo = UNITS[from] ?: return null
        val toUnitInfo = UNITS[to] ?: return null
        if (fromUnitInfo.category != toUnitInfo.category) return null
        return quantity * fromUnitInfo.toBase / toUnitInfo.toBase
    }

    override fun unitBucket(unit: String): String {
        val key = unitKey(unit)
        return UNITS[key]?.category?.name ?: "unit:$key"
    }

    private fun singularize(word: String): String = when {
        word in NO_SINGULAR -> word
        word.length <= 3 -> word
        word.endsWith("ies") -> word.dropLast(3) + "y"          // berries -> berry
        word.endsWith("oes") -> word.dropLast(2)                // tomatoes -> tomato
        SIBILANT_PLURALS.any { word.endsWith(it) } -> word.dropLast(2) // peaches -> peach, boxes -> box
        word.endsWith("ss") || word.endsWith("us") || word.endsWith("is") -> word
        word.endsWith("s") -> word.dropLast(1)                  // eggs -> egg
        else -> word
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
        val SIBILANT_PLURALS = listOf("ches", "shes", "sses", "xes")
        val NO_SINGULAR = setOf("hummus", "couscous", "molasses", "swiss", "asparagus", "series", "species")

        val UNITS: Map<String, KnownUnit> = mapOf(
            // Mass, base unit g
            "g" to KnownUnit(UnitCategory.MASS, 1.0),
            "kg" to KnownUnit(UnitCategory.MASS, 1000.0),
            "oz" to KnownUnit(UnitCategory.MASS, 28.3495),
            "lb" to KnownUnit(UnitCategory.MASS, 453.592),
            // Volume, base unit mL
            "ml" to KnownUnit(UnitCategory.VOLUME, 1.0),
            "l" to KnownUnit(UnitCategory.VOLUME, 1000.0),
            "tsp" to KnownUnit(UnitCategory.VOLUME, 4.929),
            "tbsp" to KnownUnit(UnitCategory.VOLUME, 14.787),
            "cup" to KnownUnit(UnitCategory.VOLUME, 236.588),
            "fl oz" to KnownUnit(UnitCategory.VOLUME, 29.5735),
            // Count, base unit piece. The plan treats these as 1:1 for now.
            "piece" to KnownUnit(UnitCategory.COUNT, 1.0),
            "can" to KnownUnit(UnitCategory.COUNT, 1.0),
            "clove" to KnownUnit(UnitCategory.COUNT, 1.0),
        )

        val UNIT_ALIASES: Map<String, String> = mapOf(
            "gram" to "g", "grams" to "g",
            "kilogram" to "kg", "kilograms" to "kg", "kgs" to "kg",
            "ounce" to "oz", "ounces" to "oz",
            "lbs" to "lb", "pound" to "lb", "pounds" to "lb",
            "milliliter" to "ml", "milliliters" to "ml", "millilitre" to "ml", "millilitres" to "ml",
            "liter" to "l", "liters" to "l", "litre" to "l", "litres" to "l",
            "teaspoon" to "tsp", "teaspoons" to "tsp",
            "tablespoon" to "tbsp", "tablespoons" to "tbsp", "tbs" to "tbsp",
            "cups" to "cup", "c" to "cup",
            "fluid ounce" to "fl oz", "fluid ounces" to "fl oz", "floz" to "fl oz",
            "" to "piece", "pc" to "piece", "pcs" to "piece", "pieces" to "piece",
            "whole" to "piece", "each" to "piece", "ea" to "piece",
            "cans" to "can", "cloves" to "clove",
        )
    }
}
