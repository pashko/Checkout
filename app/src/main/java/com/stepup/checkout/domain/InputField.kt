package com.stepup.checkout.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

/**
 * Represents an input field of a form, including its value,
 * the [InputFilter], validation that the field has been completed
 * and that the input is valid
 */
@Stable
class InputField(
    private val rawValue: MutableState<String>,
    filter: State<InputFilter>,
    completionValidator: State<InputValidator>,
    valueValidator: State<InputValidator>,
) {

    constructor(
        initialValue: String = "",
        filter: State<InputFilter>,
        completionValidator: State<InputValidator>,
        valueValidator: State<InputValidator>,
    ) : this(
        mutableStateOf(initialValue),
        filter,
        completionValidator,
        valueValidator
    )

    private val _value by derivedStateOf { filter.value.filter(rawValue.value) }
    var value
        get() = _value
        set(value) { rawValue.value = value }

    private val isComplete by derivedStateOf { completionValidator.value.isValid(value) }

    /** Returns true if the input in the field is valid, regardless of completion, false otherwise */
    val isInputValid by derivedStateOf { valueValidator.value.isValid(value) }

    /** Returns true if the field has been completed and the input in it is valid, false otherwise */
    val isValidAndComplete get() = isComplete && isInputValid
}