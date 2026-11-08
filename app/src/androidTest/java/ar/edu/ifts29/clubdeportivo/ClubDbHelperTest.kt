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
}
