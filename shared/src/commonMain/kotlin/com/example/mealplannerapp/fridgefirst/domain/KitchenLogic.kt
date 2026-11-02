package com.example.mealplannerapp.fridgefirst.domain

import com.example.mealplannerapp.data.local.RecipeWithIngredients
import com.example.mealplannerapp.domain.GroceryItem
import com.example.mealplannerapp.domain.InventoryData

data class Coverage(val have: Int, val total: Int) {
    val missing: Int get() = total - have
    val fraction: Float get() = if (total == 0) 1f else have.toFloat() / total
}

sealed interface InventoryChange {
    data class SetQuantity(val id: Int, val quantity: Double) : InventoryChange
    data class Remove(val id: Int) : InventoryChange
}

/** Feature logic used by the screens. */
interface KitchenLogic {
    fun missingFor(recipe: RecipeWithIngredients, inventory: List<InventoryData>): List<GroceryItem>

    fun coverage(recipe: RecipeWithIngredients, inventory: List<InventoryData>): Coverage

    fun groceryList(recipes: List<RecipeWithIngredients>, inventory: List<InventoryData>): List<GroceryItem>

    fun cookingChanges(recipe: RecipeWithIngredients, inventory: List<InventoryData>): List<InventoryChange>

    fun mergeTarget(name: String, unit: String, inventory: List<InventoryData>): InventoryData?
}
