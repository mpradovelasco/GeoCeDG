# PRE-G9B-CU2 — Propuesta normativa: existencia, continuidad, componentes y representación

| Campo | Valor |
|---|---|
| Carácter | Propuesta normativa candidata. **No modifica ninguna especificación ni ADR vigente**: los textos se incorporan a sus documentos solo tras la aprobación autoral, como primer paso (T0) de la implementación de `CU2` |
| Estado | `CU2 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Sustituye | las enmiendas candidatas A2.1 a A2.4 de [`CU0`](pre_g9b_cu0_candidate_normative_amendments.md). Las enmiendas de `CU3` (A3.x) no cambian |
| Encargo | [registro de cierre de `CU1`](../validation/pre_g9b_cu1_closeout_record.md) §7 |
| Fundamento | [diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md); [evento de la longitud orientada](pre_g9b_cu2_oriented_length_event_design.md); [consumo y representación](pre_g9b_cu2_consumption_and_representation.md); [análisis del modelo del autor](pre_g9b_cu2_author_model_certification_analysis.md) |
| ADR candidato | [ADR 0038 (PROPOSED)](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md), que extiende el ADR 0027 |

Los textos normativos se proponen en inglés, como sus especificaciones de destino.

## 1. Conformidad y ampliación

| Punto | Destino | Tipo | Motivo |
|---|---|---|---|
| Retirada del certificado de compatibilidad | ADR 0027 l. 114; `existence-components` §9, §10 y §12 | **conformidad** | el productor declara `COMPLETE` sin prueba (`AlgoDependentPointLocusV2.java:189-191`) contra «Dependent producers fail closed as `NOT_ESTABLISHED`» |
| Cuatro niveles de evidencia y evidencia exploratoria tipada | `existence-components` §2–§5; ADR 0038 | **ampliación** | no existe nivel exploratorio; decisión DA-1 |
| Continuidad de `F` como requisito de componente | `existence-components` §4 | **ampliación** | hoy basta existencia más continuación del selector; no hay frontera para un salto con evaluaciones válidas a ambos lados (B10.2) |
| Componentes como arcos en dominios periódicos | `existence-components` §2–§3 | **ampliación** | una componente que cruza la costura no se puede expresar (`LocusInterval2D`) |
| Indefinición computacional frente a no existencia | `existence-components` §3 | **aclaración** | `DEPENDENCY_UNDEFINED` cuenta hoy como no existencia certificada |
| Programa de clase v3 y resultado `UNIFORMLY_INVALID` | `certified-construction-model` §3–§9 | **ampliación** | el modelo no distingue indecidible de uniformemente inválido y excluye las operaciones del modelo del autor |
| Transiciones certificadas por raíz simple de un predicado de validez | `existence-components` §7 (autoridad 4) | **concreción** | §7 admite predicados de intervalos sin definir la prueba |
| Totalidad y periodicidad estructurales | `existence-components` §7 (autoridad 1); `certified-construction-model` §7 | **ampliación** | §7 de `certified-construction-model` declara periodo 0 y ninguna identificación periódica |
| Autoridad única de cierre | `existence-components` §12; `presentation`; `metrics` §22; `intersections` | **conformidad** (§12 ya exige el certificado para el DXF) **y ampliación** a todos los consumidores | hoy cinco predicados distintos |
| Certificado estructural de la función métrica y evento de envoltura | `metrics` §22, §24.5 y §25.5 | **ampliación** y **aclaración** | el valor de clase 3 no es entrada rigurosa; la estructura de la función sí se puede certificar |
| Regla de publicación | `semantics`, «Status and degeneration axes»; `public-surface` §6.3 (347-350) | **aclaración** | los ejes de estado separan la definición de la evaluación, y desde `CU1` la instantánea semántica no depende de la posición del conductor (A1.1-R); el código exige aún el punto trazado definido (H01) |
| Ninguna definición obsoleta tras un fallo de publicación | `semantics`, «Status and degeneration axes» | **conformidad** | el contrato ya prohíbe reutilizar coordenadas o datos de dibujo obsoletos; hallazgo F-CU2-3 |
| Linaje estructural de componentes y migración de selectores | `existence-components` §8; `point-interaction` §16; `intersections` | **ampliación** | la identidad incluye hoy los bits de los extremos |
| Topología del dibujo y política de teselación v2 | `presentation`, «Render continuity contract» | **ampliación** | no hay regla de cierre, de calidad de teselación ni de aproximación a fronteras (B06, B07) |
| Reglas de exportación DXF y LaTeX | `dxf-curve-fidelity-and-approximation`; especificación durable de LaTeX | **conformidad** y **traslado** | las reglas de LaTeX solo están en un registro de validación (DQ-C5) |

## 2. A2.1-R — Evidencia, componentes, arcos y publicación

Destino: `locus-v2-existence-components.md` v1.1 (§2–§5, §7, §8 y §11) y `locus-v2-semantics.md`
v1.3, sección «Status and degeneration axes», con una nota de alcance en
`locus-v2-public-surface.md` §6.3. Requiere el ADR 0038.

> **Evidence levels.** Locus V2 existence evidence has four levels:
>
> 1. `POINT_VALID`: one valid evaluation at one parameter. It implies nothing about any
>    interval.
> 2. `EXPLORATORY`: sampled valid runs, validity transitions bracketed by bisection and
>    suspected jumps, with method, policy version and work recorded. It is a separate typed
>    structure, derived per revision and policy, not persisted and not part of the semantic
>    snapshot. It is never promoted to a component, a completeness claim or an identity, and
>    it is never exported.
> 3. `LOCAL_CERTIFIED`: a certified component (below), maximal or a local subinterval with
>    explicit unresolved bands.
> 4. `COMPLETE`: a certified cover of the whole canonical domain by certified components,
>    certified nonexistence and certified boundaries, with finitely many components.
>
> A finite exploration never establishes level 3 or 4.

> **Certified component.** A parameter set `I` is a certified component of a branch when
> every validity predicate of its construction is certified true on `I`, every captured
> selection is the same geometric rule on `I`, the traced map is continuous on `I`, and each
> end of `I` is a declared domain end, a certified transition, a structural event or the edge
> of an unresolved band. A structural discontinuity with valid evaluations on both sides is a
> boundary, never an interior point. End inclusion follows the semantics of the boundary
> predicate, not pointwise evaluation.

> **Arcs on periodic domains.** On a periodic canonical domain a component is an arc given
> by its canonical start, its length in `(0, period]` and the inclusion of each end. An arc of
> full length whose traced map is certified continuous at its closing point is a closed
> periodic component; any other arc, including a full-length arc with a structural
> discontinuity at its closing point, is open. Membership is decided on the circle. Component
> order is canonical start order and is never identity.

> **Computational undefinedness.** Coverage `CERTIFIED_DOES_NOT_EXIST` requires a validity
> predicate certified false, or a dependency whose own certificate establishes
> nonexistence. A dependency that is undefined because its computation failed, for example a
> metric of class `NOT_ESTABLISHED` or an estimate above its tolerance, yields `UNRESOLVED`
> coverage when the failure decides existence, and only a point evaluation status
> `NOT_ESTABLISHED` when existence is already certified.

> **Publication.** A Locus V2 is published when its definition inputs (every input that is
> not the driver, together with the subgraph that does not depend on the driver) are defined.
> Whether the traced point is defined at the current driver position never affects
> publication. Existence over the driver domain is stated only by the existence certificate.
> A defined definition whose certified cover is empty is published as valid, `COMPLETE`, with
> no component. A recompute that does not publish discards the previous semantic definition;
> no consumer may evaluate a stale definition.

> **Component lineage.** The durable lineage of a component is its branch key together with
> the ordered sources of its boundaries: `(durable operation identity, predicate kind, side)`
> for a certified transition, `(event source, side)` for a structural event, or the declared
> domain end. Moving a boundary without changing its source keeps the lineage. Appearance,
> disappearance, split and merge of components are recorded as typed lineage transitions.
> Persisted component selectors are resolved by unique containment in a certified component
> of the current revision and rewritten lazily; without unique containment the selector is
> unresolved and its point undefined with a typed cause.

`locus-v2-public-surface.md` §6.3 (líneas 347-350) se acota: un punto queda indefinido cuando
desaparece su componente por un cambio de la definición, no por la posición actual del conductor.

## 3. A2.2-R — Modelo de construcciones certificadas, clase v3

Destino: `locus-v2-certified-construction-model.md` v1.2 (§3–§9).

> **Class v3.** A program of class v3 has its own version identifier; v1 and v2 programs are
> not relabelled. Class v3 adds:
>
> - rays and segments, as a line with a parameter-membership validity predicate;
> - circles by centre and constant radius, and by centre and point;
> - line–circle intersection, including indexed single outputs, where each output is
>   identified by a geometric root rule (smaller or larger line parameter) captured once per
>   revision from the non-continuous output permutation and certified constant on every box;
>   continuous-mode intersections stay outside the class;
> - algorithms with several outputs, as one step with indexed outputs;
> - scalar multiples of vectors, dot products, unit vectors of vectors with a nonzero
>   validity predicate, and point-plus-vector;
> - expansion of a macro whose internal steps are all in the class;
> - a structural metric input (A2.3-R), which contributes its structural certificate and
>   never its value;
> - second-order derivative enclosures for the operations of the class, used to certify
>   isolated stationary points.

> **Box outcome.** Evaluating a program on a parameter box yields `VALID` (every validity
> predicate certified true), `UNIFORMLY_INVALID` with a typed reason (`DISCRIMINANT_NEGATIVE`,
> `RAY_PARAMETER_NEGATIVE`, `SEGMENT_PARAMETER_OUTSIDE`, `ZERO_DIRECTION`, `ZERO_RADIUS`,
> `DIVISOR_ZERO`) or `UNRESOLVED`. Only `UNIFORMLY_INVALID` may produce
> `CERTIFIED_DOES_NOT_EXIST`.

> **Certified transition.** A transition is certified when the validity predicate `P` of one
> identified operation changes sign across a bracket and an enclosure of `P′` over the
> bracket excludes zero; the bracket is narrowed to width at most `δ`. The end is closed for a
> discriminant root and for a ray or segment parameter root, and open for a vanishing divisor,
> direction or radius. When the enclosures of `P` and `P′` both contain zero on a box that
> cannot be subdivided further within the budget, the box is an unresolved band.

> **Structural totality and periodicity.** An operation is structurally total when it is
> defined and continuous for every valid input; the total operations form a versioned table.
> A program whose driver is a point of a circle and whose operations are functions of their
> current inputs, with captured selections certified constant on each component, defines a
> function on the circle: its boxes may wrap the seam, and it may claim the periodic
> identification of its driver circle. The identification belongs to the real semantic map:
> outward enclosures of the real driver functions absorb the binary64 seam gap that §7
> records for the evaluator. Otherwise the period is `0` and no periodic identification is
> claimed.

> **Step limit.** The limit applies to driver-dependent steps after macro expansion and is a
> versioned constant.

## 4. A2.3-R — Certificado estructural de la función métrica y evento de envoltura

Destino: `locus-v2-metrics.md` v1.6, §22 y §24.5, y aclaración de §25.5.

> **Structural certificate of a metric function.** For `LocusLength(L, A, B)` whose target
> `B` is the generator occurrence of the driver of `L` and whose origin `A` is a fixed
> semantic position on a certified component `C`, the rich result publishes, as
> revision-scoped derived data that is not persisted, the structure of the function of the
> driver it defines: its definition domain (where the route exists), its continuity away from
> its events, and its events. These structural facts derive only from the certified
> structure of `C`, from semantic addresses and from the route policy; they never derive from
> the computed value.

> **Origin wrap event.** When `C` is a certified closed periodic component, the structure
> contains one event `ORIGIN_WRAP` at the driver parameter equal to the canonical address of
> `A`, with the traversal direction, the same-position side, a reference to the total length
> of `C` with its own guarantee class, and `jumpPositive`. `jumpPositive` is `CERTIFIED` when an
> outward-rounded chord between two certified points of `C` is positive; otherwise it is
> `NOT_ESTABLISHED`. The event changes the value of no query.

> **25.5 (clarification).** A class-3 value is never a rigorous input of a certificate. The
> structural certificate of a metric function is not a value and may be used by an existence
> certificate.

## 5. A2.4-R — Topología del dibujo y teselación

Destino: `locus-v2-presentation.md` versión 2 (hoy `1`), «Render continuity contract».

> **Render topology.** Rendering is derived from the existence structure. Each certified
> component is tessellated up to its certified boundaries. No render segment crosses an
> unresolved band, a certified nonexistence interval or a structural discontinuity. A curve is
> closed only when its component is certified closed periodic. A new subpath starts only at
> the start of a component. Rendering never falls back to uniform sampling and never infers
> a join, a closure or a discontinuity from samples. Exploratory evidence, when its display is
> enabled, is drawn with a distinct style only inside unresolved bands and is never joined to
> a component.

> **Tessellation policy.** The render policy is versioned and deterministic per view. A span
> is accepted when its chord deviation and its turning angle with the adjacent span are both
> within versioned limits, after a versioned minimum depth. The turning limit is waived only in
> a minimal parameter band around a certified isolated stationary point. Near a certified
> discriminant transition the tessellation uses a local square-root reparametrization and
> ends at the boundary image; at a structural discontinuity it ends at the one-sided limit.
> A vertex budget per view coarsens the result deterministically, never crossing a boundary.

## 6. A2.5 — Alineación de los consumidores

Destinos: `locus-v2-intersections.md` y `locus-v2-extended-intersections.md` (marco de fase del
arco y cuarentena solo en componentes periódicas cerradas); `locus-v2-point-interaction.md` §16
(componente certificada contenedora, decisión E-2; linaje estructural);
`locus-v2-metrics.md` §23.4, §24.4 y §24.5 (extremos en una componente certificada; total,
`full-cycle` y `wrap-to-start` exigen `COMPLETE` o una componente periódica cerrada);
`locus-v2-similarity-transformations.md` (herencia explícita de la estructura de existencia);
`locus-v2-semantics.md` (referencia a ADR 0027 y ADR 0038).

> **Consumer contract.** Every consumer reads the existence structure. Local consumers accept
> a certified component containing their parameters; global consumers require `COMPLETE`.
> Periodic semantics (full cycle, wrap to start, seam quarantine, lift and seam side) apply
> only to a certified closed periodic component. The compatibility view of valid domain
> components never decides topology.

## 7. A2.6 — Exportaciones

Destinos: `dxf-curve-fidelity-and-approximation.md` (versión siguiente) y una especificación
durable de la exportación LaTeX (decisión D-CU2-11).

> **Export topology.** An export approximation follows the render topology: one open or closed
> path per certified component, in arc order across the seam, closed only for a certified
> closed periodic component, and ending at certified boundary images or one-sided limits. A
> component with an open end is exported, not dropped. Without `COMPLETE`, export uses
> incomplete fidelity with its mandatory sidecar, and every exported component remains strict.

## 8. Constantes candidatas

| Constante | Candidato | Intervalo propuesto | Se fija con |
|---|---|---|---|
| Anchura máxima del corchete de una transición `δ` | 2⁻⁴⁰ veces el periodo | 2⁻⁴⁸ a 2⁻³² | precisión de las fronteras frente a la referencia analítica |
| Presupuesto de cajas de existencia por rama y revisión | 4 096 | 1 024 a 16 384 | banco de certificación del modelo del autor |
| Profundidad mínima de existencia | 6 (64 cajas) | 4 a 8 | banco de certificación: anchura de las envolventes y coste en la partición inicial |
| Límite de pasos dependientes del conductor (clase v3) | 256 | 128 a 512 | corte de `e_2` con macros expandidas |
| Profundidad mínima de teselación `d_min` | 4 | 2 a 6 | banco `tessellation` (DC-6) |
| Giro máximo `θ_max` | 10° | 5° a 20° | banco `tessellation` (DC-6) |
| Presupuesto de vértices por vista | 4 096 | 1 024 a 16 384 | banco `tessellation` |

Como en `CU1` (D-CU1-4), el autor aprueba los intervalos y el candidato de implementación
presenta los valores elegidos con su evidencia.

## 9. Dependencias

- **`CU1`:** se respetan sin cambio las clases de garantía (§25), el contrato de estimación
  (D-CU1-2) y la estabilidad de revisiones (A1.1-R). La regla de publicación es coherente con
  A1.1-R: no depende del conductor.
- **`CU3`:** `CU2` no define puntos estructurales, contactos ni materialización de raíces
  tangentes. Las fronteras certificadas y los chorros de segundo orden son entradas para `CU3`.
- **ADR 0028 y 0029:** la clase v3 extiende la certificación por capacidad del ADR 0028; la
  regla de publicación no toca la proyección de dependencias durables del ADR 0029.

## 10. Borradores

- [ADR 0038 (PROPOSED)](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md).
- Versiones de destino: `locus-v2-existence-components.md` 1.1,
  `locus-v2-certified-construction-model.md` 1.2, `locus-v2-metrics.md` 1.6,
  `locus-v2-presentation.md` 2 y `locus-v2-semantics.md` 1.3, más las notas de A2.5 y A2.6.

## 11. Decisiones

Las decisiones necesarias están en la
[hoja de decisiones de `CU2`](../validation/pre_g9b_cu2_author_decision_sheet.md).
