# GeoCeDG — PRE-G9B-MAINT-A2: familia B-spline polinómica en el núcleo

**PROMPT CANDIDATO — NO AUTORIZADO.** Requiere la especificación de familia de
`MAINT-A-N1` aprobada y una instrucción autoral con base exacta. No depende de
`MAINT-A-DXF0`.

## Objective
<!-- geocedg-field: objective -->

Implementar el alcance inicial de `bspline-2d/v1` aprobado, el subconjunto polinómico de una
familia preparada para NURBS:

- el comando `BSpline` y sus formas, sin forma con pesos;
- un algoritmo subclase de `AlgoLocusV2`;
- un modelo inmutable con estructura homogénea (pesos implícitos unitarios, no serializados) y
  un evaluador de de Boor homogéneo, sobre la biblioteca matemática común, que implemente
  `LocusEvaluator2D`, `LocusDifferentialEvaluator2D` y `PiecewisePolynomialLocus2D`;
- el identificador de contrato serializado con su regla de versiones;
- la existencia probada por el proveedor;
- persistencia versionada, Undo y reapertura;
- un mínimo de producto: tipo, estilo por defecto, familia de inserción, ayuda y
  traducciones EN/ES.

Sin herramienta gráfica si D-A-3 lo mantiene.

`CHANGE_ROUTE=ORDINARY`. `VERIFICATION_CLASS=INTEGRATED_PHASE`. Nivel previsto: `STATIC` +
`PHASE PRE-G9B-MAINT-A2` + `INTEGRATION`, más smoke autoral. Escalado razonado solo si cambia
una interfaz genérica de Locus V2.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor, que contengan la especificación de familia aprobada
y, si `MAINT-A-DXF0` se autorizó antes, la biblioteca matemática común que creó.

## Authority and evidence hierarchy

`bspline-2d.md` aprobada y su ADR; `AGENTS.md` §10 (arquitectura de comandos), §11 y §14;
ADR 0031 (cabeceras canónicas). Código de referencia: `AlgoSplineV2`,
`SplineSemanticEvaluator2D`, `LocusV2PublicOperations.createSpline`, `CmdSplineV2`.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- Paquete nuevo bajo `org.geocedg.common.kernel` para el modelo y el evaluador; un algoritmo
  nuevo; un procesador de comando nuevo; registro del comando y traducciones.
- Fábrica en `LocusV2PublicOperations`.
- Cadena de tipo, estilo por defecto y admisión de inserción.
- Extensión aditiva de la interfaz polinómica si A1 la fija.
- Corpus de huellas R5-B regenerado.
- Pruebas, escenarios congelados y selecciones.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- SplineV2 y sus pruebas.
- Modelo certificado de intervalos y tokens D2 de `BSpline`.
- Exportación `SPLINE`, importación DXF y aproximación.
- Pesos explícitos, interfaces racionales en el núcleo y NURBS nativa (`MAINT-A-R`).
- Periódica y no anclada.
- Herramienta gráfica (salvo D-A-3).

## Architectural placement

Núcleo Java compartido, AGENTS §4: semántica, DAG, identidad y serialización.

## Required design/specification

`bspline-2d.md` aprobada.

## Geometric invariants and degeneracies

- Los de la especificación.
- La evaluación coincide con una referencia racional exacta independiente.
- Continuidad publicada igual a la estructura de nudos.
- Invariancia de zoom y DPI.
- Determinismo.

## Compatibility and serialization

Comando y contrato nuevos; los lectores 1.2.2 fallan cerrados al encontrar el comando; sin
cambio de documentos existentes.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- Casos BS-1 a BS-13 y BS-15 de la propuesta B §12.
- Huellas R5-B regeneradas desde evidencia ejecutada.
- Sin cambios: `G9S1SemanticSpline2DTest`, `G9S1R1StructuralSplineContinuityTest`,
  `G9S1R1SplinePrecisionAdmissionTest`, `G9S1R1SplinePairMaterializationTest`,
  `PreG9BR4SplineV2AuthoringTest`.
- Comandos: `STATIC`, `PHASE PRE-G9B-MAINT-A2` e `INTEGRATION`; `git diff --check`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Este incremento; no autoriza A3 ni A4.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

Candidato congelado, guía de smoke manual.

## Required artifacts

Commits, verificación, escenarios y evidencia congelados, informe; `GUIDE_IMPACT = UPDATE`
(sección nueva de curvas `BSpline`); impacto de bootstrap: sin cambio.

## Stop conditions

- Necesidad de cambiar SplineV2.
- Interfaz genérica cambiada de forma no aditiva.
- Una decisión de estructura que impediría añadir pesos sin cambiar la identidad conceptual
  de la familia.
- Condicionamiento que la especificación no resuelve.
- Fallo de `INTEGRATION` atribuible.

## Deferred debts

`MAINT-A3`, `MAINT-A-DXF1`, `MAINT-A4`, `A5`, `A7` y `MAINT-A-R`.
