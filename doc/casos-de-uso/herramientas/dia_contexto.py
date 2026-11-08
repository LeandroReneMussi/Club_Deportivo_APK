"""Diagrama de contexto (nivel 0) — App Club Deportivo v1.5.1.

Solo muestra entidades que existen en el código:
- los cuatro roles con cuenta (tabla usuario) y el visitante, que no usa la app;
- el reloj del dispositivo (Reloj.hoy, fecha real desde la v1.5.0);
- la base SQLite local club_deportivo.db (ClubDbHelper.NOMBRE).
El manifiesto no declara el permiso INTERNET: no hay servidor ni pasarela de pago.
"""
import os
import uml
from uml import Lienzo

SALIDA = os.path.join(os.path.dirname(__file__), "..", "diagramas", "00_contexto.png")

L = Lienzo(1600, 1120)
L.titulo("Diagrama de contexto — App Club Deportivo v1.5.1", "Nivel 0 · Grupo 3 · 2026-11-08")

C_ENT = (255, 248, 225)   # entidad externa
C_SIS = (233, 242, 255)   # sistema


def entidad(x, y, w, h, nombre, sub=None, punteado=False):
    if punteado:
        L.rect(x, y, w, h, C_ENT)
        L.rect(x, y, w, h, borde=uml.GRIS, punteado=True)
    else:
        L.rect(x, y, w, h, C_ENT, radio=4)
    cy = y + h / 2 - (8 if sub else 0)
    L.texto(x + w / 2, cy, nombre, 14, bold=True, anchor="mm")
    if sub:
        L.texto(x + w / 2, cy + 20, sub, 11, color=uml.GRIS, anchor="mm")
    return (x, y, w, h)


def rotulo(x, y, lineas, size=11):
    """Etiqueta de flujo de varias líneas, con fondo blanco para no cruzar la flecha."""
    f = uml.fuente(size)
    tw = max(L.tsize(s, f)[0] for s in lineas)
    th = L.alto_lineas(len(lineas), size)
    L.rect(x - tw / 2 - 4, y - th / 2 - 3, tw + 8, th + 6, uml.BLANCO, uml.BLANCO)
    L.lineas_texto(x, y - th / 2, lineas, size, centrado=True)


# ---- Límite físico: todo corre dentro del dispositivo
DX, DY, DW, DH = 470, 140, 660, 790
L.rect(DX, DY, DW, DH, (250, 250, 250), uml.GRIS, 1.2)
L.rect(DX, DY, DW, DH, borde=uml.GRIS, punteado=True)
L.texto(DX + 12, DY + 10, "Dispositivo Android (7.0 / API 24 o superior)", 12, bold=True, color=uml.GRIS, anchor="la")

# ---- Sistema
SX, SY, SW, SH = 610, 430, 380, 150
L.rect(SX, SY, SW, SH, C_SIS, uml.NEGRO, 2.2, radio=24)
L.texto(800, 478, "App Club Deportivo", 18, bold=True, anchor="mm")
L.texto(800, 506, "v1.5.1 · Kotlin + Jetpack Compose", 12, anchor="mm")
L.texto(800, 528, "una Activity por rol", 12, color=uml.GRIS, anchor="mm")

# ---- Reloj del dispositivo (arriba, dentro del límite)
reloj = entidad(640, 200, 320, 66, "Reloj del sistema", "fecha y hora del dispositivo")
L.flecha([(800, 266), (800, SY)])
rotulo(800, 350, ["fecha real (Reloj.hoy)", "y hora de presencia"])

# ---- Base SQLite (abajo, dentro del límite): almacén con dos líneas
BY1, BY2 = 790, 850
L.linea((620, BY1), (980, BY1), uml.NEGRO, 1.8)
L.linea((620, BY2), (980, BY2), uml.NEGRO, 1.8)
L.texto(800, 812, "club_deportivo.db (SQLite)", 14, bold=True, anchor="mm")
L.texto(800, 833, "usuarios, socios, cuotas, pagos, parámetros, sesión", 11, color=uml.GRIS, anchor="mm")
L.flecha([(740, SY + SH), (740, BY1)])
rotulo(740, 690, ["guarda altas, cambios,", "pagos y parámetros"])
L.flecha([(860, BY1), (860, SY + SH)])
rotulo(860, 690, ["lee datos y", "parámetros vigentes"])

# ---- Entidades externas: roles con cuenta
socio = entidad(60, 200, 300, 70, "Socio")
admin = entidad(60, 690, 300, 70, "Administrativo")
prof = entidad(1240, 200, 300, 70, "Entrenadora")
nutr = entidad(1240, 690, 300, 70, "Nutricionista")

# Socio <-> sistema
L.flecha([(360, 225), (520, 225), (520, 470), (SX, 470)])
rotulo(440, 330, ["credenciales", "pago de cuota", "(medio simulado)", "turnos de nutrición"])
L.flecha([(SX, 500), (560, 500), (560, 250), (360, 250)])
rotulo(560, 600 - 290, ["estado de cuenta", "y contador de días", "carnet digital y QR", "rutina · comprobante"])

# Administrativo <-> sistema
L.flecha([(360, 715), (500, 715), (500, 560), (SX, 560)])
rotulo(420, 630, ["socios: alta, edición, baja", "DNI de acceso", "cobro en caja", "pago diario de visitante", "gracia, recargo, interés"])
L.flecha([(SX, 540), (560, 540), (560, 740), (360, 740)])
rotulo(560, 655, ["resultado", "del acceso", "reportes", "comprobantes", "carnet"])

# Entrenadora <-> sistema
L.flecha([(1240, 225), (1080, 225), (1080, 470), (SX + SW, 470)])
rotulo(1160, 330, ["presencia propia", "asistencia de clase", "rutinas asignadas"])
L.flecha([(SX + SW, 500), (1040, 500), (1040, 250), (1240, 250)])
rotulo(1040, 310, ["clases del día", "y socios"])

# Nutricionista <-> sistema
L.flecha([(1240, 715), (1080, 715), (1080, 560), (SX + SW, 560)])
rotulo(1160, 640, ["consultas", "nutricionales"])
L.flecha([(SX + SW, 540), (1025, 540), (1025, 740), (1240, 740)])
rotulo(1025, 655, ["agenda", "de turnos"])

# ---- Visitante: no usa la app; lo atiende el Administrativo
visit = entidad(60, 960, 300, 70, "No socio / Visitante", "no usa la app", punteado=True)
L.flecha([(210, 960), (210, 760)], punteada=True)
rotulo(210, 860, ["DNI y pago del día", "en el mostrador"])

# ---- Notas con lo que NO existe (verificado en el código)
n = L.nota(1180, 900, 380,
           "Sin conexión a Internet: el manifiesto no declara el permiso INTERNET. "
           "No hay servidor, pasarela de pago ni lector de QR: el pago desde la app es simulado "
           "y el QR de ingreso solo se muestra en pantalla.")

# Leyenda
L.rect(470, 960, 26, 18, C_ENT)
L.texto(504, 969, "Entidad externa", 11, anchor="lm")
L.rect(470, 988, 26, 18, C_SIS, radio=6)
L.texto(504, 997, "Sistema", 11, anchor="lm")
L.linea((470, 1018), (496, 1018), uml.NEGRO, 1.8)
L.linea((470, 1032), (496, 1032), uml.NEGRO, 1.8)
L.texto(504, 1025, "Almacén de datos local", 11, anchor="lm")
L.linea((700, 969), (740, 969), uml.GRIS, 1.5, punteada=True)
L.texto(748, 969, "Relación fuera de la app", 11, anchor="lm")

L.guardar(SALIDA)
print("ok", SALIDA)
