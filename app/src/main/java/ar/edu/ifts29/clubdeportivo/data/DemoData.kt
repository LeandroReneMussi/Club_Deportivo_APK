package ar.edu.ifts29.clubdeportivo.data

import ar.edu.ifts29.clubdeportivo.R

/**
 * Datos de demostración del prototipo. Es un dataset armado para que cada historia de
 * usuario tenga un caso que la ejercite: Lucas está en mora, Sofía tiene el carnet
 * vencido, Natación está sin cupo y Valentina no presentó el apto físico.
 *
 * Prototipo académico: todo vive en memoria y se reinicia al cerrar la app.
 */
object DemoData {

    /** Usuario → (contraseña, rol). */
    val cuentas: Map<String, Pair<String, Rol>> = mapOf(
        "andres" to ("Socio123" to Rol.SOCIO),
        "jorge" to ("Admin123" to Rol.ADMIN),
        "ana" to ("Prof123" to Rol.PROFESOR),
        "leandro" to ("Nut123" to Rol.NUTRICIONISTA),
    )

    val usuarios: Map<Rol, Usuario> = mapOf(
        Rol.ADMIN to Usuario(
            rol = Rol.ADMIN,
            avatar = R.drawable.avatar_jorge,
            nombre = "Jorge Lesme",
            nombreCorto = "Jorge",
            rolLabel = "Administrativo",
            datos = listOf("Usuario" to "Jorge", "Rol" to "Administrativo", "Alta" to "01/02/2019"),
            permisos = listOf("Socios", "Visitantes", "Cuotas", "Carnets", "Acceso", "Reportes", "Supervisión"),
        ),
        Rol.EMPLEADO to Usuario(
            rol = Rol.EMPLEADO,
            avatar = null,
            nombre = "Nicolás Vera",
            nombreCorto = "Nico",
            rolLabel = "Empleado",
            datos = listOf("Usuario" to "Nico", "Rol" to "Empleado · Recepción", "Turno" to "Tarde 14–22 h"),
            permisos = listOf("Socios", "Visitantes", "Cobros", "Carnets", "Acceso", "Reportes"),
        ),
        Rol.PROFESOR to Usuario(
            rol = Rol.PROFESOR,
            avatar = R.drawable.avatar_ana,
            nombre = "Ana Alvarez",
            nombreCorto = "Ana",
            rolLabel = "Entrenadora",
            datos = listOf(
                "Usuario" to "Ana",
                "Rol" to "Entrenadora · Musculación",
                "Actividades" to "Musculación, Funcional",
            ),
            permisos = listOf("Asistencia", "Rutinas"),
        ),
        Rol.NUTRICIONISTA to Usuario(
            rol = Rol.NUTRICIONISTA,
            avatar = R.drawable.avatar_leandro,
            nombre = "Leandro Mussi",
            nombreCorto = "Leandro",
            rolLabel = "Nutricionista",
            datos = listOf("Usuario" to "Leandro", "Rol" to "Nutricionista · MN 4821", "Consultorio" to "2"),
            permisos = listOf("Fichas nutricionales", "Turnos"),
        ),
        Rol.SOCIO to Usuario(
            rol = Rol.SOCIO,
            avatar = R.drawable.avatar_andres,
            nombre = "Andrés Weitzel",
            nombreCorto = "Andrés",
            rolLabel = "Socio",
            datos = listOf(
                "Usuario" to "Andres",
                "DNI" to "38.412.907",
                "N° socio" to "01245",
                "Plan" to "Full",
            ),
            permisos = listOf("Mi cuota", "Pagos", "Carnet digital", "Mi rutina", "Turnos nutrición"),
        ),
    )

    val planes = listOf(
        Plan("Básico", 12_000),
        Plan("Full", 18_500),
        Plan("Familiar", 29_000),
    )

    /** El nutricionista del club, referenciado desde las pantallas del socio. */
    const val NUTRICIONISTA = "Lic. Leandro Mussi"
    const val PROFESORA = "Ana Alvarez"

    private val rutinaHipertrofia = Rutina(
        nombre = "Hipertrofia 3 días",
        profesor = PROFESORA,
        actualizada = fecha("2026-09-01"),
        dias = listOf(
            DiaRutina(
                "D1", "Tren inferior",
                listOf(
                    Ejercicio("Sentadilla", 4, 8, "60 kg · 90\" pausa"),
                    Ejercicio("Prensa 45°", 4, 12, "100 kg"),
                    Ejercicio("Peso muerto rumano", 3, 10, "40 kg"),
                    Ejercicio("Estocadas", 3, 12, "por pierna"),
                    Ejercicio("Elevación de talones", 4, 15, "lento en bajada"),
                ),
            ),
            DiaRutina(
                "D2", "Tren superior",
                listOf(
                    Ejercicio("Press banca", 4, 10, "40 kg · 90\" pausa"),
                    Ejercicio("Remo con barra", 4, 10, "35 kg"),
                    Ejercicio("Press militar", 3, 12, "20 kg"),
                    Ejercicio("Dominadas asistidas", 3, 8, "banda verde"),
                    Ejercicio("Curl bíceps", 3, 12, "8 kg c/u"),
                    Ejercicio("Fondos en banco", 3, 12, "tempo 2-1-2"),
                ),
            ),
            DiaRutina(
                "D3", "Full body",
                listOf(
                    Ejercicio("Peso muerto", 4, 6, "70 kg"),
                    Ejercicio("Press inclinado", 3, 10, "30 kg"),
                    Ejercicio("Remo en polea", 3, 12, "35 kg"),
                    Ejercicio("Hip thrust", 3, 12, "60 kg"),
                    Ejercicio("Plancha", 3, 45, "segundos"),
                ),
            ),
        ),
    )

    val socios: List<Socio> = listOf(
        Socio(
            id = 1,
            foto = R.drawable.avatar_andres,
            nombre = "Andrés Weitzel",
            dni = 38_412_907,
            nro = "01245",
            plan = "Full",
            alta = fecha("2024-03-12"),
            nac = fecha("1994-06-21"),
            carnetEmitido = fecha("2026-04-01"),
            carnetVence = fecha("2027-03-31"),
            ficha = true,
            actividades = listOf(
                ActividadSocio("Musculación", "Lun · Mié · Vie"),
                ActividadSocio("Natación", "Mar · Jue 19:00"),
            ),
            cuota = Cuota("Septiembre 2026", 18_500, fecha("2026-09-10"), EstadoCuota.PENDIENTE),
            historial = listOf(
                PagoHistorial("Agosto 2026", 18_500, fecha("2026-08-05"), "Tarjeta"),
                PagoHistorial("Julio 2026", 17_000, fecha("2026-07-08"), "Caja"),
                PagoHistorial("Junio 2026", 17_000, fecha("2026-06-03"), "Tarjeta"),
            ),
            rutina = rutinaHipertrofia,
            nut = FichaNutricional(
                peso = 62.4, altura = 1.68,
                objetivo = "Recomposición corporal, +masa muscular",
                antecedentes = "Sin antecedentes relevantes",
                consultas = listOf(
                    Consulta(fecha("2026-08-14"), 62.4, "Buena adherencia. Sube proteína a 1,8 g/kg. Colación post-entreno."),
                    Consulta(fecha("2026-07-10"), 63.8, "Ajuste de porciones en cena. Hidratación insuficiente."),
                    Consulta(fecha("2026-06-05"), 64.5, "Primera consulta. Plan base 4 comidas."),
                ),
            ),
            turno = Turno(fecha("2026-09-11"), "17:30"),
            ingresos = listOf(
                Ingreso("Lun 7 sep · 18:52", "Permitido"),
                Ingreso("Vie 4 sep · 19:10", "Permitido"),
                Ingreso("Mié 2 sep · 18:40", "Permitido"),
            ),
        ),
        Socio(
            id = 2,
            nombre = "Lucas Benítez",
            dni = 40_120_334,
            nro = "01302",
            plan = "Básico",
            alta = fecha("2025-01-20"),
            nac = fecha("1997-02-11"),
            carnetEmitido = fecha("2026-01-20"),
            carnetVence = fecha("2027-01-19"),
            ficha = true,
            actividades = listOf(ActividadSocio("Musculación", "Lun · Mié · Vie")),
            cuota = Cuota("Agosto 2026", 12_000, fecha("2026-08-10"), EstadoCuota.VENCIDA),
            historial = listOf(PagoHistorial("Julio 2026", 11_000, fecha("2026-07-15"), "Caja")),
            rutina = null,
            nut = FichaNutricional(
                peso = 81.0, altura = 1.79,
                objetivo = "Descenso de grasa",
                antecedentes = "Hipertensión leve controlada",
                consultas = listOf(Consulta(fecha("2026-05-20"), 81.0, "Primera consulta. Reducir sodio.")),
            ),
            turno = null,
            ingresos = emptyList(),
        ),
        Socio(
            id = 3,
            nombre = "Sofía Acosta",
            dni = 35_887_210,
            nro = "00877",
            plan = "Full",
            alta = fecha("2021-09-01"),
            nac = fecha("1991-11-03"),
            carnetEmitido = fecha("2025-08-31"),
            carnetVence = fecha("2026-08-31"),
            ficha = true,
            actividades = listOf(
                ActividadSocio("Yoga", "Mar · Jue 08:30"),
                ActividadSocio("Funcional", "Sáb 10:00"),
            ),
            cuota = Cuota("Septiembre 2026", 18_500, fecha("2026-09-10"), EstadoCuota.PAGADA),
            historial = listOf(
                PagoHistorial("Septiembre 2026", 18_500, fecha("2026-09-02"), "App"),
                PagoHistorial("Agosto 2026", 18_500, fecha("2026-08-04"), "App"),
            ),
            rutina = Rutina(
                nombre = "Movilidad y fuerza",
                profesor = PROFESORA,
                actualizada = fecha("2026-08-20"),
                dias = listOf(
                    DiaRutina(
                        "D1", "Fuerza general",
                        listOf(
                            Ejercicio("Sentadilla goblet", 3, 12, "12 kg"),
                            Ejercicio("Remo TRX", 3, 12),
                            Ejercicio("Puente glúteo", 3, 15),
                        ),
                    ),
                ),
            ),
            nut = FichaNutricional(
                peso = 58.2, altura = 1.63,
                objetivo = "Mantenimiento",
                antecedentes = "Intolerancia a la lactosa",
                consultas = listOf(Consulta(fecha("2026-08-21"), 58.2, "Estable. Mantener plan.")),
            ),
            turno = Turno(fecha("2026-09-08"), "16:30"),
            ingresos = emptyList(),
        ),
        Socio(
            id = 4,
            nombre = "Julián Paredes",
            dni = 42_501_118,
            nro = "01410",
            plan = "Full",
            alta = fecha("2025-11-05"),
            nac = fecha("2000-08-30"),
            carnetEmitido = fecha("2025-11-05"),
            carnetVence = fecha("2026-11-04"),
            ficha = true,
            actividades = listOf(
                ActividadSocio("Musculación", "Lun · Mié · Vie"),
                ActividadSocio("Funcional", "Mar · Jue 19:30"),
            ),
            cuota = Cuota("Septiembre 2026", 18_500, fecha("2026-09-10"), EstadoCuota.PAGADA),
            historial = listOf(PagoHistorial("Septiembre 2026", 18_500, fecha("2026-09-01"), "App")),
            rutina = Rutina(
                nombre = "Fuerza 4 días",
                profesor = PROFESORA,
                actualizada = fecha("2026-09-03"),
                dias = listOf(
                    DiaRutina(
                        "D1", "Empuje",
                        listOf(
                            Ejercicio("Press banca", 5, 5, "80 kg"),
                            Ejercicio("Press militar", 4, 8, "45 kg"),
                            Ejercicio("Fondos", 3, 10, "lastre 10 kg"),
                        ),
                    ),
                    DiaRutina(
                        "D2", "Tracción",
                        listOf(
                            Ejercicio("Dominadas", 5, 5, "lastre 5 kg"),
                            Ejercicio("Remo pendlay", 4, 8, "70 kg"),
                        ),
                    ),
                ),
            ),
            nut = FichaNutricional(76.0, 1.82, "Ganancia muscular", "Sin antecedentes", emptyList()),
            turno = Turno(fecha("2026-09-08"), "18:00"),
            ingresos = emptyList(),
        ),
        Socio(
            id = 5,
            nombre = "Valentina Ruiz",
            dni = 39_004_567,
            nro = "01188",
            plan = "Básico",
            alta = fecha("2023-06-14"),
            nac = fecha("1995-12-09"),
            carnetEmitido = fecha("2026-06-14"),
            carnetVence = fecha("2027-06-13"),
            ficha = false,
            actividades = listOf(ActividadSocio("Natación", "Mar · Jue 19:00")),
            cuota = Cuota("Septiembre 2026", 12_000, fecha("2026-09-12"), EstadoCuota.PENDIENTE),
            historial = listOf(PagoHistorial("Agosto 2026", 12_000, fecha("2026-08-11"), "Caja")),
            rutina = null,
            nut = FichaNutricional(66.0, 1.70, "Rendimiento en natación", "Asma leve", emptyList()),
            turno = Turno(fecha("2026-09-09"), "17:00"),
            ingresos = emptyList(),
        ),
        Socio(
            id = 6,
            nombre = "Tomás Giménez",
            dni = 41_777_320,
            nro = "01355",
            plan = "Full",
            alta = fecha("2025-05-02"),
            nac = fecha("1999-04-17"),
            carnetEmitido = fecha("2026-05-02"),
            carnetVence = fecha("2027-05-01"),
            ficha = true,
            actividades = listOf(ActividadSocio("Funcional", "Mar · Jue 19:30")),
            cuota = Cuota("Julio 2026", 17_000, fecha("2026-07-10"), EstadoCuota.VENCIDA),
            historial = listOf(PagoHistorial("Junio 2026", 17_000, fecha("2026-06-09"), "Caja")),
            rutina = null,
            nut = FichaNutricional(70.0, 1.75, "Tonificación", "Sin antecedentes", emptyList()),
            turno = null,
            ingresos = emptyList(),
        ),
        Socio(
            id = 7,
            nombre = "Camila Duarte",
            dni = 43_210_987,
            nro = "01466",
            plan = "Full",
            alta = fecha("2026-02-10"),
            nac = fecha("2001-01-25"),
            carnetEmitido = fecha("2026-02-10"),
            carnetVence = fecha("2027-02-09"),
            ficha = true,
            actividades = listOf(ActividadSocio("Musculación", "Lun · Mié · Vie")),
            // Al día: el prototipo lista solo a Andrés y Valentina como próximos a vencer ($30.500).
            cuota = Cuota("Septiembre 2026", 18_500, fecha("2026-09-10"), EstadoCuota.PAGADA),
            historial = listOf(
                PagoHistorial("Septiembre 2026", 18_500, fecha("2026-09-06"), "App"),
                PagoHistorial("Agosto 2026", 18_500, fecha("2026-08-06"), "App"),
            ),
            rutina = Rutina(
                nombre = "Inicial",
                profesor = PROFESORA,
                actualizada = fecha("2026-07-01"),
                dias = listOf(
                    DiaRutina(
                        "D1", "Adaptación",
                        listOf(
                            Ejercicio("Sentadilla", 3, 12, "barra sola"),
                            Ejercicio("Press banca", 3, 12, "20 kg"),
                        ),
                    ),
                ),
            ),
            nut = FichaNutricional(59.0, 1.66, "Salud general", "Sin antecedentes", emptyList()),
            turno = null,
            ingresos = emptyList(),
        ),
    )

    val actividades = listOf(
        Actividad("musc", "Musculación", "18:00", cupo = 30, ocupados = 27, precio = 6_000, profesor = PROFESORA, inscriptos = listOf(1, 2, 4, 7)),
        Actividad("nat", "Natación", "19:00", cupo = 12, ocupados = 12, precio = 8_000, profesor = "Laura Mena", inscriptos = listOf(1, 5)),
        Actividad("func", "Funcional", "19:30", cupo = 20, ocupados = 14, precio = 6_000, profesor = PROFESORA, inscriptos = listOf(3, 4, 6)),
        Actividad("yoga", "Yoga", "08:30", cupo = 15, ocupados = 9, precio = 5_000, profesor = "Inés Robledo", inscriptos = listOf(3)),
    )

    val visitantesIniciales = listOf(
        Visitante("Pedro Alonso", "33.120.455", "Funcional", "18:10", 6_000),
    )

    val ingresosHoyIniciales = listOf(
        IngresoHoy("09:12", "Sofía Acosta", "Denegado", ok = false),
        IngresoHoy("08:47", "Julián Paredes", "Permitido", ok = true),
    )

    /** DNIs sugeridos en la pantalla de control de acceso, para probar los cuatro casos. */
    val dnisDemo = listOf(
        38_412_907L to "Al día",
        40_120_334L to "En mora",
        35_887_210L to "Carnet vencido",
        11_111_111L to "No existe",
    )

    /** Días con turnos libres que ofrece la pantalla "Solicitar turno". */
    val diasDeTurnos = listOf(fecha("2026-09-09"), fecha("2026-09-10"), fecha("2026-09-11"))

    val horasDeTurnos = listOf("16:00", "16:30", "17:00", "17:30", "18:00", "18:30")

    /** Días que muestra la agenda del nutricionista. */
    val diasDeAgenda = listOf(fecha("2026-09-08"), fecha("2026-09-09"), fecha("2026-09-11"))

    /** Tabs de la barra inferior por rol. */
    val tabs: Map<Rol, List<Pair<Screen, String>>> = mapOf(
        Rol.SOCIO to listOf(
            Screen.S_HOME to "Inicio",
            Screen.S_CARNET to "Carnet",
            Screen.S_RUTINA to "Rutina",
            Screen.S_NUTRI to "Nutrición",
            Screen.PERFIL to "Perfil",
        ),
        Rol.EMPLEADO to listOf(
            Screen.E_HOME to "Inicio",
            Screen.E_SOCIOS to "Socios",
            Screen.E_ACCESO to "Acceso",
            Screen.E_VISITANTES to "Visitantes",
            Screen.E_REPORTES to "Reportes",
        ),
        Rol.PROFESOR to listOf(
            Screen.P_HOME to "Inicio",
            Screen.P_ASISTENCIA to "Asistencia",
            Screen.P_RUTINAS to "Rutinas",
            Screen.PERFIL to "Perfil",
        ),
        Rol.NUTRICIONISTA to listOf(
            Screen.N_AGENDA to "Agenda",
            Screen.N_FICHAS to "Fichas",
            Screen.PERFIL to "Perfil",
        ),
    ).let { it + (Rol.ADMIN to it.getValue(Rol.EMPLEADO)) }

    /** Títulos del header de las sub-pantallas. */
    val titulos: Map<Screen, String> = mapOf(
        Screen.S_CUOTAS to "Estado de cuenta",
        Screen.S_PAGAR to "Pagar cuota",
        Screen.S_TURNO to "Solicitar turno",
        Screen.PERFIL to "Perfil",
        Screen.E_SOCIO to "Ficha del socio",
        Screen.E_ALTA to "Alta de socio",
        Screen.E_EDITAR to "Editar socio",
        Screen.E_VISITANTE_NUEVO to "Ingreso de visitante",
        Screen.P_RUTINA_EDIT to "Rutina del socio",
        Screen.N_FICHA to "Ficha nutricional",
    )

    /** Supervisión del administrativo: asistencia de profesores del día. */
    val profesoresDelDia = listOf(
        Triple("Inés Robledo", "Yoga 08:30", "Presente 08:21"),
        Triple("Laura Mena", "Natación 19:00", "Sin marcar"),
    )
}
