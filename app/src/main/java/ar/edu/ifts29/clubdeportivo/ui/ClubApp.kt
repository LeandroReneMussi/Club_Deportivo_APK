package ar.edu.ifts29.clubdeportivo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.ifts29.clubdeportivo.data.Screen
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaAcceso
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaAgenda
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaAlta
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaAsistencia
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaCarnet
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaQrIngreso
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaCuotas
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaEmpleadoHome
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaFichaNutricional
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaFichaSocio
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaFichas
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaLogin
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaNutriSocio
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaPagar
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaPagoOk
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaPerfil
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaProfesorHome
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaReportes
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaRutinaEdit
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaRutinaSocio
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaRutinasProfesor
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaSocioHome
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaSocios
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaTurno
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaVisitanteNuevo
import ar.edu.ifts29.clubdeportivo.ui.screens.PantallaVisitantes
import ar.edu.ifts29.clubdeportivo.ui.theme.Pista
import ar.edu.ifts29.clubdeportivo.vm.ClubViewModel

/**
 * Raíz de la app: chrome fijo (header, barra inferior, overlays) alrededor de la
 * pantalla activa. Una sola app para los cinco roles — lo que se ve depende de quién
 * inició sesión (HU-01).
 */
@Composable
fun ClubApp(vm: ClubViewModel = viewModel()) {
    // El botón atrás del sistema desapila igual que la flecha del header.
    BackHandler(enabled = vm.puedeVolver || vm.estado.sheet != null || vm.estado.logoutSheet) {
        when {
            vm.estado.logoutSheet -> vm.cancelarCerrarSesion()
            vm.estado.sheet != null -> vm.cerrarSheet()
            else -> vm.volver()
        }
    }

    Box(Modifier.fillMaxSize().background(Pista.bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding(),
        ) {
            if (vm.muestraHeader) {
                HeaderSub(vm.titulo.orEmpty(), vm::volver)
            }

            when (vm.pantalla) {
                Screen.LOGIN -> PantallaLogin(vm)

                Screen.S_HOME -> PantallaSocioHome(vm)
                Screen.S_CARNET -> PantallaCarnet(vm)
                Screen.S_QR -> PantallaQrIngreso(vm)
                Screen.S_CUOTAS -> PantallaCuotas(vm)
                Screen.S_PAGAR -> PantallaPagar(vm)
                Screen.S_PAGO_OK -> PantallaPagoOk(vm)
                Screen.S_RUTINA -> PantallaRutinaSocio(vm)
                Screen.S_NUTRI -> PantallaNutriSocio(vm)
                Screen.S_TURNO -> PantallaTurno(vm)

                Screen.E_HOME -> PantallaEmpleadoHome(vm)
                Screen.E_SOCIOS -> PantallaSocios(vm)
                Screen.E_SOCIO -> PantallaFichaSocio(vm)
                Screen.E_ALTA, Screen.E_EDITAR -> PantallaAlta(vm)
                Screen.E_ACCESO -> PantallaAcceso(vm)
                Screen.E_VISITANTES -> PantallaVisitantes(vm)
                Screen.E_VISITANTE_NUEVO -> PantallaVisitanteNuevo(vm)
                Screen.E_REPORTES -> PantallaReportes(vm)

                Screen.P_HOME -> PantallaProfesorHome(vm)
                Screen.P_ASISTENCIA -> PantallaAsistencia(vm)
                Screen.P_RUTINAS -> PantallaRutinasProfesor(vm)
                Screen.P_RUTINA_EDIT -> PantallaRutinaEdit(vm)

                Screen.N_AGENDA -> PantallaAgenda(vm)
                Screen.N_FICHAS -> PantallaFichas(vm)
                Screen.N_FICHA -> PantallaFichaNutricional(vm)

                Screen.PERFIL -> PantallaPerfil(vm)
            }

            if (vm.muestraNav) {
                BarraInferior(vm.tabs, vm.tabActiva, vm::ir)
            }
        }

        vm.estado.toast?.let { Toast(it) }
        vm.estado.sheet?.let { HojaConfirmacion(it, vm::cerrarSheet) }
        if (vm.estado.logoutSheet) {
            HojaCerrarSesion(
                nombre = vm.usuario.nombre,
                onCerrarSesion = vm::cerrarSesion,
                onCancelar = vm::cancelarCerrarSesion,
            )
        }
    }
}
