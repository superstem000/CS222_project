package com.example.mealplannerapp.fridgefirst.data

import com.example.mealplannerapp.domain.InventoryData
import com.example.mealplannerapp.fridgefirst.domain.InventoryChange
import com.example.mealplannerapp.fridgefirst.domain.KitchenLogic
import kotlinx.coroutines.flow.first

/**
 * Adds an item to inventory, merging it into an existing row with the same ingredient and unit
 * (used by the Inventory screen's "Add" and by checking off grocery items).
 */
suspend fun InventoryRepository.stock(
    name: String,
    quantity: Double,
    unit: String,
    kitchen: KitchenLogic
): InventoryData {
    val existing = kitchen.mergeTarget(name, unit, observeItems().first())
    return if (existing != null) {
        val merged = existing.quantity + quantity
        updateQuantity(existing.id, merged)
        existing.copy(quantity = merged)
    } else {
        add(name.trim(), quantity, unit.trim())
    }
}

/** Applies the result of [KitchenLogic.cookingChanges]. */
suspend fun InventoryRepository.apply(changes: List<InventoryChange>) {
    for (change in changes) {
        when (change) {
            is InventoryChange.SetQuantity -> updateQuantity(change.id, change.quantity)
            is InventoryChange.Remove -> remove(change.id)
        }
    }
}
