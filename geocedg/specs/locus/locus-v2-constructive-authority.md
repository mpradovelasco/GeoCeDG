# Locus V2 constructive geometric authority and relation integrity

| Field | Value |
|---|---|
| Status | **NORMATIVE — AUTHOR APPROVED AS CONTRACT (2026-10-25); IMPLEMENTATION AUTHORIZED IN INCREMENTS E1 TO E4 WITH BLOCKING GATES, NOT YET APPROVED AS PRODUCT** |
| Version | `1.0` |
| Phase | `PRE-G9B-CU3` |
| Scope | Shared-kernel geometric authority of documents governed by `constructive-authority/v1`: single constructive meaning, class table and boundary, document policy, relation integrity, relaxed admission and constructive identity certificates |
| Product state | Experimental, internal; documents that have not adopted the policy are unchanged |
| Parents | [Locus V2 semantics](locus-v2-semantics.md), [intersections](locus-v2-intersections.md), [certified construction model](locus-v2-certified-construction-model.md), [existence and components](locus-v2-existence-components.md), [public surface](locus-v2-public-surface.md) |
| Decision | [ADR 0040](../../../docs/adr/0040-constructive-geometric-authority-and-relation-integrity.md), accepted as architecture on 2026-10-25 |
| Approval | [approval record](../../../docs/validation/pre_g9b_cu3_constructive_authority_approval_record.md) of 2026-10-25 |

Normative texts AUT-1, AUT-2, AUT-3, AUT-5 and AUT-6 of the
[constructive authority normative proposal](../../../docs/architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md),
approved by the author on 2026-10-25 with decisions D-AUT-1 to D-AUT-12 and the conciliation of
its section 14. Sections 3.2, 3.3, 7.1 to 7.3 and 8.1 to 8.2 state the operational rules that the
approved decisions D-AUT-3, D-AUT-5 and D-AUT-8 adopt from sections 2.6b, 4.2b, 4.3, 4.5 and 5.4b
of that proposal. Texts AUT-4 and AUT-7 are amendments of the
[certified construction model](locus-v2-certified-construction-model.md) and of the
[intersection contract](locus-v2-intersections.md) (version 1.4).

This contract applies only to documents governed by `constructive-authority/v1`. In every other
document the approved contracts apply unchanged. The implementation status is recorded only in
the `CU3` records.

## 1. Single geometric authority

Under the document policy `constructive-authority/v1`, every object has exactly one geometric
meaning: the ideal object that its construction defines from the free inputs. The binary64 value
of a free input is its definition. The meaning is a pure function of the construction graph, the
free inputs, the class table `constructive-class/v1` and the semantics of points on paths. It does
not depend on the command that consumes the object, on a driver, on a captured configuration or
on the boundary of a reconstruction. Every numerical state of an object — its binary64 state, a
captured configuration, an enclosure, a boundary substitute — is a representation with a declared
numeric guarantee and never decides a relation of the object.

The rule is uniform per document and per version, never per relation. The meaning of a target is
never redefined to make an incidence hold.

## 2. Inherited algorithms

Inherited algorithms are unchanged computation engines. Their outputs represent the ideal
outputs. A relation that an algorithm establishes by provenance holds strictly for the ideal
objects whenever they exist, whatever the error of the representations. The existence of an
ideal output — the validity of its construction, its membership of a limited part and its branch
— is a separate fact, decided rigorously by margin, by structure or by a constructive identity
certificate, and `NOT_ESTABLISHED` otherwise; an inherited tolerance never decides it. Inherited
commands that decide relations (such as `Relation`, `AreEqual` or the generic prover) keep their
behaviour; their verdicts are results of the representation or generic truths over the complex
numbers, and they are never integrity evidence.

No line of an inherited algorithm changes its behaviour. Only what the GeoCeDG layers state about
its outputs — certificates, integrity and admissions — changes.

## 3. Class table and boundary

Membership of an object in the exact class is decided by the class of its algorithm only. An
object outside the class enters an exact evaluation through its binary64 state as a declared
boundary substitute, which is a premise of every relation whose evidence uses it. The ideal point
of a point on a path is the point of the ideal path assigned to its persisted coordinates by the
inherited path map evaluated exactly on the ideal path.

### 3.1 Inherited numerical decisions

A tolerance-based decision inside an algorithm of the class (equal points, parallel lines,
tangency, incident special cases, limited-path membership) is replaced, for integrity, by a
rigorous predicate, and its branch is checked against the live algorithm; it never decides
integrity itself. Commands that return booleans or numbers from geometric tests, conditional
definitions, conditioned selections and numerical solvers are outside the class: their outputs
keep their behaviour and are declared boundary objects. No relation acquires `STRICT` through a
decision that is not rigorously established: an identity may be `STRICT` across a cut only when
the certified box contains every value the cut object can take ideally, including every outcome of
an undecided decision, and a condition that is a polynomial predicate of objects of the class
enters the cell as a certified sign condition. Booleans and numbers returned by geometric tests
carry no integrity.

### 3.2 `constructive-class/v1`

The class enumerates the algorithms with exact semantics:

- lines, segments and rays through two points;
- parallel and perpendicular lines;
- line–line and line–circle intersections, with the inherited selection;
- midpoints;
- circles by centre and point and by centre and radius;
- points on paths;
- the class-v3 programs of loci (`locus-v2-certified-construction-model.md` §10).

Membership is decided by the algorithm class, never by a value. A circle belongs to the class by
the algorithm that defines it, never by its current value.

Inherited commands are classified as follows:

| Category | Examples | Treatment |
|---|---|---|
| D1. Algorithm of the class with tolerance-based internal decisions | `AlgoJoinPoints` (equal points), `AlgoIntersectLines` (parallel lines), `AlgoIntersectLineConic` (tangency decided with `isEqual(dis, 0, delta)`, special case of a point already incident, membership of a segment or ray with a tolerance), degenerate circles, clipping of a point on a limited path | stays in the class. For integrity, each internal decision is replaced by a rigorous predicate: a certified sign or an exact decision. The branch is checked against the live output outside the slice of the driver (§8). A disagreement gives `NOT_ESTABLISHED`, with the cause of existence or of correspondence; the tolerance is never accepted |
| D2. Command that returns a boolean or a number from a geometric test | `Relation`, `AreEqual`, `AreParallel`, `ArePerpendicular`, `AreCollinear`, `AreConcyclic`, `IsInRegion`, comparisons `a < b` or `a == b` | outside the class. Its result is a verdict of the representation, without integrity (§2) |
| D3. Conditional definition | `If(condition, P, Q)`, conditions inside expressions, piecewise definitions | outside the class, unless the condition is a predicate of the class on objects of the class and is certified in the cell (rule P3) |
| D4. Conditioned selection | `Intersect` with a reference point (the nearest root), `Element(list, computed index)`, `Min`, `Max`, `Sort` or `First` over computed values, the nearest point | outside the class |
| D5. Numerical algorithm with tolerances or iterations | `Root`, `NSolve`, `Intersect` of numerical curves | outside the class |

Objects of D2 to D5 are **declared boundary**:

- a point they produce is a substitute in an exact evaluation and a cut in a certificate; what is
  computed from it afterwards with algorithms of the class is evaluated from that cut;
- a number they produce is a substitute or, in the automatic scope of a certificate, an
  indeterminate;
- a line or a circle they produce is outside the class, and its relations stay
  `NOT_ESTABLISHED` until another object is cut.

### 3.3 Propagation through inherited decisions

- **P1. No inherited decision is a strict premise.** A relation is `STRICT` only if every decision
  that its proof uses is rigorously established: provenance with established existence, certified
  signs, exact decisions or transferred certificates. A decision of D1 to D5 taken by the
  inherited algorithm never is.
- **P2. A cut forgets the decision, and its enclosure covers it.** An identity that holds for every
  value of a cut does not use the decision that produced it, and may be `STRICT`. The certified box
  must then contain **every** value that the dependency can take ideally: the enclosure of the
  chosen value when the decision is established (for example, when the condition of an `If` is
  certified); otherwise an enclosure of every possible outcome of the decision (both branches of
  an `If`, every candidate of a selection). If that enclosure does not fit the certified cell, the
  result is `NOT_ESTABLISHED`.
- **P3. A decision of the class enters the cell.** When the condition or the selection is a
  polynomial predicate of objects of the class (for example, `x(A) > 0`), the certificate decides
  it like any other sign, records it and rechecks it; the conditioned object is then no longer a
  boundary.
- **P4. Booleans and numbers of D2 carry no integrity.** Using them as a condition leaves the
  existence or the selection of the conditioned object `NOT_ESTABLISHED`, unless P3 establishes it.
- **P5. Relaxed evidence across the boundary.** `SUBSTITUTE_GENERIC` (§7.1) may make `RELAXED` a
  relation that depends on a boundary substitute: it perturbs the substitute, never re-executes the
  inherited decision, and the record names the inherited decision as a premise that is not
  established.
- **P6. Nothing inherited changes.** The outputs of inherited commands are the same under every
  policy, and GeoCeDG adds no reinterpretation by tolerance.

## 4. Document policy

The policy is a versioned, additive document record. A document without it is governed by
`legacy-representation-authority/v1`, the contracts approved before this amendment, and no
ordinary operation changes it. Every document, new or existing, is created or kept under that
policy and adopts `constructive-authority/v1` only through the explicit semantic event
`AUTHORITY_POLICY_ADOPTION`; making the constructive policy the default for new documents requires
a specific author decision. The event reports its impact before it is applied — an empty report
for an empty document — is atomic and undoable, changes no identity, token or selector, withdraws
contradicted results with a typed cause and never reassociates them. One document has one
authority. The authority is never chosen per relation, per object or per command. Creating a
construct that requires the constructive policy in a legacy document requires the adoption event
or fails closed. A reader that does not support the record fails closed with a typed cause and
loads nothing; it never reads the document under another authority.

Opening, editing, saving, undoing and redoing never change the policy of a document; there is no
automatic way back, and returning to the earlier policy is Undo. Copy, paste and macros follow the
policy of the destination. A constructive document never loses its record when it is saved.

## 5. What is persisted

The policy record is persisted. Integrity, transfer records, certificates and their evidence are
revision evidence, recomputed and never persisted. The syntax and the place of the record in the
native document are part of the persistence contract of increment E1, under the rule of §4 that a
reader that does not support the record fails closed.

## 6. Integrity of relations (`relation-integrity/v1`)

Every relation decided under the constructive authority carries an integrity of its own type,
`RelationIntegrity`, never a value added to an existing enumeration: `STRICT` (proved or rigorously
certified for the constructive objects), `RELAXED` (admitted by a versioned positive policy without
rigorous contradiction), `NOT_ESTABLISHED` (neither proved, admitted nor contradicted) or
`CONTRADICTED` (rigorously refuted for the constructive objects). Integrity is independent of the
numeric guarantee of any representation. A relation carries its method and premises; its
integrity is that of its method bounded by the worst of its premises; a `CONTRADICTED` premise
voids the derivation, whose result is `NOT_ESTABLISHED` unless it is itself refuted. A residual
that is not identically zero is not a contradiction. A consumer declares the minimum integrity it
requires and fails explicitly with a typed cause when it is not met. Integrity is revision
evidence; it never enters a token, a selector or the construction graph and is never persisted.

### 6.1 Premises and existing vocabularies

- A relation records its method and its premises: provenance relations, certified relations and
  existence facts. It inherits only what its proof uses: a provenance incidence is `STRICT` even
  when a distant relation is `RELAXED`.
- Toward `STRICT` counts only rigorous evidence for the constructive object: provenance with
  established existence (§2), transferred CU2 certificates
  (`locus-v2-certified-construction-model.md` §12.2), exact or rigorous interval evidence of the
  nature fixed by ADR 0038 (decision 18) decided in `Σ_C`, the grades `EXISTENCE_CERTIFIED`,
  `EXISTENCE_STRUCTURAL` and `EXISTENCE_EXACT` and the contact classes 1 and 2 of the intersection
  contract decided against the constructive target, and the certificates of §8. A class 2 obtained
  with non-constructible substitutes is `SUBSTITUTE_GENERIC` evidence, that is `RELAXED`.
- Toward `RELAXED` count only the kinds of §7. A numerical verification under hypotheses, the grade
  `LOCALLY_VERIFIED` or the metric class 3 (`ESTIMATED`, ADR 0037) is not enough by itself.
- Metrics keep their own admissibility (ADR 0037); relaxed admission applies only to incidences
  and memberships. `NumericGuarantee` remains the axis of the precision of a representation and
  does not correspond to integrity.

## 7. Relaxed admission (`constructive-relaxed-admission/v1`)

A relation is `RELAXED` if and only if (a) it is an incidence or a membership between constructive
objects requested by a consumer that accepts `RELAXED`; (b) no rigorous evidence of the same
revision refutes it for the constructive objects — an exact refutation whose premise contains a
non-constructible boundary substitute is not rigorous for the ideal object and leaves the relation
`NOT_ESTABLISHED`; (c) it has positive evidence of a declared kind: `SUBSTITUTE_GENERIC` (the
relation holds exactly in the exact evaluation with the declared binary64 substitutes of the
non-constructible boundary and at every point of the deterministic perturbation set of the policy
version — in version 1, `N = max(8, (m + 1)(m + 2)/2)` dyadic perturbations of the `m` boundary
coordinates, generated by SplitMix64 from a fixed seed and accepted so that the whole set lies on
no quadric — with every validity, branch and membership predicate decided with the same strict
sign as at the base, the branches outside a driver slice corresponding to the live algorithm at
the base, and the versioned budget respected; it is recomputed at each revision, it is withdrawn
when a constructive identity certificate shows that the relation is not an identity, and it never
proves an identity) or `ENCLOSURE_COMPATIBLE` (only for a root whose sign-change bracket is
verified against the representation: a rigorous enclosure of the normalized constructive residual
contains zero and lies within `2e-12 + 2e-12 max(1, S)`, the G8B-R1 threshold with its scale `S`
per target family; a contact without a sign change is never relaxed by a residual bound); and (d)
the record names the evidence kind, the substitutes, the premises and the policy version.
`RELAXED` is never presented as a proof, never becomes `STRICT` through a tolerance, a coordinate
coincidence, persistence or another revision, and a `CONTRADICTED` relation never becomes
`RELAXED`.

### 7.1 `SUBSTITUTE_GENERIC` version 1

**Boundary coordinates.** `z ∈ Q^m` collects the exact rational values of the coordinates of every
declared substitute of the relation: two for each boundary point (the cut rule of §3.2 and §8)
and one for each boundary number. Free inputs are not substitutes: their binary64 value is their
definition.

**Deterministic perturbations.**

- **Number.** `N = max(8, (m + 1)(m + 2)/2)`.
- **Scale.** For each coordinate, `rho_i = 2^(e_i − 30)` with `e_i = floor(log2(max(1, |z_i|)))`.
  Every perturbed point is an exact dyadic rational.
- **Directions.** `d_k ∈ Z^m`, `k = 1…N`, are taken in order from the integer generator of the
  version: SplitMix64 with the seed `0x9E3779B97F4A7C15`, each output `x` mapped to the integer
  `(x >>> 53) − 1024` in `[−1024, 1023]`, zero outputs discarded.
- **Acceptance.** A direction is accepted if it gives a point distinct from the earlier ones and,
  while the matrix of the monomials of degree at most 2 of the accepted points (base point
  included) does not have full rank, if it raises that rank; otherwise the next output is taken, up
  to a versioned cap of `64·N` outputs, whose exhaustion gives `NOT_ESTABLISHED`. The `N + 1`
  points `z, z + rho∘d_1, …, z + rho∘d_N` therefore do not all lie on one quadric; this is checked
  in exact arithmetic.
- **Determinism.** The rule, the seed and the interval belong to the policy version. Two
  executions in the same revision give the same points and the same results.

**Preconditions.** If any fails, the result is `NOT_ESTABLISHED` with a typed cause:

1. every object of the relation is in the class table, except the declared boundary;
2. at the base point, the exact evaluation decides every validity, branch and membership predicate
   with a strict sign, and every branch outside the slice of a substituted driver corresponds to
   the live output of the inherited algorithm (§8);
3. at every perturbed point, the exact evaluation, with the inherited rule and the captured
   selection state, gives the same sign vector of those predicates as at the base;
4. the relation holds exactly at the `N + 1` points;
5. the cost stays within the versioned budget (§10): `N + 1` exact evaluations, with their
   deterministic counters.

**Branch checks.** At the base, outside the slice of the driver, the live output must be closer to
the chosen branch than to the other, as in §8; inside the slice the captured selection state
governs. At every perturbation the branch is the same formal branch — the same raw root index and
the same membership — with the same signs; the live product is not in those configurations and is
not consulted.

**Operational cell before a certificate.** The cell of `SUBSTITUTE_GENERIC` is the sign vector of
every exact decision at the base point. A perturbation with another sign vector gives
`PERTURBATION_LEFT_CELL` and `NOT_ESTABLISHED`; two cells are never mixed. The evidence is
recomputed at each revision and never reused from one revision to another; a drag of the driver
without a change of revision repeats nothing. When a constructive identity certificate is
available, it replaces this operational cell by its certified cell; if it gives `NOT_AN_IDENTITY`
for the same relation and cell, it withdraws the `SUBSTITUTE_GENERIC` evidence, and the relation
is `NOT_ESTABLISHED`.

**What it excludes and what it does not prove.** Let `P(z)` be the numerator of the residual as a
function of the boundary coordinates, with everything else fixed; precondition 2 ensures nonzero
denominators at the points. If `P` is a nonzero polynomial of degree at most 2, version 1 rejects
it, because no such polynomial vanishes at the `N + 1` points. When the residual contains a square
root, `a + b sqrt(Delta)`, the argument applies to its norm `a^2 − b^2 Delta`, which vanishes
wherever the residual does and usually has a higher degree. No finite set of points proves an
identity: for every finite set `S`, the product of affine forms that each vanish at one point of
`S` is a nonzero polynomial that vanishes on `S`. `RELAXED` is positive computational evidence and
never strict proof.

### 7.2 `ENCLOSURE_COMPATIBLE`

The scale `S` of the threshold is 1 for lines, segments and rays and the radius for circles. The
kind applies only to a root whose sign-change bracket is verified against the representation, that
is, a transverse root. If the bracket is re-certified against the constructive target, the root has
certified existence and its relation is `STRICT` (§7.4). A contact without a sign change, tangency
or fold, is never `RELAXED` by this kind, and a contact of class 3 of the intersection contract is
never materialized by a tolerance.

### 7.3 What `RELAXED` never is, and its consumption

- It is never a proof and is never presented as one; the interface distinguishes it from `STRICT`.
- It is never a premise of `STRICT`: by propagation, every derivation that uses it is at most
  `RELAXED`.
- It is never identity: it enters no token, selector or graph and is never persisted.
- It changes without reassociation: it falls to `NOT_ESTABLISHED` or `CONTRADICTED` when a
  contradiction appears, and rises to `STRICT` when a certificate proves the relation, without
  changing any identity.
- A relation `CONTRADICTED` for the authority of the document never becomes `RELAXED`, whatever its
  binary64 residual.
- Each consumer declares the minimum integrity it requires; a consumer that requires `STRICT` fails
  explicitly, with a typed cause, when it does not receive it. The integrity of an object is the
  worst of the relations its definition relies on, shown with the list of its `RELAXED` premises
  and always separate from the precision of its representation.

### 7.4 G8B-R1 under the constructive authority

The G8B-R1 local verification, relative to the binary64 target adapter, is unchanged in documents
governed by `legacy-representation-authority/v1` and remains the candidate search everywhere. Its
admission is not inherited by the constructive authority. In a document governed by
`constructive-authority/v1`, a root it verifies is `STRICT` when its sign-change bracket is
re-certified against the constructive target, `RELAXED` by `ENCLOSURE_COMPATIBLE` without
contradiction, and otherwise `NOT_ESTABLISHED`; a root that rigorous evidence contradicts is
withdrawn with a typed cause and never reassociated. This re-qualification happens once, in the
explicit adoption event, which reports it before it is applied; afterwards, the admissibility of a
root changes only by rigorous contradiction or new evidence of the revision.

## 8. Constructive identity certificates (`constructive-identity-certificate/v1`)

A constructive identity certificate decides a relation between objects of the class by exact
evaluation in the field `Q(V)[sqrt(Delta)]` of rational functions of a set `V` of indeterminates,
extended by square roots under a versioned rule. The boundary rule is automatic: free points and
numbers are indeterminates, the axes are constants, a point on a constructible line path is one
indeterminate with its other coordinate solved from the exact line, and every point computed
outside the class or lying on a path that depends on objects outside the class is cut as a free
point of the plane. A cut forgets relations and never creates them. A circle belongs to the class
by the algorithm that defines it, never by its current value. Every branch, validity, membership
and non-degeneracy decision is a sign condition certified by outward-rounded enclosures on a
decision box and recorded with the inherited selection states it used. Every line-circle output is
the one the inherited selection rule gives with the captured selection state; outside the slice of
a driver replaced by an exact point, its live binary64 output must be certified strictly closer, on
the whole box, to the selected branch than to the other, or the result is
`BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED`; inside the slice, the captured selection state governs,
as in the class-v3 evaluator. Continuous-mode intersections and reference-point selections are
outside the class. When the residual of the relation is the zero element and every recorded
condition is certified on a box that contains the ideal value of the indeterminates, the relation
is `STRICT` on that box (`IDENTITY_CERTIFIED`). A certified non-membership of a limited part is a
rigorous contradiction of the relation with that limited target. A residual that is not the zero
element is `NOT_AN_IDENTITY`, never a contradiction. An undecided condition, a construct outside
the class or an exhausted versioned budget gives `NOT_ESTABLISHED` with a typed cause; a validity
condition certified false gives `INVALID`, and the relation is `NOT_ESTABLISHED` with the cause
`OBJECT_UNDEFINED`. A certificate identifies objects by durable identity only (`PersistentGeoId`,
the transitive durable frontier and the revision fingerprint of each locus definition); labels and
runtime object references never establish identity. Its structural key `cic-structural-key/v1`
covers the dependency cone of the relation by durable identity and structural definition, the
classification of every object (including what is cut), the inherited selection and label states
of every intersection used, the exact values of the free inputs taken as constants and the policy
versions; the recorded signs are the cell, not part of the key. A rename keeps the key; a
redefinition, a deletion followed by recreation, a copy or a change of what is cut changes it. At
another revision a certificate applies only when its structural key is unchanged, every recorded
sign is certified with the same value on a box around the live values of its indeterminates and
every branch outside the driver slice still corresponds to the live algorithm; otherwise a new
certificate is attempted, and its absence leaves the relation `NOT_ESTABLISHED`. Certificates are
revision evidence and are never persisted. The ideal value of every indeterminate that is not a
free input — a point on a path or a cut — is enclosed rigorously.

### 8.1 Structural key `cic-structural-key/v1`

A proof is reused at another revision only when its key is equal. The key collects:

1. **the dependency cone** of the relation: the slice of the locus, the target, the driver and all
   their ancestors; anchors by durable identity (`PersistentGeoId` and the identity registry of
   ADR 0010), and intermediate objects without identity by their structural definition (ADR 0029,
   version 2): algorithm class, inputs by structural position and stable output role in
   algorithms with several outputs;
2. **the classification of every object**: in the class, point on a path, cut or outside the class,
   with the version of the class table;
3. **the branch selection**: the inherited selection state of every intersection used, inside and
   outside the slice, and the label state that decides effective outputs;
4. **the constants**: the exact binary64 values of the free inputs that the proof takes as
   constants; the values of the indeterminates never enter, since their change is a recheck and
   not a new proof;
5. **the versions** of the authority, the class table, the relaxed policy, the certifier and the
   budget.

The recorded validity and sign conditions are the cell and are rechecked at every revision; the
revision fingerprint of every slice (ADR 0038, decision 17) is cited as a premise.

### 8.2 Lifecycle

- **Rename** changes only the label: durable identities and the key are unchanged, and the proof
  is rechecked without becoming inapplicable.
- **Redefine** of an object of the cone, even when it keeps its identity by compatible retention
  (ADR 0029), changes its structural definition and the key: the proof becomes inapplicable and
  another is attempted.
- **Delete and recreate** with the same label gives another durable identity and another key:
  inapplicable, without reassociation. Deleting an ancestor deletes the subject of the relation,
  so no certificate outlives its subject.
- **Undo and Redo** restore the committed identities, roles, dependencies and revisions; the
  certificate is evaluated again and equals, after Undo, the one before the change and, after
  Redo, the one after it; no stale certificate survives.
- **Reopen**: certificates are not persisted. After loading they are recomputed with the same key,
  because durable identities are persisted, and give the same results deterministically.
- **Copy or duplicate** follows the fresh-closure rule: new identities, new keys and new
  certificates, none reused.
- **Drag** without a structural change keeps the key and only rechecks, without new symbolic work.
- **Another cell** — another sign vector — needs a new proof; when none can be made, the
  integrity is `NOT_ESTABLISHED`.

## 9. Versioned names

| Name | Meaning |
|---|---|
| `legacy-representation-authority/v1` | the authority of documents without the policy record: the contracts approved before this specification |
| `constructive-authority/v1` | the single constructive authority of §1 |
| `constructive-class/v1` | the class table of §3.2 |
| `relation-integrity/v1` | the integrity of §6 |
| `constructive-relaxed-admission/v1` | the relaxed admission of §7, with `SUBSTITUTE_GENERIC` version 1 |
| `cu2-certificate-transfer/v1` | the transfer of `locus-v2-certified-construction-model.md` §12.2 |
| `constructive-identity-certificate/v1` | the certificate of §8 |
| `cic-structural-key/v1` | the structural key of §8.1 |

## 10. Budgets

Every evaluation that this contract admits — exact evaluation, `SUBSTITUTE_GENERIC` and
constructive identity certificates — runs under a versioned budget with deterministic counters.
Exhausting a budget gives `NOT_ESTABLISHED` with a typed cause and never a decision. A budget that
the contracts leave to the author is fixed by the author from the counters of the benchmarks,
never by the implementation itself.

The author fixed these budgets on 2026-10-25
([approval record](../../../docs/validation/pre_g9b_cu3_constructive_authority_approval_record.md)
§16) and approved the limits of the certificate budget on 2026-10-26
([disposition record](../../../docs/validation/pre_g9b_cu3_author_disposition_record.md) §4). They
are semantic limits of the computation in the shared kernel; their instrumentation, benchmarks and
reports belong to the external layers.

| Budget | Scope | Limits | Status |
|---|---|---|---|
| `cu3-exact-evaluation-budget/v1` | each exact evaluation of a structural point against its target, including each of the `N + 1` exact evaluations of `SUBSTITUTE_GENERIC` (§7.1, precondition 5) | 100 000 exact operations; tower depth 8; 4 096 bits | approved; exhaustion gives `EXACT_EVALUATION_BUDGET_EXHAUSTED`, fails closed with a diagnostic and deterministic counters, and is never raised automatically nor turned into an accepted approximation |
| `constructive-identity-certificate-budget/v1` | each attempt of a constructive identity certificate | 16 000 000 term products; 16 indeterminates; 200 000 terms per polynomial; 4 096 bits per coefficient | approved on 2026-10-26, after gate G-AUT-8 measured, instrumented and validated them with adversarial tests; deterministic counters and a budget per attempt; exhaustion gives `BUDGET_EXHAUSTED` and fails closed; never extended without an author authorization, never raised automatically nor turned into an accepted approximation |

The monomial representation of the certificate supports all 16 indeterminates without collision
or overflow; an exponent beyond its representable range fails closed with a typed cause and is
never wrapped or truncated.

## 11. Compatibility

No existing format changes: the policy record is additive. Documents that do not adopt the policy
keep their policy and their results. Tokens, selectors, lineage keys and the certificates of `CU2`
are unchanged. A `CU2` certificate keeps its authority over its captured configuration; a
constructive transfer is new and separate evidence. The future promotion of the constructive
authority to the default is planned and is not authorized by this version.
