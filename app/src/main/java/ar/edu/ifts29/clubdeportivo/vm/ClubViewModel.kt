package ar.edu.ifts29.clubdeportivo.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.ifts29.clubdeportivo.data.Acceso
import ar.edu.ifts29.clubdeportivo.data.Actividad
import ar.edu.ifts29.clubdeportivo.data.Campos
import ar.edu.ifts29.clubdeportivo.data.Consulta
import ar.edu.ifts29.clubdeportivo.data.Cuota
import ar.edu.ifts29.clubdeportivo.data.DemoData
import ar.edu.ifts29.clubdeportivo.data.DiaRutina
import ar.edu.ifts29.clubdeportivo.data.Ejercicio
import ar.edu.ifts29.clubdeportivo.data.EstadoCuota
import ar.edu.ifts29.clubdeportivo.data.Fecha
import ar.edu.ifts29.clubdeportivo.data.FichaNutricional
import ar.edu.ifts29.clubdeportivo.data.FiltroSocios
import ar.edu.ifts29.clubdeportivo.data.HOY
import ar.edu.ifts29.clubdeportivo.data.HORA_ACTUAL
import ar.edu.ifts29.clubdeportivo.data.Ingreso
import ar.edu.ifts29.clubdeportivo.data.IngresoHoy
import ar.edu.ifts29.clubdeportivo.data.MedioPago
import ar.edu.ifts29.clubdeportivo.data.MedioVisitante
import ar.edu.ifts29.clubdeportivo.data.PagoHistorial
import ar.edu.ifts29.clubdeportivo.data.ReportTab
import ar.edu.ifts29.clubdeportivo.data.Rol
import ar.edu.ifts29.clubdeportivo.data.Rutina
import ar.edu.ifts29.clubdeportivo.data.Screen
import ar.edu.ifts29.clubdeportivo.data.SheetData
import ar.edu.ifts29.clubdeportivo.data.Socio
import ar.edu.ifts29.clubdeportivo.data.Turno
import ar.edu.ifts29.clubdeportivo.data.Usuario
import ar.edu.ifts29.clubdeportivo.data.Validaciones
import ar.edu.ifts29.clubdeportivo.data.Visitante
import ar.edu.ifts29.clubdeportivo.data.daysTo
import ar.edu.ifts29.clubdeportivo.data.dniFmt
import ar.edu.ifts29.clubdeportivo.data.fecha
import ar.edu.ifts29.clubdeportivo.data.fmt
import ar.edu.ifts29.clubdeportivo.data.fmtCorto
import ar.edu.ifts29.clubdeportivo.data.fmtIso
import ar.edu.ifts29.clubdeportivo.data.fmtLargo
import ar.edu.ifts29.clubdeportivo.data.masAnios
import ar.edu.ifts29.clubdeportivo.data.masDias
import ar.edu.ifts29.clubdeportivo.data.masMeses
import ar.edu.ifts29.clubdeportivo.data.money
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Todo el estado mutable de la app, en una sola pieza inmutable. */
data class UiState(
    val rol: Rol? = null,
    val stack: List<Screen> = listOf(Screen.LOGIN),
    val selId: Int? = null,
    val campos: Campos = Campos(),
    val loginError: Boolean = false,
    val socios: List<Socio> = DemoData.socios,
    val acts: List<Actividad> = DemoData.actividades,
    val visitantes: List<Visitante> = DemoData.visitantesIniciales,
    val ingresosHoy: List<IngresoHoy> = DemoData.ingresosHoyIniciales,
    val acceso: Acceso? = null,
    val medio: MedioPago = MedioPago.TARJETA,
    val diaIdx: Int = 1,
    val editDia: Int = 0,
    val editEj: List<DiaRutina>? = null,
    val slot: String? = null,
    val filtro: FiltroSocios = FiltroSocios.TODOS,
    val reportTab: ReportTab = ReportTab.HOY,
    /** Día que muestra la pestaña Visitantes de Reportes; arranca en hoy. */
    val fechaReporte: Fecha = HOY,
    val claseId: String = "musc",
    val asistencia: Map<String, List<Int>> = mapOf("musc" to listOf(4, 7), "func" to emptyList()),
    val profePresente: Boolean = false,
    val toast: String? = null,
    val sheet: SheetData? = null,
    val logoutSheet: Boolean = false,
    val apto: Boolean = true,
    val planIdx: Int = 1,
    val altaError: String? = null,
    val visError: String? = null,
    val visAct: String? = null,
    val visMedio: MedioVisitante = MedioVisitante.EFECTIVO,
    val comprobante: String? = null,
    /** Socio que se está editando; `null` cuando el formulario es un alta. */
    val editandoId: Int? = null,
)

/** Socio vinculado a la cuenta de la app del socio (Andrés, en los datos de demo). */
const val ID_SOCIO_APP = 1

/** Una actividad con su cupo del día: los inscriptos de base más los visitantes de hoy. */
data class ActividadVista(val act: Actividad, val visitantesHoy: Int = 0) {
    val id get() = act.id
    val nombre get() = act.nombre
    val horario get() = act.horario
    val ocupadosHoy = act.ocupados + visitantesHoy
    val libres = act.cupo - ocupadosHoy
    val sinCupo = libres <= 0
    val cupoJusto = libres in 1..3
    val cupoLabel = if (libres > 0) "$libres libres" else "Sin cupo"
    val ocupacion = ocupadosHoy.toFloat() / act.cupo
    val precioLabel = money(act.precio)
}

data class ClaseVista(
    val act: Actividad,
    val presentes: Int,
    val inscriptos: Int,
)

data class SlotHora(val hora: String, val key: String, val ocupado: Boolean, val elegido: Boolean)
data class GrupoSlots(val dia: String, val horas: List<SlotHora>)
data class GrupoAgenda(val dia: String, val esHoy: Boolean, val turnos: List<SocioVista>)

/**
 * Lógica de la app, portada del componente único del prototipo. La navegación usa la
 * misma pila que el original: las pantallas raíz (las de la barra inferior) reemplazan
 * la pila y el resto se apila encima, con un header con flecha para volver.
 */
class ClubViewModel : ViewModel() {

    private var estadoActual by mutableStateOf(estadoInicial())

    /** Cada cambio se copia a [ClubRepositorio] para que la próxima Activity lo encuentre. */
    var estado: UiState
        get() = estadoActual
        private set(valor) {
            estadoActual = valor
            ClubRepositorio.guardar(valor)
        }

    private var toastJob: Job? = null
    private val proximoIdSocio get() = estado.socios.maxOf { it.id } + 1

    private fun estadoInicial(): UiState {
        val d = ClubRepositorio.datos
        return UiState(
            rol = d.rol,
            stack = listOf(d.rol?.let { DemoData.tabs.getValue(it).first().first } ?: Screen.LOGIN),
            socios = d.socios,
            acts = d.acts,
            visitantes = d.visitantes,
            ingresosHoy = d.ingresosHoy,
            asistencia = d.asistencia,
            profePresente = d.profePresente,
        )
    }

    // ---------------------------------------------------------------- navegación

    val pantalla: Screen get() = estado.stack.last()

    val esStaff: Boolean get() = estado.rol == Rol.EMPLEADO || estado.rol == Rol.ADMIN

    val usuario: Usuario get() = DemoData.usuarios.getValue(estado.rol ?: Rol.SOCIO)

    val titulo: String? get() = DemoData.titulos[pantalla]

    val muestraHeader: Boolean
        get() = (titulo != null && pantalla != Screen.PERFIL) || (pantalla == Screen.PERFIL && esStaff)

    val muestraNav: Boolean
        get() = estado.rol != null && pantalla != Screen.LOGIN && pantalla != Screen.S_PAGO_OK

    val tabs: List<Pair<Screen, String>>
        get() = estado.rol?.let { DemoData.tabs.getValue(it) } ?: emptyList()

    /** La raíz activa: la que se marca en la barra inferior. */
    val tabActiva: Screen get() = estado.stack.first()

    val hoyLabel: String get() = HOY.fmtLargo().replaceFirstChar { it.uppercase() }

    /**
     * Las pantallas raíz del rol activo reemplazan la pila; el resto se apila.
     *
     * El criterio es por rol y no global a propósito: Perfil es una pestaña para el
     * socio, el profesor y el nutricionista, pero para el administrativo se abre desde
     * el avatar y tiene que poder volver a donde estaba.
     */
    fun ir(scr: Screen) {
        val esRaiz = tabs.any { it.first == scr }
        estado = if (esRaiz) {
            estado.copy(stack = listOf(scr))
        } else {
            estado.copy(stack = estado.stack + scr)
        }
    }

    fun volver() {
        if (estado.stack.size > 1) estado = estado.copy(stack = estado.stack.dropLast(1))
    }

    /** Hay algo a lo que volver: usado por el botón atrás del sistema. */
    val puedeVolver: Boolean get() = estado.stack.size > 1

    fun toast(msg: String) {
        toastJob?.cancel()
        estado = estado.copy(toast = msg)
        toastJob = viewModelScope.launch {
            delay(2600)
            estado = estado.copy(toast = null)
        }
    }

    fun cerrarSheet() {
        estado = estado.copy(sheet = null)
    }

    // ---------------------------------------------------------------- sesión (HU-01)

    fun campo(update: Campos.() -> Campos) {
        estado = estado.copy(
            campos = estado.campos.update(),
            loginError = false,
            altaError = null,
            visError = null,
        )
    }

    fun login() {
        val rolCuenta = ClubRepositorio.autenticar(estado.campos.email, estado.campos.pass)
        // Un socio dado de baja (CRUD) pierde el acceso a la app.
        val dadoDeBaja = rolCuenta == Rol.SOCIO && estado.socios.none { it.id == ID_SOCIO_APP }
        if (rolCuenta != null && !dadoDeBaja) {
            val rol = rolCuenta
            estado = estado.copy(
                rol = rol,
                stack = listOf(DemoData.tabs.getValue(rol).first().first),
                loginError = false,
                campos = estado.campos.copy(email = "", pass = ""),
            )
        } else {
            estado = estado.copy(loginError = true)
        }
    }

    /** Datos incompletos o credenciales inválidas (HU-01, escenario 2). */
    val mensajeLogin: String
        get() = if (estado.campos.email.isBlank() || estado.campos.pass.isBlank()) {
            "Completá usuario y contraseña."
        } else {
            "Usuario o contraseña incorrectos."
        }

    fun pedirCerrarSesion() {
        estado = estado.copy(logoutSheet = true)
    }

    fun cancelarCerrarSesion() {
        estado = estado.copy(logoutSheet = false)
    }

    /** Vuelve al login y descarta la sesión; los datos de demo siguen como estaban. */
    fun cerrarSesion() {
        estado = estado.copy(rol = null, stack = listOf(Screen.LOGIN), logoutSheet = false, selId = null)
    }

    // ---------------------------------------------------------------- vistas derivadas

    val socios: List<SocioVista> get() = estado.socios.map { SocioVista(it) }

    /** El socio logueado. En el prototipo es siempre el primero de la lista. */
    val yo: SocioVista get() = socios.firstOrNull { it.id == ID_SOCIO_APP } ?: socios.first()

    val seleccionado: SocioVista
        get() = socios.firstOrNull { it.id == estado.selId } ?: yo

    val sociosFiltrados: List<SocioVista>
        get() {
            val q = estado.campos.busq.trim().lowercase()
            return socios
                .filter {
                    q.isEmpty() ||
                        it.nombre.lowercase().contains(q) ||
                        it.socio.dni.toString().contains(q.replace(".", ""))
                }
                .filter {
                    when (estado.filtro) {
                        FiltroSocios.TODOS -> true
                        FiltroSocios.MORA -> it.mora
                        FiltroSocios.HAB -> it.hab
                    }
                }
        }

    val actividades: List<ActividadVista>
        get() {
            val deHoy = visitantesDeHoy
            return estado.acts.map { a -> ActividadVista(a, deHoy.count { it.actividad == a.nombre }) }
        }

    val actividadElegida: ActividadVista?
        get() = actividades.firstOrNull { it.id == estado.visAct }

    /**
     * Cuotas pendientes que vencen entre [desde] y [hasta], ordenadas por vencimiento (HU-14).
     * Con la base abierta el filtro sale del `SELECT` del listado diario; sin base, de la
     * copia en memoria.
     */
    private fun pendientesEntre(desde: Fecha, hasta: Fecha): List<SocioVista> {
        val ids = ClubRepositorio.idsCuotasPorVencer(desde.fmtIso(), hasta.fmtIso())
        val lista = if (ids == null) {
            socios.filter { it.cuota.estado == EstadoCuota.PENDIENTE && it.cuota.vence in desde..hasta }
        } else {
            val pedidas = ids.toSet()
            socios.filter { it.id in pedidas }
        }
        return lista.sortedBy { daysTo(it.cuota.vence) }
    }

    /** Socios cuya cuota vence en la fecha (consigna, punto 5). */
    val vencenHoy: List<SocioVista> get() = pendientesEntre(HOY, HOY)

    /** Cuotas que vencen de mañana a 7 días: la semana, sin las de hoy. */
    val porVencer: List<SocioVista> get() = pendientesEntre(HOY.masDias(1), HOY.masDias(7))

    val enMora: List<SocioVista> get() = socios.filter { it.mora }

    val visitantesDeHoy: List<Visitante> get() = estado.visitantes.filter { it.fecha == HOY }

    /** Pagos diarios de no socios del día elegido en Reportes. */
    val pagosDiarios: List<Visitante>
        get() = ClubRepositorio.visitantesPorFecha(estado.fechaReporte)
            ?: estado.visitantes.filter { it.fecha == estado.fechaReporte }

    val totalPagosDiarios: Int get() = pagosDiarios.sumOf { it.monto }

    val totalPagosHoy: Int get() = visitantesDeHoy.sumOf { it.monto }

    val filasReporte: List<SocioVista>
        get() = when (estado.reportTab) {
            ReportTab.HOY -> vencenHoy
            ReportTab.VENCER -> porVencer
            ReportTab.MORA -> enMora
            ReportTab.VISITANTES -> emptyList()
        }

    val tituloReporte: String
        get() = when (estado.reportTab) {
            ReportTab.HOY -> "Cuotas que vencen hoy · ${HOY.fmtCorto()}"
            ReportTab.VENCER -> "Cuotas que vencen en 7 días"
            ReportTab.MORA -> "Socios en mora · acceso bloqueado"
            ReportTab.VISITANTES -> "Pagos diarios de no socios"
        }

    val totalReporte: String
        get() = money(
            if (estado.reportTab == ReportTab.VISITANTES) totalPagosDiarios else filasReporte.sumOf { it.cuota.monto },
        )

    val puedeAvanzarDia: Boolean get() = estado.fechaReporte < HOY

    val fechaReporteLabel: String get() = estado.fechaReporte.fmtLargo().replaceFirstChar { it.uppercase() }

    val fechaReporteSub: String
        get() = when (daysTo(estado.fechaReporte)) {
            0 -> "Hoy"
            -1 -> "Ayer"
            else -> estado.fechaReporte.fmtCorto()
        }

    fun diaReporteAnterior() {
        estado = estado.copy(fechaReporte = estado.fechaReporte.masDias(-1))
    }

    fun diaReporteSiguiente() {
        if (puedeAvanzarDia) estado = estado.copy(fechaReporte = estado.fechaReporte.masDias(1))
    }

    val ingresosDeHoy: Int
        get() = estado.ingresosHoy.count { it.ok } + visitantesDeHoy.size

    /** Las clases de la entrenadora logueada. */
    val clases: List<ClaseVista>
        get() = estado.acts
            .filter { it.profesor == DemoData.PROFESORA }
            .map {
                ClaseVista(
                    act = it,
                    presentes = (estado.asistencia[it.id] ?: emptyList()).size,
                    inscriptos = it.inscriptos.size,
                )
            }

    val claseSeleccionada: ClaseVista get() = clases.firstOrNull { it.act.id == estado.claseId } ?: clases.first()

    /** Los inscriptos de la clase activa, con su marca de presente. */
    val filasAsistencia: List<Pair<SocioVista, Boolean>>
        get() {
            val clase = claseSeleccionada.act
            val presentes = estado.asistencia[clase.id] ?: emptyList()
            return clase.inscriptos.mapNotNull { id ->
                socios.firstOrNull { it.id == id }?.let { it to (it.id in presentes) }
            }
        }

    /** Socios de las actividades que dicta la entrenadora. */
    val sociosDeLaProfe: List<SocioVista>
        get() = socios.filter { s ->
            s.actividades.any { it.nombre == "Musculación" || it.nombre == "Funcional" }
        }

    val rutinasPendientes: List<SocioVista> get() = sociosDeLaProfe.filter { !it.tieneRutina }

    val diasDeRutina: List<DiaRutina> get() = yo.rutina?.dias ?: emptyList()

    val diaDeRutina: DiaRutina?
        get() = diasDeRutina.getOrNull(estado.diaIdx) ?: diasDeRutina.firstOrNull()

    /** La tarjeta "Rutina · hoy" del inicio del socio apunta al segundo día. */
    val diaDeHoy: DiaRutina? get() = diasDeRutina.getOrNull(1) ?: diasDeRutina.firstOrNull()

    val diaEnEdicion: DiaRutina?
        get() = estado.editEj?.getOrNull(estado.editDia) ?: estado.editEj?.firstOrNull()

    /** Horarios de turno con nutrición, marcando los ya tomados. */
    val slots: List<GrupoSlots>
        get() {
            val tomados = estado.socios.mapNotNull { s -> s.turno?.let { "${it.fecha.fmtIso()} ${it.hora}" } }.toSet()
            return DemoData.diasDeTurnos.map { d ->
                GrupoSlots(
                    dia = d.fmtLargo().replaceFirstChar { it.uppercase() },
                    horas = DemoData.horasDeTurnos.map { h ->
                        val key = "${d.fmtIso()} $h"
                        SlotHora(hora = h, key = key, ocupado = key in tomados, elegido = estado.slot == key)
                    },
                )
            }
        }

    /** Agenda del nutricionista: los turnos del día agrupados por fecha. */
    val agenda: List<GrupoAgenda>
        get() = DemoData.diasDeAgenda.mapNotNull { d ->
            val turnos = socios
                .filter { it.turno?.fecha == d }
                .sortedBy { it.turno!!.hora }
            if (turnos.isEmpty()) return@mapNotNull null
            GrupoAgenda(
                dia = if (d == HOY) "Hoy · ${HOY.fmtLargo().lowercase().substringBefore(" de ")}" else d.fmtLargo().replaceFirstChar { it.uppercase() },
                esHoy = d == HOY,
                turnos = turnos,
            )
        }

    // ---------------------------------------------------------------- socio

    private fun actualizarSocio(id: Int, fn: (Socio) -> Socio) {
        estado = estado.copy(socios = estado.socios.map { if (it.id == id) fn(it) else it })
    }

    fun elegirMedio(m: MedioPago) {
        estado = estado.copy(medio = m)
    }

    /**
     * Marca la cuota del socio como pagada, abre la del mes siguiente (vence un mes después
     * del vencimiento original) y antepone el pago al historial con su comprobante.
     * Devuelve el comprobante, o `null` si la cuota ya estaba pagada: una cuota no se cobra
     * dos veces, ni desde la app ni en caja.
     */
    private fun pagarCuota(id: Int, medio: String): String? {
        val s = estado.socios.firstOrNull { it.id == id } ?: return null
        if (s.cuota.estado == EstadoCuota.PAGADA) return null
        val comprobante = "CD-2609-" + (1000..9999).random()
        actualizarSocio(id) { x ->
            x.copy(
                cuota = x.cuota.copy(estado = EstadoCuota.PAGADA, proximaVence = x.cuota.vence.masMeses(1)),
                historial = listOf(PagoHistorial(x.cuota.mes, x.cuota.monto, HOY, medio, comprobante)) + x.historial,
            )
        }
        return comprobante
    }

    /**
     * HU-04. El socio paga su cuota desde la app: queda pagada, se guarda el comprobante y
     * se abre la cuota del mes siguiente. Si ya estaba pagada, avisa y no navega.
     */
    fun confirmarPago() {
        val comprobante = pagarCuota(ID_SOCIO_APP, estado.medio.etiqueta) ?: run {
            toast("La cuota de ${yo.cuota.mes} ya está pagada.")
            return
        }
        estado = estado.copy(
            stack = estado.stack.dropLast(1) + Screen.S_PAGO_OK,
            comprobante = comprobante,
        )
    }

    fun elegirDiaRutina(i: Int) {
        estado = estado.copy(diaIdx = i)
    }

    fun elegirSlot(key: String) {
        estado = estado.copy(slot = key)
    }

    /** HU-11. */
    fun confirmarTurno() {
        val slot = estado.slot ?: run {
            toast("Elegí un horario disponible")
            return
        }
        val (iso, hora) = slot.split(" ")
        val f = fecha(iso)
        actualizarSocio(ID_SOCIO_APP) { it.copy(turno = Turno(f, hora)) }
        volver()
        toast("Turno confirmado · ${f.fmtCorto()} $hora")
    }

    fun pedirCancelarTurno() {
        estado = estado.copy(
            sheet = SheetData(
                titulo = "Cancelar turno",
                texto = "Vas a liberar tu turno con la nutricionista. Podés pedir otro cuando quieras.",
                filas = listOf("Turno" to yo.turnoLabel),
                cta = "Sí, cancelar",
                onConfirm = {
                    actualizarSocio(ID_SOCIO_APP) { it.copy(turno = null) }
                    cerrarSheet()
                    toast("Turno cancelado")
                },
            ),
        )
    }

    // ---------------------------------------------------------------- empleado / admin

    fun abrirSocio(id: Int) {
        estado = estado.copy(selId = id)
        ir(Screen.E_SOCIO)
    }

    fun elegirFiltro(f: FiltroSocios) {
        estado = estado.copy(filtro = f)
    }

    fun elegirReportTab(t: ReportTab) {
        estado = estado.copy(reportTab = t)
    }

    /**
     * Cobro en caja desde la ficha del socio (HU-04 del lado del mostrador). Hace lo mismo
     * que el pago desde la app: cuota siguiente y comprobante. Una cuota ya pagada no abre la hoja.
     */
    fun pedirRegistrarPago() {
        val s = seleccionado
        if (s.cuotaPagada) {
            toast("La cuota de ${s.cuota.mes} ya está pagada.")
            return
        }
        estado = estado.copy(
            sheet = SheetData(
                titulo = "Registrar pago",
                texto = "Cobro en caja. Al confirmar, la cuota queda pagada, se abre la del mes siguiente y el socio queda habilitado.",
                filas = listOf(
                    "Socio" to s.nombre,
                    "Cuota" to s.cuota.mes,
                    "Importe" to s.cuotaMonto,
                ),
                cta = "Cobrar ${s.cuotaMonto}",
                onConfirm = {
                    pagarCuota(s.id, "Caja")
                    cerrarSheet()
                    toast("Pago registrado · socio habilitado")
                },
            ),
        )
    }

    /** HU-07: emisión y renovación de carnet, siempre por 12 meses desde hoy. */
    fun pedirRenovarCarnet() {
        val s = seleccionado
        val desde = HOY
        val hasta = HOY.masAnios(1).masDias(-1)
        estado = estado.copy(
            sheet = SheetData(
                titulo = s.carnetAccion,
                texto = "Se emite un carnet nuevo con 12 meses de vigencia desde hoy.",
                filas = listOf(
                    "Socio" to s.nombre,
                    "Nueva vigencia" to "${desde.fmt()} → ${hasta.fmt()}",
                ),
                cta = "Emitir carnet",
                onConfirm = {
                    actualizarSocio(s.id) { it.copy(carnetEmitido = desde, carnetVence = hasta) }
                    cerrarSheet()
                    toast("Carnet emitido · vigente hasta ${hasta.fmt()}")
                },
            ),
        )
    }

    fun elegirPlan(i: Int) {
        estado = estado.copy(planIdx = i)
    }

    fun alternarApto() {
        estado = estado.copy(apto = !estado.apto)
    }

    val planSeleccionado get() = DemoData.planes[estado.planIdx]

    /** Vencimiento de la primera cuota de un alta: 10 días. */
    val venceCuotaInicial: Fecha = HOY.masDias(10)

    val editando: Boolean get() = estado.editandoId != null

    fun irAAlta() {
        estado = estado.copy(
            altaError = null,
            editandoId = null,
            apto = true,
            planIdx = 1,
            campos = estado.campos.copy(nombre = "", dni = "", mail = "", obs = ""),
        )
        ir(Screen.E_ALTA)
    }

    /** CRUD de socios — modificación: abre el formulario del alta con los datos cargados. */
    fun irAEditar() {
        val s = seleccionado.socio
        estado = estado.copy(
            altaError = null,
            editandoId = s.id,
            apto = s.ficha,
            planIdx = DemoData.planes.indexOfFirst { it.nombre == s.plan }.coerceAtLeast(0),
            campos = estado.campos.copy(
                nombre = s.nombre,
                dni = s.dni.toString(),
                mail = s.email,
                obs = s.nut.antecedentes,
            ),
        )
        ir(Screen.E_EDITAR)
    }

    /** Valida el formulario de alta / edición; devuelve el primer error o `null`. */
    private fun errorFormularioSocio(): String? {
        val c = estado.campos
        Validaciones.nombre(c.nombre)?.let { return it }
        Validaciones.dni(c.dni)?.let { return it }
        Validaciones.email(c.mail)?.let { return it }
        val dni = c.dni.filter { it.isDigit() }.toLongOrNull()
        if (dni != null && ClubRepositorio.contarDni(dni, estado.editandoId) > 0) {
            return "Ya existe un socio con ese DNI."
        }
        return null
    }

    /** HU-02: el alta genera N° de socio, carnet por 12 meses y cuota inicial. */
    fun confirmarAlta() {
        errorFormularioSocio()?.let {
            estado = estado.copy(altaError = it)
            return
        }
        if (editando) {
            guardarEdicion()
            return
        }
        val nombre = estado.campos.nombre.trim()
        val dni = estado.campos.dni.filter { it.isDigit() }
        val plan = planSeleccionado
        val nuevo = Socio(
            id = proximoIdSocio,
            nombre = nombre,
            dni = dni.toLong(),
            email = estado.campos.mail.trim(),
            nro = proximoNroSocio(),
            plan = plan.nombre,
            alta = HOY,
            nac = fecha("1998-01-01"),
            carnetEmitido = HOY,
            carnetVence = HOY.masAnios(1).masDias(-1),
            ficha = estado.apto,
            actividades = emptyList(),
            cuota = Cuota("Septiembre 2026", plan.precio, venceCuotaInicial, EstadoCuota.PENDIENTE),
            historial = emptyList(),
            rutina = null,
            nut = FichaNutricional(
                peso = 0.0,
                altura = 0.0,
                objetivo = "—",
                antecedentes = estado.campos.obs.ifBlank { "Sin antecedentes" },
                consultas = emptyList(),
            ),
            turno = null,
            ingresos = emptyList(),
        )
        estado = estado.copy(
            socios = estado.socios + nuevo,
            stack = listOf(Screen.E_SOCIOS, Screen.E_SOCIO),
            selId = nuevo.id,
            filtro = FiltroSocios.TODOS,
        )
        toast("Socio N° ${nuevo.nro} registrado · carnet y cuota inicial generados")
    }

    /** 1500 + cantidad de socios; si ese número ya existe (por una baja), el siguiente libre. */
    private fun proximoNroSocio(): String {
        val usados = estado.socios.map { it.nro }.toSet()
        var n = 1500 + estado.socios.size
        while (n.toString().padStart(5, '0') in usados) n++
        return n.toString().padStart(5, '0')
    }

    /** El cambio de plan rige desde la próxima cuota; la cuota emitida no se modifica. */
    private fun guardarEdicion() {
        val id = estado.editandoId ?: return
        val c = estado.campos
        actualizarSocio(id) { s ->
            s.copy(
                nombre = c.nombre.trim(),
                dni = c.dni.filter { it.isDigit() }.toLong(),
                email = c.mail.trim(),
                plan = planSeleccionado.nombre,
                ficha = estado.apto,
                nut = s.nut.copy(antecedentes = c.obs.ifBlank { "Sin antecedentes" }),
            )
        }
        estado = estado.copy(stack = listOf(Screen.E_SOCIOS, Screen.E_SOCIO), selId = id, editandoId = null)
        toast("Datos del socio actualizados")
    }

    /** CRUD de socios — baja, con confirmación. Lo quita también de clases y asistencias. */
    fun pedirBajaSocio() {
        val s = seleccionado
        estado = estado.copy(
            sheet = SheetData(
                titulo = "Dar de baja",
                texto = "El socio deja de figurar en el padrón, en sus clases y en la agenda. " +
                    "Esta acción no se puede deshacer.",
                filas = listOf("Socio" to s.nombre, "DNI" to s.dniLabel, "N° de socio" to s.nro),
                cta = "Confirmar baja",
                onConfirm = {
                    val id = s.id
                    estado = estado.copy(
                        socios = estado.socios.filterNot { it.id == id },
                        acts = estado.acts.map { a -> a.copy(inscriptos = a.inscriptos - id) },
                        asistencia = estado.asistencia.mapValues { (_, ids) -> ids - id },
                        stack = listOf(Screen.E_SOCIOS),
                        selId = null,
                        sheet = null,
                    )
                    toast("${s.nombre} dado de baja")
                },
            ),
        )
    }

    /** HU-05: control de acceso por DNI. */
    fun validarAcceso() {
        Validaciones.dni(estado.campos.dniAcceso)?.let {
            estado = estado.copy(acceso = null)
            toast(it)
            return
        }
        val dni = estado.campos.dniAcceso.filter { it.isDigit() }
        val s = socios.firstOrNull { it.socio.dni.toString() == dni }
        val a = when {
            s == null -> Acceso(
                socioId = null,
                titulo = "No encontrado",
                detalle = "No hay ningún socio con DNI ${dniFmt(dni)}. Podés registrarlo como visitante o dar de alta un socio.",
                tono = Acceso.Tono.NEUTRO,
            )
            s.hab -> Acceso(
                socioId = s.id,
                titulo = "Permitido",
                detalle = "Carnet vigente y cuota al día.",
                tono = Acceso.Tono.PERMITIDO,
            )
            else -> Acceso(
                socioId = s.id,
                titulo = "Denegado",
                detalle = if (s.mora) {
                    "Cuota ${s.cuota.mes} en mora. Debe regularizar el pago en recepción."
                } else {
                    "Carnet vencido el ${s.socio.carnetVence.fmt()}. Debe renovarse."
                },
                tono = Acceso.Tono.DENEGADO,
            )
        }
        estado = estado.copy(acceso = a)
    }

    fun ponerDniAcceso(dni: Long) {
        estado = estado.copy(campos = estado.campos.copy(dniAcceso = dni.toString()))
        validarAcceso()
    }

    fun registrarIngreso() {
        val id = estado.acceso?.socioId ?: return
        val s = socios.firstOrNull { it.id == id } ?: return
        estado = estado.copy(
            ingresosHoy = listOf(IngresoHoy(HORA_ACTUAL, s.nombre, "Permitido", ok = true)) + estado.ingresosHoy,
            acceso = estado.acceso?.copy(registrado = true),
        )
        actualizarSocio(id) {
            it.copy(ingresos = listOf(Ingreso("${HOY.fmtCorto()} · $HORA_ACTUAL", "Permitido")) + it.ingresos)
        }
        toast("Ingreso registrado · ${s.nombre}")
    }

    fun irAVisitanteNuevo() {
        estado = estado.copy(
            visError = null,
            visAct = null,
            visMedio = MedioVisitante.EFECTIVO,
            campos = estado.campos.copy(vNombre = "", vDni = ""),
        )
        ir(Screen.E_VISITANTE_NUEVO)
    }

    fun elegirActividadVisitante(a: ActividadVista) {
        estado = estado.copy(
            visAct = a.id,
            visError = if (a.sinCupo) {
                "${a.nombre} no tiene cupo disponible hoy (${a.ocupadosHoy}/${a.act.cupo}). El ingreso no puede realizarse."
            } else {
                null
            },
        )
    }

    fun elegirMedioVisitante(m: MedioVisitante) {
        estado = estado.copy(visMedio = m)
    }

    /**
     * HU-03: pago diario de un no socio (consigna, punto 3). Queda con fecha y medio de
     * pago; se rechaza si la actividad no tiene cupo hoy o si ese DNI ya pagó hoy.
     * El cupo del día sale de los visitantes de hoy, así que no se toca `act.ocupados`.
     */
    fun confirmarVisitante() {
        val nombre = estado.campos.vNombre.trim()
        val dni = estado.campos.vDni.filter { it.isDigit() }
        val error = Validaciones.nombre(nombre, " del visitante") ?: Validaciones.dni(estado.campos.vDni)
        if (error != null) {
            estado = estado.copy(visError = error)
            return
        }
        val a = actividadElegida
        if (a == null) {
            estado = estado.copy(visError = "Seleccioná una actividad.")
            return
        }
        if (a.sinCupo) {
            estado = estado.copy(visError = "Sin cupo en ${a.nombre}. El ingreso no puede realizarse.")
            return
        }
        val dniConPuntos = dniFmt(dni)
        if (ClubRepositorio.contarVisitante(dniConPuntos, HOY) > 0) {
            estado = estado.copy(visError = "Ya hay un pago diario registrado hoy con el DNI $dniConPuntos.")
            return
        }
        estado = estado.copy(
            visitantes = listOf(
                Visitante(nombre, dniConPuntos, a.nombre, HORA_ACTUAL, a.act.precio, HOY, estado.visMedio.etiqueta),
            ) + estado.visitantes,
            stack = listOf(Screen.E_VISITANTES),
        )
        toast("Pago diario registrado · ${money(a.act.precio)}")
    }

    // ---------------------------------------------------------------- profesor

    fun marcarPresencia() {
        estado = estado.copy(profePresente = true)
        toast("Presencia registrada · $HORA_ACTUAL")
    }

    fun elegirClase(id: String) {
        estado = estado.copy(claseId = id)
    }

    fun abrirAsistencia(id: String) {
        estado = estado.copy(claseId = id)
        ir(Screen.P_ASISTENCIA)
    }

    /** HU-08. */
    fun alternarAsistencia(socioId: Int) {
        val clase = claseSeleccionada.act.id
        val actuales = estado.asistencia[clase] ?: emptyList()
        val nuevos = if (socioId in actuales) actuales - socioId else actuales + socioId
        estado = estado.copy(asistencia = estado.asistencia + (clase to nuevos))
    }

    fun guardarAsistencia() {
        val filas = filasAsistencia
        val presentes = filas.count { it.second }
        toast("Asistencia guardada · ${claseSeleccionada.act.nombre} · $presentes/${filas.size} presentes")
        ir(Screen.P_HOME)
    }

    /** HU-09: abre el editor de rutina; si el socio no tenía, arranca con 3 días vacíos. */
    fun abrirRutina(id: Int) {
        val s = estado.socios.first { it.id == id }
        val base = s.rutina?.dias ?: listOf(
            DiaRutina("D1", "Día 1", emptyList()),
            DiaRutina("D2", "Día 2", emptyList()),
            DiaRutina("D3", "Día 3", emptyList()),
        )
        estado = estado.copy(selId = id, editEj = base, editDia = 0)
        ir(Screen.P_RUTINA_EDIT)
    }

    fun elegirDiaEdicion(i: Int) {
        estado = estado.copy(editDia = i)
    }

    fun agregarEjercicio() {
        val nombre = estado.campos.ejNombre.trim()
        val error = (if (nombre.isEmpty()) "Ingresá el nombre del ejercicio" else null)
            ?: Validaciones.entero(estado.campos.ejSeries, "Las series", 1..20)
            ?: Validaciones.entero(estado.campos.ejReps, "Las repeticiones", 1..100)
        if (error != null) {
            toast(error)
            return
        }
        val ej = Ejercicio(
            nombre = nombre,
            series = estado.campos.ejSeries.toIntOrNull() ?: 3,
            reps = estado.campos.ejReps.toIntOrNull() ?: 12,
        )
        estado = estado.copy(
            editEj = estado.editEj?.mapIndexed { i, d ->
                if (i == estado.editDia) d.copy(ejercicios = d.ejercicios + ej) else d
            },
            campos = estado.campos.copy(ejNombre = "", ejSeries = "", ejReps = ""),
        )
    }

    fun quitarEjercicio(indice: Int) {
        estado = estado.copy(
            editEj = estado.editEj?.mapIndexed { i, d ->
                if (i == estado.editDia) {
                    d.copy(ejercicios = d.ejercicios.filterIndexed { k, _ -> k != indice })
                } else {
                    d
                }
            },
        )
    }

    fun guardarRutina() {
        val dias = estado.editEj.orEmpty().filter { it.ejercicios.isNotEmpty() }
        if (dias.isEmpty()) {
            toast("Agregá al menos un ejercicio")
            return
        }
        val s = seleccionado
        actualizarSocio(s.id) { x ->
            x.copy(
                rutina = Rutina(
                    nombre = x.rutina?.nombre ?: "Rutina personalizada",
                    profesor = DemoData.PROFESORA,
                    actualizada = HOY,
                    dias = dias,
                ),
            )
        }
        estado = estado.copy(stack = listOf(Screen.P_RUTINAS))
        toast("Rutina asignada a ${s.nombre} · visible en su app")
    }

    // ---------------------------------------------------------------- nutricionista

    fun abrirFicha(id: Int) {
        estado = estado.copy(
            selId = id,
            campos = estado.campos.copy(nPeso = "", nAltura = "", nObs = ""),
        )
        ir(Screen.N_FICHA)
    }

    /** HU-12: la consulta actualiza la ficha y consume el turno del día. */
    fun guardarConsulta() {
        val error = Validaciones.peso(estado.campos.nPeso) ?: Validaciones.altura(estado.campos.nAltura)
        if (error != null) {
            toast(error)
            return
        }
        val peso = estado.campos.nPeso.trim().replace(',', '.').toDouble()
        val altura = estado.campos.nAltura.trim().replace(',', '.').toDoubleOrNull()
        val obs = estado.campos.nObs.trim().ifBlank { "Control de rutina." }
        actualizarSocio(seleccionado.id) { x ->
            x.copy(
                nut = x.nut.copy(
                    peso = peso,
                    altura = altura ?: x.nut.altura,
                    consultas = listOf(Consulta(HOY, peso, obs)) + x.nut.consultas,
                ),
                turno = if (x.turno?.fecha == HOY) null else x.turno,
            )
        }
        estado = estado.copy(campos = estado.campos.copy(nPeso = "", nAltura = "", nObs = ""))
        toast("Consulta guardada en el historial")
    }
}
