package com.example.mealplannerapp.fridgefirst.ui.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mealplannerapp.fridgefirst.ui.components.EmptyState
import com.example.mealplannerapp.fridgefirst.ui.components.FridgeFirstScaffold
import com.example.mealplannerapp.fridgefirst.ui.components.ScreenContentPaddingWithFab
import com.example.mealplannerapp.fridgefirst.ui.fridgeFirstViewModel
import com.example.mealplannerapp.fridgefirst.ui.theme.StockColors

@Composable
fun RecipesRoute(
    onNewRecipe: () -> Unit,
    onOpenRecipe: (Long) -> Unit,
    viewModel: RecipesViewModel = fridgeFirstViewModel {
        RecipesViewModel(it.recipeBookRepository, it.inventoryRepository, it.weeklyPlanRepository, it.kitchen)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RecipesScreen(state, onNewRecipe, onOpenRecipe, viewModel::togglePlanned)
}

@Composable
fun RecipesScreen(
    state: RecipesUiState,
    onNewRecipe: () -> Unit,
    onOpenRecipe: (Long) -> Unit,
    onTogglePlanned: (Long) -> Unit,
) {
    FridgeFirstScaffold(
        title = "Recipes",
        subtitle = if (state.recipes.isEmpty()) null else "${state.recipes.size} saved",
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewRecipe,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New recipe") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = ScreenContentPaddingWithFab,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!state.isLoading && state.recipes.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = "No recipes yet",
                        body = "Tap \"New recipe\" to save your first one.",
                    )
                }
            }
            items(state.recipes, key = { it.id }) { recipe ->
                RecipeCard(recipe, onClick = { onOpenRecipe(recipe.id) }, onTogglePlanned = { onTogglePlanned(recipe.id) })
            }
        }
    }
}

@Composable
private fun RecipeCard(recipe: RecipeCardUi, onClick: () -> Unit, onTogglePlanned: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(recipe.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "Serves ${recipe.servings} · ${recipe.ingredientSummary}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StockStatus(recipe.missing, Modifier.weight(1f))
                FilterChip(
                    selected = recipe.isPlanned,
                    onClick = onTogglePlanned,
                    label = { Text(if (recipe.isPlanned) "This week" else "Add to week") },
                    leadingIcon = if (recipe.isPlanned) {
                        { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

/** "You have everything" in green, or "Missing: …" in red. Shared with the Weekly Plan screen. */
@Composable
fun StockStatus(missing: List<String>, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (missing.isEmpty()) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StockColors.haveIt, modifier = Modifier.size(18.dp))
            Text("You have everything", color = StockColors.haveIt, style = MaterialTheme.typography.labelLarge)
        } else {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = StockColors.missing, modifier = Modifier.size(18.dp))
            Text(
                "Missing: " + missing.joinToString(),
                color = StockColors.missing,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
