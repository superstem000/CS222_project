package com.example.mealplannerapp.fridgefirst.ui

import com.example.mealplannerapp.fridgefirst.ViewModelTest
import com.example.mealplannerapp.fridgefirst.ui.plan.WeeklyPlanViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WeeklyPlanViewModelTest : ViewModelTest() {

    @Test
    fun recommendationsAreRankedByIngredientsOnHand() = runTest {
        val c = sampleContainer()
        val vm = WeeklyPlanViewModel(c.recipeBookRepository, c.weeklyPlanRepository, c.inventoryRepository, c.kitchen)
        keepCollecting(vm.uiState)

        // Sample inventory: Fried Rice 3/3, Veggie Omelette 3/3, Chicken Stir Fry 3/4, Pasta Marinara 1/3.
        assertEquals(
            listOf("Fried Rice", "Veggie Omelette", "Chicken Stir Fry", "Pasta Marinara"),
            vm.uiState.value.recommendations.map { it.name }
        )
    }

    @Test
    fun plannedRecipesLeaveTheRecommendations() = runTest {
        val c = sampleContainer()
        val vm = WeeklyPlanViewModel(c.recipeBookRepository, c.weeklyPlanRepository, c.inventoryRepository, c.kitchen)
        keepCollecting(vm.uiState)

        vm.addToPlan(c.recipeId("Pasta Marinara"))

        val state = vm.uiState.value
        assertEquals(listOf("Pasta Marinara"), state.planned.map { it.name })
        assertEquals(listOf("Pasta", "Tomato sauce"), state.planned.single().missing)
        assertTrue(state.recommendations.none { it.name == "Pasta Marinara" })
    }

    @Test
    fun markingCookedSubtractsIngredientsAndUnplansTheRecipe() = runTest {
        val c = sampleContainer()
        val vm = WeeklyPlanViewModel(c.recipeBookRepository, c.weeklyPlanRepository, c.inventoryRepository, c.kitchen)
        keepCollecting(vm.uiState)
        val omelette = c.recipeId("Veggie Omelette")
        vm.addToPlan(omelette)

        vm.markCooked(omelette) // uses 3 pcs Eggs, 1 cup Spinach, 50 g Cheese

        val inventory = c.inventorySnapshot()
        assertEquals("1.0 pcs", inventory["Eggs"])
        assertEquals(null, inventory["Spinach"], "used-up items are removed")
        assertEquals("50.0 g", inventory["Cheese"])
        assertTrue(vm.uiState.value.planned.isEmpty())
        assertNotNull(vm.uiState.value.message)
    }
}
