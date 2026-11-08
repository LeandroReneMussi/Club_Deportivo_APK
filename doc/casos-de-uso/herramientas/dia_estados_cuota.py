"""Máquina de estados UML 2.0 de la Cuota (v1.5)."""
import os
import uml
from uml import Lienzo

SALIDA = os.path.join(os.path.dirname(__file__), "..", "diagramas", "07_estados_cuota.png")

L = Lienzo(1300, 780)
L.titulo("Máquina de estados — Cuota del socio (v1.5)",
         "UML 2.0 · d = días desde Cuota.vence (negativo si falta) · gracia = parametros.vigenteEn(HOY).graciaDias · parámetros en UiState, leídos de SQLite al iniciar")

ini = L.inicial(90, 200)
pend = L.estado(330, 200, "Pendiente", ["habilitado · tono OK (verde)", "«Vence en N días» / «Vence hoy»", "totalAdeudado = cuota", "d <= 0"], w=230)
grac = L.estado(700, 200, "En período de gracia", ["habilitado · tono AVISO (naranja)", "«Quedan N días de gracia» / «Último día de gracia»", "totalAdeudado = cuota + recargo + interés", "0 < d <= gracia"], w=250)
mora = L.estado(1080, 200, "En mora", ["no habilitado · tono BLOQUEO (rojo)", "«Vencida hace N días · gracia de G días superada»", "totalAdeudado = cuota + recargo + interés", "d > gracia"], w=270)
pag = L.estado(640, 500, "Pagada", ["«Pagada · próxima dd/mm/aaaa»", "proximaVence = vence + 1 mes", "historial += PagoHistorial(total, medio, comprobante)"], w=300, relleno=(232, 245, 233))
fin = L.final(1080, 500)

# inicial → Pendiente
L.transicion(ini, pend, "alta de socio (HU-02) / cuota inicial: monto = precio del plan, vence = hoy + 10 días", ancho_etiqueta=150, pos_etiqueta=(150, 270))

# Pendiente → En gracia
L.transicion(pend, grac, "cambio de fecha [0 < d <= gracia]", desplaz=(0, -30))
# Pendiente → En mora (por arriba)
L.transicion(pend, mora, "cambio de fecha [d > gracia]  (p. ej. gracia = 0)", puntos=[(330, 95), (1080, 95)], pos_etiqueta=(705, 95))
# En gracia → En mora
L.transicion(grac, mora, "cambio de fecha [d > gracia]", desplaz=(0, -30))
# En mora → En gracia (el administrador baja la... sube la gracia)
L.transicion(mora, grac, "el administrador guarda una gracia mayor [d <= gracia nueva]", puntos=[(1040, 125), (740, 125)],
             p1=(1040, mora[1]), p2=(740, grac[1]), pos_etiqueta=(890, 125), ancho_etiqueta=260)

# pagos
L.transicion(pend, pag, "pagar(medio) [estado != PAGADA] / total = cuota (sin vencer)", puntos=[(260, 500)],
             p1=(260, pend[1] + pend[3]), p2=(pag[0], 500), pos_etiqueta=(260, 400), ancho_etiqueta=150)
L.transicion(grac, pag, "pagar(medio) / total = cuota + recargo + interés (se deben desde el vencimiento)", pos_etiqueta=(830, 310), ancho_etiqueta=200)
L.transicion(mora, pag, "pagar(medio) [en caja o desde la app] / total = cuota + recargo + interés", pos_etiqueta=(1060, 390), ancho_etiqueta=220)

# Pagada → Pendiente (cuota siguiente) y fin
L.transicion(pag, pend, "apertura de la cuota del mes siguiente (completarCuotasSiguientes) / nueva Cuota PENDIENTE, vence = vence + 1 mes",
             puntos=[(540, 340), (420, 340)], p1=(540, pag[1]), p2=(420, pend[1] + pend[3]), pos_etiqueta=(470, 290), ancho_etiqueta=200)
L.transicion(pag, fin, "baja del socio (ON DELETE CASCADE)", desplaz=(0, -14))

# notas
n1 = L.nota(40, 620, 560,
            "Estado derivado (v1.5): Pendiente, En período de gracia y En mora se calculan en SocioVista a partir de la fecha real del dispositivo y "
            "del parámetro vigente; en SQLite la cuota sigue guardada como PENDIENTE hasta que se paga. El valor VENCIDA deja de decidir. "
            "Si no hay pago, la cuota no cambia de mes: la deuda crece día a día por el interés.")
n2 = L.nota(640, 620, 620,
            "Recargo e interés se deben desde que la cuota vence, también dentro de la gracia. Recargo: importe fijo vigente hoy, una vez. Interés: por cada día desde el vencimiento hasta hoy, "
            "cuota × (interés mensual vigente ese día / 100) / 30 con la tasa de ESE día (vigenteEn); antes de la primera fila no se devenga nada (no retroactivo). Demo: Lucas, vencido hace 29 días, debe $12.848 = $12.000 + $500 + $348 (29 días al 3 %); Rocío Paz, en gracia hace 3 días, $12.536. "
            "pagarCuota cobra totalAdeudado en gracia y en mora; una cuota PAGADA no se cobra dos veces. El carnet vencido bloquea el acceso en cualquier estado de la cuota.")

L.guardar(SALIDA)
print("ok", SALIDA)
