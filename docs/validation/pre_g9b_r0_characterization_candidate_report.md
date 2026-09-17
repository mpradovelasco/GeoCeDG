# PRE-G9B-R0 — characterization, technical-debt disposition and staged design

```text
PRE-G9B-R0                = PASS — AUTHOR APPROVED (author decision, 2026-09-17)
REVIEW DISPOSITION        = AUTHOR APPROVED WITH REQUIRED PLANNING CHANGE, applied
PHASE KIND                = CHARACTERIZATION / DEBT DISPOSITION / STAGED DESIGN
ROADMAP GATE EXECUTED     = POST-P1 ROADMAP / TECHNICAL-DEBT DISPOSITION
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = DOCUMENTATION_STATUS_ONLY (frozenAtPhaseStart = true)
PRODUCT_PHASE_EFFECT      = NONE
D1                        = INTENTIONALLY UNSUPPORTED CURRENT CAPABILITY (not a defect,
                            and not technical debt) — classification UNCHANGED by review
D2                        = MISSING FRONTEND EXPOSURE AND INCORRECT CONTEXTUAL HELP
D3-a                      = INCORRECTLY REJECTED (kernel)
D3-b                      = INCORRECTLY REPORTED (status vocabulary)
DEBT RETAINED             = 4 pre-existing + 1 open risk
DEBT OPENED BY R0         = 4
SEQUENCE                  = R1, R1-R1, R2, R2-E0, R2-E1, R3, R4, R5-A, R5-B, R6, R7
                            — every phase after R0 DESIGNED / NOT AUTHORIZED
AUTHOR DECISIONS          = AD-R0-1 … AD-R0-9 all decided
selfApproved              = false
authorApproved            = true
passClaimed               = true   (author decision; not an agent claim)
```

`PRE-G9B-R0` is a planning, characterization and documentation phase. It changes
no kernel, geometry, dependency graph, serialization, semantic identity, metric,
intersection, redefine, command, script, DXF, packaging or version behaviour. It
marks no specification or ADR as approved, and it authorizes no later phase —
including `R2-E0` and `R2-E1`, which the author's review added to the track but
did **not** authorize for execution.

Governing artifacts produced by this phase:

- [PRE-G9B-R staged design](../architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
- [technical-debt disposition and observation matrix](pre_g9b_r0_technical_debt_disposition_matrix.md)
- [machine-readable disposition](../../geocedg/validation/pre-g9b-r0/pre-g9b-r0-disposition.json)
- the `PRE-G9B-R` track section in the [living roadmap](../roadmap/geocedg_roadmap.md)

## 1. Repository and state audit

| Fact | Value |
|---|---|
| `HEAD` at entry | `0d13434cf1ab4d31f4bb4d50e7aea6cb3e69e35d` |
| Tree at entry | `922034fd46411530bd02ae15fcc832e3fc541dc7` |
| `main` at entry | `0d13434cf1ab4d31f4bb4d50e7aea6cb3e69e35d` |
| `origin/main` at entry | `0d13434cf1ab4d31f4bb4d50e7aea6cb3e69e35d` |
| Divergence | none; `HEAD == main == origin/main` |
| Worktree / index at entry | clean |
| Working branch | `planning/pre-g9b-r0-characterization-and-debt-disposition` |

The branch name follows the roadmap's own branch strategy, in which
`planning/<capability>` denotes explicit prior planning. The single existing
planning-only precedent in the repository is `planning/g10p-study-optimization`.

**Declared roadmap state at entry.** Living roadmap version 4.06, dated 16
September 2026. Last closed phase `PRE-G9B-P1 = PASS — AUTHOR APPROVED`. Next
gate `POST-P1 ROADMAP / TECHNICAL-DEBT DISPOSITION`, explicitly "acción de
planificación, no autorización productiva", whose purpose is to decide where the
four open debts are resolved and how they relate to
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP`, and only then which productive
gate follows. It does not presume that the next implementation is `G9B`.

**Identifier reconciliation.** The string `PRE-G9B-R0` did not previously exist
in the repository's tracked content. It is introduced here as the phase
identifier that *executes* the already-declared POST-P1 gate, not as a new gate.
The family letter `R` (readiness/remediation) is introduced on the same basis as
the existing `S`, `D` and `P` families: it does not occupy a `G9` semantic
identifier, and no `R` identifier is yet a registered verification phase or an
implementation authorization.

**Applicable authority consulted.** `AGENTS.md`;
`.github/prompts/canonical/governance.prompt.md` and
`verification.prompt.md`; `.github/prompts/tasks/task-template.prompt.md`;
`geocedg/specs/operations/verification-levels.md`,
`verification-registry.json`, `verification-static-contracts.json`,
`verification-receipt.schema.json`, `prompt-contracts.json` and
`documentation-maintenance.md`; `geocedg/specs/curves/semantic-spline-2d.md`
and `spline-v2-pair-materialization.md`; `geocedg/specs/locus/*`;
ADRs 0008, 0009, 0013, 0014, 0016, 0017, 0018, 0019, 0020, 0021, 0022, 0025,
0026, 0027; the PRE-G9B stabilization design; the POST-G9U1-A3-R1
compatible-redefine design; the G9U0-R4 admissibility design; the G9S1-R1
pair-sheet contract; the G9U1 command/tool consistency matrix; and current
source under `source/shared/common`, `source/shared/common-jre`,
`source/desktop/desktop`, `tools/agent`, `tools/release` and `apps/geocedg`.

**Known debt state at entry**, verbatim from the roadmap table:

| ID | Origin | Type | State |
|---|---|---|---|
| `TD-P1-SEMANTIC-LENGTH-INTERSECTION` | P1-D1, P1 author smoke | kernel semantic / provenance / metric endpoint | `OBSERVED — REQUIRES CHARACTERIZATION/DESIGN` |
| `TD-P1-PACKAGING-SUMMARY` | P1-D2, P1 author smoke | external tooling / reporting | `OBSERVED — SMALL BOUNDED MAINTENANCE` |
| `TD-VERIFY-RECEIPT-RECOVERY` | pre-P1 | verification infrastructure | `PRE-EXISTING — NON-BLOCKING` |
| `TD-G9X1-HISTORICAL-PIN` | pre-P1 | verification / historical authority reconciliation | `PRE-EXISTING — NON-BLOCKING` |
| `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` | G9U0-R4 | kernel risk | `OPEN / TRACKED` |

## 2. Author witness

| Fact | Value |
|---|---|
| Path | `artifacts/author-input/post-p1-defects/SeveralDefects.cedg` |
| SHA-256 | `c0cbdb8ea218e4a07b7cd4d7bcfb4ba2b74da373bbd49711aa4657349619dff8` |
| Size | 24 844 bytes |
| Versioned | no — `artifacts/` is git-ignored except its README |
| Modified by this phase | no |
| Derived fixtures created | no — R0 is documentation-only; reproduction procedures are specified for the owning phases |

The archive was inspected read-only by extracting `geogebra.xml` into the
session scratchpad. The original was never opened for writing, moved, renamed or
promoted into versioned authority.

### 2.1 Decoded construction

```text
A, B                free points
c  = Circle(A,B)                    conic,   geo:d5d14e14b01f806756df070298fe4d1e
C  = Point(c)                       point,   geo:0ec40b8a27f0fcbfc125079d03bd8e6d
f  = Line(C, xAxis)
D  = Intersect(f, yAxis)            point,   no durable identity
E  = Midpoint(D, C)                 point,   geo:116a0f9adabcd515e0ef728b9ac5fe81
a  = LocusV2(E, C)                  locusv2, geo:75930c9f360aea5f9544518579b34705
F, G, H             free points
l1 = {F, G, H}                      list,    geo:0bd2fff528b7575f92d510aa44ad3419
b  = 3                              numeric, geo:64242749513467590704cd98ca6d1e23
d  = SplineV2(l1, b)                locusv2, geo:af7727d47fbd9456fe580de49d3bd231
e  = Intersect(d, a)                locusintersectionresult, geo:023cf87a1b102bbb529452ba53eb533c
g  = Intersect(a, c)                locusintersectionresult, geo:c8fddcdb1612ae92d7c78a7a466b8841
text1..text4        exact selection tokens, "locus-root/v3/..."
I,J,K,L = Intersect(g, textN)       points materialized from g
M  = Midpoint(D, C + (1, 0))        point,   no durable identity
```

Two facts from the file are load-bearing for the characterization below and are
worth stating plainly. First, `M` demonstrates that `Midpoint(D, C + (1, 0))`
parses, resolves and constructs correctly **as a new object** — so D3 cannot be
a parsing, overload-resolution or expression-evaluation problem. Second, `D` has
no `geocedgId` while `E` does, and `E`'s persisted identity record lists only
`C` as a dependency — so the durable dependency projection demonstrably drops
non-participating inputs at publication time.

## 3. D1 — `LocusV2 × SplineV2` intersection materialization

**Classification: `INTENTIONALLY_UNSUPPORTED_CURRENT_CAPABILITY`. This is not a
bug.** It is a deliberately unsupported materialization boundary, and the
observed behaviour is exactly what the approved contracts prescribe.

### 3.1 Producers and semantic families

`a = LocusV2(E, C)` takes two points, so the command routes to the point-driven
public operation and builds the dependent-point locus algorithm, which publishes
a **reconstructible locus evaluator** — a deterministic numeric dependency
evaluator with no exact coefficient authority.

`d = SplineV2(l1, b)` routes to the spline public operation and builds the
spline algorithm, which publishes a **spline semantic evaluator** implementing
the piecewise-polynomial capability. Its authenticated provenance is threefold:
the stable branch key, the branch provenance string
`semantic-spline-v2/piecewise-polynomial/v1|degree=…|spans=…`, and the evaluator
signature `spline-v2-evaluator/v2|branch=…|<model signature>`. The kernel
authenticates primarily by **evaluator class identity**, not by parsing those
strings — although two non-presentational kernel sites additionally discriminate
spline from generic by parent algorithm, which a future contract would have to
reconcile.

Both produce the identical runtime class `GeoLocusV2`. There is no distinguished
subtype.

### 3.2 The rich result actually produced

`Intersect(d, a)` — both `GeoLocusV2` — dispatches to the locus-pair
intersection algorithm and produces a `GeoLocusIntersectionResult`, the
`locusintersectionresult` type visible in the witness. Its immutable payload
carries computation status, completeness and completeness method, geometry kind,
currentness, support level and numeric guarantee, with per-solution local
isolation status.

### 3.3 Why ordinary point materialization is not permitted

Four independent gates, all in the shared kernel:

1. the public spline-pair resolver is reached only when a ledger evaluation is
   supplied, which the pair algorithm does only for a public command;
2. that resolver returns the discovered result unchanged unless spline-pair
   interval certification reports supported;
3. certification requires interval-model capture to succeed on **both** sides,
   and capture returns null unless a side resolves — through at most the
   permitted non-collapsed similarity captures — to the spline semantic
   evaluator. `a` cannot;
4. independently, the generic discovery capabilities construct every candidate
   with **not-established** local pair isolation, and per-solution point
   admissibility for a pair requires **established** isolation, which only the
   certified route produces, with a certified rectangle.

Downstream, the materialization algorithm never solves; it only looks up an
exact-token-admissible solution, which additionally requires the token ledger to
validate the token as current for a publicly persisted result, plus
`SUCCESS` + finite (or mixed finite overlap) geometry + `CURRENT` +
support level not unsupported.

### 3.4 Whether eligible exact tokens exist — decided from persisted state

They do not, and this is settled by the witness's own serialized ledgers rather
than by inference. The ledger state format is
`version|nextIncarnation|encode(current)|encode(copySource)`, a snapshot encodes
as `hex(owner)~hex(sourcePair)~hex(constructive)~hex(topology)~entryCount[~entries…]`,
and the exporter selects the pair format version **if and only if** a pair
binding exists.

| Rich result | Format version | `nextIncarnation` | Constructive lineage | Durable entries |
|---|---|---|---|---|
| `e = Intersect(d, a)` | `4` — the **non-pair** version | `1` | `g9u0-public-locus-pair-intersection/v1` | **0**; copy-source snapshot absent |
| `g = Intersect(a, c)` | `4` | `5` | `g9u0-public-intersection/v1` | **4** |

`e`'s non-pair format version is direct persisted proof that **no pair binding
was ever allocated**; its `nextIncarnation` of `1` proves nothing was ever
allocated at all. `g`'s `5` matches its four allocations — the four exact tokens
the author used for `I`, `J`, `K` and `L`. Independently, the ledger uses two
distinct allocation prefixes, and the author's tokens name the current-root
prefix belonging to the one-source route, which the pair route structurally
cannot produce. An existing regression already pins the generic outcome by
asserting that a generic pair's ledger state does not start with the pair format
version.

### 3.5 Applicable approved contracts

- `geocedg/specs/curves/spline-v2-pair-materialization.md` §1 — permits
  exact-token materialization for supported `SplineV2 × SplineV2` roots;
  similarity images qualify only when both sources retain authenticated spline
  provenance and certifiable current polynomial capability; and explicitly:
  "Generic LocusV2 x LocusV2 remains under its existing rich-only
  materialization policy." Its exact-token contract is the
  `pair-singleton-transverse-germ/v1` scheme, whose durable key must not contain
  current parameters, coordinates, boxes, span/knot/chart identifiers, indices,
  ranks, cardinalities, coefficients, revisions, labels, pixels, proximity or
  movement history.
- `geocedg/specs/curves/semantic-spline-2d.md` §5.2 — the base case: pair
  candidates are deliberately rich-only, no active public-ledger allocation is
  created, and a point cannot be materialized from a pair diagnostic token; any
  future extension requires a separately reviewed symmetric rectangle and
  uniqueness certificate and selector.
- ADR 0018 — pair results are not point-materializable.
- ADR 0021 Decision 1 — materialize only authenticated `SplineV2 × SplineV2`
  component pairs including supported similarity compositions; generic
  non-spline `LocusV2` pairs remain rich-only.
- `geocedg/specs/locus/locus-v2-public-surface.md` — lists `Locus V2 × Locus V2`
  on finite components or periodic fundamental domains as a supported **rich**
  family, with exact-token materialization in its own separate section.
- ADR 0013 — an ordinary dynamic point may be produced only from an exact
  admissible result token.
- The G9U0-R4 admissibility design governs the one-source ledger/current-root
  route, which is the `a × c` path that does work.

### 3.6 Classification and the approved boundary

The alternatives were tested and rejected:

- **implementation regression** — refuted. `semantic-spline-2d.md` §5.2 and ADR
  0018 positively forbid pair materialization, so no prior contract was lost.
- **missing frontend exposure** — refuted. The desktop dialog enumerates only
  tokens the kernel has already marked point-admissible and otherwise renders
  the no-admissible-token message, so there is nothing withheld to expose.
- **candidate semantic extension** — not as *observed*: the observation is
  conformance. An extension is a separate, newly scoped decision.

### 3.7 Bounded unresolved hypotheses

Stated as unresolved rather than asserted:

1. The decoded ledger proves zero allocations but does not distinguish a
   `SUCCESS` result carrying discovered solutions with not-established pair
   isolation from an `UNSUPPORTED` computation status, which the pair solver
   returns when the fallback capability's finite-components precondition fails.
   Both are consistent with the witness, and they are different diagnostic
   stories for an author. This must be settled by the first scenario of any
   future phase.
2. On the generic path the public spline-pair resolver returns before its
   diagnostics loop, so no pair-selector class evidence is emitted at all. The
   honest label may therefore be "unsupported *and additionally undiagnosed*",
   which would make a bounded diagnostics improvement worth considering
   independently of any capability extension.

### 3.8 What today's capabilities can prove — and the author's extension decision

This subsection states what is provable with the kernel exactly as it stands. It
is an R0 finding about the **current** proof capability, not a ceiling on the
achievable family set.

With today's machinery, the only widening the existing proof apparatus could
certify is a pair in which **both** sides can already supply an exact,
outward-roundable local germ of the rigour the approved contract requires —
concretely, an evaluator exposing a certifiable piecewise-polynomial model with
exact rational coefficients equivalent to what interval-model capture extracts
today. The reconstructible evaluator carried by a generic dependent-point locus
has no exact coefficient authority, so no outward enclosure of the true function
and Jacobian is obtainable *from it as it exists now*, and substituting rounded
or sampled data is forbidden by the same contract.

The chain `rich semantic intersection -> current admissible exact token ->
explicit materialization` would be preserved unchanged. The selector would be
built only from durable source identities, branch and component lineage,
orientation, domain kind, parameterization contract version and the normalized
transverse germ sign. No coordinate or proximity matching participates at any
step; the existing replacement test is a component-key plus semantic-parameter
chart containment, never a nearest-root match.

**Author decision (`AD-R0-1`), superseding R0's original planning conclusion.**
R0 originally concluded that this extension lay outside `PRE-G9B-R`. The author
has decided instead that broader exact-token point materialization between
semantic curves is an important GeoCeDG capability and must be incorporated
explicitly into the track before it closes, as `R2-E0` (design) and `R2-E1`
(implementation).

The decision supersedes **only** that planning statement. It does not invalidate
the factual D1 characterization above, the historical contracts, the existing
`G9S1`/`G9S1-R1` approval, or any existing evidence. D1 remains
`INTENTIONALLY_UNSUPPORTED_CURRENT_CAPABILITY` and must not be rewritten as
technical debt.

The objective is also **broader than the originally observed mixed pair**: `E0`
must investigate, and where semantically defensible enable, materialization
across certified `SplineV2 × SplineV2` (the approved reference baseline),
`SplineV2 × generic LocusV2`, `generic LocusV2 × SplineV2`,
`generic LocusV2 × generic LocusV2`, and supported semantic images and
compositions of those families where provenance remains certifiable. `E0` is
charged with determining the *minimum additional kernel capability* that would
let further families be certified, and must presuppose neither that every
generic pair can be materialized nor that only polynomial or spline-based pairs
ever can. Eligibility must be defined by **semantic capability and provenance**,
never by Java class name alone.

The extension still requires a new ADR amending ADR 0021 Decision 1 and normative
specification amendments. Those belong to `R2-E0` after separate authorization;
none is created by `PRE-G9B-R0`, and neither `E0` nor `E1` is authorized by this
closeout.

### 3.9 Documentation obligation

One bounded gap: the user guide does not state, in user terms, that a pair
intersection materializes points only when both operands are `SplineV2` or
certifiable similarity images of one. Recorded for whichever phase next touches
the guide; not itself opened as debt.

## 4. D2 — `SplineV2` authoring and help interaction

**Classification: `MISSING_FRONTEND_EXPOSURE_AND_INCORRECT_CONTEXTUAL_HELP`.
Layer: Desktop/frontend and localization, with one qualified kernel authority
question.**

### 4.1 The implemented command surface, verified from source

| Arguments | Accepted shape | Degree |
|---|---|---|
| 1 | list of ≥ 3 finite 2D points | defaults to 3 |
| 2 | list + explicit degree, degree validated | explicit |
| 3 | list + degree + weight function | explicit |
| 3 | three bare points, wrapped into a list | defaults to 3 |
| ≥ 4 | bare points, wrapped into a list | defaults to 3 |

All four forms the author expects already work, including bare point arguments
and default degree 3. Exactly two bare point arguments is not accepted, which is
consistent with the three-point minimum. The bare-point form is not merely
inferable from the dispatch table — it is directly tested as numerically
equivalent to the list form. The wrapping forms build their list under
suppressed label creation, so **no visible auxiliary list is ever created**.

One precision that matters for the help text: explicit-degree validation — which
enforces the degree range and the degree ≤ point-count ceiling — runs **only** on
the forms that supply a degree. The default-degree paths bypass it, so an
over-large point set is rejected later by the polynomial model's own guard and
surfaces as the localized invalid-definition message. The forms therefore differ
in where and how they fail.

### 4.2 (1) Missing / incorrect contextual command help

Three independent causes, all outside the kernel:

- **Everything mode-keyed is blind to `SplineV2`.** Tool name and tool help
  resolve through the Euclidian mode text; the product's contextual-help action
  and the toolbar help strip both read the current mode. The profile declares the
  `SplineV2` action as command-kind with no numeric mode, and the registry
  dispatches mode-less actions without setting a mode. Contextual help therefore
  necessarily describes the previously active tool — exactly the author's
  observation.
- **The action opens the generic panel without a selection.** It shows the
  Algebra input, prefills the literal `SplineV2(` **only when the field is
  blank**, sets a tooltip, requests focus and opens the generic input-help
  panel. Nothing preselects `SplineV2`. The correct localized syntax already
  exists in the default, `_en` and `_es` command bundles and the panel can render
  it. This is **not** a missing-key degradation.
- **The one contextual string shown is wrong.** The profile tooltip advertises a
  degree-2 example, which the command's degree validation rejects, and mentions
  forms with no counterpart in the dispatch table. Opened as
  `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX`.

Two things are **not** missing, contrary to the obvious first hypothesis: the
tool icon already exists as an owned SVG and is already bound to the action key
by the desktop tool-image resource; and the action is already rendered in the
construction-semantic-curves profile flyout, because that group is skipped by the
ordinary toolbar compiler and built by the flyout builder, which iterates every
action id with no mode test. What is missing is mode *semantics*, not rendering.

Secondary: `SplineV2` is filed under the function/calculus command table while
`LocusV2` sits under geometry, so a user browsing the help tree looks in the
wrong category.

### 4.3 (2) Deficient interactive construction workflow

`SplineV2` has no tool: no mode constant, no mode setup, no controller branch,
no tool or help keys. Its only surface is the Algebra input. `LocusV2` is fully
wired and is the template — mode constant, mode text, localized tool and help
keys, an `upstream-mode` profile action with an audited numeric id in the same
flyout group, and a controller branch that collects two points and calls the
kernel operation. The single undo point comes free from the end-of-mode callback.

### 4.4 (3) Kernel extension need

**No kernel extension is required for the open forms**, decisively: ordered
point-list consumption, default degree 3, explicit degree, weight function,
bare-point wrapping, invisible auxiliary list, bounded validation and a
localized error all already exist.

**Qualified exception — the closed periodic form.** Closedness is real kernel
state: the structural spline model carries and branches on a `closed` flag,
exercised by existing kernel and native-archive tests through a list whose first
and last members are the same point. The only public creation entry carries
**no** closed parameter, so closedness is declared solely by the repeated list
member. Meanwhile the host selection manager toggles selection, so a point
cannot be selected twice by the ordinary gesture, and the polyline gesture
already consumes a re-click as "finish". Whether the desktop may *synthesize*
that repeated member is an authority question, not only a gesture question
(`AD-R0-8`).

### 4.5 Designed tools

Both designs, with their concrete seams and the full list-tool contract
(ordering, duplicates, mixed types, cancel, undo/redo, downstream dependencies,
member deletion and redefinition), are recorded in
[the staged design](../architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
§2.2. Nothing is implemented. The frontend collects an ordered selection and
nothing more; the list tool produces the **ordinary** host `GeoList`, not a new
CeDG-specific semantic type; and the `SplineV2` tool is not required to create a
visible auxiliary list.

## 5. D3 — apparently false ambiguity during Redefine

**Classification: `INCORRECTLY_REJECTED`**, with two separable defects.

### 5.1 The message and its single producing site

The user-visible text is the GeoCeDG profile key `Redefine.Status.AMBIGUOUS`
("The semantic redefine is ambiguous. The construction was not changed." / "La
redefinición semántica es ambigua. La construcción no se modificó."),
synthesized by the desktop redefine adapter from the closed kernel status. The
frontend performs no analysis: it renders the status and returns null, which the
algebra processor turns into a silent cancellation.

`SpatialRedefineAssessmentStatus.AMBIGUOUS` is produced in exactly one place —
the `IllegalArgumentException` catch around the proposal description inside the
registry's `assessRedefine`. Any other runtime exception there yields
`UNSUPPORTED`.

### 5.2 Which seam it is, and which it is not

The catch runs **before** cycle analysis, before the host-redefine shape check,
before the compatible-redefine `inspect`, before procedural-position planning,
and before the legacy/impact branch. Therefore the rejection does **not**
originate in:

- parsing, overload/command resolution or evaluation of `C + (1,0)` — all have
  already succeeded, and the identical expression constructs `M`;
- dependency/cycle analysis;
- the G9A compatible-redefine assessment;
- impact or currentness analysis;
- frontend confirmation logic.

It originates in **durable dependency projection during redefine assessment**.

### 5.3 Defect D3-a — the null-handling asymmetry

The throw that fires is the construction-geo redefine provider's dependency-id
computation, which walks the candidate's direct algorithm inputs and throws
`"Construction-defined candidate has an unregistered dependency"` for any input
without a persistent identity. `E`'s parent is a midpoint algorithm whose direct
inputs are `D` and the unlabeled helper from `C + (1, 0)`; neither participates.

Precisely: **publication and assessment use the same direct projection over the
parent algorithm's inputs, with opposite null handling.** Publication *drops*
inputs lacking an identity — and requires completeness only for public Locus V2
outputs — while assessment *throws*. The stored signature, produced by the
dropping rule, is then compared against a candidate signature produced by the
throwing rule. The witness proves the premise: `E`'s persisted record lists
`dependencies="geo:0ec40b8a…"`, i.e. `C` alone, with `D` silently dropped.

**Corollary:** because `D` is unregistered independently of the edit, *any*
redefine of `E` — including re-entering the identical `Midpoint(D, C)` — takes
the same throw. The verdict is not about the author's expression at all.

### 5.4 Why a naive fix is insufficient

A second gate sits behind the throw: for a non-public candidate, any
dependency-set change is rejected outright. Under a null-dropping direct
projection, `E`'s candidate dependency set becomes empty, unequal to the stored
`{C}`, so that gate rejects — on both the retain inspection and the replacement
inspection the assessment already performs with replacement explicitly selected
— yielding `UNSUPPORTED` rather than a legacy offer. Only a projection that
reaches `C` **transitively through** the unregistered helper restores `{C}`,
hence a definition change and `RETAIN`.

That transitive closure is a genuinely **new third rule** and must be adopted on
both sides together, or the asymmetry merely moves. Which rule becomes canonical,
and whether persisted signatures change as a consequence, is the central `R3`
design decision (`AD-R0-6`). **No document on disk currently states the
normative durable dependency projection rule; `R3` must write it.**

### 5.5 A tempting authority claim, corrected

ADR 0026 lists "Ignoring unregistered neighbours or replacing their omission
with a universal rejection" among its rejected alternatives. That text is real,
but its context and its corresponding decision item concern **procedural
position and surviving order**, not dependency-signature projection — and that
same item explicitly sanctions failing closed when surviving order cannot be
established. The ADR supports the general CeDG principle that ordinary
unregistered elements must neither become identities nor cause blanket
rejection; it does not directly adjudicate this path. **The classification rests
on the code-level asymmetry, not on that quotation.**

### 5.6 Defect D3-b — the status vocabulary

Nine distinct `IllegalArgumentException` sites — seven in the construction-geo
provider plus two in the registry's proposal description — all collapse onto the
single `AMBIGUOUS` status. Among them is a *correct* rejection, "Candidate
changes the construction-defined geo family", which is therefore reported to the
user as ambiguity. This is independent of D3-a and could be fixed separately.
Opened as `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY`.

Related and cheap: on the path where no assessment handler is installed, the
kernel already surfaces the provider's own reason string through a typed
diagnostic; only the desktop dialog discards it. The provider also already has
an in-class precedent for treating a description failure as a boolean rather
than a thrown exception.

### 5.7 Verdict and expected effect

The redefine is a **compatible retain**. `E` keeps its durable identity; the
`a → E` DAG edge survives; `a`, then `e` and `g`, and the materialized points
`I`–`L` are recomputed and revalidated under the existing currentness rules; the
definition revision advances, and the topology revision only if the durable edge
set genuinely changed; the change stays one host redefine and one ordinary undo
step; save and reopen reconstruct the committed relation. `M` is unaffected and
stays identity-free.

### 5.8 Coverage gap

No kernel test asserts `AMBIGUOUS` at all — the only occurrence is a desktop
presentation loop over unavailable statuses. The kernel assessment test covers
retain, relocation, legacy and invalid-DAG only, and every compatible-redefine
test targets public V2 outputs whose inputs are all promoted. Redefining a
promoted **ordinary** participant with an unregistered input is untested, and the
`"unregistered dependency"` message has no coverage whatsoever.

### 5.9 Invariants a future fix must not break

Eight constraints, listed in
[the staged design](../architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
§2.3. In summary: no coordinate, proximity, label, ordinal or render-geometry
repair; ordinary elements stay identity-free; one shared projection; genuine
contract changes still fail closed; cycles, stable-role completeness and the
staged-dependency closure check unchanged; atomicity, host-state seal, final
currentness authorization and XML rollback preserved; retain failure never
implies fresh; narrowing only.

## 6. Technical-debt disposition

The full disposition with evidence is in
[the disposition matrix](pre_g9b_r0_technical_debt_disposition_matrix.md). Nothing
is deleted; every pre-existing identifier keeps its origin.

| ID | Disposition | Owning phase |
|---|---|---|
| `TD-P1-SEMANTIC-LENGTH-INTERSECTION` | `RETAIN — RESCOPED` | `R2` |
| `TD-P1-PACKAGING-SUMMARY` | `RETAIN` | `R1` |
| `TD-VERIFY-RECEIPT-RECOVERY` | `RETAIN — RESCOPED` | `R1` |
| `TD-G9X1-HISTORICAL-PIN` | `RETAIN` | `R1` |
| `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` | `RETAIN`; closure **proposed** in `R1-R1` | `R1-R1` |
| `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX` | `NEW` | `R4` |
| `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY` | `NEW` | `R3` |
| `TD-R0-GGBSCRIPT-STRATEGY-RESTORE` | `NEW` | `R6` |
| `TD-R0-G9U1-Q02-BINDING` | `NEW` | `R1-R1` |

### 6.1 Relationship between D1 and `TD-P1-SEMANTIC-LENGTH-INTERSECTION`

`PARTIALLY_COUPLED`, with **no dependency in either direction**. D1 answers
whether a point may exist at all for a token; the debt answers whether an
existing point can address a semantic position on `S`. Spline-pair
materialization already works, so the metric failure reproduces without any D1
change; widening D1 would publish more points and change nothing in the
resolver. They must not be merged into one phase.

The metric resolver admits exactly two provenance shapes — an explicit semantic
locus point whose source is the metric's own locus by reference identity, and a
unique spline constructor occurrence — and the latter is bounded by **source
family before slot membership**, normatively so. An intersection-materialized
point is a bare `GeoPoint` carrying only coordinates, so both shapes fail and
the metric publishes `INVALID_QUERY`.

Feasibility is established rather than speculative: the kernel **already
performs** the exact provenance walk a third endpoint family would need, with a
fail-closed idiom in which every non-matching shape returns null.

### 6.2 `TD-VERIFY-RECEIPT-RECOVERY` and this phase

The gap is larger than the roadmap wording: the flow does not "not always" emit
a receipt — it never does. The verifier does not import the receipt module; the
supervisor writes only the run-level result and journal; the only receipt-writer
invocations in the repository are in its own test module; and closeout requires a
receipt path as a mandatory parameter. Acceptance gating inside the receipt
factory is already complete, so no new acceptance logic is needed; what is
missing is producers for three caller-supplied inputs and one emission branch.

**Effect on this phase, reported truthfully.** `PRE-G9B-R0` executed `STATIC`,
not `FINAL`, so no receipt was expected or required, and none was fabricated.
This debt therefore did not prevent R0's acceptance. It does, however, mean that
no later `PRE-G9B-R` phase can produce *canonical machine* closeout evidence
until it is closed — each would repeat the manual author-disposition record used
by the most recent documentation track.

## 7. Staged design

The proposed sequence, with per-phase objective, layer, prerequisites, normative
work, productive and forbidden scope, acceptance criteria, focal tests,
verification profile, author-smoke expectation, stop conditions and explicit
non-authorizations, is in
[the staged design](../architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
§4.

| ID | Scope | State |
|---|---|---|
| `PRE-G9B-R0` | characterization, debt disposition, staged design | `PASS — AUTHOR APPROVED` |
| `PRE-G9B-R1` | verification and bounded operational debt | `DESIGNED — NOT AUTHORIZED` |
| `PRE-G9B-R1-R1` | native periodic-quarantine round-trip closure | `DESIGNED — NOT AUTHORIZED` |
| `PRE-G9B-R2` | semantic metric endpoint provenance | `DESIGNED — NOT AUTHORIZED` |
| `PRE-G9B-R2-E0` | Locus V2 pair exact-token materialization extension design | `DESIGNED — NOT AUTHORIZED FOR EXECUTION` |
| `PRE-G9B-R2-E1` | Locus V2 pair exact-token materialization implementation | `DESIGNED — NOT AUTHORIZED` |
| `PRE-G9B-R3` | redefine correctness | `DESIGNED — NOT AUTHORIZED` |
| `PRE-G9B-R4` | `SplineV2` and list authoring UX | `DESIGNED — NOT AUTHORIZED` |
| `PRE-G9B-R5-A` | user-guide index, outline and navigation | `DESIGNED — NOT AUTHORIZED` |
| `PRE-G9B-R5-B` | canonical English command surface | `DESIGNED — NOT AUTHORIZED` |
| `PRE-G9B-R6` | GGBScript compatibility gate | `DESIGNED — NOT AUTHORIZED` |
| `PRE-G9B-R7` | integrated PRE-G9B readiness and closeout | `DESIGNED — NOT AUTHORIZED` |

Four deviations from the author's originally proposed boundaries. Three are
evidence-driven decisions taken by R0: `R2` is re-scoped to metric endpoint
provenance only; `R1-R1` is added because the R4 risk closure is a Desktop
product test under the `G9U1` perimeter and cannot share `R1`'s frozen class; and
`R5` is split because the canonical command surface needs its own compatibility
ADR, reaches shared-kernel localization seams, and carries a semantic-identity
hazard the guide work does not.

The fourth is the author's review decision: **`R2-E0` and `R2-E1` are inserted
between `R2` and `R3`** to carry generalized exact-token point materialization
between semantic curves (`AD-R0-1`). `R2` and `R2-E0`/`E1` remain conceptually
distinct and neither substitutes for the other — `R2` asks whether an already
materialized point can serve as a semantic endpoint, while `E0`/`E1` ask which
pair roots may be materialized at all — but `E0` must ensure any new materialized
pair point exposes provenance `R2` can consume without coordinate matching, so
that two incompatible provenance mechanisms are not built.

### 7.1 I1, I2, I3 design input

All three author-requested improvements were characterized and designed, not
implemented.

**I1 — guide index and navigation.** The two editions are already structurally
aligned: identical heading-level sequences, identical decimal numbering prefixes
and identical ordered stable section identifiers, with zero Markdown links and
zero nested lists in either. One derived navigation model is therefore possible
and two manually maintained indexes are explicitly rejected. Anchors are
computed, never authored, and never derived from translated prose. The exact
invariant that holds — and that a STATIC structural diagnostic can assert — is:
*every stable section marker is immediately followed by an h1 or h2, and every
h1/h2 is immediately preceded by a marker*. Note that the **first marker sits on
the level-1 title**, so there are 17 markers and 16 level-2 headings; inventing
a synthetic root identifier would orphan an authored, parity-tested id. The
renderer needs two additive changes — emit an anchor before each heading, and
return the outline alongside the HTML — and the window gains a split pane with a
tree whose selection scrolls to a reference. No hyperlink listener is needed,
which matters because an existing test forbids one.

**I2 — canonical English command names.** The lookup strategy is a three-constant
enum: user (localized, English or internal), XML (internal only) and script
(English or internal). The Algebra input runs under user, GGBScript under script,
and XML/file parsing plus nested `Execute` under XML. Four facts shape the
design, and one enlarges it materially:

- English and internal names are **already accepted** in the Algebra input in
  every locale, because the reverse lookup falls back to the internal table. The
  alias half of the candidate policy needs no new mechanism.
- The correct canonical resolver is the **English** name, not the internal name.
  Several commands have internal names that differ from their English names, so
  switching localization off naively would display internal identifiers. This is
  the largest naive-application failure.
- Serialization does not break. Construction XML always stores internal names
  and loads under the XML strategy; scripts inside documents were delocalized at
  authoring time and load untranslated. The risk lies in editing round-trips and
  in text-valued command strings, which already require internal names.
- The three GeoCeDG-added commands are `LocusV2`, `LocusLength` and `SplineV2`.
  Spanish names exist for the first two and not for the third.
- **It is not a frontend-only change.** Command heads are printed at roughly a
  dozen sites beyond the two obvious ones — including a GeoCeDG-owned kernel
  adapter and several function-head sites in the expression serializer — so a
  hook at two sites would produce mixed-language output. And the desktop
  syntax-help path is not selectable: the localization object holds a single
  final command-syntax instance with a getter and no setter. The existing
  English syntax class is also not a drop-in, because it overrides the resolver
  used for both the head and the syntax-bundle key, and on the JRE client that
  resolver returns the bundle key verbatim, so the syntax degrades to the literal
  key and is rejected as command-not-found. It works on the web client only
  because that client overrides the resolver to read the English bundle.

A residual semantic-identity hazard must be covered by the compatibility
decision: a localized name equal to another command's English name resolves to
the localized meaning, because the localized entry is inserted after the
internal one. With English display plus user-strategy parsing this can silently
redefine an object on edit.

**I3 — GGBScript compatibility gate.** The script path and the Algebra-input
path converge on exactly one command dispatcher instance and one processor
factory, so GeoCeDG commands are structurally reachable from scripts — proven
from source, not inferred. The desktop Algebra input **wraps** the identical
processor entry point, adding a redefine branch and, on the creation branch
only, frontend orchestration and a single undo store; it does not bypass the
processor. The genuine differences the gate must cover are name lookup and case
sensitivity, locale exposure for raw localized tokens that bypass
delocalization, evaluation flags, error handling, undo storage, the nested
`Execute` third regime, and the swallowed-throwable/strategy-restore defect
(`TD-R0-GGBSCRIPT-STRATEGY-RESTORE`). Existing coverage already executes all
three GeoCeDG commands through the script path and mechanizes the feature-off
atomic-rejection row with an XML-identity assertion; what is unmechanized is the
locale dimension, the truthful-failure row, the undo row and the
rich-result/auto-materialize row. The proposed machine-readable capability matrix
shape, its row identity and the four properties that make it reproducible
authority rather than a report are in the staged design §4.

One attribution was tested and **rejected**: the desktop auto-materialize session
behaviour does not explain the D1 witness. It is off by default, exists only on
the explicit human Algebra path, and the witness shows no materialization
asymmetry it could account for — the four materialized points came from four
explicit token calls.

## 8. Verification

`VERIFICATION_CLASS = DOCUMENTATION_STATUS_ONLY`, declared at phase start with
`frozenAtPhaseStart=true`. Under
`geocedg/specs/operations/verification-levels.md` §12.8 that class requires
"Static validation only; no FULL", and §12.8 also makes a physical level above
the frozen plan a **failure** requiring an explicit
`VERIFICATION_ESCALATION_REQUEST`. The freeze is a per-phase declaration, so its
ratification is an author decision at review; it is not derivable from the
document alone.

The phase changed only `docs/architecture/**`, `docs/validation/**`,
`docs/roadmap/**` and `geocedg/validation/**`. It deliberately did **not** touch
`geocedg/specs/operations/verification-*.json`, which would have reclassified the
change as a verification-infrastructure change requiring FULL.

### 8.1 Executed runs

| # | Purpose | Bound identity | Result |
|---|---|---|---|
| 1 | pre-candidate smoke, **not acceptance evidence** | `candidate_commit 0d13434cf…`, tree `922034fd…` — the *entry* commit, because the verifier binds `HEAD` and the worktree was still dirty | run `verification-f764b18d94174b32a8cc445ba6cb122c`; exit 0; `ACCEPTED / COMPLETE`; 3/3 required acceptance checks completed; 3 diagnostics clear, 1 finding, 1 unavailable |
| 2 | **acceptance evidence for this candidate** | `candidate_commit a13fdd69069fa525eb09f19886231024710a5f13`, tree `cd69789c28f27505cb6fd28114f82c987a910f84`, clean worktree and index | run `verification-00d1902e02f94e18a1784814dbdfed55`; exit 0; `ACCEPTED / COMPLETE`; identities in §8.4 |

Both runs used
`tools/agent/verify.ps1 -Profile STATIC -LogDirectory <new external root>` under
PowerShell 7.2 or later, with a fresh, initially absent log directory outside
the repository.

Run 1 is explicitly **not** claimed as acceptance evidence for this candidate:
its bound identity is the pre-change commit, and the canonical contract forbids
reattributing dirty or pre-commit evidence. It is reported because it was
executed, and because it established that the profile resolves and completes.
Both runs share the same execution plan hash, which is expected: `STATIC`'s plan
does not depend on the documents this phase changed.

### 8.2 Required acceptance coverage under `STATIC`

`required_check_ids` = `prompt.execution-safety` (`SAFETY`),
`repository.boundary-safety` (`SAFETY`),
`static.scientific-inputs.semantic` (`SEMANTIC`, domain `SCIENTIFIC`).
`untrusted_check_ids` and `not_run_check_ids` were both empty.

`prompt.execution-safety` validates only the three documents enumerated in
`geocedg/specs/operations/prompt-contracts.json`; it does not enumerate
`.github/prompts/`. `static.scientific-inputs.semantic` pins five Python
reference generators, none of which this phase touched. The other two catalog
contracts have no registry consumer today.

### 8.3 Diagnostics

Diagnostics never alter acceptance, coverage or exit status. The single finding
is `diagnostic.governance`, and it says nothing about this candidate: the script
emits `outcome = DIAGNOSTIC_FINDING` unconditionally with the constant cause
"Legacy governance remains historical context during the temporary recovery
protocol." `diagnostic.historical-consistency` likewise emits
`DIAGNOSTIC_UNAVAILABLE` unconditionally. `diagnostic.documentation`,
`diagnostic.style` and `repository.whitespace-diagnostic` were clear.

`git diff --check` reported no whitespace errors.

**No repository-relative Markdown link checker exists**; it is listed only as
future documentation-validation work. The links in this phase's documents were
resolved by hand against the working tree.

### 8.4 Candidate acceptance run

```text
CANDIDATE COMMIT   = a13fdd69069fa525eb09f19886231024710a5f13
CANDIDATE TREE     = cd69789c28f27505cb6fd28114f82c987a910f84
PROFILE            = STATIC
COMMAND            = tools/agent/verify.ps1 -Profile STATIC -LogDirectory <new external root>
RUN                = verification-00d1902e02f94e18a1784814dbdfed55
RUN STATE          = COMPLETED
EXIT CODE          = 0
EXECUTION PLAN HASH= 7d52b28a732b35635009a4e515095232f569d308ce34964db4597454bfa8d5f0
RESULT HASH        = d68b830e8983f8ebe50fb1d2a33184f25fb79b1648cd903019ebc76749309b9b
ACCEPTANCE VERDICT = ACCEPTED
COVERAGE VERDICT   = COMPLETE
ACCEPTANCE CHECKS  = 3 required, 3 completed, 0 untrusted, 0 not run
DIAGNOSTICS        = 3 clear, 1 finding, 1 unavailable
RECEIPT            = NOT APPLICABLE — STATIC is not FINAL, so no receipt is
                     contractually expected; none was fabricated
                     (TD-VERIFY-RECEIPT-RECOVERY)
```

The worktree and index were clean when this run executed, and the verifier bound
the exact candidate commit and tree above. The only change made after it is this
subsection and the run-identity lines in §8.1, recorded in a separate
evidence commit — the ordinary pattern for a status-only evidence record. The
acceptance evidence therefore belongs to `a13fdd69…` / `cd69789c…`, and that
identity is the one the author reviews; no evidence is reattributed to the later
commit.

Log root and preserved raw evidence live outside the repository, under the
session's external log directory, consistent with `artifacts/` not being
authority.

### 8.5 Byte-exactness discipline

The living roadmap has genuinely mixed line endings in its committed blob: 2 912
`CRLF` lines from older content and 893 bare `LF` lines in regions edited more
recently. An editing pass normalized the whole file to `LF`, which would have
rewritten 2 912 lines that this phase does not touch. That was detected before
committing — the diff showed roughly 2 900 spurious deletions — and repaired by
restoring the committed bytes and re-applying only the four intended edits at
their original sites, each of which already sat in an `LF` region.

The committed diff is consequently four hunks, 150 insertions and 4 deletions,
and `git diff --check` is clean. This is recorded because `.gitattributes` pins
byte-exact author evidence and generated references elsewhere in the tree, so a
silent end-of-line normalization is a real hazard for any documentation task,
not a cosmetic one.

The four new files are pure `LF`, matching the convention of comparable recent
documents under `docs/validation/`, `docs/architecture/` and
`geocedg/validation/`.

### 8.6 Mandatory impact declarations

```text
PRODUCT_PHASE_EFFECT = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
Rationale: no verifier entry point, registry, schema, static-contract catalog,
  acceptance classification, closeout readiness or mode, promotion audit,
  numerical baseline, test selection, cache policy or parallelization changed.
  No PHASE selection was registered for PRE-G9B-R0.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, supported JDK or toolchain version,
  Gradle behaviour, build or verification entry point, numerical reference,
  generated source, packaging prerequisite, Conda/Python environment, download
  or environment contract consumed by Windows bootstrap was inspected as
  changed; this phase adds only Markdown and one JSON evidence document.

GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = No observable user workflow, command, tool, persistence
  behaviour, menu, installation path, exported format or developer-facing
  contract changed. The user guide's missing statement about pair-intersection
  materialization is recorded in section 3.9 as an obligation for whichever
  authorized phase next touches the guide, not as a change made here.

selfApproved = false
```

## 9. Scope limits observed

`PRE-G9B-R0` did not implement any `R1`–`R7` product change; did not touch the
intersection solver or materializer; did not change `Length` or `LocusLength`
semantics; did not modify redefine behaviour; did not add `SplineV2` or list
tools; did not change command-language behaviour; did not add GGBScript
functionality; did not redesign `G9B`; did not implement `G9B`, `G9C`, `G9U2`,
`G10` or `G12`; did not publish binaries; did not create a release or tag; did
not contact GeoGebra or OpenGeoProver or alter the `COMMERCIAL` disposition; and
did not itself promote any candidate to `PASS` or `AUTHOR APPROVED` — the `PASS`
recorded in §12 is the author's explicit decision of 2026-09-17, not an agent
claim. The author's required planning amendment added `R2-E0` and `R2-E1` to the
track; **neither was executed, and no ADR, normative specification, selector
schema or capability matrix for them was created.**

No test code, probe or diagnostic harness was added. All characterization was
performed by reading source, specifications, ADRs, manifests and the persisted
author witness. No build, test suite or product run was executed.

### 9.1 Deliberate omission, reported rather than worked around

The task contemplated preparing future prompt stubs for `R1`–`R7` "if repository
convention supports it". It does not: `CLAUDE.md` prohibits editing
`.github/prompts/**` as a side effect of another task, stating that changing the
governance layer is its own author-authorized task, and the permission layer
enforces the same boundary. **No prompt stub was created.** Every per-phase
contract field such a stub would have carried — objective, implementation base,
allowed and forbidden scope, required checks, authorization boundary,
publication boundary, acceptance criteria, focal tests, verification profile,
author smoke and stop conditions — is recorded in the staged design §4 instead,
so no authority is missing. `AD-R0-9` authorizes a separate governance-layer task
**after R0 closeout** to add the stubs; they must remain
`PROPOSED / UNEXECUTED / NOT AUTHORIZED` until separately authorized, and the
inventory must include `R2-E0` and `R2-E1`. `.github/prompts/**` was not edited
by this amendment either.

Two supporting facts for that decision, established during R0:
`prompt-contracts.json` is an enumerated registry of exactly three documents and
the safety checker iterates only that list, so unregistered stubs could not fail
`STATIC`; and three unexecuted stubs already exist in the tree using the banner
`**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**`, while the modern
PRE-G9B house style additionally places the seven `geocedg-field` markers in
task-profile order.

## 10. Author decisions

All nine were decided by the author on 2026-09-17. None of them authorizes a
phase for execution.

| ID | Author decision | Owner |
|---|---|---|
| `AD-R0-1` | **AUTHOR DECIDED — INCLUDE LOCUS V2 PAIR MATERIALIZATION AS A `PRE-G9B-R` CAPABILITY EXTENSION.** Broader than the originally observed mixed pair: `E0` investigates and, where semantically defensible, enables exact-token materialization across certified `SplineV2 × SplineV2` (the approved reference baseline), `SplineV2 × generic LocusV2`, `generic LocusV2 × SplineV2`, `generic LocusV2 × generic LocusV2`, and supported certifiable semantic images and compositions. Supersedes only R0's planning statement that the extension lay outside the track; D1's factual characterization, the historical contracts, the `G9S1`/`G9S1-R1` approval and all existing evidence stand | `R2-E0`, then `R2-E1` |
| `AD-R0-2` | Resolve the semantics and production of the checker identity hash, the input identity hash and the accepted-profiles set **inside `R1`**, reusing existing acceptance semantics. No second acceptance system; no fabricated hashes | `R1` |
| `AD-R0-3` | Plan closure of `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` in `R1-R1` using genuine native `.cedg` lifecycle evidence. Avoid widening the kernel API solely to enable a test where durable lifecycle evidence can establish the property | `R1-R1` |
| `AD-R0-4` | Adopt: GeoCeDG displays, suggests, inserts and documents command names canonically in English; localized names remain accepted compatibility aliases in ordinary user input where current compatibility permits; XML and serialization semantics unchanged; GGBScript remains canonical English/internal. Formalize in the `R5-B` compatibility ADR | `R5-B` |
| `AD-R0-5` | Fix the invalid `SplineV2` tooltip as part of `R4`, not as an independent microphase | `R4` |
| `AD-R0-6` | `R3` is planned as an integrated phase. No silent eager migration of historical persisted signatures. If the canonical durable-dependency projection requires a persisted-signature migration, design it explicitly and **stop for author review before implementing it** | `R3` |
| `AD-R0-7` | Do not create a second global technical-debt ledger now. The roadmap plus phase-specific machine-readable disposition remains sufficient authority unless a concrete consumer later justifies one | — |
| `AD-R0-8` | Desktop may construct the existing canonical closed form **only through an explicit closed-form user action**, and may synthesize the repeated first/last member the kernel representation requires. Never infer closedness from coordinate proximity, visual coincidence or an ambiguous repeated click | `R4` |
| `AD-R0-9` | Authorize a separate governance-layer task **after R0 closeout** to add proposed future prompt stubs, which remain `PROPOSED / UNEXECUTED / NOT AUTHORIZED` until separately authorized. The inventory must include `R2-E0` and `R2-E1`. `.github/prompts/**` is untouched by this amendment | separate task |

## 11. Unresolved scientific and architectural questions

These remain open after the author review. Questions 1–3 are now explicitly
owned by `R2-E0`, which is designed and not authorized.

1. Whether the witness's `e` is a `SUCCESS` result with not-established pair
   isolation or an `UNSUPPORTED` computation status (§3.7). `R2-E0` must settle
   this in its first scenario rather than assume it.
2. Whether the generic pair path should emit pair-selector diagnostics at all,
   independently of any capability extension (§3.7).
3. Which family discriminator is authoritative — evaluator capability or parent
   algorithm — given that two kernel sites already discriminate by parent
   algorithm (§3.8), and what *minimum additional kernel capability* would let a
   reconstructible evaluator support rigorous local root certification. `R2-E0`
   must answer this per family, presupposing neither universal admissibility nor
   a polynomial-only restriction.
4. Which durable dependency projection becomes canonical, and whether persisted
   signatures must migrate (§5.4). Under `AD-R0-6` a migration must be designed
   explicitly and stop for author review before implementation.
5. How the `S × S` self-intersection side selection is disambiguated for a
   future metric endpoint family without a coordinate comparison (§6.1). The
   parallel question for pair materialization is owned by `R2-E0`.
6. Whether declaring a closed periodic spline from the frontend is admissible at
   all (§4.4). `AD-R0-8` resolves the product policy — explicit user action only,
   never inferred — leaving the kernel-representation question to `R4`'s design.
7. What `checker_identity_hash` means, since nothing in the repository defines or
   produces it (§6.2). `AD-R0-2` assigns this to `R1`.

## 12. Closeout state

```text
PRE-G9B-R0 = PASS — AUTHOR APPROVED

PRE_G9B_R0_AUTHOR_APPROVED            = true
PRE_G9B_R1_AUTHORIZED                 = false
PRE_G9B_R1_R1_AUTHORIZED              = false
PRE_G9B_R2_AUTHORIZED                 = false
PRE_G9B_R2_E0_AUTHORIZED              = false
PRE_G9B_R2_E1_AUTHORIZED              = false
PRE_G9B_R3_AUTHORIZED                 = false
PRE_G9B_R4_AUTHORIZED                 = false
PRE_G9B_R5A_AUTHORIZED                = false
PRE_G9B_R5B_AUTHORIZED                = false
PRE_G9B_R6_AUTHORIZED                 = false
PRE_G9B_R7_AUTHORIZED                 = false
D1_CLASSIFICATION                     = INTENTIONALLY_UNSUPPORTED_CURRENT_CAPABILITY
D1_EXTENSION_SCHEDULED                = true  (as R2-E0 / R2-E1)
D1_EXTENSION_AUTHORIZED               = false
G9B_AUTHORIZED                        = false
G9C_AUTHORIZED                        = false
G9U2_AUTHORIZED                       = false
G10_PRODUCT_IMPLEMENTATION_AUTHORIZED = false
FURTHER_G12_AUTHORIZED                = false
COMMERCIAL_DISTRIBUTION_AUTHORIZED    = false
PUBLIC_RELEASE_AUTHORIZED             = false
selfApproved                          = false
authorApproved                        = true
passClaimed                           = true   (author decision; not an agent claim)
```

The approval covers the R0 characterization, the technical-debt disposition,
`AD-R0-1` through `AD-R0-9` as decided, and the revised `PRE-G9B-R` staged design
including `R2-E0` and `R2-E1`. It does **not** authorize execution of `R1`,
`R1-R1`, `R2`, `R2-E0`, `R2-E1`, `R3`–`R7`, `G9B`, `G9C`, `G9U2`, productive
`G10`, further `G12`, commercial distribution or public release. Recording
`R2-E0`/`R2-E1` in the roadmap does not start either phase.

Preserved states, unchanged by this phase: `G9X1`, `G9S1`, `G9S1-R1`, `G9U0` and
its refinements, `G9U1`, the POST-G9U1 track, `PRE-G9B-S1`–`S4`, `D1`, `P0`,
`P1` and `POST-P1-DOC-HELP` all retain their recorded `PASS — AUTHOR APPROVED`
status. `G9B` and `G9C` remain `DESIGNED — NOT AUTHORIZED`; `G9U2` remains
`BLOCKED ON GLOBAL G9 APPROVAL`; `PROFILE COMMERCIAL` remains
`PENDING EXTERNAL TERMS / NOT AUTHORIZED`. No approved specification or ADR was
rewritten by this phase.
