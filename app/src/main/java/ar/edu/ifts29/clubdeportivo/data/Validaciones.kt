package ar.edu.ifts29.clubdeportivo.data

/**
 * Validaciones de los formularios. Cada función devuelve el mensaje para el usuario, o
 * `null` si el dato es válido. Cubren datos incompletos y formatos incorrectos.
 */
object Validaciones {

    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val NOMBRE = Regex("^[\\p{L} '.-]+$")

    fun nombre(valor: String, quien: String = ""): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Ingresá nombre y apellido$quien."
            !NOMBRE.matches(v) -> "El nombre solo puede contener letras y espacios."
            v.length < 5 || !v.contains(' ') -> "Ingresá nombre y apellido$quien."
            else -> null
        }
    }

    /** Nombre o apellido por separado (alta de socio): obligatorio y solo letras. */
    fun nombrePropio(valor: String, campo: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Ingresá el $campo."
            !NOMBRE.matches(v) -> "El $campo solo puede contener letras y espacios."
            v.length < 2 -> "El $campo es demasiado corto."
            else -> null
        }
    }

    /** Acepta el DNI con o sin puntos; cualquier otro carácter es un formato incorrecto. */
    fun dni(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Ingresá el DNI."
            v.any { !it.isDigit() && it != '.' && it != ' ' } -> "El DNI solo puede contener números."
            v.count { it.isDigit() } !in 7..8 -> "El DNI debe tener 7 u 8 dígitos."
            else -> null
        }
    }

    fun email(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Ingresá el correo electrónico."
            !EMAIL.matches(v) -> "El correo electrónico no tiene un formato válido (ej.: nombre@dominio.com)."
            else -> null
        }
    }

    /** Peso obligatorio, en kilos. */
    fun peso(valor: String): String? {
        val v = valor.trim().replace(',', '.')
        if (v.isEmpty()) return "Ingresá el peso."
        val n = v.toDoubleOrNull() ?: return "El peso debe ser un número (ej.: 62,4)."
        return if (n !in 20.0..300.0) "El peso debe estar entre 20 y 300 kg." else null
    }

    /** Altura opcional, en metros. */
    fun altura(valor: String): String? {
        val v = valor.trim().replace(',', '.')
        if (v.isEmpty()) return null
        val n = v.toDoubleOrNull() ?: return "La altura debe ser un número en metros (ej.: 1,68)."
        return if (n !in 0.5..2.5) "La altura debe estar entre 0,50 y 2,50 m." else null
    }

    /** Series y repeticiones son opcionales; si se cargan, tienen que ser enteros en rango. */
    fun entero(valor: String, campo: String, rango: IntRange): String? {
        val v = valor.trim()
        if (v.isEmpty()) return null
        val n = v.toIntOrNull() ?: return "$campo deben ser un número entero."
        return if (n !in rango) "$campo deben estar entre ${rango.first} y ${rango.last}." else null
    }
}
