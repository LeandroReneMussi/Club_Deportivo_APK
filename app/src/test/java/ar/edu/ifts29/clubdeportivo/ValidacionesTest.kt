package ar.edu.ifts29.clubdeportivo

import ar.edu.ifts29.clubdeportivo.data.Validaciones
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {

    @Test
    fun `nombre`() {
        assertNull(Validaciones.nombre("Carlos Gardel"))
        assertNull(Validaciones.nombre("María José Núñez"))
        assertNotNull(Validaciones.nombre(""))
        assertNotNull("falta el apellido", Validaciones.nombre("Carlos"))
        assertNotNull("números", Validaciones.nombre("Carlos G4rdel"))
    }

    @Test
    fun `dni con o sin puntos`() {
        assertNull(Validaciones.dni("12.345.678"))
        assertNull(Validaciones.dni("1234567"))
        assertNotNull(Validaciones.dni(""))
        assertNotNull(Validaciones.dni("123456"))
        assertNotNull(Validaciones.dni("123456789"))
        assertNotNull(Validaciones.dni("12A45678"))
    }

    @Test
    fun `email`() {
        assertNull(Validaciones.email("carlos@gardel.com.ar"))
        assertNotNull(Validaciones.email(""))
        assertNotNull(Validaciones.email("carlos"))
        assertNotNull(Validaciones.email("carlos@gardel"))
        assertNotNull(Validaciones.email("carlos gardel@mail.com"))
    }

    @Test
    fun `peso obligatorio y en rango, con coma o punto`() {
        assertNull(Validaciones.peso("62,4"))
        assertNull(Validaciones.peso("62.4"))
        assertNotNull(Validaciones.peso(""))
        assertNotNull(Validaciones.peso("abc"))
        assertNotNull(Validaciones.peso("5"))
        assertNotNull(Validaciones.peso("400"))
    }

    @Test
    fun `altura opcional pero en rango`() {
        assertNull(Validaciones.altura(""))
        assertNull(Validaciones.altura("1,68"))
        assertNotNull(Validaciones.altura("168"))
        assertNotNull(Validaciones.altura("-1"))
        assertNotNull(Validaciones.altura("uno"))
    }

    @Test
    fun `series y repeticiones`() {
        assertNull(Validaciones.entero("", "Las series", 1..20))
        assertNull(Validaciones.entero("4", "Las series", 1..20))
        assertNotNull(Validaciones.entero("0", "Las series", 1..20))
        assertNotNull(Validaciones.entero("3.5", "Las series", 1..20))
    }
}
