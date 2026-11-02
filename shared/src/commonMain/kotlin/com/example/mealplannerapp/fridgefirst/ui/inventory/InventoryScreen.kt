package com.example.mealplannerapp.fridgefirst.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mealplannerapp.fridgefirst.ui.components.EmptyState
import com.example.mealplannerapp.fridgefirst.ui.components.errorSupportingText
import com.example.mealplannerapp.fridgefirst.ui.components.FridgeFirstScaffold
import com.example.mealplannerapp.fridgefirst.ui.components.MessageSnackbar
import com.example.mealplannerapp.fridgefirst.ui.components.ScreenContentPadding
import com.example.mealplannerapp.fridgefirst.ui.components.SectionHeader
import com.example.mealplannerapp.fridgefirst.ui.fridgeFirstViewModel

@Composable
fun InventoryRoute(
    viewModel: InventoryViewModel = fridgeFirstViewModel { InventoryViewModel(it.inventoryRepository, it.kitchen) }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageSnackbar(state.message, snackbarHostState, viewModel::messageShown)

    InventoryScreen(
        state = state,
        form = form,
        snackbarHostState = snackbarHostState,
        onNameChange = viewModel::onNameChange,
        onQuantityChange = viewModel::onQuantityChange,
        onUnitChange = viewModel::onUnitChange,
        onAdd = viewModel::addItem,
        onRemove = viewModel::removeItem,
    )
}

@Composable
fun InventoryScreen(
    state: InventoryUiState,
    form: InventoryForm,
    snackbarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
) {
    FridgeFirstScaffold(
        title = "Inventory",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} items on hand",
        snackbarHostState = snackbarHostState,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = ScreenContentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "form") {
                AddItemCard(form, onNameChange, onQuantityChange, onUnitChange, onAdd)
            }
            item(key = "header") {
                SectionHeader("On hand", supporting = "Adding an item you already have adds to its amount.")
            }
            if (state.items.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Filled.Kitchen,
                        title = "Your fridge is empty",
                        body = "Add what you have above, or check items off your grocery list.",
                    )
                }
            }
            items(state.items, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().animateItem(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    ListItem(
                        headlineContent = { Text(item.name, style = MaterialTheme.typography.titleMedium) },
                        supportingContent = { Text(item.amount) },
                        trailingContent = {
                            IconButton(onClick = { onRemove(item.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove ${item.name}")
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddItemCard(
    form: InventoryForm,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Add to inventory", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = form.name,
                onValueChange = onNameChange,
                label = { Text("Item") },
                placeholder = { Text("e.g. Eggs") },
                singleLine = true,
                isError = form.nameError != null,
                supportingText = errorSupportingText(form.nameError),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                OutlinedTextField(
                    value = form.quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Amount") },
                    singleLine = true,
                    isError = form.quantityError != null,
                    supportingText = errorSupportingText(form.quantityError),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = form.unit,
                    onValueChange = onUnitChange,
                    label = { Text("Unit") },
                    placeholder = { Text("g, cup, pcs") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onAdd() }),
                    modifier = Modifier.weight(1f),
                )
            }
            Button(onClick = onAdd, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Add", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
