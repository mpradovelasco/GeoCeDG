# PRE-G9B-R6-plus-F2 author-decision and preparation closeout record

```text
ARTIFACT_KIND             = AUTHOR-DECISION RECORD AND DOCUMENTARY CLOSEOUT
RECORD VERSION            = 1.0
TASK                      = PRE-G9B-R6-plus-F2-PREP-CLOSEOUT
AUTHOR_DECISION_SOURCE    = author instruction of 2026-10-11 ("Author Decisions and
                            Design Reconciliation"; AUTHOR AUTHORIZED — AUTHOR-DECISION
                            RECONCILIATION AND DOCUMENTARY CLOSEOUT ONLY)
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

This record transcribes the author's decisions of 2026-10-11 on `DQ-F2-1` to
`DQ-F2-10`. It is their authority and prevails over the recommendations of the
preparation candidate wherever they differ. It authorizes **no implementation**
and **no publication**. Machine-readable mirror:
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
