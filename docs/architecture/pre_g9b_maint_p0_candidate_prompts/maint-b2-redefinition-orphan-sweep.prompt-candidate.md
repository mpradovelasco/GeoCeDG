# GeoCeDG — PRE-G9B-MAINT-B2: barrido de la autoridad rica huérfana

**PROMPT CANDIDATO — NO AUTORIZADO.** Requiere la enmienda `MAINT-N1` (§29) aprobada, la
decisión D-B-2 y una instrucción autoral con base exacta.

## Objective
<!-- geocedg-field: objective -->

Al completar la redefinición de un número de longitud, retirar su autoridad rica privada si
quedó sin lector, dentro del mismo paso de Undo. Si D-B-2 lo decide, retirar también al cargar
las autoridades privadas sin lector.

`CHANGE_ROUTE=ORDINARY`. `VERIFICATION_CLASS=INTEGRATED_PHASE`. Nivel previsto: `STATIC` +
`PHASE PRE-G9B-MAINT-B2` + `INTEGRATION`.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor.

## Authority and evidence hierarchy

`AGENTS.md`; `locus-v2-metrics.md` §29 enmendado; ADR 0026 y 0029 (transacción de
redefinición); código de `AlgoLocusMetricScalarAdapter`, `GeoLocusMetricResult`,
`Construction.completeSpatialRedefineOperation`, `rollbackCollectedSpatialRedefines` y
`SpatialIdentityRegistry` (guardas). Evidencia: plan C §2, informe §B.2.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- `AlgoLocusMetricScalarAdapter.remove()`: registrar la candidata en lugar de omitirla.
- Un punto de barrido al completar la operación de redefinición, también tras el conmutador
  final en lotes, fuera del arrendamiento y antes de almacenar el Undo.
- Opcional: un barrido al terminar la carga.
- Pruebas y selecciones `PRE-G9B-MAINT-B2`.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Las guardas de identidad del registro.
- Tratar la redefinición como borrar y crear.
- Retirar resultados ricos con nombre del usuario o con otro lector.
- La sustitución de puntos (bloque C).

## Architectural placement

Núcleo Java compartido: ciclo de vida de objetos y transacción de redefinición.

## Required design/specification

§29 enmendado. Antes de codificar, documentar en el informe el punto exacto del barrido para
redefinición simple, en lote y revertida.

## Geometric invariants and degeneracies

No aplica a la geometría. Invariantes de ciclo de vida: ninguna autoridad con lector se
retira; ningún resultado con nombre del usuario se retira; la reversión conserva el par.

## Compatibility and serialization

Sin XML nuevo. El barrido de carga cambia el contenido de documentos con huérfanos al volver a
guardarlos: declararlo en la nota de versión.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- Invertir `PostR1PrivateMetricAuthorityTest.aRedefinedNumberKeepsTheRecordedLimitation`.
- Redefinir hacia otra `LocusLength`: exactamente una autoridad.
- Redefinición en lote.
- Redefinición fallida y revertida.
- Undo y Redo exactos; guardar y reabrir.
- Documento con huérfano escrito por un candidato anterior (fixture versionado).
- Desktop: modelo del autor con `cuT = 5`, evaluaciones de `w_2` en la base.
- Medición previa del coste aislado de la redefinición en el arnés post-R1.
- Sin cambios: `PostR1LengthDeletionDesktopTest`, `RedefineTest`,
  `G9A3SpatialRedefineTransactionTest`, `PreG9BR6PlusF3RecoveryTest`,
  `DesktopInputRedefineCancelRecoveryTest`.
- Comandos: `STATIC`, `PHASE PRE-G9B-MAINT-B2` e `INTEGRATION` con `tools/agent/verify.ps1`;
  `git diff --check`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Este incremento.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

Candidato congelado para revisión autoral.

## Required artifacts

Commits técnico y de inventario, identificadores de verificación, informe, medición previa,
`GUIDE_IMPACT = NONE` salvo decisión autoral.

## Stop conditions

- El barrido exige relajar una guarda de identidad.
- Una redefinición revertida pierde el par.
- Un resultado con nombre del usuario queda afectado.

## Deferred debts

Ninguna nueva.
