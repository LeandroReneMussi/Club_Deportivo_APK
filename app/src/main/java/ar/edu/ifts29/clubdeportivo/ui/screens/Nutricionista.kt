package ar.edu.ifts29.clubdeportivo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.ifts29.clubdeportivo.data.Screen
import ar.edu.ifts29.clubdeportivo.data.fmt
import ar.edu.ifts29.clubdeportivo.data.numero
import ar.edu.ifts29.clubdeportivo.ui.BotonPrimario
import ar.edu.ifts29.clubdeportivo.ui.CampoLargo
import ar.edu.ifts29.clubdeportivo.ui.CampoTexto
import ar.edu.ifts29.clubdeportivo.ui.ColumnaGutter
import ar.edu.ifts29.clubdeportivo.ui.CuerpoPantalla
import ar.edu.ifts29.clubdeportivo.ui.Fila
import ar.edu.ifts29.clubdeportivo.ui.HeaderSaludo
import ar.edu.ifts29.clubdeportivo.ui.Seccion
import ar.edu.ifts29.clubdeportivo.ui.theme.Pista
import ar.edu.ifts29.clubdeportivo.ui.theme.body
import ar.edu.ifts29.clubdeportivo.ui.theme.disp
import ar.edu.ifts29.clubdeportivo.vm.ClubViewModel

/** HU-11 del lado del profesional: la agenda de turnos. */
@Composable
fun ColumnScope.PantallaAgenda(vm: ClubViewModel) {
    val u = vm.usuario
    CuerpoPantalla {
        HeaderSaludo(
            contexto = "${vm.hoyLabel} · ${u.rolLabel}",
            nombreCorto = u.nombreCorto,
            avatar = u.avatar,
            onSalir = vm::pedirCerrarSesion,
            onPerfil = { vm.ir(Screen.PERFIL) },
        )

        Seccion("Mi agenda")

        val agenda = vm.agenda
        agenda.forEach { grupo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Pista.gutter, end = Pista.gutter, top = 22.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    grupo.dia.uppercase(),
                    style = disp(700, 13, Pista.mut, letterSpacing = 1.5.sp),
                    modifier = Modifier.weight(1f),
                )
                Text("${grupo.turnos.size} turnos", style = body(500, 13, Pista.mut))
            }
            ColumnaGutter {
                grupo.turnos.forEach { s ->
                    Fila(
                        fondo = if (grupo.esHoy) Pista.sur else Pista.sur2,
                        forma = Pista.rCard,
                        onClick = { vm.abrirFicha(s.id) },
                    ) {
                        Text(s.turno?.hora.orEmpty(), style = disp(800, 20, lineHeight = 20.sp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.nombre, style = body(600, 15))
                            Text(
                                s.nutSub,
                                style = body(500, 12, Pista.ink.copy(alpha = 0.7f)),
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        Text("›", style = disp(700, 18))
                    }
                }
            }
        }

        if (agenda.isEmpty()) {
            ColumnaGutter(top = 22.dp) {
                Text("No tenés turnos agendados.", style = body(500, 14, Pista.mut))
            }
        }
    }
}

@Composable
fun ColumnScope.PantallaFichas(vm: ClubViewModel) {
    CuerpoPantalla {
        Text(
            "Fichas nutricionales".uppercase(),
            style = disp(700, 32, lineHeight = 32.sp),
            modifier = Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
        )
        CampoTexto(
            valor = vm.estado.campos.busq,
            onValorChange = { v -> vm.campo { copy(busq = v) } },
            placeholder = "Buscar por DNI o nombre",
            alto = 48.dp,
            imeAction = ImeAction.Search,
            modifier = Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 16.dp),
        )
        ColumnaGutter(top = 14.dp) {
            vm.sociosFiltrados.forEach { s ->
                Fila(fondo = Pista.sur, forma = Pista.rCard, onClick = { vm.abrirFicha(s.id) }) {
                    AvatarTrama(40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.nombre, style = body(600, 15))
                        Text(
                            s.nutSub,
                            style = body(500, 12, Pista.mut),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text("›", style = disp(700, 18))
                }
            }
            if (vm.sociosFiltrados.isEmpty()) {
                Text("Ningún socio coincide con la búsqueda.", style = body(500, 14, Pista.mut))
            }
        }
    }
}

/** HU-12: consulta y actualización de la ficha nutricional. */
@Composable
fun ColumnScope.PantallaFichaNutricional(vm: ClubViewModel) {
    val s = vm.seleccionado
    val f = vm.estado.campos
    CuerpoPantalla {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AvatarTrama(52.dp)
            Column {
                Text(s.nombre.uppercase(), style = disp(700, 24, lineHeight = 24.sp))
                Text(
                    "DNI ${s.dniLabel} · ${s.edadLabel}",
                    style = body(500, 13, Pista.mut),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricaFicha("Peso", s.pesoLabel, " kg", Modifier.weight(1f))
            MetricaFicha("Altura", s.alturaLabel, " m", Modifier.weight(1f))
            MetricaFicha("IMC", s.imc, "", Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 8.dp)
                .clip(Pista.rCard)
                .background(Pista.sur)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Pista.mut)) { append("Objetivo · ") }
                    append(s.nut.objetivo)
                },
                style = body(500, 14, lineHeight = 21.sp),
            )
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Pista.mut)) { append("Antecedentes · ") }
                    append(s.nut.antecedentes)
                },
                style = body(500, 14, lineHeight = 21.sp),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Seccion("Registrar consulta de hoy")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 10.dp)
                .clip(Pista.rCard)
                .background(Pista.sur2)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoTexto(
                    f.nPeso,
                    { v -> vm.campo { copy(nPeso = v) } },
                    "Peso (kg)",
                    modifier = Modifier.weight(1f),
                    alto = 46.dp,
                    tamano = 14,
                    tecladoDecimal = true,
                )
                CampoTexto(
                    f.nAltura,
                    { v -> vm.campo { copy(nAltura = v) } },
                    "Altura (m)",
                    modifier = Modifier.weight(1f),
                    alto = 46.dp,
                    tamano = 14,
                    tecladoDecimal = true,
                )
            }
            CampoLargo(
                f.nObs,
                { v -> vm.campo { copy(nObs = v) } },
                "Observaciones de la consulta",
                lineas = 3,
            )
            BotonPrimario("Guardar en historial", vm::guardarConsulta, alto = 48.dp, tamano = 15)
        }

        Seccion("Historial")
        ColumnaGutter {
            s.consultas.forEach { c ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(Pista.rCard)
                        .background(Pista.sur)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        Text(c.fecha.fmt(), style = body(600, 14), modifier = Modifier.weight(1f))
                        Text("${numero(c.peso)} kg", style = disp(800, 16))
                    }
                    Text(
                        c.obs,
                        style = body(500, 13, Pista.mut, lineHeight = 19.sp),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            if (s.consultas.isEmpty()) {
                Text("Sin consultas registradas.", style = body(500, 14, Pista.mut))
            }
        }
    }
}
