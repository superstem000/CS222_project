package com.example.mealplannerapp.fridgefirst.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/** Type-safe Navigation Compose routes for Fridge First. */
@Serializable
sealed interface FridgeFirstRoute {
    @Serializable data object Inventory : FridgeFirstRoute
    @Serializable data object Recipes : FridgeFirstRoute
    @Serializable data object WeeklyPlan : FridgeFirstRoute
    @Serializable data object Grocery : FridgeFirstRoute

    /** [recipeId] == [NEW_RECIPE] creates a new recipe. (Non-null so it works with every NavType.) */
    @Serializable data class RecipeEditor(val recipeId: Long = NEW_RECIPE) : FridgeFirstRoute {
        companion object {
            const val NEW_RECIPE = -1L
        }
    }
}

data class TopLevelTab(val route: FridgeFirstRoute, val label: String, val icon: ImageVector)

val topLevelTabs = listOf(
    TopLevelTab(FridgeFirstRoute.Inventory, "Inventory", Icons.Filled.Kitchen),
    TopLevelTab(FridgeFirstRoute.Recipes, "Recipes", Icons.AutoMirrored.Filled.MenuBook),
    TopLevelTab(FridgeFirstRoute.WeeklyPlan, "Weekly Plan", Icons.Filled.CalendarMonth),
    TopLevelTab(FridgeFirstRoute.Grocery, "Grocery", Icons.Filled.ShoppingCart),
)
