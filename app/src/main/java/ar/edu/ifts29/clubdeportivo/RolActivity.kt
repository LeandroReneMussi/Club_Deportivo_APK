package ar.edu.ifts29.clubdeportivo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.ifts29.clubdeportivo.data.Rol
import ar.edu.ifts29.clubdeportivo.ui.ClubApp
import ar.edu.ifts29.clubdeportivo.ui.theme.Pista
import ar.edu.ifts29.clubdeportivo.vm.ClubRepositorio
import ar.edu.ifts29.clubdeportivo.vm.ClubViewModel

/** Activity a la que lleva cada rol después del login. */
fun activityDelRol(rol: Rol): Class<out Activity> = when (rol) {
    Rol.SOCIO -> SocioActivity::class.java
    Rol.ADMIN, Rol.EMPLEADO -> AdminActivity::class.java
    Rol.PROFESOR -> EntrenadoraActivity::class.java
    Rol.NUTRICIONISTA -> NutricionistaActivity::class.java
}

internal fun Activity.abrirActivityDelRol(rol: Rol) {
    startActivity(Intent(this, activityDelRol(rol)))
    finish()
}

internal fun Activity.volverAlLogin() {
    startActivity(Intent(this, LoginActivity::class.java))
    finish()
}

/**
 * Base de las Activities de cada rol. Dentro de la Activity, las pantallas del rol se
 * navegan con la pila de Compose y la barra inferior, igual que en el prototipo.
 *
 * Si se abre sin una sesión de su rol, vuelve al login. Al cerrar sesión, también.
 */
abstract class RolActivity(private val roles: Set<Rol>) : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ClubRepositorio.rol !in roles) {
            volverAlLogin()
            return
        }

        enableEdgeToEdge()
        setContent {
            val vm: ClubViewModel = viewModel()
            val rol = vm.estado.rol
            LaunchedEffect(rol) {
                if (rol == null) volverAlLogin()
            }
            if (rol != null) {
                ClubApp(vm)
            } else {
                Box(Modifier.fillMaxSize().background(Pista.bg))
            }
        }
    }
}

/** Inicio, Carnet, Rutina, Nutrición y Perfil del socio. */
class SocioActivity : RolActivity(setOf(Rol.SOCIO))

/** Inicio, Socios, Acceso, Visitantes y Reportes del personal administrativo. */
class AdminActivity : RolActivity(setOf(Rol.ADMIN, Rol.EMPLEADO))

/** Inicio, Asistencia, Rutinas y Perfil de la entrenadora. */
class EntrenadoraActivity : RolActivity(setOf(Rol.PROFESOR))

/** Agenda, Fichas y Perfil de la nutricionista. */
class NutricionistaActivity : RolActivity(setOf(Rol.NUTRICIONISTA))
