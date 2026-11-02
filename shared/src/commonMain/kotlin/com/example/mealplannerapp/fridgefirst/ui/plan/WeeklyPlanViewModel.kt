package com.example.mealplannerapp.fridgefirst.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplannerapp.fridgefirst.data.InventoryRepository
import com.example.mealplannerapp.fridgefirst.data.RecipeBookRepository
import com.example.mealplannerapp.fridgefirst.data.WeeklyPlanRepository
import com.example.mealplannerapp.fridgefirst.data.apply
import com.example.mealplannerapp.fridgefirst.domain.Coverage
import com.example.mealplannerapp.fridgefirst.domain.KitchenCalculator
import com.example.mealplannerapp.fridgefirst.ui.MessageHolder
import com.example.mealplannerapp.fridgefirst.ui.UserMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlannedRecipeUi(
    val id: Long,
    val name: String,
    val coverage: Coverage,
    /** Names of ingredients still missing; empty means ready to cook. */
    val missing: List<String>,
)

data class RecommendationUi(val id: Long, val name: String, val coverage: Coverage)

data class WeeklyPlanUiState(
    val planned: List<PlannedRecipeUi> = emptyList(),
    /** Unplanned recipes, best match with current inventory first. */
    val recommendations: List<RecommendationUi> = emptyList(),
    val hasAnyRecipes: Boolean = false,
    val isLoading: Boolean = true,
    val message: UserMessage? = null,
)

class WeeklyPlanViewModel(
    private val recipes: RecipeBookRepository,
    private val plan: WeeklyPlanRepository,
    private val inventory: InventoryRepository,
    private val kitchen: KitchenCalculator,
) : ViewModel() {

    private val messages = MessageHolder()

    val uiState: StateFlow<WeeklyPlanUiState> = combine(
        recipes.observeRecipes(),
        plan.observePlannedRecipeIds(),
        inventory.observeItems(),
        messages.current,
    ) { all, plannedIds, items, message ->
        val byId = all.associateBy { it.recipe.id }
        WeeklyPlanUiState(
            planned = plannedIds.mapNotNull { byId[it] }.map { entry ->
                PlannedRecipeUi(
                    id = entry.recipe.id,
                    name = entry.recipe.name,
                    coverage = kitchen.coverage(entry, items),
                    missing = kitchen.missingFor(entry, items).map { it.displayName },
                )
            },
            recommendations = all
                .filter { it.recipe.id !in plannedIds }
                .map { RecommendationUi(it.recipe.id, it.recipe.name, kitchen.coverage(it, items)) }
                .sortedWith(
                    compareByDescending<RecommendationUi> { it.coverage.have }
                        .thenBy { it.coverage.missing }
                        .thenBy { it.name.lowercase() }
                ),
            hasAnyRecipes = all.isNotEmpty(),
            isLoading = false,
            message = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeeklyPlanUiState())

    fun addToPlan(recipeId: Long) {
        viewModelScope.launch { plan.add(recipeId) }
    }

    fun removeFromPlan(recipeId: Long) {
        viewModelScope.launch { plan.remove(recipeId) }
    }

    /** Subtracts the recipe's ingredients from inventory and takes it off this week's plan. */
    fun markCooked(recipeId: Long) {
        viewModelScope.launch {
            val recipe = recipes.observeRecipe(recipeId).first() ?: return@launch
            val items = inventory.observeItems().first()
            inventory.apply(kitchen.cookingChanges(recipe, items))
            plan.remove(recipeId)
            messages.show("Cooked ${recipe.recipe.name}. Used ingredients were taken out of inventory.")
        }
    }

    fun messageShown(id: Long) = messages.shown(id)
}
