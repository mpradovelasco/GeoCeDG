# PRE-G9B-R — final stabilization, semantic debt and authoring readiness

- Status: **DESIGN CANDIDATE — PENDING AUTHOR REVIEW**
- Recorded: 2026-09-17
- Implementation base commit: `0d13434cf1ab4d31f4bb4d50e7aea6cb3e69e35d`
- Implementation base tree: `922034fd46411530bd02ae15fcc832e3fc541dc7`
- Producing phase: `PRE-G9B-R0` (characterization, technical-debt disposition, staged design)
- Change route: `ORDINARY`
- `PRE-G9B-R0` frozen verification class: `DOCUMENTATION_STATUS_ONLY` (declared at phase start; ratification is an author decision)
- Claim vocabulary: **proposed / not normative**
- Self approval: **false**

This note is the staged design produced by `PRE-G9B-R0`. It is planning
documentation. It authorizes no implementation, creates no normative geometric
contract, and does not promote any phase, specification, ADR or roadmap item to
`PASS` or `AUTHOR APPROVED`. `R1`–`R7` below are **designed and not
authorized**; each requires a separate explicit author authorization naming its
exact scope before any product change is made.

`PRE-G9B-R0` executes the planning action the living roadmap already declares as
the next gate, `POST-P1 ROADMAP / TECHNICAL-DEBT DISPOSITION`
([roadmap](../roadmap/geocedg_roadmap.md)). `PRE-G9B-R` is the track identifier
proposed for the work that gate disposes of; it is not a new productive gate and
it does not presume that the next implementation is `G9B`.

**Family naming.** The existing PRE-G9B track uses `S` for stabilization, `D` for
deployability and `P` for promotion
([design](pre_g9b_stabilization_deployability_design.md)). `R` is proposed here
for *readiness and remediation*, on the same basis as those families: it does
not occupy a `G9` semantic identifier, and none of these identifiers is yet a
registered verification phase or an implementation authorization.

## 1. Governing authority and placement

The authority order is `AGENTS.md` section 2. This note is subordinate to
current source, to the accepted specifications under `geocedg/specs/`, and to
the accepted ADRs. Where this note describes present behaviour, the source
citation is the authority and this text is a pointer.

Every item below carries an explicit architectural layer decision, made against
`AGENTS.md` section 4. The recurring rule for this track is:

- **Shared Java kernel** owns semantic identity, durable dependency projection,
  DAG propagation, semantic parameterization, intersection identity and
  admissibility, metric endpoint provenance, compatible redefine and
  serialization.
- **Desktop / frontend** owns gestures, ordered selection collection, tool
  modes, previews, help presentation and navigation. It may collect and
  present; it never becomes geometric authority.
- **Documentation / help** owns explanatory structure. It is never consulted by
  intersection, identity, DAG or serialization code.
- **External tooling / verification** owns receipts, pins, capability matrices
  and reporting. It never translates an environment failure into product code.

No item in this track may repair semantic identity or incidence from
coordinates, proximity, screen state, output order, labels or render geometry.

## 2. Characterized observations

The author witness is
`artifacts/author-input/post-p1-defects/SeveralDefects.cedg`
(untracked author evidence under the ignored `artifacts/` tree, SHA-256
`c0cbdb8ea218e4a07b7cd4d7bcfb4ba2b74da373bbd49711aa4657349619dff8`, preserved
byte-unchanged). Its decoded construction and the per-observation evidence are
recorded in
[the R0 candidate report](../validation/pre_g9b_r0_characterization_candidate_report.md)
and
[the R0 disposition matrix](../validation/pre_g9b_r0_technical_debt_disposition_matrix.md).

### 2.1 D1 — `LocusV2 × SplineV2` intersection materialization

**Classification: `INTENTIONALLY_UNSUPPORTED_CURRENT_CAPABILITY`.** The observed
behaviour conforms to the approved contract, and no normative text ever
authorized materializing a generic `LocusV2 × SplineV2` pair.
`geocedg/specs/curves/spline-v2-pair-materialization.md` §1 states it directly:
"Generic LocusV2 x LocusV2 remains under its existing rich-only materialization
policy." `geocedg/specs/curves/semantic-spline-2d.md` §5.2 and ADR 0018 state
the same for the base case, and ADR 0021 Decision 1 restricts materialization to
authenticated `SplineV2 × SplineV2` pairs and certifiable similarity images.

**Layer: shared Java kernel** (only if the author ever authorizes an extension).
The frontend owes nothing: the desktop dialog enumerates only tokens the kernel
has already marked point-admissible and otherwise renders the
no-admissible-token message, so it faithfully mirrors the kernel gate. The one
bounded documentation obligation is that the user guide does not currently state,
in user terms, that a pair intersection materializes points only when both
operands are `SplineV2` (or certifiable similarity images of one).

**The gate chain.** `Intersect(d, a)` is a supported `Locus V2 × Locus V2` target
family and produces a `GeoLocusIntersectionResult`. What it does not produce is
a durable public token, because public pair resolution is guarded four times
over:

1. the public spline-pair resolver is reached only when a ledger evaluation is
   supplied, which the pair algorithm does only for a public command — internal
   callers bypass public resolution entirely;
2. that resolver returns the discovered result unchanged unless the spline-pair
   interval certification reports supported;
3. certification requires the interval-model capture to succeed on **both**
   sides, and capture returns null unless a side resolves — through at most the
   permitted non-collapsed similarity captures — to the spline semantic
   evaluator. `a = LocusV2(E, C)` carries a reconstructible-locus evaluator
   instead;
4. independently, the generic discovery capabilities construct every candidate
   with *not-established* local pair isolation, and only the certified route
   constructs established isolation with a certified rectangle. Per-solution
   point admissibility for a pair requires established isolation.

Consequently no ledger allocation occurs, and no exact token exists to pass to
the materialization algorithm.

**Witness corroboration from persisted state, not inference.** The serialized
token ledger of `e = Intersect(d, a)` decodes to owner `geo:023cf87a…`, source
pair `(geo:75930c9f… , geo:af7727d4…)` = `(a, d)`, constructive lineage
`g9u0-public-locus-pair-intersection/v1`, and a durable token count of **0**.
The ledger of `g = Intersect(a, c)` decodes to source pair `(a, c)`, lineage
`g9u0-public-intersection/v1`, and a durable token count of **4** — the four
tokens the author used to materialize `I`, `J`, `K` and `L`. Independently, the
ledger uses two distinct allocation prefixes, `g9s1-r1/ledger-pair-slot/v1/` for
the pair route and the current-root prefix for the one-source route, and the
author's four tokens name `g9u0-r4/ledger-current-root/v2` — a prefix the pair
route structurally cannot produce. Zero durable pair tokens is the contractually
expected outcome, not a token-enumeration defect.

**Bounded unresolved hypothesis (must be settled by the first scenario of any
future phase, not assumed).** The decoded ledger proves zero durable tokens but
does not by itself distinguish two runtime states for `e`: a `SUCCESS` result
carrying discovered solutions whose pair isolation is *not established*, or an
`UNSUPPORTED` computation status, which the pair solver returns when the
fallback capability's precondition — finite components on both sides — does not
hold. Both are consistent with "rich result, no materializable points", and they
are different diagnostic stories for an author. A related open question: on the
generic path the spline resolver returns before its diagnostics loop, so no
pair-selector class evidence is emitted at all; the honest label may be
"unsupported *and additionally undiagnosed*".

**Smallest defensible extension family, if ever wanted.** Not a blanket
admission of arbitrary `LocusV2` pairs. The only defensible widening is a pair
in which *both* sides can supply an exact, outward-roundable local germ of the
rigour the approved `pair-singleton-transverse-germ/v1` contract already
requires — concretely, an evaluator exposing a certifiable piecewise-polynomial
model with exact rational coefficients equivalent to what the interval-model
capture extracts today. A reconstructible-locus evaluator is a deterministic
numeric dependency evaluator with no exact coefficient authority, so no outward
enclosure of the true function and Jacobian is obtainable from it, and
substituting rounded or sampled data is forbidden by the same contract.

Note for any such design: evaluator-class identity is *not* the only family
discriminator already present in the kernel. Two non-presentational kernel sites
discriminate spline from generic by parent algorithm — the selected-root
continuous-domain certifier and the spline constructor-occurrence resolver — so
a future contract must state which discriminator is authoritative rather than
assume one.

The chain `rich semantic intersection -> current admissible exact token ->
explicit materialization` would be preserved unchanged, with the selector built
only from durable source identities, branch and component lineage, orientation,
domain kind, parameterization contract version and the normalized transverse
germ sign. No coordinate or proximity matching participates at any step: the
existing replacement test is a component-key plus semantic-parameter chart
containment, never a nearest-root match.

**This extension is not part of `PRE-G9B-R`.** It would amend ADR 0021 Decision 1
and the `spline-v2-pair-materialization.md` scope, so it requires a new ADR and
a normative specification amendment, and therefore a separate author decision
(`AD-R0-1`). Scheduling it inside a stabilization track would misclassify a new
capability as a defect fix.

### 2.2 D2 — `SplineV2` authoring and help interaction

**Classification: `MISSING_FRONTEND_EXPOSURE_AND_INCORRECT_CONTEXTUAL_HELP`.**

**Layer: Desktop/frontend and documentation/help, with one qualified kernel
question (closed form).**

The kernel command surface is already complete for the open forms.
`CmdSplineV2` accepts a point list; a point list with an explicit degree; a
point list with degree and a weight function; and bare point arguments, which it
wraps into a list. Omitted degree defaults to `3`. The wrapping form builds its
list under suppressed label creation, so no visible auxiliary list is ever
produced. Exactly two bare point arguments is not accepted, consistent with the
three-point minimum. The bare-point form is not merely inferable from the
dispatch table — it is directly tested as numerically equivalent to the list
form.

One precision that matters for help text: explicit-degree validation, which
enforces the degree range and the degree ≤ point-count ceiling, runs **only** on
the forms that supply a degree. The default-degree paths bypass it, so an
over-large point set is rejected later by the polynomial model's own guard and
surfaces as the localized invalid-definition error. The forms therefore differ
in where and how they fail.

Three distinct frontend/help defects were characterized, and they are separable:

1. **Contextual help is mode-keyed and `SplineV2` has no mode.** Tool name and
   tool help resolve through the Euclidian mode text. The profile declares the
   `SplineV2` action as a command-kind action with no numeric mode, so the
   registry dispatches it without setting a mode and the contextual-help action
   necessarily describes the *previously active* tool. This is exactly the
   author's observation.
2. **The action opens the generic input-help panel without a selection.** It
   shows the Algebra input, prefills the literal `SplineV2(` **only when the
   input field is blank**, sets a tooltip, requests focus and opens the generic
   help panel. The correct localized syntax already exists in the default, `_en`
   and `_es` command bundles, and the help panel can render it; the panel is
   simply opened with nothing selected. This is not a missing-key degradation.
3. **The one contextual string the product does show is wrong.** The profile
   tooltip advertises a degree-2 example, which the command's degree validation
   rejects, and mentions forms that have no counterpart in the dispatch table. A
   user who follows the only contextual text shown gets an error. Recorded as
   `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX`.

Two things are **not** missing, contrary to the obvious first hypothesis. The
tool icon already exists as an owned SVG and is already bound to the action key
by the desktop tool-image resource, so no new asset is required. And the action
is **already rendered** in the construction-semantic-curves profile flyout: that
group is skipped by the ordinary toolbar compiler and built by the flyout
builder, which iterates every action id with no mode test. What is missing is
mode *semantics* — set-mode dispatch, a gesture and mode-keyed help — not
toolbar rendering.

Secondary: `SplineV2` is filed under the function/calculus command table while
`LocusV2` sits under geometry, so a user browsing the help tree looks in the
wrong category.

**Designed (not implemented) `SplineV2` tool.** `LocusV2` is the template and is
fully wired — mode constant, mode text, localized tool and help keys, an
`upstream-mode` profile action with an audited numeric id, and a controller
branch that collects two points and calls the kernel operation. The `SplineV2`
tool follows the same seams:

```
activate -> select ordered points -> finish -> degree defaults to 3
         -> optional explicit degree change -> one normal construction commit
```

- a new mode constant and mode text returning a `SplineV2.Tool` key, plus
  `SplineV2.Tool` / `SplineV2.Help` entries in the default, `_en` and `_es`
  bundles — this alone repairs the contextual-help action and the toolbar help
  strip;
- the profile action changes from command-kind to `upstream-mode` with an
  audited numeric id, so the registry takes the set-mode branch and the flyout
  mode selector recognizes it;
- a controller branch modelled on the existing polyline collection, accumulating
  into the ordered selected-point list and requiring at least three points;
- the existing polyline preview seam reused as a provisional control polygon.
  The preview is never the spline; the spline appears only after commit;
- degree defaults to `3` with no prompt. Changing the degree is a *second*,
  explicit gesture, never inferred;
- one call to the existing kernel creation operation, returning through the
  normal end-of-mode path so the existing single undo-store callback produces
  one undo step.

The frontend collects an ordered selection and nothing else. Degree
admissibility, span count, knot placement, closedness detection and continuity
stay in the kernel.

**Qualified kernel question — the closed periodic form (`AD-R0-8`).** Closedness
is real kernel state: the structural spline model carries and branches on a
`closed` flag, exercised by existing kernel and native-archive tests through a
list whose first and last members are the same point. The only public creation
entry takes construction, label, points, degree and weight — it carries **no**
closed parameter, so closedness is declared solely by the repeated list member.
Two consequences: the host selection manager toggles selection, so a point
cannot be selected twice by the ordinary gesture, and the polyline gesture
already consumes a re-click as "finish". Whether the desktop may *synthesize*
that repeated member is an authority question, not only a gesture question, and
it must be decided before the tool is implemented. Subject to that decision, no
kernel extension is required for the open forms.

**Designed (not implemented) "Create List from Selection" tool.** The host
already has a list-from-selection mode, but it is rectangle-driven, strips
parent points before building the list, and orders members by hit order. It is a
valid precedent for the *output* — an ordinary `GeoList` built by the existing
dependent-list algorithm — and not for the *input gesture*. The proposed ordered
variant:

| Aspect | Contract |
|---|---|
| Ordering | exactly the click order taken from the ordered selection list; never construction order, label order, hit order or z-order |
| Duplicates | rejected by default, because the host selection path toggles; a repeated member would need its own explicit gesture and must never arise implicitly |
| Mixed types | allowed; the output is an ordinary `GeoList`. Consumers validate and report their own localized errors |
| Cancel | Escape or a mode switch clears the pending selection and creates nothing; no partial list is committed |
| Undo/redo | one undo point per committed list, from the existing end-of-mode callback; a cancelled collection stores nothing |
| Downstream dependencies | normal dependent `GeoList` in the ordinary DAG; no CeDG-specific list semantics and no new semantic type |
| Member deletion / redefinition | inherited host behaviour for a dependent list; nothing is special-cased for splines |

The `SplineV2` tool must **not** be required to create a visible auxiliary list.
Its default commit uses the bare-point form, whose wrapped list is unlabeled and
suppressed. A visible `GeoList` appears only when the user explicitly invokes
the list tool first and feeds that list to the spline.

Note the interaction with `AD-R0-8`: if the closed form is to be authorable at
all, the ordered list tool's duplicate policy and the spline tool's finish
gesture must be decided together.

### 2.3 D3 — apparently false ambiguity during Redefine

**Classification: `INCORRECTLY_REJECTED`**, resting on a code-level asymmetry.
Two separable defects are involved.

**Layer: shared Java kernel** for the projection defect; kernel status typing
plus Desktop presentation for the vocabulary defect. The frontend is a faithful
renderer of a closed kernel status and has no authority to upgrade it.

The user-visible message is the GeoCeDG profile text
`Redefine.Status.AMBIGUOUS` ("The semantic redefine is ambiguous. The
construction was not changed." / "La redefinición semántica es ambigua. La
construcción no se modificó."), synthesized by the desktop redefine adapter from
the kernel assessment status.

`SpatialRedefineAssessmentStatus.AMBIGUOUS` has exactly one producing site: the
`IllegalArgumentException` catch around `describeRedefineProposal` in the
registry's `assessRedefine`. That catch runs *before* cycle analysis, before the
host-redefine shape check, before the compatible-redefine `inspect`, before
impact/currentness analysis and before any legacy-replacement offer. Parsing,
overload resolution and evaluation of `C + (1, 0)` have all already succeeded at
that point — which is why the identical expression constructs `M` without
complaint.

**Defect D3-a — the dependency-projection null-handling asymmetry.** The throw
that fires is in the construction-geo redefine provider's dependency-id
computation, which walks the candidate's direct algorithm inputs and throws
`"Construction-defined candidate has an unregistered dependency"` for any input
without a persistent identity. `E`'s parent is a midpoint algorithm whose direct
inputs are `D` and the unlabeled helper materialized from `C + (1, 0)`; neither
is registry-participating. The witness proves `D` is not participating: the
persisted identity record for `E` lists `dependencies="geo:0ec40b8a…"` — `C`
alone, with `D` dropped.

Precisely stated, the two sides use **the same direct projection over the parent
algorithm's inputs, with opposite null handling**: publication *drops* inputs
without an identity, assessment *throws*. The old signature is read back
verbatim from the record produced by the dropping rule and compared against a
candidate signature produced by the throwing rule. That single asymmetry is the
defect. It is not "two different projections", and saying so would misdirect the
fix.

A direct corollary: because `D` is unregistered independently of the edit, *any*
redefine of `E` — including re-entering the identical `Midpoint(D, C)` — takes
the same throw. The verdict is not about the author's expression at all.

**A narrow fix is not simply "make assessment drop nulls too".** A second gate
sits behind the throw: for a non-public candidate, any dependency-set change is
rejected outright. Under a null-dropping direct projection, `E`'s candidate
dependency set becomes empty, unequal to the stored `{C}`, so the dependency-set
gate rejects — and it rejects on *both* the retain inspection and the
replacement inspection that the assessment already performs with replacement
explicitly selected, yielding `UNSUPPORTED` rather than a legacy offer. Only a
projection that reaches `C` **transitively through** the unregistered helper
yields an unchanged `{C}`, hence a definition change and `RETAIN`.

That transitive closure is a genuinely **new third rule**, and it must be adopted
on both sides together or the asymmetry simply moves. Which rule becomes
canonical, and whether persisted signatures change as a result, is the central
R3 design decision. No document currently on disk states the normative durable
dependency projection rule; R3 must write it.

**Correction to a tempting authority claim.** ADR 0026 lists "Ignoring
unregistered neighbours or replacing their omission with a universal rejection"
among its rejected alternatives, but its context and its corresponding decision
item are about **procedural position and surviving order**, not about
dependency-signature projection — and that same decision item explicitly
sanctions failing closed when surviving order cannot be established. The ADR
supports the general CeDG principle that ordinary unregistered elements must
neither become identities nor cause blanket rejection; it does not directly
adjudicate this path. The classification rests on the code-level asymmetry, not
on that quotation.

**Defect D3-b — the status vocabulary collapses distinct causes.** Nine distinct
`IllegalArgumentException` sites — seven in the construction-geo provider plus
two in the registry's proposal description — all collapse onto the single
`AMBIGUOUS` status. Among them is a *correct* rejection, "Candidate changes the
construction-defined geo family", which is reported to the user as "ambiguous".
This vocabulary defect is independent of D3-a and could be fixed separately
without touching the projection.

Related and cheap: on the path where no assessment handler is installed, the
kernel already surfaces the provider's own reason string through a typed
diagnostic. Only the desktop dialog discards it, showing a generic status text
instead. The provider's in-class precedent for treating a description failure as
a boolean rather than a thrown exception already exists.

**Expected effect of a correct `RETAIN`.** `E` keeps its durable identity; the
`a → E` DAG edge survives; `a`, then `e` and `g`, and the materialized points
`I`–`L` are recomputed and revalidated under the existing currentness rules; the
definition revision advances, and the topology revision only if the durable edge
set genuinely changed; the change stays one host redefine and one ordinary undo
step; save and reopen reconstruct the committed relation. `M` is unaffected and
stays identity-free.

**Coverage gap that let this through.** No kernel test asserts `AMBIGUOUS` at
all; the only occurrence is a desktop presentation loop over unavailable
statuses. Every compatible-redefine test targets public V2 outputs whose inputs
are all promoted. Redefining a promoted *ordinary* participant that has an
unregistered input is untested, and the `"unregistered dependency"` message has
no test coverage whatsoever.

**Invariants a fix must not break.** These are normative constraints on R3:

1. no coordinate, proximity, label, ordinal or render-geometry repair of
   identity or incidence, at any point of the projection;
2. ordinary construction elements must not become durable CeDG identities; the
   helper from `C + (1, 0)` must stay identity-free;
3. publication and redefine must use one and the same durable dependency
   projection — the asymmetry is the defect;
4. a genuine durable-contract change (provider, family, schema, authority,
   binding role, stable role, cardinality) must still fail closed;
5. real cycles must still be `INVALID_DAG`, incomplete stable-role groups must
   still fail closed, and the staged-dependency closure check must stay exact;
6. atomicity, the host-state seal, the final pre-mutation currentness
   authorization and XML rollback must be preserved;
7. failure of `RETAIN` must never imply `FRESH`; legacy replacement stays
   explicit and requires a complete impact report;
8. narrowing only — every case the detector rejects correctly today must still
   be rejected, and the frontend must remain a renderer with no authority to
   upgrade a status.

## 3. Technical-debt disposition

The full disposition, with evidence, is in
[the R0 disposition matrix](../validation/pre_g9b_r0_technical_debt_disposition_matrix.md).
Summary of the decisions this design assumes:

| Item | Disposition | Owning phase |
|---|---|---|
| `TD-P1-SEMANTIC-LENGTH-INTERSECTION` | **retain**, re-scoped: it is *not* an intersection-materialization problem | `R2` |
| `TD-P1-PACKAGING-SUMMARY` | **retain**, bounded; three couplings identified | `R1` |
| `TD-VERIFY-RECEIPT-RECOVERY` | **retain**, re-stated: the gap is larger than the roadmap wording | `R1` |
| `TD-G9X1-HISTORICAL-PIN` | **retain**, exact stale pin and successor identified | `R1` |
| `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` | **retain as open risk**; closure proposed in `R1-R1`, author decision required | `R1-R1` (proposed) |
| `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX` | **new**, opened by R0 | `R4` (or an authorized bounded correction) |
| `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY` | **new**, opened by R0 | `R3` |
| `TD-R0-GGBSCRIPT-STRATEGY-RESTORE` | **new**, opened by R0 | `R6` |
| `TD-R0-G9U1-Q02-BINDING` | **new**, opened by R0 | `R1-R1` |

Nothing is deleted from the roadmap. Each retained item keeps its identifier and
its recorded origin.

### 3.1 `TD-P1-SEMANTIC-LENGTH-INTERSECTION` and its relation to D1

The two are **`PARTIALLY_COUPLED` with no dependency in either direction**, and
they must not be merged.

- **D1 answers:** *may a point exist at all for this token?* The decisive
  per-candidate gate is the local pair isolation evidence attached to each
  candidate: the evaluator-only pair capability constructs every candidate with
  *not-established* isolation, and only the certified spline-pair route sets
  *established* with a certified rectangle. Global completeness does **not** gate
  materialization — a materialized point demonstrably coexists with
  `Completeness.NOT_ESTABLISHED` today.
- **This debt answers:** *given a point that already exists, can it address a
  semantic position on `S`?* The metric endpoint resolver accepts exactly two
  provenance shapes: an explicit semantic locus point whose source is the
  metric's own locus by reference identity, and a unique spline constructor
  occurrence. A point produced by the intersection-point algorithm is a bare
  `GeoPoint` carrying only coordinates; all provenance stays on the algorithm
  and the rich result. Both shapes fail, and the metric publishes
  `INVALID_QUERY`.

Spline-pair materialization already works today, so the metric failure is
reproducible without any D1 change; conversely, widening D1 would publish more
points and change nothing in the resolver.

Three refinements matter for R2's design.

First, the constructor-occurrence shape is bounded by **source family before
slot membership**: the resolver rejects any source without a supported
`SplineV2` constructor lineage before it inspects slots, and the metric
specification states the same normatively — an ordinary point is admitted only
when the supplied source is a current `SplineV2` or a supported similarity image
of one. For the witness source `a = LocusV2(E, C)` the rejection is therefore a
source-family rejection, which is a contractually harder barrier than a slot
miss.

Second — and decisively for feasibility — the kernel **already performs the exact
provenance walk** a third endpoint family would need: the selected-root
continuous-domain certifier walks dependent point → intersection-point
algorithm → rich input → intersection algorithm → source, then reads the
effective root token and the retained root selector, with a fail-closed idiom in
which every non-matching shape returns null. A third admissible endpoint
provenance family has an established in-kernel pattern to follow.

Third, per-side locus identity and semantic revision are copied straight from
the query binding, so side ordering is fixed and the "which side is `S`"
question narrows to the genuine `S × S` case. And the metric already rejects an
undefined endpoint before any provenance work, so a dormant materialized point
already fails closed one step earlier than the resolver.

**Invariant R2 must preserve.** A metric endpoint is admitted only by explicit
semantic provenance carried through the DAG. Coordinates, proximity, tolerance,
labels, branch order, solution index, render samples, construction order and XML
position must never establish, disambiguate or repair an endpoint address.
Ambiguity and staleness fail closed to `INVALID_QUERY`, never to a nearest
match. No current code path resolves a metric endpoint from Cartesian
coincidence, and two existing negative tests pin that.

## 4. Proposed phase sequence

The author's proposed sequence is adopted with three evidence-driven changes,
each explained:

1. **`R2` is re-scoped** from "semantic intersection/metric provenance
   stabilization" to metric endpoint provenance only. D1 is conformance, not a
   defect; any intersection extension is a new capability requiring its own ADR,
   and folding it into a stabilization phase would misclassify it.
2. **`R1-R1` is added** as a refinement of `R1`, carrying the
   `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` closure. It is a product test in
   the Desktop module under the `G9U1` regression perimeter, so it cannot share
   `R1`'s frozen verification class, which is operational verification
   infrastructure.
3. **`R5` is split into two separately authorized slices**, `R5-A` (guide
   navigation) and `R5-B` (canonical English command names), because `R5-B`
   requires its own compatibility ADR, reaches shared-kernel localization
   seams, and carries a semantic-identity hazard that `R5-A` does not.

### Common rules for every phase

- Each phase freezes exactly one `VERIFICATION_CLASS` before implementation, and
  the frozen class selects the minimum acceptance coverage. An agent may not
  raise it by prudence; a higher level requires a concrete
  `VERIFICATION_ESCALATION_REQUEST` and explicit author authorization. The
  classes proposed below are **proposals for the author to freeze**, not
  derivations.
- Each phase ends as a candidate pending author review. `selfApproved=false`
  throughout. Technical `PASS` never implies author approval.
- No phase publishes binaries, creates a release or tag, contacts
  GeoGebra/OpenGeoProver, or alters the `COMMERCIAL` disposition.
- No phase reopens an approved phase. Existing `PASS — AUTHOR APPROVED` states
  are preserved.
- Each phase records `BOOTSTRAP IMPACT`, infrastructure impact, required levels
  with exact commands and exit/log evidence, and `GUIDE_IMPACT`.

### `PRE-G9B-R0` — characterization, debt disposition, staged design

| Field | Value |
|---|---|
| Objective | Characterize D1/D2/D3 against live source, dispose the open technical debt, and produce this staged design |
| Layer | documentation / planning only |
| Prerequisites | none beyond the closed `PRE-G9B-P1` |
| Normative work | none; no spec or ADR is created or changed |
| Productive scope | `PRODUCT_PHASE_EFFECT = NONE` |
| Forbidden scope | every `R1`–`R7` product change; the intersection solver/materializer; `Length`/`LocusLength` semantics; redefine behaviour; `SplineV2`/list tools; command-language behaviour; GGBScript functionality; `G9B` redesign; `G9B`/`G9C`/`G9U2`/`G10`/`G12` implementation |
| Acceptance criteria | every claim carries a live source citation or is explicitly marked unresolved; every classification is typed; no debt is silently deleted; later phases are recorded as designed and not authorized |
| Focal tests | none; no product or test code is added |
| Verification profile | `VERIFICATION_CLASS = DOCUMENTATION_STATUS_ONLY` declared at phase start; `tools/agent/verify.ps1 -Profile STATIC` |
| Author smoke | not applicable |
| Stop conditions | a claim that cannot be grounded in source and cannot be stated as a bounded hypothesis; any change that would touch `geocedg/specs/operations/verification-*.json`, which would forfeit the documentation class |
| Does not authorize | anything productive; any later phase; any promotion |

### `PRE-G9B-R1` — verification and bounded operational debt

| Field | Value |
|---|---|
| Objective | Close `TD-VERIFY-RECEIPT-RECOVERY`, `TD-P1-PACKAGING-SUMMARY` and `TD-G9X1-HISTORICAL-PIN` |
| Layer | external tooling / verification; no kernel, no geometry |
| Prerequisites | author authorization; and for the receipt slice, explicit author definitions for the three caller-supplied receipt inputs that have no production producer (`AD-R0-2`) |
| Normative work | amend `geocedg/specs/operations/verification-levels.md` only if the receipt emission point changes the documented contract; record the G9X1 supersession relation |
| Productive scope | receipt emission wired into the accepted-FINAL path using the existing, already-gated writer; producers for the missing receipt inputs; the packaging summary derived from the distribution profile; the G9X1 pin reconciled as historical snapshot plus live successor plus supersession record |
| Forbidden scope | fabricating any receipt or hash; substituting the historical G9X1 hash; reopening `G9X1`; changing acceptance semantics, gating or coverage; any kernel or geometric change; any packaging composition or licensing change |
| Acceptance criteria | a FINAL campaign on an accepted, complete, trusted, immutable candidate emits exactly one schema-valid receipt; a non-FINAL or non-accepted run emits none; the packaging summary prints the profile-derived condition for an NC package; the G9X1 verifier passes with both the historical and the successor authority recorded |
| Focal tests | receipt emission positive and negative paths, extending the existing receipt test module; packaging summary derivation; G9X1 verifier run |
| Verification profile | `VERIFICATION_CLASS = OPERATIONAL_VERIFICATION_INFRASTRUCTURE`; focused operational evidence **plus FULL**, because receipt emission is global verification infrastructure |
| Author smoke | recommended for the packaging summary on a real NC package build |
| Stop conditions | any receipt input whose semantics the author has not defined; any temptation to synthesize a hash; the static-contract repin or registry catalog pin not moving atomically with the packaging change |
| Does not authorize | `R2`–`R7`; any product/geometric change; publication, tag or release |

**Corrected statement of the receipt gap.** The roadmap says the flow "does not
always" emit a receipt; the source says it never does. The top-level verifier
does not import the receipt module at all, and the supervisor writes only the
run-level result and journal (plus a per-check output directory). The only
invocations of the receipt writer in the repository are in its own test module,
and closeout requires a receipt path as a mandatory parameter — which is exactly
why the most recent documentation track recorded its canonical closeout as not
executed and the receipt as absent, with the author accepting that as known
operational debt rather than fabricating one.

The receipt factory's acceptance gating is already complete — it refuses
non-completed runs, non-accepted acceptance, incomplete coverage, an invalid
result hash, any untrusted or not-run check, a coverage/acceptance mismatch and
a profile set that does not contain the report profile, and the writer refuses
to overwrite. So **no new acceptance logic is needed**. What is missing is
producers for the caller-supplied inputs — the checker identity hash, the input
identity hash and the accepted-profiles set — and a single emission branch.
A tracked-inventory hash already exists and is the natural source for the input
identity; the checker identity has no defined semantics anywhere and is an
author decision.

**Must this close before later phases?** No. The roadmap records it as
non-blocking and there is an executed author precedent. The qualification: a
later phase can produce *acceptable* closeout evidence without it, but cannot
produce *canonical machine* closeout evidence, because closeout cannot be
invoked at all without a receipt file. Every such phase will otherwise repeat
the same manual author-disposition record.

**Known couplings that must move in the same candidate.** The packaging script's
SHA-256 is pinned by the static-contract catalog, the registry pins that
catalog, and the registry check fails closed for *every* profile if either
drifts; two existing desktop tests assert the current wording in living
documents. The receipt slice additionally touches verification-core nodes, which
is the reason FULL is required.

### `PRE-G9B-R1-R1` — native periodic-quarantine round-trip closure *(proposed; requires `AD-R0-3`)*

| Field | Value |
|---|---|
| Objective | Supply the missing real native `.cedg` lifecycle evidence for `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP`, and reconcile the overclaimed `U1-Q02` manifest binding |
| Layer | Desktop test + operational manifest; no kernel semantics |
| Prerequisites | author disposition to close in this track rather than at global G9 closeout |
| Normative work | none; the closure fixture is already specified in the G9U1 command/tool consistency matrix |
| Productive scope | two additional test methods in the existing periodic-quarantine native test — proved-nonzero-offset retirement, and the feature-off/Classic preservation boundary — each forking a byte-identical reopened archive; a second unresolved quarantined round trip to satisfy the declared `U1-Q02` procedure; manifest reconciliation for `U1-Q02`, `U1-Q04`, `U1-Q05` |
| Forbidden scope | XML fabrication; ledger-only export/import as a substitute; any change to quarantine semantics, the resolver or the ledger format; moving the canonical risk record to resolved |
| Acceptance criteria | the declared `U1-Q02` procedure is actually executed by the test bound to it; `U1-Q04` and `U1-Q05` are executed; every assertion goes through a real save/reopen of a real `.cedg` in a fresh application |
| Focal tests | the two new methods plus the reconciled `U1-Q02` path, dispatched by the existing G9U1 verifier |
| Verification profile | `VERIFICATION_CLASS = BOUNDED_PHASE`; `tools/agent/verify.ps1 -Profile PHASE -Phase G9U1` |
| Author smoke | not required |
| Stop conditions | the quarantine observation surface cannot be reached without widening a deliberately resolver-scoped kernel API — that widening is its own author decision, not a refactor |
| Does not authorize | resolving the risk record; global G9 closeout; any kernel API widening without a separate decision |

**Design note.** The closure fixture is mostly implemented already. An existing
Desktop test builds a genuinely double-covered periodic locus, materializes four
points each consuming its own exact token, drives the parameter into claimed
quarantine, saves to a real `.cedg`, reopens in a fresh application and asserts
survival, emptiness of the session surfaces, unchanged point identity and
identical selected tokens, then establishes offset zero and asserts reactivation
of the same points. It is dispatched by the G9U1 verifier — in the non-skip-build
branch. The residual is genuinely small, but it is three items, not two:

- `U1-Q04` (proved-nonzero quarantine retirement) and `U1-Q05` (quarantine
  preservation boundary under a feature-off GeoCeDG host and the Classic
  diagnostic route) are honestly declared retained-risk gaps with no bound
  tests. The nonzero-offset retirement branch that `U1-Q04` must exercise sits
  immediately above the release branch in the public root identity resolver;
- **`TD-R0-G9U1-Q02-BINDING` — a new finding.** `U1-Q02` *is* bound to the
  existing test, but its declared procedure — reopen the native quarantined
  document **twice** while offset evidence remains insufficient — is not
  performed by that test, which does exactly one quarantined round trip before
  resolving the offset. This is an overclaimed manifest binding, a different
  kind of item from an honestly declared gap, and it must be reconciled in its
  own right rather than folded into "two more test methods".

The byte-identical seed-forking pattern the fixture requires already exists in
an earlier native-archive test, where one seed document is written once and then
loaded independently by two separate applications. A cleaner,
layout-independent evidence style — asserting ledger-state equality rather than
hand-parsing the serialized string — already exists in the spline-pair native
archive test and should be preferred, because the current helper parses the
serialized ledger by hand and is therefore coupled to field layout.

One real design decision is exposed: the quarantine mechanism is complete in the
kernel, but no quarantine-observation surface is reachable from the Desktop
module. The status enum, the rebase entry and the typed
`isPeriodicallyQuarantined` accessor are private or package-private and
documented as exposed only to the resolver. A `Q04`/`Q05` test can drive the
experiments black-box, but must either repeat the string parse or widen a
deliberately resolver-scoped kernel surface — which is an author decision, not
a neutral refactor.

`R1-R1` supplies evidence and a disposition proposal only. Only the author may
move the canonical risk record to resolved.

### `PRE-G9B-R2` — semantic metric endpoint provenance

| Field | Value |
|---|---|
| Objective | Close `TD-P1-SEMANTIC-LENGTH-INTERSECTION` by admitting a third, explicitly typed metric endpoint provenance family |
| Layer | shared Java kernel |
| Prerequisites | author authorization; a normative amendment to `geocedg/specs/locus/locus-v2-metrics.md` defining the third family; an ADR if the endpoint contract is judged a decision rather than an extension of an accepted one |
| Normative work | **required before implementation**: the endpoint family definition, its currentness rule, its ambiguity rule, and its persistence/reopen behaviour |
| Productive scope | a resolver family that walks point → intersection-point algorithm → rich result and exact token → admissible solution → per-side evidence whose locus identity and semantic revision match the addressed source → semantic position → the existing position binder, following the established in-kernel walk and its fail-closed idiom |
| Forbidden scope | any Cartesian, proximity, tolerance, label, order or index resolution; any change to intersection materialization admissibility; any change to `Length`/`LocusLength` semantics beyond admitting the new endpoint family; any widening of D1 |
| Acceptance criteria | `Length(S,P,Q)` and `LocusLength(S,P,Q)` are defined when an endpoint is a current, materialized point of a supported semantic intersection on `S`; every ambiguous, stale, dormant or non-matching case fails closed to `INVALID_QUERY`; existing metric regressions are unchanged |
| Focal tests | unambiguous side selection for `S`; currentness (ledger token validity plus semantic-revision equality); token retention and reactivation via the dormant path; copy and reopen, including the single authorized closure-copy rebase; self-intersection `S × S`, where both sides carry the same identity and side selection is genuinely ambiguous; source A/B ordering including the reversed view; duplicate-token ambiguity; redefine; persistence and reopen; plus the two existing coincident-free-point negative regressions. No existing test combines an intersection-materialized point with `Length`/`LocusLength` |
| Verification profile | `VERIFICATION_CLASS = INTEGRATED_PHASE`; `PHASE -Phase G9S1-R1` and `PHASE -Phase G9U0-R6`, plus `INTEGRATION`. Escalate to `GLOBAL_IMPACT`/FINAL by explicit request if the change reaches shared metric infrastructure beyond the resolver |
| Author smoke | recommended: the author's own witness construction plus a partial `Length` on a spline-pair intersection endpoint |
| Stop conditions | the side corresponding to `S` cannot be selected unambiguously in the `S × S` case without a new rule — that rule is normative work, not an implementation choice; any design that needs a coordinate comparison |
| Does not authorize | D1's extension; `R3`–`R7`; any new intersection capability |

**Design anchor for the no-retarget guard.** The shape already exists for spline
constructor occurrences: a composite occurrence key of addressed source, root
source, constructor, slot and point durable identities under an explicit version
tag. An intersection-endpoint key should mirror that shape rather than invent
one.

### `PRE-G9B-R3` — redefine correctness

| Field | Value |
|---|---|
| Objective | Remove the false `AMBIGUOUS` verdict (D3-a) and separate the collapsed status vocabulary (D3-b) |
| Layer | shared Java kernel; plus Desktop presentation for surfacing the kernel reason |
| Prerequisites | author authorization; a decision on which durable dependency projection becomes canonical, and on whether persisted signatures change (`AD-R0-6` also fixes the verification class) |
| Normative work | write the durable dependency projection rule, which no document currently states; update the compatible-redefine design note; an ADR if the canonical projection changes observable behaviour or persisted signatures |
| Productive scope | one durable dependency projection shared by publication and assessment; a typed status set that distinguishes a genuine contract change, an undescribable proposal and a real ambiguity; surfacing the kernel reason in the desktop dialog |
| Forbidden scope | any global weakening of ambiguity detection; promoting ordinary construction elements to durable identities; any change to cycle detection, stable-role completeness, the staged-dependency closure check, atomicity, the host-state seal, the final currentness authorization or XML rollback; any frontend authority to upgrade a status |
| Acceptance criteria | redefining `E` to `Midpoint(D, C + (1, 0))` is assessed as advanced retain available, commits as `RETAIN` with a definition change, preserves `E`'s durable identity and the `a → E` edge, and survives undo/redo and save/reopen; redefining `E` to its identical current definition is a no-op; a family change is reported as a family change, not as ambiguity; every case the detector rejects correctly today is still rejected |
| Focal tests | the witness case as a minimal derived fixture; identical-definition no-op; a candidate whose durable closure genuinely changes, still classified as a topology change; a genuine durable-contract change, still failing closed; a real cycle, still `INVALID_DAG`; an incomplete stable-role group, still failing closed; undo/redo; persistence and reopen; one test per distinct former-`AMBIGUOUS` cause; plus the first kernel assertions of the status itself, which has no coverage today |
| Verification profile | `VERIFICATION_CLASS = INTEGRATED_PHASE` (recommended); `PHASE -Phase POST-G9U1-A3` plus `INTEGRATION`. If the fix must also change the publication projection or persisted signatures, escalate to `GLOBAL_IMPACT`/FINAL by explicit request, since that is shared kernel infrastructure with a migration consequence |
| Author smoke | required: the author's own witness construction, redefining `E` and confirming `M`, `a`, `e`, `g` and `I`–`L` behave as expected after undo/redo and reopen |
| Stop conditions | the transitive projection cannot be made identical on both sides without changing persisted signatures — a persisted-signature change is a migration decision, not an implementation detail |
| Does not authorize | `R4`–`R7`; any change to intersection, metric or spline semantics |

### `PRE-G9B-R4` — `SplineV2` and list authoring UX

| Field | Value |
|---|---|
| Objective | Give `SplineV2` real mode semantics and correct contextual help; add an ordered "Create List from Selection" tool |
| Layer | Desktop/frontend and localization resources; **no kernel change for the open forms**, subject to `AD-R0-8` |
| Prerequisites | author authorization; resolution of the closed-form authority question (`AD-R0-8`) |
| Normative work | a UI specification under `geocedg/specs/ui/` for the two tools, consistent with the existing workspace/interaction specs |
| Productive scope | a `SplineV2` mode constant, mode text, tool/help localization in the default, `_en` and `_es` bundles; the profile action converted to `upstream-mode` with an audited numeric id; a controller collection branch with preview; the ordered list tool; correction of `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX`; optionally, moving `SplineV2` to the geometry command table |
| Forbidden scope | any spline mathematics, degree admissibility, span/knot computation, closedness detection or continuity logic in the frontend; a new CeDG-specific list semantic type; requiring a visible auxiliary list; any change to `CmdSplineV2` argument semantics; synthesizing a repeated closing member without the `AD-R0-8` decision |
| Acceptance criteria | activating the tool, selecting ordered points and finishing produces one construction commit and one undo step with degree 3; an explicit degree change is a separate gesture; contextual help describes `SplineV2`, not the previously active tool; the input-help panel opens with `SplineV2` selected and shows the existing syntax; the list tool produces an ordinary `GeoList` in click order |
| Focal tests | ordered collection and commit; cancel; undo/redo; the three-point minimum; degree default and explicit change; the over-large point set failing through the model guard with the localized message; mode text and help-key resolution; list ordering, duplicates, mixed types, cancel, undo, downstream dependency and member deletion/redefinition; the corrected tooltip matching an actually accepted form |
| Verification profile | `VERIFICATION_CLASS = BOUNDED_PHASE`; a new `PHASE` selection registered for `PRE-G9B-R4` |
| Author smoke | required: build a spline from ordered points with the tool and with an explicit degree, and build a list from selection and feed it to `SplineV2` |
| Stop conditions | any requirement that would place spline mathematics in the frontend; the closed-form authority question still undecided |
| Does not authorize | `R5`–`R7`; any kernel or command change; any change to Locus V2 tools |

### `PRE-G9B-R5-A` — user-guide index, outline and navigation

| Field | Value |
|---|---|
| Objective | Give the bilingual guide a derived table of contents, working anchors, a left navigation tree and practical scroll synchronization |
| Layer | Desktop help presentation, documentation convention and an operational structural audit |
| Prerequisites | author authorization |
| Normative work | extend the documentation maintenance contract with the anchor derivation rule |
| Productive scope | anchor emission before each heading in the renderer; the renderer returning a structural outline alongside the HTML; a split-pane window with a navigation tree; scroll-position synchronization; a STATIC-tier structural diagnostic asserting EN/ES alignment |
| Forbidden scope | two independently maintained manual indexes; a separate JSON/YAML outline resource; slugifying translated heading text; any transformed packaged artifact — the Markdown stays the single authored source and the packaged copies stay byte-identical; any hyperlink listener, which an existing test forbids |
| Acceptance criteria | every heading has a deterministic, language-independent anchor derived from the existing stable section markers and the decimal numbering; the tree matches document order; selecting a node scrolls to the heading; the EN/ES structural vectors are equal and are checked automatically; the packaged-copy byte-identity test still passes |
| Focal tests | anchor vector equality and uniqueness across editions; outline construction; existing renderer parity tests unchanged; the new structural diagnostic |
| Verification profile | `VERIFICATION_CLASS = BOUNDED_PHASE`; a new `PHASE` selection, plus `STATIC` for the diagnostic |
| Author smoke | required: open the guide in both languages and navigate |
| Stop conditions | a design that requires nested lists or in-body links, which are outside the renderer contract; any need to change the authored guide structure to make navigation work |
| Does not authorize | `R5-B`; any change to guide content or to the packaged resource contract |

**Why the sources are the asset, and the exact anchor rule.** The two editions
are already structurally aligned — identical heading-level sequences, identical
decimal numbering prefixes and identical ordered stable section identifiers — and
neither contains a single Markdown link or nested list. Anchors are therefore
computable, never authored, and never derived from translated prose.

The rule must account for one detail: there are 17 section markers but only 16
level-2 headings, because the **first marker sits on the level-1 title**. The
invariant that actually holds in both editions is: *every marker is immediately
followed by an h1 or h2, and every h1/h2 is immediately preceded by a marker.*
The level-1 heading therefore takes the first marker's identifier; inventing a
synthetic root identifier would orphan an authored, parity-tested id. Level-3
headings take the enclosing marker id plus the decimal numbering token, which
diffs clean across editions.

One latent renderer detail to respect: an indented list line following an open
list item is currently absorbed as a wrapped continuation of that item, with its
marker left visible. Both guides contain zero nested lists, so this is latent
rather than live — but it is another reason an in-document TOC written as an
indented list is out of contract.

### `PRE-G9B-R5-B` — canonical English command surface *(requires `AD-R0-4`)*

| Field | Value |
|---|---|
| Objective | Present command names canonically in English independently of UI language, while keeping localized names as accepted input aliases |
| Layer | **shared kernel localization seams** plus Desktop presentation; **not** the parser and **not** serialization |
| Prerequisites | author authorization **and** a separate compatibility decision recorded as an ADR |
| Normative work | an ADR covering the display/insertion/documentation policy, the alias-acceptance policy, and the localized-name clash hazard |
| Productive scope | a canonical display-name mechanism routed through the English-name resolver and applied at **every** command-head print site; an English syntax path that actually resolves syntax keys on the JRE/desktop client; English heads in the desktop completion list; the script-editor display policy; the policy-derived Algebra prefix literal; guide and matrix updates |
| Forbidden scope | changing the command lookup strategy; changing XML/file parsing or the serialization template; changing the reverse command table or removing localized entries; reusing the existing localize-commands template flag as the naming switch |
| Acceptance criteria | GeoCeDG displays, suggests, inserts and documents commands in English in every UI language, with **no mixed-language output**; localized names still parse in the Algebra input; construction XML is unchanged; historical Spanish-UI documents open and recompute identically; scripts remain canonical English/internal |
| Focal tests | English display under an ES UI across every print site; localized alias still accepted; round trip of a historical ES-authored document; definition presentation; autocomplete insertion; syntax-help resolution on the desktop path; script editor round trip; the documented clash class |
| Verification profile | `VERIFICATION_CLASS = INTEGRATED_PHASE`; `INTEGRATION`, because the change crosses shared localization, presentation and existing localization regressions |
| Author smoke | required in both EN and ES UI |
| Stop conditions | the clash class cannot be made safe without changing parsing — that is a separate compatibility decision and must stop the phase |
| Does not authorize | `R6`–`R7`; any parser or serialization change; removing any localized command name |

**Four facts that shape this slice, and one that enlarges it.**

1. English and internal command names are *already accepted* in the Algebra input
   in every locale, because the reverse lookup falls back to the internal table.
   The "aliases remain accepted" half needs no new mechanism.
2. The correct canonical resolver is the **English** name, not the internal name.
   Several commands have internal names that differ from their English names, so
   switching off localization naively would display internal identifiers. That
   is the single largest naive-application failure.
3. The three GeoCeDG-added commands are `LocusV2`, `LocusLength` and `SplineV2`.
   Spanish names exist for the first two and not for the third, which is itself
   part of the inconsistency this slice removes.
4. **This is not a frontend-only change.** Two findings enlarge it. First,
   command heads are printed at roughly a dozen sites beyond the two obvious
   ones — including a GeoCeDG-owned kernel adapter, a vector wrapper, stroke
   loci, CAS cells, chart and axis objects, and several function-head sites in
   the expression serializer — so a hook placed at only two sites would produce
   mixed-language output. Second, the desktop syntax-help path is not
   selectable: the localization object holds a single final command-syntax
   instance with a getter and no setter, so routing desktop syntax help through
   an English provider requires a shared-kernel change. The existing English
   syntax class is also **not** a drop-in: it overrides the name resolver used
   for *both* the head and the syntax-bundle key, and on the JRE client the
   English resolver is a pure enum mapper that returns the bundle key verbatim,
   so the "syntax" degrades to the literal key and is then rejected as command
   not found. It works on the web client only because that client overrides the
   English resolver to read the English bundle.

Consequently `R5-B` must be planned as a shared-kernel localization change with
a desktop consumer, not as a presentation tweak. An existing localization
regression asserting the Spanish display names is a deliberate barrier that the
ADR must decide to amend.

### `PRE-G9B-R6` — GGBScript compatibility gate

| Field | Value |
|---|---|
| Objective | Prove, reproducibly, that GeoCeDG commands work through the real script path, and record the result as machine-readable authority |
| Layer | verification/tooling, plus any product corrections the gate exposes |
| Prerequisites | author authorization; `R5-B` decided, because the locale dimension of the matrix depends on the naming policy |
| Normative work | a capability-matrix schema and its instance contract, following the existing schema conventions |
| Productive scope | the capability matrix and its executing tests; correction of `TD-R0-GGBSCRIPT-STRATEGY-RESTORE`; any truthful-failure corrections the gate exposes |
| Forbidden scope | adding GGBScript functionality; changing command semantics to make a row pass; declaring a second command registry; asserting geometric values not already owned by a normative spec |
| Acceptance criteria | every GeoCeDG-added and GeoCeDG-modified command has rows covering name lookup, processor dispatch, DAG construction, recompute, undo where relevant, native save/reopen and truthful failure, under at least EN and ES UI locale, on the Algebra-input, GGBScript and nested-`Execute` surfaces; each unsupported-but-truthful verdict carries an error key and a before/after XML-identity assertion; adding a command without adding rows fails validation |
| Focal tests | the matrix-executing tests; the strategy-restore regression; the locale dimension; the truthful-failure row for a swallowed throwable |
| Verification profile | `VERIFICATION_CLASS = OPERATIONAL_VERIFICATION_INFRASTRUCTURE`; focused operational evidence plus the registered phase selection; FULL only if the matrix is wired into global verification infrastructure |
| Author smoke | recommended: run a representative script under an ES UI |
| Stop conditions | a command that cannot be reached from the script path — that is a product defect and needs its own authorization |
| Does not authorize | `R7`; any new command; any script-language extension |

**What is already known.** The script path and the Algebra-input path converge on
exactly one command dispatcher instance and one processor factory, so GeoCeDG
commands are structurally reachable from scripts. The GeoCeDG desktop Algebra
input **wraps** the identical processor entry point — it does not bypass it —
adding a redefine branch and, on the creation branch only, post-creation
frontend orchestration and a single undo store. The real differences the matrix
must record are:

- **name lookup**: the script strategy accepts English or internal,
  case-insensitively; the user strategy accepts localized, English or internal,
  and reaches the localized reverse table. Scripts are laxer on case and
  stricter on language;
- **locale exposure**: scripts are *stored* internal, because the script editor
  delocalizes on save and re-localizes on display, so a script authored in a
  Spanish UI runs later under an English UI. The exposure is the reverse — a raw
  localized token that reaches the runner without passing through
  delocalization (pasted text, strings built at runtime, XML-loaded scripts)
  cannot resolve, because the Spanish bundle names `LocusV2` differently and the
  internal lookup never sees that name;
- **evaluation flags, errors and undo**: scripts run without sliders, without
  the degree flag, with a script error handler that refuses undefined-variable
  creation and returns no current command, and with undo suppressed per line;
- **a third regime**: nested `Execute` forces the XML strategy, so it is neither
  the script nor the user regime;
- **silent failure** (`TD-R0-GGBSCRIPT-STRATEGY-RESTORE`): the runner catches
  `Throwable` per line and degrades success only when the processor returned
  null, so an escaping throwable can leave a script reporting success. The same
  method also restores the previous lookup strategy outside any `finally`, so an
  escaping throwable can leave the kernel in the script strategy;
- **frontend orchestration asymmetry**: the opt-in auto-materialize session
  behaviour exists only on the explicit human Algebra path and is **off by
  default**. It is therefore a declared asymmetry the matrix must record, and it
  is *not* an explanation of the D1 witness — the witness shows no
  materialization asymmetry that it could explain.

Existing coverage is better than "prose only": the script path already executes
all three GeoCeDG commands, and the feature-off atomic-rejection row is
mechanized with an XML-identity assertion. What is genuinely unmechanized is the
locale dimension, the truthful-failure row for a swallowed throwable, the undo
row and the rich-result/auto-materialize row.

**Proposed matrix shape.** Row identity
`(command_id, arity_form, entry_surface, ui_locale)` with surfaces
`ALGEBRA_INPUT | GGBSCRIPT | GGBSCRIPT_EXECUTE` and locales `en | es`;
`command_id` must be a kernel command enum constant, never redeclared, and
`arity_form` must quote a syntax line from the command bundle. Each capability
field carries a typed verdict plus a JUnit identity. Additional fields record the
lookup strategy in force, the expected error key, and whether frontend
orchestration is expected on that surface. Reproducibility comes from four
properties already used in this repository: every row names a JUnit identity and
the matrix is registered so a verification level executes it; the row set is
derived structurally from the command inventory crossed with surfaces and
locales, so adding a command without rows fails validation; inputs are pinned by
hash in a static-contract-style catalog; and every unsupported verdict carries
both an error key and an XML-identity assertion.

### `PRE-G9B-R7` — integrated PRE-G9B readiness and closeout

| Field | Value |
|---|---|
| Objective | Integrate the track, reconcile the roadmap and debt records, and place an explicit author decision about what follows |
| Layer | verification, documentation and roadmap |
| Prerequisites | every authorized earlier phase closed as `PASS — AUTHOR APPROVED` |
| Normative work | roadmap and traceability reconciliation only |
| Productive scope | integrated verification of the accumulated candidate; reconciliation of every debt record opened, retained or closed by the track; the readiness statement |
| Forbidden scope | any new capability; any publication, tag or release; moving any risk record to resolved without an explicit author decision; authorizing `G9B` |
| Acceptance criteria | every debt item has a recorded terminal disposition or an explicit retention with an owner; the roadmap states the exact status of each phase; nothing is marked approved by the agent |
| Focal tests | the accumulated focal suites of the authorized phases |
| Verification profile | `VERIFICATION_CLASS` frozen at phase start from the actual integrated scope; `INTEGRATION` at minimum, `FINAL` if any authorized phase changed global verification infrastructure |
| Author smoke | required |
| Stop conditions | an open debt item with no owner; an unresolved author decision from the list in section 6 |
| Does not authorize | `G9B`, `G9C`, `G9U2`, productive `G10`, further `G12`, `PROFILE COMMERCIAL`, exact DXF `SPLINE`, publication, or contact with GeoGebra/OpenGeoProver |

## 5. Dependency classes

Per the documentation maintenance contract, the three dependency classes are
recorded separately and must not be conflated.

**Hard semantic or contract dependencies**

- `R2` depends on a normative metric endpoint amendment existing first.
- `R3` depends on a decision about which durable dependency projection is
  canonical, and on whether persisted signatures change.
- `R5-B` depends on an accepted compatibility ADR.
- `R6`'s locale dimension depends on `R5-B`'s outcome.
- `R1`'s receipt slice depends on author definitions for the receipt inputs that
  have no producer.
- `R4`'s closed-form scope depends on `AD-R0-8`.
- Nothing in this track is a hard dependency of `G9B`. `G9B`'s only phase
  dependency remains the author-approved closure of `G9A3` and the primitive
  contract.

**Recommended execution predecessors**

- `R1` first, so later phases can produce canonical closeout evidence.
- `R1-R1` early, because it is cheap and because the risk otherwise propagates
  into every `G9B` family stage.
- `R3` before `R4`, so the authoring work lands on a correct redefine path.
- `R5-B` before `R6`.

**Global / release gates**

- `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` requires resolution or explicit
  author disposition before global `G9` closeout. It is not a `G9B` entry gate.
- Publication, tag, release and `PROFILE COMMERCIAL` remain separate author
  dispositions, untouched by this track.

## 6. Open author decisions

| ID | Decision required | Blocks |
|---|---|---|
| `AD-R0-1` | Whether a mixed `LocusV2 × SplineV2` materialization extension is wanted at all. If yes, it is a new capability requiring a new ADR amending ADR 0021 Decision 1 and a normative spec amendment — not part of `PRE-G9B-R` | any D1 change |
| `AD-R0-2` | Semantics and producers for the receipt inputs that have none today — the checker identity hash, the input identity hash and the accepted-profiles set | `R1` receipt slice |
| `AD-R0-3` | Close `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` in this track (`R1-R1`) or retain it for global `G9` closeout; and, if closing, whether to widen the resolver-scoped quarantine observation surface | `R1-R1` |
| `AD-R0-4` | Adoption of the canonical English command-name policy, plus the compatibility decision covering localized aliases, the name-clash hazard, and amendment of the existing Spanish-display regression | `R5-B`, `R6` locale dimension |
| `AD-R0-5` | Whether the invalid `SplineV2` tooltip is corrected immediately as a bounded change or waits for `R4` | `R4` scope |
| `AD-R0-6` | `R3`'s frozen verification class, and whether a persisted-signature migration is acceptable | `R3` |
| `AD-R0-7` | Whether a durable machine-readable technical-debt ledger is created, and where | roadmap hygiene |
| `AD-R0-8` | Whether the desktop may synthesize the repeated closing member that declares a closed periodic `SplineV2`, given that closedness is kernel state with no public parameter | `R4` |
| `AD-R0-9` | Whether to authorize a separate governance-layer task that adds `PROPOSED / UNEXECUTED / NOT AUTHORIZED` prompt stubs for `R1`–`R7`. `PRE-G9B-R0` did **not** create them: `CLAUDE.md` prohibits editing `.github/prompts/**` as a side effect of another task. Every per-phase contract field such a stub would carry is recorded in section 4 above, so no authority is missing — only the prompt files | prompt-layer hygiene only |

## 7. Authorization state

```text
PRE_G9B_R_TRACK              = DESIGN CANDIDATE — PENDING AUTHOR REVIEW
PRE_G9B_R0_EXECUTED          = true (characterization/planning only)
PRE_G9B_R0_AUTHOR_APPROVED   = false
PRE_G9B_R1_AUTHORIZED        = false
PRE_G9B_R1_R1_AUTHORIZED     = false
PRE_G9B_R2_AUTHORIZED        = false
PRE_G9B_R3_AUTHORIZED        = false
PRE_G9B_R4_AUTHORIZED        = false
PRE_G9B_R5A_AUTHORIZED       = false
PRE_G9B_R5B_AUTHORIZED       = false
PRE_G9B_R6_AUTHORIZED        = false
PRE_G9B_R7_AUTHORIZED        = false
D1_EXTENSION_AUTHORIZED      = false
G9B_AUTHORIZED               = false
G9C_AUTHORIZED               = false
G9U2_AUTHORIZED              = false
G10_PRODUCT_IMPLEMENTATION_AUTHORIZED = false
FURTHER_G12_AUTHORIZED       = false
PUBLICATION_AUTHORIZED       = false
selfApproved                 = false
```
