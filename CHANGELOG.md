# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

### [3.1.4](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-3.1.3...ixigo-sdk-3.1.4) (2022-02-24)


### Bug Fixes

* add displayMode=embedded to flight trips when displaying in a fragment ([01f1e43](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/01f1e43a6e385ea98e982ae0a402fd3857f6a67e))

### [3.1.3](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-3.1.2...ixigo-sdk-3.1.3) (2022-02-23)


### Bug Fixes

* loader not appearing when pressing retry ([ce0b48f](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/ce0b48f47188fb1d3d0cb68770c4b87dbe5d85e8))

### [3.1.2](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-3.1.1...ixigo-sdk-3.1.2) (2022-02-22)


### Bug Fixes

* **analytics:** log webviewStartLoad and add referrer to events ([d3fd855](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/d3fd8551b05708dae9318890cb2c3045af0d362f))

### [3.1.1](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-3.1.0...ixigo-sdk-3.1.1) (2022-02-21)


### Bug Fixes

* add funnel config to bus trips method ([86fed9a](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/86fed9a2ee04e2b98449d1feb94803a705b27156))
* use correct flight trips URL ([1baa070](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/1baa07074984850a1c80f9b712f33f37de4bf5fa))

## [3.1.0](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-3.0.0...ixigo-sdk-3.1.0) (2022-02-17)


### Features

* add support for IxigoJS SDK ([02594d5](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/02594d5bd98bbcedaec169bf4c0dfd0399aa832c))


### Bug Fixes

* fix fragment changing status bar color ([cf74775](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/cf74775ca39312bbd8ced340cf1e08ef48a9c940))
* remove hideHeader parameter which caused trips to not show up ([91e878e](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/91e878e5f646c91fc3721772f2f13e4859fe075e))

## [3.0.0](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-2.4.0...ixigo-sdk-3.0.0) (2022-02-15)


### ⚠ BREAKING CHANGES

* **auth:** modify signature of PartnerTokenProvider.fetchToken

### Bug Fixes

* add hideHeader to flights trips fragment url ([c647ff5](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/c647ff5906149c041eb5ee8d504d911f5f41c0d1))
* **auth:** pass partnerId to PartnerTokenProvider.fetchToken method ([31908ba](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/31908ba627c9a179d32ef6e3eedc7aab4870a50b))

## [2.4.0](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-2.3.0...ixigo-sdk-2.4.0) (2022-02-15)


### Features

* add train trips APIs ([c9992a3](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/c9992a38e15a0be55fc82052507c815f273eca8a))

## [2.3.0](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-2.2.0...ixigo-sdk-2.3.0) (2022-02-14)


### Features

* add ability to track events from Javascript ([9594b7c](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/9594b7c59b072b3ce6376472eeb81595eefe0c31))
* add trips flights functionality ([39d773e](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/39d773e38465dab61122dcabab66c715c0135509))


### Bug Fixes

* ixigoSDK.init returns ixigoSDK object ([9f84cd1](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/9f84cd1a5fad2da009e8400a5397dbef4d9de309))

## [2.2.0](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-2.1.1...ixigo-sdk-2.2.0) (2022-02-11)


### Features

* add covid sdk ([57d0495](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/57d049577af49ca7855b166129bcdc115fffc06c))
* allow funnel config in Bus and Trains ([3236447](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/3236447a6f85baee5b00d2be1cc229057f4cfb7e))
* make SDK initialized public ([72bd537](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/72bd53703ba7d9a04d8711497341851330f067c1))


### Bug Fixes

* allow passing FunnelConfig when opening vaccines ([4954267](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/495426762affd0b1973a38d679477c1dd129b38d))

### [2.1.1](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-2.1.0...ixigo-sdk-2.1.1) (2022-02-10)


### Bug Fixes

* disable top exit bar for multi module ([271d6e5](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/271d6e5278927ad6a62fdb854efc3ae20c08cfeb))
* remove obfuscation from HTMLOUT json fields ([87e3ec3](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/87e3ec34db601c5e37b3ffaceee8b8da61ed63e4))

## [2.1.0](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-2.0.3...ixigo-sdk-2.1.0) (2022-02-09)


### Features

* add exitTopBar ([59966d0](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/59966d0f1d39a23136ceea835e0acd26ca4b46a2))

### [2.0.3](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-2.0.2...ixigo-sdk-2.0.3) (2022-02-09)


### Bug Fixes

* add Keep to moshi models ([d3c0ee2](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/d3c0ee2a48df818e216945f38d0155a8480e9677))
* fix UUID NPE creation ([f31690e](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/f31690e8bbc5e987085f10eb6e5984b875351af4))

### [2.0.2](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-2.0.1...ixigo-sdk-2.0.2) (2022-02-04)


### Bug Fixes

* allow cookie deletion ([d09d813](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/d09d813669e111ee86d010cdef6504045f6c4c5a))
* do not apply HTMLOut to ixigo PWAs ([cd1281e](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/cd1281e69967654efee4ec666646cbe15cd56423))

### [2.0.1](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-2.0.0...ixigo-sdk-2.0.1) (2022-02-03)


### Bug Fixes

* make sure activity result is forwarded to PartnerTokenProvider ([705d138](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/705d1382dfae6eb430def4a7596f17cbc4df524b))

## [2.0.0](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/compare/ixigo-sdk-1.11.4...ixigo-sdk-2.0.0) (2022-02-02)


### ⚠ BREAKING CHANGES

* **auth:** Added activity parameter to fetchToken

### Bug Fixes

* **auth:** pass activity to partnerTokenProvider fetch method ([9a31ddf](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/9a31ddf5a7b2a7eccaa78d9b909d2851e4ddbbcb))
* use correct gradle scan option ([2c961a7](https://git.ixigo.com/ixigo-sdk/ixigo-android-sdk/commit/2c961a71e0ffc11e70d3a9aaa4413aec1e8abed8))

### [1.11.4](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.11.3...ixigo-sdk-1.11.4) (2022-02-02)


### Bug Fixes

* allow activityForResult in PartnerTokenProvider ([240edde](https://git.ixigo.com/android/ixigo-android-sdk/commit/240edde3bab92634270cadcc4038368e10078dab))
* fix issue showing errorView when a Cache miss occurs ([594e24e](https://git.ixigo.com/android/ixigo-android-sdk/commit/594e24e9506c40350cf09407d9fc7c7b0ca1dc11))

### [1.11.3](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.11.2...ixigo-sdk-1.11.3) (2022-01-21)


### Bug Fixes

* modify status bar color using theme-color ([ab2e44a](https://git.ixigo.com/android/ixigo-android-sdk/commit/ab2e44af45a53300b42280d57db0b5122ddc8fd9))

### [1.11.2](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.11.1...ixigo-sdk-1.11.2) (2022-01-21)


### Bug Fixes

* add bus trips SDK ([169e832](https://git.ixigo.com/android/ixigo-android-sdk/commit/169e832f4392012923773beb17e3ae5400f503ec))
* disable bus multimodel and trains home buttons when needed in sample app ([f6eff56](https://git.ixigo.com/android/ixigo-android-sdk/commit/f6eff564e1bf70ecbbaf9afc0039047395294e05))
* remove confirmtkt abhibus integration as it is not ready yet ([a3dce31](https://git.ixigo.com/android/ixigo-android-sdk/commit/a3dce31ed1daa2e992fe56341be46639aba1e250))
* use shouldOverrideUrl instead of PageStarted to set the Loading status ([acdd874](https://git.ixigo.com/android/ixigo-android-sdk/commit/acdd87423d9f9d2e1b8e3630120538bc0e435046))

### [1.11.1](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.11.0...ixigo-sdk-1.11.1) (2022-01-20)


### Bug Fixes

* only show error view for errors loading webview url ([13a2e9e](https://git.ixigo.com/android/ixigo-android-sdk/commit/13a2e9e9d2f3b6f110a9e6c0a08ad51e367c5dd8))
* skip git hooks with standard release ([d7a2158](https://git.ixigo.com/android/ixigo-android-sdk/commit/d7a21581a5d1fbbe60cf1a88632b62a653bd9c1a))

## [1.11.0](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.10.1...ixigo-sdk-1.11.0) (2022-01-20)


### Features

* add Cheapest Fare ([f86316c](https://git.ixigo.com/android/ixigo-android-sdk/commit/f86316cea2f0026218aedd801e7c409472a6b814))


### Bug Fixes

* add missing SDK start logs for Bus and Trains ([395ded4](https://git.ixigo.com/android/ixigo-android-sdk/commit/395ded48560f024a3a1b7355138619104f5bd15b))
* fix Trains crash in sample app ([f8cf575](https://git.ixigo.com/android/ixigo-android-sdk/commit/f8cf575a485253aab20b0e3a926db47490b4cbc1))
* log webview errors ([8f0b0ac](https://git.ixigo.com/android/ixigo-android-sdk/commit/8f0b0ac24e9641586610a3723a76f1a76700deae))

### [1.10.1](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.10.0...ixigo-sdk-1.10.1) (2022-01-20)


### Bug Fixes

* fixes to BusSDK ([bb315ce](https://git.ixigo.com/android/ixigo-android-sdk/commit/bb315cec721085a116ef7013e8e0e29aa6a61c32))
* handle http errors ([8a206d0](https://git.ixigo.com/android/ixigo-android-sdk/commit/8a206d03c0b80a41af909678dddf5f92864e65e2))
* make progressBar use correct color ([90f88f5](https://git.ixigo.com/android/ixigo-android-sdk/commit/90f88f562d317021cd2f045ba42a99907ae5d57b))
* only show error state if we were loading a page ([f014267](https://git.ixigo.com/android/ixigo-android-sdk/commit/f014267432902424d2723153546daa970798a40f))

## [1.10.0](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.9.3...ixigo-sdk-1.10.0) (2022-01-19)


### Features

* add TrainsSDK ([e11998e](https://git.ixigo.com/android/ixigo-android-sdk/commit/e11998e3a5324efda35532e94924a544f4534268))

### [1.9.3](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.9.2...ixigo-sdk-1.9.3) (2022-01-19)


### Bug Fixes

* fix issue showing errorView while loading ([3e3b0f0](https://git.ixigo.com/android/ixigo-android-sdk/commit/3e3b0f02f7cee5001dc0b7a39c6aaca8901dc191))

### [1.9.2](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.9.1...ixigo-sdk-1.9.2) (2022-01-19)


### Bug Fixes

* add quit method to htmlOut ([cf73615](https://git.ixigo.com/android/ixigo-android-sdk/commit/cf73615d9b41a5493eee16a6b2613bcdfd096cf8))
* improve loadable error view ([734d77a](https://git.ixigo.com/android/ixigo-android-sdk/commit/734d77a206a69e2fc76378a328a9c648e704455b))
* show loading state by default ([365c57a](https://git.ixigo.com/android/ixigo-android-sdk/commit/365c57a0952b2c1061bd016d4e4fd49fdd5dddbf))

### [1.9.1](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.9.0...ixigo-sdk-1.9.1) (2022-01-17)


### Bug Fixes

* fix typo in JSON data for HtmlOut JS Interface ([a1a071e](https://git.ixigo.com/android/ixigo-android-sdk/commit/a1a071eb8c94a28e93078553309159f47ae0ced9))
* run slack release script when releasing and not in preview ([71fc575](https://git.ixigo.com/android/ixigo-android-sdk/commit/71fc575a55b8428765cd09cbe8c72a0911d51631))

## [1.9.0](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.8.0...ixigo-sdk-1.9.0) (2022-01-14)


### Features

* introduce bus sdk ([03c3961](https://git.ixigo.com/android/ixigo-android-sdk/commit/03c39617f2c43138b812b96cd8cbb2bcfea08a5e))
* introduce htmlOutJsInterface ([d6b556c](https://git.ixigo.com/android/ixigo-android-sdk/commit/d6b556c65b94ac8bbd1b829d51de8d0b0373c245))

## [1.8.0](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.7.0...ixigo-sdk-1.8.0) (2022-01-13)


### Features

* allow passing a requester to TokenProvider ([060be4b](https://git.ixigo.com/android/ixigo-android-sdk/commit/060be4bce8a0ccfb80348d517a75ecd9be80b169))

## [1.7.0](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.6.1...ixigo-sdk-1.7.0) (2022-01-13)


### Features

* add loading and error views to WebView ([a3ce91e](https://git.ixigo.com/android/ixigo-android-sdk/commit/a3ce91e6c8f48eef8b6c27033e5e99a1032cd7c2))
* log sdk type from IxigoSDK ([f20f200](https://git.ixigo.com/android/ixigo-android-sdk/commit/f20f2004ef2dcf2d828ffeb2c8bc89ae4b155e7a))


### Bug Fixes

* make partnerTokenProvider asynchronous ([2a6c4ee](https://git.ixigo.com/android/ixigo-android-sdk/commit/2a6c4eed5679b9c49e3eb3e1518bee0aeee8fda5))

### [1.6.1](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.6.0...ixigo-sdk-1.6.1) (2022-01-02)


### Bug Fixes

* correct quit behavior ([928697e](https://git.ixigo.com/android/ixigo-android-sdk/commit/928697ebe3ca24624d81d04c85b3b64134f07d8c))

## [1.6.0](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.5.0...ixigo-sdk-1.6.0) (2022-01-02)


### Features

* add multi model flight search ([73d93cf](https://git.ixigo.com/android/ixigo-android-sdk/commit/73d93cf04249a15aae607c552027b3055fe3d794))
* introduce ChainAnalyticsProvider ([4022f27](https://git.ixigo.com/android/ixigo-android-sdk/commit/4022f277a71d1690ec7c90847726c733a2d8a185))


### Bug Fixes

* read proper error message from SSOAuth error ([f38b165](https://git.ixigo.com/android/ixigo-android-sdk/commit/f38b1650d297d53dc4274caaec9958437980842b))
* remove webview delegate ([52bcb60](https://git.ixigo.com/android/ixigo-android-sdk/commit/52bcb60eea178383260131982220867670ae1c32))

## [1.5.0](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.4.0...ixigo-sdk-1.5.0) (2021-12-24)


### Features

* make UUID optional ([222a4ee](https://git.ixigo.com/android/ixigo-android-sdk/commit/222a4ee5e2497b64187df7f6203f11191768bfbc))


### Bug Fixes

* make app version a long ([41b2452](https://git.ixigo.com/android/ixigo-android-sdk/commit/41b2452b5ea420c692b440f8c1d78dd48169eb34))

## [1.4.0](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.3.0...ixigo-sdk-1.4.0) (2021-12-22)


### Features

* improve sample app ([129ddcd](https://git.ixigo.com/android/ixigo-android-sdk/commit/129ddcde480aa4848116b9301ef9e6a3861004b8))

## [1.3.0](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.2.3...ixigo-sdk-1.3.0) (2021-12-22)


### Features

* log sdkVersion as a dimension ([756856a](https://git.ixigo.com/android/ixigo-android-sdk/commit/756856aa514bfcb02e53570598aa0383a788e6e7))

### [1.2.3](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.2.1...ixigo-sdk-1.2.3) (2021-12-22)


### Bug Fixes

* correct version ([1777697](https://git.ixigo.com/android/ixigo-android-sdk/commit/1777697c7055948997782e960d4473823e79d81f))

### [1.2.1](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.2.0...ixigo-sdk-1.2.1) (2021-12-22)

## [1.2.0](https://git.ixigo.com/android/ixigo-android-sdk/compare/ixigo-sdk-1.1.0...ixigo-sdk-1.2.0) (2021-12-21)


### Features

* add flights metrics ([95dff42](https://git.ixigo.com/android/ixigo-android-sdk/commit/95dff42fc674ac24f76082ab0102f21cafabf2ea))
* add login and payment metrics ([9551e21](https://git.ixigo.com/android/ixigo-android-sdk/commit/9551e21e88c236835a615e0ebf25ab9124ef4925))
* introduce google analytics ([0f8a2d3](https://git.ixigo.com/android/ixigo-android-sdk/commit/0f8a2d3eea234fd96443024f74fc0471a46b9f83))


### Bug Fixes

* revert apkscale working ([76170bb](https://git.ixigo.com/android/ixigo-android-sdk/commit/76170bbed3fcfce76da7ec42a8063a095033f1d7))

## 1.1.0 (2021-12-17)


### Features

* add openWindow JS bridge functionality to webviews ([c534eed](https://git.ixigo.com/android/ixigo-android-sdk/commit/c534eed4ae54b2ed0a5b3fbbb6f8787649fdc793))
* **build:** introduce commitlint ([a7b9935](https://git.ixigo.com/android/ixigo-android-sdk/commit/a7b9935f0197c841907b9a5a1b6ff08ad37ef1b3))


### Bug Fixes

* add equals method for Result class ([426b91e](https://git.ixigo.com/android/ixigo-android-sdk/commit/426b91e884c6fc1de4ce312d9054e2c3f6307f65))
* allow throwable in Result.Err ([377a938](https://git.ixigo.com/android/ixigo-android-sdk/commit/377a93864e0dcc1474ebd40d144a770e533e4685))
* do not enable webview debug baesd on build type ([79a76b7](https://git.ixigo.com/android/ixigo-android-sdk/commit/79a76b7afbf1742225453f39cb143f9bc2914205))
* **payment:** allow paymentprovider to receive activity results ([8c7815e](https://git.ixigo.com/android/ixigo-android-sdk/commit/8c7815e4942257758495020cf39e3a59fd5abe24))
* use light theme for activity ([fd981bb](https://git.ixigo.com/android/ixigo-android-sdk/commit/fd981bb71b4306d5d6bdb913b600d3cb2b841818))

### [1.0.1](https://git.ixigo.com/android/ixigo-android-sdk/compare/flights-sdk-1.0.0...flights-sdk-1.0.1) (2021-11-30)


### Bug Fixes

* add equals method for Result class ([426b91e](https://git.ixigo.com/android/ixigo-android-sdk/commit/426b91e884c6fc1de4ce312d9054e2c3f6307f65))
* allow throwable in Result.Err ([377a938](https://git.ixigo.com/android/ixigo-android-sdk/commit/377a93864e0dcc1474ebd40d144a770e533e4685))
* **payment:** allow paymentprovider to receive activity results ([8c7815e](https://git.ixigo.com/android/ixigo-android-sdk/commit/8c7815e4942257758495020cf39e3a59fd5abe24))
* use light theme for activity ([fd981bb](https://git.ixigo.com/android/ixigo-android-sdk/commit/fd981bb71b4306d5d6bdb913b600d3cb2b841818))

## 1.0.0 (2021-11-16)


### Features

* **build:** introduce commitlint ([a7b9935](https://git.ixigo.com/android/ixigo-android-sdk/commit/a7b9935f0197c841907b9a5a1b6ff08ad37ef1b3))


### Bug Fixes

* do not enable webview debug baesd on build type ([79a76b7](https://git.ixigo.com/android/ixigo-android-sdk/commit/79a76b7afbf1742225453f39cb143f9bc2914205))
