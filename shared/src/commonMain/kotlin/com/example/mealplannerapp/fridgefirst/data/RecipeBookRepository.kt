package com.example.mealplannerapp.fridgefirst.data

import com.example.mealplannerapp.data.local.Ingredient
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.data.local.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

/** Recipe storage used by the UI. */
interface RecipeBookRepository {
    fun observeRecipes(): Flow<List<RecipeWithIngredients>>

    fun observeRecipe(recipeId: Long): Flow<RecipeWithIngredients?>

    /** Inserts (id == 0) or replaces a recipe; returns its id. */
    suspend fun save(recipe: Recipe, ingredients: List<Ingredient>): Long

    suspend fun delete(recipeId: Long)
}
