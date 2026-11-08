package com.example.mealplannerapp.fridgefirst.data

import com.example.mealplannerapp.data.local.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Planned recipes, in plan order. */
fun plannedRecipes(
    plan: WeeklyPlanRepository,
    recipes: RecipeBookRepository
): Flow<List<RecipeWithIngredients>> =
    combine(plan.observePlannedRecipeIds(), recipes.observeRecipes()) { ids, all ->
        val byId = all.associateBy { it.recipe.id }
        ids.mapNotNull { byId[it] }
    }
