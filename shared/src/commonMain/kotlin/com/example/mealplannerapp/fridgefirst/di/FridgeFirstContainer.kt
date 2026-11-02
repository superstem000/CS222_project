package com.example.mealplannerapp.fridgefirst.di

import com.example.mealplannerapp.fridgefirst.data.InventoryRepository
import com.example.mealplannerapp.fridgefirst.data.RecipeBookRepository
import com.example.mealplannerapp.fridgefirst.data.WeeklyPlanRepository
import com.example.mealplannerapp.fridgefirst.data.fake.InMemoryInventoryRepository
import com.example.mealplannerapp.fridgefirst.data.fake.InMemoryRecipeBookRepository
import com.example.mealplannerapp.fridgefirst.data.fake.InMemoryWeeklyPlanRepository
import com.example.mealplannerapp.fridgefirst.data.fake.SampleData
import com.example.mealplannerapp.fridgefirst.domain.IngredientMatcher
import com.example.mealplannerapp.fridgefirst.domain.KitchenCalculator
import com.example.mealplannerapp.fridgefirst.domain.NormalizingIngredientMatcher

/**
 * Everything the Fridge First screens depend on. This is the ONE place to change when the real
 * database implementations are ready: construct them here instead of the in-memory fakes.
 */
class FridgeFirstContainer(
    val inventoryRepository: InventoryRepository,
    val recipeBookRepository: RecipeBookRepository,
    val weeklyPlanRepository: WeeklyPlanRepository,
    ingredientMatcher: IngredientMatcher = NormalizingIngredientMatcher(),
) {
    val kitchen: KitchenCalculator = KitchenCalculator(ingredientMatcher)

    companion object {
        /** In-memory fakes, optionally seeded with the prototype's sample data. */
        fun inMemory(withSampleData: Boolean = true) = FridgeFirstContainer(
            inventoryRepository = InMemoryInventoryRepository(if (withSampleData) SampleData.inventory else emptyList()),
            recipeBookRepository = InMemoryRecipeBookRepository(if (withSampleData) SampleData.recipes else emptyList()),
            weeklyPlanRepository = InMemoryWeeklyPlanRepository(),
        )
    }
}
