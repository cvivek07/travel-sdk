import { App, LogLevel } from "@slack/bolt";
import fs from "fs";
import glob from "glob-promise";

const channel = "C02P9A6JQ0J"; // #ixigo-mobile-sdk
const token = process.env.SLACK_TOKEN;
const signingSecret = process.env.SLACK_SIGNING_SECRET;

(async () => {
  await uploadToSlack();
})();

async function uploadToSlack() {
  const app = new App({
    token,
    signingSecret,
    logLevel: LogLevel.WARN,
  });

  const files = await glob("app/build/**/*.apk");
  if (files.length < 1) {
    throw "Could not find apk file";
  }
  const file = files[0];
  console.log(`Uploading ${file}`);

  const stream = fs.createReadStream(file);
  await app.client.files.upload({
    channels: channel,
    file: stream,
    initial_comment: `*Ixigo Android SDK Test App*
    *User:* @${process.env.GITLAB_USER_LOGIN}
    *Branch:* <${process.env.CI_PROJECT_URL}/-/tree/${process.env.CI_COMMIT_BRANCH}|${process.env.CI_COMMIT_BRANCH}>
    *Commit:* <${process.env.CI_PROJECT_URL}/-/commit/${process.env.CI_COMMIT_SHA}|${process.env.CI_COMMIT_TITLE}>
    *Pipeline*: <${process.env.CI_PIPELINE_URL}|${process.env.CI_PIPELINE_ID}>
    `,
  });
}
