package com.stepup.checkout.domain

import androidx.compose.runtime.Stable

/**
 * Filters incoming text, e.g. removes any undesired characters,
 * clips to desired maximum length. Only use if needed to change the underlying data,
 * not for formatting.
 */
@Stable
fun interface InputFilter {

    /**
     * @param newValue incoming text
     * @return the filtered value
     */
    fun filter(newValue: String): String

    data class MaxLength(val maxLength: Int) : InputFilter {
        override fun filter(newValue: String) = newValue.take(maxLength)
    }

    object NumbersOnly : InputFilter {
        private val notNumbers = Regex("[^0-9]")
        override fun filter(newValue: String) = newValue.replace(notNumbers, "")
    }

    companion object
}

operator fun InputFilter.plus(other: InputFilter): InputFilter =
    CombinedInputFilter(this, other)

private data class CombinedInputFilter(
    val first: InputFilter,
    val second: InputFilter
) : InputFilter {
    override fun filter(newValue: String) = first.filter(newValue).let(second::filter)
}