package com.example.mealplannerapp.fridgefirst.data.fake

import com.example.mealplannerapp.domain.Inventory
import com.example.mealplannerapp.domain.InventoryData
import com.example.mealplannerapp.fridgefirst.data.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** In-memory [InventoryRepository] wrapping the `Inventory` draft. */
class InMemoryInventoryRepository(initialItems: List<InventoryData> = emptyList()) : InventoryRepository {

    private val inventory = Inventory()
    private val mutex = Mutex()
    private val items = MutableStateFlow<List<InventoryData>>(emptyList())

    init {
        initialItems.forEach { inventory.add(it.name, it.quantity, it.unit) }
        publish()
    }

    override fun observeItems(): Flow<List<InventoryData>> = items.asStateFlow()

    override suspend fun add(name: String, quantity: Double, unit: String): InventoryData =
        mutex.withLock { inventory.add(name, quantity, unit).also { publish() } }

    override suspend fun updateQuantity(id: Int, quantity: Double): Boolean =
        mutex.withLock { inventory.update(id, quantity).also { publish() } }

    override suspend fun remove(id: Int): Boolean =
        mutex.withLock { inventory.remove(id).also { publish() } }

    private fun publish() {
        items.value = inventory.getItemsList()
    }
}
