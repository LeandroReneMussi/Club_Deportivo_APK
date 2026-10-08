# Club Deportivo — app Android (Kotlin + Jetpack Compose)

Implementación del prototipo `project/export/ClubDeportivo-App-v4.html` (Claude Design)
como app Android nativa. Cubre las **14 historias de usuario** del documento de análisis
en los **5 roles**, con la dirección visual **1a Pista**.

Es un prototipo académico con **SQLite en el dispositivo**. El alta, la modificación,
la baja, el cobro de la cuota y el listado se guardan en `club_deportivo.db` y siguen
ahí al cerrar la app. No hay backend ni pasarela de pago: el pago se registra en la
base, tal como se definió en el diseño.

---

## Compilar el APK

El proyecto no trae el SDK de Android; hace falta tenerlo instalado.

**Con Android Studio** (lo más simple)

1. `File → Open` y elegir la carpeta raíz del repositorio.
2. Esperar el Gradle Sync (baja AGP, Kotlin y Compose la primera vez).
3. `Run ▶` con un emulador o un teléfono conectado, o `Build → Build APK(s)`.

**Por línea de comandos**

```bash
./gradlew assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease   # app/build/outputs/apk/release/app-release.apk
./gradlew test              # 57 pruebas unitarias de la lógica
./gradlew connectedDebugAndroidTest   # capturas por Activity en un emulador
```

`assembleRelease` está firmado a propósito con la clave de debug, así que el APK que
genera se puede instalar y compartir sin configurar un keystore. Para una publicación
real hay que reemplazar esa firma en `app/build.gradle.kts`.

Requisitos: JDK 17+, SDK de Android con la plataforma **35**. `minSdk` es 24 (Android 7).

---

## Cuentas de prueba

El rol se deduce de la cuenta; no hay selector de perfil (HU-01).

| Usuario   | Clave      | Rol            |
|-----------|------------|----------------|
| `Andres`  | `Socio123` | Socio          |
| `Jorge`   | `Admin123` | Administrativo |
| `Ana`     | `Prof123`  | Entrenadora    |
| `Leandro` | `Nut123`   | Nutricionista  |

La pantalla de login las lista y un toque completa el formulario. En la maqueta esas
credenciales estaban en el panel lateral del canvas, fuera del teléfono; acá no hay
panel, así que se movieron adentro de la app para que la demo sea usable.

---

## Recorridos para probar

- **Socio** → pagar la cuota de septiembre y mirar el carnet digital.
- **Administrativo** → Acceso: probar `40.120.334` (en mora, denegado) y `35.887.210`
  (carnet vencido); renovar el carnet desde su ficha y revalidar.
- **Administrativo** → Visitantes: Natación está sin cupo y el sistema bloquea el ingreso.
- **Entrenadora** → Rutinas: Lucas Benítez no puede recibir rutina hasta regularizar.
- **Nutricionista** → abrir un turno de la agenda y registrar la consulta.

---

## Estructura

```
app/src/main/java/ar/edu/ifts29/clubdeportivo/
├── LoginActivity.kt     Entry point (LAUNCHER): login y derivación al rol
├── RolActivity.kt       SocioActivity, AdminActivity, EntrenadoraActivity, NutricionistaActivity
├── data/
│   ├── Format.kt        Fecha propia, formato es-AR de importes, DNI, edades
│   ├── Models.kt        Socio, Cuota, Rutina, FichaNutricional, Screen, Rol…
│   ├── Validaciones.kt  Datos incompletos y formatos incorrectos de los formularios
│   ├── DemoData.kt      Semilla de la base y tablas por rol
│   └── db/
│       └── ClubDbHelper.kt  SQLiteOpenHelper: tablas, ABM, login y listado
├── vm/
│   ├── SocioVista.kt    Reglas derivadas: habilitación, badges, etiquetas
│   ├── ClubRepositorio.kt Sesión y acceso a SQLite compartido entre Activities
│   └── ClubViewModel.kt Estado y acciones de las 14 HU y el CRUD de socios
└── ui/
    ├── theme/Theme.kt   Tokens de la dirección Pista + tipografías Barlow
    ├── Widgets.kt       Tarjetas, filas, campos, botones, avatares
    ├── Chrome.kt        Header, barra inferior, toast, action sheets
    ├── ClubApp.kt       Raíz: chrome + despacho de pantallas
    └── screens/         Login, Socio, Empleado, Profesor, Nutricionista
```

**Activities.** `LoginActivity` es el entry point. Al iniciar sesión abre la Activity del
rol (`SocioActivity`, `AdminActivity`, `EntrenadoraActivity` o `NutricionistaActivity`) y
se cierra; «Cerrar sesión» vuelve a `LoginActivity`. Las Activities de rol no se exportan
y, si se abren sin una sesión de su rol, vuelven al login. Los datos viven en
SQLite (`ClubDbHelper`) y `ClubRepositorio` los comparte entre Activities. La primera
vez se cargan los datos de demostración; después, cada alta, baja, cobro o listado
lee y escribe la base.

Dentro de cada Activity, la navegación replica la del prototipo: una **pila de pantallas**
donde las raíces (las de la barra inferior) reemplazan la pila y el resto se apila con un
header con flecha. El botón atrás del sistema desapila igual.

---

## Diseño

El prototipo de la app está en Figma:
[CLUB DEPORTIVO MOBILE](https://www.figma.com/design/I4Izi6iGsklxyC2cyQiSOv/CLUB-DEPORTIVO-MOBILE--Copia-?node-id=0-1&t=leUzkiidK3gHrWAz-1).
La exportación en PDF y el detalle están en [`doc/diseno/`](doc/diseno/README.md).

---

## Cómo se portó el diseño

La maqueta es HTML/CSS con un componente único al estilo React. El mapeo:

| Maqueta | App |
|---|---|
| `sc-if` / `sc-for` sobre `renderVals()` | `when` sobre `Screen` y `forEach` en Compose |
| `this.state` + `setState` | `UiState` inmutable en `mutableStateOf` dentro del ViewModel |
| `socioView(s)` | `SocioVista`, con las mismas reglas derivadas |
| Variables CSS `--bg`, `--acc`… en OKLCH | `object Pista` con los mismos nombres, convertidos a sRGB |
| `font: 700 32px var(--disp)` | `disp(700, 32)` — Barlow Condensed, versalita, `letter-spacing` .5 |

**Decisiones que se apartan de la maqueta, y por qué:**

- **Sin marco de teléfono ni barra de estado dibujada.** En el canvas eran el encuadre
  del mockup; en un teléfono real esa franja es la barra del sistema, así que se
  respetan los insets.
- **Sin el "home indicator" de iOS.** En Android esa franja la ocupa la barra de gestos;
  se reserva su inset en lugar de dibujar la barrita. El resto del tab bar mantiene el
  aire de iOS que se pidió en el diseño (glifo grande, etiqueta chica), igual que el
  action sheet de cierre de sesión.
- **Íconos en lugar de glifos Unicode.** La maqueta usa `⌂ ▭ ≣ ◐ ⚇ ⊙ ⊕ ▤ ☑ ▦`, que el
  navegador resuelve con la fuente del sistema. En Android varios de esos caracteres no
  existen en la fuente por defecto y se verían como cuadraditos, así que se usan los
  íconos de Material equivalentes.
- **Los ejercicios de la rutina se numeran.** La maqueta dibuja el círculo del número
  pero deja el dato sin enlazar (`{{ e.n }}` no existe en el modelo), así que en el
  prototipo se ve vacío. Acá se numera el ejercicio, que es lo que el diseño buscaba.
- **Un alta nueva muestra `—` en vez de `NaN` en el IMC.** Sin peso ni altura cargados
  el cálculo del prototipo da `NaN`; se muestra un guión hasta la primera consulta.
- **Sin panel lateral ni conmutador de temas.** Eran ayudas de revisión del canvas.
  Las cuentas de prueba, que sí hacían falta, se mudaron al login.
- **Fechas propias en vez de `java.time`.** `java.time` pide API 26 o desugaring; la
  app necesita apenas cinco operaciones sobre fechas, así que `Fecha` las resuelve con
  el algoritmo civil de Hinnant y `minSdk` se queda en 24.

El "hoy" está congelado en el **8 de septiembre de 2026**, igual que el prototipo
(`const TODAY = new Date(2026, 8, 8, 9, 41)`). Es lo que hace que los datos de demo
cuenten la historia diseñada: Lucas en mora, Sofía con el carnet vencido, la cuota de
Andrés por vencer en dos días. Está en un solo lugar, `data/Format.kt`.

---

## Pruebas

`./gradlew test` corre 57 pruebas sobre la lógica: las reglas de las 14 HU, la navegación entre Activities, el CRUD de socios y las validaciones
(`ClubViewModelTest`) y las utilidades de fecha e importe (`FormatoTest`). Cubren la
regla central de habilitación, los cuatro casos del control de acceso, las validaciones
de alta, el bloqueo por cupo, el bloqueo de rutina por mora o falta de apto físico y el
comportamiento de la pila de navegación.

---

## Cobertura de las historias de usuario

| HU | Qué | Dónde |
|----|-----|-------|
| HU-01 | Inicio de sesión por rol | `PantallaLogin` |
| HU-02 | Registrar socio (carnet + cuota inicial + ficha médica) | `PantallaAlta` |
| HU-03 | Visitantes con control de cupo | `PantallaVisitantes`, `PantallaVisitanteNuevo` |
| HU-04 | Consultar y pagar cuota | `PantallaCuotas`, `PantallaPagar`, ficha del socio |
| HU-05 | Control de acceso por DNI | `PantallaAcceso` |
| HU-06 | Carnet digital | `PantallaCarnet` |
| HU-07 | Emitir / renovar carnet | `PantallaFichaSocio` |
| HU-08 | Registrar asistencia | `PantallaAsistencia` |
| HU-09 | Crear y asignar rutina, con validación | `PantallaRutinasProfesor`, `PantallaRutinaEdit` |
| HU-10 | Consultar rutina asignada | `PantallaRutinaSocio` |
| HU-11 | Turnos de nutrición (socio y profesional) | `PantallaTurno`, `PantallaAgenda` |
| HU-12 | Ficha nutricional | `PantallaFichaNutricional` |
| HU-13 | Información nutricional propia | `PantallaNutriSocio` |
| HU-14 | Reportes: por vencer y en mora | `PantallaReportes` |
