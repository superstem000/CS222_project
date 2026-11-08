package com.example.mealplannerapp.fridgefirst.data.fake

import com.example.mealplannerapp.data.local.RecipeWithIngredients
import com.example.mealplannerapp.domain.GroceryItem
import com.example.mealplannerapp.domain.InventoryData
import com.example.mealplannerapp.fridgefirst.domain.Coverage
import com.example.mealplannerapp.fridgefirst.domain.InventoryChange
import com.example.mealplannerapp.fridgefirst.domain.KitchenLogic

/** DEMO ONLY: matches identical name (ignoring case) and unit; no conversion. */
class PlaceholderKitchenLogic : KitchenLogic {

    private fun key(name: String, unit: String) = name.trim().lowercase() + "|" + unit.trim().lowercase()

    private fun onHand(key: String, inventory: List<InventoryData>) =
        inventory.filter { key(it.name, it.unit) == key }.sumOf { it.quantity }

    override fun missingFor(recipe: RecipeWithIngredients, inventory: List<InventoryData>) =
        groceryList(listOf(recipe), inventory)

    override fun coverage(recipe: RecipeWithIngredients, inventory: List<InventoryData>): Coverage {
        val total = recipe.ingredients.map { key(it.name, it.unit) }.distinct().size
        return Coverage(have = total - missingFor(recipe, inventory).size, total = total)
    }

    override fun groceryList(recipes: List<RecipeWithIngredients>, inventory: List<InventoryData>): List<GroceryItem> =
        recipes.flatMap { entry -> entry.ingredients.map { entry.recipe.name to it } }
            .groupBy { (_, ingredient) -> key(ingredient.name, ingredient.unit) }
            .mapNotNull { (key, uses) ->
                val first = uses.first().second
                val stillNeeded = uses.sumOf { it.second.quantity } - onHand(key, inventory)
                if (stillNeeded <= 0.0) null else GroceryItem(
                    key = key,
                    displayName = first.name,
                    unit = first.unit,
                    totalQuantity = stillNeeded,
                    sourceRecipeNames = uses.map { it.first }.toSet(),
                )
            }
            .sortedBy { it.displayName.lowercase() }

    override fun cookingChanges(recipe: RecipeWithIngredients, inventory: List<InventoryData>): List<InventoryChange> =
        recipe.ingredients.mapNotNull { ingredient ->
            val row = mergeTarget(ingredient.name, ingredient.unit, inventory) ?: return@mapNotNull null
            val left = row.quantity - ingredient.quantity
            if (left > 0.0) InventoryChange.SetQuantity(row.id, left) else InventoryChange.Remove(row.id)
        }

    override fun mergeTarget(name: String, unit: String, inventory: List<InventoryData>): InventoryData? =
        inventory.firstOrNull { key(it.name, it.unit) == key(name, unit) }
}
