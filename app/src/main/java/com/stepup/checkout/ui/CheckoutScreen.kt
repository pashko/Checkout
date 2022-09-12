package com.stepup.checkout.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepup.checkout.R
import com.stepup.checkout.common.Collect
import com.stepup.checkout.domain.CheckoutFeature
import com.stepup.checkout.domain.CheckoutFeature.Event
import com.stepup.checkout.domain.PaymentCardType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Composable
fun CheckoutScreen(
    verify: (url: String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier,
        scaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState),
        snackbarHost = { SnackbarHost(it, Modifier.systemBarsPadding().imePadding()) },
        topBar = {
            TopAppBar(contentPadding = WindowInsets.statusBars.asPaddingValues()) {
                Text(
                    "Checkout",
                    Modifier.fillMaxSize().wrapContentSize(),
                    style = MaterialTheme.typography.h6
                )
            }
        },
        content = { padding ->
            CheckoutScreenContent(
                onVerifyRequested = verify,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(padding)
            )
        },
    )
}

@Composable
private fun CheckoutScreenContent(
    onVerifyRequested: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    feature: CheckoutFeature = hiltViewModel<CheckoutViewModel>().feature,
) = Box(modifier) {
    val focusManager = LocalFocusManager.current
    Collect(feature.events) {
        when (it) {
            is Event.UnknownError -> snackbarHostState.showSnackbar(
                "An error has occurred, please try again"
            )
            is Event.VerificationReady -> {
                focusManager.clearFocus()
                onVerifyRequested(it.url)
            }
        }
    }
    val form = feature.form
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        InputField(
            data = form.number,
            modifier = Modifier.fillMaxWidth(),
            label = "Card Number",
            errorText = "Invalid card number",
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
            trailingIcon = {
                form.type?.let {
                    CardTypeLogo(it, Modifier.width(24.dp))
                }
            },
            visualTransformation = rememberCardNumberTransformation(form.type)
        )
        Row(
            Modifier.padding(vertical = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            InputField(
                data = form.expiry,
                modifier = Modifier.weight(1f),
                label = "Expiry Date",
                errorText = "Card expired",
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                ),
                visualTransformation = rememberExpiryDateTransformation()
            )
            InputField(
                data = form.cvv,
                modifier = Modifier.weight(1f),
                label = "CVV",
                errorText = "Invalid CVV",
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Go,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(
                    onGo = { feature.requestVerification() }
                )
            )
        }
        Button(
            onClick = { feature.requestVerification() },
            enabled = form.isValid,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            content = { Text("Pay") },
        )
    }
    if (feature.isLoading) {
        CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize())
    }
}


private fun CheckoutFeature.requestVerification() {
    requestVerification(VerificationResult.Success.url, VerificationResult.Failure.url)
}

@Composable
private fun CardTypeLogo(type: PaymentCardType, modifier: Modifier = Modifier) {
    Icon(
        painterResource(
            when (type) {
                PaymentCardType.MasterCard -> R.drawable.ic_mastercard_logo
                PaymentCardType.Visa -> R.drawable.ic_visa_logo
                PaymentCardType.Amex -> R.drawable.ic_amex_logo
            }
        ),
        contentDescription = null,
        modifier = modifier
    )
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    factory: CheckoutFeature.Factory,
) : ViewModel() {
    val feature = factory.create(viewModelScope)
}
