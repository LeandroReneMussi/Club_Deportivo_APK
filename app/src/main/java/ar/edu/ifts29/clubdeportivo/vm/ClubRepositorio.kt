package ar.edu.ifts29.clubdeportivo.vm

import android.content.Context
import ar.edu.ifts29.clubdeportivo.data.Actividad
import ar.edu.ifts29.clubdeportivo.data.DemoData
import ar.edu.ifts29.clubdeportivo.data.Fecha
import ar.edu.ifts29.clubdeportivo.data.IngresoHoy
import ar.edu.ifts29.clubdeportivo.data.Rol
import ar.edu.ifts29.clubdeportivo.data.Socio
import ar.edu.ifts29.clubdeportivo.data.Visitante
import ar.edu.ifts29.clubdeportivo.data.db.ClubDbHelper
import ar.edu.ifts29.clubdeportivo.data.fmtIso
import ar.edu.ifts29.clubdeportivo.data.norm

/** Lo que tiene que sobrevivir al pasar de una Activity a otra y al cerrar la app. */
data class DatosClub(
    val rol: Rol? = null,
    val socios: List<Socio> = DemoData.socios,
    val acts: List<Actividad> = DemoData.actividades,
    val visitantes: List<Visitante> = DemoData.visitantesIniciales,
    val ingresosHoy: List<IngresoHoy> = DemoData.ingresosHoyIniciales,
    val asistencia: Map<String, List<Int>> = mapOf("musc" to listOf(4, 7), "func" to emptyList()),
    val profePresente: Boolean = false,
)

/**
 * Punto único de los datos del club.
 *
 * En el teléfono delega en [ClubDbHelper]: el alta, la modificación, la baja, el cobro
 * y el listado quedan en SQLite y siguen ahí al cerrar la app. Las pruebas unitarias
 * no abren la base y siguen usando la copia en memoria.
 */
object ClubRepositorio {

    @Volatile
    var datos: DatosClub = DatosClub()
        private set

    @Volatile
    private var db: ClubDbHelper? = null

    val rol: Rol? get() = datos.rol

    val usaBase: Boolean get() = db != null

    /** La llama [ar.edu.ifts29.clubdeportivo.ClubApplication] al abrir la app. */
    fun iniciar(context: Context) {
        if (db != null) return
        val helper = ClubDbHelper(context.applicationContext)
        if (helper.estaVacia()) helper.guardar(DatosClub())
        helper.completarCuotasSiguientes()
        db = helper
        datos = helper.leer()
    }

    fun guardar(estado: UiState) {
        val nuevo = DatosClub(
            rol = estado.rol,
            socios = estado.socios,
            acts = estado.acts,
            visitantes = estado.visitantes,
            ingresosHoy = estado.ingresosHoy,
            asistencia = estado.asistencia,
            profePresente = estado.profePresente,
        )
        val cambio = nuevo != datos
        datos = nuevo
        if (cambio) db?.guardar(nuevo)
    }

    /**
     * `COUNT` del DNI. Con la base abierta es la consulta del ABM; sin ella, el padrón
     * en memoria (pruebas).
     */
    fun contarDni(dni: Long, exceptoId: Int?): Int {
        val helper = db
        if (helper != null) return helper.contarDni(dni, exceptoId)
        return datos.socios.count { it.dni == dni && it.id != exceptoId }
    }

    /** Ids de las cuotas pendientes que vencen en el rango. `null` cuando no hay base (pruebas). */
    fun idsCuotasPorVencer(desdeIso: String, hastaIso: String): List<Int>? =
        db?.idsCuotasPorVencer(desdeIso, hastaIso)

    /** Pagos diarios de un día. `null` cuando no hay base (pruebas). */
    fun visitantesPorFecha(fecha: Fecha): List<Visitante>? = db?.visitantesPorFecha(fecha.fmtIso())

    /**
     * `COUNT` del DNI del visitante en el día. Con la base abierta es la consulta; sin
     * ella, la copia en memoria (pruebas).
     */
    fun contarVisitante(dni: String, fecha: Fecha): Int {
        val helper = db
        if (helper != null) return helper.contarVisitante(dni, fecha.fmtIso())
        return datos.visitantes.count { it.dni == dni && it.fecha == fecha }
    }

    /** Alias y clave contra la tabla `usuario`, o las cuentas de demo si no hay base. */
    fun autenticar(alias: String, clave: String): Rol? {
        val helper = db
        if (helper != null) return helper.autenticar(alias, clave)
        val cuenta = DemoData.cuentas[norm(alias)] ?: return null
        if (cuenta.first != clave) return null
        return cuenta.second
    }

    /** Vuelve a los datos de demostración y cierra la sesión. Usado por las pruebas. */
    fun reiniciar() {
        val fresco = DatosClub()
        datos = fresco
        db?.guardar(fresco)
    }
}
