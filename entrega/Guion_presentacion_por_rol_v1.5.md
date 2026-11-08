# Club Deportivo v1.5.0 · Guion de presentación por rol

Grupo 3 · Alvarez · Aquino Lesme · Mussi · Weitzel — Desarrollo de Aplicaciones Móviles · Etapa Entrega · 8 de noviembre de 2026.

**Duración total sugerida: 12 a 14 minutos.** Un integrante por rol. Cada bloque dice qué decir (💬), qué tocar (👉) y qué tiene que verse (✅). Todo lo que figura acá sale de la app v1.5.0 y de sus capturas; los importes y días son los que muestra la demo instalada limpia.

## Antes de empezar (preparación, 5 minutos)

- Instalar la **v1.5.0 limpia** (desinstalar la anterior) en el emulador o el teléfono: ver `INSTALAR.md`, o doble clic en `instalar.cmd`.
- La app usa la **fecha real del dispositivo**; los datos de demo se generan relativos a hoy, así que los nombres de mes y las fechas cambian, pero los estados y montos son los de este guion.
- Tener a mano un segundo celular con lector de QR (la cámara alcanza).
- Cuentas: `Jorge / Admin123` · `Andres / Socio123` · `Ana / Prof123` · `Leandro / Nut123`.

---

## 0. Apertura · todos (1 minuto)

💬 "Club Deportivo es una app Android nativa, en Kotlin con Jetpack Compose, que guarda todo en **SQLite** en el teléfono. Tiene cinco Activities, una por rol, y cubre las 14 historias de usuario del análisis más los seis mínimos de la consigna: registrar socios y no socios, pago de cuota mensual, pago diario de no socios, consultas, listado de cuotas que vencen en la fecha y validaciones."

👉 Pantalla de login. Tocar **Iniciar sesión** con los campos vacíos.
✅ "Completá usuario y contraseña." Probar una clave incorrecta: "Usuario o contraseña incorrectos." (HU-01)

---

## 1. Administrativo · `Jorge / Admin123` (6 minutos)

### 1.1 Inicio: el día del club (HU-14)
💬 "El administrativo ve primero los indicadores del día."
✅ Socios activos y habilitados · **En mora** (rojo) · **Vencen hoy** (naranja) · Esta semana · Ingresos hoy · Pagos diarios con el total recaudado. Acciones rápidas: Validar ingreso, Registrar socio, Registrar visitante y **Parámetros del club**.

### 1.2 Alta de socio con validaciones (HU-02 · consigna 1 y 6)
👉 Socios → **+ Alta**.
💬 "Nombre y apellido van por separado y son obligatorios; el DNI tiene que tener 7 u 8 dígitos y no repetirse; el correo, formato válido."
👉 Probar en orden: sin apellido → "Ingresá el apellido." · DNI con letra → "El DNI solo puede contener números." · correo sin dominio → mensaje de formato · DNI `38412907` → "Ya existe un socio con ese DNI."
👉 Alta válida: `Carlos` / `Gardel` / `12345678` / `carlos@gardel.com.ar`, plan a elección → **Registrar socio**.
✅ Ficha con N° de socio, carnet por 12 meses y cuota inicial que vence a los 10 días. Editar datos y Dar de baja están en la misma ficha (CRUD completo).

### 1.3 Padrón y estados (CRUD: leer)
👉 Socios. Buscar por nombre o DNI; filtros Todos / Habilitados / En mora.
✅ Etiquetas: verde **Habilitado**, rojo **En mora** / **Carnet venc.**, naranja **En período de gracia** (Rocío Paz, al final de la lista).

### 1.4 Período de gracia, recargo e interés (v1.5 · consigna 2 y 6)
👉 Ficha de **Rocío Paz**.
💬 "Su cuota venció hace 3 días, pero el club da 5 días de gracia: sigue habilitada, con aviso naranja y el contador de días que le quedan. Fuera de término se suman el recargo y el interés."
✅ "Habilitado · En período de gracia" con cápsula "Quedan 2 días" · deuda **$12.536** = cuota $12.000 + recargo $500 + interés $36 · carnet "Por vencer".
👉 Ficha de **Lucas Benítez**.
✅ "No habilitado · En mora", cápsula "Debe $12.848" = cuota $12.000 + recargo $500 + interés $348 (29 días al 3 % mensual).

### 1.5 Parámetros del club con vigencia (v1.5)
👉 Inicio → **Parámetros del club** (o avatar → Perfil).
💬 "Los fija el administrador: días de gracia, recargo fijo e interés mensual. Cada cambio rige desde el día en que se guarda y queda en el historial, así se ve que en 2025 regía un interés y en 2026 otro. El interés se calcula día por día con la tasa vigente en cada período, nunca hacia atrás."
✅ Vigente: gracia 5 días · recargo $500 · interés 3 %. Historial con la fila de hace un año (10 días · $300 · 2,5 %).
👉 Cambiar **gracia a 30** → Guardar. Volver a la ficha de Lucas.
✅ Lucas pasa a naranja "En período de gracia · Queda 1 día". Volver a poner **5** y guardar.

### 1.6 Control de acceso por DNI (HU-05)
👉 **Acceso**. Usar los DNI de prueba de la pantalla.
✅ `38.412.907` Permitido (al día) · `39.876.540` Permitido con aviso de gracia y deuda · `40.120.334` **Denegado**: "gracia de 5 días superada", con la deuda · `35.887.210` Denegado por carnet vencido · `11.111.111` No encontrado, con los botones **Registrar visitante** (abre el ingreso con el DNI cargado) y Registrar socio. En un permitido, **Registrar ingreso** lo suma a "Ingresos de hoy".

### 1.7 Cobro de cuota en caja (HU-04 · consigna 2)
👉 Ficha de Lucas → **Registrar pago en caja** → confirmar.
✅ Cobra el total ($12.848), la cuota queda pagada, se abre la del mes siguiente y Lucas vuelve a estar habilitado. Tocar de nuevo: "La cuota de … ya está pagada." (no se cobra dos veces).

### 1.8 Pago diario de no socios (HU-03 · consigna 3)
👉 **Visitantes**: cupos de hoy por actividad y los pagos diarios con "Recaudado hoy".
👉 **+ Ingreso**: elegir Natación → "sin cupo, el ingreso no puede realizarse". DNI `33.120.455` en otra actividad → "Ya hay un pago diario registrado hoy con el DNI 33.120.455."
👉 Un DNI nuevo, actividad con cupo, medio **Débito** → **Registrar pago diario**.
✅ Entra en la lista de hoy, baja el cupo y sube el total.

### 1.9 Reportes (HU-14 · consigna 4 y 5)
👉 **Reportes**.
✅ **Vencen hoy**: Martín Ledesma (el listado de socios cuya cuota vence en la fecha, consultado en SQLite) · **Esta semana** · **En mora**: total con recargo e interés y los parámetros vigentes · **Visitantes**: con ‹ se recorre por fecha (ayer, un día sin pagos y hace tres días); la flecha › no avanza más allá de hoy.

---

## 2. Socio · `Andres / Socio123` (2,5 minutos)

### 2.1 Inicio y contador (HU-04)
💬 "El socio ve su situación apenas entra."
✅ "Habilitado · Cuota pendiente", tarjeta de la cuota con la cápsula verde **"Vence en 2 días"**, rutina de hoy y turno de nutrición.

### 2.2 Carnet digital y QR de ingreso (HU-06 · v1.5)
👉 Pestaña **Carnet**.
✅ Carnet con formato de tarjeta: club, estado "Vigente", nombre, DNI, N° de socio, plan, foto, vigencia y estado de la cuota.
👉 **QR de ingreso** → escanear con el otro celular.
✅ El lector muestra: CLUB DEPORTIVO · CARNET DE SOCIO, N° de socio, nombre, DNI, plan, emisión y vencimiento del carnet y estado.

### 2.3 Estado de cuenta y pago desde la app (HU-04 · consigna 2)
👉 Inicio → **Pagar** (o Ver estado de cuenta → Pagar cuota) → elegir medio → confirmar.
✅ Comprobante del pago; la cuota queda pagada y el historial suma el pago. Intentar pagar de nuevo: no se cobra dos veces.

### 2.4 Rutina y nutrición (HU-10, HU-13, HU-11)
👉 **Rutina**: la asignada por la entrenadora, por día. **Nutrición**: peso, altura, IMC, objetivo y consultas. Turnos: cancelar con confirmación, reservar (sin horario elegido avisa) y confirmar.

---

## 3. Entrenadora · `Ana / Prof123` (1,5 minutos)

### 3.1 Presencia y asistencia (HU-08)
👉 Inicio → **Marcar presencia**.
✅ La tarjeta pasa a "Presente · HH:MM" con la hora real en que se marcó.
👉 **Asistencia** de la clase: presentes y ausentes.
✅ Los socios no habilitados aparecen señalados (por ejemplo, el que está en mora).

### 3.2 Rutinas con validación (HU-09)
👉 **Rutinas**: socios asignados y estado de su rutina.
✅ Un socio en mora o sin ficha médica **no puede recibir rutina** (la pantalla lo explica). Con un socio habilitado: editar la rutina; cargar series fuera de rango (válidas 1 a 20; repeticiones 1 a 100) muestra la validación.

---

## 4. Nutricionista · `Leandro / Nut123` (1,5 minutos)

### 4.1 Agenda (HU-11)
✅ Turnos asignados por día.

### 4.2 Ficha nutricional (HU-12)
👉 **Fichas** → un socio.
✅ Medidas, objetivo, antecedentes e historial de consultas.
👉 Cargar peso con letras → validación de formato; altura en centímetros → "se pide en metros". Datos válidos → la consulta se guarda en el historial.

---

## 5. Cierre · todos (1 minuto)

👉 Cerrar la app por completo y volver a abrirla.
✅ Lo hecho sigue ahí (socio nuevo, pagos, parámetros): está en `club_deportivo.db`, SQLite versión 4.

💬 "Calidad y versionado: pruebas unitarias de las reglas de negocio y pruebas instrumentadas de SQLite en el emulador; cada versión se publica en GitHub con su APK y se puede volver a la anterior con el plan de rollback. La documentación, los casos de uso con sus diagramas UML 2.0 y la guía de instalación están en la entrega."

## Preguntas que pueden hacer (respuestas cortas)

| Pregunta | Respuesta |
|---|---|
| ¿Dónde se guardan los datos? | En SQLite en el dispositivo (`club_deportivo.db`), con migraciones de versión. |
| ¿Por qué un socio vencido puede entrar? | Por el período de gracia que fija el administrador; pasado el plazo se bloquea. |
| ¿El interés cambia si cambia la tasa? | Cada día se calcula con la tasa vigente ese día; un cambio no se aplica hacia atrás. |
| ¿Qué tiene el QR? | Los datos del carnet en texto: cualquier lector los muestra. |
| ¿Hay pagos reales? | No: el pago es simulado y queda registrado en la base. |
| ¿Funciona la web? | El prototipo web es solo maqueta en memoria; la app entregada es la Android. |
