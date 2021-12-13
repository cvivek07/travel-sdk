import { scan } from "danger-plugin-lint-report";
import {
  checkMergeRequestSize,
  commitlint,
} from "@ixigo-packages/ixigo-danger-common";
import { diffCoverage } from "./diffCoverage";

(async () => {
  await checkMergeRequestSize();
  await scan({
    fileMask: "**/reports/lint-results*.xml",
    reportSeverity: true,
    requireLineModification: true,
  });
  await commitlint();
  await diffCoverage();
})();
