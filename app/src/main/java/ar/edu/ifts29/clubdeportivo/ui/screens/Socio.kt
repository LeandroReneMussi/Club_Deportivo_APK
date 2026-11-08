package ar.edu.ifts29.clubdeportivo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.ifts29.clubdeportivo.data.DemoData
import ar.edu.ifts29.clubdeportivo.data.DiaRutina
import ar.edu.ifts29.clubdeportivo.data.MedioPago
import ar.edu.ifts29.clubdeportivo.data.Rol
import ar.edu.ifts29.clubdeportivo.data.Screen
import ar.edu.ifts29.clubdeportivo.data.fmt
import ar.edu.ifts29.clubdeportivo.data.money
import ar.edu.ifts29.clubdeportivo.data.numero
import ar.edu.ifts29.clubdeportivo.ui.Avatar
import ar.edu.ifts29.clubdeportivo.ui.BotonBorde
import ar.edu.ifts29.clubdeportivo.ui.BotonPrimario
import ar.edu.ifts29.clubdeportivo.ui.CampoTexto
import ar.edu.ifts29.clubdeportivo.ui.Chip
import ar.edu.ifts29.clubdeportivo.ui.ColumnaGutter
import ar.edu.ifts29.clubdeportivo.ui.CuerpoPantalla
import ar.edu.ifts29.clubdeportivo.ui.Fila
import ar.edu.ifts29.clubdeportivo.ui.FilaDato
import ar.edu.ifts29.clubdeportivo.ui.FotoCarnet
import ar.edu.ifts29.clubdeportivo.ui.HeaderSaludo
import ar.edu.ifts29.clubdeportivo.ui.Seccion
import ar.edu.ifts29.clubdeportivo.ui.Tarjeta
import ar.edu.ifts29.clubdeportivo.ui.theme.Pista
import ar.edu.ifts29.clubdeportivo.ui.theme.body
import ar.edu.ifts29.clubdeportivo.ui.theme.disp
import ar.edu.ifts29.clubdeportivo.ui.theme.label
import ar.edu.ifts29.clubdeportivo.vm.ClubViewModel
import ar.edu.ifts29.clubdeportivo.vm.SocioVista
import ar.edu.ifts29.clubdeportivo.vm.TonoCuota
import kotlin.math.roundToInt

/** Resumen de un día de rutina: `5 ejercicios · 40 min`. */
fun resumenDia(d: DiaRutina): String = "${d.ejercicios.size} ejercicios · ${d.ejercicios.size * 8} min"

// ------------------------------------------------------------------ inicio del socio

@Composable
fun ColumnScope.PantallaSocioHome(vm: ClubViewModel) {
    val yo = vm.yo
    CuerpoPantalla {
        HeaderSaludo(
            contexto = vm.hoyLabel,
            nombreCorto = yo.nombreCorto,
            avatar = vm.usuario.avatar,
            onSalir = vm::pedirCerrarSesion,
            onPerfil = { vm.ir(Screen.PERFIL) },
        )

        // Estado de habilitación: el dato que el socio mira primero.
        Row(
            modifier = Modifier
                .padding(start = Pista.gutter, top = 16.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(fondoEstado(yo))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val tinta = tintaEstado(yo)
            Box(Modifier.size(8.dp).clip(CircleShape).background(tinta))
            Text(yo.badge.uppercase(), style = disp(700, 13, tinta, letterSpacing = 0.8.sp))
        }

        // Cuota del mes (HU-04), con el contador de días y la deuda (v1.5)
        Tarjeta(Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text("Cuota ${yo.cuota.mes}".uppercase(), style = label(), modifier = Modifier.weight(1f))
                Text(
                    yo.contador.uppercase(),
                    style = disp(700, 12, if (yo.cuotaPagada) Pista.mut else colorTono(yo.tono), letterSpacing = 1.sp),
                )
            }
            yo.deudaDetalle?.let {
                Text(it, style = body(500, 12, Pista.mut), modifier = Modifier.padding(top = 6.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(yo.totalLabel, style = disp(800, 48, lineHeight = 48.sp), modifier = Modifier.weight(1f))
                if (yo.cuotaPendiente) {
                    Box(
                        modifier = Modifier
                            .clip(Pista.rs)
                            .background(Pista.cta)
                            .clickable { vm.ir(Screen.S_PAGAR) }
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                    ) {
                        Text("Pagar".uppercase(), style = disp(700, 15, Pista.ctaInk, letterSpacing = 0.8.sp))
                    }
                }
                if (yo.cuotaPagada) {
                    Chip("Pagada", Pista.accSoft, Pista.ink, tamano = 13)
                }
            }
            EnlaceTexto(
                "Ver estado de cuenta →",
                { vm.ir(Screen.S_CUOTAS) },
                Modifier.padding(top = 14.dp),
            )
        }

        Seccion("Hoy")
        val hoy = vm.diaDeHoy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TarjetaAccion(
                modifier = Modifier.weight(1f),
                encabezado = "Rutina · ${hoy?.tag ?: "—"}",
                colorEncabezado = Pista.acc,
                titulo = hoy?.foco ?: "Sin rutina",
                sub = hoy?.let { resumenDia(it) } ?: "Pedile a tu profe",
                onClick = { vm.ir(Screen.S_RUTINA) },
            )
            TarjetaAccion(
                modifier = Modifier.weight(1f),
                encabezado = "Nutrición",
                colorEncabezado = Pista.mut,
                titulo = yo.turnoLabel,
                sub = yo.turnoSub,
                onClick = { vm.ir(Screen.S_NUTRI) },
            )
        }

        Seccion("Mis actividades")
        ColumnaGutter {
            yo.actividades.forEach { a ->
                Fila {
                    Text(a.nombre, style = body(500, 14), modifier = Modifier.weight(1f))
                    Text(a.horario, style = body(500, 14, Pista.mut))
                }
            }
        }
    }
}

@Composable
private fun TarjetaAccion(
    modifier: Modifier,
    encabezado: String,
    colorEncabezado: Color,
    titulo: String,
    sub: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(Pista.r)
            .background(Pista.sur)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(encabezado.uppercase(), style = body(600, 11, colorEncabezado, letterSpacing = 1.2.sp))
        Text(
            titulo.uppercase(),
            style = disp(700, 24, lineHeight = 24.sp),
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(sub, style = body(500, 13, Pista.mut), modifier = Modifier.padding(top = 6.dp))
    }
}

// ------------------------------------------------------------------ carnet digital

/** HU-06. */
@Composable
fun ColumnScope.PantallaCarnet(vm: ClubViewModel) {
    val yo = vm.yo
    CuerpoPantalla {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Carnet digital".uppercase(),
                style = disp(700, 32, lineHeight = 32.sp),
                modifier = Modifier.weight(1f),
            )
            Text("N° ${yo.nro}", style = body(600, 12, Pista.mut))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 22.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Pista.acc)
                .clipToBounds(),
        ) {
            // Marca decorativa del club, deliberadamente recortada por el borde.
            // Va dentro de un Box con matchParentSize para que sus 200sp no estiren la
            // tarjeta: sólo se pinta, no participa del cálculo de tamaño.
            Box(Modifier.matchParentSize().clipToBounds()) {
                Text(
                    "CD",
                    style = disp(
                        weight = 800,
                        size = 200,
                        color = Pista.accInk.copy(alpha = 0.12f),
                        lineHeight = 200.sp,
                        letterSpacing = (-8).sp,
                    ),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 30.dp, y = (-40).dp),
                )
            }
            Column(Modifier.padding(22.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "Club Deportivo".uppercase(),
                        style = disp(800, 18, Pista.accInk, letterSpacing = 1.5.sp),
                        modifier = Modifier.weight(1f),
                    )
                    Chip(
                        texto = yo.carnetEstado,
                        fondo = if (yo.carnetVencido) Pista.bad else Pista.accInk,
                        tinta = if (yo.carnetVencido) Pista.badInk else Pista.acc,
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 26.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    FotoCarnet(yo.foto, 84.dp, 104.dp)
                    Column {
                        Text(
                            "Socio".uppercase(),
                            style = body(600, 12, Pista.accInk.copy(alpha = 0.7f), letterSpacing = 1.2.sp),
                        )
                        Text(
                            yo.nombre.uppercase(),
                            style = disp(800, 32, Pista.accInk, lineHeight = 32.sp),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            "Plan ${yo.plan} · desde ${yo.altaLabel}",
                            style = body(600, 13, Pista.accInk.copy(alpha = 0.75f)),
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }

                Column(Modifier.padding(top = 24.dp)) {
                    Text(
                        "DNI",
                        style = body(600, 12, Pista.accInk.copy(alpha = 0.7f), letterSpacing = 1.2.sp),
                    )
                    Text(
                        yo.dniLabel,
                        style = disp(800, 54, Pista.accInk, lineHeight = 54.sp, letterSpacing = 1.sp),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .lineaPunteada(Pista.accInk.copy(alpha = 0.25f))
                        .padding(top = 14.dp),
                ) {
                    DatoCarnet("Emitido", yo.carnetEmitidoLabel, Modifier.weight(1f))
                    DatoCarnet("Vence", yo.carnetVenceLabel, Modifier.weight(1f))
                    DatoCarnet("Cuota", yo.cuotaCorto, Modifier.weight(1f))
                }
            }
        }

        Text(
            buildAnnotatedString {
                append("Mostrá tu ")
                withStyle(SpanStyle(color = Pista.ink, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) {
                    append("DNI")
                }
                append(" en recepción. El personal valida tu carnet y el estado de la cuota en el momento.")
            },
            style = body(500, 14, Pista.mut, lineHeight = 20.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 18.dp)
                .clip(Pista.rCard)
                .background(Pista.sur)
                .padding(16.dp),
        )

        Seccion("Últimos ingresos")
        ColumnaGutter {
            yo.ingresos.forEach { i ->
                Fila(paddingV = 12.dp) {
                    Text(i.fecha, style = body(500, 14), modifier = Modifier.weight(1f))
                    Text(
                        i.estado,
                        style = body(600, 14, if (i.estado == "Permitido") Pista.acc else Pista.bad),
                    )
                }
            }
            if (yo.ingresos.isEmpty()) {
                Text("Todavía no registraste ingresos.", style = body(500, 14, Pista.mut))
            }
        }
    }
}

@Composable
private fun DatoCarnet(clave: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            clave.uppercase(),
            style = body(600, 11, Pista.accInk.copy(alpha = 0.7f), letterSpacing = 1.2.sp),
        )
        Text(valor, style = disp(700, 18, Pista.accInk))
    }
}

/** `border-top:1px dashed`, que Compose no trae de fábrica. */
fun Modifier.lineaPunteada(color: Color): Modifier = drawBehind {
    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())),
    )
}

// ------------------------------------------------------------------ cuotas y pago

/** HU-04: estado de cuenta e historial. */
@Composable
fun ColumnScope.PantallaCuotas(vm: ClubViewModel) {
    val yo = vm.yo
    CuerpoPantalla {
        Tarjeta(Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp)) {
            Text("Cuota actual · ${yo.cuota.mes}".uppercase(), style = label())
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(yo.totalLabel, style = disp(800, 44, lineHeight = 44.sp), modifier = Modifier.weight(1f))
                Chip(
                    texto = yo.cuotaEstadoLabel,
                    fondo = fondoEstado(yo),
                    tinta = tintaEstado(yo),
                    tamano = 12,
                )
            }
            Text(
                yo.cuotaVenceLabel,
                style = body(600, 13, if (yo.cuotaPagada) Pista.mut else colorTono(yo.tono)),
                modifier = Modifier.padding(top = 8.dp),
            )
            yo.deudaDetalle?.let {
                Text(it, style = body(500, 12, Pista.mut), modifier = Modifier.padding(top = 4.dp))
            }
            if (yo.cuotaPendiente) {
                BotonPrimario(
                    "Pagar cuota",
                    { vm.ir(Screen.S_PAGAR) },
                    Modifier.padding(top = 16.dp),
                    alto = 50.dp,
                    tamano = 16,
                )
            }
        }

        Seccion("Historial")
        ColumnaGutter {
            yo.historial.forEach { h ->
                Fila {
                    Column(Modifier.weight(1f)) {
                        Text(h.mes, style = body(600, 15))
                        Text(
                            "Pagada el ${h.pagada.fmt()} · ${h.medio}" +
                                if (h.comprobante.isBlank()) "" else " · ${h.comprobante}",
                            style = body(500, 12, Pista.mut),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(money(h.monto), style = disp(700, 16))
                }
            }
            if (yo.historial.isEmpty()) {
                Text("Sin pagos registrados.", style = body(500, 14, Pista.mut))
            }
        }
    }
}

@Composable
fun ColumnScope.PantallaPagar(vm: ClubViewModel) {
    val yo = vm.yo
    CuerpoPantalla(
        pie = { BotonPrimario("Confirmar pago de ${yo.totalLabel}", vm::confirmarPago) },
    ) {
        Tarjeta(Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Total a pagar".uppercase(), style = label())
                    Text(
                        yo.deudaDetalle ?: "Cuota ${yo.cuota.mes}",
                        style = body(500, 14),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Text(yo.totalLabel, style = disp(800, 40, lineHeight = 40.sp))
            }
        }

        Seccion("Medio de pago")
        ColumnaGutter {
            MedioPago.entries.forEach { m ->
                val elegido = vm.estado.medio == m
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Pista.rCard)
                        .background(Pista.sur2)
                        .border(2.dp, if (elegido) Pista.acc else Pista.line, Pista.rCard)
                        .clickable { vm.elegirMedio(m) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(m.nombre, style = body(600, 15))
                        Text(m.sub, style = body(500, 12, Pista.mut), modifier = Modifier.padding(top = 2.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (elegido) Pista.acc else Pista.line, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(if (elegido) Pista.acc else Color.Transparent),
                    )
                }
            }
        }

        Text(
            "Prototipo académico: el pago se simula, no se conecta a una pasarela real.",
            style = body(500, 12, Pista.mut, lineHeight = 18.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 16.dp)
                .clip(Pista.rRow)
                .background(Pista.sur)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        )
    }
}

@Composable
fun ColumnScope.PantallaPagoOk(vm: ClubViewModel) {
    val yo = vm.yo
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(Pista.acc),
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", style = disp(800, 48, Pista.accInk))
        }
        Text(
            "Pago registrado".uppercase(),
            style = disp(700, 40, lineHeight = 40.sp),
            modifier = Modifier.padding(top = 28.dp),
        )
        Text(
            "Cuota ${yo.cuota.mes} · ${money(yo.historial.firstOrNull()?.monto ?: yo.cuota.monto)}\nTu acceso al club está habilitado.",
            style = body(500, 15, Pista.mut, lineHeight = 22.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
                .clip(Pista.rCard)
                .background(Pista.sur)
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Text("Comprobante", style = body(500, 13, Pista.mut), modifier = Modifier.weight(1f))
            Text("#${vm.estado.comprobante}", style = body(600, 13))
        }
        BotonPrimario(
            "Volver al inicio",
            { vm.ir(Screen.S_HOME) },
            Modifier.padding(top = 32.dp),
            fondo = Pista.cta,
            tinta = Pista.ctaInk,
        )
    }
}

// ------------------------------------------------------------------ rutina

/** HU-10: la rutina que cargó el profesor, en modo lectura. */
@Composable
fun ColumnScope.PantallaRutinaSocio(vm: ClubViewModel) {
    val yo = vm.yo
    CuerpoPantalla {
        Column(Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp)) {
            Text("Mi rutina".uppercase(), style = disp(700, 32, lineHeight = 32.sp))
            yo.rutina?.let { r ->
                Text(
                    "${r.nombre} · Prof. ${r.profesor} · actualizada ${yo.rutinaFechaLabel}",
                    style = body(500, 13, Pista.mut),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        val dias = vm.diasDeRutina
        if (dias.isEmpty()) {
            Text(
                "Todavía no tenés una rutina asignada. Tu profesor la va a cargar en tu próxima clase.",
                style = body(500, 15, Pista.mut, lineHeight = 23.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Pista.gutter, end = Pista.gutter, top = 40.dp)
                    .clip(Pista.r)
                    .background(Pista.sur)
                    .padding(24.dp),
            )
            return@CuerpoPantalla
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            dias.forEachIndexed { i, d ->
                val activo = i == vm.estado.diaIdx
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(Pista.rRow)
                        .background(if (activo) Pista.acc else Pista.sur)
                        .clickable { vm.elegirDiaRutina(i) }
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                ) {
                    val tinta = if (activo) Pista.accInk else Pista.ink
                    Text(d.tag, style = disp(800, 20, tinta, lineHeight = 20.sp))
                    Text(
                        d.foco,
                        style = body(600, 11, tinta.copy(alpha = 0.85f)),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        val sel = vm.diaDeRutina
        if (sel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Pista.gutter, end = Pista.gutter, top = 22.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    sel.foco.uppercase(),
                    style = disp(700, 13, Pista.mut, letterSpacing = 1.5.sp),
                    modifier = Modifier.weight(1f),
                )
                Text(resumenDia(sel), style = body(500, 13, Pista.mut))
            }
            ColumnaGutter {
                sel.ejercicios.forEachIndexed { i, e ->
                    Fila(fondo = Pista.sur, forma = Pista.rCard) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Pista.sur2),
                            contentAlignment = Alignment.Center,
                        ) {
                            // La maqueta dejó este número sin enlazar; acá se numera el ejercicio.
                            Text("${i + 1}", style = disp(800, 14, Pista.mut))
                        }
                        Spacer(Modifier.width(14.dp))
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
                        Text("${e.series}×${e.reps}", style = disp(800, 18))
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ nutrición

/** HU-11 y HU-13: turno con el nutricionista y ficha propia. */
@Composable
fun ColumnScope.PantallaNutriSocio(vm: ClubViewModel) {
    val yo = vm.yo
    CuerpoPantalla {
        Text(
            "Nutrición".uppercase(),
            style = disp(700, 32, lineHeight = 32.sp),
            modifier = Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
        )

        Tarjeta(
            modifier = Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 18.dp),
            fondo = Pista.acc,
        ) {
            Text(
                "Próximo turno".uppercase(),
                style = body(600, 12, Pista.accInk.copy(alpha = 0.75f), letterSpacing = 1.2.sp),
            )
            if (yo.tieneTurno) {
                Text(
                    yo.turnoLabel.uppercase(),
                    style = disp(800, 34, Pista.accInk, lineHeight = 34.sp),
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    "${yo.turnoSub} · Consultorio 2",
                    style = body(600, 14, Pista.accInk.copy(alpha = 0.85f)),
                    modifier = Modifier.padding(top = 6.dp),
                )
                Box(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .clip(Pista.rs)
                        .border(1.dp, Color.Black.copy(alpha = 0.3f), Pista.rs)
                        .clickable(onClick = vm::pedirCancelarTurno)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Text("Cancelar turno", style = body(600, 13, Pista.accInk))
                }
            } else {
                Text(
                    "Sin turno asignado".uppercase(),
                    style = disp(700, 22, Pista.accInk, lineHeight = 26.sp),
                    modifier = Modifier.padding(top = 8.dp),
                )
                Box(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .clip(Pista.rs)
                        .background(Pista.accInk)
                        .clickable { vm.ir(Screen.S_TURNO) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        "Solicitar turno".uppercase(),
                        style = disp(700, 14, Pista.acc, letterSpacing = 0.8.sp),
                    )
                }
            }
        }

        Seccion("Mi ficha")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricaFicha("Peso", yo.pesoLabel, " kg", Modifier.weight(1f))
            MetricaFicha("Altura", yo.alturaLabel, " m", Modifier.weight(1f))
            MetricaFicha("IMC", yo.imc, "", Modifier.weight(1f))
        }

        Row(
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
                    append(yo.nut.objetivo)
                },
                style = body(500, 14, lineHeight = 20.sp),
            )
        }

        Seccion("Historial de consultas")
        ColumnaGutter {
            yo.consultas.forEach { c ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(Pista.rCard)
                        .background(Pista.sur2)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            c.fecha.fmt(),
                            style = body(600, 14),
                            modifier = Modifier.weight(1f),
                        )
                        Text("${numero(c.peso)} kg", style = disp(800, 16))
                    }
                    Text(
                        c.obs,
                        style = body(500, 13, Pista.mut, lineHeight = 19.sp),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            if (yo.consultas.isEmpty()) {
                Text("Sin consultas registradas.", style = body(500, 14, Pista.mut))
            }
        }
    }
}

@Composable
fun MetricaFicha(titulo: String, valor: String, unidad: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(Pista.rCard)
            .background(Pista.sur)
            .padding(14.dp),
    ) {
        Text(titulo.uppercase(), style = body(600, 11, Pista.mut, letterSpacing = 1.sp))
        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.Bottom) {
            Text(valor, style = disp(800, 24, lineHeight = 24.sp))
            if (unidad.isNotEmpty()) Text(unidad, style = body(500, 13, Pista.mut))
        }
    }
}

// ------------------------------------------------------------------ solicitar turno

@Composable
fun ColumnScope.PantallaTurno(vm: ClubViewModel) {
    CuerpoPantalla(
        pie = {
            BotonPrimario(
                "Confirmar turno",
                vm::confirmarTurno,
                fondo = if (vm.estado.slot != null) Pista.acc else Pista.acc.copy(alpha = 0.45f),
                habilitado = vm.estado.slot != null,
            )
        },
    ) {
        Text(
            "${DemoData.NUTRICIONISTA} · Consultorio 2. Elegí un horario disponible.",
            style = body(500, 14, Pista.mut, lineHeight = 21.sp),
            modifier = Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
        )
        vm.slots.forEach { grupo ->
            Seccion(grupo.dia, top = 20.dp)
            grupo.horas.chunked(3).forEachIndexed { fila, tres ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Pista.gutter,
                            end = Pista.gutter,
                            top = if (fila == 0) 10.dp else 8.dp,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    tres.forEach { h ->
                        val fondo = when {
                            h.elegido -> Pista.acc
                            h.ocupado -> Pista.sur2
                            else -> Pista.sur
                        }
                        val tinta = when {
                            h.elegido -> Pista.accInk
                            h.ocupado -> Pista.line
                            else -> Pista.ink
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(Pista.rRow)
                                .background(fondo)
                                .border(
                                    2.dp,
                                    if (h.elegido) Pista.acc else Color.Transparent,
                                    Pista.rRow,
                                )
                                .clickable(enabled = !h.ocupado) { vm.elegirSlot(h.key) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(h.hora, style = disp(700, 15, tinta))
                        }
                    }
                    repeat(3 - tres.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ perfil (compartido)

@Composable
fun ColumnScope.PantallaPerfil(vm: ClubViewModel) {
    val u = vm.usuario
    CuerpoPantalla {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Avatar(u.avatar, 64.dp)
            Column {
                Text(u.nombre.uppercase(), style = disp(700, 28, lineHeight = 28.sp))
                Text(
                    u.rolLabel.uppercase(),
                    style = body(600, 13, Pista.acc, letterSpacing = 1.sp),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        ColumnaGutter(top = 22.dp) {
            u.datos.forEach { (k, v) -> FilaDato(k, v) }
        }

        // Solo el administrativo fija los parámetros del club (v1.5).
        if (vm.estado.rol == Rol.ADMIN) ConfiguracionClub(vm)

        Seccion("Permisos de este rol")
        FilaDeChips(u.permisos)

        BotonBorde(
            "Cerrar sesión",
            vm::pedirCerrarSesion,
            Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 32.dp),
        )
    }
}

/** Parámetros del club (v1.5): gracia, recargo e interés, con el historial de vigencias. */
@Composable
private fun ConfiguracionClub(vm: ClubViewModel) {
    val f = vm.estado.campos
    // Solo precarga si el formulario está vacío: rotar la pantalla no pisa lo que se estaba tipeando.
    LaunchedEffect(Unit) { if (f.pGracia.isBlank()) vm.prepararParametros() }
    val p = vm.parametros
    Seccion("Configuración del club")
    ColumnaGutter {
        Tarjeta(padding = 16.dp) {
            Text("Cuota vencida: gracia, recargo e interés", style = body(600, 15))
            Text(
                "Rigen para todos los socios desde el día en que se guardan. Hoy: " +
                    "gracia ${p.graciaDias} días · recargo ${money(p.recargo.roundToInt())} · " +
                    "interés ${numero(p.interesMensual)} % mensual.",
                style = body(500, 12, Pista.mut, lineHeight = 17.sp),
                modifier = Modifier.padding(top = 4.dp),
            )
            CampoParametro("Días de gracia después del vencimiento", f.pGracia) { v -> vm.campo { copy(pGracia = v) } }
            CampoParametro("Recargo fijo por pago fuera de término ($)", f.pRecargo, decimal = true) { v -> vm.campo { copy(pRecargo = v) } }
            CampoParametro("Interés mensual sobre la cuota (%)", f.pInteres, decimal = true) { v -> vm.campo { copy(pInteres = v) } }
            vm.estado.paramError?.let { err ->
                Text(
                    err,
                    style = body(600, 13, Pista.badInk, lineHeight = 19.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(Pista.rs)
                        .background(Pista.bad)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                )
            }
            BotonPrimario("Guardar parámetros", vm::guardarParametros, Modifier.padding(top = 12.dp), alto = 48.dp, tamano = 15)
        }
    }

    Seccion("Historial de parámetros")
    ColumnaGutter {
        vm.historialParametros.forEach { h ->
            Fila(forma = Pista.rCard) {
                Column(Modifier.weight(1f)) {
                    Text("Desde ${h.vigenteDesde.fmt()}", style = body(600, 14))
                    Text(
                        "Gracia ${h.graciaDias} días · recargo ${money(h.recargo.roundToInt())} · interés ${numero(h.interesMensual)} % mensual",
                        style = body(500, 12, Pista.mut),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                if (h == p) Chip("Vigente", Pista.acc, Pista.accInk)
            }
        }
    }
}

@Composable
private fun CampoParametro(etiqueta: String, valor: String, decimal: Boolean = false, onChange: (String) -> Unit) {
    Text(
        etiqueta.uppercase(),
        style = disp(700, 11, Pista.mut, letterSpacing = 1.sp),
        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
    )
    CampoTexto(valor, onChange, etiqueta, tecladoNumerico = !decimal, tecladoDecimal = decimal)
}

/** Fondo según la situación del socio: rojo si no puede ingresar, naranja en gracia, verde si está bien. */
internal fun fondoEstado(s: SocioVista): Color = when {
    !s.hab -> Pista.bad
    s.enGracia -> Pista.warn
    else -> Pista.ok
}

internal fun tintaEstado(s: SocioVista): Color = when {
    !s.hab -> Pista.badInk
    s.enGracia -> Pista.warnInk
    else -> Pista.okInk
}

/** Color del contador de la cuota. */
internal fun colorTono(t: TonoCuota): Color = when (t) {
    TonoCuota.OK -> Pista.ok
    TonoCuota.AVISO -> Pista.warn
    TonoCuota.BLOQUEO -> Pista.bad
}

/** Chips que se acomodan en varias líneas, como el `flex-wrap` de la maqueta. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilaDeChips(items: List<String>) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Pista.gutter, end = Pista.gutter, top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items.forEach { p ->
            Text(
                p,
                style = body(600, 12),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Pista.sur2)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
            )
        }
    }
}
