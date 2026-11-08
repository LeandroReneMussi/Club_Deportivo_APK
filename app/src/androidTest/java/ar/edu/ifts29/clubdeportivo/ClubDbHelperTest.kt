package ar.edu.ifts29.clubdeportivo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ar.edu.ifts29.clubdeportivo.data.db.ClubDbHelper
import ar.edu.ifts29.clubdeportivo.data.fecha
import ar.edu.ifts29.clubdeportivo.vm.DatosClub
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** SQLite de verdad, en el emulador, sobre una base propia que no toca la de la app. */
@RunWith(AndroidJUnit4::class)
class ClubDbHelperTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val nombre = "test_club.db"

    @Before
    fun limpiar() {
        context.deleteDatabase(nombre)
    }

    private fun conSemilla(): ClubDbHelper = ClubDbHelper(context, nombre).also { it.guardar(DatosClub()) }

    @Test
    fun laSemillaSobreviveACerrarYReabrir() {
        conSemilla().close()
        val h = ClubDbHelper(context, nombre)
        val datos = h.leer()
        assertEquals(8, datos.socios.size)
        assertEquals(5, datos.visitantes.size)
        val carla = datos.visitantes.first { it.nombre == "Carla Núñez" }
        assertEquals(fecha("2026-09-07"), carla.fecha)
        assertEquals("Efectivo", carla.medio)
        assertEquals("Débito", datos.visitantes.first { it.nombre == "Lucía Ferreyra" }.medio)
        h.close()
    }

    @Test
    fun unaBaseVersion2GanaFechaYMedio() {
        val archivo = context.getDatabasePath(nombre)
        archivo.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(archivo, null).use { db ->
            // Una base 1.3.x siempre tiene `actividad`; acá alcanza con la tabla vacía.
            db.execSQL(
                "CREATE TABLE actividad (id TEXT PRIMARY KEY, nombre TEXT NOT NULL, horario TEXT NOT NULL, " +
                    "cupo INTEGER NOT NULL, ocupados INTEGER NOT NULL, precio INTEGER NOT NULL, profesor TEXT NOT NULL, orden INTEGER NOT NULL)",
            )
            db.execSQL(
                "CREATE TABLE visitante (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, " +
                    "dni TEXT NOT NULL, actividad TEXT NOT NULL, hora TEXT NOT NULL, monto INTEGER NOT NULL, orden INTEGER NOT NULL)",
            )
            db.execSQL(
                "INSERT INTO visitante (nombre, dni, actividad, hora, monto, orden) " +
                    "VALUES ('Pedro Alonso', '33.120.455', 'Funcional', '18:10', 6000, 0)",
            )
            db.version = 2
        }
        val h = ClubDbHelper(context, nombre)
        h.readableDatabase.rawQuery("SELECT fecha, medio, nombre FROM visitante", null).use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("2026-09-08", c.getString(0))
            assertEquals("Efectivo", c.getString(1))
            assertEquals("Pedro Alonso", c.getString(2))
        }
        h.close()
    }

    @Test
    fun unaBaseVersion2DescuentaDelCupoLosVisitantesQueYaHabiaSumado() {
        // En la 1.3.x cada visitante cargado desde la app sumaba 1 a `ocupados`; la 1.4.0
        // deriva el cupo del día de los visitantes, así que la migración tiene que deshacerlo.
        val archivo = context.getDatabasePath(nombre)
        archivo.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(archivo, null).use { db ->
            db.execSQL(
                "CREATE TABLE actividad (id TEXT PRIMARY KEY, nombre TEXT NOT NULL, horario TEXT NOT NULL, " +
                    "cupo INTEGER NOT NULL, ocupados INTEGER NOT NULL, precio INTEGER NOT NULL, profesor TEXT NOT NULL, orden INTEGER NOT NULL)",
            )
            db.execSQL("INSERT INTO actividad VALUES ('func', 'Funcional', '19:30', 20, 16, 6000, 'Ana', 0)")
            db.execSQL("INSERT INTO actividad VALUES ('yoga', 'Yoga', '08:30', 15, 9, 5000, 'Inés', 1)")
            db.execSQL(
                "CREATE TABLE visitante (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, " +
                    "dni TEXT NOT NULL, actividad TEXT NOT NULL, hora TEXT NOT NULL, monto INTEGER NOT NULL, orden INTEGER NOT NULL)",
            )
            // Dos cargados desde la app (sumaron) y Pedro, el de la semilla (no sumaba).
            db.execSQL("INSERT INTO visitante (nombre, dni, actividad, hora, monto, orden) VALUES ('Marina López', '31.555.908', 'Funcional', '09:41', 6000, 0)")
            db.execSQL("INSERT INTO visitante (nombre, dni, actividad, hora, monto, orden) VALUES ('Tomás Vega', '42.101.877', 'Funcional', '09:41', 6000, 1)")
            db.execSQL("INSERT INTO visitante (nombre, dni, actividad, hora, monto, orden) VALUES ('Pedro Alonso', '33.120.455', 'Funcional', '18:10', 6000, 2)")
            db.version = 2
        }
        val h = ClubDbHelper(context, nombre)
        h.readableDatabase.rawQuery("SELECT id, ocupados FROM actividad ORDER BY orden", null).use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("func", c.getString(0))
            assertEquals("vuelve a la base: 16 - 2 cargados desde la app", 14, c.getInt(1))
            assertTrue(c.moveToNext())
            assertEquals("sin visitantes no cambia", 9, c.getInt(1))
        }
        h.close()
    }

    @Test
    fun elListadoDeVencimientosFiltraPorRangoDeFechas() {
        val h = conSemilla()
        assertEquals("solo Martín vence hoy", listOf(8), h.idsCuotasPorVencer("2026-09-08", "2026-09-08"))
        assertEquals("Andrés y Valentina en la semana", listOf(1, 5), h.idsCuotasPorVencer("2026-09-09", "2026-09-15"))
        assertTrue(h.idsCuotasPorVencer("2026-09-16", "2026-09-30").isEmpty())
        h.close()
    }

    @Test
    fun losPagosDiariosSeConsultanPorFecha() {
        val h = conSemilla()
        assertEquals(listOf("Carla Núñez", "Julián Soto"), h.visitantesPorFecha("2026-09-07").map { it.nombre })
        assertEquals(3, h.visitantesPorFecha("2026-09-08").size)
        assertEquals(17_000, h.visitantesPorFecha("2026-09-08").sumOf { it.monto })
        assertTrue(h.visitantesPorFecha("2026-09-06").isEmpty())
        h.close()
    }

    @Test
    fun elDniRepetidoSeCuentaSoloEnSuDia() {
        val h = conSemilla()
        assertEquals(1, h.contarVisitante("33.120.455", "2026-09-08"))
        assertEquals(0, h.contarVisitante("33.120.455", "2026-09-07"))
        h.close()
    }
}
