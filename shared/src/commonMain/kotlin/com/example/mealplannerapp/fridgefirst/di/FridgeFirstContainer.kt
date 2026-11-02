package com.example.mealplannerapp.fridgefirst.di

import com.example.mealplannerapp.fridgefirst.data.InventoryRepository
import com.example.mealplannerapp.fridgefirst.data.RecipeBookRepository
import com.example.mealplannerapp.fridgefirst.data.WeeklyPlanRepository
import com.example.mealplannerapp.fridgefirst.data.fake.InMemoryInventoryRepository
import com.example.mealplannerapp.fridgefirst.data.fake.InMemoryRecipeBookRepository
import com.example.mealplannerapp.fridgefirst.data.fake.InMemoryWeeklyPlanRepository
import com.example.mealplannerapp.fridgefirst.data.fake.PlaceholderKitchenLogic
import com.example.mealplannerapp.fridgefirst.data.fake.SampleData
import com.example.mealplannerapp.fridgefirst.domain.KitchenLogic

/**
 * Everything the Fridge First screens depend on. For the UI demo these are all in-memory fakes;
 * this is the one place to swap in the real database and feature logic later.
 */
class FridgeFirstContainer(
    val inventoryRepository: InventoryRepository,
    val recipeBookRepository: RecipeBookRepository,
    val weeklyPlanRepository: WeeklyPlanRepository,
    val kitchen: KitchenLogic = PlaceholderKitchenLogic(),
) {

    companion object {
        /** Demo setup: in-memory fakes, optionally seeded with sample data. */
        fun inMemory(withSampleData: Boolean = true) = FridgeFirstContainer(
            inventoryRepository = InMemoryInventoryRepository(if (withSampleData) SampleData.inventory else emptyList()),
            recipeBookRepository = InMemoryRecipeBookRepository(if (withSampleData) SampleData.recipes else emptyList()),
            weeklyPlanRepository = InMemoryWeeklyPlanRepository(),
        )
    }
}
