# GeoCeDG — PRE-G9B-MAINT-U2: simplificación de la herramienta Punto

**PROMPT DE TAREA AUTORIZADO POR EL AUTOR EL 2026-11-23** («GeoCeDG — PRE-G9B-MAINT-U2.
Autorización de implementación — Simplificación de la herramienta Punto»), sobre la base exacta
indicada abajo. Es la versión reconciliada del prompt candidato
[`maint-u2-point-tool-unification`](../../../docs/architecture/pre_g9b_maint_p0_candidate_prompts/maint-u2-point-tool-unification.prompt-candidate.md)
con la decisión D-U2 de la [hoja F](../../../docs/validation/pre_g9b_maint_p0_author_decision_sheet.md) §0
y con la autorización autoral. `selfApproved=false`. Este prompt no autoriza ningún otro
incremento de `PRE-G9B-MAINT`.

## Objective
<!-- geocedg-field: objective -->

Unificar en la interfaz Desktop las entradas interactivas de creación de puntos sobre Locus V2,
sin cambiar la semántica del núcleo:

- **U2-1:** panel único de Punto y Punto en objeto con un selector de origen, «Clic» o
  «Referencia Q», que sustituye los dos conmutadores excluyentes por modo.
- **U2-2:** la herramienta 145 se conserva (modo, acción, icono y posición) como alias que abre
  el mismo panel con «Referencia Q» preseleccionada.
- **U2-3:** el panel presenta el origen del radio («de la vista» o «r world») y la regla de
  confirmación de cada origen, sin cambiar ninguna de las dos conductas.
- **U2-4:** la asistencia «Elegir una posición» de los extremos métricos usa la presentación de
  direcciones del panel (`GeoCeDGAddressPresentation`), la garantía de la ruta y «Permitir
  arrastre», con su primera prueba (un solo paso de Undo).
- **U2-5:** el rechazo «Componente no establecida» explica la alternativa `Point(L, u)` y la
  herramienta de dirección explícita (134). No implementa F8.
- **U2-6:** las guías EN/ES §8.1 se reescriben como «Crear un punto sobre una curva», con los
  dos orígenes y la alternativa de dirección explícita.

Las vías de creación por clic, por Q, por la herramienta 145 y por el menú contextual se
conservan, sin diferencias semánticas nuevas entre rutas equivalentes.

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE`. Nivel de aceptación:
`STATIC` + `PHASE PRE-G9B-MAINT-U2` + `INTEGRATION`, más smoke manual del autor.
`FINAL = NOT AUTHORIZED`.

## Implementation base
<!-- geocedg-field: implementation_base -->

- Repositorio `mpradovelasco/GeoCeDG`.
- Commit `e79ace7d1473f13d32d0b660e92a4d1d45ce39bf`.
- Árbol `9c43dfa5548a0844215c0ab9ba808f20e3dd3a84`.

Trabajo aislado en un worktree propio, hermano del repositorio principal
(`GeoCeDG-wt-maint-u2`, rama local `phase/pre-g9b-maint-u2`, creada desde el commit exacto).
No se trabaja en el worktree principal ni se cambia la rama de otro agente. Otros agentes
desarrollan `MAINT-N1` y `MAINT-A-N1` en paralelo: sus worktrees, ramas y directorios de
resultados no se tocan, y no se incorpora su trabajo (sin rebase, merge ni cherry-pick).

## Authority and evidence hierarchy

1. `AGENTS.md`, código, pruebas y contratos de serialización vigentes en la base.
2. ADR 0019 y ADR 0041 con sus enmiendas; `geocedg/specs/locus/locus-v2-point-interaction.md`;
   `geocedg/specs/locus/candidates/cu7-m08-point-placement.md`; resolución autoral CU7-R5
   (`docs/validation/pre_g9b_cu7_r5_author_decision_record.md`); decisión E del mantenimiento
   posterior a 1.2.0 («Point on Object» conservada,
   `docs/validation/geocedg_1_2_0_post_release_locus_v2_point_interaction_closeout_record.md`).
3. Planificación aprobada de `PRE-G9B-MAINT-P0`: diseño D
   (`docs/architecture/pre_g9b_maint_p0_cu7_revised_design.md`) §1 y hoja F §0 (D-U2). Es
   autoridad de planificación; no sustituye el código, las pruebas ni los contratos normativos.
4. Contrato de verificación vigente: `tools/agent/verify.ps1`, el registro tipado y sus
   esquemas, y la sección activa de `geocedg/specs/operations/verification-levels.md`.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGReferencePointTool.java`.
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGEuclidianController.java`,
  únicamente para la activación del alias 145, si hace falta.
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGMetricEndpointAssistance.java`.
- Recursos de traducción EN/ES de las cadenas afectadas, si hace falta.
- `docs/user/geocedg_user_guide_en.md` y `docs/user/geocedg_user_guide_es.md`, §8.1 y las
  referencias cruzadas que nombran sus controles.
- Pruebas Desktop nuevas de U2 y su registro en `docs/upstream/modified-files.yml`.
- Registro de la selección `PHASE PRE-G9B-MAINT-U2` con la infraestructura vigente: selecciones
  del inventario JUnit, nodos y `phase_selections` del registro de verificación, pin del
  inventario y literales de `tools/agent/tests/verification-final-coverage.Tests.ps1`.
- Informe del candidato técnico y guía de smoke manual en `docs/validation/`.
- Este prompt, registrado en un commit preparatorio de gobernanza separado del commit técnico.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Núcleo: `LocusPointCreation2D` y todas sus rutas, colocadores y permisos;
  `IndividualForwardPointCreation2D`, `M08SnapshotPlacement2D`, `LocusSnapshotPlacement2D`,
  `ConditionedPointPlacement2D`; evaluadores, certificadores y algoritmos de Locus V2.
- `GeoCeDGReferencePlacement` y la vigencia de `GeoCeDGPointRequest`.
- Identidad, DAG, XML, códecs (`geocedg-locus-address/v1`,
  `geocedg-locus-principal-selection/v1`, `geocedg-locus-parameter-address/v1`), rol de salida,
  Q como padre, Undo semántico.
- Identificadores de modo (1, 501, 134, 145) y de acción; número y coherencia de acciones del
  perfil; retirar la herramienta 145 de la barra.
- La herramienta 134; «Punto en objeto» sobre objetos Classic (decisión E); la colocación nativa
  `AlgoPointOnPath`; las exclusiones de ejes, 3D y cotas.
- Geometría, tolerancias y valores por defecto (radio Q `0,1`, radio de clic tomado de la vista,
  «Permitir arrastre» activo).
- `MAINT-F8`, `MAINT-S0`, `MAINT-S1`, `MAINT-S2`, `MAINT-I2G0` y cualquier otro incremento.
- `CLAUDE.md`, `AGENTS.md`, otros prompts canónicos o de `ai-shell/prompts/`, el roadmap
  canónico, `geocedg_book`, `main`, etiquetas y releases.

## Architectural placement

Capa de aplicación Desktop y perfil de producto: presentación y organización de la interfaz.
La admisión semántica, el enrutamiento y la publicación siguen en el núcleo a través de
`LocusPointCreation2D`; el panel solo selecciona el origen de la petición.

## Required design/specification

Diseño D §1 con la decisión D-U2. No requiere especificación normativa nueva. Si el cambio
exigiera modificar el núcleo, se detiene y se documenta la causa.

## Geometric invariants and degeneracies

Las direcciones creadas por clic y por Q en el mismo locus y con la misma petición son idénticas
a las de la base. Un paso de Undo por creación. Ninguna ruta equivalente adquiere semántica
distinta; el selector solo elige la ruta existente.

## Compatibility and serialization

Sin cambios de XML, modos ni acciones. Las perspectivas y barras de herramientas guardadas
siguen siendo válidas. Los documentos de 1.2.2 se cargan y se guardan igual.

## Required tests and commands
<!-- geocedg-field: required_checks -->

Antes de implementar, caracterizar con las pruebas existentes la conducta de las entradas
afectadas. Nuevas pruebas focales:

1. selector Clic/Q y equivalencia de direcciones cuando corresponda;
2. activación de la herramienta 145 como alias del mismo panel;
3. menú contextual y selección del origen;
4. conservación de modos y del perfil;
5. asistencia de extremos métricos: presentación y garantía;
6. «Permitir arrastre» y un único Undo;
7. mensajes de rechazo y alternativas, en EN y ES;
8. no regresión de Punto en objeto y de los procedimientos R5.

Sin cambios y en verde: `Cu7M08SGeneralCreationTest`, `GeneralPointInteractionDesktopTest`,
`R3PointInteractionDesktopTest`, `G9U1SemanticPointInteractionTest`,
`R4ViewActivationDesktopTest`, `R4NativeBudgetDesktopTest`, `R5ReferenceCreationDesktopTest`,
`R5SemanticPointPointerDesktopTest`, `Cu7M08SReferenceToolTest`, `Cu7M08SDesktopEventsTest`,
`Cu7M08SCorrectionTest`, `Maintenance120PointToolsDesktopTest`,
`Maintenance120ConeInteractionDesktopTest`, `PostR1ConditionedPointDesktopTest`, las pruebas de
perfil e iconos, y las del núcleo `M08PointCreationKernelTest`,
`IndividualForwardPointCreationTest`, `R5ForwardCreationRoutingTest` y
`Maintenance120PointInteractionTest`.

`PHASE PRE-G9B-MAINT-U2` reúne las pruebas nuevas y las anteriores, sin debilitar ninguna
selección existente. Durante el desarrollo se ejecutan pruebas focales con Gradle; no son
evidencia de aceptación. Sobre el commit técnico congelado y limpio:

```powershell
git diff --check e79ace7d1473f13d32d0b660e92a4d1d45ce39bf HEAD
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-MAINT-U2 -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory <directorio-nuevo>
```

Registrar comandos exactos, códigos de salida, identificadores de ejecución, rutas de registro y
cobertura efectiva. No ejecutar `FINAL`. No atribuir cobertura exhaustiva a `CORE` + `FOCUSED`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Autorizado: este incremento U2 (U2-1 a U2-6), el registro de este prompt, la selección `PHASE`,
los commits locales y la verificación `STATIC`, `PHASE` e `INTEGRATION`. No autoriza F8, S0, S1,
S2, I2G0, N1, A-N1 ni ningún otro incremento, ni `FINAL`. La aceptación técnica no es
aprobación autoral.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Commits locales en `phase/pre-g9b-maint-u2`. Sin push, merge, promoción a `main`, etiquetas ni
releases. El worktree se conserva para la revisión del autor.

## Acceptance and closeout

El incremento termina como `TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`. El candidato
congelado no registra una aprobación futura:

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

La aprobación, la promoción y el cierre requieren autorizaciones separadas que nombren el SHA
exacto.

## Required artifacts

- Rama, ruta del worktree, SHA y árbol del commit técnico y del commit de gobernanza.
- Lista de archivos modificados y resultados de verificación con su evidencia.
- Evidencia de no regresión.
- `GUIDE_IMPACT = UPDATED`, con rutas; `BOOTSTRAP IMPACT — NO CHANGE REQUIRED` con su razón;
  impacto en la infraestructura de verificación limitado al registro de la selección.
- Capturas del panel en EN y ES cuando sean necesarias para la revisión.
- Guía breve de smoke manual (EN y ES; clic, Q, 145, menú contextual y asistencia de extremos).
- Deudas residuales.

## Stop conditions

Cualquier cambio necesario en el núcleo; un cambio en una dirección creada; la ruptura de una
perspectiva o barra guardada; un cambio en el número de acciones del perfil; una colisión con
los worktrees o ramas de otros agentes; cobertura incompleta o no fiable; un candidato sucio.

## Deferred debts

Conducta de Ctrl+clic con Punto sobre Locus V2 (fijarla con una prueba si el autor lo pide); F8.
