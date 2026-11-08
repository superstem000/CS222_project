// Credits: Andrew; GPT-6.1 Sol and ChatGPT 6 (answering questions on Kotlin, giving suggestions,
//                                             and checking code)

package com.example.mealplannerapp.data.local

import androidx.room3.*

@Entity
data class InventoryEntity(@PrimaryKey(autoGenerate = true) val id: Int = 0,
                           val name: String, val quantity: Double, val unit: String)

@Dao
interface InventoryDao {
    // get all inventory items sorted by id
    @Query("SELECT * FROM InventoryEntity ORDER by id")
    suspend fun getAll(): List<InventoryEntity>

    // get an inventory item by id
    @Query("SELECT * FROM InventoryEntity WHERE id = :id")
    suspend fun get(id: Int): InventoryEntity?

    // insert an item to the inventory
    @Insert
    suspend fun insert(item: InventoryEntity): Long

    // delete inventory item by id, returning 1 (removed) or 0 (not found)
    @Query("DELETE FROM InventoryEntity WHERE id = :id")
    suspend fun remove(id: Int): Int

    // update inventory item's quantity, returning 1 (updated) or 0 (not found)
    @Query("UPDATE InventoryEntity SET quantity = :new WHERE id = :id")
    suspend fun update(id: Int, new: Double): Int
}