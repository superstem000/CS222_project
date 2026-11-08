package com.example.mealplannerapp.fridgefirst.data

import kotlinx.coroutines.flow.Flow

/** Recipe ids planned for this week. */
interface WeeklyPlanRepository {
    fun observePlannedRecipeIds(): Flow<List<Long>>

    suspend fun add(recipeId: Long)

    suspend fun remove(recipeId: Long)
}
