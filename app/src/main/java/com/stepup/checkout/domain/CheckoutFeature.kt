package com.stepup.checkout.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.stepup.checkout.common.ErrorHandlerScope
import com.stepup.checkout.common.EventsContainer
import com.stepup.checkout.common.perform
import com.stepup.checkout.common.withLoading
import com.stepup.checkout.repo.CheckoutService
import com.stepup.checkout.repo.VerificationRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class CheckoutFeature internal constructor(
    private val service: CheckoutService,
    val form: PaymentCardForm,
    coroutineScope: CoroutineScope
) {

    @AssistedFactory
    interface Factory {
        fun create(coroutineScope: CoroutineScope): CheckoutFeature
    }

    @AssistedInject
    constructor(
        service: CheckoutService,
        @Assisted coroutineScope: CoroutineScope
    ) : this(service, PaymentCardForm(), coroutineScope)

    private val eventsContainer = EventsContainer<Event>()
    val events: Flow<Event> = eventsContainer
    var isLoading by mutableStateOf(false)
        private set

    private val safeScope = ErrorHandlerScope(coroutineScope) {
        eventsContainer.emit(Event.UnknownError)
    }

    fun requestVerification(
        successUrl: String,
        failureUrl: String
    ) = safeScope.perform {
        val request = form.toRequest(successUrl, failureUrl) ?: return@perform
        withLoading(update = { isLoading = it }) {
            val result = service.requestVerification(request)
            eventsContainer.emit(Event.VerificationReady(result.url))
        }
    }

    sealed interface Event {
        data class VerificationReady(val url: String) : Event
        object UnknownError : Event
    }
}

private fun PaymentCardForm.toRequest(
    successUrl: String,
    failureUrl: String
): VerificationRequest? {
    if (!isValid) return null
    return VerificationRequest(
        number = number.value,
        cvv = cvv.value.toInt(),
        expiryMonth = ExpiryDateValidator.monthFromMMYY(expiry.value),
        expiryYear = ExpiryDateValidator.yearFromMMYY(expiry.value),
        successUrl = successUrl,
        failureUrl = failureUrl
    )
}

