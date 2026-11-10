import fs from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import PptxGenJS from "pptxgenjs";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const OUT_DIR = path.join(ROOT, "outputs");
const OUT_FILE = path.join(OUT_DIR, "智驾伴侣_期中项目汇报_可编辑.pptx");

const FONT = "Hiragino Sans GB";
const C = {
  bg: "EEF8FD",
  bgDeep: "E2F2FA",
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
  red: "B8584D",
};

const pptx = new PptxGenJS();
pptx.layout = "LAYOUT_WIDE";
pptx.author = "智驾伴侣项目组";
pptx.company = "浙江工商大学";
pptx.subject = "智驾伴侣期中项目汇报";
pptx.title = "智驾伴侣（Smart Drive Companion）";
pptx.lang = "zh-CN";
pptx.theme = {
  headFontFace: FONT,
  bodyFontFace: FONT,
  lang: "zh-CN",
};

await fs.mkdir(OUT_DIR, { recursive: true });

function addText(slide, text, x, y, w, h, options = {}) {
  slide.addText(text, {
    x, y, w, h,
    fontFace: FONT,
    color: C.ink,
    fontSize: 16,
    margin: 0,
    breakLine: false,
    fit: "shrink",
    valign: "mid",
    ...options,
  });
}

function addRule(slide, x, y, w, color = C.line, width = 1.2) {
  slide.addShape(pptx.ShapeType.line, {
    x, y, w, h: 0,
    line: { color, width },
  });
}

function addHeader(slide, index, section, title, subtitle = "") {
  addText(slide, index, 0.68, 0.48, 0.7, 0.32, {
    fontSize: 11,
    bold: true,
    color: C.blue,
    charSpacing: 1.4,
  });
  addText(slide, section, 1.23, 0.47, 2.8, 0.32, {
    fontSize: 12,
    bold: true,
    color: C.teal,
  });
  addText(slide, title, 0.68, 0.83, 10.9, 0.62, {
    fontSize: 29,
    bold: true,
    color: C.ink,
    breakLine: false,
    valign: "mid",
  });
  if (subtitle) {
    addText(slide, subtitle, 8.5, 1.28, 4.15, 0.36, {
      fontSize: 12,
      color: C.muted,
      align: "right",
    });
  }
}

function addFooter(slide, number, label = "智驾伴侣 · 期中项目汇报") {
  addText(slide, label, 0.7, 7.09, 4.2, 0.2, {
    fontSize: 9,
    color: "8BA2B2",
  });
  addText(slide, `${String(number).padStart(2, "0")} / 09`, 11.52, 7.04, 1.12, 0.26, {
    fontSize: 9,
    color: "6D8799",
    align: "right",
  });
}

function addNewSlide(number, label = "智驾伴侣 · 期中项目汇报") {
  const slide = pptx.addSlide();
  slide.background = { color: C.bg };
  addFooter(slide, number, label);
  return slide;
}

function addLabel(slide, text, x, y, w, color = C.blue) {
  addText(slide, text, x, y, w, 0.28, {
    fontSize: 10.5,
    bold: true,
    color,
    charSpacing: 0.8,
  });
}

function addBodyBlock(slide, label, body, x, y, w, h, accent = C.blue) {
  addLabel(slide, label, x, y, w, accent);
  addText(slide, body, x, y + 0.34, w, h - 0.34, {
    fontSize: 16,
    color: C.inkSoft,
    valign: "top",
    breakLine: false,
    fit: "shrink",
    lineSpacingMultiple: 1.2,
  });
}

function addPictureFrame(slide, x, y, w, h, fill = C.white) {
  slide.addShape(pptx.ShapeType.roundRect, {
    x, y, w, h,
    rectRadius: 0.08,
    fill: { color: fill },
    line: { color: C.line, width: 1 },
    shadow: {
      type: "outer",
      color: "315D75",
      opacity: 0.14,
      blur: 2,
      angle: 45,
      distance: 2,
    },
  });
}

function addScreenshot(slide, filePath, x, y, w, h, altText) {
  slide.addImage({
    path: filePath,
    x, y, w, h,
    altText,
    rounding: false,
    sizing: { type: "contain", w, h },
  });
}

// Slide 1: cover
{
  const slide = pptx.addSlide();
  slide.background = { color: "E9F6FC" };
  addText(slide, "SMART DRIVE COMPANION", 0.86, 0.72, 5.2, 0.34, {
    fontSize: 11,
    bold: true,
    color: C.blue,
    charSpacing: 2.2,
  });
  addText(slide, "智驾伴侣", 0.84, 1.55, 6.7, 0.98, {
    fontSize: 50,
    bold: true,
    color: C.ink,
    valign: "mid",
  });
  addText(slide, "Smart Drive Companion", 0.87, 2.62, 5.5, 0.54, {
    fontSize: 23,
    color: C.blueDeep,
  });
  addRule(slide, 0.88, 3.44, 3.65, C.sky, 2);
  addText(slide, "面向车主的智能车辆服务平台", 0.87, 3.73, 6.4, 0.42, {
    fontSize: 20,
    color: C.inkSoft,
  });
  addText(slide, "期中项目汇报", 0.87, 4.24, 3.3, 0.38, {
    fontSize: 16,
    bold: true,
    color: C.teal,
  });
  addText(slide, "汇报人：[姓名 / 学号缩写]", 0.88, 5.56, 4.2, 0.3, {
    fontSize: 13,
    color: C.muted,
  });
  addText(slide, "2026 年 11 月", 0.88, 5.96, 3.2, 0.3, {
    fontSize: 13,
    color: C.muted,
  });
  addText(slide, "01", 10.55, 4.72, 2.0, 1.1, {
    fontSize: 74,
    bold: true,
    color: "CFE8F5",
    align: "right",
  });
  slide.addNotes(
    "封面汇报人信息保留为占位文字，可在 PowerPoint 中直接替换姓名与学号缩写。",
  );
}

// Slide 2: contents
{
  const slide = addNewSlide(2);
  addHeader(slide, "00", "CONTENTS", "目录");
  addRule(slide, 0.7, 1.52, 11.92, C.line, 1);

  const leftItems = [
    ["01", "项目背景与主要用途", "从车主需求与套餐分层切入"],
    ["02", "当前技术架构", "从单体应用演进为五个微服务"],
  ];
  const rightItems = [
    ["03", "已完成的功能与技术工作", "数据边界、跨服务通信、部署与验证"],
    ["04", "遇到的挑战与解决方案", "版本兼容、Nacos 端口、依赖失败"],
    ["05", "尚未完成的内容与后续计划", "网关、配置中心、分布式事务与可观测性"],
  ];

  const drawItem = (item, x, y, w) => {
    addText(slide, item[0], x, y, 0.72, 0.42, {
      fontSize: 22,
      bold: true,
      color: C.sky,
    });
    addText(slide, item[1], x + 0.82, y - 0.02, w - 0.82, 0.42, {
      fontSize: 20,
      bold: true,
      color: C.ink,
    });
    addText(slide, item[2], x + 0.82, y + 0.47, w - 0.82, 0.34, {
      fontSize: 12.5,
      color: C.muted,
    });
  };

  leftItems.forEach((item, i) => drawItem(item, 0.96, 1.92 + i * 1.7, 5.25));
  rightItems.forEach((item, i) => drawItem(item, 6.84, 1.9 + i * 1.34, 5.44));
  slide.addShape(pptx.ShapeType.line, {
    x: 6.18, y: 1.78, w: 0, h: 4.72,
    line: { color: C.line, width: 1 },
  });
  slide.addNotes("汇报顺序：背景、架构、当前完成度、问题解决与后续路线。");
}

// Slide 3: background and uses
{
  const slide = addNewSlide(3);
  addHeader(slide, "01", "BACKGROUND", "项目背景与用途", "面向车主，按套餐提供差异化数据服务");

  addBodyBlock(
    slide,
    "项目定位",
    "面向车主的智能车辆服务平台，围绕车辆状态与使用数据形成持续可查询的服务闭环。",
    0.7, 1.58, 4.52, 1.42,
  );
  addBodyBlock(
    slide,
    "主要用途",
    "车辆定位、告警分级、驾驶行为分析、碳排核算。",
    0.7, 3.2, 4.52, 1.2,
    C.teal,
  );
  addBodyBlock(
    slide,
    "业务分层",
    "轻量版覆盖基础状态；标准版增加告警分级与驾驶分析；豪华版进一步提供碳排及高级体验。",
    0.7, 4.56, 4.52, 1.6,
    C.amber,
  );

  const border = { type: "solid", color: C.line, pt: 1 };
  const headerCell = (text) => ({
    text,
    options: {
      bold: true,
      color: C.white,
      fill: { color: C.blueDeep },
      align: "center",
      valign: "mid",
      margin: 0.06,
    },
  });
  const bodyCell = (text, options = {}) => ({
    text,
    options: {
      color: C.inkSoft,
      fill: { color: options.fill ?? C.white },
      align: options.align ?? "center",
      valign: "mid",
      bold: options.bold ?? false,
      margin: 0.06,
    },
  });

  const rows = [
    [bodyCell("能力", { fill: C.blueDeep, bold: true, align: "left" }), headerCell("轻量版"), headerCell("标准版"), headerCell("豪华版")],
    [bodyCell("车辆定位、状态查看", { align: "left", bold: true }), bodyCell("✓"), bodyCell("✓"), bodyCell("✓")],
    [bodyCell("基础告警列表", { align: "left", bold: true, fill: "E8F4FA" }), bodyCell("✓", { fill: "E8F4FA" }), bodyCell("✓", { fill: "E8F4FA" }), bodyCell("✓", { fill: "E8F4FA" })],
    [bodyCell("告警分级 1–3", { align: "left", bold: true }), bodyCell("—"), bodyCell("✓"), bodyCell("✓")],
    [bodyCell("驾驶行为、油耗、月报", { align: "left", bold: true, fill: "E8F4FA" }), bodyCell("—", { fill: "E8F4FA" }), bodyCell("✓", { fill: "E8F4FA" }), bodyCell("✓", { fill: "E8F4FA" })],
    [bodyCell("碳排与 3D / AI 体验", { align: "left", bold: true }), bodyCell("—"), bodyCell("—"), bodyCell("✓")],
  ];

  slide.addText("\uFEFF", { x: 5.58, y: 1.65, w: 0.01, h: 0.01 });
  addLabel(slide, "套餐功能矩阵", 5.62, 1.55, 2.3, C.blueDeep);
  slide.addTable(rows, {
    x: 5.62, y: 1.94, w: 7.02, h: 3.98,
    colW: [3.02, 1.33, 1.33, 1.34],
    rowH: [0.56, 0.62, 0.62, 0.62, 0.78, 0.78],
    fontFace: FONT,
    fontSize: 11.5,
    border,
    autoPage: false,
  });
  addText(slide, "当前汇报聚焦后端服务能力；套餐矩阵来自项目 README 与原功能模块划分。", 5.63, 6.02, 6.85, 0.34, {
    fontSize: 10.5,
    color: C.muted,
    italic: true,
  });
  slide.addNotes("套餐矩阵按 README 与原功能模块划分整理，已做成可编辑表格。");
}

// Slide 4: architecture
{
  const slide = addNewSlide(4);
  addHeader(slide, "02", "ARCHITECTURE", "当前技术架构", "单体应用已拆分为五个核心微服务");

  addText(slide, "5", 0.77, 1.61, 1.22, 0.92, {
    fontSize: 58,
    bold: true,
    color: C.sky,
  });
  addText(slide, "个核心微服务", 1.72, 1.73, 2.22, 0.45, {
    fontSize: 19,
    bold: true,
    color: C.ink,
  });
  addText(slide, "按业务边界拆分，统一注册到 COURSE_GROUP。", 0.78, 2.45, 3.05, 0.68, {
    fontSize: 15,
    color: C.inkSoft,
    valign: "top",
    lineSpacingMultiple: 1.18,
  });
  addLabel(slide, "数据边界", 0.78, 3.36, 1.4, C.teal);
  addText(slide, "每个服务只读写自己的数据库，不建立跨库外键。", 0.78, 3.7, 3.05, 0.78, {
    fontSize: 14.5,
    color: C.inkSoft,
    valign: "top",
  });
  addLabel(slide, "远程依赖", 0.78, 4.67, 1.4, C.amber);
  addText(
    slide,
    [
      { text: "alert-service", options: { bold: true, color: C.blueDeep } },
      { text: "  发现并校验  ", options: { color: C.muted } },
      { text: "vehicle-service", options: { bold: true, color: C.blueDeep } },
      { text: "\n", options: {} },
      { text: "driving-behavior-service", options: { bold: true, color: C.blueDeep } },
      { text: "  校验车辆\n", options: { color: C.muted } },
      { text: "carbon-service", options: { bold: true, color: C.blueDeep } },
      { text: "  读取驾驶月报", options: { color: C.muted } },
    ],
    0.78, 5.02, 3.15, 0.9,
    { fontSize: 11.8, color: C.inkSoft, valign: "top", breakLine: false },
  );

  const services = [
    ["user-service", "8081", "车主与套餐", "user_db"],
    ["vehicle-service", "8082", "车辆档案", "vehicle_db"],
    ["alert-service", "8083", "告警与分级", "alert_db"],
    ["driving-behavior-service", "8084", "行为与月报", "driving_db"],
    ["carbon-service", "8085", "碳排报告", "carbon_db"],
  ];

  addLabel(slide, "业务服务", 4.24, 1.43, 2.0);
  addLabel(slide, "职责", 7.48, 1.43, 1.2, C.teal);
  addLabel(slide, "独立数据库", 9.92, 1.43, 1.8, C.amber);

  services.forEach(([name, port, duty, db], i) => {
    const y = 1.82 + i * 0.78;
    slide.addShape(pptx.ShapeType.rect, {
      x: 4.18, y, w: 3.22, h: 0.56,
      fill: { color: i % 2 === 0 ? C.white : "E7F4FA" },
      line: { color: C.line, width: 1 },
    });
    addText(slide, name, 4.34, y + 0.02, 2.25, 0.23, {
      fontSize: 12,
      bold: true,
      color: C.blueDeep,
    });
    addText(slide, `:${port}`, 6.43, y + 0.02, 0.78, 0.23, {
      fontSize: 10.5,
      color: C.muted,
    });
    addText(slide, duty, 7.45, y + 0.02, 1.82, 0.5, {
      fontSize: 12.5,
      color: C.inkSoft,
    });
    slide.addShape(pptx.ShapeType.line, {
      x: 9.38, y: y + 0.28, w: 0.45, h: 0,
      line: { color: C.sky, width: 1.5, endArrowType: "triangle" },
    });
    slide.addShape(pptx.ShapeType.rect, {
      x: 9.92, y: y + 0.07, w: 1.57, h: 0.42,
      fill: { color: C.white },
      line: { color: C.sky, width: 1 },
    });
    addText(slide, db, 10.05, y + 0.05, 1.33, 0.26, {
      fontSize: 11.3,
      color: C.inkSoft,
      align: "center",
    });
  });

  slide.addShape(pptx.ShapeType.roundRect, {
    x: 4.18, y: 5.88, w: 8.18, h: 0.58,
    rectRadius: 0.08,
    fill: { color: C.blueDeep },
    line: { color: C.blueDeep, width: 1 },
  });
  addText(slide, "Nacos 3.2.4  ·  COURSE_GROUP  ·  服务注册与发现", 4.47, 5.97, 7.58, 0.32, {
    fontSize: 14,
    bold: true,
    color: C.white,
    align: "center",
  });
  addText(slide, "基础设施", 10.02, 5.98, 1.85, 0.3, {
    fontSize: 12,
    color: C.blue,
    bold: true,
    align: "right",
  });
  slide.addNotes("架构页按项目中真实落地的五个服务、端口和数据库绘制，全部为可编辑图形。");
}

// Slide 5: completed business functions and code
{
  const slide = addNewSlide(5);
  addHeader(slide, "03", "IMPLEMENTATION", "已完成的功能与代码", "业务规则已经落到服务层");

  const items = [
    ["01", "独立数据读写", "各服务拥有独立数据库，只保留业务 ID，不建立跨库外键。"],
    ["02", "跨服务通信", "通过 DiscoveryClient 获取实例，再由 RestTemplate 发起请求。"],
    ["03", "业务规则校验", "告警等级限定为 1–3；找不到实例返回 503；车辆不存在返回 400。"],
  ];
  items.forEach(([number, title, body], i) => {
    const y = 1.54 + i * 1.55;
    addText(slide, number, 0.7, y, 0.52, 0.3, {
      fontSize: 11,
      bold: true,
      color: C.sky,
    });
    addText(slide, title, 1.2, y - 0.05, 3.95, 0.38, {
      fontSize: 18,
      bold: true,
      color: C.ink,
    });
    addText(slide, body, 1.2, y + 0.42, 4.18, 0.72, {
      fontSize: 14,
      color: C.inkSoft,
      valign: "top",
      lineSpacingMultiple: 1.2,
    });
  });
  addText(slide, "关键顺序：先发现实例，再校验车辆，最后写入本地数据库。", 0.72, 6.06, 5.0, 0.42, {
    fontSize: 13,
    bold: true,
    color: C.blueDeep,
  });

  addPictureFrame(slide, 5.92, 1.36, 6.56, 4.95, "F8FCFE");
  addScreenshot(
    slide,
    path.join(ROOT, "docs/homework/week-07/screenshots/ca1519abd299555453e7aa836df4f7ae.png"),
    6.03, 1.46, 6.34, 4.76,
    "AlertService.java 中通过 DiscoveryClient 获取 vehicle-service 实例并校验车辆的代码截图",
  );
  addText(slide, "AlertService.java：实例为空时阻断请求，校验通过后才落库", 5.96, 6.44, 6.43, 0.3, {
    fontSize: 10.5,
    color: C.muted,
    align: "center",
  });
  slide.addNotes("代码截图来自 docs/homework/week-07/screenshots，可在 PowerPoint 中直接替换。");
}

// Slide 6: deployment and verification
{
  const slide = addNewSlide(6);
  addHeader(slide, "03", "IMPLEMENTATION", "已完成的技术工作：环境与部署", "容器化、服务发现、自动化验证");

  const workItems = [
    ["容器化", "多阶段 Dockerfile，将服务打包为轻量级镜像。"],
    ["一键编排", "Docker Compose 管理五个业务服务及对应数据库。"],
    ["注册中心", "Nacos 3.2.4 开启鉴权，服务统一注册至 COURSE_GROUP。"],
    ["自动化验证", "11 个测试全部通过；健康检查与服务下线恢复流程已验证。"],
  ];
  workItems.forEach(([title, body], i) => {
    const y = 1.53 + i * 1.24;
    addText(slide, `0${i + 1}`, 0.7, y, 0.5, 0.28, {
      fontSize: 10.5,
      bold: true,
      color: C.sky,
    });
    addText(slide, title, 1.18, y - 0.04, 1.25, 0.34, {
      fontSize: 16,
      bold: true,
      color: C.ink,
    });
    addText(slide, body, 2.42, y - 0.08, 2.7, 0.76, {
      fontSize: 13,
      color: C.inkSoft,
      valign: "top",
      lineSpacingMultiple: 1.16,
    });
  });

  addLabel(slide, "运行结果", 5.42, 1.39, 1.8, C.teal);
  addPictureFrame(slide, 5.4, 1.74, 7.2, 3.7, "F8FCFE");
  addScreenshot(
    slide,
    path.join(ROOT, "docs/homework/week-07/screenshots/697f1c0660ad89d3f336e4c49512bf53.png"),
    5.5, 1.84, 6.99, 3.5,
    "Nacos 控制台服务列表截图，显示 COURSE_GROUP 下五个健康服务",
  );
  addText(slide, "COURSE_GROUP", 5.45, 5.63, 2.25, 0.3, {
    fontSize: 12,
    bold: true,
    color: C.blueDeep,
  });
  addText(slide, "5 个服务", 8.0, 5.56, 1.25, 0.44, {
    fontSize: 24,
    bold: true,
    color: C.ink,
  });
  addText(slide, "每个服务 1 个健康实例", 9.25, 5.68, 2.15, 0.3, {
    fontSize: 12,
    color: C.muted,
  });
  addText(slide, "2026-11-07 实测", 11.3, 5.65, 1.18, 0.3, {
    fontSize: 10.5,
    color: C.muted,
    align: "right",
  });
  addText(slide, "smoke-test.sh  ·  verify-discovery.sh", 5.47, 6.14, 4.85, 0.3, {
    fontSize: 11,
    color: C.teal,
    bold: true,
  });
  slide.addNotes("Nacos 截图来自项目实测记录，显示 COURSE_GROUP 下五个服务的健康实例。");
}

// Slide 7: challenges
{
  const slide = addNewSlide(7);
  addHeader(slide, "04", "CHALLENGES", "遇到的挑战与解决方案", "先定位边界，再恢复业务闭环");

  const challenges = [
    ["01", "版本兼容性冲突", "Spring Boot 4 与 Spring Cloud Alibaba 版本不匹配，服务无法连接。", "统一为 Spring Boot 3.2.x、Java 17 与 SCA 2023.0.1.0。"],
    ["02", "Nacos 3 端口与路径变化", "访问 8848 控制台返回空响应。", "控制台使用 18080，SDK 使用 8848 / 9848，并通过环境变量隔离。"],
    ["03", "跨服务依赖失败产生脏数据风险", "依赖服务不可用时，请求可能绕过业务校验。", "增加实例空判断并返回 503；车辆不存在返回 400，阻断落库。"],
  ];
  challenges.forEach(([number, title, problem, solution], i) => {
    const y = 1.45 + i * 1.62;
    addText(slide, number, 0.7, y, 0.5, 0.3, {
      fontSize: 10.5,
      bold: true,
      color: i === 2 ? C.amber : C.sky,
    });
    addText(slide, title, 1.2, y - 0.05, 7.28, 0.38, {
      fontSize: 17,
      bold: true,
      color: C.ink,
    });
    addText(slide, `现象  ${problem}`, 1.2, y + 0.4, 7.28, 0.32, {
      fontSize: 12,
      color: C.muted,
      valign: "top",
    });
    addText(slide, `解决  ${solution}`, 1.2, y + 0.76, 7.28, 0.48, {
      fontSize: 12.5,
      color: C.blueDeep,
      bold: true,
      valign: "top",
      lineSpacingMultiple: 1.08,
    });
    addRule(slide, 0.7, y + 1.34, 7.85, C.line, 0.8);
  });

  addLabel(slide, "运行证据", 9.16, 1.42, 1.5, C.teal);
  addPictureFrame(slide, 9.14, 1.78, 3.48, 1.72, "F8FCFE");
  addScreenshot(
    slide,
    path.join(ROOT, "docs/homework/week-07/screenshots/47ff001d932dbbef41e88afee8490472.png"),
    9.23, 1.89, 3.3, 1.47,
    "车辆服务下线后，alert-service 返回 503 的终端截图",
  );
  addText(slide, "vehicle-service 下线后返回 503", 9.18, 3.54, 3.42, 0.3, {
    fontSize: 10.5,
    color: C.muted,
    align: "center",
  });
  addLabel(slide, "验证链路", 9.18, 4.08, 1.5, C.blue);
  addText(slide, "正常 200", 9.2, 4.48, 3.35, 0.2, {
    fontSize: 12,
    bold: true,
    color: C.teal,
  });
  addText(slide, "实例下线 503", 9.2, 4.87, 3.35, 0.2, {
    fontSize: 12,
    bold: true,
    color: C.amber,
  });
  addText(slide, "恢复后 200", 9.2, 5.26, 3.35, 0.2, {
    fontSize: 12,
    bold: true,
    color: C.teal,
  });
  addText(slide, "请求在失败阶段被拦截，没有形成无效告警记录。", 9.2, 5.8, 3.32, 0.58, {
    fontSize: 10.7,
    color: C.inkSoft,
    valign: "top",
    lineSpacingMultiple: 1.1,
  });
  slide.addNotes("右侧截图来自 true 环境验证：vehicle-service 下线后，alert-service 返回 503。");
}

// Slide 8: gaps and next steps
{
  const slide = addNewSlide(8);
  addHeader(slide, "05", "ROADMAP", "尚未完成的内容与后续计划", "当前边界清晰，后续按基础设施能力逐步扩展");

  addLabel(slide, "当前未实现", 0.7, 1.53, 1.7, C.amber);
  const gaps = [
    ["前端与聚合", "前端 UI、BFF 聚合层"],
    ["复杂业务", "3D 建模、AI 助手、预测性维护"],
    ["分布式能力", "Seata 分布式事务、消息队列"],
  ];
  gaps.forEach(([title, body], i) => {
    const y = 2.02 + i * 1.5;
    addText(slide, title, 0.7, y, 1.55, 0.34, {
      fontSize: 15,
      bold: true,
      color: C.ink,
    });
    addText(slide, body, 2.18, y - 0.02, 3.0, 0.56, {
      fontSize: 13,
      color: C.inkSoft,
      valign: "top",
    });
  });
  addRule(slide, 0.7, 6.24, 4.56, C.line, 1);

  addLabel(slide, "后续计划", 5.65, 1.53, 1.7, C.blue);
  slide.addShape(pptx.ShapeType.line, {
    x: 5.92, y: 2.16, w: 0, h: 3.74,
    line: { color: C.line, width: 2 },
  });
  const roadmap = [
    ["阶段一", "API 网关", "Spring Cloud Gateway 统一鉴权与路由"],
    ["阶段二", "配置中心", "Nacos Config 管理环境配置并支持动态刷新"],
    ["阶段三", "可观测性", "SkyWalking 链路追踪与 Prometheus + Grafana 监控"],
  ];
  roadmap.forEach(([stage, title, body], i) => {
    const y = 1.95 + i * 1.48;
    slide.addShape(pptx.ShapeType.ellipse, {
      x: 5.82, y: y + 0.05, w: 0.2, h: 0.2,
      fill: { color: i === 0 ? C.blue : C.sky },
      line: { color: C.bg, width: 2 },
    });
    addText(slide, stage, 6.3, y - 0.03, 0.72, 0.26, {
      fontSize: 10.5,
      bold: true,
      color: C.blue,
    });
    addText(slide, title, 7.06, y - 0.08, 1.45, 0.36, {
      fontSize: 17,
      bold: true,
      color: C.ink,
    });
    addText(slide, body, 8.55, y - 0.08, 3.95, 0.64, {
      fontSize: 13,
      color: C.inkSoft,
      valign: "top",
      lineSpacingMultiple: 1.12,
    });
  });
  addText(
    slide,
    "优先补齐统一入口与配置治理，再引入事务、消息与监控能力。",
    5.67, 6.05, 6.85, 0.5,
    { fontSize: 13.5, bold: true, color: C.blueDeep, valign: "top" },
  );
  slide.addNotes("路线按依赖关系排序：先统一入口与配置，再补充分布式能力与可观测性。");
}

// Slide 9: closing
{
  const slide = pptx.addSlide();
  slide.background = { color: "E9F6FC" };
  addText(slide, "09", 10.55, 0.56, 1.95, 0.5, {
    fontSize: 13,
    bold: true,
    color: C.sky,
    align: "right",
    charSpacing: 1.2,
  });
  addText(slide, "感谢聆听", 1.08, 2.08, 7.2, 0.94, {
    fontSize: 47,
    bold: true,
    color: C.ink,
  });
  addRule(slide, 1.12, 3.23, 3.3, C.sky, 2);
  addText(slide, "欢迎老师批评指正", 1.12, 3.54, 6.2, 0.48, {
    fontSize: 22,
    color: C.blueDeep,
  });
  addText(slide, "智驾伴侣  ·  Smart Drive Companion", 1.12, 5.78, 6.4, 0.32, {
    fontSize: 12,
    color: C.muted,
  });
  addText(slide, "2026 年 11 月", 10.05, 5.78, 2.1, 0.32, {
    fontSize: 12,
    color: C.muted,
    align: "right",
  });
  slide.addNotes("结束页。可在此处补充答辩联系方式。");
}

await fs.rm(`${OUT_FILE}.tmp`, { force: true });
await pptx.writeFile({ fileName: OUT_FILE });
console.log(OUT_FILE);
