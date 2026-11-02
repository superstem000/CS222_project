package com.example.mealplannerapp.fridgefirst.ui.grocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplannerapp.fridgefirst.data.InventoryRepository
import com.example.mealplannerapp.fridgefirst.data.RecipeBookRepository
import com.example.mealplannerapp.fridgefirst.data.WeeklyPlanRepository
import com.example.mealplannerapp.fridgefirst.data.plannedRecipes
import com.example.mealplannerapp.fridgefirst.data.stock
import com.example.mealplannerapp.fridgefirst.domain.KitchenLogic
import com.example.mealplannerapp.fridgefirst.domain.formatAmount
import com.example.mealplannerapp.fridgefirst.ui.MessageHolder
import com.example.mealplannerapp.fridgefirst.ui.UserMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroceryLineUi(
    val key: String,
    val name: String,
    val amount: String,
    val forRecipes: String,
)

data class GroceryUiState(
    val lines: List<GroceryLineUi> = emptyList(),
    val plannedRecipeCount: Int = 0,
    val isLoading: Boolean = true,
    val message: UserMessage? = null,
)

class GroceryViewModel(
    private val recipes: RecipeBookRepository,
    private val plan: WeeklyPlanRepository,
    private val inventory: InventoryRepository,
    private val kitchen: KitchenLogic,
) : ViewModel() {

    private val messages = MessageHolder()
    private val planned = plannedRecipes(plan, recipes)

    val uiState: StateFlow<GroceryUiState> =
        combine(planned, inventory.observeItems(), messages.current) { plannedRecipes, items, message ->
            GroceryUiState(
                lines = kitchen.groceryList(plannedRecipes, items).map {
                    GroceryLineUi(
                        key = it.key,
                        name = it.displayName,
                        amount = formatAmount(it.totalQuantity, it.unit),
                        forRecipes = "For " + it.sourceRecipeNames.joinToString(),
                    )
                },
                plannedRecipeCount = plannedRecipes.size,
                isLoading = false,
                message = message,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GroceryUiState())

    fun checkOff(key: String) {
        viewModelScope.launch {
            val item = kitchen.groceryList(planned.first(), inventory.observeItems().first())
                .firstOrNull { it.key == key } ?: return@launch
            inventory.stock(item.displayName, item.totalQuantity, item.unit, kitchen)
            messages.show("Moved ${formatAmount(item.totalQuantity, item.unit)} ${item.displayName} to inventory")
        }
    }

    fun messageShown(id: Long) = messages.shown(id)
}
