package ar.edu.ifts29.clubdeportivo

import ar.edu.ifts29.clubdeportivo.data.HOY
import ar.edu.ifts29.clubdeportivo.data.daysTo
import ar.edu.ifts29.clubdeportivo.data.desdeEpochDay
import ar.edu.ifts29.clubdeportivo.data.dniFmt
import ar.edu.ifts29.clubdeportivo.data.edadA
import ar.edu.ifts29.clubdeportivo.data.fecha
import ar.edu.ifts29.clubdeportivo.data.fmt
import ar.edu.ifts29.clubdeportivo.data.fmtCorto
import ar.edu.ifts29.clubdeportivo.data.fmtIso
import ar.edu.ifts29.clubdeportivo.data.fmtLargo
import ar.edu.ifts29.clubdeportivo.data.fmtMesAnio
import ar.edu.ifts29.clubdeportivo.data.masAnios
import ar.edu.ifts29.clubdeportivo.data.masDias
import ar.edu.ifts29.clubdeportivo.data.money
import ar.edu.ifts29.clubdeportivo.data.norm
import ar.edu.ifts29.clubdeportivo.data.numero
import ar.edu.ifts29.clubdeportivo.data.unDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Las utilidades de fecha y formato están escritas a mano para no depender de
 * `java.time` ni del locale del dispositivo, así que conviene fijarlas con pruebas.
 */
class FormatoTest {

    @Test
    fun `el hoy del prototipo es martes 8 de septiembre de 2026`() {
        assertEquals(2, HOY.diaSemana)
        assertEquals("Martes 8 de septiembre", HOY.fmtLargo().replaceFirstChar { it.uppercase() })
        assertEquals("08/09/2026", HOY.fmt())
        assertEquals("09/2026", HOY.fmtMesAnio())
    }

    @Test
    fun `formato corto de fecha`() {
        assertEquals("Vie 11 sep", fecha("2026-09-11").fmtCorto())
        assertEquals("Mar 8 sep", HOY.fmtCorto())
    }

    @Test
    fun `los importes usan el separador de miles argentino`() {
        assertEquals("$500", money(500))
        assertEquals("$18.500", money(18500))
        assertEquals("$118.500", money(118500))
        assertEquals("$1.234.567", money(1234567))
    }

    @Test
    fun `el DNI se muestra con puntos`() {
        assertEquals("38.412.907", dniFmt(38412907L))
        assertEquals("1.234.567", dniFmt("1234567"))
    }

    @Test
    fun `sumar dias cruza meses y anios`() {
        assertEquals("18/09/2026", HOY.masDias(10).fmt())
        assertEquals("01/10/2026", fecha("2026-09-30").masDias(1).fmt())
        assertEquals("01/01/2027", fecha("2026-12-31").masDias(1).fmt())
        assertEquals("31/12/2026", fecha("2027-01-01").masDias(-1).fmt())
    }

    @Test
    fun `un carnet dura doce meses`() {
        assertEquals("07/09/2027", HOY.masAnios(1).masDias(-1).fmt())
    }

    @Test
    fun `el 29 de febrero cae al 28 en anios no bisiestos`() {
        assertEquals("28/02/2027", fecha("2024-02-29").masAnios(3).fmt())
        assertEquals("29/02/2028", fecha("2024-02-29").masAnios(4).fmt())
    }

    @Test
    fun `epochDay va y vuelve`() {
        listOf("1970-01-01", "2000-02-29", "2026-09-08", "2027-12-31").forEach {
            assertEquals(it, desdeEpochDay(fecha(it).epochDay).fmtIso())
        }
    }

    @Test
    fun `los dias al vencimiento se cuentan contra hoy`() {
        assertEquals(2, daysTo(fecha("2026-09-10")))
        assertEquals(0, daysTo(HOY))
        assertEquals(-29, daysTo(fecha("2026-08-10")))
    }

    @Test
    fun `la edad se calcula a la fecha del prototipo`() {
        assertEquals(32, edadA(fecha("1994-06-21")))
        // Cumple después del 8 de septiembre: todavía no los cumplió.
        assertEquals(34, edadA(fecha("1991-11-03")))
    }

    @Test
    fun `los numeros se muestran como en la maqueta`() {
        assertEquals("22.1", unDecimal(22.12))
        assertEquals("81", numero(81.0))
        assertEquals("62.4", numero(62.4))
    }

    @Test
    fun `el usuario del login se normaliza`() {
        assertEquals("andres", norm("  ANDRÉS "))
        assertEquals("jorge", norm("Jorge"))
    }
}
