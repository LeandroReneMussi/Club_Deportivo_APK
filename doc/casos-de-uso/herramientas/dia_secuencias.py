# -*- coding: utf-8 -*-
"""Dibuja los 26 diagramas de secuencia UML 2.0 (uno por caso de uso) definidos en cu_secuencias.py."""
import os
import cu_secuencias as S

DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "diagramas")

if __name__ == "__main__":
    for cu, slug, titulo, sub, sec in S.SEC:
        sec.dibujar(os.path.join(DIR, slug + ".png"))
        print("ok", slug)
