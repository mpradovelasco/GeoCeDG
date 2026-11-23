# GeoCeDG — PRE-G9B-MAINT-B5a: guarda de saltos en rutas condicionadas

**PROMPT CANDIDATO — NO AUTORIZADO.** Requiere la enmienda `MAINT-N1` (§28.3) aprobada y una
instrucción autoral con base exacta.

## Objective
<!-- geocedg-field: objective -->

Una ruta de una región condicionada no se admite como número cuando la prueba de escalado de
cuerdas de §28.3 enmendada observa la firma de un salto. Cierra el escalar erróneo confirmado
por la sonda P-1, que es un incumplimiento no detectado de la hipótesis H3. El recorrido
condicionado no se toca.

La guarda es una comprobación numérica conservadora con presupuesto de trabajo acotado, no
una prueba universal de continuidad: las rutas que siguen admitidas no pasan a tener
continuidad certificada. Es la prioridad científica del programa.

`CHANGE_ROUTE=ORDINARY`. `VERIFICATION_CLASS=INTEGRATED_PHASE`. Nivel previsto: `STATIC` +
`PHASE PRE-G9B-MAINT-B5A` + `INTEGRATION`. `selfApproved=false`.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor, que contengan la enmienda N1 aprobada.

## Authority and evidence hierarchy

`AGENTS.md` §11.4 y §14; `locus-v2-metrics.md` §28 enmendado; contrato de uso condicionado
§4-§5; código de `LocusMetricEstimator2D`, `LocusMetricEngine2D.conditionedBetween` y
`LocusMetricResult2D`. Evidencia: plan C §1, informe §B.5 y §F.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- Paquete `org.geocedg.common.kernel.locus.metric`: la admisión de rutas condicionadas y,
  si hace falta, un diagnóstico nuevo.
- Pruebas nuevas en `source/shared/common-jre/src/test/.../locus/` y una prueba Desktop si se
  observa el número en la interfaz.
- Selecciones de inventario y registro para `PRE-G9B-MAINT-B5A`.
- La prueba que fija la reordenación de piezas de deslizadores (B4), solo prueba.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

`LocusConditionedTraversal2D` y su política (la identidad de contenido de la evidencia no
cambia); certificados, P2, H10; `Intersect`; serialización.

## Architectural placement

Núcleo Java compartido: semántica métrica de Locus V2.

## Required design/specification

`locus-v2-metrics.md` §28.3 enmendado (N1). Si la implementación exige un parámetro no fijado
por la enmienda, detenerse.

## Geometric invariants and degeneracies

- Monotonía de la longitud acumulada por rama.
- Determinismo para una revisión y tolerancia fijas.
- Invariancia de zoom y DPI.
- Ninguna ruta admitida que cruce un salto de la batería.
- Las curvas suaves con puntos estacionarios o parametrización muy no uniforme siguen
  admitidas.
- Trabajo adicional por ruta dentro del tope de la enmienda; si se agota, negativa con causa
  propia, nunca un número.

## Compatibility and serialization

Sin XML nuevo. Un documento con `LocusLength(L, A, B)` sobre una ruta que cruza un salto pasa
de número a indefinido: declararlo en la nota de versión.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- Pruebas nuevas:
  - recta con salto `(1, 2)` en `0,75` y arco con salto tangente (la sonda P-1), sin escalar;
  - el tramo anterior al salto sigue siendo número;
  - un extremo de tipo pliegue mantiene su causa actual;
  - batería adversarial del plan C §1.5 (ángulos de 0 a `0,35 rad`, tamaños desde la
    resolución, posiciones diádicas y no diádicas, varios saltos, salto de ida y vuelta,
    oscilación junto al salto); lo que la guarda no pueda negar (discontinuidad evitable) se
    fija como límite declarado;
  - no regresión de admisibilidad: parametrizaciones `u ↦ u³` y `u ↦ u⁸`, puntos
    estacionarios de orden 1 y 2, lazos estrechos, velocidad divergente bajo el umbral;
  - agotamiento del presupuesto: negativa con su causa.
- Pruebas que deben seguir verdes sin cambios: `PostR1ConditionedMetricTest` (incluida
  `aRouteAcrossAJumpThatTheTraversalDoesNotSeeIsNoNumber`), `PostR1ConditionedEvidenceTest`
  (incluida la prueba fijada del recorrido) y `PostR1LengthDeletionDesktopTest`.
- Medición: rutas admitidas hoy en los modelos del autor (`w_2`, `a_3`, `trHole`) y en las
  baterías post-R1, antes y después, con el recuento de evaluaciones adicionales de la
  guarda.
- Comandos:
  - `Set-Location <repo>; [Environment]::CurrentDirectory = <repo>`;
  - `tools/agent/verify.ps1 -Profile STATIC`, `-Profile PHASE -Phase PRE-G9B-MAINT-B5A`,
    `-Profile INTEGRATION`, con registros fuera de `artifacts/` si así lo pide la política;
  - `git diff --check`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Implementación y verificación de este incremento. Ninguna otra corrección del bloque B.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local propia. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

Candidato congelado para revisión autoral, con informe de rutas antes y después.

## Required artifacts

Commit técnico, commit de inventario, identificadores de verificación, informe del
candidato, `GUIDE_IMPACT` (probable `UPDATE` breve de la sección de longitudes condicionadas),
impacto de bootstrap: sin cambio.

## Stop conditions

- Una ruta admitida hoy en los modelos del autor o en las baterías deja de admitirse sin
  firma de salto, o por agotamiento del presupuesto.
- La guarda exige tocar el recorrido.
- Un fallo de `INTEGRATION` atribuible al cambio.

## Deferred debts

B5b; descubrimiento de raíces de `Intersect` a través de un salto alineado (inferido, sin
prueba).
