This repository is the entry point to using Ixigo Android SDK.

[[_TOC_]]

# Installation

Inside **root/build.gradle**

```groovy
buildscript {
  repositories {
    // Other repositories...
    maven { url 'https://nexus.ixigo.com/nexus/content/repositories/androidshared' }
  }
}

```

Inside **app/build.gradle**

```groovy
dependencies {
  // Other dependencies...
  implementation "com.ixigo.sdk.flights:ixigo-sdk:1.0.1"
}

```

## Use Snapshots

Inside **root/build.gradle**

```groovy
buildscript {
  repositories {
    // Other repositories...
    maven { url 'https://nexus.ixigo.com/nexus/content/repositories/androidshared-snapshots' }
  }
}

```

Inside **app/build.gradle**

```groovy
dependencies {
  // Other dependencies...
  implementation "com.ixigo.sdk.flights:ixigo-sdk:1.0.1-SNAPSHOT"
}

```

# Usage

## Initialize the SDK

Initialize the SDK calling `IxigoSDK.init(...)` in your App creation flow.

## Authentication

### SSO Authentication (Recommended)

To use SSO Authentication, initialize `IxigoSDK` with an instance of `SSOAuthProvider`.

You will need to pass a `PartnerTokenProvider` so that we can retrieve your App access token and exchange it for an Ixigo access token.

```kotlin
class MyAppPartnerTokenProvider(): PartnerTokenProvider {
  override val partnerToken: PartnerToken?
        get() = TODO("Logic to retrieve MyApp access token")
}

// Inside your Application initialization code
IxigoSDK.init(context, SSOAuthProvider(MyAppPartnerTokenProvider()), /* Other Params */)

```

### Custom Authentication

If your App has other means of getting an Ixigo access token, you can implement `AuthProvider` and use it when initializing `IxigoSDK`.

```kotlin
private class MyAppAuthProvider(): AuthProvider {
  override val authData: AuthData?
    get() = TODO("Return an authToken if already available")

  override fun login(fragmentActivity: FragmentActivity, callback: AuthCallback): Boolean {
    TODO("Perform login")
  }
}

// Inside your Application initialization code
IxigoSDK.init(context, MyAppAuthProvider, /* Other Params */)

```

## Payment

If your App supports a custom Payment mechanism, you can implement `PaymentProvider` and use it when initializing `IxigoSDK`

```kotlin
class MyPaymentProvider(): PaymentProvider {
  override fun startPayment(activity: FragmentActivity, input: PaymentInput, callback: PaymentCallback): Boolean {
      TODO("Handle payment")
   }
}

```

If your `PaymentProvider` implementation starts another activity for result, you can implement as well `ActivityResultHandler` to get a callback when the Activity is finished

```kotlin
class MyPaymentProvider(): PaymentProvider, ActivityResultHandler {
  override fun startPayment(activity: FragmentActivity, input: PaymentInput, callback: PaymentCallback): Boolean {
      TODO("Handle payment")
  }

  fun handle(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    TODO("Handle Activity Result")
  }
}

```

### Configure Appearance

You can configure the appearance of certain UI elements presented by the sdk by providing the following keys in your resources

```xml
<resources>
  ...
  <!-- Primary color: will be used in several UI elements. eg: status bar bg color-->
  <color name="ixigosdk_primary_color">#FF0000</color>
</resources>

```

## [Development](docs/DEVELOPMENT.md)

Find development docs [here](docs/DEVELOPMENT.md)

## Docs

You can find classes documentation here: http://android.pages.ixigo.com/ixigo-android-sdk/docs/
