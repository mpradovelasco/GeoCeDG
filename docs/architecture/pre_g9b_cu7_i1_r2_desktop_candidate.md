# M08-S-R2 Desktop — candidato integrado para revisión

Fecha: 2026-11-01. selfApproved=false. Implementación autorizada, pendiente de revisión
independiente y smoke autoral. **No PASS — AUTHOR APPROVED**. Rama
codex/pre-g9b-cu7-i1-m08-s-r2-desktop. El índice de entrega generado bajo
artifacts/pre-g9b-cu7-i1-r2-desktop/delivery.json identifica el commit y árbol exactos,
los archivos y las ejecuciones, sin autorreferencia imposible dentro del commit.

## Base y autoridad

H1 6c6c863ba28413af4e14f350f88832f72b4caa1d / árbol
874c40c293f8240c932b8236657662ca393441d2 permanece preservado. La entrada documental
632962595e23b9b7e860d3c3d8dc717248484028 se conserva como antecedente; su condición
sobre alcance quedó resuelta expresamente por el autor. main y origin/main local
siguen en caf67ffc3758eccdef5202b7524b6c2b4e5d2fd4. No publicación ni fetch inferido.

Fuentes inspeccionadas: AGENTS.md, ADR 0019/0041, diseños R2 local e interacción,
puente M08SnapshotPlacement2D y política/sesión H1, controller Desktop, AppGeoCeDG,
diálogo avanzado, perfil, recursos de menú, pruebas R1/H1, registro/inventario JUnit
y procedimiento canónico. .github/copilot-instructions.md ausente. No cambio de
contratos aceptados ni del libro. Los informes previos conservan su cohorte original.

## Cambio y ubicación

GeoCeDGReferencePointTool es un orquestador Desktop por controlador. Retiene objetos
de construcción para la selección transitoria; no determina geometría. El modo 145
se registra en EuclidianConstants y en el perfil experimental bajo cedg.locus.v2,
reutilizando icono propio. El censo cerrado de acciones pasa de 133 a 134 y de 74 a
75 acciones upstream; se actualizan expectativas concretas, sin debilitar validación.

El controlador consume press/drag/release del modo antes de crear puntos o mover Q.
Seleccione L y después Q. Solapamientos de objetos requieren elegir un handle real;
las etiquetas sólo presentan objetos. Se consulta M08SnapshotPlacement2D.queryFromReference.
Desktop no reinterpreta R6, no filtra componentes, no calcula posiciones ni aproxima
cotas. Una oferta H1 se muestra por rama, componente y parámetro completos y desplazables.
Incluso con una sola oferta hay botón Confirmar; con varias ninguna está seleccionada
por defecto. La selección transporta la Choice original; los índices de JList son
sólo navegación efímera y nunca identidad. Cancelar/Escape/cambio de modo cierran la
capability; tickets de generación impiden aceptar callbacks antiguos o dos veces.

Selection.commit realiza revalidación y construcción forward. Desktop almacena Undo
una sola vez después de éxito porque los eventos interceptados no alcanzan el callback
creador del host. No hay redefinición ni nueva serialización. Q no se persiste como padre;
si influye ya en L conserva su influencia legítima. El punto se recomputa por el DAG
forward, con identidad/ownership existentes. No se amplía el arrastre general.

## Política visible y configuración

Indicador no modal con etapa, radio world, alcance fijo y cobertura global no establecida.
Opciones de herramienta abre ajustes voluntarios de sesión; no es paso de cada creación.
Valores iniciales existentes: radio .1, equalDistanceTolerance 1e-10, parameterTolerance
1e-12; presupuesto 32768 misses semánticos / 512 subdivisiones-pases intervalares /
80 iteraciones / 1024 candidatos. Son unidades H1: misses compuestos y revalidación
acumulados; pases intervalares separados; no llamadas Java ni muestras de pantalla.
La política conserva versión g9u0-r6-semantic-point/v1 y semántica de sus campos.
Cambiarla cancela selección pendiente. No se escribe preferencia, XML ni expresión dinámica.
Nueva identidad de registro documental reinicializa los ajustes. No usar zoom, DPI,
hit radius o coordenadas de pantalla como autoridad geométrica: sólo seleccionan objetos.

El alcance incluye todas las ramas/componentes admitidas por el proveedor/política.
Eso NO implica cobertura inversa completa. No hay filtros ocultos ni alcance restringible.
Esta ampliación posible queda para autorización futura. No se modifican tolerancias de
certificación exacta, autoridad constructiva, STRICT ni la semántica histórica R6.

## Diálogo avanzado y accesibilidad

Se conserva modo de dirección explícita y su construcción existente. El diálogo sólo
contiene rama y parámetro en GridBagLayout, campos de 18 columnas, labelFor y nombres
accesibles. Se elimina la pestaña Q sustituida por la herramienta gráfica. Las claves
largas permanecen íntegras y desplazables, sin ensanchar el tamaño preferido. Mensajes
y controles nuevos EN/ES; códigos tipados visibles junto a explicación localizada.
Confirmación contextual compacta, lista desplazable hasta cinco filas, cancelación y
teclado; no se presentan mínimos, proyecciones exactas ni unicidad global.

## Compatibilidad, límites y riesgos

R6 y kernel H1 sin cambios semánticos. Los modos Punto/Punto en objeto y el helper R1
conservan sus rutas; nuevo modo separado evita prohibiciones generales. Persistencia
.cedg, dirección forward, copia/linaje y lectores existentes reutilizan codecs vigentes.
No nuevo comando P=Operación(L,Q). Proveedores no acreditados, presupuesto agotado,
referencia/selección obsoletas o evidencia insuficiente se rechazan sin creación.
No se ofrece ausencia certificada por falta de ofertas. Una dirección local no establece
mínimo, proyección exacta, unicidad global ni completitud del continuum.

Riesgo residual: ergonomía real, foco de ventanas, resolución/DPI, traducciones y hit-testing
requieren smoke autoral; las pruebas Swing no sustituyen inspección visual. El caso
nativo usa exclusivamente copia en memoria y salida temporal; hash del original verificado.
El ajuste de política pertenece a sesión de herramienta, no a documento persistente.

M09, A0, M08-D, I2G, I3 y redefinición general siguen pendientes/no autorizados.
DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG es independiente, no bloqueante de M08-S. CU10,
guía futura y DEBT-CU3-EXACT-TANGENCY-GENERALIZATION se conservan.

## Evidencia y clasificación

VERIFICATION_CLASS=INTEGRATED_PHASE, frozenAtPhaseStart=true;
VERIFICATION_INFRASTRUCTURE_IMPACT=BOUNDED_CATALOG_REGISTRATION. No nuevo runner,
schema, política de aceptación ni allowlist. Selección PHASE derivada de JUnit real
mediante Read-VerificationJUnitFiles/Get-VerificationDeterministicHash y actualización
canónica del inventario. El cambio de nombre del test R1 registra la sustitución del
flujo de pestaña por prueba real de press/release L→Q; no borra evidencia histórica.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: mismos runtimes, Gradle, Conda, prerequisitos,
configuración y packaging. GUIDE_IMPACT=UPDATED: instrucciones de herramienta en guía
Developer y protocolo adjunto. Sin efecto editorial sobre el libro.

Intentos conservados en artifacts/pre-g9b-cu7-i1-r2-desktop/: compile, focal, regression,
profile-focus y focal-complete. El fallo inicial de mensajes procedía del fallback del
perfil al mantener el censo 133; después se localizaron expectativas de listado 74 y
cinco herramientas semánticas. Son desajustes de catálogo por la acción nueva, no defectos
científicos ni razones para alterar admisibilidad. Resultados y fallos reales se conservan.
Los resultados finales y códigos de STATIC/INFRA_UNIT/PHASE/INTEGRATION y las sondas
nativas se consultan en delivery.json y sus JSON/JUnit/logs originales; este texto no
anticipa un PASS. No se ejecuta FINAL ni smoke como aprobación automática.


### Correcciones de preparación integrada

La primera ejecución final.desktop de preparación (e9afcb8e3f2c93f124fb979788c7fc351368e515)
terminó con código 1 y cinco fallos: dos expectativas de toolbar/menu en WorkspaceSurface,
una enumeración de variantes Spline, ausencia de la nueva acción en la matriz de cobertura
de guía y falta del alias de icono en el lookup heredado de modos. Se conservan sus JUnit.
Se actualizan las listas exactas, toolbar 59→60, se añade entrada MISSING (sin completar
CU10 ni modificar guía de usuario/libro) y se reutiliza SEMANTIC_POINT en el lookup propio.
El viewport contextual se acota sin fijar ancho de celda, permitiendo desplazar IDs largos;
Enter confirma explícitamente y se solicita foco en la lista. No cambia el kernel.
Las cinco clases afectadas pasan en el foco catalog-corrections, código hijo 0.
Una edición documental se interrumpió por el nombre incorrecto utf8-sig en el arnés;
se corrigió a utf-8-sig, sin defecto de producto ni pérdida de evidencias.
La verificación final corresponde al candidato posterior, no reetiqueta la ejecución fallida.


### Catálogo preparado desde ejecución real

En 42768911948cb58997214c0c27fa6f14d678ed1e / árbol
36469bcf73da58f456a92b544d081be007fc4d5c, desktop-executed-corrected termina con
código hijo 0: 2285 PASSED y un SKIPPED_LIKE histórico de CasValueExtractor, ya previsto
por la allowlist; 2286 identidades. No se añade ninguna exención. Los descubrimientos
previos registran 6635 shared y 2292 Desktop; la sonda optativa queda fuera del catálogo.
No cambian nombres de tests en las correcciones; sí sus expectativas y la integración.

Se deriva pre-g9b-cu7-i1-r2-desktop.desktop (47 tests: Tool 10, Profile 5, Compiler 32)
y se actualiza la huella de la selección R1 (31, un nombre sustituido por el flujo gráfico).
Read-VerificationJUnitFiles y Get-VerificationDeterministicHash conservan las identidades
extraídas; update-verification-junit-inventory.ps1 actualiza descubrimiento/final.desktop.
PHASE PRE-G9B-CU7-I1-M08-S-R2-DESKTOP conserva los contratos H1/R1 y añade el foco nuevo.
Sólo registro acotado y censo del fixture: 75 selecciones / 77 PHASE; no cambios del runner.
El informe final sobre el commit posterior congelado se emitirá en delivery.json; estas
pruebas de preparación no se presentan como sus resultados canónicos.


### Ayuda localizada y campaña interrumpida

La inspección de getHelpTransKey detectó que elimina `.Tool`: la clave correcta es
LocusV2.ReferencePoint.Help. Las tres ediciones se corrigieron por bytes, conservando
sus demás contenidos. La aserción EN/ES usa app.getToolHelp en un test existente;
no añade identidades ni cambia el registro. El foco help-correction pasa 14/14,
código hijo 0, y se compara la misma lista de diez identidades Tool con la derivación.

La campaña de ee847c1769d2c3b346e0f3b896bc4c4be5d821de conservó STATIC e INFRA_UNIT
ACCEPTED/COMPLETE, ambos código 0. Se canceló manualmente su coordinador propio y
sus descendientes al detectar el defecto, antes de INTEGRATION; no por duración.
PHASE quedó interrumpida y no se reclama aceptación ni cierre de esa ejecución.
La identificación de procesos y motivo están en campaign-interruption.json; no se
detuvieron aplicaciones/sesiones ajenas. Los resultados bajo final/ se conservan.
La campaña del commit corregido usa final-helpfix/ y sus índices con sufijo helpfix,
sin sobrescribirlos ni atribuirle resultados del anterior. La entrega se detiene
pendiente de revisión independiente y smoke autoral, cualquiera que sea el éxito técnico.
