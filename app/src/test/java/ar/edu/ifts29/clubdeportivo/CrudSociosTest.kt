package ar.edu.ifts29.clubdeportivo

import ar.edu.ifts29.clubdeportivo.data.Screen
import ar.edu.ifts29.clubdeportivo.vm.ClubRepositorio
import ar.edu.ifts29.clubdeportivo.vm.ClubViewModel
import ar.edu.ifts29.clubdeportivo.vm.ID_SOCIO_APP
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

/** Modificación y baja de socios (el alta y la consulta están en ClubViewModelTest). */
@OptIn(ExperimentalCoroutinesApi::class)
class CrudSociosTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        ClubRepositorio.reiniciar()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun admin() = ClubViewModel().apply {
        campo { copy(email = "Jorge", pass = "Admin123") }
        login()
    }

    @Test
    fun `editar precarga los datos y guarda los cambios`() {
        val vm = admin()
        vm.abrirSocio(2)
        vm.irAEditar()
        assertEquals(Screen.E_EDITAR, vm.pantalla)
        assertEquals("Lucas Benítez", vm.estado.campos.nombre)
        assertEquals("40120334", vm.estado.campos.dni)

        vm.campo { copy(nombre = "Lucas Benítez Paz", mail = "lucas@club.com") }
        vm.elegirPlan(2)
        vm.confirmarAlta()

        val s = vm.socios.first { it.id == 2 }
        assertEquals("Lucas Benítez Paz", s.nombre)
        assertEquals("lucas@club.com", s.socio.email)
        assertEquals("Familiar", s.plan)
        assertEquals("no crea un socio nuevo", 7, vm.socios.size)
        assertEquals(Screen.E_SOCIO, vm.pantalla)
        assertNull(vm.estado.editandoId)
    }

    @Test
    fun `editar valida igual que el alta y no choca con su propio DNI`() {
        val vm = admin()
        vm.abrirSocio(2)
        vm.irAEditar()
        vm.campo { copy(mail = "lucas@club.com") }

        vm.campo { copy(dni = "38412907") } // DNI de otro socio
        vm.confirmarAlta()
        assertTrue(vm.estado.altaError!!.contains("Ya existe"))

        vm.campo { copy(dni = "40120334") } // su propio DNI
        vm.confirmarAlta()
        assertNull(vm.estado.altaError)
    }

    @Test
    fun `la baja pide confirmacion y quita al socio de clases y asistencias`() {
        val vm = admin()
        vm.abrirSocio(4)
        vm.pedirBajaSocio()
        assertNotNull("pide confirmación", vm.estado.sheet)
        assertEquals("todavía no se borró", 7, vm.socios.size)

        vm.estado.sheet!!.onConfirm()

        assertEquals(6, vm.socios.size)
        assertTrue(vm.socios.none { it.id == 4 })
        assertTrue(vm.estado.acts.none { 4 in it.inscriptos })
        assertTrue(vm.estado.asistencia.values.none { 4 in it })
        assertEquals(Screen.E_SOCIOS, vm.pantalla)
    }

    @Test
    fun `un alta despues de una baja no repite el numero de socio`() {
        val vm = admin()
        vm.irAAlta()
        vm.campo { copy(nombre = "Marcos Ferreyra", dni = "30111222", mail = "marcos@club.com") }
        vm.confirmarAlta()
        val primero = vm.seleccionado.nro

        vm.abrirSocio(3)
        vm.pedirBajaSocio()
        vm.estado.sheet!!.onConfirm()

        vm.irAAlta()
        vm.campo { copy(nombre = "Paula Rinaldi", dni = "31222333", mail = "paula@club.com") }
        vm.confirmarAlta()
        val nros = vm.socios.map { it.nro }
        assertEquals(nros.size, nros.toSet().size)
        assertTrue(primero in nros)
    }

    @Test
    fun `el socio dado de baja no puede iniciar sesion`() {
        val vm = admin()
        vm.abrirSocio(ID_SOCIO_APP)
        vm.pedirBajaSocio()
        vm.estado.sheet!!.onConfirm()
        vm.cerrarSesion()

        val login = ClubViewModel()
        login.campo { copy(email = "Andres", pass = "Socio123") }
        login.login()
        assertTrue(login.estado.loginError)
        assertNull(login.estado.rol)
        assertFalse(login.muestraNav)
    }
}
