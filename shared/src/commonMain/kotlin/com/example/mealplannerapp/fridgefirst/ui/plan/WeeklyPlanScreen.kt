package com.example.mealplannerapp.fridgefirst.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mealplannerapp.fridgefirst.domain.Coverage
import com.example.mealplannerapp.fridgefirst.ui.components.EmptyState
import com.example.mealplannerapp.fridgefirst.ui.components.FridgeFirstScaffold
import com.example.mealplannerapp.fridgefirst.ui.components.MessageSnackbar
import com.example.mealplannerapp.fridgefirst.ui.components.ScreenContentPadding
import com.example.mealplannerapp.fridgefirst.ui.components.SectionHeader
import com.example.mealplannerapp.fridgefirst.ui.fridgeFirstViewModel
import com.example.mealplannerapp.fridgefirst.ui.recipes.StockStatus
import com.example.mealplannerapp.fridgefirst.ui.theme.StockColors

@Composable
fun WeeklyPlanRoute(
    onBrowseRecipes: () -> Unit,
    viewModel: WeeklyPlanViewModel = fridgeFirstViewModel {
        WeeklyPlanViewModel(it.recipeBookRepository, it.weeklyPlanRepository, it.inventoryRepository, it.kitchen)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageSnackbar(state.message, snackbarHostState, viewModel::messageShown)

    WeeklyPlanScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAdd = viewModel::addToPlan,
        onRemove = viewModel::removeFromPlan,
        onCooked = viewModel::markCooked,
        onBrowseRecipes = onBrowseRecipes,
    )
}

@Composable
fun WeeklyPlanScreen(
    state: WeeklyPlanUiState,
    snackbarHostState: SnackbarHostState,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onCooked: (Long) -> Unit,
    onBrowseRecipes: () -> Unit,
) {
    FridgeFirstScaffold(
        title = "Weekly Plan",
        subtitle = when (val count = state.planned.size) {
            0 -> null
            1 -> "1 recipe this week"
            else -> "$count recipes this week"
        },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = ScreenContentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "planned-header") { SectionHeader("This week") }
            if (!state.isLoading && state.planned.isEmpty()) {
                item(key = "planned-empty") {
                    EmptyState(
                        icon = Icons.Filled.CalendarMonth,
                        title = "Nothing planned yet",
                        body = "Pick a recipe from the recommendations below. Your grocery list fills in automatically.",
                    )
                }
            }
            items(state.planned, key = { "planned-${it.id}" }) { planned ->
                PlannedRecipeCard(planned, onCooked = { onCooked(planned.id) }, onRemove = { onRemove(planned.id) })
            }

            item(key = "recommended-header") {
                SectionHeader("Recommended", supporting = "Ranked by how many ingredients you already have")
            }
            if (!state.isLoading && state.recommendations.isEmpty()) {
                item(key = "recommended-empty") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            if (state.hasAnyRecipes) "Every saved recipe is already planned." else "Save some recipes to get recommendations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = onBrowseRecipes) { Text("Go to Recipes") }
                    }
                }
            }
            items(state.recommendations, key = { "recommended-${it.id}" }) { recommendation ->
                RecommendationCard(recommendation, onAdd = { onAdd(recommendation.id) })
            }
        }
    }
}

@Composable
private fun PlannedRecipeCard(planned: PlannedRecipeUi, onCooked: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    planned.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove ${planned.name} from this week")
                }
            }
            StockStatus(planned.missing)
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = onCooked) {
                Icon(Icons.Filled.Restaurant, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Mark cooked")
            }
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: RecommendationUi, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(recommendation.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    coverageLabel(recommendation.coverage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { recommendation.coverage.fraction },
                    color = coverageColor(recommendation.coverage),
                    modifier = Modifier.fillMaxWidth(0.8f),
                )
            }
            OutlinedButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add")
            }
        }
    }
}

private fun coverageLabel(coverage: Coverage): String =
    if (coverage.missing == 0) "You have all ${coverage.total} ingredients"
    else "Have ${coverage.have} of ${coverage.total} ingredients"

@Composable
private fun coverageColor(coverage: Coverage) = when {
    coverage.missing == 0 -> StockColors.haveIt
    coverage.have == 0 -> StockColors.missing
    else -> StockColors.partial
}
