package com.example.mealplannerapp.fridgefirst.data.fake

import com.example.mealplannerapp.fridgefirst.data.WeeklyPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [WeeklyPlanRepository]. */
class InMemoryWeeklyPlanRepository(initialRecipeIds: List<Long> = emptyList()) : WeeklyPlanRepository {

    private val plannedIds = MutableStateFlow(initialRecipeIds.distinct())

    override fun observePlannedRecipeIds(): Flow<List<Long>> = plannedIds.asStateFlow()

    override suspend fun add(recipeId: Long) {
        plannedIds.update { if (recipeId in it) it else it + recipeId }
    }

    override suspend fun remove(recipeId: Long) {
        plannedIds.update { it - recipeId }
    }
}
