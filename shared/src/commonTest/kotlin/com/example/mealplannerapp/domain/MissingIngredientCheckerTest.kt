// Credits: Jayda; written with Claude (Anthropic) at Jayda's request.
// Cases 1-7 match section 4 of docs/ingredient-matching-plan.md.

package com.example.mealplannerapp.domain

import com.example.mealplannerapp.data.local.Ingredient
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MissingIngredientCheckerTest {

    private fun ingredient(name: String, quantity: Double, unit: String) =
        Ingredient(recipeId = 1, name = name, quantity = quantity, unit = unit)

    private var nextId = 1
    private fun stock(name: String, quantity: Double, unit: String) =
        InventoryData(nextId++, name, quantity, unit)

    private fun checkOne(recipe: Ingredient, vararg inventory: InventoryData): IngredientCheck =
        MissingIngredientChecker.check(listOf(recipe), inventory.toList()).single()

    @Test
    fun case1_enoughOnHand() {
        val result = checkOne(ingredient("egg", 2.0, ""), stock("egg", 6.0, ""))
        assertFalse(result.isMissing)
    }

    @Test
    fun case2_pluralAndCapitalizationStillMatch() {
        val result = checkOne(ingredient("Eggs", 2.0, ""), stock("egg", 6.0, ""))
        assertFalse(result.isMissing)
    }

    @Test
    fun case3_sameCategoryDifferentUnitsReportsShortfallInRecipeUnit() {
        // needed: 2 cups = 473.176473 mL; on hand 250 mL; short 223.176473 mL
        // in cups: 223.176473 / 236.5882365 = 0.94331...
        val result = checkOne(ingredient("milk", 2.0, "cup"), stock("milk", 250.0, "mL"))
        assertTrue(result.isMissing)
        assertClose(223.176473 / 236.5882365, result.missingQuantity)
    }

    @Test
    fun case4_poundsVsGramsHaveEnough() {
        // needed: 1 lb = 453.59237 g <= 500 g on hand
        val result = checkOne(ingredient("chicken", 1.0, "lb"), stock("chicken", 500.0, "g"))
        assertFalse(result.isMissing)
    }

    @Test
    fun case5_volumeVsMassCountsAsMissing() {
        val result = checkOne(ingredient("flour", 2.0, "cup"), stock("flour", 1.0, "kg"))
        assertTrue(result.isMissing)
        assertClose(2.0, result.missingQuantity)
    }

    @Test
    fun case6_notInInventoryMissesFullAmount() {
        val result = checkOne(ingredient("basil", 3.0, "tbsp"))
        assertClose(3.0, result.missingQuantity)
    }

    @Test
    fun case7_multipleInventoryRowsAreSummed() {
        // 1 cup + 1 cup = 2 cups on hand, recipe needs 2 cups
        val result = checkOne(
            ingredient("rice", 2.0, "cups"),
            stock("rice", 1.0, "cup"),
            stock("Rice", 236.5882365, "ml")
        )
        assertFalse(result.isMissing)
    }

    @Test
    fun unrecognizedUnitOnlyMatchesSameUnit() {
        // "cans" and "can" normalize to the same unit key
        val enough = checkOne(ingredient("black beans", 1.0, "can"), stock("black bean", 2.0, "cans"))
        assertFalse(enough.isMissing)

        val wrongUnit = checkOne(ingredient("black beans", 1.0, "can"), stock("black bean", 400.0, "g"))
        assertClose(1.0, wrongUnit.missingQuantity)
    }

    @Test
    fun scaleMultipliesNeededAmount() {
        // recipe 1 cup x scale 3 = 3 cups needed; 2 cups on hand -> missing 1 cup
        val result = MissingIngredientChecker.check(
            listOf(ingredient("oats", 1.0, "cup")),
            listOf(stock("oats", 2.0, "cup")),
            scale = 3.0
        ).single()
        assertClose(1.0, result.missingQuantity)
    }

    @Test
    fun blankIngredientNamesAreSkipped() {
        val results = MissingIngredientChecker.check(listOf(ingredient("  ", 1.0, "cup")), emptyList())
        assertEquals(0, results.size)
    }

    private fun assertClose(expected: Double, actual: Double) =
        assertTrue(abs(expected - actual) <= 1e-9, "Expected $expected but was $actual")
}
