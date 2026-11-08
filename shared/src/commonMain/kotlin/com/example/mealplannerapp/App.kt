package com.example.mealplannerapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.mealplannerapp.di.AppContainer
import com.example.mealplannerapp.di.LocalAppContainer
import com.example.mealplannerapp.fridgefirst.ui.FridgeFirstApp
import com.example.mealplannerapp.fridgefirst.ui.rememberFridgeFirstContainer

@Composable
fun App(appContainer: AppContainer) {
    CompositionLocalProvider(LocalAppContainer provides appContainer) {
        // Fridge First UI: recipes in the Room database, inventory and plan still in memory.
        // The starter UI is kept but unrouted; to show it again, replace this call with
        // `MealPlannerAppTheme { MealPlannerNavGraph() }`.
        FridgeFirstApp(rememberFridgeFirstContainer(appContainer.recipeRepository))
    }
}
