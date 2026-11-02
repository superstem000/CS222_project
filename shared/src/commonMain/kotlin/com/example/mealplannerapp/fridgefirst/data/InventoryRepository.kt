package com.example.mealplannerapp.fridgefirst.data

import com.example.mealplannerapp.domain.InventoryData
import kotlinx.coroutines.flow.Flow

/** Inventory storage used by the UI. */
interface InventoryRepository {
    fun observeItems(): Flow<List<InventoryData>>

    suspend fun add(name: String, quantity: Double, unit: String): InventoryData

    suspend fun updateQuantity(id: Int, quantity: Double): Boolean

    suspend fun remove(id: Int): Boolean
}
