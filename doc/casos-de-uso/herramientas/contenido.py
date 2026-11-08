# -*- coding: utf-8 -*-
"""Contenido del documento de casos de uso (fuente única para el .md y el .docx)."""

TITULO = "Casos de uso y diagramas UML 2.0 — Club Deportivo v1.5.0"
GRUPO = "Grupo 3"
INTEGRANTES = ["Alvarez", "Aquino Lesme", "Mussi", "Weitzel"]
FECHA = "2026-11-08"
VERSION_APP = "1.5.0"

INTRO = [
    "Este documento especifica los casos de uso de la app Android «Club Deportivo» (Kotlin + Jetpack Compose + SQLite) y los "
    "acompaña con diagramas UML 2.0: casos de uso, clases del dominio, secuencia, máquina de estados y actividad.",
    "La fuente de verdad del comportamiento actual (v1.4.0, rama main) es el código: ClubViewModel.kt (todas las acciones), "
    "SocioVista.kt (reglas derivadas), ClubRepositorio.kt y ClubDbHelper.kt (persistencia SQLite), Models.kt, Validaciones.kt, "
    "las pantallas de ui/screens y la tabla de historias de usuario del README. Las reglas nuevas de la v1.5.0 (gracia configurable, "
    "recargo e interés sobre la deuda, contador de días, parámetros con vigencia, apellido separado y fecha real del dispositivo) "
    "salen del spec doc/superpowers/specs/2026-11-08-v1.5.0-gracia-recargos-design.md y se marcan con «(v1.5)». Los escenarios de "
    "las pruebas unitarias (ClubViewModelTest, CrudSociosTest, ValidacionesTest) se usaron para los flujos alternativos.",
    "Cada caso de uso corresponde a una función existente en ClubViewModel o a una regla del spec v1.5. Lo que no quedó claro se anota "
    "en la lista final «A confirmar» en lugar de suponerse.",
]

ACTORES = [
    ("Socio", "Persona asociada al club con cuenta en la app (cuenta de demo «Andres»). Paga su cuota, consulta su estado de cuenta, "
              "carnet digital, rutina e información nutricional y gestiona su turno con la nutricionista."),
    ("Administrativo", "Personal de recepción/caja (cuenta «Jorge», rol ADMIN; el enum también prevé EMPLEADO). Administra el padrón "
                       "(alta, edición, baja, consulta), cobra cuotas en caja, emite carnets, valida el ingreso por DNI, registra los pagos "
                       "diarios de no socios, consulta reportes y (v1.5) configura los parámetros del club."),
    ("Entrenadora", "Profesora de las actividades (cuenta «Ana», rol PROFESOR). Marca su presencia, toma asistencia por clase y crea y "
                    "asigna rutinas a los socios habilitados con apto físico."),
    ("Nutricionista", "Profesional de nutrición (cuenta «Leandro», rol NUTRICIONISTA). Consulta la agenda de turnos y registra consultas "
                      "en la ficha nutricional del socio."),
    ("No socio / Visitante", "Actor externo que no opera la app: paga un pase diario en el mostrador y el Administrativo lo registra (CU-11). "
                             "Se modela como beneficiario, no como usuario del sistema."),
]

# --------------------------------------------------------------------------- casos de uso
# Campos: id, nombre, actor, hu, pantalla, pre, flujo, alt, post, v15 (texto opcional)
CASOS = [
    dict(
        id="CU-01", nombre="Iniciar sesión", actor="Usuario del club (Socio, Administrativo, Entrenadora, Nutricionista)",
        hu="HU-01", pantalla="PantallaLogin (LoginActivity) → Activity del rol",
        pre="La app está abierta sin sesión. La cuenta existe en la tabla usuario (semilla: Andres/Socio123, Jorge/Admin123, Ana/Prof123, Leandro/Nut123).",
        flujo=[
            "El usuario escribe alias y contraseña (o toca una de las cuentas de prueba listadas, que completa el formulario).",
            "Toca «Ingresar» (login()).",
            "El sistema normaliza el alias y lo verifica contra la tabla usuario (ClubRepositorio.autenticar → ClubDbHelper.autenticar).",
            "El sistema deduce el rol de la cuenta; no hay selector de perfil.",
            "Abre la Activity del rol en su pantalla raíz (Socio, Administrativo y Entrenadora → Inicio; Nutricionista → Agenda), limpia los campos y guarda la sesión.",
        ],
        alt=[
            "2a. Alias o contraseña vacíos → mensaje «Completá usuario y contraseña.»; permanece en el login.",
            "3a. Credenciales inválidas → «Usuario o contraseña incorrectos.»; loginError = true.",
            "3b. La cuenta es de un socio dado de baja (CU-06) → el ingreso se rechaza con el mismo mensaje.",
        ],
        post="Sesión iniciada con el rol de la cuenta (tabla sesion); se muestra la barra inferior del rol.",
    ),
    dict(
        id="CU-02", nombre="Cerrar sesión", actor="Usuario del club", hu="HU-01", pantalla="PantallaPerfil (hoja de confirmación)",
        pre="Sesión iniciada.",
        flujo=[
            "El usuario abre Perfil (pestaña para Socio, Entrenadora y Nutricionista; desde el avatar para el Administrativo) y toca «Cerrar sesión» (pedirCerrarSesion()).",
            "El sistema muestra la hoja de confirmación.",
            "El usuario confirma (cerrarSesion()).",
            "El sistema descarta el rol y la pila de pantallas y vuelve a LoginActivity.",
        ],
        alt=["3a. Cancela → se cierra la hoja y sigue en Perfil (cancelarCerrarSesion())."],
        post="Sin sesión. Los datos persistidos en SQLite no cambian.",
    ),
    dict(
        id="CU-03", nombre="Consultar socios (búsqueda, filtros y ficha)", actor="Administrativo", hu="HU-02, HU-05, HU-14",
        pantalla="PantallaSocios → PantallaFichaSocio",
        pre="Sesión de Administrativo.",
        flujo=[
            "Abre la pestaña Socios.",
            "El sistema lista el padrón leído de SQLite con la etiqueta de estado de cada socio (v1.5: «Habilitado · En gracia», «Habilitado · Cuota pendiente», «Habilitado · Cuota al día», «No habilitado · En mora», «No habilitado · Carnet vencido») y el contador de días con su tono verde / naranja / rojo.",
            "Puede escribir en la búsqueda (nombre o DNI, con o sin puntos) y elegir el filtro Todos / Habilitados / En mora (elegirFiltro()).",
            "Toca un socio (abrirSocio()) y el sistema muestra la ficha: datos, N° de socio, plan, carnet (emitido / vence / estado), cuota vigente con vencimiento, historial de pagos con comprobantes, actividades, ingresos, ficha médica; (v1.5) el aviso de habilitación lleva a la derecha la cápsula estadoCorto («Debe $…», «Quedan N días», «Vence en N días», «Al día», «Renovar carnet»), la tarjeta de la cuota muestra el contador en cápsula (contadorCorto) y una línea cuotaSubLabel con la fecha, y el total adeudado con el detalle cuota + recargo + interés si la cuota venció.",
        ],
        alt=["3a. La búsqueda o el filtro no coinciden con nadie → lista vacía."],
        post="Ninguna modificación. Desde la ficha se accede a CU-05, CU-06, CU-07 y CU-08.",
    ),
    dict(
        id="CU-04", nombre="Registrar socio", actor="Administrativo", hu="HU-02", pantalla="PantallaAlta (E_ALTA, «Alta de socio»)",
        pre="Sesión de Administrativo. El DNI a registrar no pertenece a otro socio.",
        flujo=[
            "Desde Inicio toca «Registrar socio» (irAAlta()): formulario limpio, plan «Full» por defecto, apto físico activo.",
            "Completa nombre, apellido (v1.5: campo aparte), DNI, correo, plan (Básico $12.000 / Full $18.500 / Familiar $29.000), apto físico y observaciones.",
            "Toca «Registrar socio» (confirmarAlta()).",
            "El sistema valida en orden: nombre, apellido (v1.5), DNI (formato), DNI único (COUNT en SQLite), correo.",
            "Genera el N° de socio (1500 + cantidad de socios; si ese número ya existe por una baja, el siguiente libre), emite el carnet (hoy → hoy + 12 meses − 1 día) y crea la cuota inicial PENDIENTE (precio del plan, vence hoy + 10 días) y la ficha nutricional inicial.",
            "Agrega el socio al padrón y lo guarda en SQLite; navega a Socios → Ficha del socio con el toast «Socio N° xxxxx registrado · carnet y cuota inicial generados».",
        ],
        alt=[
            "4a. Nombre vacío o sin apellido → «Ingresá nombre y apellido.»; con caracteres no permitidos → «El nombre solo puede contener letras y espacios.»",
            "4b. (v1.5) Apellido vacío o con caracteres no permitidos → mensaje equivalente para el apellido.",
            "4c. DNI vacío → «Ingresá el DNI.»; con letras → «El DNI solo puede contener números.»; distinto de 7 u 8 dígitos → «El DNI debe tener 7 u 8 dígitos.»",
            "4d. DNI ya registrado → «Ya existe un socio con ese DNI.»",
            "4e. Correo vacío → «Ingresá el correo electrónico.»; formato inválido → «El correo electrónico no tiene un formato válido (ej.: nombre@dominio.com).»",
            "En todos los casos el error se muestra bajo el formulario (altaError) y se permanece en «Alta de socio».",
        ],
        post="Socio persistido (tablas socio, carnet, cuota) con N° único, carnet vigente 12 meses, cuota inicial pendiente, sin rutina ni turno, IMC «—»; nace habilitado.",
    ),
    dict(
        id="CU-05", nombre="Editar socio", actor="Administrativo", hu="HU-02 (CRUD)", pantalla="PantallaAlta en modo edición (E_EDITAR, «Editar socio»)",
        pre="Ficha de un socio abierta (CU-03).",
        flujo=[
            "Toca «Editar datos» (irAEditar()).",
            "El sistema precarga nombre (v1.5: nombre y apellido partidos en el primer espacio), DNI, correo, plan, apto físico y observaciones.",
            "Modifica los datos y toca «Guardar cambios» (confirmarAlta() → guardarEdicion()).",
            "El sistema valida igual que el alta; el DNI puede coincidir con el del propio socio (contarDni excluye su id).",
            "Guarda nombre, DNI, correo, plan, apto físico y antecedentes; vuelve a la ficha con el toast «Datos del socio actualizados».",
        ],
        alt=["4a. Cualquier validación falla → altaError bajo el formulario; permanece en «Editar socio»."],
        post="Cambios persistidos en SQLite. El cambio de plan rige desde la próxima cuota: la cuota emitida, el carnet y el historial no se modifican.",
    ),
    dict(
        id="CU-06", nombre="Dar de baja socio", actor="Administrativo", hu="HU-02 (CRUD)", pantalla="PantallaFichaSocio (hoja «Dar de baja»)",
        pre="Ficha de un socio abierta.",
        flujo=[
            "Toca «Dar de baja» (pedirBajaSocio()).",
            "El sistema muestra la hoja con Socio, DNI y N° de socio y la advertencia de que la acción no se puede deshacer.",
            "Confirma «Confirmar baja».",
            "El sistema quita al socio del padrón, de los inscriptos de las actividades y de las asistencias; vuelve a Socios con el toast «{nombre} dado de baja».",
        ],
        alt=["3a. Cancela → se cierra la hoja y el socio sigue en el padrón."],
        post="Filas eliminadas en SQLite (carnet, cuota y pagos por ON DELETE CASCADE). Si era el socio de la cuenta de la app, esa cuenta ya no puede iniciar sesión (CU-01, 3b). El N° del próximo alta no repite ninguno existente.",
    ),
    dict(
        id="CU-07", nombre="Registrar pago de cuota en caja", actor="Administrativo", hu="HU-04", pantalla="PantallaFichaSocio (hoja «Registrar pago»)",
        pre="Ficha de un socio abierta; la cuota vigente no está pagada.",
        flujo=[
            "Toca «Registrar pago en caja» (pedirRegistrarPago()).",
            "(v1.5) El sistema calcula el importe con SocioVista(socio, parametros).totalAdeudado: si la cuota ya venció (en gracia o en mora), total = cuota + recargo (importe fijo vigente hoy, una vez) + interés (por cada día desde el vencimiento hasta hoy, cuota × interés mensual vigente ESE día / 100 / 30; los días anteriores al primer parámetro guardado no devengan); si no venció, total = cuota.",
            "Muestra la hoja «Registrar pago» con Socio, Cuota (mes) e Importe (detalle «Cuota $x + recargo $y + interés $z» cuando corresponde) y el botón «Cobrar $total».",
            "Confirma. El sistema ejecuta pagarCuota(id, «Caja»): la cuota pasa a PAGADA, proximaVence = vence + 1 mes, se antepone PagoHistorial(mes, total, hoy, «Caja», comprobante «CD-2609-####»).",
            "Guarda en SQLite (UPDATE cuota, INSERT pago), cierra la hoja y muestra «Pago registrado · socio habilitado».",
        ],
        alt=[
            "1a. La cuota ya está pagada → toast «La cuota de {mes} ya está pagada.» y la hoja no se abre (una cuota no se cobra dos veces).",
            "4a. Cancela la hoja → nada cambia.",
        ],
        post="Cuota pagada con comprobante; el socio queda habilitado si su carnet está vigente; sale de los listados «Vencen hoy» y «En mora». Al reabrir la app, completarCuotasSiguientes() crea la cuota del mes siguiente.",
        v15="Recargo e interés se deben desde que la cuota vence, también dentro de la gracia, pero no se aplican hacia atrás: cada día usa la tasa vigente ese día y antes de la primera fila no se devenga. Con la semilla de la demo (hace un año gracia 10 / $300 / 2,5 %; hace dos meses gracia 5 / $500 / 3 %) Lucas, vencido hace 29 días, debe $12.848 = $12.000 + recargo $500 + interés $348 (29 días al 3 %); Tomás debe $18.520; Rocío Paz, vencida hace 3 días y en gracia, $12.536. El historial guarda monto = total cobrado.",
    ),
    dict(
        id="CU-08", nombre="Emitir / renovar carnet", actor="Administrativo", hu="HU-07", pantalla="PantallaFichaSocio (hoja «Renovar carnet» / «Reemitir carnet»)",
        pre="Ficha de un socio abierta.",
        flujo=[
            "Toca la acción del carnet (pedirRenovarCarnet()); el título es «Renovar carnet» si venció o «Reemitir carnet» si está vigente.",
            "El sistema muestra la hoja con Socio y «Nueva vigencia»: hoy → hoy + 12 meses − 1 día.",
            "Confirma «Emitir carnet».",
            "El sistema actualiza carnetEmitido y carnetVence y muestra «Carnet emitido · vigente hasta dd/mm/aaaa».",
        ],
        alt=["3a. Cancela → el carnet no cambia."],
        post="Carnet vigente 12 meses persistido (tabla carnet). Si el socio estaba bloqueado solo por carnet vencido, queda habilitado; el carnet digital del socio (CU-16) muestra la nueva vigencia.",
    ),
    dict(
        id="CU-09", nombre="Validar ingreso por DNI (control de acceso)", actor="Administrativo", hu="HU-05", pantalla="PantallaAcceso",
        pre="Sesión de Administrativo.",
        flujo=[
            "Escribe el DNI (con o sin puntos) y toca «Validar» (validarAcceso()), o toca un DNI de prueba (ponerDniAcceso()): permitido, en mora, carnet vencido y (v1.5) «En gracia» (Rocío Paz, 39.876.540, vencida hace 3 días con 2 de gracia, deuda $12.536, carnet por vencer en 20 días).",
            "El sistema valida el formato del DNI.",
            "Busca el socio por DNI en el padrón.",
            "(v1.5) Construye SocioVista(socio, parametros) con los parámetros del estado (leídos de SQLite al iniciar la app): días vencida, gracia vigente hoy, recargo e interés; evalúa carnet y cuota.",
            "Muestra el resultado «Permitido» (verde): con cuota pagada «Carnet vigente y cuota al día.»; con cuota pendiente «Carnet vigente · cuota {mes} pendiente, vence en N días.» (o «vence hoy»).",
        ],
        alt=[
            "2a. DNI inválido → toast con el mensaje de validación y sin resultado.",
            "3a. No hay socio con ese DNI → «No encontrado»: «No hay ningún socio con DNI xx.xxx.xxx. Podés registrarlo como visitante o dar de alta un socio.» (atajos a CU-11 y CU-04).",
            "4a. Carnet vencido → «Denegado»: «Carnet vencido el dd/mm/aaaa. Debe renovarse.» (CU-08).",
            "4b. (v1.5) 0 < d <= gracia → «Permitido» (naranja): «Cuota {mes} vencida · quedan N días de gracia. Debe $total en recepción.» (con «queda 1 día de gracia» o «último día de gracia» cuando quedan 0).",
            "4c. (v1.5) d > gracia → «Denegado» (rojo): «Cuota {mes} en mora: vencida hace N días · gracia de G días superada. Debe $total en recepción.»",
        ],
        post="Resultado en pantalla; nada se persiste hasta CU-10.",
        v15="En v1.4 la mora era el estado guardado VENCIDA; en v1.5 se deriva de la fecha y de la gracia configurada en CU-13, sin consultar SQLite en cada acceso.",
    ),
    dict(
        id="CU-10", nombre="Registrar ingreso del socio («extend» de CU-09)", actor="Administrativo", hu="HU-05", pantalla="PantallaAcceso (resultado «Permitido»)",
        pre="Resultado «Permitido» de CU-09 todavía no registrado.",
        flujo=[
            "Toca «Registrar ingreso» (registrarIngreso()).",
            "El sistema antepone IngresoHoy(hora actual, nombre, «Permitido») a los ingresos del día y Ingreso(fecha · hora, «Permitido») al historial del socio; marca acceso.registrado = true.",
            "Guarda en SQLite (ingreso_hoy, ingresos_json) y muestra «Ingreso registrado · {nombre}».",
        ],
        alt=["1a. El ingreso ya fue registrado para ese resultado → no se vuelve a registrar (registrado = true)."],
        post="KPI «Ingresos de hoy» del Inicio +1; el ingreso figura en la ficha del socio.",
    ),
    dict(
        id="CU-11", nombre="Registrar pago diario de no socio", actor="Administrativo (beneficiario: No socio / Visitante)", hu="HU-03",
        pantalla="PantallaVisitantes → PantallaVisitanteNuevo («Ingreso de visitante»)",
        pre="Sesión de Administrativo. Hay actividades con cupo del día.",
        flujo=[
            "Desde Inicio («Registrar visitante») o desde Visitantes toca nuevo ingreso (irAVisitanteNuevo()): nombre y DNI vacíos, medio Efectivo, sin actividad.",
            "Escribe nombre y apellido y DNI del visitante; elige la actividad (elegirActividadVisitante()) y el medio de pago (Efectivo / Débito).",
            "El sistema muestra el cupo del día de cada actividad: libres = cupo − (ocupados + visitantes de hoy).",
            "Toca «Registrar pago diario» (confirmarVisitante()).",
            "El sistema valida nombre y DNI, la actividad y su cupo, y consulta en SQLite si ese DNI ya pagó hoy (contarVisitante(dni, HOY)).",
            "Registra Visitante(nombre, DNI con puntos, actividad, hora actual, monto = precio de la actividad, fecha de hoy, medio), lo guarda (INSERT visitante), vuelve a Visitantes y muestra «Pago diario registrado · $precio».",
        ],
        alt=[
            "2a. La actividad elegida no tiene cupo → «{actividad} no tiene cupo disponible hoy (x/y). El ingreso no puede realizarse.»",
            "5a. Nombre o DNI inválidos → visError con el mensaje de Validaciones («Ingresá nombre y apellido del visitante.», «El DNI debe tener 7 u 8 dígitos.», …).",
            "5b. Sin actividad elegida → «Seleccioná una actividad.»",
            "5c. Actividad sin cupo → «Sin cupo en {actividad}. El ingreso no puede realizarse.»",
            "5d. El DNI ya tiene un pago diario hoy → «Ya hay un pago diario registrado hoy con el DNI xx.xxx.xxx.»",
        ],
        post="Pago diario persistido con fecha y medio; aparece en Visitantes (hoy) con el total del día y en Reportes → «Pagos diarios» por fecha (CU-12); el cupo del día baja en 1 y el KPI de ingresos de hoy sube en 1.",
    ),
    dict(
        id="CU-12", nombre="Consultar reportes", actor="Administrativo", hu="HU-14", pantalla="PantallaReportes",
        pre="Sesión de Administrativo.",
        flujo=[
            "Abre la pestaña Reportes.",
            "Elige una pestaña (elegirReportTab()): «Vencen hoy» (cuotas pendientes con vencimiento hoy, SELECT por rango en SQLite), «Esta semana» (de mañana a 7 días), «En mora» (socios no habilitados por cuota), «Pagos diarios» (visitantes del día elegido).",
            "El sistema muestra las filas ordenadas por vencimiento (o por hora) y el total: suma de cuotas o suma de pagos diarios con su medio.",
            "En «Pagos diarios» navega al día anterior / siguiente (diaReporteAnterior/Siguiente); no se puede avanzar más allá de hoy.",
            "(v1.5) En «En mora» cada socio muestra el total adeudado (totalAdeudado) con el detalle cuota + recargo + interés y el total de la pestaña es la suma de esos totales; una sección «Parámetros vigentes» muestra el historial de gracia, recargo e interés por fecha.",
        ],
        alt=["3a. La pestaña no tiene filas → texto vacío y total $0."],
        post="Ninguna modificación. Cobrar una cuota (CU-07) la saca de «Vencen hoy» y «En mora».",
    ),
    dict(
        id="CU-13", nombre="Configurar parámetros del club (v1.5)", actor="Administrativo", hu="Regla de negocio de HU-04 y HU-05 (sin HU nueva; ver «A confirmar»)",
        pantalla="PantallaPerfil → sección «Configuración del club»",
        pre="Sesión de Administrativo. Base SQLite versión 4 con la tabla parametro; la semilla de la demo trae dos filas (desde hace un año: gracia 10 días, recargo $300, interés 2,5 %; desde hace dos meses: gracia 5, $500, 3 %). Si la tabla está vacía rige Parametros.INICIALES (5 / 0 / 0).",
        flujo=[
            "Abre Perfil → «Configuración del club» (también desde la acción rápida «Parámetros del club» del Inicio). El sistema carga el formulario con los valores vigentes hoy (prepararParametros() → campos pGracia, pRecargo, pInteres) y muestra el historial por fecha, más nuevo primero (historialParametros).",
            "Edita el período de gracia (días enteros, 0..60), el recargo (importe fijo en pesos, decimal, con coma o punto) y el interés mensual (%, 0..100) y toca «Guardar parámetros» (guardarParametros()).",
            "El sistema valida y arma Parametros(gracia, recargo, interés, vigenteDesde = hoy); lo antepone a la lista del estado quitando cualquier otra fila de hoy (guardar dos veces el mismo día reemplaza la de hoy).",
            "El setter de estado llama a ClubRepositorio.guardar → ClubDbHelper.guardar, que reescribe la tabla parametro (DELETE + INSERT de todas las filas, con orden) dentro de la transacción; toast «Parámetros guardados · rigen desde hoy».",
            "Las SocioVista se recalculan con la lista nueva: estado derivado (pendiente / en gracia / mora), total adeudado y contador de todos los socios.",
        ],
        alt=[
            "3a. Gracia no entera o fuera de 0..60 → «La gracia debe ser un número entero de 0 a 60 días.»",
            "3b. Recargo no numérico, no finito o fuera de 0..1.000.000 → «El recargo debe ser un importe de 0 a 1.000.000.»",
            "3c. Interés no numérico o fuera de 0..100 → «El interés mensual debe ser un porcentaje de 0 a 100.»",
            "En los tres casos el error se muestra en el formulario (paramError) y no se guarda nada.",
        ],
        post="Los parámetros rigen desde hoy inclusive: List<Parametros>.vigenteEn(fecha) toma el más nuevo con vigenteDesde <= fecha; para fechas anteriores al primer parámetro guardado devuelve la gracia de la fila más antigua con recargo 0 e interés 0 (recargo e interés no se aplican hacia atrás). El interés se devenga día a día desde el vencimiento con la tasa que regía cada día; los días previos a que el administrador fijara la primera tasa no devengan nada. Tabla parametro(id, gracia_dias, recargo, interes_mensual, vigente_desde, orden), base versión 4; la lista vuelve a cargarse en ClubRepositorio.iniciar() → ClubDbHelper.leer() al abrir la app.",
        v15="Caso nuevo definido íntegramente por el spec v1.5.0 (objetivo 2 y decisiones).",
    ),
    dict(
        id="CU-14", nombre="Pagar cuota desde la app", actor="Socio", hu="HU-04", pantalla="PantallaSocioHome / PantallaCuotas → PantallaPagar → PantallaPagoOk",
        pre="Sesión de Socio; la cuota vigente no está pagada.",
        flujo=[
            "Toca «Pagar» en el Inicio o «Pagar cuota» en Estado de cuenta.",
            "Elige el medio (elegirMedio()): Tarjeta de débito / crédito, Billetera virtual o Efectivo en el club (cupón para recepción).",
            "(v1.5) El sistema muestra el importe total (totalAdeudado): cuota, más recargo e interés si la cuota ya venció (en gracia o en mora).",
            "Toca «Confirmar pago de $total» (confirmarPago()).",
            "El sistema ejecuta pagarCuota(socio de la app, medio): cuota PAGADA, proximaVence = vence + 1 mes, PagoHistorial con comprobante «CD-2609-####»; guarda en SQLite.",
            "Reemplaza la pantalla de pago por «Pago OK» con el comprobante (sin barra inferior).",
        ],
        alt=["4a. La cuota ya estaba pagada → toast «La cuota de {mes} ya está pagada.», no se registra otro pago ni se navega al comprobante."],
        post="Igual que CU-07. No hay pasarela de pago real: el pago se registra en la base, tal como define el diseño.",
    ),
    dict(
        id="CU-15", nombre="Consultar estado de cuenta, deuda y contador de días", actor="Socio", hu="HU-04", pantalla="PantallaSocioHome, PantallaCuotas («Estado de cuenta»)",
        pre="Sesión de Socio.",
        flujo=[
            "En el Inicio el sistema muestra la tarjeta de la cuota: mes, monto, estado (Pagada / Pendiente / Vencida) y «Vence en N días · dd/mm/aaaa», «Vence hoy» o «Venció hace N días».",
            "(v1.5) Muestra el contador con su tono: «Vence en N días» / «Vence hoy» (OK, verde), «Quedan N días de gracia» / «Queda 1 día de gracia» / «Último día de gracia» (AVISO, naranja), «Vencida hace N días · gracia de G días superada» (BLOQUEO, rojo) y «Pagada»; si la cuota venció, el total adeudado con el detalle «Cuota $x + recargo $y + interés $z» (interés devengado día a día desde el vencimiento con la tasa de cada día; nada antes del primer parámetro guardado).",
            "Abre «Estado de cuenta»: cuota vigente y el historial de pagos con fecha, medio y comprobante; si la cuota está pagada, «Pagada · próxima dd/mm/aaaa».",
        ],
        alt=["2a. En mora → además se avisa que el acceso está bloqueado hasta regularizar (CU-14 o CU-07)."],
        post="Ninguna modificación.",
        v15="Contador de días y deuda del socio: objetivos 4 y 5 del spec.",
    ),
    dict(
        id="CU-16", nombre="Ver carnet digital", actor="Socio", hu="HU-06", pantalla="PantallaCarnet",
        pre="Sesión de Socio.",
        flujo=[
            "Abre la pestaña Carnet.",
            "El sistema muestra el carnet digital: nombre, N° de socio, DNI, plan, foto, fecha de emisión y de vencimiento y el estado Vigente / Por vencer (menos de 30 días) / Vencido.",
        ],
        alt=["2a. Carnet vencido → estado «Vencido»; el socio debe renovarlo en recepción (CU-08)."],
        post="Ninguna modificación.",
    ),
    dict(
        id="CU-17", nombre="Consultar rutina asignada", actor="Socio", hu="HU-10", pantalla="PantallaRutinaSocio (y tarjeta «Rutina · hoy» del Inicio)",
        pre="Sesión de Socio.",
        flujo=[
            "Abre la pestaña Rutina.",
            "El sistema muestra nombre de la rutina, profesora, fecha de actualización y los días; el socio cambia de día (elegirDiaRutina()) y ve los ejercicios numerados con series × repeticiones.",
        ],
        alt=["2a. Sin rutina asignada → «Sin rutina asignada» o «Sin rutina · falta ficha médica» según el apto físico."],
        post="Ninguna modificación.",
    ),
    dict(
        id="CU-18", nombre="Solicitar turno de nutrición", actor="Socio", hu="HU-11", pantalla="PantallaNutriSocio → PantallaTurno («Solicitar turno»)",
        pre="Sesión de Socio sin turno vigente.",
        flujo=[
            "En Nutrición toca «Solicitar turno».",
            "El sistema muestra la grilla de días y horarios; los ya tomados por otros socios aparecen ocupados.",
            "Elige un horario libre (elegirSlot()) y toca «Confirmar turno» (confirmarTurno()).",
            "El sistema guarda Turno(fecha, hora), vuelve a Nutrición y muestra «Turno confirmado · dd/mm hh:mm».",
        ],
        alt=["3a. No eligió horario → toast «Elegí un horario disponible»."],
        post="Turno persistido (turno_fecha, turno_hora); aparece en la agenda de la nutricionista (CU-24).",
    ),
    dict(
        id="CU-19", nombre="Cancelar turno de nutrición", actor="Socio", hu="HU-11", pantalla="PantallaNutriSocio (hoja «Cancelar turno»)",
        pre="Sesión de Socio con turno vigente.",
        flujo=[
            "Toca «Cancelar turno» (pedirCancelarTurno()).",
            "El sistema muestra la hoja con el turno a liberar.",
            "Confirma «Sí, cancelar»; el sistema borra el turno y muestra «Turno cancelado».",
        ],
        alt=["3a. Cancela la hoja → el turno se mantiene."],
        post="Horario liberado para otros socios.",
    ),
    dict(
        id="CU-20", nombre="Consultar información nutricional propia", actor="Socio", hu="HU-13", pantalla="PantallaNutriSocio",
        pre="Sesión de Socio.",
        flujo=[
            "Abre la pestaña Nutrición.",
            "El sistema muestra peso, altura e IMC (o «—» si no hay medidas), objetivo, antecedentes, el historial de consultas (fecha, peso, observaciones), el turno vigente y la nutricionista.",
        ],
        alt=["2a. Sin consultas → «Sin consultas registradas»."],
        post="Ninguna modificación.",
    ),
    dict(
        id="CU-21", nombre="Registrar presencia propia", actor="Entrenadora", hu="HU-08", pantalla="PantallaProfesorHome",
        pre="Sesión de Entrenadora.",
        flujo=[
            "En el Inicio toca marcar presencia (marcarPresencia()).",
            "El sistema registra profePresente = true y muestra «Presencia registrada · hh:mm».",
        ],
        alt=["—"],
        post="Presencia persistida en la tabla sesion (profe_presente).",
    ),
    dict(
        id="CU-22", nombre="Registrar asistencia de clase", actor="Entrenadora", hu="HU-08", pantalla="PantallaProfesorHome → PantallaAsistencia",
        pre="Sesión de Entrenadora; la clase es suya (Musculación 18:00, Funcional 19:30).",
        flujo=[
            "Desde el Inicio toca una clase (abrirAsistencia()) o en la pestaña Asistencia elige la clase (elegirClase()).",
            "El sistema lista los inscriptos con su marca de presente.",
            "Toca cada socio para alternar presente / ausente (alternarAsistencia()).",
            "Toca «Guardar asistencia» (guardarAsistencia()); el sistema muestra «Asistencia guardada · {clase} · n/m presentes» y vuelve al Inicio.",
        ],
        alt=["3a. Cambia de clase → cada clase conserva su propia lista de presentes."],
        post="Asistencia por clase persistida (asistencia_clase, asistencia); el Inicio muestra presentes / inscriptos.",
    ),
    dict(
        id="CU-23", nombre="Crear y asignar rutina", actor="Entrenadora", hu="HU-09", pantalla="PantallaRutinasProfesor → PantallaRutinaEdit («Rutina del socio»)",
        pre="Sesión de Entrenadora. El socio pertenece a sus actividades.",
        flujo=[
            "Abre Rutinas: lista de socios de Musculación / Funcional con «Crear» o «Ver» y las rutinas pendientes.",
            "Abre un socio (abrirRutina()); si no tenía rutina el editor arranca con tres días vacíos.",
            "Elige el día (elegirDiaEdicion()), escribe el ejercicio, series (1..20) y repeticiones (1..100) y toca «Nuevo ejercicio» (agregarEjercicio()); puede quitar ejercicios (quitarEjercicio()).",
            "Toca «Guardar y asignar rutina» (guardarRutina()): se guardan los días con ejercicios, nombre «Rutina personalizada» (o el anterior), profesora y fecha de hoy; toast «Rutina asignada a {nombre} · visible en su app».",
        ],
        alt=[
            "2a. El socio no está habilitado (cuota en mora o carnet vencido) o no tiene apto físico → la asignación se bloquea con el motivo («cuota en mora», «carnet vencido», «falta la ficha médica»).",
            "3a. Nombre del ejercicio vacío → «Ingresá el nombre del ejercicio»; series o repeticiones no enteras o fuera de rango → «Las series deben estar entre 1 y 20.» / «Las repeticiones deben estar entre 1 y 100.»",
            "4a. Ningún día con ejercicios → «Agregá al menos un ejercicio».",
        ],
        post="Rutina persistida (rutina_json) y visible para el socio (CU-17).",
    ),
    dict(
        id="CU-24", nombre="Consultar agenda de turnos", actor="Nutricionista", hu="HU-11", pantalla="PantallaAgenda",
        pre="Sesión de Nutricionista.",
        flujo=[
            "Abre la pestaña Agenda.",
            "El sistema agrupa los turnos por día (hoy y los próximos), ordenados por hora, con el nombre del socio.",
            "Toca un turno para abrir la ficha nutricional del socio (abrirFicha(), CU-25).",
        ],
        alt=["2a. Un día sin turnos no se lista."],
        post="Ninguna modificación.",
    ),
    dict(
        id="CU-25", nombre="Registrar consulta nutricional (ficha nutricional)", actor="Nutricionista", hu="HU-12", pantalla="PantallaFichas → PantallaFichaNutricional",
        pre="Sesión de Nutricionista; socio elegido desde Agenda o Fichas.",
        flujo=[
            "El sistema muestra la ficha: datos del socio, peso, altura, IMC, objetivo, antecedentes e historial de consultas.",
            "En «Registrar consulta de hoy» ingresa peso (kg, obligatorio, 20..300, con coma o punto), altura (m, opcional, 0,50..2,50) y observaciones.",
            "Toca «Guardar en historial» (guardarConsulta()).",
            "El sistema actualiza peso y altura, antepone Consulta(hoy, peso, observaciones o «Control de rutina.»), consume el turno si era de hoy, limpia el formulario y muestra «Consulta guardada en el historial».",
        ],
        alt=[
            "3a. Peso vacío → «Ingresá el peso.»; no numérico → «El peso debe ser un número (ej.: 62,4).»; fuera de rango → «El peso debe estar entre 20 y 300 kg.»",
            "3b. Altura no numérica → «La altura debe ser un número en metros (ej.: 1,68).»; fuera de rango → «La altura debe estar entre 0,50 y 2,50 m.»",
        ],
        post="Consulta persistida (consultas_json, nut_peso, nut_altura); el turno de hoy queda liberado; el socio la ve en CU-20.",
    ),
]

A_CONFIRMAR = [
    "Comportamiento de CU-21 (presencia de la entrenadora) al tocar dos veces: el código solo pone profePresente = true; no hay prueba ni spec del segundo toque.",
]

RESUELTOS = [
    "Recargo e interés se deben desde que la cuota vence, también dentro de la gracia: SocioVista.recargo = recargo vigente hoy si cuotaVencida; interes se suma por día con la tasa vigente cada día; totalAdeudado = cuota + recargo + interés es lo que cobra pagarCuota en gracia y en mora. No es retroactivo: antes del primer parámetro guardado vigenteEn devuelve recargo 0 e interés 0.",
    "Los parámetros viven en UiState.parametros (List<Parametros>), se cargan en ClubRepositorio.iniciar() → ClubDbHelper.leer() y se reescriben en guardar(); SocioVista(socio, parametros) usa vigenteEn(fecha) en memoria, sin consultar SQLite en cada acceso.",
    "Nombres definitivos en ClubViewModel: val parametros (vigente hoy), val historialParametros (más nuevo primero), prepararParametros(), guardarParametros(); campos pGracia, pRecargo, pInteres; mensajes de validación («La gracia debe ser un número entero de 0 a 60 días.», «El recargo debe ser un importe de 0 a 1.000.000.», «El interés mensual debe ser un porcentaje de 0 a 100.») y toast documentados en CU-13.",
    "Textos de la pantalla de acceso y etiquetas de SocioVista (badge, contador, tono OK / AVISO / BLOQUEO) documentados en CU-09 y CU-15.",
    "CU-13 queda como regla de negocio de HU-04 y HU-05 (sin HU propia). Un único actor Administrativo (el rol EMPLEADO no tiene cuenta de demo). La semilla de v1.5 es relativa a la fecha real del dispositivo.",
]

TRAZABILIDAD = [
    ("HU-01", "Inicio de sesión por rol", "CU-01, CU-02"),
    ("HU-02", "Registrar socio (carnet + cuota inicial + ficha médica) y CRUD", "CU-03, CU-04, CU-05, CU-06"),
    ("HU-03", "Visitantes: pago diario con fecha, medio y cupo del día", "CU-11"),
    ("HU-04", "Consultar y pagar cuota", "CU-07, CU-14, CU-15, (CU-13 regla v1.5)"),
    ("HU-05", "Control de acceso por DNI", "CU-09, CU-10, (CU-13 regla v1.5)"),
    ("HU-06", "Carnet digital", "CU-16"),
    ("HU-07", "Emitir / renovar carnet", "CU-08"),
    ("HU-08", "Registrar asistencia", "CU-21, CU-22"),
    ("HU-09", "Crear y asignar rutina, con validación", "CU-23"),
    ("HU-10", "Consultar rutina asignada", "CU-17"),
    ("HU-11", "Turnos de nutrición (socio y profesional)", "CU-18, CU-19, CU-24"),
    ("HU-12", "Ficha nutricional", "CU-25"),
    ("HU-13", "Información nutricional propia", "CU-20"),
    ("HU-14", "Reportes: vencen hoy, esta semana, en mora y pagos diarios por fecha", "CU-12"),
]

DIAGRAMAS_SECUENCIA = [
    ("03_sec_control_acceso.png", "Control de acceso por DNI con la regla de habilitación (CU-09 / CU-10) (v1.5)",
     "validarAcceso() valida el DNI, busca el socio y construye SocioVista(socio, parametros) con los parámetros del estado (leídos de SQLite al iniciar la app, sin consulta por acceso). "
     "El resultado se deriva de la fecha y la gracia vigente: carnet vencido (denegado), cuota pagada o pendiente sin vencer (permitido), en gracia (permitido con aviso y total adeudado) o en mora (denegado, con el total). Si es «Permitido», registrarIngreso() persiste el ingreso del día."),
    ("04_sec_cobro_caja.png", "Cobro de cuota en caja con recargo e interés (CU-07) (v1.5)",
     "pedirRegistrarPago() rechaza la cuota ya pagada; si no, SocioVista calcula totalAdeudado (cuota + recargo + interés desde el vencimiento, también en gracia) con los parámetros del estado, la hoja lo muestra y, al confirmar, "
     "pagarCuota(id, «Caja») marca la cuota como pagada, abre la siguiente y guarda el pago por el total con su comprobante en SQLite."),
    ("05_sec_pago_diario_visitante.png", "Registro de pago diario de un no socio (CU-11)",
     "elegirActividadVisitante() avisa si la actividad no tiene cupo hoy; confirmarVisitante() valida nombre y DNI, actividad y cupo, consulta en SQLite si ese DNI ya pagó hoy "
     "y, si no, inserta el Visitante con fecha, hora, monto y medio."),
    ("06_sec_parametros_club.png", "El administrador guarda los parámetros del club (CU-13) (v1.5)",
     "La sección «Configuración del club» del Perfil carga el formulario con prepararParametros() y muestra historialParametros; guardarParametros() valida, antepone Parametros(vigenteDesde = hoy) a la lista del estado "
     "(reemplazando la fila de hoy si existe) y el setter de estado persiste la foto completa: ClubRepositorio.guardar → ClubDbHelper.guardar reescribe la tabla parametro (DELETE + INSERT) en la transacción."),
]
