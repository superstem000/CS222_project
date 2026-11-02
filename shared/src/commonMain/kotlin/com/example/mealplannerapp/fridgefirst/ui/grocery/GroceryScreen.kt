package com.example.mealplannerapp.fridgefirst.ui.grocery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import com.example.mealplannerapp.fridgefirst.ui.components.EmptyState
import com.example.mealplannerapp.fridgefirst.ui.components.FridgeFirstScaffold
import com.example.mealplannerapp.fridgefirst.ui.components.MessageSnackbar
import com.example.mealplannerapp.fridgefirst.ui.components.ScreenContentPadding
import com.example.mealplannerapp.fridgefirst.ui.fridgeFirstViewModel

@Composable
fun GroceryRoute(
    onOpenPlan: () -> Unit,
    viewModel: GroceryViewModel = fridgeFirstViewModel {
        GroceryViewModel(it.recipeBookRepository, it.weeklyPlanRepository, it.inventoryRepository, it.kitchen)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageSnackbar(state.message, snackbarHostState, viewModel::messageShown)

    GroceryScreen(state, snackbarHostState, onCheckOff = viewModel::checkOff, onOpenPlan = onOpenPlan)
}

@Composable
fun GroceryScreen(
    state: GroceryUiState,
    snackbarHostState: SnackbarHostState,
    onCheckOff: (String) -> Unit,
    onOpenPlan: () -> Unit,
) {
    FridgeFirstScaffold(
        title = "Grocery List",
        subtitle = if (state.lines.isEmpty()) null else "${state.lines.size} to buy · only what you're missing",
        snackbarHostState = snackbarHostState,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = ScreenContentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                state.isLoading -> Unit
                state.plannedRecipeCount == 0 -> item(key = "no-plan") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        EmptyState(
                            icon = Icons.Filled.ShoppingCart,
                            title = "Nothing to buy yet",
                            body = "Plan some recipes for the week and the missing ingredients show up here.",
                        )
                        TextButton(onClick = onOpenPlan) { Text("Open Weekly Plan") }
                    }
                }
                state.lines.isEmpty() -> item(key = "all-set") {
                    EmptyState(
                        icon = Icons.Filled.CheckCircle,
                        title = "You're all set",
                        body = "Your inventory covers everything in this week's plan.",
                    )
                }
                else -> item(key = "hint") {
                    Text(
                        "Check an item off when you buy it. It moves into your inventory.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
            items(state.lines, key = { it.key }) { line ->
                Card(
                    onClick = { onCheckOff(line.key) },
                    modifier = Modifier.fillMaxWidth().animateItem(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    ListItem(
                        leadingContent = {
                            Checkbox(checked = false, onCheckedChange = { onCheckOff(line.key) })
                        },
                        headlineContent = { Text(line.name, style = MaterialTheme.typography.titleMedium) },
                        supportingContent = { Text(line.forRecipes) },
                        trailingContent = {
                            Text(line.amount, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    )
                }
            }
        }
    }
}
