# M08-S-R2 Desktop Events — candidato de corrección focal

Estado: candidato pendiente de revisión independiente y smoke autoral.
No PASS — AUTHOR APPROVED. El smoke de D1 no fue superado.

Base: `7ef6a26c573c57a3959b611f0f4c2a8dbc3f5027`, árbol
`51abc83fbb911788335e21d354456b2262c092cb`, preservada en la rama Desktop.
Rama de trabajo: `codex/pre-g9b-cu7-i1-m08-s-r2-desktop-events`.
Identidad final y resultados inmutables se entregan en
`artifacts/pre-g9b-cu7-i1-r2-desktop-events/delivery.json`; este texto no pretende
conocer su propio SHA antes de congelarse. Evidencias anteriores intactas.

## Problema medido y corrección

El diagnóstico separado `pre_g9b_cu7_i1_r2_desktop_events_diagnosis.md` documenta
la colisión entre la confirmación heavyweight focusable de Swing y el handler real
`GeoGebraFrame.windowLostFocus`, que la oculta dejando una selección pendiente e
inaccesible. El arnés utiliza el frame y PopupFactory reales con flags Java de
visibilidad simulados, sin mostrar ventanas ni automatizar Windows. Delimita esa
evidencia respecto del síntoma físico anterior y del smoke humano aún pendiente.

El cambio se sitúa exclusivamente en Desktop:

- GeoCeDGReferencePointTool sustituye JWindow/JPopupMenu/JDialog propios por un
  panel específico embebido. Elección de objetos y direcciones por handles exactos,
  con confirmación y cancelación locales. No dependencia de un popup focusable.
- GuiManagerGeoCeDG instala el panel en NORTH del AlgebraInputD existente y lo
  vuelve a adjuntar cuando el input se reconstruye. Conserva el tipo y resto del
  layout nativos; retirada sólo por el propietario vigente. No posición de pantalla.
- GeoCeDGEuclidianController distingue sincronizar el mismo modo de comenzar otra
  operación; el primer caso conserva L/oferta, el cambio real cancela como antes.
- AppGeoCeDG deja los eventos de teclado del panel a sus controles Swing. Enter,
  Space y flechas no ejecutan atajos globales que puedan editar la construcción.
  Escape local reinicia la selección; el Escape gráfico conserva la ruta host.

No se cambia el dispatch de publicación, el presupuesto ni el kernel. La consulta
se realiza al seleccionar Q en mousePressed. Released/clicked/preview/foco no
reconsultan. Con una oferta pendiente no hay otra consulta hasta confirmación,
cancelación o invalidación explícita. El confirmador consume una única capability;
reusar un botón retirado queda invalidado por la generación de UI.

## Política y selección

Radio world r visible una sola vez, en la cabecera de la operación. Es el mismo
valor durante L y Q. Policy/Política despliega los seis campos técnicos existentes;
el radio no se duplica allí. No se añaden unidades, tolerancias ni garantías.
Valores iniciales vigentes: r=0.1, tolerancia de empate=1e-10, parámetro=1e-12,
misses semánticos=32768, subdivisiones/pases=512, refinamiento=80, candidatos=1024.
Los cambios son de sesión, no documento. El alcance permanece fijo: todas las
ramas/componentes admitidas por proveedor y política, sin filtro Desktop.

Editar cualquier campo cancela inmediatamente ofertas previas, lo comunica y
conserva L. Aplicar o el siguiente gesto valida conjuntamente los campos con los
constructores de política existentes. Entrada inválida bloquea la consulta, sin
mutar. No hay reconsulta automática. Abrir/cerrar Policy no reinicia la operación.
La cobertura sólo aparece tras consultar, dentro del diagnóstico técnico; no se
muestra «no query yet» ni alcance fijo en la presentación ordinaria. Alcance de
consulta y cobertura probada siguen siendo conceptos distintos.

Múltiples hits de objetos requieren elección explícita de referencias reales. No
se elige Q por cercanía, etiqueta ni orden. El índice visual sólo accede a un handle
efímero propio de esa lista, nunca se serializa ni aporta autoridad geométrica.
El selector semántico estructural de componente sigue siendo parte de la dirección
durable forward; no confundirlo con el selector visual/capability efímeros (D2).

## Contratos conservados y conciliación normativa propuesta

GeoCeDGReferencePlacement permanece literalmente igual a D1. Primero R1/R6:
UNIQUE_ADMISSIBLE_PREIMAGE y MULTIPLE_SEMANTIC_PREIMAGES conservan sus propias
ofertas y commit. Sólo UNRESOLVED_NUMERICAL_SEARCH con la causa tipada
GLOBAL_UNIQUENESS_NOT_ESTABLISHED permite una nueva consulta R2/H1. Los demás
rechazos son terminales. No hay fallback tras fallo de commit ni mezcla de ofertas,
revisiones o coberturas. Presupuestos separados e inalterados de cada autoridad;
no se presenta su suma como un presupuesto acumulado único.

En R2/H1 cada oferta local exige confirmación incluso si es única; no se afirma
mínimo, proyección exacta, completitud o unicidad global. La evidencia científica
y el perímetro de proveedores permanecen los del candidato H1; esta corrección
no acredita nuevos proveedores, componentes o direcciones.

La propuesta `pre_g9b_cu7_i1_r2_desktop_normative_reconciliation.md`, incluida su
precisión D2, sigue candidata sin cambios. Para ADR 0019, ADR 0041 y las specs
relacionadas este parche **no añade diferencias normativas geométricas**: sólo
sustituye la presentación transitoria por panel integrado y corrige sus reinicios.
Los cambios normativos de admisión individual R2 siguen pendientes de decisión
autoral final; no se modifica un ADR aceptado en este encargo.

P se publica con la operación forward existente, sus padres semánticos, dirección
durable e identidad existentes y un solo storeUndoInfo host. Q se consume como
valor transitorio; si Q es antecesor legítimo de L, esa dependencia subsiste a
través de L. No nuevos padres ficticios, codecs, tokens o asociaciones retrospectivas.
No cambios en lectores, XML/GGB/.cedg, copia/remapeo ni comandos legacy.

## Validación y límites probatorios

VERIFICATION_CLASS=INTEGRATED_PHASE; frozenAtPhaseStart=true.
VERIFICATION_INFRASTRUCTURE_IMPACT=UPDATE_REQUIRED únicamente para catálogo derivado
y filtros de la selección PHASE Desktop existente, obtenidos de JUnit real.
Las regresiones nuevas usan Component.dispatchEvent sobre
la superficie gráfica y el JButton real: moved/pressed/released/clicked llegan a
listeners Desktop. No llaman tool.click/commit en sustitución de esos gestos.
La entrega controlada de FocusEvent/WindowEvent y las acciones de teclado Swing
se identifican como tales; no son foco físico, navegación nativa o smoke visual.
Las pruebas programáticas históricas se conservan y se etiquetan por su alcance.

La matriz y el protocolo están en
`docs/validation/pre_g9b_cu7_i1_r2_desktop_events_matrix_and_smoke.md`.
Se ejecutan focales y STATIC, INFRA_UNIT, PHASE registrada e INTEGRATION sobre
un mismo candidato limpio, con JUnit y códigos reales. La tabla final de resultados,
intentos fallidos y hashes de evidencia pertenece al delivery.json, sin sustituir
informes D1/H1 ni proclamar aprobación autoral. No FINAL.

La suite Desktop completa contiene pruebas históricas que muestran ventanas Java
temporales (p. ej. PreG9BR6PlusF2R1DualActivationTest). La restricción de sesión
gráfica exige decisión explícita antes de esa ejecución: sin ella, INTEGRATION se
mantendrá pendiente y no se declarará aceptación integrada. Las sondas nuevas y
las focales de Events no muestran ventanas. Las imágenes de panel son renders
fuera de pantalla, no capturas del escritorio ni evidencia de foco físico.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: no cambio de JDK, runtimes, dependencias,
variables persistentes ni prerrequisitos. Infraestructura: únicamente actualización
derivada de catálogo JUnit y selección PHASE existente; no nueva semántica del verificador.
GUIDE_IMPACT=UPDATE: protocolo candidato; deuda editorial CU10 preservada, libro intacto.

Limitaciones: no smoke físico posterior automatizado; render/DPI/foco nativo exigen
autor. No ejecución de binarios de lectores antiguos; evidencia de codec inalterado
y regresiones actuales de documentos legacy. M08-S pendiente de aprobación final;
M09 pendiente y sin implementación. Sin M08-D, A0, I2G, I3, reconstrucción/redefinición
general ni ampliación del arrastre. Mantener DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG,
DEBT-CU3-EXACT-TANGENCY-GENERALIZATION y compromisos de CU10.

## Propuesta futura, sin implementación

Patrón de panel contextual de autoría geométrica: instrucciones de selección,
parámetros visibles, política avanzada desplegable e integración con la entrada
algebraica. Podría servir a otros comandos CeDG tras diseño y autorización propios.
Este parche no crea infraestructura general ni cambia otras herramientas. La futura
restricción dinámica de ramas/componentes sigue no autorizada y no bloqueante.

Salida: detenerse para revisión independiente y smoke autoral. main, ramas históricas,
ADR aceptados y libro intactos; no publicación, etiquetas, releases o paquetes.
