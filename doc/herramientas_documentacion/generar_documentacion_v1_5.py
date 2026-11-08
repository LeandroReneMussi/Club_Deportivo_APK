# -*- coding: utf-8 -*-
"""Genera entrega/DAM_Entrega_GRUPO3_..._Documentacion_v1.5.docx y .pdf.

Uso: python generar_documentacion_v1_5.py   (desde cualquier carpeta)
Las cifras (pruebas, capturas, version) se leen del repositorio, no se escriben a mano.
"""
import os
import re
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from docgen import Doc  # noqa: E402

RAIZ = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
ENTREGA = os.path.join(RAIZ, "entrega")
PLANTILLA = os.path.join(ENTREGA, "Documentacion_ClubDeportivo_v1.2.docx")
NOMBRE = "DAM_Entrega_GRUPO3_ALVAREZ_AQUINO_MUSSI_WEITZEL_Documentacion_v1.5"
SALIDA = os.path.join(ENTREGA, NOMBRE + ".docx")
SOPLA = r"C:\Program Files\LibreOffice\program\soffice.exe"
CAPT_ORIG = os.path.join(ENTREGA, "capturas")


def _capturas_reducidas():
    """Copia las capturas a 720x1600 px (suficiente para 4,6 cm a 400 dpi) para que el docx no pese de mas."""
    import tempfile
    from PIL import Image
    destino = os.path.join(tempfile.gettempdir(), "capturas_doc_v15")
    os.makedirs(destino, exist_ok=True)
    for f in os.listdir(CAPT_ORIG):
        if f.endswith(".png"):
            out = os.path.join(destino, f[:-4] + ".jpg")
            if not os.path.exists(out) or os.path.getmtime(out) < os.path.getmtime(os.path.join(CAPT_ORIG, f)):
                im = Image.open(os.path.join(CAPT_ORIG, f)).convert("RGB")
                im.thumbnail((720, 1600), Image.LANCZOS)
                im.save(out, quality=90, optimize=True)
    return destino


CAPT = CAPT_ORIG
def _diagrama():
    import tempfile
    import zipfile
    destino = os.path.join(tempfile.gettempdir(), "diagrama_navegacion_v12.png")
    with zipfile.ZipFile(PLANTILLA) as z:
        open(destino, "wb").write(z.read("word/media/image1.png"))
    return destino


DIAGRAMA = _diagrama()  # diagrama de navegacion de la v1.2 (sigue vigente)

# ------------------------------------------------------------------ datos leidos del repo
def contar_tests():
    res = {}
    for base, tag in (("test", "unit"), ("androidTest", "instr")):
        carpeta = os.path.join(RAIZ, "app", "src", base, "java", "ar", "edu", "ifts29", "clubdeportivo")
        for f in sorted(os.listdir(carpeta)):
            if f.endswith(".kt"):
                n = len(re.findall(r"^\s*@Test", open(os.path.join(carpeta, f), encoding="utf8").read(), flags=re.M))
                res[f[:-3]] = (tag, n)
    return res


TESTS = contar_tests()
N_UNIT = sum(n for t, n in TESTS.values() if t == "unit")
N_INSTR = sum(n for t, n in TESTS.values() if t == "instr")
gradle = open(os.path.join(RAIZ, "app", "build.gradle.kts"), encoding="utf8").read()
VERSION = re.search(r'versionName = "([^"]+)"', gradle).group(1)
VCODE = re.search(r"versionCode = (\d+)", gradle).group(1)
assert VERSION == "1.5.1", VERSION
indice = [l.rstrip("\n").split("|") for l in open(os.path.join(CAPT, "indice.txt"), encoding="utf8") if l.strip()]
N_CAPT = len(indice)
assert N_CAPT == 72, N_CAPT
CAPT_RED = _capturas_reducidas()
CASOS = "doc/casos-de-uso/DAM_Entrega_GRUPO3_ALVAREZ_AQUINO_MUSSI_WEITZEL_Casos_de_uso_UML.pdf"

doc = Doc(PLANTILLA, "Club Deportivo — App Android v1.5.1 — Documentación general de la Etapa Entrega")

# ------------------------------------------------------------------ portada
doc.vacio(5)
doc.centro("Desarrollo de Aplicaciones Móviles", 36, b=True, color="1F3A5F", after=120)
doc.centro("Desarrollo Mobile", 28, b=False, after=120)
doc.centro("Comisión 2° D", 26, b=False, after=120)
doc.centro("Grupo 3", 28, b=True, after=360)
for n in ("Ana Alvarez", "Jorge Aquino Lesme", "Leandro Mussi", "Andrés Weitzel"):
    doc.centro(n, 24, b=False)
doc.vacio(4)
doc.centro("Club Deportivo — Aplicación Android", 26, b=True, after=80)
doc.centro("Etapa Entrega", 32, b=True, color="1F3A5F")
doc.centro("Documentación general del proyecto", 32, b=True, color="1F3A5F", after=240)
doc.centro("Versión de la app 1.5.1 — 2026-11-08", 22, b=False)
doc.centro("Apellidos: Alvarez · Aquino Lesme · Mussi · Weitzel", 22, b=False)
doc.salto()

# ------------------------------------------------------------------ indice (se rellena al final)
doc.h1("Índice")
doc.headings.pop()  # el titulo del indice no se lista a si mismo
doc.parrafo(
    "Entregables que acompañan a este documento (Etapa Entrega, v1.5.1): carpeta comprimida del proyecto de Android Studio "
    "(DAM_Entrega_GRUPO3_ALVAREZ_AQUINO_MUSSI_WEITZEL_ProyectoAndroidStudio_v1.5.1.zip), APK de la aplicación (ClubDeportivo-v1.5.1-debug.apk), "
    "PDF de capturas, casos de uso y diagramas UML 2.0, diagramas de contexto y de casos de uso (sección 15), y video de demostración. El estado de cada uno figura en la sección «A confirmar».",
    jc="left", after=200)
doc.marcar_indice()
doc.salto()

# ================================================================== PARTE I
doc.parte("I", "Video de demostración del proyecto")
doc.h1("1. Video de demostración del proyecto")
doc.parrafo(
    "Video del grupo con el funcionamiento de la aplicación v1.5.1 y la explicación de las funcionalidades de la Etapa Entrega "
    "(enlace de YouTube «No listado»). Según entrega/README_entrega_v1.5.md, el video está pendiente de grabación; el guion y el checklist "
    "están en entrega/Checklist_video_v1.5.md y entrega/Guion_presentacion_por_rol_v1.5.md.")
doc.boton("VIDEO v1.5.1 — ENLACE A CONFIRMAR")
doc.centro("Enlace: a confirmar (ver «A confirmar», punto 1)", 22, b=False)
doc.parrafo(
    "Video de la etapa anterior (v1.2.0, Etapa Desarrollo): https://www.youtube.com/watch?v=bwIpHXsPboc", jc="center")
doc.salto()

# ================================================================== PARTE II
doc.parte("II", "Alcance, navegación, persistencia, reglas de negocio y capturas por Activity")

# ---------------------------------------------------------------- 2. alcance
doc.h1("2. Alcance y cobertura de las historias de usuario")
doc.parrafo(
    f"La app Android del Club Deportivo (versión {VERSION}, versionCode {VCODE}; minSdk 24, targetSdk y compileSdk 35) implementa las 14 historias de usuario "
    "del documento de análisis, con la dirección visual «1a Pista», a partir del prototipo web/index.html (ClubDeportivo-App-v4). "
    "Desde la v1.3.0 guarda los datos en SQLite en el dispositivo (club_deportivo.db): el alta, la modificación, la baja, el cobro de la cuota y los listados "
    "se escriben en la base y siguen ahí al cerrar la app. La versión web conserva los datos en memoria, como el prototipo original. "
    "No hay backend ni pasarela de pago: el pago se registra en la base (README.md).")
doc.parrafo(
    "Hay cuatro cuentas de prueba (una por rol). El código define cinco valores de rol (SOCIO, EMPLEADO, ADMIN, PROFESOR, NUTRICIONISTA); "
    "EMPLEADO y ADMIN abren la misma AdminActivity (RolActivity.kt).")
doc.h2("2.1 Historias de usuario y pantallas")
doc.tabla(
    ["HU", "Qué", "Pantalla (función Compose)", "Novedades v1.3–v1.5"],
    [
        ["HU-01", "Inicio de sesión por rol", "PantallaLogin; autenticación contra la tabla usuario", "El login consulta SQLite (autenticar)"],
        ["HU-02", "Registrar socio (carnet, cuota inicial, ficha médica)", "PantallaAlta (alta y edición)", "v1.5: nombre y apellido en campos separados; DNI único por COUNT"],
        ["HU-03", "Visitantes: pago diario con fecha, medio y cupo del día", "PantallaVisitantes, PantallaVisitanteNuevo", "v1.4: fecha y medio de pago, un pase por DNI y día"],
        ["HU-04", "Consultar y pagar la cuota", "PantallaCuotas, PantallaPagar, PantallaPagoOk, ficha del socio", "v1.4: cobro en caja igual al pago por app, sin doble cobro; v1.5: gracia, recargo e interés"],
        ["HU-05", "Control de acceso por DNI", "PantallaAcceso", "v1.5: período de gracia configurable"],
        ["HU-06", "Carnet digital", "PantallaCarnet, PantallaQrIngreso (Screen.S_QR)", "v1.5: carnet con formato de tarjeta y QR de ingreso"],
        ["HU-07", "Emitir / renovar carnet", "PantallaFichaSocio", "—"],
        ["HU-08", "Registrar asistencia", "PantallaAsistencia, PantallaProfesorHome", "—"],
        ["HU-09", "Crear y asignar rutina, con validación", "PantallaRutinasProfesor, PantallaRutinaEdit", "—"],
        ["HU-10", "Consultar rutina asignada", "PantallaRutinaSocio", "—"],
        ["HU-11", "Turnos de nutrición", "PantallaTurno, PantallaAgenda", "—"],
        ["HU-12", "Ficha nutricional", "PantallaFichas, PantallaFichaNutricional", "—"],
        ["HU-13", "Información nutricional propia", "PantallaNutriSocio", "—"],
        ["HU-14", "Reportes", "PantallaReportes (4 pestañas), PantallaEmpleadoHome (KPIs)", "v1.4: vencen hoy, esta semana, en mora y pagos diarios por fecha; v1.5: deuda con recargo e interés"],
    ],
    [700, 2250, 3121, 3000], sz=18)
doc.h2("2.2 Mínimos de la consigna de la Etapa Entrega")
doc.parrafo(
    "Correspondencia entre los seis mínimos de la consigna y las pantallas y funciones reales (README.md y entrega/README_entrega_v1.5.md, verificados contra el código).")
doc.tabla(
    ["Mínimo de la consigna", "Pantalla y función en la app", "Prueba que lo verifica"],
    [
        ["1. Registrar socios y no socios",
         "Administrativo → Socios → «+ Alta» (PantallaAlta; ClubViewModel.confirmarAlta): nombre, apellido, DNI, correo, plan y ficha médica. "
         "No socios: Visitantes → «+ Ingreso» (PantallaVisitanteNuevo; confirmarVisitante).",
         "ClubViewModelTest: HU-02 (3), HU-03 (4); CrudSociosTest"],
        ["2. Pago de la cuota mensual",
         "Socio: Estado de cuenta → Pagar cuota (PantallaPagar; confirmarPago). Administrativo: ficha del socio → Registrar pago (pedirRegistrarPago). "
         "Ambos cobran SocioVista.totalAdeudado (cuota + recargo + interés si venció).",
         "ClubViewModelTest: HU-04 (4), recargo e interés"],
        ["3. Pago diario de no socios",
         "Visitantes → «+ Ingreso»: actividad, medio de pago (efectivo o débito), control de cupo del día y de DNI repetido en el día; Reportes → Visitantes por fecha.",
         "ClubViewModelTest: HU-03 (4), HU-14; ClubDbHelperTest: losPagosDiariosSeConsultanPorFecha, elDniRepetidoSeCuentaSoloEnSuDia"],
        ["4. Consultar la información",
         "Socios (búsqueda y filtros Todos / Habilitados / En mora), ficha del socio, Reportes, Inicio con indicadores y Perfil del administrador con parámetros e historial.",
         "ClubViewModelTest: los filtros y la búsqueda del listado de socios; HU-14"],
        ["5. Listar socios cuya cuota vence en la fecha",
         "Reportes → «Vencen hoy» y KPI «Vencen hoy» del Inicio; consulta SQL por rango de fechas (ClubDbHelper.idsCuotasPorVencer).",
         "ClubViewModelTest: HU-14 (vencen hoy, esta semana y mora; cobrar la cuota que vence hoy); ClubDbHelperTest: elListadoDeVencimientosFiltraPorRangoDeFechas"],
        ["6. Validaciones",
         "Alta y edición (obligatorios, solo letras, DNI de 7 u 8 dígitos y único, correo con formato), visitantes (cupo, DNI repetido), "
         "parámetros del club (rangos), pagos (no se cobra dos veces), rutinas (series 1–20, repeticiones 1–100), consulta nutricional (peso 20–300 kg, altura 0,50–2,50 m) y control de acceso.",
         "ValidacionesTest (7); ClubViewModelTest: HU-02, HU-03, HU-04"],
    ],
    [1900, 4571, 2600], sz=18)
doc.h2("2.3 Fuera de alcance")
doc.vineta("Backend, sincronización entre dispositivos y pasarela de pago: el pago se registra en la base local.")
doc.vineta("Lectura de QR: la app genera el QR del carnet (librería ZXing, solo codifica; no usa la cámara). El control de acceso del administrativo se hace por DNI.")
doc.vineta("La versión web (web/index.html) no usa SQLite: sus datos viven en memoria.")
doc.vineta("Publicación en tiendas de aplicaciones. El APK se firma con la clave de debug (app/build.gradle.kts).")

# ---------------------------------------------------------------- 3. navegacion
doc.h1("3. Navegación y entry point")
doc.parrafo(
    "La aplicación tiene cinco Activities. LoginActivity es el entry point (LAUNCHER): valida las credenciales contra SQLite, abre la Activity del rol y se cierra, "
    "de modo que el botón atrás no vuelve al login con la sesión abierta. «Cerrar sesión» vuelve a LoginActivity. Las Activities de rol no se exportan "
    "(android:exported=\"false\") y, si se abren sin una sesión de su rol, redirigen al login (RolActivity.kt).")
if DIAGRAMA:
    doc.imagen(DIAGRAMA, 5760000, 3240000)
doc.tabla(
    ["Activity", "Rol", "Cuenta de prueba", "Pantallas"],
    [
        ["LoginActivity", "—", "—", "Inicio de sesión"],
        ["SocioActivity", "Socio", "Andres / Socio123", "Inicio, Carnet digital, QR de ingreso, Estado de cuenta, Pagar cuota, Pago registrado, Mi rutina, Nutrición, Solicitar turno, Perfil"],
        ["AdminActivity", "Administrativo", "Jorge / Admin123", "Inicio, Socios, Ficha del socio, Alta de socio, Editar socio, Control de acceso, Visitantes, Ingreso de visitante, Reportes, Perfil (con Configuración del club)"],
        ["EntrenadoraActivity", "Entrenadora", "Ana / Prof123", "Inicio, Asistencia, Rutinas, Rutina del socio, Perfil"],
        ["NutricionistaActivity", "Nutricionista", "Leandro / Nut123", "Agenda, Fichas nutricionales, Ficha nutricional, Perfil"],
    ],
    [2041, 1474, 1814, 3742])
doc.parrafo(
    "Dentro de cada Activity, ClubApp (ui/ClubApp.kt) despacha la pantalla activa con un when sobre el enum Screen (27 valores). "
    "La barra inferior muestra las pestañas del rol (DemoData.tabs); las pantallas raíz reemplazan la pila y el resto se apila con un header con flecha cuyo título sale de DemoData.titulos. "
    "El botón atrás del sistema desapila igual (BackHandler). La barra se oculta en el login y en el comprobante de pago. "
    "La pantalla nueva de la v1.5 es Screen.S_QR («QR de ingreso»), que se abre desde el Carnet digital.")
doc.tabla(
    ["Rol", "Pestañas de la barra inferior (DemoData.tabs)", "Sub-pantallas con título (DemoData.titulos)"],
    [
        ["Socio", "Inicio, Carnet, Rutina, Nutrición, Perfil", "QR de ingreso, Estado de cuenta, Pagar cuota, Solicitar turno"],
        ["Administrativo", "Inicio, Socios, Acceso, Visitantes, Reportes (Perfil se abre desde el avatar)", "Ficha del socio, Alta de socio, Editar socio, Ingreso de visitante, Perfil"],
        ["Entrenadora", "Inicio, Asistencia, Rutinas, Perfil", "Rutina del socio"],
        ["Nutricionista", "Agenda, Fichas, Perfil", "Ficha nutricional"],
    ],
    [1500, 4000, 3571])
doc.parrafo(
    "Los datos se guardan en SQLite (ClubDbHelper) y ClubRepositorio los comparte entre Activities: un socio dado de alta por el administrativo aparece en las rutinas de la entrenadora. "
    "La primera vez se cargan los datos de demostración, que se generan relativos a la fecha de hoy.")

# ---------------------------------------------------------------- 4. persistencia
doc.h1("4. Persistencia en SQLite")
doc.parrafo(
    "La base es club_deportivo.db, administrada por ClubDbHelper (SQLiteOpenHelper, data/db/ClubDbHelper.kt), con claves foráneas activadas. "
    "La versión actual del esquema es la 4 (ClubDbHelper.VERSION = 4). Se sigue el estilo del ABM visto en el curso: COUNT antes de aceptar un alta, rawQuery con cursor y listados con ORDER BY.")
doc.h2("4.1 Tablas")
doc.tabla(
    ["Tabla", "Qué guarda", "Detalle"],
    [
        ["plan", "Planes de cuota", "nombre (PK), precio: Básico $12.000, Full $18.500, Familiar $29.000"],
        ["usuario", "Cuentas de la app", "alias (PK), clave, rol, socio_id; es lo que consulta autenticar"],
        ["socio", "Padrón de socios", "id, nombre (nombre y apellido juntos), dni UNIQUE, nro, email, plan, alta, nac, ficha médica, datos nutricionales, turno; rutina, consultas, actividades e ingresos como JSON"],
        ["carnet", "Carnet del socio", "socio_id (FK, ON DELETE CASCADE), emitido, vence"],
        ["cuota", "Cuota vigente y la siguiente", "socio_id (FK), mes, monto, vence, estado, vigente (1 = actual, 0 = siguiente), proxima_vence"],
        ["pago", "Historial de pagos de cuota", "socio_id (FK), mes, monto, pagada, medio, comprobante"],
        ["actividad / actividad_inscripto", "Actividades, cupo y ocupación; inscriptos", "id, nombre, horario, cupo, ocupados, precio, profesor"],
        ["visitante", "Pagos diarios de no socios", "nombre, dni, actividad, hora, monto, fecha, medio (v3)"],
        ["ingreso_hoy", "Ingresos registrados en el control de acceso", "hora, nombre, estado, ok"],
        ["asistencia_clase / asistencia", "Presentes por clase", "clase_id, socio_id"],
        ["sesion", "Rol con sesión abierta y presencia de la profesora", "fila única (id = 1)"],
        ["parametro", "Parámetros del club con vigencia (v4)", "gracia_dias, recargo, interes_mensual, vigente_desde, orden"],
    ],
    [2200, 2600, 4271], sz=18)
doc.parrafo(
    "ClubRepositorio escribe con ClubDbHelper.guardar, una transacción que reescribe las tablas con el estado de la app cada vez que cambia, y lee con leer al abrir. "
    "La primera ejecución carga la semilla (estaVacia) y completarCuotasSiguientes agrega la fila de la cuota siguiente a las cuotas ya pagadas que no la tienen. "
    "Las pruebas unitarias no abren la base y usan la copia en memoria.")
doc.h2("4.2 Migraciones")
doc.tabla(
    ["Versión", "onUpgrade", "Qué cambia"],
    [
        ["1 → 2", "oldVersion < 2", "cuota.proxima_vence y pago.comprobante"],
        ["2 → 3 (v1.4.0)", "oldVersion < 3", "visitante.fecha y visitante.medio; los pagos diarios ya cargados quedan como de hoy y en efectivo; se descuenta de actividad.ocupados lo que la 1.3.x ya había sumado por visitante, porque desde la 1.4.0 el cupo del día se deriva de los visitantes de hoy"],
        ["3 → 4 (v1.5.0)", "oldVersion < 4", "Crea la tabla parametro (CREATE TABLE IF NOT EXISTS). Sin filas, el repositorio usa los valores iniciales hasta el primer guardado"],
    ],
    [1700, 1700, 5671], sz=18)
doc.h2("4.3 Qué se guarda y qué consultas son en vivo")
doc.parrafo(
    "El estado completo de la app se persiste con guardar. Cuatro listados y comprobaciones no se calculan sobre la copia en memoria sino con una consulta SQL en el momento, a través de ClubRepositorio:")
doc.tabla(
    ["Consulta", "SQL", "Se usa en"],
    [
        ["idsCuotasPorVencer(desde, hasta)", "SELECT s.id FROM socio s INNER JOIN cuota c ON c.socio_id = s.id AND c.vigente = 1 WHERE c.estado = ? AND c.vence >= ? AND c.vence <= ? ORDER BY c.vence, s.nombre", "Reportes «Vencen hoy» (ambas cotas iguales) y «Esta semana»; KPIs del Inicio (consigna, punto 5)"],
        ["visitantesPorFecha(fecha)", "SELECT * FROM visitante WHERE fecha = ? ORDER BY orden", "Reportes → Visitantes, con selector de fecha"],
        ["contarVisitante(dni, fecha)", "SELECT COUNT(*) FROM visitante WHERE dni = ? AND fecha = ?", "Validación: un pase diario por DNI y por fecha"],
        ["contarDni(dni, exceptoId)", "SELECT COUNT(dni) FROM socio WHERE dni = ? [AND id <> ?]", "Validación de alta y edición: DNI único"],
        ["autenticar(alias, clave)", "SELECT rol FROM usuario WHERE alias = ? AND clave = ?", "Inicio de sesión (HU-01)"],
        ["contarAlias(alias)", "SELECT COUNT(alias) FROM usuario WHERE alias = ?", "Alias único de los usuarios"],
    ],
    [2300, 4271, 2500], sz=17)
doc.parrafo("Sin base abierta (pruebas unitarias), cada una de estas funciones usa la copia en memoria y los resultados son equivalentes.")

# ---------------------------------------------------------------- 5. CRUD
doc.h1("5. Operaciones CRUD por entidad")
doc.parrafo("✔ implementada en la interfaz · — no aplica. Todas las operaciones persisten en SQLite.")
doc.tabla(
    ["Entidad", "Crear", "Leer", "Actualizar", "Eliminar"],
    [
        ["Socio", "✔ Alta de socio (nombre y apellido separados, DNI, correo, plan, apto físico)", "✔ Padrón, búsqueda, filtros, ficha", "✔ Editar socio (formulario precargado; el cambio de plan rige desde la próxima cuota)", "✔ Dar de baja, con confirmación; lo quita de clases y asistencias"],
        ["Cuota", "✔ Cuota inicial al dar de alta; la siguiente se registra al pagar", "✔ Estado de cuenta, Reportes (vencen hoy, esta semana, en mora)", "✔ Pago: queda pagada", "— (se conserva como historial)"],
        ["Pago de cuota", "✔ Pago en la app y cobro en caja (cuota + recargo + interés), con comprobante", "✔ Historial de pagos", "— (una cuota pagada no se cobra dos veces)", "—"],
        ["Carnet", "✔ Emitido en el alta", "✔ Carnet digital y QR de ingreso", "✔ Renovar / reemitir (12 meses)", "—"],
        ["Visitante / pago diario", "✔ Ingreso con cobro, fecha y medio de pago", "✔ Pagos de hoy y por fecha (Reportes)", "—", "—"],
        ["Actividad / cupo", "—", "✔ Cupos del día", "✔ Ocupación derivada de los visitantes de hoy", "—"],
        ["Parámetros del club", "✔ Guardar gracia, recargo e interés (vigencia desde hoy)", "✔ Vigentes e historial por fecha", "✔ Guardar de nuevo el mismo día reemplaza la fila de hoy", "— (el historial se conserva)"],
        ["Rutina", "✔ Crear rutina", "✔ Mi rutina, listado de la entrenadora", "✔ Agregar ejercicios", "✔ Quitar ejercicios"],
        ["Turno de nutrición", "✔ Reservar turno", "✔ Agenda, próximo turno", "—", "✔ Cancelar turno"],
        ["Consulta / ficha nutricional", "✔ Registrar consulta", "✔ Ficha e historial", "✔ Peso y altura con cada consulta", "—"],
        ["Asistencia", "✔ Marcar presencia y asistentes", "✔ Clases del día", "✔ Presente / ausente", "—"],
        ["Ingreso al club", "✔ Registrar ingreso (control de acceso)", "✔ Ingresos de hoy", "—", "—"],
    ],
    [1250, 2150, 1800, 1971, 1900], sz=17)

# ---------------------------------------------------------------- 6. reglas
doc.h1("6. Reglas de negocio de la v1.5.0")
doc.h2("6.1 Fecha real del dispositivo")
doc.parrafo(
    "Reloj (data/Format.kt) entrega HOY: la fecha y la hora reales del dispositivo. Las pruebas la fijan con Reloj.fijar en el martes 8 de septiembre de 2026, el «hoy» del prototipo. "
    "Los datos de demo (DemoData) se expresan como desplazamientos desde hoy, de modo que la historia (socio en mora, cuota por vencer, socia en gracia) vale cualquier día; por eso las fechas de las capturas no coinciden con las de una ejecución nueva.")
doc.h2("6.2 Cuota pendiente, en período de gracia y en mora")
doc.parrafo("SocioVista (vm/SocioVista.kt) deriva el estado de la cuota por fecha, con el parámetro vigente hoy:")
doc.tabla(
    ["Estado", "Condición", "Acceso y color"],
    [
        ["Pendiente", "Cuota sin pagar y sin vencer", "Habilitado, verde (TonoCuota.OK)"],
        ["En período de gracia", "Vencida y diasVencida ≤ días de gracia (enGracia)", "Habilitado con aviso naranja (TonoCuota.AVISO): «Habilitado · En período de gracia», con los días de gracia que quedan"],
        ["Mora", "Vencida y diasVencida > días de gracia (mora)", "No habilitado, rojo (TonoCuota.BLOQUEO): acceso denegado, no se asigna rutina"],
        ["Carnet vencido", "carnetVence anterior a hoy", "No habilitado, independiente de la cuota"],
    ],
    [1900, 3371, 3800])
doc.parrafo("La regla central: hab = !mora && !carnetVencido. El contador de días muestra «Vence en N días», «Vence hoy», «Quedan N días de gracia», «Último día de gracia» o «Vencida hace N días» con el color de su tono.")
doc.h2("6.3 Recargo e interés por tramos de vigencia")
doc.parrafo(
    "El administrador fija en Perfil → Configuración del club los días de gracia (0 a 60), el recargo fijo (0 a 1.000.000) y el interés mensual (0 a 100 %). "
    "Cada guardado rige desde hoy (Parametros.vigenteDesde); guardar dos veces el mismo día reemplaza la fila de hoy y el resto del historial se conserva en la tabla parametro.")
doc.vineta("El recargo es un monto fijo, se debe una sola vez y toma el parámetro vigente hoy.")
doc.vineta("El interés es simple, se acumula día a día desde el vencimiento con la tasa mensual (prorrateada a 30 días) que regía cada día: List<Parametros>.vigenteEn(fecha) elige el parámetro más nuevo con vigencia hasta esa fecha, de modo que un cambio de tasa no modifica los días anteriores.")
doc.vineta("Antes del primer parámetro guardado no se devengan recargo ni interés (la gracia se toma de la fila más antigua).")
doc.vineta("totalAdeudado = cuota + recargo + interés, redondeado; es lo que se cobra en caja y por la app. deudaDetalle muestra «Cuota $X + recargo $Y + interés $Z» y los sumandos siempre dan el total.")
doc.parrafo(
    "Datos de demo del historial: hace un año, gracia 10 días, recargo $300, interés 2,5 %; desde hace dos meses, gracia 5 días, recargo $500, interés 3 % (DemoData.parametrosIniciales).")
doc.h2("6.4 QR de ingreso")
doc.parrafo(
    "Desde el Carnet digital, el botón «QR de ingreso» abre Screen.S_QR. El QR (ZXing, dibujado con Compose en ui/CodigoQr.kt) contiene en texto plano SocioVista.qrCarnet: "
    "«CLUB DEPORTIVO · CARNET DE SOCIO», N° de socio, nombre, DNI, plan, fechas de emisión y vencimiento del carnet y estado. "
    "QrCarnetTest lo codifica y lo vuelve a leer. El carnet digital tiene proporción de tarjeta (85,6 × 54 mm).")
doc.h2("6.5 Otras reglas vigentes")
doc.vineta("Un socio dado de baja no puede iniciar sesión. Un alta después de una baja no repite el número de socio.")
doc.vineta("Visitantes: sin cupo el ingreso se bloquea; el cupo del día se deriva de los visitantes de hoy; un DNI con pago diario hoy no se registra dos veces. Precio por actividad: Musculación $6.000, Natación $8.000, Funcional $6.000, Yoga $5.000.")
doc.vineta("Rutina: sin habilitación (mora o carnet vencido) o sin apto físico no se puede asignar (HU-09).")
doc.vineta("Un carnet se emite por 12 meses desde hoy (alta, renovación y reemisión).")

# ---------------------------------------------------------------- capturas
ACT = [
    ("1_LoginActivity", "LoginActivity", "Entry point de la app. Valida las credenciales y abre la Activity del rol."),
    ("2_SocioActivity", "SocioActivity", "Funciones del socio: cuota y pago, carnet digital con QR de ingreso, rutina, nutrición y turnos."),
    ("3_AdminActivity", "AdminActivity", "Funciones del personal administrativo: padrón y CRUD de socios, control de acceso, visitantes con pago diario, reportes y configuración del club (gracia, recargo e interés)."),
    ("4_EntrenadoraActivity", "EntrenadoraActivity", "Funciones de la entrenadora: presencia, asistencia de clases y rutinas con validación."),
    ("5_NutricionistaActivity", "NutricionistaActivity", "Funciones de la nutricionista: agenda de turnos y fichas nutricionales."),
]
num = 7
for pref, nombre, desc in ACT:
    doc.h1(f"{num}. Capturas — {nombre}", nueva_pagina=True)
    doc.parrafo(desc)
    entradas = [(f[0][:-4] + ".jpg", f[2], f[3], f[4]) for f in indice if f[0].startswith(pref)]
    doc.capturas(entradas, CAPT_RED, cierre=False)
    num += 1
assert num == 12
doc.parrafo(f"Total: {N_CAPT} capturas, generadas por la prueba instrumentada CapturasPorActivityTest y listadas con su epígrafe en entrega/capturas/indice.txt. "
            "El PDF de capturas de la entrega (DAM_Entrega_GRUPO3_ALVAREZ_AQUINO_MUSSI_WEITZEL_Capturas_v1.5.pdf) trae las mismas imágenes.")

# ================================================================== PARTE III
doc.salto()
doc.parte("III", "Pruebas, versionado, instalación y casos de uso")

# ---------------------------------------------------------------- 12. pruebas
doc.h1("12. Estrategia de pruebas y trazabilidad")
doc.parrafo(
    f"Conteo hecho con las anotaciones @Test de app/src/test y app/src/androidTest: {N_UNIT} pruebas unitarias (JUnit 4, se ejecutan con gradlew :app:testDebugUnitTest) "
    f"y {N_INSTR} instrumentadas (se ejecutan en emulador o teléfono). Los resultados guardados en app/build indican 0 fallas en las {N_UNIT} unitarias y en las 8 de ClubDbHelperTest.")
tests_filas = [
    ["ClubViewModelTest", "unitaria", str(TESTS["ClubViewModelTest"][1]), "Reglas de las 14 HU: login por rol, habilitación, gracia, recargo e interés, parámetros y vigencia, QR, control de acceso, pagos y cobro en caja, alta, visitantes y cupo, reportes, rutinas, asistencia, turnos, fichas y navegación por pila"],
    ["FormatoTest", "unitaria", str(TESTS["FormatoTest"][1]), "Reloj real y fijado, fechas, importes es-AR, DNI, meses y días, año bisiesto, edad, normalización del usuario"],
    ["ValidacionesTest", "unitaria", str(TESTS["ValidacionesTest"][1]), "Nombre, nombre y apellido por separado, DNI, correo, peso, altura, series y repeticiones"],
    ["CrudSociosTest", "unitaria", str(TESTS["CrudSociosTest"][1]), "Editar, validar al editar, baja con confirmación, número de socio tras una baja, socio de baja sin login"],
    ["NavegacionActivitiesTest", "unitaria", str(TESTS["NavegacionActivitiesTest"][1]), "Cada rol abre su Activity, arranque sin sesión, primera pestaña, cierre de sesión, alta visible para la entrenadora"],
    ["QrCarnetTest", "unitaria", str(TESTS["QrCarnetTest"][1]), "El QR del carnet se decodifica con los datos del carnet"],
    ["ClubDbHelperTest", "instrumentada", str(TESTS["ClubDbHelperTest"][1]), "Persistencia al cerrar y reabrir; migraciones 2→3 (fecha, medio y cupo) y 3→4 (tabla parametro); parámetros con vigencia; vencimientos por rango; pagos diarios por fecha; DNI repetido en el día"],
    ["CapturasPorActivityTest", "instrumentada", str(TESTS["CapturasPorActivityTest"][1]), f"Recorre cada Activity y genera las {N_CAPT} capturas con epígrafe (una prueba por Activity) y comprueba la guarda de rol"],
]
doc.tabla(["Clase de prueba", "Tipo", "@Test", "Qué cubre"], tests_filas, [2300, 1250, 700, 4821], sz=18)
doc.parrafo(
    f"Total: {N_UNIT} unitarias + {N_INSTR} instrumentadas = {N_UNIT + N_INSTR} pruebas. Las instrumentadas no corren en GitHub Actions (CI ejecuta las unitarias y lint informativo).")
doc.h2("12.1 Matriz de trazabilidad HU → pruebas")
doc.parrafo("Las pruebas de ClubViewModelTest llevan el identificador de la HU al comienzo de su nombre. Cuando una HU no tiene prueba propia se indica.")
doc.tabla(
    ["HU / tema", "Pruebas unitarias", "Instrumentadas y capturas"],
    [
        ["HU-01 Inicio de sesión", "ClubViewModelTest: HU-01 (4); NavegacionActivitiesTest", "CapturasPorActivityTest: a_loginActivity, f_guardaDeRol"],
        ["HU-02 Registrar socio", "ClubViewModelTest: HU-02 (3); ValidacionesTest; CrudSociosTest", "Capturas del alta y sus validaciones (AdminActivity)"],
        ["HU-03 Visitantes y pago diario", "ClubViewModelTest: HU-03 (4)", "ClubDbHelperTest: losPagosDiariosSeConsultanPorFecha, elDniRepetidoSeCuentaSoloEnSuDia"],
        ["HU-04 Cuota, pago, gracia, recargo e interés", "ClubViewModelTest: HU-04 (4); la gracia configurada…; el recargo y el interés vigentes…; los parámetros guardan su vigencia…; los parámetros rechazan valores…; el detalle de la deuda…; las cápsulas de estado…", "ClubDbHelperTest: losParametrosDelClubSePersistenConSuVigencia, unaBaseVersion3GanaLaTablaDeParametros"],
        ["HU-05 Control de acceso", "ClubViewModelTest: HU-05 (2); la demo trae un socio en gracia…", "Capturas 3_AdminActivity_17 a 20, 32, 36 y 37"],
        ["HU-06 Carnet digital y QR", "ClubViewModelTest: el QR de ingreso es una pantalla propia…; QrCarnetTest", "Capturas 2_SocioActivity_02 y 03"],
        ["HU-07 Emitir / renovar carnet", "ClubViewModelTest: HU-07 (1)", "Captura 3_AdminActivity_06"],
        ["HU-08 Asistencia", "ClubViewModelTest: HU-08 (2)", "d_entrenadoraActivity"],
        ["HU-09 Rutina", "ClubViewModelTest: HU-09 (2)", "d_entrenadoraActivity"],
        ["HU-10 Consultar rutina", "Sin prueba unitaria propia", "b_socioActivity (captura Mi rutina)"],
        ["HU-11 Turnos de nutrición", "ClubViewModelTest: HU-11 (1)", "b_socioActivity, e_nutricionistaActivity"],
        ["HU-12 Ficha nutricional", "ClubViewModelTest: HU-12 (2)", "e_nutricionistaActivity"],
        ["HU-13 Información nutricional", "Sin prueba unitaria propia", "b_socioActivity (captura Nutrición)"],
        ["HU-14 Reportes", "ClubViewModelTest: HU-14 (4)", "ClubDbHelperTest: elListadoDeVencimientosFiltraPorRangoDeFechas"],
        ["Persistencia y migraciones", "—", "ClubDbHelperTest: laSemillaSobreviveACerrarYReabrir, unaBaseVersion2GanaFechaYMedio, unaBaseVersion2DescuentaDelCupo…"],
        ["Fecha y formatos", "FormatoTest (14)", "—"],
    ],
    [2200, 3771, 3100], sz=17)

# ---------------------------------------------------------------- 13. versionado
doc.h1("13. Versionado y rollback")
doc.parrafo("Resumen de doc/VERSIONADO_Y_ROLLBACK.md.")
doc.vineta("", partes=[("Versión del código: ", True), ("versionName y versionCode en app/build.gradle.kts (SemVer X.Y.Z); la versión actual es 1.5.1 (versionCode 7).", False)])
doc.vineta("", partes=[("Publicación: ", True), ("el workflow Release (.github/workflows/release.yml) corre en cada merge a main las pruebas unitarias, crea el tag vX.Y.Z y una GitHub Release con el APK y SHA256SUMS.txt. Si la versión ya tiene tag, publica el siguiente parche. Cada release es inmutable.", False)])
doc.vineta("", partes=[("Integración continua: ", True), ("el workflow CI (ci.yml) corre las pruebas unitarias y lint informativo en cada push a main, release/** y feature-**, y en cada Pull Request.", False)])
doc.vineta("", partes=[("APK a demanda: ", True), ("el workflow apk.yml se ejecuta a mano y arma un APK debug descargable por 14 días, sin tag ni Release.", False)])
doc.tabla(
    ["Versión", "Qué trae", "Base de datos", "Si hay que volver"],
    [
        ["v1.3.1", "SQLite en la app Android", "v2", "—"],
        ["v1.4.0", "Vencen hoy, pagos diarios por fecha, cobro en caja completo, pruebas de SQLite", "v3", "v1.3.1"],
        ["v1.5.0", "Fecha real, apellido separado, gracia / recargo / interés con vigencia, QR de ingreso, carnet con formato de tarjeta", "v4", "v1.4.0"],
        ["v1.5.1", "«No encontrado» en el control de acceso ofrece «Registrar visitante»; la presencia de la Entrenadora muestra la hora real", "v4 (sin cambios)", "v1.5.0, sin desinstalar"],
    ],
    [1100, 4571, 1500, 1900])
doc.h2("13.1 Rollback de la v1.5.0 a la v1.4.0")
doc.vineta("", partes=[("En un teléfono de prueba: ", True), ("desinstalar e instalar el APK de la release v1.4.0 (adb install -r -d). Una base v4 no se abre con la v1.4.0, por eso se desinstala: se pierden los datos de prueba de ese teléfono.", False)])
doc.vineta("", partes=[("En el repositorio: ", True), ("Actions → Rollback → Run workflow con v1.4.0; abre un Pull Request hacia main que restaura esa versión sin reescribir el historial, lo aprueba un integrante distinto del autor y Release publica un parche con el contenido restaurado.", False)])
doc.vineta("", partes=[("Qué se pierde al volver: ", True), ("gracia, recargo e interés configurables, QR de ingreso, apellido separado y la fecha real (la v1.4.0 usa la fecha fija 8/9/2026).", False)])
doc.vineta("No se borran tags ni releases publicadas y no se hace push --force sobre main ni release/**.")
doc.parrafo("Limitaciones conocidas: el APK publicado se firma con la clave de depuración; la protección de ramas de main no está disponible en el plan gratuito de GitHub para repositorios privados, de modo que la aprobación de un segundo integrante se cumple por procedimiento.")

# ---------------------------------------------------------------- 14. instalacion
doc.h1("14. Instalación")
doc.parrafo(
    "La guía de instalación está en INSTALAR.md, en la raíz del repositorio. Cubre tres caminos: teléfono Android (APK), emulador de Android Studio y Android Studio desde el código fuente. "
    "Requiere Android 7.0 (API 24) o superior. No se repite aquí su contenido.")

# ---------------------------------------------------------------- 15. casos de uso
doc.h1("15. Diagramas de contexto y de casos de uso")
DIAG = os.path.join(RAIZ, "doc", "casos-de-uso", "diagramas")


def girado(nombre):
    """Copia girada 90° del diagrama para ocupar una página vertical completa."""
    import tempfile
    from PIL import Image
    destino = os.path.join(tempfile.gettempdir(), "girado_" + nombre)
    Image.open(os.path.join(DIAG, nombre)).rotate(90, expand=True).save(destino)
    return destino


doc.h2("15.1 Diagrama de contexto")
doc.parrafo(
    "Muestra la aplicación como un único proceso y lo que intercambia con su entorno. Las entidades externas son los cuatro roles con cuenta "
    "(Socio, Administrativo, Entrenadora y Nutricionista) y el No socio / Visitante, que no usa la app: el Administrativo registra su pago diario en el mostrador. "
    "Dentro del dispositivo, la app toma la fecha y la hora del reloj del sistema y guarda todo en la base SQLite local club_deportivo.db.")
doc.parrafo(
    "La app no tiene conexión a Internet: el manifiesto no declara el permiso INTERNET. Por eso no hay servidor, pasarela de pago ni lector de QR. "
    "El pago desde la app es simulado y el QR de ingreso solo se muestra en pantalla.")
doc.parrafo("El diagrama ocupa la página siguiente, girado para leerlo en horizontal. La imagen original está en doc/casos-de-uso/diagramas/00_contexto.png.")
doc.salto()
doc.imagen(girado("00_contexto.png"), 5400000, 7714286)
doc.salto()
doc.h2("15.2 Diagrama de casos de uso (UML 2.0)")
doc.parrafo(
    "Reúne los 26 casos de uso por actor. Socio, Administrativo, Entrenadora y Nutricionista especializan a «Usuario del club», que inicia y cierra sesión. "
    "«Calcular estado y deuda de la cuota» es el caso incluido por CU-07, CU-09, CU-14 y CU-15. CU-10 extiende a CU-09 y CU-26 (QR de ingreso) extiende a CU-16 (carnet digital). "
    "El fondo naranja marca los casos nuevos o modificados en la v1.5.")
doc.parrafo("El diagrama ocupa la página siguiente, girado para leerlo en horizontal. La imagen original está en doc/casos-de-uso/diagramas/01_casos_de_uso.png.")
doc.salto()
doc.imagen(girado("01_casos_de_uso.png"), 5760000, 7200000)
doc.salto()
doc.h2("15.3 Especificación de los casos de uso")
doc.parrafo(
    f"La plantilla de cada caso de uso, su diagrama de secuencia y los diagramas de clases del dominio, de estados de la cuota y de actividad del alta están en un documento aparte: {CASOS}. "
    "Los dos diagramas de esta sección se generan con Python y Pillow desde doc/casos-de-uso/herramientas (dia_contexto.py y dia_casos_de_uso.py).")

# ================================================================== PARTE IV
doc.salto()
doc.parte("IV", "Registro de uso de Inteligencia Artificial")
doc.h1("16. Herramientas utilizadas")
doc.tabla(
    ["Herramienta", "Quién la utilizó", "Modo de uso"],
    [
        ["Claude Code (Anthropic), modelo Claude Opus 5.5", "Leandro Mussi", "Asistente en la terminal y en VS Code, con acceso al repositorio local, al SDK de Android y a un emulador. Todo cambio quedó en una copia local del repositorio y se incorporó al repositorio remoto solo con autorización del equipo."],
        ["Claude Code (Anthropic), modelos Claude Fable 5.1 y Claude Opus 5.5", "Leandro Mussi", "Sesión del 07 y 08/11/2026 (v1.4.0 y v1.5.0). Según el trailer Co-Authored-By de los commits: Claude Fable 5.1 en 23 commits y Claude Opus 5.5 desde el commit 1c93f07 (4 commits). Autor de todos los commits: Leandro Mussi."],
        ["(a completar por el equipo)", "", ""],
    ],
    [2600, 1500, 4971])
doc.h2("16.1 Uso de IA en Figma, documentación y pruebas")
doc.parrafo(
    "Resumen por área. Las cifras salen del historial de git: se cuentan los commits que llevan el trailer «Co-Authored-By: Claude». "
    "Lo que el repositorio no registra queda marcado «a completar por el equipo»; no se completó por suposición.")
doc.tabla(
    ["Área", "Uso de IA", "Evidencia en el repositorio", "A completar"],
    [
        ["Diseño en Figma (CLUB DEPORTIVO MOBILE)",
         "El equipo indicó el 08/11/2026 que usó IA como apoyo en el diseño de Figma.",
         "El commit 03273b7 (04/10/2026) agrega solo el enlace y la exportación en PDF (doc/diseno), sin trailer de IA. "
         "El prototipo web web/index.html (ClubDeportivo-App-v4), base visual de la app Android, se armó con Claude Design según el README del commit b8cd601 (13/09/2026).",
         "Herramienta usada en Figma, pantallas en las que se usó y qué decidió el equipo."],
        ["Documentación",
         "Generación con scripts en Python de la documentación general, del documento de casos de uso con sus diagramas UML 2.0 y del PDF de capturas; "
         "redacción de README, INSTALAR.md, plan de versionado y rollback y guion de presentación.",
         "doc/herramientas_documentacion: 2 de 2 commits con trailer. doc/casos-de-uso: 5 de 5. entrega/: 11 de 14. "
         "Los textos se generan a partir del código, de git y de los documentos de la entrega.",
         "Revisión y aprobación del contenido por cada integrante (sección 27)."],
        ["Pruebas",
         "Escritura y ampliación de pruebas unitarias e instrumentadas, y de la prueba que genera las capturas. Cada prueba se ejecutó y se leyó su resultado antes de incorporarla.",
         "ClubViewModelTest: 13 de 16 commits con trailer. ClubDbHelperTest: 6 de 6. QrCarnetTest: 1 de 1. CapturasPorActivityTest: 5 de 8. "
         "CrudSociosTest: 3 de 6. FormatoTest, ValidacionesTest y NavegacionActivitiesTest: 1 commit con trailer cada una.",
         "Qué pruebas escribió o revisó a mano cada integrante."],
    ],
    [1700, 2500, 3171, 1700], sz=16)
doc.h1("17. Registro de consultas — sesión del 03 y 04/10/2026 (Leandro Mussi)")
doc.parrafo("Las indicaciones se transcriben como fueron escritas. La columna «Decisión del equipo» registra lo que el equipo aceptó, modificó o resolvió sobre cada propuesta. Esta sección se conserva sin cambios respecto de la v1.2.")
SES1 = [
    ["1", "Revisar los cambios de un integrante en el repositorio", "«quiero revisar los cambios que mandó el usuario Andres al repo»", "Se identificó el commit «Update colors» de Andrés. Se verificó que main no compilaba (colors.xml vacío) y que el commit lo corrige; las 40 pruebas pasaron.", "Se trabajó sobre un clon local, en la rama release/v1.2, con el cambio de Andrés incorporado."],
    ["2", "Definir políticas de calidad y un circuito de revisión", "«hagamos … un .md de policies o quality gates … OBLIGATORIO: vulnerabilidades, fallos en la lógica, fallos funcionales, test unitario, NO DEBES presuponer NADA, cada ciclo lo dispara el commit…» y «que funcione como un ciclo pipeline … DEV > QA > PRE > PROD»", "Documento «Política de Calidad y Pipeline de Promoción» con 6 gates, 4 stages y matriz de trazabilidad de las 14 HU.", "El equipo eligió la variante híbrida: aprobación local hasta PRE y Pull Request con revisión de otro integrante para PROD."],
    ["3", "Identificar la fuente de verdad de las historias de usuario", "Se entregó el prototipo «CLUB DEPORTIVO MOBILE».", "La IA tomó el prototipo como fuente de las 14 HU y registró las diferencias entre el prototipo, el README y el código.", "Se adoptó el prototipo como fuente; las diferencias quedaron resueltas en la v1.2.0 (ver Anexo, «Hallazgos registrados»)."],
    ["4", "Analizar la consigna de la Etapa Desarrollo contra la app", "Se pegó la consigna completa de la etapa.", "Se detectó que la app usaba una sola Activity, que faltaban validaciones de formato (correo, rangos) y que el CRUD de socios no tenía modificación ni baja.", "El equipo decidió pasar a una Activity por rol y priorizar Activities, validaciones, CRUD y entregables por la fecha de entrega."],
    ["5", "Implementar la navegación entre Activities", "Opción elegida: «Pasar a una Activity por rol».", "LoginActivity (entry point) y SocioActivity, AdminActivity, EntrenadoraActivity y NutricionistaActivity. ClubRepositorio conserva los datos en memoria entre Activities. 5 pruebas nuevas y verificación en emulador.", "Se mantuvo la navegación del prototipo dentro de cada Activity."],
    ["6", "Validaciones y mensajes al usuario", "Incluido en el plan aprobado («si me parece bien»).", "Validaciones de nombre, DNI, correo, peso, altura, series y repeticiones; mensajes de login para datos incompletos; mensajes al confirmar sin horario o sin ejercicio. 7 pruebas nuevas.", "Correo obligatorio en el alta: el prototipo lo muestra como campo del alta y el perfil del socio lo exhibe. El equipo lo confirmó."],
    ["7", "Operaciones CRUD", "Incluido en el plan aprobado.", "Matriz CRUD por entidad. En la interfaz: editar y dar de baja socios (con confirmación). El correo del alta, que antes se descartaba, ahora se guarda. 5 pruebas nuevas.", "Un socio dado de baja no puede iniciar sesión. Un cambio de plan rige desde la próxima cuota."],
    ["8", "Capturas por Activity, APK y documentación", "«debes agregar capturas de las pantallas por cada activity»", "Prueba instrumentada que recorre cada Activity y genera 57 capturas con epígrafe; PDF con diagrama de navegación y matriz CRUD; APK 1.2.0; README actualizado.", "Se revisaron las capturas una por una; se corrigieron toasts superpuestos y el diagrama."],
    ["9", "Resolver las discrepancias con el prototipo", "«hay dos discrepancias que mencionas ambas deben estar resueltas»", "Reportes: la cuota de septiembre de Camila Duarte pasa a pagada y la app muestra 2 cuotas por $30.500, como el prototipo. Correo del alta confirmado como obligatorio. Prueba de HU-14 actualizada.", "Se reemplazaron solo las 3 capturas afectadas; el resto se mantuvo."],
]
doc.tabla(["#", "Tarea / problema", "Indicación realizada", "Resultado obtenido", "Decisión o modificación del equipo"], SES1, [400, 1500, 2100, 2800, 2271], sz=16)

doc.h1("18. Registro de consultas — sesión del 07 y 08/11/2026 (Leandro Mussi)")
doc.parrafo(
    "Entradas reconstruidas del historial de git (git log de main y de feature-08-gracia-recargos, desde el merge del PR #7 del 07/11 hasta el último commit del 08/11) y de los documentos de diseño de doc/especificaciones. "
    "En el repositorio no constan las indicaciones textuales ni las decisiones del equipo más allá de lo que dicen los commits; esas celdas quedan marcadas «a completar» y se listan en «A confirmar».")
A = "(a completar: transcribir la indicación)"
SES2 = [
    ["10", "Diseño y plan de la v1.4.0 (entrega final)", A, "Documentos doc/especificaciones/specs/2026-11-08-v1.4.0-entrega-final-design.md y plans/2026-11-08-v1.4.0-entrega-final.md (commit 2de4f88).", "Incorporado a la rama; decisión del equipo a completar."],
    ["11", "SQLite v3 y consultas de la v1.4.0", A, "Fecha y medio de pago en visitante, socio que vence hoy, vencimientos por rango de fechas, pagos diarios por fecha, DNI repetido en el día y pruebas de la base (commits 26d9299, bd6b47a). Migración 2→3 que descuenta del cupo los visitantes que la 1.3.x ya había sumado, con prueba instrumentada (ed5a4c7).", "Incorporado (v1.4.0)."],
    ["12", "Reportes, visitantes y cobro en caja (HU-14, HU-03, HU-04)", A, "Reportes con cuatro pestañas y selector de fecha, KPIs de hoy, pago diario con fecha, medio, DNI único por día y cupo del día, y cobro en caja igual al pago por app con rechazo del doble pago (c9ce74d, c5c09b4, d5db561, 414eca8).", "Incorporado (v1.4.0)."],
    ["13", "Ajuste de la etiqueta de la ficha del socio", A, "La habilitación (HU-05) no cambia; «Cuota al día» queda solo para la cuota pagada y la etiqueta pasa a «Habilitado · Cuota pendiente» (fe32a33, db5aabe).", "Incorporado (v1.4.0)."],
    ["14", "Capturas y README de la v1.4.0; integración", A, "Capturas de las pantallas nuevas y README (92246fe); Pull Request #8 «v1.4.0 entrega final» integrado en main (5635d4e).", "Pull Request #8 integrado en main."],
    ["15", "v1.5: reloj real y apellido separado", A, "Reloj.hoy() lee la fecha del dispositivo (las pruebas la fijan en el 8/9/2026); datos de demo relativos a hoy; alta y edición con nombre y apellido separados, obligatorios y solo letras (8b4ac01).", "Incorporado en feature-08-gracia-recargos."],
    ["16", "v1.5: parámetros del club, gracia, mora, recargo e interés", A, "Tabla parametro (migración 3→4); parámetros con vigencia por fecha; SocioVista deriva pendiente / en gracia / mora; deuda con recargo e interés por tramos; pantallas de gracia, deuda y parámetros del club (6da281e, 25271f3). Revisión posterior: antes del primer parámetro guardado no se devenga recargo ni interés, rechazo de NaN/Infinity, tope de recargo de 1.000.000 y detalle de deuda que siempre suma el total (827d8bc).", "Arreglos de la revisión incorporados en 827d8bc."],
    ["17", "Demo v1.5 y pulido visual", A, "Socia en gracia (Rocío Paz), historial de parámetros 2025/2026, tercer día de pagos diarios y correos en los socios de demo; aviso compacto con cápsula y contador de la cuota (5491cb0, 5b0cb97).", "Incorporado en feature-08-gracia-recargos."],
    ["18", "Carnet con formato de tarjeta y QR de ingreso", A, "Carnet digital con proporción de tarjeta, pantalla QR de ingreso con los datos del carnet (prueba que lo codifica y lo lee con ZXing) y etiqueta «En período de gracia» (1c93f07).", "Incorporado en feature-08-gracia-recargos."],
    ["19", "Capturas, PDF y guía de la entrega v1.5.0", A, "Capturas y PDF/docx de capturas con el Grupo 3, README de entrega, checklist del video, regeneraciones tras la revisión y con el carnet nuevo (aaf6eab, c004df0, 25c134c, 072991c, aa6d0cc).", "Incorporado en feature-08-gracia-recargos."],
    ["20", "Casos de uso y diagramas UML 2.0", A, "Casos de uso especificados y diagramas (casos de uso, clases, cuatro secuencias, máquina de estados de la cuota y actividad del alta), con ejemplos con los datos de demo de la v1.5 (fcd2a23, 73580e1; CU-26 en 1c93f07).", "Incorporado; el documento lo regenera otro integrante."],
    ["21", "Instalación, rollback y guion de presentación", A, "INSTALAR.md e instalar.cmd, tabla de versiones y plan de rollback de la v1.5.0 en doc/VERSIONADO_Y_ROLLBACK.md y guion de presentación por rol (e51c3c2, 9103416).", "Incorporado en feature-08-gracia-recargos."],
    ["22", "Documentación general v1.5.0 (este documento)", A, "Documento generado con scripts de doc/herramientas_documentacion a partir del código, de git y de los documentos de la entrega; sin commit al momento de generarse.", "Pendiente de revisión del equipo."],
]
doc.tabla(["#", "Tarea / problema", "Indicación realizada", "Resultado obtenido", "Decisión o modificación del equipo"], SES2, [400, 1500, 1500, 3800, 1871], sz=16)

doc.h1("19. Uso previo de IA en etapas anteriores (a completar por el equipo)")
doc.parrafo("El README del proyecto indica que la app se implementó a partir del prototipo «ClubDeportivo-App-v4.html» realizado con Claude Design. Cada integrante debe registrar aquí, con la misma estructura, las herramientas y consultas que utilizó antes de estas sesiones.")
doc.tabla(["#", "Integrante", "Herramienta", "Tarea", "Indicación", "Resultado y decisión del equipo"],
          [[str(n), "", "", "", "", ""] for n in (23, 24, 25, 26)], [400, 1400, 1400, 1700, 1900, 2271], sz=17)
doc.h1("20. Criterios aplicados")
doc.vineta("Ningún cambio se incorporó al repositorio remoto sin la autorización explícita del equipo.")
doc.vineta(f"Cada propuesta se verificó compilando y ejecutando las pruebas: en la v1.2, 57 unitarias y 6 instrumentadas; en la v1.5.0, {N_UNIT} unitarias y {N_INSTR} instrumentadas, sin fallas en los resultados guardados en app/build.")
doc.vineta("Cuando la documentación no definía un comportamiento, la IA lo informó como discrepancia en lugar de suponerlo; las decisiones quedaron a cargo del equipo.")
doc.vineta("Las cifras y los nombres de esta documentación se verificaron contra el código y git; lo que no se pudo verificar figura en «A confirmar».")
doc.vineta("Cada integrante revisa y debe poder explicar las partes del proyecto en las que participó, incluidas las generadas con asistencia de IA.")

# ================================================================== ANEXO
doc.salto()
doc.parte("V", "Anexo: política de calidad, hallazgos y puntos a confirmar")
doc.h1("21. Control del documento")
doc.tabla(
    ["Versión", "Fecha", "Autor", "Descripción"],
    [
        ["1.0", "04/10/2026", "Leandro Mussi (asistido por Claude Code)", "Versión inicial: gates obligatorias, pipeline por stages, reglas de autorización y matriz de trazabilidad de las 14 historias de usuario."],
        ["1.5", "08/11/2026", "Grupo 3 (generado con asistencia de Claude Code)", "Actualización a la app v1.5.0: SQLite, gracia, recargo e interés, QR, estrategia de pruebas y versionado vigente. El pipeline local DEV → QA → PRE → PROD de la v1.0 sigue descripto en doc/politica_calidad_pipeline_v1.2.docx."],
    ],
    [900, 1100, 2300, 4771])
doc.h1("22. Objetivo y alcance")
doc.parrafo("Establecer las políticas de calidad de cumplimiento obligatorio para la aplicación Android del Club Deportivo y el circuito por el cual todo cambio avanza hasta la publicación del APK. Comprende el repositorio LeandroReneMussi/Club_Deportivo_APK: código Kotlin (Jetpack Compose), recursos Android, Gradle, pruebas y APK. Características del producto relevantes para las pruebas:")
doc.vineta("Prototipo académico con SQLite local en la app Android (esquema versión 4). No hay backend ni pasarela de pago: el pago se registra en la base.")
doc.vineta("La fecha «hoy» es la del dispositivo (Reloj en data/Format.kt); las pruebas unitarias la fijan en el 8 de septiembre de 2026 para que los escenarios sean reproducibles.")
doc.vineta("minSdk 24 (Android 7), targetSdk y compileSdk 35.")
doc.parrafo("Quedan fuera de alcance la corrección de los defectos detectados (cada corrección es un cambio independiente) y la publicación en tiendas de aplicaciones.")
doc.h1("23. Principio rector: no presuponer")
doc.vineta("Cada resultado esperado cita su fuente: documento y pantalla o sección.")
doc.vineta("Cada hallazgo incluye evidencia verificable: archivo y línea, salida obtenida y pasos de reproducción.")
doc.vineta("Cuando las fuentes son ambiguas o no cubren un caso, se registra una «Discrepancia documental» y la decisión queda a cargo del equipo. Nunca se completa el comportamiento por criterio propio.")
doc.vineta("Un PASS solo se informa cuando la verificación fue efectivamente ejecutada y su salida lo confirma.")
doc.h1("24. Quality Gates obligatorias")
doc.tabla(
    ["Gate", "Verificación", "Condición de rechazo (FAIL)"],
    [
        ["G1 — Vulnerabilidades", "Dependencias Gradle, Android Lint (seguridad), manifiesto (componentes exportados, allowBackup, permisos), secretos en el repositorio, firma del APK.", "Hallazgo de severidad Crítica o Alta."],
        ["G2 — Fallos de lógica", "Revisión del cambio contra las reglas del prototipo y de la v1.5: habilitación, control de acceso por DNI, cupos, bloqueo de rutina, pago de cuota, gracia, recargo e interés, fechas.", "Hallazgo de severidad Crítica o Alta."],
        ["G3 — Funcional (caja negra)", "Pruebas instrumentadas y recorrido de cada historia de usuario en un emulador, por rol.", "Cualquier prueba fallida."],
        ["G4 — Unitarias (caja blanca)", f"Pruebas JUnit sobre la lógica interna ({N_UNIT} hoy).", "Pruebas fallidas o lógica nueva sin pruebas."],
        ["G5 — No presuponer", "Auditoría de trazabilidad de pruebas y hallazgos.", "Prueba sin fuente o hallazgo sin evidencia."],
        ["G6 — Ciclo automático", "Cada push dispara el workflow CI y el merge a main dispara Release.", "Promoción sin ciclo en PASS del mismo commit."],
    ],
    [1900, 4271, 2900], sz=18)
doc.h2("24.1 Severidades")
doc.tabla(
    ["Severidad", "Criterio", "Efecto"],
    [
        ["Crítica", "Exposición de datos, cierre inesperado de la aplicación, imposibilidad de compilar o instalar, incumplimiento grave de una HU.", "Bloquea"],
        ["Alta", "Comportamiento distinto al documentado o vulnerabilidad explotable.", "Bloquea"],
        ["Media", "Defecto con alternativa de uso o riesgo acotado.", "Se registra; no bloquea"],
        ["Baja", "Mejora de calidad, legibilidad o mantenibilidad.", "Se registra; no bloquea"],
        ["Info", "Observación o discrepancia documental.", "Se registra; puede dejar pruebas pendientes"],
    ],
    [1300, 5271, 2500])
doc.h1("25. Hallazgos registrados")
doc.parrafo("Revisión del 03 y 04/10/2026 sobre main (b8cd601) y la rama feature-01-update-res (44f9b8d). H son defectos o riesgos; D son discrepancias entre el prototipo, el README y el código. La columna «Estado» indica cómo quedó cada uno en la v1.2.0 y, cuando se pudo verificar, en la v1.5.0.")
doc.tabla(
    ["ID", "Severidad", "Descripción", "Estado / resolución"],
    [
        ["H-01", "Alta", "main no compilaba: colors.xml estaba vacío y themes.xml e ic_launcher usan @color/pista_*.", "Resuelto en la v1.2.0 (commit 44f9b8d de Andrés)."],
        ["H-02", "Media", "El APK de release se firma con la clave de debug.", "Persiste en la v1.5.0 (signingConfig = debug en app/build.gradle.kts). Aceptado para la entrega académica; reemplazar antes de una publicación real."],
        ["H-03", "Baja", "La paleta de colores está definida en colors.xml y en Theme.kt.", "Aceptado: si se cambia un color, cambiarlo en los dos."],
        ["H-04", "Baja", "android:allowBackup=\"true\" en el manifiesto.", "Persiste en la v1.5.0, y ahora la app guarda una base SQLite local: revisar si corresponde desactivar el backup."],
        ["H-05", "Info", "Las cuentas de prueba se muestran en el login.", "Aceptado: decisión de diseño para la demostración, documentada en el README."],
        ["D-01", "Info", "El prototipo muestra a Lucas Benítez con dos DNI y números de socio distintos.", "Resuelto en la v1.2.0: DNI 40.120.334 y N° 01302 en todas las pantallas."],
        ["D-02", "Info", "El historial nutricional de Andrés difiere entre la vista del socio y la de la nutricionista.", "Resuelto en la v1.2.0: un único historial que muestran las dos vistas."],
        ["D-03", "Info", "El texto del login del prototipo nombra los roles de forma distinta al resto del diseño.", "Resuelto: 4 cuentas, una por rol, cada una con su Activity."],
        ["D-04", "Info", "El campo Usuario del prototipo pide «DNI o email».", "Resuelto: se ingresa con nombre de usuario, como indican las cuentas de prueba."],
        ["D-05", "Info", "La app declaraba versión 1.0 y el prototipo indica v1.2.", "Resuelto en la v1.2.0; hoy versionName 1.5.0."],
        ["D-06", "Info", "El repositorio no incluía la descripción de las 14 historias de usuario.", "Resuelto: se adoptó el prototipo como fuente; la matriz de este documento cubre las 14 HU."],
        ["D-07", "Info", "El README indicaba «cd android», carpeta que no existe.", "Resuelto: el proyecto se abre desde la raíz."],
        ["D-08", "Info", "Reportes: la app mostraba 3 cuotas próximas a vencer ($49.000) y el prototipo 2 ($30.500).", "Resuelto en la v1.2.0 con los datos de entonces. Desde la v1.4.0 los reportes se calculan con la fecha y las consultas SQL, por lo que ya no son los valores fijos del prototipo."],
        ["D-09", "Info", "No estaba definido si el correo del alta es obligatorio.", "Resuelto: obligatorio, con validación de formato."],
        ["D-10", "Info", "README.md y entrega/README_entrega_v1.5.md indican 70 pruebas unitarias y 68 capturas.", f"Detectado al generar este documento: el código tiene {N_UNIT} @Test unitarios y entrega/capturas tiene {N_CAPT} imágenes. Corregir los README."],
    ],
    [700, 1000, 3771, 3600], sz=17)
doc.h1("26. Limitaciones y riesgos")
doc.vineta("Las pruebas instrumentadas requieren un emulador o teléfono y no corren en GitHub Actions.")
doc.vineta("La protección de ramas de main exigiendo aprobación no está disponible para repositorios privados en el plan gratuito; el control de cuatro ojos se cumple por procedimiento.")
doc.vineta("El APK publicado se firma con la clave de depuración del runner de GitHub Actions.")
doc.vineta("Una base SQLite de la v1.5.0 (versión 4) no se abre con una app anterior: para volver a una versión previa hay que desinstalar.")
doc.vineta("Una instalación que se actualiza conserva los datos de la versión anterior: para ver los datos de demo nuevos hay que instalar limpio (INSTALAR.md).")
doc.h1("27. Aprobación del documento")
doc.parrafo("Los integrantes del equipo manifiestan su conformidad con el presente documento:")
doc.tabla(["Integrante", "Firma", "Fecha"], [["Ana Alvarez", "", ""], ["Jorge Aquino Lesme", "", ""], ["Leandro Mussi", "", ""], ["Andrés Weitzel", "", ""]], [3400, 3500, 2171])

# ---------------------------------------------------------------- A confirmar
doc.salto()
doc.h1("28. A confirmar")
doc.parrafo("Lo que no se pudo verificar en el código ni en git al generar este documento. Ninguno de estos puntos se completó por suposición.")
CONF = [
    ["1", "Enlace del video de YouTube «No listado» de la v1.5.1.", "entrega/README_entrega_v1.5.md lo marca como pendiente de grabación."],
    ["2", "Release v1.5.1 en GitHub.", "La Release v1.5.0 está publicada con el APK y SHA256SUMS.txt. La v1.5.1 se publica sola al hacer merge del Pull Request #10 a main. El zip del proyecto y el APK v1.5.1 están en la carpeta de entrega, fuera del repositorio."],
    ["3", "Cantidad de pruebas y capturas en los README.", f"README.md dice 78 unitarias; el código tiene {N_UNIT} unitarias y {N_INSTR} instrumentadas, y hay {N_CAPT} capturas."],
    ["4", "Ejecución actual de las pruebas.", "Los resultados guardados en app/build son del 08/11/2026 16:50 (unitarias) y del 08/11/2026 con timestamp 19:05:16 en el XML (ClubDbHelperTest); volver a correrlas antes de entregar."],
    ["5", "Indicaciones textuales y decisiones del equipo de la sesión del 07 y 08/11 (sección 18).", "No constan en git; solo los mensajes de commit y los specs de doc/especificaciones."],
    ["6", "Uso previo de IA de cada integrante (sección 19).", "Vacío en la v1.2; debe completarlo cada integrante."],
    ["7", "Pipeline local DEV → QA → PRE → PROD de la política v1.0 (qa/promote.ps1, hooks, AVD de QA y PRE).", "Esos archivos no existen en el repositorio actual (sin carpeta qa/ ni .githooks/, y nunca estuvieron en el historial). La automatización verificable es la de .github/workflows (ci, release, rollback, apk). Confirmar si el pipeline local sigue vigente."],
    ["8", "Casos de uso y UML.", "El PDF doc/casos-de-uso/…Casos_de_uso_UML.pdf lo está regenerando otro integrante; no se copió. Confirmar el contenido final."],
    ["9", "INSTALAR.md.", "Lo escribe otro integrante; al generar este documento existía con tres caminos (teléfono, emulador, Android Studio). Confirmar la versión final."],
    ["10", "Firmas de aprobación y fecha de conformidad.", "La tabla de la sección 27 está en blanco."],
    ["11", "Comisión «2° D».", "Tomada de la v1.2; no figura en el código ni en git."],
]
doc.tabla(["#", "Punto", "Motivo / dónde quedó pendiente"], CONF, [450, 3700, 4921], sz=18)

# ================================================================== indice y guardado
doc.parrafo("")


def construir(paginas=None):
    doc.indice(paginas)
    doc.guardar(SALIDA)


def pdf():
    subprocess.run([SOPLA, "--headless", "--convert-to", "pdf", "--outdir", ENTREGA, SALIDA], check=True, capture_output=True)
    return os.path.join(ENTREGA, NOMBRE + ".pdf")


def paginas_de(ruta_pdf):
    import pymupdf
    d = pymupdf.open(ruta_pdf)
    inicio = next(i for i in range(len(d)) if any(l.strip() == "Parte I" for l in d[i].get_text().splitlines()))
    res = {}
    for nivel, texto, _ in doc.headings:
        buscado = texto
        for i in range(inicio, len(d)):
            lineas = [l.strip() for l in d[i].get_text().splitlines()]
            if nivel == 0:
                p = buscado.split(" — ")[0]
                if p in lineas:
                    res[texto] = i + 1
                    break
            elif buscado in lineas:
                res[texto] = i + 1
                break
        else:
            raise RuntimeError("sin pagina: " + texto)
    return res, len(d)


if __name__ == "__main__":
    import json
    pj = os.path.join(os.path.dirname(os.path.abspath(__file__)), "_paginas.json")
    prev = json.load(open(pj, encoding="utf8")) if os.path.exists(pj) else None
    construir(prev)
    p, n = paginas_de(pdf())
    if p != prev:
        json.dump(p, open(pj, "w", encoding="utf8"), ensure_ascii=False)
        print("indice actualizado; volver a ejecutar. paginas:", n)
    else:
        print("estable. paginas:", n)
