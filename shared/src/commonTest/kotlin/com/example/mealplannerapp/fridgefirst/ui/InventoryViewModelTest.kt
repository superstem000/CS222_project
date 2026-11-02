package com.example.mealplannerapp.fridgefirst.ui

import com.example.mealplannerapp.fridgefirst.ViewModelTest
import com.example.mealplannerapp.fridgefirst.ui.inventory.InventoryViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InventoryViewModelTest : ViewModelTest() {

    @Test
    fun addingAnItemYouAlreadyHaveMergesIntoTheExistingRow() = runTest {
        val container = sampleContainer()
        val vm = InventoryViewModel(container.inventoryRepository, container.kitchen)
        keepCollecting(vm.uiState)
        val before = vm.uiState.value.items.size

        vm.onNameChange("egg")
        vm.onQuantityChange("2")
        vm.onUnitChange("pcs")
        vm.addItem()

        assertEquals(before, vm.uiState.value.items.size)
        assertEquals("6 pcs", vm.uiState.value.items.single { it.name == "Eggs" }.amount)
        assertEquals("", vm.form.value.name, "form is cleared after adding")
        assertNotNull(vm.uiState.value.message)
    }

    @Test
    fun newItemsAreAddedAndListedAlphabetically() = runTest {
        val container = emptyContainer()
        val vm = InventoryViewModel(container.inventoryRepository, container.kitchen)
        keepCollecting(vm.uiState)

        listOf("Milk" to "1", "Apples" to "1.5").forEach { (name, qty) ->
            vm.onNameChange(name)
            vm.onQuantityChange(qty)
            vm.onUnitChange("kg")
            vm.addItem()
        }

        assertEquals(listOf("Apples", "Milk"), vm.uiState.value.items.map { it.name })
        assertEquals("1.5 kg", vm.uiState.value.items.first().amount)
    }

    @Test
    fun invalidInputShowsErrorsAndAddsNothing() = runTest {
        val container = emptyContainer()
        val vm = InventoryViewModel(container.inventoryRepository, container.kitchen)
        keepCollecting(vm.uiState)

        vm.onNameChange("   ")
        vm.onQuantityChange("0")
        vm.addItem()

        assertNotNull(vm.form.value.nameError)
        assertNotNull(vm.form.value.quantityError)
        assertTrue(vm.uiState.value.items.isEmpty())

        vm.onNameChange("Milk")
        assertNull(vm.form.value.nameError, "typing clears the error")
    }

    @Test
    fun removeDeletesTheItem() = runTest {
        val container = sampleContainer()
        val vm = InventoryViewModel(container.inventoryRepository, container.kitchen)
        keepCollecting(vm.uiState)
        val rice = vm.uiState.value.items.single { it.name == "Rice" }

        vm.removeItem(rice.id)

        assertTrue(vm.uiState.value.items.none { it.name == "Rice" })
        assertEquals("Removed Rice", vm.uiState.value.message?.text)
    }
}
