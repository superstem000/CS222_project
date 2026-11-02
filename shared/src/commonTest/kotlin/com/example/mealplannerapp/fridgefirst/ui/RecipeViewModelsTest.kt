package com.example.mealplannerapp.fridgefirst.ui

import com.example.mealplannerapp.fridgefirst.ViewModelTest
import com.example.mealplannerapp.fridgefirst.ui.recipes.IngredientDraft
import com.example.mealplannerapp.fridgefirst.ui.recipes.RecipeEditorViewModel
import com.example.mealplannerapp.fridgefirst.ui.recipes.RecipesViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecipeViewModelsTest : ViewModelTest() {

    @Test
    fun recipeListShowsWhatIsMissingFromInventory() = runTest {
        val c = sampleContainer()
        val vm = RecipesViewModel(c.recipeBookRepository, c.inventoryRepository, c.weeklyPlanRepository, c.kitchen)
        keepCollecting(vm.uiState)

        val byName = vm.uiState.value.recipes.associateBy { it.name }
        assertEquals(emptyList(), byName.getValue("Veggie Omelette").missing)
        assertEquals(listOf("Pasta", "Tomato sauce"), byName.getValue("Pasta Marinara").missing)
        assertEquals(listOf("Chicken"), byName.getValue("Chicken Stir Fry").missing)
    }

    @Test
    fun togglePlannedAddsAndRemovesFromTheWeek() = runTest {
        val c = sampleContainer()
        val vm = RecipesViewModel(c.recipeBookRepository, c.inventoryRepository, c.weeklyPlanRepository, c.kitchen)
        keepCollecting(vm.uiState)
        val id = c.recipeId("Fried Rice")

        vm.togglePlanned(id)
        assertTrue(vm.uiState.value.recipes.single { it.id == id }.isPlanned)
        vm.togglePlanned(id)
        assertFalse(vm.uiState.value.recipes.single { it.id == id }.isPlanned)
    }

    @Test
    fun savingANewRecipeIgnoresBlankRowsAndFinishes() = runTest {
        val c = emptyContainer()
        val vm = RecipeEditorViewModel(recipeId = null, c.recipeBookRepository, c.weeklyPlanRepository)

        vm.onNameChange("  Toast ")
        vm.onServingsChange("2")
        vm.onIngredientChange(0, IngredientDraft("Bread", "2", "slice"))
        vm.addIngredientRow()
        vm.onIngredientChange(1, IngredientDraft("Butter", "1,5", "tbsp"))
        vm.addIngredientRow() // left blank on purpose
        vm.save()

        assertTrue(vm.uiState.value.isFinished)
        val saved = c.recipeBookRepository.observeRecipes().first().single()
        assertEquals("Toast", saved.recipe.name)
        assertEquals(2, saved.recipe.servings)
        assertEquals(listOf("Bread" to 2.0, "Butter" to 1.5), saved.ingredients.map { it.name to it.quantity })
    }

    @Test
    fun invalidRecipeIsNotSaved() = runTest {
        val c = emptyContainer()
        val vm = RecipeEditorViewModel(recipeId = null, c.recipeBookRepository, c.weeklyPlanRepository)

        vm.onServingsChange("0")
        vm.onIngredientChange(0, IngredientDraft("Bread", "", ""))
        vm.save()

        val state = vm.uiState.value
        assertNotNull(state.nameError)
        assertNotNull(state.servingsError)
        assertNotNull(state.ingredientsError)
        assertFalse(state.isFinished)
        assertTrue(c.recipeBookRepository.observeRecipes().first().isEmpty())
    }

    @Test
    fun editingLoadsTheRecipeAndDeleteAlsoRemovesItFromThePlan() = runTest {
        val c = sampleContainer()
        val id = c.recipeId("Fried Rice")
        c.weeklyPlanRepository.add(id)
        val vm = RecipeEditorViewModel(recipeId = id, c.recipeBookRepository, c.weeklyPlanRepository)

        val loaded = vm.uiState.value
        assertFalse(loaded.isNew)
        assertEquals("Fried Rice", loaded.name)
        assertEquals(IngredientDraft("Rice", "2", "cup"), loaded.ingredients.first())

        vm.delete()

        assertTrue(vm.uiState.value.isFinished)
        assertNull(c.recipeBookRepository.observeRecipe(id).first())
        assertTrue(c.weeklyPlanRepository.observePlannedRecipeIds().first().isEmpty())
    }
}
