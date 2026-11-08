package com.example.mealplannerapp.fridgefirst.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealplannerapp.data.repository.RecipeRepository
import com.example.mealplannerapp.fridgefirst.data.fake.SampleData
import com.example.mealplannerapp.fridgefirst.di.FridgeFirstContainer
import kotlinx.coroutines.launch

val LocalFridgeFirstContainer = staticCompositionLocalOf<FridgeFirstContainer> {
    error("No FridgeFirstContainer provided. Wrap the UI in FridgeFirstApp().")
}

@Composable
inline fun <reified VM : ViewModel> fridgeFirstViewModel(crossinline create: (FridgeFirstContainer) -> VM): VM {
    val container = LocalFridgeFirstContainer.current
    return viewModel { create(container) }
}

/** Keeps the container (and its in-memory data) across rotation. */
private class FridgeFirstContainerHolder(recipeRepository: RecipeRepository) : ViewModel() {
    val container: FridgeFirstContainer = FridgeFirstContainer.withRoomRecipes(recipeRepository)

    init {
        viewModelScope.launch { SampleData.seedRecipesIfEmpty(container.recipeBookRepository) }
    }
}

@Composable
fun rememberFridgeFirstContainer(recipeRepository: RecipeRepository): FridgeFirstContainer =
    viewModel { FridgeFirstContainerHolder(recipeRepository) }.container
