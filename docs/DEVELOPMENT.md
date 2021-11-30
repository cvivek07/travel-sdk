# Development

## Setup development environment

1. Add a personal Gitlab Token as an environment variable called `NPM_TOKEN`
   1. This is needed to use [Gitlab NPM Registry](https://docs.gitlab.com/ee/user/packages/npm_registry/) for some custom npm dependencies needed for build process.
   1. [Create the personal token](https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html#create-a-personal-access-token)
   1. Edit your `bash_profile` file (or `.zprofile` or which ever shell you use) and add `export NPM_TOKEN=<GITLAB_PERSONAL_TOKEN>`
1. Install [yarn](https://classic.yarnpkg.com/lang/en/)
   1. I used homebrew: `brew install yarn`
1. Clone the repository and initialize
   1. `git clone git@git.ixigo.com:android/ixigo-android-sdk.git`
   1. `cd ixigo-android-sdk.git`
   1. `yarn install`

## Create a new commit

We use [standard-version](https://github.com/conventional-changelog/standard-version) to handle commits and releases.

This means our commits need to follow the standard message. If you don't, you might find a pre-commit hook that prevents your commit.

To create a commit that always complies you can create it using `yarn run commit`. This will guide you through creating a compliant ommit message.

## Release a new version

To release a new version:

1. Find the pipeline for the commit you want to release in [development` branch](https://git.ixigo.com/android/ixigo-android-sdk/-/pipelines?page=1&scope=all&ref=development&status=success) and click on it.
2. To preview the changelog that will be generated:
3. Find the job in `release` stage called `release-preview`
4. Browse the artifacts and open `build/CHANGELOG.pdf`. For instance, if the `release-preview` id job of your job is `123456`, the preview of the changelog will be at https://git.ixigo.com/android/ixigo-android-sdk/-/jobs/123456/artifacts/file/build/CHANGELOG.pdf
5. In the pipeline page, click on ▶ in the `release` job to release the sdk

![](images/release_screenshot.png)
