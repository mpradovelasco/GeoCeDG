# PRE-G9B-CU3 — Propuesta normativa candidata

| Campo | Valor |
|---|---|
| Carácter | Texto normativo **candidato** de `CU3` para la revisión autoral. No modifica ninguna especificación ni ningún ADR vigentes; su incorporación exige la aprobación autoral de la arquitectura y un paso T0 de la implementación |
| Estado | Texto candidato revisado por la revisión científica focal y por la comprobación final de coherencia (2026-10-24: A3.1-R, A3.3-R y A3.5). **Aprobado por el autor el 2026-10-24** en la dirección científica documentada e incorporado en el paso T0, con las precisiones de la decisión: la autoridad del objetivo (`locus-v2-certified-construction-model.md` §11.3) y las condiciones de conformidad de M3 (§11.4). Desde la incorporación, las especificaciones son la autoridad y este documento es antecedente ([registro de aprobación](../validation/pre_g9b_cu3_author_design_approval_record.md)); `selfApproved=false` |
| Encargo | Instrucción autoral «PRE-G9B-CU3 — Autorización de preparación arquitectónica, científica y normativa» (2026-10-24), §5 punto 4; precisión autoral «Decisión sobre la planificación de CU3» (2026-10-24); instrucción «PRE-G9B-CU3 — Revisión científica focal del candidato de preparación» (2026-10-24) |
| Sustituye | las enmiendas candidatas A3.1–A3.4 de `CU0` ([enmiendas candidatas](pre_g9b_cu0_candidate_normative_amendments.md)), que se conservan como antecedente |
| ADR | [ADR 0039 (ACCEPTED, 2026-10-24)](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md) |
| Diseños | [análisis](pre_g9b_cu3_initial_state_and_capability_analysis.md); [Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md); [Etapa B](pre_g9b_cu3_stage_b_tangent_contact_certification_design.md); [normalización, admisibilidad y ejes](pre_g9b_cu3_residual_normalization_and_axes_design.md) |
| Validación | [matriz de validación](../validation/pre_g9b_cu3_validation_matrix.md) |

Los textos van en inglés, como las especificaciones a las que se destinan. Cada enmienda indica
su destino, el texto candidato, las decisiones de las que depende, si **amplía**, **precisa** o
**restringe** un contrato aprobado, y la evidencia que exige. Donde una decisión tiene
alternativas, el texto corresponde a la recomendada.

## 1. A3.0 — What interval evidence certifies, and what it does not

- **Destino:** `locus-v2-intersections.md`, nuevo §5.4; referencia en `locus-v2-public-surface.md`
  §10.
- **Naturaleza:** precisa el contrato (formula `OBS-CU0-CU3-TANGENT-EXISTENCE` como norma); no
  restringe nada aprobado.
- **Texto candidato:**

  > **§5.4 Kinds of evidence.** Three kinds of claim are distinguished.
  >
  > (a) *Existence of some root in an interval.* If the source program is certified valid on a
  > bracket, so that the residual is continuous there, and outward-rounded enclosures of the
  > residual at the bracket ends exclude zero with opposite signs, then a root exists in the
  > bracket. Interval Newton, Krawczyk and Miranda tests give the same kind of certificate. A
  > derivative enclosure that excludes zero on the bracket adds uniqueness and simplicity. This is
  > rigorous interval evidence for a transverse root, and for the critical point of a residual as
  > a root of its derivative.
  >
  > (b) *Equality at a prescribed parameter or point.* `h(t₀) = 0` at a given `t₀`, the incidence
  > `P ∈ r` of a given point, the equality of two given points, and the vanishing of a derivative at
  > a given parameter are closed conditions. An enclosure that contains zero is compatible with the
  > equality and with its negation, so interval or floating-point evaluation never decides them. The
  > existence of a root of even multiplicity has no sign change and requires such an equality at
  > the critical point.
  >
  > (c) *Structural and exact equality.* An equality of kind (b) is established either
  > structurally (construction provenance, term identity under driver substitution, a certified
  > exact symmetry) or by exact arithmetic in a declared configuration with a declared capture
  > boundary. Both are rigorous and are recorded as distinct evidence. A residual below a
  > tolerance, a certified critical point with a small residual, a finite-difference contact
  > indicator, or a higher-precision floating evaluation is numerical evidence of kind (b), never
  > its proof.

## 2. A3.1-R — Admissibility: local verification, certified existence and conflicts

- **Destino:** `locus-v2-intersections.md` §8.1, §8.4 y un nuevo §8.5; ADR 0008, nota sobre la
  aclaración G8B-R1.
- **Decisiones:** D-CU3-6, D-CU3-7 y D-CU3-13.
- **Naturaleza:** **conserva** la admisibilidad G8B-R1; la **amplía** con una vía certificada (H02);
  la **restringe** solo mediante la regla de conflicto, con las consecuencias de compatibilidad del
  [diseño](pre_g9b_cu3_residual_normalization_and_axes_design.md) §5.
- **Texto candidato:**

  > **§8.5 Evidence grades and admissibility (policy `cu3-certified-admission/v1`).** Every finite
  > solution carries an existence grade, orthogonal to its contact class and to global
  > completeness. The grades are:
  >
  > - `LOCALLY_VERIFIED`: the G8B-R1 predicate, with residual and isolation evidence evaluated in
  >   binary64;
  > - `EXISTENCE_CERTIFIED`: §5.4 (a);
  > - `EXISTENCE_STRUCTURAL` and `EXISTENCE_EXACT`: §5.4 (c).
  >
  > `LOCALLY_VERIFIED` is numerical verification under hypotheses, not a certificate of existence.
  >
  > The G8B-R1 point-admissibility predicate and its versioned thresholds are unchanged. In
  > addition, a transverse root graded `EXISTENCE_CERTIFIED` satisfies the residual condition of
  > that predicate when its normalized residual is within `τ_rel · L`. `L` is the larger of the
  > extent of the image of the source component and the extent of a bounded target, or the source
  > extent for an unbounded target, with an absolute floor only for a degenerate extent. This scale
  > never decides existence. Refinement on this path stops on the residual criterion or on a
  > bracket of a few `ulp` of the semantic parameter.
  >
  > **Conflict rule.** For one-parameter targets and sources with residual enclosures, a locally
  > verified solution is withdrawn or reclassified only when a root-count certificate, computed in
  > the same revision and in the decision configuration `Σ_L⁺` of §11.2 of the certified
  > construction model, which contains the captured configuration of `L` unchanged, really
  > contradicts its existence, its multiplicity or its classification.
  >
  > A root-count certificate on a parameter box `T` requires continuity: `T` lies in one certified
  > continuous component and the class-v3 box evaluation is valid on `T`. A component end, a seam
  > that ends a component, a transition, an unresolved band, an invalid gap or a point component
  > inside `T` voids it, except an included domain end whose value is decided exactly. The
  > certificate establishes the exact number of roots in `T`, with multiplicity, and their location,
  > in one of four forms:
  >
  > - none (the residual has a certified fixed sign on `T`);
  > - one simple root (derivative enclosure without zero and opposite certified end signs);
  > - two simple roots located on either side of a certified critical point (fixed second-derivative
  >   sign, residual at the critical point and at both ends of certified signs);
  > - one double root at an exact point (class 1 or 2, vanishing derivative by structure, exact
  >   symmetry or exact arithmetic, fixed second-derivative sign).
  >
  > A solution is contradicted when:
  >
  > - the certificate gives no root, or the solution's isolating interval is disjoint from every
  >   root it locates (withdrawn);
  > - more solutions lie in one located sub-bracket than the certificate allows (merged into one
  >   preimage, keeping an established identity if exactly one had it);
  > - a simple or transverse solution lies where the certificate gives a double root (reclassified);
  > - a tangent or undetermined solution lies where the certificate gives a simple root
  >   (reclassified as transverse with certified existence).
  >
  > An isolated certified critical point, a class-3 candidate, a certificate that fails these
  > conditions, or a contradiction of set completeness alone is never a contradiction of a
  > solution: the last one lowers the result's completeness. The analysis is triggered
  > deterministically and independently of scale, only for candidates that the solver marks as
  > non-transverse, as local minima, or as closer than their own bracket width. Without
  > contradiction, admissibility is unchanged.
  >
  > Rejections emit their declared diagnostics (`RESIDUAL_REJECTED`, `PAIR_RESIDUAL_REJECTED`,
  > `MEMBERSHIP_REJECTED`, `NONFINITE_EVALUATION`). A grade never rises through a tolerance, a
  > coordinate coincidence, a label, persistence or another revision. A consumer that requires a
  > minimum grade fails explicitly when it is not met. The grade is revision evidence and is not
  > part of a root token. The residual-adapter contract text inside tokens is unchanged.

- **Sin cambios en `CU3`:** la normalización de los pares por la distancia al origen, una no
  conformidad con §8.1 que se registra como limitación (diseño §3.6).
- **Evidencia exigida:**
  - inversión de `pins.H02` por la vía certificada;
  - las suites G8B, G8C y G9U0-R4 intactas fuera de las filas de conflicto;
  - V-EVID-1 a V-EVID-6, V-CONF-1 a V-CONF-8, V-H2-1 a V-H2-5, V-B-NOISE-1, V-B-DUP-1 y
    V-COMPAT-1 a V-COMPAT-2.

## 3. A3.2-R — Contact evidence classes, contact order and lifecycle

- **Destino:** `locus-v2-intersections.md` §5.1 (ampliación), nuevo §5.5, §6.2 y §6.3;
  `locus-v2-public-surface.md` §10 (fila «tangent root»); precisión de la fila «established
  even/tangent root with local evidence» de G8B-R1. Requiere enmendar el ADR 0017 (ADR 0039).
- **Decisiones:** D-CU3-5 (opción A), D-CU3-11 y D-CU3-18.
- **Naturaleza:**
  - **precisa** G8B-R1 para las raíces pares de objetivos de un parámetro (sin impacto en el
    producto actual: ninguna recibe hoy aislamiento `ESTABLISHED`);
  - **amplía** la materialización a las clases 1 y 2.
- **Texto candidato:**

  > **§5.5 Contact evidence.** A contact candidate without a local sign change has exactly one
  > class:
  >
  > 1. `CONTACT_STRUCTURAL` (§5.4 (c), structural);
  > 2. `CONTACT_EXISTENCE_EXACT` (§5.4 (c), exact arithmetic at an exact driver point, with its
  >    configuration and capture boundary);
  > 3. `CONTACT_NUMERICALLY_COMPATIBLE` (a certified critical point whose residual enclosure
  >    contains zero);
  > 4. `CRITICAL_POINT_CERTIFIED_WITHOUT_INTERSECTION` or `CRITICAL_POINT_CERTIFIED_TWO_ROOTS`
  >    (the residual enclosure at the certified critical point excludes zero);
  > 5. `CONTACT_NOT_ESTABLISHED`.
  >
  > For a root of even multiplicity, "established" in the G8B-R1 admissibility predicate means
  > class 1 or 2. Only classes 1 and 2 are materialized as intersections. Classes 3 to 5 remain in
  > the rich result with their evidence and as diagnostics. An equality that is not structural is
  > established exactly when it holds exactly in the declared configuration.
  >
  > Contact order is a geometric attribute of a regular source parameter:
  >
  > - one, when existence is proved and the residual-derivative enclosure excludes zero;
  > - two, when existence is of class 1 or 2, the source-derivative enclosure excludes the zero
  >   vector, the residual derivative vanishes by structure, exact symmetry or exact arithmetic, and
  >   the second-derivative enclosure excludes zero;
  > - `FOLD_CONTACT`, at a certified fold of the source (exact symmetry `F(t₀ + s) = F(t₀ − s)`),
  >   where the point set of the source ends at the contact and the two parameter roots of a
  >   perturbed target are two preimages of one geometric point, carried as one identity;
  > - not established otherwise.
  >
  > The finite-difference indicator of §8.3 is diagnostic.
  >
  > **§6.2 addition.** The selector of a materialized contact is the stable component lineage plus
  > a typed tangent-contact germ `…/tangent-contact-germ/v1`, made of:
  >
  > - target family and canonical orientation;
  > - side of contact;
  > - established order;
  > - evidence class.
  >
  > It contains no coordinate, parameter, index or configuration. Transverse germs are unchanged.
  >
  > **§6.3 addition.** The critical point is continued as a simple root of the residual derivative,
  > as revision evidence only. The transitions are:
  >
  > - classes 1 and 2 from the same germ keep the token (`CURRENT`);
  > - class 4 without intersection gives `CONTACT_LOST`;
  > - class 4 with two roots at a regular source gives `CONTACT_SPLIT`, with new identities;
  > - class 3 gives `CONTACT_NOT_CERTIFIED`;
  > - a lost second-derivative certificate or isolation gives `IDENTITY_DISCONTINUITY`.
  >
  > A tangent token is never transferred.

- **Opción futura, no incorporada:** el objeto público de punto crítico (opción B de D-CU3-5).
- **Evidencia exigida:**
  - V-B-1 a V-B-12 de la matriz;
  - la inversión de `pins.B08_B09` con la reformulación de la raíz de la costura (D-CU3-10).

## 4. A3.3-R — Structural points by driver correspondence

- **Destino:** `locus-v2-public-surface.md`, nuevo §20; `locus-v2-point-interaction.md`, nuevo
  §18 (familia de punto); `locus-v2-existence-components.md` §15, nota de consumidor.
- **Decisiones:** D-CU3-1, D-CU3-2, D-CU3-3, D-CU3-4, D-CU3-9 y D-CU3-12.
- **Naturaleza:** **amplía** la superficie con formas nuevas; ningún comportamiento existente cambia.
- **Texto candidato:**

  > **§20 Structural points by driver correspondence.** `Point(L, D)` is the point of the Locus V2
  > `L` at the driver position that corresponds to the point `D`. `Intersect(L, r, D)` is the same
  > point as an intersection of `L` with `r`. The third argument is never an initial point of a
  > numerical iteration. The point is defined only when four facts hold, checked in this order.
  >
  > 1. **Provenance.** The exact point that the parent algorithm of `D` defines lies on its origin
  >    objects. The provenance classes form a closed versioned table: point on a path; defining
  >    point of a segment or arc; output of an admitted intersection algorithm that places its
  >    output on an input. The exact output is selected by the exact form of the inherited
  >    selection rule (`locus-v2-certified-construction-model.md` §10.2).
  > 2. **Driver support.** That input is the driver support of `L` itself: the same live object
  >    with the same persistent identity.
  > 3. **Correspondence.** The exact correspondence `t*(D)` is defined by the provider's
  >    parameterization in a declared configuration. It is known exactly for a path point (its
  >    path parameter) and for a defining point (0 or 1). For an intersection output it is
  >    enclosed by evaluating the provider inverse with outward intervals on a certified
  >    enclosure of the exact point. The binary64 coordinates of `D` alone never certify the
  >    correspondence. The numerical value `t̃(D)` serves only the displayed position. The live
  >    driver is never moved.
  > 4. **Evaluation and incidence.** The enclosure of `t*(D)` lies in the interior of a certified
  >    continuous component of `L`. The point fails closed with a typed cause when the enclosure:
  >    - meets a bounded-domain end that provenance does not place inside the limited support;
  >    - meets a seam that ends a component;
  >    - meets a component end, a certified transition, an unresolved band or an invalid gap;
  >    - meets a point component.
  >
  >    A point component creates no public point in this version (§15.4). The authoritative object
  >    is `L` in its captured configuration (§10 of the certified construction model), and
  >    `Intersect(L, r, D)` materializes the same exact point as `Point(L, D)`. For `Intersect`, the
  >    incidence of that point with `r` and its membership in a limited target are equalities at a
  >    prescribed point (§5.4 (b)). They are established only by term identity under driver
  >    substitution, or by exact evaluation in the decision configuration that contains the
  >    captured configuration of `L` unchanged (certified construction model §11.2). Exact
  >    evaluation either proves the equality, refutes it exactly, or reports that it is out of
  >    class or out of budget. A proof in any configuration that recomputes the captured constants
  >    of `L` is diagnostic and never establishes the incidence.
  >
  > No parameter or certificate is persisted: the point is reconstructed from the dependency graph
  > at every revision, after Undo, Redo, reopen and copy. Its identity is its own durable identity
  > with the durable identities of `L`, `D` and `r` as inputs. It never includes a root token,
  > parameter, coordinate, output index, configuration or proximity.

- **Evidencia exigida:** V-A-1 a V-A-22 de la matriz y los tres casos obligatorios con la
  dinámica.

## 5. A3.4-R — Axes as reserved line targets

- **Destino:** `locus-v2-intersections.md` §2.1 y la tabla de objetivos admitidos;
  `geocedg/specs/spatial/g9-spatial-projection-semantics.md`, sección de participaciones de
  construcción; enmienda acotada del ADR 0010, recogida en el ADR 0039.
- **Decisión:** D-CU3-8.
- **Naturaleza:** **amplía**; ningún documento existente cambia.
- **Texto candidato:**

  > `xAxis` and `yAxis` are reserved kernel references with stable symbolic identities. They may
  > participate in a durable construction relation as non-ordinary participants whose identity is
  > the reserved reference, not a label. A Locus V2 intersection admits them as unbounded line
  > targets. A target that remains unsupported is rejected with a typed message, never with a
  > generic computer-algebra error.

- **Evidencia exigida:** inversión de `pins.H06`; V-H3-1 a V-H3-4.

## 6. A3.5 — Residual jets and exact evaluation in the authoritative configuration

- **Destino:** `locus-v2-certified-construction-model.md`, nuevo §11 (versión 1.3).
- **Decisiones:** D-CU3-4 (M3 y su puerta) y D-CU3-11.
- **Naturaleza:** **amplía** con capacidades nuevas del núcleo. No cambia la clase v3 ni los
  certificados de `CU2`.
- **Texto candidato:**

  > **§11.1 Residual jets.** The second-order enclosure of the traced point (§10.7) may be composed
  > with the residual of a supported target into enclosures of the residual and of its first two
  > derivatives. This extends §10.7 to the isolation of critical points of contact residuals, and
  > certifies only claims of kind (a) of `locus-v2-intersections.md` §5.4.
  >
  > **§11.2 Exact evaluation in the authoritative configuration.** The decision configuration
  > `Σ_L⁺` of a structural point contains the captured configuration of `L` (§3 and §10) without
  > any change: the same captured constants, with the same exact meaning that the class-v3
  > program gives them, and the same program. It adds the driver point `D`, the target `r` and
  > their ancestors. Every object of the joint subgraph receives exactly one exact meaning:
  >
  > - a captured constant of `L` keeps its captured value and representation (a constant circle is
  >   its matrix circle), also when it is the target;
  > - the driver support keeps the meaning of the captured driver constants, and the algorithm of
  >   `D` is evaluated on that meaning;
  > - an object outside the captured configuration that is an ancestor of a captured constant is
  >   taken at its binary64 state;
  > - the independent elements, the path parameters and the objects whose exact computation leaves
  >   the class are taken at their binary64 states;
  > - every other object is recomputed exactly from its parents' meanings;
  > - term-identical objects share one meaning;
  > - a constant with two different exact representations in the program of `L` fails closed
  >   (`CONFIGURATION_REPRESENTATION_CONFLICT`).
  >
  > The exact point `D*` is reconstructed by the exact form of the inherited selection rule
  > (§10.2) with the captured selection state. Coincident roots, continuous-mode intersections,
  > reference-point selections and an uncertified initialization fail closed. The slice is
  > evaluated with the driver replaced by the exact point `D*`, never by a parameter.
  > Ruler-and-compass operations generate a finite tower of quadratic extensions. The signs and
  > the vanishing of residuals, validity predicates, limited-target parameters and, when needed,
  > first derivatives (exact jets) are decided with exact arithmetic and constructive
  > root-separation bounds. The result is "equal", "different", or
  > `EXACT_EVALUATION_OUT_OF_CLASS` / `EXACT_EVALUATION_BUDGET_EXHAUSTED`, never "equal within a
  > tolerance".
  >
  > Because the captured configuration of `L` is contained unchanged, a decision is a property of
  > the authoritative point of `L` and needs no transfer. A deeper joint recomputation that also
  > recomputes the captured constants of `L` is a different configuration. It may be computed
  > only as a diagnostic (`INCIDENCE_HOLDS_ONLY_IN_JOINT_RECOMPUTATION`). Its result applies to the
  > authoritative point only if both configurations are proved to give the same exact value to
  > every object the proof uses. A refutation in `Σ_L⁺` is reported as such
  > (`INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION`), never as nonexistence in the intended
  > configuration. No captured constant or certificate of `L` is changed or reinterpreted. The
  > binary64 coordinates of `D` enter only the check that the reconstructed branch corresponds to
  > the live algorithm.

- **Puerta de la implementación (estrategia, paso T2):** G-M3-1 (viabilidad matemática en
  `Σ_L⁺` en los cinco contactos obligatorios y en los controles), G-M3-2 (presupuesto por
  contadores) y G-M3-3 (correspondencia con el algoritmo vivo). Sin la puerta superada, no se
  implementa la segunda incidencia, y su evidencia nunca se sustituye por un residuo ni por una
  demostración en `Σ_B`.
- **Evidencia exigida:** V-M3-1 a V-M3-8, V-CFG-1 a V-CFG-6 y las filas de la puerta.

## 7. Lo que estas enmiendas no cambian

- Los contratos aprobados de `CU0`–`CU2`: certificados de existencia, componentes (incluida la
  regla de §15.4 sobre componentes puntuales), longitudes, presentación y exportación.
- La admisibilidad G8B-R1 de las raíces transversales y sus umbrales, salvo la regla de conflicto
  identificada.
- La aceptación de los pares de loci.
- La identidad de los tokens transversales existentes y el texto de los contratos de los
  adaptadores de residuo.
- Los documentos existentes, salvo lo que decida D-CU3-7 sobre las soluciones contradichas por
  evidencia certificada.
- Ninguna tolerancia pasa a ser configurable (H-1 sigue pospuesta).
