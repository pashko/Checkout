@file:Suppress("FunctionName")

package com.stepup.checkout.domain

import com.stepup.checkout.domain.InputFilter.MaxLength
import java.time.ZonedDateTime

/**
 * Represents the provider of a payment card.
 * Contains the blocks that the card number is conventionally broken into,
 * the number's length and the length of the security code.
 */
enum class PaymentCardType(
    val regex: Regex,
    val numberBlocks: List<Int>,
    val cvvLength: Int
) {

    Visa(
        regex = Regex("^4[0-9]*$"),
        numberBlocks = listOf(4, 4, 4, 4),
        cvvLength = 3
    ),

    MasterCard(
        regex = Regex("^(5[1-5]|222[1-9]|22[3-9]|2[3-6]|27[01]|2720)[0-9]*$"),
        numberBlocks = listOf(4, 4, 4, 4),
        cvvLength = 3
    ),

    Amex(
        regex = Regex("^3[47][0-9]*$"),
        numberBlocks = listOf(4, 6, 5),
        cvvLength = 4
    );

    val numberLength = numberBlocks.sum()

    companion object {

        internal const val DefaultCvvLength = 3
        internal val DefaultNumberBlocks = listOf(4, 4, 4, 4)
        internal val DefaultNumberLength = DefaultNumberBlocks.sum()
        internal val ExpiryDateLength = { value: String ->
            if (value.startsWith('1') || value.startsWith('0')) 4 else 3
        }

        fun typeOf(cardNumber: String) = values().find { it.regex.matches(cardNumber) }
    }
}

val PaymentCardType?.numberLength get() = this?.numberLength ?: PaymentCardType.DefaultNumberLength
val PaymentCardType?.numberBlocks get() = this?.numberBlocks ?: PaymentCardType.DefaultNumberBlocks
val PaymentCardType?.cvvLength get() = this?.cvvLength ?: PaymentCardType.DefaultCvvLength

/**
 * An [InputFilter] for card numbers. Will remove non-number characters and clip the text to
 * the [type]'s maximum length
 */
fun InputFilter.Companion.CardNumber(
    type: PaymentCardType? = null
) = InputFilter.NumbersOnly + MaxLength(type.numberLength)

/**
 * An [InputFilter] for card security codes. Will remove non-number characters and clip the text to
 * the [type]'s maximum length
 */
fun InputFilter.Companion.Cvv(
    type: PaymentCardType? = null
) = InputFilter.NumbersOnly + MaxLength(type.cvvLength)

/**
 * An [InputFilter] for card expiry dates. Will remove non-number characters and clip the length
 * to 4 characters if the date starts with 0 or 1, or 3 otherwise
 */
val InputFilter.Companion.ExpiryDate
    get() = InputFilter.NumbersOnly + ExpiryDateLength

private object ExpiryDateLength : InputFilter {
    override fun filter(newValue: String) =
        newValue.take(PaymentCardType.ExpiryDateLength(newValue))
}
/**
 * An [InputValidator] for card numbers. Validates that the input
 * doesn't contain non-number characters and its length is at most what's required by [type]
 * TODO add real card number validation
 */
fun InputValidator.Companion.CardNumber(type: PaymentCardType? = null) = NumbersOnly +
        InputValidator.MaxLength(type.numberLength)

/**
 * An [InputValidator] for card security codes. Validates that the input
 * doesn't contain non-number characters and its length is at most what's required by [type]
 */
fun InputValidator.Companion.Cvv(type: PaymentCardType? = null) = NumbersOnly +
        InputValidator.MaxLength(type.cvvLength)

/**
 * An [InputValidator] for card expiry date. Validates that the input
 * is incomplete, MMYY, or MYY, and that the date is in the future
 */
val InputValidator.Companion.ExpiryDate: InputValidator get() = ExpiryDateValidator

internal object ExpiryDateValidator : InputValidator {

    /**  For internal use, only after input validation */
    fun monthFromMMYY(text: String) = text.substring(0, text.length - 2).toInt()

    /**  For internal use, only after input validation */
    fun yearFromMMYY(text: String) = text.takeLast(2).toInt()

    private val regex = Regex("^(0?[1-9]|1[0-2])[0-9]{2}$").asInputValidator()

    override fun isValid(value: String): Boolean {
        if (value.length < PaymentCardType.ExpiryDateLength(value)) return true
        if (!regex.isValid(value)) return false
        val now = ZonedDateTime.now()
        val currentYear = now.year % 100
        val currentMonth = now.monthValue
        val expiryMonth = monthFromMMYY(value)
        val expiryYear = yearFromMMYY(value)
        return expiryYear > currentYear
                || expiryYear == currentYear && expiryMonth >= currentMonth
    }
}
