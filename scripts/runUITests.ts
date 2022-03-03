import { browserStack } from "@ixigo-packages/ixigo-danger-common";

(async () => {
  const apkFile = "app/**/apk/debug/**/*.apk";
  const apkTestSuiteFile = "app/**/apk/androidTest/debug/*.apk";
  await browserStack.runEspressoTests(
    apkFile,
    apkTestSuiteFile,
    "ixigosdk-sampleapp"
  );
})();
