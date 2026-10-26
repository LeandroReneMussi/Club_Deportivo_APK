package ar.edu.ifts29.clubdeportivo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.ifts29.clubdeportivo.data.DemoData
import ar.edu.ifts29.clubdeportivo.ui.BotonPrimario
import ar.edu.ifts29.clubdeportivo.ui.CampoTexto
import ar.edu.ifts29.clubdeportivo.ui.theme.Pista
import ar.edu.ifts29.clubdeportivo.ui.theme.body
import ar.edu.ifts29.clubdeportivo.ui.theme.disp
import ar.edu.ifts29.clubdeportivo.ui.theme.label
import ar.edu.ifts29.clubdeportivo.vm.ClubViewModel

/** HU-01: ingreso por usuario y contraseña; el rol se deduce de la cuenta. */
@Composable
fun ColumnScope.PantallaLogin(vm: ClubViewModel) {
    val campos = vm.estado.campos
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = Pista.gutter),
    ) {
        Row(
            modifier = Modifier.padding(top = 60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Pista.acc),
                contentAlignment = Alignment.Center,
            ) {
                Text("CD", style = disp(800, 20, Pista.accInk))
            }
            Text("Club Deportivo".uppercase(), style = disp(800, 22, letterSpacing = 1.sp))
        }

        Text(
            "Bienvenido".uppercase(),
            style = disp(700, 44, lineHeight = 44.sp),
            modifier = Modifier.padding(top = 40.dp),
        )
        Text(
            "Ingresá con tus credenciales. Vas a ver solo las funciones de tu rol.",
            style = body(500, 15, Pista.mut, lineHeight = 21.sp),
            modifier = Modifier.padding(top = 8.dp),
        )

        Column(
            modifier = Modifier.padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CampoTexto(
                valor = campos.email,
                onValorChange = { v -> vm.campo { copy(email = v) } },
                placeholder = "Usuario",
            )
            CampoTexto(
                valor = campos.pass,
                onValorChange = { v -> vm.campo { copy(pass = v) } },
                placeholder = "Contraseña",
                esPassword = true,
                imeAction = ImeAction.Go,
                onImeAction = vm::login,
            )
            if (vm.estado.loginError) {
                Text(
                    "Usuario o contraseña incorrectos.",
                    style = body(600, 13, Pista.badInk),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Pista.rs)
                        .background(Pista.bad)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                )
            }
            BotonPrimario("Iniciar sesión", vm::login)
        }

        CuentasDePrueba(vm)

        Spacer(Modifier.weight(1f))
        Text(
            "¿Olvidaste tu contraseña? Consultá en recepción.",
            style = body(500, 13, Pista.mut),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )
    }
}

/**
 * Ayuda de demostración.
 *
 * El prototipo listaba las cuentas de prueba en el panel lateral del canvas, fuera del
 * teléfono. Como acá no hay panel, viven en el login: sin esto no habría manera de
 * entrar a la app. Un click completa usuario y contraseña.
 */
@Composable
private fun CuentasDePrueba(vm: ClubViewModel) {
    Column(modifier = Modifier.padding(top = 28.dp)) {
        Text("Cuentas de prueba", style = label(11))
        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            DemoData.cuentas.forEach { (usuario, credencial) ->
                val (clave, rol) = credencial
                val u = DemoData.usuarios.getValue(rol)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, Pista.line, RoundedCornerShape(6.dp))
                        .clickable {
                            vm.campo {
                                copy(
                                    email = usuario.replaceFirstChar { it.uppercase() },
                                    pass = clave,
                                )
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${u.nombreCorto} · ${u.rolLabel}",
                        style = body(600, 12),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${usuario.replaceFirstChar { it.uppercase() }} / $clave",
                        style = body(500, 11, Pista.mut),
                    )
                }
            }
        }
    }
}

/** Enlace de texto subrayado, como el "Ver estado de cuenta →" del inicio. */
@Composable
fun EnlaceTexto(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = body(500, 13, Pista.mut).copy(textDecoration = TextDecoration.Underline),
        modifier = modifier.clickable(onClick = onClick),
    )
}
