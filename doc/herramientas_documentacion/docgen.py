# -*- coding: utf-8 -*-
"""Utilidades para generar la documentacion general (docx) con el estilo de la v1.2.

Toma como plantilla entrega/Documentacion_ClubDeportivo_v1.2.docx (estilos, encabezado,
pie de pagina, margenes) y arma el cuerpo con XML equivalente al de la v1.2.
"""
import copy
from xml.sax.saxutils import escape

from docx import Document
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls, qn
from docx.shared import Emu

AZUL = "1F3A5F"
TOC_TAB = 9071


def esc(t):
    return escape(str(t))


class Doc:
    def __init__(self, plantilla, encabezado):
        self.d = Document(plantilla)
        self.body = self.d.element.body
        self.sect = self.body.find(qn("w:sectPr"))
        for el in list(self.body):
            if el is not self.sect:
                self.body.remove(el)
        # La plantilla trae las imagenes de la v1.2 como relaciones: se sueltan para no arrastrarlas al paquete.
        for rid, rel in list(self.d.part.rels.items()):
            if rel.reltype.endswith("/image"):
                self.d.part.rels.pop(rid)
        self.headings = []  # (nivel, texto, bookmark)
        self._anchor = None
        self._marca = None
        self._bm = 100
        self.d.sections[0].header.paragraphs[0].runs[0].text = encabezado

    # ---------------------------------------------------------------- bajo nivel
    def _add(self, xml):
        el = parse_xml(xml)
        (self._anchor if self._anchor is not None else self.sect).addprevious(el)
        return el

    def _p(self, inner, ppr=""):
        return self._add(f'<w:p {nsdecls("w", "r")}>{("<w:pPr>" + ppr + "</w:pPr>") if ppr else ""}{inner}</w:p>')

    @staticmethod
    def _run(text, b=None, i=False, color=None, sz=None):
        rpr = ""
        if b is True:
            rpr += "<w:b/>"
        elif b is False:
            rpr += '<w:b w:val="0"/>'
        if i:
            rpr += "<w:i/>"
        if color:
            rpr += f'<w:color w:val="{color}"/>'
        if sz:
            rpr += f'<w:sz w:val="{sz}"/>'
        return f'<w:r>{("<w:rPr>" + rpr + "</w:rPr>") if rpr else ""}<w:t xml:space="preserve">{esc(text)}</w:t></w:r>'

    # ---------------------------------------------------------------- bloques
    def salto(self):
        self._add(f'<w:p {nsdecls("w")}><w:r><w:br w:type="page"/></w:r></w:p>')

    def vacio(self, n=1):
        for _ in range(n):
            self._add(f'<w:p {nsdecls("w")}/>')

    def centro(self, text, sz, b=None, color=None, after=40):
        self._p(self._run(text, b=b, color=color, sz=sz), f'<w:spacing w:after="{after}"/><w:jc w:val="center"/>')

    def parrafo(self, text, jc="both", after=None, partes=None):
        """partes: lista de (texto, negrita) para mezclar negritas."""
        ppr = (f'<w:spacing w:after="{after}"/>' if after is not None else "") + (f'<w:jc w:val="{jc}"/>' if jc else "")
        if partes:
            inner = "".join(self._run(t, b=True if neg else None) for t, neg in partes)
        else:
            inner = self._run(text)
        self._p(inner, ppr)

    def vineta(self, text, partes=None):
        if partes:
            inner = "".join(self._run(t, b=True if neg else None) for t, neg in partes)
        else:
            inner = self._run(text)
        self._p(inner, '<w:pStyle w:val="ListBullet"/>')

    def _bookmark(self, nivel, text):
        self._bm += 1
        nombre = f"_Indice_{self._bm}"
        self.headings.append((nivel, text, nombre))
        return self._bm, nombre

    def h1(self, text):
        i, n = self._bookmark(1, text)
        self._p(f'<w:bookmarkStart w:id="{i}" w:name="{n}"/><w:r><w:t xml:space="preserve">{esc(text)}</w:t></w:r><w:bookmarkEnd w:id="{i}"/>',
                '<w:pStyle w:val="Heading1"/>')

    def h2(self, text):
        i, n = self._bookmark(2, text)
        self._p(f'<w:bookmarkStart w:id="{i}" w:name="{n}"/><w:r><w:t xml:space="preserve">{esc(text)}</w:t></w:r><w:bookmarkEnd w:id="{i}"/>',
                '<w:pStyle w:val="Heading2"/>')

    def parte(self, numero, titulo):
        self.vacio(8)
        i, n = self._bookmark(0, f"Parte {numero} — {titulo}")
        self._p(f'<w:bookmarkStart w:id="{i}" w:name="{n}"/>{self._run("Parte " + numero, b=True, color=AZUL, sz=44)}<w:bookmarkEnd w:id="{i}"/>',
                '<w:spacing w:after="120"/><w:jc w:val="center"/>')
        self.centro(titulo, 24, b=False)
        self.salto()

    def tabla(self, cabecera, filas, anchos, sz=19):
        total = sum(anchos)
        assert abs(total - TOC_TAB) < 40, (cabecera, total)
        grid = "".join(f'<w:gridCol w:w="{a}"/>' for a in anchos)

        def celda(texto, ancho, enc=False):
            shd = f'<w:shd w:val="clear" w:color="auto" w:fill="{AZUL}"/>' if enc else ""
            rpr = f'<w:b/><w:color w:val="FFFFFF"/><w:sz w:val="{sz}"/>' if enc else f'<w:sz w:val="{sz}"/>'
            parrafos = ""
            for linea in str(texto).split("\n"):
                parrafos += f'<w:p><w:r><w:rPr>{rpr}</w:rPr><w:t xml:space="preserve">{esc(linea)}</w:t></w:r></w:p>'
            return f'<w:tc><w:tcPr><w:tcW w:type="dxa" w:w="{ancho}"/>{shd}</w:tcPr>{parrafos}</w:tc>'

        trs = '<w:tr><w:trPr><w:cantSplit/><w:tblHeader/></w:trPr>' + "".join(celda(c, a, True) for c, a in zip(cabecera, anchos)) + "</w:tr>"
        for f in filas:
            assert len(f) == len(anchos), f
            trs += '<w:tr><w:trPr><w:cantSplit/></w:trPr>' + "".join(celda(c, a) for c, a in zip(f, anchos)) + "</w:tr>"
        xml = (f'<w:tbl {nsdecls("w")}><w:tblPr><w:tblStyle w:val="TableGrid"/><w:tblW w:type="auto" w:w="0"/><w:jc w:val="center"/>'
               f'<w:tblLayout w:type="fixed"/><w:tblLook w:firstColumn="1" w:firstRow="1" w:lastColumn="0" w:lastRow="0" w:noHBand="0" w:noVBand="1" w:val="04A0"/></w:tblPr>'
               f'<w:tblGrid>{grid}</w:tblGrid>{trs}</w:tbl>')
        self._add(xml)
        self._p("")  # separacion

    def boton(self, texto, url=None):
        rid = self.d.part.relate_to(url, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink", is_external=True) if url else None
        run = (f'<w:r><w:rPr><w:color w:val="131809"/><w:sz w:val="32"/><w:b/><w:u w:val="none"/></w:rPr><w:t xml:space="preserve">{esc(texto)}</w:t></w:r>')
        inner = f'<w:hyperlink r:id="{rid}">{run}</w:hyperlink>' if rid else run
        self._add(
            f'<w:tbl {nsdecls("w", "r")}><w:tblPr><w:tblW w:type="auto" w:w="0"/><w:jc w:val="center"/><w:tblLayout w:type="fixed"/></w:tblPr>'
            f'<w:tblGrid><w:gridCol w:w="6600"/></w:tblGrid><w:tr><w:trPr><w:trHeight w:val="907"/></w:trPr><w:tc><w:tcPr><w:tcW w:type="dxa" w:w="6600"/>'
            f'<w:shd w:val="clear" w:color="auto" w:fill="B3E635"/></w:tcPr><w:p><w:pPr><w:spacing w:before="200" w:after="200"/><w:jc w:val="center"/></w:pPr>{inner}</w:p></w:tc></w:tr></w:tbl>')
        self._p("")

    def imagen(self, ruta, ancho_emu, alto_emu):
        """Imagen centrada en un parrafo propio."""
        p = self.d.add_paragraph()
        self.body.remove(p._p)
        self.sect.addprevious(p._p)
        p.alignment = 1
        p.add_run().add_picture(ruta, width=Emu(ancho_emu), height=Emu(alto_emu))

    def capturas(self, entradas, carpeta):
        """entradas: lista de (archivo, pantalla, hu, epigrafe). Tres por fila, como en la v1.2."""
        tbl = self.d.add_table(rows=0, cols=3)
        self.body.remove(tbl._tbl)
        self.sect.addprevious(tbl._tbl)
        tblPr = tbl._tbl.tblPr
        for hijo in list(tblPr):
            tblPr.remove(hijo)
        tblPr.append(parse_xml(f'<w:tblW {nsdecls("w")} w:type="auto" w:w="0"/>'))
        tblPr.append(parse_xml(f'<w:jc {nsdecls("w")} w:val="center"/>'))
        for fila in range(0, len(entradas), 3):
            row = tbl.add_row()
            row._tr.get_or_add_trPr().append(parse_xml(f'<w:cantSplit {nsdecls("w")}/>'))
            for k in range(3):
                celda = row.cells[k]
                celda.width = Emu(3005 * 635)
                if fila + k >= len(entradas):
                    continue
                archivo, pantalla, hu, epi = entradas[fila + k]
                p0 = celda.paragraphs[0]
                p0.alignment = 1
                p0.add_run().add_picture(f"{carpeta}/{archivo}", width=Emu(1655999), height=Emu(3679998))
                celda._tc.append(parse_xml(f'<w:p {nsdecls("w")}><w:pPr><w:jc w:val="center"/></w:pPr>{self._run(pantalla, b=True, sz=18)}</w:p>'))
                celda._tc.append(parse_xml(f'<w:p {nsdecls("w")}><w:pPr><w:spacing w:after="200"/><w:jc w:val="center"/></w:pPr>{self._run(hu + " — " + epi, sz=16)}</w:p>'))
        self._p("")

    # ---------------------------------------------------------------- indice
    def marcar_indice(self):
        self._marca = self._add(f'<w:p {nsdecls("w")}/>')

    def indice(self, paginas=None):
        """Escribe el indice (hipervinculos a los marcadores) donde quedo la marca."""
        paginas = paginas or {}
        self._anchor = self._marca
        for nivel, texto, nombre in list(self.headings):
            num = paginas.get(texto, "")
            if nivel == 0:
                ppr = (f'<w:tabs><w:tab w:pos="{TOC_TAB}" w:val="right" w:leader="dot"/></w:tabs><w:spacing w:after="120" w:before="160"/><w:ind w:left="0"/>')
                rpr = '<w:b/><w:color w:val="1F3A5F"/><w:sz w:val="23"/>'
            else:
                izq = 227 if nivel == 1 else 454
                ppr = (f'<w:tabs><w:tab w:pos="{TOC_TAB}" w:val="right" w:leader="dot"/></w:tabs><w:spacing w:after="40"/><w:ind w:left="{izq}"/>')
                rpr = '<w:sz w:val="21"/>'
            self._p(
                f'<w:hyperlink w:anchor="{nombre}"><w:r><w:rPr>{rpr}</w:rPr><w:t xml:space="preserve">{esc(texto)}</w:t></w:r>'
                f'<w:r><w:rPr>{rpr}</w:rPr><w:tab/></w:r><w:r><w:rPr>{rpr}</w:rPr><w:t xml:space="preserve">{num}</w:t></w:r></w:hyperlink>',
                ppr)
        self._anchor = None

    def guardar(self, ruta):
        self.d.save(ruta)
