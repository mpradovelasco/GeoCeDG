# M08-S-R2-DESKTOP — decisión de entrada sobre el alcance configurable

Fecha: 2026-11-01. Estado: **DECISIÓN AUTORAL PENDIENTE; NO CANDIDATO DESKTOP IMPLEMENTADO**.
selfApproved=false. Esta nota no modifica ADR aceptados ni la aprobación técnica H1.

## Base y clasificación

Base obligatoria verificada con worktree limpio y un único worktree registrado:
6c6c863ba28413af4e14f350f88832f72b4caa1d,
árbol 874c40c293f8240c932b8236657662ca393441d2.
Rama de continuación: codex/pre-g9b-cu7-i1-m08-s-r2-desktop, creada desde H1.
La referencia H1 se conserva. main y origin/main locales permanecen en
caf67ffc3758eccdef5202b7524b6c2b4e5d2fd4. No fetch/push ni publicación.

Encargo: implementación Desktop acumulativa sobre R1/R2/H1. Trabajo realizado en
esta entrada: inspección estática y documentación de una decisión necesaria antes
de cerrar la política de consulta. Capa afectada por esta nota: arquitectura/UI;
ningún cambio productivo ni artefacto generado de producto.

## Diferencia que necesita decisión

La autorización Desktop exige: «La política de radio, tolerancias, presupuesto y
alcance debe ser visible y configurable». También ordena detener la parte afectada
si queda una decisión normativa de política no deducible del diseño candidato.

El [diseño R2 de interacción](pre_g9b_cu7_i1_r2_refinement_interaction.md), §3,
propone valores configurables de radio/tolerancias/presupuesto, pero define el
alcance como las ramas/componentes declaradas y certificadas del proveedor
soportado. No define opciones de restricción de rama, componente o intervalo.

Hechos del código vigente:

- LocusPointInteractionPolicy2D sólo contiene maximumWorldDistance,
  equalDistanceTolerance, parameterTolerance y workBudget. No tiene selector de alcance.
- El constructor de creación de LocusPointInteractionQuery2D declara expresamente
  que consulta todas las ramas/componentes de la fuente. Su otro constructor es
  para movimiento, restringido por la dirección exacta de un punto existente.
- M08SnapshotPlacement2D.queryFromReference utiliza el constructor de creación,
  copia la posición de Q y no recibe una dirección/selector de alcance.
- El contexto H1 liga fuente, definición, revisión, política y referencia; no existe
  una entrada de alcance restringido que Desktop pueda configurar y revalidar.

Por ello, mostrar un alcance fijo es implementable con el kernel auditado, pero no
equivale a ofrecer un alcance configurable. Filtrar una lista de ofertas en Desktop
no restringe la búsqueda ni su presupuesto y no debe anunciarse como tal. Tampoco
puede usarse la consulta de movimiento atribuyendo una dirección ficticia a Q.

## Alternativas concretas

**A — recomendada para el primer incremento Desktop.** Autorizar explícitamente
alcance fijo y visible sobre todas las ramas/componentes que admite el kernel.
Configurar en sesión radio, tolerancias y presupuestos existentes; sin preferencias
de rama, ni filtros de dominio que pretendan cambiar la consulta. La selección de L
y la elección posterior de una Choice siguen siendo explícitas e independientes.

Precisión candidata propuesta a §3 del diseño de interacción:

> En este incremento el alcance de creación es fijo: todas las ramas/componentes
> declaradas y admitidas que expone el kernel para L. Se muestra como dato de sólo
> lectura. Son configurables el radio world, las tolerancias y los presupuestos.
> No se ofrece restricción de rama, componente o subintervalo de búsqueda.

Los valores iniciales de radio .1, tolerancias 1e-10/1e-12 y límites 32768/512/80/1024
sí proceden del diseño y del caso autoral; no requieren inventar valores adicionales.
La contabilidad aplicable es H1, que sustituye la propuesta anterior de coste fijo.

**B — alcance restringible.** Diseñar primero una entrada tipada en el kernel que
identifique ramas/componentes/dominio permitidos, la ligue a la fuente y revisión,
defina sus efectos sobre cobertura, descubrimiento, presupuesto y caducidad, y
conserve R6. No asignar componentes por proximidad ni implementar el filtro geométrico
en Desktop. Esta alternativa amplía la frontera H1 auditada y necesita un contrato
y una autorización propios antes de código productivo.

No se elige A unilateralmente porque sería una excepción al texto de la nueva
autorización. No se implementa B por inferencia de la palabra «configurable».

## Resto del trabajo conservado y puerta de reanudación

La autorización L→Q, confirmación contextual incluso con una Choice local única,
política world sin zoom/DPI, dirección avanzada compacta y Undo único sigue vigente.
No se pide volver a aprobar esos requisitos ni la corrección H1.

Se inspeccionaron GeoCeDGPointInteraction, GeoCeDGEuclidianController,
GeoCeDGLocusV2Dialogs, el perfil vivo y los contratos ADR 0019/0041 y R2. La ruta R1
abre actualmente un diálogo de dos pestañas y consume R6; todavía no consume el
permiso M08SnapshotPlacement2D. Se conserva íntegra hasta que exista su sustitución
gráfica, evitando retirar la capacidad desde Q antes de disponer de L→Q.

La puerta de reanudación de la política y del flujo integrado es una decisión A/B
o una precisión autoral equivalente. No hay un bloqueo científico nuevo de H1:
esta discrepancia afecta al alcance solicitado para la nueva interfaz.

El original C:/Temp/TestM08-S.cedg conserva SHA-256
4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d.
No se ha ejecutado una prueba Desktop L→Q ni smoke nuevo. ReferenceR1 se creará
temporalmente en los ensayos futuros; esta entrada no altera el archivo original.

## Verificación de esta entrada documental

STATIC canónico y git diff --check sobre el commit documental limpio; los resultados
se conservan bajo artifacts/pre-g9b-cu7-i1-r2-desktop-entry/. No se presentan como
aceptación de Desktop ni se reutilizan los PASS H1 como pruebas de interfaz nueva.
INFRA_UNIT, PHASE e INTEGRATION del candidato productivo quedan pendientes porque
todavía no existe ese candidato; no hay registro de selección ni runner modificado.
BOOTSTRAP IMPACT=NO CHANGE REQUIRED: sin nuevos prerequisitos o configuración.
GUIDE_IMPACT=NO CHANGE: no hay un nuevo flujo disponible que anunciar en la guía.

H1, informes históricos, main, ADR aceptados y libro intactos. Sin publicación,
etiquetas o releases. Se conservan CU10, deuda de tangencias y exclusiones M08-D,
M09 y arrastre general. M08-S sigue pendiente de smoke y aprobación final.


## Resolución autoral posterior — 2026-11-01

El autor confirma A: alcance fijo visible, todas las ramas/componentes admitidas
por proveedor y política; configuración sólo de los parámetros existentes. Se
prohíbe simular restricciones mediante filtrado Desktop. La restricción dinámica
por rama/componente queda como posible ampliación futura no autorizada y no
bloqueante. Queda resuelta la condición de parada de esta entrada. El texto anterior
conserva el estado histórico de 632962595e23b9b7e860d3c3d8dc717248484028;
la continuación está en el plan e informe Desktop, sin reescribir sus evidencias.
