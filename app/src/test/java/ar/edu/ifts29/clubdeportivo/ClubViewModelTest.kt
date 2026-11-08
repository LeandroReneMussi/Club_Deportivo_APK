package ar.edu.ifts29.clubdeportivo

import ar.edu.ifts29.clubdeportivo.data.Acceso
import ar.edu.ifts29.clubdeportivo.data.Fecha
import ar.edu.ifts29.clubdeportivo.data.FiltroSocios
import ar.edu.ifts29.clubdeportivo.data.HOY
import ar.edu.ifts29.clubdeportivo.data.MedioPago
import ar.edu.ifts29.clubdeportivo.data.MedioVisitante
import ar.edu.ifts29.clubdeportivo.data.Reloj
import ar.edu.ifts29.clubdeportivo.data.ReportTab
import ar.edu.ifts29.clubdeportivo.data.Rol
import ar.edu.ifts29.clubdeportivo.data.Screen
import ar.edu.ifts29.clubdeportivo.data.daysTo
import ar.edu.ifts29.clubdeportivo.data.fecha
import ar.edu.ifts29.clubdeportivo.data.fmt
import ar.edu.ifts29.clubdeportivo.data.money
import ar.edu.ifts29.clubdeportivo.data.vigenteEn
import ar.edu.ifts29.clubdeportivo.vm.ClubRepositorio
import ar.edu.ifts29.clubdeportivo.vm.ClubViewModel
import ar.edu.ifts29.clubdeportivo.vm.TonoCuota
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
import kotlin.math.roundToInt

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
        Reloj.fijar(Fecha(2026, 9, 8))
        ClubRepositorio.reiniciar()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        Reloj.fijar(null)
    }

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

        assertTrue("cuota pendiente sin vencer y carnet vigente", andres.hab)
        assertEquals("la etiqueta dice que debe la cuota", "Habilitado · Cuota pendiente", andres.badge)
        assertEquals("Habilitado · Cuota pendiente", vm.socios.first { it.id == 8 }.badge)
        assertEquals("solo la cuota pagada está al día", "Habilitado · Cuota al día", vm.socios.first { it.id == 7 }.badge)

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
            "En mora · cuota Agosto 2026 vencida hace 29 días · debe $12.848",
            vm.socios.first { it.id == 2 }.motivo,
        )
    }

    // ------------------------------------------------------------------ v1.5 · gracia, recargo e interés

    @Test
    fun `la gracia configurada decide si la cuota vencida bloquea`() {
        val vm = admin()
        assertEquals("por defecto 5 días", 5, vm.parametros.graciaDias)
        val lucasAntes = vm.socios.first { it.id == 2 }
        assertTrue("29 días vencida supera la gracia", lucasAntes.mora)
        assertFalse(lucasAntes.hab)

        vm.prepararParametros()
        vm.campo { copy(pGracia = "30") }
        vm.guardarParametros()

        val lucas = vm.socios.first { it.id == 2 }
        assertTrue(lucas.enGracia)
        assertTrue(lucas.hab)
        assertFalse(lucas.mora)
        assertEquals("Habilitado · En gracia", lucas.badge)
        assertEquals("En gracia", lucas.badgeCorto)
        assertEquals("Queda 1 día de gracia", lucas.contador)
        assertEquals(TonoCuota.AVISO, lucas.tono)
        assertEquals("Tomás (60 días) sigue en mora", setOf(6), vm.enMora.map { it.id }.toSet())

        vm.ponerDniAcceso(40_120_334L)
        assertEquals("Permitido", vm.estado.acceso?.titulo)
        assertTrue(vm.estado.acceso!!.detalle.contains("gracia"))
    }

    @Test
    fun `el recargo y el interes vigentes se suman a la deuda del moroso`() {
        val vm = admin()
        vm.prepararParametros()
        vm.campo { copy(pGracia = "5", pRecargo = "500", pInteres = "3") }
        vm.guardarParametros()

        // La demo trae un 3 % vigente desde hace dos meses: los 29 días vencidos devengan.
        val lucas = vm.socios.first { it.id == 2 } // $12.000, vencida hace 29 días
        assertEquals(500.0, lucas.recargo, 0.001)
        assertEquals("12.000 × 3% / 30 × 29 días", 348.0, lucas.interes, 0.01)
        assertEquals(12_848, lucas.totalAdeudado)
        assertEquals("Cuota $12.000 + recargo $500 + interés $348", lucas.deudaDetalle)
        assertEquals(TonoCuota.BLOQUEO, lucas.tono)
        assertEquals("Vencida hace 29 días · gracia de 5 días superada", lucas.contador)

        val tomas = vm.socios.first { it.id == 6 } // $17.000, vencida hace 60 días → $1.020 de interés
        assertEquals(18_520, tomas.totalAdeudado)
        vm.elegirReportTab(ReportTab.MORA)
        assertEquals(money(12_848 + 18_520), vm.totalReporte)

        vm.abrirSocio(2)
        vm.pedirRegistrarPago()
        vm.estado.sheet!!.onConfirm()
        val pagado = vm.socios.first { it.id == 2 }
        assertTrue(pagado.cuotaPagada)
        assertEquals("se cobra el total con recargo e interés", 12_848, pagado.historial.first().monto)
        assertTrue(pagado.hab)
    }

    @Test
    fun `la demo trae un socio en gracia con el carnet por vencer`() {
        val vm = admin()
        val rocio = vm.socios.first { it.id == 9 }
        assertTrue(rocio.enGracia)
        assertTrue(rocio.hab)
        assertEquals("Habilitado · En gracia", rocio.badge)
        assertEquals("Quedan 2 días", rocio.estadoCorto)
        assertEquals("Por vencer", rocio.carnetEstado)
        assertEquals("3 días al 3 % sobre $12.000 más el recargo", 12_000 + 500 + 36, rocio.totalAdeudado)
        vm.ponerDniAcceso(39_876_540L)
        assertEquals("Permitido", vm.estado.acceso?.titulo)
        assertTrue(vm.estado.acceso!!.detalle.contains("gracia"))
    }

    @Test
    fun `las capsulas de estado y de cuota resumen la situacion en pocas palabras`() {
        val vm = admin()
        val lucas = vm.socios.first { it.id == 2 } // mora, con la tasa de demo vigente
        assertEquals("Debe $12.848", lucas.estadoCorto)
        assertEquals("Vencida hace 29 días", lucas.contadorCorto)
        assertEquals("Venció el 10/08/2026 · gracia de 5 días superada", lucas.cuotaSubLabel)

        val andres = vm.socios.first { it.id == 1 }
        assertEquals("Vence en 2 días", andres.estadoCorto)
        assertEquals("Vence en 2 días", andres.contadorCorto)
        assertEquals("Vencimiento 10/09/2026", andres.cuotaSubLabel)

        val camila = vm.socios.first { it.id == 7 }
        assertEquals("Al día", camila.estadoCorto)
        assertEquals("Pagada", camila.contadorCorto)
        assertEquals("Próxima cuota 10/10/2026", camila.cuotaSubLabel)

        assertEquals("Renovar carnet", vm.socios.first { it.id == 3 }.estadoCorto)

        vm.prepararParametros()
        vm.campo { copy(pGracia = "30") }
        vm.guardarParametros()
        val enGracia = vm.socios.first { it.id == 2 }
        assertEquals("Queda 1 día", enGracia.estadoCorto)
        assertEquals("Queda 1 día de gracia", enGracia.contadorCorto)
        assertEquals("Venció el 10/08/2026 · gracia de 30 días", enGracia.cuotaSubLabel)
    }

    @Test
    fun `el detalle de la deuda siempre suma el total aunque haya centavos`() {
        val vm = admin()
        vm.prepararParametros()
        vm.campo { copy(pGracia = "5", pRecargo = "500,5", pInteres = "2,9") }
        vm.guardarParametros()
        // 28 días al 3 % de la demo ($336) y hoy al 2,9 % ($11,6).
        val lucas = vm.socios.first { it.id == 2 }
        assertEquals((12_000 + 500.5 + 336 + 11.6).roundToInt(), lucas.totalAdeudado)
        assertEquals("Cuota $12.000 + recargo $501 + interés $347", lucas.deudaDetalle)
    }

    @Test
    fun `los parametros rechazan valores que no son numeros finitos`() {
        val vm = admin()
        vm.prepararParametros()
        vm.campo { copy(pRecargo = "NaN") }
        vm.guardarParametros()
        assertNotNull(vm.estado.paramError)
        vm.campo { copy(pRecargo = "Infinity") }
        vm.guardarParametros()
        assertNotNull(vm.estado.paramError)
        vm.campo { copy(pRecargo = "2000000") }
        vm.guardarParametros()
        assertNotNull("tope del recargo", vm.estado.paramError)
        assertEquals("nada se guardó: siguen las dos filas de la demo", 2, vm.historialParametros.size)
        assertEquals(500.0, vm.parametros.recargo, 0.0)
    }

    @Test
    fun `los parametros guardan su vigencia y el interes se calcula por tramos`() {
        val vm = admin()
        vm.prepararParametros()
        vm.campo { copy(pInteres = "2") }
        vm.guardarParametros()
        assertEquals("la demo trae dos filas (2025 y 2026) y se suma la de hoy", 3, vm.historialParametros.size)
        vm.campo { copy(pInteres = "2") }
        vm.guardarParametros()
        assertEquals("guardar de nuevo el mismo día reemplaza", 3, vm.historialParametros.size)
        assertNull(vm.estado.paramError)

        Reloj.fijar(Fecha(2026, 9, 9))
        vm.campo { copy(pInteres = "4") }
        vm.guardarParametros()
        assertEquals(4, vm.historialParametros.size)
        assertEquals(4.0, vm.parametros.interesMensual, 0.0)
        assertEquals(2.0, vm.estado.parametros.vigenteEn(Fecha(2026, 9, 8)).interesMensual, 0.0)
        assertEquals("la tasa de 2025 de la demo", 2.5, vm.estado.parametros.vigenteEn(Fecha(2026, 1, 15)).interesMensual, 0.0)
        val antes = vm.estado.parametros.vigenteEn(Fecha(2025, 1, 1))
        assertEquals("antes del primer parámetro no se devenga interés", 0.0, antes.interesMensual, 0.0)
        assertEquals("ni recargo", 0.0, antes.recargo, 0.0)
        assertEquals("pero la gracia sigue siendo la más antigua", 10, antes.graciaDias)

        // Lucas: 30 días vencida; 28 días al 3 % de la demo, el 8/9 al 2 % y el 9/9 al 4 %, sobre $12.000.
        val lucas = vm.socios.first { it.id == 2 }
        assertEquals(28 * 12_000 * 0.03 / 30 + 12_000 * 0.02 / 30 + 12_000 * 0.04 / 30, lucas.interes, 0.01)

        vm.campo { copy(pGracia = "x") }
        vm.guardarParametros()
        assertNotNull("valida el formulario", vm.estado.paramError)
        assertEquals("con error no guarda nada", 4, vm.historialParametros.size)
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

    @Test
    fun `HU-04 - el cobro en caja abre la cuota siguiente y deja comprobante`() {
        val vm = admin()
        vm.abrirSocio(2) // Lucas: Agosto 2026, vencida el 10/08
        vm.pedirRegistrarPago()
        vm.estado.sheet!!.onConfirm()

        val lucas = vm.socios.first { it.id == 2 }
        assertEquals("un mes después del vencimiento original", fecha("2026-09-10"), lucas.cuota.proximaVence)
        assertEquals("Caja", lucas.historial.first().medio)
        assertTrue(lucas.historial.first().comprobante.startsWith("CD-2609-"))
        assertEquals("Pagada · próxima 10/09/2026", lucas.cuotaVenceLabel)
    }

    @Test
    fun `HU-04 - una cuota pagada no se cobra dos veces`() {
        val vm = socio()
        vm.ir(Screen.S_PAGAR)
        vm.confirmarPago()
        val historial = vm.yo.historial.size
        val comprobante = vm.estado.comprobante

        vm.ir(Screen.S_PAGAR)
        vm.confirmarPago()

        assertEquals("no suma otro pago", historial, vm.yo.historial.size)
        assertEquals("conserva el comprobante original", comprobante, vm.estado.comprobante)
        assertEquals("no navega al comprobante", Screen.S_PAGAR, vm.pantalla)

        val adm = admin()
        adm.abrirSocio(7) // Camila ya pagó septiembre
        adm.pedirRegistrarPago()
        assertNull("no abre la hoja de cobro", adm.estado.sheet)
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
        assertEquals("falta el apellido", "Ingresá el apellido.", vm.estado.altaError)

        vm.campo { copy(nombre = "", apellido = "Ferreyra") }
        vm.confirmarAlta()
        assertEquals("falta el nombre", "Ingresá el nombre.", vm.estado.altaError)

        vm.campo { copy(nombre = "Marcos", apellido = "Ferreyra", dni = "123") }
        vm.confirmarAlta()
        assertTrue(vm.estado.altaError!!.contains("7 u 8"))

        vm.campo { copy(dni = "38412907") }
        vm.confirmarAlta()
        assertTrue(vm.estado.altaError!!.contains("Ya existe"))
    }

    @Test
    fun `HU-02 - el alta rechaza formatos incorrectos y datos faltantes`() {
        val vm = admin()
        vm.irAAlta()

        vm.campo { copy(nombre = "Marcos", apellido = "Ferr3yra", dni = "30111222", mail = "m@club.com") }
        vm.confirmarAlta()
        assertTrue(vm.estado.altaError!!.contains("letras"))

        vm.campo { copy(apellido = "Ferreyra", dni = "30A11222") }
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

        vm.campo { copy(nombre = "Marcos", apellido = "Ferreyra", dni = "30111222", mail = "marcos@club.com") }
        vm.elegirPlan(2)
        vm.confirmarAlta()

        assertEquals(cuantos + 1, vm.estado.socios.size)
        assertEquals(Screen.E_SOCIO, vm.pantalla)

        val nuevo = vm.seleccionado
        assertEquals("nombre y apellido juntos", "Marcos Ferreyra", nuevo.nombre)
        assertEquals("01509", nuevo.nro)
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
    fun `HU-03 - con cupo se registra el pago diario con fecha y medio`() {
        val vm = admin()
        vm.irAVisitanteNuevo()
        vm.campo { copy(vNombre = "Pedro Gómez", vDni = "30111222") }
        vm.elegirActividadVisitante(vm.actividades.first { it.id == "func" })
        vm.elegirMedioVisitante(MedioVisitante.DEBITO)
        val libresAntes = vm.actividades.first { it.id == "func" }.libres
        val antes = vm.estado.visitantes.size

        vm.confirmarVisitante()

        assertEquals(antes + 1, vm.estado.visitantes.size)
        val v = vm.estado.visitantes.first()
        assertEquals("30.111.222", v.dni)
        assertEquals(HOY, v.fecha)
        assertEquals("Débito", v.medio)
        assertEquals(6_000, v.monto)
        assertEquals("el cupo del día baja en uno", libresAntes - 1, vm.actividades.first { it.id == "func" }.libres)
        assertEquals("la base de ocupados no se toca", 14, vm.estado.acts.first { it.id == "func" }.ocupados)
        assertEquals(Screen.E_VISITANTES, vm.pantalla)
    }

    @Test
    fun `HU-03 - el cupo de hoy cuenta a los visitantes de hoy`() {
        val vm = admin()
        val funcional = vm.actividades.first { it.id == "func" }
        assertEquals("14 de base + Pedro y Tomás; Julián fue el lunes", 16, funcional.ocupadosHoy)
        assertEquals(4, funcional.libres)
        assertEquals("9 de base + Lucía", 10, vm.actividades.first { it.id == "yoga" }.ocupadosHoy)
        assertTrue("Natación sigue sin cupo", vm.actividades.first { it.id == "nat" }.sinCupo)
    }

    @Test
    fun `HU-03 - un DNI ya cobrado hoy no se registra dos veces`() {
        val vm = admin()
        vm.irAVisitanteNuevo()
        // Sin puntos: hoy figura como "33.120.455".
        vm.campo { copy(vNombre = "Pedro Alonso", vDni = "33120455") }
        vm.elegirActividadVisitante(vm.actividades.first { it.id == "func" })
        val antes = vm.estado.visitantes.size

        vm.confirmarVisitante()

        assertEquals(antes, vm.estado.visitantes.size)
        assertEquals("Ya hay un pago diario registrado hoy con el DNI 33.120.455.", vm.estado.visError)
        assertEquals(Screen.E_VISITANTE_NUEVO, vm.pantalla)

        // El mismo DNI de un día anterior sí puede pagar hoy.
        vm.campo { copy(vNombre = "Carla Núñez", vDni = "40.318.902") }
        vm.elegirActividadVisitante(vm.actividades.first { it.id == "func" })
        vm.confirmarVisitante()
        assertEquals(antes + 1, vm.estado.visitantes.size)
    }

    // ------------------------------------------------------------------ HU-14

    @Test
    fun `HU-14 - vencen hoy, esta semana y mora salen de pestanias distintas`() {
        val vm = admin()
        assertEquals("arranca en la pestaña de hoy", ReportTab.HOY, vm.estado.reportTab)
        assertEquals(listOf(8), vm.vencenHoy.map { it.id })
        assertEquals(money(18_500), vm.totalReporte)

        vm.elegirReportTab(ReportTab.VENCER)
        assertEquals("ordenadas por vencimiento y sin las de hoy", listOf(2, 4), vm.porVencer.map { daysTo(it.cuota.vence) })
        assertEquals(money(18_500 + 12_000), vm.totalReporte)

        vm.elegirReportTab(ReportTab.MORA)
        assertEquals(setOf(2, 6), vm.enMora.map { it.id }.toSet())
        // Con la tasa de demo vigente (recargo $500, 3 % mensual): Lucas $12.848 y Tomás $18.520.
        assertEquals(money(12_848 + 18_520), vm.totalReporte)
    }

    @Test
    fun `HU-14 - los pagos diarios se consultan por fecha`() {
        val vm = admin()
        vm.elegirReportTab(ReportTab.VISITANTES)
        assertEquals(3, vm.pagosDiarios.size)
        assertEquals(money(17_000), vm.totalReporte)
        assertEquals("Hoy", vm.fechaReporteSub)
        assertFalse(vm.puedeAvanzarDia)

        vm.diaReporteSiguiente()
        assertEquals("en hoy no avanza", HOY, vm.estado.fechaReporte)

        vm.diaReporteAnterior()
        assertEquals(fecha("2026-09-07"), vm.estado.fechaReporte)
        assertEquals(listOf("Carla Núñez", "Julián Soto"), vm.pagosDiarios.map { it.nombre })
        assertEquals(money(14_000), vm.totalReporte)
        assertEquals("Ayer", vm.fechaReporteSub)
        assertTrue(vm.puedeAvanzarDia)

        vm.diaReporteAnterior()
        assertTrue("un día sin pagos queda vacío", vm.pagosDiarios.isEmpty())
        assertEquals(money(0), vm.totalReporte)
        assertEquals("Dom 6 sep", vm.fechaReporteSub)
    }

    @Test
    fun `HU-14 - cobrar la cuota que vence hoy la saca del listado`() {
        val vm = admin()
        vm.abrirSocio(8)
        vm.pedirRegistrarPago()
        vm.estado.sheet!!.onConfirm()
        assertTrue(vm.vencenHoy.isEmpty())
        assertEquals(money(0), vm.totalReporte)
    }

    @Test
    fun `HU-14 - los ingresos de hoy cuentan solo a los visitantes de hoy`() {
        val vm = admin()
        assertEquals(3, vm.visitantesDeHoy.size)
        assertEquals(17_000, vm.totalPagosHoy)
        assertEquals("1 socio permitido + 3 visitantes", 4, vm.ingresosDeHoy)
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
        assertEquals("incluye a Rocío, que está en gracia", 6, vm.sociosFiltrados.size)
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
