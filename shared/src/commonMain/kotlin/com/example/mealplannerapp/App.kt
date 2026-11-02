package com.example.mealplannerapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.mealplannerapp.di.AppContainer
import com.example.mealplannerapp.di.LocalAppContainer
import com.example.mealplannerapp.fridgefirst.ui.FridgeFirstApp

@Composable
fun App(appContainer: AppContainer) {
    CompositionLocalProvider(LocalAppContainer provides appContainer) {
        // Fridge First UI (Inventory / Recipes / Weekly Plan / Grocery), running on in-memory
        // repositories for now. The starter UI is kept but unrouted; to show it again, replace
        // this call with `MealPlannerAppTheme { MealPlannerNavGraph() }`.
        FridgeFirstApp()
    }
}
