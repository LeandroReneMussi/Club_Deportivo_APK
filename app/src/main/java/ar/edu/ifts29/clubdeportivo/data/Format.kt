package ar.edu.ifts29.clubdeportivo.data

import kotlin.math.abs

/**
 * La maqueta congela el "hoy" del prototipo en el 8 de septiembre de 2026
 * (`const TODAY = new Date(2026, 8, 8, 9, 41)`), y todos los vencimientos, agendas
 * y estados de cuota se calculan contra esa fecha. Mantener el mismo anclaje es lo
 * que hace que los datos de demo cuenten la historia que se diseñó: Lucas en mora,
 * Sofía con el carnet vencido, la cuota de Andrés a punto de vencer.
 */
val HOY = Fecha(2026, 9, 8)

const val HORA_ACTUAL = "09:41"

private val DIAS = listOf(
    "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado",
)

private val MESES = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)

/**
 * Fecha civil mínima. El prototipo usa strings ISO y `new Date(y, m-1, d)`; acá alcanza
 * con un valor propio, que además evita depender de `java.time` (API 26) o del
 * desugaring en un minSdk 24.
 */
data class Fecha(val anio: Int, val mes: Int, val dia: Int) : Comparable<Fecha> {

    /** Días desde 1970-01-01, algoritmo `days_from_civil` de Howard Hinnant. */
    val epochDay: Long
        get() {
            val y = if (mes <= 2) anio - 1 else anio
            val era = (if (y >= 0) y else y - 399) / 400
            val yoe = y - era * 400
            val mp = (mes + 9) % 12
            val doy = (153 * mp + 2) / 5 + dia - 1
            val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
            return era * 146097L + doe - 719468L
        }

    /** 0 = domingo, como `Date.getDay()`. */
    val diaSemana: Int get() = (((epochDay + 4) % 7 + 7) % 7).toInt()

    override fun compareTo(other: Fecha): Int = epochDay.compareTo(other.epochDay)

    override fun toString(): String = fmtIso()
}

/** Parsea `"2026-09-10"`. */
fun fecha(iso: String): Fecha {
    val p = iso.split("-")
    return Fecha(p[0].toInt(), p[1].toInt(), p[2].toInt())
}

/** Reconstruye una fecha civil a partir de los días desde 1970-01-01. */
fun desdeEpochDay(dias: Long): Fecha {
    val z = dias + 719468L
    val era = (if (z >= 0) z else z - 146096L) / 146097L
    val doe = z - era * 146097L
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = if (mp < 10) mp + 3 else mp - 9
    return Fecha((if (m <= 2) y + 1 else y).toInt(), m.toInt(), d.toInt())
}

fun Fecha.masDias(n: Int): Fecha = desdeEpochDay(epochDay + n)

/** Misma fecha [n] años después; el 29/02 cae al 28/02. */
fun Fecha.masAnios(n: Int): Fecha {
    val a = anio + n
    val ultimo = diasDelMes(a, mes)
    return Fecha(a, mes, minOf(dia, ultimo))
}

private fun diasDelMes(anio: Int, mes: Int): Int = when (mes) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    else -> if (anio % 4 == 0 && (anio % 100 != 0 || anio % 400 == 0)) 29 else 28
}

private fun p2(n: Int) = n.toString().padStart(2, '0')

/** `fmt` de la maqueta: `10/09/2026`. */
fun Fecha.fmt(): String = "${p2(dia)}/${p2(mes)}/$anio"

fun Fecha.fmtIso(): String = "$anio-${p2(mes)}-${p2(dia)}"

/** `fmtCorto` de la maqueta: `Vie 11 sep`. */
fun Fecha.fmtCorto(): String = "${DIAS[diaSemana].take(3)} $dia ${MESES[mes - 1].take(3)}"

/** `mm/yyyy`, como el `altaLabel` del prototipo. */
fun Fecha.fmtMesAnio(): String = "${p2(mes)}/$anio"

/** `Martes 8 de septiembre`. */
fun Fecha.fmtLargo(): String = "${DIAS[diaSemana]} $dia de ${MESES[mes - 1]}"

/** Días entre [HOY] y la fecha; negativo si ya pasó. */
fun daysTo(f: Fecha): Int = (f.epochDay - HOY.epochDay).toInt()

/** Años cumplidos a [HOY]. */
fun edadA(nacimiento: Fecha): Int {
    var edad = HOY.anio - nacimiento.anio
    if (HOY.mes < nacimiento.mes || (HOY.mes == nacimiento.mes && HOY.dia < nacimiento.dia)) edad--
    return edad
}

/**
 * `money` de la maqueta: `'$' + n.toLocaleString('es-AR')` → `$18.500`.
 * Se escribe a mano para no depender del locale del dispositivo, que en un teléfono
 * configurado en inglés formatearía `$18,500`.
 */
fun money(n: Int): String {
    val s = abs(n).toString()
    val out = StringBuilder()
    for ((i, c) in s.withIndex()) {
        if (i > 0 && (s.length - i) % 3 == 0) out.append('.')
        out.append(c)
    }
    return (if (n < 0) "-$" else "$") + out
}

/** `dniFmt`: `38412907` → `38.412.907`. */
fun dniFmt(dni: Any): String {
    val s = dni.toString().filter { it.isDigit() }
    val out = StringBuilder()
    for ((i, c) in s.withIndex()) {
        if (i > 0 && (s.length - i) % 3 == 0) out.append('.')
        out.append(c)
    }
    return out.toString()
}

/**
 * Un decimal, con punto, como el `toFixed(1)` de la maqueta (`IMC 22.1`).
 * Se fuerza [java.util.Locale.US] para que un teléfono en español no muestre `22,1`.
 */
fun unDecimal(d: Double): String = String.format(java.util.Locale.US, "%.1f", d)

/**
 * Número "como lo escribiría JavaScript": `62.4` → `"62.4"`, pero `81.0` → `"81"`.
 * Los pesos de la ficha nutricional se muestran así en la maqueta.
 */
fun numero(d: Double): String =
    if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()

/** `norm` del login: sin espacios, minúsculas y sin tildes. */
fun norm(s: String): String = s.trim().lowercase()
    .replace('á', 'a').replace('é', 'e').replace('í', 'i')
    .replace('ó', 'o').replace('ú', 'u').replace('ü', 'u')
