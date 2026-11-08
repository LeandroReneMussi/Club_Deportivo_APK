"""Arma el documento de capturas de la entrega (docx) a partir de indice.txt y los PNG.

Uso: python armar_pdf_capturas.py entrega/capturas entrega/DAM_Entrega_GRUPO3_ALVAREZ_AQUINO_MUSSI_WEITZEL_Capturas_v1.5.docx
El PDF se convierte después con LibreOffice en modo headless.
"""
import sys
from pathlib import Path

from docx import Document
from docx.enum.section import WD_ORIENT
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

CAPTURAS = Path(sys.argv[1])
SALIDA = Path(sys.argv[2])
VERSION = "1.5.1"
GRUPO = "Grupo 3"
INTEGRANTES = "Alvarez · Aquino Lesme · Mussi · Weitzel"
FECHA = "8 de noviembre de 2026"

NOMBRES = {
    "LoginActivity": "Inicio de sesión (todos los roles)",
    "SocioActivity": "Socio",
    "AdminActivity": "Administrativo",
    "EntrenadoraActivity": "Entrenadora",
    "NutricionistaActivity": "Nutricionista",
}

filas = []
for linea in (CAPTURAS / "indice.txt").read_text(encoding="utf-8").splitlines():
    if not linea.strip():
        continue
    archivo, actividad, pantalla, hu, epigrafe = linea.split("|", 4)
    filas.append((archivo, actividad, pantalla, hu, epigrafe))

doc = Document()
estilo = doc.styles["Normal"]
estilo.font.name = "Calibri"
estilo.font.size = Pt(10.5)
for s in doc.sections:
    s.page_height, s.page_width = Cm(29.7), Cm(21.0)
    s.orientation = WD_ORIENT.PORTRAIT
    s.left_margin = s.right_margin = Cm(1.6)
    s.top_margin = s.bottom_margin = Cm(1.5)


def parrafo(texto, tam=11, negrita=False, centrado=False, color=None, espacio_despues=4):
    p = doc.add_paragraph()
    r = p.add_run(texto)
    r.font.size = Pt(tam)
    r.bold = negrita
    if color:
        r.font.color.rgb = RGBColor.from_string(color)
    if centrado:
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(espacio_despues)
    return p


# Portada
for _ in range(6):
    doc.add_paragraph()
parrafo("Club Deportivo", 30, True, True, espacio_despues=2)
parrafo(f"App Android v{VERSION} · Capturas de pantalla", 16, False, True, "555555", 18)
parrafo("Desarrollo de Aplicaciones Móviles · Etapa Entrega", 12, False, True, "555555", 2)
parrafo("IFTS N° 29", 12, False, True, "555555", 30)
parrafo(GRUPO, 20, True, True, espacio_despues=4)
parrafo(INTEGRANTES, 14, False, True, espacio_despues=30)
parrafo(FECHA, 11, False, True, "555555")
doc.add_page_break()

# Introducción
parrafo("Cómo leer este documento", 16, True, espacio_despues=8)
parrafo(
    f"Las capturas se tomaron sobre la versión {VERSION} de la app instalada en un emulador Android "
    "(Pixel, Android 15) con la fecha real del dispositivo. Cada pantalla se fotografió con el "
    "recorrido automatizado CapturasPorActivityTest, que ejecuta las mismas acciones que los "
    "botones de la interfaz, así que lo que se ve es la app funcionando sobre su base SQLite, "
    "no una maqueta.",
    espacio_despues=6,
)
parrafo(
    "Cuentas de prueba: Andres / Socio123 (socio), Jorge / Admin123 (administrativo), "
    "Ana / Prof123 (entrenadora), Leandro / Nut123 (nutricionista).",
    espacio_despues=6,
)
parrafo(
    f"Qué cubre la versión {VERSION} respecto de la consigna: registro de socios (nombre y apellido "
    "separados, DNI único, correo válido) y de no socios (pago diario con fecha y medio de pago); "
    "pago de la cuota mensual en caja y desde la app; consulta de la información registrada; "
    "listado de socios cuya cuota vence hoy y en la semana; validaciones de datos incompletos o "
    "incorrectos. Además, el administrador fija los días de gracia, el recargo y el interés mensual, "
    "con vigencia por fecha: una cuota vencida sigue habilitada durante la gracia (aviso naranja y "
    "contador de días) y pasado ese plazo bloquea el acceso; la deuda suma cuota, recargo e interés. "
    "La 1.5.1 agrega «Registrar visitante» cuando el DNI no es de un socio y la hora real en la presencia de la entrenadora.",
    espacio_despues=6,
)
parrafo("Cada epígrafe indica la pantalla, la historia de usuario (HU) y lo que muestra la captura.", espacio_despues=6)

# Índice por actividad
parrafo("Contenido", 14, True, espacio_despues=6)
orden = []
for f in filas:
    if f[1] not in orden:
        orden.append(f[1])
for i, act in enumerate(orden, 1):
    n = sum(1 for f in filas if f[1] == act)
    parrafo(f"{i}. {NOMBRES.get(act, act)} — {n} capturas", 11, espacio_despues=2)
doc.add_page_break()


def celda_captura(celda, fila):
    archivo, actividad, pantalla, hu, epigrafe = fila
    celda.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = celda.paragraphs[0].add_run()
    run.add_picture(str(CAPTURAS / archivo), width=Cm(7.6))
    p = celda.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    r1 = p.add_run(f"{pantalla} · {hu}\n")
    r1.bold = True
    r1.font.size = Pt(9.5)
    r2 = p.add_run(epigrafe)
    r2.font.size = Pt(9)
    r2.font.color.rgb = RGBColor.from_string("444444")
    p.paragraph_format.space_after = Pt(6)


def sin_bordes(tabla):
    tbl = tabla._tbl
    borders = OxmlElement("w:tblBorders")
    for b in ("top", "left", "bottom", "right", "insideH", "insideV"):
        el = OxmlElement(f"w:{b}")
        el.set(qn("w:val"), "nil")
        borders.append(el)
    tbl.tblPr.append(borders)


for i, act in enumerate(orden, 1):
    del_act = [f for f in filas if f[1] == act]
    parrafo(f"{i}. {NOMBRES.get(act, act)}", 16, True, espacio_despues=6)
    parrafo(f"{act} · {len(del_act)} capturas", 10, False, False, "777777", 8)
    for j in range(0, len(del_act), 2):
        par = del_act[j : j + 2]
        tabla = doc.add_table(rows=1, cols=2)
        tabla.alignment = WD_TABLE_ALIGNMENT.CENTER
        sin_bordes(tabla)
        for k, fila in enumerate(par):
            celda_captura(tabla.rows[0].cells[k], fila)
    if i < len(orden):
        doc.add_page_break()

SALIDA.parent.mkdir(parents=True, exist_ok=True)
doc.save(str(SALIDA))
print("docx:", SALIDA, SALIDA.stat().st_size, "bytes;", len(filas), "capturas")
