package com.stepup.checkout.domain

import androidx.compose.runtime.Stable

/**
 * Validates that a [String] value is valid
 */
@Stable
fun interface InputValidator {

    /**
     * @return true if [value] is valid, false otherwise
     */
    fun isValid(value: String): Boolean

    data class RequiredLength(val maxLength: Int) : InputValidator {
        override fun isValid(value: String) = value.length == maxLength
    }

    data class MaxLength(val maxLength: Int) : InputValidator {
        override fun isValid(value: String) = value.length <= maxLength
    }

    companion object {
        val NumbersOnly = Regex("^[0-9]*$").asInputValidator()
    }
}

fun Regex.asInputValidator(): InputValidator = RegexValidator(this)

private data class RegexValidator(val regex: Regex) : InputValidator {
    override fun isValid(value: String) = value.matches(regex)
}

operator fun InputValidator.plus(other: InputValidator): InputValidator =
    CombinedInputValidator(this, other)

private data class CombinedInputValidator(
    val first: InputValidator,
    val second: InputValidator,
) : InputValidator {
    override fun isValid(value: String) = first.isValid(value) && second.isValid(value)
}