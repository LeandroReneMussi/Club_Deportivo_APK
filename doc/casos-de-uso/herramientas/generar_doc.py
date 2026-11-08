# -*- coding: utf-8 -*-
"""Genera el Markdown y el .docx del documento de casos de uso (plantilla única, 26 CU)."""
import os
from docx import Document
from docx.enum.section import WD_ORIENT, WD_SECTION
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor
from PIL import Image

import contenido as C
import casos_extra as X
import cu_secuencias as S

AQUI = os.path.dirname(os.path.abspath(__file__))
BASE = os.path.abspath(os.path.join(AQUI, ".."))
DIAG = os.path.join(BASE, "diagramas")
MD = os.path.join(BASE, "Casos_de_uso_UML_ClubDeportivo_v1.5.md")
DOCX = os.path.join(BASE, "DAM_Entrega_GRUPO3_ALVAREZ_AQUINO_MUSSI_WEITZEL_Casos_de_uso_UML.docx")

RESUMEN_COLS = ["CU", "Nombre", "Actor principal", "HU", "Pantalla"]
SLUG = {cu: slug for cu, slug, *_ in S.SEC}

INTRO = [
    "Este documento especifica los 26 casos de uso de la app Android «Club Deportivo» (Kotlin + Jetpack Compose + SQLite) con una misma plantilla para todos y los acompaña con diagramas UML 2.0: casos de uso, clases del dominio, máquina de estados de la cuota, actividad del alta y un diagrama de secuencia por cada caso de uso.",
    "La fuente de verdad es el código de la rama feature-08-gracia-recargos (app 1.5.0): ClubViewModel.kt (todas las acciones), SocioVista.kt (reglas derivadas), ClubRepositorio.kt y ClubDbHelper.kt (persistencia SQLite), Models.kt, Validaciones.kt, DemoData.kt, las pantallas de ui/screens y las pruebas unitarias de app/src/test (de ellas salen los flujos alternativos y los mensajes exactos). Novedades de esta versión: carnet digital con formato de tarjeta, pantalla «QR de ingreso» (CU-26), etiquetas «Habilitado · En período de gracia» y «En período de gracia», acción rápida «Parámetros del club», cápsulas estadoCorto / contadorCorto / cuotaSubLabel y datos de demo con Rocío Paz (en gracia) y parámetros 2025/2026. Lo nuevo de la v1.5 se marca «(v1.5)».",
    "Plantilla de cada caso: ID, nombre, actor principal, actores secundarios / participantes, HU relacionada, pantallas, precondiciones, disparador, flujo principal numerado, flujos alternativos y de excepción (con los mensajes reales), postcondiciones, reglas de negocio, datos de demo y diagrama de secuencia. En los diagramas, la persistencia se muestra así: cada cambio de estado pasa por el setter de ClubViewModel, que llama a ClubRepositorio.guardar; este escribe en SQLite solo si los datos cambiaron, con ClubDbHelper.guardar (una transacción que reescribe las tablas). Los datos de demo son relativos a la fecha del dispositivo; las pruebas fijan el 08/09/2026. Lo que no surge del código figura en «A confirmar».",
]


def actor_corto(a: str) -> str:
    return a.split(" (")[0]


def cu_completos():
    out = []
    for cu in C.CASOS:
        d = dict(cu)
        d.update(X.FIX.get(cu["id"], {}))
        d.update(X.EXTRA[cu["id"]])
        out.append(d)
    return out


CASOS = cu_completos()
CAMPOS = [
    ("ID", lambda c: c["id"]),
    ("Nombre", lambda c: c["nombre"]),
    ("Actor principal", lambda c: c["actor"]),
    ("Actores secundarios / participantes", lambda c: c["part"]),
    ("HU relacionada", lambda c: c["hu"]),
    ("Pantalla(s)", lambda c: c["pantalla"]),
    ("Precondiciones", lambda c: c["pre"]),
    ("Disparador", lambda c: c["disp"]),
    ("Flujo principal", lambda c: [f"{i + 1}. {p}" for i, p in enumerate(c["flujo"])]),
    ("Flujos alternativos y de excepción", lambda c: c["alt"]),
    ("Postcondiciones", lambda c: c["post"]),
    ("Reglas de negocio", lambda c: [f"RN{i + 1}. {r}" for i, r in enumerate(c["reglas"])]),
    ("Datos de demo para probarlo", lambda c: c["demo"]),
]


def pie_diagrama(c):
    return f"Diagrama de secuencia UML 2.0 de {c['id']} (diagramas/{SLUG[c['id']]}.png), en la figura siguiente"


def trazabilidad():
    filas = [[c["hu"], c["id"], c["pantalla"], "; ".join(X.PRUEBAS[c["id"]])] for c in CASOS]
    filas.sort(key=lambda f: (f[0], f[1]))
    return filas


# =============================================================================== Markdown
def md():
    out = []
    w = out.append
    w(f"# {C.TITULO}\n")
    w(f"**{C.GRUPO}** · {', '.join(C.INTEGRANTES)} · Fecha: {C.FECHA} · Versión de la app: {C.VERSION_APP}\n")
    w("## 1. Introducción y alcance\n")
    for p in INTRO:
        w(p + "\n")
    w("## 2. Actores\n")
    w("| Actor | Descripción |\n|---|---|")
    for a, d in C.ACTORES:
        w(f"| **{a}** | {d} |")
    w("")
    w("## 3. Diagrama de casos de uso (UML 2.0)\n")
    w("![Diagrama de casos de uso](diagramas/01_casos_de_uso.png)\n")
    w("Los casos con fondo naranja son nuevos o cambian con el spec v1.5.0. «Calcular estado y deuda de la cuota» es el caso incluido por CU-07, CU-09, CU-14 y CU-15; CU-10 extiende a CU-09 cuando el resultado es «Permitido» y CU-26 (QR de ingreso) extiende a CU-16 (carnet digital).\n")
    w("### 3.1 Resumen de casos de uso\n")
    w("| " + " | ".join(RESUMEN_COLS) + " |\n|" + "---|" * len(RESUMEN_COLS))
    for cu in CASOS:
        w(f"| {cu['id']} | {cu['nombre']} | {actor_corto(cu['actor'])} | {cu['hu']} | {cu['pantalla']} |")
    w("")
    w("## 4. Especificación de los casos de uso (plantilla única, con su diagrama de secuencia)\n")
    for c in CASOS:
        w(f"### {c['id']} — {c['nombre']}\n")
        w("| Campo | Detalle |\n|---|---|")
        for nombre, f in CAMPOS:
            v = f(c)
            w(f"| {nombre} | " + ("<br>".join(v) if isinstance(v, list) else v) + " |")
        w(f"| Diagrama | {pie_diagrama(c)} |")
        w("")
        w(f"![Secuencia {c['id']}](diagramas/{SLUG[c['id']]}.png)\n")
    w("## 5. Diagrama de clases del dominio (UML 2.0)\n")
    w("![Diagrama de clases](diagramas/02_clases_dominio.png)\n")
    w("Modelo tomado de data/Models.kt: Socio (el carnet son sus atributos carnetEmitido / carnetVence), Cuota, PagoHistorial, Plan, Actividad, Visitante, Rutina → DiaRutina → Ejercicio, FichaNutricional → Consulta, Turno, Ingreso, Usuario y Rol. SocioVista es la clase derivada que aplica la regla de habilitación. Parametros es la entidad con vigencia por fecha; EstadoCuota.VENCIDA deja de decidir la mora.\n")
    w("## 6. Máquina de estados de la cuota (UML 2.0)\n")
    w("![Máquina de estados de la cuota](diagramas/07_estados_cuota.png)\n")
    w("Pendiente → En período de gracia → En mora se derivan de d = días desde el vencimiento y de la gracia configurada; cualquiera de los tres pasa a Pagada con el pago: por la cuota sola si no venció, y por cuota + recargo + interés desde que venció. La cuota pagada abre la del mes siguiente.\n")
    w("## 7. Diagrama de actividad: alta de socio (UML 2.0)\n")
    w("![Actividad: alta de socio](diagramas/08_actividad_alta_socio.png)\n")
    w("Validaciones en orden: nombre, apellido, DNI de 7 u 8 dígitos, correo con formato y DNI único (COUNT en SQLite); luego N° de socio, carnet por 12 meses, cuota inicial a 10 días y ficha nutricional inicial.\n")
    w("## 8. Trazabilidad HU → CU → pantalla → prueba automatizada\n")
    w("Las pruebas están en app/src/test/java/ar/edu/ifts29/clubdeportivo; sin aclaración son de ClubViewModelTest.\n")
    w("| HU | CU | Pantalla(s) | Prueba automatizada |\n|---|---|---|---|")
    for f in trazabilidad():
        w("| " + " | ".join(f) + " |")
    w("")
    w("## 9. A confirmar\n")
    for i, a in enumerate(X.A_CONFIRMAR, 1):
        w(f"{i}. {a}")
    w("")
    w("## Anexo. Cómo regenerar los diagramas\n")
    w("Los PNG se generan con Python 3 + Pillow desde `herramientas/` (`python dia_casos_de_uso.py`, `dia_clases.py`, `dia_estados_cuota.py`, `dia_actividad_alta.py` y `dia_secuencias.py`, que dibuja los 26 diagramas de `cu_secuencias.py`). `generar_puml.py` escribe en `diagramas/` la fuente PlantUML (`.puml`) equivalente de cada diagrama. El documento (.md y .docx) sale de `generar_doc.py` y el PDF se convierte con LibreOffice en modo headless.\n")
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
            c.width = Cm(anchos[j])
            celda_texto(c, valor, size, bold=(encabezado and i == 0))
            if encabezado and i == 0:
                sombrear(c)
    doc.add_paragraph().paragraph_format.space_after = Pt(2)
    return t


def imagen(doc, archivo, pie=None, ancho_cm=16):
    doc.add_picture(os.path.join(DIAG, archivo), width=Cm(ancho_cm))
    doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
    if pie:
        parrafo(doc, pie, 9, italic=True, align=WD_ALIGN_PARAGRAPH.CENTER, after=8, color="555555")


def seccion(doc, apaisada):
    sec = doc.add_section(WD_SECTION.NEW_PAGE)
    if apaisada:
        sec.orientation = WD_ORIENT.LANDSCAPE
        sec.page_width, sec.page_height = Cm(29.7), Cm(21.0)
        sec.left_margin = sec.right_margin = Cm(1.2)
        sec.top_margin = sec.bottom_margin = Cm(1.2)
    else:
        sec.orientation = WD_ORIENT.PORTRAIT
        sec.page_width, sec.page_height = Cm(21.0), Cm(29.7)
        sec.left_margin = sec.right_margin = Cm(2.2)
        sec.top_margin = sec.bottom_margin = Cm(2)
    return sec


def imagen_apaisada(doc, archivo, titulo):
    ruta = os.path.join(DIAG, archivo)
    w, h = Image.open(ruta).size
    ancho = min(27.0, 17.0 * w / h)
    parrafo(doc, titulo, 11, bold=True, after=2)
    doc.add_picture(ruta, width=Cm(ancho))
    doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER


def docx():
    doc = Document()
    for s in doc.sections:
        s.top_margin = s.bottom_margin = Cm(2)
        s.left_margin = s.right_margin = Cm(2.2)
    estilo = doc.styles["Normal"]
    estilo.font.name = "Calibri"
    estilo.font.size = Pt(10.5)

    for _ in range(6):
        doc.add_paragraph()
    parrafo(doc, "Desarrollo de Aplicaciones Móviles", 14, align=WD_ALIGN_PARAGRAPH.CENTER, color="555555")
    parrafo(doc, C.TITULO, 24, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, after=18)
    parrafo(doc, "Especificación de casos de uso con plantilla única y diagramas UML 2.0 de la app Android «Club Deportivo» (Kotlin + Jetpack Compose + SQLite)", 12, italic=True, align=WD_ALIGN_PARAGRAPH.CENTER, after=40)
    parrafo(doc, C.GRUPO, 18, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, after=6)
    parrafo(doc, " · ".join(C.INTEGRANTES), 14, align=WD_ALIGN_PARAGRAPH.CENTER, after=40)
    parrafo(doc, f"Fecha: {C.FECHA}", 12, align=WD_ALIGN_PARAGRAPH.CENTER, after=4)
    parrafo(doc, f"Versión de la app: {C.VERSION_APP}", 12, align=WD_ALIGN_PARAGRAPH.CENTER, after=4)
    doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)

    doc.add_heading("1. Introducción y alcance", 1)
    for p in INTRO:
        parrafo(doc, p, after=6)
    doc.add_heading("2. Actores", 1)
    tabla(doc, [["Actor", "Descripción"]] + [[a, d] for a, d in C.ACTORES], [3.6, 13])

    doc.add_heading("3. Diagrama de casos de uso (UML 2.0)", 1)
    imagen(doc, "01_casos_de_uso.png", "Figura 1. Diagrama de casos de uso de la App Club Deportivo v1.5.0.")
    parrafo(doc, "Los casos con fondo naranja son nuevos o cambian con el spec v1.5.0. «Calcular estado y deuda de la cuota» es el caso incluido por CU-07, CU-09, CU-14 y CU-15; CU-10 extiende a CU-09 cuando el resultado es «Permitido» y CU-26 (QR de ingreso) extiende a CU-16 (carnet digital). El No socio / Visitante no opera la app: su pago diario lo registra el Administrativo (CU-11).", after=8)
    doc.add_heading("3.1 Resumen de casos de uso", 2)
    filas = [RESUMEN_COLS] + [[cu["id"], cu["nombre"], actor_corto(cu["actor"]), cu["hu"], cu["pantalla"]] for cu in CASOS]
    tabla(doc, filas, [1.5, 5.0, 2.6, 2.3, 5.2], size=8.5)

    doc.add_heading("4. Especificación de los casos de uso", 1)
    parrafo(doc, "Todos los casos usan la misma plantilla. Tras la tabla de cada caso, en una página apaisada, va su diagrama de secuencia UML 2.0 con los participantes reales del código.", after=8)
    for c in CASOS:
        doc.add_heading(f"{c['id']} — {c['nombre']}", 2)
        filas = [[n, f(c)] for n, f in CAMPOS] + [["Diagrama", pie_diagrama(c)]]
        t = tabla(doc, filas, [3.6, 13], encabezado=False, size=9)
        for fila in t.rows:
            cel = fila.cells[0]
            sombrear(cel, "F3F3F3")
            for p in cel.paragraphs:
                for r in p.runs:
                    r.bold = True
        seccion(doc, True)
        imagen_apaisada(doc, SLUG[c["id"]] + ".png", f"Diagrama de secuencia de {c['id']} — {c['nombre']}")
        seccion(doc, False)

    doc.add_heading("5. Diagrama de clases del dominio (UML 2.0)", 1)
    parrafo(doc, "Modelo tomado de data/Models.kt: Socio (el carnet son sus atributos carnetEmitido / carnetVence), Cuota, PagoHistorial, Plan, Actividad, Visitante, Rutina → DiaRutina → Ejercicio, FichaNutricional → Consulta, Turno, Ingreso, Usuario y Rol. SocioVista es la clase derivada que aplica la regla de habilitación. Parametros es la entidad con vigencia por fecha; EstadoCuota.VENCIDA deja de decidir la mora.", after=6)
    imagen(doc, "02_clases_dominio.png", "Figura. Diagrama de clases del modelo de dominio (v1.5).")
    doc.add_heading("6. Máquina de estados de la cuota (UML 2.0)", 1)
    parrafo(doc, "Pendiente → En período de gracia → En mora se derivan de d = días desde el vencimiento y de la gracia configurada; cualquiera de los tres pasa a Pagada con el pago: por la cuota sola si no venció, y por cuota + recargo + interés desde que venció. La cuota pagada abre la del mes siguiente. Si el administrador cambia la gracia, el estado se recalcula.", after=6)
    imagen(doc, "07_estados_cuota.png", "Figura. Máquina de estados de la cuota del socio (v1.5).")
    doc.add_heading("7. Diagrama de actividad: alta de socio (UML 2.0)", 1)
    parrafo(doc, "Validaciones en orden: nombre, apellido, DNI de 7 u 8 dígitos, correo con formato y DNI único (COUNT en SQLite); luego N° de socio, carnet por 12 meses, cuota inicial a 10 días y ficha nutricional inicial.", after=6)
    imagen(doc, "08_actividad_alta_socio.png", "Figura. Diagrama de actividad del alta de socio.", ancho_cm=14)

    seccion(doc, True)
    doc.add_heading("8. Trazabilidad HU → CU → pantalla → prueba automatizada", 1)
    parrafo(doc, "Las pruebas están en app/src/test/java/ar/edu/ifts29/clubdeportivo; sin aclaración son de ClubViewModelTest.", after=4)
    tabla(doc, [["HU", "CU", "Pantalla(s)", "Prueba automatizada"]] + trazabilidad(), [1.6, 1.6, 7.0, 16.0], size=8)
    seccion(doc, False)
    doc.add_heading("9. A confirmar", 1)
    parrafo(doc, "Puntos que no surgen con claridad del código ni de las pruebas y que conviene validar con el equipo:", after=4)
    for a in X.A_CONFIRMAR:
        p = doc.add_paragraph(a, style="List Number")
        for r in p.runs:
            r.font.size = Pt(10)
    doc.add_heading("Anexo. Cómo regenerar los diagramas", 1)
    parrafo(doc, "Los PNG se generan con Python 3 + Pillow desde doc/casos-de-uso/herramientas/ (dia_casos_de_uso.py, dia_clases.py, dia_estados_cuota.py, dia_actividad_alta.py y dia_secuencias.py, que dibuja los 26 diagramas de cu_secuencias.py). generar_puml.py escribe la fuente PlantUML (.puml) equivalente de cada diagrama. El documento sale de generar_doc.py y el PDF se convierte con LibreOffice en modo headless.", after=6)
    doc.save(DOCX)
    return DOCX


if __name__ == "__main__":
    print(md())
    print(docx())
