package com.example.mealplannerapp.fridgefirst.di

import com.example.mealplannerapp.data.repository.RecipeRepository
import com.example.mealplannerapp.fridgefirst.data.InventoryRepository
import com.example.mealplannerapp.fridgefirst.data.RecipeBookRepository
import com.example.mealplannerapp.fridgefirst.data.WeeklyPlanRepository
import com.example.mealplannerapp.fridgefirst.data.fake.InMemoryInventoryRepository
import com.example.mealplannerapp.fridgefirst.data.fake.InMemoryRecipeBookRepository
import com.example.mealplannerapp.fridgefirst.data.fake.InMemoryWeeklyPlanRepository
import com.example.mealplannerapp.fridgefirst.data.fake.PlaceholderKitchenLogic
import com.example.mealplannerapp.fridgefirst.data.fake.SampleData
import com.example.mealplannerapp.fridgefirst.data.room.RoomRecipeBookRepository
import com.example.mealplannerapp.fridgefirst.domain.CheckerKitchenLogic
import com.example.mealplannerapp.fridgefirst.domain.KitchenLogic

/** Screen dependencies. Swap the fakes for real implementations here. */
class FridgeFirstContainer(
    val inventoryRepository: InventoryRepository,
    val recipeBookRepository: RecipeBookRepository,
    val weeklyPlanRepository: WeeklyPlanRepository,
    val kitchen: KitchenLogic = CheckerKitchenLogic(fallback = PlaceholderKitchenLogic()),
) {

    companion object {
        /** Recipes in the Room database; inventory and plan still in memory. */
        fun withRoomRecipes(recipeRepository: RecipeRepository) = FridgeFirstContainer(
            inventoryRepository = InMemoryInventoryRepository(SampleData.inventory),
            recipeBookRepository = RoomRecipeBookRepository(recipeRepository),
            weeklyPlanRepository = InMemoryWeeklyPlanRepository(),
        )

        fun inMemory(withSampleData: Boolean = true) = FridgeFirstContainer(
            inventoryRepository = InMemoryInventoryRepository(if (withSampleData) SampleData.inventory else emptyList()),
            recipeBookRepository = InMemoryRecipeBookRepository(if (withSampleData) SampleData.recipes else emptyList()),
            weeklyPlanRepository = InMemoryWeeklyPlanRepository(),
        )
    }
}
