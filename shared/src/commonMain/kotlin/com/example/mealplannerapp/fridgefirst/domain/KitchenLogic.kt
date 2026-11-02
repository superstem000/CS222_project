package com.example.mealplannerapp.fridgefirst.domain

import com.example.mealplannerapp.data.local.RecipeWithIngredients
import com.example.mealplannerapp.domain.GroceryItem
import com.example.mealplannerapp.domain.InventoryData

/** How many of a recipe's ingredients are covered by inventory. */
data class Coverage(val have: Int, val total: Int) {
    val missing: Int get() = total - have
    val fraction: Float get() = if (total == 0) 1f else have.toFloat() / total
}

/** A change to apply to inventory after cooking. */
sealed interface InventoryChange {
    data class SetQuantity(val id: Int, val quantity: Double) : InventoryChange
    data class Remove(val id: Int) : InventoryChange
}

/**
 * The feature logic the screens need. The UI demo uses
 * [com.example.mealplannerapp.fridgefirst.data.fake.PlaceholderKitchenLogic];
 * the real implementation comes from the feature owners and is swapped in via
 * [com.example.mealplannerapp.fridgefirst.di.FridgeFirstContainer].
 */
interface KitchenLogic {
    /** Missing-ingredient comparison (Jayda). Empty list = "you have everything". */
    fun missingFor(recipe: RecipeWithIngredients, inventory: List<InventoryData>): List<GroceryItem>

    /** Used to rank recommendations on the Weekly Plan screen (Jayda). */
    fun coverage(recipe: RecipeWithIngredients, inventory: List<InventoryData>): Coverage

    /** Grocery list for the planned recipes: duplicates merged, minus inventory (Namit). */
    fun groceryList(recipes: List<RecipeWithIngredients>, inventory: List<InventoryData>): List<GroceryItem>

    /** What cooking a recipe removes from inventory (Namit). */
    fun cookingChanges(recipe: RecipeWithIngredients, inventory: List<InventoryData>): List<InventoryChange>

    /** Existing row a newly added item should be merged into, or null for a new row (Andrew). */
    fun mergeTarget(name: String, unit: String, inventory: List<InventoryData>): InventoryData?
}
