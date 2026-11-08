# -*- coding: utf-8 -*-
"""Escribe la fuente PlantUML equivalente de cada diagrama en doc/casos-de-uso/diagramas/."""
import os

DIAG = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "diagramas")

PUML = {}

PUML["01_casos_de_uso.puml"] = """@startuml 01_casos_de_uso
title Diagrama de casos de uso — App Club Deportivo v1.5.0
left to right direction
skinparam usecase {
  BackgroundColor #E9F2FF
  BackgroundColor<<v15>> #FFE0B2
}
actor "Usuario del club" as U
actor Socio as S
actor Administrativo as A
actor Entrenadora as P
actor Nutricionista as N
S --|> U
A --|> U
P --|> U
N --|> U
rectangle "App Club Deportivo" {
  usecase "CU-01 Iniciar sesión" as CU01
  usecase "CU-02 Cerrar sesión" as CU02
  usecase "CU-03 Consultar socios" as CU03
  usecase "CU-04 Registrar socio" as CU04
  usecase "CU-05 Editar socio" as CU05
  usecase "CU-06 Dar de baja socio" as CU06
  usecase "CU-07 Registrar pago de cuota en caja (v1.5)" as CU07 <<v15>>
  usecase "CU-08 Emitir / renovar carnet" as CU08
  usecase "CU-09 Validar ingreso por DNI (v1.5)" as CU09 <<v15>>
  usecase "CU-10 Registrar ingreso del socio" as CU10
  usecase "CU-11 Registrar pago diario de no socio" as CU11
  usecase "CU-12 Consultar reportes (v1.5)" as CU12 <<v15>>
  usecase "CU-13 Configurar parámetros del club (v1.5)" as CU13 <<v15>>
  usecase "CU-14 Pagar cuota desde la app (v1.5)" as CU14 <<v15>>
  usecase "CU-15 Consultar estado de cuenta y contador de días (v1.5)" as CU15 <<v15>>
  usecase "CU-16 Ver carnet digital" as CU16
  usecase "CU-17 Consultar rutina asignada" as CU17
  usecase "CU-18 Solicitar turno de nutrición" as CU18
  usecase "CU-19 Cancelar turno de nutrición" as CU19
  usecase "CU-20 Consultar información nutricional propia" as CU20
  usecase "CU-21 Registrar presencia propia" as CU21
  usecase "CU-22 Registrar asistencia de clase" as CU22
  usecase "CU-23 Crear y asignar rutina" as CU23
  usecase "CU-24 Consultar agenda de turnos" as CU24
  usecase "CU-25 Registrar consulta nutricional" as CU25
  usecase "Calcular estado y deuda de la cuota:\\ngracia, recargo e interés (v1.5)" as CALC <<v15>>
}
U --> CU01
U --> CU02
A --> CU03
A --> CU04
A --> CU05
A --> CU06
A --> CU07
A --> CU08
A --> CU09
A --> CU11
A --> CU12
A --> CU13
S --> CU14
S --> CU15
S --> CU16
S --> CU17
S --> CU18
S --> CU19
S --> CU20
P --> CU21
P --> CU22
P --> CU23
N --> CU24
N --> CU25
CU10 .> CU09 : <<extend>>
CU07 .> CALC : <<include>>
CU09 .> CALC : <<include>>
CU14 .> CALC : <<include>>
CU15 .> CALC : <<include>>
note bottom of CU11
  El No socio / Visitante no opera la app:
  el Administrativo registra su pago diario.
end note
@enduml
"""

PUML["02_clases_dominio.puml"] = """@startuml 02_clases_dominio
title Diagrama de clases — modelo de dominio Club Deportivo v1.5.0
skinparam classAttributeIconSize 0
enum Rol { SOCIO; EMPLEADO; ADMIN; PROFESOR; NUTRICIONISTA }
enum EstadoCuota { PENDIENTE; PAGADA; VENCIDA }
class Usuario { alias: String {id}; clave: String; rol: Rol; socioId: Int [0..1] }
class Plan { nombre: String {id}; precio: Int }
class Socio {
  id: Int {id}
  nro: String {unique}
  nombre: String  (Nombre Apellido)
  dni: Long {unique, 7..8 dígitos}
  email: String
  plan: String
  alta: Fecha
  nac: Fecha
  ficha: Boolean  (apto físico)
  carnetEmitido: Fecha
  carnetVence: Fecha
}
class "SocioVista(socio, parametros)" as SocioVista <<derivada>> {
  /carnetVencido, /hab: Boolean
  /cuotaVencida, /enGracia, /mora: Boolean (v1.5)
  /diasVencida, /gracia, /diasDeGracia: Int (v1.5)
  /recargo, /interes: Double · /totalAdeudado: Int (v1.5)
  /badge, /contador: String · /tono: TonoCuota (v1.5)
}
class Cuota { mes: String; monto: Int; vence: Fecha; estado: EstadoCuota; proximaVence: Fecha [0..1] }
class PagoHistorial { mes: String; monto: Int (v1.5: total cobrado); pagada: Fecha; medio: String; comprobante: String }
class Actividad { id: String {id}; nombre: String; horario: String; cupo: Int; ocupados: Int; precio: Int; profesor: String }
class Visitante { nombre: String; dni: String; actividad: String; hora: String; monto: Int; fecha: Fecha; medio: String }
class "Parametros (v1.5)" as Parametros #FFE0B2 { graciaDias: Int {0..60}; recargo: Double ($ fijo, 0..1.000.000); interesMensual: Double (%, 0..100); vigenteDesde: Fecha; {static} INICIALES; List<Parametros>.vigenteEn(fecha) }
class Rutina { nombre: String; profesor: String; actualizada: Fecha }
class DiaRutina { tag: String; foco: String }
class Ejercicio { nombre: String; series: Int {1..20}; reps: Int {1..100}; nota: String }
class FichaNutricional { peso: Double; altura: Double; objetivo: String; antecedentes: String }
class Consulta { fecha: Fecha; peso: Double; obs: String }
class Turno { fecha: Fecha; hora: String }
class Ingreso { fecha: String; estado: String }
Usuario --> "1" Rol : rol
Usuario "0..1" -- "1" Socio : cuenta de la app
SocioVista ..> Socio : <<derive>>
Socio "*" -- "1" Plan : plan (por nombre)
Socio "1" *-- "1" Cuota : cuota vigente
Cuota --> "1" EstadoCuota
Socio "1" *-- "0..*" PagoHistorial : historial
Socio "1" *-- "0..1" Turno : turno
Socio "1" *-- "0..*" Ingreso : ingresos
Socio "1" *-- "0..1" Rutina : rutina
Rutina "1" *-- "1..*" DiaRutina : dias
DiaRutina "1" *-- "0..*" Ejercicio : ejercicios
Socio "1" *-- "1" FichaNutricional : nut
FichaNutricional "1" *-- "0..*" Consulta : consultas
Visitante "0..*" -- "1" Actividad : actividad (por nombre)
Socio "0..*" -- "0..*" Actividad : inscriptos / actividades
Parametros ..> Cuota : <<use>> estado derivado y deuda
note bottom of SocioVista
  hab = !mora && !carnetVencido. En v1.5 mora se deriva de la fecha:
  d <= 0 pendiente; 0 < d <= graciaDias en gracia; d > graciaDias mora.
end note
@enduml
"""

PUML["03_sec_control_acceso.puml"] = """@startuml 03_sec_control_acceso
title Control de acceso por DNI (CU-09 / CU-10, HU-05) con la regla v1.5
actor Administrativo as adm
participant ":PantallaAcceso\\n(Compose)" as ui
participant ":ClubViewModel" as vm
participant ":SocioVista" as sv
participant ":ClubRepositorio" as repo
participant ":ClubDbHelper\\n(SQLite)" as db
adm -> ui : escribe el DNI y toca «Validar»
ui -> vm : validarAcceso()
vm -> vm : Validaciones.dni(campos.dniAcceso)
alt DNI inválido
  vm --> ui : acceso = null · toast(mensaje de validación)
else DNI válido
  vm -> vm : busca el socio por DNI en estado.socios
  alt no hay socio con ese DNI
    vm --> ui : acceso = «No encontrado» · sugiere visitante o alta
  else existe el socio
    vm -> sv : SocioVista(socio, estado.parametros)
    note over sv : parámetros desde el estado (UiState.parametros), leídos de SQLite al iniciar; sin consulta por acceso
    sv -> sv : gracia = parametros.vigenteEn(HOY).graciaDias · diasVencida · carnetVencido · recargo, interés, totalAdeudado
    alt carnet vencido
      sv --> vm : hab = false · «Carnet vencido el dd/mm/aaaa»
      vm --> ui : acceso = «Denegado» (rojo)
    else cuota pagada o d <= 0
      sv --> vm : hab = true · tono OK (verde)
      vm --> ui : acceso = «Permitido»: «Carnet vigente y cuota al día.» / «Carnet vigente · cuota {mes} pendiente, vence en N días.»
    else 0 < d <= gracia (v1.5: en gracia)
      sv --> vm : hab = true · enGracia · tono AVISO (naranja) · totalAdeudado
      vm --> ui : acceso = «Permitido»: «Cuota {mes} vencida · quedan N días de gracia. Debe $total en recepción.»
    else d > gracia (v1.5: mora)
      sv --> vm : hab = false · mora · tono BLOQUEO (rojo) · totalAdeudado
      vm --> ui : acceso = «Denegado»: «Cuota {mes} en mora: vencida hace N días · gracia de G días superada. Debe $total en recepción.»
    end
  end
end
opt acceso Permitido y no registrado
  adm -> ui : toca «Registrar ingreso»
  ui -> vm : registrarIngreso()
  vm -> vm : ingresosHoy += IngresoHoy · socio.ingresos += Ingreso · acceso.registrado = true
  vm -> repo : guardar(estado)
  repo -> db : INSERT INTO ingreso_hoy · UPDATE socio SET ingresos_json
  vm --> ui : toast «Ingreso registrado · nombre»
end
@enduml
"""

PUML["04_sec_cobro_caja.puml"] = """@startuml 04_sec_cobro_caja
title Cobro de cuota en caja con recargo e interés (CU-07, HU-04) (v1.5)
actor Administrativo as adm
participant ":PantallaFichaSocio\\n(Compose)" as ui
participant ":ClubViewModel" as vm
participant ":SocioVista" as sv
participant ":ClubRepositorio" as repo
participant ":ClubDbHelper\\n(SQLite)" as db
adm -> ui : toca «Registrar pago en caja»
ui -> vm : pedirRegistrarPago()
vm -> sv : seleccionado
alt cuota.estado == PAGADA
  vm --> ui : toast «La cuota de {mes} ya está pagada.» · no abre la hoja
else cuota no pagada
  vm -> sv : SocioVista(socio, estado.parametros).totalAdeudado (v1.5)
  sv -> sv : si cuotaVencida (gracia o mora): recargo vigente hoy (una vez) + interés por día = cuota × (tasa de ESE día / 100) / 30, 0 antes del primer parámetro guardado · totalAdeudado = cuota + recargo + interés
  sv --> vm : totalAdeudado · deudaDetalle
  vm --> ui : sheet «Registrar pago»: Socio · Cuota · Importe = total · «Cobrar $total»
  adm -> ui : confirma «Cobrar»
  ui -> vm : onConfirm → pagarCuota(id, «Caja»)
  vm -> vm : comprobante CD-2609-#### · estado = PAGADA · proximaVence = vence + 1 mes · historial += PagoHistorial(total)
  vm -> repo : guardar(estado)
  repo -> db : UPDATE cuota SET estado = 'PAGADA', proxima_vence · INSERT INTO pago (monto = total, medio, comprobante)
  db --> repo : ok
  vm --> ui : cerrarSheet() · toast «Pago registrado · socio habilitado»
  note over repo, db : Al reabrir la app, completarCuotasSiguientes() crea la cuota del mes siguiente (PENDIENTE).
end
@enduml
"""

PUML["05_sec_pago_diario_visitante.puml"] = """@startuml 05_sec_pago_diario_visitante
title Pago diario de un no socio (CU-11, HU-03): cupo del día y DNI repetido
actor Administrativo as adm
participant ":PantallaVisitanteNuevo\\n(Compose)" as ui
participant ":ClubViewModel" as vm
participant ":ClubRepositorio" as repo
participant ":ClubDbHelper\\n(SQLite)" as db
adm -> ui : abre Visitantes → «Registrar visitante»
ui -> vm : irAVisitanteNuevo()
adm -> ui : escribe nombre y DNI, elige actividad y medio (Efectivo / Débito)
ui -> vm : elegirActividadVisitante(actividad)
vm -> vm : libres = cupo − (ocupados + visitantes de hoy)
alt libres <= 0
  vm --> ui : visError «{actividad} no tiene cupo disponible hoy (x/y). El ingreso no puede realizarse.»
else con cupo
  vm --> ui : visAct = actividad.id · visError = null
end
adm -> ui : toca «Registrar pago diario»
ui -> vm : confirmarVisitante()
vm -> vm : Validaciones.nombre(vNombre, « del visitante») ?: Validaciones.dni(vDni)
alt nombre o DNI inválidos
  vm --> ui : visError = mensaje de validación
else sin actividad elegida
  vm --> ui : visError «Seleccioná una actividad.»
else actividad sin cupo
  vm --> ui : visError «Sin cupo en {actividad}. El ingreso no puede realizarse.»
else datos válidos y con cupo
  vm -> repo : contarVisitante(dniFmt(dni), HOY)
  repo -> db : SELECT COUNT(*) FROM visitante WHERE dni = ? AND fecha = ?
  db --> repo : n
  repo --> vm : n
  alt n > 0
    vm --> ui : visError «Ya hay un pago diario registrado hoy con el DNI xx.xxx.xxx.»
  else n = 0
    vm -> vm : visitantes = [Visitante(nombre, dni, actividad, HORA_ACTUAL, precio, HOY, medio)] + visitantes
    vm -> repo : guardar(estado)
    repo -> db : INSERT INTO visitante (...)
    db --> repo : ok
    vm --> ui : vuelve a Visitantes · toast «Pago diario registrado · $precio»
  end
end
@enduml
"""

PUML["06_sec_parametros_club.puml"] = """@startuml 06_sec_parametros_club
title El administrador guarda los parámetros del club (CU-13) (v1.5)
actor Administrativo as adm
participant ":PantallaPerfil\\n«Configuración del club»" as ui
participant ":ClubViewModel" as vm
participant ":ClubRepositorio" as repo
participant ":ClubDbHelper\\n(SQLite v4)" as db
note over repo, db : Al abrir la app, iniciar() → leer() carga UiState.parametros (SELECT * FROM parametro ORDER BY orden). Semilla demo: hace un año 10 / $300 / 2,5 %; hace dos meses 5 / $500 / 3 %. Ej.: Lucas (29 días vencido) debe $12.848 = $12.000 + $500 + $348
adm -> ui : abre Perfil → «Configuración del club»
ui -> vm : prepararParametros() (v1.5)
vm -> vm : parametros = estado.parametros.vigenteEn(HOY) → campos pGracia / pRecargo / pInteres
vm --> ui : formulario con los valores vigentes · historialParametros (más nuevo primero)
adm -> ui : edita gracia, recargo e interés y toca «Guardar parámetros»
ui -> vm : guardarParametros() (v1.5)
alt gracia no entera o fuera de 0..60
  vm --> ui : paramError «La gracia debe ser un número entero de 0 a 60 días.»
else recargo no numérico, no finito o fuera de 0..1.000.000
  vm --> ui : paramError «El recargo debe ser un importe de 0 a 1.000.000.»
else interés no numérico o fuera de 0..100
  vm --> ui : paramError «El interés mensual debe ser un porcentaje de 0 a 100.»
else valores válidos
  vm -> vm : estado.parametros = [Parametros(gracia, recargo, interes, HOY)] + anteriores sin la fila de hoy
  vm -> repo : set estado → guardar(estado)
  repo -> db : guardar(datos): DELETE FROM parametro · INSERT INTO parametro (...) por fila, en la transacción
  db --> repo : ok (versión 4)
  vm -> vm : SocioVista recalculadas con vigenteEn(fecha)
  vm --> ui : historial con la fila nueva arriba · toast «Parámetros guardados · rigen desde hoy»
  note over repo, db : Los parámetros no se editan retroactivamente; el interés se suma por tramos.
end
@enduml
"""

PUML["07_estados_cuota.puml"] = """@startuml 07_estados_cuota
title Máquina de estados — Cuota del socio (v1.5)
[*] --> Pendiente : alta de socio / cuota inicial\\n(vence = hoy + 10 días)
state Pendiente : habilitado · verde\\n«Vence en N días» / «Vence hoy»\\nd <= 0
state "En gracia" as Gracia : habilitado con aviso · naranja\\n«Quedan N días de gracia»\\n0 < d <= gracia
state "En mora" as Mora : no habilitado · rojo\\n«Vencida hace N días · gracia superada»\\ndeuda = cuota + recargo + interés\\nd > gracia
state Pagada : proximaVence = vence + 1 mes\\nhistorial += PagoHistorial(total, medio, comprobante)
Pendiente --> Gracia : cambio de fecha [0 < d <= gracia]
Pendiente --> Mora : cambio de fecha [d > gracia] (p. ej. gracia = 0)
Gracia --> Mora : cambio de fecha [d > gracia]
Mora --> Gracia : el administrador guarda una gracia mayor [d <= gracia nueva]
Pendiente --> Pagada : pagar(medio) [estado != PAGADA] / total = cuota (sin vencer)
Gracia --> Pagada : pagar(medio) / total = cuota + recargo + interés
Mora --> Pagada : pagar(medio) [caja o app] / total = cuota + recargo + interés
Pagada --> Pendiente : apertura de la cuota del mes siguiente\\n(completarCuotasSiguientes) / nueva Cuota PENDIENTE
Pagada --> [*] : baja del socio (ON DELETE CASCADE)
note bottom
  d = días desde Cuota.vence (negativo si falta); gracia = Parametros.graciaDias vigente hoy.
  Estado derivado en SocioVista; en SQLite la cuota sigue PENDIENTE hasta pagarse. VENCIDA deja de decidir.
end note
@enduml
"""

PUML["08_actividad_alta_socio.puml"] = """@startuml 08_actividad_alta_socio
title Diagrama de actividad — Alta de socio (CU-04, HU-02)
|Administrativo|
start
:Inicio → «Registrar socio»;
|App · ClubViewModel|
:irAAlta(): formulario limpio, plan Full, apto físico activo;
|Administrativo|
repeat
  :Completa nombre, apellido (v1.5), DNI, correo, plan, apto físico y observaciones;
  toca «Registrar socio»;
  |App · ClubViewModel|
  :confirmarAlta() → errorFormularioSocio();
  if (¿nombre válido? (obligatorio, solo letras)) then (sí)
    if (¿apellido válido? (v1.5)) then (sí)
      if (¿DNI con 7 u 8 dígitos, solo números?) then (sí)
        |ClubRepositorio · SQLite|
        :SELECT COUNT(*) FROM socio WHERE dni = ?;
        |App · ClubViewModel|
        if (¿DNI único? contarDni(dni) == 0) then (sí)
          if (¿correo válido? (nombre@dominio.com)) then (sí)
            #palegreen:ok;
          else (no)
            :altaError;
          endif
        else (no)
          :altaError;
        endif
      else (no)
        :altaError;
      endif
    else (no)
      :altaError;
    endif
  else (no)
    :altaError;
  endif
  |Administrativo|
repeat while (¿hubo error?) is (sí: corrige los datos) not (no)
|App · ClubViewModel|
:Generar N° de socio: 1500 + cantidad; si existe, el siguiente libre;
:Emitir carnet: emitido = hoy · vence = hoy + 12 meses − 1 día;
:Crear cuota inicial PENDIENTE: precio del plan · vence = hoy + 10 días;
:Ficha nutricional inicial, sin rutina ni turno;
|ClubRepositorio · SQLite|
:guardar(estado): INSERT socio, carnet, cuota;
|Administrativo|
:Ve la ficha del socio · toast «Socio N° xxxxx registrado · carnet y cuota inicial generados»;
stop
@enduml
"""

if __name__ == "__main__":
    for nombre, texto in PUML.items():
        with open(os.path.join(DIAG, nombre), "w", encoding="utf-8") as f:
            f.write(texto)
        print("ok", nombre)
