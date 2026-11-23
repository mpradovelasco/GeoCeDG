# GeoCeDG — PRE-G9B-MAINT-U2: simplificación de la herramienta Punto

**PROMPT CANDIDATO — NO AUTORIZADO.** Requiere la decisión D-U2 y una instrucción autoral con
base exacta.

## Objective
<!-- geocedg-field: objective -->

Unificar en la interfaz las entradas de creación de puntos sin cambiar el núcleo:

- U2-1: panel de Punto/Punto en objeto con un selector de origen único (Clic o Referencia Q);
- U2-2: herramienta 145 como alias del mismo panel con Q preseleccionado;
- U2-3: origen del radio y regla de confirmación visibles;
- U2-4: asistencia «Elegir una posición» de extremos métricos alineada con el panel y probada;
- U2-5: el rechazo «Componente no establecida» remite a la dirección explícita (mitigación de F8);
- U2-6: guías EN/ES §8.1 reescritas.

`CHANGE_ROUTE=ORDINARY`. `VERIFICATION_CLASS=INTEGRATED_PHASE`. Nivel previsto: `STATIC` +
`PHASE PRE-G9B-MAINT-U2` + `INTEGRATION`, más smoke manual del autor.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor.

## Authority and evidence hierarchy

`AGENTS.md` §5-§6; ADR 0019, 0041 y sus enmiendas; `locus-v2-point-interaction.md`;
`cu7-m08-point-placement.md`; decisión autoral R5 del 2026-11-05; decisión E del
mantenimiento de 1.2.0. Evidencia: diseño D §1 e informe §C.1.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- `GeoCeDGReferencePointTool.java`.
- La activación de modos en `GeoCeDGEuclidianController.java`, solo para el alias.
- `GeoCeDGMetricEndpointAssistance.java`.
- Recursos de traducción EN/ES.
- `docs/user/geocedg_user_guide_en.md` y `_es.md` §8.1.
- Pruebas Desktop y selecciones `PRE-G9B-MAINT-U2`.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Núcleo: `LocusPointCreation2D` y todas sus rutas, colocadores y permisos.
- `GeoCeDGReferencePlacement`; códecs; rol de salida; Q como padre.
- Identificadores de modo y de acción; recuento de acciones del perfil.
- «Punto en objeto»; herramienta 134; colocación nativa; valores por defecto.
- Retirar la herramienta 145 de la barra, salvo que D-U2 lo decida.

## Architectural placement

Capa de aplicación Desktop y perfil de producto.

## Required design/specification

Diseño D §1 con la decisión D-U2. No requiere especificación normativa nueva.

## Geometric invariants and degeneracies

Las direcciones creadas por clic y por Q en el mismo locus y la misma petición son idénticas a
las de 1.2.2; un paso de Undo por creación.

## Compatibility and serialization

Sin cambios de XML, modos ni acciones; perspectivas y barras guardadas siguen válidas.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- Nuevas:
  - paridad del selector de origen;
  - alias 145;
  - colocación en el perfil;
  - asistencia de extremos: presentación, «Permitir arrastre» y un Undo;
  - texto del rechazo.
- Sin cambios: `Cu7M08SGeneralCreationTest`, `GeneralPointInteractionDesktopTest`,
  `R3PointInteractionDesktopTest`, `R5ReferenceCreationDesktopTest`, `Cu7M08SReferenceToolTest`,
  `Cu7M08SDesktopEventsTest`, `Cu7M08SCorrectionTest`, `Maintenance120PointToolsDesktopTest`,
  `PostR1ConditionedPointDesktopTest` y las pruebas del núcleo
  (`M08PointCreationKernelTest`, `IndividualForwardPointCreationTest`,
  `R5ForwardCreationRoutingTest`, `Maintenance120PointInteractionTest`).
- Comandos: `STATIC`, `PHASE PRE-G9B-MAINT-U2` e `INTEGRATION`; `git diff --check`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Este incremento; no autoriza F8.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

Candidato congelado, guía de smoke manual para el autor (clic, Q, 145, menú contextual,
asistencia de extremos, ES/EN).

## Required artifacts

Commits, verificación, capturas del panel en ES/EN, `GUIDE_IMPACT = UPDATED` con rutas.

## Stop conditions

Cualquier cambio necesario en el núcleo; cambio de una dirección creada; ruptura de una
perspectiva guardada.

## Deferred debts

Conducta de Ctrl+clic con Punto sobre Locus V2 (fijar con prueba si el autor lo pide); F8.
