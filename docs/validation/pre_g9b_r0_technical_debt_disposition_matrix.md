# PRE-G9B-R0 — technical-debt disposition and observation matrix

- Status: **CANDIDATE — PENDING AUTHOR REVIEW**
- Recorded: 2026-09-17
- Base commit: `0d13434cf1ab4d31f4bb4d50e7aea6cb3e69e35d`
- Base tree: `922034fd46411530bd02ae15fcc832e3fc541dc7`
- Producing phase: `PRE-G9B-R0`
- Claim vocabulary: **proposed / not normative**
- Self approval: **false**

This matrix records the disposition of every open technical-debt item and of
every observation characterized by `PRE-G9B-R0`. It deletes nothing: every
pre-existing identifier is retained with its recorded origin. It approves
nothing and authorizes nothing.

Governing design:
[PRE-G9B-R staged design](../architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md).
Evidence and reproduction detail:
[PRE-G9B-R0 candidate report](pre_g9b_r0_characterization_candidate_report.md).
Machine-readable form:
[`geocedg/validation/pre-g9b-r0/pre-g9b-r0-disposition.json`](../../geocedg/validation/pre-g9b-r0/pre-g9b-r0-disposition.json).

## 1. Disposition vocabulary

| Term | Meaning |
|---|---|
| `RETAIN` | the item stays open with its identifier and origin unchanged; an owning phase is proposed |
| `RETAIN — RESCOPED` | retained, with a corrected statement of what the item actually is |
| `NEW` | opened by `PRE-G9B-R0`; not previously recorded anywhere |
| `CLOSE` | proposed for closure, contingent on an explicit author decision; `PRE-G9B-R0` closes nothing |
| `NOT A DEFECT` | characterized as conformance with an approved contract; any change is a new capability |

No item is merged, split away from its identifier, superseded or deleted by this
disposition.

## 2. Pre-existing technical debt

| ID | Origin | Layer | Roadmap state before R0 | R0 disposition | Owning phase | Dependencies |
|---|---|---|---|---|---|---|
| `TD-P1-SEMANTIC-LENGTH-INTERSECTION` | P1-D1, P1 author smoke | kernel semantic / provenance / metric endpoint | `OBSERVED — REQUIRES CHARACTERIZATION/DESIGN` | `RETAIN — RESCOPED` | `R2` | requires a normative metric-endpoint amendment first; independent of D1 |
| `TD-P1-PACKAGING-SUMMARY` | P1-D2, P1 author smoke | external tooling / reporting | `OBSERVED — SMALL BOUNDED MAINTENANCE` | `RETAIN` | `R1` | static-contract repin + registry catalog pin + two desktop tests must move together |
| `TD-VERIFY-RECEIPT-RECOVERY` | pre-P1 | verification infrastructure | `PRE-EXISTING — NON-BLOCKING` | `RETAIN — RESCOPED` | `R1` | blocked on `AD-R0-2`; requires FULL |
| `TD-G9X1-HISTORICAL-PIN` | pre-P1 | verification / historical authority reconciliation | `PRE-EXISTING — NON-BLOCKING` | `RETAIN` | `R1` | none |
| `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` | G9U0-R4 | kernel risk, closable only by real native lifecycle evidence | `OPEN / TRACKED` | `RETAIN`; closure **proposed** in `R1-R1` | `R1-R1` (proposed) | blocked on `AD-R0-3`; may need a kernel-surface decision |

### 2.1 `TD-P1-SEMANTIC-LENGTH-INTERSECTION` — rescoped statement

The roadmap statement is confirmed accurate and is sharpened in three ways.

1. The failure is **not** an intersection-materialization problem. Materialized
   spline-pair points exist today; the metric simply cannot address them. The
   two concerns are `PARTIALLY_COUPLED` with **no dependency in either
   direction**.
2. The metric endpoint resolver admits exactly two provenance shapes — an
   explicit semantic locus point whose source is the metric's own locus by
   reference identity, and a unique spline constructor occurrence. A point
   produced by the intersection-point algorithm is a bare `GeoPoint` carrying
   only coordinates; all provenance stays on the algorithm and the rich result.
3. The constructor-occurrence shape is bounded by **source family before slot
   membership**, and that bound is normative, not incidental:
   `geocedg/specs/locus/locus-v2-metrics.md` §21 admits an ordinary `GeoPoint`
   "when, and only when, the explicitly supplied source is a current `SplineV2`
   or a supported R5 similarity image of one".

**Feasibility anchor.** The kernel already performs the exact provenance walk a
third endpoint family would require — dependent point → intersection-point
algorithm → rich input → intersection algorithm → source, then effective root
token and retained root selector — with a fail-closed idiom in which every
non-matching shape returns null. This is an extension of an established
in-kernel pattern, not a new mechanism.

**Cases a future contract must characterize.** Unambiguous selection of the side
corresponding to `S`; currentness; token retention and reactivation; copy and
reopen; self-intersections; source A/B ordering; ambiguity; redefine;
persistence; focal tests with regressions. Two of these are already narrowed:
per-side locus identity and semantic revision are copied straight from the query
binding, so side ordering is fixed and only the genuine `S × S` case is
ambiguous; and an undefined endpoint is already rejected one step earlier than
the resolver, so a dormant materialized point already fails closed.

**Coverage gap.** No existing test combines an intersection-materialized point
with `Length` or `LocusLength`.

### 2.2 `TD-VERIFY-RECEIPT-RECOVERY` — rescoped statement

The roadmap says the flow "does not always" emit a receipt. The source says it
**never** does: the top-level verifier does not import the receipt module, the
supervisor writes only the run-level result and journal (plus one output
directory per check), and the only invocations of the receipt writer anywhere in
the repository are in its own test module. Closeout requires a receipt path as a
mandatory parameter, so canonical machine closeout cannot be invoked at all.

The most recent documentation track recorded exactly this, with the author
accepting the gap rather than fabricating a receipt.

Acceptance gating inside the receipt factory is **already complete** — it
refuses non-completed runs, non-accepted acceptance, incomplete coverage, an
invalid result hash, any untrusted or not-run check, a coverage/acceptance
mismatch and a profile set that does not contain the report profile, and the
writer refuses to overwrite. **No new acceptance logic is needed.**

What is missing is producers for the caller-supplied receipt inputs — the
checker identity hash, the input identity hash and the accepted-profiles set —
and a single emission branch on the accepted-FINAL path. A tracked-inventory
hash already exists and is the natural source for the input identity. The
checker identity has no defined semantics anywhere in the repository and is an
author decision (`AD-R0-2`).

**Blocking assessment.** Not blocking for later `PRE-G9B-R` phases. Each such
phase can produce *acceptable* closeout evidence without it, but not *canonical
machine* closeout evidence, and will otherwise repeat the same manual
author-disposition record. Fixing it is itself a verifier-orchestration change
and therefore requires FULL.

### 2.3 `TD-P1-PACKAGING-SUMMARY` — still open, exact site

Still present and still unconditional: the final package-summary block of
`tools/release/build-windows-package.ps1` prints
`PUBLIC REDISTRIBUTION = BLOCKED PENDING LICENSE/ASSET APPROVAL` with no profile
guard. The fix direction already exists in the same file, which derives the
redistribution flag for the build manifest from the selected distribution
profile and already prints that profile and its status.

Three couplings must move in the same candidate, or the change fails closed for
every profile:

1. the static-contract catalog pins this script's SHA-256;
2. the verification registry pins the static-contract catalog, and the registry
   check fails closed for *every* profile if either drifts;
3. two desktop tests assert the current wording — one forbids the sentence in
   live documents, the other requires the operations manual to keep naming
   `TD-P1-PACKAGING-SUMMARY`.

### 2.4 `TD-G9X1-HISTORICAL-PIN` — exact pin, measured

Independently measured during R0, read-only:

| Pinned input | Pinned value | Live canonical-LF SHA-256 | State |
|---|---|---|---|
| `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md` | `e5de67f5cc2108cb1f4f85954b3538a8971805ec976ce23a51604574454e147d` | `f2a0cacc88b13dfac927c8ad59b25cf55fe702d12620e8ba5e91e43b49b676ef` | **stale** |
| `.github/prompts/tasks/g9x1-extended-dxf-curves.prompt.md` | `fab1dd78c85b337a4f5ac29b8b56ca5565761a2b7cc199312634f42cbe975b94` | identical | current |
| `docs/adr/0014-export-only-dxf-approximation-and-sidecar.md` | `2bc0a4f7eed551778f1e4b50b6704214ca3dab96a2c69d0514bdc24a9c715eb3` | identical | current |

Exactly one pin is stale. The pinned value is the correct **historical**
authority: the specification blob at both recorded G9X1 entry commits
(`f528b2dcbe4a802d3dfdb334f842e39ec7f33015` and
`22bcc888ebb2ecb102fbeb5b07c87778fddeb3a0`) hashes to `e5de67f5…`. The live
successor was produced by exactly one commit, `e5260fc7d Implement PRE-G9B-S1-R1
typed DXF population`.

**Reconciliation shape (not implemented).** Keep the pinned value as a *named
historical* constant explicitly bound to the frozen tag and verified from the
tag blob; add a separate live-successor pin; add an explicit supersession record
naming the superseding commit and `PRE-G9B-S1-R1` as the authority that
legitimately evolved it. Leave the recorded G9X1 evidence and its approval tuple
byte-untouched. **Do not substitute the hash.** `G9X1 = PASS — AUTHOR APPROVED`
is unchanged and is not reopened.

### 2.5 `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` — status and closure options

All tracked statements across roughly twenty-five documents agree on
`OPEN / TRACKED`, and each is correctly scoped: every one says its own phase's
evidence does not discharge the risk. The strongest closure statement is in the
G9U1 command/tool consistency matrix: the risk is closed only by an actual
native-archive lifecycle; ledger export/import, copy and non-periodic
dormant/reactivated archive tests remain supporting evidence and cannot
substitute for it. Nine mandatory fixture steps are specified there.

**The prose is stale relative to source.** A Desktop test already implements most
of the fixture: it builds a genuinely double-covered periodic locus, materializes
four points each consuming its own exact token, drives the parameter into four
claimed-quarantine entries, saves to a real `.cedg`, reopens in a fresh
application, and asserts survival, empty session surfaces, unchanged point count,
undefined points with identical durable identities and identical selected tokens;
then establishes offset zero and asserts reactivation of the same points with no
count growth; then performs a second save/reopen of the reactivated document. It
is registered in the G9U1 scenario and lifecycle manifests and dispatched by the
G9U1 verifier, in its non-skip-build branch.

**No substitution is occurring.** The ledger-only analogue explicitly simulates
reopen by importing a serialized state and is consistently labelled supporting
evidence only; spline lifecycle tests use in-process XML self-round-trips; ADR
0021 explicitly denies that pair persistence closes this gap; hand-constructed
XML in the test trees is confined to malformed-input rejection fixtures.

**Residual: three items, not two.**

| Item | Kind | State |
|---|---|---|
| `U1-Q04` proved-nonzero quarantine retirement | honestly declared gap | `RETAINED_RISK`, no bound test |
| `U1-Q05` quarantine preservation boundary (feature-off host and Classic route) | honestly declared gap | `RETAINED_RISK`, no bound test |
| `U1-Q02` second unresolved quarantined round trip | **overclaimed binding** | bound to a test that does not perform its declared procedure — see `TD-R0-G9U1-Q02-BINDING` |

**Recommendation: close in this track (`R1-R1`), subject to `AD-R0-3`.** The
benefit is that the risk is cross-cutting and otherwise propagates into every
`G9B` family stage; the declared next gate already asks where it is resolved.
The cost is two additional test methods plus one manifest reconciliation, reusing
helpers that all exist. One genuine design decision is exposed: no
quarantine-observation surface is reachable from the Desktop module — the status
enum, the rebase entry and the typed accessor are private or package-private and
documented as exposed only to the resolver — so a closure test must either
repeat the existing hand parse of the serialized ledger or widen a deliberately
resolver-scoped kernel API. That widening is an author decision, not a refactor.
A cleaner, layout-independent style already exists elsewhere: asserting
ledger-state equality across a real save/reopen.

`PRE-G9B-R0` does not close this risk and does not move its canonical record.

## 3. Observations characterized by R0

| ID | Observation | Classification | Layer | Owning phase |
|---|---|---|---|---|
| `D1` | no intersection points materializable between `LocusV2 a` and `SplineV2 d` | `INTENTIONALLY_UNSUPPORTED_CURRENT_CAPABILITY` — **not a defect** | kernel (only if an extension is ever authorized); documentation has one bounded obligation | none; extension requires `AD-R0-1` |
| `D2` | `SplineV2` gives no contextual help, opens the generic input-help panel, and is awkward to construct | `MISSING_FRONTEND_EXPOSURE_AND_INCORRECT_CONTEXTUAL_HELP` | Desktop/frontend + localization; one qualified kernel question | `R4` |
| `D3-a` | `Midpoint(D, C + (1, 0))` rejected as ambiguous on redefine | `INCORRECTLY_REJECTED` | shared kernel | `R3` |
| `D3-b` | nine distinct failure causes collapse onto one `AMBIGUOUS` status | `INCORRECTLY_REPORTED` | kernel status typing + Desktop presentation | `R3` |

### 3.1 D1 — decisive persisted evidence

The witness's own serialized token ledgers settle this without inference. The
ledger state format is
`version|nextIncarnation|encode(current)|encode(copySource)`, and a snapshot
encodes as
`hex(owner)~hex(sourcePair)~hex(constructive)~hex(topology)~entryCount[~entries…]`.
The exporter selects the pair format version only when a pair binding exists.

| Rich result | Format version | `nextIncarnation` | Constructive lineage | Entry count |
|---|---|---|---|---|
| `e = Intersect(d, a)` (`LocusV2 × SplineV2`) | `4` — the **non-pair** version | `1` | `g9u0-public-locus-pair-intersection/v1` | **0**, copy-source snapshot absent |
| `g = Intersect(a, c)` (`LocusV2 × conic`) | `4` | `5` | `g9u0-public-intersection/v1` | **4** |

Because the exporter emits the pair format version if and only if a pair binding
exists, `e`'s version `4` is direct persisted proof that **no pair binding was
ever allocated**. Its `nextIncarnation` of `1` confirms that nothing was ever
allocated at all, while `g`'s `5` matches its four allocations — the four exact
tokens the author used for `I`, `J`, `K` and `L`. Independently, the two ledger
allocation prefixes differ, and the author's tokens name the current-root prefix
that the pair route structurally cannot produce.

An existing regression already pins this generic outcome by asserting that a
generic pair's ledger state does **not** start with the pair format version.

**Bounded unresolved hypothesis.** The decoded ledger proves zero allocations but
does not distinguish a `SUCCESS` result carrying discovered solutions with
not-established pair isolation from an `UNSUPPORTED` computation status, which
the pair solver returns when the fallback capability's finite-components
precondition fails. Both are consistent with the witness. This must be settled by
the first scenario of any future phase, not assumed. A related open question: on
the generic path the spline resolver returns before its diagnostics loop, so no
pair-selector class evidence is emitted at all.

### 3.2 D3 — the asymmetry, stated precisely

Publication and redefine assessment use **the same direct projection over the
parent algorithm's inputs, with opposite null handling**: publication *drops*
inputs that have no persistent identity; assessment *throws*. The stored
signature is produced by the dropping rule and compared against a candidate
signature produced by the throwing rule.

The witness proves the premise: `E`'s persisted identity record lists
`dependencies="geo:0ec40b8a…"` — `C` alone — because `D` has no identity and was
silently dropped at publication. `E`'s midpoint algorithm has two direct inputs,
`D` and the unlabeled helper from `C + (1, 0)`, neither participating, so
assessment throws on the first of them.

A naive "drop nulls on both sides" fix is insufficient: a second gate rejects any
dependency-set change for a non-public candidate, on both the retain inspection
and the replacement inspection the assessment already performs, so the result
would be `UNSUPPORTED` rather than a legacy offer. Only a projection that reaches
`C` transitively through the unregistered helper restores the stored `{C}`. That
transitive closure is a genuinely **new third rule** and must be adopted on both
sides together.

**Authority note.** ADR 0026 lists "Ignoring unregistered neighbours or replacing
their omission with a universal rejection" among its rejected alternatives, but
its context and corresponding decision item concern **procedural position and
surviving order**, not dependency-signature projection — and that item explicitly
sanctions failing closed when surviving order cannot be established. The ADR
supports the general principle; it does not directly adjudicate this path. The
classification rests on the code-level asymmetry. No document currently on disk
states the normative durable dependency projection rule; `R3` must write it.

## 4. New debt opened by R0

| ID | Statement | Layer | Owning phase | Blocking? |
|---|---|---|---|---|
| `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX` | the only contextual text the product shows for `SplineV2` advertises a degree-2 example that the command's own degree validation rejects, and mentions forms with no counterpart in the dispatch table | Desktop profile text | `R4`, or an authorized bounded correction (`AD-R0-5`) | no; actively misinforms users |
| `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY` | nine distinct `IllegalArgumentException` causes — seven in the construction-geo redefine provider and two in the registry's proposal description — collapse onto the single `AMBIGUOUS` assessment status, so a *correct* rejection such as a geo-family change is reported as ambiguity. The kernel already carries the provider's reason on the no-handler path; the desktop dialog discards it | kernel status typing + Desktop presentation | `R3` | no; separable from `D3-a` |
| `TD-R0-GGBSCRIPT-STRATEGY-RESTORE` | the script runner catches `Throwable` per line and only degrades success when the processor returned null, so an escaping throwable can leave a script reporting success; the same method restores the previous command lookup strategy at the end of the body, **outside any `finally`**, so an escaping throwable can also leave the kernel in the script strategy | shared kernel (script runner) | `R6` | no |
| `TD-R0-G9U1-Q02-BINDING` | scenario `U1-Q02` is bound to a test that does not perform its declared procedure — its manifest procedure requires reopening the native quarantined document **twice** while offset evidence remains insufficient, and the bound test performs exactly one quarantined round trip before resolving the offset | operational manifest / Desktop test | `R1-R1` | no; but it is an overclaimed coverage binding, a different kind of item from a declared gap |

## 5. Items explicitly **not** opened as debt

| Observation | Why it is not debt |
|---|---|
| no materialized points for `LocusV2 × SplineV2` | conformance with `spline-v2-pair-materialization.md` §1, `semantic-spline-2d.md` §5.2, ADR 0018 and ADR 0021 Decision 1. Any change is a new capability requiring a new ADR (`AD-R0-1`) |
| `SplineV2` has no tool icon | the owned SVG exists and is already bound to the action key by the desktop tool-image resource |
| `SplineV2` is absent from the toolbar | the action is already rendered in the construction-semantic-curves profile flyout; what is missing is mode semantics, not rendering |
| the desktop auto-materialize session behaviour | it is off by default and exists only on the explicit human Algebra path; it is a declared frontend asymmetry for `R6`'s matrix to record, and it does **not** explain the D1 witness |

## 6. Traceability

| Artifact | Path |
|---|---|
| staged design | [`docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md`](../architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md) |
| candidate report | [`docs/validation/pre_g9b_r0_characterization_candidate_report.md`](pre_g9b_r0_characterization_candidate_report.md) |
| machine-readable disposition | [`geocedg/validation/pre-g9b-r0/pre-g9b-r0-disposition.json`](../../geocedg/validation/pre-g9b-r0/pre-g9b-r0-disposition.json) |
| roadmap entry | [`docs/roadmap/geocedg_roadmap.md`](../roadmap/geocedg_roadmap.md) |
| proposed future prompts | **not created.** `CLAUDE.md` prohibits editing `.github/prompts/**` as a side effect of another task; changing the governance layer is its own author-authorized task. The per-phase contract fields that such stubs would carry are recorded in the staged design instead, so no authority is lost. See `AD-R0-9` |

```text
PRE-G9B-R0 = CANDIDATE PENDING AUTHOR REVIEW
selfApproved   = false
authorApproved = false
passClaimed    = false
```
