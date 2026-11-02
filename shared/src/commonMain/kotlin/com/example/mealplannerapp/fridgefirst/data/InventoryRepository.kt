package com.example.mealplannerapp.fridgefirst.data

import com.example.mealplannerapp.domain.InventoryData
import kotlinx.coroutines.flow.Flow

/**
 * What the UI needs from inventory storage. Uses Andrew's [InventoryData] model as-is.
 *
 * Today this is backed by [com.example.mealplannerapp.fridgefirst.data.fake.InMemoryInventoryRepository]
 * (which wraps Andrew's `Inventory` draft). When the inventory table lands in the database,
 * add a database-backed implementation of this interface and swap it in
 * [com.example.mealplannerapp.fridgefirst.di.FridgeFirstContainer] — screen code does not change.
 */
interface InventoryRepository {
    /** Emits the full inventory every time it changes. */
    fun observeItems(): Flow<List<InventoryData>>

    /** Adds a new row. Merging with an existing row is a UI decision and happens before this call. */
    suspend fun add(name: String, quantity: Double, unit: String): InventoryData

    /** Returns false if no item has this id. */
    suspend fun updateQuantity(id: Int, quantity: Double): Boolean

    /** Returns false if no item has this id. */
    suspend fun remove(id: Int): Boolean
}
