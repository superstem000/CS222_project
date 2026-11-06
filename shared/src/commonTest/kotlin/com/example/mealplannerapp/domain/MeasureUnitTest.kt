// Credits: Jayda; written with Claude (Anthropic) at Jayda's request.

package com.example.mealplannerapp.domain

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MeasureUnitTest {

    @Test
    fun parsesCommonSpellings() {
        assertEquals(MeasureUnit.CUP, MeasureUnit.parse("Cups"))
        assertEquals(MeasureUnit.TABLESPOON, MeasureUnit.parse("tbsp."))
        assertEquals(MeasureUnit.GRAM, MeasureUnit.parse(" g "))
        assertEquals(MeasureUnit.POUND, MeasureUnit.parse("LBS"))
        assertEquals(MeasureUnit.FLUID_OUNCE, MeasureUnit.parse("fl  oz"))
        assertEquals(MeasureUnit.PIECE, MeasureUnit.parse(""))
    }

    @Test
    fun unknownUnitIsNull() {
        assertNull(MeasureUnit.parse("can"))
        assertNull(MeasureUnit.parse("pinch"))
    }

    @Test
    fun convertsWithinCategory() {
        // 2 cups = 2 x 236.5882365 = 473.176473 mL
        assertClose(473.176473, MeasureUnit.CUP.toBaseQuantity(2.0))
        // 1 lb = 453.59237 g
        assertClose(453.59237, MeasureUnit.POUND.toBaseQuantity(1.0))
        // 3 tsp = 1 tbsp: 3 x 4.92892159375 = 14.78676478125
        assertClose(MeasureUnit.TABLESPOON.toBase, MeasureUnit.TEASPOON.toBaseQuantity(3.0))
    }

    private fun assertClose(expected: Double, actual: Double) =
        assertTrue(abs(expected - actual) <= 1e-9, "Expected $expected but was $actual")
}
