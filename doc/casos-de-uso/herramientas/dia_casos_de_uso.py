"""Diagrama de casos de uso UML 2.0 — App Club Deportivo v1.5.0."""
import os
import uml
from uml import Lienzo

SALIDA = os.path.join(os.path.dirname(__file__), "..", "diagramas", "01_casos_de_uso.png")

L = Lienzo(1500, 1200)
L.titulo("Diagrama de casos de uso — App Club Deportivo v1.5.0", "UML 2.0 · Grupo 3 · 2026-11-08")

# Sistema
BX, BY, BW, BH = 300, 175, 900, 900
L.rect(BX, BY, BW, BH, (252, 252, 252), uml.NEGRO, 1.5)
L.texto(BX + 14, BY + 8, "App Club Deportivo", 13, bold=True, anchor="la")

# Actor generalizado (todos los roles) arriba, fuera del sistema
usuario = L.actor(750, 60, "Usuario del club")

XA, XB, XC = 480, 750, 1020
Y0, P = 250, 62

def fila(i):
    return Y0 + i * P

# ---- casos compartidos (columna B, arriba)
cu01 = L.caso(XB, fila(0), "CU-01 Iniciar sesión")
cu02 = L.caso(XB, fila(1), "CU-02 Cerrar sesión")
L.asociacion(usuario["pies"], cu01)
codo = (880, fila(0))
L.polilinea([usuario["pies"], codo, L.borde_elipse(cu02, codo)])

# ---- Administrativo (columna A)
admin_cases = [
    ("CU-03 Consultar socios", False),
    ("CU-04 Registrar socio", False),
    ("CU-05 Editar socio", False),
    ("CU-06 Dar de baja socio", False),
    ("CU-07 Registrar pago de cuota en caja (v1.5)", True),
    ("CU-08 Emitir / renovar carnet", False),
    ("CU-09 Validar ingreso por DNI (v1.5)", True),
    ("CU-11 Registrar pago diario de no socio", False),
    ("CU-12 Consultar reportes (v1.5)", True),
    ("CU-13 Configurar parámetros del club (v1.5)", True),
]
ea = {}
for i, (nombre, v15) in enumerate(admin_cases):
    ea[nombre[:5]] = L.caso(XA, fila(i), nombre, relleno=uml.C_V15 if v15 else uml.C_CASO)

admin = L.actor(120, 500, "Administrativo")
for k, e in ea.items():
    L.asociacion(admin["der"], e)

# CU-10 extiende CU-09 (columna B)
cu10 = L.caso(XB, fila(7), "CU-10 Registrar ingreso del socio")
L.dependencia(cu10, ea["CU-09"], "«extend»")

# Caso incluido (columna B)
inc = L.caso(XB, fila(4.5), "Calcular estado y deuda de la cuota: gracia, recargo e interés (v1.5)", relleno=uml.C_V15, maxw=200)

# ---- Columna C: Entrenadora, Nutricionista, Socio
col_c = [
    ("CU-21 Registrar presencia propia", False, "P"),
    ("CU-22 Registrar asistencia de clase", False, "P"),
    ("CU-23 Crear y asignar rutina", False, "P"),
    ("CU-24 Consultar agenda de turnos", False, "N"),
    ("CU-25 Registrar consulta nutricional", False, "N"),
    ("CU-14 Pagar cuota desde la app (v1.5)", True, "S"),
    ("CU-15 Consultar estado de cuenta y contador de días (v1.5)", True, "S"),
    ("CU-16 Ver carnet digital", False, "S"),
    ("CU-17 Consultar rutina asignada", False, "S"),
    ("CU-18 Solicitar turno de nutrición", False, "S"),
    ("CU-19 Cancelar turno de nutrición", False, "S"),
    ("CU-20 Consultar información nutricional propia", False, "S"),
]
ec = {}
for i, (nombre, v15, quien) in enumerate(col_c):
    ec[nombre[:5]] = (L.caso(XC, fila(i), nombre, relleno=uml.C_V15 if v15 else uml.C_CASO), quien)

prof = L.actor(1380, 240, "Entrenadora")
nutr = L.actor(1380, 450, "Nutricionista")
socio = L.actor(1380, 790, "Socio")
pies = {"P": prof["izq"], "N": nutr["izq"], "S": socio["izq"]}
for k, (e, quien) in ec.items():
    L.asociacion(pies[quien], e)

# CU-26 extiende CU-16 (columna B)
cu26 = L.caso(XB, fila(8.2), "CU-26 Mostrar QR de ingreso")
L.dependencia(cu26, ec["CU-16"][0], "«extend»")

# includes hacia el caso de cálculo
for e in (ea["CU-07"], ea["CU-09"], ec["CU-14"][0], ec["CU-15"][0]):
    L.dependencia(e, inc, "«include»")

# ---- Notas
n1 = L.nota(30, 880, 240,
            "El No socio / Visitante no opera la app: el Administrativo registra su pago diario en el mostrador (CU-11) y valida el cupo del día.")
L.ancla_nota(n1, L.borde_elipse(ea["CU-11"], (270, 900)))

n2 = L.nota(960, 50, 300,
            "Generalización de actores: Socio, Administrativo, Entrenadora y Nutricionista especializan a «Usuario del club». Todos inician y cierran sesión (HU-01); el rol sale de la cuenta.")
L.ancla_nota(n2, (770, 80))

n3 = L.nota(30, 1090, 420,
            "Fondo naranja = caso nuevo o modificado por el spec v1.5.0 (gracia configurable, recargo e interés sobre la deuda, contador de días, parámetros con vigencia en SQLite).")
L.rect(480, 1100, 26, 18, uml.C_V15)
L.texto(514, 1109, "(v1.5)", 11, anchor="lm")
L.rect(480, 1128, 26, 18, uml.C_CASO)
L.texto(514, 1137, "v1.4.0 (código actual)", 11, anchor="lm")

L.guardar(SALIDA)
print("ok", SALIDA)
