# Club Deportivo v1.5.1 · Guion del coloquio de entrega

Grupo 3 · Alvarez · Aquino Lesme · Mussi · Weitzel — Desarrollo de Aplicaciones Móviles, IFTS N° 29 · Noviembre de 2026.

**Duración: 10 minutos como máximo**, repartidos entre los cuatro integrantes: apertura breve, cuatro bloques de 2 min 20 s
(funcionalidad, documentación, parte técnica, decisiones y enfoque) y cierre. Cada bloque dice qué decir (💬) y qué mostrar si el
coloquio permite apoyarse en la pantalla (👉). Las frases marcadas ✂ se saltean si el bloque anterior se pasó de tiempo. Todo lo que
figura acá sale de la app v1.5.1, de su README y de los documentos de la entrega (`README_entrega_v1.5.md`, casos de uso UML,
`doc/VERSIONADO_Y_ROLLBACK.md`).

## Reparto y tiempos

| Minuto | Bloque | Quién |
|---|---|---|
| 0:00 – 0:20 | Apertura | Integrante 1 |
| 0:20 – 2:40 | 1. Funcionalidad | Integrante 1 |
| 2:40 – 5:00 | 2. Documentación | Integrante 2 |
| 5:00 – 7:20 | 3. Parte técnica | Integrante 3 |
| 7:20 – 9:40 | 4. Decisiones y enfoque | Integrante 4 |
| 9:40 – 10:00 | Cierre | Integrante 4 |

Los nombres se completan al repartir los bloques (conviene que cada uno tome la parte que mejor conoce). Uno de los cuatro lleva el
cronómetro: si un bloque llega a 2:40, el que sigue arranca igual.

## Antes de entrar

- Si se va a mostrar la app: **v1.5.1 instalada limpia** (desinstalar la anterior) en el emulador o el teléfono (`INSTALAR.md` o doble
  clic en `instalar.cmd`). Cuentas: `Jorge / Admin123` · `Andres / Socio123` · `Ana / Prof123` · `Leandro / Nut123`.
- Si no se puede mostrar la app: tener abiertos el PDF de capturas y el PDF de casos de uso de la carpeta de entrega, y la página de
  **Releases** del repositorio `LeandroReneMussi/Club_Deportivo_APK`.
- El guion funciona hablado, sin mostrar nada: lo visual es apoyo, no requisito.

---

## Apertura · Integrante 1 (0:20)

💬 «Buenas. Somos el Grupo 3 y presentamos **Club Deportivo**, una app Android nativa para la gestión de un club: socios, cuotas,
control de acceso, visitantes, rutinas y nutrición. En diez minutos vamos a contar qué hace, cómo está documentada, cómo está
construida y por qué tomamos las decisiones que tomamos.»

---

## 1. Funcionalidad · Integrante 1 (2:20)

👉 Login con las cuentas de prueba; si hay tiempo, el inicio del administrativo con los indicadores del día.

💬 «La app tiene **cuatro roles con cuenta**: socio, administrativo, entrenadora y nutricionista. El rol se deduce de la cuenta, sin
selector de perfil. Cubre las **14 historias de usuario** del análisis, que se especificaron en **26 casos de uso**.

Los **seis mínimos de la consigna** están todos:

1. **Registrar socios y no socios.** El alta pide nombre y apellido por separado, DNI de 7 u 8 dígitos que no se repita, correo con
   formato válido, plan y ficha médica; genera el número de socio, el carnet por doce meses y la primera cuota. El no socio se registra
   con un pago diario: actividad, medio de pago y control de cupo del día.
2. **Pago de la cuota mensual.** Lo cobra el administrativo en caja o lo paga el socio desde la app. Si la cuota venció, cobra cuota
   más recargo más interés, abre la cuota del mes siguiente y no deja cobrar dos veces.
3. **Pago diario de no socios**, con cupo por actividad, rechazo del DNI que ya pagó hoy y reporte por fecha.
4. **Consultas**: padrón con búsqueda y filtros, ficha del socio, indicadores del inicio y reportes en cuatro pestañas.
5. **Socios cuya cuota vence en la fecha**: la pestaña “Vencen hoy” de Reportes, que es una consulta SQL por rango de fechas, y el
   indicador del inicio.
6. **Validaciones** en todos los formularios, con mensajes concretos.

La regla central es la **habilitación**. Con la cuota al día el socio está habilitado. Cuando vence entra en un **período de
gracia** que fija el administrador: sigue entrando, con aviso y un contador de días. Pasada la gracia queda **en mora**: el acceso
se deniega hasta que regularice, y la deuda suma la cuota, un recargo fijo y un interés mensual calculado día por día. El carnet
vencido también bloquea.

👉 Acceso por DNI: un permitido, uno en gracia, uno denegado.

💬 «El **control de acceso por DNI** devuelve cuatro resultados: permitido, permitido con aviso de gracia, denegado por mora o carnet,
y no encontrado, que ofrece registrar al visitante o al socio en el momento.

✂ El socio ve su estado de cuenta, paga, tiene el carnet digital con un **QR de ingreso**, su rutina, su información nutricional y sus
turnos. La entrenadora marca presencia con la hora real, toma asistencia y arma rutinas; un socio en mora o sin apto físico no
puede recibir rutina. La nutricionista maneja su agenda y las fichas con historial de consultas.

Todo lo que se hace queda guardado en **SQLite en el dispositivo**: si se cierra la app y se vuelve a abrir, está.»

---

## 2. Documentación · Integrante 2 (2:20)

👉 Carpeta de entrega; el PDF de casos de uso abierto en el diagrama de casos de uso.

💬 «La entrega tiene todo lo que hace falta para instalar, entender y mantener la app.

**Qué entregamos.** El proyecto Android Studio comprimido. El **APK v1.5.1** publicado en las Releases del repositorio con su hash
SHA-256. Un PDF con **72 capturas** de las cinco Activities, cada una con su epígrafe y la historia de usuario que cubre. El
documento de **casos de uso UML 2.0**: 26 casos con una misma plantilla (actores, precondiciones, disparador, flujo principal,
flujos alternativos y de excepción con los mensajes reales de la app, postcondiciones, reglas de negocio y datos de demo), un diagrama
de secuencia por caso, el diagrama de casos de uso, el de clases del dominio, la máquina de estados de la cuota y el diagrama de
actividad del alta. La **documentación técnica** en Word y PDF. La **guía de instalación** para teléfono, emulador y Android Studio.
El README de entrega con los seis mínimos y dónde está cada uno. La política de calidad y pipeline, el documento de versionado y
rollback, el diseño exportado de Figma y el **video de demostración** en YouTube. ✂ También el registro de uso de herramientas de
IA, porque las usamos como apoyo y lo dejamos escrito.

**Cómo la hicimos.** La fuente de verdad es el **código**. Los casos de uso se escribieron leyendo el ViewModel, las reglas de
negocio, el acceso a la base y las pruebas unitarias: de las pruebas salen los flujos alternativos y los mensajes exactos. Lo que no
surge del código quedó marcado “a confirmar” en lugar de inventarlo.

Los documentos Word y PDF se **generan con scripts** a partir de los fuentes Markdown y de las capturas, así cada versión sale con
el mismo formato y no hay que retocarlos a mano. Las capturas las toman pruebas instrumentadas que recorren cada Activity, y por
eso coinciden con la versión publicada.

✂ Cada versión publicada tiene su fila en el documento de versionado: qué trae, qué versión de base de datos usa y a qué versión
se vuelve si hay que retroceder.»

---

## 3. Parte técnica · Integrante 3 (2:20)

👉 Estructura del proyecto en Android Studio o la sección “Estructura” del README; la página de Actions o Releases de GitHub.

💬 «Es una app **nativa en Kotlin con Jetpack Compose**, para Android 7 en adelante (API 24) y compilada contra la API 35.

**Activities.** Son cinco: `LoginActivity`, que es el punto de entrada, y una por rol. Al iniciar sesión se abre la Activity del
rol y la del login se cierra; cerrar sesión vuelve al login. Las Activities de rol no se exportan y, si se abren sin una sesión de su
rol, vuelven al login. Dentro de cada una, la navegación es una **pila de pantallas**: las raíces de la barra inferior reemplazan la
pila y el resto se apila con un encabezado con flecha; el botón atrás del sistema desapila igual.

**Capas.** `ui` tiene el tema, los widgets, el chrome y las pantallas. `vm` tiene el `ClubViewModel`, con un estado inmutable que
Compose observa, `SocioVista`, que concentra las reglas derivadas como habilitación, etiquetas y deuda, y `ClubRepositorio`, que
comparte la sesión y la base entre las Activities. `data` tiene los modelos, las validaciones, el formato de fechas e importes, los
datos de demo y el acceso a SQLite.

**Persistencia.** `SQLiteOpenHelper` con un esquema en **versión 4** y migraciones desde la 2. Cada cambio de estado pasa por el
ViewModel, que le pide al repositorio guardar; se escribe en una transacción y solo si los datos cambiaron. La primera vez que se
abre la app se siembran los datos de demo, relativos a la fecha del día.

**Pruebas.** **78 unitarias** sobre la lógica: las reglas de las 14 historias, los cuatro casos del control de acceso, las
validaciones del alta, el bloqueo por cupo, el bloqueo de rutina, la pila de navegación, fechas e importes. Y **14 instrumentadas**
en el emulador: 8 de SQLite (persistencia al cerrar y reabrir, vencen hoy y esta semana, pagos por fecha, DNI repetido, migraciones y
parámetros) y 6 recorridos de pantallas, que son los que generan las capturas.

**Pipeline en GitHub.** Trabajamos en ramas de feature con **Pull Request** revisado por otro integrante. El workflow de **CI** corre
las pruebas y el lint en cada push y en cada PR. Al hacer merge a `main`, **Release** crea el tag SemVer y publica el APK con su
SHA-256; si la versión ya tiene tag, publica el siguiente parche. **Rollback** abre un PR que deja `main` igual a una versión
publicada, sin reescribir el historial. ✂ Y “APK a demanda” arma un APK de cualquier rama para probar en un teléfono antes de una
entrega. Cada release es inmutable y es un punto de restauración: hoy están la 1.3.1, la 1.4.0, la 1.5.0 y la 1.5.1.»

---

## 4. Decisiones y enfoque · Integrante 4 (2:20)

👉 Sección “Cómo se portó el diseño” del README, o la pantalla “Parámetros del club” con el historial.

💬 «**El enfoque.** Partimos del prototipo HTML y del diseño en Figma, con la dirección visual que se pidió, y lo portamos a nativo con
un mapeo explícito: el estado del componente del prototipo pasó a un estado inmutable del ViewModel, las variables de color pasaron
a tokens de Compose con los mismos nombres, y las tipografías son las mismas. Avanzamos en **versiones chicas**, cada una publicada
con su release y su punto de rollback, en lugar de una entrega grande al final.

**Decisiones de producto.**

- **SQLite** desde la v1.3, en lugar de datos en memoria: lo que hace el usuario sobrevive al cierre, y “vencen hoy” es una consulta
  real a la base, no un filtro sobre una lista.
- **Fecha real del dispositivo** desde la v1.5, con datos de demo generados relativos a hoy: la demo cuenta la misma historia
  cualquier día que se muestre.
- **Parámetros del club con vigencia**: días de gracia, recargo e interés los fija el administrador; cada cambio rige desde que se
  guarda y queda en el historial. El interés se calcula día por día con la tasa vigente en cada período y nunca hacia atrás.
- **Sin backend ni pasarela de pago**: el pago se registra en la base, como se definió en el diseño. Es el alcance de un prototipo
  académico y lo decimos así.
- El **QR del carnet** lleva los datos en texto, para que cualquier lector lo muestre sin una app propia.

**Decisiones técnicas.** Fechas propias en vez de `java.time`, para quedarnos en API 24 sin desugaring: la app usa pocas
operaciones de fecha y se resuelven con el algoritmo civil de Hinnant. Íconos de Material en lugar de los glifos Unicode del
prototipo, que Android no dibuja. Respetar las barras del sistema en vez de dibujar un marco de teléfono. Las cuentas de prueba
se mudaron adentro del login. ✂ Donde la maqueta tenía huecos, como el número de ejercicio de la rutina o el IMC sin datos, la app
los resuelve. Cada desvío de la maqueta está escrito en el README con su porqué.

**Calidad.** Nada se publica sin pruebas en verde y sin Pull Request; nada publicado se sobrescribe; siempre hay una versión a la
que volver.»

---

## Cierre · Integrante 4 (0:20)

💬 «En resumen: Club Deportivo v1.5.1 cubre los seis mínimos y las 14 historias de usuario, tiene la documentación generada desde el
código, es una app nativa con SQLite y un pipeline que nos deja publicar y volver atrás con seguridad. Quedamos a disposición para
las preguntas.»

---

## Preguntas probables (respuestas cortas)

| Pregunta | Respuesta |
|---|---|
| ¿Dónde se guardan los datos? | En SQLite en el dispositivo (`club_deportivo.db`), esquema versión 4, con migraciones desde la versión 2. |
| ¿Por qué un socio con la cuota vencida puede entrar? | Por el período de gracia que fija el administrador; pasado ese plazo queda en mora y el acceso se deniega. |
| ¿Cómo se calcula la deuda? | Cuota + recargo fijo + interés mensual calculado día por día; cada día usa la tasa vigente en ese momento. |
| ¿Qué pasa si cambian la tasa o la gracia? | Rige desde la fecha en que se guarda y queda en el historial; no se aplica hacia atrás. |
| ¿Hay pagos reales? | No: el pago es simulado y queda registrado en la base. No hay backend ni pasarela. |
| ¿Por qué SQLite directo y no un ORM? | Alcance acotado y control total del esquema y de cada migración de versión; sin dependencias extra. |
| ¿Cómo probaron? | 78 pruebas unitarias de la lógica y 14 instrumentadas en el emulador (SQLite y recorridos de pantallas); CI en cada push y PR. |
| ¿Qué pasa si una versión falla? | Se reinstala el APK de la release anterior (`adb install -r -d`) o se corre el workflow Rollback, que abre un PR hacia `main` con esa versión. |
| ¿Por qué una Activity por rol? | Aísla la sesión y la navegación de cada rol; sin sesión del rol, la Activity vuelve al login. |
| ¿Qué tiene el QR? | Los datos del carnet en texto: número de socio, nombre, DNI, plan, vigencia y estado de la cuota. |
| ¿Y la versión web? | Es el prototipo original, en memoria; la entrega es la app Android. |
| ¿Usaron herramientas de IA? | Como apoyo, con revisión del equipo en cada resultado; está registrado en la documentación de la entrega. |
