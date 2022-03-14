import { slack } from "@ixigo-packages/ixigo-danger-common";

const channel = "C02RJSVUMLN"; // #ixigo-mobile-sdk-builds

(async () => {
  await slack.uploadFile("app/build/**/apk/debug/*.apk", getMessage(), channel);
})();

function getMessage(): string {
  return `*Ixigo Android SDK Test App*
  *User:* @${process.env.GITLAB_USER_LOGIN}
  *Branch:* <${process.env.CI_PROJECT_URL}/-/tree/${process.env.CI_COMMIT_BRANCH}|${process.env.CI_COMMIT_BRANCH}>
  *Commit:* <${process.env.CI_PROJECT_URL}/-/commit/${process.env.CI_COMMIT_SHA}|${process.env.CI_COMMIT_TITLE}>
  *Pipeline*: <${process.env.CI_PIPELINE_URL}|${process.env.CI_PIPELINE_ID}>
  `;
}
