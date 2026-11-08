package com.example.mealplannerapp.fridgefirst.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealplannerapp.fridgefirst.di.FridgeFirstContainer

val LocalFridgeFirstContainer = staticCompositionLocalOf<FridgeFirstContainer> {
    error("No FridgeFirstContainer provided. Wrap the UI in FridgeFirstApp().")
}

@Composable
inline fun <reified VM : ViewModel> fridgeFirstViewModel(crossinline create: (FridgeFirstContainer) -> VM): VM {
    val container = LocalFridgeFirstContainer.current
    return viewModel { create(container) }
}

/** Keeps the in-memory container across rotation. */
private class FridgeFirstContainerHolder : ViewModel() {
    val container: FridgeFirstContainer = FridgeFirstContainer.inMemory()
}

@Composable
fun rememberInMemoryFridgeFirstContainer(): FridgeFirstContainer =
    viewModel { FridgeFirstContainerHolder() }.container
