package com.example.mealplannerapp.fridgefirst.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplannerapp.domain.InventoryData
import com.example.mealplannerapp.fridgefirst.data.InventoryRepository
import com.example.mealplannerapp.fridgefirst.data.stock
import com.example.mealplannerapp.fridgefirst.domain.KitchenCalculator
import com.example.mealplannerapp.fridgefirst.domain.formatAmount
import com.example.mealplannerapp.fridgefirst.domain.parseQuantity
import com.example.mealplannerapp.fridgefirst.ui.MessageHolder
import com.example.mealplannerapp.fridgefirst.ui.UserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InventoryItemUi(val id: Int, val name: String, val amount: String)

data class InventoryUiState(
    val items: List<InventoryItemUi> = emptyList(),
    val message: UserMessage? = null,
)

/** The "add item" form. Exposed as its own StateFlow so text fields update synchronously. */
data class InventoryForm(
    val name: String = "",
    val quantity: String = "",
    val unit: String = "",
    val nameError: String? = null,
    val quantityError: String? = null,
)

class InventoryViewModel(
    private val inventory: InventoryRepository,
    private val kitchen: KitchenCalculator,
) : ViewModel() {

    private val messages = MessageHolder()

    private val _form = MutableStateFlow(InventoryForm())
    val form: StateFlow<InventoryForm> = _form.asStateFlow()

    val uiState: StateFlow<InventoryUiState> =
        combine(inventory.observeItems(), messages.current) { items, message ->
            InventoryUiState(
                items = items.sortedBy { it.name.lowercase() }.map { it.toUi() },
                message = message,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InventoryUiState())

    fun onNameChange(value: String) = _form.update { it.copy(name = value, nameError = null) }

    fun onQuantityChange(value: String) =
        _form.update { it.copy(quantity = value.filter { c -> c.isDigit() || c == '.' || c == ',' }, quantityError = null) }

    fun onUnitChange(value: String) = _form.update { it.copy(unit = value) }

    /** Validates the form; on success adds (or merges) the item and clears the form. */
    fun addItem() {
        val current = _form.value
        val name = current.name.trim()
        val quantity = parseQuantity(current.quantity)
        if (name.isEmpty() || quantity == null) {
            _form.update {
                it.copy(
                    nameError = if (name.isEmpty()) "Enter an item name" else null,
                    quantityError = if (quantity == null) "Enter an amount above 0" else null,
                )
            }
            return
        }
        _form.value = InventoryForm()
        viewModelScope.launch {
            val saved = inventory.stock(name, quantity, current.unit, kitchen)
            messages.show("$name: now ${formatAmount(saved.quantity, saved.unit)}")
        }
    }

    fun removeItem(id: Int) {
        viewModelScope.launch {
            val item = inventory.observeItems().first().firstOrNull { it.id == id } ?: return@launch
            if (inventory.remove(id)) messages.show("Removed ${item.name}")
        }
    }

    fun messageShown(id: Long) = messages.shown(id)

    private fun InventoryData.toUi() = InventoryItemUi(id = id, name = name, amount = formatAmount(quantity, unit))
}
