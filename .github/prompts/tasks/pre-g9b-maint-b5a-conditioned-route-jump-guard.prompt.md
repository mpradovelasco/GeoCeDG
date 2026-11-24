# GeoCeDG — PRE-G9B-MAINT-B5a: comprobación de escalado de cuerdas en rutas condicionadas

**PROMPT DE TAREA AUTORIZADO POR EL AUTOR EL 2026-11-24** («GeoCeDG — PRE-G9B-MAINT. Segunda ola
coordinada: MAINT-B5a + MAINT-A-DXF0 + MAINT-A2», §0 y §7). Es la versión reconciliada del prompt
candidato del expediente P0
([`maint-b5a-conditioned-route-jump-guard.prompt-candidate.md`](../../../docs/architecture/pre_g9b_maint_p0_candidate_prompts/maint-b5a-conditioned-route-jump-guard.prompt-candidate.md))
con la norma §28.3.1 adoptada y con la instrucción del autor; donde discrepan prevalecen la norma
adoptada y la instrucción. `selfApproved=false`. No autoriza ningún otro incremento.

## Objective
<!-- geocedg-field: objective -->

Implementar exclusivamente la comprobación de escalado de cuerdas de
[`locus-v2-metrics.md`](../../../geocedg/specs/locus/locus-v2-metrics.md) §28.3.1, política
`conditioned-route-jump-guard/v1`, como condición 8 de la admisión escalar de una ruta en una
región condicionada (`conditioned-length-scalar/v2`). La comprobación se aplica solo a rutas que
ya satisfacen las condiciones 1 a 7 de §28.3 y no cambia sus causas.

Es una detección numérica conservadora con presupuesto acotado, no una prueba de continuidad: una
firma observada es compatible con una discontinuidad o con una variación singular de la velocidad
y no demuestra que H3 sea falsa; una ruta que pasa conserva H1 a H4 como hipótesis. Diagnósticos
propios y distintos: `CHORD_SCALING_JUMP_SIGNATURE`, `CHORD_SCALING_INCONCLUSIVE`,
`CHORD_SCALING_BUDGET_EXHAUSTED`. Cierra el escalar erróneo de la sonda P-1.

Los valores de la política (`ρ_J`, `m`, `L`, factor de `ε_res(λ)`, `E_cap`) se fijan dentro de los
intervalos de §28.3.1 con pruebas focales, se justifican numéricamente (coste, peor caso de
presupuesto, variaciones concentradas, estacionariedad, saltos alineados y no alineados,
traslaciones, resolución binary64) y se registran en la política y en el informe, con el dominio
de magnitudes de coordenadas en que se mostró estabilidad experimental, sin declararlo garantía
universal.

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE`.
`PRODUCT_PHASE_EFFECT = PRE-G9B-MAINT-B5A`. Nivel de aceptación: `STATIC` +
`PHASE PRE-G9B-MAINT-B5A` + `INTEGRATION` (`CORE + FOCUSED`). `FINAL = NOT AUTHORIZED`.
`AUTHOR_DECISION = PENDING`.

## Implementation base
<!-- geocedg-field: implementation_base -->

- Repositorio `mpradovelasco/GeoCeDG`.
- Commit `228580e9684d2c54650f8d461c2a4d1142a7ef55` (`main` publicado, primera ola
  `PRE-G9B-MAINT` aprobada), árbol `a4c892aa91df1a0ac3e5c16a62bc8b87cd3064ae`.

Trabajo en el worktree hermano `GeoCeDG-wt-maint-b5a`, rama local `phase/pre-g9b-maint-b5a`. Sin
dependencia de las ramas B-spline de la misma ola (`MAINT-A-DXF0`, `MAINT-A2`) ni de su
biblioteca matemática. El checkout principal y los worktrees de la primera ola no se tocan.

## Authority and evidence hierarchy

1. `AGENTS.md`, código, pruebas y contratos de serialización vigentes en la base.
2. `locus-v2-metrics.md` §25, §28 y §28.3.1 (norma adoptada,
   [registro de aprobación](../../../docs/validation/pre_g9b_maint_n1_author_approval_record.md));
   [registro de la enmienda N1](../../../docs/validation/pre_g9b_maint_n1_metrics_amendment_record.md)
   §4 y §6 (condiciones de entrada); ADR 0042 y el contrato de uso condicionado.
3. Expediente P0, bloque B, y este prompt.
4. Contrato de verificación vigente: `tools/agent/verify.ps1`, el registro tipado, sus esquemas y
   la sección activa de `geocedg/specs/operations/verification-levels.md`.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- Paquete `org.geocedg.common.kernel.locus.metric` de `source/shared/common`: la admisión de rutas
  condicionadas (`LocusMetricEngine2D` y su camino `conditionedBetween`), una clase nueva de la
  política `conditioned-route-jump-guard/v1`, los diagnósticos nuevos en `LocusMetricResult2D` o
  en su enumeración de diagnósticos, y la lectura (sin cambiar su conducta) de las hojas aceptadas
  por `LocusMetricEstimator2D`. Si la lectura de las hojas exige exponer datos ya calculados por
  el estimador, solo un accesor aditivo que no cambie valor, error ni trabajo del estimador.
- Pruebas nuevas en `source/shared/common-jre/src/test/java/org/geocedg/common/locus/` y, si el
  número se observa en la interfaz, una prueba Desktop.
- Una prueba que fije la reordenación de piezas de deslizadores (B4), solo prueba.
- Medición de referencia antes y después con sondas temporales fuera del árbol; resultados
  reproducibles en `artifacts/agent/` y resumidos en el informe.
- Selecciones de inventario y registro de la fase `PRE-G9B-MAINT-B5A`, pin del inventario y
  recuentos de `tools/agent/tests/verification-final-coverage.Tests.ps1`.
- Guías de usuario EN/ES: la sección de longitudes condicionadas (`GUIDE_IMPACT`).
- Informe del candidato en `docs/validation/`.
- Este prompt, registrado en un commit preparatorio de gobernanza separado del commit técnico.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- `LocusConditionedTraversal2D`, su política `conditioned-traversal/v1` y la identidad de
  contenido de la evidencia condicionada.
- Inventario de componentes, tokens, identidad semántica, certificados de existencia,
  certificadores, evidencia de existencia, DAG ordinario y semántica de intersecciones.
- El valor y el error de la estimación, el presupuesto del estimador (§25.7), §25.4, §26.5 (H10),
  §26.6 (P2), §28.1 y §28.2 (totales), `Intersect` y su política de aislamiento.
- Serialización y XML.
- B2, B5b, T-A, P2/H10, las ramas y la biblioteca B-spline de la segunda ola.
- La redacción normativa de §28.3.1 y de cualquier especificación aprobada; valores fuera de los
  intervalos admisibles.
- `CLAUDE.md`, `AGENTS.md`, otros prompts canónicos o de `ai-shell/prompts/`, el roadmap,
  `geocedg_book`, `main`, etiquetas y releases.

## Architectural placement

Núcleo Java compartido: semántica métrica de Locus V2 (AGENTS §4, §11.4).

## Required design/specification

`locus-v2-metrics.md` §28.3.1 adoptada. Si la implementación exige un parámetro o una forma de
`ε_res(λ)` que la norma no fija, o un valor fuera de sus intervalos, se detiene.

## Geometric invariants and degeneracies

- Monotonía de la longitud acumulada por rama; valor y error de la estimación sin cambios.
- Determinismo para revisión, política y tolerancia fijas; zoom, DPI, viewport y render nunca
  entran en la comprobación.
- Ninguna ruta de la batería que cruce un salto observable se admite como número.
- Las curvas continuas de referencia (`u ↦ u³`, `u ↦ u⁸`, estacionarios de orden 1 y 2, lazos
  estrechos, velocidad divergente bajo el umbral) siguen admitidas.
- Trabajo de la comprobación contado aparte del estimador, a lo sumo `L` evaluaciones por hoja y
  `E_cap` por ruta; si se agota, negativa con causa propia, nunca un número.

## Compatibility and serialization

Sin XML nuevo y sin carga persistida. Un documento con `LocusLength(L, A, B)` sobre una ruta
condicionada cuya comprobación observa una firma, queda inconclusa o agota el presupuesto pasa de
número a indefinido; su resultado rico conserva la estimación con el diagnóstico. Se declara en la
nota de versión del informe.

## Required tests and commands
<!-- geocedg-field: required_checks -->

Antes del código, medición de referencia reproducible: rutas `w_2`, `a_3`, `trHole` y baterías
post-R1; admisibilidad, valores y errores, hojas aceptadas, evaluaciones y coste, presupuesto.

Pruebas nuevas, cada una con aserciones deterministas:

1. sonda P-1 (recta con salto `(1, 2)` en `0,75` y arco con salto tangente): sin escalar; la ruta
   anterior al salto sigue siendo número; un extremo de pliegue conserva su causa actual;
2. batería adversarial: saltos alineados con ángulos de `0` a `0,35 rad` respecto de la cuerda,
   tamaños desde la resolución por órdenes de magnitud, posiciones diádicas y no diádicas, junto a
   un extremo de la ruta, varios saltos, salto de ida y vuelta en una celda, oscilación junto al
   salto, saltos ocultos por el muestreo finito;
3. no regresión de admisibilidad (lista de invariantes) y de todas las rutas admitidas hoy en los
   modelos del autor y en las baterías post-R1, medidas antes y después con las evaluaciones de la
   comprobación;
4. los tres diagnósticos, cada uno producido por una prueba y distintos entre sí;
5. traslaciones de magnitud creciente, hasta y más allá de donde `2^k · u · M_λ` supera `τ_λ`,
   con comparación de admisibilidad, evaluaciones y diagnósticos frente a la curva sin trasladar;
6. cada límite declarado (§28.3.1 «Declared limits») fijado por una prueba que documenta su
   alcance sin afirmar completitud;
7. reordenación de piezas de deslizadores (B4), solo prueba.

Sin cambios y en verde: `PostR1ConditionedMetricTest` (incluida
`aRouteAcrossAJumpThatTheTraversalDoesNotSeeIsNoNumber`), `PostR1ConditionedEvidenceTest`
(incluida `aJumpAlongTheChordLeavesNoTraceAndIsNotDetected`) y `PostR1LengthDeletionDesktopTest`.

Sobre el commit técnico congelado y limpio:

```powershell
git diff --check 228580e9684d2c54650f8d461c2a4d1142a7ef55 HEAD
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-MAINT-B5A -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory <directorio-nuevo>
```

Registrar comandos exactos, códigos de salida, identificadores, rutas de registro, cobertura,
diagnósticos. No ejecutar `FINAL`; si una campaña exige escalado, emitir
`VERIFICATION_ESCALATION_REQUEST`. No atribuir cobertura exhaustiva a `CORE + FOCUSED`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Autorizado: la comprobación §28.3.1, sus pruebas, la medición, el registro de este prompt y de la
fase `PRE-G9B-MAINT-B5A`, los commits locales y la verificación `STATIC`, `PHASE` e
`INTEGRATION`. No autoriza B2, B5b, T-A, §28.2, otro incremento ni `FINAL`. La aceptación técnica
no es aprobación autoral.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Commits locales en `phase/pre-g9b-maint-b5a`. Sin push, promoción a `main`, etiquetas, releases
ni paquetes de publicación. El worktree se conserva para la revisión del autor; el ensayo técnico
de integración de la segunda ola lo incorpora por merge sin modificar esta rama.

## Acceptance and closeout

Estado final: `PRE-G9B-MAINT-B5A — TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`.

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

## Required artifacts

Commit técnico (con inventario y registro), identificadores de verificación, informe del
candidato con medición antes y después, parámetros elegidos y su justificación, traslaciones,
límites declarados y nota de versión; `GUIDE_IMPACT = UPDATE` (longitudes condicionadas);
impacto de bootstrap: sin cambio.

## Stop conditions

- Una ruta admitida hoy queda inadmisible sin firma de escalado, o por agotamiento del
  presupuesto.
- La implementación exige modificar `LocusConditionedTraversal2D`, tokens, evidencia de
  existencia o certificadores.
- Parámetros fuera de los intervalos de §28.3.1, o una política que no pueda justificarse sin
  una decisión normativa nueva.
- Un fallo de `INTEGRATION` atribuible al cambio.

## Deferred debts

B5b (`conditioned-traversal/v2`); T-A; descubrimiento de raíces de `Intersect` a través de un
salto alineado (inferido, sin prueba).
