import { readCurrentVersion } from "../.versionrc.js";
import { slack } from "@ixigo-packages/ixigo-danger-common";

const channel = "C02P9A6JQ0J"; // #ixigo-mobile-sdk

(async () => {
  const message = await getMessage();

  await slack.uploadFile("build/**/CHANGELOG.pdf", message, channel);
})();

async function getMessage(): Promise<string> {
  const version = await readCurrentVersion();
  return `*Ixigo Android SDK ${version} released*
  *User:* @${process.env.GITLAB_USER_LOGIN}
  `;
}
