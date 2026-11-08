"""Generador de diagramas de secuencia UML 2.0 a partir de una lista de pasos."""
import uml
from uml import Lienzo, fuente

LH = 14  # alto de línea de etiqueta (size 11 * 1.3)


class Secuencia:
    """
    participantes: lista de (clave, etiqueta, tipo) con tipo 'actor' | 'obj'.
    pasos: lista de tuplas:
      ('msg', de, a, texto)         mensaje síncrono
      ('ret', de, a, texto)         retorno (punteado)
      ('self', quien, texto)        automensaje
      ('alt', [(guarda, [pasos]), ...])
      ('opt', guarda, [pasos])
      ('loop', guarda, [pasos])
      ('note', quien, texto)        nota a la derecha de la línea de vida
      ('sep', texto)                separador de etapa
    """

    def __init__(self, titulo, sub, participantes, pasos, ancho=1400, margen_x=90):
        self.titulo, self.sub = titulo, sub
        self.participantes = participantes
        self.pasos = pasos
        self.ancho = ancho
        n = len(participantes)
        paso = (ancho - margen_x - 250) / (n - 1)
        self.x = {k: margen_x + i * paso for i, (k, _, _) in enumerate(participantes)}
        self.orden = {k: i for i, (k, _, _) in enumerate(participantes)}
        self.paso = paso

    # ------------------------------------------------------------ medición / dibujo
    def _involucrados(self, pasos):
        ks = set()
        for p in pasos:
            t = p[0]
            if t in ("msg", "ret"):
                ks.update([p[1], p[2]])
            elif t in ("self", "note"):
                ks.add(p[1])
            elif t == "alt":
                for _, sub in p[1]:
                    ks |= self._involucrados(sub)
            elif t in ("opt", "loop"):
                ks |= self._involucrados(p[2])
        return ks

    def _lineas(self, L, texto, maxw, italic=False):
        return L.wrap(texto, fuente(11, italic=italic), maxw)

    def _render(self, L, pasos, y, dibujar=True, nivel=0):
        for p in pasos:
            t = p[0]
            if t in ("msg", "ret"):
                _, de, a, texto = p
                x1, x2 = self.x[de], self.x[a]
                lineas = self._lineas(L, texto, abs(x2 - x1) - 20, italic=(t == "ret"))
                y += len(lineas) * LH + 12
                if dibujar:
                    L.linea((x1, y), (x2, y), uml.NEGRO, 1.5, punteada=(t == "ret"))
                    L.punta((x1, y), (x2, y), "open" if t == "ret" else "filled")
                    L.lineas_texto((x1 + x2) / 2, y - len(lineas) * LH - 4, lineas, 11, italic=(t == "ret"), centrado=True)
                y += 6
            elif t == "self":
                _, quien, texto = p
                x = self.x[quien]
                lineas = self._lineas(L, texto, max(120, min(260, self.ancho - self.x[quien] - 60)))
                y += 10
                if dibujar:
                    L.polilinea([(x, y), (x + 26, y), (x + 26, y + 18), (x + 6, y + 18)], uml.NEGRO, 1.5, flecha="filled")
                    tw = max(L.tsize(s, fuente(11))[0] for s in lineas)
                    L.rect(x + 32, y - 4, tw + 6, len(lineas) * LH + 4, uml.BLANCO, uml.BLANCO)
                    L.lineas_texto(x + 34, y - 2, lineas, 11)
                y += max(26, len(lineas) * LH + 4) + 4
            elif t == "note":
                _, quien, texto = p
                x = self.x[quien]
                w = min(300, self.ancho - x - 40)
                lineas = self._lineas(L, texto, w - 16)
                h = len(lineas) * LH + 14
                y += 8
                if dibujar:
                    L.nota(x + 14, y, w, texto, 11)
                y += h + 8
            elif t == "sep":
                y += 14
                if dibujar:
                    L.linea((40, y), (self.ancho - 40, y), uml.GRIS, 1, punteada=True)
                    tw, th = L.tsize(p[1], fuente(11, bold=True))
                    L.rect(60, y - th / 2 - 3, tw + 12, th + 6, uml.BLANCO, uml.GRIS, 1)
                    L.texto(66, y, p[1], 11, bold=True, anchor="lm")
                y += 16
            elif t in ("alt", "opt", "loop"):
                if t == "alt":
                    operandos = p[1]
                else:
                    operandos = [(p[1], p[2])]
                ks = self._involucrados([p])
                xs = [self.x[k] for k in ks]
                margen = 48 + 14 * (3 - nivel)
                fx1, fx2 = max(12, min(xs) - margen), min(self.ancho - 12, max(xs) + margen)
                y0 = y + 10
                y = y0 + 24
                seps = []
                for i, (guarda, sub) in enumerate(operandos):
                    if i > 0:
                        y += 6
                        seps.append(y)
                        y += 18
                    y = self._render(L, sub, y, dibujar, nivel + 1)
                y += 10
                if dibujar:
                    L.fragmento(fx1, y0, fx2 - fx1, y - y0, t, [f"[{g}]" for g, _ in operandos], seps)
                y += 6
        return y

    def dibujar(self, salida):
        # pasada 1: medir
        L0 = Lienzo(self.ancho, 200)
        y_top = 150
        y_fin = self._render(L0, self.pasos, y_top, dibujar=False)
        alto = int(y_fin + 50)
        L = Lienzo(self.ancho, alto)
        L.titulo(self.titulo, self.sub)
        for k, etiqueta, tipo in self.participantes:
            x = self.x[k]
            if tipo == "actor":
                L.actor(x, 62, etiqueta, 11)
                L.linea((x, 62 + 18 + 48 + LH + 4), (x, alto - 20), uml.GRIS, 1.2, punteada=True)
            else:
                L.participante(x, 88, etiqueta, alto - 20)
        self._render(L, self.pasos, y_top, dibujar=True)
        L.guardar(salida)
        return salida
