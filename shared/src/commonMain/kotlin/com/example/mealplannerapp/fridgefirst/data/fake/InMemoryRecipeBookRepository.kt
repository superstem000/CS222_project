package com.example.mealplannerapp.fridgefirst.data.fake

import com.example.mealplannerapp.data.local.Ingredient
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.data.local.RecipeWithIngredients
import com.example.mealplannerapp.fridgefirst.data.RecipeBookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** In-memory [RecipeBookRepository]. */
class InMemoryRecipeBookRepository(initialRecipes: List<RecipeWithIngredients> = emptyList()) : RecipeBookRepository {

    private val mutex = Mutex()
    private val recipes = MutableStateFlow<List<RecipeWithIngredients>>(emptyList())
    private var nextRecipeId = 1L
    private var nextIngredientId = 1L

    init {
        initialRecipes.forEach { saveUnlocked(it.recipe, it.ingredients) }
    }

    override fun observeRecipes(): Flow<List<RecipeWithIngredients>> = recipes.asStateFlow()

    override fun observeRecipe(recipeId: Long): Flow<RecipeWithIngredients?> =
        recipes.map { list -> list.firstOrNull { it.recipe.id == recipeId } }.distinctUntilChanged()

    override suspend fun save(recipe: Recipe, ingredients: List<Ingredient>): Long =
        mutex.withLock { saveUnlocked(recipe, ingredients) }

    override suspend fun delete(recipeId: Long) {
        mutex.withLock { recipes.value = recipes.value.filterNot { it.recipe.id == recipeId } }
    }

    private fun saveUnlocked(recipe: Recipe, ingredients: List<Ingredient>): Long {
        val id = if (recipe.id == 0L) nextRecipeId++ else recipe.id.also { nextRecipeId = maxOf(nextRecipeId, it + 1) }
        val saved = RecipeWithIngredients(
            recipe = recipe.copy(id = id),
            ingredients = ingredients.mapIndexed { index, ingredient ->
                ingredient.copy(id = nextIngredientId++, recipeId = id, sortOrder = index)
            }
        )
        recipes.value = (recipes.value.filterNot { it.recipe.id == id } + saved)
            .sortedBy { it.recipe.name.lowercase() }
        return id
    }
}
