"""Los cuatro diagramas de secuencia UML 2.0 (ver spec v1.5.0 y ClubViewModel.kt)."""
import os
from secuencia import Secuencia

DIR = os.path.join(os.path.dirname(__file__), "..", "diagramas")

# ------------------------------------------------------------------ (a) control de acceso
a = Secuencia(
    "Secuencia — Control de acceso por DNI (CU-09 / CU-10, HU-05) con la regla v1.5",
    "pendiente · en gracia · mora: el estado se deriva de la fecha y de la gracia vigente; los parámetros viven en el estado (UiState.parametros), no se consultan en cada acceso",
    [("adm", "Administrativo", "actor"), ("ui", ":PantallaAcceso\n(Compose)", "obj"), ("vm", ":ClubViewModel", "obj"),
     ("sv", ":SocioVista", "obj"), ("repo", ":ClubRepositorio", "obj"), ("db", ":ClubDbHelper\n(SQLite)", "obj")],
    [
        ("msg", "adm", "ui", "escribe el DNI y toca «Validar»"),
        ("msg", "ui", "vm", "validarAcceso()"),
        ("self", "vm", "Validaciones.dni(campos.dniAcceso)"),
        ("alt", [
            ("DNI inválido: vacío, con letras o distinto de 7–8 dígitos", [
                ("ret", "vm", "ui", "acceso = null · toast(mensaje de validación)"),
            ]),
            ("DNI válido", [
                ("self", "vm", "busca el socio por DNI en estado.socios (padrón leído de SQLite)"),
                ("alt", [
                    ("no hay socio con ese DNI", [
                        ("ret", "vm", "ui", "acceso = «No encontrado» · sugiere registrarlo como visitante o dar de alta"),
                    ]),
                    ("existe el socio", [
                        ("msg", "vm", "sv", "SocioVista(socio, estado.parametros)  (v1.5: parámetros desde el estado, leídos de SQLite al iniciar)"),
                        ("self", "sv", "gracia = parametros.vigenteEn(HOY).graciaDias · diasVencida = días desde cuota.vence · carnetVencido = carnetVence < HOY · recargo e interés si cuotaVencida · totalAdeudado"),
                        ("alt", [
                            ("carnet vencido", [
                                ("ret", "sv", "vm", "hab = false · carnetVencido"),
                                ("ret", "vm", "ui", "acceso = «Denegado»: «Carnet vencido el dd/mm/aaaa. Debe renovarse.»"),
                            ]),
                            ("cuota pagada, o pendiente con d <= 0", [
                                ("ret", "sv", "vm", "hab = true · contador «Vence en N días» / «Vence hoy» · tono OK (verde)"),
                                ("ret", "vm", "ui", "acceso = «Permitido»: «Carnet vigente y cuota al día.» o «Carnet vigente · cuota {mes} pendiente, vence en N días.»"),
                            ]),
                            ("0 < d <= gracia  (v1.5: en gracia)", [
                                ("ret", "sv", "vm", "hab = true · enGracia · contador «Quedan N días de gracia» · tono AVISO (naranja) · totalAdeudado"),
                                ("ret", "vm", "ui", "acceso = «Permitido»: «Cuota {mes} vencida · quedan N días de gracia. Debe $total en recepción.»"),
                            ]),
                            ("d > gracia  (v1.5: mora)", [
                                ("ret", "sv", "vm", "hab = false · mora · contador «Vencida hace N días · gracia de G días superada» · tono BLOQUEO (rojo) · totalAdeudado"),
                                ("ret", "vm", "ui", "acceso = «Denegado»: «Cuota {mes} en mora: vencida hace N días · gracia de G días superada. Debe $total en recepción.»"),
                            ]),
                        ]),
                    ]),
                ]),
            ]),
        ]),
        ("opt", "acceso Permitido y todavía no registrado", [
            ("msg", "adm", "ui", "toca «Registrar ingreso»"),
            ("msg", "ui", "vm", "registrarIngreso()"),
            ("self", "vm", "ingresosHoy = [IngresoHoy(HORA_ACTUAL, nombre, «Permitido»)] + ingresosHoy · socio.ingresos += Ingreso(fecha·hora) · acceso.registrado = true"),
            ("msg", "vm", "repo", "guardar(estado)"),
            ("msg", "repo", "db", "INSERT INTO ingreso_hoy · UPDATE socio SET ingresos_json"),
            ("ret", "vm", "ui", "toast «Ingreso registrado · nombre»"),
        ]),
    ],
)
a.dibujar(os.path.join(DIR, "03_sec_control_acceso.png"))

# ------------------------------------------------------------------ (b) cobro en caja
b = Secuencia(
    "Secuencia — Cobro de cuota en caja con recargo e interés (CU-07, HU-04) (v1.5)",
    "pedirRegistrarPago() → hoja con totalAdeudado → pagarCuota(id, «Caja») → SQLite · recargo e interés se deben desde el vencimiento, también en gracia",
    [("adm", "Administrativo", "actor"), ("ui", ":PantallaFichaSocio\n(Compose)", "obj"), ("vm", ":ClubViewModel", "obj"),
     ("sv", ":SocioVista", "obj"), ("repo", ":ClubRepositorio", "obj"), ("db", ":ClubDbHelper\n(SQLite)", "obj")],
    [
        ("msg", "adm", "ui", "toca «Registrar pago en caja» en la ficha del socio"),
        ("msg", "ui", "vm", "pedirRegistrarPago()"),
        ("msg", "vm", "sv", "seleccionado (SocioVista del socio abierto)"),
        ("alt", [
            ("cuota.estado == PAGADA", [
                ("ret", "vm", "ui", "toast «La cuota de {mes} ya está pagada.» · no abre la hoja (una cuota no se cobra dos veces)"),
            ]),
            ("cuota no pagada", [
                ("msg", "vm", "sv", "SocioVista(socio, estado.parametros).totalAdeudado  (v1.5)"),
                ("self", "sv", "cuotaVencida = !pagada && diasVencida > 0 · recargo = si cuotaVencida, parametros.vigenteEn(HOY).recargo (una vez) · interés = Σ d = 1..diasVencida: cuota × vigenteEn(vence + d).interesMensual / 100 / 30 (los días anteriores al primer parámetro guardado devengan 0) · totalAdeudado = cuota + recargo + interés (en gracia y en mora; sin vencer = cuota)"),
                ("ret", "sv", "vm", "totalAdeudado · deudaDetalle «Cuota $x + recargo $y + interés $z»"),
                ("ret", "vm", "ui", "sheet «Registrar pago»: Socio · Cuota {mes} · Importe = total con el detalle · CTA «Cobrar $total»"),
                ("msg", "adm", "ui", "confirma «Cobrar»"),
                ("msg", "ui", "vm", "onConfirm → pagarCuota(id, «Caja»)"),
                ("self", "vm", "total = SocioVista(s, estado.parametros).totalAdeudado · comprobante = «CD-2609-####» · cuota.estado = PAGADA · proximaVence = vence + 1 mes · historial = [PagoHistorial(mes, total, HOY, «Caja», comprobante)] + historial"),
                ("msg", "vm", "repo", "guardar(estado)"),
                ("msg", "repo", "db", "UPDATE cuota SET estado = 'PAGADA', proxima_vence = ? · INSERT INTO pago (mes, monto = total, pagada, medio, comprobante)"),
                ("ret", "db", "repo", "ok"),
                ("ret", "vm", "ui", "cerrarSheet() · toast «Pago registrado · socio habilitado»"),
                ("note", "repo", "Al reabrir la app, completarCuotasSiguientes() da de alta la cuota del mes siguiente (PENDIENTE, vence = vence + 1 mes) para cada cuota pagada que aún no la tenga."),
            ]),
        ]),
    ],
)
b.dibujar(os.path.join(DIR, "04_sec_cobro_caja.png"))

# ------------------------------------------------------------------ (c) pago diario no socio
c = Secuencia(
    "Secuencia — Pago diario de un no socio (CU-11, HU-03): cupo del día y DNI repetido",
    "elegirActividadVisitante() · confirmarVisitante() · contarVisitante(dni, HOY) en SQLite",
    [("adm", "Administrativo", "actor"), ("ui", ":PantallaVisitanteNuevo\n(Compose)", "obj"), ("vm", ":ClubViewModel", "obj"),
     ("repo", ":ClubRepositorio", "obj"), ("db", ":ClubDbHelper\n(SQLite)", "obj")],
    [
        ("msg", "adm", "ui", "abre Visitantes → «Registrar visitante»"),
        ("msg", "ui", "vm", "irAVisitanteNuevo()  · limpia nombre y DNI, medio = Efectivo, sin actividad"),
        ("msg", "adm", "ui", "escribe nombre y DNI del visitante, elige actividad y medio (Efectivo / Débito)"),
        ("msg", "ui", "vm", "elegirActividadVisitante(actividad)"),
        ("self", "vm", "libres = cupo − (ocupados + visitantes de hoy en esa actividad)"),
        ("alt", [
            ("libres <= 0 (sin cupo)", [
                ("ret", "vm", "ui", "visError «{actividad} no tiene cupo disponible hoy (x/y). El ingreso no puede realizarse.»"),
            ]),
            ("con cupo", [
                ("ret", "vm", "ui", "visAct = actividad.id · visError = null · muestra «N libres»"),
            ]),
        ]),
        ("msg", "adm", "ui", "toca «Registrar pago diario»"),
        ("msg", "ui", "vm", "confirmarVisitante()"),
        ("self", "vm", "Validaciones.nombre(vNombre, « del visitante») ?: Validaciones.dni(vDni)"),
        ("alt", [
            ("nombre o DNI inválidos", [
                ("ret", "vm", "ui", "visError = «Ingresá nombre y apellido del visitante.» / «El DNI debe tener 7 u 8 dígitos.» …"),
            ]),
            ("sin actividad elegida", [
                ("ret", "vm", "ui", "visError «Seleccioná una actividad.»"),
            ]),
            ("actividad sin cupo", [
                ("ret", "vm", "ui", "visError «Sin cupo en {actividad}. El ingreso no puede realizarse.»"),
            ]),
            ("datos válidos y con cupo", [
                ("msg", "vm", "repo", "contarVisitante(dniFmt(dni), HOY)"),
                ("msg", "repo", "db", "SELECT COUNT(*) FROM visitante WHERE dni = ? AND fecha = ?"),
                ("ret", "db", "repo", "n"),
                ("ret", "repo", "vm", "n"),
                ("alt", [
                    ("n > 0 (ese DNI ya pagó hoy)", [
                        ("ret", "vm", "ui", "visError «Ya hay un pago diario registrado hoy con el DNI xx.xxx.xxx.»"),
                    ]),
                    ("n = 0", [
                        ("self", "vm", "visitantes = [Visitante(nombre, dni, actividad, HORA_ACTUAL, precio, HOY, medio)] + visitantes · stack = [E_VISITANTES]"),
                        ("msg", "vm", "repo", "guardar(estado)"),
                        ("msg", "repo", "db", "INSERT INTO visitante (nombre, dni, actividad, hora, monto, fecha, medio, orden)"),
                        ("ret", "db", "repo", "ok"),
                        ("ret", "vm", "ui", "vuelve a Visitantes · toast «Pago diario registrado · $precio» · el cupo del día baja en 1"),
                    ]),
                ]),
            ]),
        ]),
    ],
)
c.dibujar(os.path.join(DIR, "05_sec_pago_diario_visitante.png"))

# ------------------------------------------------------------------ (d) parámetros del club (v1.5)
d = Secuencia(
    "Secuencia — El administrador guarda los parámetros del club (CU-13) (v1.5)",
    "Perfil → «Configuración del club» · UiState.parametros se persiste con la foto completa en la tabla parametro (SQLite versión 4)",
    [("adm", "Administrativo", "actor"), ("ui", ":PantallaPerfil\n«Configuración del club»", "obj"), ("vm", ":ClubViewModel", "obj"),
     ("repo", ":ClubRepositorio", "obj"), ("db", ":ClubDbHelper\n(SQLite v4)", "obj")],
    [
        ("note", "repo", "Al abrir la app, ClubRepositorio.iniciar() → ClubDbHelper.leer() carga UiState.parametros (SELECT * FROM parametro ORDER BY orden). La semilla de la demo trae dos filas: desde hace un año gracia 10 / recargo $300 / interés 2,5 %; desde hace dos meses gracia 5 / $500 / 3 %. Vacía → Parametros.INICIALES (5 / 0 / 0)."),
        ("msg", "adm", "ui", "abre Perfil → «Configuración del club» (o la acción rápida «Parámetros del club» del Inicio)"),
        ("msg", "ui", "vm", "prepararParametros()  (v1.5)"),
        ("self", "vm", "p = parametros = estado.parametros.vigenteEn(HOY) · campos.pGracia / pRecargo / pInteres = p · paramError = null"),
        ("ret", "vm", "ui", "formulario con los valores vigentes · historialParametros (más nuevo primero)"),
        ("msg", "adm", "ui", "edita gracia (días), recargo ($) e interés mensual (%) y toca «Guardar parámetros»"),
        ("msg", "ui", "vm", "guardarParametros()  (v1.5)"),
        ("self", "vm", "gracia = pGracia.toIntOrNull() · recargo, interes = toDoubleOrNull() (coma o punto)"),
        ("alt", [
            ("gracia no entera o fuera de 0..60", [
                ("ret", "vm", "ui", "paramError «La gracia debe ser un número entero de 0 a 60 días.»"),
            ]),
            ("recargo no numérico, no finito o fuera de 0..1.000.000", [
                ("ret", "vm", "ui", "paramError «El recargo debe ser un importe de 0 a 1.000.000.»"),
            ]),
            ("interés no numérico o fuera de 0..100", [
                ("ret", "vm", "ui", "paramError «El interés mensual debe ser un porcentaje de 0 a 100.»"),
            ]),
            ("valores válidos", [
                ("self", "vm", "nuevo = Parametros(gracia, recargo, interes, vigenteDesde = HOY) · estado.parametros = [nuevo] + anteriores sin la fila de hoy (guardar dos veces el mismo día la reemplaza)"),
                ("msg", "vm", "repo", "set estado → ClubRepositorio.guardar(estado)"),
                ("msg", "repo", "db", "ClubDbHelper.guardar(datos): en la transacción, DELETE FROM parametro · INSERT INTO parametro (gracia_dias, recargo, interes_mensual, vigente_desde, orden) por cada fila"),
                ("ret", "db", "repo", "ok (base versión 4)"),
                ("self", "vm", "las SocioVista se recalculan con la lista nueva: vigenteEn(fecha) toma el más nuevo con vigenteDesde <= fecha; antes del primero guardado, gracia de la fila más antigua con recargo 0 e interés 0 (no retroactivo)"),
                ("ret", "vm", "ui", "historial con la fila nueva arriba · toast «Parámetros guardados · rigen desde hoy»"),
                ("note", "repo", "Recargo e interés no se aplican hacia atrás: cada día vencido devenga con la tasa que regía ese día y los días anteriores al primer parámetro guardado no devengan nada. Ej. de la demo (semilla con dos filas: hace un año gracia 10 / $300 / 2,5 %; hace dos meses gracia 5 / $500 / 3 %): Lucas, vencido hace 29 días, debe $12.848 = cuota $12.000 + recargo $500 + interés $348 (29 días al 3 %); Rocío Paz, vencida hace 3 días, está en gracia (quedan 2 días) y debe $12.536."),
            ]),
        ]),
    ],
)
d.dibujar(os.path.join(DIR, "06_sec_parametros_club.png"))
print("ok")
