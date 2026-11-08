# Club Deportivo v1.5.0 · Guion y checklist del video de demostración

Grupo 3 · Alvarez, Aquino Lesme, Mussi, Weitzel · Etapa Entrega · 8 de noviembre de 2026.

Duración sugerida: 8 a 10 minutos. Grabar con la **v1.5.0 instalada limpia** (desinstalar cualquier
versión anterior antes, así carga los datos de demo nuevos). APK: release v1.5.0 en GitHub
(`Club_Deportivo_APK` → Releases) o `app-debug.apk` del zip. La app usa la **fecha real del
teléfono**: los datos de demo se acomodan a hoy (Lucas vencido hace 29 días, Andrés vence en 2,
Martín vence hoy), así que lo que se dice abajo vale cualquier día que se grabe.

Cuentas: `Andres / Socio123` (socio) · `Jorge / Admin123` (administrativo) · `Ana / Prof123` (entrenadora) · `Leandro / Nut123` (nutricionista).

## 1. Presentación (30 s)

- [ ] Decir: app Android nativa (Kotlin + Jetpack Compose) con **SQLite** en el dispositivo; cinco roles, una Activity por rol.
- [ ] Mostrar el login y las cuentas de prueba. Probar un login inválido (validación).

## 2. Registrar socios y no socios · consigna 1 (2 min)

- [ ] Entrar como `Jorge`. Socios → **+ Alta**. Mostrar que **Nombre y Apellido son campos separados**.
- [ ] Validaciones: confirmar sin apellido ("Ingresá el apellido."), DNI con letra, correo sin formato, DNI repetido `38412907` ("Ya existe un socio con ese DNI.").
- [ ] Alta válida (por ejemplo `Carlos` / `Gardel` / `12345678` / `carlos@gardel.com.ar`): se genera N° de socio, carnet por 12 meses y cuota inicial con vencimiento a 10 días.
- [ ] Visitantes → **+ Ingreso**: no socio con nombre, DNI, actividad, **medio de pago** (Efectivo / Débito) y total. Probar DNI `33.120.455` (ya pagó hoy → rechazado) y luego otro DNI: entra en la lista de hoy y suma al total recaudado.

## 3. Pago de cuota y regla de gracia · consignas 2 y 6 (2,5 min)

- [ ] Socios → **Rocío Paz**: la demo ya trae una socia **en gracia** (vencida hace 3 días, gracia de 5): franja naranja "Habilitado · En gracia · Quedan 2 días", deuda $12.536 (cuota + recargo $500 + 3 días de interés) y carnet "Por vencer". Acceso → DNI `39876540`: **Permitido** con el aviso de gracia.
- [ ] Socios → **Lucas Benítez**: en mora (vencida hace 29 días), franja roja "Debe $12.848": cuota $12.000 + recargo $500 + interés $348 (29 días al 3 % mensual). Acceso → `40120334`: **Denegado**, "gracia de 5 días superada".
- [ ] Inicio → acción rápida **Parámetros del club** (o avatar → Perfil): se ve la tasa vigente (gracia 5, recargo $500, interés 3 %) y el **historial por fecha**: la fila de 2025 (gracia 10, $300, 2,5 %) y la de 2026. Explicar: lo fija el administrador, rige desde la fecha en que se guarda y no se aplica hacia atrás; el interés se calcula día por día con la tasa de cada período.
- [ ] Cambiar **gracia a 30** → Guardar. Socios → Lucas pasa a naranja "En gracia · Queda 1 día"; Acceso → `40120334`: **Permitido** con aviso. Volver a gracia 5 → Guardar → Denegado otra vez.
- [ ] Ficha de Lucas → **Registrar pago en caja**: cobra el total ($12.848), la cuota queda pagada, se abre la del mes siguiente y queda habilitado. Tocar de nuevo "Registrar pago": aviso de cuota ya pagada (no se cobra dos veces).

## 4. Consultar y listar · consignas 4 y 5 (1,5 min)

- [ ] Inicio del administrativo: KPIs (Vencen hoy, Esta semana, En mora, Pagos diarios).
- [ ] **Reportes → Vencen hoy**: Martín Ledesma (consigna 5: socios cuya cuota vence en la fecha).
- [ ] Reportes → Esta semana, En mora (total $31.368 con recargo e interés y parámetros vigentes), **Visitantes** con el selector de fecha (‹ al día anterior: 2 pagos; ‹‹ tres días atrás: otros 2; en el medio un día vacío).
- [ ] Control de acceso con los cuatro casos: al día, en mora, carnet vencido, DNI inexistente.

## 5. El socio ve su situación (1 min)

- [ ] Salir y entrar como `Andres`: Inicio con "Habilitado · Cuota pendiente" y el **contador "Vence en 2 días"** en verde; Cuotas; pagar desde la app (total, medio de pago, comprobante). Pagar de nuevo: no se cobra dos veces.
- [ ] **Carnet → botón "QR de ingreso"**: se abre la pantalla con el código grande; escanearlo con otro celular muestra N° de socio, nombre, DNI, plan, vigencia del carnet y estado de la cuota (los datos del carnet digital).

## 6. SQLite y cierre (1 min)

- [ ] Cerrar la app por completo y volver a abrirla: los cambios siguen (socio nuevo, pago, parámetros). Decir que todo se guarda en `club_deportivo.db` (SQLite, versión 4 del esquema).
- [ ] Mencionar: 70 pruebas unitarias y 8 pruebas instrumentadas de SQLite; versionado con releases en GitHub y rollback a la versión anterior.

## Antes de subir

- [ ] Video en YouTube como **No listado**; pegar el enlace en la documentación y en el campus.
- [ ] Verificar que el zip y el PDF digan **Grupo 3** y los cuatro apellidos.
