package com.stepup.checkout.repo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface CheckoutService {
    @POST("pay/")
    suspend fun requestVerification(@Body request: VerificationRequest): VerificationUrl
}

@Serializable
data class VerificationRequest(
    val number: String,
    @SerialName("expiry_month")
    val expiryMonth: Int,
    @SerialName("expiry_year")
    val expiryYear: Int,
    val cvv: Int,
    @SerialName("success_url")
    val successUrl: String,
    @SerialName("failure_url")
    val failureUrl: String
)

@Serializable
data class VerificationUrl(val url: String)