

package com.example.mealplannerapp.domain

enum class UnitCategory { MASS, VOLUME, COUNT }

/**
 * Known units. [toBase] is how many base units one of this unit equals:
 * grams for MASS, milliliters for VOLUME, pieces for COUNT (US customary, exact definitions).
 * Units are only converted within the same category; volume <-> mass needs a density per
 * ingredient, which is out of scope (see docs/ingredient-matching-plan.md).
 */
enum class MeasureUnit(
    val category: UnitCategory,
    val toBase: Double,
    private val aliases: Set<String>
) {
    GRAM(UnitCategory.MASS, 1.0, setOf("g", "gr", "gram", "grams")),
    KILOGRAM(UnitCategory.MASS, 1000.0, setOf("kg", "kgs", "kilogram", "kilograms")),
    OUNCE(UnitCategory.MASS, 28.349523125, setOf("oz", "ounce", "ounces")),
    POUND(UnitCategory.MASS, 453.59237, setOf("lb", "lbs", "pound", "pounds")),

    MILLILITER(UnitCategory.VOLUME, 1.0, setOf("ml", "milliliter", "milliliters", "millilitre", "millilitres")),
    LITER(UnitCategory.VOLUME, 1000.0, setOf("l", "liter", "liters", "litre", "litres")),
    TEASPOON(UnitCategory.VOLUME, 4.92892159375, setOf("tsp", "tsps", "teaspoon", "teaspoons")),
    TABLESPOON(UnitCategory.VOLUME, 14.78676478125, setOf("tbsp", "tbsps", "tbs", "tablespoon", "tablespoons")),
    FLUID_OUNCE(UnitCategory.VOLUME, 29.5735295625, setOf("fl oz", "floz", "fluid ounce", "fluid ounces")),
    CUP(UnitCategory.VOLUME, 236.5882365, setOf("c", "cup", "cups")),

    // An empty unit ("2 eggs") counts as pieces.
    PIECE(UnitCategory.COUNT, 1.0, setOf("", "piece", "pieces", "pc", "pcs", "whole", "each", "ea", "item", "items"));

    fun toBaseQuantity(quantity: Double): Double = quantity * toBase

    fun fromBaseQuantity(baseQuantity: Double): Double = baseQuantity / toBase

    companion object {
        /** Reads a typed unit ("Cups", "tbsp.", "  g ") or returns null if it isn't recognized. */
        fun parse(raw: String): MeasureUnit? {
            val key = raw.trim().lowercase().removeSuffix(".").replace(Regex("\\s+"), " ")
            return values().firstOrNull { key in it.aliases }
        }
    }
}
