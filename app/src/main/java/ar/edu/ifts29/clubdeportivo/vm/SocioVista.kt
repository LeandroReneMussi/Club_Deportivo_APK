package ar.edu.ifts29.clubdeportivo.vm

import ar.edu.ifts29.clubdeportivo.data.Consulta
import ar.edu.ifts29.clubdeportivo.data.DemoData
import ar.edu.ifts29.clubdeportivo.data.EstadoCuota
import ar.edu.ifts29.clubdeportivo.data.HOY
import ar.edu.ifts29.clubdeportivo.data.Parametros
import ar.edu.ifts29.clubdeportivo.data.Socio
import ar.edu.ifts29.clubdeportivo.data.daysTo
import ar.edu.ifts29.clubdeportivo.data.dniFmt
import ar.edu.ifts29.clubdeportivo.data.edadA
import ar.edu.ifts29.clubdeportivo.data.fmt
import ar.edu.ifts29.clubdeportivo.data.fmtCorto
import ar.edu.ifts29.clubdeportivo.data.fmtMesAnio
import ar.edu.ifts29.clubdeportivo.data.masDias
import ar.edu.ifts29.clubdeportivo.data.masMeses
import ar.edu.ifts29.clubdeportivo.data.money
import ar.edu.ifts29.clubdeportivo.data.numero
import ar.edu.ifts29.clubdeportivo.data.unDecimal
import ar.edu.ifts29.clubdeportivo.data.vigenteEn
import kotlin.math.roundToInt

/** Color con que se muestra la situación de la cuota: verde, naranja o rojo. */
enum class TonoCuota { OK, AVISO, BLOQUEO }

/**
 * Equivalente de `socioView()` del prototipo: toma un [Socio] y deriva todas las
 * etiquetas y estados que las pantallas muestran, para que ninguna vista tenga que
 * repetir estas reglas.
 *
 * La regla central del sistema vive acá: un socio está **habilitado** sólo si no
 * está en mora y su carnet no venció (HU-05, HU-09). Desde la v1.5 la mora se calcula
 * con las fechas y los [parametros] del club: la cuota vencida tiene un período de
 * gracia, y recién pasado ese plazo bloquea el acceso. Fuera de término se deben
 * además el recargo y el interés vigentes.
 */
class SocioVista(val socio: Socio, private val parametros: List<Parametros> = listOf(Parametros.INICIALES)) {

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

    // ------------------------------------------------------------ cuota: pendiente, gracia, mora

    private val diasParaVencer = daysTo(socio.cuota.vence)

    /** Días transcurridos desde el vencimiento; 0 si todavía no venció. */
    val diasVencida = (-diasParaVencer).coerceAtLeast(0)

    private val vigenteHoy = parametros.vigenteEn(HOY)

    /** Días de gracia que rigen hoy. */
    val gracia = vigenteHoy.graciaDias

    val cuotaPagada = socio.cuota.estado == EstadoCuota.PAGADA

    /** No pagada y con el vencimiento ya pasado. */
    val cuotaVencida = !cuotaPagada && diasVencida > 0

    /** Vencida, pero dentro del período de gracia: sigue habilitado, con aviso. */
    val enGracia = cuotaVencida && diasVencida <= gracia

    /** Vencida y con la gracia superada: bloquea el acceso. */
    val mora = cuotaVencida && diasVencida > gracia

    /** Días de gracia que quedan (0 en el último día). */
    val diasDeGracia = (gracia - diasVencida).coerceAtLeast(0)

    /** Sin pagar, venza o no: es lo que habilita el botón de pago. */
    val cuotaPendiente = !cuotaPagada

    val carnetVencido = daysTo(socio.carnetVence) < 0

    /** Habilitado para ingresar y para recibir rutina. */
    val hab = !mora && !carnetVencido

    // ------------------------------------------------------------ deuda (v1.5)

    /** Recargo fijo del parámetro vigente hoy; se debe una sola vez, apenas la cuota vence. */
    val recargo: Double = if (cuotaVencida) vigenteHoy.recargo else 0.0

    /**
     * Interés simple acumulado día a día desde el vencimiento, con la tasa mensual que regía
     * cada día (prorrateada a 30 días). Así un cambio de tasa no toca los días anteriores.
     */
    val interes: Double = if (!cuotaVencida) {
        0.0
    } else {
        (1..diasVencida).sumOf { d ->
            socio.cuota.monto * parametros.vigenteEn(socio.cuota.vence.masDias(d)).interesMensual / 100.0 / 30.0
        }
    }

    /** Lo que hay que cobrar hoy: cuota más recargo e interés si venció. */
    val totalAdeudado: Int = (socio.cuota.monto + recargo + interes).roundToInt()

    val totalLabel = money(totalAdeudado)

    /** "Cuota $12.000 + recargo $500 + interés $348", o `null` si no hay nada que sumar. */
    val deudaDetalle: String? = if (cuotaVencida && (recargo > 0 || interes > 0)) {
        "Cuota ${money(socio.cuota.monto)} + recargo ${money(recargo.roundToInt())} + interés ${money(interes.roundToInt())}"
    } else {
        null
    }

    // ------------------------------------------------------------ etiquetas

    val nombreCorto = socio.nombre.substringBefore(' ')
    val dniLabel = dniFmt(socio.dni)
    val altaLabel = socio.alta.fmtMesAnio()
    val edadLabel = "${edadA(socio.nac)} años"

    private val graciaRestanteLabel = when (diasDeGracia) {
        0 -> "Último día de gracia"
        1 -> "Queda 1 día de gracia"
        else -> "Quedan $diasDeGracia días de gracia"
    }

    /** El contador que ven el administrador y el socio, con su color en [tono]. */
    val contador = when {
        cuotaPagada -> "Pagada"
        enGracia -> graciaRestanteLabel
        mora -> "Vencida hace $diasVencida días · gracia de $gracia días superada"
        diasParaVencer == 0 -> "Vence hoy"
        else -> "Vence en $diasParaVencer días"
    }

    val tono = when {
        mora -> TonoCuota.BLOQUEO
        enGracia -> TonoCuota.AVISO
        else -> TonoCuota.OK
    }

    val badge = when {
        hab && enGracia -> "Habilitado · En gracia"
        hab && !cuotaPagada -> "Habilitado · Cuota pendiente"
        hab -> "Habilitado · Cuota al día"
        mora -> "No habilitado · En mora"
        else -> "No habilitado · Carnet vencido"
    }

    val badgeCorto = when {
        hab && enGracia -> "En gracia"
        hab -> "Habilitado"
        mora -> "En mora"
        else -> "Carnet venc."
    }

    val motivo = when {
        hab && enGracia -> graciaRestanteLabel
        hab -> "Puede ingresar"
        mora -> "En mora · cuota ${socio.cuota.mes} vencida hace $diasVencida días · debe $totalLabel"
        else -> "Carnet vencido el ${socio.carnetVence.fmt()}"
    }

    val cuotaMonto = money(socio.cuota.monto)

    val cuotaEstadoLabel = when {
        cuotaPagada -> "Pagada"
        cuotaVencida -> "Vencida"
        else -> "Pendiente"
    }

    val cuotaCorto = when {
        mora -> "Mora"
        enGracia -> "Gracia"
        else -> "Al día"
    }

    val cuotaVenceLabel = when {
        cuotaPagada -> {
            val proxima = socio.cuota.proximaVence ?: socio.cuota.vence.masMeses(1)
            "Pagada · próxima ${proxima.fmt()}"
        }
        mora -> "Venció hace $diasVencida días · gracia de $gracia días superada"
        enGracia -> "Venció hace $diasVencida días · ${graciaRestanteLabel.replaceFirstChar { it.lowercase() }}"
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
