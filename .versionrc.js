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
  readVersion: function (contents) {
    const match = /version\s*=\s*[?:'|"](\d+\.\d+\.\d+)[?:'|"]/.exec(contents);
    const version = match[1];
    return version;
  },
  writeVersion: function (contents, version) {
    return contents.replace(
      /(.*version\s*=\s*[?:'|"])(\d+\.\d+\.\d+)([?:'|"].*)/,
      `\$1${version}\$3`
    );
  },
};

const gradleDependencyUpdater = {
  readVersion: function (contents) {
    const match = /com\.ixigo\.sdk:ixigo-sdk:(\d+\.\d+\.\d+)/.exec(contents);
    const version = match[1];
    return version;
  },
  writeVersion: function (contents, version) {
    return contents.replace(
      /(.*com\.ixigo\.sdk:ixigo-sdk:)(\d+\.\d+\.\d+)(.*)/,
      `\$1${version}\$3`
    );
  },
};

const gradleTracker = {
  filename: "ixigo-sdk/build.gradle",
  updater: gradleUpdater,
};

const gradleAppDependencyTracker = {
  filename: "app/build.gradle",
  updater: gradleDependencyUpdater,
};

const gradleAppTracker = {
  filename: "app/build.gradle",
  updater: gradleUpdater,
};

module.exports = {
  bumpFiles: [readmeTracker, gradleTracker, gradleAppTracker, gradleAppDependencyTracker],
  packageFiles: [gradleTracker],
};
