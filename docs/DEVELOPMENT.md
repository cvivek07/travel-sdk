[[_TOC_]]

## [SDK Architecture](https://ixigodev.atlassian.net/wiki/spaces/~922755443/pages/2793930759/ixigo+SDK+architecture)

You can read about design and architecture of the sdk [here](https://ixigodev.atlassian.net/wiki/spaces/~922755443/pages/2793930759/ixigo+SDK+architecture)

## Setup development environment

1. Add a personal Gitlab Token as an environment variable called `NPM_TOKEN`
   1. This is needed to use [Gitlab NPM Registry](https://docs.gitlab.com/ee/user/packages/npm_registry/) for some custom npm dependencies needed for build process.
   1. [Create the personal token](https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html#create-a-personal-access-token)
   1. Add it as an environment variable: `export NPM_TOKEN=<GITLAB_PERSONAL_TOKEN>`
   1. Edit your `bash_profile` file (or `.zprofile` or which ever shell you use) and add `export NPM_TOKEN=<GITLAB_PERSONAL_TOKEN>` to make sure the environment variable is added in every new shell session.
1. Install [yarn](https://classic.yarnpkg.com/lang/en/)
   1. I used homebrew: `brew install yarn`
1. Clone the repository and initialize
   1. `git clone git@git.ixigo.com:android/ixigo-android-sdk.git`
   1. `cd ixigo-android-sdk.git`
   1. `yarn install`
1. Install latest [Android Studio](https://developer.android.com/studio/install)
1. Import `ixigo-android-sdk` project in Android Studio
1. Run the sample App

## Troubleshooting

### 404 accessing npm package 

```shell
An unexpected error occurred: "https://git.ixigo.com/api/v4/projects/536/packages/npm/@ixigo-packages/ixigo-danger-common/-/@ixigo-packages/ixigo-danger-common-2.2.0.tgz: Request failed \"404 Not Found\"".
```

We use [Gitlab NPM Registry](https://docs.gitlab.com/ee/user/packages/npm_registry/) to host certain npm packages (`ixigo-danger-common` in the example error above). If you get a 404 when running `yarn setup` it is most likely that you don't have permission for a specific package. 

Reach out to @miguel or use [HRMS](https://hrms.ixigo.com/) to request access to that repository. 

Once you have access, run `yarn setup` again.

## Development

Most of the development should be done agains Unit Tets and against our Sample App.

![](images/sample_app.png)

### Development inside other Apps

If you want to modify ixigo-sdk and test it quickly inside other Apps, we recommend publishing ixigo-sdk to your maven local repository and configure your app to read from maven local repository.

To push to your local maven repository:

**Release**
```shell
./gradlew ixigo-sdk:publishReleasePublicationToMavenLocal
```

**Snapshot**
```shell
./gradlew ixigo-sdk:publishSnapshotPublicationToMavenLocal
```

Once it is pushed in your local maven repository, configure your App to use `mavenLocal()` giving it a higher priority than `ixigo-sdk` repository

```groovy
repositories {
  mavenLocal()
  ...
  // ixigo-sdk repository
}
```


## Code Format

We use [ktfmt](https://github.com/facebookincubator/ktfmt) to format our code.

We use [spotless](https://github.com/diffplug/spotless) in gradle to run ktfmt.

We enforce it via:

- **Git pre-commit hook:** installed via `yarn install` and running `spotlessApply` gradle task to automatically format files
- **CI**: invoking `spotlessCheck` gradle task and failing the build if there are formatting errors.

If you want, you can also install ktfmt plugin for Android Studio. Find instructions [here](https://github.com/facebookincubator/ktfmt#intellij-android-studio-and-other-jetbrains-ides)

## Create a new commit

We use [standard-version](https://github.com/conventional-changelog/standard-version) to handle commits and releases.

This means our commits need to follow the standard message. If you don't, you might find a pre-commit hook that prevents your commit.

To create a commit that always complies you can create it using `yarn run commit`. This will guide you through creating a compliant ommit message.

## Analytics

Metrics of the SDK are collected using [Google Analytics](https://developers.google.com/analytics/devguides/collection/android/v4) Legacy (not GA4). The rationale for this is:

- We can't use Firebase Analytics since it only works with 1 Firebase App, and analytics would go to the host Firebase App.
- GA4 for mobile forces you to use Firebase

Analytics Account Id: [211766096](https://analytics.google.com/analytics/web/#/report-home/a211766096w295339199p256394053). Contact miguel@travenues (or rajnish@travenues) for access if needed.

### Dashboard

You can access dashboard here: https://datastudio.google.com/reporting/d875166c-bda3-4108-8aa4-868843c4b1e8/page/UqEiC

## RemoteConfig

It is possible to configure certain aspects of the SDK using Firebase Remote Config.

The Firebase App is [Ixigo SDK](https://console.firebase.google.com/project/ixigo-sdk-demo-app). Contact miguel@travenues or rajnish@travenues for access if needed.

### Create RemoteConfig Host App overrides

You can create remote config overrides for specific client Apps in your Remote Config. To do so:

1. We need to [create a Firebase App](https://console.firebase.google.com/project/ixigo-sdk-demo-app/settings/general) to identify each client.
    - PackageName is not important. We just want the FirebaseAppId
2. Once you have created the firebase App, copy the Firebase App Id (`eg: 1:132902544575:android:9270763a13b544f571120a`) and add it alongside ixigo's app `clientId` to the Remote Config with key **clientId_to_firebaseAppId**

![](images/remote_config_app_override.png)

## Release a new version

To release a new version:

1. Find the pipeline for the commit you want to release in [development` branch](https://git.ixigo.com/android/ixigo-android-sdk/-/pipelines?page=1&scope=all&ref=development&status=success) and click on it.
2. To preview the changelog that will be generated:
3. Find the job in `release` stage called `release-preview`
4. Browse the artifacts and open `build/CHANGELOG.pdf`. For instance, if the `release-preview` id job of your job is `123456`, the preview of the changelog will be at https://git.ixigo.com/android/ixigo-android-sdk/-/jobs/123456/artifacts/file/build/CHANGELOG.pdf
5. In the pipeline page, click on ▶ in the `release` job to release the sdk

![](images/release_screenshot.png)
