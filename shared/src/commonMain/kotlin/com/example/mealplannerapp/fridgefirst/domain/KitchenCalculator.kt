package com.example.mealplannerapp.fridgefirst.domain

import com.example.mealplannerapp.data.local.RecipeWithIngredients
import com.example.mealplannerapp.domain.GroceryItem
import com.example.mealplannerapp.domain.InventoryData
import kotlin.math.min

/** How many of a recipe's (merged) ingredients are fully covered by inventory. */
data class Coverage(val have: Int, val total: Int) {
    val missing: Int get() = total - have
    val fraction: Float get() = if (total == 0) 1f else have.toFloat() / total
}

/** A change to apply to inventory, produced by [KitchenCalculator.cookingChanges]. */
sealed interface InventoryChange {
    data class SetQuantity(val id: Int, val quantity: Double) : InventoryChange
    data class Remove(val id: Int) : InventoryChange
}

/**
 * Pure inventory/recipe math used by the screens: missing ingredients, recommendations,
 * the grocery list, stocking and cooking.
 *
 * INTERIM: grocery generation and cooking belong to Namit's feature area and the matching rules
 * to Jayda's. This class keeps them in one stateless place so the UI works end-to-end now, and
 * can be replaced by (or delegate to) their implementations later. Quantities are used as written
 * in the recipe; servings scaling is not applied yet.
 */
class KitchenCalculator(private val matcher: IngredientMatcher) {

    /** Ingredients the recipe still needs, in the recipe's units. Empty means "you have everything". */
    fun missingFor(recipe: RecipeWithIngredients, inventory: List<InventoryData>): List<GroceryItem> =
        groceryList(listOf(recipe), inventory)

    fun coverage(recipe: RecipeWithIngredients, inventory: List<InventoryData>): Coverage {
        val total = aggregate(listOf(recipe)).size
        return Coverage(have = total - missingFor(recipe, inventory).size, total = total)
    }

    /**
     * Grocery list for a set of planned recipes: ingredients with the same name and compatible
     * units are merged (duplicates across recipes become one line), then whatever is already in
     * inventory is subtracted. Only lines with something left to buy are returned.
     */
    fun groceryList(recipes: List<RecipeWithIngredients>, inventory: List<InventoryData>): List<GroceryItem> =
        aggregate(recipes).mapNotNull { need ->
            val stillNeeded = need.quantity - onHand(need.name, need.unit, inventory)
            if (stillNeeded <= EPSILON) {
                null
            } else {
                GroceryItem(
                    key = need.key,
                    displayName = need.name,
                    unit = need.unit,
                    totalQuantity = stillNeeded,
                    sourceRecipeNames = need.sources
                )
            }
        }.sortedBy { it.displayName.lowercase() }

    /** Total amount of [name] in inventory, expressed in [unit]. Rows with incomparable units are ignored. */
    fun onHand(name: String, unit: String, inventory: List<InventoryData>): Double {
        val key = matcher.nameKey(name)
        return inventory
            .filter { matcher.nameKey(it.name) == key }
            .sumOf { matcher.convert(it.quantity, it.unit, unit) ?: 0.0 }
    }

    /**
     * The existing inventory row that a newly stocked item should be merged into (same ingredient,
     * same unit), or null if it should become a new row.
     */
    fun mergeTarget(name: String, unit: String, inventory: List<InventoryData>): InventoryData? {
        val key = matcher.nameKey(name)
        val unitKey = matcher.unitKey(unit)
        return inventory.firstOrNull { matcher.nameKey(it.name) == key && matcher.unitKey(it.unit) == unitKey }
    }

    /**
     * What cooking [recipe] does to inventory: each ingredient is deducted from matching rows
     * (converting units where possible). Rows that run out are removed. Ingredients that aren't
     * on hand are simply skipped.
     */
    fun cookingChanges(recipe: RecipeWithIngredients, inventory: List<InventoryData>): List<InventoryChange> {
        val remaining = inventory.associate { it.id to it.quantity }.toMutableMap()
        for (need in aggregate(listOf(recipe))) {
            var toUse = need.quantity
            for (row in inventory) {
                if (toUse <= EPSILON) break
                if (matcher.nameKey(row.name) != matcher.nameKey(need.name)) continue
                val left = remaining.getValue(row.id)
                val availableInNeedUnit = matcher.convert(left, row.unit, need.unit) ?: continue
                val used = min(toUse, availableInNeedUnit)
                val usedInRowUnit = matcher.convert(used, need.unit, row.unit) ?: continue
                remaining[row.id] = left - usedInRowUnit
                toUse -= used
            }
        }
        return inventory.mapNotNull { row ->
            val left = remaining.getValue(row.id)
            when {
                left <= EPSILON -> InventoryChange.Remove(row.id)
                left != row.quantity -> InventoryChange.SetQuantity(row.id, left)
                else -> null
            }
        }
    }

    private class Need(
        val key: String,
        val name: String,
        val unit: String,
        var quantity: Double,
        val sources: MutableSet<String>
    )

    /** Merges ingredients across recipes by (name key, unit bucket), converting to the first-seen unit. */
    private fun aggregate(recipes: List<RecipeWithIngredients>): List<Need> {
        val needs = LinkedHashMap<String, Need>()
        for (entry in recipes) {
            for (ingredient in entry.ingredients) {
                val nameKey = matcher.nameKey(ingredient.name)
                if (nameKey.isEmpty() || ingredient.quantity <= 0.0) continue
                val key = "$nameKey|${matcher.unitBucket(ingredient.unit)}"
                val need = needs.getOrPut(key) {
                    Need(key, ingredient.name.trim(), ingredient.unit.trim(), 0.0, linkedSetOf())
                }
                need.quantity += matcher.convert(ingredient.quantity, ingredient.unit, need.unit) ?: ingredient.quantity
                need.sources += entry.recipe.name
            }
        }
        return needs.values.toList()
    }

    private companion object {
        const val EPSILON = 1e-6
    }
}
