package com.example.mealplannerapp.fridgefirst.domain

import com.example.mealplannerapp.data.local.RecipeWithIngredients
import com.example.mealplannerapp.domain.GroceryItem
import com.example.mealplannerapp.domain.IngredientNames
import com.example.mealplannerapp.domain.InventoryData
import com.example.mealplannerapp.domain.MissingIngredientChecker

/** Missing ingredients and coverage from [MissingIngredientChecker]; everything else from [fallback]. */
class CheckerKitchenLogic(private val fallback: KitchenLogic) : KitchenLogic by fallback {

    override fun missingFor(recipe: RecipeWithIngredients, inventory: List<InventoryData>): List<GroceryItem> =
        MissingIngredientChecker.check(recipe.ingredients, inventory)
            .filter { it.isMissing }
            .map {
                GroceryItem(
                    key = IngredientNames.normalize(it.ingredientName) + "|" + it.unit.lowercase(),
                    displayName = it.ingredientName,
                    unit = it.unit,
                    totalQuantity = it.missingQuantity,
                    sourceRecipeNames = setOf(recipe.recipe.name),
                )
            }

    override fun coverage(recipe: RecipeWithIngredients, inventory: List<InventoryData>): Coverage {
        val checks = MissingIngredientChecker.check(recipe.ingredients, inventory)
        return Coverage(have = checks.count { !it.isMissing }, total = checks.size)
    }
}
