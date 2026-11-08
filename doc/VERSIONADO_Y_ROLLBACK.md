# Versionado automático y rollback — Club Deportivo

Complementa la política de calidad y pipeline (`doc/politica_calidad_pipeline_v1.2.docx`).

## 1. Versionado automático

| Qué | Cómo |
|---|---|
| Versión del código | `versionName` y `versionCode` en `app/build.gradle.kts` (SemVer `X.Y.Z`). Se actualizan al preparar una versión. |
| Publicación | Workflow **Release** (`.github/workflows/release.yml`): en cada merge a `main` corre las pruebas unitarias, crea el tag `vX.Y.Z` y una **GitHub Release** con el APK y `SHA256SUMS.txt`. |
| Versión ya publicada | Si el código declara una versión que ya tiene tag, se publica automáticamente el siguiente parche (`X.Y.Z+1`) y la release lo indica. Ninguna versión publicada se sobrescribe. |
| Integración continua | Workflow **CI** (`.github/workflows/ci.yml`): pruebas unitarias y lint (informativo) en cada push a `main`, `release/**`, `feature-**` y en cada Pull Request. El APK no se construye en CI: solo lo construye **Release** al hacer merge a `main` (o al ejecutarlo a mano desde Actions), una vez por versión. |
| APK a demanda | Workflow **APK a demanda** (`.github/workflows/apk.yml`): solo manual, desde Actions → "APK a demanda" → *Run workflow*, eligiendo la rama. Corre las pruebas (opcional) y arma el APK debug como artefacto descargable por 14 días, con la versión, la rama y el commit en el nombre. No crea tag ni Release: sirve para probar en un teléfono antes de una entrega. |

Cada release es **inmutable** y queda como punto de restauración.

## 2. Rollback

### 2.1 Rollback inmediato del APK (sin tocar el código)

1. Ir a **Releases** del repositorio y elegir la última versión estable.
2. Descargar `club-deportivo-android-vX.Y.Z.apk` y verificar el hash contra `SHA256SUMS.txt`.
3. Instalar con `adb install -r -d club-deportivo-android-vX.Y.Z.apk` (`-d` permite volver a una versión anterior).

### 2.2 Rollback del código

1. **Actions → Rollback → Run workflow**, con la versión a restaurar (ej. `v1.2.0`) y el motivo.
2. El workflow abre un Pull Request hacia `main` que deja el repositorio igual a esa versión, sin reescribir el historial.
3. Un integrante distinto del autor lo aprueba y se hace merge.
4. El workflow **Release** publica una nueva versión de parche con el contenido restaurado.

Alternativa manual: `git revert -m 1 <commit de merge>` en una rama, Pull Request hacia `main` y aprobación.

### 2.3 Qué no se hace

- No se borran tags ni releases publicadas.
- No se hace `push --force` sobre `main` ni sobre las ramas `release/**`.

## 3. Versiones publicadas y punto de rollback de cada una

| Versión | Qué trae | Base de datos | Si hay que volver |
|---|---|---|---|
| v1.3.1 | SQLite en la app Android | v2 | — |
| v1.4.0 | Vencen hoy, pagos diarios por fecha, cobro en caja completo, pruebas de SQLite | v3 | v1.3.1 |
| v1.5.0 | Fecha real, apellido separado, gracia/recargo/interés con vigencia, QR de ingreso, carnet con formato de tarjeta | v4 | v1.4.0 |
| v1.5.1 | Control de acceso: "Registrar visitante" con el DNI cargado; hora real de presencia de la entrenadora | v4 (sin cambios) | **v1.5.0** (misma base: se instala encima sin desinstalar) |

### 3.1 Plan de rollback de la v1.5.0

1. **Si falla en un teléfono de prueba:** desinstalar e instalar el APK de la release `v1.4.0` (sección 2.1). Una base v4 no se abre con la v1.4.0 (`SQLiteOpenHelper` no baja de versión), por eso se desinstala: se pierden los datos de prueba de ese teléfono y vuelve la demo de la v1.4.0.
2. **Si hay que retirar la v1.5.0 del repositorio:** Actions → **Rollback** con `v1.4.0` (sección 2.2). Queda publicada una versión de parche con el contenido de la v1.4.0.
3. **Qué se pierde al volver:** gracia/recargo/interés configurables, QR de ingreso, apellido separado y la fecha real (la v1.4.0 usa la fecha fija 8/9/2026). Lo demás de la v1.4.0 sigue igual.

## 4. Limitaciones conocidas

- El APK publicado está firmado con la clave de depuración del runner de GitHub Actions. Para distribuirlo fuera del equipo hay que configurar una clave de release propia (secreto del repositorio).
- La protección de ramas (exigir Pull Request y aprobación en `main`) no está disponible para repositorios privados en el plan gratuito de GitHub; la aprobación de un segundo integrante se cumple por procedimiento.
- Las pruebas instrumentadas (`app/src/androidTest`) no corren en GitHub Actions: se ejecutan en emulador local.
