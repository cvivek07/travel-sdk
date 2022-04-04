[[_TOC_]]

# Usage

In order to access Payment functionality, you need to initialize `PaymentSDK` after `IxigoSDK` initialization

```kotlin
IxigoSDK.init(...)
PaymentSDK.init(...)
```

## Process funnel payments with Ixigo SDK

In order to automatically process payments in any funnel using PaymentSDK, initialize `IxigoSDK` like this:

```kotlin
IxigoSDK.init(paymentProvider = PaymentSDKPaymentProvider(), /* Other params */)
```

## Process funnel payments with host App

If your App supports a custom Payment mechanism, you can implement `PaymentProvider` and use it when initializing `IxigoSDK`

```kotlin
class MyPaymentProvider(): PaymentProvider {
  override fun startPayment(activity: FragmentActivity, input: PaymentInput, callback: PaymentCallback): Boolean {
      TODO("Handle payment")
   }
}

```

## Manually process a payment

If you want to trigger a payment process outside of an `IxigoSDK` funnel, you can do so by calling `processPayment`:

```kotlin
PaymentSDK.instance.processPayment(...)
```
