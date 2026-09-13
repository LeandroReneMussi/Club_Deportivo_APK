package ar.edu.ifts29.clubdeportivo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.ifts29.clubdeportivo.data.Screen
import ar.edu.ifts29.clubdeportivo.data.SheetData
import ar.edu.ifts29.clubdeportivo.ui.theme.Pista
import ar.edu.ifts29.clubdeportivo.ui.theme.body
import ar.edu.ifts29.clubdeportivo.ui.theme.disp

/** Header de las sub-pantallas: flecha circular para volver y título. */
@Composable
fun HeaderSub(titulo: String, onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Pista.sur)
                .border(1.dp, Pista.line, CircleShape)
                .clickable(onClick = onVolver),
            contentAlignment = Alignment.Center,
        ) {
            Text("←", style = body(600, 18))
        }
        Text(titulo.uppercase(), style = disp(700, 24, lineHeight = 24.sp))
    }
}

/**
 * Barra inferior con el aire de iOS que pidió el diseño: glifo grande arriba, etiqueta
 * chica abajo, acento sólo en la pestaña activa.
 *
 * La maqueta dibujaba además el "home indicator" del iPhone; en Android esa franja la
 * ocupa la barra de gestos del sistema, así que en su lugar se respeta ese inset.
 */
@Composable
fun BarraInferior(
    tabs: List<Pair<Screen, String>>,
    activa: Screen,
    onTab: (Screen) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Pista.bg),
    ) {
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(Pista.line))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(top = 6.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Top,
        ) {
            tabs.forEach { (scr, label) ->
                val activo = scr == activa
                val color = if (activo) Pista.acc else Pista.mut
                Column(
                    modifier = Modifier
                        .width(64.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTab(scr) },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(
                        imageVector = iconoDeTab(label),
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        label,
                        style = body(500, 10, color, letterSpacing = (-0.24).sp),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        // El inset va por fuera del alto para que se sume, no para que lo recorte.
        Spacer(Modifier.navigationBarsPadding().height(6.dp))
    }
}

/**
 * La maqueta usa glifos Unicode (`⌂ ▭ ≣ ◐ ⚇ ⊙ ⊕ ▤ ☑ ▦`) que en el navegador resuelve la
 * fuente del sistema. En Android varios de esos caracteres no existen en la fuente por
 * defecto y se verían como cuadraditos, así que se usan los íconos equivalentes.
 */
private fun iconoDeTab(label: String): ImageVector = when (label) {
    "Inicio" -> Icons.Outlined.Home
    "Carnet" -> Icons.Outlined.Badge
    "Rutina", "Rutinas" -> Icons.Outlined.FitnessCenter
    "Nutrición" -> Icons.Outlined.Restaurant
    "Perfil" -> Icons.Outlined.Person
    "Socios" -> Icons.Outlined.Group
    "Acceso" -> Icons.Outlined.HowToReg
    "Visitantes" -> Icons.Outlined.PersonAdd
    "Reportes" -> Icons.Outlined.Assessment
    "Asistencia" -> Icons.Outlined.Checklist
    "Agenda" -> Icons.Outlined.CalendarMonth
    "Fichas" -> Icons.Outlined.Description
    else -> Icons.Outlined.Home
}

/** Aviso efímero, igual que el toast del prototipo. */
@Composable
fun Toast(mensaje: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(start = Pista.gutter, end = Pista.gutter, bottom = 96.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            mensaje,
            style = body(600, 14, Pista.bg),
            modifier = Modifier
                .fillMaxWidth()
                .clip(Pista.rCard)
                .background(Pista.ink)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}

/** Hoja de confirmación genérica: título, texto, filas de detalle y dos botones. */
@Composable
fun HojaConfirmacion(sheet: SheetData, onCerrar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCerrar,
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Pista.bg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .navigationBarsPadding()
                .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(sheet.titulo.uppercase(), style = disp(700, 26, lineHeight = 26.sp))
            Text(sheet.texto, style = body(500, 14, Pista.mut, lineHeight = 21.sp))
            sheet.filas.forEach { (k, v) ->
                Fila(fondo = Pista.sur, paddingV = 12.dp, paddingH = 14.dp) {
                    Text(k, style = body(500, 14, Pista.mut), modifier = Modifier.weight(1f))
                    Text(v, style = body(700, 14))
                }
            }
            Spacer(Modifier.height(0.dp))
            BotonPrimario(sheet.cta, sheet.onConfirm)
            BotonBorde("Cancelar", onCerrar, alto = 48.dp)
        }
    }
}

/**
 * Cierre de sesión con el action sheet de iOS que pidió el diseño: acción destructiva
 * en rojo y "Cancelar" separado abajo.
 */
@Composable
fun HojaCerrarSesion(nombre: String, onCerrarSesion: () -> Unit, onCancelar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCancelar,
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 8.dp, end = 8.dp, bottom = 34.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Pista.sur)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                Text(
                    "¿Cerrar la sesión de $nombre?\nVas a volver a la pantalla de inicio de sesión.",
                    style = body(400, 13, Pista.mut, lineHeight = 18.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
                )
                Box(Modifier.fillMaxWidth().height(0.5.dp).background(Pista.line))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(57.dp)
                        .clickable(onClick = onCerrarSesion),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Cerrar sesión", style = body(400, 20, Pista.iosRed))
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(57.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Pista.sur)
                    .clickable(onClick = onCancelar),
                contentAlignment = Alignment.Center,
            ) {
                Text("Cancelar", style = body(600, 20))
            }
        }
    }
}

/** Cabecera de saludo compartida por los cuatro inicios de rol. */
@Composable
fun HeaderSaludo(
    contexto: String,
    nombreCorto: String,
    avatar: Int?,
    onSalir: () -> Unit,
    onPerfil: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(Modifier.weight(1f)) {
            Text(contexto, style = body(500, 14, Pista.mut))
            Text(
                "Hola, $nombreCorto".uppercase(),
                style = disp(700, 38, lineHeight = 38.sp),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(Pista.rs)
                    .border(1.dp, Pista.line, Pista.rs)
                    .clickable(onClick = onSalir)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Salir", style = disp(700, 12, Pista.mut, letterSpacing = 0.8.sp))
            }
            Avatar(avatar, 44.dp, onClick = onPerfil)
        }
    }
}
