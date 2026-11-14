from docx import Document
from docx.oxml.ns import qn
from docx.shared import Pt

fonts = [
    "Arial Unicode MS",
    "Heiti SC",
    "STHeiti",
    "Songti SC",
    "STSong",
    "PingFang SC",
    "Hiragino Sans GB W3",
    "Noto Sans CJK SC",
    "Noto Sans",
    "Aptos",
]

document = Document()
for font in fonts:
    paragraph = document.add_paragraph()
    run = paragraph.add_run(f"{font} 智驾伴侣 周轩亦 2435050112")
    run.font.name = font
    run.font.size = Pt(16)
    run._element.get_or_add_rPr().rFonts.set(qn("w:eastAsia"), font)
document.save(
    "/Users/zhouyiming/Documents/work/micro_lesson/lesson/.codex-doc-build/test-fonts.docx"
)
