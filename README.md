## Ixigo Android SDK

This repository is the entry point to using Ixigo Android SDK. 

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

### Usage

Initialize the SDK calling `IxigoSDK.init(...)` in your App creation flow. 
