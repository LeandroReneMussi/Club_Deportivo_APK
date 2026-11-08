@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul
title Club Deportivo - instalador
echo.
echo  Club Deportivo - instalacion en emulador o telefono
echo  ---------------------------------------------------
echo.

rem 1. Buscar adb: SDK de Android Studio, ANDROID_HOME, o el PATH.
set "ADB="
if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if not defined ADB if defined ANDROID_HOME if exist "%ANDROID_HOME%\platform-tools\adb.exe" set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
if not defined ADB for %%A in (adb.exe) do if not "%%~$PATH:A"=="" set "ADB=%%~$PATH:A"
if not defined ADB (
  echo  No se encontro adb. Instala Android Studio ^(trae el SDK^) o agrega platform-tools al PATH.
  goto :fin
)

rem 2. Buscar el APK en esta carpeta (el primero que coincida).
set "APK="
for %%F in ("%~dp0ClubDeportivo*.apk" "%~dp0club-deportivo*.apk" "%~dp0*.apk") do if not defined APK if exist "%%~fF" set "APK=%%~fF"
if not defined APK (
  echo  No hay ningun .apk en esta carpeta. Copia el APK junto a instalar.cmd.
  goto :fin
)
echo  APK: !APK!

rem 3. Verificar que haya un dispositivo conectado.
"%ADB%" start-server >nul 2>&1
set "DISP="
for /f "skip=1 tokens=1,2" %%a in ('"%ADB%" devices') do if "%%b"=="device" if not defined DISP set "DISP=%%a"
if not defined DISP (
  echo  No hay emulador ni telefono conectado. Encende un emulador ^(Android Studio ^> Device Manager^)
  echo  o conecta el telefono con Depuracion USB activada, y volve a ejecutar este archivo.
  goto :fin
)
echo  Dispositivo: !DISP!
echo.

rem 4. Instalar; si la firma no coincide, ofrecer desinstalar la version anterior.
"%ADB%" -s !DISP! install -r "!APK!" > "%TEMP%\cd_instalar.log" 2>&1
findstr /c:"Success" "%TEMP%\cd_instalar.log" >nul && goto :ok
findstr /c:"INSTALL_FAILED_UPDATE_INCOMPATIBLE" "%TEMP%\cd_instalar.log" >nul && goto :firma
type "%TEMP%\cd_instalar.log"
echo.
echo  La instalacion fallo. Revisa el mensaje de arriba.
goto :fin

:firma
echo  Hay una version anterior firmada con otra clave.
choice /c SN /m " Desinstalarla (se borran los datos de esa version) e instalar la nueva"
if errorlevel 2 goto :fin
"%ADB%" -s !DISP! uninstall ar.edu.ifts29.clubdeportivo >nul 2>&1
"%ADB%" -s !DISP! install "!APK!" | findstr /c:"Success" >nul || ( echo  La instalacion fallo. & goto :fin )

:ok
echo  Instalada. Abriendo la app...
"%ADB%" -s !DISP! shell monkey -p ar.edu.ifts29.clubdeportivo -c android.intent.category.LAUNCHER 1 >nul 2>&1
echo.
echo  Cuentas: Andres/Socio123  Jorge/Admin123  Ana/Prof123  Leandro/Nut123

:fin
echo.
pause
