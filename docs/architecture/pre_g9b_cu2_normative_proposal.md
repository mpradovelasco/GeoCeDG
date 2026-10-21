# PRE-G9B-CU2 — Propuesta normativa: existencia, continuidad, componentes y representación

| Campo | Valor |
|---|---|
| Carácter | Propuesta normativa candidata. **No modifica ninguna especificación ni ADR vigente**: los textos se incorporan a sus documentos solo tras la aprobación autoral, como primer paso (T0) de la implementación de `CU2` |
| Estado | Enmiendas A2.1-R a A2.6 **aprobadas por el autor** el 2026-10-21 con la precisión D-CU2-20-R ([registro](../validation/pre_g9b_cu2_author_architecture_approval_record.md)) e incorporadas a sus especificaciones de destino en el paso T0; el texto incorporado pertenece al candidato técnico de `CU2`, pendiente de revisión autoral; `selfApproved=false` |
| Sustituye | las enmiendas candidatas A2.1 a A2.4 de [`CU0`](pre_g9b_cu0_candidate_normative_amendments.md). Las enmiendas de `CU3` (A3.x) no cambian |
| Encargo | [registro de cierre de `CU1`](../validation/pre_g9b_cu1_closeout_record.md) §7; revisión focalizada P1–P5 del autor (2026-10-20) |
| Fundamento | [diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md); [evento de la longitud orientada](pre_g9b_cu2_oriented_length_event_design.md); [consumo y representación](pre_g9b_cu2_consumption_and_representation.md); [análisis del modelo del autor](pre_g9b_cu2_author_model_certification_analysis.md) |
| ADR | [ADR 0038](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md), que extiende el ADR 0027, aceptado como decisión arquitectónica el 2026-10-21 |

Los textos normativos se proponen en inglés, como sus especificaciones de destino.

**Revisión focalizada de 2026-10-20.** La arquitectura no cambia. Cambian estos textos:

| Precisión | Enmiendas revisadas |
|---|---|
| P1, valor crítico | A2.2-R: certificado algebraico exacto; A2.1-R: parámetro singular interior |
| P2, extremos de `e_2` | A2.1-R: coordenada de arco y extremos laterales; A2.3-R: los dos extremos del corte |
| P3, pliegues | A2.2-R: puntos estacionarios certificados; A2.4-R: exenciones |
| P4, selección e identidad | A2.1-R: identidades, linaje y selectores; A2.2-R: estado de selección |
| P5, existencia y disponibilidad | A2.1-R: existencia, evaluación y representación; A2.4-R y A2.6: fidelidad declarada |
| Coherencia (§8 de la instrucción) | A2.1-R: completitud como recubrimiento; A2.2-R: `UNIFORMLY_INVALID` por desigualdad estricta o identidad estructural |

**Cierre de coherencia de 2026-10-20** (instrucción autoral «PRE-G9B-CU2 — Cierre de coherencia
normativa y registro del programa de apéndices científicos»). La arquitectura tampoco cambia:

| Punto | Enmiendas revisadas |
|---|---|
| C1, salidas efectivas en raíces coincidentes | A2.2-R: estado de selección con el etiquetado de las salidas y salidas efectivas en raíces coincidentes; A2.1-R: huella exacta de la revisión (hallazgo F-CU2-12) |
| C2, componentes puntuales | A2.1-R: componentes puntuales; A2.4-R, A2.5 y A2.6: su consumo y su representación |
| C3, naturaleza de la evidencia | A2.2-R: transiciones, raíces múltiples y certificado algebraico exacto |

## 1. Conformidad y ampliación

| Punto | Destino | Tipo | Motivo |
|---|---|---|---|
| Retirada del certificado de compatibilidad | ADR 0027 l. 114; `existence-components` §9, §10 y §12 | **conformidad** | el productor declara `COMPLETE` sin prueba (`AlgoDependentPointLocusV2.java:189-191`) contra «Dependent producers fail closed as `NOT_ESTABLISHED`» |
| Cuatro niveles de evidencia y evidencia exploratoria tipada | `existence-components` §2–§5; ADR 0038 | **ampliación** | no existe nivel exploratorio; decisión DA-1 |
| Completitud como recubrimiento certificado | `existence-components` §5 | **conformidad** | §5 ya prohíbe aproximar un conjunto finito deteniendo un muestreador |
| Continuidad de `F` como requisito de componente | `existence-components` §4 | **ampliación** | hoy basta existencia más continuación del selector; no hay frontera para un salto con evaluaciones válidas a ambos lados (B10.2) |
| Componentes como arcos y extremos laterales | `existence-components` §2–§3 | **ampliación** | una componente que cruza la costura no se puede expresar (`LocusInterval2D`), y la canonicalización módulo `2π` identificaría los dos extremos de `e_2` (P2) |
| Existencia, evaluación y representación | `existence-components` §3 y §11 | **aclaración** y **ampliación** | `DEPENDENCY_UNDEFINED` cuenta hoy como no existencia certificada; la existencia certificada no garantiza coordenadas ni representación (P5) |
| Programa de clase v3 y resultado `UNIFORMLY_INVALID` | `certified-construction-model` §3–§9 | **ampliación** y **corrección** | el modelo no distingue indecidible de uniformemente inválido y excluye las operaciones del modelo del autor; una envolvente que contiene el cero no prueba que un divisor se anule en toda la caja |
| Estado de selección de las intersecciones | `certified-construction-model` §3 y §9; `existence-components` §6 y §8 | **ampliación** y **aclaración** | la permutación de salida es estado del algoritmo y se reinicializa por cercanía (P4, hallazgo F-CU2-8) |
| Transiciones certificadas por raíz simple de un predicado de validez | `existence-components` §7 (autoridad 4) | **concreción** | §7 admite predicados de intervalos sin definir la prueba |
| Certificado algebraico exacto de predicados de primer nivel | `existence-components` §7 (autoridad 3, fronteras analíticas); `certified-construction-model` | **ampliación** | el aislamiento ordinario de raíces simples no certifica una raíz múltiple, y el valor crítico de la mordedura quedaba `NOT_ESTABLISHED` (P1, C3) |
| Naturaleza de la evidencia | ADR 0038; `existence-components` §7 | **aclaración** | separa prueba exacta, intervalos rigurosos, verificación bajo hipótesis, exploración y falta de evidencia, y las constantes binary64 capturadas de las relaciones ideales (C3) |
| Componentes puntuales | `existence-components` §2–§3, §8 y §11; `metrics`; `presentation` | **ampliación** | no hay entidad para una existencia reducida a un parámetro aislado, que no es un arco de longitud positiva (C2) |
| Salidas efectivas en raíces coincidentes | `certified-construction-model` §3 y §9; `existence-components` §4 | **ampliación** | la regla heredada anula la segunda salida sin etiqueta que coincide con la primera: la existencia de la raíz no implica la de la salida (C1, hallazgo F-CU2-13) |
| Huella exacta de la revisión y fidelidad del corte | `semantics`, «exact fingerprint» (A1.1-R) | **conformidad** | la huella de `CU1` no incluye el estado de selección ni el de etiquetado, y nada valida la fidelidad del corte; la atribución inicial de F-CU2-12 a una serialización de 15 cifras se corrigió el 2026-10-21. Precisión autoral D-CU2-20-R |
| Puntos estacionarios certificados y parámetros singulares | `certified-construction-model`; `presentation` | **ampliación** | una envolvente de `F′` que contiene el cero no prueba un pliegue (P3) |
| Totalidad y periodicidad estructurales | `existence-components` §7 (autoridad 1); `certified-construction-model` §7 | **ampliación** | §7 de `certified-construction-model` declara periodo 0 y ninguna identificación periódica |
| Autoridad única de cierre | `existence-components` §12; `presentation`; `metrics` §22; `intersections` | **conformidad** (§12 ya exige el certificado para el DXF) **y ampliación** a todos los consumidores | hoy cinco predicados distintos |
| Certificado estructural de la función métrica y evento de envoltura | `metrics` §22, §24.5 y §25.5 | **ampliación** y **aclaración** | el valor de clase 3 no es entrada rigurosa; la estructura de la función sí se puede certificar |
| Regla de publicación | `semantics`, «Status and degeneration axes»; `public-surface` §6.3 (347-350) | **aclaración** | los ejes de estado separan la definición de la evaluación, y desde `CU1` la instantánea semántica no depende de la posición del conductor (A1.1-R); el código exige aún el punto trazado definido (H01) |
| Ninguna definición obsoleta tras un fallo de publicación | `semantics`, «Status and degeneration axes» | **conformidad** | el contrato ya prohíbe reutilizar coordenadas o datos de dibujo obsoletos; hallazgo F-CU2-3 |
| Identidades, linaje estructural y resolución de selectores | `existence-components` §8; `point-interaction` §16; `intersections` | **ampliación** | la identidad incluye hoy los bits de los extremos, y la contención única no demuestra una correspondencia (P4) |
| Topología del dibujo, teselación v2 y fidelidad declarada | `presentation`, «Render continuity contract» | **ampliación** | no hay regla de cierre, de calidad de teselación, de aproximación a fronteras ni de fidelidad (B06, B07, P5) |
| Reglas de exportación DXF y LaTeX | `dxf-curve-fidelity-and-approximation`; especificación durable de LaTeX | **conformidad** y **traslado** | las reglas de LaTeX solo están en un registro de validación (DQ-C5) |

## 2. A2.1-R — Evidencia, componentes, arcos, disponibilidad, publicación e identidad

Destino: `locus-v2-existence-components.md` v1.1 (§2–§5, §7, §8 y §11) y `locus-v2-semantics.md`
v1.3, secciones «Status and degeneration axes» y enmienda de `CU1` (huella exacta), con una nota
de alcance en `locus-v2-public-surface.md` §6.3. Requiere el ADR 0038.

> **Evidence levels.** Locus V2 existence evidence has four levels:
>
> 1. `POINT_VALID`: one valid evaluation at one parameter. It implies nothing about any
>    interval.
> 2. `EXPLORATORY`: sampled valid runs, validity transitions bracketed by bisection and
>    suspected jumps, with method, policy version and work recorded. It is a separate typed
>    structure, derived per revision and policy, not persisted and not part of the semantic
>    snapshot. It is never promoted to a component, a completeness claim or an identity, it
>    has no authority over the dependency graph, and it is never exported.
> 3. `LOCAL_CERTIFIED`: a certified component (below), maximal or a local subinterval with
>    explicit unresolved bands.
> 4. `COMPLETE`: a certified cover of the whole canonical domain by certified components,
>    certified nonexistence and certified boundaries, with finitely many components. The union
>    of the certified sets is the whole domain; completeness is never inferred from explored
>    boxes being valid or from not having found other components.
>
> A finite exploration never establishes level 3 or 4.

> **Certified component.** A parameter set `I` is a certified component of a branch when
> every validity predicate of its construction is certified true on `I`, every captured
> selection is the same rule on `I`, the traced map is continuous on `I`, and each end of `I`
> is a declared domain end, a certified transition, a structural event or the edge of an
> unresolved band. A structural discontinuity with valid evaluations on both sides is a
> boundary, never an interior point. A certified singular parameter, an even-multiplicity zero
> of a validity predicate that keeps its sign, is interior to the component. End inclusion
> follows the semantics of the boundary predicate, not pointwise evaluation. A certified
> component is an interval or arc of positive length, or a point component.

> **Arcs on periodic domains.** On a periodic canonical domain a component is an arc given
> by its canonical start, its length in `(0, period]` and the inclusion of each end. An arc of
> full length whose traced map is certified continuous at its closing point is a closed
> periodic component; any other arc, including a full-length arc with a structural
> discontinuity at its closing point, is open. Each arc carries its arc coordinate
> `s ∈ [0, length]` from its start. A canonical parameter `t` belongs to the arc when an
> included coordinate `s ≡ t − start (mod period)` exists; on a full-length arc with one
> included end this coordinate is unique. Component order is canonical start order and is
> never identity.

> **One-sided ends.** The two ends of a full-length open arc share a canonical parameter and
> are distinct ends: each has its side, its inclusion, its one-sided limit and its boundary
> source. The excluded end is never the image of a canonical parameter; it is reached only
> through an explicit one-sided limit query. Canonicalization never identifies the two ends,
> and continuity between them is never inferred from coinciding coordinates or from a periodic
> driver.

> **Point components.** When a certificate proves that existence reduces to an isolated
> parameter `t*`, for example an even-multiplicity zero of a validity predicate that is
> nonpositive on both sides, the set `{t*}` is a point component: a kind of component of its
> own, never an interval or arc of positive length. It carries a certified enclosure of `t*`,
> which in general is not a binary64 value, and the structural source of the isolated root
> (operation, predicate and root). Its inclusion is closed. Its continuity is trivial and
> asserts no continuation and no direction. It is maximal when certified nonexistence bounds it
> on both sides, and local otherwise; a cover by point components, other components and
> certified nonexistence is `COMPLETE`. Its lineage is its branch key and its structural source;
> appearance, disappearance and change of dimension (point to arc, when the even root splits
> into two simple roots that bound an arc, and arc to point) are typed lineage transitions. Its
> length is zero and it contributes zero to every total; a route exists only within it and
> measures zero. Membership and selection use its structural address, never equality with a
> binary64 parameter. Its position is a certified enclosure of the limit form where one exists;
> otherwise it exists with its position unavailable. It is never represented as a segment or a
> curve, and a consumer that does not support point components declares the limitation and
> fails explicitly. A point component is never confused with a certified singular parameter,
> which is interior to a component of positive length. It creates no public point object and
> materializes no intersection.

> **Existence, evaluation and representation.** Coverage `CERTIFIED_DOES_NOT_EXIST` requires a
> validity predicate certified false, or a dependency whose own certificate establishes
> nonexistence. A dependency that is undefined because its computation failed, for example a
> metric of class `NOT_ESTABLISHED` or an estimate above its tolerance, yields `UNRESOLVED`
> coverage when the failure decides existence, and only an `EVALUATION_FAILED` point status
> with a computational cause when existence is already certified. Mathematical existence and
> certified topology (stated by the certificate), numerical availability (the evaluator) and
> representation availability (rendering and export) are distinct facts, and none is inferred
> from another: a valid evaluation never proves existence, a failed one never proves
> nonexistence, and certified existence is never a numerical guarantee of coordinates. Where
> the certificate establishes nonexistence, a point returned by the evaluator is not published
> as valid.

> **Publication.** A Locus V2 is published when its definition inputs (every input that is
> not the driver, together with the subgraph that does not depend on the driver) are defined.
> Whether the traced point is defined at the current driver position never affects
> publication. Existence over the driver domain is stated only by the existence certificate.
> A defined definition whose certified cover is empty is published as valid, `COMPLETE`, with
> no component. A recompute that does not publish discards the previous semantic definition;
> no consumer may evaluate a stale definition.

> **Exact fingerprint (clarification of the `CU1` amendment).** The fingerprint of the
> semantic snapshot is exact: every change of a definition input changes it, including a change
> of one unit in the last place of a captured binary64 value and a change of the selection state
> of a captured selection. A serialization that does not reproduce every binary64 value exactly
> is not an exact fingerprint. A certificate is valid only for the revision whose fingerprint
> it cites and is never reused for a state that its fingerprint does not distinguish.

> **Identities, lineage and selectors.** The durable identity of a construction result, the
> semantic identity of its selection and the lineage of a component are distinct. The
> semantic identity of a selection is the rule that maps the inputs to the selected result:
> the algorithm and its version, the raw root order and the algorithm's selection state; it is
> an input of the revision. The lineage of a component is its branch key together with the
> ordered sources of its boundaries: `(durable operation identity, predicate kind, side)` for
> a certified transition, `(event source, side)` for a structural event, or the declared
> domain end. Moving a boundary without changing its source keeps the lineage. Appearance,
> disappearance, split and merge of components are typed lineage transitions. No revision,
> recompute or temporary change of existence rebuilds an association by label, index,
> coordinates or proximity. A persisted component selector resolves only through a
> demonstrated structural correspondence: equality of the decomposition, or a recorded lineage
> transition from the cited component under the same selection rule. Unique containment of its
> parameter validates that correspondence and never creates one. Without it the selector is
> unresolved and its point undefined with a typed cause. Resolution never rewrites the
> persisted selector.

`locus-v2-public-surface.md` §6.3 (líneas 347-350) se acota: un punto queda indefinido cuando
desaparece su componente por un cambio de la definición, no por la posición actual del conductor.

## 3. A2.2-R — Modelo de construcciones certificadas, clase v3

Destino: `locus-v2-certified-construction-model.md` v1.2 (§3–§9).

> **Class v3.** A program of class v3 has its own version identifier; v1 and v2 programs are
> not relabelled. Class v3 adds:
>
> - rays and segments, as a line with a parameter-membership validity predicate;
> - circles by centre and constant radius, and by centre and point;
> - line–circle intersection, including indexed single outputs (below);
> - algorithms with several outputs, as one step with indexed outputs;
> - scalar multiples of vectors, dot products, unit vectors of vectors with a nonzero
>   validity predicate, and point-plus-vector;
> - expansion of a macro whose internal steps are all in the class;
> - a structural metric input (A2.3-R), which contributes its structural certificate and
>   never its value;
> - second-order derivative enclosures for the operations of the class, used for the
>   isolation and non-degeneracy of stationary points.

> **Selection state.** Each output of a line–circle intersection is defined by its algorithm,
> never by a sample: the raw roots are ordered by the parameter along the algorithm's line
> direction, and the output is selected through the algorithm's selection state (its
> permutation, its initialization flags and the label state of its outputs). The capture reads
> that state, and the revision fingerprint includes it. At every revision synchronization the
> evaluator slice must reproduce the selection state of the live algorithm; otherwise the
> selection is `NOT_ESTABLISHED` with a typed diagnostic. An algorithm still in its
> initialization selects by compaction of the roots that lie on its limited paths; this rule is
> certified only where the number of such roots is proven constant, and a change of that number
> is a selection boundary. A change of the live selection state, including the end of the
> initialization, changes the fingerprint and publishes a revision with a diagnostic.
> Continuous-mode intersections and reference-point selections stay outside the class.

> **Effective outputs at coincident roots.** The mathematical existence of a root, its
> multiplicity, the semantic identity of the selected solution, the effective definition of the
> output that defines the traced point, its availability in the evaluator slice, the continuity
> of the one-sided limits and the continuity of the selected construction are distinct facts,
> and none is inferred from another. A component is never declared continuous, and its locus
> never `COMPLETE`, only because the geometric equation has a double root, when the indexed
> output that defines the traced point is undefined at that parameter. Where the inherited
> algorithm sets an unlabelled second output undefined because it coincides with the first
> within a tolerance, the effective definition of that output near a coincident root depends on
> tolerance and rounding: the certificate states neither existence nor continuity of that
> output across the coincident root, and publishes around it an unresolved band with a typed
> cause, bounded by conservative first-level predicates; outside the band the output exists
> where its validity predicates are certified. Where both outputs are labelled in the live
> construction and in the slice, the inherited rule does not apply and the double root is
> classified by the exact certificate. A certified limit value at a coincident root is
> evidence, never an available output, and no output identity is ever replaced by another
> because their points coincide.

> **Box outcome.** Evaluating a program on a parameter box yields `VALID` (every validity
> predicate certified true), `UNIFORMLY_INVALID` with a typed reason (`DISCRIMINANT_NEGATIVE`,
> `RAY_PARAMETER_NEGATIVE`, `SEGMENT_PARAMETER_OUTSIDE`, `ZERO_DIRECTION`, `ZERO_RADIUS`,
> `DIVISOR_ZERO`) or `UNRESOLVED`. Only `UNIFORMLY_INVALID` may produce
> `CERTIFIED_DOES_NOT_EXIST`. Interval enclosures establish it only for strict-inequality
> predicates; an equality predicate (vanishing divisor, direction or radius) is uniformly
> invalid only through an exact structural identity, and an enclosure that contains zero
> yields `UNRESOLVED`.

> **Certified transition.** A transition is certified when the validity predicate `P` of one
> identified operation changes sign across a bracket and an enclosure of `P′` over the
> bracket excludes zero; the bracket is narrowed to width at most `δ`. The end is closed for a
> discriminant root and for a ray or segment parameter root, and open for a vanishing divisor,
> direction or radius. When the enclosures of `P` and `P′` both contain zero on a box that
> cannot be subdivided further within the budget, the box is decided by the exact algebraic
> certificate, when `P` is first-level, or by an isolated stationary point of `P` (a simple root
> of `P′` certified with an enclosure of `P″` that excludes zero) at which the enclosure of `P`
> excludes zero; otherwise it is an unresolved band.

> **Exact algebraic certificate.** A validity predicate is first-level when its inputs are
> captured constants and points that depend rationally on the driver, and the driver runs on
> a circle or a segment. Its captured constants are exact rationals; with `u = tan(t/2)` it
> becomes a polynomial with exact rational coefficients, and `t = ±π` is evaluated separately.
> Square-free factorization and Sturm sequences in exact integer arithmetic give its real
> roots, their multiplicities and the signs between them. An odd-multiplicity root with a sign
> change is a certified transition with a closed end. An even-multiplicity root with a
> nonnegative predicate on both sides is a certified singular parameter, interior to its
> component. An even-multiplicity root with a nonpositive predicate on both sides is an
> isolated point of existence, a point component. Simple-root isolation (a sign change with a
> derivative enclosure that excludes zero) never certifies a multiple root; in class v3 the
> multiple roots of first-level predicates are certified by this certificate, and those of
> other predicates remain unresolved. The certificate decides the configuration defined by the
> captured binary64 constants, never an ideal geometric relation that the construction computes
> with rounding. Exhausting the certificate budget yields `UNRESOLVED`.

> **Certified stationary points.** A stationary point of the traced map is certified only by
> separate proofs of: existence, from an exact structural symmetry `σ` of the driver domain
> with `F ∘ σ = F`, verified step by step on the captured program; isolation, from an enclosure
> of one component of `F″` that excludes zero on a box around it; non-degeneracy,
> `F″(t₀) ≠ 0`; continuity, as an interior point of a certified component; and, for a fold,
> opposite one-sided tangents. An enclosure of `F′` that contains zero certifies nothing.
> Without these proofs a high turn is not a certified fold.

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
> `A`, with the traversal direction, the same-position policy, its two one-sided ends, a
> reference to the total length of `C` with its own guarantee class, and `jumpPositive`.
> `jumpPositive` is `CERTIFIED` when an outward-rounded chord between two certified points of
> `C` is positive; otherwise it is `NOT_ESTABLISHED`. The two ends of the cut share the
> canonical address of `A` and are distinct: each carries its side, its inclusion under the
> same-position policy and its one-sided limit. The certificate establishes that both limits
> exist; their numerical evaluation is a separate fact. The event changes the value of no
> query.

> **25.5 (clarification).** A class-3 value is never a rigorous input of a certificate. The
> structural certificate of a metric function is not a value and may be used by an existence
> certificate.

## 5. A2.4-R — Topología del dibujo, teselación y fidelidad

Destino: `locus-v2-presentation.md` versión 2 (hoy `1`), «Render continuity contract».

> **Render topology.** Rendering is derived from the existence structure. Each certified
> component is tessellated up to its certified boundaries. No render segment crosses an
> unresolved band, a certified nonexistence interval or a structural discontinuity. A curve is
> closed only when its component is certified closed periodic. A new subpath starts only at
> the start of a component. Rendering never falls back to uniform sampling and never infers
> a join, a closure or a discontinuity from samples. A point that cannot be evaluated is never
> interpolated: the path is cut there with a computational-gap mark. Exploratory evidence,
> when its display is enabled, is drawn with a distinct style only inside unresolved bands and
> is never joined to a component. A point component is never drawn as a segment or a curve;
> presentation version 2 declares it, with its parameter enclosure and the status of its
> position, and does not draw it.

> **Tessellation policy.** The render policy is versioned and deterministic per view. A span
> is accepted when its chord deviation and its turning angle with the adjacent span are both
> within versioned limits, after a versioned minimum depth. The turning limit is waived only in
> a minimal parameter band around a certified fold and at the vertex of a certified singular
> parameter, which is drawn as an exact vertex. Near a certified discriminant transition the
> tessellation uses a local square-root reparametrization and ends at the boundary image; at a
> structural discontinuity it ends at the one-sided limit, or at the last evaluated vertex,
> with a mark, when that limit cannot be evaluated. A vertex budget per view coarsens the
> result deterministically, never crossing a boundary.

> **Declared fidelity.** Each rendering or export approximation declares its effective
> fidelity per component (complete, partial or failed) with its causes, and fails explicitly
> when it cannot represent a result. It never invents a point, a join or an end.

## 6. A2.5 — Alineación de los consumidores

Destinos: `locus-v2-intersections.md` y `locus-v2-extended-intersections.md` (marco de fase del
arco y cuarentena solo en componentes periódicas cerradas); `locus-v2-point-interaction.md` §16
(componente certificada contenedora, decisión E-2; linaje estructural y resolución de
selectores por correspondencia); `locus-v2-metrics.md` §23.4, §24.4 y §24.5 (extremos en una
componente certificada; total, `full-cycle` y `wrap-to-start` exigen `COMPLETE` o una
componente periódica cerrada); `locus-v2-similarity-transformations.md` (herencia explícita de
la estructura de existencia); `locus-v2-semantics.md` (referencia a ADR 0027 y ADR 0038).

> **Consumer contract.** Every consumer reads the existence structure. Local consumers accept
> a certified component containing their parameters; global consumers require `COMPLETE`.
> Periodic semantics (full cycle, wrap to start, seam quarantine, lift and seam side) apply
> only to a certified closed periodic component; the ends of an open arc are addressed through
> their one-sided limits. The compatibility view of valid domain components never decides
> topology.

> **Point components in consumers.** A point component contributes zero to totals and to
> routes. A consumer that addresses a component through a binary64 parameter (semantic points,
> interaction, metric positions, principal branch) cannot select it and fails explicitly with a
> typed cause. An intersection never reports `EMPTY` or a complete set while it has not
> excluded a point component of its input rigorously, and a dependent locus whose support has a
> point component declares the limitation and is not `COMPLETE`. No consumer treats a point
> component as an arc.

## 7. A2.6 — Exportaciones

Destinos: `dxf-curve-fidelity-and-approximation.md` (versión siguiente) y una especificación
durable de la exportación LaTeX (decisión D-CU2-11).

> **Export topology.** An export approximation follows the render topology: one open or closed
> path per certified component, in arc order across the seam, closed only for a certified
> closed periodic component, and ending at certified boundary images or one-sided limits. A
> component with an open end is exported, not dropped. An end that cannot be evaluated is
> exported up to the last evaluated vertex and declared in the sidecar or in LaTeX comments,
> and the export is then incomplete. Certified singular parameters are exported as exact
> vertices. Without `COMPLETE`, export uses incomplete fidelity with its mandatory sidecar, and
> every exported component remains strict. A point component is never exported as a path: the
> sidecar or the LaTeX comments declare it, with its parameter enclosure and the status of its
> position, and the export is then incomplete.

## 8. Constantes candidatas

| Constante | Candidato | Intervalo propuesto | Se fija con |
|---|---|---|---|
| Anchura máxima del corchete de una transición `δ` | 2⁻⁴⁰ veces el periodo | 2⁻⁴⁸ a 2⁻³² | precisión de las fronteras frente a la referencia analítica |
| Presupuesto de cajas de existencia por rama y revisión | 4 096 | 1 024 a 16 384 | banco de certificación del modelo del autor |
| Profundidad mínima de existencia | 6 (64 cajas) | 4 a 8 | banco de certificación: anchura de las envolventes y coste en la partición inicial |
| Límite de pasos dependientes del conductor (clase v3) | 256 | 128 a 512 | corte de `e_2` con macros expandidas |
| Bits máximos de los coeficientes intermedios del certificado exacto | 4 096 | 1 024 a 16 384 | banco de certificación (en el modelo: hasta 217 bits) |
| Bisecciones exactas por raíz | 512 | 256 a 2 048 | separación mínima de raíces que se quiere decidir (`2⁻¹⁰⁰` en las pruebas sintéticas) |
| Profundidad mínima de teselación `d_min` | 4 | 2 a 6 | banco `tessellation` (DC-6) |
| Giro máximo `θ_max` | 10° | 5° a 20° | banco `tessellation` (DC-6) |
| Presupuesto de vértices por vista | 4 096 | 1 024 a 16 384 | banco `tessellation` |

Como en `CU1` (D-CU1-4), el autor aprueba los intervalos y el candidato de implementación
presenta los valores elegidos con su evidencia.

## 9. Dependencias

- **`CU1`:** se respetan sin cambio las clases de garantía (§25), el contrato de estimación
  (D-CU1-2) y la estabilidad de revisiones (A1.1-R). La regla de publicación es coherente con
  A1.1-R: no depende del conductor. El estado de selección se añade a la huella de la revisión
  sin cambiar su regla. La huella exacta es conformidad con A1.1-R, pero su corrección toca
  `ReconstructionSliceFingerprint2D` y la reconstrucción del corte, componentes de `CU1`
  (precisión autoral D-CU2-20-R, autorizada dentro de `CU2` sin reabrir el cierre de `CU1`).
- **`CU3`:** `CU2` no define puntos estructurales, contactos ni materialización de raíces
  tangentes. Las fronteras certificadas, los parámetros singulares, las componentes puntuales
  y los chorros de segundo orden son entradas para `CU3`; una componente puntual no crea ningún
  objeto punto.
- **ADR 0028 y 0029:** la clase v3 extiende la certificación por capacidad del ADR 0028; la
  regla de publicación no toca la proyección de dependencias durables del ADR 0029, y la
  resolución de selectores no reescribe ningún contenido persistido.
- **Código heredado:** la lectura del estado de selección necesita accesores de solo lectura en
  clases de GeoGebra, registrados como modificaciones; el estado de etiquetado ya es público
  (`GeoElement.isLabelSet`). La regla heredada que anula la segunda salida sin etiqueta no se
  modifica, y ninguna capacidad nueva la sortea sin la decisión D-CU2-18.

## 10. Borradores

- [ADR 0038](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md):
  borrador de la preparación, `ACCEPTED` como decisión arquitectónica el 2026-10-21.
- Versiones de destino: `locus-v2-existence-components.md` 1.1,
  `locus-v2-certified-construction-model.md` 1.2, `locus-v2-metrics.md` 1.6,
  `locus-v2-presentation.md` 2 y `locus-v2-semantics.md` 1.3, más las notas de A2.5 y A2.6.

## 11. Decisiones

Las decisiones necesarias están en la
[hoja de decisiones de `CU2`](../validation/pre_g9b_cu2_author_decision_sheet.md).
