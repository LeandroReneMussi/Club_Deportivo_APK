# Cómo instalar Club Deportivo (v1.5.1)

Tres caminos, del más simple al más técnico. Cualquiera sirve para probar la app o grabar la demo.

**Cuentas de prueba:** `Andres / Socio123` (socio) · `Jorge / Admin123` (administrativo) · `Ana / Prof123` (entrenadora) · `Leandro / Nut123` (nutricionista).

**Requisito:** Android 7.0 o superior (API 24).

---

## 1. En un teléfono Android (lo más fácil)

1. Descargá el APK:
   - desde GitHub: repositorio `LeandroReneMussi/Club_Deportivo_APK` → **Releases** → `v1.5.1` → archivo `.apk`;
   - o usá el que viene en la carpeta de entrega: `ClubDeportivo-v1.5.1-debug.apk`.
2. Pasalo al teléfono (cable, Drive, WhatsApp, correo) y tocalo.
3. Si Android pregunta, permití **"Instalar apps desconocidas"** para la app con la que lo abriste (Archivos, Chrome, Drive).
4. Tocá **Instalar** y después **Abrir**.

> **Si ya tenías una versión anterior** y aparece "La app no está instalada" o "Conflicto con un paquete existente": desinstalá primero la anterior (mantené apretado el ícono → Desinstalar) y volvé a instalar. Pasa cuando la versión vieja se firmó con otra clave.
>
> **Para ver los datos de demo nuevos** conviene instalar limpio (desinstalar antes). Una instalación que se actualiza conserva sus datos.

## 2. En el emulador de Android Studio

**Opción A — arrastrar y soltar (sin comandos)**

1. Abrí Android Studio → **Device Manager** → ▶ en cualquier dispositivo virtual (por ejemplo Pixel, API 34 o 35).
2. Cuando termine de arrancar, **arrastrá el archivo `.apk` sobre la ventana del emulador**. Se instala solo.
3. Abrí "Club Deportivo" desde el cajón de apps.

**Opción B — doble clic en `instalar.cmd` (Windows)**

Con un emulador encendido o un teléfono conectado por USB (con *Depuración USB* activada):

1. Poné `instalar.cmd` en la misma carpeta que el `.apk`.
2. Hacé doble clic. El script busca `adb` en el SDK de Android, instala el APK y abre la app.
3. Si detecta que hay una versión con otra firma, te pregunta antes de desinstalarla.

## 3. Desde el código, con Android Studio

1. Descomprimí el zip del proyecto (o `git clone` del repositorio) y abrí la carpeta `Club_Deportivo_APK` en Android Studio (Ladybug o posterior, JDK 17, que ya viene incluido).
2. Esperá a que termine la sincronización de Gradle.
3. Elegí el dispositivo (emulador o teléfono) y tocá **Run ▶** (configuración `app`).

Por consola, desde la carpeta del proyecto:

```
gradlew :app:installDebug          (compila e instala en el dispositivo conectado)
gradlew :app:testDebugUnitTest     (pruebas unitarias)
```

## Problemas frecuentes

| Síntoma | Solución |
|---|---|
| "Conflicto con un paquete existente" / `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Desinstalar la versión anterior y volver a instalar. |
| No aparecen los socios nuevos de la demo (Rocío Paz, etc.) | La base de una versión anterior se conserva; desinstalar e instalar limpio. |
| `instalar.cmd` dice que no encuentra `adb` | Instalar Android Studio (trae el SDK) o agregar `platform-tools` al PATH. |
| `instalar.cmd` no ve el dispositivo | Encender el emulador antes, o en el teléfono activar Opciones de desarrollador → Depuración USB y aceptar el aviso. |
| Las fechas no coinciden con las de las capturas | La app usa la fecha real del dispositivo; los datos de demo se generan relativos a hoy, así que las fechas cambian pero la historia es la misma. |

## Volver a la versión anterior

Ver `doc/VERSIONADO_Y_ROLLBACK.md`. En resumen: descargar el APK de la release `v1.4.0` e instalarlo con `adb install -r -d <apk>` (o desinstalar e instalar).
