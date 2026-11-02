package com.example.mealplannerapp.fridgefirst.ui

import com.example.mealplannerapp.fridgefirst.ViewModelTest
import com.example.mealplannerapp.fridgefirst.di.FridgeFirstContainer
import com.example.mealplannerapp.fridgefirst.ui.grocery.GroceryViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GroceryViewModelTest : ViewModelTest() {

    private fun FridgeFirstContainer.groceryViewModel() =
        GroceryViewModel(recipeBookRepository, weeklyPlanRepository, inventoryRepository, kitchen)

    @Test
    fun listContainsOnlyMissingIngredientsWithDuplicatesMerged() = runTest {
        val c = sampleContainer()
        val vm = c.groceryViewModel()
        keepCollecting(vm.uiState)
        listOf("Chicken Stir Fry", "Fried Rice", "Veggie Omelette").forEach { c.weeklyPlanRepository.add(c.recipeId(it)) }

        // Needed: Chicken 300 g, Rice 1+2 cup, Spinach 1+1 cup, Soy sauce 2+1 tbsp, Eggs 2+3 pcs, Cheese 50 g.
        // On hand: Rice 3 cup, Spinach 1 cup, Soy sauce 5 tbsp, Eggs 4 pcs, Cheese 100 g.
        val lines = vm.uiState.value.lines
        assertEquals(listOf("Chicken" to "300 g", "Eggs" to "1 pcs", "Spinach" to "1 cup"), lines.map { it.name to it.amount })
        assertEquals("For Chicken Stir Fry, Veggie Omelette", lines.single { it.name == "Spinach" }.forRecipes)
        assertEquals(3, vm.uiState.value.plannedRecipeCount)
    }

    @Test
    fun checkingOffAnItemMovesItIntoInventory() = runTest {
        val c = sampleContainer()
        val vm = c.groceryViewModel()
        keepCollecting(vm.uiState)
        c.weeklyPlanRepository.add(c.recipeId("Chicken Stir Fry"))
        val chicken = vm.uiState.value.lines.single { it.name == "Chicken" }

        vm.checkOff(chicken.key)

        assertTrue(vm.uiState.value.lines.isEmpty(), "everything for the plan is now on hand")
        assertEquals("300.0 g", c.inventorySnapshot()["Chicken"])
        assertEquals("Moved 300 g Chicken to inventory", vm.uiState.value.message?.text)
    }

    @Test
    fun emptyPlanMeansNothingToBuy() = runTest {
        val vm = sampleContainer().groceryViewModel()
        keepCollecting(vm.uiState)

        assertEquals(0, vm.uiState.value.plannedRecipeCount)
        assertTrue(vm.uiState.value.lines.isEmpty())
    }
}
