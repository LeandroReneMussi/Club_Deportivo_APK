package ar.edu.ifts29.clubdeportivo.data

import androidx.annotation.DrawableRes

enum class Rol { SOCIO, EMPLEADO, ADMIN, PROFESOR, NUTRICIONISTA }

/** Todas las pantallas del prototipo. Los nombres siguen a los del original. */
enum class Screen {
    LOGIN,

    // Socio
    S_HOME, S_CARNET, S_CUOTAS, S_PAGAR, S_PAGO_OK, S_RUTINA, S_NUTRI, S_TURNO,

    // Empleado / Administrativo
    E_HOME, E_SOCIOS, E_SOCIO, E_ALTA, E_EDITAR, E_ACCESO, E_VISITANTES, E_VISITANTE_NUEVO, E_REPORTES,

    // Profesor
    P_HOME, P_ASISTENCIA, P_RUTINAS, P_RUTINA_EDIT,

    // Nutricionista
    N_AGENDA, N_FICHAS, N_FICHA,

    // Compartida
    PERFIL,
}

enum class EstadoCuota { PENDIENTE, PAGADA, VENCIDA }

data class Cuota(
    val mes: String,
    val monto: Int,
    val vence: Fecha,
    val estado: EstadoCuota,
    /** Vencimiento de la cuota que sigue, cuando esta ya está pagada. */
    val proximaVence: Fecha? = null,
)

data class PagoHistorial(
    val mes: String,
    val monto: Int,
    val pagada: Fecha,
    val medio: String,
    val comprobante: String = "",
)

data class Ejercicio(
    val nombre: String,
    val series: Int,
    val reps: Int,
    val nota: String = "",
)

data class DiaRutina(
    val tag: String,
    val foco: String,
    val ejercicios: List<Ejercicio>,
)

data class Rutina(
    val nombre: String,
    val profesor: String,
    val actualizada: Fecha,
    val dias: List<DiaRutina>,
)

data class Consulta(
    val fecha: Fecha,
    val peso: Double,
    val obs: String,
)

data class FichaNutricional(
    val peso: Double,
    val altura: Double,
    val objetivo: String,
    val antecedentes: String,
    val consultas: List<Consulta>,
)

data class Turno(val fecha: Fecha, val hora: String)

data class ActividadSocio(val nombre: String, val horario: String)

data class Ingreso(val fecha: String, val estado: String)

data class Socio(
    val id: Int,
    @DrawableRes val foto: Int? = null,
    val nombre: String,
    val dni: Long,
    val nro: String,
    val plan: String,
    val alta: Fecha,
    val nac: Fecha,
    val carnetEmitido: Fecha,
    val carnetVence: Fecha,
    /** Apto físico presentado. Sin ficha médica no se le puede asignar rutina (HU-09). */
    val ficha: Boolean,
    val actividades: List<ActividadSocio>,
    val cuota: Cuota,
    val historial: List<PagoHistorial>,
    val rutina: Rutina?,
    val nut: FichaNutricional,
    val turno: Turno?,
    val ingresos: List<Ingreso>,
    val email: String = "",
)

data class Actividad(
    val id: String,
    val nombre: String,
    val horario: String,
    val cupo: Int,
    val ocupados: Int,
    val precio: Int,
    val profesor: String,
    val inscriptos: List<Int>,
)

/** Pago diario de un no socio (HU-03): queda con la fecha y el medio con que se cobró. */
data class Visitante(
    val nombre: String,
    val dni: String,
    val actividad: String,
    val hora: String,
    val monto: Int,
    val fecha: Fecha,
    val medio: String,
)

data class IngresoHoy(
    val hora: String,
    val nombre: String,
    val estado: String,
    val ok: Boolean,
)

data class Plan(val nombre: String, val precio: Int)

data class Usuario(
    val rol: Rol,
    @DrawableRes val avatar: Int?,
    val nombre: String,
    val nombreCorto: String,
    val rolLabel: String,
    val datos: List<Pair<String, String>>,
    val permisos: List<String>,
)

/** Resultado de una validación de acceso por DNI (HU-05). */
data class Acceso(
    val socioId: Int?,
    val titulo: String,
    val detalle: String,
    val tono: Tono,
    val registrado: Boolean = false,
) {
    enum class Tono { NEUTRO, PERMITIDO, DENEGADO }
}

/** Hoja de confirmación genérica del prototipo. */
data class SheetData(
    val titulo: String,
    val texto: String,
    val filas: List<Pair<String, String>> = emptyList(),
    val cta: String,
    val onConfirm: () -> Unit,
)

/** Los campos de formulario, que en la maqueta viven todos juntos en `state.f`. */
data class Campos(
    val email: String = "",
    val pass: String = "",
    val busq: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val dni: String = "",
    val mail: String = "",
    val obs: String = "",
    val dniAcceso: String = "",
    val vNombre: String = "",
    val vDni: String = "",
    val ejNombre: String = "",
    val ejSeries: String = "",
    val ejReps: String = "",
    val nPeso: String = "",
    val nAltura: String = "",
    val nObs: String = "",
    /** Configuración del club (v1.5): gracia en días, recargo en pesos e interés mensual en %. */
    val pGracia: String = "",
    val pRecargo: String = "",
    val pInteres: String = "",
)

enum class FiltroSocios(val label: String) { TODOS("Todos"), HAB("Habilitados"), MORA("En mora") }

/** Pestañas de Reportes (HU-14): vencen hoy, esta semana, en mora y pagos diarios por fecha. */
enum class ReportTab { HOY, VENCER, MORA, VISITANTES }

enum class MedioPago(val nombre: String, val sub: String, val etiqueta: String) {
    TARJETA("Tarjeta de débito / crédito", "Visa terminada en 4421", "Tarjeta"),
    BILLETERA("Billetera virtual", "Transferencia con QR", "Billetera"),
    CAJA("Efectivo en el club", "Genera un cupón para pagar en recepción", "Caja"),
}

/** Medio con que el mostrador cobra el pase diario de un visitante. */
enum class MedioVisitante(val etiqueta: String) { EFECTIVO("Efectivo"), DEBITO("Débito") }

/**
 * Parámetros del club que fija el administrador (v1.5): días de gracia después del
 * vencimiento, recargo fijo en pesos por pagar fuera de término e interés mensual (%)
 * prorrateado por día. Cada guardado rige desde [vigenteDesde]; el historial se conserva.
 */
data class Parametros(
    val graciaDias: Int,
    val recargo: Double,
    val interesMensual: Double,
    val vigenteDesde: Fecha,
) {
    companion object {
        /** Semilla: 5 días de gracia, sin recargo ni interés, vigente desde el día de la instalación. */
        val INICIALES: Parametros get() = Parametros(5, 0.0, 0.0, HOY)
    }
}

/** El parámetro que regía en [fecha]: el más nuevo con vigencia hasta esa fecha; antes del primero, el más antiguo. */
fun List<Parametros>.vigenteEn(fecha: Fecha): Parametros {
    val ordenados = sortedByDescending { it.vigenteDesde }
    return ordenados.firstOrNull { it.vigenteDesde <= fecha } ?: ordenados.last()
}
