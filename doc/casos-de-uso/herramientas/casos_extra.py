# -*- coding: utf-8 -*-
"""Campos de la plantilla única (participantes, disparador, reglas, datos de demo) y correcciones
al texto de contenido.CASOS tras verificarlo contra el código de la v1.5.0."""

EXTRA = {
 "CU-01": dict(
  part="Pantalla: PantallaLogin (LoginActivity). Participantes: ClubViewModel, ClubRepositorio, ClubDbHelper (SQLite, tabla usuario), Activity del rol (SocioActivity, AdminActivity, EntrenadoraActivity, NutricionistaActivity).",
  disp="El usuario toca «Iniciar sesión» (login()).",
  reglas=["El rol sale de la cuenta (tabla usuario); no hay selector de perfil.", "El alias se normaliza (sin tildes ni mayúsculas) antes de compararlo.", "Un socio dado de baja pierde el acceso (dadoDeBaja)."],
  demo="Socio: Andres / Socio123 · Administrativo: Jorge / Admin123 · Entrenadora: Ana / Prof123 · Nutricionista: Leandro / Nut123. Para el error: Andres / incorrecta → «Usuario o contraseña incorrectos.»"),
 "CU-02": dict(
  part="Pantalla: PantallaPerfil con HojaCerrarSesion. Participantes: ClubViewModel, ClubRepositorio, ClubDbHelper (tabla sesion), LoginActivity.",
  disp="El usuario toca «Cerrar sesión» en Perfil (pedirCerrarSesion()).",
  reglas=["Se pide confirmación antes de cerrar.", "Los datos del club no se tocan; solo se descarta el rol y la pila de pantallas."],
  demo="Cualquier cuenta de CU-01 (por ejemplo Jorge / Admin123): Perfil → Cerrar sesión → confirmar."),
 "CU-03": dict(
  part="Pantallas: PantallaSocios, PantallaFichaSocio. Participantes: ClubViewModel, SocioVista (etiquetas de estado, contador, deuda).",
  disp="El Administrativo abre la pestaña Socios.",
  reglas=["Habilitado = no está en mora y el carnet no venció (SocioVista.hab).", "La búsqueda acepta nombre o DNI con o sin puntos.", "El filtro «En mora» usa SocioVista.mora (d > gracia), no el estado guardado."],
  demo="Jorge / Admin123. Buscar «39876540» → Rocío Paz (En período de gracia). Filtro «En mora» → Lucas Benítez y Tomás Giménez. Buscar «zzz» → «Ningún socio coincide con la búsqueda.»"),
 "CU-04": dict(
  part="Pantalla: PantallaAlta (E_ALTA). Participantes: ClubViewModel, Validaciones, ClubRepositorio (contarDni), ClubDbHelper (SQLite: socio, carnet, cuota).",
  disp="El Administrativo toca «Registrar socio» (Inicio o «+ Alta» en Socios).",
  reglas=["Validaciones en orden: nombre, apellido, DNI (7 u 8 dígitos), correo y DNI único.", "N° de socio = 1500 + cantidad de socios, o el siguiente libre.", "Carnet por 12 meses; cuota inicial al precio del plan con vencimiento a 10 días.", "Planes: Básico $12.000, Full $18.500, Familiar $29.000."],
  demo="Jorge / Admin123. Alta válida: nombre «Paula», apellido «Gómez», DNI 30111222, correo paula.gomez@club.com, plan Full. DNI duplicado: 38412907 (Andrés) → «Ya existe un socio con ese DNI.»"),
 "CU-05": dict(
  part="Pantalla: PantallaAlta en modo edición (E_EDITAR). Participantes: ClubViewModel, ClubRepositorio (contarDni con exceptoId), ClubDbHelper (SQLite: socio).",
  disp="El Administrativo toca «Editar datos» en la ficha (irAEditar()).",
  reglas=["Mismas validaciones que el alta; el DNI puede ser el del propio socio.", "El cambio de plan rige desde la próxima cuota; la cuota emitida, el carnet y el historial no cambian."],
  demo="Jorge / Admin123 → Socios → Camila Duarte → «Editar datos»: cambiar plan a Básico y guardar. Para el error, poner el DNI 38412907 (de Andrés)."),
 "CU-06": dict(
  part="Pantalla: PantallaFichaSocio (HojaConfirmacion). Participantes: ClubViewModel, ClubRepositorio, ClubDbHelper (SQLite: socio con ON DELETE CASCADE a carnet, cuota y pago).",
  disp="El Administrativo toca «Dar de baja» en la ficha (pedirBajaSocio()).",
  reglas=["La baja pide confirmación y no se puede deshacer.", "Quita al socio del padrón, de los inscriptos de las actividades y de las asistencias.", "La cuenta de app del socio (id 1) deja de poder iniciar sesión."],
  demo="Jorge / Admin123 → Socios → Camila Duarte (DNI 43.210.987) → «Dar de baja» → «Confirmar baja»."),
 "CU-07": dict(
  part="Pantalla: PantallaFichaSocio (hoja «Registrar pago»). Participantes: ClubViewModel, SocioVista (totalAdeudado), ClubRepositorio, ClubDbHelper (SQLite: cuota, pago).",
  disp="El Administrativo toca «Registrar pago en caja» (pedirRegistrarPago()).",
  reglas=["Una cuota pagada no se cobra dos veces.", "totalAdeudado = cuota + recargo (una vez, si venció) + interés diario con la tasa vigente cada día / 30.", "Recargo e interés se deben desde el vencimiento, también dentro de la gracia; no son retroactivos.", "El comprobante es «CD-2609-» + 4 dígitos; el historial guarda monto = total cobrado."],
  demo="Jorge / Admin123. Lucas Benítez (40.120.334): cuota $12.000 vencida hace 29 días → $12.848 (recargo $500 + interés $348). Rocío Paz (39.876.540, en gracia): $12.536. Camila Duarte (cuota pagada) → «La cuota de {mes} ya está pagada.»"),
 "CU-08": dict(
  part="Pantalla: PantallaFichaSocio (hoja «Renovar carnet» / «Reemitir carnet»). Participantes: ClubViewModel, SocioVista (carnetAccion), ClubRepositorio, ClubDbHelper (SQLite: carnet).",
  disp="El Administrativo toca la acción del carnet en la ficha (pedirRenovarCarnet()).",
  reglas=["Siempre 12 meses: de hoy a hoy + 12 meses − 1 día.", "Título «Renovar carnet» si venció, «Reemitir carnet» si está vigente."],
  demo="Jorge / Admin123 → Socios → Sofía Acosta (35.887.210, carnet vencido hace 8 días) → «Renovar carnet» → «Emitir carnet»."),
 "CU-09": dict(
  part="Pantalla: PantallaAcceso. Participantes: ClubViewModel (validarAcceso), Validaciones, SocioVista (hab, enGracia, mora, totalAdeudado). No consulta SQLite: el padrón y los parámetros ya están en el estado.",
  disp="El Administrativo toca «Validar» (validarAcceso()) o un DNI de prueba (ponerDniAcceso()).",
  reglas=["hab = no está en mora y el carnet no venció.", "Con la cuota vencida: d <= gracia → Permitido con aviso (naranja); d > gracia → Denegado por mora (rojo).", "Si coinciden mora y carnet vencido, el mensaje es el de la mora.", "La gracia es la vigente hoy (CU-13)."],
  demo="Jorge / Admin123 → Acceso, botones «Probar»: 38.412.907 Al día (Permitido) · 40.120.334 En mora (Denegado) · 39.876.540 En gracia (Permitido, naranja, debe $12.536) · 35.887.210 Carnet vencido (Denegado) · 11.111.111 No existe."),
 "CU-10": dict(
  part="Pantalla: PantallaAcceso (resultado «Permitido»). Participantes: ClubViewModel, ClubRepositorio, ClubDbHelper (SQLite: ingreso_hoy, socio.ingresos_json).",
  disp="El Administrativo toca «Registrar ingreso» (registrarIngreso()).",
  reglas=["El botón solo aparece si el socio está habilitado y el ingreso aún no se registró.", "Un socio en gracia es habilitado: su ingreso se registra."],
  demo="Jorge / Admin123 → Acceso → 38.412.907 (Andrés) → «Registrar ingreso» → «Ingreso registrado · Andrés Weitzel». Repetirlo: el botón ya no aparece. Con 40.120.334 (Lucas, Denegado) el botón no existe."),
 "CU-11": dict(
  part="Pantallas: PantallaVisitantes, PantallaVisitanteNuevo. Participantes: ClubViewModel, ClubRepositorio (contarVisitante), ClubDbHelper (SQLite: visitante). Beneficiario: No socio / Visitante.",
  disp="El Administrativo toca «Registrar visitante» (irAVisitanteNuevo()).",
  reglas=["Cupo del día = cupo − (ocupados + visitantes de hoy en esa actividad).", "Un solo pago diario por DNI y fecha.", "El monto es el precio de la actividad; el medio es Efectivo o Débito."],
  demo="Jorge / Admin123. Natación está sin cupo (12/12) → error. Pedro Alonso, DNI 33.120.455, ya pagó hoy → «Ya hay un pago diario registrado hoy con el DNI 33.120.455.» Alta válida: «Sergio Lara», 31222333, Yoga, Efectivo → «Pago diario registrado · $5.000»."),
 "CU-12": dict(
  part="Pantalla: PantallaReportes. Participantes: ClubViewModel, SocioVista (totalAdeudado), ClubRepositorio (idsCuotasPorVencer, visitantesPorFecha), ClubDbHelper (SQLite: cuota, socio, visitante).",
  disp="El Administrativo abre la pestaña Reportes y elige una pestaña (elegirReportTab()).",
  reglas=["«Vencen hoy»: cuotas PENDIENTES que vencen hoy; «Esta semana»: de mañana a 7 días.", "«En mora»: socios con mora; el total suma totalAdeudado.", "«Visitantes»: pagos diarios del día elegido; no se avanza más allá de hoy."],
  demo="Jorge / Admin123 → Reportes. Vencen hoy: Martín Ledesma. Esta semana: Andrés Weitzel y Valentina Ruiz ($30.500). En mora: Lucas y Tomás. Visitantes: hoy $17.000 (3 pagos); día anterior $14.000."),
 "CU-13": dict(
  part="Pantalla: PantallaPerfil (sección «Configuración del club», solo ADMIN). Participantes: ClubViewModel, ClubRepositorio, ClubDbHelper (SQLite versión 4: tabla parametro).",
  disp="El Administrativo toca «Guardar parámetros» (guardarParametros()).",
  reglas=["Gracia entera 0..60; recargo 0..1.000.000; interés mensual 0..100 %.", "Cada guardado rige desde hoy; guardar dos veces el mismo día reemplaza la fila de hoy.", "No es retroactivo: antes del primer parámetro guardado no se devenga recargo ni interés."],
  demo="Jorge / Admin123 → Inicio → «Parámetros del club». Valores de la demo: gracia 5 días, recargo $500, interés 3 % (vigente desde hace dos meses; antes 10 / $300 / 2,5 % desde hace un año). Error: gracia «x» → «La gracia debe ser un número entero de 0 a 60 días.»"),
 "CU-14": dict(
  part="Pantallas: PantallaSocioHome / PantallaCuotas → PantallaPagar → PantallaPagoOk. Participantes: ClubViewModel, SocioVista (totalAdeudado), ClubRepositorio, ClubDbHelper (SQLite: cuota, pago).",
  disp="El Socio toca «Confirmar pago de $total» (confirmarPago()).",
  reglas=["Se cobra totalAdeudado (cuota más recargo e interés si venció).", "El pago se simula: no hay pasarela real.", "El botón «Pagar» solo aparece con la cuota pendiente; el ViewModel además rechaza una cuota ya pagada."],
  demo="Andres / Socio123 (DNI 38.412.907): cuota Full $18.500 pendiente, vence en 2 días → Pagar → Tarjeta → «Confirmar pago de $18.500»."),
 "CU-15": dict(
  part="Pantallas: PantallaSocioHome, PantallaCuotas («Estado de cuenta»). Participantes: ClubViewModel, SocioVista (badge, contadorCorto, cuotaSubLabel, deudaDetalle, totalLabel).",
  disp="El Socio abre el Inicio o «Ver estado de cuenta →».",
  reglas=["Tono verde (OK), naranja (AVISO, en gracia) o rojo (BLOQUEO, mora).", "Si la cuota venció se muestra el total adeudado con el detalle cuota + recargo + interés."],
  demo="Andres / Socio123: «Vence en 2 días». Para ver gracia y mora, entrar como Jorge y abrir las fichas de Rocío Paz («Quedan 2 días») y Lucas Benítez («Vencida hace 29 días»), que usan los mismos campos de SocioVista."),
 "CU-16": dict(
  part="Pantalla: PantallaCarnet (S_CARNET). Participantes: ClubViewModel (yo), SocioVista (carnetEstado, dniLabel, vigencia).",
  disp="El Socio toca la pestaña Carnet.",
  reglas=["Estado del carnet: Vencido si venció; Por vencer si faltan menos de 30 días; Vigente en otro caso.", "El pie de la tarjeta resume la cuota: al día, en mora, en período de gracia o pendiente."],
  demo="Andres / Socio123: Andrés Weitzel, DNI 38.412.907, Socio N° 01245, Plan Full, carnet Vigente."),
 "CU-17": dict(
  part="Pantalla: PantallaRutinaSocio (y tarjeta «Rutina · hoy» de PantallaSocioHome). Participantes: ClubViewModel (diasDeRutina, diaDeRutina).",
  disp="El Socio toca la pestaña Rutina.",
  reglas=["Solo se ve la rutina propia.", "Sin rutina se muestra el aviso para pedirla a la entrenadora."],
  demo="Andres / Socio123: rutina «Hipertrofia 3 días» (Prof. Ana Alvarez), días D1 Tren inferior, D2 Tren superior, D3 Full body."),
 "CU-18": dict(
  part="Pantallas: PantallaNutriSocio → PantallaTurno. Participantes: ClubViewModel (slots, elegirSlot, confirmarTurno), ClubRepositorio, ClubDbHelper (SQLite: socio.turno_fecha, turno_hora).",
  disp="El Socio toca «Confirmar turno» (confirmarTurno()).",
  reglas=["Hay 3 días × 6 horarios (16:00 a 18:30); un horario tomado por otro socio queda deshabilitado.", "«Confirmar turno» está deshabilitado hasta elegir un horario."],
  demo="Andres / Socio123: primero cancelar su turno (CU-19); luego Nutrición → «Solicitar turno». El horario de Valentina (mañana 17:00) aparece ocupado."),
 "CU-19": dict(
  part="Pantalla: PantallaNutriSocio (hoja «Cancelar turno»). Participantes: ClubViewModel, ClubRepositorio, ClubDbHelper (SQLite: socio).",
  disp="El Socio toca «Cancelar turno» (pedirCancelarTurno()).",
  reglas=["Se pide confirmación y el horario queda libre para otros socios."],
  demo="Andres / Socio123: tiene turno dentro de 3 días a las 17:30 → Nutrición → «Cancelar turno» → «Sí, cancelar»."),
 "CU-20": dict(
  part="Pantalla: PantallaNutriSocio. Participantes: ClubViewModel, SocioVista (imc, pesoLabel, alturaLabel, turnoSub).",
  disp="El Socio toca la pestaña Nutrición.",
  reglas=["IMC = peso / altura²; sin altura se muestra «—».", "Solo ve su propia ficha."],
  demo="Andres / Socio123: 62,4 kg · 1,68 m · IMC 22,1; objetivo «Recomposición corporal, +masa muscular»; tres consultas en el historial."),
 "CU-21": dict(
  part="Pantalla: PantallaProfesorHome. Participantes: ClubViewModel, ClubRepositorio, ClubDbHelper (SQLite: sesion.profe_presente).",
  disp="La Entrenadora toca «Marcar presencia» (marcarPresencia()).",
  reglas=["Una vez marcada, el botón desaparece y la tarjeta muestra «Presente».", "El Administrativo la ve en Inicio → Supervisión."],
  demo="Ana / Prof123 → Inicio → «Marcar presencia» → toast «Presencia registrada · hh:mm»."),
 "CU-22": dict(
  part="Pantallas: PantallaProfesorHome → PantallaAsistencia. Participantes: ClubViewModel, ClubRepositorio, ClubDbHelper (SQLite: asistencia_clase, asistencia).",
  disp="La Entrenadora toca una clase (abrirAsistencia()).",
  reglas=["Solo las clases que dicta: Musculación 18:00 y Funcional 19:30.", "Cada clase conserva su propia lista de presentes."],
  demo="Ana / Prof123 → Musculación (inscriptos: Andrés, Lucas, Julián, Camila; presentes de base Julián y Camila) → tocar a Andrés → «Guardar asistencia»."),
 "CU-23": dict(
  part="Pantallas: PantallaRutinasProfesor → PantallaRutinaEdit. Participantes: ClubViewModel, SocioVista (bloqueaRutina, bloqueoMotivo), ClubRepositorio, ClubDbHelper (SQLite: socio.rutina_json).",
  disp="La Entrenadora abre un socio de la lista (abrirRutina()).",
  reglas=["Solo se asigna a socios habilitados con apto físico (permiteRutina = hab && ficha).", "Series 1..20 y repeticiones 1..100; se guardan solo los días con ejercicios."],
  demo="Ana / Prof123 → Rutinas. Martín Ledesma (36.410.228, habilitado, sin rutina): cargar «Sentadilla» 4 × 10 → «Guardar y asignar rutina». Lucas Benítez (en mora) → «No se puede asignar rutina: cuota en mora…»."),
 "CU-24": dict(
  part="Pantalla: PantallaAgenda. Participantes: ClubViewModel (agenda); PantallaFichaNutricional para el detalle.",
  disp="La Nutricionista abre la pestaña Agenda.",
  reglas=["Se agrupan por día (hoy, mañana y dentro de 3 días) y se ordenan por hora.", "Un día sin turnos no se lista."],
  demo="Leandro / Nut123: hoy Sofía Acosta 16:30 y Julián Paredes 18:00; mañana Valentina Ruiz 17:00; dentro de 3 días Andrés Weitzel 17:30."),
 "CU-25": dict(
  part="Pantallas: PantallaFichas → PantallaFichaNutricional. Participantes: ClubViewModel, Validaciones, ClubRepositorio, ClubDbHelper (SQLite: socio.nut_peso, nut_altura, consultas_json, turno).",
  disp="La Nutricionista toca «Guardar en historial» (guardarConsulta()).",
  reglas=["Peso obligatorio 20..300 kg; altura opcional 0,50..2,50 m; acepta coma o punto.", "Sin observaciones se guarda «Control de rutina.».", "Si el turno era de hoy, queda consumido."],
  demo="Leandro / Nut123 → Fichas → Sofía Acosta (turno hoy 16:30): peso «57,4», observaciones «Bajó 800 g.» → IMC 21,6. Error: peso vacío → «Ingresá el peso.»"),
 "CU-26": dict(
  part="Pantallas: PantallaCarnet → PantallaQrIngreso (Screen.S_QR). Participantes: ClubViewModel, SocioVista (qrCarnet, estadoCorto, badge), composable CodigoQr (ZXing core).",
  disp="El Socio toca «QR de ingreso» en el carnet (vm.ir(Screen.S_QR)).",
  reglas=["El QR contiene texto plano con los datos del carnet (SocioVista.qrCarnet), de menos de 300 caracteres.", "El QR no habilita por sí mismo el ingreso: lo decide CU-09."],
  demo="Andres / Socio123 → Carnet → «QR de ingreso»: «Estado: Habilitado · Cuota pendiente», carnet que vence en 204 días."),
}

# correcciones verificadas contra el código (reemplazan el campo completo)
FIX = {
 "CU-01": dict(flujo=[
    "El usuario escribe usuario y contraseña (o toca una de las «Cuentas de prueba», que completa el formulario).",
    "Toca «Iniciar sesión» (login()).",
    "El sistema verifica alias normalizado y clave contra la tabla usuario (ClubRepositorio.autenticar → ClubDbHelper.autenticar).",
    "Deduce el rol de la cuenta; no hay selector de perfil.",
    "Guarda rol y pila en el estado (se persiste en la tabla sesion); LoginActivity abre la Activity del rol (abrirActivityDelRol) en su pantalla raíz: Inicio para Socio, Administrativo y Entrenadora; Agenda para la Nutricionista. Los campos de usuario y clave se limpian."]),
 "CU-03": dict(alt=["3a. La búsqueda o el filtro no coinciden con nadie → «Ningún socio coincide con la búsqueda.»"]),
 "CU-04": dict(alt=[
    "4a. Nombre vacío → «Ingresá el nombre.»; con caracteres no permitidos → «El nombre solo puede contener letras y espacios.»; menos de 2 letras → «El nombre es demasiado corto.»",
    "4b. Apellido: «Ingresá el apellido.» / «El apellido solo puede contener letras y espacios.» / «El apellido es demasiado corto.»",
    "4c. DNI vacío → «Ingresá el DNI.»; con letras → «El DNI solo puede contener números.»; distinto de 7 u 8 dígitos → «El DNI debe tener 7 u 8 dígitos.»",
    "4d. Correo vacío → «Ingresá el correo electrónico.»; formato inválido → «El correo electrónico no tiene un formato válido (ej.: nombre@dominio.com).»",
    "4e. DNI ya registrado (COUNT en SQLite) → «Ya existe un socio con ese DNI.»",
    "En todos los casos el error se muestra bajo el formulario (altaError) y se permanece en «Alta de socio»."]),
 "CU-07": dict(flujo=[
    "Toca «Registrar pago en caja» (pedirRegistrarPago()).",
    "El sistema verifica seleccionado.cuotaPagada; si no está pagada, toma el importe de SocioVista(socio, parametros).totalAdeudado: la cuota si no venció; cuota + recargo vigente hoy (una vez) + interés (por cada día desde el vencimiento, cuota × interés mensual vigente ese día / 100 / 30) si venció, en gracia o en mora.",
    "Muestra la hoja «Registrar pago» con Socio, Cuota (mes) e Importe (el total) y el botón «Cobrar $total»; el detalle «Cuota $x + recargo $y + interés $z» se ve en la tarjeta de la cuota de la ficha (deudaDetalle).",
    "Confirma. Se ejecuta pagarCuota(id, «Caja»): la cuota pasa a PAGADA, proximaVence = vence + 1 mes y se antepone PagoHistorial(mes, total, hoy, «Caja», comprobante «CD-2609-####»).",
    "El setter de estado guarda en SQLite (cuota, pago), se cierra la hoja y aparece «Pago registrado · socio habilitado»."]),
 "CU-09": dict(flujo=[
    "Escribe el DNI (con o sin puntos) y toca «Validar» (validarAcceso()), o toca un DNI de la fila «Probar:» (ponerDniAcceso()).",
    "El sistema valida el formato del DNI (Validaciones.dni).",
    "Busca el socio por DNI en el padrón del estado y arma SocioVista(socio, parametros) con los parámetros vigentes (sin consultar SQLite).",
    "Si el socio está habilitado, muestra «Permitido» (verde): con cuota pagada «Carnet vigente y cuota al día.»; con cuota pendiente «Carnet vigente · cuota {mes} pendiente, vence en N días.» (o «vence hoy»).",
    "La tarjeta de resultado ofrece «Ver ficha» y, si es Permitido y aún no se registró, «Registrar ingreso» (CU-10)."],
   alt=[
    "2a. DNI inválido → toast con el mensaje de validación («Ingresá el DNI.», «El DNI solo puede contener números.», «El DNI debe tener 7 u 8 dígitos.») y sin resultado.",
    "3a. No hay socio con ese DNI → «No encontrado»: «No hay ningún socio con DNI xx.xxx.xxx. Podés registrarlo como visitante o dar de alta un socio.»; la tarjeta ofrece el botón «Registrar socio» (CU-04).",
    "4a. (v1.5) 0 < d <= gracia → «Permitido» (naranja): «Cuota {mes} vencida · quedan N días de gracia. Debe $total en recepción.» (o «queda 1 día de gracia», «último día de gracia»).",
    "4b. (v1.5) d > gracia → «Denegado» (rojo): «Cuota {mes} en mora: vencida hace N días · gracia de G días superada. Debe $total en recepción.»",
    "4c. Carnet vencido (sin mora) → «Denegado»: «Carnet vencido el dd/mm/aaaa. Debe renovarse.» (CU-08). Si además hay mora, prevalece el mensaje de mora."]),
 "CU-12": dict(flujo=[
    "Abre la pestaña Reportes.",
    "Elige una pestaña (elegirReportTab()): «Vencen hoy» (cuotas PENDIENTES con vencimiento hoy, SELECT por rango en SQLite), «Esta semana» (de mañana a 7 días), «En mora» (socios en mora) o «Visitantes» (pagos diarios del día elegido).",
    "El sistema muestra las filas ordenadas por vencimiento (o por hora) y «Total»: suma de cuotas, de totalAdeudado en «En mora», o de pagos diarios.",
    "En «Visitantes» navega al día anterior / siguiente (diaReporteAnterior/Siguiente); no se puede avanzar más allá de hoy.",
    "(v1.5) En «En mora» cada fila muestra el total adeudado y una línea «Rige: gracia … recargo … interés … desde dd/mm/aaaa. El total suma cuota, recargo e interés.»; tocar una fila abre la ficha."],
   alt=["3a. Sin filas → «Sin registros en este reporte.» o «Sin pagos diarios el dd/mm/aaaa.»; total $0."]),
 "CU-15": dict(flujo=[
    "En el Inicio el sistema muestra el aviso de habilitación (badge) y la tarjeta «Cuota {mes}»: cápsula contadorCorto, línea cuotaSubLabel, deudaDetalle si la cuota venció y el total (totalLabel) con el botón «Pagar» si está pendiente.",
    "(v1.5) La cápsula cambia de tono: «Vence en N días» / «Vence hoy» (verde); «Quedan N días de gracia» / «Queda 1 día de gracia» / «Último día de gracia» (naranja); «Vencida hace N días» (rojo, con «Venció el … · gracia de G días superada»); «Pagada» con «Próxima cuota dd/mm/aaaa».",
    "Si la cuota venció, el total adeudado lleva el detalle «Cuota $x + recargo $y + interés $z».",
    "Toca «Ver estado de cuenta →» (PantallaCuotas): «Cuota actual», botón «Pagar cuota» si está pendiente y el Historial con «Pagada el … · medio · comprobante»."],
   alt=["2a. En mora el badge pasa a «No habilitado · En mora» y la cápsula a rojo hasta regularizar (CU-14 o CU-07).", "4a. Sin pagos → «Sin pagos registrados.»"]),
 "CU-16": dict(flujo=[
    "Toca la pestaña Carnet.",
    "El sistema muestra la tarjeta con formato de carnet «Club Deportivo»: chip de estado (Vigente / Por vencer si faltan menos de 30 días / Vencido), nombre, DNI, Socio N°, Plan y antigüedad, foto, «Vigencia emitido – vence» y el pie «Cuota al día / en mora / en período de gracia / pendiente».",
    "Debajo, el botón «QR de ingreso» (vm.ir(Screen.S_QR), CU-26), la leyenda «Mostrá tu DNI o el QR de ingreso en recepción…» y «Últimos ingresos»."],
   alt=["2a. Carnet vencido → chip «Vencido» en rojo; el socio debe renovarlo en recepción (CU-08).", "3a. Sin ingresos → «Todavía no registraste ingresos.»"]),
 "CU-17": dict(alt=["2a. Sin rutina asignada → «Todavía no tenés una rutina asignada. Tu profesor la va a cargar en tu próxima clase.» (en el Inicio, la tarjeta dice «Sin rutina» · «Pedile a tu profe»)."]),
 "CU-18": dict(alt=["2a. Los horarios tomados por otro socio no se pueden elegir.", "3a. «Confirmar turno» está deshabilitado mientras no haya horario elegido; si igual se invoca confirmarTurno() sin slot, toast «Elegí un horario disponible»."]),
 "CU-21": dict(flujo=["En el Inicio la tarjeta «Mi asistencia de hoy» muestra «Sin marcar» y el botón «Marcar presencia» (marcarPresencia()).", "El sistema registra profePresente = true, lo persiste (tabla sesion) y muestra «Presencia registrada · hh:mm»; la tarjeta pasa a «Presente» y el botón desaparece."],
   alt=["1a. Si ya está marcada, no hay botón: no se puede marcar dos veces desde la pantalla."]),
 "CU-23": dict(alt=[
    "2a. El socio no está habilitado (cuota en mora o carnet vencido) o no tiene apto físico → el editor muestra «No se puede asignar rutina: {motivo}. El socio debe regularizar la situación en recepción.» (motivos: «cuota en mora», «carnet vencido», «falta la ficha médica») y no ofrece formulario.",
    "3a. Nombre del ejercicio vacío → toast «Ingresá el nombre del ejercicio»; series o repeticiones no enteras o fuera de rango → «Las series deben estar entre 1 y 20.» / «Las repeticiones deben estar entre 1 y 100.» (o «… deben ser un número entero.»)",
    "4a. Ningún día con ejercicios → toast «Agregá al menos un ejercicio»."]),
 "CU-25": dict(alt=[
    "3a. Peso vacío → toast «Ingresá el peso.»; no numérico → «El peso debe ser un número (ej.: 62,4).»; fuera de rango → «El peso debe estar entre 20 y 300 kg.»",
    "3b. Altura no numérica → «La altura debe ser un número en metros (ej.: 1,68).»; fuera de rango → «La altura debe estar entre 0,50 y 2,50 m.»"]),
 "CU-26": dict(flujo=[
    "Toca «QR de ingreso» en el carnet digital (vm.ir(Screen.S_QR)); la pantalla se apila sobre el carnet con flecha para volver.",
    "El sistema muestra nombre, «N° · DNI · Plan», la cápsula estadoCorto y, más abajo, «Carnet vigente · vence dd/mm/aaaa» con el badge del socio.",
    "Dibuja el código QR (composable CodigoQr, ZXing core, sin cámara) cuyo contenido es SocioVista.qrCarnet: «CLUB DEPORTIVO · CARNET DE SOCIO», N° de socio, Socio, DNI, Plan, Carnet emitido, Carnet vence y Estado.",
    "Toca la flecha o el botón atrás (volver()) para regresar al carnet."]),
}

# trazabilidad CU -> pruebas reales (nombre exacto; clase entre paréntesis si no es ClubViewModelTest)
PRUEBAS = {
 "CU-01": ["HU-01 - una credencial invalida no abre sesion", "HU-01 - el usuario se normaliza y el rol sale de la cuenta", "HU-01 - cada rol aterriza en su propia pantalla", "cada rol abre su Activity (NavegacionActivitiesTest)"],
 "CU-02": ["HU-01 - cerrar sesion vuelve al login", "cerrar sesion la cierra para la proxima Activity (NavegacionActivitiesTest)"],
 "CU-03": ["los filtros y la busqueda del listado de socios"],
 "CU-04": ["HU-02 - el alta valida nombre, DNI y duplicados", "HU-02 - el alta rechaza formatos incorrectos y datos faltantes", "HU-02 - el alta genera numero de socio, carnet y cuota inicial", "nombre y apellido por separado (ValidacionesTest)"],
 "CU-05": ["editar precarga los datos y guarda los cambios (CrudSociosTest)", "editar valida igual que el alta y no choca con su propio DNI (CrudSociosTest)"],
 "CU-06": ["la baja pide confirmacion y quita al socio de clases y asistencias (CrudSociosTest)", "un alta despues de una baja no repite el numero de socio (CrudSociosTest)", "el socio dado de baja no puede iniciar sesion (CrudSociosTest)"],
 "CU-07": ["HU-04 - el cobro en caja habilita al socio en mora", "HU-04 - el cobro en caja abre la cuota siguiente y deja comprobante", "HU-04 - una cuota pagada no se cobra dos veces", "el recargo y el interes vigentes se suman a la deuda del moroso"],
 "CU-08": ["HU-07 - renovar el carnet lo emite por doce meses y habilita"],
 "CU-09": ["HU-05 - el control de acceso resuelve los cuatro casos", "la habilitacion combina mora y vigencia del carnet", "la gracia configurada decide si la cuota vencida bloquea", "la demo trae un socio en gracia con el carnet por vencer"],
 "CU-10": ["HU-05 - registrar el ingreso lo agrega al dia y al historial del socio"],
 "CU-11": ["HU-03 - sin cupo el ingreso de visitante se bloquea", "HU-03 - con cupo se registra el pago diario con fecha y medio", "HU-03 - el cupo de hoy cuenta a los visitantes de hoy", "HU-03 - un DNI ya cobrado hoy no se registra dos veces"],
 "CU-12": ["HU-14 - vencen hoy, esta semana y mora salen de pestanias distintas", "HU-14 - los pagos diarios se consultan por fecha", "HU-14 - cobrar la cuota que vence hoy la saca del listado", "HU-14 - los ingresos de hoy cuentan solo a los visitantes de hoy"],
 "CU-13": ["los parametros guardan su vigencia y el interes se calcula por tramos", "los parametros rechazan valores que no son numeros finitos"],
 "CU-14": ["HU-04 - el pago simulado deja la cuota paga y emite comprobante", "HU-04 - una cuota pagada no se cobra dos veces"],
 "CU-15": ["las etiquetas de vencimiento se redactan segun los dias restantes", "las capsulas de estado y de cuota resumen la situacion en pocas palabras", "el detalle de la deuda siempre suma el total aunque haya centavos"],
 "CU-16": ["la demo trae un socio en gracia con el carnet por vencer (asserta carnetEstado «Por vencer»)", "HU-07 - renovar el carnet lo emite por doce meses y habilita"],
 "CU-17": ["HU-09 - crear y asignar una rutina (verifica la rutina asignada; la pantalla del socio no tiene prueba propia)"],
 "CU-18": ["HU-11 - cancelar y volver a pedir turno"],
 "CU-19": ["HU-11 - cancelar y volver a pedir turno"],
 "CU-20": ["HU-12 - registrar una consulta actualiza la ficha y consume el turno (IMC e historial)", "HU-02 - el alta genera numero de socio, carnet y cuota inicial (IMC «—»)"],
 "CU-21": ["HU-08 - la profesora marca su propia presencia"],
 "CU-22": ["HU-08 - asistencia por clase"],
 "CU-23": ["HU-09 - crear y asignar una rutina", "HU-09 - la rutina se bloquea por mora o por falta de apto fisico", "series y repeticiones (ValidacionesTest)"],
 "CU-24": ["HU-12 - la agenda ordena los turnos del dia"],
 "CU-25": ["HU-12 - registrar una consulta actualiza la ficha y consume el turno", "peso obligatorio y en rango, con coma o punto (ValidacionesTest)", "altura opcional pero en rango (ValidacionesTest)"],
 "CU-26": ["el QR de ingreso es una pantalla propia con los datos del carnet", "el QR del carnet se decodifica con los datos del carnet (QrCarnetTest)"],
}

A_CONFIRMAR = [
 "CU-21: la tarjeta de la Entrenadora muestra el texto fijo «Presente · 09:41» mientras el toast usa la hora real (HORA_ACTUAL). No hay spec que diga cuál debe mostrarse.",
 "CU-23: el motivo «falta la ficha médica» no se alcanza con los datos de demo desde la lista de la Entrenadora (los socios sin apto, como Valentina Ruiz, no cursan Musculación ni Funcional); solo lo ejercita la prueba «HU-09 - la rutina se bloquea por mora o por falta de apto fisico».",
 "CU-13: no hay historia de usuario propia en el README; se trata como regla de negocio de HU-04 y HU-05.",
 "CU-16, CU-17, CU-20: las pantallas del Socio no tienen prueba automatizada propia (solo se cubren las reglas de SocioVista y del ViewModel que usan).",
 "CU-14 / CU-18: las guardas del ViewModel (cuota ya pagada, turno sin horario) no son alcanzables desde la pantalla porque el botón no se muestra o está deshabilitado; se documentan como defensas del ViewModel.",
 "CU-09: el texto de «No encontrado» sugiere registrar al visitante, pero la tarjeta solo ofrece el botón «Registrar socio»; no hay atajo a CU-11.",
 "CU-12: las listas «Vencen hoy» / «Esta semana» filtran por estado PENDIENTE guardado; una cuota guardada como VENCIDA (Lucas y Tomás en la semilla) aparece en «En mora» y no en esas listas. Es coherente, pero el spec no lo explicita.",
 "Importes de la demo: dependen de la fecha del dispositivo; las pruebas fijan el 08/09/2026 (con esa fecha Tomás Giménez debe $18.520). Los de Lucas ($12.848) y Rocío ($12.536) no cambian con la fecha mientras rijan los parámetros de la semilla.",
]
