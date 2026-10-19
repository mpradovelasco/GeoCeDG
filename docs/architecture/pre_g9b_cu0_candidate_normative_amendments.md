# PRE-G9B-CU0 — Enmiendas normativas candidatas para `CU1`–`CU3`

| Campo | Valor |
|---|---|
| Carácter | Texto candidato de enmiendas. **No modifica ninguna especificación ni ADR**: cada enmienda se incorporará a su documento solo tras aprobación autoral, con la versión y el registro que exija el procedimiento vigente |
| Estado | `CANDIDATE AMENDMENTS — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Encargo | Instrucción autoral de 2026-10-19, §5 punto 9 |
| Fuentes | [análisis D](pre_g9b_cu0_oriented_length_analysis.md); [propuestas E](pre_g9b_cu0_hybrid_certification_proposal.md), [F](pre_g9b_cu0_tangent_materialization_proposal.md) y [H](pre_g9b_cu0_precision_and_tolerance_study.md); [plan consolidado](pre_g9b_cu_corrective_usability_plan.md) |

Cada ficha indica el documento y la sección afectados, si es **conformidad** (aclara una
regla ya vigente que el código incumple) o **ampliación** (cambia el contrato), el texto
candidato y la evidencia exigida. Los textos normativos se proponen en inglés, como las
especificaciones de destino.

## Para `CU1` (autorización siguiente)

### A1.1 — Estabilidad de revisión y composición anidada (conformidad)

- **Destino:** `geocedg/specs/locus/locus-v2-semantics.md`, «Dependency graph, revisions
  and nested composition».
- **Motivo:** la regla vigente («one monotonically increasing semantic revision per
  normal recompute that changes its semantic snapshot»; «Slice construction may occur at
  most once per locus definition/revision, never per point query») se incumple: la firma
  del evaluador incluye un contador de recálculo (H03) y cada consulta puntual reconstruye
  el corte aislado y la métrica anidada (B10.1).
- **Texto candidato (aclaración):**

  > A semantic snapshot changes only when a definition input, an existence structure or
  > an evaluator contract changes. A recompute counter, a dependency-update ordinal or the
  > current position of the driver is not semantic content, and moving only the driver
  > never publishes a revision. A nested Locus V2 inside an isolated slice keeps its
  > revision while the slice inputs it depends on are unchanged, and its metric component
  > state is reused across outer point queries. Implementations shall expose nested work
  > counters (slice builds, nested revisions, nested metric component builds) through the
  > public instrumentation.

- **Evidencia exigida:** fijaciones `pins.B10.1` y `pins.H03` invertidas; referencias
  `preserve` reproducidas; banco antes/después.

### A1.2 — Política métrica de evaluador, versión 2 (conformidad y ampliación acotada)

- **Destino:** `geocedg/specs/locus/locus-v2-metrics.md` §10 y §15.
- **Motivo:** H10 (la cuerda se acepta como longitud de arco con error cero), H11
  (presupuesto agotado a partir de radio 8) y H12 (precisión menor que la declarada). Es
  conformidad con `AGENTS.md` §1.8 y §11.4 («Never describe a sampled chord sum as exact»)
  y con §10 de la especificación; la forma concreta de la política es ampliación.
- **Texto candidato:**

  > Evaluator-only refinement shall not accept a component estimate before a minimum
  > refinement depth fixed by the versioned policy, and shall require agreement of two
  > consecutive refinement levels within the component tolerance. The component tolerance
  > is `max(eps_metric_abs, eps_metric_rel * S_component)`, where `S_component` is a
  > translation-invariant extent of the component image, and it is distributed over
  > refinement nodes in proportion to their parameter or chord share. The work budget
  > shall be sufficient for the declared relative tolerance on regular components of any
  > size; exhaustion yields `Absent` with `LIMIT_NOT_ESTABLISHED` and the work dimension.
  > A scalar adapter is admissible only when the reported error estimate does not exceed
  > the declared tolerance. The policy, algorithm version and tolerance participate in the
  > metric index key.

- **Valores:** a fijar con el banco (`DT-1`, decisión C1-2).
- **Evidencia exigida:** `pins.H10_H11` invertidas (longitudes de referencia
  `4,1883`, `8,1118`, `16,0647`; circunferencias de radio 8 y 20 medidas);
  `preserve.o_1_total_length_independent` dentro de `10⁻⁶`.

## Para preparar `CU2`

### A2.1 — Niveles de evidencia y certificación estructural (ampliación; requiere ADR)

- **Destino:** `geocedg/specs/locus/locus-v2-existence-components.md` (versión 1.1) y un
  ADR nuevo que amplíe el ADR 0027.
- **Texto candidato (secciones nuevas):**

  > **Evidence levels.** Locus V2 existence evidence has four levels: `POINT_VALID`
  > (one valid evaluation), `EXPLORATORY` (bounded deterministic sampling and transition
  > brackets, recorded with method and work), `LOCAL_CERTIFIED` (the certified
  > continuous-valid components of §4) and `COMPLETE` (§5). `EXPLORATORY` evidence is a
  > separate typed structure; it is never promoted to a component, a completeness claim
  > or an identity.
  >
  > **Structural evidence.** A slice whose driver-dependent operations are all
  > structurally total on their input domain, with certified driver-dependent inputs on an
  > interval, is certified on that interval. A slice that depends on the driver only
  > through the driver point, and whose branch selections are functions of the current
  > configuration, inherits the periodic identification of its driver provider. A
  > dependency may publish an exact structural event (for example a metric wrap event);
  > such an event is boundary authority 1 of §7.
  >
  > **Transitions.** A transition between certified existence and certified
  > nonexistence is certified when the validity predicate of one identified operation has
  > a certified simple root in a bracket. Endpoint inclusion follows the predicate at the
  > root.
  >
  > **Publication.** A dependent locus whose definition inputs are defined is published
  > even when its traced point is undefined at the current driver position; existence
  > over the domain is stated only by its certificate.

- **Cambio de la tabla §11:** las direcciones de extremo métrico (ocurrencia de generador,
  intersección de una fuente) y `Point(L, u)` aceptan una componente localmente
  certificada que contenga la dirección.
- **Linaje (§8):** una componente cuya frontera procede de una fuente estructural recibe
  linaje por esa fuente (operación, predicado y lado), no por los bits del extremo.
- **Evidencia exigida:** `pins.B07`, `pins.H01` y `pins.B10.2` invertidas;
  `preserve.branch_ends` respetados dentro de la tolerancia de frontera; cadena de `e_2`
  válida sin certificado de compatibilidad.

### A2.2 — Programa de construcción certificado, clase v3 (ampliación)

- **Destino:** `geocedg/specs/locus/locus-v2-certified-construction-model.md` (versión
  1.2), §3, §5, §6, §7 y §9.
- **Texto candidato (resumen normativo):**

  > Class v3 adds: bounded meets on rays and segments with a uniform containment
  > predicate; circles by centre and constant radius and by centre and point; line–circle
  > intersections whose output index maps to the computational root fixed by the
  > algorithm's captured permutation in non-continuous mode (continuous mode stays
  > outside the class); scalar products and vectors scaled by driver-dependent scalars;
  > expansion of a user tool whose internal steps are all in the class; and second-order
  > jets. A box outcome is `VALID`, `UNIFORMLY_INVALID` with a typed reason (certainly
  > negative discriminant, certainly vanishing divisor, point at infinity) or
  > `UNRESOLVED`; only `UNIFORMLY_INVALID` may produce `CERTIFIED_DOES_NOT_EXIST`.

### A2.3 — Evento estructural de envoltura métrica (ampliación acotada)

- **Destino:** `geocedg/specs/locus/locus-v2-metrics.md` §22 y §24.5.
- **Texto candidato:**

  > When the target of a between-position query is the generator occurrence of the
  > source locus at the live driver and the origin is a fixed semantic position on one
  > periodic component, the rich result publishes, as revision-scoped derived data, a
  > wrap event: the driver parameter equal to the canonical address of the origin, the
  > cycle length and the same-position side. The event is exact, is not persisted and does
  > not change the value of any query.

- **Evidencia exigida:** valores `preserve.oriented_length_profile` y `preserve.e_2`
  idénticos dentro de la tolerancia; `e_2` publicada como una componente abierta.

### A2.4 — Dibujo por componentes y teselación (conformidad y ampliación)

- **Destino:** `geocedg/specs/locus/locus-v2-presentation.md`, «Render continuity
  contract».
- **Texto candidato:**

  > Each certified component is tessellated up to its certified boundary. No render
  > segment crosses an unresolved band, a certified nonexistence interval or a published
  > structural discontinuity. A component is closed only when it is one certified complete
  > periodic component. Adaptive tessellation applies a minimum depth and a maximum
  > turning angle between consecutive render segments, except at certified cusps and
  > folds, in addition to the chord criterion; the values are versioned render-policy
  > constants justified by reproducible benchmark evidence.

- **Decisión E-1:** si la evidencia `EXPLORATORY` se dibuja (con marca visual) o no.
- **Evidencia exigida:** `pins.B06` invertida con valores elegidos con el banco (los
  candidatos 10° y profundidad 4 no son constantes aprobadas).

## Para preparar `CU3`

### A3.1 — Normalización de residuos (ampliación)

- **Destino:** `geocedg/specs/locus/locus-v2-intersections.md` §8.1, §8.2 y §8.4.
- **Texto candidato:**

  > The residual of every target is divided by a translation-invariant characteristic
  > scale: the larger of the extent of the source component image and the extent of a
  > bounded target, or the source extent for an unbounded target, with an absolute floor
  > for degenerate extents. The combination rule is `max(absolute floor, relative
  > tolerance × scale)`. Root refinement stops on the residual criterion, not on an
  > absolute parameter bracket. Pair residuals use the same rule and never the distance
  > from the coordinate origin. A candidate rejected by the residual or membership test
  > emits its typed diagnostic.

- **Evidencia exigida:** `pins.H02` invertida (dos raíces admisibles en todo el
  barrido), sin pérdida de las raíces de la línea base.

### A3.2 — Contactos tangentes (ampliación; requiere enmienda del ADR 0017)

- **Destino:** `locus-v2-intersections.md` §5.1, §6.2 y §6.3; `locus-v2-public-surface.md`
  §10.
- **Texto candidato:**

  > A tangent contact is classified `TANGENT_STRUCTURAL` (both incidences established by
  > construction provenance), `TANGENT_CERTIFIED_ISOLATED` (a certified unique simple
  > root of the residual derivative in a bracket, a certified sign of the second
  > derivative and a residual within the declared contact tolerance) or
  > `TANGENT_NUMERICAL`. Its selector is the component lineage plus a tangent germ (side
  > of contact with canonical target orientation) continued through the critical point.
  > When the residual at the continued critical point leaves the tolerance on the
  > no-root side the solution becomes `CONTACT_LOST`; on the two-root side it becomes
  > `CONTACT_SPLIT` and the transverse roots receive new identities. A tangent token is
  > never transferred to another root.

- **Decisión F-2:** qué clases son materializables.

### A3.3 — Puntos por correspondencia del conductor (ampliación)

- **Destino:** `locus-v2-public-surface.md` (superficie pública) y
  `locus-v2-point-interaction.md` (familia de punto).
- **Texto candidato:**

  > `Point(L, D)` is the point of `L` generated at the driver position `D`, where the
  > membership of `D` in the driver support is established by provenance. `Intersect(L,
  > r, D)` is that point when its incidence with `r` is verified under the residual policy
  > of §8; otherwise it is undefined with a typed diagnostic. Neither form searches for
  > roots or uses coordinates, proximity or order as identity.

- **Decisión F-1:** nombres y formas definitivas.

### A3.4 — Ejes como objetivos (conformidad de mensajes o ampliación)

- **Destino:** `locus-v2-intersections.md` §2.1 y la tabla de objetivos admitidos.
- **Texto candidato:** «`GeoAxis` is an unbounded line target» o, si el autor lo prefiere,
  un mensaje explícito que sustituya al error genérico de CAS (H06).

## Fuera de estas enmiendas

Las enmiendas de `CU4`–`CU7` (matriz `P4`, valores por defecto, memoria de autoría,
indicación de orientación, redefinición, selección en diálogos) se prepararán en sus
paquetes y no condicionan `CU1`–`CU3`.
