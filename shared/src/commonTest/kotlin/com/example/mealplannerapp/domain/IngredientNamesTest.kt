// Credits: Jayda; written with Claude (Anthropic) at Jayda's request.

package com.example.mealplannerapp.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class IngredientNamesTest {

    @Test
    fun trimsLowercasesAndCollapsesSpaces() {
        assertEquals("ground beef", IngredientNames.normalize("  Ground   Beef "))
    }

    @Test
    fun singularizesCommonPlurals() {
        assertEquals("egg", IngredientNames.normalize("Eggs"))
        assertEquals("tomato", IngredientNames.normalize("tomatoes"))
        assertEquals("berry", IngredientNames.normalize("Berries"))
        assertEquals("peach", IngredientNames.normalize("peaches"))
        assertEquals("green onion", IngredientNames.normalize("Green Onions"))
    }

    @Test
    fun leavesSingularWordsEndingInSAlone() {
        assertEquals("hummus", IngredientNames.normalize("Hummus"))
        assertEquals("swiss", IngredientNames.normalize("Swiss"))
        assertEquals("molasses", IngredientNames.normalize("molasses"))
        assertEquals("asparagus", IngredientNames.normalize("asparagus"))
    }

    @Test
    fun pluralAndSingularGiveSameKey() {
        assertEquals(IngredientNames.normalize("egg"), IngredientNames.normalize("EGGS"))
    }

    @Test
    fun blankNameGivesEmptyKey() {
        assertEquals("", IngredientNames.normalize("   "))
    }
}
