import { promise as glob } from "glob-promise";
import * as path from "path";
import {
  checkCobertura,
  CoberturaThreshold,
  coverageTableRow,
  parseCobertura,
} from "./cobertura";
import { CoverageRow, coverageTables } from "./coverage";

const threshold: CoberturaThreshold = {
  minLineRate: 0.9,
};

export async function diffCoverage() {
  const files = await glob("**/cobertura.xml");

  const rows: CoverageRow[] = [];
  for (const file of files) {
    const moduleName = file.split(path.sep)[0];
    const report = await parseCobertura(file);
    if (report.hasChanges) {
      checkCobertura(moduleName, report, threshold);
      rows.push(coverageTableRow(moduleName, report));
    }
  }

  await coverageTables([
    {
      name: "Diff Coverage",
      rows,
    },
  ]);
}
