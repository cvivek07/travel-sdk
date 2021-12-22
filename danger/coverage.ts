// We can't import danger here so we create the type ourselfs
// https://spectrum.chat/danger/javascript/danger-js-actually-runs-your-imports-as-globals~0a005b56-31ec-4919-9a28-ced623949d4d
declare function markdown(string: String);

export interface CoverageRow {
  name: string;
  data: CoverageColumn[];
}

export interface CoverageColumn {
  name: string;
  value: string;
}

export interface CoverageGroup {
  name: string;
  rows: CoverageRow[];
}

export function coverageTables(groups: CoverageGroup[]) {
  const markdownTables = groups
    .map((group) => coverageTable(group))
    .join("\n\n");
  markdown("# Test Coverage\n" + markdownTables);
}

function coverageTable(group: CoverageGroup): string {
  const { rows } = group;
  const columns = getColumns(rows);
  if (columns.length == 0) {
    return `## ${group.name}
No coverage info found.`;
  }

  const header = `| Project | ${columns.join(" | ")} |`;
  const headerSeparator = `| --- | ${columns.map(() => "---").join("|")} |`;

  const projectRows = rows.map((row) => {
    const coverageData = columns.map((column) => getDataCoverage(row, column));
    return `| ${row.name} | ${coverageData.join(" | ")} |`;
  });

  return `## ${group.name}
${header}
${headerSeparator}
${projectRows.join("\n")}`;
}

function getDataCoverage(project: CoverageRow, dataName: string): string {
  const data = project.data.find((data) => data.name == dataName);
  if (!data) {
    return "-";
  }
  return data.value;
}

function getColumns(projects: CoverageRow[]): string[] {
  const columns: Array<string> = [];
  for (let project of projects) {
    for (let data of project.data) {
      if (!columns.includes(data.name)) {
        columns.push(data.name);
      }
    }
  }
  return columns;
}
