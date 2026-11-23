# GeoCeDG — PRE-G9B-MAINT-B2: barrido de la autoridad rica huérfana

**PROMPT CANDIDATO — NO AUTORIZADO.** Requiere la enmienda `MAINT-N1` (§29) aprobada y una
instrucción autoral con base exacta. D-B-2 quedó aprobada el 2026-11-23 con alcance reducido a
B2-F1.

## Objective
<!-- geocedg-field: objective -->

Al completar correctamente la redefinición de un número de longitud, retirar su autoridad rica
auxiliar privada si quedó sin consumidores, en el borde transaccional adecuado y dentro del
mismo ciclo de Undo/Redo (B2-F1). Respetar las referencias y consumidores legítimos, no retirar
resultados ricos con identidad y uso independiente, no alterar construcciones que sigan
dependiendo de la autoridad rica y no dejar cambios parciales si la redefinición falla y se
revierte.

**Decisión expresa del autor: no se implementa B2-F2.** Ningún barrido al cargar documentos,
ninguna migración ni reparación silenciosa de archivos históricos. Los huérfanos heredados quedan
como limitación documentada.

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
- Pruebas y selecciones `PRE-G9B-MAINT-B2`.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Cualquier barrido al cargar, migración o reparación de documentos existentes (B2-F2).
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

Sin XML nuevo. La carga de documentos existentes no cambia: un documento con huérfanos
heredados los conserva; declararlo como limitación en la nota de versión.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- Invertir `PostR1PrivateMetricAuthorityTest.aRedefinedNumberKeepsTheRecordedLimitation`.
- Redefinir hacia otra `LocusLength`: exactamente una autoridad.
- Redefinición en lote.
- Redefinición fallida y revertida.
- Undo y Redo exactos; guardar y reabrir.
- Documento con huérfano escrito por un candidato anterior (fixture versionado): se abre y se
  guarda sin cambios respecto de hoy.
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

Huérfanos heredados en documentos anteriores: limitación documentada, sin barrido.
