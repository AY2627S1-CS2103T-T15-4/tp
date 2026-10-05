import re
from pathlib import Path
from pypdf import PdfReader
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak, Preformatted
from reportlab.lib.units import mm

src = Path(r'C:\Users\xande\Downloads\cs2106_1819s2_midterm_solution.pdf')
out = Path('output/pdf/cs2106_1819s2_midterm_questions_only.pdf')
out.parent.mkdir(parents=True, exist_ok=True)

def clean(text):
    # Remove Word comment balloons and their wrapped continuation text.
    text = re.sub(r'\s*Commented \[TKYC\d+\]:.*?(?=\s*Commented \[TKYC\d+\]:|\n(?:\d+\.|[a-e]\.|Questions|The following|For questions|Which ONE|~~ END)|\Z)', ' ', text, flags=re.S)
    text = re.sub(r'Commented \[TKYC\d+\]:[^\n]*', '', text)
    text = text.replace('+', '')
    text = re.sub(r'\n\s*\n+', '\n\n', text)
    return text.strip()

reader = PdfReader(str(src))
pages = [clean(p.extract_text() or '') for p in reader.pages]

styles = getSampleStyleSheet()
body = ParagraphStyle('body', parent=styles['BodyText'], fontName='Helvetica', fontSize=9.2, leading=11.5, spaceAfter=4)
head = ParagraphStyle('head', parent=styles['Heading1'], alignment=TA_CENTER, fontSize=15, leading=18, spaceAfter=10)
code = ParagraphStyle('code', parent=body, fontName='Courier', fontSize=7.5, leading=9)

story = [Paragraph('CS2106 Operating Systems - 2017/18 Semester 2<br/>Mid-Term Test (Questions Only)', head)]
for idx, page in enumerate(pages):
    if idx == 0:
        # Keep only the exam instructions, omitting the solution-document comment artifacts.
        pass
    for block in re.split(r'\n\s*\n', page):
        block = block.strip()
        if not block:
            continue
        safe = block.replace('&','&amp;').replace('<','&lt;').replace('>','&gt;').replace('\n','<br/>')
        if 'int fd[' in block or 'ISR(TIMER' in block or 'void Process_' in block or '#define ' in block or 'P1:' in block:
            story.append(Preformatted(block, code))
        else:
            story.append(Paragraph(safe, body))
    if idx != len(pages)-1:
        story.append(PageBreak())

doc = SimpleDocTemplate(str(out), pagesize=A4, rightMargin=17*mm, leftMargin=17*mm, topMargin=15*mm, bottomMargin=15*mm, title='CS2106 Mid-Term Questions Only')
doc.build(story)
print(out)
