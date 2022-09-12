package com.stepup.checkout.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.stepup.checkout.R

@Composable
fun ResultScreen(
    success: Boolean,
    restart: () -> Unit,
    modifier: Modifier = Modifier
) = Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
    Spacer(Modifier.weight(1f))
    Image(
        painterResource(
            if (success) R.drawable.ic_success else R.drawable.ic_failure,
        ),
        contentDescription = null
    )
    Spacer(Modifier.height(24.dp))
    Text(
        if (success) "Payment successful"
        else "Payment failed"
    )
    Button(
        onClick = restart,
        modifier = Modifier.weight(1f).wrapContentSize()
    ) {
        Text(if (success) "New payment" else "Retry")
    }
}