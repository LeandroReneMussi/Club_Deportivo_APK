# -*- coding: utf-8 -*-
"""Genera entrega/Guion_coloquio_v1.5.docx y .pdf a partir de entrega/Guion_coloquio_v1.5.md.

Uso: python generar_guion_coloquio.py   (desde cualquier carpeta)
El contenido sale del Markdown; este script solo lo vuelca con el estilo de la documentación de la entrega
(plantilla de la v1.2 y mismos encabezados, tablas y portada que generar_documentacion_v1_5.py).
"""
import os
import re
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from docgen import Doc  # noqa: E402

RAIZ = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
ENTREGA = os.path.join(RAIZ, "entrega")
PLANTILLA = os.path.join(ENTREGA, "Documentacion_ClubDeportivo_v1.2.docx")
FUENTE = os.path.join(ENTREGA, "Guion_coloquio_v1.5.md")
NOMBRE = "Guion_coloquio_v1.5"
SALIDA = os.path.join(ENTREGA, NOMBRE + ".docx")
SOPLA = r"C:\Program Files\LibreOffice\program\soffice.exe"
ANCHOS = {2: [3300, 5771], 3: [1700, 4371, 3000]}  # suman TOC_TAB (9071)

# ------------------------------------------------------------------ texto en línea


def partes_de(texto):
    """Convierte **negrita** y `código` en una lista de (texto, negrita) para Doc.parrafo / Doc.vineta."""
    texto = texto.replace("`", "")
    partes = []
    for i, trozo in enumerate(re.split(r"\*\*", texto)):
        if trozo:
            partes.append((trozo, i % 2 == 1))
    return partes or [("", False)]


def plano(texto):
    return re.sub(r"\*\*|`", "", texto)


# ------------------------------------------------------------------ bloques del Markdown


def bloques(lineas):
    """Agrupa las líneas del Markdown en bloques: ('h2', texto), ('p', texto), ('li', texto), ('ol', n, texto), ('tabla', filas)."""
    i = 0
    while i < len(lineas):
        l = lineas[i]
        if not l.strip() or l.strip() == "---":
            i += 1
            continue
        if l.startswith("## "):
            yield ("h2", l[3:].strip())
            i += 1
            continue
        if l.startswith("|"):
            filas = []
            while i < len(lineas) and lineas[i].startswith("|"):
                celdas = [c.strip() for c in lineas[i].strip().strip("|").split("|")]
                if not all(re.fullmatch(r"-+", c) for c in celdas):
                    filas.append(celdas)
                i += 1
            yield ("tabla", filas)
            continue
        m = re.match(r"^(\d+)\. (.*)", l)
        if m or l.startswith("- "):
            texto = m.group(2) if m else l[2:]
            i += 1
            while i < len(lineas) and lineas[i].startswith("  ") and lineas[i].strip():
                texto += " " + lineas[i].strip()
                i += 1
            yield (("ol", int(m.group(1)), texto) if m else ("li", texto))
            continue
        texto = l.strip()
        i += 1
        while i < len(lineas) and lineas[i].strip() and not re.match(r"^(## |\||- |\d+\. |---)", lineas[i]):
            texto += " " + lineas[i].strip()
            i += 1
        yield ("p", texto)


# ------------------------------------------------------------------ documento

doc = Doc(PLANTILLA, "Club Deportivo — App Android v1.5.1 — Guion del coloquio de entrega")

doc.vacio(5)
doc.centro("Desarrollo de Aplicaciones Móviles", 36, b=True, color="1F3A5F", after=120)
doc.centro("Desarrollo Mobile", 28, b=False, after=120)
doc.centro("Comisión 2° D", 26, b=False, after=120)
doc.centro("Grupo 3", 28, b=True, after=360)
for n in ("Ana Alvarez", "Jorge Aquino Lesme", "Leandro Mussi", "Andrés Weitzel"):
    doc.centro(n, 24, b=False)
doc.vacio(4)
doc.centro("Club Deportivo — Aplicación Android", 26, b=True, after=80)
doc.centro("Etapa Entrega", 32, b=True, color="1F3A5F")
doc.centro("Guion del coloquio de entrega", 32, b=True, color="1F3A5F", after=240)
doc.centro("Versión de la app 1.5.1 — 2026-11-10 — 10 minutos, cuatro integrantes", 22, b=False)
doc.centro("Apellidos: Alvarez · Aquino Lesme · Mussi · Weitzel", 22, b=False)
doc.salto()

with open(FUENTE, encoding="utf8") as f:
    lineas = f.read().splitlines()
lineas = [l for l in lineas[1:] if not l.startswith("Grupo 3 ·")]  # título y grupo ya están en la portada

for b in bloques(lineas):
    if b[0] == "h2":
        doc.h1(b[1])
    elif b[0] == "tabla":
        filas = [[plano(c) for c in f] for f in b[1]]
        doc.tabla(filas[0], filas[1:], ANCHOS[len(filas[0])], sz=20)
    elif b[0] == "li":
        doc.vineta("", partes=partes_de(b[1]))
    elif b[0] == "ol":
        doc.parrafo("", partes=[(f"{b[1]}. ", True)] + partes_de(b[2]), after=80)
    else:
        texto = b[1].replace(" (💬)", " (el texto entre comillas)").replace(" (👉)", " (las líneas «Mostrar:»)")
        if texto.startswith("💬"):
            doc.parrafo("", partes=partes_de(texto[1:].strip()))
        elif texto.startswith("👉"):
            doc.parrafo("", partes=[("Mostrar: ", True)] + partes_de(texto[1:].strip()), jc="left")
        else:
            doc.parrafo("", partes=partes_de(texto))

doc.guardar(SALIDA)


def pdf():
    perfil = "file:///" + os.path.join(os.environ.get("TEMP", RAIZ), "lo_guion").replace("\\", "/")
    subprocess.run([SOPLA, f"-env:UserInstallation={perfil}", "--headless", "--convert-to", "pdf", "--outdir", ENTREGA, SALIDA],
                   check=True, capture_output=True)
    return os.path.join(ENTREGA, NOMBRE + ".pdf")


if __name__ == "__main__":
    import pymupdf
    ruta = pdf()
    d = pymupdf.open(ruta)
    print(os.path.relpath(ruta, RAIZ), "paginas:", len(d))
