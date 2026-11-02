package com.example.mealplannerapp.fridgefirst.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplannerapp.data.local.Ingredient
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.fridgefirst.data.RecipeBookRepository
import com.example.mealplannerapp.fridgefirst.data.WeeklyPlanRepository
import com.example.mealplannerapp.fridgefirst.domain.formatQuantity
import com.example.mealplannerapp.fridgefirst.domain.parseQuantity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IngredientDraft(val name: String = "", val quantity: String = "", val unit: String = "") {
    val isBlank: Boolean get() = name.isBlank() && quantity.isBlank() && unit.isBlank()
}

data class RecipeEditorUiState(
    val isNew: Boolean = true,
    val isLoading: Boolean = false,
    val name: String = "",
    val servings: String = "1",
    val ingredients: List<IngredientDraft> = listOf(IngredientDraft()),
    val nameError: String? = null,
    val servingsError: String? = null,
    val ingredientsError: String? = null,
    /** Set once the recipe was saved or deleted; the screen navigates back. */
    val isFinished: Boolean = false,
)

/**
 * Create or edit one recipe. [recipeId] == null means a new recipe.
 * State is a single MutableStateFlow (no combine) so text fields update synchronously.
 */
class RecipeEditorViewModel(
    private val recipeId: Long?,
    private val recipes: RecipeBookRepository,
    private val plan: WeeklyPlanRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeEditorUiState(isNew = recipeId == null, isLoading = recipeId != null))
    val uiState: StateFlow<RecipeEditorUiState> = _uiState.asStateFlow()

    /** The stored recipe being edited, kept so fields the editor doesn't show (nutrition, tags) survive a save. */
    private var original: Recipe? = null

    init {
        if (recipeId != null) {
            viewModelScope.launch {
                val existing = recipes.observeRecipe(recipeId).first()
                original = existing?.recipe
                _uiState.update { state ->
                    if (existing == null) {
                        state.copy(isLoading = false, isNew = true)
                    } else {
                        state.copy(
                            isLoading = false,
                            name = existing.recipe.name,
                            servings = existing.recipe.servings.toString(),
                            ingredients = existing.ingredients.map {
                                IngredientDraft(it.name, formatQuantity(it.quantity), it.unit)
                            }.ifEmpty { listOf(IngredientDraft()) },
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null) }

    fun onServingsChange(value: String) =
        _uiState.update { it.copy(servings = value.filter(Char::isDigit), servingsError = null) }

    fun onIngredientChange(index: Int, draft: IngredientDraft) = _uiState.update { state ->
        state.copy(
            ingredients = state.ingredients.mapIndexed { i, old -> if (i == index) draft else old },
            ingredientsError = null,
        )
    }

    fun addIngredientRow() = _uiState.update { it.copy(ingredients = it.ingredients + IngredientDraft()) }

    fun removeIngredientRow(index: Int) = _uiState.update { state ->
        val remaining = state.ingredients.filterIndexed { i, _ -> i != index }
        state.copy(ingredients = remaining.ifEmpty { listOf(IngredientDraft()) }, ingredientsError = null)
    }

    fun save() {
        val state = _uiState.value
        val name = state.name.trim()
        val servings = state.servings.toIntOrNull()?.takeIf { it > 0 }
        val filled = state.ingredients.filterNot { it.isBlank }
        val ingredientsError = when {
            filled.isEmpty() -> "Add at least one ingredient"
            filled.any { it.name.isBlank() } -> "Every ingredient needs a name"
            filled.any { parseQuantity(it.quantity) == null } -> "Every ingredient needs an amount above 0"
            else -> null
        }
        if (name.isEmpty() || servings == null || ingredientsError != null) {
            _uiState.update {
                it.copy(
                    nameError = if (name.isEmpty()) "Give the recipe a name" else null,
                    servingsError = if (servings == null) "At least 1" else null,
                    ingredientsError = ingredientsError,
                )
            }
            return
        }

        val recipe = original?.copy(name = name, servings = servings) ?: Recipe(
            name = name,
            servings = servings,
            caloriesPerServing = 0.0,
            proteinGramsPerServing = 0.0,
            carbsGramsPerServing = 0.0,
            fatGramsPerServing = 0.0,
        )
        val ingredients = filled.map {
            Ingredient(recipeId = recipe.id, name = it.name.trim(), quantity = parseQuantity(it.quantity)!!, unit = it.unit.trim())
        }
        viewModelScope.launch {
            recipes.save(recipe, ingredients)
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    /** Deletes the recipe and removes it from the weekly plan. */
    fun delete() {
        val id = original?.id ?: return
        viewModelScope.launch {
            plan.remove(id)
            recipes.delete(id)
            _uiState.update { it.copy(isFinished = true) }
        }
    }
}
