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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.ifts29.clubdeportivo.data.Screen
import ar.edu.ifts29.clubdeportivo.ui.BotonPrimario
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

@Composable
fun ColumnScope.PantallaProfesorHome(vm: ClubViewModel) {
    val u = vm.usuario
    val presente = vm.estado.profePresente
    CuerpoPantalla {
        HeaderSaludo(
            contexto = "${vm.hoyLabel} · ${u.rolLabel}",
            nombreCorto = u.nombreCorto,
            avatar = u.avatar,
            onSalir = vm::pedirCerrarSesion,
            onPerfil = { vm.ir(Screen.PERFIL) },
        )

        // Asistencia propia de la profesora: lo primero que hace al llegar.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 18.dp)
                .clip(Pista.r)
                .background(if (presente) Pista.acc else Pista.sur)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val tinta = if (presente) Pista.accInk else Pista.ink
            Column(Modifier.weight(1f)) {
                Text(
                    "Mi asistencia de hoy".uppercase(),
                    style = body(600, 11, tinta.copy(alpha = 0.75f), letterSpacing = 1.2.sp),
                )
                Text(
                    (if (presente) "Presente · 09:41" else "Sin marcar").uppercase(),
                    style = disp(800, 24, tinta, lineHeight = 24.sp),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            if (!presente) {
                Box(
                    modifier = Modifier
                        .clip(Pista.rs)
                        .background(Pista.accInk)
                        .clickable(onClick = vm::marcarPresencia)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        "Marcar presencia".uppercase(),
                        style = disp(700, 14, Pista.acc, letterSpacing = 0.8.sp),
                    )
                }
            }
        }

        Seccion("Mis clases de hoy")
        ColumnaGutter {
            vm.clases.forEach { c ->
                Fila(
                    fondo = Pista.sur,
                    forma = Pista.rCard,
                    paddingV = 16.dp,
                    onClick = { vm.abrirAsistencia(c.act.id) },
                ) {
                    Text(c.act.horario, style = disp(800, 20, lineHeight = 20.sp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(c.act.nombre, style = body(600, 15))
                        Text(
                            "${c.inscriptos} inscriptos · ${c.presentes} presentes",
                            style = body(500, 12, Pista.mut),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        "Asistencia →".uppercase(),
                        style = disp(700, 12, Pista.acc, letterSpacing = 0.8.sp),
                    )
                }
            }
        }

        Seccion("Rutinas pendientes")
        ColumnaGutter {
            val pendientes = vm.rutinasPendientes
            pendientes.forEach { s ->
                Fila(forma = Pista.rCard, onClick = { vm.abrirRutina(s.id) }) {
                    Column(Modifier.weight(1f)) {
                        Text(s.nombre, style = body(600, 15))
                        Text(
                            s.rutinaSub,
                            style = body(500, 12, Pista.mut),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text("›", style = disp(700, 18))
                }
            }
            if (pendientes.isEmpty()) {
                Text("Todos tus socios tienen rutina asignada.", style = body(500, 14, Pista.mut))
            }
        }
    }
}

/** HU-08: registro de asistencia de una clase. */
@Composable
fun ColumnScope.PantallaAsistencia(vm: ClubViewModel) {
    val filas = vm.filasAsistencia
    CuerpoPantalla {
        Column(Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp)) {
            Text("Asistencia".uppercase(), style = disp(700, 32, lineHeight = 32.sp))
            Text(
                "${vm.hoyLabel} · marcá los socios presentes",
                style = body(500, 13, Pista.mut),
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            vm.clases.forEach { c ->
                val activa = c.act.id == vm.estado.claseId
                val tinta = if (activa) Pista.accInk else Pista.ink
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(Pista.rRow)
                        .background(if (activa) Pista.acc else Pista.sur)
                        .clickable { vm.elegirClase(c.act.id) }
                        .padding(12.dp),
                ) {
                    Text(c.act.horario, style = disp(800, 18, tinta, lineHeight = 18.sp))
                    Text(
                        c.act.nombre,
                        style = body(600, 12, tinta.copy(alpha = 0.85f)),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 18.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                "Inscriptos".uppercase(),
                style = disp(700, 13, Pista.mut, letterSpacing = 1.5.sp),
                modifier = Modifier.weight(1f),
            )
            Text("${filas.count { it.second }} / ${filas.size} presentes", style = disp(700, 14))
        }

        ColumnaGutter {
            filas.forEach { (s, presente) ->
                val tinta = if (presente) Pista.accInk else Pista.ink
                Fila(
                    fondo = if (presente) Pista.acc else Pista.sur,
                    forma = Pista.rCard,
                    onClick = { vm.alternarAsistencia(s.id) },
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(s.nombre, style = body(600, 15, tinta))
                        Text(
                            if (s.hab) "Habilitado" else s.badge,
                            style = body(500, 12, tinta.copy(alpha = 0.7f)),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(2.dp, tinta, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (presente) Text("✓", style = disp(800, 15, tinta))
                    }
                }
            }
        }

        BotonPrimario(
            "Guardar asistencia",
            vm::guardarAsistencia,
            Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
        )
    }
}

/** HU-09: lista de socios de la profesora, con el estado de su rutina. */
@Composable
fun ColumnScope.PantallaRutinasProfesor(vm: ClubViewModel) {
    CuerpoPantalla {
        Column(Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp)) {
            Text("Rutinas".uppercase(), style = disp(700, 32, lineHeight = 32.sp))
            Text(
                "Socios asignados · ${vm.sociosDeLaProfe.size}",
                style = body(500, 13, Pista.mut),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        ColumnaGutter(top = 16.dp) {
            vm.sociosDeLaProfe.forEach { s ->
                Fila(fondo = Pista.sur, forma = Pista.rCard, onClick = { vm.abrirRutina(s.id) }) {
                    AvatarTrama(40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.nombre, style = body(600, 15))
                        Text(
                            s.rutinaSub,
                            style = body(500, 12, Pista.mut),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        s.rutinaTag.uppercase(),
                        style = disp(700, 12, if (s.tieneRutina) Pista.mut else Pista.acc, letterSpacing = 0.8.sp),
                    )
                }
            }
        }
    }
}

/**
 * Editor de rutina. La validación de HU-09 es la parte importante: si el socio está en
 * mora, con el carnet vencido o sin apto físico, el editor no se muestra.
 */
@Composable
fun ColumnScope.PantallaRutinaEdit(vm: ClubViewModel) {
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
                    "N° ${s.nro} · Plan ${s.plan}",
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
            EstadoChip(
                etiqueta = s.badgeCorto,
                check = if (s.hab) "✓" else "✕",
                fondo = if (s.hab) Pista.ok else Pista.bad,
                tinta = if (s.hab) Pista.okInk else Pista.badInk,
                modifier = Modifier.weight(1f),
            )
            EstadoChip(
                etiqueta = "Ficha médica",
                check = if (s.ficha) "✓" else "✕",
                fondo = if (s.ficha) Pista.ok else Pista.bad,
                tinta = if (s.ficha) Pista.okInk else Pista.badInk,
                modifier = Modifier.weight(1f),
            )
        }

        if (s.bloqueaRutina) {
            Text(
                "No se puede asignar rutina: ${s.bloqueoMotivo}. El socio debe regularizar la situación en recepción.",
                style = body(600, 13, Pista.badInk, lineHeight = 19.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Pista.gutter, end = Pista.gutter, top = 12.dp)
                    .clip(Pista.rCard)
                    .background(Pista.bad)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            )
            return@CuerpoPantalla
        }

        val dias = vm.estado.editEj.orEmpty()
        val diaActual = vm.diaEnEdicion

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                "Ejercicios · ${diaActual?.foco ?: ""}".uppercase(),
                style = disp(700, 13, Pista.mut, letterSpacing = 1.5.sp),
                modifier = Modifier.weight(1f),
            )
            Text(
                "${diaActual?.ejercicios?.size ?: 0} ejercicios",
                style = body(500, 13, Pista.mut),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            dias.forEachIndexed { i, d ->
                val activo = i == vm.estado.editDia
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activo) Pista.acc else Pista.sur)
                        .clickable { vm.elegirDiaEdicion(i) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(d.tag, style = disp(700, 13, if (activo) Pista.accInk else Pista.ink))
                }
            }
        }

        ColumnaGutter {
            diaActual?.ejercicios?.forEachIndexed { i, e ->
                Fila(fondo = Pista.sur, forma = Pista.rCard, paddingV = 12.dp, paddingH = 14.dp) {
                    Column(Modifier.weight(1f)) {
                        Text(e.nombre, style = body(600, 15))
                        if (e.nota.isNotEmpty()) {
                            Text(
                                e.nota,
                                style = body(500, 12, Pista.mut),
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("${e.series}×${e.reps}", style = disp(800, 16))
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.dp, Pista.line, CircleShape)
                            .clickable { vm.quitarEjercicio(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("×", style = body(600, 16, Pista.mut))
                    }
                }
            }
            if (diaActual?.ejercicios.isNullOrEmpty()) {
                Text("Este día todavía no tiene ejercicios.", style = body(500, 14, Pista.mut))
            }
        }

        // Alta de ejercicio: nombre a lo ancho, series y repeticiones en columnas fijas.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 12.dp)
                .clip(Pista.rCard)
                .background(Pista.sur2)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoTexto(
                    f.ejNombre,
                    { v -> vm.campo { copy(ejNombre = v) } },
                    "Nuevo ejercicio",
                    modifier = Modifier.weight(1f),
                    alto = 44.dp,
                    tamano = 14,
                )
                CampoTexto(
                    f.ejSeries,
                    { v -> vm.campo { copy(ejSeries = v) } },
                    "Ser.",
                    modifier = Modifier.width(64.dp),
                    alto = 44.dp,
                    tamano = 14,
                    tecladoNumerico = true,
                    alinearCentro = true,
                )
                CampoTexto(
                    f.ejReps,
                    { v -> vm.campo { copy(ejReps = v) } },
                    "Rep.",
                    modifier = Modifier.width(64.dp),
                    alto = 44.dp,
                    tamano = 14,
                    tecladoNumerico = true,
                    alinearCentro = true,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Pista.mut, RoundedCornerShape(8.dp))
                    .clickable(onClick = vm::agregarEjercicio),
                contentAlignment = Alignment.Center,
            ) {
                Text("+ Agregar ejercicio".uppercase(), style = disp(700, 14, letterSpacing = 0.8.sp))
            }
        }

        BotonPrimario(
            "Guardar y asignar rutina",
            vm::guardarRutina,
            Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
        )
    }
}

@Composable
private fun EstadoChip(
    etiqueta: String,
    check: String,
    fondo: Color,
    tinta: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(Pista.rRow)
            .background(fondo)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            etiqueta.uppercase(),
            style = disp(700, 13, tinta, letterSpacing = 0.8.sp),
            modifier = Modifier.weight(1f),
        )
        Text(check, style = disp(700, 13, tinta))
    }
}
