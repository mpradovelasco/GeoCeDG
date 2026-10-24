# PRE-G9B-CU3 — Propuesta normativa candidata

| Campo | Valor |
|---|---|
| Carácter | Texto normativo **candidato** de `CU3` para su revisión autoral. No modifica ninguna especificación ni ningún ADR vigentes; su incorporación exige la aprobación autoral de la arquitectura y un paso T0 de la implementación |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Encargo | Instrucción autoral «PRE-G9B-CU3 — Autorización de preparación arquitectónica, científica y normativa» (2026-10-24), §5 punto 4; precisión autoral «Decisión sobre la planificación de CU3» (2026-10-24) |
| Sustituye | las enmiendas candidatas A3.1–A3.4 de `CU0` ([enmiendas candidatas](pre_g9b_cu0_candidate_normative_amendments.md)), que se conservan como antecedente |
| ADR | [ADR 0039 (PROPOSED)](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md) |
| Diseños | [análisis](pre_g9b_cu3_initial_state_and_capability_analysis.md); [Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md); [Etapa B](pre_g9b_cu3_stage_b_tangent_contact_certification_design.md); [normalización y ejes](pre_g9b_cu3_residual_normalization_and_axes_design.md) |
| Validación | [matriz de validación](../validation/pre_g9b_cu3_validation_matrix.md) |

Los textos van en inglés, como las especificaciones a las que se destinan. Cada enmienda indica
su destino, el texto candidato, las decisiones de las que depende y la evidencia que exige. Donde
una decisión tiene alternativas, el texto corresponde a la **recomendada** y se señala qué cambia
con las otras.

## 1. A3.0 — Principle of evidence for incidences and contacts

- **Destino:** `locus-v2-intersections.md`, nuevo §5.4; referencia en `locus-v2-public-surface.md`
  §10.
- **Decisiones:** ninguna. Es la formulación de `OBS-CU0-CU3-TANGENT-EXISTENCE` como norma.
- **Texto candidato:**

  > **Open and closed conditions.** A strict inequality (a sign, a non-vanishing derivative, a
  > non-parallelism, a transversality) may be certified by an outward-rounded interval enclosure
  > that excludes zero. An equality (an incidence `P ∈ r`, a value `h(t*) = 0`, a multiple root,
  > the equality of two points) is never certified by floating-point or interval evaluation: an
  > enclosure that contains zero is compatible with the equality, with nearby roots and with no
  > root. An equality is established only by construction provenance, by an exact structural
  > relation (term identity, exact symmetry) or by exact arithmetic with a declared scope. A
  > residual below a tolerance, a certified critical point with a small residual, a
  > finite-difference contact indicator or a higher-precision floating evaluation is numerical
  > evidence, never an incidence.

## 2. A3.1-R — Acceptance, residual normalization and deduplication

- **Destino:** `locus-v2-intersections.md` §8.1, §8.4 y §9, y un nuevo §8.5.
- **Decisiones:** D-CU3-6 (escala S1 e inclusión de las capacidades sin envolventes) y D-CU3-7
  (compatibilidad).
- **Texto candidato:**

  > **§8.5 Certified acceptance (policy `cu3-certified-existence-normalized/v1`).** A transverse
  > root is accepted only when, on a bracket where the source program is certified valid, the
  > enclosures of the residual at the bracket ends exclude zero with opposite signs (existence)
  > and the enclosure of its derivative on the bracket excludes zero (uniqueness and simplicity).
  > A candidate whose bracket does not certify both conditions is classified by the contact
  > evidence of §5.4 and §5.5 and is not admissible as a transverse root.
  >
  > The normalized residual is a consistency check, not acceptance evidence. Its characteristic
  > scale `L` is the larger of the extent of the image of the source component and the extent of
  > a bounded target, or the source extent for an unbounded target; the check is
  > `|residual| ≤ max(τ_abs, τ_rel · L)`, where `τ_abs` applies only to a degenerate extent. Pair
  > residuals use the larger extent of the two components and never the distance from the
  > coordinate origin. A candidate rejected by this check or by limited-target membership emits its
  > typed diagnostic (`RESIDUAL_REJECTED`, `PAIR_RESIDUAL_REJECTED`, `MEMBERSHIP_REJECTED`).
  >
  > Root refinement stops on the residual criterion or on a bracket of a few `ulp` of the
  > semantic parameter, never on an absolute parameter width. Two candidates are one semantic
  > preimage if and only if their certified brackets overlap and the derivative enclosure excludes
  > zero on their union; distinct preimages are never merged because their coordinates are close.
  >
  > Sources for which no residual enclosure is available (outside class v3 and outside SplineV2)
  > publish their roots with numerical evidence; such roots are not admissible for
  > materialization.
  >
  > The policy identity participates in result identity (§8). The residual-adapter contract text,
  > which identifies the residual quantity inside a root token, is unchanged.

- **Cambio en §8.4:** la tabla añade la fila «relative residual multiplies the characteristic
  scale of §8.5» y suprime el uso del corchete `1e-12` como criterio de parada.
- **Alternativa (ii) de D-CU3-6:** el último párrafo antes del de la identidad pasaría a decir que
  esas raíces conservan la aceptación numérica con la escala `L` y se marcan como verificación
  numérica bajo hipótesis.
- **Evidencia exigida:**
  - inversión de `pins.H02` en todo el barrido de escala y traslación;
  - V-H2-1 a V-H2-6 y V-B-NOISE-1 de la matriz;
  - la regresión de los tokens persistidos (V-COMPAT-1).

## 3. A3.2-R — Contact evidence classes, tangent germ and lifecycle

- **Destino:** `locus-v2-intersections.md` §5.1 (ampliación), nuevo §5.5, §6.2 y §6.3;
  `locus-v2-public-surface.md` §10 (fila «tangent root»). Requiere enmendar el ADR 0017 (ADR 0039).
- **Decisiones:** D-CU3-5 (F-2-R, opciones A, B o C) y D-CU3-11 (germen, tokens y alcance de la
  Etapa B).
- **Texto candidato:**

  > **§5.5 Contact evidence.** The existence of a common point of a source and a target and its
  > contact order are separate facts. A contact candidate has exactly one evidence class:
  >
  > 1. `CONTACT_STRUCTURAL`: both incidences are established by construction provenance
  >    (§5.4).
  > 2. `TANGENCY_EXISTENCE_CERTIFIED`: `h(t*) = 0` is established by exact evaluation, or by an
  >    exact symmetry certificate together with exact evaluation, in the declared scope of the
  >    captured configuration.
  > 3. `CONTACT_NUMERICALLY_COMPATIBLE`: a certified critical point `t*` of the residual (unique
  >    simple root of its derivative, certified sign of the second derivative) whose residual
  >    enclosure contains zero.
  > 4. `CRITICAL_POINT_CERTIFIED_WITHOUT_INTERSECTION` or `CRITICAL_POINT_CERTIFIED_TWO_ROOTS`:
  >    the residual enclosure at the certified critical point excludes zero.
  > 5. `CONTACT_NOT_ESTABLISHED`.
  >
  > Only classes 1 and 2 may be materialized as intersections. Class 3 is never presented as an
  > intersection and no tolerance promotes it. Class 4 certifies the absence of roots in its
  > bracket, or two simple transverse roots which receive new identities through §8.5. The contact
  > order is a geometric attribute of a regular source parameter only. It is one when existence is
  > proved and the residual-derivative enclosure excludes zero. It is two when existence is of
  > class 1 or 2, the enclosure of the source derivative excludes the zero vector, the residual
  > derivative vanishes by structure, exact symmetry or exact evaluation, and the second-derivative
  > enclosure excludes zero. At a certified fold of the source (exact symmetry
  > `F(t₀ + s) = F(t₀ − s)`), a vanishing residual derivative is due to the fold. The contact is
  > then `FOLD_CONTACT`: the point set of the source ends at `F(t₀)`, and the two parameter roots
  > of a perturbed target are two preimages of one geometric point, never two points. Otherwise
  > the order is not established. The finite-difference indicator of §8.3 remains diagnostic.
  >
  > **§6.2 addition.** The selector of a materialized tangent contact is the stable component
  > lineage plus a typed tangent-contact germ `…/tangent-contact-germ/v1`: target family and
  > canonical orientation, side of contact (sign of the second derivative with respect to the
  > canonical target orientation), established order and evidence class. It contains no
  > coordinate, parameter or index. Transverse germs and their versions are unchanged.
  >
  > **§6.3 addition.** At each revision the critical point is continued as a simple root of the
  > residual derivative, as revision evidence only. Classes 1 and 2 from the same germ keep the
  > token (`CURRENT`). Class 4 without intersection gives `CONTACT_LOST`. Class 4 with two roots
  > gives `CONTACT_SPLIT`, and the two transverse roots receive new identities. Class 3 gives
  > `CONTACT_NOT_CERTIFIED`. A lost second-derivative certificate or isolation gives
  > `IDENTITY_DISCONTINUITY`. Each of these leaves the point undefined. A tangent token is never
  > transferred to another root.

- **Opción B de D-CU3-5** (recomendada), texto adicional para `locus-v2-public-surface.md`:

  > A certified critical point of a source with respect to a target (classes 3 and 4) may be
  > materialized only through its own command family as a point of the source whose existence,
  > local uniqueness and continuity are proved. It never asserts incidence with the target and
  > carries the residual enclosure at the critical point as evidence. A later promotion of the
  > evidence to class 1 or 2 creates no silent conversion into an intersection.

- **Opción A:** se suprime ese párrafo. **Opción C:** rechazada; no hay texto.
- **Evidencia exigida:** V-B-1 a V-B-12 de la matriz y la inversión de `pins.B08_B09` con la
  reformulación de la raíz de la costura (D-CU3-10).

## 4. A3.3-R — Structural points by driver correspondence

- **Destino:** `locus-v2-public-surface.md`, nuevo §20; `locus-v2-point-interaction.md`, nuevo
  §18 (familia de punto); `locus-v2-existence-components.md` §15, nota de consumidor.
- **Decisiones:** D-CU3-1 (formas), D-CU3-2 (clases de procedencia), D-CU3-3 (vías de la
  correspondencia), D-CU3-4 (autoridad de la segunda incidencia) y D-CU3-12 (sintaxis y lectores
  anteriores).
- **Texto candidato:**

  > **§20 Structural points by driver correspondence.** `Point(L, D)` is the point of the Locus V2
  > `L` at the driver position that corresponds to the point `D`. `Intersect(L, r, D)` is the same
  > point as an intersection of `L` with `r`. The third argument is never an initial point of a
  > numerical iteration. The point is defined only when four facts hold, checked in this order:
  >
  > 1. **Provenance.** `D` lies on its origin objects by the semantics of its parent algorithm,
  >    under a closed versioned table of provenance classes: point on a path; output of an
  >    admitted intersection algorithm that places its outputs on an input; defining point of a
  >    segment or arc.
  > 2. **Driver support.** One origin object of `D` that places it on a support is the driver
  >    support of `L` itself: the same live object with the same persistent identity. A
  >    geometrically coincident or copied object does not qualify.
  > 3. **Correspondence.** The provider family of the driver defines the existence, uniqueness
  >    (modulo the period for a periodic domain) and orientation of `D ↦ t(D)`. The value is
  >    computed by the provider's canonical projection. Outside a bounded domain, beyond the
  >    certified error enclosure of that projection, the point is undefined. A defining point
  >    receives its exact end parameter. The live driver is never moved.
  > 4. **Evaluation and incidence.** `t(D)` lies in a certified continuous or point component of
  >    `L`. For `Intersect`, the incidence with `r`, including membership in a limited target, is
  >    established with class 1 or 2 evidence (§5.4 and §5.5 of the intersection contract), by term
  >    identity under driver substitution or by exact evaluation of the slice at the exact point
  >    `D`. A residual is never sufficient.
  >
  > The first failing fact gives the typed cause and leaves the point undefined. No parameter is
  > persisted: the point is reconstructed from the dependency graph at every revision, after
  > Undo, Redo, reopen and copy. Its identity is its own durable identity with the durable
  > identities of `L`, `D` and `r` as inputs. It never includes a root token, parameter,
  > coordinate, output index or proximity. When the condition holds again the point is defined
  > again with the same identity.

- **Opción B de D-CU3-1:** sustituye la segunda forma por un comando propio.
  **Opción C:** suprime la segunda forma.
- **Evidencia exigida:** V-A-1 a V-A-16 de la matriz (los cuatro hechos, cada uno con su fallo
  cerrado) y los tres casos obligatorios con la dinámica.

## 5. A3.4-R — Axes as reserved line targets

- **Destino:** `locus-v2-intersections.md` §2.1 y la tabla de objetivos admitidos;
  `geocedg/specs/spatial/g9-spatial-projection-semantics.md`, sección de participaciones de
  construcción; enmienda acotada del ADR 0010 (identidad durable), recogida en el ADR 0039.
- **Decisiones:** D-CU3-8 (opciones A, B o C).
- **Texto candidato:**

  > `xAxis` and `yAxis` are reserved kernel references with stable symbolic identities. They may
  > participate in a durable construction relation as non-ordinary participants whose identity is
  > the reserved reference, not a label. A Locus V2 intersection admits them as unbounded line
  > targets. A target that remains unsupported is rejected with a typed message, never with a
  > generic computer-algebra error.

- **Opción B:** solo la última frase.
- **Evidencia exigida:** inversión de `pins.H06`; V-H3-1 a V-H3-4.

## 6. A3.5 — Residual jets and exact evaluation of class-v3 programs

- **Destino:** `locus-v2-certified-construction-model.md`, nuevo §11 (versión 1.3).
- **Decisiones:** D-CU3-4 (M3) y D-CU3-11.
- **Texto candidato:**

  > **§11.1 Residual jets.** The second-order enclosure of the traced point (§10.7) may be
  > composed with the residual of a supported target into enclosures of the residual and of its
  > first two derivatives. This use extends §10.7 to the isolation of critical points of contact
  > residuals. The result certifies only open conditions.
  >
  > **§11.2 Exact evaluation.** A class-v3 program whose operations are ruler-and-compass
  > constructions over captured binary64 constants, taken as exact rationals, may be evaluated
  > exactly at an exact driver point in the tower of quadratic extensions that it generates. The
  > sign and the vanishing of a residual are decided by exact arithmetic with constructive
  > root-separation bounds. Its scope is the captured configuration: it says nothing of the
  > values typed by the user or of the rounded computation. A transcendental operation, an
  > unsupported operation or an exhausted versioned budget gives
  > `EXACT_EVALUATION_OUT_OF_CLASS` or `EXACT_EVALUATION_BUDGET_EXHAUSTED`, never a decision.

- **Evidencia exigida:**
  - V-M3-1 a V-M3-5 de la matriz: los tres casos obligatorios y la costura demostrados, una
    coincidencia accidental no demostrada y un fuera de clase;
  - un banco de coste con contadores deterministas (decisión C1-1 de `CU0`).

## 7. Lo que estas enmiendas no cambian

- Los contratos aprobados de `CU0`–`CU2`: certificados de existencia, componentes, longitudes,
  presentación y exportación.
- La identidad de los tokens transversales existentes y el texto de los contratos de los
  adaptadores de residuo.
- Los documentos existentes, salvo lo que decida D-CU3-7 sobre las raíces reclasificadas o
  espurias.
- Ninguna tolerancia pasa a ser configurable (H-1 sigue pospuesta).
