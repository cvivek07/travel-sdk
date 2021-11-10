import { scan } from "danger-plugin-lint-report";
import { checkMergeRequestSize } from "@ixigo-packages/danger-plugin-merge-request-size";
import commitlint from "danger-plugin-conventional-commitlint";
import configConventional from "@commitlint/config-conventional";

const mergeRequestSizeOptions = {
  rules: [
    {
      minSize: 0,
      maxSize: 50,
      name: "x-small",
      type: "message",
    },
    {
      minSize: 50,
      maxSize: 100,
      name: "small",
      type: "message",
    },
    {
      minSize: 100,
      maxSize: 150,
      name: "medium",
      type: "warn",
    },
    {
      minSize: 150,
      maxSize: Number.MAX_SAFE_INTEGER,
      name: "large",
      type: "warn",
    },
  ],
};

(async () => {
  await checkMergeRequestSize(mergeRequestSizeOptions);
  await scan({
    fileMask: "**/reports/lint-results*.xml",
    reportSeverity: true,
    requireLineModification: true,
  });
  await commitlint(configConventional.rules, {
    severity: "warn",
  });
})();
