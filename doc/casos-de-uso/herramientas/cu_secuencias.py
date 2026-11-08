# -*- coding: utf-8 -*-
"""Un diagrama de secuencia UML 2.0 por caso de uso (26). Fuente única: la usan dia_secuencias.py
(PNG) y generar_puml.py (PlantUML). Cada paso corresponde a una función real de ClubViewModel,
SocioVista, ClubRepositorio o ClubDbHelper."""
from secuencia import Secuencia

# ------------------------------------------------------------------ participantes
USR = ("usr", "Usuario del club", "actor")
SOC = ("soc", "Socio", "actor")
ADM = ("adm", "Administrativo", "actor")
ENT = ("ent", "Entrenadora", "actor")
NUT = ("nut", "Nutricionista", "actor")


def UI(clave, nombre):
    return (clave, f":{nombre}\n(Compose)", "obj")


VM = ("vm", ":ClubViewModel", "obj")
SV = ("sv", ":SocioVista", "obj")
REPO = ("repo", ":ClubRepositorio", "obj")
DB = ("db", ":ClubDbHelper\n(SQLite)", "obj")


def guardar(tablas):
    """Persistencia: el setter de estado llama a ClubRepositorio.guardar, que escribe solo si cambió algo."""
    return [
        ("msg", "vm", "repo", "guardar(estado)  (setter de estado)"),
        ("msg", "repo", "db", f"guardar(datos): transacción, DELETE + INSERT de las tablas · cambia: {tablas}"),
        ("ret", "db", "repo", "ok"),
    ]


SEC = []  # (id, slug, título, subtítulo, Secuencia)


def agregar(cu, slug, titulo, sub, parts, pasos, ancho=1400):
    SEC.append((cu, slug, titulo, sub, Secuencia(f"Secuencia — {cu}: {titulo}", sub, parts, pasos, ancho=ancho)))


# =================================================================== CU-01
agregar("CU-01", "sec_cu01_iniciar_sesion", "Iniciar sesión",
        "HU-01 · login() autentica contra la tabla usuario; el rol sale de la cuenta, sin selector de perfil",
        [USR, UI("ui", "PantallaLogin\nLoginActivity"), VM, REPO, DB, ("act", ":Activity del rol\n(RolActivity)", "obj")],
        [
            ("msg", "usr", "ui", "escribe usuario y contraseña (o toca una cuenta de prueba) y toca «Iniciar sesión»"),
            ("msg", "ui", "vm", "login()"),
            ("msg", "vm", "repo", "autenticar(email, pass)"),
            ("msg", "repo", "db", "autenticar(alias, clave): SELECT rol FROM usuario WHERE alias = ? AND clave = ?  (alias normalizado con norm())"),
            ("ret", "db", "repo", "Rol? (null si no coincide)"),
            ("ret", "repo", "vm", "rolCuenta"),
            ("self", "vm", "dadoDeBaja = rolCuenta == SOCIO && no existe el socio ID_SOCIO_APP (1) en estado.socios"),
            ("alt", [
                ("rolCuenta == null  o  dadoDeBaja", [
                    ("self", "vm", "loginError = true"),
                    ("ret", "vm", "ui", "mensajeLogin: «Completá usuario y contraseña.» (campos vacíos) / «Usuario o contraseña incorrectos.»"),
                ]),
                ("credenciales válidas", [
                    ("self", "vm", "rol = rolCuenta · stack = [primera pestaña del rol] · limpia email y pass"),
                    *guardar("sesion (rol)"),
                    ("ret", "vm", "ui", "estado.rol != null"),
                    ("msg", "ui", "act", "LaunchedEffect(rol): abrirActivityDelRol(rol)  (Socio / Admin / Entrenadora / Nutricionista)"),
                    ("ret", "act", "usr", "pantalla raíz del rol: Inicio (Socio, Administrativo, Entrenadora) o Agenda (Nutricionista)"),
                ]),
            ]),
        ])

# =================================================================== CU-02
agregar("CU-02", "sec_cu02_cerrar_sesion", "Cerrar sesión",
        "HU-01 · hoja de confirmación en Perfil; los datos persistidos no cambian, solo la sesión",
        [USR, UI("ui", "PantallaPerfil"), VM, REPO, DB, ("act", ":LoginActivity", "obj")],
        [
            ("msg", "usr", "ui", "toca «Cerrar sesión» en Perfil"),
            ("msg", "ui", "vm", "pedirCerrarSesion()"),
            ("self", "vm", "logoutSheet = true"),
            ("ret", "vm", "ui", "HojaCerrarSesion (nombre del usuario)"),
            ("alt", [
                ("confirma «Cerrar sesión»", [
                    ("msg", "ui", "vm", "cerrarSesion()"),
                    ("self", "vm", "rol = null · stack = [LOGIN] · logoutSheet = false · selId = null"),
                    *guardar("sesion (rol = NULL)"),
                    ("ret", "vm", "ui", "estado.rol == null"),
                    ("msg", "ui", "act", "RolActivity: volverAlLogin()"),
                    ("ret", "act", "usr", "PantallaLogin sin sesión"),
                ]),
                ("cancela", [
                    ("msg", "ui", "vm", "cancelarCerrarSesion()"),
                    ("ret", "vm", "ui", "logoutSheet = false · sigue en Perfil"),
                ]),
            ]),
        ])

# =================================================================== CU-03
agregar("CU-03", "sec_cu03_consultar_socios", "Consultar socios (búsqueda, filtros y ficha)",
        "HU-02 · HU-05 · HU-14 · solo lectura: el padrón sale del estado leído de SQLite al iniciar; las etiquetas, de SocioVista",
        [ADM, UI("ui", "PantallaSocios"), VM, SV, UI("fi", "PantallaFichaSocio")],
        [
            ("msg", "adm", "ui", "abre la pestaña Socios"),
            ("msg", "ui", "vm", "sociosFiltrados"),
            ("msg", "vm", "sv", "SocioVista(socio, estado.parametros) por cada socio"),
            ("ret", "sv", "vm", "badge · estadoCorto · contador · tono"),
            ("ret", "vm", "ui", "lista con N° de socio, DNI y estado de cada uno"),
            ("opt", "busca por nombre o DNI (con o sin puntos)", [
                ("msg", "adm", "ui", "escribe en «Buscar por DNI o nombre»"),
                ("msg", "ui", "vm", "campo { copy(busq = v) }"),
            ]),
            ("opt", "elige filtro", [
                ("msg", "adm", "ui", "toca Todos / Habilitados / En mora"),
                ("msg", "ui", "vm", "elegirFiltro(TODOS | HAB | MORA)"),
            ]),
            ("alt", [
                ("ningún socio coincide", [
                    ("ret", "vm", "ui", "texto «Ningún socio coincide con la búsqueda.»"),
                ]),
                ("hay coincidencias", [
                    ("msg", "adm", "ui", "toca un socio"),
                    ("msg", "ui", "vm", "abrirSocio(id)"),
                    ("self", "vm", "selId = id · ir(E_SOCIO)"),
                    ("ret", "vm", "fi", "seleccionado: ficha con aviso de habilitación (estadoCorto), cuota (contadorCorto, cuotaSubLabel, deudaDetalle), carnet, pagos, actividades e ingresos"),
                ]),
            ]),
        ])

# =================================================================== CU-04
agregar("CU-04", "sec_cu04_registrar_socio", "Registrar socio",
        "HU-02 · confirmarAlta() valida en orden nombre, apellido, DNI, correo y DNI único (COUNT en SQLite)",
        [ADM, UI("ui", "PantallaAlta\n(E_ALTA)"), VM, REPO, DB],
        [
            ("msg", "adm", "ui", "toca «Registrar socio» en el Inicio"),
            ("msg", "ui", "vm", "irAAlta()"),
            ("ret", "vm", "ui", "formulario limpio · plan Full · apto físico = true"),
            ("msg", "adm", "ui", "completa nombre, apellido, DNI, correo, plan, apto y observaciones y toca «Registrar socio»"),
            ("msg", "ui", "vm", "confirmarAlta()"),
            ("self", "vm", "errorFormularioSocio(): nombrePropio(nombre) ?: nombrePropio(apellido) ?: dni ?: email"),
            ("alt", [
                ("alguna validación de formato falla", [
                    ("ret", "vm", "ui", "altaError: «Ingresá el nombre.» / «El apellido solo puede contener letras y espacios.» / «El DNI debe tener 7 u 8 dígitos.» / «Ingresá el correo electrónico.» …"),
                ]),
                ("formato válido", [
                    ("msg", "vm", "repo", "contarDni(dni, null)"),
                    ("msg", "repo", "db", "SELECT COUNT(dni) FROM socio WHERE dni = ?"),
                    ("ret", "db", "repo", "n"),
                    ("ret", "repo", "vm", "n"),
                    ("alt", [
                        ("n > 0 (DNI ya registrado)", [
                            ("ret", "vm", "ui", "altaError «Ya existe un socio con ese DNI.»"),
                        ]),
                        ("n = 0", [
                            ("self", "vm", "Socio(id = máx + 1, nro = proximoNroSocio(), carnet hoy → hoy + 12 meses − 1 día, Cuota(mes, precio del plan, vence hoy + 10 días, PENDIENTE), FichaNutricional inicial)"),
                            *guardar("socio, carnet, cuota"),
                            ("ret", "vm", "ui", "stack = [E_SOCIOS, E_SOCIO] · toast «Socio N° xxxxx registrado · carnet y cuota inicial generados»"),
                        ]),
                    ]),
                ]),
            ]),
        ])

# =================================================================== CU-05
agregar("CU-05", "sec_cu05_editar_socio", "Editar socio",
        "HU-02 (CRUD) · mismo formulario y mismas validaciones que el alta; el DNI puede ser el del propio socio",
        [ADM, UI("ui", "PantallaAlta\n(E_EDITAR)"), VM, REPO, DB],
        [
            ("msg", "adm", "ui", "toca «Editar datos» en la ficha del socio"),
            ("msg", "ui", "vm", "irAEditar()"),
            ("self", "vm", "editandoId = id · precarga nombre (hasta el 1.er espacio), apellido (resto), DNI, correo, plan, apto y antecedentes"),
            ("ret", "vm", "ui", "formulario «Editar socio» con los datos cargados"),
            ("msg", "adm", "ui", "modifica los datos y toca «Guardar cambios»"),
            ("msg", "ui", "vm", "confirmarAlta()  (editando = true)"),
            ("self", "vm", "errorFormularioSocio(): nombre, apellido, DNI, correo"),
            ("alt", [
                ("formato inválido", [
                    ("ret", "vm", "ui", "altaError con el mensaje de Validaciones · sigue en «Editar socio»"),
                ]),
                ("formato válido", [
                    ("msg", "vm", "repo", "contarDni(dni, editandoId)"),
                    ("msg", "repo", "db", "SELECT COUNT(dni) FROM socio WHERE dni = ? AND id <> ?"),
                    ("ret", "db", "repo", "n"),
                    ("ret", "repo", "vm", "n"),
                    ("alt", [
                        ("n > 0 (lo usa otro socio)", [
                            ("ret", "vm", "ui", "altaError «Ya existe un socio con ese DNI.»"),
                        ]),
                        ("n = 0", [
                            ("self", "vm", "guardarEdicion(): actualizarSocio(id) cambia nombre, DNI, correo, plan, ficha y antecedentes; cuota, carnet e historial quedan"),
                            *guardar("socio"),
                            ("ret", "vm", "ui", "stack = [E_SOCIOS, E_SOCIO] · toast «Datos del socio actualizados»"),
                        ]),
                    ]),
                ]),
            ]),
        ])

# =================================================================== CU-06
agregar("CU-06", "sec_cu06_baja_socio", "Dar de baja socio",
        "HU-02 (CRUD) · hoja de confirmación; la baja quita al socio del padrón, de las clases y de las asistencias",
        [ADM, UI("ui", "PantallaFichaSocio"), VM, REPO, DB],
        [
            ("msg", "adm", "ui", "toca «Dar de baja» en la ficha"),
            ("msg", "ui", "vm", "pedirBajaSocio()"),
            ("self", "vm", "sheet = «Dar de baja» con Socio, DNI y N° de socio · «Esta acción no se puede deshacer.»"),
            ("ret", "vm", "ui", "HojaConfirmacion · CTA «Confirmar baja»"),
            ("alt", [
                ("confirma «Confirmar baja»", [
                    ("msg", "ui", "vm", "sheet.onConfirm()"),
                    ("self", "vm", "socios sin el id · acts: inscriptos − id · asistencia − id · stack = [E_SOCIOS] · selId = null · sheet = null"),
                    *guardar("socio (y por ON DELETE CASCADE: carnet, cuota, pago), actividad_inscripto, asistencia"),
                    ("ret", "vm", "ui", "vuelve a Socios · toast «{nombre} dado de baja»"),
                ]),
                ("cancela (cerrarSheet)", [
                    ("msg", "ui", "vm", "cerrarSheet()"),
                    ("ret", "vm", "ui", "el socio sigue en el padrón"),
                ]),
            ]),
        ])

# =================================================================== CU-07
agregar("CU-07", "sec_cu07_cobro_caja", "Registrar pago de cuota en caja",
        "HU-04 · pedirRegistrarPago() → hoja con totalAdeudado → pagarCuota(id, «Caja»); recargo e interés se deben desde el vencimiento, también en gracia",
        [ADM, UI("ui", "PantallaFichaSocio"), VM, SV, REPO, DB],
        [
            ("msg", "adm", "ui", "toca «Registrar pago en caja»"),
            ("msg", "ui", "vm", "pedirRegistrarPago()"),
            ("msg", "vm", "sv", "seleccionado.cuotaPagada"),
            ("alt", [
                ("cuota ya pagada", [
                    ("ret", "vm", "ui", "toast «La cuota de {mes} ya está pagada.» · no abre la hoja"),
                ]),
                ("cuota pendiente, en gracia o en mora", [
                    ("self", "sv", "totalAdeudado = cuota + recargo vigente hoy (una vez, si venció) + interés día a día con la tasa de cada día / 30"),
                    ("ret", "sv", "vm", "totalLabel"),
                    ("ret", "vm", "ui", "hoja «Registrar pago»: Socio · Cuota {mes} · Importe {total} · CTA «Cobrar {total}»"),
                    ("alt", [
                        ("cancela", [
                            ("msg", "ui", "vm", "cerrarSheet()"),
                        ]),
                        ("confirma «Cobrar»", [
                            ("msg", "ui", "vm", "sheet.onConfirm() → pagarCuota(id, «Caja»)"),
                            ("msg", "vm", "sv", "SocioVista(socio, estado.parametros).totalAdeudado"),
                            ("ret", "sv", "vm", "total"),
                            ("self", "vm", "cuota.estado = PAGADA · proximaVence = vence + 1 mes · PagoHistorial(mes, total, HOY, «Caja», «CD-2609-####») al frente del historial"),
                            *guardar("cuota, pago"),
                            ("ret", "vm", "ui", "cerrarSheet() · toast «Pago registrado · socio habilitado»"),
                        ]),
                    ]),
                ]),
            ]),
        ])

# =================================================================== CU-08
agregar("CU-08", "sec_cu08_carnet", "Emitir / renovar carnet",
        "HU-07 · el carnet siempre se emite por 12 meses desde hoy: hoy → hoy + 12 meses − 1 día",
        [ADM, UI("ui", "PantallaFichaSocio"), VM, REPO, DB],
        [
            ("msg", "adm", "ui", "toca «Renovar carnet» (vencido) o «Reemitir carnet» (vigente)"),
            ("msg", "ui", "vm", "pedirRenovarCarnet()"),
            ("self", "vm", "desde = HOY · hasta = HOY + 1 año − 1 día · título = seleccionado.carnetAccion"),
            ("ret", "vm", "ui", "hoja: Socio · Nueva vigencia {desde} → {hasta} · CTA «Emitir carnet»"),
            ("alt", [
                ("confirma «Emitir carnet»", [
                    ("msg", "ui", "vm", "sheet.onConfirm()"),
                    ("self", "vm", "actualizarSocio(id): carnetEmitido = desde · carnetVence = hasta"),
                    *guardar("carnet"),
                    ("ret", "vm", "ui", "cerrarSheet() · toast «Carnet emitido · vigente hasta {hasta}»"),
                ]),
                ("cancela", [
                    ("msg", "ui", "vm", "cerrarSheet()"),
                    ("ret", "vm", "ui", "el carnet no cambia"),
                ]),
            ]),
        ])

# =================================================================== CU-09
agregar("CU-09", "sec_cu09_validar_acceso", "Validar ingreso por DNI (control de acceso)",
        "HU-05 · la habilitación se deriva de la fecha y de la gracia vigente en SocioVista; los parámetros viven en el estado, no se consultan a SQLite en cada acceso",
        [ADM, UI("ui", "PantallaAcceso"), VM, SV],
        [
            ("msg", "adm", "ui", "escribe el DNI y toca «Validar» (o toca un DNI de prueba: ponerDniAcceso)"),
            ("msg", "ui", "vm", "validarAcceso()"),
            ("self", "vm", "Validaciones.dni(campos.dniAcceso)"),
            ("alt", [
                ("DNI inválido (vacío, con letras o distinto de 7–8 dígitos)", [
                    ("ret", "vm", "ui", "acceso = null · toast con el mensaje de validación"),
                ]),
                ("DNI válido", [
                    ("msg", "vm", "sv", "socios.firstOrNull { dni }  →  SocioVista(socio, estado.parametros)"),
                    ("alt", [
                        ("no hay socio con ese DNI", [
                            ("ret", "vm", "ui", "«No encontrado»: «No hay ningún socio con DNI xx.xxx.xxx. Podés registrarlo como visitante o dar de alta un socio.» · botón «Registrar socio»"),
                        ]),
                        ("hab y en gracia (0 < d <= gracia)", [
                            ("ret", "vm", "ui", "«Permitido» (naranja): «Cuota {mes} vencida · quedan N días de gracia. Debe $total en recepción.»"),
                        ]),
                        ("hab y cuota pagada", [
                            ("ret", "vm", "ui", "«Permitido»: «Carnet vigente y cuota al día.»"),
                        ]),
                        ("hab y cuota pendiente sin vencer", [
                            ("ret", "vm", "ui", "«Permitido»: «Carnet vigente · cuota {mes} pendiente, vence en N días.»"),
                        ]),
                        ("mora (d > gracia)", [
                            ("ret", "vm", "ui", "«Denegado» (rojo): «Cuota {mes} en mora: vencida hace N días · gracia de G días superada. Debe $total en recepción.»"),
                        ]),
                        ("carnet vencido, sin mora", [
                            ("ret", "vm", "ui", "«Denegado»: «Carnet vencido el dd/mm/aaaa. Debe renovarse.»"),
                        ]),
                    ]),
                ]),
            ]),
            ("opt", "hay socio: botones de la tarjeta de resultado", [
                ("msg", "adm", "ui", "«Ver ficha» (abrirSocio) · «Registrar ingreso» solo si es Permitido (CU-10)"),
            ]),
        ])

# =================================================================== CU-10
agregar("CU-10", "sec_cu10_registrar_ingreso", "Registrar ingreso del socio",
        "HU-05 · «extend» de CU-09: el botón «Registrar ingreso» solo aparece si el socio está habilitado y el ingreso aún no se registró",
        [ADM, UI("ui", "PantallaAcceso"), VM, REPO, DB],
        [
            ("note", "ui", "Precondición: validarAcceso() dejó acceso «Permitido» con registrado = false."),
            ("msg", "adm", "ui", "toca «Registrar ingreso»"),
            ("msg", "ui", "vm", "registrarIngreso()"),
            ("self", "vm", "id = acceso.socioId · ingresosHoy = [IngresoHoy(HORA_ACTUAL, nombre, «Permitido», ok)] + ingresosHoy · acceso.registrado = true"),
            ("self", "vm", "actualizarSocio(id): ingresos = [Ingreso(«dd/mm · hh:mm», «Permitido»)] + ingresos"),
            *guardar("ingreso_hoy, socio (ingresos_json)"),
            ("ret", "vm", "ui", "toast «Ingreso registrado · {nombre}» · desaparece el botón (registrado = true)"),
        ])

# =================================================================== CU-11
agregar("CU-11", "sec_cu11_pago_diario", "Registrar pago diario de no socio",
        "HU-03 · cupo del día (cupo − ocupados − visitantes de hoy) y un solo pago diario por DNI y fecha, consultado en SQLite",
        [ADM, UI("ui", "PantallaVisitanteNuevo"), VM, REPO, DB],
        [
            ("msg", "adm", "ui", "toca «Registrar visitante» (Inicio) o «+ Ingreso» (Visitantes)"),
            ("msg", "ui", "vm", "irAVisitanteNuevo()"),
            ("ret", "vm", "ui", "nombre y DNI vacíos · medio Efectivo · sin actividad"),
            ("msg", "adm", "ui", "escribe nombre y DNI, elige actividad y medio (Efectivo / Débito)"),
            ("msg", "ui", "vm", "elegirActividadVisitante(actividad)  /  elegirMedioVisitante(m)"),
            ("alt", [
                ("actividad sin cupo (libres <= 0)", [
                    ("ret", "vm", "ui", "visError «{actividad} no tiene cupo disponible hoy (x/y). El ingreso no puede realizarse.»"),
                ]),
                ("con cupo", [
                    ("ret", "vm", "ui", "visAct = actividad · visError = null · «N libres»"),
                ]),
            ]),
            ("msg", "adm", "ui", "toca «Registrar pago diario»"),
            ("msg", "ui", "vm", "confirmarVisitante()"),
            ("self", "vm", "Validaciones.nombre(vNombre, « del visitante») ?: Validaciones.dni(vDni)"),
            ("alt", [
                ("nombre o DNI inválidos", [
                    ("ret", "vm", "ui", "visError «Ingresá nombre y apellido del visitante.» / «El DNI debe tener 7 u 8 dígitos.» …"),
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
                        ("n > 0 (ya pagó hoy)", [
                            ("ret", "vm", "ui", "visError «Ya hay un pago diario registrado hoy con el DNI xx.xxx.xxx.»"),
                        ]),
                        ("n = 0", [
                            ("self", "vm", "visitantes = [Visitante(nombre, DNI con puntos, actividad, HORA_ACTUAL, precio, HOY, medio)] + visitantes · stack = [E_VISITANTES]"),
                            *guardar("visitante"),
                            ("ret", "vm", "ui", "vuelve a Visitantes · toast «Pago diario registrado · $precio»"),
                        ]),
                    ]),
                ]),
            ]),
        ])

# =================================================================== CU-12
agregar("CU-12", "sec_cu12_reportes", "Consultar reportes",
        "HU-14 · cuatro pestañas; «Vencen hoy» y «Esta semana» salen de un SELECT por rango de fechas en SQLite; «En mora» suma totalAdeudado",
        [ADM, UI("ui", "PantallaReportes"), VM, SV, REPO, DB],
        [
            ("msg", "adm", "ui", "abre Reportes y elige una pestaña"),
            ("msg", "ui", "vm", "elegirReportTab(HOY | VENCER | MORA | VISITANTES)"),
            ("alt", [
                ("Vencen hoy / Esta semana", [
                    ("msg", "vm", "repo", "idsCuotasPorVencer(desde, hasta)"),
                    ("msg", "repo", "db", "SELECT s.id FROM socio s JOIN cuota c (vigente = 1) WHERE c.estado = 'PENDIENTE' AND c.vence BETWEEN ? AND ? ORDER BY c.vence, s.nombre"),
                    ("ret", "db", "repo", "ids"),
                    ("ret", "repo", "vm", "ids → filasReporte ordenadas por vencimiento"),
                ]),
                ("En mora", [
                    ("msg", "vm", "sv", "enMora = socios.filter { mora }"),
                    ("ret", "sv", "vm", "totalAdeudado de cada uno"),
                    ("ret", "vm", "ui", "filas con total adeudado · «Rige: gracia … recargo … interés … desde dd/mm/aaaa»"),
                ]),
                ("Visitantes", [
                    ("msg", "vm", "repo", "visitantesPorFecha(fechaReporte)"),
                    ("msg", "repo", "db", "SELECT * FROM visitante WHERE fecha = ? ORDER BY orden"),
                    ("ret", "db", "repo", "pagos del día"),
                    ("ret", "repo", "vm", "pagosDiarios"),
                    ("opt", "navega de día", [
                        ("msg", "ui", "vm", "diaReporteAnterior() / diaReporteSiguiente() (solo si fechaReporte < HOY)"),
                    ]),
                ]),
            ]),
            ("alt", [
                ("la pestaña no tiene filas", [
                    ("ret", "vm", "ui", "«Sin registros en este reporte.» / «Sin pagos diarios el dd/mm/aaaa.» · Total $0"),
                ]),
                ("hay filas", [
                    ("ret", "vm", "ui", "filas + «Total {totalReporte}»; tocar una fila abre la ficha del socio (abrirSocio)"),
                ]),
            ]),
        ])

# =================================================================== CU-13
agregar("CU-13", "sec_cu13_parametros", "Configurar parámetros del club",
        "v1.5 · Perfil → «Configuración del club» (solo ADMIN) · UiState.parametros se persiste con la foto completa en la tabla parametro (SQLite versión 4)",
        [ADM, UI("ui", "PantallaPerfil\n«Configuración del club»"), VM, REPO, DB],
        [
            ("note", "repo", "Al abrir la app, ClubRepositorio.iniciar() → ClubDbHelper.leer() carga UiState.parametros. La semilla trae dos filas (desde hace un año: gracia 10 / $300 / 2,5 %; desde hace dos meses: 5 / $500 / 3 %)."),
            ("msg", "adm", "ui", "abre Perfil (avatar) o toca la acción rápida «Parámetros del club» del Inicio"),
            ("msg", "ui", "vm", "prepararParametros()"),
            ("self", "vm", "p = parametros (vigenteEn(HOY)) · campos.pGracia / pRecargo / pInteres = p · paramError = null"),
            ("ret", "vm", "ui", "formulario con los valores vigentes + historialParametros (más nuevo primero)"),
            ("msg", "adm", "ui", "edita gracia, recargo e interés y toca «Guardar parámetros»"),
            ("msg", "ui", "vm", "guardarParametros()"),
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
                    ("self", "vm", "parametros = [Parametros(gracia, recargo, interes, vigenteDesde = HOY)] + los anteriores sin la fila de hoy"),
                    ("msg", "vm", "repo", "guardar(estado)  (setter de estado)"),
                    ("msg", "repo", "db", "guardar(datos): en la transacción DELETE FROM parametro + INSERT por cada fila (gracia_dias, recargo, interes_mensual, vigente_desde, orden)"),
                    ("ret", "db", "repo", "ok"),
                    ("ret", "vm", "ui", "historial con la fila nueva (chip «Vigente») · toast «Parámetros guardados · rigen desde hoy»"),
                    ("note", "repo", "Las SocioVista se recalculan con la lista nueva: vigenteEn(fecha) toma el más nuevo con vigenteDesde <= fecha; antes del primero, gracia de la fila más antigua con recargo 0 e interés 0 (no retroactivo)."),
                ]),
            ]),
        ])

# =================================================================== CU-14
agregar("CU-14", "sec_cu14_pagar_app", "Pagar cuota desde la app",
        "HU-04 · el pago se simula (no hay pasarela); se cobra totalAdeudado y se emite comprobante",
        [SOC, UI("ui", "PantallaPagar"), VM, SV, REPO, DB],
        [
            ("msg", "soc", "ui", "toca «Pagar» (Inicio) o «Pagar cuota» (Estado de cuenta); el botón solo existe si la cuota está pendiente"),
            ("msg", "ui", "vm", "ir(S_PAGAR)"),
            ("msg", "vm", "sv", "yo.totalLabel · yo.deudaDetalle"),
            ("ret", "sv", "vm", "total (cuota, más recargo e interés si venció)"),
            ("ret", "vm", "ui", "«Total a pagar» con el detalle · medios: Tarjeta, Billetera, Efectivo en el club"),
            ("msg", "soc", "ui", "elige el medio y toca «Confirmar pago de {total}»"),
            ("msg", "ui", "vm", "elegirMedio(m) · confirmarPago()"),
            ("self", "vm", "pagarCuota(ID_SOCIO_APP, medio.etiqueta)"),
            ("alt", [
                ("la cuota ya estaba PAGADA", [
                    ("ret", "vm", "ui", "toast «La cuota de {mes} ya está pagada.» · no registra otro pago ni navega"),
                ]),
                ("cuota pendiente", [
                    ("self", "vm", "cuota PAGADA · proximaVence = vence + 1 mes · PagoHistorial(mes, total, HOY, medio, «CD-2609-####») · comprobante"),
                    *guardar("cuota, pago"),
                    ("ret", "vm", "ui", "stack: S_PAGAR se reemplaza por S_PAGO_OK · «Pago registrado» + comprobante #CD-2609-#### · «Volver al inicio»"),
                ]),
            ]),
        ])

# =================================================================== CU-15
agregar("CU-15", "sec_cu15_estado_cuenta", "Consultar estado de cuenta, deuda y contador de días",
        "HU-04 · solo lectura: todo sale de SocioVista(yo, parametros); el contador cambia de tono verde / naranja / rojo",
        [SOC, UI("ui", "PantallaSocioHome\nPantallaCuotas"), VM, SV],
        [
            ("msg", "soc", "ui", "abre Inicio o «Ver estado de cuenta →»"),
            ("msg", "ui", "vm", "yo"),
            ("msg", "vm", "sv", "SocioVista(socio 1, estado.parametros)"),
            ("self", "sv", "diasVencida · enGracia · mora · recargo · interés por día · totalAdeudado"),
            ("ret", "sv", "vm", "badge · contadorCorto · cuotaSubLabel · deudaDetalle · totalLabel"),
            ("ret", "vm", "ui", "aviso con badge, tarjeta de la cuota (cápsula contadorCorto, cuotaSubLabel, deudaDetalle) y total"),
            ("alt", [
                ("cuota pagada", [
                    ("ret", "ui", "soc", "cápsula «Pagada» · «Próxima cuota dd/mm/aaaa» · sin botón de pago"),
                ]),
                ("pendiente sin vencer", [
                    ("ret", "ui", "soc", "«Vence en N días» / «Vence hoy» (verde) · «Vencimiento dd/mm/aaaa» · botón «Pagar»"),
                ]),
                ("en gracia (0 < d <= gracia)", [
                    ("ret", "ui", "soc", "«Quedan N días de gracia» / «Queda 1 día de gracia» / «Último día de gracia» (naranja) · detalle de la deuda · «Pagar»"),
                ]),
                ("mora (d > gracia)", [
                    ("ret", "ui", "soc", "«Vencida hace N días» (rojo) · «Venció el … · gracia de G días superada» · detalle de la deuda · «Pagar»"),
                ]),
            ]),
            ("opt", "Estado de cuenta", [
                ("ret", "ui", "soc", "Historial: mes, «Pagada el … · medio · comprobante» e importe, o «Sin pagos registrados.»"),
            ]),
        ])

# =================================================================== CU-16
agregar("CU-16", "sec_cu16_carnet_digital", "Ver carnet digital",
        "HU-06 · tarjeta con formato de carnet (v1.5); solo lectura, los datos son los del socio de la cuenta",
        [SOC, UI("ui", "PantallaCarnet"), VM, SV],
        [
            ("msg", "soc", "ui", "toca la pestaña Carnet"),
            ("msg", "ui", "vm", "ir(S_CARNET)  (pestaña raíz: reemplaza la pila)"),
            ("msg", "ui", "vm", "yo"),
            ("msg", "vm", "sv", "SocioVista(socio 1, estado.parametros)"),
            ("ret", "sv", "vm", "carnetEstado · dniLabel · nro · plan · carnetEmitidoLabel · carnetVenceLabel · cuota"),
            ("ret", "vm", "ui", "tarjeta «Club Deportivo»: nombre, DNI, Socio N°, Plan · desde, foto, Vigencia emitido – vence"),
            ("alt", [
                ("carnet vencido", [
                    ("ret", "ui", "soc", "chip «Vencido» (rojo); debe renovarlo en recepción (CU-08)"),
                ]),
                ("vence en menos de 30 días", [
                    ("ret", "ui", "soc", "chip «Por vencer»"),
                ]),
                ("vigente", [
                    ("ret", "ui", "soc", "chip «Vigente»"),
                ]),
            ]),
            ("self", "ui", "pie de la tarjeta: «Cuota al día» / «en mora» / «en período de gracia» / «pendiente» · «Últimos ingresos»"),
            ("opt", "toca «QR de ingreso»", [
                ("msg", "soc", "ui", "toca «QR de ingreso»"),
                ("msg", "ui", "vm", "ir(S_QR)  → CU-26"),
            ]),
        ])

# =================================================================== CU-17
agregar("CU-17", "sec_cu17_rutina_socio", "Consultar rutina asignada",
        "HU-10 · solo lectura; el socio navega entre los días de su rutina",
        [SOC, UI("ui", "PantallaRutinaSocio"), VM],
        [
            ("msg", "soc", "ui", "toca la pestaña Rutina (o la tarjeta «Rutina · hoy» del Inicio)"),
            ("msg", "ui", "vm", "ir(S_RUTINA)"),
            ("msg", "ui", "vm", "diasDeRutina  (= yo.rutina?.dias)"),
            ("alt", [
                ("el socio no tiene rutina", [
                    ("ret", "vm", "ui", "«Todavía no tenés una rutina asignada. Tu profesor la va a cargar en tu próxima clase.»  (Inicio: «Sin rutina» · «Pedile a tu profe»)"),
                ]),
                ("tiene rutina", [
                    ("ret", "vm", "ui", "nombre · «Prof. {profesora}» · «actualizada dd/mm/aaaa» · pestañas de días"),
                    ("opt", "cambia de día", [
                        ("msg", "soc", "ui", "toca otro día"),
                        ("msg", "ui", "vm", "elegirDiaRutina(i)"),
                        ("ret", "vm", "ui", "diaDeRutina: ejercicios numerados con series×repeticiones y nota"),
                    ]),
                ]),
            ]),
        ])

# =================================================================== CU-18
agregar("CU-18", "sec_cu18_solicitar_turno", "Solicitar turno de nutrición",
        "HU-11 · tres días × seis horarios; los horarios tomados por otros socios aparecen deshabilitados",
        [SOC, UI("ui", "PantallaNutriSocio\nPantallaTurno"), VM, REPO, DB],
        [
            ("msg", "soc", "ui", "en Nutrición toca «Solicitar turno» (no tiene turno)"),
            ("msg", "ui", "vm", "ir(S_TURNO)"),
            ("msg", "ui", "vm", "slots"),
            ("ret", "vm", "ui", "días y horarios; ocupado = otro socio ya tiene esa fecha y hora"),
            ("msg", "soc", "ui", "toca un horario libre"),
            ("msg", "ui", "vm", "elegirSlot(key)"),
            ("msg", "soc", "ui", "toca «Confirmar turno»  (deshabilitado mientras no haya horario)"),
            ("msg", "ui", "vm", "confirmarTurno()"),
            ("alt", [
                ("slot == null", [
                    ("ret", "vm", "ui", "toast «Elegí un horario disponible»"),
                ]),
                ("hay horario elegido", [
                    ("self", "vm", "actualizarSocio(1): turno = Turno(fecha, hora) · volver()"),
                    *guardar("socio (turno_fecha, turno_hora)"),
                    ("ret", "vm", "ui", "vuelve a Nutrición · toast «Turno confirmado · dd/mm hh:mm»"),
                ]),
            ]),
        ])

# =================================================================== CU-19
agregar("CU-19", "sec_cu19_cancelar_turno", "Cancelar turno de nutrición",
        "HU-11 · hoja de confirmación; libera el horario para otros socios",
        [SOC, UI("ui", "PantallaNutriSocio"), VM, REPO, DB],
        [
            ("msg", "soc", "ui", "toca «Cancelar turno»"),
            ("msg", "ui", "vm", "pedirCancelarTurno()"),
            ("ret", "vm", "ui", "hoja «Cancelar turno»: «Vas a liberar tu turno con la nutricionista…» · Turno {fecha · hora} · CTA «Sí, cancelar»"),
            ("alt", [
                ("confirma «Sí, cancelar»", [
                    ("msg", "ui", "vm", "sheet.onConfirm()"),
                    ("self", "vm", "actualizarSocio(1): turno = null · cerrarSheet()"),
                    *guardar("socio (turno_fecha, turno_hora = NULL)"),
                    ("ret", "vm", "ui", "toast «Turno cancelado» · aparece «Solicitar turno»"),
                ]),
                ("cierra la hoja", [
                    ("msg", "ui", "vm", "cerrarSheet()"),
                    ("ret", "vm", "ui", "el turno se mantiene"),
                ]),
            ]),
        ])

# =================================================================== CU-20
agregar("CU-20", "sec_cu20_info_nutricional", "Consultar información nutricional propia",
        "HU-13 · solo lectura: peso, altura, IMC, objetivo, antecedentes, turno e historial de consultas",
        [SOC, UI("ui", "PantallaNutriSocio"), VM, SV],
        [
            ("msg", "soc", "ui", "toca la pestaña Nutrición"),
            ("msg", "ui", "vm", "ir(S_NUTRI)  ·  yo"),
            ("msg", "vm", "sv", "SocioVista(socio 1, estado.parametros)"),
            ("self", "sv", "imc = peso / altura² (o «—» si no hay altura) · pesoLabel · alturaLabel · turnoLabel · consultas"),
            ("ret", "sv", "vm", "pesoLabel · alturaLabel · imc · turnoSub · consultas"),
            ("ret", "vm", "ui", "«Próximo turno» (o «Sin turno asignado» + «Solicitar turno») · Mi ficha: Peso, Altura, IMC · Objetivo · Antecedentes"),
            ("alt", [
                ("hay consultas", [
                    ("ret", "vm", "ui", "Historial de consultas: fecha, peso en kg y observaciones"),
                ]),
                ("sin consultas", [
                    ("ret", "vm", "ui", "«Sin consultas registradas.»"),
                ]),
            ]),
        ])

# =================================================================== CU-21
agregar("CU-21", "sec_cu21_presencia", "Registrar presencia propia",
        "HU-08 · el botón «Marcar presencia» desaparece una vez marcada; profePresente se guarda en la tabla sesion",
        [ENT, UI("ui", "PantallaProfesorHome"), VM, REPO, DB],
        [
            ("alt", [
                ("profePresente == true", [
                    ("ret", "ui", "ent", "se muestra «Presente · 09:41» y no hay botón"),
                ]),
                ("profePresente == false", [
                    ("ret", "ui", "ent", "se muestra «Sin marcar» y el botón «Marcar presencia»"),
                    ("msg", "ent", "ui", "toca «Marcar presencia»"),
                    ("msg", "ui", "vm", "marcarPresencia()"),
                    ("self", "vm", "profePresente = true"),
                    *guardar("sesion (profe_presente)"),
                    ("ret", "vm", "ui", "toast «Presencia registrada · hh:mm» (HORA_ACTUAL)"),
                ]),
            ]),
        ])

# =================================================================== CU-22
agregar("CU-22", "sec_cu22_asistencia", "Registrar asistencia de clase",
        "HU-08 · cada clase conserva su propia lista de presentes; solo las clases que dicta la entrenadora (Musculación 18:00, Funcional 19:30)",
        [ENT, UI("ui", "PantallaProfesorHome\nPantallaAsistencia"), VM, REPO, DB],
        [
            ("msg", "ent", "ui", "toca una clase en «Mis clases de hoy»"),
            ("msg", "ui", "vm", "abrirAsistencia(claseId)"),
            ("self", "vm", "claseId = id · ir(P_ASISTENCIA)"),
            ("ret", "vm", "ui", "filasAsistencia: inscriptos de la clase con su marca de presente"),
            ("opt", "cambia de clase", [
                ("msg", "ent", "ui", "toca la otra clase"),
                ("msg", "ui", "vm", "elegirClase(id)"),
            ]),
            ("msg", "ent", "ui", "toca a cada socio para alternar presente / ausente"),
            ("msg", "ui", "vm", "alternarAsistencia(socioId)"),
            ("self", "vm", "asistencia[clase] = lista con el socio agregado o quitado"),
            *guardar("asistencia_clase, asistencia"),
            ("msg", "ent", "ui", "toca «Guardar asistencia»"),
            ("msg", "ui", "vm", "guardarAsistencia()"),
            ("self", "vm", "presentes = filasAsistencia.count { presente }"),
            ("ret", "vm", "ui", "toast «Asistencia guardada · {clase} · n/m presentes» · ir(P_HOME)"),
        ])

# =================================================================== CU-23
agregar("CU-23", "sec_cu23_rutina", "Crear y asignar rutina",
        "HU-09 · el editor se bloquea si el socio no está habilitado o no tiene apto físico; los errores del ejercicio salen como toast",
        [ENT, UI("ui", "PantallaRutinasProfesor\nPantallaRutinaEdit"), VM, SV, REPO, DB],
        [
            ("msg", "ent", "ui", "en Rutinas toca un socio («Crear» o «Ver»)"),
            ("msg", "ui", "vm", "abrirRutina(id)"),
            ("self", "vm", "editEj = rutina del socio o tres días vacíos (Día 1, 2 y 3) · editDia = 0 · ir(P_RUTINA_EDIT)"),
            ("msg", "ui", "sv", "seleccionado.bloqueaRutina  (= !(hab && ficha))"),
            ("alt", [
                ("bloqueado", [
                    ("ret", "sv", "ui", "bloqueoMotivo: «cuota en mora» / «carnet vencido» / «falta la ficha médica»"),
                    ("ret", "ui", "ent", "«No se puede asignar rutina: {motivo}. El socio debe regularizar la situación en recepción.» (sin formulario)"),
                ]),
                ("habilitado y con apto físico", [
                    ("msg", "ent", "ui", "elige el día, escribe ejercicio, series y repeticiones y toca «+ Agregar ejercicio»"),
                    ("msg", "ui", "vm", "elegirDiaEdicion(i) · agregarEjercicio()"),
                    ("alt", [
                        ("nombre vacío, o series (1..20) / repeticiones (1..100) inválidas", [
                            ("ret", "vm", "ui", "toast «Ingresá el nombre del ejercicio» / «Las series deben estar entre 1 y 20.» / «Las repeticiones deben estar entre 1 y 100.»"),
                        ]),
                        ("datos válidos", [
                            ("self", "vm", "editEj[día].ejercicios += Ejercicio(nombre, series ?: 3, reps ?: 12) · limpia el formulario"),
                        ]),
                    ]),
                    ("opt", "quita un ejercicio", [
                        ("msg", "ui", "vm", "quitarEjercicio(indice)"),
                    ]),
                    ("msg", "ent", "ui", "toca «Guardar y asignar rutina»"),
                    ("msg", "ui", "vm", "guardarRutina()"),
                    ("alt", [
                        ("ningún día con ejercicios", [
                            ("ret", "vm", "ui", "toast «Agregá al menos un ejercicio»"),
                        ]),
                        ("al menos un día con ejercicios", [
                            ("self", "vm", "actualizarSocio(id): rutina = Rutina(nombre previo o «Rutina personalizada», profesora, HOY, días con ejercicios)"),
                            *guardar("socio (rutina_json)"),
                            ("ret", "vm", "ui", "stack = [P_RUTINAS] · toast «Rutina asignada a {nombre} · visible en su app»"),
                        ]),
                    ]),
                ]),
            ]),
        ])

# =================================================================== CU-24
agregar("CU-24", "sec_cu24_agenda", "Consultar agenda de turnos",
        "HU-11 · solo lectura: los turnos de los socios agrupados por día (hoy, mañana y dentro de tres días) y ordenados por hora",
        [NUT, UI("ui", "PantallaAgenda"), VM, UI("fi", "PantallaFichaNutricional")],
        [
            ("msg", "nut", "ui", "abre la pestaña Agenda (pantalla raíz del rol)"),
            ("msg", "ui", "vm", "agenda"),
            ("self", "vm", "por cada día de diasDeAgenda: socios con turno ese día, ordenados por hora; los días sin turnos se omiten"),
            ("alt", [
                ("no hay turnos", [
                    ("ret", "vm", "ui", "«No tenés turnos agendados.»"),
                ]),
                ("hay turnos", [
                    ("ret", "vm", "ui", "grupos «Hoy · {día}» y fechas siguientes: «N turnos», hora y nombre del socio"),
                    ("opt", "toca un turno", [
                        ("msg", "nut", "ui", "toca el turno"),
                        ("msg", "ui", "vm", "abrirFicha(id)  → CU-25"),
                        ("ret", "vm", "fi", "N_FICHA con los campos de consulta vacíos"),
                    ]),
                ]),
            ]),
        ])

# =================================================================== CU-25
agregar("CU-25", "sec_cu25_consulta", "Registrar consulta nutricional",
        "HU-12 · peso obligatorio (20–300 kg) y altura opcional (0,50–2,50 m); la consulta de hoy consume el turno de hoy",
        [NUT, UI("ui", "PantallaFichas\nPantallaFichaNutricional"), VM, REPO, DB],
        [
            ("msg", "nut", "ui", "elige un socio en Fichas (o desde la Agenda)"),
            ("msg", "ui", "vm", "abrirFicha(id)"),
            ("self", "vm", "selId = id · limpia nPeso, nAltura y nObs · ir(N_FICHA)"),
            ("ret", "vm", "ui", "ficha: peso, altura, IMC, objetivo, antecedentes e historial"),
            ("msg", "nut", "ui", "completa peso, altura y observaciones y toca «Guardar en historial»"),
            ("msg", "ui", "vm", "guardarConsulta()"),
            ("self", "vm", "Validaciones.peso(nPeso) ?: Validaciones.altura(nAltura)"),
            ("alt", [
                ("peso o altura inválidos", [
                    ("ret", "vm", "ui", "toast «Ingresá el peso.» / «El peso debe ser un número (ej.: 62,4).» / «El peso debe estar entre 20 y 300 kg.» / «La altura debe estar entre 0,50 y 2,50 m.» …"),
                ]),
                ("datos válidos", [
                    ("self", "vm", "nut.peso = peso · altura (si se cargó) · consultas = [Consulta(HOY, peso, obs o «Control de rutina.»)] + consultas · turno = null si era de hoy"),
                    *guardar("socio (nut_peso, nut_altura, consultas_json, turno)"),
                    ("ret", "vm", "ui", "formulario limpio · toast «Consulta guardada en el historial»"),
                ]),
            ]),
        ])

# =================================================================== CU-26
agregar("CU-26", "sec_cu26_qr_ingreso", "Mostrar QR de ingreso",
        "HU-06 · v1.5 · «extend» de CU-16: el QR se genera en el dispositivo (ZXing core, sin cámara) con el texto SocioVista.qrCarnet",
        [SOC, UI("ui", "PantallaCarnet"), VM, SV, UI("qr", "PantallaQrIngreso\nS_QR")],
        [
            ("msg", "soc", "ui", "toca «QR de ingreso» en el carnet digital"),
            ("msg", "ui", "vm", "ir(Screen.S_QR)  (no es pestaña: se apila sobre el carnet)"),
            ("ret", "vm", "qr", "pantalla «QR de ingreso» con flecha para volver"),
            ("msg", "qr", "vm", "yo"),
            ("msg", "vm", "sv", "SocioVista(socio 1, estado.parametros)"),
            ("self", "sv", "qrCarnet = «CLUB DEPORTIVO · CARNET DE SOCIO» + N° de socio, Socio, DNI, Plan, Carnet emitido, Carnet vence, Estado (badge)"),
            ("ret", "sv", "vm", "qrCarnet · estadoCorto · badge · carnetEstadoLower"),
            ("ret", "vm", "qr", "nombre en mayúsculas, N° · DNI · Plan, cápsula estadoCorto, CodigoQr(qrCarnet), «Carnet vigente · vence dd/mm/aaaa» y badge"),
            ("alt", [
                ("carnet vencido o socio en mora", [
                    ("ret", "qr", "soc", "la cápsula y el campo «Estado:» del QR lo indican; el QR no habilita por sí mismo (lo decide CU-09)"),
                ]),
                ("habilitado", [
                    ("ret", "qr", "soc", "«Estado: Habilitado · …» dentro del QR"),
                ]),
            ]),
            ("msg", "soc", "qr", "toca la flecha o el botón atrás"),
            ("msg", "qr", "vm", "volver()"),
            ("ret", "vm", "ui", "stack sin S_QR: vuelve al carnet"),
        ], ancho=1500)
