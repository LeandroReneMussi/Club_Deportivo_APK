"""
Mini biblioteca de dibujo UML 2.0 sobre Pillow.

Todo se dibuja a escala S (2x) para que el PNG quede nítido; las coordenadas que
reciben las funciones son "lógicas" (1x). Fuentes: Segoe UI de Windows.
"""
from __future__ import annotations

import math
from PIL import Image, ImageDraw, ImageFont

S = 2
FONT_REG = r"C:\Windows\Fonts\segoeui.ttf"
FONT_BOLD = r"C:\Windows\Fonts\segoeuib.ttf"
FONT_ITAL = r"C:\Windows\Fonts\segoeuii.ttf"

NEGRO = (20, 20, 20)
GRIS = (110, 110, 110)
BLANCO = (255, 255, 255)
# Colores suaves por tipo de elemento
C_ACTOR = (255, 255, 255)
C_CASO = (233, 242, 255)      # celeste
C_CLASE = (255, 248, 225)     # crema
C_ENUM = (240, 240, 240)
C_LIFELINE = (232, 245, 233)  # verde suave
C_ESTADO = (255, 243, 224)    # naranja suave
C_ACCION = (236, 239, 255)    # lavanda
C_NOTA = (255, 253, 208)      # amarillo nota
C_FRAG = (250, 250, 250)
C_V15 = (255, 224, 178)       # resalte v1.5

_fuentes: dict = {}


def fuente(size: int = 13, bold: bool = False, italic: bool = False) -> ImageFont.FreeTypeFont:
    key = (size, bold, italic)
    if key not in _fuentes:
        path = FONT_BOLD if bold else (FONT_ITAL if italic else FONT_REG)
        _fuentes[key] = ImageFont.truetype(path, int(size * S))
    return _fuentes[key]


class Lienzo:
    def __init__(self, w: int, h: int, fondo=BLANCO):
        self.w, self.h = w, h
        self.im = Image.new("RGB", (w * S, h * S), fondo)
        self.d = ImageDraw.Draw(self.im)

    # ------------------------------------------------------------ texto
    def tsize(self, s: str, f) -> tuple[float, float]:
        l, t, r, b = self.d.textbbox((0, 0), s, font=f)
        return (r - l) / S, (b - t) / S

    def wrap(self, s: str, f, maxw: float) -> list[str]:
        lineas = []
        for parrafo in s.split("\n"):
            palabras = parrafo.split(" ")
            actual = ""
            for p in palabras:
                prueba = (actual + " " + p).strip()
                if self.tsize(prueba, f)[0] <= maxw or not actual:
                    actual = prueba
                else:
                    lineas.append(actual)
                    actual = p
            lineas.append(actual)
        return lineas

    def texto(self, x, y, s, size=13, bold=False, italic=False, color=NEGRO, anchor="la"):
        """anchor: PIL anchor ('la' = left-ascender, 'mm' = middle-middle, 'ma' = middle-top)."""
        f = fuente(size, bold, italic)
        self.d.text((x * S, y * S), s, font=f, fill=color, anchor=anchor)

    def lineas_texto(self, x, y, lineas, size=13, bold=False, italic=False, color=NEGRO, centrado=False, interlinea=1.3):
        f = fuente(size, bold, italic)
        lh = size * interlinea
        for i, s in enumerate(lineas):
            if centrado:
                self.d.text((x * S, (y + i * lh) * S), s, font=f, fill=color, anchor="ma")
            else:
                self.d.text((x * S, (y + i * lh) * S), s, font=f, fill=color, anchor="la")
        return len(lineas) * lh

    def alto_lineas(self, n, size=13, interlinea=1.3):
        return n * size * interlinea

    # ------------------------------------------------------------ primitivas
    def linea(self, p1, p2, color=NEGRO, ancho=1.5, punteada=False):
        (x1, y1), (x2, y2) = p1, p2
        if not punteada:
            self.d.line([(x1 * S, y1 * S), (x2 * S, y2 * S)], fill=color, width=int(ancho * S))
            return
        dx, dy = x2 - x1, y2 - y1
        largo = math.hypot(dx, dy)
        if largo == 0:
            return
        ux, uy = dx / largo, dy / largo
        pos, on = 0.0, True
        while pos < largo:
            seg = min(6 if on else 4, largo - pos)
            if on:
                a = (x1 + ux * pos, y1 + uy * pos)
                b = (x1 + ux * (pos + seg), y1 + uy * (pos + seg))
                self.d.line([(a[0] * S, a[1] * S), (b[0] * S, b[1] * S)], fill=color, width=int(ancho * S))
            pos += seg
            on = not on

    def polilinea(self, puntos, color=NEGRO, ancho=1.5, punteada=False, flecha="none", flecha_inicio="none"):
        for a, b in zip(puntos, puntos[1:]):
            self.linea(a, b, color, ancho, punteada)
        if flecha != "none":
            self.punta(puntos[-2], puntos[-1], flecha, color)
        if flecha_inicio != "none":
            self.punta(puntos[1], puntos[0], flecha_inicio, color)

    def punta(self, desde, hasta, tipo="filled", color=NEGRO, largo=11, semi=5.5):
        """tipo: filled | open | triangle (generalización, hueco) | diamond (composición) | odiamond (agregación)."""
        (x1, y1), (x2, y2) = desde, hasta
        ang = math.atan2(y2 - y1, x2 - x1)
        if tipo in ("diamond", "odiamond"):
            largo = 14
            semi = 6
        bx, by = x2 - largo * math.cos(ang), y2 - largo * math.sin(ang)
        px, py = -math.sin(ang) * semi, math.cos(ang) * semi
        a = (bx + px, by + py)
        b = (bx - px, by - py)
        if tipo == "open":
            self.linea(a, (x2, y2), color)
            self.linea(b, (x2, y2), color)
        elif tipo == "filled":
            self.d.polygon([(x2 * S, y2 * S), (a[0] * S, a[1] * S), (b[0] * S, b[1] * S)], fill=color)
        elif tipo == "triangle":
            self.d.polygon([(x2 * S, y2 * S), (a[0] * S, a[1] * S), (b[0] * S, b[1] * S)], fill=BLANCO, outline=color, width=int(1.5 * S))
        elif tipo in ("diamond", "odiamond"):
            cx, cy = x2 - 2 * largo * math.cos(ang), y2 - 2 * largo * math.sin(ang)
            pts = [(x2, y2), a, (cx, cy), b]
            self.d.polygon([(p[0] * S, p[1] * S) for p in pts], fill=(color if tipo == "diamond" else BLANCO), outline=color, width=int(1.5 * S))

    def rect(self, x, y, w, h, relleno=BLANCO, borde=NEGRO, ancho=1.5, radio=0, punteado=False):
        if punteado:
            self.polilinea([(x, y), (x + w, y), (x + w, y + h), (x, y + h), (x, y)], borde, ancho, True)
            return
        if radio:
            self.d.rounded_rectangle([x * S, y * S, (x + w) * S, (y + h) * S], radius=radio * S, fill=relleno, outline=borde, width=int(ancho * S))
        else:
            self.d.rectangle([x * S, y * S, (x + w) * S, (y + h) * S], fill=relleno, outline=borde, width=int(ancho * S))

    def elipse(self, cx, cy, rx, ry, relleno=BLANCO, borde=NEGRO, ancho=1.5):
        self.d.ellipse([(cx - rx) * S, (cy - ry) * S, (cx + rx) * S, (cy + ry) * S], fill=relleno, outline=borde, width=int(ancho * S))

    def titulo(self, s, sub=None):
        self.texto(self.w / 2, 14, s, 18, bold=True, anchor="ma")
        if sub:
            self.texto(self.w / 2, 40, sub, 12, color=GRIS, anchor="ma")

    def guardar(self, ruta):
        self.im.save(ruta, optimize=True)

    # ------------------------------------------------------------ casos de uso
    def actor(self, cx, y, nombre, size=12):
        """Monigote con la cabeza en (cx, y). Devuelve (cx, y_pies+texto)."""
        r = 9
        self.elipse(cx, y + r, r, r, C_ACTOR)
        self.linea((cx, y + 2 * r), (cx, y + 2 * r + 24))
        self.linea((cx - 16, y + 2 * r + 7), (cx + 16, y + 2 * r + 7))
        self.linea((cx, y + 2 * r + 24), (cx - 13, y + 2 * r + 44))
        self.linea((cx, y + 2 * r + 24), (cx + 13, y + 2 * r + 44))
        lineas = nombre.split("\n")
        alto_txt = self.lineas_texto(cx, y + 2 * r + 48, lineas, size, bold=True, centrado=True)
        torso = y + 2 * r + 12
        # Puntos de conexión: por debajo del nombre, o a los costados del torso.
        return {"pies": (cx, y + 2 * r + 48 + alto_txt + 2), "izq": (cx - 20, torso), "der": (cx + 20, torso)}

    def caso(self, cx, cy, etiqueta, size=12, minw=130, maxw=190, relleno=C_CASO):
        f = fuente(size)
        lineas = self.wrap(etiqueta, f, maxw)
        tw = max(self.tsize(s, f)[0] for s in lineas)
        th = self.alto_lineas(len(lineas), size)
        rx = max(minw, tw + 34) / 2
        ry = max(26, th / 2 + 12)
        self.elipse(cx, cy, rx, ry, relleno)
        self.lineas_texto(cx, cy - th / 2 + 1, lineas, size, centrado=True)
        return (cx, cy, rx, ry)

    def borde_elipse(self, e, hacia):
        """Punto del borde de la elipse e=(cx,cy,rx,ry) en dirección a `hacia`."""
        cx, cy, rx, ry = e
        ang = math.atan2(hacia[1] - cy, hacia[0] - cx)
        return (cx + rx * math.cos(ang), cy + ry * math.sin(ang))

    def asociacion(self, p, e, color=NEGRO):
        """Línea del actor (punto p) al borde de la elipse e."""
        self.linea(p, self.borde_elipse(e, p), color)

    def dependencia(self, e1, e2, estereotipo, color=NEGRO, size=11):
        a = self.borde_elipse(e1, (e2[0], e2[1]))
        b = self.borde_elipse(e2, (e1[0], e1[1]))
        self.linea(a, b, color, punteada=True)
        self.punta(a, b, "open", color)
        mx, my = (a[0] + b[0]) / 2, (a[1] + b[1]) / 2
        f = fuente(size, italic=True)
        tw, th = self.tsize(estereotipo, f)
        self.rect(mx - tw / 2 - 3, my - th / 2 - 2, tw + 6, th + 4, BLANCO, BLANCO)
        self.texto(mx, my, estereotipo, size, italic=True, anchor="mm")

    # ------------------------------------------------------------ clases
    def clase(self, x, y, nombre, atributos=(), operaciones=(), estereotipo=None, relleno=C_CLASE, w=None, size=11, w_min=150):
        f = fuente(size)
        fb = fuente(size, bold=True)
        cab = [nombre]
        if estereotipo:
            cab = [estereotipo, nombre]
        textos = list(atributos) + list(operaciones) + cab
        tw = max(self.tsize(s, fb)[0] for s in textos) + 20
        w = w or max(w_min, tw)
        lh = size * 1.35
        h_cab = lh * len(cab) + 10
        h_at = lh * len(atributos) + 8 if atributos else 8
        h_op = lh * len(operaciones) + 8 if operaciones else 0
        h = h_cab + h_at + h_op
        self.rect(x, y, w, h, relleno)
        yy = y + 5
        if estereotipo:
            self.texto(x + w / 2, yy, estereotipo, size - 1, italic=True, anchor="ma")
            yy += lh
        self.texto(x + w / 2, yy, nombre, size, bold=True, anchor="ma")
        self.linea((x, y + h_cab), (x + w, y + h_cab))
        yy = y + h_cab + 4
        for a in atributos:
            self.texto(x + 8, yy, a, size)
            yy += lh
        if operaciones:
            self.linea((x, y + h_cab + h_at), (x + w, y + h_cab + h_at))
            yy = y + h_cab + h_at + 4
            for o in operaciones:
                self.texto(x + 8, yy, o, size)
                yy += lh
        return (x, y, w, h)

    @staticmethod
    def centro(r):
        return (r[0] + r[2] / 2, r[1] + r[3] / 2)

    @staticmethod
    def borde_rect(r, hacia):
        """Punto del borde del rect r=(x,y,w,h) en dirección al punto `hacia`."""
        cx, cy = r[0] + r[2] / 2, r[1] + r[3] / 2
        dx, dy = hacia[0] - cx, hacia[1] - cy
        if dx == 0 and dy == 0:
            return (cx, cy)
        hw, hh = r[2] / 2, r[3] / 2
        sx = hw / abs(dx) if dx else float("inf")
        sy = hh / abs(dy) if dy else float("inf")
        t = min(sx, sy)
        return (cx + dx * t, cy + dy * t)

    def relacion(self, r1, r2, tipo="asoc", m1=None, m2=None, etiqueta=None, puntos=None, punteada=False, size=10, desplaz_m=(0, 0), p1=None, p2=None):
        """
        Relación entre dos rects. tipo: asoc | comp | agreg | gen | dep | nav.
        `puntos`: waypoints intermedios opcionales. m1/m2: multiplicidades en cada extremo.
        """
        via = list(puntos or [])
        p1 = p1 or self.borde_rect(r1, via[0] if via else self.centro(r2))
        p2 = p2 or self.borde_rect(r2, via[-1] if via else self.centro(r1))
        pts = [p1] + via + [p2]
        if tipo == "dep":
            punteada = True
        self.polilinea(pts, NEGRO, 1.5, punteada)
        if tipo == "comp":
            self.punta(pts[1], pts[0], "diamond")
        elif tipo == "agreg":
            self.punta(pts[1], pts[0], "odiamond")
        elif tipo == "gen":
            self.punta(pts[-2], pts[-1], "triangle")
        elif tipo in ("dep", "nav"):
            self.punta(pts[-2], pts[-1], "open")
        # multiplicidades: un poco al costado del extremo, hacia afuera de la línea
        if m1:
            self._mult(pts[0], pts[1], m1, size, desplaz_m)
        if m2:
            self._mult(pts[-1], pts[-2], m2, size, desplaz_m)
        if etiqueta:
            mid = len(pts) // 2
            if len(pts) % 2 == 0:
                a, b = pts[mid - 1], pts[mid]
                mx, my = (a[0] + b[0]) / 2, (a[1] + b[1]) / 2
            else:
                mx, my = pts[mid]
            f = fuente(size, italic=True)
            tw, th = self.tsize(etiqueta, f)
            self.rect(mx - tw / 2 - 3, my - th / 2 - 2, tw + 6, th + 4, BLANCO, BLANCO)
            self.texto(mx, my, etiqueta, size, italic=True, anchor="mm")

    def _mult(self, extremo, hacia, s, size, desplaz=(0, 0)):
        ex, ey = extremo
        dx, dy = hacia[0] - ex, hacia[1] - ey
        largo = math.hypot(dx, dy) or 1
        ux, uy = dx / largo, dy / largo
        # 14 px adentro de la línea y 10 px perpendicular (lado derecho mirando hacia adentro)
        px, py = ex + ux * 16 - uy * 11 + desplaz[0], ey + uy * 16 + ux * 11 + desplaz[1]
        self.texto(px, py, s, size, color=NEGRO, anchor="mm")

    def nota(self, x, y, w, texto, size=11, relleno=C_NOTA):
        f = fuente(size)
        lineas = self.wrap(texto, f, w - 16)
        h = self.alto_lineas(len(lineas), size) + 14
        d = 10
        pts = [(x, y), (x + w - d, y), (x + w, y + d), (x + w, y + h), (x, y + h)]
        self.d.polygon([(p[0] * S, p[1] * S) for p in pts], fill=relleno, outline=NEGRO, width=int(1.2 * S))
        self.polilinea([(x + w - d, y), (x + w - d, y + d), (x + w, y + d)], NEGRO, 1.2)
        self.lineas_texto(x + 8, y + 7, lineas, size)
        return (x, y, w, h)

    def ancla_nota(self, r_nota, hacia):
        a = self.borde_rect(r_nota, hacia)
        self.linea(a, hacia, GRIS, 1.2, punteada=True)

    # ------------------------------------------------------------ secuencia
    def participante(self, cx, y, nombre, alto, size=11, w=None, relleno=C_LIFELINE):
        f = fuente(size, bold=True)
        lineas = nombre.split("\n")
        tw = max(self.tsize(s, f)[0] for s in lineas) + 20
        w = w or max(96, tw)
        h = self.alto_lineas(len(lineas), size) + 12
        self.rect(cx - w / 2, y, w, h, relleno)
        self.lineas_texto(cx, y + 6, lineas, size, bold=True, centrado=True)
        self.linea((cx, y + h), (cx, y + alto), GRIS, 1.2, punteada=True)
        return cx

    def activacion(self, cx, y1, y2, relleno=(245, 245, 245)):
        self.rect(cx - 5, y1, 10, y2 - y1, relleno)

    def mensaje(self, x1, x2, y, etiqueta, retorno=False, size=11, asinc=False, arriba=True):
        self.linea((x1, y), (x2, y), NEGRO, 1.5, punteada=retorno)
        self.punta((x1, y), (x2, y), "open" if (retorno or asinc) else "filled")
        mx = (x1 + x2) / 2
        f = fuente(size, italic=retorno)
        lineas = self.wrap(etiqueta, f, abs(x2 - x1) - 16) if abs(x2 - x1) > 60 else [etiqueta]
        th = self.alto_lineas(len(lineas), size)
        self.lineas_texto(mx, y - th - 3 if arriba else y + 4, lineas, size, italic=retorno, centrado=True)
        return y

    def automensaje(self, x, y, etiqueta, size=11, ancho_lazo=28):
        self.polilinea([(x, y), (x + ancho_lazo, y), (x + ancho_lazo, y + 18), (x + 6, y + 18)], NEGRO, 1.5, flecha="filled")
        f = fuente(size)
        lineas = self.wrap(etiqueta, f, 240)
        self.lineas_texto(x + ancho_lazo + 8, y - 3, lineas, size)
        return y + 18 + max(0, (len(lineas) - 1) * size * 1.3)

    def fragmento(self, x, y, w, h, operador, guardas, separadores=(), size=11):
        """Marco combinado. guardas: lista de textos (uno por operando), separadores: y de cada línea divisoria."""
        self.rect(x, y, w, h, None, NEGRO, 1.3)
        f = fuente(size, bold=True)
        tw, th = self.tsize(operador, f)
        pw, ph = tw + 16, th + 6
        pts = [(x, y), (x + pw, y), (x + pw, y + ph - 5), (x + pw - 6, y + ph), (x, y + ph)]
        self.d.polygon([(p[0] * S, p[1] * S) for p in pts], fill=(235, 235, 235), outline=NEGRO, width=int(1.3 * S))
        self.texto(x + 8, y + 3, operador, size, bold=True)
        fi = fuente(size, italic=True)
        if guardas:
            tw2, th2 = self.tsize(guardas[0], fi)
            self.rect(x + pw + 6, y + 2, tw2 + 4, th2 + 2, BLANCO, BLANCO)
            self.texto(x + pw + 8, y + 3, guardas[0], size, italic=True)
        for i, sy in enumerate(separadores):
            self.linea((x, sy), (x + w, sy), NEGRO, 1.2, punteada=True)
            if i + 1 < len(guardas):
                tw2, th2 = self.tsize(guardas[i + 1], fi)
                self.rect(x + 6, sy + 2, tw2 + 4, th2 + 2, BLANCO, BLANCO)
                self.texto(x + 8, sy + 3, guardas[i + 1], size, italic=True)

    # ------------------------------------------------------------ estados / actividad
    def estado(self, cx, cy, nombre, detalle=(), size=12, relleno=C_ESTADO, w=None, h=None):
        f = fuente(size, bold=True)
        fd = fuente(size - 1)
        tw = max([self.tsize(nombre, f)[0]] + [self.tsize(s, fd)[0] for s in detalle]) + 24
        w = w or max(120, tw)
        h_nom = size * 1.4 + 8
        h = h or (h_nom + (self.alto_lineas(len(detalle), size - 1) + 8 if detalle else 0))
        x, y = cx - w / 2, cy - h / 2
        self.rect(x, y, w, h, relleno, radio=10)
        if detalle:
            self.texto(cx, y + 5, nombre, size, bold=True, anchor="ma")
            self.linea((x, y + h_nom), (x + w, y + h_nom))
            self.lineas_texto(x + 10, y + h_nom + 4, detalle, size - 1)
        else:
            self.texto(cx, cy, nombre, size, bold=True, anchor="mm")
        return (x, y, w, h)

    def inicial(self, cx, cy, r=8):
        self.elipse(cx, cy, r, r, NEGRO)
        return (cx - r, cy - r, 2 * r, 2 * r)

    def final(self, cx, cy, r=10):
        self.elipse(cx, cy, r, r, BLANCO)
        self.elipse(cx, cy, r - 4, r - 4, NEGRO)
        return (cx - r, cy - r, 2 * r, 2 * r)

    def transicion(self, r1, r2, etiqueta=None, puntos=None, size=10, pos_etiqueta=None, ancho_etiqueta=170, desplaz=(0, 0), p1=None, p2=None):
        via = list(puntos or [])
        p1 = p1 or self.borde_rect(r1, via[0] if via else self.centro(r2))
        p2 = p2 or self.borde_rect(r2, via[-1] if via else self.centro(r1))
        pts = [p1] + via + [p2]
        self.polilinea(pts, NEGRO, 1.5, flecha="filled")
        if etiqueta:
            if pos_etiqueta is None:
                mid = len(pts) // 2
                if len(pts) % 2 == 0:
                    a, b = pts[mid - 1], pts[mid]
                    pos_etiqueta = ((a[0] + b[0]) / 2, (a[1] + b[1]) / 2)
                else:
                    pos_etiqueta = pts[mid]
            mx, my = pos_etiqueta[0] + desplaz[0], pos_etiqueta[1] + desplaz[1]
            f = fuente(size)
            lineas = self.wrap(etiqueta, f, ancho_etiqueta)
            tw = max(self.tsize(s, f)[0] for s in lineas)
            th = self.alto_lineas(len(lineas), size)
            self.rect(mx - tw / 2 - 4, my - th / 2 - 2, tw + 8, th + 4, BLANCO, BLANCO)
            self.lineas_texto(mx, my - th / 2 + 1, lineas, size, centrado=True)
        return pts

    def accion(self, cx, cy, texto, size=11, relleno=C_ACCION, w=180):
        f = fuente(size)
        lineas = self.wrap(texto, f, w - 20)
        h = self.alto_lineas(len(lineas), size) + 14
        x, y = cx - w / 2, cy - h / 2
        self.rect(x, y, w, h, relleno, radio=9)
        self.lineas_texto(cx, y + 7, lineas, size, centrado=True)
        return (x, y, w, h)

    def decision(self, cx, cy, texto=None, size=10, lado=22, ancho_texto=150):
        pts = [(cx, cy - lado), (cx + lado, cy), (cx, cy + lado), (cx - lado, cy)]
        self.d.polygon([(p[0] * S, p[1] * S) for p in pts], fill=(255, 255, 255), outline=NEGRO, width=int(1.5 * S))
        if texto:
            f = fuente(size)
            lineas = self.wrap(texto, f, ancho_texto)
            # el texto va a la derecha del rombo, alineado al centro vertical
            th = self.alto_lineas(len(lineas), size)
            self.lineas_texto(cx + lado + 6, cy - th / 2 + 1, lineas, size)
        return (cx - lado, cy - lado, 2 * lado, 2 * lado)

    def flecha(self, puntos, etiqueta=None, size=10, pos=None, desplaz=(0, 0), punteada=False):
        self.polilinea(puntos, NEGRO, 1.5, punteada, flecha="filled")
        if etiqueta:
            if pos is None:
                a, b = puntos[0], puntos[1]
                pos = ((a[0] + b[0]) / 2, (a[1] + b[1]) / 2)
            mx, my = pos[0] + desplaz[0], pos[1] + desplaz[1]
            f = fuente(size)
            tw, th = self.tsize(etiqueta, f)
            self.rect(mx - tw / 2 - 3, my - th / 2 - 2, tw + 6, th + 4, BLANCO, BLANCO)
            self.texto(mx, my, etiqueta, size, anchor="mm")

    def barra(self, cx, cy, w, h=6):
        self.rect(cx - w / 2, cy - h / 2, w, h, NEGRO)
        return (cx - w / 2, cy - h / 2, w, h)

    def carril(self, x, y, w, h, titulo, size=12):
        self.rect(x, y, w, h, None, GRIS, 1.2)
        self.rect(x, y, w, 26, (245, 245, 245), GRIS, 1.2)
        self.texto(x + w / 2, y + 13, titulo, size, bold=True, anchor="mm")


def abajo(r):
    return (r[0] + r[2] / 2, r[1] + r[3])


def arriba(r):
    return (r[0] + r[2] / 2, r[1])


def izquierda(r):
    return (r[0], r[1] + r[3] / 2)


def derecha(r):
    return (r[0] + r[2], r[1] + r[3] / 2)
