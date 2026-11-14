import fs from "node:fs/promises";
import path from "node:path";
import { pathToFileURL } from "node:url";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const workspaceDir =
  "/Users/zhouyiming/Documents/work/micro_lesson/lesson";
const skillDir =
  "/Users/zhouyiming/.codex/plugins/cache/openai-primary-runtime/presentations/26.905.11957/skills/presentations";
const sourcePath = path.join(
  workspaceDir,
  ".codex-ppt-build/source-compatible.pptx",
);
const originalSourcePath = path.join(
  workspaceDir,
  "outputs/智驾伴侣_期中项目汇报_可编辑.pptx",
);
const finalPath = path.join(
  workspaceDir,
  "outputs/智驾伴侣_期中项目汇报_周轩亦_2435050112.pptx",
);
const stagingDir = path.join(workspaceDir, ".codex-finalizer");
const previewDir = path.join(
  workspaceDir,
  ".codex-ppt-artifact/rendered-final",
);

const FONT = "Hiragino Sans GB";
const C = {
  bg: "EEF8FD",
  white: "FFFFFF",
  ink: "0B2846",
  inkSoft: "29485F",
  muted: "627A8D",
  blue: "2F7DBB",
  blueDeep: "1D5E8C",
  sky: "71B9E6",
  skyPale: "D7EDF8",
  teal: "2E97A3",
  line: "C4DEEB",
  amber: "D98B36",
  band: "E7F4FA",
};

const narration = [
  "各位老师好，我是周轩亦，学号 2435050112。我的期中项目叫智驾伴侣，面向车主提供车辆状态、告警、驾驶行为和碳排数据服务。本次汇报重点说明项目从单体应用拆分成五个微服务后，业务边界如何划分、服务之间如何协作，以及目前完成了哪些部署和验证。",
  "报告按五个部分展开。第一部分交代项目要解决什么问题，以及三类套餐分别覆盖哪些能力。第二部分说明当前技术架构。第三部分展示已经落地的代码、部署和验证结果。第四部分说明开发过程中遇到的问题和解决方式。最后说明尚未完成的能力，以及后续计划的先后顺序。",
  "智驾伴侣的核心用户是车主。平台以车辆档案为入口，围绕车辆状态、告警、驾驶事件和月度行车数据提供查询与分析。轻量版保留基础状态和告警列表，标准版增加告警分级、驾驶行为和月报，豪华版再增加碳排与高级体验。当前汇报主要聚焦后端服务能力，套餐矩阵用于说明不同业务能力的边界。",
  "当前系统由五个 Spring Boot 微服务组成。用户服务管理车主与套餐，车辆服务管理车辆档案，告警服务保存告警并校验等级，驾驶行为服务保存驾驶事件并生成月报，碳排服务计算月度碳排放。五个服务各自连接独立数据库，只保存对方的业务 ID。服务统一注册到 Nacos 的 COURSE_GROUP，调用方通过服务名获取实例，不依赖固定 IP。",
  "代码落地时，我先保证每个服务只操作自己的数据库。告警服务创建告警前会校验车辆是否存在，当前通过 OpenFeign 调用车辆服务并配置 fallback；驾驶行为和碳排服务使用 DiscoveryClient 找到实例，再用 RestTemplate 发起请求。业务规则包括告警等级只能是 1 到 3、车辆不存在返回 400、依赖服务不可用返回 503，只有校验通过才写入本地数据库。",
  "部署方面，五个服务都通过多阶段 Dockerfile 打包成镜像，Docker Compose 统一管理业务服务、五个数据库和 Nacos。截图显示 COURSE_GROUP 下的五个服务都有健康实例。测试方面，五个服务共 11 个测试全部通过，测试使用 H2 内存数据库并关闭 Nacos 注册，不依赖真实运行环境。smoke-test 检查健康接口，verify-discovery 验证正常调用、车辆服务下线返回 503，以及恢复后重新返回 200。",
  "开发中主要遇到三类问题。第一是 Spring Boot 和 Spring Cloud Alibaba 版本不匹配，最后统一到 Spring Boot 3.2、Java 17 和对应的版本管理。第二是 Nacos 3 的控制台、HTTP 服务和 SDK gRPC 使用不同端口，所以分别配置为 18080、8848 和 9848。第三是依赖服务失效时可能产生无效数据，因此增加了 fallback、实例判断和状态码处理，在写库之前阻断请求。",
  "目前还没有做前端、BFF 聚合层、3D 与 AI 能力，也没有引入 Seata、消息队列和完整监控。后续先补 API 网关，统一入口和鉴权；再用 Nacos Config 管理环境配置；最后加入链路追踪和指标监控，并根据业务需要引入分布式事务或消息机制。",
  "以上是智驾伴侣期中项目的汇报。项目已经完成五个微服务的基本拆分、数据归属、跨服务调用、容器部署和服务发现验证。感谢老师，欢迎批评指正。",
];

function addText(slide, text, left, top, width, height, style = {}) {
  const shape = slide.shapes.add({
    geometry: "textbox",
    position: { left, top, width, height },
    fill: "none",
    line: { style: "solid", fill: "none", width: 0 },
  });
  shape.text = text;
  shape.text.style = {
    typeface: FONT,
    fontSize: 16,
    color: C.ink,
    verticalAlignment: "middle",
    margin: 0,
    ...style,
  };
  return shape;
}

function styleCell(cell, options) {
  cell.fill = options.fill;
  cell.text.style = {
    typeface: FONT,
    fontSize: options.fontSize ?? 16,
    bold: options.bold ?? false,
    color: options.color ?? C.inkSoft,
    alignment: options.alignment ?? "left",
    verticalAlignment: "middle",
  };
}

const presentation = await PresentationFile.importPptx(
  await FileBlob.load(sourcePath),
);
const slides = presentation.slides.items;

// Cover identity
const reporter = presentation.resolve("sh/fu94fe98");
reporter.text = "汇报人：周轩亦  ·  学号：2435050112";

// Slide 3: improve table contrast and remove a stranded terminal period.
const packageTable = presentation.resolve("tb/kfmt0ru1");
styleCell(packageTable.getCell(0, 0), {
  fill: C.blueDeep,
  color: C.white,
  bold: true,
  fontSize: 15,
});
presentation.resolve("sh/83ulovat").text =
  "轻量版提供基础状态\n标准版增加告警分级与驾驶分析\n豪华版提供碳排及高级体验";

// Slide 4: replace the crowded architecture layout with a full-width service
// table and a separate relationship section.
const architectureSlide = slides[3];
architectureSlide.shapes.deleteAll();

addText(architectureSlide, "02", 72, 44, 52, 28, {
  fontSize: 14,
  bold: true,
  color: C.blue,
});
addText(architectureSlide, "ARCHITECTURE", 123, 43, 220, 28, {
  fontSize: 15,
  bold: true,
  color: C.teal,
});
addText(architectureSlide, "当前技术架构", 66, 77, 820, 53, {
  fontSize: 38,
  bold: true,
  color: C.ink,
  verticalAlignment: "top",
});
addText(
  architectureSlide,
  "五个服务按业务边界拆分，每个服务连接独立数据库",
  72,
  132,
  860,
  26,
  {
    fontSize: 15,
    color: C.muted,
    verticalAlignment: "top",
  },
);
addText(architectureSlide, "核心服务与数据归属", 72, 172, 300, 22, {
  fontSize: 13,
  bold: true,
  color: C.blueDeep,
});

const services = [
  ["user-service", "8081", "车主与套餐", "user_db"],
  ["vehicle-service", "8082", "车辆档案", "vehicle_db"],
  ["alert-service", "8083", "告警记录与等级校验", "alert_db"],
  [
    "driving-behavior-service",
    "8084",
    "驾驶事件、评分与月报",
    "driving_db",
  ],
  ["carbon-service", "8085", "碳排计算与历史报告", "carbon_db"],
];

const serviceTable = architectureSlide.tables.add({
  rows: 6,
  columns: 4,
  left: 72,
  top: 198,
  width: 1136,
  height: 258,
  columnWidths: [390, 108, 355, 283],
  values: [
    ["业务服务", "端口", "主要职责", "独立数据库"],
    ...services,
  ],
});
serviceTable.borders.assign({
  style: "solid",
  fill: C.line,
  width: 1,
});
serviceTable.cells
  .block({ row: 0, column: 0, rowCount: 6, columnCount: 4 })
  .assign({
    anchor: "middle",
    margins: { top: 7, right: 10, bottom: 7, left: 10 },
  });
for (let column = 0; column < 4; column += 1) {
  styleCell(serviceTable.getCell(0, column), {
    fill: C.blueDeep,
    color: C.white,
    bold: true,
    fontSize: 15,
    alignment: column === 2 || column === 3 ? "left" : "center",
  });
}
for (let row = 1; row < 6; row += 1) {
  const fill = row % 2 === 1 ? C.white : C.band;
  styleCell(serviceTable.getCell(row, 0), {
    fill,
    color: C.blueDeep,
    bold: true,
    fontSize: 15,
  });
  styleCell(serviceTable.getCell(row, 1), {
    fill,
    color: C.muted,
    fontSize: 14,
    alignment: "center",
  });
  styleCell(serviceTable.getCell(row, 2), {
    fill,
    color: C.inkSoft,
    fontSize: 15,
  });
  styleCell(serviceTable.getCell(row, 3), {
    fill,
    color: C.inkSoft,
    fontSize: 15,
  });
}

addText(architectureSlide, "数据边界", 72, 485, 180, 22, {
  fontSize: 13,
  bold: true,
  color: C.teal,
});
addText(
  architectureSlide,
  "每个服务只读写自己的数据库，跨服务只保存业务 ID。\n不建立跨库外键，也不访问其他服务的 Repository。",
  72,
  512,
  535,
  58,
  {
    fontSize: 15,
    color: C.inkSoft,
    verticalAlignment: "top",
  },
);
addText(architectureSlide, "远程依赖", 660, 485, 180, 22, {
  fontSize: 13,
  bold: true,
  color: C.amber,
});
addText(
  architectureSlide,
  "告警、驾驶行为：调用 vehicle-service 校验车辆\n碳排：调用 driving-behavior-service 读取驾驶月报",
  660,
  512,
  535,
  58,
  {
    fontSize: 14,
    color: C.inkSoft,
    verticalAlignment: "top",
  },
);
const nacosBar = architectureSlide.shapes.add({
  geometry: "roundRect",
  position: { left: 72, top: 620, width: 1136, height: 38 },
  fill: C.blueDeep,
  line: { style: "solid", fill: C.blueDeep, width: 0 },
  borderRadius: 7,
});
nacosBar.text =
  "Nacos 3.2.4 将服务注册到 COURSE_GROUP，并提供动态服务发现";
nacosBar.text.style = {
  typeface: FONT,
  fontSize: 16,
  bold: true,
  color: C.white,
  alignment: "center",
  verticalAlignment: "middle",
  margin: 0,
};

// Slide 5: keep the title area clear and reflect the current communication path.
presentation.resolve("sh/032tgr6d").delete();
presentation.resolve("sh/v2twb6dw").text =
  "告警使用 OpenFeign；驾驶行为与碳排使用 DiscoveryClient 和 RestTemplate。";
presentation.resolve("sh/hgrmpwj2").text =
  "第 7 周代码：先获取 vehicle-service 实例，再校验车辆";

// Slide 6: restore clear columns and keep the result summary on one line.
const slide6 = slides[5];
presentation.resolve("sh/j6dcr65c").text =
  "Nacos 3.2.4 以 standalone 模式运行，服务统一注册至 COURSE_GROUP。";
presentation.resolve("sh/h4bupgn6").text =
  "多阶段 Dockerfile\n将服务打包为轻量级镜像";
presentation.resolve("sh/u1kbu1ov").text =
  "Docker Compose 统一编排\n五个业务服务及对应数据库";
presentation.resolve("sh/j6dcr65c").text =
  "Nacos 3.2.4 以 standalone 模式运行\n服务统一注册至 COURSE_GROUP";
presentation.resolve("sh/svmt4v6t").text =
  "11 个测试全部通过\n健康检查与服务恢复已验证";
for (const id of ["pc76hkr2", "v2tcn650", "47mt0b6x", "twfux0ne"]) {
  const title = presentation.resolve(`sh/${id}`);
  title.position = { ...title.position, width: 126 };
}
const slide6BodyTops = [182, 301, 420, 539];
for (const [index, id] of [
  "h4bupgn6",
  "u1kbu1ov",
  "j6dcr65c",
  "svmt4v6t",
].entries()) {
  const body = presentation.resolve(`sh/${id}`);
  body.position = {
    ...body.position,
    left: 115,
    top: slide6BodyTops[index],
    width: 365,
    height: 58,
  };
}
for (const id of ["bq547yhw", "0fy5k3id", "lg76d8zy", "ydgnit07"]) {
  presentation.resolve(`sh/${id}`).delete();
}
addText(
  slide6,
  "COURSE_GROUP 下 5 个服务均为健康实例",
  523,
  536,
  585,
  30,
  {
    fontSize: 17,
    bold: true,
    color: C.blueDeep,
    verticalAlignment: "middle",
  },
);
addText(slide6, "11-07 实测", 1108, 539, 100, 24, {
  fontSize: 12,
  color: C.muted,
  alignment: "right",
  verticalAlignment: "middle",
});

// Slide 7: the right-hand evidence label already serves as the section anchor.
presentation.resolve("sh/l0vuh0rm").delete();

// Speaker narration: stored in notes, not on the visible slide.
slides.forEach((slide, index) => {
  slide.speakerNotes.textFrame.setText(narration[index]);
  slide.speakerNotes.setVisible(true);
});

await fs.mkdir(stagingDir, { recursive: true });
await fs.mkdir(previewDir, { recursive: true });
const candidatePath = path.join(
  stagingDir,
  "candidate-zhou-2435050112-v3.pptx",
);
await (await PresentationFile.exportPptx(presentation)).save(candidatePath);

for (let index = 0; index < slides.length; index += 1) {
  const preview = await presentation.export({
    slide: slides[index],
    format: "png",
    scale: 1,
  });
  await fs.writeFile(
    path.join(previewDir, `slide-${String(index + 1).padStart(2, "0")}.png`),
    new Uint8Array(await preview.arrayBuffer()),
  );
}

const { finalizePresentation } = await import(
  pathToFileURL(path.join(skillDir, "container_tools/artifact_tool_utils.mjs"))
    .href
);
const result = await finalizePresentation({
  explicitTotalSlideCount: 9,
  requiredNativeTableOwnerSlides: [3, 4],
  requiredNativeChartOwnerSlides: [],
  workspaceDir,
  candidatePath,
  finalPath,
  pythonExecutable:
    "/Users/zhouyiming/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3.12",
  integrityValidatorPath: path.join(
    skillDir,
    "container_tools/inspect_presentation_package_integrity.py",
  ),
  layoutValidatorPath: path.join(
    skillDir,
    "container_tools/inspect_presentation_layout_geometry.py",
  ),
  layoutArgs: [
    "--expected-slide-size-emu",
    "12192000,6858000",
    "--validate-bullet-geometry",
    "--validate-heading-fit",
    "--require-native-table-slide",
    "3",
    "--require-native-table-slide",
    "4",
  ],
  fontPolicy: {
    basis: "design",
    families: [FONT],
  },
  verifyArtifactToolImport: true,
  receiptPath: path.join(
    stagingDir,
    "智驾伴侣_期中项目汇报_周轩亦_2435050112.validation.json",
  ),
});

console.log(JSON.stringify({ finalPath, previewDir, result }, null, 2));
