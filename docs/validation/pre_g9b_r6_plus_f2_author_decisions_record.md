# PRE-G9B-R6-plus-F2 author-decision and preparation closeout record

```text
ARTIFACT_KIND             = AUTHOR-DECISION RECORD AND DOCUMENTARY CLOSEOUT
RECORD VERSION            = 1.1
VERSION HISTORY           = 1.0 (2026-10-11) PRE-G9B-R6-plus-F2-PREP-CLOSEOUT: DQ-F2-1 to
                                DQ-F2-10 recorded; design approved
                            1.1 (2026-10-11) PRE-G9B-R6-plus-F2: preparation approved and
                                published (PRE-G9B-R6-plus-F2-PREP-FINAL); implementation
                                authorized on 6d9bafb0; entry gate, OTQ-F2-1 to OTQ-F2-3
                                resolved and class frozen (§8); §1-§7 unchanged
TASKS                     = PRE-G9B-R6-plus-F2-PREP-CLOSEOUT; PRE-G9B-R6-plus-F2-PREP-FINAL;
                            PRE-G9B-R6-plus-F2
AUTHOR_DECISION_SOURCE    = author instruction of 2026-10-11 ("Author Decisions and
                            Design Reconciliation"; AUTHOR AUTHORIZED — AUTHOR-DECISION
                            RECONCILIATION AND DOCUMENTARY CLOSEOUT ONLY); author
                            instruction of 2026-10-11 ("Author Approval and Controlled
                            Documentary Publication"; PRE-G9B-R6-plus-F2-PREP-FINAL);
                            author instruction of 2026-10-11 ("PRE-G9B-R6-plus-F2
                            Implementation Authorization"; AUTHOR AUTHORIZED — BOUNDED
                            IMPLEMENTATION AND TECHNICAL VERIFICATION)
STATE AT VERSION 1.1      = PRE-G9B-R6-plus-F2-PREP = PASS — AUTHOR APPROVED — PUBLISHED
                            (chain 24985f14 -> 09bb6302 -> 6d9bafb0, non-force
                            fast-forward of main; remote main 6d9bafb0, tree 6bc65715)
                            PRE-G9B-R6-plus-F2 = DESIGN — AUTHOR APPROVED; CANONICAL PROMPT
                            AUTHORIZED FOR IMPLEMENTATION (§8)
                            IMPLEMENTATION = AUTHORIZED on 6d9bafb0 (bounded; local
                            candidate only)
                            VERIFICATION_CLASS = BOUNDED_PHASE — FROZEN at the entry gate (§8)
                            PUBLICATION = preparation chain published; implementation
                            publication NOT AUTHORIZED
PUBLISHED BASE            = P_F1 24985f14c565b9ba6282ec80fa0cb7be20a2e459
                            tree 1c683ebb3e303b7692915feae3417cf51e4cce22
PREPARATION CANDIDATE     = T_R6PLUS_F2_PREP 09bb63022c649cb6882440120aa121e8ecbb799f
                            tree 31d436733a71f626b412092515d23bcd87b30b0d
                            (STATIC verification-d61c1adb78414a2ab4608ddabfd078a7,
                            ACCEPTED / COMPLETE, 3/3); kept as the parent, not amended
CLOSEOUT CANDIDATE        = the commit that contains this record (direct child of
                            09bb6302); its identity and STATIC are reported outside
                            this record, which cannot name its own commit
PRE-G9B-R6-plus-F2        = DESIGN — AUTHOR APPROVED; CANONICAL PROMPT PREPARED — NOT AUTHORIZED
IMPLEMENTATION            = NOT AUTHORIZED
PUBLICATION               = NOT AUTHORIZED
selfApproved              = false
passClaimed               = false   (no product claim of any kind)
```

In the block above, the entries from `PUBLISHED BASE` to `passClaimed` record
the state at version 1.0; `STATE AT VERSION 1.1` supersedes them.

This record transcribes the author's decisions of 2026-10-11 on `DQ-F2-1` to
`DQ-F2-10`. It is their authority and prevails over the recommendations of the
preparation candidate wherever they differ. Version 1.0 authorized **no
implementation** and **no publication**; version 1.1 records the later
implementation authorization and the entry gate (§8), and leaves §1–§7
unchanged. Machine-readable mirror:
[`pre-g9b-r6-plus-f2-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f2-author-decisions.json).
The reconciled design is
[`pre_g9b_r6_plus_f2_authoring_memory_reconciled_design_candidate.md`](../architecture/pre_g9b_r6_plus_f2_authoring_memory_reconciled_design_candidate.md)
(`DESIGN — AUTHOR APPROVED`); the execution contract is the
[canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f2-authoring-memory.prompt.md)
(`PREPARED — NOT AUTHORIZED`). The
[characterization report](pre_g9b_r6_plus_f2_preparation_characterization_report.md),
its JSON mirror and the
[author-decision preparation record](pre_g9b_r6_plus_f2_author_decision_preparation_record.md)
are frozen preparation evidence and are not edited; corrections and
supplements found during this reconciliation are listed in §5. The `P0`
[F2 design candidate](../architecture/pre_g9b_r6_plus_f2_authoring_memory_design_candidate.md)
stays unchanged as historical evidence.

## Classification vocabulary

| Tag | Meaning |
|---|---|
| **AUTHOR DECISION** | given by the author on 2026-10-11; authority of this record |
| **NORMATIVE INHERITANCE** | already fixed by an approved specification, ADR or earlier author decision; cited, not re-decided |
| **CHARACTERIZATION EVIDENCE** | read or measured at the base (report `K0`–`K16`) |
| **IMPLEMENTATION DESIGN** | resolution by this reconciliation inside the decided scope; reviewable by the author; not an author decision |
| **OPEN TECHNICAL ITEM** | open; listed with whether it blocks implementation authorization (§4) |

No agent recommendation is recorded here as an author approval.

## 1. Decisions

### `DQ-F2-1` — memory across tool changes

- Recommendation of the preparation: survive tool changes (P1).
- **AUTHOR DECISION:** the parallel and perpendicular reference memories
  survive ordinary tool changes within the same application document session.
  They are not serialized.
- IMPLEMENTATION DESIGN: the memory is not reset in the GeoCeDG controller's
  `setMode`; Escape (`setMoveMode`) is an ordinary tool change. The pending
  upstream selection is still discarded as today (report `K4` T08).

### `DQ-F2-2` — independent slots

- Recommendation: two slots, replaced on successful creation (S1).
- **AUTHOR DECISION:** two independent slots, one for parallel and one for
  perpendicular constructions. A newly selected admissible reference replaces
  the relevant slot only after a successful construction. Cancellation or an
  unsuccessful construction never replaces a previously valid slot.

### `DQ-F2-3` — first-use behavior

- Recommendation: point-first includes empty space; #2610 unchanged (G1).
- **AUTHOR DECISION:** with a valid remembered reference, a click on an
  existing point or on empty space may start a new construction using that
  reference. With no reference remembered, an explicit reference selection is
  required before the tool constructs. The implementation never invents a
  reference or creates unintended geometry. The inherited point-on-path
  gesture is characterized and preserved; selection is not pre-seeded at mouse
  press where that causes the known conflict. Release-time injection may be
  used if the actual tool flows prove it correct.
- CHARACTERIZATION EVIDENCE: with an empty selection at press, the issue
  #2610 gesture and the preview are unaffected (report `K6`, interception I1;
  probe T04, T15); an empty-space press creates the free point that the
  release adds to the hits (EC:10499-10501; T06).
- IMPLEMENTATION DESIGN: release-time injection (design §4); its correctness
  is an obligation of the implementation tests (`T-F2-2610-UNCHANGED`,
  `T-F2-PREVIEW-NO-CREATION`, `T-F2-EMPTY-SPACE`), not a claim of this record.

### `DQ-F2-4` — eligible references

- Recommendation: accepted references with a labelled 2D output (R1).
- **AUTHOR DECISION:** remembered references are limited to the supported 2D
  types established by the characterization: lines, segments, rays, polygon
  edges, the appropriate 2D vectors and linear functions. The reference
  recorded is the one actually used by the successful kernel algorithm, never
  an inferred one. A resulting object's label is not the sole certificate of a
  successful construction. Inherited behavior for unsupported or 3D inputs is
  not modified; remembered-reference semantics are simply not extended to
  them in this version.
- Difference from the preparation: R1 named "a labelled output" as the
  criterion; it is replaced by the success certificate below.
- IMPLEMENTATION DESIGN — success certificate (all required): the call is not a
  preview; the inherited process step returned a created line; that line is an
  exact-instance member of the current construction order; it is defined; its
  parent algorithm is exactly one of `AlgoLinePointLine`, `AlgoLinePointVector`
  (parallel) or `AlgoOrthoLinePointLine`, `AlgoOrthoLinePointVector`
  (perpendicular); and the reference is that algorithm's `input[1]`.
- IMPLEMENTATION DESIGN — admissibility of the reference: a 2D (non-3D)
  `GeoLine` (line, segment, ray, polygon edge), a 2D `GeoVector`, or a
  `GeoFunction` whose kernel linear interpretation is defined — the
  `Lineable2D.getX()` of `GeoFunction` returns the gradient only for degree
  ≤ 1 (`common/kernel/geos/GeoFunction.java:2895-2913`), which is the same
  route `AlgoLinePointLine` consumes. Parallel 3D vectors and every 3D input
  keep their inherited behavior and are never remembered.

### `DQ-F2-5` — identity and lifecycle

- Recommendation: identity scan; suspend undefined or hidden; lazy plus
  document-boundary clearing; undo forgets.
- **AUTHOR DECISION:** the memory holds an existing exact object reference
  only. Currentness is verified by exact-instance membership in the current
  construction, using the construction-order mechanism established by the
  characterization. A label lookup may only corroborate the same instance; it
  never resolves a replacement or restores identity from a matching label.
  Affected memory is cleared when its object is removed, replaced or becomes
  stale. The session memories are cleared on New, Open, full document
  replacement and undo/redo rebuilds. Valid memory is preserved across
  ordinary renames and recomputations that keep the same instance. Temporary
  invalidity or invisibility suspends reuse. An inadmissible reference is never
  used implicitly. No durable IDs, kernel identity infrastructure or document
  associations are introduced.
- Difference from the preparation: undo/redo now **clear** eagerly (the
  preparation relied on lazy staleness); the observable behavior — no reuse
  after an undo or redo — is the same.
- CHARACTERIZATION EVIDENCE: report `K8` (L01–L14), `K9`: a client
  `EventListener.reset()` is called on undo, redo, Open and New; delete arrives
  as `REMOVE`, redefinition as `REDEFINE` with the new object; undo invalidates
  without a `REMOVE`.
- IMPLEMENTATION DESIGN: eager clearing at every construction rebuild and
  document boundary (`OTQ-F2-1` fixes the hook), plus the lazy currentness
  predicate at every use, which clears a stale slot whatever path made it
  stale.

### `DQ-F2-6` — preview, cancellation and undo

- Recommendation: release-time seeding, no preview, no indicator (C1).
- **AUTHOR DECISION:** no additional preview or reference-memory indicator in
  v1. Preview never updates memory. Memory updates only on a successful
  committed construction. Remembering introduces no undo point; existing
  geometric undo behavior is unchanged.

### `DQ-F2-7` — Angle with Given Size hook

- Recommendation: GeoCeDG companion `createAngle` override (H1).
- **AUTHOR DECISION:** adopt the narrow GeoCeDG controller-companion override
  of `createAngle(GeoPointND, GeoPointND, GeoNumberValue, boolean)`; verify its
  call sites and restrict the visibility change to the approved tool-specific
  creation route; hide the resulting angle before the inherited undo state is
  stored. `AlgoDispatcher.angle`, `Angle(...)`, GGBScript and shared kernel
  algorithms are not modified.
- CHARACTERIZATION EVIDENCE: the only non-test callers of the four-argument
  `createAngle` are `DialogManager.doAngleFixed` (`DialogManager.java:163,166`),
  which stores undo after it returns (`:170`) (report `K10`).
- IMPLEMENTATION DESIGN: the override hides only when its controller is in
  `MODE_ANGLE_FIXED` (defense in depth); the implementation entry gate
  re-establishes the call-site census.

### `DQ-F2-8` — angle visibility and persistence

- Recommendation: hidden in the graphics views only; ordinary persistence (V1).
- **AUTHOR DECISION:** the generated angle is hidden in the graphical views by
  the ordinary object visibility mechanism; it stays a valid dependent object
  and accessible in the Algebra View. The rotated point stays visible. The user
  may show the angle later. Existing save, reload, undo and redo semantics
  apply. The new tool behavior is intercepted only in GeoCeDG Graphics View 1;
  the created angle's ordinary visibility property is not an additional
  per-view session overlay. The Graphics View 2 tool controller and Classic are
  not modified.
- Consequence (IMPLEMENTATION DESIGN): an angle created through GV1 is hidden
  in every graphics view, because Euclidian visibility is one object property;
  an angle created with the tool in GV2 stays visible (inherited behavior).

### `DQ-F2-9` — working-layer direct entry

- Recommendation: keep independent (K).
- **AUTHOR DECISION:** `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` remains a
  separate pending enhancement, not implemented in F2; its tracking and
  authorization status are preserved (`ENHANCEMENT — NOT A-2 BLOCKING —
  IMPLEMENTATION NOT AUTHORIZED`).

### `DQ-F2-10` — architecture and verification

- Recommendation: GV1 only, `BOUNDED_PHASE` (A1).
- **AUTHOR DECISION:** Desktop-only orchestration and a GeoCeDG-owned
  controller companion are approved. Proposed future implementation class
  `BOUNDED_PHASE`, one registered `PHASE`, subject to the implementation entry
  gate. No change to shared kernel algorithms, command behavior, XML schema,
  global construction identity or inherited source classes is authorized. If
  the required seams are not available within GeoCeDG-owned code, the work
  stops for author review and classification.

## 2. Distinctions kept by the design

- **Controller scope versus object property.** The memory and the
  angle-hiding interception live in the GeoCeDG Graphics View 1 controller; the
  hidden state of an angle is the ordinary, persisted object visibility shared
  by every view.
- **Reference identity versus label.** Only the exact held instance is ever
  reused; a label only corroborates it.
- **Kernel semantics.** Every created object is the one the unchanged upstream
  tool or algorithm creates for the same point and reference.

## 3. Compatibility check of the decisions with the characterized paths

| Decision | Characterized path | Result |
|---|---|---|
| `DQ-F2-1`, `-2`, `-6` | slot field outside `setMode` reset; read-back after `super` (GCEC:688) | compatible |
| `DQ-F2-3` | release-time injection with an empty press-time selection (`K6` I1) | compatible, to be proven by tests |
| `DQ-F2-4` | `input[1]` of the four 2D algorithms; `GeoFunction.getX()` linearity | compatible |
| `DQ-F2-5` | exact-instance membership; `reset()`, `REMOVE`, `REDEFINE`; lazy check | compatible |
| `DQ-F2-7`, `-8` | companion `createAngle` before `doAngleFixed` undo store; ordinary `<show>` | compatible |
| `DQ-F2-9` | — | not in scope |
| `DQ-F2-10` | all seams in GeoCeDG-owned classes (`K7`, `K10`) | compatible; no inherited change identified |

No decision is incompatible with the characterized code. **No technical
blocker** is open; feasibility is not claimed beyond the characterization.

## 4. Open technical items

| Id | Item | Blocks implementation authorization? |
|---|---|---|
| `OTQ-F2-1` | Eager-clearing hook for undo/redo and other rebuilds: a GeoCeDG-owned client `EventListener` whose `reset()` clears both slots (proven to fire on undo, redo, Open and New) or the existing `AppGeoCeDG` document-boundary overrides plus an undo listener; fixed at the entry gate by which needs no inherited change; the lazy predicate is required either way | no |
| `OTQ-F2-2` | Read-back route: `getJustCreatedGeos()` after `super`, or a wrapped release callback (GCEC intersect precedent); fixed at the entry gate | no |
| `OTQ-F2-3` | Exact "visible in GV1" suspension predicate (Euclidian visibility, view flags, layer shown); fixed in the specification | no |

## 5. Corrections and supplements to the frozen preparation evidence

| Id | Location | Correction or supplement |
|---|---|---|
| `ER-F2-10` | decision preparation record `DQ-F2-4` R1; design (preparation) §5 | "labelled output" replaced by the success certificate of §1 `DQ-F2-4` |
| `ER-F2-11` | design (preparation) §3.3, §6 | undo/redo now clear the memory eagerly (`DQ-F2-5`) |
| `ER-F2-12` | report `K5` | supplement: function linearity is read through `GeoFunction.getX()` (`GeoFunction.java:2895-2913`), finite only for degree ≤ 1 |

## 6. Preparation closeout

| Item | State |
|---|---|
| preparation candidate | `09bb63022c649cb6882440120aa121e8ecbb799f`, tree `31d436733a71f626b412092515d23bcd87b30b0d`, direct child of `P_F1` `24985f14`, seven files, worktree clean |
| preparation STATIC | `verification-d61c1adb78414a2ab4608ddabfd078a7`, `ACCEPTED / COMPLETE` 3/3, bound to that commit and tree; standing diagnostics only (`diagnostic.governance` finding, `diagnostic.historical-consistency` unavailable); not reused for the closeout commit |
| `P0` F2 design candidate | unchanged since the base |
| characterization | `COMPLETE`, frozen |
| decisions | `DQ-F2-1` to `DQ-F2-10` recorded (§1) |
| reconciled design | `DESIGN — AUTHOR APPROVED` (the design only) |
| canonical prompt | reconciled; `PREPARED — NOT AUTHORIZED` |
| specification / ADR | none promoted; the specification is the first deliverable of an authorized F2 |
| working-layer enhancement | unchanged, separate, not authorized |
| planned implementation class | `BOUNDED_PHASE`, author-accepted, conditioned on the entry gate |
| closeout class | `DOCUMENTATION_STATUS_ONLY`, acceptance `STATIC` |
| implementation, publication | `NOT AUTHORIZED` |

## 7. Next decision

After review of this documentary closeout, the author decides whether to
authorize and publish the F2 preparation package (preparation candidate and
this closeout commit), and then, separately, whether to authorize the F2
implementation against an exact published base. `F3`, `F4` and `G` are
unchanged and unauthorized.

```text
AUTHOR_DECISION          = RECORDED IN THIS ARTIFACT (DQ-F2-1 to DQ-F2-10, 2026-10-11)
DESIGN                   = DESIGN — AUTHOR APPROVED
CANONICAL PROMPT         = PREPARED — NOT AUTHORIZED
IMPLEMENTATION           = NOT AUTHORIZED
PUBLICATION              = NOT AUTHORIZED
selfApproved             = false
```

The block above is the state at version 1.0.

## 8. Preparation publication, implementation authorization and entry gate (v1.1, `PRE-G9B-R6-plus-F2`)

**Preparation publication (author instruction "Author Approval and Controlled
Documentary Publication", `PRE-G9B-R6-plus-F2-PREP-FINAL`, 2026-10-11).** The
author approved the F2 preparation and `DQ-F2-1` to `DQ-F2-10`. The exact chain
`24985f14` → `09bb63022c649cb6882440120aa121e8ecbb799f` →
`6d9bafb04235a71c8dfadef9631cd6057964b1f9` was published by an ordinary
non-force fast-forward of `main`; remote `main` = `6d9bafb0`, tree
`6bc65715cd37f5872b804eea7949d2f9fea96092`. GitHub reported its standing
"Bypassed rule violations" notice (pull request required; locked branch).
Preparation STATIC `verification-d61c1adb78414a2ab4608ddabfd078a7` and
closeout STATIC `verification-6faf76297e6c4b86b1f8b9080060ef5d`, both
`ACCEPTED / COMPLETE` on their exact commits. `PRE-G9B-R6-plus-F2-PREP` =
`PASS — AUTHOR APPROVED — PUBLISHED`. That instruction named `OTQ-F2-1` to
`OTQ-F2-3` and the linear-function domain as implementation-entry checks.

**AUTHOR DECISION (implementation authorization, 2026-10-11):** implementation
and technical verification of `DQ-F2-1` to `DQ-F2-10`, without change of meaning
or scope, are authorized on the published base `6d9bafb0` (tree `6bc65715`), on
a local phase branch, up to one frozen local technical candidate for author
review and author smoke. Publication, author approval of the implementation,
`ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`, `F3`, `F4` and `G` are not authorized.
The canonical prompt is amended to the authorized state as the first tracked
change of the phase; the durable specification
`geocedg/specs/ui/authoring-memory.md` (`PROPOSED`) precedes production code.

**Entry gate (2026-10-11).** Local `main`, `origin/main` and the live remote
`main` resolve to `6d9bafb0`; the worktree is clean; the frozen preparation
evidence is unchanged since `09bb6302`; no F2 code exists (search of
`source/desktop/desktop/src/main/java/org/geocedg` for
`GeoCeDGLineReferenceMemory`, `newCompanion`, `MODE_ANGLE_FIXED`,
`MODE_PARALLEL`: no match). Branch `phase/pre-g9b-r6-plus-f2` (local) from
`6d9bafb0`. The gate questions were resolved by source reading and one
scratch-only probe (`F2GateProbeTest`, three methods, 0 failures; Desktop test
source set through a Gradle init script, removed afterwards; outputs
`reset` `d022c6aa1040f93974d1b5fa6632bc9d17ddb36015d48113c57426e2bc5eb361`,
`readback` `4d532bf21693433f61505b84521b7c94a068bc5cde31248092c8371838923e61`,
`domain` `c70b377d56de591beb707837b007079b42eafc98b4acced116ede3c4dd26fd0f`,
probe source `66db992e31ea7048faf8d067e306c7508b8d3282c3a52113fcd6f42c68533f0b`).

### 8.1 `OTQ-F2-1` — immediate invalidation (RESOLVED)

- CHARACTERIZATION EVIDENCE (source): `EventDispatcher` is a kernel `View`;
  its `clearView()` calls `reset()` on every registered client
  `EventListener` (`common/plugin/EventDispatcher.java:256-263`).
  `Kernel.notifyClearView` runs from `Kernel.clearConstruction` (`Kernel.java:3080`)
  and when view notification is switched off (`:3703`), which only
  `MyXMLio.doParseXML` does, for zooming XML loads (`MyXMLio.java:419-421`, call at `:420`);
  undo, redo, Open, document replacement and a rebuilding redefinition all load
  XML this way.
- CHARACTERIZATION EVIDENCE (probe, `reset` census on a held line): `reset()`
  fires on undo (2), redo (2), redefinition of an object with children (2,
  every object replaced), clearing `setXML` (2), `loadXML` (the document-open
  route, 2) and File → New (1); it does **not** fire on store-undo, mode
  changes, recomputation, hide/show, layer hiding, language change, creation
  of Graphics View 2, non-clearing `evalXML` or non-clearing `setXML`. A plain
  redefinition sends `REDEFINE` with the new object (held instance no longer
  current); deletion sends `REMOVE`.
- IMPLEMENTATION DESIGN (decided by this gate): a GeoCeDG-owned client
  `EventListener` of the memory helper, registered on
  `app.getEventDispatcher()` at the first slot write (not in the controller
  constructor, so application start-up and view-attachment order are
  unchanged): `reset()`, `UNDO` and `REDO` clear both slots; `REMOVE` and
  `REDEFINE` re-validate both slots (a non-current slot is cleared). The
  exact-instance currentness check runs before every implicit reuse
  regardless. No inherited class is changed; no label, `isInConstructionList`
  or `isLabelSet` is used.

### 8.2 `OTQ-F2-2` — read-back of the reference actually used (RESOLVED)

- Source: `input[0]` is the point and `input[1]` the reference in
  `AlgoLinePointLine` (`setInputOutput`, `input[1] = l.toGeoElement()`),
  `AlgoOrthoLinePointLine` (`setInput`), `AlgoLinePointVector` (`:92-93`) and
  `AlgoOrthoLinePointVector` (`:91-92`).
- Probe (`readback`, both tools × line, segment, ray, polygon edge, vector,
  linear function): every admitted route creates one labelled, defined line,
  an exact-instance member of the construction, whose algorithm is the
  expected class with `input[0]` = the clicked point and `input[1]` = the
  clicked reference (12 of 12).
- Success detection: `endOfSwitchModeForProcessMode` memorizes the returned
  output and, on a non-preview step without output, **clears** the
  just-created list (`EuclidianController.java:5466-5485`). After the GeoCeDG
  override's `super.switchModeForProcessMode` returns `true` for a non-preview
  call, `getJustCreatedGeos()` therefore holds this step's output only.
- Non-null output is not success: for non-linear, constant and
  non-polynomial functions the tool returns an unlabelled, undefined line that
  is not a construction member (probe). IMPLEMENTATION DESIGN: the certificate
  of design §4.3 (one created line; exact-instance member; defined; algorithm
  class; `input[1]` admissible) is applied to that list.

### 8.3 `OTQ-F2-3` — effective GV1 usability (RESOLVED)

- IMPLEMENTATION DESIGN: usable = current (exact-instance membership) ∧
  admissible now ∧ `isDefined()` ∧ `isEuclidianVisible()` ∧
  `isVisibleInView(GV1 view id)` ∧ `app.isLayerShown(getLayer())` — the
  visibility conjunction is the one already used by the approved F1 cue model
  (`GeoCeDGOrientationCue.java:130-132`), so the two GV1 features share one
  definition of "visible in GV1", including A-2 hidden layers.
- Probe (`domain`): each component switches independently (hidden object,
  object shown only in GV2, layer hidden, layer shown again) while the
  instance stays a member: these are `SUSPENDED`, never `EMPTY`; suspension
  never binds another object.

### 8.4 Linear-function domain (VERIFIED)

`GeoFunction.getX()` (`GeoFunction.java:2895-2913`) expands the function to a
polynomial and returns the gradient only for degree ≤ 1; `getY()` is −1.
`AlgoLinePointLine` and `AlgoOrthoLinePointLine` compute from `getX()`/`getY()`.

| Function | `getX()` | parallel / perpendicular line |
|---|---|---|
| `2x + 1`, `x`, `(x+1)² − x²` | 2, 1, 2 | defined |
| `a x² + x` with `a = 0` / `a = 1` | 1 / NaN | defined / undefined |
| constant `3`, `0`, `0x² + 5` | NaN | **undefined** |
| `x²`, `sin(x)`, `sqrt(x)`, `abs(x)`, `If(x > 0, x, 2x)` | NaN | undefined |

**Limitation (reported, not broadened):** constant functions are not lineable
through the kernel route: the degree-0 polynomial has no `getCoeffs()[1]`, so
`getX()` is NaN and the inherited tools create no line from them today. The
function-reference predicate is therefore exactly the kernel contract —
`GeoFunction` (non-3D) with a finite `getX()` — and constant functions are
neither remembered nor reused. A function whose expression stops being
lineable (e.g. `a` from 0 to 1) suspends its slot.

### 8.5 Readiness and frozen class

| # | Condition | Finding |
|---|---|---|
| 1 | memory and injection | GeoCeDG-owned: a new helper and the existing GCEC `switchModeForProcessMode` override (before/after its `super` call, GCEC:688), using the protected `selection` field and the public `SelectionManager.addToSelectionList`; `parallel(Hits, boolean)` untouched |
| 2 | angle hook | GCEC overrides the protected `newCompanion()` with a GeoCeDG subclass of the public, non-final `EuclidianControllerFor3DCompanion` overriding the public `createAngle(GeoPointND, GeoPointND, GeoNumberValue, boolean)` |
| 3 | invalidation | client `EventListener` through public `EventDispatcher` API (§8.1) |
| 4 | no shared, kernel, command, algorithm, XML, identity, GV2 or Classic change | confirmed |
| 5 | phase selection | `PRE-G9B-R6-PLUS-F2`: `compile.desktop.semantic` and `junit.desktop.pre-g9b-r6-plus-f2.semantic` (selection `pre-g9b-r6-plus-f2.desktop`: the F2 test class and the controller-regression classes of the other GeoCeDG tool branches) |

```text
VERIFICATION_CLASS       = BOUNDED_PHASE — FROZEN (2026-10-11)
F2 IMPLEMENTATION        = AUTHORIZED (local technical candidate only)
F2 PUBLICATION           = NOT AUTHORIZED
selfApproved             = false
```
