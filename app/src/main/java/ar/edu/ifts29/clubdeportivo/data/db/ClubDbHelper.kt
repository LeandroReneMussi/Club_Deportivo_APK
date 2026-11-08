package ar.edu.ifts29.clubdeportivo.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ar.edu.ifts29.clubdeportivo.data.Actividad
import ar.edu.ifts29.clubdeportivo.data.ActividadSocio
import ar.edu.ifts29.clubdeportivo.data.Consulta
import ar.edu.ifts29.clubdeportivo.data.Cuota
import ar.edu.ifts29.clubdeportivo.data.DemoData
import ar.edu.ifts29.clubdeportivo.data.DiaRutina
import ar.edu.ifts29.clubdeportivo.data.Ejercicio
import ar.edu.ifts29.clubdeportivo.data.EstadoCuota
import ar.edu.ifts29.clubdeportivo.data.FichaNutricional
import ar.edu.ifts29.clubdeportivo.data.HOY
import ar.edu.ifts29.clubdeportivo.data.Ingreso
import ar.edu.ifts29.clubdeportivo.data.IngresoHoy
import ar.edu.ifts29.clubdeportivo.data.PagoHistorial
import ar.edu.ifts29.clubdeportivo.data.Parametros
import ar.edu.ifts29.clubdeportivo.data.Rol
import ar.edu.ifts29.clubdeportivo.data.Rutina
import ar.edu.ifts29.clubdeportivo.data.Socio
import ar.edu.ifts29.clubdeportivo.data.Turno
import ar.edu.ifts29.clubdeportivo.data.Visitante
import ar.edu.ifts29.clubdeportivo.data.fecha
import ar.edu.ifts29.clubdeportivo.data.fmtIso
import ar.edu.ifts29.clubdeportivo.data.masMeses
import ar.edu.ifts29.clubdeportivo.data.norm
import ar.edu.ifts29.clubdeportivo.data.tituloMes
import ar.edu.ifts29.clubdeportivo.vm.DatosClub
import ar.edu.ifts29.clubdeportivo.vm.ID_SOCIO_APP
import org.json.JSONArray
import org.json.JSONObject

/**
 * Base SQLite del club. El esquema y las consultas siguen el ABM de estudiantes
 * del curso: `SQLiteOpenHelper`, `COUNT` antes de aceptar un alta, `rawQuery`
 * con cursor y listado con `ORDER BY`.
 *
 * Lo que E4 pide como tablas (usuario, socio, carnet, cuota, pago, plan) está
 * normalizado. Rutina, consultas, actividades del socio e ingresos se guardan
 * en JSON para no perder el resto de las historias al cerrar la app.
 */
class ClubDbHelper(context: Context, nombre: String = NOMBRE) : SQLiteOpenHelper(context, nombre, null, VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE plan (
                nombre TEXT PRIMARY KEY,
                precio INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE usuario (
                alias TEXT PRIMARY KEY,
                clave TEXT NOT NULL,
                rol TEXT NOT NULL,
                socio_id INTEGER
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE socio (
                id INTEGER PRIMARY KEY,
                nombre TEXT NOT NULL,
                dni INTEGER NOT NULL UNIQUE,
                nro TEXT NOT NULL,
                email TEXT NOT NULL DEFAULT '',
                plan TEXT NOT NULL,
                alta TEXT NOT NULL,
                nac TEXT NOT NULL,
                ficha INTEGER NOT NULL,
                foto INTEGER,
                nut_peso REAL NOT NULL,
                nut_altura REAL NOT NULL,
                nut_objetivo TEXT NOT NULL,
                nut_antecedentes TEXT NOT NULL,
                turno_fecha TEXT,
                turno_hora TEXT,
                rutina_json TEXT,
                consultas_json TEXT,
                actividades_json TEXT,
                ingresos_json TEXT
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE carnet (
                socio_id INTEGER PRIMARY KEY,
                emitido TEXT NOT NULL,
                vence TEXT NOT NULL,
                FOREIGN KEY (socio_id) REFERENCES socio(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE cuota (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                socio_id INTEGER NOT NULL,
                mes TEXT NOT NULL,
                monto INTEGER NOT NULL,
                vence TEXT NOT NULL,
                estado TEXT NOT NULL,
                vigente INTEGER NOT NULL,
                proxima_vence TEXT,
                FOREIGN KEY (socio_id) REFERENCES socio(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE pago (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                socio_id INTEGER NOT NULL,
                mes TEXT NOT NULL,
                monto INTEGER NOT NULL,
                pagada TEXT NOT NULL,
                medio TEXT NOT NULL,
                orden INTEGER NOT NULL,
                comprobante TEXT NOT NULL DEFAULT '',
                FOREIGN KEY (socio_id) REFERENCES socio(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE actividad (
                id TEXT PRIMARY KEY,
                nombre TEXT NOT NULL,
                horario TEXT NOT NULL,
                cupo INTEGER NOT NULL,
                ocupados INTEGER NOT NULL,
                precio INTEGER NOT NULL,
                profesor TEXT NOT NULL,
                orden INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE actividad_inscripto (
                actividad_id TEXT NOT NULL,
                socio_id INTEGER NOT NULL,
                orden INTEGER NOT NULL,
                PRIMARY KEY (actividad_id, socio_id)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE visitante (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                dni TEXT NOT NULL,
                actividad TEXT NOT NULL,
                hora TEXT NOT NULL,
                monto INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                medio TEXT NOT NULL,
                orden INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE ingreso_hoy (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                hora TEXT NOT NULL,
                nombre TEXT NOT NULL,
                estado TEXT NOT NULL,
                ok INTEGER NOT NULL,
                orden INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE asistencia_clase (
                clase_id TEXT PRIMARY KEY,
                orden INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE asistencia (
                clase_id TEXT NOT NULL,
                socio_id INTEGER NOT NULL,
                orden INTEGER NOT NULL,
                PRIMARY KEY (clase_id, socio_id)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE sesion (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                rol TEXT,
                profe_presente INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(CREATE_PARAMETRO)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            // Parámetros del club (gracia, recargo, interés) con vigencia por fecha (v1.5).
            // Sin filas, el repositorio usa los valores iniciales hasta el primer guardado.
            db.execSQL(CREATE_PARAMETRO)
        }
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE cuota ADD COLUMN proxima_vence TEXT")
            db.execSQL("ALTER TABLE pago ADD COLUMN comprobante TEXT NOT NULL DEFAULT ''")
        }
        if (oldVersion < 3) {
            // Los pagos diarios ya registrados quedan como de hoy y en efectivo.
            db.execSQL("ALTER TABLE visitante ADD COLUMN fecha TEXT NOT NULL DEFAULT ''")
            db.execSQL("UPDATE visitante SET fecha = ? WHERE fecha = ''", arrayOf(HOY.fmtIso()))
            db.execSQL("ALTER TABLE visitante ADD COLUMN medio TEXT NOT NULL DEFAULT 'Efectivo'")
            // Hasta la 1.3.x cada visitante cargado desde la app sumaba 1 a `ocupados`. Desde la
            // 1.4.0 el cupo del día se deriva de los visitantes de hoy, así que se deshace esa
            // suma para no contarlos dos veces. Pedro Alonso es el de la semilla: nunca sumó.
            db.execSQL(
                """
                UPDATE actividad SET ocupados = MAX(0, ocupados - (
                    SELECT COUNT(*) FROM visitante v
                    WHERE v.actividad = actividad.nombre
                      AND NOT (v.dni = '33.120.455' AND v.nombre = 'Pedro Alonso')
                ))
                """.trimIndent(),
            )
        }
    }

    fun estaVacia(): Boolean = contar("SELECT COUNT(*) FROM socio") == 0

    /**
     * Igual que el `buscaCurso` del ABM de estudiantes: si el `COUNT` es mayor
     * que cero, el alta no se inserta.
     */
    fun contarDni(dni: Long, exceptoId: Int?): Int {
        val sql = if (exceptoId == null) {
            "SELECT COUNT(dni) FROM socio WHERE dni = ?"
        } else {
            "SELECT COUNT(dni) FROM socio WHERE dni = ? AND id <> ?"
        }
        val args = if (exceptoId == null) {
            arrayOf(dni.toString())
        } else {
            arrayOf(dni.toString(), exceptoId.toString())
        }
        return contar(sql, args)
    }

    /** El alias del usuario de la app tiene que ser único, como pide el registro de E4. */
    fun contarAlias(alias: String): Int =
        contar("SELECT COUNT(alias) FROM usuario WHERE alias = ?", arrayOf(norm(alias)))

    /**
     * Listado diario de E4: cuotas vigentes pendientes que vencen entre [desdeIso] y
     * [hastaIso], ya ordenadas. Reportes la usa para "hoy" (ambas cotas iguales) y
     * para "esta semana" (de mañana a 7 días).
     */
    fun idsCuotasPorVencer(desdeIso: String, hastaIso: String): List<Int> {
        val sql = """
            SELECT s.id
            FROM socio s
            INNER JOIN cuota c ON c.socio_id = s.id AND c.vigente = 1
            WHERE c.estado = ? AND c.vence >= ? AND c.vence <= ?
            ORDER BY c.vence, s.nombre
        """.trimIndent()
        val ids = mutableListOf<Int>()
        readableDatabase.rawQuery(sql, arrayOf(EstadoCuota.PENDIENTE.name, desdeIso, hastaIso)).use { c ->
            while (c.moveToNext()) ids += c.getInt(0)
        }
        return ids
    }

    /** Pagos diarios de no socios de un día, en el orden de la lista (más nuevos primero). */
    fun visitantesPorFecha(fechaIso: String): List<Visitante> {
        val lista = mutableListOf<Visitante>()
        readableDatabase.rawQuery("SELECT * FROM visitante WHERE fecha = ? ORDER BY orden", arrayOf(fechaIso)).use { c ->
            while (c.moveToNext()) lista += visitanteDe(c)
        }
        return lista
    }

    /** `COUNT` del DNI del visitante en un día: un pase diario por persona y por fecha. */
    fun contarVisitante(dni: String, fechaIso: String): Int =
        contar("SELECT COUNT(*) FROM visitante WHERE dni = ? AND fecha = ?", arrayOf(dni, fechaIso))

    /** Login: la combinación alias + clave sale de la tabla `usuario`. */
    fun autenticar(alias: String, clave: String): Rol? {
        readableDatabase.rawQuery(
            "SELECT rol FROM usuario WHERE alias = ? AND clave = ?",
            arrayOf(norm(alias), clave),
        ).use { c ->
            if (!c.moveToFirst()) return null
            return runCatching { Rol.valueOf(c.getString(0)) }.getOrNull()
        }
    }

    /**
     * Las cuotas ya pagadas que no tienen mes siguiente (el pago de Andrés anterior
     * a esta versión, y la semilla) ganan `proxima_vence` y una fila pendiente.
     */
    fun completarCuotasSiguientes() {
        data class Pagada(val socioId: Int, val monto: Int, val vence: String)
        val pagadas = mutableListOf<Pagada>()
        readableDatabase.rawQuery(
            """
            SELECT socio_id, monto, vence
            FROM cuota
            WHERE vigente = 1 AND estado = ? AND proxima_vence IS NULL
            """.trimIndent(),
            arrayOf(EstadoCuota.PAGADA.name),
        ).use { c ->
            while (c.moveToNext()) pagadas += Pagada(c.getInt(0), c.getInt(1), c.getString(2))
        }
        if (pagadas.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            pagadas.forEach { p ->
                val proxima = fecha(p.vence).masMeses(1)
                db.update(
                    "cuota",
                    ContentValues().apply { put("proxima_vence", proxima.fmtIso()) },
                    "socio_id = ? AND vigente = 1",
                    arrayOf(p.socioId.toString()),
                )
                val yaExiste = contar(
                    "SELECT COUNT(*) FROM cuota WHERE socio_id = ? AND vigente = 0 AND vence = ?",
                    arrayOf(p.socioId.toString(), proxima.fmtIso()),
                )
                if (yaExiste == 0) {
                    db.insert("cuota", null, ContentValues().apply {
                        put("socio_id", p.socioId)
                        put("mes", proxima.tituloMes())
                        put("monto", p.monto)
                        put("vence", proxima.fmtIso())
                        put("estado", EstadoCuota.PENDIENTE.name)
                        put("vigente", 0)
                    })
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun guardar(datos: DatosClub) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            listOf(
                "pago", "cuota", "carnet", "actividad_inscripto", "actividad",
                "visitante", "ingreso_hoy", "asistencia", "asistencia_clase",
                "usuario", "socio", "plan", "sesion", "parametro",
            ).forEach { db.delete(it, null, null) }

            DemoData.planes.forEach { p ->
                db.insert("plan", null, ContentValues().apply {
                    put("nombre", p.nombre)
                    put("precio", p.precio)
                })
            }

            datos.socios.forEach { s -> insertarSocio(db, s) }

            DemoData.cuentas.forEach { (alias, credencial) ->
                val (clave, rol) = credencial
                val socioId = if (rol == Rol.SOCIO && datos.socios.any { it.id == ID_SOCIO_APP }) {
                    ID_SOCIO_APP
                } else {
                    null
                }
                db.insert("usuario", null, ContentValues().apply {
                    put("alias", alias)
                    put("clave", clave)
                    put("rol", rol.name)
                    if (socioId == null) putNull("socio_id") else put("socio_id", socioId)
                })
            }

            datos.acts.forEachIndexed { i, act ->
                db.insert("actividad", null, ContentValues().apply {
                    put("id", act.id)
                    put("nombre", act.nombre)
                    put("horario", act.horario)
                    put("cupo", act.cupo)
                    put("ocupados", act.ocupados)
                    put("precio", act.precio)
                    put("profesor", act.profesor)
                    put("orden", i)
                })
                act.inscriptos.forEachIndexed { j, socioId ->
                    db.insert("actividad_inscripto", null, ContentValues().apply {
                        put("actividad_id", act.id)
                        put("socio_id", socioId)
                        put("orden", j)
                    })
                }
            }

            datos.visitantes.forEachIndexed { i, v ->
                db.insert("visitante", null, ContentValues().apply {
                    put("nombre", v.nombre)
                    put("dni", v.dni)
                    put("actividad", v.actividad)
                    put("hora", v.hora)
                    put("monto", v.monto)
                    put("fecha", v.fecha.fmtIso())
                    put("medio", v.medio)
                    put("orden", i)
                })
            }

            datos.ingresosHoy.forEachIndexed { i, ing ->
                db.insert("ingreso_hoy", null, ContentValues().apply {
                    put("hora", ing.hora)
                    put("nombre", ing.nombre)
                    put("estado", ing.estado)
                    put("ok", if (ing.ok) 1 else 0)
                    put("orden", i)
                })
            }

            datos.parametros.forEachIndexed { i, p ->
                db.insert("parametro", null, ContentValues().apply {
                    put("gracia_dias", p.graciaDias)
                    put("recargo", p.recargo)
                    put("interes_mensual", p.interesMensual)
                    put("vigente_desde", p.vigenteDesde.fmtIso())
                    put("orden", i)
                })
            }

            datos.asistencia.entries.forEachIndexed { i, (claseId, ids) ->
                db.insert("asistencia_clase", null, ContentValues().apply {
                    put("clase_id", claseId)
                    put("orden", i)
                })
                ids.forEachIndexed { j, socioId ->
                    db.insert("asistencia", null, ContentValues().apply {
                        put("clase_id", claseId)
                        put("socio_id", socioId)
                        put("orden", j)
                    })
                }
            }

            db.insert("sesion", null, ContentValues().apply {
                put("id", 1)
                if (datos.rol == null) putNull("rol") else put("rol", datos.rol.name)
                put("profe_presente", if (datos.profePresente) 1 else 0)
            })

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun leer(): DatosClub {
        val db = readableDatabase
        val carnets = mutableMapOf<Int, Pair<String, String>>()
        db.rawQuery("SELECT socio_id, emitido, vence FROM carnet", null).use { c ->
            while (c.moveToNext()) carnets[c.getInt(0)] = c.getString(1) to c.getString(2)
        }
        val cuotas = mutableMapOf<Int, Cuota>()
        db.rawQuery(
            "SELECT socio_id, mes, monto, vence, estado, proxima_vence FROM cuota WHERE vigente = 1",
            null,
        ).use { c ->
            while (c.moveToNext()) {
                val proxima = if (c.isNull(5)) null else fecha(c.getString(5))
                cuotas[c.getInt(0)] = Cuota(
                    mes = c.getString(1),
                    monto = c.getInt(2),
                    vence = fecha(c.getString(3)),
                    estado = EstadoCuota.valueOf(c.getString(4)),
                    proximaVence = proxima,
                )
            }
        }
        val pagos = mutableMapOf<Int, MutableList<PagoHistorial>>()
        db.rawQuery(
            "SELECT socio_id, mes, monto, pagada, medio, comprobante FROM pago ORDER BY socio_id, orden",
            null,
        ).use { c ->
            while (c.moveToNext()) {
                val id = c.getInt(0)
                pagos.getOrPut(id) { mutableListOf() } += PagoHistorial(
                    mes = c.getString(1),
                    monto = c.getInt(2),
                    pagada = fecha(c.getString(3)),
                    medio = c.getString(4),
                    comprobante = if (c.isNull(5)) "" else c.getString(5),
                )
            }
        }
        val socios = mutableListOf<Socio>()
        db.rawQuery("SELECT * FROM socio ORDER BY id", null).use { c ->
            while (c.moveToNext()) socios += leerSocio(c, carnets, cuotas, pagos)
        }

        val inscriptosPorAct = mutableMapOf<String, MutableList<Int>>()
        db.rawQuery(
            "SELECT actividad_id, socio_id FROM actividad_inscripto ORDER BY actividad_id, orden",
            null,
        ).use { c ->
            while (c.moveToNext()) {
                inscriptosPorAct.getOrPut(c.getString(0)) { mutableListOf() } += c.getInt(1)
            }
        }
        val acts = mutableListOf<Actividad>()
        db.rawQuery("SELECT * FROM actividad ORDER BY orden", null).use { c ->
            while (c.moveToNext()) {
                val id = c.getString(c.getColumnIndexOrThrow("id"))
                acts += Actividad(
                    id = id,
                    nombre = c.getString(c.getColumnIndexOrThrow("nombre")),
                    horario = c.getString(c.getColumnIndexOrThrow("horario")),
                    cupo = c.getInt(c.getColumnIndexOrThrow("cupo")),
                    ocupados = c.getInt(c.getColumnIndexOrThrow("ocupados")),
                    precio = c.getInt(c.getColumnIndexOrThrow("precio")),
                    profesor = c.getString(c.getColumnIndexOrThrow("profesor")),
                    inscriptos = inscriptosPorAct[id] ?: emptyList(),
                )
            }
        }

        val visitantes = mutableListOf<Visitante>()
        db.rawQuery("SELECT * FROM visitante ORDER BY orden", null).use { c ->
            while (c.moveToNext()) visitantes += visitanteDe(c)
        }

        val ingresosHoy = mutableListOf<IngresoHoy>()
        db.rawQuery("SELECT * FROM ingreso_hoy ORDER BY orden", null).use { c ->
            while (c.moveToNext()) {
                ingresosHoy += IngresoHoy(
                    hora = c.getString(c.getColumnIndexOrThrow("hora")),
                    nombre = c.getString(c.getColumnIndexOrThrow("nombre")),
                    estado = c.getString(c.getColumnIndexOrThrow("estado")),
                    ok = c.getInt(c.getColumnIndexOrThrow("ok")) == 1,
                )
            }
        }

        val presentesPorClase = mutableMapOf<String, MutableList<Int>>()
        db.rawQuery(
            "SELECT clase_id, socio_id FROM asistencia ORDER BY clase_id, orden",
            null,
        ).use { c ->
            while (c.moveToNext()) {
                presentesPorClase.getOrPut(c.getString(0)) { mutableListOf() } += c.getInt(1)
            }
        }
        val asistencia = linkedMapOf<String, List<Int>>()
        db.rawQuery("SELECT clase_id FROM asistencia_clase ORDER BY orden", null).use { c ->
            while (c.moveToNext()) {
                val claseId = c.getString(0)
                asistencia[claseId] = presentesPorClase[claseId] ?: emptyList()
            }
        }

        var rol: Rol? = null
        var profePresente = false
        db.rawQuery("SELECT rol, profe_presente FROM sesion WHERE id = 1", null).use { c ->
            if (c.moveToFirst()) {
                rol = if (c.isNull(0)) null else runCatching { Rol.valueOf(c.getString(0)) }.getOrNull()
                profePresente = c.getInt(1) == 1
            }
        }

        val parametros = mutableListOf<Parametros>()
        db.rawQuery("SELECT * FROM parametro ORDER BY orden", null).use { c ->
            while (c.moveToNext()) {
                parametros += Parametros(
                    graciaDias = c.getInt(c.getColumnIndexOrThrow("gracia_dias")),
                    recargo = c.getDouble(c.getColumnIndexOrThrow("recargo")),
                    interesMensual = c.getDouble(c.getColumnIndexOrThrow("interes_mensual")),
                    vigenteDesde = fecha(c.getString(c.getColumnIndexOrThrow("vigente_desde"))),
                )
            }
        }

        return DatosClub(
            rol = rol,
            socios = socios,
            acts = acts,
            visitantes = visitantes,
            ingresosHoy = ingresosHoy,
            asistencia = asistencia,
            profePresente = profePresente,
            parametros = parametros.ifEmpty { listOf(Parametros.INICIALES) },
        )
    }

    private fun insertarSocio(db: SQLiteDatabase, s: Socio) {
        db.insert("socio", null, ContentValues().apply {
            put("id", s.id)
            put("nombre", s.nombre)
            put("dni", s.dni)
            put("nro", s.nro)
            put("email", s.email)
            put("plan", s.plan)
            put("alta", s.alta.fmtIso())
            put("nac", s.nac.fmtIso())
            put("ficha", if (s.ficha) 1 else 0)
            if (s.foto == null) putNull("foto") else put("foto", s.foto)
            put("nut_peso", s.nut.peso)
            put("nut_altura", s.nut.altura)
            put("nut_objetivo", s.nut.objetivo)
            put("nut_antecedentes", s.nut.antecedentes)
            if (s.turno == null) {
                putNull("turno_fecha")
                putNull("turno_hora")
            } else {
                put("turno_fecha", s.turno.fecha.fmtIso())
                put("turno_hora", s.turno.hora)
            }
            if (s.rutina == null) putNull("rutina_json") else put("rutina_json", rutinaAJson(s.rutina))
            put("consultas_json", consultasAJson(s.nut.consultas))
            put("actividades_json", actividadesAJson(s.actividades))
            put("ingresos_json", ingresosAJson(s.ingresos))
        })
        db.insert("carnet", null, ContentValues().apply {
            put("socio_id", s.id)
            put("emitido", s.carnetEmitido.fmtIso())
            put("vence", s.carnetVence.fmtIso())
        })
        db.insert("cuota", null, ContentValues().apply {
            put("socio_id", s.id)
            put("mes", s.cuota.mes)
            put("monto", s.cuota.monto)
            put("vence", s.cuota.vence.fmtIso())
            put("estado", s.cuota.estado.name)
            put("vigente", 1)
            if (s.cuota.proximaVence == null) putNull("proxima_vence") else put("proxima_vence", s.cuota.proximaVence.fmtIso())
        })
        s.cuota.proximaVence?.let { proxima ->
            db.insert("cuota", null, ContentValues().apply {
                put("socio_id", s.id)
                put("mes", proxima.tituloMes())
                put("monto", s.cuota.monto)
                put("vence", proxima.fmtIso())
                put("estado", EstadoCuota.PENDIENTE.name)
                put("vigente", 0)
            })
        }
        s.historial.forEachIndexed { i, p ->
            db.insert("pago", null, ContentValues().apply {
                put("socio_id", s.id)
                put("mes", p.mes)
                put("monto", p.monto)
                put("pagada", p.pagada.fmtIso())
                put("medio", p.medio)
                put("orden", i)
                put("comprobante", p.comprobante)
            })
        }
    }

    private fun leerSocio(
        c: android.database.Cursor,
        carnets: Map<Int, Pair<String, String>>,
        cuotas: Map<Int, Cuota>,
        pagos: Map<Int, List<PagoHistorial>>,
    ): Socio {
        val id = c.getInt(c.getColumnIndexOrThrow("id"))
        val alta = fecha(c.getString(c.getColumnIndexOrThrow("alta")))
        val carnet = carnets[id]
        val emitido = carnet?.let { fecha(it.first) } ?: alta
        val venceCarnet = carnet?.let { fecha(it.second) } ?: alta
        val turnoFecha = c.getColumnIndexOrThrow("turno_fecha")
        val turno = if (c.isNull(turnoFecha)) {
            null
        } else {
            Turno(fecha(c.getString(turnoFecha)), c.getString(c.getColumnIndexOrThrow("turno_hora")))
        }
        val fotoIdx = c.getColumnIndexOrThrow("foto")
        return Socio(
            id = id,
            foto = if (c.isNull(fotoIdx)) null else c.getInt(fotoIdx),
            nombre = c.getString(c.getColumnIndexOrThrow("nombre")),
            dni = c.getLong(c.getColumnIndexOrThrow("dni")),
            nro = c.getString(c.getColumnIndexOrThrow("nro")),
            plan = c.getString(c.getColumnIndexOrThrow("plan")),
            alta = fecha(c.getString(c.getColumnIndexOrThrow("alta"))),
            nac = fecha(c.getString(c.getColumnIndexOrThrow("nac"))),
            carnetEmitido = emitido,
            carnetVence = venceCarnet,
            ficha = c.getInt(c.getColumnIndexOrThrow("ficha")) == 1,
            actividades = actividadesDeJson(c.getString(c.getColumnIndexOrThrow("actividades_json"))),
            cuota = cuotas[id] ?: Cuota("", 0, alta, EstadoCuota.PENDIENTE),
            historial = pagos[id] ?: emptyList(),
            rutina = c.getString(c.getColumnIndexOrThrow("rutina_json"))?.let { rutinaDeJson(it) },
            nut = FichaNutricional(
                peso = c.getDouble(c.getColumnIndexOrThrow("nut_peso")),
                altura = c.getDouble(c.getColumnIndexOrThrow("nut_altura")),
                objetivo = c.getString(c.getColumnIndexOrThrow("nut_objetivo")),
                antecedentes = c.getString(c.getColumnIndexOrThrow("nut_antecedentes")),
                consultas = consultasDeJson(c.getString(c.getColumnIndexOrThrow("consultas_json"))),
            ),
            turno = turno,
            ingresos = ingresosDeJson(c.getString(c.getColumnIndexOrThrow("ingresos_json"))),
            email = c.getString(c.getColumnIndexOrThrow("email")),
        )
    }

    private fun visitanteDe(c: Cursor) = Visitante(
        nombre = c.getString(c.getColumnIndexOrThrow("nombre")),
        dni = c.getString(c.getColumnIndexOrThrow("dni")),
        actividad = c.getString(c.getColumnIndexOrThrow("actividad")),
        hora = c.getString(c.getColumnIndexOrThrow("hora")),
        monto = c.getInt(c.getColumnIndexOrThrow("monto")),
        fecha = fecha(c.getString(c.getColumnIndexOrThrow("fecha"))),
        medio = c.getString(c.getColumnIndexOrThrow("medio")),
    )

    private fun contar(sql: String, args: Array<String>? = null): Int =
        readableDatabase.rawQuery(sql, args).use { c ->
            c.moveToFirst()
            c.getInt(0)
        }

    private companion object {
        const val NOMBRE = "club_deportivo.db"
        const val VERSION = 4

        val CREATE_PARAMETRO = """
            CREATE TABLE IF NOT EXISTS parametro (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                gracia_dias INTEGER NOT NULL,
                recargo REAL NOT NULL,
                interes_mensual REAL NOT NULL,
                vigente_desde TEXT NOT NULL,
                orden INTEGER NOT NULL
            )
        """.trimIndent()
    }
}

private fun rutinaAJson(r: Rutina): String {
    val dias = JSONArray()
    r.dias.forEach { d ->
        val ejercicios = JSONArray()
        d.ejercicios.forEach { e ->
            ejercicios.put(
                JSONObject()
                    .put("nombre", e.nombre)
                    .put("series", e.series)
                    .put("reps", e.reps)
                    .put("nota", e.nota),
            )
        }
        dias.put(JSONObject().put("tag", d.tag).put("foco", d.foco).put("ejercicios", ejercicios))
    }
    return JSONObject()
        .put("nombre", r.nombre)
        .put("profesor", r.profesor)
        .put("actualizada", r.actualizada.fmtIso())
        .put("dias", dias)
        .toString()
}

private fun rutinaDeJson(raw: String): Rutina {
    val o = JSONObject(raw)
    val diasJson = o.getJSONArray("dias")
    val dias = List(diasJson.length()) { i ->
        val d = diasJson.getJSONObject(i)
        val ejerciciosJson = d.getJSONArray("ejercicios")
        DiaRutina(
            tag = d.getString("tag"),
            foco = d.getString("foco"),
            ejercicios = List(ejerciciosJson.length()) { j ->
                val e = ejerciciosJson.getJSONObject(j)
                Ejercicio(
                    nombre = e.getString("nombre"),
                    series = e.getInt("series"),
                    reps = e.getInt("reps"),
                    nota = e.optString("nota"),
                )
            },
        )
    }
    return Rutina(
        nombre = o.getString("nombre"),
        profesor = o.getString("profesor"),
        actualizada = fecha(o.getString("actualizada")),
        dias = dias,
    )
}

private fun consultasAJson(consultas: List<Consulta>): String {
    val arr = JSONArray()
    consultas.forEach { c ->
        arr.put(JSONObject().put("fecha", c.fecha.fmtIso()).put("peso", c.peso).put("obs", c.obs))
    }
    return arr.toString()
}

private fun consultasDeJson(raw: String?): List<Consulta> {
    if (raw.isNullOrBlank()) return emptyList()
    val arr = JSONArray(raw)
    return List(arr.length()) { i ->
        val o = arr.getJSONObject(i)
        Consulta(fecha = fecha(o.getString("fecha")), peso = o.getDouble("peso"), obs = o.getString("obs"))
    }
}

private fun actividadesAJson(acts: List<ActividadSocio>): String {
    val arr = JSONArray()
    acts.forEach { a -> arr.put(JSONObject().put("nombre", a.nombre).put("horario", a.horario)) }
    return arr.toString()
}

private fun actividadesDeJson(raw: String?): List<ActividadSocio> {
    if (raw.isNullOrBlank()) return emptyList()
    val arr = JSONArray(raw)
    return List(arr.length()) { i ->
        val o = arr.getJSONObject(i)
        ActividadSocio(o.getString("nombre"), o.getString("horario"))
    }
}

private fun ingresosAJson(ingresos: List<Ingreso>): String {
    val arr = JSONArray()
    ingresos.forEach { ing -> arr.put(JSONObject().put("fecha", ing.fecha).put("estado", ing.estado)) }
    return arr.toString()
}

private fun ingresosDeJson(raw: String?): List<Ingreso> {
    if (raw.isNullOrBlank()) return emptyList()
    val arr = JSONArray(raw)
    return List(arr.length()) { i ->
        val o = arr.getJSONObject(i)
        Ingreso(o.getString("fecha"), o.getString("estado"))
    }
}
