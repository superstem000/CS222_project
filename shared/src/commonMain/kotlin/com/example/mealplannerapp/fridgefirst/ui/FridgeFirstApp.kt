package com.example.mealplannerapp.fridgefirst.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mealplannerapp.fridgefirst.di.FridgeFirstContainer
import com.example.mealplannerapp.fridgefirst.ui.grocery.GroceryRoute
import com.example.mealplannerapp.fridgefirst.ui.inventory.InventoryRoute
import com.example.mealplannerapp.fridgefirst.ui.navigation.FridgeFirstRoute
import com.example.mealplannerapp.fridgefirst.ui.navigation.topLevelTabs
import com.example.mealplannerapp.fridgefirst.ui.plan.WeeklyPlanRoute
import com.example.mealplannerapp.fridgefirst.ui.recipes.RecipeEditorRoute
import com.example.mealplannerapp.fridgefirst.ui.recipes.RecipesRoute
import com.example.mealplannerapp.fridgefirst.ui.theme.FridgeFirstTheme

/** Root of the Fridge First UI. */
@Composable
fun FridgeFirstApp(container: FridgeFirstContainer = rememberInMemoryFridgeFirstContainer()) {
    CompositionLocalProvider(LocalFridgeFirstContainer provides container) {
        FridgeFirstTheme {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            val showBottomBar = currentDestination?.hasRoute(FridgeFirstRoute.RecipeEditor::class) != true

            Scaffold(
                bottomBar = { if (showBottomBar) FridgeFirstBottomBar(navController, currentDestination) },
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = FridgeFirstRoute.Inventory,
                    modifier = Modifier.padding(innerPadding),
                ) {
                    composable<FridgeFirstRoute.Inventory> { InventoryRoute() }
                    composable<FridgeFirstRoute.Recipes> {
                        RecipesRoute(
                            onNewRecipe = { navController.navigate(FridgeFirstRoute.RecipeEditor()) },
                            onOpenRecipe = { id -> navController.navigate(FridgeFirstRoute.RecipeEditor(recipeId = id)) },
                        )
                    }
                    composable<FridgeFirstRoute.RecipeEditor> { entry ->
                        val route: FridgeFirstRoute.RecipeEditor = entry.toRoute()
                        RecipeEditorRoute(
                            recipeId = route.recipeId.takeIf { it != FridgeFirstRoute.RecipeEditor.NEW_RECIPE },
                            onDone = { navController.popBackStack() },
                        )
                    }
                    composable<FridgeFirstRoute.WeeklyPlan> {
                        WeeklyPlanRoute(onBrowseRecipes = { navController.navigateToTab(FridgeFirstRoute.Recipes) })
                    }
                    composable<FridgeFirstRoute.Grocery> {
                        GroceryRoute(onOpenPlan = { navController.navigateToTab(FridgeFirstRoute.WeeklyPlan) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FridgeFirstBottomBar(navController: NavHostController, currentDestination: NavDestination?) {
    NavigationBar {
        topLevelTabs.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToTab(tab.route) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.label) },
            )
        }
    }
}

private fun NavHostController.navigateToTab(route: FridgeFirstRoute) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
