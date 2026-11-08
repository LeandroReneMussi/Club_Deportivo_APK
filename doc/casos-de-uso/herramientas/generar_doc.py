# -*- coding: utf-8 -*-
"""Genera el Markdown y el .docx del documento de casos de uso a partir de contenido.py."""
import os
from docx import Document
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

import contenido as C

AQUI = os.path.dirname(os.path.abspath(__file__))
BASE = os.path.abspath(os.path.join(AQUI, ".."))
DIAG = os.path.join(BASE, "diagramas")
MD = os.path.join(BASE, "Casos_de_uso_UML_ClubDeportivo_v1.5.md")
DOCX = os.path.join(BASE, "DAM_Entrega_GRUPO3_ALVAREZ_AQUINO_MUSSI_WEITZEL_Casos_de_uso_UML.docx")

RESUMEN_COLS = ["CU", "Nombre", "Actor principal", "HU", "Pantalla"]


def actor_corto(a: str) -> str:
    return a.split(" (")[0]


# =============================================================================== Markdown
def md():
    out = []
    w = out.append
    w(f"# {C.TITULO}\n")
    w(f"**{C.GRUPO}** · {', '.join(C.INTEGRANTES)} · Fecha: {C.FECHA} · Versión de la app: {C.VERSION_APP}\n")
    w("## 1. Introducción y alcance\n")
    for p in C.INTRO:
        w(p + "\n")
    w("## 2. Actores\n")
    w("| Actor | Descripción |\n|---|---|")
    for a, d in C.ACTORES:
        w(f"| **{a}** | {d} |")
    w("")
    w("## 3. Diagrama de casos de uso (UML 2.0)\n")
    w("![Diagrama de casos de uso](diagramas/01_casos_de_uso.png)\n")
    w("Los casos con fondo naranja son nuevos o cambian con el spec v1.5.0. «Calcular estado y deuda de la cuota» es el caso incluido por CU-07, CU-09, CU-14 y CU-15; CU-10 extiende a CU-09 cuando el resultado es «Permitido».\n")
    w("### 3.1 Resumen de casos de uso\n")
    w("| " + " | ".join(RESUMEN_COLS) + " |\n|" + "---|" * len(RESUMEN_COLS))
    for cu in C.CASOS:
        w(f"| {cu['id']} | {cu['nombre']} | {actor_corto(cu['actor'])} | {cu['hu']} | {cu['pantalla']} |")
    w("")
    w("## 4. Especificación de los casos de uso\n")
    for cu in C.CASOS:
        w(f"### {cu['id']} — {cu['nombre']}\n")
        w("| Campo | Detalle |\n|---|---|")
        w(f"| Identificador | {cu['id']} |")
        w(f"| Nombre | {cu['nombre']} |")
        w(f"| Actor principal | {cu['actor']} |")
        w(f"| HU relacionada | {cu['hu']} |")
        w(f"| Pantalla | {cu['pantalla']} |")
        w(f"| Precondiciones | {cu['pre']} |")
        w("| Flujo principal | " + "<br>".join(f"{i + 1}. {p}" for i, p in enumerate(cu["flujo"])) + " |")
        w("| Flujos alternativos y de excepción | " + "<br>".join(cu["alt"]) + " |")
        w(f"| Postcondiciones | {cu['post']} |")
        if cu.get("v15"):
            w(f"| (v1.5) | {cu['v15']} |")
        w("")
    w("## 5. Diagrama de clases del dominio (UML 2.0)\n")
    w("![Diagrama de clases](diagramas/02_clases_dominio.png)\n")
    w("Modelo tomado de data/Models.kt: Socio (el carnet son sus atributos carnetEmitido / carnetVence), Cuota, PagoHistorial, Plan, Actividad, Visitante, Rutina → DiaRutina → Ejercicio, FichaNutricional → Consulta, Turno, Ingreso, Usuario y Rol. SocioVista es la clase derivada que aplica la regla de habilitación. Parametros (v1.5) es la entidad nueva con vigencia por fecha; EstadoCuota.VENCIDA deja de decidir la mora.\n")
    w("## 6. Diagramas de secuencia (UML 2.0)\n")
    w("Participantes: la pantalla Compose, ClubViewModel, SocioVista (reglas derivadas), ClubRepositorio y ClubDbHelper (SQLite).\n")
    for i, (archivo, titulo, texto) in enumerate(C.DIAGRAMAS_SECUENCIA, 1):
        w(f"### 6.{i} {titulo}\n")
        w(texto + "\n")
        w(f"![{titulo}](diagramas/{archivo})\n")
    w("## 7. Máquina de estados de la cuota (UML 2.0, v1.5)\n")
    w("![Máquina de estados de la cuota](diagramas/07_estados_cuota.png)\n")
    w("Pendiente → En gracia → En mora se derivan de d = días desde el vencimiento y de la gracia configurada; cualquiera de los tres pasa a Pagada con el pago: por la cuota sola si no venció, y por cuota + recargo + interés desde que venció (en gracia y en mora). La cuota pagada abre la del mes siguiente.\n")
    w("## 8. Diagrama de actividad: alta de socio (UML 2.0)\n")
    w("![Actividad: alta de socio](diagramas/08_actividad_alta_socio.png)\n")
    w("Validaciones en orden: nombre, apellido (v1.5), DNI de 7 u 8 dígitos, DNI único (COUNT en SQLite), correo con formato; luego N° de socio, carnet por 12 meses, cuota inicial a 10 días y ficha nutricional inicial.\n")
    w("## 9. Trazabilidad HU → CU\n")
    w("| HU | Qué | Casos de uso |\n|---|---|---|")
    for hu, que, cus in C.TRAZABILIDAD:
        w(f"| {hu} | {que} | {cus} |")
    w("")
    w("## 10. A confirmar\n")
    for i, a in enumerate(C.A_CONFIRMAR, 1):
        w(f"{i}. {a}")
    w("")
    w("### 10.1 Resuelto con la implementación de la rama feature-08-gracia-recargos\n")
    for a in C.RESUELTOS:
        w(f"- {a}")
    w("")
    w("### 10.1 Resuelto con la implementación de la rama feature-08-gracia-recargos\n")
    for a in C.RESUELTOS:
        w(f"- {a}")
    w("")
    w("## Anexo. Cómo regenerar los diagramas\n")
    w("Los PNG se generan con Python 3 + Pillow desde `herramientas/` (`python dia_casos_de_uso.py`, `dia_clases.py`, `dia_secuencias.py`, `dia_estados_cuota.py`, `dia_actividad_alta.py`). "
      "En `diagramas/` también está la fuente PlantUML (`.puml`) equivalente de cada diagrama para regenerarlos con PlantUML. El documento (.md y .docx) sale de `generar_doc.py`; el PDF se convierte con LibreOffice en modo headless.\n")
    with open(MD, "w", encoding="utf-8") as f:
        f.write("\n".join(out))
    return MD


# =============================================================================== DOCX
def sombrear(celda, color="E8EEF7"):
    tcPr = celda._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), color)
    tcPr.append(shd)


def ancho_celda(celda, cm):
    celda.width = Cm(cm)


def parrafo(doc, texto, size=10.5, bold=False, italic=False, align=None, after=4, color=None):
    p = doc.add_paragraph()
    r = p.add_run(texto)
    r.font.size = Pt(size)
    r.bold = bold
    r.italic = italic
    if color:
        r.font.color.rgb = RGBColor.from_string(color)
    if align:
        p.alignment = align
    p.paragraph_format.space_after = Pt(after)
    return p


def celda_texto(celda, texto, size=9.5, bold=False):
    celda.text = ""
    lineas = texto if isinstance(texto, list) else [texto]
    for i, l in enumerate(lineas):
        p = celda.paragraphs[0] if i == 0 else celda.add_paragraph()
        r = p.add_run(l)
        r.font.size = Pt(size)
        r.bold = bold
        p.paragraph_format.space_after = Pt(1)


def tabla(doc, filas, anchos, encabezado=True, size=9.5):
    t = doc.add_table(rows=len(filas), cols=len(anchos))
    t.style = "Table Grid"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    t.autofit = False
    for j, cm in enumerate(anchos):
        t.columns[j].width = Cm(cm)
    for i, fila in enumerate(filas):
        for j, valor in enumerate(fila):
            c = t.cell(i, j)
            ancho_celda(c, anchos[j])
            celda_texto(c, valor, size, bold=(encabezado and i == 0))
            if encabezado and i == 0:
                sombrear(c)
    doc.add_paragraph().paragraph_format.space_after = Pt(2)
    return t


def imagen(doc, archivo, pie=None, ancho_cm=16):
    ruta = os.path.join(DIAG, archivo)
    doc.add_picture(ruta, width=Cm(ancho_cm))
    doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
    if pie:
        parrafo(doc, pie, 9, italic=True, align=WD_ALIGN_PARAGRAPH.CENTER, after=8, color="555555")


def docx():
    doc = Document()
    for s in doc.sections:
        s.top_margin = s.bottom_margin = Cm(2)
        s.left_margin = s.right_margin = Cm(2.2)
    estilo = doc.styles["Normal"]
    estilo.font.name = "Calibri"
    estilo.font.size = Pt(10.5)

    # ---- portada
    for _ in range(6):
        doc.add_paragraph()
    parrafo(doc, "Desarrollo de Aplicaciones Móviles", 14, align=WD_ALIGN_PARAGRAPH.CENTER, color="555555")
    parrafo(doc, C.TITULO, 24, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, after=18)
    parrafo(doc, "Especificación de casos de uso y diagramas UML 2.0 de la app Android «Club Deportivo» (Kotlin + Jetpack Compose + SQLite)", 12, italic=True, align=WD_ALIGN_PARAGRAPH.CENTER, after=40)
    parrafo(doc, C.GRUPO, 18, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, after=6)
    parrafo(doc, " · ".join(C.INTEGRANTES), 14, align=WD_ALIGN_PARAGRAPH.CENTER, after=40)
    parrafo(doc, f"Fecha: {C.FECHA}", 12, align=WD_ALIGN_PARAGRAPH.CENTER, after=4)
    parrafo(doc, f"Versión de la app: {C.VERSION_APP}", 12, align=WD_ALIGN_PARAGRAPH.CENTER, after=4)
    doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)

    # ---- 1
    doc.add_heading("1. Introducción y alcance", 1)
    for p in C.INTRO:
        parrafo(doc, p, after=6)

    # ---- 2
    doc.add_heading("2. Actores", 1)
    tabla(doc, [["Actor", "Descripción"]] + [[a, d] for a, d in C.ACTORES], [3.6, 13])

    # ---- 3
    doc.add_heading("3. Diagrama de casos de uso (UML 2.0)", 1)
    imagen(doc, "01_casos_de_uso.png", "Figura 1. Diagrama de casos de uso de la App Club Deportivo v1.5.0.")
    parrafo(doc, "Los casos con fondo naranja son nuevos o cambian con el spec v1.5.0. «Calcular estado y deuda de la cuota» es el caso incluido por CU-07, CU-09, CU-14 y CU-15; CU-10 extiende a CU-09 cuando el resultado es «Permitido». El No socio / Visitante no opera la app: su pago diario lo registra el Administrativo (CU-11).", after=8)
    doc.add_heading("3.1 Resumen de casos de uso", 2)
    filas = [RESUMEN_COLS] + [[cu["id"], cu["nombre"], actor_corto(cu["actor"]), cu["hu"], cu["pantalla"]] for cu in C.CASOS]
    tabla(doc, filas, [1.5, 5.0, 2.6, 2.3, 5.2], size=8.5)

    # ---- 4
    doc.add_heading("4. Especificación de los casos de uso", 1)
    parrafo(doc, "Cada caso de uso se especifica con identificador, nombre, actor principal, precondiciones, flujo principal numerado, flujos alternativos y de excepción (validaciones), postcondiciones, historia de usuario relacionada y pantalla. Las referencias entre paréntesis son las funciones de ClubViewModel que ejecutan cada paso.", after=8)
    for cu in C.CASOS:
        doc.add_heading(f"{cu['id']} — {cu['nombre']}", 2)
        filas = [
            ["Identificador", cu["id"]],
            ["Nombre", cu["nombre"]],
            ["Actor principal", cu["actor"]],
            ["HU relacionada", cu["hu"]],
            ["Pantalla", cu["pantalla"]],
            ["Precondiciones", cu["pre"]],
            ["Flujo principal", [f"{i + 1}. {p}" for i, p in enumerate(cu["flujo"])]],
            ["Flujos alternativos y de excepción", cu["alt"]],
            ["Postcondiciones", cu["post"]],
        ]
        if cu.get("v15"):
            filas.append(["(v1.5)", cu["v15"]])
        t = tabla(doc, filas, [3.6, 13], encabezado=False, size=9)
        for fila in t.rows:
            c = fila.cells[0]
            sombrear(c, "F3F3F3")
            for p in c.paragraphs:
                for r in p.runs:
                    r.bold = True

    # ---- 5
    doc.add_heading("5. Diagrama de clases del dominio (UML 2.0)", 1)
    parrafo(doc, "Modelo tomado de data/Models.kt: Socio (el carnet son sus atributos carnetEmitido / carnetVence), Cuota, PagoHistorial, Plan, Actividad, Visitante, Rutina → DiaRutina → Ejercicio, FichaNutricional → Consulta, Turno, Ingreso, Usuario y Rol. SocioVista es la clase derivada que aplica la regla de habilitación. Parametros (v1.5) es la entidad nueva con vigencia por fecha; EstadoCuota.VENCIDA deja de decidir la mora. Las multiplicidades siguen las listas y los opcionales de Models.kt.", after=6)
    imagen(doc, "02_clases_dominio.png", "Figura 2. Diagrama de clases del modelo de dominio (v1.4.0 + v1.5).")

    # ---- 6
    doc.add_heading("6. Diagramas de secuencia (UML 2.0)", 1)
    parrafo(doc, "Participantes: la pantalla Compose, ClubViewModel, SocioVista (reglas derivadas), ClubRepositorio y ClubDbHelper (SQLite). Las guardas de los marcos alt / opt son las condiciones del código y del spec.", after=6)
    for i, (archivo, titulo, texto) in enumerate(C.DIAGRAMAS_SECUENCIA, 1):
        doc.add_heading(f"6.{i} {titulo}", 2)
        parrafo(doc, texto, after=6)
        imagen(doc, archivo, f"Figura {2 + i}. {titulo}.")

    # ---- 7
    doc.add_heading("7. Máquina de estados de la cuota (UML 2.0, v1.5)", 1)
    parrafo(doc, "Pendiente → En gracia → En mora se derivan de d = días desde el vencimiento y de la gracia configurada; cualquiera de los tres pasa a Pagada con el pago: por la cuota sola si no venció, y por cuota + recargo + interés desde que venció (en gracia y en mora). La cuota pagada abre la del mes siguiente. Si el administrador cambia la gracia, el estado se recalcula.", after=6)
    imagen(doc, "07_estados_cuota.png", "Figura 7. Máquina de estados de la cuota del socio (v1.5).")

    # ---- 8
    doc.add_heading("8. Diagrama de actividad: alta de socio (UML 2.0)", 1)
    parrafo(doc, "Validaciones en orden: nombre, apellido (v1.5), DNI de 7 u 8 dígitos, DNI único (COUNT en SQLite), correo con formato; luego N° de socio, carnet por 12 meses, cuota inicial a 10 días y ficha nutricional inicial. El mismo flujo, sin la generación de carnet y cuota, sirve para la edición.", after=6)
    imagen(doc, "08_actividad_alta_socio.png", "Figura 8. Diagrama de actividad del alta de socio con sus validaciones.", ancho_cm=14)

    # ---- 9
    doc.add_heading("9. Trazabilidad HU → CU", 1)
    tabla(doc, [["HU", "Qué", "Casos de uso"]] + [list(f) for f in C.TRAZABILIDAD], [1.6, 9.0, 6.0], size=9)

    # ---- 10
    doc.add_heading("10. A confirmar", 1)
    parrafo(doc, "Puntos que no surgen con claridad del código actual ni del spec v1.5.0 y que conviene validar con el equipo antes de dar por cerrada la especificación:", after=4)
    for a in C.A_CONFIRMAR:
        p = doc.add_paragraph(a, style="List Number")
        for r in p.runs:
            r.font.size = Pt(10)
    doc.add_heading("10.1 Resuelto con la implementación de la rama feature-08-gracia-recargos", 2)
    for a in C.RESUELTOS:
        p = doc.add_paragraph(a, style="List Bullet")
        for r in p.runs:
            r.font.size = Pt(10)
    doc.add_heading("10.1 Resuelto con la implementación de la rama feature-08-gracia-recargos", 2)
    for a in C.RESUELTOS:
        p = doc.add_paragraph(a, style="List Bullet")
        for r in p.runs:
            r.font.size = Pt(10)

    doc.add_heading("Anexo. Cómo regenerar los diagramas", 1)
    parrafo(doc, "Los PNG se generan con Python 3 + Pillow desde doc/casos-de-uso/herramientas/ (dia_casos_de_uso.py, dia_clases.py, dia_secuencias.py, dia_estados_cuota.py, dia_actividad_alta.py). En doc/casos-de-uso/diagramas/ también está la fuente PlantUML (.puml) equivalente de cada diagrama. El documento (.md y .docx) sale de generar_doc.py y el PDF se convierte con LibreOffice en modo headless.", after=6)

    doc.save(DOCX)
    return DOCX


if __name__ == "__main__":
    print(md())
    print(docx())
