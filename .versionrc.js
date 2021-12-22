const readmeUpdater = {
  readVersion: function (contents) {
    const match = /com\.ixigo\.sdk:ixigo-sdk:(\d+\.\d+\.\d+)/.exec(contents);
    const version = match[1];
    return version;
  },
  writeVersion: function (contents, version) {
    return contents.replace(
      /(.*com\.ixigo\.sdk:ixigo-sdk:)(\d+\.\d+\.\d+)(.*)/g,
      `\$1${version}\$3`
    );
  },
};

const readmeTracker = {
  filename: "docs/USAGE.md",
  updater: readmeUpdater,
};

const gradleUpdater = {
  filename: "ixigo-sdk/build.gradle",
  updater:
    "node_modules/@damlys/standard-version-updater-gradle/dist/build-gradle.js",
};
module.exports = {
  bumpFiles: [readmeTracker, gradleUpdater],
  packageFiles: [gradleUpdater],
};
