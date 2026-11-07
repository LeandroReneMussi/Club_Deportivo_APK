package ar.edu.ifts29.clubdeportivo.vm

import ar.edu.ifts29.clubdeportivo.data.Consulta
import ar.edu.ifts29.clubdeportivo.data.DemoData
import ar.edu.ifts29.clubdeportivo.data.EstadoCuota
import ar.edu.ifts29.clubdeportivo.data.Socio
import ar.edu.ifts29.clubdeportivo.data.daysTo
import ar.edu.ifts29.clubdeportivo.data.dniFmt
import ar.edu.ifts29.clubdeportivo.data.edadA
import ar.edu.ifts29.clubdeportivo.data.fmt
import ar.edu.ifts29.clubdeportivo.data.fmtCorto
import ar.edu.ifts29.clubdeportivo.data.fmtMesAnio
import ar.edu.ifts29.clubdeportivo.data.masMeses
import ar.edu.ifts29.clubdeportivo.data.money
import ar.edu.ifts29.clubdeportivo.data.numero
import ar.edu.ifts29.clubdeportivo.data.unDecimal

/**
 * Equivalente de `socioView()` del prototipo: toma un [Socio] y deriva todas las
 * etiquetas y estados que las pantallas muestran, para que ninguna vista tenga que
 * repetir estas reglas.
 *
 * La regla central del sistema vive acá: un socio está **habilitado** sólo si no
 * está en mora y su carnet no venció (HU-05, HU-09).
 */
class SocioVista(val socio: Socio) {

    val id get() = socio.id
    val nombre get() = socio.nombre
    val nro get() = socio.nro
    val plan get() = socio.plan
    val cuota get() = socio.cuota
    val foto get() = socio.foto
    val ficha get() = socio.ficha
    val rutina get() = socio.rutina
    val turno get() = socio.turno
    val nut get() = socio.nut
    val actividades get() = socio.actividades
    val historial get() = socio.historial
    val ingresos get() = socio.ingresos

    val mora = socio.cuota.estado == EstadoCuota.VENCIDA
    val carnetVencido = daysTo(socio.carnetVence) < 0

    /** Habilitado para ingresar y para recibir rutina. */
    val hab = !mora && !carnetVencido

    private val diasParaVencer = daysTo(socio.cuota.vence)

    val nombreCorto = socio.nombre.substringBefore(' ')
    val dniLabel = dniFmt(socio.dni)
    val altaLabel = socio.alta.fmtMesAnio()
    val edadLabel = "${edadA(socio.nac)} años"

    val badge = when {
        hab -> "Habilitado · Cuota al día"
        mora -> "No habilitado · En mora"
        else -> "No habilitado · Carnet vencido"
    }

    val badgeCorto = when {
        hab -> "Habilitado"
        mora -> "En mora"
        else -> "Carnet venc."
    }

    val motivo = when {
        hab -> "Puede ingresar"
        mora -> "Cuota ${socio.cuota.mes} vencida hace ${-diasParaVencer} días"
        else -> "Carnet vencido el ${socio.carnetVence.fmt()}"
    }

    val cuotaMonto = money(socio.cuota.monto)
    val cuotaPendiente = socio.cuota.estado == EstadoCuota.PENDIENTE
    val cuotaPagada = socio.cuota.estado == EstadoCuota.PAGADA

    val cuotaEstadoLabel = when {
        cuotaPagada -> "Pagada"
        mora -> "Vencida"
        else -> "Pendiente"
    }

    val cuotaCorto = if (mora) "Mora" else "Al día"

    val cuotaVenceLabel = when {
        cuotaPagada -> {
            val proxima = socio.cuota.proximaVence ?: socio.cuota.vence.masMeses(1)
            "Pagada · próxima ${proxima.fmt()}"
        }
        diasParaVencer < 0 -> "Venció hace ${-diasParaVencer} días"
        diasParaVencer == 0 -> "Vence hoy"
        else -> "Vence en $diasParaVencer días · ${socio.cuota.vence.fmt()}"
    }

    val venceRel = when {
        diasParaVencer < 0 -> "venció hace ${-diasParaVencer} d"
        diasParaVencer == 0 -> "vence hoy"
        else -> "vence en $diasParaVencer d"
    }

    val carnetEstado = when {
        carnetVencido -> "Vencido"
        daysTo(socio.carnetVence) < 30 -> "Por vencer"
        else -> "Vigente"
    }

    val carnetEstadoLower = if (carnetVencido) "vencido" else "vigente"
    val carnetAccion = if (carnetVencido) "Renovar carnet" else "Reemitir carnet"
    val carnetEmitidoLabel = socio.carnetEmitido.fmt()
    val carnetVenceLabel = socio.carnetVence.fmt()

    val tieneRutina = socio.rutina != null
    val rutinaFechaLabel = socio.rutina?.actualizada?.fmt() ?: ""

    val rutinaSub = socio.rutina?.let {
        "${it.nombre} · ${it.dias.size} días · act. ${it.actualizada.fmt()}"
    } ?: if (socio.ficha) "Sin rutina asignada" else "Sin rutina · falta ficha médica"

    val rutinaTag = if (tieneRutina) "Ver" else "Crear"

    val fichaLabel = if (socio.ficha) "Apto físico vigente" else "Falta apto físico"

    /** HU-09: sin habilitación o sin apto físico no se puede asignar una rutina. */
    val permiteRutina = hab && socio.ficha
    val bloqueaRutina = !permiteRutina

    val bloqueoMotivo = when {
        !hab && mora -> "cuota en mora"
        !hab -> "carnet vencido"
        else -> "falta la ficha médica"
    }

    val actividadesLabel = socio.actividades.joinToString(", ") { it.nombre }.ifEmpty { "—" }

    /** Un alta nueva todavía no tiene medidas cargadas: sin altura no hay IMC que mostrar. */
    val imc = if (socio.nut.altura > 0) {
        unDecimal(socio.nut.peso / (socio.nut.altura * socio.nut.altura))
    } else {
        "—"
    }
    val pesoLabel = numero(socio.nut.peso)
    val alturaLabel = numero(socio.nut.altura)

    val consultas: List<Consulta> = socio.nut.consultas

    val nutSub = consultas.firstOrNull()
        ?.let { "Última consulta ${it.fecha.fmt()} · ${numero(socio.nut.peso)} kg" }
        ?: "Sin consultas registradas"

    val tieneTurno = socio.turno != null
    val turnoLabel = socio.turno?.let { "${it.fecha.fmtCorto()} · ${it.hora}" } ?: "Sin turno"
    val turnoSub = if (tieneTurno) DemoData.NUTRICIONISTA else "Solicitá uno"
}
