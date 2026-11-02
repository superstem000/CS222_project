// Credits: Andrew; GPT-6.1 Sol and ChatGPT 6 (answering questions on Kotlin, giving suggestions,
//                                             and checking code)

package com.example.mealplannerapp.domain

// data of inventory items
data class InventoryData(val id: Int, val name: String, val quantity: Double, val unit: String)

class Inventory {
    // inventory properties
    val items_map = mutableMapOf<Int, InventoryData>()
    var nextID = 1

    // add item to the inventory
    fun add(name: String, quantity: Double, unit: String): InventoryData {
        val item = InventoryData(nextID, name.trim(), quantity, unit.trim())
        items_map[item.id] = item
        nextID++
        return item
    }

    // get an item from inventory based on id
    fun get(id: Int): InventoryData? {
        return items_map[id]
    }

    // update quantity of an item, identified by id
    fun update(id: Int, new_quantity: Double): Boolean {
        val item = items_map[id]
        if (item == null) {
            return false
        }
        items_map[id] = item.copy(quantity = new_quantity)
        return true
    }

    // remove item from inventory based on id
    fun remove(id: Int): Boolean {
        var removed = items_map.remove(id)
        return removed != null
    }

    // get list of all inventory items
    fun getItemsList(): List<InventoryData> {
        return items_map.values.toList()
    }
}

// main() function for testing
fun main() {
    val inventory = Inventory()
    val test_item = inventory.add("Test Item 1", 6.7, "original unit")
    // list all inventory items
    println(inventory.getItemsList())
    // get item with id 1
    println(inventory.get(1))

    val test_item2 = inventory.add("Test Item 2", 1.0, "original unitz")
    println(inventory.getItemsList())
    println(inventory.get(2))

    // remove Test Item 1
    inventory.remove(1)
    println(inventory.getItemsList())

    // update Test Item 2's quantity to 67
    inventory.update(2, 67.0)
    println(inventory.getItemsList())
}