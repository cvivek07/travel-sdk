package com.ixigo.sdk.payment

internal object EmptyPaymentProvider: PaymentProvider {
    override fun startPayment(input: PaymentInput, callback: PaymentCallback) = false
}