package com.example.mealplannerapp.fridgefirst

import com.example.mealplannerapp.domain.InventoryData
import com.example.mealplannerapp.fridgefirst.data.fake.InMemoryInventoryRepository
import com.example.mealplannerapp.fridgefirst.data.fake.InMemoryRecipeBookRepository
import com.example.mealplannerapp.fridgefirst.data.fake.InMemoryWeeklyPlanRepository
import com.example.mealplannerapp.fridgefirst.di.FridgeFirstContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Base class for ViewModel tests: viewModelScope runs on Dispatchers.Main, so Main is replaced
 * with an unconfined test dispatcher. Work launched by the ViewModel then runs immediately.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class ViewModelTest {
    @BeforeTest
    fun setUpMainDispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDownMainDispatcher() = Dispatchers.resetMain()

    /** Fresh in-memory repositories seeded with the prototype's sample data. */
    fun sampleContainer(): FridgeFirstContainer = FridgeFirstContainer.inMemory(withSampleData = true)

    fun emptyContainer(inventory: List<InventoryData> = emptyList()) = FridgeFirstContainer(
        inventoryRepository = InMemoryInventoryRepository(inventory),
        recipeBookRepository = InMemoryRecipeBookRepository(),
        weeklyPlanRepository = InMemoryWeeklyPlanRepository(),
    )

    /** UI state uses WhileSubscribed, so a test must subscribe for it to update. */
    fun TestScope.keepCollecting(flow: Flow<*>) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect {} }
    }

    suspend fun FridgeFirstContainer.inventorySnapshot(): Map<String, String> =
        inventoryRepository.observeItems().first().associate { it.name to "${it.quantity} ${it.unit}" }

    suspend fun FridgeFirstContainer.recipeId(name: String): Long =
        recipeBookRepository.observeRecipes().first().first { it.recipe.name == name }.recipe.id
}

