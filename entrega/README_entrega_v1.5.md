# Entrega final · Club Deportivo v1.5.0 · Grupo 3

Alvarez · Aquino Lesme · Mussi · Weitzel — Desarrollo de Aplicaciones Móviles, IFTS N° 29 — 8 de noviembre de 2026.

## Qué se entrega

| Entregable | Archivo |
|---|---|
| Proyecto Android Studio comprimido | `DAM_Entrega_GRUPO3_ALVAREZ_AQUINO_MUSSI_WEITZEL_ProyectoAndroidStudio_v1.5.0.zip` (generado desde `main` con `git archive`; sin `build/` ni `local.properties`) |
| PDF de capturas de pantalla | `DAM_Entrega_GRUPO3_ALVAREZ_AQUINO_MUSSI_WEITZEL_Capturas_v1.5.pdf` (71 capturas de las cinco Activities, con epígrafe e historia de usuario; también en `.docx`) |
| Casos de uso y diagramas UML 2.0 | `../doc/casos-de-uso/DAM_Entrega_GRUPO3_ALVAREZ_AQUINO_MUSSI_WEITZEL_Casos_de_uso_UML.pdf` (25 casos de uso, diagrama de casos de uso, clases, 4 secuencias, máquina de estados de la cuota y actividad del alta; fuentes `.puml` y `.md` al lado) |
| APK | Release **v1.5.1** (corrige dos detalles de la v1.5.0) en GitHub (`LeandroReneMussi/Club_Deportivo_APK` → Releases). Rollback: v1.5.0, y v1.4.0 si hiciera falta |
| Video de demostración | Enlace de YouTube "No listado": _pendiente de grabación_ (guion y checklist en `Checklist_video_v1.5.md`) |

## Cómo abrir y correr el proyecto

1. Descomprimir el zip y abrir la carpeta en Android Studio (Ladybug o posterior, JDK 17). Gradle se sincroniza solo (`gradlew`).
2. Correr la app en un emulador o teléfono con Android 7.0 (API 24) o superior. La primera vez carga los datos de demo en SQLite (`club_deportivo.db`).
3. Cuentas: `Andres / Socio123`, `Jorge / Admin123`, `Ana / Prof123`, `Leandro / Nut123`.
4. Pruebas: `gradlew :app:testDebugUnitTest` (78 unitarias). Con un emulador conectado, `gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ar.edu.ifts29.clubdeportivo.ClubDbHelperTest` (8 pruebas de SQLite).

## Mínimos de la consigna y dónde están

| Consigna | Dónde |
|---|---|
| 1. Registrar socios y no socios | Administrativo → Socios → + Alta (nombre, apellido, DNI, correo, plan, ficha médica). Visitantes → + Ingreso (pago diario con fecha y medio de pago). |
| 2. Pago de la cuota mensual | Ficha del socio → Registrar pago en caja; socio → Pagar desde la app. Ambos cobran cuota + recargo + interés si venció y abren la cuota siguiente. |
| 3. Pago diario de no socios | Visitantes → + Ingreso; control de cupo del día y de DNI repetido; Reportes → Visitantes por fecha. |
| 4. Consultar la información | Socios (búsqueda y filtros), ficha, Reportes (4 pestañas), Inicio con indicadores, Perfil del administrador con parámetros e historial. |
| 5. Socios cuya cuota vence en la fecha | Reportes → Vencen hoy (consulta SQL por rango de fechas) y KPI "Vencen hoy". |
| 6. Validaciones | Alta/edición (obligatorios, letras, DNI 7-8 dígitos único, correo con formato), visitantes (cupo, DNI repetido), parámetros (rangos), pagos (no se cobra dos veces), control de acceso (gracia / mora / carnet). |

## Lo nuevo de la v1.5.0

- La app usa la **fecha real** del dispositivo; los datos de demo se generan relativos a hoy.
- El administrador fija **días de gracia, recargo fijo e interés mensual** con **vigencia por fecha**; el historial queda en SQLite.
- Una cuota vencida sigue habilitada durante la gracia (aviso naranja y contador de días) y pasado el plazo bloquea el acceso; la deuda suma cuota + recargo + interés calculado por tramos.
- Nombre y apellido separados en el alta, con validación de obligatorios y formato de correo.
- Pantalla **QR de ingreso** (desde el carnet digital): el QR contiene los datos del carnet (N° de socio, nombre, DNI, plan, vigencia y estado) y se lee con cualquier escáner.
- Datos de demo que cubren los casos: socios al día, pendiente, que vence hoy, **en gracia** (Rocío Paz, con carnet por vencer), en mora (Lucas, Tomás), carnet vencido (Sofía), sin apto físico (Valentina); historial de parámetros con la tasa de 2025 y la de 2026; pagos diarios de no socios de tres días distintos.

Versionado: SemVer con releases automáticos en GitHub (`release.yml`), rollback con `rollback.yml`. Política en `doc/VERSIONADO_Y_ROLLBACK.md`.
