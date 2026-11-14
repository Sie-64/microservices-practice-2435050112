from pathlib import Path
from docx import Document
from docx.enum.section import WD_SECTION
from docx.enum.table import WD_ALIGN_VERTICAL, WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.enum.text import WD_LINE_SPACING
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt, RGBColor


ROOT = Path("/Users/zhouyiming/Documents/work/micro_lesson/lesson")
OUT_DIR = ROOT / "outputs"
FONT = "Hiragino Sans GB W3"
BLACK = "000000"
INK = "1F2933"
MUTED = "5C6670"
BLUE = "1D5E8C"
PALE_BLUE = "E7F4FA"
BORDER = "D9D9D9"
WHITE = "FFFFFF"


def set_run_font(run, size=11, bold=False, color=INK, italic=False):
    run.font.name = FONT
    run.font.size = Pt(size)
    run.font.bold = bold
    run.font.italic = italic
    run.font.color.rgb = RGBColor.from_string(color)
    rpr = run._element.get_or_add_rPr()
    rfonts = rpr.rFonts
    if rfonts is None:
        rfonts = OxmlElement("w:rFonts")
        rpr.insert(0, rfonts)
    rfonts.set(qn("w:ascii"), FONT)
    rfonts.set(qn("w:hAnsi"), FONT)
    rfonts.set(qn("w:eastAsia"), FONT)


def set_paragraph_spacing(paragraph, before=0, after=8, line=1.45):
    paragraph.paragraph_format.space_before = Pt(before)
    paragraph.paragraph_format.space_after = Pt(after)
    paragraph.paragraph_format.line_spacing = line


def add_page_number(paragraph):
    paragraph.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    run = paragraph.add_run()
    set_run_font(run, size=8.5, color=MUTED)
    begin = OxmlElement("w:fldChar")
    begin.set(qn("w:fldCharType"), "begin")
    instruction = OxmlElement("w:instrText")
    instruction.set(qn("xml:space"), "preserve")
    instruction.text = " PAGE "
    end = OxmlElement("w:fldChar")
    end.set(qn("w:fldCharType"), "end")
    run._r.extend([begin, instruction, end])


def configure_document(document):
    section = document.sections[0]
    section.page_width = Inches(8.5)
    section.page_height = Inches(11)
    section.top_margin = Inches(0.8)
    section.bottom_margin = Inches(0.72)
    section.left_margin = Inches(0.82)
    section.right_margin = Inches(0.82)
    section.footer_distance = Inches(0.34)

    styles = document.styles
    normal = styles["Normal"]
    normal.font.name = FONT
    normal.font.size = Pt(11)
    normal.font.color.rgb = RGBColor.from_string(INK)
    normal._element.rPr.rFonts.set(qn("w:eastAsia"), FONT)
    normal.paragraph_format.space_after = Pt(6)
    normal.paragraph_format.line_spacing = 1.45

    for style_name, size, before, after in [
        ("Title", 26, 0, 12),
        ("Subtitle", 13, 0, 12),
        ("Heading 1", 18, 10, 8),
        ("Heading 2", 13.5, 8, 5),
    ]:
        style = styles[style_name]
        style.font.name = FONT
        style.font.size = Pt(size)
        style.font.color.rgb = RGBColor.from_string(BLACK)
        style.font.bold = style_name in {"Title", "Heading 1", "Heading 2"}
        style._element.rPr.rFonts.set(qn("w:eastAsia"), FONT)
        style.paragraph_format.space_before = Pt(before)
        style.paragraph_format.space_after = Pt(after)
        ppr = style._element.get_or_add_pPr()
        for border in ppr.findall(qn("w:pBdr")):
            ppr.remove(border)

    footer = section.footer
    footer_paragraph = footer.paragraphs[0]
    footer_paragraph.text = "智驾伴侣  ·  周轩亦  ·  2435050112"
    footer_paragraph.alignment = WD_ALIGN_PARAGRAPH.LEFT
    for run in footer_paragraph.runs:
        set_run_font(run, size=8.5, color=MUTED)
    page_paragraph = footer.add_paragraph()
    add_page_number(page_paragraph)


def add_title_page(document, title, subtitle, description):
    paragraph = document.add_paragraph(style="Title")
    paragraph.alignment = WD_ALIGN_PARAGRAPH.LEFT
    run = paragraph.add_run(title)
    set_run_font(run, size=27, bold=True, color=BLACK)
    set_paragraph_spacing(paragraph, before=44, after=8, line=1.05)
    ppr = paragraph._p.get_or_add_pPr()
    for border in ppr.findall(qn("w:pBdr")):
        ppr.remove(border)

    paragraph = document.add_paragraph(style="Subtitle")
    run = paragraph.add_run(subtitle)
    set_run_font(run, size=14, color=BLACK)
    set_paragraph_spacing(paragraph, before=0, after=26, line=1.1)

    paragraph = document.add_paragraph()
    run = paragraph.add_run(description)
    set_run_font(run, size=11.5, color=INK)
    set_paragraph_spacing(paragraph, before=0, after=12, line=1.55)

    paragraph = document.add_paragraph()
    run = paragraph.add_run("汇报人  周轩亦\n学号  2435050112\n日期  2026 年 11 月")
    set_run_font(run, size=10.5, color=MUTED)
    set_paragraph_spacing(paragraph, before=20, after=0, line=1.55)


def add_heading(document, text, level=1):
    paragraph = document.add_paragraph(style=f"Heading {level}")
    run = paragraph.add_run(text)
    set_run_font(
        run,
        size=18 if level == 1 else 13.5,
        bold=True,
        color=BLACK,
    )
    set_paragraph_spacing(
        paragraph,
        before=10 if level == 1 else 8,
        after=8 if level == 1 else 5,
        line=1.1,
    )
    return paragraph


def add_body(document, text, size=11.5, after=8, color=INK, bold_prefix=None):
    paragraph = document.add_paragraph()
    if bold_prefix and text.startswith(bold_prefix):
        first = paragraph.add_run(bold_prefix)
        set_run_font(first, size=size, bold=True, color=color)
        rest = paragraph.add_run(text[len(bold_prefix) :])
        set_run_font(rest, size=size, color=color)
    else:
        run = paragraph.add_run(text)
        set_run_font(run, size=size, color=color)
    set_paragraph_spacing(paragraph, before=0, after=after, line=1.5)
    return paragraph


def add_bullet(document, text):
    paragraph = document.add_paragraph(style="List Bullet")
    run = paragraph.add_run(text)
    set_run_font(run, size=11.2, color=INK)
    set_paragraph_spacing(paragraph, before=0, after=5, line=1.38)


def shade_cell(cell, fill):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = tc_pr.find(qn("w:shd"))
    if shd is None:
        shd = OxmlElement("w:shd")
        tc_pr.append(shd)
    shd.set(qn("w:fill"), fill)


def set_cell_margins(cell, top=100, start=120, bottom=100, end=120):
    tc = cell._tc
    tc_pr = tc.get_or_add_tcPr()
    tc_mar = tc_pr.first_child_found_in("w:tcMar")
    if tc_mar is None:
        tc_mar = OxmlElement("w:tcMar")
        tc_pr.append(tc_mar)
    for margin, value in [
        ("top", top),
        ("start", start),
        ("bottom", bottom),
        ("end", end),
    ]:
        node = tc_mar.find(qn(f"w:{margin}"))
        if node is None:
            node = OxmlElement(f"w:{margin}")
            tc_mar.append(node)
        node.set(qn("w:w"), str(value))
        node.set(qn("w:type"), "dxa")


def set_table_borders(table, color=BORDER, size="5"):
    tbl_pr = table._tbl.tblPr
    borders = tbl_pr.find(qn("w:tblBorders"))
    if borders is None:
        borders = OxmlElement("w:tblBorders")
        tbl_pr.append(borders)
    for edge in ["top", "left", "bottom", "right", "insideH", "insideV"]:
        element = borders.find(qn(f"w:{edge}"))
        if element is None:
            element = OxmlElement(f"w:{edge}")
            borders.append(element)
        element.set(qn("w:val"), "single")
        element.set(qn("w:sz"), size)
        element.set(qn("w:space"), "0")
        element.set(qn("w:color"), color)


def prevent_row_split(row):
    tr_pr = row._tr.get_or_add_trPr()
    cant_split = OxmlElement("w:cantSplit")
    tr_pr.append(cant_split)


def mark_header_row(row):
    tr_pr = row._tr.get_or_add_trPr()
    tbl_header = OxmlElement("w:tblHeader")
    tbl_header.set(qn("w:val"), "true")
    tr_pr.append(tbl_header)


def add_table(document, headers, rows, widths=None, font_size=9.5):
    table = document.add_table(rows=1, cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = True
    set_table_borders(table)
    header = table.rows[0]
    mark_header_row(header)
    prevent_row_split(header)
    for index, text in enumerate(headers):
        cell = header.cells[index]
        shade_cell(cell, BLUE)
        cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        set_cell_margins(cell)
        paragraph = cell.paragraphs[0]
        paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
        set_paragraph_spacing(paragraph, before=0, after=0, line=1.12)
        run = paragraph.add_run(text)
        set_run_font(run, size=font_size, bold=True, color=WHITE)
        if widths:
            cell.width = Inches(widths[index])
    for row_index, row_values in enumerate(rows):
        row = table.add_row()
        prevent_row_split(row)
        for index, text in enumerate(row_values):
            cell = row.cells[index]
            shade_cell(cell, WHITE if row_index % 2 == 0 else PALE_BLUE)
            cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
            set_cell_margins(cell)
            paragraph = cell.paragraphs[0]
            paragraph.alignment = (
                WD_ALIGN_PARAGRAPH.CENTER
                if index in {0, 2, 3}
                else WD_ALIGN_PARAGRAPH.LEFT
            )
            set_paragraph_spacing(paragraph, before=0, after=0, line=1.22)
            run = paragraph.add_run(str(text))
            set_run_font(
                run,
                size=font_size,
                bold=(index == 0),
                color=WHITE if False else INK,
            )
            if widths:
                cell.width = Inches(widths[index])
    document.add_paragraph().paragraph_format.space_after = Pt(3)
    return table


def page_break(document):
    paragraph = document.add_paragraph()
    paragraph.add_run().add_break(WD_BREAK.PAGE)


def build_narration():
    document = Document()
    configure_document(document)
    document.core_properties.author = "周轩亦"
    document.core_properties.title = "智驾伴侣期中项目汇报逐页旁白"
    document.core_properties.subject = "期中项目汇报逐页口头说明"
    add_title_page(
        document,
        "智驾伴侣期中项目汇报逐页旁白",
        "周轩亦 2435050112",
        "这份旁白按照 PPT 页序编写，正文可以直接照读。每页控制在约四十到六十秒，遇到老师追问时可以停下展开；页面上的英文和缩写由“知识点介绍”单独解释。",
    )
    page_break(document)

    narration_pages = [
        (
            "第 1 页 封面",
            "各位老师好，我是周轩亦，学号 2435050112。我的期中项目叫智驾伴侣，面向车主提供车辆状态、告警、驾驶行为和碳排数据服务。本次汇报重点说明项目从单体应用拆分成五个微服务后，业务边界如何划分、服务之间如何协作，以及目前完成了哪些部署和验证。",
        ),
        (
            "第 2 页 目录",
            "报告按五个部分展开。第一部分交代项目要解决什么问题，以及三类套餐分别覆盖哪些能力。第二部分说明当前技术架构。第三部分展示已经落地的代码、部署和验证结果。第四部分说明开发过程中遇到的问题和解决方式。最后说明尚未完成的能力，以及后续计划的先后顺序。",
        ),
        (
            "第 3 页 项目背景与用途",
            "智驾伴侣的核心用户是车主。平台以车辆档案为入口，围绕车辆状态、告警、驾驶事件和月度行车数据提供查询与分析。轻量版保留基础状态和告警列表，标准版增加告警分级、驾驶行为和月报，豪华版再增加碳排与高级体验。当前汇报主要聚焦后端服务能力，套餐矩阵用于说明不同业务能力的边界。",
        ),
        (
            "第 4 页 当前技术架构",
            "当前系统由五个 Spring Boot 微服务组成。用户服务管理车主与套餐，车辆服务管理车辆档案，告警服务保存告警并校验等级，驾驶行为服务保存驾驶事件并生成月报，碳排服务计算月度碳排放。五个服务各自连接独立数据库，只保存对方的业务 ID。服务统一注册到 Nacos 的 COURSE_GROUP，调用方通过服务名获取实例，不依赖固定 IP。",
        ),
        (
            "第 5 页 已完成的功能与代码",
            "代码落地时，我先保证每个服务只操作自己的数据库。告警服务创建告警前会校验车辆是否存在，当前通过 OpenFeign 调用车辆服务并配置 fallback；驾驶行为和碳排服务使用 DiscoveryClient 找到实例，再用 RestTemplate 发起请求。业务规则包括告警等级只能是 1 到 3、车辆不存在返回 400、依赖服务不可用返回 503，只有校验通过才写入本地数据库。",
        ),
        (
            "第 6 页 环境与部署",
            "部署方面，五个服务都通过多阶段 Dockerfile 打包成镜像，Docker Compose 统一管理业务服务、五个数据库和 Nacos。截图显示 COURSE_GROUP 下的五个服务都有健康实例。测试方面，五个服务共 11 个测试全部通过，测试使用 H2 内存数据库并关闭 Nacos 注册，不依赖真实运行环境。smoke-test 检查健康接口，verify-discovery 验证正常调用、车辆服务下线返回 503，以及恢复后重新返回 200。",
        ),
        (
            "第 7 页 遇到的挑战与解决方案",
            "开发中主要遇到三类问题。第一是 Spring Boot 和 Spring Cloud Alibaba 版本不匹配，最后统一到 Spring Boot 3.2、Java 17 和对应的版本管理。第二是 Nacos 3 的控制台、HTTP 服务和 SDK gRPC 使用不同端口，所以分别配置为 18080、8848 和 9848。第三是依赖服务失效时可能产生无效数据，因此增加了 fallback、实例判断和状态码处理，在写库之前阻断请求。",
        ),
        (
            "第 8 页 后续计划",
            "目前还没有做前端、BFF 聚合层、3D 与 AI 能力，也没有引入 Seata、消息队列和完整监控。后续先补 API 网关，统一入口和鉴权；再用 Nacos Config 管理环境配置；最后加入链路追踪和指标监控，并根据业务需要引入分布式事务或消息机制。",
        ),
        (
            "第 9 页 结束页",
            "以上是智驾伴侣期中项目的汇报。项目已经完成五个微服务的基本拆分、数据归属、跨服务调用、容器部署和服务发现验证。感谢老师，欢迎批评指正。",
        ),
    ]

    for index, (heading, text) in enumerate(narration_pages):
        add_heading(document, heading, 1)
        add_body(document, "逐字旁白", size=9.8, after=3, color=MUTED)
        add_body(document, text, size=13.2, after=14, color=INK)
        add_body(
            document,
            "读到这里可以停顿一到两秒，再进入下一页。" if index < 8 else "说完后停留片刻，等待老师提问。",
            size=9.8,
            after=0,
            color=MUTED,
        )
        if index < len(narration_pages) - 1:
            page_break(document)

    output = OUT_DIR / "智驾伴侣_逐页旁白_周轩亦_2435050112.docx"
    document.save(output)
    return output


def build_knowledge():
    document = Document()
    configure_document(document)
    document.core_properties.author = "周轩亦"
    document.core_properties.title = "智驾伴侣项目知识点介绍"
    document.core_properties.subject = "期中项目技术概念说明"
    add_title_page(
        document,
        "智驾伴侣项目知识点介绍",
        "从业务到微服务技术栈",
        "这份材料把 PPT 中会出现的概念按顺序解释清楚。阅读时先抓住一句话结论，再看项目里的具体用法；不需要一次记住所有英文，答辩时能说明每个组件解决什么问题即可。",
    )
    page_break(document)

    add_heading(document, "先看整体 项目在做什么")
    add_body(
        document,
        "智驾伴侣是一个面向车主的车辆数据服务平台。车主把车辆和行程数据录入系统后，平台可以保存告警、分析驾驶行为、生成月度报告，并根据油耗与电耗计算碳排放。项目当前的重点是后端微服务，而不是完整的前端产品。",
    )
    add_body(
        document,
        "PPT 中反复出现的 vehicleId 是业务关联键。它表示“这辆车的编号”，让不同服务知道数据属于哪辆车，但各服务不会直接读取对方数据库。",
    )
    add_table(
        document,
        ["服务", "端口", "当前作用", "数据库"],
        [
            ["user-service", "8081", "车主基础接口，目前是简化实现", "user_db"],
            ["vehicle-service", "8082", "创建和查询车辆档案", "vehicle_db"],
            ["alert-service", "8083", "创建告警并校验车辆", "alert_db"],
            ["driving-behavior-service", "8084", "保存驾驶事件并生成月报", "driving_db"],
            ["carbon-service", "8085", "读取驾驶月报并计算碳排", "carbon_db"],
        ],
        widths=[1.55, 0.65, 3.35, 1.25],
    )
    add_body(
        document,
        "最重要的结论：五个服务通过 HTTP 协作，但数据仍归各自服务所有。这样修改一个服务时，不需要直接改另一个服务的数据库。",
        bold_prefix="最重要的结论：",
    )
    page_break(document)

    add_heading(document, "单体应用与微服务")
    add_body(
        document,
        "单体应用是把车辆、告警、驾驶员等模块写在一个项目中，最后打包成一个程序。它的优点是启动和部署简单，模块之间调用也直接；缺点是所有代码一起发布，一个模块出问题可能影响整个系统，团队并行开发也容易互相牵连。",
    )
    add_body(
        document,
        "微服务是把一个系统按业务能力拆成多个可以独立运行的小程序。智驾伴侣把车辆、告警、驾驶行为和碳排分别放进不同服务。拆分以后，每个服务有自己的程序、端口、数据库和部署单元。",
    )
    add_table(
        document,
        ["比较项", "单体应用", "微服务"],
        [
            ["部署单位", "一个整体程序", "多个独立程序"],
            ["数据库", "通常集中或共享", "每个服务独立数据库"],
            ["修改影响", "可能牵动整个项目", "影响面通常限制在服务内"],
            ["运行复杂度", "较低", "需要注册发现、部署编排和容错"],
        ],
        widths=[1.2, 2.5, 2.9],
    )
    add_body(
        document,
        "微服务并没有自动消除复杂度。它把代码之间的复杂度，转移成了服务通信、部署和故障处理的复杂度，所以项目还需要 Nacos、Docker Compose、超时和降级处理。",
    )
    page_break(document)

    add_heading(document, "服务边界与数据库归属")
    add_body(
        document,
        "服务边界可以理解为一句话：“哪类数据应该由哪个服务负责。”车辆档案归车辆服务，告警记录归告警服务，驾驶事件归驾驶服务。一个服务只通过自己的 Repository 访问自己的数据库。",
    )
    add_body(
        document,
        "项目采用了“每个服务一个数据库”的做法。alert_db 里只保存 vehicleId，不建立指向 vehicle_db 的外键。这种做法叫逻辑关联，它只能说明业务上有关联，数据库本身并不知道另一张表在哪里。",
    )
    add_table(
        document,
        ["概念", "这句话可以这样理解"],
        [
            ["独立数据库", "每个服务管理自己的表，不直接操作别人的库"],
            ["业务 ID", "保存 vehicleId，用编号表示关联对象"],
            ["跨库查询", "一个服务直接查另一个服务的数据库，本项目禁止"],
            ["Repository", "Spring Data JPA 中负责数据库读写的接口"],
        ],
        widths=[1.45, 5.1],
    )
    add_body(
        document,
        "这样做的好处是边界清楚，代价是需要通过 HTTP 请求获取其他服务的信息，还要处理对方不可用的情况。",
    )
    page_break(document)

    add_heading(document, "Nacos 服务注册与发现")
    add_body(
        document,
        "容器启动后，IP 地址可能变化，因此调用方不能把 vehicle-service 的地址写死在代码里。Nacos 是注册中心，它像一个动态通讯录：服务启动后把自己的名称、地址和端口登记进去，调用方需要时再按服务名查询。",
    )
    add_body(
        document,
        "登记动作叫服务注册，按名称查找实例叫服务发现。COURSE_GROUP 是 Nacos 中的分组，用来把本课程相关服务放在一起。一个服务可能有多个实例，每个实例就是一份可以接收请求的运行程序。",
    )
    add_table(
        document,
        ["概念", "在项目中的值", "用途"],
        [
            ["注册中心", "Nacos 3.2.4", "保存服务实例的动态地址"],
            ["分组", "COURSE_GROUP", "隔离本课程的服务"],
            ["控制台端口", "18080", "浏览器查看服务列表"],
            ["HTTP 服务端口", "8848", "Nacos 服务端 HTTP API"],
            ["SDK gRPC 端口", "9848", "业务服务 SDK 使用"],
        ],
        widths=[1.4, 1.8, 3.4],
    )
    add_body(
        document,
        "服务进程会持续向 Nacos 发送心跳。实例停止后，Nacos 在一定时间内将它摘除；恢复运行后，实例重新注册。verify-discovery 脚本验证的就是“找到服务、服务下线、服务恢复”这一过程。",
    )
    page_break(document)

    add_heading(document, "HTTP API 与状态码")
    add_body(
        document,
        "API 是程序对外提供的调用入口。HTTP API 通常由请求方法、路径和请求内容组成。GET 表示查询，POST 表示新增。例如 GET /api/vehicles/1 表示查询编号为 1 的车辆，POST /api/alerts 表示创建一条告警。",
    )
    add_body(
        document,
        "JSON 是一种常用的数据格式，用大括号和字段名表示结构化数据。例如 {\"vehicleId\":1,\"level\":2} 表示车辆编号为 1、告警等级为 2。控制器接收到 JSON 后，把它转换成 Java 对象。",
    )
    add_table(
        document,
        ["状态码", "含义", "项目中的例子"],
        [
            ["200", "请求成功", "查询或创建成功后返回"],
            ["201", "资源已创建", "创建驾驶事件和碳排报告"],
            ["400", "请求内容不合法", "告警等级不合法或车辆不存在"],
            ["404", "找不到资源", "查询指定车辆或月度报告失败"],
            ["503", "依赖服务当前不可用", "依赖服务没有可用实例"],
        ],
        widths=[0.9, 2.3, 3.35],
    )
    add_body(
        document,
        "状态码不是随便选的。它让调用方不需要读自然语言，也能判断下一步应该重试、修改请求，还是把错误返回给用户。",
    )
    page_break(document)

    add_heading(document, "Spring Boot 的分层结构")
    add_body(
        document,
        "Spring Boot 是 Java 的企业应用开发框架。它提供 Web、数据库、配置和监控等常用能力，让开发者可以把精力放在业务代码上。项目中的每个服务都是一个 Spring Boot 应用。",
    )
    add_body(
        document,
        "一个典型请求会经过 Controller、Service、Repository 和数据库。Controller 负责接收 HTTP 请求；Service 负责业务规则；Repository 负责数据读写；Entity 表示数据库表的 Java 对象；DTO 表示服务之间传递的数据结构。",
    )
    add_table(
        document,
        ["代码层", "解决的问题", "项目例子"],
        [
            ["Controller", "接收和返回 HTTP 数据", "AlertController 创建告警"],
            ["Service", "执行校验和业务计算", "AlertService 校验等级和车辆"],
            ["Repository", "访问数据库", "AlertRepository 保存告警"],
            ["Entity", "映射数据库记录", "Vehicle、Alert、DrivingEvent"],
            ["DTO", "定义请求和响应格式", "AlertRequest、CarbonReportRequest"],
        ],
        widths=[1.2, 2.6, 2.75],
    )
    add_body(
        document,
        "JPA 和 Hibernate 负责把 Java 对象与数据库表对应起来。测试时项目使用 H2 内存数据库，因此测试启动快，也不会修改真实 MySQL 数据。",
    )
    page_break(document)

    add_heading(document, "跨服务调用与容错")
    add_body(
        document,
        "RestTemplate 是 Spring 提供的 HTTP 客户端，可以直接写出请求地址并获取响应。DiscoveryClient 先向 Nacos 查询实例地址，再把地址交给 RestTemplate。驾驶行为服务校验车辆、碳排服务读取月报时使用这种方式。",
    )
    add_body(
        document,
        "OpenFeign 把远程 HTTP 调用声明成 Java 接口。开发者写一个带 @FeignClient 的接口，指定服务名和路径，调用时像调用本地方法一样。告警服务当前使用 OpenFeign 调用 vehicle-service。",
    )
    add_body(
        document,
        "Fallback 是降级处理。被调用服务不可用时，程序不继续执行正常流程，而是进入预先准备的失败分支。项目的降级逻辑会阻止告警落库，避免车辆尚未确认就产生无效数据。",
    )
    add_table(
        document,
        ["概念", "一句话解释"],
        [
            ["LoadBalancer", "服务有多个实例时，选择一个实例发送请求"],
            ["Fallback", "远程调用失败时执行备用处理"],
            ["Circuit Breaker", "失败过多时暂时停止继续请求，防止故障扩散"],
            ["降级", "牺牲部分功能或立即报错，保证主流程不被拖垮"],
        ],
        widths=[1.5, 5.05],
    )
    page_break(document)

    add_heading(document, "告警业务规则")
    add_body(
        document,
        "创建告警时，告警服务先检查 level 是否在 1 到 3 之间，再调用车辆服务确认 vehicleId 对应的车辆存在。两项都通过后，系统把状态设为 pending，最后写入 alert_db。",
    )
    add_body(
        document,
        "如果等级不合法，接口返回 400。车辆不存在也返回 400。车辆服务没有可用实例时返回 503。这个顺序说明业务校验发生在写库之前，失败请求不会留下半成品记录。",
    )
    add_body(
        document,
        "“脏数据”在这里指没有合法业务依据、却已经被保存的数据。例如车辆不存在，告警记录却已经写入，就会让后续查询和处理变得不可信。",
    )
    add_body(
        document,
        "当前代码中 alert-service 已经切换到 OpenFeign，驾驶行为和碳排服务仍使用 DiscoveryClient 加 RestTemplate。两者目标相同，都是通过 Nacos 找到服务后再发 HTTP 请求。",
    )
    page_break(document)

    add_heading(document, "驾驶月报与碳排计算")
    add_body(
        document,
        "驾驶行为服务按车辆和月份查询驾驶事件，再汇总里程、油耗、电耗、急加速次数、急刹车次数和高速事件次数。若这个月没有任何驾驶数据，就返回资源不存在。",
    )
    add_body(
        document,
        "月度评分从 100 分开始，急加速每次扣 2 分，最多扣 30 分；急刹车每次扣 3 分，最多扣 30 分；最高速度超过 120 km/h 的事件每次扣 2 分，最多扣 20 分。公式可以写成：",
    )
    add_body(
        document,
        "score = max(0,\n100 - min(30, 急加速次数 × 2)\n"
        "- min(30, 急刹车次数 × 3)\n"
        "- min(20, 高速事件数 × 2))",
        size=10.5,
        color=INK,
    )
    add_body(
        document,
        "碳排服务先通过 HTTP 读取驾驶月报，再用默认系数计算二氧化碳排放：每升汽油按 2.31 千克 CO₂，每千瓦时电力按 0.581 千克 CO₂。",
    )
    add_body(
        document,
        "CO₂ 千克数 = 汽油升数 × 2.31 + 用电千瓦时数 × 0.581",
        size=10.5,
        color=INK,
    )
    add_body(
        document,
        "同一车辆、同一月份重复生成报告时，系统更新原报告，不重复插入。这个唯一关系写在 vehicleId + report_month 上。",
    )
    page_break(document)

    add_heading(document, "Docker 部署与自动化验证")
    add_body(
        document,
        "镜像是程序及其运行环境的打包结果，容器是镜像运行后的进程。Dockerfile 定义如何构建镜像，Docker Compose 则用一个 YAML 文件启动多个容器，并配置端口、环境变量、依赖关系和数据卷。",
    )
    add_table(
        document,
        ["概念", "初学者理解"],
        [
            ["Image 镜像", "程序的安装包和运行环境模板"],
            ["Container 容器", "根据镜像启动的一次运行实例"],
            ["Volume 数据卷", "把数据库数据保存在容器之外"],
            ["Port Mapping 端口映射", "把宿主机端口转到容器端口"],
            ["Environment Variable 环境变量", "不写死在代码里的配置，例如密码和服务地址"],
            ["Health Check 健康检查", "判断容器是否已经可以正常提供服务"],
        ],
        widths=[1.75, 4.8],
    )
    add_body(
        document,
        "测试使用 H2 内存数据库和 Mock 对象，验证单个服务的业务逻辑，不依赖真实 MySQL 或 Nacos。冒烟测试检查系统能否启动并响应，服务发现脚本进一步检查正常、下线和恢复三个场景。",
    )
    page_break(document)

    add_heading(document, "尚未完成的能力与后续路线")
    add_body(
        document,
        "当前项目已经完成基础服务拆分和调用验证，但还没有面向用户的前端，也没有 BFF 聚合层。BFF 是专门为某类前端整合多个后端接口的一层服务，可以减少前端分别调用多个微服务的负担。",
    )
    add_body(
        document,
        "API 网关位于所有客户端和后端服务之间，负责统一入口、路由和鉴权。Nacos Config 负责集中管理配置，并支持配置变化后刷新。SkyWalking、Prometheus 和 Grafana 用于观察请求链路、指标和运行状态。",
    )
    add_body(
        document,
        "消息队列适合把不强依赖立即完成的工作异步处理。Seata 用于分布式事务，但它不能代替清晰的业务设计。当前项目先用校验、状态码和失败阻断保证基础一致性。",
    )
    add_table(
        document,
        ["老师可能追问", "简短回答"],
        [
            ["为什么拆分", "按业务能力独立开发、部署和数据管理"],
            ["为什么使用 Nacos", "服务地址动态变化，调用方按服务名查找实例"],
            ["为什么不能跨库查询", "会破坏服务边界，使数据库重新耦合"],
            ["怎样避免脏数据", "写库前校验，依赖失败时 fallback 或返回 503"],
            ["怎样证明能运行", "11 个测试、冒烟测试和上下线恢复脚本"],
        ],
        widths=[1.8, 4.75],
    )
    add_body(
        document,
        "答辩时可以把这套项目概括为：五个服务按业务边界拆分，各自管理数据，通过 Nacos 发现彼此，用 HTTP 完成必要协作，并通过测试和脚本验证主流程。",
        bold_prefix="答辩时可以把这套项目概括为：",
    )

    output = OUT_DIR / "智驾伴侣_知识点介绍_周轩亦_2435050112.docx"
    document.save(output)
    return output


if __name__ == "__main__":
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    print(build_narration())
    print(build_knowledge())
