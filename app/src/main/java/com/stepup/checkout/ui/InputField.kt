package com.stepup.checkout.ui

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import com.stepup.checkout.domain.InputField

@Composable
internal fun InputField(
    data: InputField,
    modifier: Modifier = Modifier,
    label: String? = null,
    errorText: String? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    visualTransformation: VisualTransformation = VisualTransformation.None
) = TextField(
    value = data.value,
    onValueChange = { data.value = it },
    modifier = modifier,
    textStyle = textStyle,
    isError = !data.isInputValid,
    label = (label.takeIf { data.isInputValid } ?: errorText)?.let { { Text(it) } },
    trailingIcon = trailingIcon,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    visualTransformation = visualTransformation
)
