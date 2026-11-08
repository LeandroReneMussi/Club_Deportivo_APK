package ar.edu.ifts29.clubdeportivo

import ar.edu.ifts29.clubdeportivo.data.Acceso
import ar.edu.ifts29.clubdeportivo.data.FiltroSocios
import ar.edu.ifts29.clubdeportivo.data.HOY
import ar.edu.ifts29.clubdeportivo.data.MedioPago
import ar.edu.ifts29.clubdeportivo.data.ReportTab
import ar.edu.ifts29.clubdeportivo.data.Rol
import ar.edu.ifts29.clubdeportivo.data.Screen
import ar.edu.ifts29.clubdeportivo.data.daysTo
import ar.edu.ifts29.clubdeportivo.data.fecha
import ar.edu.ifts29.clubdeportivo.data.fmt
import ar.edu.ifts29.clubdeportivo.data.money
import ar.edu.ifts29.clubdeportivo.vm.ClubRepositorio
import ar.edu.ifts29.clubdeportivo.vm.ClubViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Cubre las reglas de negocio de las 14 historias de usuario contra el ViewModel real.
 *
 * La regla que atraviesa todo el sistema es la de habilitación: un socio puede ingresar
 * y recibir rutina sólo si no está en mora y su carnet está vigente.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ClubViewModelTest {

    // `toast()` publica en viewModelScope, que sin esto no tiene dispatcher en la JVM.
    // Cada prueba arranca con los datos de demo y sin sesión, como una app recién abierta.
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        ClubRepositorio.reiniciar()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun ingresar(usuario: String, clave: String) = ClubViewModel().apply {
        campo { copy(email = usuario, pass = clave) }
        login()
    }

    private fun socio() = ingresar("Andres", "Socio123")
    private fun admin() = ingresar("Jorge", "Admin123")
    private fun profe() = ingresar("Ana", "Prof123")
    private fun nutri() = ingresar("Leandro", "Nut123")

    // ------------------------------------------------------------------ HU-01

    @Test
    fun `HU-01 - una credencial invalida no abre sesion`() {
        val vm = ClubViewModel()
        vm.campo { copy(email = "Andres", pass = "incorrecta") }
        vm.login()
        assertTrue(vm.estado.loginError)
        assertNull(vm.estado.rol)
        assertEquals(Screen.LOGIN, vm.pantalla)
    }

    @Test
    fun `HU-01 - el usuario se normaliza y el rol sale de la cuenta`() {
        val vm = ClubViewModel()
        vm.campo { copy(email = "  ANDRÉS ", pass = "Socio123") }
        vm.login()
        assertEquals(Rol.SOCIO, vm.estado.rol)
        assertEquals(Screen.S_HOME, vm.pantalla)
        assertTrue("no debe conservar la contraseña", vm.estado.campos.pass.isEmpty())
    }

    @Test
    fun `HU-01 - cada rol aterriza en su propia pantalla`() {
        assertEquals(Screen.S_HOME, socio().pantalla)
        assertEquals(Screen.E_HOME, admin().pantalla)
        assertEquals(Screen.P_HOME, profe().pantalla)
        assertEquals(Screen.N_AGENDA, nutri().pantalla)
    }

    @Test
    fun `HU-01 - cerrar sesion vuelve al login`() {
        val vm = socio()
        vm.pedirCerrarSesion()
        assertTrue(vm.estado.logoutSheet)
        vm.cancelarCerrarSesion()
        assertFalse(vm.estado.logoutSheet)
        vm.pedirCerrarSesion()
        vm.cerrarSesion()
        assertEquals(Screen.LOGIN, vm.pantalla)
        assertNull(vm.estado.rol)
        assertFalse(vm.estado.logoutSheet)
    }

    // ------------------------------------------------------------------ habilitación

    @Test
    fun `la habilitacion combina mora y vigencia del carnet`() {
        val vm = admin()
        val andres = vm.socios.first { it.id == 1 }
        val lucas = vm.socios.first { it.id == 2 }
        val sofia = vm.socios.first { it.id == 3 }

        assertTrue("cuota al día y carnet vigente", andres.hab)
        assertEquals("Habilitado · Cuota al día", andres.badge)

        assertTrue(lucas.mora)
        assertFalse("la mora bloquea", lucas.hab)
        assertEquals("En mora", lucas.badgeCorto)

        assertTrue("la cuota está paga", sofia.cuotaPagada)
        assertTrue(sofia.carnetVencido)
        assertFalse("el carnet vencido bloquea igual", sofia.hab)
        assertEquals("Carnet venc.", sofia.badgeCorto)
    }

    @Test
    fun `las etiquetas de vencimiento se redactan segun los dias restantes`() {
        val vm = admin()
        assertEquals(
            "Vence en 2 días · 10/09/2026",
            vm.socios.first { it.id == 1 }.cuotaVenceLabel,
        )
        assertEquals(
            "Cuota Agosto 2026 vencida hace 29 días",
            vm.socios.first { it.id == 2 }.motivo,
        )
    }

    // ------------------------------------------------------------------ HU-05

    @Test
    fun `HU-05 - el control de acceso resuelve los cuatro casos`() {
        val vm = admin()

        vm.ponerDniAcceso(38_412_907L)
        assertEquals("Permitido", vm.estado.acceso?.titulo)
        assertEquals(Acceso.Tono.PERMITIDO, vm.estado.acceso?.tono)

        vm.ponerDniAcceso(40_120_334L)
        assertEquals("Denegado", vm.estado.acceso?.titulo)
        assertTrue(vm.estado.acceso!!.detalle.contains("mora"))

        vm.ponerDniAcceso(35_887_210L)
        assertEquals("Denegado", vm.estado.acceso?.titulo)
        assertTrue(vm.estado.acceso!!.detalle.contains("Carnet vencido"))

        vm.ponerDniAcceso(11_111_111L)
        assertEquals("No encontrado", vm.estado.acceso?.titulo)
        assertNull(vm.estado.acceso?.socioId)
    }

    @Test
    fun `HU-05 - registrar el ingreso lo agrega al dia y al historial del socio`() {
        val vm = admin()
        val antes = vm.estado.ingresosHoy.size
        vm.ponerDniAcceso(38_412_907L)
        vm.registrarIngreso()

        assertEquals(antes + 1, vm.estado.ingresosHoy.size)
        assertTrue("no se puede registrar dos veces", vm.estado.acceso!!.registrado)
        assertEquals("Permitido", vm.socios.first { it.id == 1 }.ingresos.first().estado)
    }

    // ------------------------------------------------------------------ HU-04

    @Test
    fun `HU-04 - el pago simulado deja la cuota paga y emite comprobante`() {
        val vm = socio()
        val historialAntes = vm.yo.historial.size
        assertTrue(vm.yo.cuotaPendiente)

        vm.ir(Screen.S_PAGAR)
        vm.elegirMedio(MedioPago.BILLETERA)
        vm.confirmarPago()

        assertTrue(vm.yo.cuotaPagada)
        assertTrue(vm.yo.hab)
        assertEquals(historialAntes + 1, vm.yo.historial.size)
        assertEquals("Billetera", vm.yo.historial.first().medio)
        assertEquals(Screen.S_PAGO_OK, vm.pantalla)
        assertTrue(vm.estado.comprobante!!.startsWith("CD-2609-"))
        assertEquals(vm.estado.comprobante, vm.yo.historial.first().comprobante)
        assertEquals(fecha("2026-10-10"), vm.yo.cuota.proximaVence)
        assertEquals("Pagada · próxima 10/10/2026", vm.yo.cuotaVenceLabel)
    }

    @Test
    fun `HU-04 - el cobro en caja habilita al socio en mora`() {
        val vm = admin()
        vm.abrirSocio(2)
        assertFalse(vm.seleccionado.hab)

        vm.pedirRegistrarPago()
        vm.estado.sheet!!.onConfirm()

        val lucas = vm.socios.first { it.id == 2 }
        assertTrue(lucas.cuotaPagada)
        assertTrue(lucas.hab)
        assertEquals("Caja", lucas.historial.first().medio)
        assertNull("la hoja se cierra", vm.estado.sheet)
    }

    // ------------------------------------------------------------------ HU-07

    @Test
    fun `HU-07 - renovar el carnet lo emite por doce meses y habilita`() {
        val vm = admin()
        vm.abrirSocio(3)
        assertEquals("Renovar carnet", vm.seleccionado.carnetAccion)

        vm.pedirRenovarCarnet()
        assertNotNull(vm.estado.sheet)
        vm.estado.sheet!!.onConfirm()

        val sofia = vm.socios.first { it.id == 3 }
        assertEquals("08/09/2026", sofia.carnetEmitidoLabel)
        assertEquals("07/09/2027", sofia.carnetVenceLabel)
        assertTrue(sofia.hab)
    }

    // ------------------------------------------------------------------ HU-02

    @Test
    fun `HU-02 - el alta valida nombre, DNI y duplicados`() {
        val vm = admin()
        vm.irAAlta()

        vm.campo { copy(nombre = "Ana", dni = "30111222", mail = "ana@club.com") }
        vm.confirmarAlta()
        assertNotNull("nombre demasiado corto", vm.estado.altaError)

        vm.campo { copy(nombre = "Marcos Ferreyra", dni = "123") }
        vm.confirmarAlta()
        assertTrue(vm.estado.altaError!!.contains("7 u 8"))

        vm.campo { copy(nombre = "Marcos Ferreyra", dni = "38412907") }
        vm.confirmarAlta()
        assertTrue(vm.estado.altaError!!.contains("Ya existe"))
    }

    @Test
    fun `HU-02 - el alta rechaza formatos incorrectos y datos faltantes`() {
        val vm = admin()
        vm.irAAlta()

        vm.campo { copy(nombre = "Marcos Ferr3yra", dni = "30111222", mail = "m@club.com") }
        vm.confirmarAlta()
        assertTrue(vm.estado.altaError!!.contains("letras"))

        vm.campo { copy(nombre = "Marcos Ferreyra", dni = "30A11222") }
        vm.confirmarAlta()
        assertTrue(vm.estado.altaError!!.contains("números"))

        vm.campo { copy(dni = "30111222", mail = "") }
        vm.confirmarAlta()
        assertTrue(vm.estado.altaError!!.contains("correo"))

        vm.campo { copy(mail = "marcos@club") }
        vm.confirmarAlta()
        assertTrue(vm.estado.altaError!!.contains("formato"))
        assertEquals(Screen.E_ALTA, vm.pantalla)
    }

    @Test
    fun `HU-02 - el alta genera numero de socio, carnet y cuota inicial`() {
        val vm = admin()
        vm.irAAlta()
        val cuantos = vm.estado.socios.size

        vm.campo { copy(nombre = "Marcos Ferreyra", dni = "30111222", mail = "marcos@club.com") }
        vm.elegirPlan(2)
        vm.confirmarAlta()

        assertEquals(cuantos + 1, vm.estado.socios.size)
        assertEquals(Screen.E_SOCIO, vm.pantalla)

        val nuevo = vm.seleccionado
        assertEquals("01508", nuevo.nro)
        assertEquals("Familiar", nuevo.plan)
        assertEquals(money(29_000), nuevo.cuotaMonto)
        assertEquals("18/09/2026", nuevo.socio.cuota.vence.fmt())
        assertEquals("07/09/2027", nuevo.carnetVenceLabel)
        assertTrue("nace habilitado", nuevo.hab)
        assertEquals("sin medidas todavía no hay IMC", "—", nuevo.imc)
    }

    // ------------------------------------------------------------------ HU-03

    @Test
    fun `HU-03 - sin cupo el ingreso de visitante se bloquea`() {
        val vm = admin()
        val natacion = vm.actividades.first { it.id == "nat" }
        assertTrue(natacion.sinCupo)

        vm.irAVisitanteNuevo()
        vm.campo { copy(vNombre = "Pedro Gómez", vDni = "30111222") }
        vm.elegirActividadVisitante(natacion)
        assertNotNull("avisa al elegirla", vm.estado.visError)

        val antes = vm.estado.visitantes.size
        vm.confirmarVisitante()
        assertEquals("no registra el ingreso", antes, vm.estado.visitantes.size)
    }

    @Test
    fun `HU-03 - con cupo se registra el ingreso y se descuenta`() {
        val vm = admin()
        vm.irAVisitanteNuevo()
        vm.campo { copy(vNombre = "Pedro Gómez", vDni = "30111222") }
        vm.elegirActividadVisitante(vm.actividades.first { it.id == "func" })

        val antes = vm.estado.visitantes.size
        vm.confirmarVisitante()

        assertEquals(antes + 1, vm.estado.visitantes.size)
        assertEquals(15, vm.actividades.first { it.id == "func" }.act.ocupados)
        assertEquals(6_000, vm.estado.visitantes.first().monto)
        assertEquals(Screen.E_VISITANTES, vm.pantalla)
    }

    // ------------------------------------------------------------------ HU-14

    @Test
    fun `HU-14 - los reportes separan proximas a vencer de la mora`() {
        val vm = admin()

        // Martín vence hoy; Andrés y Valentina en la semana. (La separación "hoy" / "semana"
        // llega con la pestaña Vencen hoy.)
        assertEquals(3, vm.porVencer.size)
        assertEquals(
            "ordenadas por vencimiento",
            listOf(0, 2, 4),
            vm.porVencer.map { daysTo(it.cuota.vence) },
        )
        assertEquals(money(18_500 + 18_500 + 12_000), vm.totalReporte)

        assertEquals(setOf(2, 6), vm.enMora.map { it.id }.toSet())
        vm.elegirReportTab(ReportTab.MORA)
        assertEquals(money(12_000 + 17_000), vm.totalReporte)
    }

    @Test
    fun `los filtros y la busqueda del listado de socios`() {
        val vm = admin()

        vm.campo { copy(busq = "Sofía") }
        assertEquals(1, vm.sociosFiltrados.size)

        vm.campo { copy(busq = "40.120.334") }
        assertEquals("busca por DNI con o sin puntos", 1, vm.sociosFiltrados.size)

        vm.campo { copy(busq = "") }
        vm.elegirFiltro(FiltroSocios.MORA)
        assertEquals(2, vm.sociosFiltrados.size)
        vm.elegirFiltro(FiltroSocios.HAB)
        assertEquals(5, vm.sociosFiltrados.size)
    }

    // ------------------------------------------------------------------ HU-09

    @Test
    fun `HU-09 - la rutina se bloquea por mora o por falta de apto fisico`() {
        val vm = profe()

        val lucas = vm.socios.first { it.id == 2 }
        assertTrue(lucas.bloqueaRutina)
        assertEquals("cuota en mora", lucas.bloqueoMotivo)

        val valentina = vm.socios.first { it.id == 5 }
        assertTrue("está habilitada para entrar", valentina.hab)
        assertTrue("pero no presentó el apto", valentina.bloqueaRutina)
        assertEquals("falta la ficha médica", valentina.bloqueoMotivo)

        assertTrue(vm.socios.first { it.id == 1 }.permiteRutina)
    }

    @Test
    fun `HU-09 - crear y asignar una rutina`() {
        val vm = profe()
        assertTrue(vm.rutinasPendientes.any { it.id == 6 })

        vm.abrirRutina(6)
        assertEquals(Screen.P_RUTINA_EDIT, vm.pantalla)
        assertEquals("arranca con tres días vacíos", 3, vm.estado.editEj?.size)

        vm.guardarRutina()
        assertNull("una rutina sin ejercicios no se guarda", vm.socios.first { it.id == 6 }.rutina)

        vm.campo { copy(ejNombre = "Sentadilla", ejSeries = "4", ejReps = "10") }
        vm.agregarEjercicio()
        vm.campo { copy(ejNombre = "Remo") }
        vm.agregarEjercicio()
        assertEquals(2, vm.diaEnEdicion?.ejercicios?.size)
        assertEquals("series por defecto", 3, vm.diaEnEdicion!!.ejercicios[1].series)
        assertTrue("limpia el formulario", vm.estado.campos.ejNombre.isEmpty())

        vm.quitarEjercicio(1)
        assertEquals(1, vm.diaEnEdicion?.ejercicios?.size)

        vm.guardarRutina()
        val tomas = vm.socios.first { it.id == 6 }
        assertTrue(tomas.tieneRutina)
        assertEquals("descarta los días vacíos", 1, tomas.rutina?.dias?.size)
        assertEquals(HOY, tomas.rutina?.actualizada)
        assertEquals(Screen.P_RUTINAS, vm.pantalla)
    }

    // ------------------------------------------------------------------ HU-08

    @Test
    fun `HU-08 - asistencia por clase`() {
        val vm = profe()
        assertEquals("sólo las clases que dicta", 2, vm.clases.size)

        vm.abrirAsistencia("musc")
        assertEquals(Screen.P_ASISTENCIA, vm.pantalla)
        assertEquals(4, vm.filasAsistencia.size)
        assertEquals(2, vm.filasAsistencia.count { it.second })

        vm.alternarAsistencia(1)
        assertEquals(3, vm.filasAsistencia.count { it.second })
        vm.alternarAsistencia(1)
        assertEquals(2, vm.filasAsistencia.count { it.second })

        vm.elegirClase("func")
        assertEquals(3, vm.filasAsistencia.size)
        assertEquals(0, vm.filasAsistencia.count { it.second })
    }

    @Test
    fun `HU-08 - la profesora marca su propia presencia`() {
        val vm = profe()
        assertFalse(vm.estado.profePresente)
        vm.marcarPresencia()
        assertTrue(vm.estado.profePresente)
    }

    // ------------------------------------------------------------------ HU-11

    @Test
    fun `HU-11 - cancelar y volver a pedir turno`() {
        val vm = socio()
        assertEquals("Vie 11 sep · 17:30", vm.yo.turnoLabel)

        vm.ir(Screen.S_NUTRI)
        vm.pedirCancelarTurno()
        vm.estado.sheet!!.onConfirm()
        assertFalse(vm.yo.tieneTurno)

        vm.ir(Screen.S_TURNO)
        val horas = vm.slots.flatMap { it.horas }
        assertEquals("3 días × 6 horarios", 18, horas.size)
        assertEquals("el turno de Valentina está tomado", 1, horas.count { it.ocupado })

        val libre = horas.first { !it.ocupado }
        vm.elegirSlot(libre.key)
        vm.confirmarTurno()
        assertTrue(vm.yo.tieneTurno)
        assertEquals("vuelve a la pantalla anterior", Screen.S_NUTRI, vm.pantalla)
    }

    // ------------------------------------------------------------------ HU-12

    @Test
    fun `HU-12 - la agenda ordena los turnos del dia`() {
        val vm = nutri()
        assertEquals(3, vm.agenda.size)
        assertTrue(vm.agenda.first().esHoy)
        assertEquals(
            listOf("16:30", "18:00"),
            vm.agenda.first().turnos.map { it.turno!!.hora },
        )
    }

    @Test
    fun `HU-12 - registrar una consulta actualiza la ficha y consume el turno`() {
        val vm = nutri()
        vm.abrirFicha(3)
        assertEquals(Screen.N_FICHA, vm.pantalla)

        val antes = vm.seleccionado.consultas.size
        vm.guardarConsulta()
        assertEquals("sin peso no guarda", antes, vm.seleccionado.consultas.size)

        vm.campo { copy(nPeso = "57,4", nObs = "Bajó 800 g.") }
        vm.guardarConsulta()

        assertEquals(antes + 1, vm.seleccionado.consultas.size)
        assertEquals("acepta la coma decimal", 57.4, vm.seleccionado.nut.peso, 0.001)
        assertEquals("Bajó 800 g.", vm.seleccionado.consultas.first().obs)
        assertEquals("21.6", vm.seleccionado.imc)
        assertFalse("el turno de hoy queda consumido", vm.seleccionado.tieneTurno)
        assertTrue(vm.estado.campos.nPeso.isEmpty())
    }

    // ------------------------------------------------------------------ navegación

    @Test
    fun `las pantallas raiz reemplazan la pila y el resto se apila`() {
        val vm = socio()
        assertEquals(1, vm.estado.stack.size)

        vm.ir(Screen.S_CUOTAS)
        assertEquals(2, vm.estado.stack.size)
        assertTrue(vm.muestraHeader)
        assertEquals("Estado de cuenta", vm.titulo)

        vm.volver()
        assertEquals(Screen.S_HOME, vm.pantalla)

        vm.ir(Screen.S_CARNET)
        assertEquals("una raíz no apila", 1, vm.estado.stack.size)
    }

    @Test
    fun `Perfil es pestania del socio pero sub-pantalla del administrativo`() {
        val s = socio()
        s.ir(Screen.PERFIL)
        assertEquals(1, s.estado.stack.size)
        assertFalse("como pestaña no lleva header", s.muestraHeader)

        val a = admin()
        a.ir(Screen.E_SOCIOS)
        a.ir(Screen.PERFIL)
        assertEquals(2, a.estado.stack.size)
        assertTrue("apilada sí lleva header", a.muestraHeader)
        a.volver()
        assertEquals(Screen.E_SOCIOS, a.pantalla)
    }

    @Test
    fun `la barra inferior se oculta en el login y en el comprobante`() {
        assertFalse(ClubViewModel().muestraNav)

        val vm = socio()
        assertTrue(vm.muestraNav)
        vm.ir(Screen.S_PAGAR)
        vm.confirmarPago()
        assertFalse(vm.muestraNav)
    }

    @Test
    fun `cada rol tiene su juego de pestanias`() {
        assertEquals(5, socio().tabs.size)
        assertEquals(5, admin().tabs.size)
        assertEquals(4, profe().tabs.size)
        assertEquals(3, nutri().tabs.size)
    }
}
