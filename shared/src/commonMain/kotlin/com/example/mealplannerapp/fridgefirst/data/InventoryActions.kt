package com.example.mealplannerapp.fridgefirst.data

import com.example.mealplannerapp.domain.InventoryData
import com.example.mealplannerapp.fridgefirst.domain.InventoryChange
import com.example.mealplannerapp.fridgefirst.domain.KitchenLogic
import kotlinx.coroutines.flow.first

/** Adds an item, merging into an existing row with the same name and unit. */
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

suspend fun InventoryRepository.apply(changes: List<InventoryChange>) {
    for (change in changes) {
        when (change) {
            is InventoryChange.SetQuantity -> updateQuantity(change.id, change.quantity)
            is InventoryChange.Remove -> remove(change.id)
        }
    }
}
