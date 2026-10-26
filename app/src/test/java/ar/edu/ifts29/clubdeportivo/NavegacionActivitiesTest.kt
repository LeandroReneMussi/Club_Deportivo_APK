package ar.edu.ifts29.clubdeportivo

import ar.edu.ifts29.clubdeportivo.data.Rol
import ar.edu.ifts29.clubdeportivo.data.Screen
import ar.edu.ifts29.clubdeportivo.vm.ClubRepositorio
import ar.edu.ifts29.clubdeportivo.vm.ClubViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * La app tiene una Activity por rol y cada una crea su propio ViewModel. Estas pruebas
 * cubren lo que tiene que pasar entre Activities: a dónde lleva cada rol, qué pantalla
 * abre primero y que los datos y la sesión sobrevivan al cambio.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NavegacionActivitiesTest {

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

    @Test
    fun `cada rol abre su Activity`() {
        assertEquals(SocioActivity::class.java, activityDelRol(Rol.SOCIO))
        assertEquals(AdminActivity::class.java, activityDelRol(Rol.ADMIN))
        assertEquals(AdminActivity::class.java, activityDelRol(Rol.EMPLEADO))
        assertEquals(EntrenadoraActivity::class.java, activityDelRol(Rol.PROFESOR))
        assertEquals(NutricionistaActivity::class.java, activityDelRol(Rol.NUTRICIONISTA))
    }

    @Test
    fun `sin sesion la app arranca en el login`() {
        val vm = ClubViewModel()
        assertNull(vm.estado.rol)
        assertEquals(Screen.LOGIN, vm.pantalla)
    }

    @Test
    fun `la Activity del rol abre en la primera pestana del rol`() {
        ingresar("Jorge", "Admin123")
        // La Activity del administrativo crea su propio ViewModel.
        val enAdmin = ClubViewModel()
        assertEquals(Rol.ADMIN, enAdmin.estado.rol)
        assertEquals(Screen.E_HOME, enAdmin.pantalla)

        ClubRepositorio.reiniciar()
        ingresar("Leandro", "Nut123")
        assertEquals(Screen.N_AGENDA, ClubViewModel().pantalla)
    }

    @Test
    fun `cerrar sesion la cierra para la proxima Activity`() {
        ingresar("Andres", "Socio123")
        val enSocio = ClubViewModel()
        enSocio.cerrarSesion()
        assertNull(ClubRepositorio.rol)
        assertEquals(Screen.LOGIN, ClubViewModel().pantalla)
    }

    @Test
    fun `un alta hecha por el administrativo la ve la entrenadora`() {
        ingresar("Jorge", "Admin123")
        val admin = ClubViewModel()
        admin.irAAlta()
        admin.campo { copy(nombre = "Carlos Gardel", dni = "12.345.678", mail = "carlos@gardel.com.ar") }
        admin.confirmarAlta()
        admin.cerrarSesion()

        ingresar("Ana", "Prof123")
        val entrenadora = ClubViewModel()
        assertTrue(entrenadora.socios.any { it.nombre == "Carlos Gardel" })
    }
}
