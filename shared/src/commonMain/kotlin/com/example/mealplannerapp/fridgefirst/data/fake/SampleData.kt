package com.example.mealplannerapp.fridgefirst.data.fake

import com.example.mealplannerapp.data.local.Ingredient
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.data.local.RecipeWithIngredients
import com.example.mealplannerapp.domain.InventoryData
import com.example.mealplannerapp.fridgefirst.data.RecipeBookRepository
import kotlinx.coroutines.flow.first

/** Sample data for the demo. */
object SampleData {

    val inventory: List<InventoryData> = listOf(
        item("Eggs", 4.0, "pcs"),
        item("Spinach", 1.0, "cup"),
        item("Rice", 3.0, "cup"),
        item("Cheese", 100.0, "g"),
        item("Soy sauce", 5.0, "tbsp"),
    )

    val recipes: List<RecipeWithIngredients> = listOf(
        recipe(
            "Veggie Omelette", servings = 1,
            ingredient("Eggs", 3.0, "pcs"), ingredient("Spinach", 1.0, "cup"), ingredient("Cheese", 50.0, "g")
        ),
        recipe(
            "Chicken Stir Fry", servings = 2,
            ingredient("Chicken", 300.0, "g"), ingredient("Rice", 1.0, "cup"),
            ingredient("Spinach", 1.0, "cup"), ingredient("Soy sauce", 2.0, "tbsp")
        ),
        recipe(
            "Pasta Marinara", servings = 2,
            ingredient("Pasta", 200.0, "g"), ingredient("Tomato sauce", 1.0, "cup"), ingredient("Cheese", 30.0, "g")
        ),
        recipe(
            "Fried Rice", servings = 2,
            ingredient("Rice", 2.0, "cup"), ingredient("Eggs", 2.0, "pcs"), ingredient("Soy sauce", 1.0, "tbsp")
        ),
    )

    /** Adds the sample recipes when there are no recipes yet. */
    suspend fun seedRecipesIfEmpty(repository: RecipeBookRepository) {
        if (repository.observeRecipes().first().isEmpty()) {
            recipes.forEach { repository.save(it.recipe, it.ingredients) }
        }
    }

    fun item(name: String, quantity: Double, unit: String) = InventoryData(id = 0, name = name, quantity = quantity, unit = unit)

    fun ingredient(name: String, quantity: Double, unit: String) =
        Ingredient(recipeId = 0, name = name, quantity = quantity, unit = unit)

    fun recipe(name: String, servings: Int, vararg ingredients: Ingredient) = RecipeWithIngredients(
        recipe = newRecipe(name, servings),
        ingredients = ingredients.toList()
    )

    fun newRecipe(name: String, servings: Int, id: Long = 0) = Recipe(
        id = id,
        name = name,
        servings = servings,
        caloriesPerServing = 0.0,
        proteinGramsPerServing = 0.0,
        carbsGramsPerServing = 0.0,
        fatGramsPerServing = 0.0
    )
}
