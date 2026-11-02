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

/** Screen dependencies. Swap the fakes for real implementations here. */
class FridgeFirstContainer(
    val inventoryRepository: InventoryRepository,
    val recipeBookRepository: RecipeBookRepository,
    val weeklyPlanRepository: WeeklyPlanRepository,
    val kitchen: KitchenLogic = PlaceholderKitchenLogic(),
) {

    companion object {
        fun inMemory(withSampleData: Boolean = true) = FridgeFirstContainer(
            inventoryRepository = InMemoryInventoryRepository(if (withSampleData) SampleData.inventory else emptyList()),
            recipeBookRepository = InMemoryRecipeBookRepository(if (withSampleData) SampleData.recipes else emptyList()),
            weeklyPlanRepository = InMemoryWeeklyPlanRepository(),
        )
    }
}
