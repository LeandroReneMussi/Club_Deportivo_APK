"""Diagrama de actividad UML 2.0 — Alta de socio (CU-04, HU-02) con validaciones."""
import os
import uml
from uml import Lienzo, abajo, arriba, izquierda, derecha

SALIDA = os.path.join(os.path.dirname(__file__), "..", "diagramas", "08_actividad_alta_socio.png")

L = Lienzo(1100, 1330)
L.titulo("Diagrama de actividad — Alta de socio (CU-04, HU-02)",
         "UML 2.0 · ClubViewModel.irAAlta() → confirmarAlta() · validaciones de data/Validaciones.kt y spec v1.5")

# carriles
L.carril(40, 70, 300, 1200, "Administrativo")
L.carril(340, 70, 420, 1200, "App · ClubViewModel (Compose)")
L.carril(760, 70, 300, 1200, "ClubRepositorio · SQLite")

XA, XB, XC = 190, 550, 910
W = 260

ini = L.inicial(XA, 130)
a1 = L.accion(XA, 200, "Inicio → «Registrar socio»", w=220)
a2 = L.accion(XB, 200, "irAAlta(): formulario limpio, plan Full por defecto, apto físico activo", w=W)
a3 = L.accion(XA, 300, "Completa nombre, apellido (v1.5), DNI, correo, plan, apto físico y observaciones; toca «Registrar socio»", w=240)
a4 = L.accion(XB, 300, "confirmarAlta() → errorFormularioSocio()", w=W)

ys = [400, 480, 560, 640, 720]
textos = [
    "¿nombre válido? (obligatorio, solo letras)",
    "¿apellido válido? (v1.5: obligatorio, solo letras)",
    "¿DNI con 7 u 8 dígitos, solo números (con o sin puntos)?",
    "¿DNI único? contarDni(dni) == 0",
    "¿correo válido? (obligatorio, nombre@dominio.com)",
]
dec = [L.decision(XB - 100, y, t, ancho_texto=230) for y, t in zip(ys, textos)]

# COUNT en SQLite para el DNI único
q = L.accion(XC, 640, "SELECT COUNT(*) FROM socio WHERE dni = ?", w=240)

err = L.accion(XA, 870, "Ve el mensaje bajo el formulario (altaError) y permanece en «Alta de socio»", w=240)

a5 = L.accion(XB, 820, "Generar N° de socio: 1500 + cantidad de socios; si ya existe (por una baja), el siguiente libre", w=W)
a6 = L.accion(XB, 905, "Emitir carnet: emitido = hoy · vence = hoy + 12 meses − 1 día", w=W)
a7 = L.accion(XB, 985, "Crear cuota inicial PENDIENTE: monto = precio del plan · vence = hoy + 10 días", w=W)
a8 = L.accion(XB, 1065, "Ficha nutricional inicial (antecedentes = observaciones o «Sin antecedentes»), sin rutina ni turno", w=W)
a9 = L.accion(XC, 1065, "guardar(estado): INSERT socio, carnet, cuota (padrón actualizado)", w=240)
a10 = L.accion(XA, 1160, "Ve la ficha del socio nuevo · toast «Socio N° xxxxx registrado · carnet y cuota inicial generados»", w=240)
fin = L.final(XA, 1250)

# flujo principal
L.flecha([abajo(ini), arriba(a1)])
L.flecha([derecha(a1), izquierda(a2)])
L.flecha([abajo(a2), (XB, 260), (XA, 260), arriba(a3)])
L.flecha([derecha(a3), izquierda(a4)])
L.flecha([abajo(a4), arriba(dec[0])])
for i in range(len(dec) - 1):
    L.flecha([abajo(dec[i]), arriba(dec[i + 1])], "sí", desplaz=(14, 0))
L.flecha([abajo(dec[-1]), arriba(a5)], "sí", desplaz=(14, 0))
L.flecha([abajo(a5), arriba(a6)])
L.flecha([abajo(a6), arriba(a7)])
L.flecha([abajo(a7), arriba(a8)])
L.flecha([derecha(a8), izquierda(a9)])
L.flecha([abajo(a9), (XC, 1120), (XA, 1120), arriba(a10)])
L.flecha([abajo(a10), arriba(fin)])

# consulta del DNI único
L.flecha([(XB - 100 + 22, 640 - 10), (XC - 120, 630)], punteada=True)
L.flecha([(XC - 120, 650), (XB - 100 + 22, 650)], "n", punteada=True)

# ramas "no" → nodo de unión → error
XM = XB - 220
for d, y in zip(dec, ys):
    L.flecha([izquierda(d), (XM, y)], "no", desplaz=(0, -10))
L.linea((XM, ys[0]), (XM, 790))
L.flecha([(XM, 790), (XA, 790), (XA, err[1])])
# el administrativo corrige y vuelve al formulario
L.flecha([izquierda(err), (60, 870), (60, 300), (a3[0], 300)], "corrige los datos", pos=(60, 585))

L.nota(770, 180, 280,
       "Mensajes (Validaciones.kt): «Ingresá nombre y apellido.» · «El nombre solo puede contener letras y espacios.» · "
       "«Ingresá el DNI.» · «El DNI solo puede contener números.» · «El DNI debe tener 7 u 8 dígitos.» · «Ya existe un socio con ese DNI.» · "
       "«Ingresá el correo electrónico.» · «El correo electrónico no tiene un formato válido (ej.: nombre@dominio.com).»")
L.nota(770, 800, 280,
       "En v1.5 el apellido va en un campo aparte y se guarda nombre = «Nombre Apellido»; al editar se precarga partiendo en el primer espacio. "
       "El mismo flujo sirve para la edición (guardarEdicion), que no toca la cuota emitida.")

L.guardar(SALIDA)
print("ok", SALIDA)
