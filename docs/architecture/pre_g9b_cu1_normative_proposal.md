# PRE-G9B-CU1 — Propuesta normativa revisada: evaluación, composición y política métrica

| Campo | Valor |
|---|---|
| Carácter | Propuesta normativa candidata. **No modifica ninguna especificación ni ADR**: los textos se incorporan a sus documentos solo tras la aprobación autoral, como primer paso de la implementación de `CU1` |
| Estado | `CU1 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Sustituye | las enmiendas candidatas A1.1 y A1.2 de [`CU0`](pre_g9b_cu0_candidate_normative_amendments.md); las enmiendas de `CU2` y `CU3` (A2.x, A3.x) no cambian |
| Encargo | [registro de cierre de `CU0`](../validation/pre_g9b_cu0_closeout_record.md) §3–§5 y §7 |
| Fundamento | [análisis del aliasing métrico](pre_g9b_cu1_metric_aliasing_analysis.md); [estrategia de corrección](pre_g9b_cu1_correction_strategy.md) |
| ADR candidato | [ADR 0037 (PROPOSED)](../adr/0037-locus-v2-metric-guarantee-classes-and-admissibility.md) |

Los textos normativos se proponen en inglés, como sus especificaciones de destino.

## 1. Conformidad y ampliación

| Punto | Destino | Tipo | Motivo |
|---|---|---|---|
| Revisión estable al mover solo el conductor | `locus-v2-semantics.md`, «Dependency graph, revisions and nested composition» | **conformidad** | la regla vigente (una revisión por recálculo que cambia la instantánea semántica; la evaluación puntual no publica revisiones; `semantics.md:169-172`, `presentation.md:96`) se incumple por el token `dependency-update` (H03) |
| Corte construido una vez por revisión; ninguna reconstrucción por consulta | ídem | **conformidad** | `semantics.md:186-191` y `metrics.md:529-537` lo exigen; el evaluador actualiza el corte entero en cada punto (B10.1) |
| Huella exacta de las entradas del corte como contenido semántico | ídem | **aclaración** | hace realizable la regla anterior sin resultados obsoletos |
| Sesión hija con ámbito de corte | ídem; `locus-v2-public-surface.md:336-341` | **concreción de diseño** | la sesión compartida exigida no puede propagarse tal cual al corte sin colisionar con la identidad viva (`INCOHERENT_REVISION`) |
| Contadores anidados públicos | ídem; `public-surface.md:361-371` | **ampliación menor** | el contrato exige contadores deterministas en el anidamiento, pero no define cuáles; hoy tres contadores nunca se registran |
| Consulta entre posiciones con trabajo local | `locus-v2-metrics.md` §14 y §17 | **conformidad** | «no metric-index build per point, same-revision/policy reuse» (`metrics.md:529-537`) |
| Clases de garantía de la métrica | `locus-v2-metrics.md` §10 y §25 nuevo | **ampliación** | la especificación tipa garantías, pero no define cómo se obtiene `CERTIFIED_ERROR_BOUND` ni su relación con la tolerancia |
| Estimador v2 de solo evaluador | `locus-v2-metrics.md` §10, §15 y §25 | **conformidad** («Never describe a sampled chord sum as exact»; hipótesis registradas, `metrics.md:281-284`) **y ampliación** (forma concreta) | H10 y H12 |
| Tolerancia por componente y repartida | `locus-v2-metrics.md` §15 | **conformidad** | §15 define `S` como escala de la geometría, invariante por traslación; el código usa la cuerda de cada nodo (H11, H12) |
| Admisibilidad condicionada a la tolerancia | `locus-v2-metrics.md` §12 y §25 | **ampliación** | §12 admite por defecto cualquier garantía estimada |
| Uso de un resultado como entrada del DAG | `locus-v2-metrics.md` §11, §12 y §25 | **ampliación** | requisito expreso de la decisión de 2026-10-20 |
| Fallos deterministas memorizados y dimensión de trabajo tipada | `locus-v2-metrics.md` §13 y §15 | **ampliación** | §13 dice hoy que un fallo no publica entrada de índice |
| Correcciones documentales | `metrics.md:3` frente a `:721-722`; `public-surface.md:398` | **corrección** | la cabecera declara v1.2 aprobada mientras §22 dice «pending author review», y la referencia de línea de `public-surface.md` está desfasada |

## 2. A1.1-R — Instantánea semántica, corte y composición anidada

Destino: `locus-v2-semantics.md` v1.2, sección «Dependency graph, revisions and nested
composition», con nota de conformidad en `locus-v2-public-surface.md`.

> **Semantic snapshot of a reconstructible definition.** The semantic snapshot of a
> Locus V2 definition reconstructed from a construction slice consists of its
> definition inputs:
>
> - the durable identities and definition/topology revisions of its dependencies;
> - its generator descriptor;
> - an exact fingerprint of the values of every non-driver input that the
>   reconstruction slice reads.
>
> The current driver position, recompute counters and dependency-update ordinals are
> not semantic content. A recompute whose snapshot is unchanged retains the
> published revision and the evaluator instance, so moving only the driver publishes
> no revision. An input whose value cannot be fingerprinted deterministically makes
> every recompute a snapshot change.
>
> **Slice lifecycle.** The reconstruction slice is built or synchronized with its
> inputs at most once per published revision. A point evaluation only applies the
> driver coordinate and updates the driver-dependent subgraph of the slice. It never
> re-reads live construction state and never updates the whole slice.
>
> **Nested definitions.** A Locus V2 copy inside a slice is a nested definition under
> the same snapshot rule. It keeps its revision while its definition inputs are
> unchanged, and its metric component states are reused across the outer point
> queries of one outer revision.
>
> **Scoped sessions.** Nested evaluation runs in a slice-scoped child session of the
> outer session. The child shares the parent's active-key stack, which provides cycle
> detection across levels, and the parent's work budget. Its memoization keys are
> qualified by the slice scope, so a nested copy never collides with the live locus
> that carries the same durable identity. Metric, binding and intersection services
> invoked inside a slice use the child session instead of opening unrelated
> sessions.
>
> **Instrumentation.** Each locus exposes deterministic work counters through its
> public instrumentation:
>
> - slice builds and slice synchronizations;
> - revision publications and equivalent recomputes;
> - evaluator calls;
> - aggregated nested counters: nested slice builds, nested revision publications,
>   nested metric component builds and nested evaluator calls.
>
> Duration is never a counter.

Destino adicional, `locus-v2-metrics.md` §14:

> A between-position query against a cached component state performs local work
> bounded by the leaves that contain its endpoints. It neither rebuilds the component
> state nor spends a fresh complete work budget.

Evidencia exigida:

- fijaciones `pins.H03` y `pins.B10.1` invertidas;
- puertas G1–G5 de la [matriz de validación](../validation/pre_g9b_cu1_validation_matrix.md);
- prueba de no obsolescencia frente a una construcción recién creada con el mismo estado
  (V-REV-2);
- referencias `preserve` reproducidas.

**Prueba existente que contradice la regla vigente.**
`LocusV2PointDrivenCreationRegressionTest.java:58-66` exige que la revisión **aumente**
al mover solo el conductor, en contra de `semantics.md:169-172`. `CU1` debe cambiar esa
aserción por la igualdad, de forma explícita (decisión D-CU1-8). La parte que exige una
revisión nueva al cambiar el radio se mantiene.

## 3. A1.2-R — Política métrica v2

Destino: `locus-v2-metrics.md` v1.5, §25 nuevo («PRE-G9B-CU1 normative amendment: metric
guarantee classes, estimator v2, admissibility and budgets»), con los ajustes de §10, §12,
§13 y §15 indicados.

### 3.1 Clases de garantía

> **25.1 Guarantee classes.** Every metric contribution belongs to exactly one
> guarantee class, recorded in its error evidence together with its method and work.
>
> 1. **`STRUCTURAL`.** The value follows from a structural identity of the provider,
>    evaluated with outward rounding:
>    - a semantic zero or collapsed image;
>    - an affine image `|a|·Δu`;
>    - a similarity image of a source with an established length, `λ·L_source`;
>    - a similarity image, with driver-independent parameters, of a circle, circular
>      arc or segment driver with a closed-form length.
>
>    The guarantee is `CERTIFIED_ERROR_BOUND` with the rounding bound. `EXACT_ARITHMETIC`
>    is reserved for exact zeros.
> 2. **`CERTIFIED_ENCLOSURE`.** A rigorous enclosure `[lower, upper]` of the semantic
>    length, obtained from outward value and first-derivative enclosures of the
>    semantic curve over a partition of parameter boxes:
>    - lower bound: the sum of chord lower bounds;
>    - upper bound: the sum of `chord_upper / cos α`, where `α < π/2` is the
>      half-angle of a cone with apex at the origin that contains the derivative
>      enclosure of the box. A box without such a cone contributes
>      `|box|·sup|F′|`.
>
>    The value is a point of the enclosure. The guarantee is `CERTIFIED_ERROR_BOUND`
>    with error `upper − lower`. A box the enclosure model refuses is unresolved and
>    prevents this class for the component.
> 3. **`ESTIMATED`.** A refinement estimate whose error amount is justified only by
>    recorded hypotheses. The guarantee is `ESTIMATED_ERROR`. Agreement between
>    refinement levels or sample sets, however many, never produces class 1 or 2.
> 4. **`NOT_ESTABLISHED`.** No value: the work budget is exhausted, the configuration
>    is degenerate, a consistency check fails or the information is insufficient. The
>    value is `Absent` with a typed status and cause.
>
> **25.2 Capability order.** The engine tries, in order:
>
> 1. structural capabilities;
> 2. certified enclosure;
> 3. differential estimate;
> 4. evaluator-only estimate;
> 5. unsupported.
>
> A capability that cannot produce its class falls through to the next. Its rigorous
> partial evidence (for example an enclosure wider than the tolerance) may be attached
> to the later result.

Ajuste de §10: la lista ordenada de capacidades se sustituye por la de 25.2, con la
clase que produce cada una.

### 3.2 Estimador v2

> **25.3 Estimator v2.** Estimates of class 3 (evaluator-only and differential) obey
> the following rules.
>
> a. **Component tolerance.** The tolerance is `τ = max(eps_metric_abs,
>    eps_metric_rel·S)`, where `S` is a translation-invariant extent of the whole
>    component image and never a local chord. It is distributed over the leaves of the
>    partition in proportion to their parameter share.
> b. **Minimum partition.** Before any acceptance, the component is partitioned
>    uniformly into `2^d_min` cells, split additionally at known semantic
>    breakpoints. `d_min` is a versioned policy constant.
> c. **Leaf acceptance.** A leaf is accepted only when both conditions hold:
>    - the Richardson-corrected estimates of two consecutive levels agree within its
>      share of `τ`;
>    - an interior probe at a non-dyadic fraction of the leaf (a versioned constant,
>      for example the golden-section point) is consistent with the leaf's chord
>      model within the same share.
>
>    The reported value is the Richardson-corrected sum. The reported error is a
>    versioned safety factor times the level difference.
> d. **Recorded hypotheses.** The result records, with their numerical values:
>    - piecewise `C²` regularity at scales above the finest probe spacing `h_probe`;
>    - the Richardson asymptotic regime on accepted leaves;
>    - no feature narrower than `h_probe`.
> e. **Failure.** A consistency failure that persists after the permitted refinement
>    ends as `NOT_ESTABLISHED` with `CONSISTENCY_NOT_ESTABLISHED`. Reaching the
>    parameter resolution without acceptance is reported with
>    `PARAMETER_RESOLUTION_REACHED`, never accepted silently.

Ajuste de §15: `S` es la extensión de la componente y no la de un nodo; el umbral se
reparte. Los valores `eps_metric_abs = 1e-10` y `eps_metric_rel = 1e-9` se mantienen como
valores por defecto de la tolerancia solicitada.

### 3.3 Coherencia de tolerancias y admisibilidad

> **25.4 Tolerance coherence and admissibility.**
>
> - **Strongest class.** A result's guarantee is the strongest class whose error
>   amount does not exceed `τ`.
> - **Wide enclosures.** A certified enclosure wider than `τ` is attached as
>   supplementary rigorous evidence to an estimated value, which must lie inside it.
>   An estimate outside its own certified enclosure is rejected with
>   `CONSISTENCY_NOT_ESTABLISHED`.
> - **Scalar adapter.** The scalar adapter publishes a value for classes 1 and 2. For
>   class 3 it publishes a value only when the estimated error does not exceed `τ`;
>   otherwise the scalar is undefined with the typed cause.
> - **Rich result.** The rich result always carries the class, the error amounts, the
>   hypotheses and the work evidence. The rich result and the scalar adapter report
>   the same value.

Ajuste de §12: «by default, at least an estimated-error guarantee» pasa a «a class 1 or 2
guarantee, or a class 3 guarantee whose estimated error does not exceed `τ`».

### 3.4 Uso como entrada del DAG

> **25.5 Use as a DAG input.** The admissibility of a metric value as an input of
> other constructions depends on its class:
>
> - **Classes 1 and 2.** A certified input: downstream certificates may use its
>   enclosure.
> - **Class 3.** An ordinary numeric input under the *estimate contract*. It
>   propagates through normal dependencies, but every downstream metric result that
>   depends on it carries at most `ESTIMATED_ERROR`. No downstream certificate
>   (existence, intersection, materialization or metric) may rely on it as a rigorous
>   input: a certifier whose captured program contains such a value treats it as
>   uncertified.
> - **Class 4.** Makes its consumers undefined through the normal kernel DAG.

Esta regla mantiene definida la cadena `o_1 → LocusLength → d_2 → e_2` (clase 3) sin
pretender certificarla. No convierte en indefinidas las métricas generales que superan
las puertas de estimación: solo pierden la pretensión de garantía.

### 3.5 Presupuestos y fallos

> **25.6 Budgets and failures.**
>
> - **Scope.** Work budgets apply per component-state build and per query, with
>   explicit inheritance for nested evaluations through the child session (A1.1-R).
> - **Values.** Budget values are versioned policy constants justified by
>   reproducible benchmarks; they are not mathematical constants.
> - **Failure memoization.** A deterministic failure is memoized under its complete
>   key (identity, revision, policy, component). It is reported with its typed work
>   dimension in the rich result and is not recomputed until the key changes.
> - **No universal budget.** No fixed budget is claimed to suffice for every regular
>   component. The cost of classes 2 and 3 grows with the total turning of the
>   component and with `1/√τ`.

Ajuste de §13: «Failed builds publish no index entry» pasa a «a failed build publishes a
memoized failure entry for its complete key».

### 3.6 Determinismo, invariancia y versiones

> **25.7 Determinism and invariance.** For a fixed revision and policy, results are
> bitwise deterministic and independent of view, zoom, DPI and render state. Under a
> similarity of the whole construction, classes and relative errors are invariant.
>
> **25.8 Versioning.** The public policy identifier becomes `cu1-metric-policy/v2`.
> The algorithm, policy and tolerance versions take part both in the complete index
> key and in the result provenance.

## 4. Constantes candidatas

Las fija el banco durante la implementación, dentro de estos intervalos que se someten a
la aprobación autoral (decisión D-CU1-4):

| Constante | Candidato | Intervalo | Justificación |
|---|---|---|---|
| `d_min` | 6 (64 celdas) | 4–8 | resuelve H10 con `k = 1, 2, 4` sin coste apreciable en el corpus |
| Fracción de sondeo | `(√5 − 1)/2` | fracción irracional fija | evita la conmensurabilidad con la rejilla diádica |
| Factor de seguridad del error estimado | 2 | 1,5–4 | calibrado con circunferencias, elipses construidas y `o_1` frente a referencias independientes |
| Presupuesto por construcción de componente (clase 3) | 32 768 evaluaciones | sin cambio salvo que el banco lo exija | con Richardson y tolerancia por componente, `o_1` y las circunferencias de H11 caben con margen (H04) |
| Presupuesto de envolvente (clase 2) | 16 384 cajas | 4 096–65 536 | certifica circunferencias a ≈ `10⁻⁸`; por encima se adjunta como evidencia |
| Presupuesto heredado por consulta anidada | igual al de la consulta externa | — | trabajo anidado contabilizado en la sesión hija |

## 5. Dependencias con `CU2` y `CU3`

Ninguna corrección de `CU1` exige cambiar un contrato de `CU2` o `CU3`. Quedan registradas
estas dependencias, que la implementación debe vigilar con la regla de parada del cierre
de `CU0` (§5):

| Id | Dependencia | Efecto en `CU1` |
|---|---|---|
| DEP-1 | La envolvente certificada de programas fuera de la clase v1/v2 (rayos, intersecciones acotadas, recta–circunferencia, expresiones generales) necesita la clase v3 del modelo certificado (enmienda A2.2, `CU2`) | `o_1`, `e_2` y H10 quedan en clase 3 en `CU1` |
| DEP-2 | Los certificadores de `CU2` y `CU3` deben tratar como no certificada una entrada métrica de clase 3 (25.5) | `CU1` solo fija la regla métrica; hoy ningún certificador captura valores métricos |
| DEP-3 | La existencia por compatibilidad (`completeCompatibility`) no cambia en `CU1`; la métrica total sigue exigiendo `COMPLETE` | la clase 2 verifica validez caja a caja, pero no publica evidencia de existencia (propiedad de `CU2`) |
| DEP-4 | La topología de `e_2` (componente abierta, evento de envoltura) es de `CU2` | ninguna |

## 6. Borradores de especificación y ADR

- ADR candidato:
  [`docs/adr/0037-locus-v2-metric-guarantee-classes-and-admissibility.md`](../adr/0037-locus-v2-metric-guarantee-classes-and-admissibility.md),
  con estado **PROPOSED**. El número es provisional hasta la aceptación.
- Especificaciones: los textos de §2 y §3 se incorporan, tras su aprobación y como
  primer paso de la implementación, a:
  - `locus-v2-semantics.md` (v1.1 → v1.2);
  - `locus-v2-metrics.md` (v1.4 → v1.5);
  - una nota de conformidad en `locus-v2-public-surface.md`.

  En el mismo paso se corrigen las dos incoherencias documentales de §1. Hasta
  entonces, las especificaciones vigentes no cambian.

## 7. Decisiones

Las decisiones que esta propuesta necesita están en la
[hoja de decisiones de `CU1`](../validation/pre_g9b_cu1_author_decision_sheet.md).
