package com.stepup.checkout

import com.stepup.checkout.domain.CheckoutFeature
import com.stepup.checkout.domain.InputField
import com.stepup.checkout.domain.PaymentCardForm
import com.stepup.checkout.repo.CheckoutService
import com.stepup.checkout.repo.VerificationRequest
import com.stepup.checkout.repo.VerificationUrl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutFeatureTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun mockInputField(mockValue: String) = mockk<InputField> {
        every { value } returns mockValue
        every { isValidAndComplete } returns true
        every { isInputValid } returns false
    }

    @Test
    fun `when requested, called with correct params`() = runTest {
        val expected = VerificationRequest(
            number = "number",
            expiryMonth = 9,
            expiryYear = 22,
            cvv = 123,
            successUrl = "success",
            failureUrl = "failure"
        )
        val url = "verification url"
        val service = mockk<CheckoutService> {
            coEvery { requestVerification(expected) } returns VerificationUrl(url)
        }
        val form = mockk<PaymentCardForm> {
            every { number } returns mockInputField(expected.number)
            every { cvv } returns mockInputField(expected.cvv.toString())
            every { expiry } returns mockInputField(
                "${expected.expiryMonth}${expected.expiryYear}"
            )
            every { isValid } returns true
        }
        val feature = CheckoutFeature(service, form, this)
        feature.requestVerification(expected.successUrl, expected.failureUrl)
        testScheduler.advanceUntilIdle()
        feature.events.first() shouldBeEqualTo CheckoutFeature.Event.VerificationReady(url)
        // CheckoutFeature creates a new Job, we need to cancel it to complete
        coroutineContext.cancelChildren()
    }

    @Test
    fun `when requested and form invalid, request ignored`() = runTest {
        val service = mockk<CheckoutService>(relaxed = true)
        val form = mockk<PaymentCardForm> {
            every { isValid } returns false
        }
        val feature = CheckoutFeature(service, form, this)
        feature.requestVerification("successUrl", "failureUrl")
        testScheduler.advanceUntilIdle()
        coVerify(exactly = 0) { service.requestVerification(any()) }
        coroutineContext.cancelChildren()
    }

    @Test
    fun `when requested and error occurs, correct event emitted`() = runTest {
        val service = mockk<CheckoutService> {
            coEvery { requestVerification(any()) } answers { error("Oopsie") }
        }
        val form = mockk<PaymentCardForm> {
            every { number } returns mockInputField("number")
            every { cvv } returns mockInputField("123")
            every { expiry } returns mockInputField("1234")
            every { isValid } returns true
        }
        val feature = CheckoutFeature(service, form, this)
        feature.requestVerification("successUrl", "failureUrl")
        testScheduler.advanceUntilIdle()
        feature.events.first() shouldBeEqualTo CheckoutFeature.Event.UnknownError
        coroutineContext.cancelChildren()
    }
}