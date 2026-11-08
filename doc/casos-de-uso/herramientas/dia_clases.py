"""Diagrama de clases UML 2.0 — modelo de dominio (data/Models.kt + spec v1.5)."""
import os
import uml
from uml import Lienzo, abajo, arriba, izquierda, derecha

SALIDA = os.path.join(os.path.dirname(__file__), "..", "diagramas", "02_clases_dominio.png")

L = Lienzo(1600, 1120)
L.titulo("Diagrama de clases — modelo de dominio Club Deportivo v1.5.0",
         "UML 2.0 · fuente: data/Models.kt (v1.4.0) + spec v1.5.0 · el carnet son atributos del socio")

# ---- fila 1
usuario = L.clase(60, 90, "Usuario", ["alias: String {id}", "clave: String", "rol: Rol", "socioId: Int [0..1]"])
rol = L.clase(330, 90, "Rol", ["SOCIO", "EMPLEADO", "ADMIN", "PROFESOR", "NUTRICIONISTA"], estereotipo="«enumeration»", relleno=uml.C_ENUM)
vista = L.clase(560, 90, "SocioVista(socio, parametros)", [
    "/carnetVencido, /hab: Boolean", "/cuotaVencida, /enGracia, /mora: Boolean (v1.5)",
    "/diasVencida, /gracia, /diasDeGracia: Int (v1.5)",
    "/recargo, /interes: Double · /totalAdeudado: Int (v1.5)",
    "/badge, /contador: String · /tono: TonoCuota (v1.5)",
    "/qrCarnet: String  (contenido del QR del carnet)",
], estereotipo="«derivada»", relleno=(236, 239, 255))
plan = L.clase(1000, 90, "Plan", ["nombre: String {id}", "precio: Int"])
actividad = L.clase(1250, 90, "Actividad", ["id: String {id}", "nombre: String", "horario: String", "cupo: Int", "ocupados: Int", "precio: Int", "profesor: String"])

# ---- fila 2
turno = L.clase(60, 430, "Turno", ["fecha: Fecha", "hora: String"])
ingreso = L.clase(60, 570, "Ingreso", ["fecha: String", "estado: String"])
socio = L.clase(420, 340, "Socio", [
    "id: Int {id}", "nro: String {unique}", "nombre: String  (Nombre Apellido)", "dni: Long {unique, 7..8 dígitos}",
    "email: String", "plan: String", "alta: Fecha", "nac: Fecha",
    "ficha: Boolean  (apto físico)", "carnetEmitido: Fecha", "carnetVence: Fecha",
])
cuota = L.clase(780, 340, "Cuota", ["mes: String", "monto: Int", "vence: Fecha", "estado: EstadoCuota", "proximaVence: Fecha [0..1]"])
estado = L.clase(1010, 340, "EstadoCuota", ["PENDIENTE", "PAGADA", "VENCIDA"], estereotipo="«enumeration»", relleno=uml.C_ENUM)
visitante = L.clase(1250, 360, "Visitante", ["nombre: String", "dni: String  (con puntos)", "actividad: String", "hora: String", "monto: Int", "fecha: Fecha", "medio: String  (Efectivo | Débito)"])

# ---- fila 3
pago = L.clase(780, 560, "PagoHistorial", ["mes: String", "monto: Int  (v1.5: total cobrado)", "pagada: Fecha", "medio: String", "comprobante: String"])
param = L.clase(1010, 560, "Parametros (v1.5)", ["graciaDias: Int  {0..60}", "recargo: Double  ($, fijo, 0..1.000.000)", "interesMensual: Double  (%, 0..100)", "vigenteDesde: Fecha", "INICIALES = (5, 0, 0, hoy)"], operaciones=["List<Parametros>.vigenteEn(fecha)"], relleno=uml.C_V15)

# ---- fila 4
rutina = L.clase(60, 760, "Rutina", ["nombre: String", "profesor: String", "actualizada: Fecha"])
dia = L.clase(320, 760, "DiaRutina", ["tag: String", "foco: String"])
ejercicio = L.clase(560, 760, "Ejercicio", ["nombre: String", "series: Int {1..20}", "reps: Int {1..100}", "nota: String"])
ficha = L.clase(800, 800, "FichaNutricional", ["peso: Double", "altura: Double", "objetivo: String", "antecedentes: String"])
consulta = L.clase(1060, 800, "Consulta", ["fecha: Fecha", "peso: Double", "obs: String"])

# ---- relaciones
L.relacion(usuario, rol, "nav", m2="1", etiqueta="rol")
L.relacion(usuario, socio, "asoc", m1="0..1", m2="1", etiqueta="cuenta de la app", puntos=[(160, 380)], p2=(socio[0], 380))
L.relacion(vista, socio, "dep", etiqueta="«derive»")
L.relacion(socio, plan, "asoc", m1="*", m2="1", etiqueta="plan (por nombre)")
L.relacion(socio, cuota, "comp", m1="1", m2="1", etiqueta="cuota vigente")
L.relacion(cuota, estado, "nav", m2="1")
L.relacion(socio, pago, "comp", m1="1", m2="0..*", etiqueta="historial")
L.relacion(socio, turno, "comp", m1="1", m2="0..1", etiqueta="turno")
L.relacion(socio, ingreso, "comp", m1="1", m2="0..*", etiqueta="ingresos")
L.relacion(socio, rutina, "comp", m1="1", m2="0..1", etiqueta="rutina", desplaz_m=(-8, -6))
L.relacion(rutina, dia, "comp", m1="1", m2="1..*", etiqueta="dias")
L.relacion(dia, ejercicio, "comp", m1="1", m2="0..*", etiqueta="ejercicios")
L.relacion(socio, ficha, "comp", m1="1", m2="1", etiqueta="nut")
L.relacion(ficha, consulta, "comp", m1="1", m2="0..*", etiqueta="consultas")
L.relacion(visitante, actividad, "asoc", m1="0..*", m2="1", etiqueta="actividad (por nombre)")
# Socio *—* Actividad con codos por arriba de Cuota
L.relacion(socio, actividad, "asoc", m1="0..*", m2="0..*", etiqueta="inscriptos / actividades",
           puntos=[(700, 305), (1200, 305), (1200, 230)], p2=(actividad[0], 230))
L.relacion(param, cuota, "dep", etiqueta="«use» estado derivado y deuda")

# ---- notas
n1 = L.nota(1250, 600, 300,
            "Parametros (v1.5): lista en UiState.parametros, cargada por ClubRepositorio.iniciar() → ClubDbHelper.leer() y reescrita (DELETE + INSERT) en guardar(). "
            "Cada guardado antepone una fila con vigenteDesde = hoy (si ya hay una de hoy, la reemplaza). vigenteEn(fecha) toma el más nuevo con vigenteDesde <= fecha; antes del primero guardado devuelve la gracia de la fila más antigua con recargo 0 e interés 0 (recargo e interés no se aplican hacia atrás).")
L.ancla_nota(n1, derecha(param))

n2 = L.nota(60, 940, 560,
            "Regla de habilitación (SocioVista): hab = !mora && !carnetVencido. En v1.5 mora se deriva de la fecha: cuotaVencida = !pagada && diasVencida > 0; "
            "enGracia = cuotaVencida && diasVencida <= gracia (AVISO, naranja); mora = cuotaVencida && diasVencida > gracia (BLOQUEO, rojo). "
            "Si la cuota venció: recargo = vigente hoy, interés por día con la tasa de cada día, totalAdeudado = cuota + recargo + interés. EstadoCuota.VENCIDA deja de decidir.")
n3 = L.nota(660, 980, 520,
            "Persistencia (ClubDbHelper, SQLite, versión 4 en v1.5): tablas usuario, socio, carnet (emitido, vence), cuota, pago, plan, "
            "actividad, actividad_inscripto, visitante, ingreso_hoy, asistencia_clase, asistencia, sesion y parametro(id, gracia_dias, recargo, interes_mensual, vigente_desde, orden) (v1.5). "
            "Rutina, consultas, actividades del socio e ingresos se guardan como JSON dentro de socio.")

L.guardar(SALIDA)
print("ok", SALIDA)
