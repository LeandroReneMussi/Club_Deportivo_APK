package ar.edu.ifts29.clubdeportivo.vm

import ar.edu.ifts29.clubdeportivo.data.Actividad
import ar.edu.ifts29.clubdeportivo.data.DemoData
import ar.edu.ifts29.clubdeportivo.data.IngresoHoy
import ar.edu.ifts29.clubdeportivo.data.Rol
import ar.edu.ifts29.clubdeportivo.data.Socio
import ar.edu.ifts29.clubdeportivo.data.Visitante

/** Lo que tiene que sobrevivir al pasar de una Activity a otra: la sesión y los datos del club. */
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
 * Almacenamiento en memoria compartido por todas las Activities del proceso.
 *
 * Cada Activity tiene su propio [ClubViewModel]; sin este objeto, un socio dado de alta
 * por el administrativo desaparecería al cerrar sesión y entrar con otro rol. Como en el
 * prototipo, los datos se reinician recién al cerrar la app. La base de datos llega en la
 * etapa siguiente y reemplaza a este objeto.
 */
object ClubRepositorio {

    @Volatile
    var datos: DatosClub = DatosClub()
        private set

    val rol: Rol? get() = datos.rol

    fun guardar(estado: UiState) {
        datos = DatosClub(
            rol = estado.rol,
            socios = estado.socios,
            acts = estado.acts,
            visitantes = estado.visitantes,
            ingresosHoy = estado.ingresosHoy,
            asistencia = estado.asistencia,
            profePresente = estado.profePresente,
        )
    }

    /** Vuelve a los datos de demostración y cierra la sesión. Usado por las pruebas. */
    fun reiniciar() {
        datos = DatosClub()
    }
}
