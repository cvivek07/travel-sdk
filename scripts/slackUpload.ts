import { App, LogLevel } from "@slack/bolt";
import fs from "fs";
import glob from "glob-promise";

const channel = "C02RJSVUMLN"; // #ixigo-mobile-sdk-builds
const token = process.env.SLACK_TOKEN;
const signingSecret = process.env.SLACK_SIGNING_SECRET;
const app = new App({
  token,
  signingSecret,
  logLevel: LogLevel.WARN,
});

(async () => {
  await uploadToSlack();
})();

async function uploadToSlack() {
  const files = await glob("app/build/**/*.apk");
  if (files.length < 1) {
    throw "Could not find apk file";
  }
  const file = files[0];
  console.log(`Uploading ${file}`);

  const stream = fs.createReadStream(file);
  const message = await getMessage();
  await app.client.files.upload({
    channels: channel,
    file: stream,
    initial_comment: message,
  });
}

async function getMessage(): Promise<string> {
  const message = `*Ixigo Android SDK Test App*
  *User:* @${process.env.GITLAB_USER_LOGIN}
  *Branch:* <${process.env.CI_PROJECT_URL}/-/tree/${process.env.CI_COMMIT_BRANCH}|${process.env.CI_COMMIT_BRANCH}>
  *Commit:* <${process.env.CI_PROJECT_URL}/-/commit/${process.env.CI_COMMIT_SHA}|${process.env.CI_COMMIT_TITLE}>
  *Pipeline*: <${process.env.CI_PIPELINE_URL}|${process.env.CI_PIPELINE_ID}>
  `;
  const userIdMap = await getSlackUserIdMap();
  return message.replace(/@([a-zA-Z0-9_]*)/g, (match, username) => {
    const userId = userIdMap.get(username);
    if (userId) {
      return `<@${userId}>`;
    } else {
      return match;
    }
  });
}

async function getSlackUserIdMap(): Promise<Map<string, string>> {
  const response = await app.client.users.list();
  if (!response.ok) {
    return new Map();
  }
  return response.members.reduce((map, member) => {
    map.set(member.name, member.id);
    return map;
  }, new Map());
}
