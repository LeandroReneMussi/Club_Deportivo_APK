package ar.edu.ifts29.clubdeportivo

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
import ar.edu.ifts29.clubdeportivo.ui.ClubApp
import ar.edu.ifts29.clubdeportivo.ui.theme.Pista
import ar.edu.ifts29.clubdeportivo.vm.ClubRepositorio
import ar.edu.ifts29.clubdeportivo.vm.ClubViewModel

/**
 * Entry point de la app (HU-01). Valida las credenciales y abre la Activity del rol.
 * Se cierra al ingresar, así el botón atrás no vuelve al login con la sesión abierta.
 */
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sesión ya abierta en este proceso: directo a su Activity.
        ClubRepositorio.rol?.let {
            abrirActivityDelRol(it)
            return
        }

        enableEdgeToEdge()
        setContent {
            val vm: ClubViewModel = viewModel()
            val rol = vm.estado.rol
            LaunchedEffect(rol) {
                rol?.let { abrirActivityDelRol(it) }
            }
            if (rol == null) {
                ClubApp(vm)
            } else {
                Box(Modifier.fillMaxSize().background(Pista.bg))
            }
        }
    }
}
