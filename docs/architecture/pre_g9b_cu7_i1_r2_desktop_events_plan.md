# M08-S-R2 Desktop — corrección tras smoke fallido: plan focal

Clasificación: debugging e implementación Desktop; documentación candidata.
Base verificada limpia: 7ef6a26c573c57a3959b611f0f4c2a8dbc3f5027,
árbol 51abc83fbb911788335e21d354456b2262c092cb. La rama Desktop de D1 queda
preservada; la corrección usa codex/pre-g9b-cu7-i1-m08-s-r2-desktop-events.
main/origin/main permanecen en caf67ffc3758eccdef5202b7524b6c2b4e5d2fd4.

El smoke autoral NO está superado. Los resultados técnicos anteriores son históricos,
no prueba de una interacción gráfica funcional. Se conserva su expediente literalmente.

## Diagnóstico y diseño antes de código productivo

Recorrer activación, AWT mouse pressed/released/clicked/moved, preview, foco,
selección de L/Q, consulta, presentación de confirmación y commit. Separar llamadas
programáticas a click/commit, dispatch de listeners Swing/AWT y entradas físicas Windows.
Usar sondas sólo Desktop, reproducibles y de propietario/cierre explícitos. No cambiar
el kernel ni atribuirle el fallo sin evidencia. El fichero autoral se copia a artifacts;
no sobrescribir C:/Temp/TestM08-S.cedg. ReferenceR1 se crea temporalmente en la copia.

La reproducción física inicial observó L seleccionado e instrucción de seleccionar Q;
tras Q no quedó confirmación ni se creó P. No establece por sí sola la causa.
Hipótesis a comprobar: sincronización del mismo modo que reinicia la sesión,
preview/hits, cierre del popup dentro del mismo gesto, y foco del JWindow.
Hecho del código: aplicar política llama reset/activate, que pierde L. Corregirlo.

Perímetro editable previsto: GeoCeDGReferencePointTool, su controlador Desktop y,
si hace falta, el anfitrión GeoCeDG de la entrada algebraica. Mantener íntegro
GeoCeDGReferencePlacement (composición R1/R6 y H1) y todo el kernel compartido.

Panel específico M08-S incrustado encima de AlgebraInputD mediante BorderLayout y
contenedores existentes; sin posiciones absolutas, JWindow o diálogo habitual de
política. r world visible una sola vez; sección Policy técnica desplegable con sólo
campos existentes. Sin mensaje de alcance fijo ni cobertura antes de consultar;
alcance/cobertura se conservan en diagnóstico técnico, separados de admisión.
La política se valida antes de aplicarse. Un cambio cierra e invalida las ofertas,
lo comunica y conserva L vigente mientras se solicita de nuevo Q. Nunca consulta
por preview/foco. Escape/cancelación/error/cambio de herramienta limpian lo transitorio.
Un gesto completado solicita una sola selección; publicación sólo por el commit
propietario existente y un único Undo host. No nueva geometría, identidad o codec.

## Validación congelada

VERIFICATION_CLASS=INTEGRATED_PHASE; frozenAtPhaseStart=true.
Primero caracterización real de eventos y regresiones focales. Mantener R1 extremo,
R1 múltiples y H1 local/nativo con su confirmación. Después obtener catálogo JUnit
por el procedimiento canónico y ejecutar STATIC, INFRA_UNIT, PHASE e INTEGRATION
sobre un SHA/árbol limpio congelado. No FINAL ni repetición de campañas ajenas.
Guardar intentos/correcciones del arnés con códigos reales, sin atribuirlos a producto.
Pruebas gráficas se describirán por nivel; no equiparar dispatch programático y
clics físicos. Capturas guardadas sólo como evidencias, no como autoridad geométrica.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: mismos runtimes y prerrequisitos.
GUIDE_IMPACT=UPDATE: protocolo candidato de smoke, sin adelantar CU10 ni editar libro.
Patrón futuro de panel contextual: sólo propuesta, sin infraestructura general ahora.
No ADR aceptados, kernel, main, publicación, tags, releases, M08-D, M09 o drag general.
Mantener deuda científica y DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG. selfApproved=false.
Entregar candidato y detenerse para revisión y smoke autoral.

## Reanudación autoral posterior a Escape

Se recuperan sin reset/clean las tres fuentes no versionadas de plan/sonda y sus
artifacts. La restricción vigente prohíbe automatizar la sesión gráfica de Windows:
no se usa Computer Use, Robot ni entrada nativa. No se invoca automáticamente la
sonda interactiva anterior. Sus resultados interrumpidos no son un smoke positivo.

Se priorizan componentes Swing no visibles, despacho AWT del controlador y entrega
controlada de eventos a handlers reales. La caracterización del foco puede utilizar
flags Java de visibilidad de un popup genuino del JDK, exclusivamente en el arnés,
sin mostrarlo, para medir el efecto del handler real del frame. Se declarará esa
simulación de estado UI; no equivale a entrega nativa de foco ni smoke visual.
La apertura java.awt es sólo del JVM optativo del arnés, no de la aplicación.

La explicación del fallo debe separar causas medidas de hipótesis de OS/foco aún
no medidas. Kernel, permisos R1/R6 y R2/H1 y contratos científicos permanecen intactos.
Si no puede justificarse una corrección segura, entregar bloqueo y protocolo humano.
