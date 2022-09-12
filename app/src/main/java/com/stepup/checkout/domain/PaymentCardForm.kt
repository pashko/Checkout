package com.stepup.checkout.domain

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

@Stable
class PaymentCardForm {

    private val rawCardNumber = mutableStateOf("")

    val type by derivedStateOf { PaymentCardType.typeOf(rawCardNumber.value) }

    val number = InputField(
        rawValue = rawCardNumber,
        filter = derivedStateOf { InputFilter.CardNumber(type) },
        completionValidator = derivedStateOf { InputValidator.RequiredLength(type.numberLength) },
        valueValidator = derivedStateOf { InputValidator.CardNumber(type) }
    )

    val cvv = InputField(
        filter = derivedStateOf { InputFilter.Cvv(type) },
        completionValidator = derivedStateOf { InputValidator.RequiredLength(type.cvvLength) },
        valueValidator = derivedStateOf { InputValidator.Cvv(type) }
    )

    val expiry = InputField(
        filter = mutableStateOf(InputFilter.ExpiryDate),
        completionValidator = mutableStateOf(
            InputValidator { it.length == PaymentCardType.ExpiryDateLength(it) }
        ),
        valueValidator = mutableStateOf(InputValidator.ExpiryDate)
    )

    val isValid by derivedStateOf {
        number.isValidAndComplete && expiry.isValidAndComplete && cvv.isValidAndComplete
    }
}