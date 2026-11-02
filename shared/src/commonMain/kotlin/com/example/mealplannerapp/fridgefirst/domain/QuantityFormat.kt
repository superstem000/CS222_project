package com.example.mealplannerapp.fridgefirst.domain

import kotlin.math.abs
import kotlin.math.roundToLong

/** "2.0" -> "2", "0.333333" -> "0.33". Common code has no String.format, so this is done by hand. */
fun formatQuantity(quantity: Double): String {
    val hundredths = (quantity * 100).roundToLong()
    val whole = hundredths / 100
    val fraction = abs(hundredths % 100)
    val sign = if (hundredths < 0 && whole == 0L) "-" else ""
    return when {
        fraction == 0L -> "$sign$whole"
        fraction % 10 == 0L -> "$sign$whole.${fraction / 10}"
        else -> "$sign$whole.${fraction.toString().padStart(2, '0')}"
    }
}

/** "3 pcs", "100 g", or just "2" when there is no unit. */
fun formatAmount(quantity: Double, unit: String): String =
    listOf(formatQuantity(quantity), unit.trim()).filter { it.isNotEmpty() }.joinToString(" ")

/** Parses user input like "2", "1.5" or "1,5". Returns null for blank/invalid/non-positive input. */
fun parseQuantity(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 && it.isFinite() }
