package com.stepup.checkout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun VerificationWebPage(
    verifyUrl: String,
    onResult: (Boolean) -> Unit
) = Box(
    Modifier
        .systemBarsPadding()
        .fillMaxSize()
        .background(MaterialTheme.colors.background)
) {
    val (isLoading, setLoading) = remember { mutableStateOf(true) }
    WebPage(
        webUrl = verifyUrl,
        onLoadingUpdated = setLoading,
        overrideUrl = { url ->
            val result = VerificationResult.match(url)
            if (result != null) {
                onResult(result == VerificationResult.Success)
            }
            result != null
        },
        modifier = Modifier.systemBarsPadding().imePadding()
    )
    if (isLoading) {
        CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize())
    }
}

internal enum class VerificationResult(val url: String) {

    Success("https://checkout.com/verify/success"),
    Failure("https://checkout.com/verify/failure");

    companion object {
        fun match(url: String) = values().find { url.startsWith(it.url)  }
    }
}