package com.example.mealplannerapp.fridgefirst.data.room

import com.example.mealplannerapp.data.local.Ingredient
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.data.local.RecipeWithIngredients
import com.example.mealplannerapp.data.repository.RecipeRepository
import com.example.mealplannerapp.fridgefirst.data.RecipeBookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** [RecipeBookRepository] backed by the existing Room [RecipeRepository]. */
class RoomRecipeBookRepository(private val recipes: RecipeRepository) : RecipeBookRepository {

    override fun observeRecipes(): Flow<List<RecipeWithIngredients>> = recipes.observeAllWithIngredients()

    override fun observeRecipe(recipeId: Long): Flow<RecipeWithIngredients?> =
        recipes.observeRecipeWithIngredients(recipeId)

    override suspend fun save(recipe: Recipe, ingredients: List<Ingredient>): Long =
        recipes.saveRecipe(recipe, ingredients)

    override suspend fun delete(recipeId: Long) {
        recipes.observeRecipeWithIngredients(recipeId).first()?.let { recipes.deleteRecipe(it.recipe) }
    }
}
