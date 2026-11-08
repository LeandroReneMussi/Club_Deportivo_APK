package ar.edu.ifts29.clubdeportivo

import androidx.activity.ComponentActivity
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ar.edu.ifts29.clubdeportivo.data.FiltroSocios
import ar.edu.ifts29.clubdeportivo.data.MedioVisitante
import ar.edu.ifts29.clubdeportivo.data.ReportTab
import ar.edu.ifts29.clubdeportivo.data.Screen
import ar.edu.ifts29.clubdeportivo.vm.ClubRepositorio
import ar.edu.ifts29.clubdeportivo.vm.ClubViewModel
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.io.File

/**
 * Recorre las pantallas de cada Activity y guarda una captura de cada una, con su
 * epígrafe, para el PDF de la entrega. Las capturas quedan en
 * /sdcard/Android/data/ar.edu.ifts29.clubdeportivo/files/capturas.
 *
 * Los estados se preparan con las mismas acciones del ViewModel que disparan los botones;
 * lo que se fotografía es la interfaz real de la app.
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CapturasPorActivityTest {

    companion object {
        private val instr get() = InstrumentationRegistry.getInstrumentation()
        private val dir: File by lazy {
            File(instr.targetContext.getExternalFilesDir(null), "capturas").apply { mkdirs() }
        }

        @BeforeClass
        @JvmStatic
        fun limpiar() {
            dir.listFiles()?.forEach { it.delete() }
        }
    }

    private fun sesion(usuario: String?, clave: String = "") {
        instr.runOnMainSync {
            ClubRepositorio.reiniciar()
            if (usuario != null) {
                ClubViewModel().apply {
                    campo { copy(email = usuario, pass = clave) }
                    login()
                }
            }
        }
    }

    private inner class Recorrido<A : ComponentActivity>(
        private val scenario: ActivityScenario<A>,
        private val prefijo: String,
        private val activity: String,
    ) {
        private var n = 0

        fun vm(accion: ClubViewModel.() -> Unit) {
            scenario.onActivity { ViewModelProvider(it)[ClubViewModel::class.java].accion() }
        }

        fun foto(pantalla: String, hu: String, descripcion: String, espera: Long = 900) {
            instr.waitForIdleSync()
            Thread.sleep(espera)
            n++
            val nombre = "%s_%02d.png".format(prefijo, n)
            val bmp: Bitmap = instr.uiAutomation.takeScreenshot()
            File(dir, nombre).outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            File(dir, "indice.txt").appendText("$nombre|$activity|$pantalla|$hu|$descripcion\n")
        }

        /** Espera a que se oculte el toast de la acción anterior, para que no tape la próxima captura. */
        fun sinToast() {
            repeat(40) {
                var visible = false
                scenario.onActivity { visible = ViewModelProvider(it)[ClubViewModel::class.java].estado.toast != null }
                if (!visible) return
                Thread.sleep(100)
            }
        }

        fun ir(screen: Screen) {
            sinToast()
            vm { ir(screen) }
        }
    }

    @Test
    fun a_loginActivity() {
        sesion(null)
        ActivityScenario.launch(LoginActivity::class.java).use { sc ->
            val r = Recorrido(sc, "1_LoginActivity", "LoginActivity")
            r.foto("Inicio de sesión", "HU-01", "Entry point de la app. Las cuentas de prueba completan el formulario.")
            r.vm {
                campo { copy(email = "", pass = "") }
                login()
            }
            r.foto("Inicio de sesión", "HU-01", "Validación: datos incompletos (usuario y contraseña vacíos).")
            r.vm {
                campo { copy(email = "Andres", pass = "incorrecta") }
                login()
            }
            r.foto("Inicio de sesión", "HU-01", "Validación: credenciales incorrectas, el acceso se rechaza.")
        }
    }

    @Test
    fun b_socioActivity() {
        sesion("Andres", "Socio123")
        ActivityScenario.launch(SocioActivity::class.java).use { sc ->
            val r = Recorrido(sc, "2_SocioActivity", "SocioActivity")
            r.foto("Inicio", "HU-04", "Estado de habilitación, cuota pendiente, rutina de hoy y turno de nutrición.")
            r.ir(Screen.S_CARNET)
            r.foto("Carnet digital", "HU-06", "Carnet vigente con datos del socio y estado de cuota.")
            r.ir(Screen.S_CUOTAS)
            r.foto("Estado de cuenta", "HU-04", "Cuota actual e historial de pagos.")
            r.ir(Screen.S_PAGAR)
            r.foto("Método de pago", "HU-04", "Selección del medio de pago (pago simulado).")
            r.vm { confirmarPago() }
            r.foto("Pago registrado", "HU-04", "Comprobante del pago; el socio queda al día.")
            r.ir(Screen.S_RUTINA)
            r.foto("Mi rutina", "HU-10", "Rutina asignada por la entrenadora, por día.")
            r.ir(Screen.S_NUTRI)
            r.foto("Nutrición", "HU-13", "Peso, altura, IMC, objetivo y consultas anteriores.")
            r.vm { pedirCancelarTurno() }
            r.foto("Cancelar turno", "HU-11", "Confirmación antes de cancelar el turno reservado.")
            r.vm { estado.sheet?.onConfirm?.invoke() }
            r.vm { ir(Screen.S_TURNO) }
            r.vm { confirmarTurno() }
            r.foto("Reservar turno", "HU-11", "Validación: confirmar sin elegir horario.", espera = 400)
            r.vm { elegirSlot(slots.first().horas.first { !it.ocupado }.key) }
            r.foto("Reservar turno", "HU-11", "Horario elegido entre los disponibles.")
            r.vm { confirmarTurno() }
            r.foto("Nutrición", "HU-11", "Turno confirmado.", espera = 400)
            r.ir(Screen.PERFIL)
            r.foto("Perfil", "HU-01", "Datos del socio y accesos rápidos.")
            r.vm { pedirCerrarSesion() }
            r.foto("Cerrar sesión", "HU-01", "Confirmación de cierre de sesión: vuelve a LoginActivity.")
        }
    }

    @Test
    fun c_adminActivity() {
        sesion("Jorge", "Admin123")
        ActivityScenario.launch(AdminActivity::class.java).use { sc ->
            val r = Recorrido(sc, "3_AdminActivity", "AdminActivity")
            r.foto("Inicio", "HU-14", "Indicadores del día y acciones rápidas.")
            r.ir(Screen.E_SOCIOS)
            r.foto("Socios", "CRUD socios: Leer", "Padrón de socios con búsqueda y filtros.")
            r.vm { elegirFiltro(FiltroSocios.MORA) }
            r.foto("Socios", "CRUD socios: Leer", "Filtro: socios en mora.")
            r.vm {
                elegirFiltro(FiltroSocios.TODOS)
                abrirSocio(2)
            }
            r.foto("Ficha del socio", "HU-04 / HU-07", "Socio en mora: cobro en caja, carnet, editar y dar de baja.")
            r.vm { pedirRegistrarPago() }
            r.foto("Registrar pago", "HU-04", "Confirmación del cobro en caja.")
            r.vm { cerrarSheet() }
            r.vm { abrirSocio(3) }
            r.vm { pedirRenovarCarnet() }
            r.foto("Renovar carnet", "HU-07", "Carnet vencido: emisión de un carnet nuevo por 12 meses.")
            r.vm { cerrarSheet() }
            r.vm { irAAlta() }
            r.foto("Alta de socio", "HU-02 / CRUD socios: Crear", "Formulario de alta.")
            r.vm {
                campo { copy(nombre = "Carlos", dni = "12.345.678", mail = "carlos@gardel.com.ar") }
                confirmarAlta()
            }
            r.foto("Alta de socio", "HU-02", "Validación: falta el apellido.")
            r.vm {
                campo { copy(apellido = "Gardel", dni = "12A45678") }
                confirmarAlta()
            }
            r.foto("Alta de socio", "HU-02", "Validación: DNI con formato incorrecto.")
            r.vm {
                campo { copy(dni = "12.345.678", mail = "carlos@gardel") }
                confirmarAlta()
            }
            r.foto("Alta de socio", "HU-02", "Validación: correo con formato incorrecto.")
            r.vm {
                campo { copy(dni = "38.412.907", mail = "carlos@gardel.com.ar") }
                confirmarAlta()
            }
            r.foto("Alta de socio", "HU-02", "Validación: ya existe un socio con ese DNI.")
            r.vm {
                campo { copy(dni = "12.345.678") }
                confirmarAlta()
            }
            r.foto("Ficha del socio", "HU-02 / CRUD socios: Crear", "Alta registrada: N° de socio, carnet y cuota inicial.", espera = 500)
            r.sinToast()
            r.vm { irAEditar() }
            r.foto("Editar socio", "CRUD socios: Actualizar", "Formulario precargado para modificar los datos.")
            r.vm {
                campo { copy(mail = "tango@gardel.com.ar") }
                confirmarAlta()
            }
            r.foto("Ficha del socio", "CRUD socios: Actualizar", "Datos actualizados.", espera = 500)
            r.vm { pedirBajaSocio() }
            r.foto("Dar de baja", "CRUD socios: Eliminar", "Confirmación antes de dar de baja al socio.")
            r.vm { estado.sheet?.onConfirm?.invoke() }
            r.foto("Socios", "CRUD socios: Eliminar", "Socio dado de baja: ya no figura en el padrón.", espera = 500)
            r.ir(Screen.E_ACCESO)
            r.vm { ponerDniAcceso(38412907) }
            r.foto("Control de acceso", "HU-05", "Socio al día: acceso permitido.")
            r.vm { ponerDniAcceso(40120334) }
            r.foto("Control de acceso", "HU-05", "Socio en mora: acceso denegado con el motivo.")
            r.vm { ponerDniAcceso(35887210) }
            r.foto("Control de acceso", "HU-05", "Carnet vencido: acceso denegado.")
            r.vm { ponerDniAcceso(99999999) }
            r.foto("Control de acceso", "HU-05", "DNI inexistente: no encontrado.")
            r.ir(Screen.E_VISITANTES)
            r.foto("Visitantes", "HU-03", "Cupos del día y pagos diarios de hoy, con el total recaudado.")
            r.vm { irAVisitanteNuevo() }
            r.vm { elegirActividadVisitante(actividades.first { it.id == "nat" }) }
            r.foto("Ingreso de visitante", "HU-03", "Natación sin cupo: el ingreso no puede realizarse.")
            r.vm {
                campo { copy(vNombre = "Pedro Alonso", vDni = "33.120.455") }
                elegirActividadVisitante(actividades.first { !it.sinCupo })
                confirmarVisitante()
            }
            r.foto("Ingreso de visitante", "HU-03", "Validación: ese DNI ya tiene un pago diario registrado hoy.")
            r.vm {
                campo { copy(vNombre = "Marina López", vDni = "31.555.908") }
                elegirMedioVisitante(MedioVisitante.DEBITO)
            }
            r.foto("Ingreso de visitante", "HU-03", "Actividad con cupo, medio de pago y total: listo para registrar el pago diario.")
            r.vm { confirmarVisitante() }
            r.foto("Visitantes", "HU-03", "Pago diario registrado: entra en la lista de hoy y suma al total.", espera = 500)
            r.sinToast()
            r.ir(Screen.E_REPORTES)
            r.vm { elegirReportTab(ReportTab.HOY) }
            r.foto("Reportes", "HU-14", "Socios cuya cuota vence hoy, consultados en SQLite.")
            r.vm { elegirReportTab(ReportTab.VENCER) }
            r.foto("Reportes", "HU-14", "Cuotas que vencen en los próximos 7 días.")
            r.vm { elegirReportTab(ReportTab.MORA) }
            r.foto("Reportes", "HU-14", "Socios en mora con acceso bloqueado.")
            r.vm { elegirReportTab(ReportTab.VISITANTES) }
            r.foto("Reportes", "HU-14 / HU-03", "Pagos diarios de no socios de hoy, con el total del día.")
            r.vm { diaReporteAnterior() }
            r.foto("Reportes", "HU-14 / HU-03", "Pagos diarios de un día anterior, recuperados por fecha.")
        }
    }

    @Test
    fun d_entrenadoraActivity() {
        sesion("Ana", "Prof123")
        ActivityScenario.launch(EntrenadoraActivity::class.java).use { sc ->
            val r = Recorrido(sc, "4_EntrenadoraActivity", "EntrenadoraActivity")
            r.foto("Inicio", "HU-08", "Presencia del día, clases y rutinas pendientes.")
            r.vm { marcarPresencia() }
            r.foto("Inicio", "HU-08", "Presencia registrada.", espera = 400)
            r.sinToast()
            r.vm { abrirAsistencia("musc") }
            r.foto("Asistencia", "HU-08", "Presentes y ausentes de la clase; socios no habilitados señalados.")
            r.ir(Screen.P_RUTINAS)
            r.foto("Rutinas", "HU-09", "Socios asignados y estado de su rutina.")
            r.vm { abrirRutina(2) }
            r.foto("Rutina del socio", "HU-09", "Socio en mora: no se puede asignar rutina.")
            r.ir(Screen.P_RUTINAS)
            r.vm { abrirRutina(5) }
            r.foto("Rutina del socio", "HU-09", "Socio sin ficha médica: no se puede asignar rutina.")
            r.ir(Screen.P_RUTINAS)
            r.vm { abrirRutina(1) }
            r.foto("Rutina del socio", "HU-09", "Edición de la rutina de un socio habilitado.")
            r.vm {
                campo { copy(ejNombre = "Sentadilla", ejSeries = "0", ejReps = "10") }
                agregarEjercicio()
            }
            r.foto("Rutina del socio", "HU-09", "Validación: series fuera de rango.", espera = 400)
            r.ir(Screen.PERFIL)
            r.foto("Perfil", "HU-01", "Datos de la entrenadora.")
        }
    }

    @Test
    fun e_nutricionistaActivity() {
        sesion("Leandro", "Nut123")
        ActivityScenario.launch(NutricionistaActivity::class.java).use { sc ->
            val r = Recorrido(sc, "5_NutricionistaActivity", "NutricionistaActivity")
            r.foto("Agenda", "HU-11", "Turnos asignados por día.")
            r.ir(Screen.N_FICHAS)
            r.foto("Fichas nutricionales", "HU-12", "Socios con su último peso registrado.")
            r.vm { abrirFicha(1) }
            r.foto("Ficha nutricional", "HU-12", "Medidas, objetivo, antecedentes e historial de consultas.")
            r.vm {
                campo { copy(nPeso = "abc") }
                guardarConsulta()
            }
            r.foto("Ficha nutricional", "HU-12", "Validación: peso con formato incorrecto.", espera = 400)
            r.vm {
                campo { copy(nPeso = "62,9", nAltura = "168") }
                guardarConsulta()
            }
            r.foto("Ficha nutricional", "HU-12", "Validación: altura fuera de rango (se pide en metros).", espera = 400)
            r.vm {
                campo { copy(nPeso = "62,9", nAltura = "1,68", nObs = "Control mensual.") }
                guardarConsulta()
            }
            r.foto("Ficha nutricional", "HU-12", "Consulta guardada en el historial.", espera = 400)
            r.ir(Screen.PERFIL)
            r.foto("Perfil", "HU-01", "Datos de la nutricionista.")
        }
    }

    @Test
    fun f_guardaDeRol() {
        // Abrir la Activity de un rol sin sesión devuelve al login.
        sesion(null)
        ActivityScenario.launch(AdminActivity::class.java).use { sc ->
            instr.waitForIdleSync()
            assertEquals(androidx.lifecycle.Lifecycle.State.DESTROYED, sc.state)
        }
    }
}
