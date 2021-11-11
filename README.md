This repository is the entry point to using Ixigo Android SDK.

[[_TOC_]]

## Installation

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
  implementation "com.ixigo.sdk.flights:flights-sdk:1.0"
}
```

### Use Snapshots

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
  implementation "com.ixigo.sdk.flights:flights-sdk:1.0-SNAPSHOT"
}
```

## Usage

### Initialize the SDK

Initialize the SDK calling `IxigoSDK.init(...)` in your App creation flow.

### Configure Appearance

You can configure the appearance of certain UI elements presented by the sdk by providing the following keys in your resources

```xml
<resources>
  ...
  <!-- Primary color: will be used in several UI elements. eg: status bar bg color-->
  <color name="ixigosdk_primary_color">#FF0000</color>
</resources>
```

## Docs

You can find classes documentation here: http://android.pages.ixigo.com/ixigo-android-sdk/docs/
