# GeoCeDG — PRE-G9B-MAINT-F8: creación interactiva sobre componentes certificadas locales

**PROMPT CANDIDATO — NO AUTORIZADO.** Requiere U2 cerrado, la decisión D-F8 y una instrucción
autoral con base exacta. Dos pasos con puerta autoral entre ellos.

## Objective
<!-- geocedg-field: objective -->

**F8-S0**: añadir una prueba discriminante sobre un locus mixto real (componentes
`LOCAL_CERTIFIED_SUBINTERVAL` y regiones condicionadas en la misma rama) que **determine**, sin
presuponerla, la conducta actual de:

- el clic o Q sobre la región condicionada;
- el clic o Q sobre la componente certificada local;
- `Point(L, u)` sobre la componente.

La traza de código predice creación en el primer caso y rechazo
(`COMPONENT_NOT_ESTABLISHED`) en el segundo; es una inferencia, no una confirmación, igual que
la sonda P-2, que no fue discriminante. La prueba solo es discriminante si el mismo montaje
puede dar creación o rechazo y si un control confirma que la ruta de clic acepta la captura
del productor.

`VERIFICATION_CLASS=BOUNDED_PHASE`.

**F8-min**, solo si S0 confirma la traza y el autor lo autoriza: el permiso
`LOCAL_COMPONENT_FORWARD_ADDRESS_CREATION` reutiliza el permiso individual existente con el
predicado local del diseño D §2.3, de modo que el clic sobre la componente certificada local
cree un punto con dirección de selector. `VERIFICATION_CLASS=INTEGRATED_PHASE`.

`CHANGE_ROUTE=ORDINARY`. Niveles: S0 `STATIC` + `PHASE PRE-G9B-MAINT-F8-S0`; min `STATIC` +
`PHASE PRE-G9B-MAINT-F8` + `INTEGRATION`, más smoke autoral.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor, posteriores al cierre de U2.

## Authority and evidence hierarchy

`AGENTS.md` §11.4; `locus-v2-existence-components.md` §11, §15.3 y §15.8;
`locus-v2-point-interaction.md` y enmiendas; contrato de uso condicionado §1 y §8; ADR 0019,
0040, 0041 y 0042; propuesta F8 `pre_g9b_cu7_r3_partial_forward_permission_proposal.md`.
Evidencia: diseño D §2, informe §C.2 y §F (P-2 no discriminante).

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- S0: solo pruebas y su selección.
- Min:
  - el predicado del permiso en `IndividualForwardPointCreation2D` o un permiso hermano;
  - el alcance de búsqueda de semillas en `LocusPointInteractionResolver2D`, sin cambiar la clasificación R6;
  - un valor de ruta en `LocusPointCreation2D`, probado antes de la ruta condicionada;
  - el espejo en `GeoCeDGReferencePlacement.Route`;
  - el texto del panel;
  - pruebas y selecciones.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Solucionador de existencia nuevo, sistema de identidad o códec nuevos.
- Arrastre nuevo.
- Respaldo multirrama (C3).
- Documentos bajo autoridad constructiva (siguen rechazados).
- Bandas, extremos no resueltos, componentes puntuales.
- Cualquier cambio de R6, H1 o de la creación condicionada.

## Architectural placement

Núcleo compartido (permiso e interacción) y capa Desktop (ruta y texto).

## Required design/specification

Antes de F8-min: enmienda candidata de `locus-v2-point-interaction.md` que declare el permiso
local con las siete condiciones de la propuesta F8, aprobada por el autor.

## Geometric invariants and degeneracies

- Completitud global y alcance del certificado local registrados como hechos separados.
- Dirección estrictamente dentro del intervalo certificado.
- Sin afirmación de punto más cercano ni de unicidad.
- La evidencia certificada se prueba antes que la condicionada.

## Compatibility and serialization

Mismo códec `geocedg-locus-address/v1` y mismo rol de salida; sin XML nuevo; documentos
existentes sin cambios.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- S0: la batería de caracterización descrita.
- Min:
  - el par de monotonía sobre el mismo locus mixto;
  - las regresiones de la propuesta F8 (líneas 94-101);
  - la autoridad constructiva sigue rechazada;
  - R6, H1 y la ruta condicionada producen resultados idénticos;
  - Undo, Redo y reapertura;
  - `IndividualForwardPointCreationTest`, `R5ForwardCreationRoutingTest` y
    `PostR1ParameterAddressTest` sin cambios.
- Comandos según el nivel.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

S0 y min se autorizan por separado.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

S0 termina con el informe de conducta y la recomendación (F8-min o cierre por redundancia).
F8-min termina con el candidato congelado y la guía de smoke.

## Required artifacts

Pruebas, verificación, informe; `GUIDE_IMPACT = UPDATE` en F8-min.

## Stop conditions

- S0 no confirma la traza: cerrar F8 como redundante con esa evidencia.
- S0 no resulta discriminante (por ejemplo, el control de captura falla): informar y no
  inferir la conducta.
- F8-min exige un códec, una identidad o un solucionador nuevos.
- Cambia un resultado de R6, H1 o condicionado.

## Deferred debts

Respaldo multirrama (C3).
