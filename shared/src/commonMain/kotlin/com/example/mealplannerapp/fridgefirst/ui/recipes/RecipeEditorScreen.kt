package com.example.mealplannerapp.fridgefirst.ui.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mealplannerapp.fridgefirst.ui.components.FridgeFirstScaffold
import com.example.mealplannerapp.fridgefirst.ui.components.errorSupportingText
import com.example.mealplannerapp.fridgefirst.ui.components.ScreenContentPadding
import com.example.mealplannerapp.fridgefirst.ui.components.SectionHeader
import com.example.mealplannerapp.fridgefirst.ui.fridgeFirstViewModel

@Composable
fun RecipeEditorRoute(
    recipeId: Long?,
    onDone: () -> Unit,
    viewModel: RecipeEditorViewModel = fridgeFirstViewModel {
        RecipeEditorViewModel(recipeId, it.recipeBookRepository, it.weeklyPlanRepository)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onDone()
    }
    RecipeEditorScreen(
        state = state,
        onBack = onDone,
        onNameChange = viewModel::onNameChange,
        onServingsChange = viewModel::onServingsChange,
        onIngredientChange = viewModel::onIngredientChange,
        onAddIngredient = viewModel::addIngredientRow,
        onRemoveIngredient = viewModel::removeIngredientRow,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
    )
}

@Composable
fun RecipeEditorScreen(
    state: RecipeEditorUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onServingsChange: (String) -> Unit,
    onIngredientChange: (Int, IngredientDraft) -> Unit,
    onAddIngredient: () -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    FridgeFirstScaffold(
        title = if (state.isNew) "New recipe" else "Edit recipe",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (!state.isNew) {
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete recipe")
                }
            }
            TextButton(onClick = onSave, enabled = !state.isLoading) { Text("Save") }
        },
    ) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))

        if (state.isLoading) {
            Box(contentModifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            EditorForm(
                state = state,
                onNameChange = onNameChange,
                onServingsChange = onServingsChange,
                onIngredientChange = onIngredientChange,
                onAddIngredient = onAddIngredient,
                onRemoveIngredient = onRemoveIngredient,
                modifier = contentModifier,
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${state.name.ifBlank { "recipe" }}?") },
            text = { Text("It will also be removed from this week's plan.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun EditorForm(
    state: RecipeEditorUiState,
    onNameChange: (String) -> Unit,
    onServingsChange: (String) -> Unit,
    onIngredientChange: (Int, IngredientDraft) -> Unit,
    onAddIngredient: () -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = ScreenContentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "name") {
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("Recipe name") },
                singleLine = true,
                isError = state.nameError != null,
                supportingText = errorSupportingText(state.nameError),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item(key = "servings") {
            OutlinedTextField(
                value = state.servings,
                onValueChange = onServingsChange,
                label = { Text("Servings") },
                singleLine = true,
                isError = state.servingsError != null,
                supportingText = errorSupportingText(state.servingsError),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(0.4f),
            )
        }
        item(key = "ingredients-header") {
            Column {
                SectionHeader("Ingredients", supporting = "Use the same names as your inventory so they match.")
                state.ingredientsError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        itemsIndexed(state.ingredients) { index, draft ->
            IngredientRow(
                draft = draft,
                onChange = { onIngredientChange(index, it) },
                onRemove = { onRemoveIngredient(index) },
            )
        }
        item(key = "add-ingredient") {
            TextButton(onClick = onAddIngredient) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Add ingredient", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun IngredientRow(draft: IngredientDraft, onChange: (IngredientDraft) -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(start = 12.dp, top = 4.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { onChange(draft.copy(name = it)) },
                    label = { Text("Ingredient") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove ingredient")
                }
            }
            Row(Modifier.padding(end = 48.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.quantity,
                    onValueChange = { value -> onChange(draft.copy(quantity = value.filter { it.isDigit() || it == '.' || it == ',' })) },
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = draft.unit,
                    onValueChange = { onChange(draft.copy(unit = it)) },
                    label = { Text("Unit") },
                    placeholder = { Text("g, cup, pcs") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
