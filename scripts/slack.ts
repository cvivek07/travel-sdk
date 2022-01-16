import { App, LogLevel } from "@slack/bolt";
import fs from "fs";
import glob from "glob-promise";

const channel = "C02P9A6JQ0J"; // #ixigo-mobile-sdk
const token = process.env.SLACK_TOKEN;
const signingSecret = process.env.SLACK_SIGNING_SECRET;
const app = new App({
  token,
  signingSecret,
  logLevel: LogLevel.WARN,
});

export async function uploadFile(
  fileGlob: string,
  message: string,
  channel: string
) {
  const files = await glob(fileGlob);
  if (files.length < 1) {
    throw `Could not find file to upload for glob=${fileGlob}`;
  }
  const file = files[0];
  console.log(`Uploading ${file}`);

  const stream = fs.createReadStream(file);
  const linkedMessage = await linkUserNames(message);
  await app.client.files.upload({
    channels: channel,
    file: stream,
    initial_comment: linkedMessage,
  });
}

async function linkUserNames(message: string): Promise<string> {
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
