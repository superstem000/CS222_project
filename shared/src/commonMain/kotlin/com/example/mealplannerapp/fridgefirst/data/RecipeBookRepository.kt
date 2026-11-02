package com.example.mealplannerapp.fridgefirst.data

import com.example.mealplannerapp.data.local.Ingredient
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.data.local.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

/**
 * What the UI needs from recipe storage. Reuses the existing Room models
 * ([Recipe], [Ingredient], [RecipeWithIngredients]) so no parallel recipe type exists.
 *
 * Named "RecipeBook" to avoid clashing with the starter's concrete
 * `data.repository.RecipeRepository`. The method shapes intentionally mirror that class, so a
 * database-backed implementation is a thin adapter over it (or over Jayda's recipe DB).
 */
interface RecipeBookRepository {
    /** All saved recipes, sorted by name. */
    fun observeRecipes(): Flow<List<RecipeWithIngredients>>

    /** A single recipe, or null if it doesn't exist (e.g. it was deleted). */
    fun observeRecipe(recipeId: Long): Flow<RecipeWithIngredients?>

    /**
     * Inserts (when `recipe.id == 0`) or replaces a recipe and its full ingredient list.
     * Ingredient `recipeId`/`sortOrder` are filled in by the repository. Returns the recipe id.
     */
    suspend fun save(recipe: Recipe, ingredients: List<Ingredient>): Long

    suspend fun delete(recipeId: Long)
}
