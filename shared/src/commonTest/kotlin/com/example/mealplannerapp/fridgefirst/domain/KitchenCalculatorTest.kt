package com.example.mealplannerapp.fridgefirst.domain

import com.example.mealplannerapp.fridgefirst.data.fake.SampleData.ingredient
import com.example.mealplannerapp.fridgefirst.data.fake.SampleData.item
import com.example.mealplannerapp.fridgefirst.data.fake.SampleData.recipe
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Cases 1–7 come from docs/ingredient-matching-plan.md §4. */
class KitchenCalculatorTest {

    private val kitchen = KitchenCalculator(NormalizingIngredientMatcher())

    private fun inventory(vararg rows: Triple<String, Double, String>) =
        rows.mapIndexed { i, (name, qty, unit) -> item(name, qty, unit).copy(id = i + 1) }

    private fun assertClose(expected: Double, actual: Double) =
        assertTrue(abs(expected - actual) < 1e-3, "expected $expected but was $actual")

    @Test
    fun case1_enoughOnHand() {
        val omelette = recipe("Omelette", 1, ingredient("egg", 2.0, "piece"))
        assertTrue(kitchen.missingFor(omelette, inventory(Triple("egg", 6.0, "piece"))).isEmpty())
    }

    @Test
    fun case2_pluralAndCaseStillMatch() {
        val omelette = recipe("Omelette", 1, ingredient("Eggs", 2.0, "pcs"))
        assertTrue(kitchen.missingFor(omelette, inventory(Triple("egg", 6.0, ""))).isEmpty())
    }

    @Test
    fun case3_sameCategoryIsConverted() {
        val pancakes = recipe("Pancakes", 2, ingredient("Milk", 2.0, "cup"))
        val missing = kitchen.missingFor(pancakes, inventory(Triple("milk", 250.0, "mL"))).single()
        assertEquals("cup", missing.unit)
        assertClose((473.176 - 250.0) / 236.588, missing.totalQuantity)
    }

    @Test
    fun case4_poundsVersusGrams() {
        val dinner = recipe("Dinner", 2, ingredient("Chicken", 1.0, "lb"))
        assertTrue(kitchen.missingFor(dinner, inventory(Triple("Chicken", 500.0, "g"))).isEmpty())
    }

    @Test
    fun case5_differentCategoriesCountAsMissing() {
        val bread = recipe("Bread", 1, ingredient("Flour", 2.0, "cup"))
        val missing = kitchen.missingFor(bread, inventory(Triple("Flour", 1.0, "kg"))).single()
        assertEquals(2.0, missing.totalQuantity)
    }

    @Test
    fun case6_notInInventoryAtAll() {
        val bread = recipe("Bread", 1, ingredient("Yeast", 1.0, "tsp"))
        assertEquals(1.0, kitchen.missingFor(bread, emptyList()).single().totalQuantity)
    }

    @Test
    fun case7_multipleRowsAreSummed() {
        val salad = recipe("Salad", 1, ingredient("Tomato", 3.0, "pcs"))
        assertTrue(kitchen.missingFor(salad, inventory(Triple("Tomatoes", 2.0, "pcs"), Triple("tomato", 1.0, "whole"))).isEmpty())
    }

    @Test
    fun groceryListMergesDuplicatesAcrossRecipesAndSubtractsInventory() {
        val a = recipe("A", 1, ingredient("Rice", 1.0, "cup"), ingredient("Eggs", 2.0, "pcs"))
        val b = recipe("B", 1, ingredient("rice", 2.0, "cups"), ingredient("Egg", 3.0, "pcs"))
        val list = kitchen.groceryList(listOf(a, b), inventory(Triple("Eggs", 4.0, "pcs")))

        assertEquals(listOf("Eggs", "Rice"), list.map { it.displayName })
        assertEquals(1.0, list.first { it.displayName == "Eggs" }.totalQuantity)
        val rice = list.first { it.displayName == "Rice" }
        assertEquals(3.0, rice.totalQuantity)
        assertEquals(setOf("A", "B"), rice.sourceRecipeNames)
    }

    @Test
    fun coverageCountsMergedIngredients() {
        val stirFry = recipe(
            "Stir fry", 2,
            ingredient("Chicken", 300.0, "g"), ingredient("Rice", 1.0, "cup"), ingredient("Soy sauce", 2.0, "tbsp")
        )
        val coverage = kitchen.coverage(stirFry, inventory(Triple("Rice", 3.0, "cup"), Triple("Soy sauce", 5.0, "tbsp")))
        assertEquals(Coverage(have = 2, total = 3), coverage)
    }

    @Test
    fun cookingDeductsAcrossRowsAndRemovesEmptyOnes() {
        val pancakes = recipe("Pancakes", 2, ingredient("Milk", 1.0, "cup"), ingredient("Eggs", 2.0, "pcs"))
        val rows = inventory(Triple("Milk", 100.0, "mL"), Triple("Milk", 1.0, "L"), Triple("Eggs", 2.0, "pcs"), Triple("Butter", 50.0, "g"))
        val changes = kitchen.cookingChanges(pancakes, rows)

        assertEquals(3, changes.size)
        assertTrue(InventoryChange.Remove(1) in changes) // the 100 mL row is used up first
        val litre = changes.filterIsInstance<InventoryChange.SetQuantity>().single { it.id == 2 }
        assertClose(1.0 - (236.588 - 100.0) / 1000.0, litre.quantity)
        assertTrue(InventoryChange.Remove(3) in changes) // eggs used up; butter untouched
    }

    @Test
    fun formatsQuantitiesForDisplay() {
        assertEquals("2", formatQuantity(2.0))
        assertEquals("1.5", formatQuantity(1.5))
        assertEquals("0.94", formatQuantity(0.94331))
        assertEquals("0.05", formatQuantity(0.05))
        assertEquals("3 pcs", formatAmount(3.0, "pcs"))
        assertEquals("2", formatAmount(2.0, ""))
    }
}
