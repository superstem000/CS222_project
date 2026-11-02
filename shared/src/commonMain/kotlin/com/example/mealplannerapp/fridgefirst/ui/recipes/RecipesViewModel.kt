package com.example.mealplannerapp.fridgefirst.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplannerapp.fridgefirst.data.InventoryRepository
import com.example.mealplannerapp.fridgefirst.data.RecipeBookRepository
import com.example.mealplannerapp.fridgefirst.data.WeeklyPlanRepository
import com.example.mealplannerapp.fridgefirst.domain.KitchenLogic
import com.example.mealplannerapp.fridgefirst.domain.formatAmount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeCardUi(
    val id: Long,
    val name: String,
    val servings: Int,
    val ingredientSummary: String,
    val missing: List<String>,
    val isPlanned: Boolean,
)

data class RecipesUiState(
    val recipes: List<RecipeCardUi> = emptyList(),
    val isLoading: Boolean = true,
)

class RecipesViewModel(
    recipes: RecipeBookRepository,
    inventory: InventoryRepository,
    private val plan: WeeklyPlanRepository,
    kitchen: KitchenLogic,
) : ViewModel() {

    val uiState: StateFlow<RecipesUiState> = combine(
        recipes.observeRecipes(),
        inventory.observeItems(),
        plan.observePlannedRecipeIds(),
    ) { all, items, plannedIds ->
        RecipesUiState(
            recipes = all.map { entry ->
                RecipeCardUi(
                    id = entry.recipe.id,
                    name = entry.recipe.name,
                    servings = entry.recipe.servings,
                    ingredientSummary = entry.ingredients.joinToString { "${formatAmount(it.quantity, it.unit)} ${it.name}" },
                    missing = kitchen.missingFor(entry, items).map { it.displayName },
                    isPlanned = entry.recipe.id in plannedIds,
                )
            },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipesUiState())

    fun togglePlanned(recipeId: Long) {
        viewModelScope.launch {
            if (recipeId in plan.observePlannedRecipeIds().first()) plan.remove(recipeId) else plan.add(recipeId)
        }
    }
}
