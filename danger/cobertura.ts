import { parseStringPromise } from "xml2js";
import { promises as fs } from "fs";
import { CoverageRow } from "./coverage";
import isObject from "is-object";

declare function message(string: String);
declare function fail(string: String);
declare function warn(string: String);

export interface CoberturaReport {
  lineRate: number;
  branchRate: number;
  complexity: number;
  hasChanges: boolean;
}

export type MessageSeverity = "message" | "warn" | "fail";

export interface CoberturaThreshold {
  minLineRate?: number;
  minBranchRate?: number;
  maxComplexity?: number;
  severity?: MessageSeverity;
}

const emptyTag = "";

export async function parseCobertura(path: string): Promise<CoberturaReport> {
  const data = await fs.readFile(path);

  const { coverage } = await parseStringPromise(data, { emptyTag });
  return {
    lineRate: Number(coverage.$["line-rate"]),
    branchRate: Number(coverage.$["branch-rate"]),
    complexity: Number(coverage.$["complexity"]),
    hasChanges: hasChanges(coverage),
  };
}

/**
 * Only consider changes if there are any lines in the changes
 */
function hasChanges(coverage: any) {
  if (!coverage.packages) {
    return false;
  }

  if (!coverage.packages[0].package) {
    return false;
  }
  for (const pkg of coverage.packages[0].package) {
    if (!isObject(pkg)) {
      continue;
    }
    if (!pkg.classes) {
      continue;
    }
    for (const classes of pkg.classes) {
      if (!isObject(classes)) {
        continue;
      }
      for (const clazz of classes.class) {
        if (clazz.lines.length > 0 && clazz.lines[0] != emptyTag) {
          return true;
        }
      }
    }
  }
  return false;
}

export function coverageTableRow(
  name: string,
  report: CoberturaReport
): CoverageRow {
  return {
    name,
    data: [
      {
        name: "Line Rate",
        value: toPerc(report.lineRate),
      },
      {
        name: "Branch Rate",
        value: toPerc(report.branchRate),
      },
      {
        name: "Complexity",
        value: report.complexity.toString(),
      },
    ],
  };
}

export async function checkCobertura(
  name: string,
  report: CoberturaReport,
  threshold: CoberturaThreshold
) {
  minValueCheck(
    `branch coverage for ${name}`,
    report.branchRate,
    threshold.minBranchRate,
    threshold.severity
  );
  minValueCheck(
    `line coverage for ${name}`,
    report.lineRate,
    threshold.minLineRate,
    threshold.severity
  );
  maxValueCheck(
    `complexity for ${name}`,
    report.complexity,
    threshold.maxComplexity,
    threshold.severity
  );
}

function minValueCheck(
  name: string,
  value: number,
  minThreshold?: number,
  severity?: MessageSeverity
) {
  if (minThreshold != undefined && value < minThreshold) {
    addMessage(
      `Current ${name} (${toPerc(
        value
      )}) is lower than the minimum threshold (${toPerc(minThreshold)})`,
      severity
    );
  }
}

function maxValueCheck(
  name: string,
  value: number,
  maxThreshold?: number,
  severity?: MessageSeverity
) {
  if (maxThreshold != undefined && value > maxThreshold) {
    addMessage(
      `Current ${name} (${toPerc(
        value
      )}) is higher than the maximum threshold (${toPerc(maxThreshold)})`,
      severity
    );
  }
}

function toPerc(rate: number): string {
  return `${(rate * 100).toFixed(2)}%`;
}

function addMessage(title: string, severity?: MessageSeverity) {
  switch (severity) {
    case "message":
      message(title);
      break;
    case "warn": {
      warn(title);
      break;
    }
    case "fail":
    default:
      fail(title);
  }
}
