package ar.edu.ifts29.clubdeportivo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.ifts29.clubdeportivo.data.Acceso
import ar.edu.ifts29.clubdeportivo.data.DemoData
import ar.edu.ifts29.clubdeportivo.data.FiltroSocios
import ar.edu.ifts29.clubdeportivo.data.HOY
import ar.edu.ifts29.clubdeportivo.data.MedioVisitante
import ar.edu.ifts29.clubdeportivo.data.ReportTab
import ar.edu.ifts29.clubdeportivo.data.Rol
import ar.edu.ifts29.clubdeportivo.data.Screen
import ar.edu.ifts29.clubdeportivo.data.Visitante
import ar.edu.ifts29.clubdeportivo.data.dniFmt
import ar.edu.ifts29.clubdeportivo.data.fmt
import ar.edu.ifts29.clubdeportivo.data.fmtCorto
import ar.edu.ifts29.clubdeportivo.data.money
import ar.edu.ifts29.clubdeportivo.data.numero
import kotlin.math.roundToInt
import ar.edu.ifts29.clubdeportivo.ui.BarraCupo
import ar.edu.ifts29.clubdeportivo.ui.BotonBorde
import ar.edu.ifts29.clubdeportivo.ui.BotonPrimario
import ar.edu.ifts29.clubdeportivo.ui.CampoLargo
import ar.edu.ifts29.clubdeportivo.ui.CampoTexto
import ar.edu.ifts29.clubdeportivo.ui.Chip
import ar.edu.ifts29.clubdeportivo.ui.ColumnaGutter
import ar.edu.ifts29.clubdeportivo.ui.CuerpoPantalla
import ar.edu.ifts29.clubdeportivo.ui.Fila
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

/** Avatar de lista de socios: la maqueta usa una trama diagonal, no una foto. */
@Composable
fun AvatarTrama(tamano: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(tamano)
            .clip(CircleShape)
            .background(Pista.sur2),
    )
}

// ------------------------------------------------------------------ inicio del mostrador

@Composable
fun ColumnScope.PantallaEmpleadoHome(vm: ClubViewModel) {
    val u = vm.usuario
    CuerpoPantalla {
        HeaderSaludo(
            contexto = "${vm.hoyLabel} · ${u.rolLabel}",
            nombreCorto = u.nombreCorto,
            avatar = u.avatar,
            onSalir = vm::pedirCerrarSesion,
            onPerfil = { vm.ir(Screen.PERFIL) },
        )

        val socios = vm.socios
        val mora = vm.enMora
        val hoy = vm.vencenHoy
        val vencer = vm.porVencer
        val visitantesHoy = vm.visitantesDeHoy
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Kpi(
                    "Socios activos", socios.size.toString(),
                    "${socios.count { it.hab }} habilitados",
                    Modifier.weight(1f),
                ) { vm.elegirFiltro(FiltroSocios.TODOS); vm.ir(Screen.E_SOCIOS) }
                Kpi(
                    "En mora", mora.size.toString(), "ingreso bloqueado",
                    Modifier.weight(1f),
                    fondo = if (mora.isNotEmpty()) Pista.bad else Pista.sur,
                    tinta = if (mora.isNotEmpty()) Pista.badInk else Pista.ink,
                ) { vm.elegirReportTab(ReportTab.MORA); vm.ir(Screen.E_REPORTES) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Kpi(
                    "Vencen hoy", hoy.size.toString(),
                    money(hoy.sumOf { it.cuota.monto }),
                    Modifier.weight(1f),
                    fondo = if (hoy.isNotEmpty()) Pista.warn else Pista.sur,
                    tinta = if (hoy.isNotEmpty()) Pista.warnInk else Pista.ink,
                ) { vm.elegirReportTab(ReportTab.HOY); vm.ir(Screen.E_REPORTES) }
                Kpi(
                    "Esta semana", vencer.size.toString(),
                    money(vencer.sumOf { it.cuota.monto }),
                    Modifier.weight(1f),
                ) { vm.elegirReportTab(ReportTab.VENCER); vm.ir(Screen.E_REPORTES) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Kpi(
                    "Ingresos hoy", vm.ingresosDeHoy.toString(),
                    "${visitantesHoy.size} visitantes",
                    Modifier.weight(1f),
                ) { vm.ir(Screen.E_ACCESO) }
                Kpi(
                    "Pagos diarios", visitantesHoy.size.toString(),
                    money(vm.totalPagosHoy),
                    Modifier.weight(1f),
                ) { vm.elegirReportTab(ReportTab.VISITANTES); vm.ir(Screen.E_REPORTES) }
            }
        }

        Seccion("Acciones rápidas")
        ColumnaGutter {
            AccionRapida(
                "Validar ingreso",
                "Buscar socio por DNI y verificar habilitación",
                destacada = true,
            ) { vm.ir(Screen.E_ACCESO) }
            AccionRapida("Registrar socio", "Alta con carnet, cuota inicial y ficha médica") { vm.irAAlta() }
            AccionRapida("Registrar visitante", "Ingreso ocasional con actividad y pago") { vm.irAVisitanteNuevo() }
            if (vm.estado.rol == Rol.ADMIN) {
                AccionRapida("Parámetros del club", "Gracia, recargo e interés de la cuota vencida") { vm.ir(Screen.PERFIL) }
            }
        }

        // Supervisión: sólo el administrativo la ve.
        if (vm.estado.rol == Rol.ADMIN) {
            Seccion("Supervisión · Asistencia de profesores hoy")
            ColumnaGutter {
                val estadoAna = if (vm.estado.profePresente) "Presente" else "Sin marcar"
                FilaProfesor(DemoData.PROFESORA, "Musculación 18:00 · Funcional 19:30", estadoAna)
                DemoData.profesoresDelDia.forEach { (nombre, clases, estado) ->
                    FilaProfesor(nombre, clases, estado)
                }
            }
        }
    }
}

@Composable
private fun FilaProfesor(nombre: String, clases: String, estado: String) {
    Fila {
        Column(Modifier.weight(1f)) {
            Text(nombre, style = body(600, 14))
            Text(clases, style = body(500, 12, Pista.mut), modifier = Modifier.padding(top = 2.dp))
        }
        Text(
            estado,
            style = body(700, 14, if (estado.startsWith("Presente")) Pista.acc else Pista.warn),
        )
    }
}

@Composable
private fun Kpi(
    titulo: String,
    valor: String,
    sub: String,
    modifier: Modifier = Modifier,
    fondo: Color = Pista.sur,
    tinta: Color = Pista.ink,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .clip(Pista.r)
            .background(fondo)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(
            titulo.uppercase(),
            style = body(600, 11, tinta.copy(alpha = 0.75f), letterSpacing = 1.2.sp),
        )
        Text(
            valor,
            style = disp(800, 40, tinta, lineHeight = 40.sp),
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            sub,
            style = body(500, 12, tinta.copy(alpha = 0.8f)),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun AccionRapida(titulo: String, sub: String, destacada: Boolean = false, onClick: () -> Unit) {
    val fondo = if (destacada) Pista.acc else Pista.sur
    val tinta = if (destacada) Pista.accInk else Pista.ink
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Pista.rCard)
            .background(fondo)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(titulo.uppercase(), style = disp(700, 18, tinta))
            Text(
                sub,
                style = body(500, 12, if (destacada) tinta.copy(alpha = 0.8f) else Pista.mut),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text("→", style = disp(700, 20, tinta))
    }
}

// ------------------------------------------------------------------ socios

@Composable
fun ColumnScope.PantallaSocios(vm: ClubViewModel) {
    CuerpoPantalla {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Socios".uppercase(),
                style = disp(700, 32, lineHeight = 32.sp),
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(Pista.rs)
                    .background(Pista.acc)
                    .clickable { vm.irAAlta() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text("+ Alta".uppercase(), style = disp(700, 13, Pista.accInk, letterSpacing = 0.8.sp))
            }
        }

        CampoTexto(
            valor = vm.estado.campos.busq,
            onValorChange = { v -> vm.campo { copy(busq = v) } },
            placeholder = "Buscar por DNI o nombre",
            alto = 48.dp,
            imeAction = ImeAction.Search,
            modifier = Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 16.dp),
        )

        Row(
            modifier = Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FiltroSocios.entries.forEach { f ->
                val activo = vm.estado.filtro == f
                Text(
                    f.label,
                    style = body(600, 12, if (activo) Pista.bg else Pista.mut),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activo) Pista.ink else Pista.sur)
                        .clickable { vm.elegirFiltro(f) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }

        ColumnaGutter(top = 14.dp) {
            vm.sociosFiltrados.forEach { s ->
                FilaSocio(s) { vm.abrirSocio(s.id) }
            }
            if (vm.sociosFiltrados.isEmpty()) {
                Text("Ningún socio coincide con la búsqueda.", style = body(500, 14, Pista.mut))
            }
        }
    }
}

@Composable
private fun FilaSocio(s: SocioVista, onClick: () -> Unit) {
    Fila(fondo = Pista.sur, forma = Pista.rCard, onClick = onClick) {
        AvatarTrama(40.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(s.nombre, style = body(600, 15))
            Text(
                "DNI ${s.dniLabel} · N° ${s.nro}",
                style = body(500, 12, Pista.mut),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Chip(
            texto = s.badgeCorto,
            fondo = fondoEstado(s),
            tinta = tintaEstado(s),
        )
    }
}

/** HU-07: ficha del socio en el mostrador, con cobro y emisión de carnet. */
@Composable
fun ColumnScope.PantallaFichaSocio(vm: ClubViewModel) {
    val s = vm.seleccionado
    CuerpoPantalla {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AvatarTrama(56.dp)
            Column {
                Text(s.nombre.uppercase(), style = disp(700, 26, lineHeight = 26.sp))
                Text(
                    "DNI ${s.dniLabel} · N° ${s.nro} · Plan ${s.plan}",
                    style = body(500, 13, Pista.mut),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 16.dp)
                .clip(Pista.rCard)
                .background(fondoEstado(s))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val tinta = tintaEstado(s)
            Text(
                s.badge.uppercase(),
                style = disp(700, 15, tinta, letterSpacing = 0.8.sp, lineHeight = 19.sp),
                modifier = Modifier.weight(1f),
                maxLines = 2,
            )
            Spacer(Modifier.width(12.dp))
            Capsula(s.estadoCorto, Color.Black.copy(alpha = 0.18f), tinta)
        }

        Tarjeta(
            Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 18.dp),
            padding = 16.dp,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Cuota ${s.cuota.mes}".uppercase(),
                    style = disp(700, 13, Pista.mut, letterSpacing = 1.5.sp),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                CapsulaCuota(s)
            }
            Text(s.cuotaSubLabel, style = body(500, 12, Pista.mut), modifier = Modifier.padding(top = 6.dp))
            s.deudaDetalle?.let {
                Text(it, style = body(500, 12, Pista.mut), modifier = Modifier.padding(top = 2.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(s.totalLabel, style = disp(800, 34, lineHeight = 34.sp), modifier = Modifier.weight(1f))
                Text(
                    s.cuotaEstadoLabel.uppercase(),
                    style = disp(700, 14, letterSpacing = 0.8.sp),
                )
            }
            if (s.cuotaPendiente) {
                BotonPrimario(
                    "Registrar pago en caja",
                    vm::pedirRegistrarPago,
                    Modifier.padding(top = 14.dp),
                    fondo = Pista.cta,
                    tinta = Pista.ctaInk,
                    alto = 48.dp,
                    tamano = 15,
                )
            }
        }

        Tarjeta(
            Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 12.dp),
            padding = 16.dp,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Carnet".uppercase(),
                    style = disp(700, 13, Pista.mut, letterSpacing = 1.5.sp),
                    modifier = Modifier.weight(1f),
                )
                Chip(
                    texto = s.carnetEstado,
                    fondo = if (s.carnetVencido) Pista.bad else Pista.accSoft,
                    tinta = if (s.carnetVencido) Pista.badInk else Pista.acc,
                )
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Text("Emitido", style = body(500, 14, Pista.mut), modifier = Modifier.weight(1f))
                Text(s.carnetEmitidoLabel, style = body(500, 14))
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text("Vence", style = body(500, 14, Pista.mut), modifier = Modifier.weight(1f))
                Text(s.carnetVenceLabel, style = body(500, 14))
            }
            BotonBorde(
                s.carnetAccion,
                vm::pedirRenovarCarnet,
                Modifier.padding(top = 14.dp),
                alto = 48.dp,
            )
        }

        Tarjeta(
            Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 12.dp),
            padding = 16.dp,
        ) {
            FilaDetalle("Alta", s.altaLabel)
            Spacer(Modifier.height(6.dp))
            FilaDetalle("Correo", s.socio.email.ifBlank { "—" })
            Spacer(Modifier.height(6.dp))
            FilaDetalle("Ficha médica", s.fichaLabel)
            Spacer(Modifier.height(6.dp))
            FilaDetalle("Actividades", s.actividadesLabel)
        }

        // CRUD de socios: modificación y baja.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BotonBorde("Editar datos", vm::irAEditar, Modifier.weight(1f), alto = 48.dp)
            BotonBorde(
                "Dar de baja",
                vm::pedirBajaSocio,
                Modifier.weight(1f),
                tinta = Pista.bad,
                borde = Pista.bad,
                alto = 48.dp,
            )
        }
    }
}

@Composable
private fun FilaDetalle(clave: String, valor: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(clave, style = body(500, 14, Pista.mut), modifier = Modifier.weight(1f))
        Text(valor, style = body(500, 14), textAlign = TextAlign.End)
    }
}

// ------------------------------------------------------------------ alta de socio

/** HU-02. */
@Composable
fun ColumnScope.PantallaAlta(vm: ClubViewModel) {
    val f = vm.estado.campos
    CuerpoPantalla {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CampoTexto(f.nombre, { v -> vm.campo { copy(nombre = v) } }, "Nombre")
            CampoTexto(f.apellido, { v -> vm.campo { copy(apellido = v) } }, "Apellido")
            CampoTexto(f.dni, { v -> vm.campo { copy(dni = v) } }, "DNI (sin puntos)", tecladoNumerico = true)
            CampoTexto(f.mail, { v -> vm.campo { copy(mail = v) } }, "Correo electrónico", tecladoEmail = true)

            Text(
                "Plan".uppercase(),
                style = disp(700, 13, Pista.mut, letterSpacing = 1.5.sp),
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DemoData.planes.forEachIndexed { i, p ->
                    val activo = i == vm.estado.planIdx
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(Pista.rRow)
                            .background(if (activo) Pista.accSoft else Pista.sur)
                            .border(2.dp, if (activo) Pista.acc else Pista.line, Pista.rRow)
                            .clickable { vm.elegirPlan(i) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(p.nombre.uppercase(), style = disp(700, 16))
                        Text(
                            "${money(p.precio)}/mes",
                            style = body(500, 12, Pista.mut),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }

            Text(
                "Ficha médica básica".uppercase(),
                style = disp(700, 13, Pista.mut, letterSpacing = 1.5.sp),
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Pista.rRow)
                    .background(Pista.sur)
                    .clickable(onClick = vm::alternarApto)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Apto físico presentado", style = body(600, 15))
                    Text(
                        "Requerido para asignar rutinas",
                        style = body(500, 12, Pista.mut),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Interruptor(vm.estado.apto)
            }

            CampoLargo(
                f.obs,
                { v -> vm.campo { copy(obs = v) } },
                "Antecedentes u observaciones (opcional)",
            )

            vm.estado.altaError?.let { err ->
                Text(
                    err,
                    style = body(600, 13, Pista.badInk),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Pista.rs)
                        .background(Pista.bad)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                )
            }

            Text(
                if (vm.editando) {
                    "Los cambios se aplican al guardar. Un cambio de plan rige desde la próxima cuota."
                } else {
                    "Al confirmar se genera: N° de socio, carnet vigente por 12 meses y cuota " +
                        "inicial ${money(vm.planSeleccionado.precio)} con vencimiento ${vm.venceCuotaInicial.fmt()}."
                },
                style = body(500, 12, Pista.mut, lineHeight = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Pista.rRow)
                    .background(Pista.sur2)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            )

            BotonPrimario(if (vm.editando) "Guardar cambios" else "Registrar socio", vm::confirmarAlta)
        }
    }
}

/** Switch de la maqueta: 44×26 con la perilla blanca de 20. */
@Composable
private fun Interruptor(activo: Boolean) {
    Box(
        modifier = Modifier
            .size(44.dp, 26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (activo) Pista.acc else Pista.line)
            .padding(3.dp),
        contentAlignment = if (activo) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(Modifier.size(20.dp).clip(CircleShape).background(Color.White))
    }
}

// ------------------------------------------------------------------ control de acceso

/** HU-05. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColumnScope.PantallaAcceso(vm: ClubViewModel) {
    CuerpoPantalla {
        Column(Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp)) {
            Text("Control de acceso".uppercase(), style = disp(700, 32, lineHeight = 32.sp))
            Text(
                "Ingresá el DNI del socio para validar carnet y cuota.",
                style = body(500, 13, Pista.mut),
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CampoTexto(
                valor = vm.estado.campos.dniAcceso,
                onValorChange = { v -> vm.campo { copy(dniAcceso = v) } },
                placeholder = "DNI",
                modifier = Modifier.weight(1f),
                alto = 56.dp,
                tamano = 24,
                peso = 800,
                tecladoNumerico = true,
                fuenteDisplay = true,
                imeAction = ImeAction.Go,
                onImeAction = vm::validarAcceso,
            )
            Box(
                modifier = Modifier
                    .height(56.dp)
                    .clip(Pista.rs)
                    .background(Pista.acc)
                    .clickable(onClick = vm::validarAcceso)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Validar".uppercase(), style = disp(700, 15, Pista.accInk, letterSpacing = 0.8.sp))
            }
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "Probar:",
                style = body(500, 12, Pista.mut),
                modifier = Modifier.padding(vertical = 6.dp),
            )
            DemoData.dnisDemo.forEach { (dni, etiqueta) ->
                Text(
                    "${dniFmt(dni)} · $etiqueta",
                    style = body(600, 12),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, Pista.line, RoundedCornerShape(6.dp))
                        .clickable { vm.ponerDniAcceso(dni) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }

        vm.estado.acceso?.let { a -> ResultadoAcceso(vm, a) }

        Seccion("Ingresos de hoy · ${vm.estado.ingresosHoy.size}")
        ColumnaGutter {
            vm.estado.ingresosHoy.forEach { i ->
                Fila(paddingV = 12.dp) {
                    Text("${i.hora} · ${i.nombre}", style = body(500, 14), modifier = Modifier.weight(1f))
                    Text(i.estado, style = body(600, 14, if (i.ok) Pista.acc else Pista.bad))
                }
            }
        }
    }
}

@Composable
private fun ResultadoAcceso(vm: ClubViewModel, a: Acceso) {
    val fondo = when (a.tono) {
        Acceso.Tono.PERMITIDO -> Pista.ok
        Acceso.Tono.DENEGADO -> Pista.bad
        Acceso.Tono.NEUTRO -> Pista.sur
    }
    val tinta = when (a.tono) {
        Acceso.Tono.PERMITIDO -> Pista.okInk
        Acceso.Tono.DENEGADO -> Pista.badInk
        Acceso.Tono.NEUTRO -> Pista.ink
    }
    val s = a.socioId?.let { id -> vm.socios.firstOrNull { it.id == id } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp)
            .clip(Pista.r)
            .background(fondo)
            .padding(22.dp),
    ) {
        Text(
            "Resultado".uppercase(),
            style = body(600, 12, tinta.copy(alpha = 0.75f), letterSpacing = 1.2.sp),
        )
        Text(
            a.titulo.uppercase(),
            style = disp(800, 44, tinta, lineHeight = 44.sp),
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            a.detalle,
            style = body(600, 15, tinta, lineHeight = 21.sp),
            modifier = Modifier.padding(top = 10.dp),
        )

        if (s != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .lineaPunteada(tinta.copy(alpha = 0.3f))
                    .padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                FotoCarnet(s.foto, 52.dp, 64.dp)
                Column {
                    Text(s.nombre.uppercase(), style = disp(800, 22, tinta, lineHeight = 22.sp))
                    Text(
                        "N° ${s.nro} · Carnet ${s.carnetEstadoLower} hasta ${s.carnetVenceLabel}",
                        style = body(600, 13, tinta.copy(alpha = 0.8f)),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        "Cuota ${s.cuota.mes}: ${s.cuotaEstadoLabel}",
                        style = body(600, 13, tinta.copy(alpha = 0.8f)),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (s.hab && !a.registrado) {
                    BotonPrimario(
                        "Registrar ingreso",
                        vm::registrarIngreso,
                        Modifier.weight(1f),
                        fondo = Pista.ink,
                        tinta = Pista.bg,
                        alto = 48.dp,
                        tamano = 15,
                    )
                }
                BotonBorde(
                    "Ver ficha",
                    { vm.abrirSocio(s.id) },
                    Modifier.weight(1f),
                    tinta = tinta,
                    borde = tinta.copy(alpha = 0.3f),
                    alto = 48.dp,
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BotonBorde("Registrar visitante", vm::registrarComoVisitante, Modifier.weight(1f), alto = 48.dp)
                BotonBorde("Registrar socio", { vm.irAAlta() }, Modifier.weight(1f), alto = 48.dp)
            }
        }
    }
}

// ------------------------------------------------------------------ visitantes

/** HU-03. */
@Composable
fun ColumnScope.PantallaVisitantes(vm: ClubViewModel) {
    CuerpoPantalla {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Visitantes".uppercase(),
                style = disp(700, 32, lineHeight = 32.sp),
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(Pista.rs)
                    .background(Pista.acc)
                    .clickable { vm.irAVisitanteNuevo() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text("+ Ingreso".uppercase(), style = disp(700, 13, Pista.accInk, letterSpacing = 0.8.sp))
            }
        }

        Seccion("Cupos de hoy", top = 18.dp)
        ColumnaGutter {
            vm.actividades.forEach { a ->
                val color = when {
                    a.sinCupo -> Pista.bad
                    a.cupoJusto -> Pista.warn
                    else -> Pista.acc
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(Pista.rCard)
                        .background(Pista.sur)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                            Text(a.nombre, style = body(600, 15))
                            Text(" · ${a.horario}", style = body(500, 13, Pista.mut))
                        }
                        Text(a.cupoLabel, style = disp(700, 14, color))
                    }
                    BarraCupo(a.ocupacion, color, Modifier.padding(top = 10.dp))
                }
            }
        }

        val deHoy = vm.visitantesDeHoy
        Seccion("Pagos diarios de hoy · ${HOY.fmtCorto()} · ${deHoy.size}")
        ColumnaGutter {
            if (deHoy.isEmpty()) {
                TextoVacio("Sin pagos diarios hoy.")
            } else {
                deHoy.forEach { v -> FilaPagoDiario(v) }
                FilaTotal("Recaudado hoy · ${deHoy.size} pagos", vm.totalPagosHoy)
            }
        }
    }
}

/** Un pago diario: nombre, DNI · actividad · hora · medio, y el importe. */
@Composable
private fun FilaPagoDiario(v: Visitante, fondo: Color = Pista.sur2) {
    Fila(fondo = fondo, forma = Pista.rCard) {
        Column(Modifier.weight(1f)) {
            Text(v.nombre, style = body(600, 15))
            Text(
                "DNI ${v.dni} · ${v.actividad} · ${v.hora} · ${v.medio}",
                style = body(500, 12, Pista.mut),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text(money(v.monto), style = disp(800, 16))
    }
}

/** Fila de cierre de una lista de pagos: "Recaudado hoy · 3 pagos" y el total. */
@Composable
private fun FilaTotal(texto: String, monto: Int) {
    Fila(fondo = Pista.acc, forma = Pista.rCard) {
        Text(
            texto.uppercase(),
            style = disp(700, 12, Pista.accInk, letterSpacing = 1.sp),
            modifier = Modifier.weight(1f),
        )
        Text(money(monto), style = disp(800, 18, Pista.accInk))
    }
}

@Composable
private fun TextoVacio(texto: String) {
    Text(
        texto,
        style = body(500, 14, Pista.mut),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(Pista.r)
            .background(Pista.sur)
            .padding(24.dp),
    )
}

@Composable
fun ColumnScope.PantallaVisitanteNuevo(vm: ClubViewModel) {
    val f = vm.estado.campos
    CuerpoPantalla {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CampoTexto(f.vNombre, { v -> vm.campo { copy(vNombre = v) } }, "Nombre y apellido")
            CampoTexto(f.vDni, { v -> vm.campo { copy(vDni = v) } }, "DNI", tecladoNumerico = true)

            Text(
                "Actividad".uppercase(),
                style = disp(700, 13, Pista.mut, letterSpacing = 1.5.sp),
                modifier = Modifier.padding(top = 8.dp),
            )
            vm.actividades.forEach { a ->
                val elegida = vm.estado.visAct == a.id
                val color = when {
                    a.sinCupo -> Pista.bad
                    a.cupoJusto -> Pista.warn
                    else -> Pista.acc
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Pista.rCard)
                        .background(if (elegida) Pista.sur2 else Pista.sur)
                        .border(
                            2.dp,
                            if (elegida) (if (a.sinCupo) Pista.bad else Pista.acc) else Pista.line,
                            Pista.rCard,
                        )
                        .clickable { vm.elegirActividadVisitante(a) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            a.nombre,
                            style = body(600, 15, if (a.sinCupo) Pista.ink.copy(alpha = 0.7f) else Pista.ink),
                        )
                        Text(
                            "${a.horario} · ${a.precioLabel}",
                            style = body(500, 12, Pista.mut),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        a.cupoLabel.uppercase(),
                        style = disp(700, 13, color, letterSpacing = 0.8.sp),
                    )
                }
            }

            Text(
                "Medio de pago".uppercase(),
                style = disp(700, 13, Pista.mut, letterSpacing = 1.5.sp),
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MedioVisitante.entries.forEach { m ->
                    val elegido = vm.estado.visMedio == m
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(Pista.rCard)
                            .background(if (elegido) Pista.acc else Pista.sur)
                            .border(2.dp, if (elegido) Pista.acc else Pista.line, Pista.rCard)
                            .clickable { vm.elegirMedioVisitante(m) }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            m.etiqueta.uppercase(),
                            style = disp(700, 13, if (elegido) Pista.accInk else Pista.ink, letterSpacing = 0.8.sp),
                        )
                    }
                }
            }

            vm.estado.visError?.let { err ->
                Text(
                    err,
                    style = body(600, 13, Pista.badInk, lineHeight = 19.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Pista.rs)
                        .background(Pista.bad)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                )
            }

            Fila(fondo = Pista.sur2, forma = Pista.rRow) {
                Text("Fecha", style = body(500, 14, Pista.mut), modifier = Modifier.weight(1f))
                Text(vm.hoyLabel, style = body(600, 14))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Pista.rRow)
                    .background(Pista.sur2)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Total a cobrar · pase diario", style = body(500, 14, Pista.mut), modifier = Modifier.weight(1f))
                Text(vm.actividadElegida?.precioLabel ?: "—", style = disp(800, 20))
            }

            BotonPrimario("Registrar pago diario", vm::confirmarVisitante)
        }
    }
}

// ------------------------------------------------------------------ reportes

/** HU-14. */
@Composable
fun ColumnScope.PantallaReportes(vm: ClubViewModel) {
    CuerpoPantalla {
        Text(
            "Reportes".uppercase(),
            style = disp(700, 32, lineHeight = 32.sp),
            modifier = Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 20.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val tab = vm.estado.reportTab
            TabReporte(
                "Vencen hoy", vm.vencenHoy.size,
                activo = tab == ReportTab.HOY,
                fondoActivo = Pista.warn, tintaActiva = Pista.warnInk,
                modifier = Modifier.weight(1f),
            ) { vm.elegirReportTab(ReportTab.HOY) }
            TabReporte(
                "Esta semana", vm.porVencer.size,
                activo = tab == ReportTab.VENCER,
                fondoActivo = Pista.acc, tintaActiva = Pista.accInk,
                modifier = Modifier.weight(1f),
            ) { vm.elegirReportTab(ReportTab.VENCER) }
            TabReporte(
                "En mora", vm.enMora.size,
                activo = tab == ReportTab.MORA,
                fondoActivo = Pista.bad, tintaActiva = Pista.badInk,
                modifier = Modifier.weight(1f),
            ) { vm.elegirReportTab(ReportTab.MORA) }
            TabReporte(
                "Visitantes", vm.pagosDiarios.size,
                activo = tab == ReportTab.VISITANTES,
                fondoActivo = Pista.cta, tintaActiva = Pista.ctaInk,
                modifier = Modifier.weight(1f),
            ) { vm.elegirReportTab(ReportTab.VISITANTES) }
        }

        if (vm.estado.reportTab == ReportTab.VISITANTES) SelectorFecha(vm)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Pista.gutter, end = Pista.gutter, top = 18.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                vm.tituloReporte.uppercase(),
                style = disp(700, 13, Pista.mut, letterSpacing = 1.5.sp),
                modifier = Modifier.weight(1f),
            )
            Text("Total ${vm.totalReporte}", style = disp(700, 14))
        }

        if (vm.estado.reportTab == ReportTab.MORA) {
            val p = vm.parametros
            Text(
                "Rige: gracia ${p.graciaDias} días · recargo ${money(p.recargo.roundToInt())} · " +
                    "interés ${numero(p.interesMensual)} % mensual · desde ${p.vigenteDesde.fmt()}. " +
                    "El total suma cuota, recargo e interés.",
                style = body(500, 12, Pista.mut, lineHeight = 17.sp),
                modifier = Modifier.padding(start = Pista.gutter, end = Pista.gutter, top = 6.dp),
            )
        }

        if (vm.estado.reportTab == ReportTab.VISITANTES) {
            val pagos = vm.pagosDiarios
            ColumnaGutter {
                if (pagos.isEmpty()) {
                    TextoVacio("Sin pagos diarios el ${vm.estado.fechaReporte.fmt()}.")
                } else {
                    pagos.forEach { v -> FilaPagoDiario(v, fondo = Pista.sur) }
                }
            }
            return@CuerpoPantalla
        }

        ColumnaGutter {
            vm.filasReporte.forEach { s ->
                Fila(fondo = Pista.sur, forma = Pista.rCard, onClick = { vm.abrirSocio(s.id) }) {
                    Column(Modifier.weight(1f)) {
                        Text(s.nombre, style = body(600, 15))
                        Text(
                            "DNI ${s.dniLabel} · ${s.cuota.mes} · ${s.venceRel}",
                            style = body(500, 12, Pista.mut),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            if (vm.estado.reportTab == ReportTab.MORA) s.totalLabel else s.cuotaMonto,
                            style = disp(800, 16),
                        )
                        Text(
                            s.badgeCorto.uppercase(),
                            style = body(600, 11, if (!s.hab) Pista.bad else if (s.enGracia) Pista.warn else Pista.acc, letterSpacing = 0.8.sp),
                        )
                    }
                }
            }
            if (vm.filasReporte.isEmpty()) TextoVacio("Sin registros en este reporte.")
        }
    }
}

/** Día de la pestaña Visitantes: ‹ día anterior · fecha · día siguiente › (hasta hoy). */
@Composable
private fun SelectorFecha(vm: ClubViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Pista.gutter, end = Pista.gutter, top = 14.dp)
            .clip(Pista.rCard)
            .background(Pista.sur)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BotonDia("‹", habilitado = true) { vm.diaReporteAnterior() }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(vm.fechaReporteLabel, style = body(600, 15))
            Text(
                vm.fechaReporteSub.uppercase(),
                style = disp(700, 11, Pista.mut, letterSpacing = 1.sp),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        BotonDia("›", habilitado = vm.puedeAvanzarDia) { vm.diaReporteSiguiente() }
    }
}

@Composable
private fun BotonDia(simbolo: String, habilitado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(Pista.rs)
            .background(if (habilitado) Pista.sur2 else Pista.sur)
            .clickable(enabled = habilitado, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            simbolo,
            style = disp(800, 26, if (habilitado) Pista.ink else Pista.ink.copy(alpha = 0.3f), lineHeight = 26.sp),
        )
    }
}

@Composable
private fun TabReporte(
    etiqueta: String,
    cuenta: Int,
    activo: Boolean,
    fondoActivo: Color,
    tintaActiva: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tinta = if (activo) tintaActiva else Pista.ink
    Column(
        modifier
            .clip(Pista.rCard)
            .background(if (activo) fondoActivo else Pista.sur)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 14.dp),
    ) {
        Text("$cuenta", style = disp(800, 30, tinta, lineHeight = 30.sp))
        Text(
            etiqueta,
            style = body(600, 12, tinta.copy(alpha = 0.85f)),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
