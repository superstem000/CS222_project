package com.example.mealplannerapp.fridgefirst.data

import kotlinx.coroutines.flow.Flow

/**
 * The recipes planned for the current week, by recipe id, in the order they were added.
 *
 * Deliberately simpler than the starter's date + meal-slot planner: Fridge First only needs
 * "which recipes am I making this week". If Jayda's plan model adds days/slots, those can be
 * added here without changing the other screens.
 */
interface WeeklyPlanRepository {
    fun observePlannedRecipeIds(): Flow<List<Long>>

    /** No-op if the recipe is already planned. */
    suspend fun add(recipeId: Long)

    /** No-op if the recipe isn't planned. */
    suspend fun remove(recipeId: Long)
}
