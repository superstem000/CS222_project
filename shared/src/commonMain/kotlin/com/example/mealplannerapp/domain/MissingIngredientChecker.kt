

package com.example.mealplannerapp.domain

import com.example.mealplannerapp.data.local.Ingredient
import kotlin.math.max

/** Result for one recipe ingredient. [missingQuantity] is in the recipe's own unit; 0 means "have it". */
data class IngredientCheck(
    val ingredientName: String,
    val neededQuantity: Double,
    val unit: String,
    val missingQuantity: Double
) {
    val isMissing: Boolean get() = missingQuantity > 0.0
}

/**
 * Compares a recipe's ingredients against inventory (docs/ingredient-matching-plan.md, section 3).
 *
 * Ingredients match on [IngredientNames.normalize] for now, because inventory isn't in the
 * database yet; this switches to catalog IDs once it is.
 *
 * For each recipe ingredient:
 *  1. No inventory item with the same name -> missing the full amount.
 *  2. Recipe unit recognized: add up the inventory rows in the SAME category (converted to base
 *     units); rows in a different category are ignored, so cups vs. grams counts as missing.
 *  3. Recipe unit not recognized ("can", "clove"): only inventory rows with the same unit count.
 *  4. missing = max(0, needed - on hand), reported in the recipe's unit.
 */
object MissingIngredientChecker {

    // Rounding noise from unit conversion below this is treated as zero.
    private const val EPSILON = 1e-9

    fun check(
        recipeIngredients: List<Ingredient>,
        inventory: List<InventoryData>,
        scale: Double = 1.0
    ): List<IngredientCheck> {
        val inventoryByName = inventory.groupBy { IngredientNames.normalize(it.name) }

        return recipeIngredients.mapNotNull { ingredient ->
            val key = IngredientNames.normalize(ingredient.name)
            if (key.isEmpty()) return@mapNotNull null

            val needed = ingredient.quantity * scale
            val matches = inventoryByName[key].orEmpty()
            val recipeUnit = MeasureUnit.parse(ingredient.unit)

            val missing = if (recipeUnit != null) {
                val onHandBase = matches
                    .mapNotNull { item ->
                        val itemUnit = MeasureUnit.parse(item.unit)
                        if (itemUnit?.category == recipeUnit.category) itemUnit.toBaseQuantity(item.quantity) else null
                    }
                    .sum()
                recipeUnit.fromBaseQuantity(max(0.0, recipeUnit.toBaseQuantity(needed) - onHandBase))
            } else {
                val unitKey = IngredientNames.normalize(ingredient.unit)
                val onHand = matches
                    .filter { IngredientNames.normalize(it.unit) == unitKey }
                    .sumOf { it.quantity }
                max(0.0, needed - onHand)
            }

            IngredientCheck(
                ingredientName = ingredient.name.trim(),
                neededQuantity = needed,
                unit = ingredient.unit.trim(),
                missingQuantity = if (missing < EPSILON) 0.0 else missing
            )
        }
    }
}
